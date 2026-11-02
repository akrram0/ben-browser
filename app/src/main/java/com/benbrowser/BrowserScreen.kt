package com.benbrowser

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.benbrowser.ui.components.SettingsSheet
import org.mozilla.geckoview.GeckoResult
import org.mozilla.geckoview.GeckoRuntime
import org.mozilla.geckoview.GeckoRuntimeSettings
import org.mozilla.geckoview.GeckoSession
import org.mozilla.geckoview.GeckoView

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
                    .doubleTapZoomingEnabled(false)
                    .inputAutoZoomEnabled(false)
                    .forceUserScalableEnabled(true)
                    .automaticFontSizeAdjustment(false)
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
    var geckoViewInstance by remember { mutableStateOf<GeckoView?>(null) }
    var isEngineReady by remember { mutableStateOf(GeckoEngine.isInitialized()) }

    val geckoSession = remember { GeckoSession() }

    // Pre-warm GeckoRuntime 100ms after first frame so cold start opens instantly
    LaunchedEffect(Unit) {
        if (!isEngineReady) {
            kotlinx.coroutines.delay(100)
        }
        val runtime = GeckoEngine.getRuntime(context)
        if (!geckoSession.isOpen) {
            geckoSession.open(runtime)
        }
        isEngineReady = true
    }

    DisposableEffect(geckoSession) {
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

        var lastScrollY = 0
        var scrollAccum = 0
        var latchedMinimized = false

        geckoSession.scrollDelegate = object : GeckoSession.ScrollDelegate {
            override fun onScrollChanged(session: GeckoSession, scrollX: Int, scrollY: Int) {
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
                } else if (scrollAccum > 120 && scrollY > 160 && !latchedMinimized) {
                    latchedMinimized = true
                    scrollAccum = 0
                    viewModel.setBarMinimized(true)
                } else if (scrollAccum < -90 && latchedMinimized) {
                    latchedMinimized = false
                    scrollAccum = 0
                    viewModel.setBarMinimized(false)
                }
            }
        }

        onDispose {
            geckoSession.scrollDelegate = null
            if (geckoSession.isOpen) {
                geckoSession.close()
            }
        }
    }

    // Dispatch user-initiated URL loads immediately
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
                view.alpha = 1f
                geckoSession.loadUri(targetUrl)
                viewModel.onUrlLoadConsumed()
            }
        }
    }

    // Toggle GeckoView alpha: stays warm in the hierarchy so TextureView is never destroyed
    LaunchedEffect(state.isStartPage, geckoViewInstance) {
        val view = geckoViewInstance ?: return@LaunchedEffect
        view.alpha = if (state.isStartPage) 0f else 1f
    }

    // System Back Handler: edit mode -> Gecko history -> start page -> exit
    BackHandler(enabled = !state.isStartPage || state.canGoBack || state.isEditingUrl) {
        when {
            state.isEditingUrl -> viewModel.setEditingUrl(false)
            state.canGoBack && !state.isStartPage -> {
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
                        setViewBackend(GeckoView.BACKEND_TEXTURE_VIEW)
                        setBackgroundColor(android.graphics.Color.parseColor("#0B0C0E"))
                        // Keep View.VISIBLE so SurfaceTexture is never destroyed
                        visibility = View.VISIBLE
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
                    view.alpha = if (state.isStartPage) 0f else 1f
                },
                modifier = Modifier.fillMaxSize()
            )
        }

        // Start Page Overlay: 120 FPS native scroll performance
        AnimatedVisibility(
            visible = state.isStartPage,
            enter = fadeIn(tween(140, easing = FastOutSlowInEasing)),
            exit = fadeOut(tween(110, easing = FastOutSlowInEasing)),
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

        // Floating iOS 26 Safari / BitChord Liquid Glass Bottom Bar
        AppleBottomBar(
            state = state,
            onBack = {
                if (state.canGoBack && !state.isStartPage) {
                    geckoViewInstance?.alpha = 1f
                    geckoSession.goBack()
                } else {
                    viewModel.openStartPage()
                }
            },
            onForward = {
                if (state.canGoForward) {
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
