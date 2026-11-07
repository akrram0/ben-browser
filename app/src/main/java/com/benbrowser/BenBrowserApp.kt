package com.benbrowser

import android.app.Application
import android.content.Intent
import android.os.Process
import android.util.Log
import java.io.PrintWriter
import java.io.StringWriter

class BenBrowserApp : Application() {
    override fun onCreate() {
        super.onCreate()

        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            try {
                Log.e("BenBrowser", "Uncaught exception caught by BenBrowserApp CrashGuard", throwable)
                val sw = StringWriter()
                throwable.printStackTrace(PrintWriter(sw))
                val stackTrace = sw.toString()

                val intent = Intent(this, CrashActivity::class.java).apply {
                    putExtra("CRASH_LOG", stackTrace)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                startActivity(intent)

                // Terminate crashed process cleanly
                Process.killProcess(Process.myPid())
                System.exit(10)
            } catch (e: Throwable) {
                defaultHandler?.uncaughtException(thread, throwable)
            }
        }
    }
}
