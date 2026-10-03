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
        val resolved = resolveUrl(input)
        _state.update {
            it.copy(
                currentUrl = resolved,
                displayUrl = extractDomain(resolved),
                isStartPage = false,
                isEditingUrl = false,
                isCurrentBookmarked = repository.isBookmarked(resolved)
            )
        }
    }

    fun openStartPage() {
        _state.update {
            it.copy(
                isStartPage = true,
                currentUrl = "",
                displayUrl = "",
                pageTitle = "Favorites",
                isEditingUrl = false,
                bookmarks = repository.getBookmarks()
            )
        }
    }

    fun setEditingUrl(editing: Boolean) {
        _state.update { it.copy(isEditingUrl = editing) }
    }

    fun toggleBookmark() {
        val current = _state.value
        val url = current.currentUrl
        if (url.isBlank()) return

        if (repository.isBookmarked(url)) {
            val bookmark = repository.getBookmarks().find { it.url.equals(url, ignoreCase = true) }
            if (bookmark != null) {
                repository.removeBookmark(bookmark.id)
            }
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
        if (show) {
            _state.update { it.copy(showBookmarksSheet = true, bookmarks = repository.getBookmarks()) }
        } else {
            _state.update { it.copy(showBookmarksSheet = false) }
        }
    }

    fun onPageStarted(url: String?) {
        val validUrl = url.orEmpty()
        _state.update {
            it.copy(
                currentUrl = validUrl,
                displayUrl = extractDomain(validUrl),
                isLoading = true,
                isCurrentBookmarked = repository.isBookmarked(validUrl)
            )
        }
    }

    fun onPageFinished(url: String?) {
        val validUrl = url.orEmpty()
        _state.update {
            it.copy(
                currentUrl = validUrl,
                displayUrl = extractDomain(validUrl),
                isLoading = false,
                isCurrentBookmarked = repository.isBookmarked(validUrl)
            )
        }
    }

    fun onProgressChanged(progress: Int) {
        _state.update {
            it.copy(
                progress = progress / 100f,
                isLoading = progress < 100
            )
        }
    }

    fun onReceivedTitle(title: String?) {
        _state.update { it.copy(pageTitle = title.orEmpty()) }
    }

    fun updateNavigationState(canGoBack: Boolean, canGoForward: Boolean) {
        _state.update {
            it.copy(
                canGoBack = canGoBack,
                canGoForward = canGoForward
            )
        }
    }

    private fun resolveUrl(input: String): String {
        val trimmed = input.trim()
        return when {
            trimmed.startsWith("http://", ignoreCase = true) || trimmed.startsWith("https://", ignoreCase = true) -> trimmed
            trimmed.contains(".") && !trimmed.contains(" ") -> "https://$trimmed"
            else -> "https://duckduckgo.com/?q=${URLEncoder.encode(trimmed, "UTF-8")}"
        }
    }

    private fun extractDomain(url: String): String {
        return try {
            val uri = URI(url)
            val host = uri.host
            if (!host.isNullOrBlank()) host.removePrefix("www.") else url
        } catch (_: Exception) {
            url
        }
    }
}
