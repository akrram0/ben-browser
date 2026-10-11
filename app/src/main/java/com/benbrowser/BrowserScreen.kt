package com.benbrowser

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.os.SystemClock
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
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
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
    // Draw-phase-only tick to invalidate HazeSource when WebView scrolls/repaints (0 recompositions)
    val webViewDrawTick = remember { mutableIntStateOf(0) }

    // Dispatch user-initiated URL loads once without interrupting in-page redirects
    LaunchedEffect(state.pendingUrl, webViewInstance) {
        val targetUrl = state.pendingUrl
        val view = webViewInstance
        if (!targetUrl.isNullOrBlank() && view != null) {
            view.visibility = View.VISIBLE
            view.loadUrl(targetUrl)
            viewModel.onUrlLoadConsumed()
        }
    }

    // Remove WebView from render/layout tree while on Start Page so it never lags animations
    LaunchedEffect(state.isStartPage) {
        val view = webViewInstance ?: return@LaunchedEffect
        if (state.isStartPage) {
            view.stopLoading()
            view.alpha = 0f
            view.visibility = View.GONE
            webViewDrawTick.intValue++
        } else {
            view.visibility = View.VISIBLE
            webViewDrawTick.intValue++
        }
    }

    // System Back Handler: edit mode -> webView history -> start page -> exit
    BackHandler(enabled = !state.isStartPage || state.canGoBack || state.isEditingUrl) {
        when {
            state.isEditingUrl -> viewModel.setEditingUrl(false)
            webViewInstance?.canGoBack() == true -> {
                webViewInstance?.visibility = View.VISIBLE
                webViewInstance?.alpha = 1f
                webViewInstance?.goBack()
            }
            else -> viewModel.openStartPage()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgCanvas)
    ) {
        // Haze Source Layer (WebView + Start Page) with draw-phase invalidation for live Liquid Glass
        Box(
            modifier = Modifier
                .fillMaxSize()
                .hazeSource(state = hazeState)
                .drawWithContent {
                    webViewDrawTick.intValue
                    drawContent()
                }
        ) {
            AndroidView(
                factory = { ctx ->
                    WebView(ctx).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        setBackgroundColor(android.graphics.Color.parseColor("#0B0C0E"))
                        isVerticalScrollBarEnabled = false
                        overScrollMode = View.OVER_SCROLL_NEVER
                        visibility = View.GONE
                        alpha = 0f

                        // Prevent WebView from consuming IME insets and reflowing DOM when typing in Bottom Bar
                        ViewCompat.setOnApplyWindowInsetsListener(this) { v, insets ->
                            if (v.hasFocus()) {
                                ViewCompat.onApplyWindowInsets(v, insets)
                            } else {
                                WindowInsetsCompat.CONSUMED
                            }
                        }

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

                        // Zero-allocation hysteresis scroll listener + 60fps draw-phase Haze refresh
                        var scrollAccum = 0
                        var latchedMinimized = false
                        var lastHazeTickMs = 0L
                        setOnScrollChangeListener { _, _, scrollY, _, oldScrollY ->
                            val now = SystemClock.uptimeMillis()
                            if (now - lastHazeTickMs >= 16L) {
                                lastHazeTickMs = now
                                webViewDrawTick.intValue++
                            }

                            val dy = scrollY - oldScrollY
                            if ((dy > 0 && scrollAccum < 0) || (dy < 0 && scrollAccum > 0)) {
                                scrollAccum = 0
                            }
                            scrollAccum += dy
                            if (scrollY < 48) {
                                scrollAccum = 0
                                if (latchedMinimized) {
                                    latchedMinimized = false
                                    viewModel.setBarMinimized(false)
                                }
                            } else if (scrollAccum > 56 && scrollY > 120 && !latchedMinimized) {
                                latchedMinimized = true
                                scrollAccum = 0
                                viewModel.setBarMinimized(true)
                            } else if (scrollAccum < -40 && latchedMinimized) {
                                latchedMinimized = false
                                scrollAccum = 0
                                viewModel.setBarMinimized(false)
                            }
                        }

                        webViewClient = object : WebViewClient() {
                            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                                if (url == "about:blank") return
                                latchedMinimized = false
                                scrollAccum = 0
                                viewModel.onPageStarted(url)
                                viewModel.updateNavigationState(
                                    canGoBack = view?.canGoBack() ?: false,
                                    canGoForward = view?.canGoForward() ?: false
                                )
                            }

                            override fun onPageCommitVisible(view: WebView?, url: String?) {
                                view?.alpha = 1f
                                webViewDrawTick.intValue++
                            }

                            override fun onPageFinished(view: WebView?, url: String?) {
                                if (url == "about:blank") return
                                view?.alpha = 1f
                                webViewDrawTick.intValue++
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
                                if (newProgress == 100) {
                                    webViewDrawTick.intValue++
                                }
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
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
            )

            // Safari Start Page Overlay (Spring Scale + Fade Transition)
            AnimatedVisibility(
                visible = state.isStartPage,
                enter = fadeIn(animationSpec = tween(220)) +
                    scaleIn(
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioLowBouncy,
                            stiffness = Spring.StiffnessMediumLow
                        ),
                        initialScale = 0.92f
                    ),
                exit = fadeOut(animationSpec = tween(180)) +
                    scaleOut(
                        animationSpec = tween(180),
                        targetScale = 1.05f
                    ),
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

        // Floating iOS 26 Safari 3-Pill Liquid Glass Bottom Bar
        AppleBottomBar(
            state = state,
            hazeState = hazeState,
            onBack = {
                if (webViewInstance?.canGoBack() == true) {
                    webViewInstance?.visibility = View.VISIBLE
                    webViewInstance?.alpha = 1f
                    webViewInstance?.goBack()
                } else {
                    viewModel.openStartPage()
                }
            },
            onForward = {
                webViewInstance?.visibility = View.VISIBLE
                webViewInstance?.alpha = 1f
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
            onOpenStartPage = {
                viewModel.openStartPage()
            },
            onEditUrl = { editing ->
                viewModel.setEditingUrl(editing)
            },
            onRestoreBar = {
                viewModel.setBarMinimized(false)
            },
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }

    // Apple Safari Bookmarks & Page Menu Modal Sheet
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
