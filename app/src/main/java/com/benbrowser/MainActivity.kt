package com.benbrowser

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.benbrowser.theme.AccentCyan
import com.benbrowser.theme.BenBrowserTheme
import com.benbrowser.theme.BgCanvas
import com.benbrowser.theme.TextPrimary
import com.benbrowser.theme.TextSecondary
import com.benbrowser.ui.BrowserViewModel
import java.io.PrintWriter
import java.io.StringWriter

class MainActivity : ComponentActivity() {
    private val viewModel: BrowserViewModel by viewModels()
    private var crashLog by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Crash Guard: catch any uncaught exceptions gracefully and present diagnostic info
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            val sw = StringWriter()
            throwable.printStackTrace(PrintWriter(sw))
            val stack = sw.toString()
            crashLog = stack
            defaultHandler?.uncaughtException(thread, throwable)
        }

        enableEdgeToEdge()
        handleIntent(intent)

        setContent {
            BenBrowserTheme {
                if (crashLog != null) {
                    CrashScreen(
                        errorDetails = crashLog ?: "Unknown error",
                        onCopyLog = {
                            val cm = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            cm.setPrimaryClip(ClipData.newPlainText("Crash Log", crashLog))
                            Toast.makeText(this, "Crash log copied to clipboard", Toast.LENGTH_SHORT).show()
                        },
                        onRestart = {
                            crashLog = null
                            recreate()
                        }
                    )
                } else {
                    BrowserScreen(viewModel = viewModel)
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        val url = intent?.dataString
        if (!url.isNullOrBlank()) {
            viewModel.loadUrl(url)
        }
    }
}

@Composable
private fun CrashScreen(
    errorDetails: String,
    onCopyLog: () -> Unit,
    onRestart: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgCanvas)
            .statusBarsPadding()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFF141722))
                .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(20.dp))
                .padding(24.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Ben Browser Recovery",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = AccentCyan
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "A startup component encountered an issue. Diagnostic details below:",
                fontSize = 13.sp,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF090A0F))
                    .padding(12.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = errorDetails,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    color = Color(0xFFFF6B6B)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(AccentCyan)
                    .clickable(onClick = onCopyLog),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Copy Crash Log",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = BgCanvas
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0x22FFFFFF))
                    .clickable(onClick = onRestart),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Restart App",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary
                )
            }
        }
    }
}
