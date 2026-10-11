package com.benbrowser

import android.content.Context
import android.os.SystemClock
import android.view.View
import android.view.ViewGroup
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
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.benbrowser.theme.BgCanvas
import com.benbrowser.ui.BrowserViewModel
import com.benbrowser.ui.components.AppleBottomBar
import com.benbrowser.ui.components.AppleStartPage
import com.benbrowser.ui.components.BookmarksSheet
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import org.mozilla.geckoview.GeckoResult
import org.mozilla.geckoview.GeckoRuntime
import org.mozilla.geckoview.GeckoRuntimeSettings
import org.mozilla.geckoview.GeckoSession
import org.mozilla.geckoview.GeckoView

private object GeckoEngine {
    @Volatile
    private var runtime: GeckoRuntime? = null

    fun getRuntime(context: Context): GeckoRuntime {
        return runtime ?: synchronized(this) {
            runtime ?: GeckoRuntime.create(
                context.applicationContext,
                GeckoRuntimeSettings.Builder()
                    .javaScriptEnabled(true)
                    .preferredColorScheme(GeckoRuntimeSettings.COLOR_SCHEME_DARK)
                    .doubleTapZoomingEnabled(true)
                    .inputAutoZoomEnabled(true)
                    .forceUserScalableEnabled(true)
                    .automaticFontSizeAdjustment(true)
                    .globalPrivacyControlEnabled(true)
                    .build()
            ).also { runtime = it }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrowserScreen(
    viewModel: BrowserViewModel = viewModel()
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsState()
    val hazeState = rememberHazeState()
    var geckoViewInstance by remember { mutableStateOf<GeckoView?>(null) }
    // Draw-phase-only tick to refresh HazeSource from GeckoView's TextureView surface at 60fps
    val geckoDrawTick = remember { mutableIntStateOf(0) }

    val geckoSession = remember {
        val runtime = GeckoEngine.getRuntime(context)
        GeckoSession().apply {
            open(runtime)
        }
    }

    DisposableEffect(geckoSession) {
        var lastScrollY = 0
        var scrollAccum = 0
        var latchedMinimized = false
        var lastHazeTickMs = 0L

        geckoSession.navigationDelegate = object : GeckoSession.NavigationDelegate {
            override fun onLocationChange(
                session: GeckoSession,
                url: String?,
                perms: MutableList<GeckoSession.PermissionDelegate.ContentPermission>,
                hasUserGesture: Boolean
            ) {
                viewModel.onLocationChanged(url)
                geckoDrawTick.intValue++
            }

            override fun onCanGoBack(session: GeckoSession, canGoBack: Boolean) {
                viewModel.setCanGoBack(canGoBack)
            }

            override fun onCanGoForward(session: GeckoSession, canGoForward: Boolean) {
                viewModel.setCanGoForward(canGoForward)
            }

            override fun onNewSession(
                session: GeckoSession,
                uri: String
            ): GeckoResult<GeckoSession>? {
                session.loadUri(uri)
                return null
            }
        }

        geckoSession.progressDelegate = object : GeckoSession.ProgressDelegate {
            override fun onPageStart(session: GeckoSession, url: String) {
                lastScrollY = 0
                scrollAccum = 0
                latchedMinimized = false
                viewModel.onPageStarted(url)
            }

            override fun onProgressChange(session: GeckoSession, progress: Int) {
                viewModel.onProgressChanged(progress)
                if (progress == 100) {
                    geckoDrawTick.intValue++
                }
            }

            override fun onPageStop(session: GeckoSession, success: Boolean) {
                viewModel.onPageStop()
                geckoDrawTick.intValue++
            }
        }

        geckoSession.contentDelegate = object : GeckoSession.ContentDelegate {
            override fun onTitleChange(session: GeckoSession, title: String?) {
                viewModel.onReceivedTitle(title)
            }

            override fun onFirstContentfulPaint(session: GeckoSession) {
                geckoViewInstance?.alpha = 1f
                geckoDrawTick.intValue++
            }

            override fun onFirstComposite(session: GeckoSession) {
                geckoViewInstance?.alpha = 1f
                geckoDrawTick.intValue++
            }

            override fun onCrash(session: GeckoSession) {
                session.open(GeckoEngine.getRuntime(context))
                session.reload()
            }

            override fun onKill(session: GeckoSession) {
                session.open(GeckoEngine.getRuntime(context))
                session.reload()
            }
        }

        geckoSession.scrollDelegate = object : GeckoSession.ScrollDelegate {
            override fun onScrollChanged(session: GeckoSession, scrollX: Int, scrollY: Int) {
                val now = SystemClock.uptimeMillis()
                if (now - lastHazeTickMs >= 16L) {
                    lastHazeTickMs = now
                    geckoDrawTick.intValue++
                }

                val dy = scrollY - lastScrollY
                lastScrollY = scrollY
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
        }

        onDispose {
            geckoSession.close()
        }
    }

    // Dispatch user-initiated URL loads once without interrupting in-page redirects
    LaunchedEffect(state.pendingUrl, geckoViewInstance) {
        val targetUrl = state.pendingUrl
        val view = geckoViewInstance
        if (!targetUrl.isNullOrBlank() && view != null) {
            view.visibility = View.VISIBLE
            view.alpha = 1f
            if (!geckoSession.isOpen) {
                geckoSession.open(GeckoEngine.getRuntime(context))
            }
            geckoSession.loadUri(targetUrl)
            viewModel.onUrlLoadConsumed()
        }
    }

    // Isolate GeckoView when on Start Page so Start Page animations run at locked 120fps
    LaunchedEffect(state.isStartPage) {
        val view = geckoViewInstance ?: return@LaunchedEffect
        if (state.isStartPage) {
            geckoSession.stop()
            view.alpha = 0f
            view.visibility = View.GONE
            geckoDrawTick.intValue++
        } else {
            view.visibility = View.VISIBLE
            view.alpha = 1f
            geckoDrawTick.intValue++
        }
    }

    // System Back Handler: edit mode -> Gecko history -> start page -> exit
    BackHandler(enabled = !state.isStartPage || state.canGoBack || state.isEditingUrl) {
        when {
            state.isEditingUrl -> viewModel.setEditingUrl(false)
            state.canGoBack && !state.isStartPage -> {
                geckoViewInstance?.visibility = View.VISIBLE
                geckoViewInstance?.alpha = 1f
                geckoSession.goBack()
            }
            else -> viewModel.openStartPage()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgCanvas)
    ) {
        // Haze Source Layer (GeckoView TextureView + Start Page) for Live Liquid Glass Refraction
        Box(
            modifier = Modifier
                .fillMaxSize()
                .hazeSource(state = hazeState)
                .drawWithContent {
                    geckoDrawTick.intValue
                    drawContent()
                }
        ) {
            AndroidView(
                factory = { ctx ->
                    GeckoView(ctx).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        // TextureView backend enables Compose GraphicsLayer / HazeGlass capture
                        setViewBackend(GeckoView.BACKEND_TEXTURE_VIEW)
                        coverUntilFirstPaint(android.graphics.Color.parseColor("#0B0C0E"))
                        setBackgroundColor(android.graphics.Color.parseColor("#0B0C0E"))
                        visibility = View.GONE
                        alpha = 0f

                        // Prevent IME insets from resizing Gecko viewport while typing in Bottom Bar
                        ViewCompat.setOnApplyWindowInsetsListener(this) { v, insets ->
                            if (v.hasFocus()) {
                                ViewCompat.onApplyWindowInsets(v, insets)
                            } else {
                                WindowInsetsCompat.CONSUMED
                            }
                        }

                        setSession(geckoSession)
                        geckoViewInstance = this
                    }
                },
                update = { view ->
                    geckoViewInstance = view
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
                if (state.canGoBack && !state.isStartPage) {
                    geckoViewInstance?.visibility = View.VISIBLE
                    geckoViewInstance?.alpha = 1f
                    geckoSession.goBack()
                } else {
                    viewModel.openStartPage()
                }
            },
            onForward = {
                if (state.canGoForward) {
                    geckoViewInstance?.visibility = View.VISIBLE
                    geckoViewInstance?.alpha = 1f
                    geckoSession.goForward()
                }
            },
            onReload = {
                geckoSession.reload()
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
