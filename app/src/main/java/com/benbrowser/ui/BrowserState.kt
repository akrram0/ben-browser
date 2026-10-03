package com.benbrowser.ui

import com.benbrowser.data.Bookmark

data class BrowserState(
    val currentUrl: String = "",
    val displayUrl: String = "",
    val pageTitle: String = "",
    val canGoBack: Boolean = false,
    val canGoForward: Boolean = false,
    val isLoading: Boolean = false,
    val progress: Float = 0f,
    val isStartPage: Boolean = true,
    val isEditingUrl: Boolean = false,
    val showBookmarksSheet: Boolean = false,
    val isCurrentBookmarked: Boolean = false,
    val bookmarks: List<Bookmark> = emptyList()
)
