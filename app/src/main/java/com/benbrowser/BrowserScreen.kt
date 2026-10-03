package com.benbrowser

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.webkit.WebSettingsCompat
import androidx.webkit.WebViewFeature
import com.benbrowser.theme.BgCanvas
import com.benbrowser.ui.BrowserViewModel
import com.benbrowser.ui.components.AppleBottomBar
import com.benbrowser.ui.components.AppleStartPage
import com.benbrowser.ui.components.BookmarksSheet
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun BrowserScreen(
    viewModel: BrowserViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    val hazeState = rememberHazeState()
    var webViewInstance by remember { mutableStateOf<WebView?>(null) }

    // Synchronize URL navigation when ViewModel state changes
    LaunchedEffect(state.currentUrl, state.isStartPage) {
        if (!state.isStartPage && state.currentUrl.isNotBlank()) {
            webViewInstance?.let { view ->
                if (view.url != state.currentUrl) {
                    view.loadUrl(state.currentUrl)
                }
            }
        }
    }

    // System Back Handler: edit mode -> webView history -> start page -> exit
    BackHandler(enabled = !state.isStartPage || state.canGoBack || state.isEditingUrl) {
        if (state.isEditingUrl) {
            viewModel.setEditingUrl(false)
        } else if (webViewInstance?.canGoBack() == true) {
            webViewInstance?.goBack()
        } else {
            viewModel.openStartPage()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgCanvas)
    ) {
        // Live Web Content with Status Bar & Navigation Bar Insets Protection
        Box(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(bottom = 72.dp)
        ) {
            AndroidView(
                factory = { ctx ->
                    WebView(ctx).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )

                        setBackgroundColor(android.graphics.Color.parseColor("#0B0C0E"))
                        if (WebViewFeature.isFeatureSupported(WebViewFeature.ALGORITHMIC_DARKENING)) {
                            WebSettingsCompat.setAlgorithmicDarkeningAllowed(settings, true)
                        } else if (WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK)) {
                            @Suppress("DEPRECATION")
                            WebSettingsCompat.setForceDark(settings, WebSettingsCompat.FORCE_DARK_ON)
                        }

                        // Enable cookies
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
                            // Remove '; wv' so search engines and web apps don't reject WebView
                            userAgentString = userAgentString.replace("; wv", "")
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
                                viewModel.onPageFinished(url)
                                viewModel.updateNavigationState(
                                    canGoBack = view?.canGoBack() ?: false,
                                    canGoForward = view?.canGoForward() ?: false
                                )
                            }

                            override fun doUpdateVisitedHistory(view: WebView?, url: String?, isReload: Boolean) {
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
                                        val intent = Intent(Intent.ACTION_VIEW, request.url)
                                        view?.context?.startActivity(intent)
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

                        if (state.currentUrl.isNotBlank()) {
                            loadUrl(state.currentUrl)
                        }

                        webViewInstance = this
                    }
                },
                update = { view ->
                    webViewInstance = view
                    if (!state.isStartPage && state.currentUrl.isNotBlank() && view.url != state.currentUrl) {
                        view.loadUrl(state.currentUrl)
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        }

        // Safari Start Page (Favorites, Quick Links) captured by hazeSource when visible
        AnimatedVisibility(
            visible = state.isStartPage,
            enter = fadeIn(animationSpec = tween(250)),
            exit = fadeOut(animationSpec = tween(200)),
            modifier = Modifier
                .fillMaxSize()
                .hazeSource(state = hazeState)
        ) {
            AppleStartPage(
                bookmarks = state.bookmarks,
                onSelectBookmark = { url ->
                    viewModel.loadUrl(url)
                },
                onSearchClick = {
                    viewModel.setEditingUrl(true)
                },
                modifier = Modifier.fillMaxSize()
            )
        }

        // Apple Safari Floating Liquid Glass Bottom Bar
        AppleBottomBar(
            state = state,
            hazeState = hazeState,
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
            onOpenBookmarks = {
                viewModel.setShowBookmarksSheet(true)
            },
            onEditUrl = { editing ->
                viewModel.setEditingUrl(editing)
            },
            modifier = Modifier.align(Alignment.BottomCenter)
        )

        // Apple Modal Bookmarks Sheet
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
}
