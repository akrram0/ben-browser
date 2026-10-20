package com.benbrowser.ui.components

import android.hardware.display.DisplayManager
import android.view.Choreographer
import android.view.Display
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

/**
 * Diagnostic-only frame stats overlay. Read it while/just after scrolling:
 * FPS, worst frame, share of missed frames and the display refresh rate.
 */
@Composable
fun FrameStatsHud(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var text by remember { mutableStateOf("measuring...") }
    val tracker = remember { FrameTracker() }

    DisposableEffect(Unit) {
        val choreographer = Choreographer.getInstance()
        val callback = object : Choreographer.FrameCallback {
            override fun doFrame(frameTimeNanos: Long) {
                tracker.onFrame(frameTimeNanos)
                choreographer.postFrameCallback(this)
            }
        }
        choreographer.postFrameCallback(callback)
        onDispose { choreographer.removeFrameCallback(callback) }
    }

    LaunchedEffect(Unit) {
        val dm = context.getSystemService(DisplayManager::class.java)
        val refreshHz = dm?.getDisplay(Display.DEFAULT_DISPLAY)?.refreshRate ?: 60f
        while (true) {
            delay(500)
            text = tracker.snapshot(refreshHz)
        }
    }

    Text(
        text = text,
        color = Color(0xFF9BE89B),
        fontSize = 11.sp,
        fontFamily = FontFamily.Monospace,
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(Color(0xAA000000))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    )
}

private class FrameTracker {
    private var lastFrameNanos = 0L
    private var windowStartNanos = 0L
    private var frames = 0
    private var missed = 0
    private var worstNanos = 0L

    fun onFrame(frameTimeNanos: Long) {
        if (lastFrameNanos != 0L) {
            val dt = frameTimeNanos - lastFrameNanos
            frames++
            if (dt > 20_000_000L) missed++
            if (dt > worstNanos) worstNanos = dt
        }
        lastFrameNanos = frameTimeNanos
        if (windowStartNanos == 0L) windowStartNanos = frameTimeNanos
    }

    fun snapshot(refreshHz: Float): String {
        val end = lastFrameNanos
        if (end == 0L || windowStartNanos == 0L) return "measuring..."
        val elapsed = end - windowStartNanos
        if (elapsed <= 0L) return "measuring..."
        val fps = ((frames * 1_000_000_000f) / elapsed).toInt()
        val worstMs = (worstNanos / 1_000_000f).toInt()
        val jankPct = if (frames > 0) missed * 100 / frames else 0
        frames = 0
        missed = 0
        worstNanos = 0L
        windowStartNanos = end
        return "FPS $fps | worst ${worstMs}ms | jank $jankPct% | ${refreshHz.toInt()}Hz"
    }
}
