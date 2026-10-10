package com.benbrowser.data

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject
import java.net.URI
import java.util.UUID

class BookmarkRepository(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("ben_browser_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_BOOKMARKS = "safari_bookmarks_v2"

        val DEFAULT_FAVORITES = listOf(
            Bookmark("fav_google", "Google", "https://www.google.com"),
            Bookmark("fav_apple", "Apple", "https://www.apple.com"),
            Bookmark("fav_github", "GitHub", "https://github.com"),
            Bookmark("fav_youtube", "YouTube", "https://m.youtube.com"),
            Bookmark("fav_wiki", "Wikipedia", "https://www.wikipedia.org"),
            Bookmark("fav_hn", "Hacker News", "https://news.ycombinator.com"),
            Bookmark("fav_reddit", "Reddit", "https://www.reddit.com"),
            Bookmark("fav_ddg", "DuckDuckGo", "https://duckduckgo.com/?kae=d&k1=-1&kak=-1&kax=-1&kaq=-1&kap=-1&kao=-1&kau=-1")
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
            List(array.length()) { i ->
                val obj = array.getJSONObject(i)
                Bookmark(
                    id = obj.getString("id"),
                    title = obj.getString("title"),
                    url = obj.getString("url"),
                    timestamp = obj.optLong("timestamp", 0L)
                )
            }
        } catch (_: Exception) {
            DEFAULT_FAVORITES
        }
    }

    fun addBookmark(title: String, url: String): Bookmark {
        val current = getBookmarks().toMutableList()
        val existing = current.find { it.url.equals(url, ignoreCase = true) }
        if (existing != null) return existing

        val newBookmark = Bookmark(
            id = UUID.randomUUID().toString(),
            title = title.ifBlank { extractDomain(url) },
            url = url
        )
        current.add(0, newBookmark)
        saveBookmarks(current)
        return newBookmark
    }

    fun removeBookmark(id: String) {
        val current = getBookmarks().filterNot { it.id == id }
        saveBookmarks(current)
    }

    fun isBookmarked(url: String): Boolean {
        if (url.isBlank()) return false
        val clean = url.trimEnd('/')
        return getBookmarks().any { it.url.trimEnd('/').equals(clean, ignoreCase = true) }
    }

    private fun saveBookmarks(bookmarks: List<Bookmark>) {
        val array = JSONArray()
        for (b in bookmarks) {
            array.put(
                JSONObject()
                    .put("id", b.id)
                    .put("title", b.title)
                    .put("url", b.url)
                    .put("timestamp", b.timestamp)
            )
        }
        prefs.edit().putString(KEY_BOOKMARKS, array.toString()).apply()
    }

    private fun extractDomain(url: String): String = try {
        URI(url).host?.removePrefix("www.")?.removePrefix("m.") ?: url
    } catch (_: Exception) {
        url
    }
}
