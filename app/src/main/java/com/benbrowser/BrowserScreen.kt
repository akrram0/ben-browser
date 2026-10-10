package com.benbrowser

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.view.View
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.webkit.WebSettingsCompat
import androidx.webkit.WebViewFeature
import com.benbrowser.theme.BgCanvas
import com.benbrowser.ui.BrowserViewModel
import com.benbrowser.ui.components.AppleBottomBar
import com.benbrowser.ui.components.AppleStartPage
import com.benbrowser.ui.components.BookmarksSheet

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun BrowserScreen(
    viewModel: BrowserViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    var webViewInstance by remember { mutableStateOf<WebView?>(null) }

    // Dispatch user-initiated URL loads once without interrupting in-page redirects
    LaunchedEffect(state.pendingUrl, webViewInstance) {
        val targetUrl = state.pendingUrl
        val view = webViewInstance
        if (!targetUrl.isNullOrBlank() && view != null) {
            view.loadUrl(targetUrl)
            viewModel.onUrlLoadConsumed()
        }
    }

    // Clear old webpage when returning to Start Page so next navigation never flashes stale content
    LaunchedEffect(state.isStartPage) {
        if (state.isStartPage) {
            webViewInstance?.stopLoading()
            webViewInstance?.loadUrl("about:blank")
        }
    }

    // System Back Handler: edit mode -> webView history -> start page -> exit
    BackHandler(enabled = !state.isStartPage || state.canGoBack || state.isEditingUrl) {
        when {
            state.isEditingUrl -> viewModel.setEditingUrl(false)
            webViewInstance?.canGoBack() == true -> webViewInstance?.goBack()
            else -> viewModel.openStartPage()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgCanvas)
    ) {
        // Main Viewport (WebView + Start Page Overlay)
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .statusBarsPadding()
        ) {
            AndroidView(
                factory = { ctx ->
                    WebView(ctx).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        setLayerType(View.LAYER_TYPE_HARDWARE, null)
                        setBackgroundColor(android.graphics.Color.parseColor("#0B0C0E"))

                        if (WebViewFeature.isFeatureSupported(WebViewFeature.ALGORITHMIC_DARKENING)) {
                            WebSettingsCompat.setAlgorithmicDarkeningAllowed(settings, true)
                        } else if (WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK)) {
                            @Suppress("DEPRECATION")
                            WebSettingsCompat.setForceDark(settings, WebSettingsCompat.FORCE_DARK_ON)
                        }

                        val cookieManager = CookieManager.getInstance()
                        cookieManager.setAcceptCookie(true)
                        cookieManager.setAcceptThirdPartyCookies(this, true)

                        settings.apply {
                            javaScriptEnabled = true
                            domStorageEnabled = true
                            databaseEnabled = true
                            setSupportZoom(true)
                            builtInZoomControls = true
                            displayZoomControls = false
                            useWideViewPort = true
                            loadWithOverviewMode = true
                            cacheMode = WebSettings.LOAD_DEFAULT
                            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                            userAgentString = userAgentString.replace("; wv", "")
                        }

                        // Safari scroll-to-minimize toolbar behavior (deduplicated)
                        setOnScrollChangeListener { _, _, scrollY, _, oldScrollY ->
                            val dy = scrollY - oldScrollY
                            if (dy > 20 && scrollY > 140) {
                                viewModel.setBarMinimized(true)
                            } else if (dy < -20 || scrollY < 40) {
                                viewModel.setBarMinimized(false)
                            }
                        }

                        webViewClient = object : WebViewClient() {
                            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                                viewModel.onPageStarted(url)
                                viewModel.updateNavigationState(
                                    canGoBack = view?.canGoBack() ?: false,
                                    canGoForward = view?.canGoForward() ?: false
                                )
                            }

                            override fun onPageFinished(view: WebView?, url: String?) {
                                if (url == "about:blank") {
                                    view?.clearHistory()
                                    viewModel.updateNavigationState(canGoBack = false, canGoForward = false)
                                    return
                                }
                                viewModel.onPageFinished(url)
                                viewModel.updateNavigationState(
                                    canGoBack = view?.canGoBack() ?: false,
                                    canGoForward = view?.canGoForward() ?: false
                                )
                            }

                            override fun doUpdateVisitedHistory(view: WebView?, url: String?, isReload: Boolean) {
                                if (url == "about:blank") return
                                viewModel.updateNavigationState(
                                    canGoBack = view?.canGoBack() ?: false,
                                    canGoForward = view?.canGoForward() ?: false
                                )
                            }

                            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                                val url = request?.url?.toString() ?: return false
                                return if (url.startsWith("http://") || url.startsWith("https://")) {
                                    false
                                } else {
                                    try {
                                        view?.context?.startActivity(Intent(Intent.ACTION_VIEW, request.url))
                                        true
                                    } catch (_: Exception) {
                                        true
                                    }
                                }
                            }
                        }

                        webChromeClient = object : WebChromeClient() {
                            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                                viewModel.onProgressChanged(newProgress)
                            }

                            override fun onReceivedTitle(view: WebView?, title: String?) {
                                viewModel.onReceivedTitle(title)
                            }
                        }

                        webViewInstance = this
                    }
                },
                update = { view ->
                    webViewInstance = view
                },
                modifier = Modifier.fillMaxSize()
            )

            // Safari Start Page Overlay (fast 120ms fade, no flash)
            androidx.compose.animation.AnimatedVisibility(
                visible = state.isStartPage,
                enter = fadeIn(animationSpec = tween(120)),
                exit = fadeOut(animationSpec = tween(120)),
                modifier = Modifier.fillMaxSize()
            ) {
                AppleStartPage(
                    bookmarks = state.bookmarks,
                    onSelectBookmark = { url ->
                        viewModel.loadUrl(url)
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        // Docked iOS Safari Bottom Bar
        AppleBottomBar(
            state = state,
            onBack = {
                if (webViewInstance?.canGoBack() == true) {
                    webViewInstance?.goBack()
                } else {
                    viewModel.openStartPage()
                }
            },
            onForward = {
                webViewInstance?.goForward()
            },
            onReload = {
                webViewInstance?.reload()
            },
            onSubmitUrl = { input ->
                viewModel.loadUrl(input)
            },
            onToggleBookmark = {
                viewModel.toggleBookmark()
            },
            onOpenBookmarks = {
                viewModel.setShowBookmarksSheet(true)
            },
            onOpenStartPage = {
                viewModel.openStartPage()
            },
            onEditUrl = { editing ->
                viewModel.setEditingUrl(editing)
            },
            onRestoreBar = {
                viewModel.setBarMinimized(false)
            }
        )
    }

    // Apple Safari Bookmarks Modal Sheet
    if (state.showBookmarksSheet) {
        BookmarksSheet(
            bookmarks = state.bookmarks,
            isCurrentBookmarked = state.isCurrentBookmarked,
            currentUrl = state.currentUrl,
            onToggleBookmark = {
                viewModel.toggleBookmark()
            },
            onOpenStartPage = {
                viewModel.openStartPage()
            },
            onSelectBookmark = { url ->
                viewModel.loadUrl(url)
            },
            onDeleteBookmark = { id ->
                viewModel.removeBookmark(id)
            },
            onDismiss = {
                viewModel.setShowBookmarksSheet(false)
            }
        )
    }
}
