package com.benbrowser.data

data class Bookmark(
    val id: String,
    val title: String,
    val url: String,
    val timestamp: Long = System.currentTimeMillis()
)
