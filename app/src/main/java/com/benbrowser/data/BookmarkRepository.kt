package com.benbrowser.data

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

class BookmarkRepository(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("ben_browser_bookmarks", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_BOOKMARKS = "saved_bookmarks"

        val DEFAULT_FAVORITES = listOf(
            Bookmark(UUID.randomUUID().toString(), "DuckDuckGo", "https://duckduckgo.com"),
            Bookmark(UUID.randomUUID().toString(), "GitHub", "https://github.com"),
            Bookmark(UUID.randomUUID().toString(), "Wikipedia", "https://wikipedia.org"),
            Bookmark(UUID.randomUUID().toString(), "Hacker News", "https://news.ycombinator.com"),
            Bookmark(UUID.randomUUID().toString(), "Reddit", "https://reddit.com"),
            Bookmark(UUID.randomUUID().toString(), "YouTube", "https://youtube.com")
        )
    }

    fun getBookmarks(): List<Bookmark> {
        val jsonString = prefs.getString(KEY_BOOKMARKS, null)
        if (jsonString == null) {
            saveBookmarks(DEFAULT_FAVORITES)
            return DEFAULT_FAVORITES
        }
        return try {
            val array = JSONArray(jsonString)
            val list = mutableListOf<Bookmark>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    Bookmark(
                        id = obj.getString("id"),
                        title = obj.getString("title"),
                        url = obj.getString("url"),
                        timestamp = obj.optLong("timestamp", System.currentTimeMillis())
                    )
                )
            }
            list
        } catch (_: Exception) {
            DEFAULT_FAVORITES
        }
    }

    fun addBookmark(title: String, url: String): Bookmark {
        val current = getBookmarks().toMutableList()
        // If already exists with same URL, don't duplicate
        val existing = current.find { it.url.equals(url, ignoreCase = true) }
        if (existing != null) return existing

        val newBookmark = Bookmark(
            id = UUID.randomUUID().toString(),
            title = if (title.isBlank()) extractDomain(url) else title,
            url = url
        )
        current.add(0, newBookmark)
        saveBookmarks(current)
        return newBookmark
    }

    fun removeBookmark(id: String) {
        val current = getBookmarks().toMutableList()
        current.removeAll { it.id == id }
        saveBookmarks(current)
    }

    fun isBookmarked(url: String): Boolean {
        if (url.isBlank()) return false
        return getBookmarks().any { it.url.equals(url, ignoreCase = true) }
    }

    private fun saveBookmarks(bookmarks: List<Bookmark>) {
        val array = JSONArray()
        for (b in bookmarks) {
            val obj = JSONObject()
            obj.put("id", b.id)
            obj.put("title", b.title)
            obj.put("url", b.url)
            obj.put("timestamp", b.timestamp)
            array.put(obj)
        }
        prefs.edit().putString(KEY_BOOKMARKS, array.toString()).apply()
    }

    private fun extractDomain(url: String): String {
        return try {
            val uri = java.net.URI(url)
            val host = uri.host
            if (!host.isNullOrBlank()) host.removePrefix("www.") else url
        } catch (_: Exception) {
            url
        }
    }
}
