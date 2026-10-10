package com.benbrowser.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.benbrowser.data.BookmarkRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.net.URI
import java.net.URLEncoder
import kotlin.math.abs

class BrowserViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = BookmarkRepository(application)

    private val _state = MutableStateFlow(
        BrowserState(
            isStartPage = true,
            bookmarks = repository.getBookmarks()
        )
    )
    val state: StateFlow<BrowserState> = _state.asStateFlow()

    fun loadUrl(input: String) {
        val trimmed = input.trim()
        if (trimmed.isBlank()) return
        val resolved = resolveUrl(trimmed)
        _state.update {
            it.copy(
                currentUrl = resolved,
                pendingUrl = resolved,
                displayUrl = extractDomain(resolved),
                isStartPage = false,
                isEditingUrl = false,
                isBarMinimized = false,
                isLoading = true,
                progress = 0.1f,
                isCurrentBookmarked = repository.isBookmarked(resolved)
            )
        }
    }

    fun onUrlLoadConsumed() {
        _state.update { it.copy(pendingUrl = null) }
    }

    fun openStartPage() {
        _state.update {
            it.copy(
                isStartPage = true,
                currentUrl = "",
                pendingUrl = null,
                displayUrl = "",
                pageTitle = "Favorites",
                isEditingUrl = false,
                isBarMinimized = false,
                isLoading = false,
                progress = 0f,
                bookmarks = repository.getBookmarks()
            )
        }
    }

    fun setEditingUrl(editing: Boolean) {
        if (_state.value.isEditingUrl == editing) return
        _state.update {
            it.copy(
                isEditingUrl = editing,
                isBarMinimized = if (editing) false else it.isBarMinimized
            )
        }
    }

    fun setBarMinimized(minimized: Boolean) {
        val cur = _state.value
        val target = if (cur.isStartPage || cur.isEditingUrl) false else minimized
        if (cur.isBarMinimized == target) return
        _state.update { it.copy(isBarMinimized = target) }
    }

    fun toggleBookmark() {
        val current = _state.value
        val url = current.currentUrl
        if (url.isBlank() || current.isStartPage) return

        val clean = url.trimEnd('/')
        val existing = repository.getBookmarks().find {
            it.url.trimEnd('/').equals(clean, ignoreCase = true)
        }
        if (existing != null) {
            repository.removeBookmark(existing.id)
        } else {
            repository.addBookmark(current.pageTitle.ifBlank { extractDomain(url) }, url)
        }

        _state.update {
            it.copy(
                isCurrentBookmarked = repository.isBookmarked(url),
                bookmarks = repository.getBookmarks()
            )
        }
    }

    fun removeBookmark(id: String) {
        repository.removeBookmark(id)
        val currentUrl = _state.value.currentUrl
        _state.update {
            it.copy(
                isCurrentBookmarked = repository.isBookmarked(currentUrl),
                bookmarks = repository.getBookmarks()
            )
        }
    }

    fun setShowBookmarksSheet(show: Boolean) {
        if (_state.value.showBookmarksSheet == show) return
        _state.update {
            it.copy(
                showBookmarksSheet = show,
                bookmarks = if (show) repository.getBookmarks() else it.bookmarks
            )
        }
    }

    fun onPageStarted(url: String?) {
        val validUrl = url.orEmpty()
        if (validUrl.isBlank() || validUrl == "about:blank") return
        _state.update {
            if (it.isStartPage) it
            else it.copy(
                currentUrl = validUrl,
                displayUrl = extractDomain(validUrl),
                isLoading = true,
                isCurrentBookmarked = repository.isBookmarked(validUrl)
            )
        }
    }

    fun onPageFinished(url: String?) {
        val validUrl = url.orEmpty()
        if (validUrl.isBlank() || validUrl == "about:blank") return
        _state.update {
            if (it.isStartPage) it
            else it.copy(
                currentUrl = validUrl,
                displayUrl = extractDomain(validUrl),
                isLoading = false,
                progress = 1f,
                isCurrentBookmarked = repository.isBookmarked(validUrl)
            )
        }
    }

    fun onProgressChanged(newProgress: Int) {
        val next = newProgress / 100f
        val cur = _state.value
        if (cur.isStartPage) return
        // Throttle progress updates to >= 5% steps to prevent recomposition lag
        if (newProgress < 100 && abs(next - cur.progress) < 0.05f) return
        _state.update {
            it.copy(
                progress = next,
                isLoading = newProgress < 100
            )
        }
    }

    fun onReceivedTitle(title: String?) {
        val clean = title.orEmpty()
        if (clean == "about:blank" || _state.value.pageTitle == clean) return
        _state.update { it.copy(pageTitle = clean) }
    }

    fun updateNavigationState(canGoBack: Boolean, canGoForward: Boolean) {
        val cur = _state.value
        if (cur.canGoBack == canGoBack && cur.canGoForward == canGoForward) return
        _state.update {
            it.copy(
                canGoBack = canGoBack,
                canGoForward = canGoForward
            )
        }
    }

    private fun resolveUrl(input: String): String = when {
        input.startsWith("http://", ignoreCase = true) ||
            input.startsWith("https://", ignoreCase = true) -> input
        input.contains(".") && !input.contains(" ") -> "https://$input"
        else -> "https://www.google.com/search?q=${URLEncoder.encode(input, "UTF-8")}"
    }

    private fun extractDomain(url: String): String = try {
        URI(url).host?.removePrefix("www.")?.removePrefix("m.") ?: url
    } catch (_: Exception) {
        url
    }
}
