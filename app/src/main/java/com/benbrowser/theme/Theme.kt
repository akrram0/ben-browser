package com.benbrowser.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = AccentBlue,
    onPrimary = TextPrimary,
    background = BgCanvas,
    onBackground = TextPrimary,
    surface = BgCanvas,
    onSurface = TextPrimary,
    outline = GlassBorder
)

@Composable
fun BenBrowserTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        content = content
    )
}
