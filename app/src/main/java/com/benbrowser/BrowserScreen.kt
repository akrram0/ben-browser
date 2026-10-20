package com.benbrowser

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.benbrowser.theme.BgCanvas
import com.benbrowser.ui.BrowserViewModel
import com.benbrowser.ui.components.AppleBottomBar
import com.benbrowser.ui.components.AppleStartPage
import com.benbrowser.ui.components.BookmarksSheet
import com.benbrowser.ui.components.FrameStatsHud
import com.benbrowser.ui.components.SettingsSheet
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import org.mozilla.geckoview.GeckoResult
import org.mozilla.geckoview.GeckoRuntime
import org.mozilla.geckoview.GeckoRuntimeSettings
import org.mozilla.geckoview.GeckoSession
import org.mozilla.geckoview.GeckoView

// Diagnostic: the bar's scroll-triggered spring morph is the only per-frame layout work
// during a scroll gesture. Disabled here to measure whether it causes the scroll jank.
private const val AUTO_COLLAPSE_ON_SCROLL = false

private object GeckoEngine {
    @Volatile
    private var runtime: GeckoRuntime? = null

    fun isInitialized(): Boolean = runtime != null

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
    val updateInfo by viewModel.updateState.collectAsState()
    val hazeState = rememberHazeState()
    var geckoViewInstance by remember { mutableStateOf<GeckoView?>(null) }
    var isEngineReady by remember { mutableStateOf(GeckoEngine.isInitialized()) }
    var liveGlassEnabled by rememberSaveable { mutableStateOf(true) }

    val geckoSession = remember { GeckoSession() }

    // Pre-warm GeckoRuntime 180ms after first frame so cold start opens instantly and never attaches GeckoView before MozGlue loads
    LaunchedEffect(Unit) {
        if (!isEngineReady) {
            kotlinx.coroutines.delay(180)
        }
        val runtime = GeckoEngine.getRuntime(context)
        if (!geckoSession.isOpen) {
            geckoSession.open(runtime)
        }
        isEngineReady = true
    }

    DisposableEffect(geckoSession) {
        var lastScrollY = 0
        var scrollAccum = 0
        var latchedMinimized = false

        geckoSession.navigationDelegate = object : GeckoSession.NavigationDelegate {
            override fun onLocationChange(
                session: GeckoSession,
                url: String?,
                perms: MutableList<GeckoSession.PermissionDelegate.ContentPermission>,
                hasUserGesture: Boolean
            ) {
                viewModel.onLocationChanged(url)
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
            }

            override fun onPageStop(session: GeckoSession, success: Boolean) {
                viewModel.onPageStop()
            }
        }

        geckoSession.contentDelegate = object : GeckoSession.ContentDelegate {
            override fun onTitleChange(session: GeckoSession, title: String?) {
                viewModel.onReceivedTitle(title)
            }

            override fun onFirstContentfulPaint(session: GeckoSession) {
                geckoViewInstance?.alpha = 1f
            }

            override fun onFirstComposite(session: GeckoSession) {
                geckoViewInstance?.alpha = 1f
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
                if (!AUTO_COLLAPSE_ON_SCROLL) return
                val dy = scrollY - lastScrollY
                lastScrollY = scrollY
                if ((dy > 0 && scrollAccum < 0) || (dy < 0 && scrollAccum > 0)) {
                    scrollAccum = 0
                }
                scrollAccum += dy
                if (scrollY < 60) {
                    scrollAccum = 0
                    if (latchedMinimized) {
                        latchedMinimized = false
                        viewModel.setBarMinimized(false)
                    }
                } else if (scrollAccum > 100 && scrollY > 150 && !latchedMinimized) {
                    latchedMinimized = true
                    scrollAccum = 0
                    viewModel.setBarMinimized(true)
                } else if (scrollAccum < -80 && latchedMinimized) {
                    latchedMinimized = false
                    scrollAccum = 0
                    viewModel.setBarMinimized(false)
                }
            }
        }

        onDispose {
            if (geckoSession.isOpen) {
                geckoSession.close()
            }
        }
    }

