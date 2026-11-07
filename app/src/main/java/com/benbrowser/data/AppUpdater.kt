package com.benbrowser.data

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.benbrowser.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

data class UpdateInfo(
    val isChecking: Boolean = false,
    val isAvailable: Boolean = false,
    val remoteTag: String = "",
    val remoteTimestamp: Long = 0L,
    val apkUrl: String = "",
    val changelog: String = "",
    val isDownloading: Boolean = false,
    val isReadyToInstall: Boolean = false,
    val errorMessage: String? = null
)

class AppUpdater(private val context: Context) {
    private val _updateState = MutableStateFlow(UpdateInfo())
    val updateState: StateFlow<UpdateInfo> = _updateState.asStateFlow()

    private var activeDownloadId: Long = -1L

    suspend fun checkForUpdates(): Boolean = withContext(Dispatchers.IO) {
        _updateState.value = _updateState.value.copy(isChecking = true, errorMessage = null)
        try {
            // First check the latest tag release (works for both prereleases and regular releases)
            val primaryEndpoint = "https://api.github.com/repos/akrram0/ben-browser/releases/tags/latest"
            val fallbackEndpoint = "https://api.github.com/repos/akrram0/ben-browser/releases"

            val body = fetchUrl(primaryEndpoint) ?: fetchUrl(fallbackEndpoint)

            if (body == null) {
                _updateState.value = _updateState.value.copy(
                    isChecking = false,
                    errorMessage = "Unable to connect to update server"
                )
                return@withContext false
            }

            parseAndCompareRelease(body)
        } catch (e: Exception) {
            _updateState.value = _updateState.value.copy(
                isChecking = false,
                errorMessage = e.message ?: "Failed to check for updates"
            )
            false
        }
    }

    private fun fetchUrl(endpoint: String): String? {
        return try {
            val conn = (URL(endpoint).openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 8000
                readTimeout = 8000
                setRequestProperty("Accept", "application/vnd.github+json")
                setRequestProperty("User-Agent", "BenBrowser-Android")
            }
            if (conn.responseCode in 200..299) {
                conn.inputStream.bufferedReader().readText()
            } else {
                null
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun parseAndCompareRelease(jsonStr: String): Boolean {
        val root: JSONObject = try {
            if (jsonStr.trim().startsWith("[")) {
                val array = JSONArray(jsonStr)
                if (array.length() == 0) return false
                array.getJSONObject(0)
            } else {
                JSONObject(jsonStr)
            }
        } catch (_: Exception) {
            return false
        }

        val tagName = root.optString("tag_name", "")
        val publishedAtStr = root.optString("published_at", "")
        val createdAtStr = root.optString("created_at", "")
        val body = root.optString("body", "")

        val assets = root.optJSONArray("assets") ?: return false
        var apkUrl = ""
        var assetUpdatedAtStr = ""
        for (i in 0 until assets.length()) {
            val asset = assets.getJSONObject(i)
            val name = asset.optString("name", "")
            if (name.endsWith(".apk", ignoreCase = true)) {
                apkUrl = asset.optString("browser_download_url", "")
                assetUpdatedAtStr = asset.optString("updated_at", publishedAtStr)
                break
            }
        }

        if (apkUrl.isBlank()) {
            _updateState.value = _updateState.value.copy(
                isChecking = false,
                errorMessage = "Release contains no APK"
            )
            return false
        }

        val effectiveTimestampStr = assetUpdatedAtStr.ifBlank { createdAtStr.ifBlank { publishedAtStr } }
        val remoteEpoch = parseIsoTimestamp(effectiveTimestampStr)
        val localBuildTime = BuildConfig.BUILD_TIME

        // An update is available if the remote build was compiled after local build time (+ buffer)
        val isNewer = remoteEpoch > (localBuildTime + 120_000L)

        _updateState.value = _updateState.value.copy(
            isChecking = false,
            isAvailable = isNewer,
            remoteTag = tagName,
            remoteTimestamp = remoteEpoch,
            apkUrl = apkUrl,
            changelog = body,
            errorMessage = null
        )
        return isNewer
    }

    fun startDownloadAndInstall() {
        val current = _updateState.value
        val url = current.apkUrl
        if (url.isBlank()) return

        _updateState.value = current.copy(isDownloading = true, errorMessage = null)

        try {
            val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

            val request = DownloadManager.Request(Uri.parse(url)).apply {
                setTitle("Ben Browser Update")
                setDescription("Downloading latest build...")
                setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                setDestinationInExternalFilesDir(context, Environment.DIRECTORY_DOWNLOADS, "ben-browser-update.apk")
                setMimeType("application/vnd.android.package-archive")
            }

            activeDownloadId = dm.enqueue(request)

            val receiver = object : BroadcastReceiver() {
                override fun onReceive(c: Context?, intent: Intent?) {
                    val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1L) ?: -1L
                    if (id == activeDownloadId) {
                        try {
                            context.unregisterReceiver(this)
                        } catch (_: Exception) {}

                        // Verify download was successful
                        val query = DownloadManager.Query().setFilterById(activeDownloadId)
                        val cursor = dm.query(query)
                        if (cursor != null && cursor.moveToFirst()) {
                            val statusIdx = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
                            val status = if (statusIdx != -1) cursor.getInt(statusIdx) else DownloadManager.STATUS_SUCCESSFUL
                            cursor.close()

                            if (status == DownloadManager.STATUS_SUCCESSFUL) {
                                _updateState.value = _updateState.value.copy(
                                    isDownloading = false,
                                    isReadyToInstall = true
                                )
                                promptInstall()
                            } else {
                                _updateState.value = _updateState.value.copy(
                                    isDownloading = false,
                                    errorMessage = "Download was interrupted"
                                )
                            }
                        } else {
                            cursor?.close()
                            promptInstall()
                        }
                    }
                }
            }

            // Must use RECEIVER_EXPORTED for system broadcasts like ACTION_DOWNLOAD_COMPLETE on Android 14+
            ContextCompat.registerReceiver(
                context,
                receiver,
                IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE),
                ContextCompat.RECEIVER_EXPORTED
            )
        } catch (e: Exception) {
            // Fallback: directly launch browser to download APK
            _updateState.value = _updateState.value.copy(
                isDownloading = false,
                errorMessage = "Opening download link directly..."
            )
            try {
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(browserIntent)
            } catch (_: Exception) {}
        }
    }

    fun promptInstall(apkFile: File? = null) {
        val file = apkFile ?: File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "ben-browser-update.apk")
        if (!file.exists() || file.length() == 0L) {
            _updateState.value = _updateState.value.copy(
                errorMessage = "Downloaded APK file not found",
                isDownloading = false
            )
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (!context.packageManager.canRequestPackageInstalls()) {
                val settingsIntent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                    data = Uri.parse("package:${context.packageName}")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(settingsIntent)
                return
            }
        }

        try {
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            val installIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/vnd.android.package-archive")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
            }
            context.startActivity(installIntent)
        } catch (e: Exception) {
            _updateState.value = _updateState.value.copy(
                errorMessage = "Failed to launch installer: ${e.message}"
            )
        }
    }

    private fun parseIsoTimestamp(isoString: String): Long = try {
        val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        format.parse(isoString)?.time ?: 0L
    } catch (_: Exception) {
        0L
    }
}
