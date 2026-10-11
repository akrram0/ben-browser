package com.benbrowser.ui

import com.benbrowser.data.Bookmark
import com.benbrowser.data.BookmarkRepository

data class BrowserState(
    val currentUrl: String = "",
    val pendingUrl: String? = null,
    val displayUrl: String = "",
    val pageTitle: String = "",
    val canGoBack: Boolean = false,
    val canGoForward: Boolean = false,
    val isLoading: Boolean = false,
    val progress: Float = 0f,
    val isStartPage: Boolean = true,
    val isEditingUrl: Boolean = false,
    val isBarMinimized: Boolean = false,
    val showBookmarksSheet: Boolean = false,
    val showSettingsSheet: Boolean = false,
    val isCurrentBookmarked: Boolean = false,
    val searchEngine: String = BookmarkRepository.ENGINE_DUCKDUCKGO,
    val bookmarks: List<Bookmark> = emptyList()
)