    // Dispatch user-initiated URL loads once without interrupting in-page redirects
    LaunchedEffect(state.pendingUrl, geckoViewInstance) {
        val targetUrl = state.pendingUrl
        if (!targetUrl.isNullOrBlank()) {
            val runtime = GeckoEngine.getRuntime(context)
            if (!geckoSession.isOpen) {
                geckoSession.open(runtime)
            }
            isEngineReady = true
            val view = geckoViewInstance
            if (view != null) {
                view.visibility = View.VISIBLE
                view.alpha = 1f
                geckoSession.loadUri(targetUrl)
                viewModel.onUrlLoadConsumed()
            }
        }
    }

    // Isolate GeckoView when on Start Page so Start Page animations run at locked 120fps
    LaunchedEffect(state.isStartPage, geckoViewInstance) {
        val view = geckoViewInstance ?: return@LaunchedEffect
        if (state.isStartPage) {
            if (geckoSession.isOpen) {
                geckoSession.stop()
            }
            view.alpha = 0f
            view.visibility = View.GONE
        } else {
            view.visibility = View.VISIBLE
            view.alpha = 1f
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
        if (isEngineReady) {
            AndroidView(
                factory = { ctx ->
                    val runtime = GeckoEngine.getRuntime(ctx)
                    if (!geckoSession.isOpen) {
                        geckoSession.open(runtime)
                    }
                    GeckoView(ctx).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        // TextureView is required so Compose/Haze can sample the page.
                        // SurfaceView punches through the Compose window and rendered
                        // the page as a fully black screen under the Haze layer.
                        setViewBackend(GeckoView.BACKEND_TEXTURE_VIEW)
                        coverUntilFirstPaint(android.graphics.Color.parseColor("#0B0C0E"))
                        setBackgroundColor(android.graphics.Color.parseColor("#0B0C0E"))
                        visibility = if (state.isStartPage) View.GONE else View.VISIBLE
                        alpha = if (state.isStartPage) 0f else 1f

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
                    .then(if (liveGlassEnabled) Modifier.hazeSource(state = hazeState) else Modifier)
                    .statusBarsPadding()
            )
        }

        // Isolated Start Page Layer: Native 120 FPS refresh with zero Haze render-node overhead
        AnimatedVisibility(
            visible = state.isStartPage,
            enter = fadeIn(tween(150, easing = FastOutSlowInEasing)) +
                scaleIn(tween(150, easing = FastOutSlowInEasing), initialScale = 0.96f),
            exit = fadeOut(tween(120, easing = FastOutSlowInEasing)) +
                scaleOut(tween(120, easing = FastOutSlowInEasing), targetScale = 1.03f),
            modifier = Modifier.fillMaxSize()
        ) {
            AppleStartPage(
                bookmarks = state.bookmarks,
                searchEngine = state.searchEngine,
                isUpdateAvailable = updateInfo.isAvailable,
                onSelectBookmark = { url ->
                    viewModel.loadUrl(url)
                },
                onOpenSearch = {
                    viewModel.setEditingUrl(true)
                },
                onOpenSettings = {
                    viewModel.setShowSettingsSheet(true)
                },
                modifier = Modifier.fillMaxSize()
            )
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
            liveGlassEnabled = liveGlassEnabled,
            onToggleLiveGlass = {
                liveGlassEnabled = !liveGlassEnabled
                Toast.makeText(
                    context,
                    if (liveGlassEnabled) "Haze glass: ON (live blur)" else "Haze glass: OFF (frosted fallback)",
                    Toast.LENGTH_SHORT
                ).show()
            },
            modifier = Modifier.align(Alignment.BottomCenter)
        )

        FrameStatsHud(
            modifier = Modifier
                .align(Alignment.TopStart)
                .statusBarsPadding()
                .padding(8.dp)
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

    // Settings & Quick Add (+) Modal Sheet
    if (state.showSettingsSheet) {
        SettingsSheet(
            searchEngine = state.searchEngine,
            updateInfo = updateInfo,
            onSelectSearchEngine = { engine ->
                viewModel.setSearchEngine(engine)
            },
            onAddBookmark = { title, url ->
                viewModel.addCustomBookmark(title, url)
            },
            onResetBookmarks = {
                viewModel.resetDefaultBookmarks()
            },
            onCheckForUpdates = {
                viewModel.checkForUpdates()
            },
            onDownloadUpdate = {
                viewModel.startUpdateDownload()
            },
            onInstallUpdate = {
                viewModel.installDownloadedUpdate()
            },
            onDismiss = {
                viewModel.setShowSettingsSheet(false)
            }
        )
    }
}
