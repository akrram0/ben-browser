package com.benbrowser.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.benbrowser.R
import com.benbrowser.data.Bookmark
import com.benbrowser.data.BookmarkRepository
import com.benbrowser.data.UpdateInfo
import com.benbrowser.theme.AccentBlue
import com.benbrowser.theme.BgCanvas
import com.benbrowser.theme.GlassBorder
import com.benbrowser.theme.TextPrimary
import com.benbrowser.theme.TextSecondary
import java.util.Calendar

private val LiquidGlassPillBorder = BorderStroke(
    width = 1.dp,
    brush = Brush.verticalGradient(
        colors = listOf(
            Color(0x38FFFFFF),
            Color(0x14FFFFFF)
        )
    )
)

private val LiquidGlassFillBrush = Brush.verticalGradient(
    colors = listOf(
        Color(0x2E252834),
        Color(0x1A15171F)
    )
)

private val TileShape = RoundedCornerShape(18.dp)

private val GREETINGS = listOf(
    "What are we cooking today?",
    "Locked in.",
    "Main character energy.",
    "Search the void.",
    "No cap, let's browse.",
    "Doomscrolling or doing work?",
    "Late night thoughts hit different.",
    "Living rent-free in your tabs."
)

@Composable
fun AppleStartPage(
    bookmarks: List<Bookmark>,
    searchEngine: String,
    isUpdateAvailable: Boolean = false,
    onSelectBookmark: (String) -> Unit,
    onOpenSearch: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val rows = remember(bookmarks) { bookmarks.chunked(4) }
    val greeting = remember { GREETINGS.random() }
    val currentHour = remember { Calendar.getInstance().get(Calendar.HOUR_OF_DAY) }
    val vibeSubtitle = remember(currentHour) {
        when (currentHour) {
            in 5..11 -> "Morning focus • Stay curious"
            in 12..17 -> "Afternoon flow • In the zone"
            in 18..22 -> "Evening wind down • Pure vibe"
            else -> "Late night mode • The void awaits"
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BgCanvas)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(
                start = 22.dp,
                end = 22.dp,
                top = 24.dp,
                bottom = 120.dp
            )
    ) {
        // Section 1: Dynamic Gen-Z Welcome Header + Liquid Glass Settings Button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = greeting,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Default,
                    color = TextPrimary,
                    lineHeight = 30.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = vibeSubtitle,
                    fontSize = 13.sp,
                    color = TextSecondary,
                    fontWeight = FontWeight.Normal
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Liquid Glass Settings Pill
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(LiquidGlassFillBrush, CircleShape)
                    .border(LiquidGlassPillBorder, CircleShape)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onOpenSettings
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_apple_sliders),
                    contentDescription = "Settings",
                    tint = TextPrimary,
                    modifier = Modifier.size(18.dp)
                )

                if (isUpdateAvailable) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .align(Alignment.TopEnd)
                            .padding(top = 2.dp, end = 2.dp)
                            .clip(CircleShape)
                            .background(AccentBlue)
                    )
                }
            }
        }

        // Section 2: Hero Liquid Glass Search Capsule
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .clip(CircleShape)
                .background(LiquidGlassFillBrush, CircleShape)
                .border(LiquidGlassPillBorder, CircleShape)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onOpenSearch
                )
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_apple_search),
                    contentDescription = "Search",
                    tint = TextSecondary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Search the void or enter URL...",
                    fontSize = 14.sp,
                    color = TextSecondary,
                    fontWeight = FontWeight.Normal,
                    maxLines = 1
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Section 3: Favorites Section Title
        Text(
            text = "Favorites",
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextSecondary,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Section 4: 4-Column Pure Liquid Glass App Icons Grid
        rows.forEach { rowItems ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 22.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                for (i in 0 until 4) {
                    val bookmark = rowItems.getOrNull(i)
                    if (bookmark != null) {
                        FavoriteTile(
                            bookmark = bookmark,
                            onClick = { onSelectBookmark(bookmark.url) },
                            modifier = Modifier.weight(1f)
                        )
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun FavoriteTile(
    bookmark: Bookmark,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cleanUrl = bookmark.url.lowercase()
    val cleanTitle = bookmark.title.lowercase()

    val brandDrawableId = when {
        cleanUrl.contains("duckduckgo") || cleanTitle.contains("duckduckgo") -> R.drawable.ic_brand_duckduckgo
        cleanUrl.contains("github") || cleanTitle.contains("github") -> R.drawable.ic_brand_github
        cleanUrl.contains("youtube") || cleanTitle.contains("youtube") -> R.drawable.ic_brand_youtube
        cleanUrl.contains("apple") || cleanTitle.contains("apple") -> R.drawable.ic_brand_apple
        cleanUrl.contains("wikipedia") || cleanTitle.contains("wikipedia") -> R.drawable.ic_brand_wikipedia
        cleanUrl.contains("ycombinator") || cleanTitle.contains("hacker") -> R.drawable.ic_brand_hackernews
        cleanUrl.contains("reddit") || cleanTitle.contains("reddit") -> R.drawable.ic_brand_reddit
        cleanUrl.contains("google") || cleanTitle.contains("google") -> R.drawable.ic_brand_google
        else -> null
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = onClick
        )
    ) {
        // Individual Liquid Glass Container with Specular Glass Border
        Box(
            modifier = Modifier
                .size(62.dp)
                .clip(TileShape)
                .background(LiquidGlassFillBrush, TileShape)
                .border(LiquidGlassPillBorder, TileShape),
            contentAlignment = Alignment.Center
        ) {
            if (brandDrawableId != null) {
                Icon(
                    painter = painterResource(brandDrawableId),
                    contentDescription = bookmark.title,
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            } else {
                val monogram = bookmark.title.trim().take(2).uppercase().ifBlank { "?" }
                Text(
                    text = monogram,
                    color = TextPrimary,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = bookmark.title,
            color = TextPrimary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Normal,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsSheet(
    searchEngine: String,
    updateInfo: UpdateInfo,
    onSelectSearchEngine: (String) -> Unit,
    onAddBookmark: (String, String) -> Unit,
    onResetBookmarks: () -> Unit,
    onCheckForUpdates: () -> Unit,
    onDownloadUpdate: () -> Unit,
    onInstallUpdate: () -> Unit,
    onDismiss: () -> Unit
) {
    var titleInput by remember { mutableStateOf("") }
    var urlInput by remember { mutableStateOf("") }

    val engines = listOf(
        BookmarkRepository.ENGINE_DUCKDUCKGO,
        BookmarkRepository.ENGINE_GOOGLE,
        BookmarkRepository.ENGINE_BRAVE
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF14161C),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 8.dp)
                    .width(36.dp)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(Color(0x38FFFFFF))
            )
        },
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .navigationBarsPadding()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Settings",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "Done",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(onClick = onDismiss)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }

            // Section 1: Liquid Glass Segmented Search Engine Selector
            Text(
                text = "Default Search Engine",
                fontSize = 13.sp,
                fontWeight = FontWeight.Normal,
                color = TextSecondary,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .clip(CircleShape)
                    .background(Color(0x2E252834))
                    .border(LiquidGlassPillBorder, CircleShape)
                    .padding(3.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                engines.forEach { engine ->
                    val isSelected = searchEngine == engine
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(CircleShape)
                            .then(
                                if (isSelected) {
                                    Modifier
                                        .background(Color(0x38FFFFFF), CircleShape)
                                        .border(1.dp, Color(0x38FFFFFF), CircleShape)
                                } else {
                                    Modifier
                                }
                            )
                            .clickable { onSelectSearchEngine(engine) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = engine,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) TextPrimary else TextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Section 2: Add Favorite (+)
            Text(
                text = "Add to Favorites",
                fontSize = 13.sp,
                fontWeight = FontWeight.Normal,
                color = TextSecondary,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0x2E252834))
                    .border(LiquidGlassPillBorder, RoundedCornerShape(12.dp))
                    .padding(horizontal = 14.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                if (titleInput.isEmpty()) {
                    Text(
                        text = "Site Name (e.g. X, ChatGPT)",
                        fontSize = 14.sp,
                        color = TextSecondary
                    )
                }
                BasicTextField(
                    value = titleInput,
                    onValueChange = { titleInput = it },
                    singleLine = true,
                    textStyle = TextStyle(color = TextPrimary, fontSize = 14.sp),
                    cursorBrush = SolidColor(Color.White),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0x2E252834))
                        .border(LiquidGlassPillBorder, RoundedCornerShape(12.dp))
                        .padding(horizontal = 14.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (urlInput.isEmpty()) {
                        Text(
                            text = "URL (e.g. x.com)",
                            fontSize = 14.sp,
                            color = TextSecondary
                        )
                    }
                    BasicTextField(
                        value = urlInput,
                        onValueChange = { urlInput = it },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Uri,
                            imeAction = ImeAction.Done
                        ),
                        textStyle = TextStyle(color = TextPrimary, fontSize = 14.sp),
                        cursorBrush = SolidColor(Color.White),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Box(
                    modifier = Modifier
                        .height(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0x38FFFFFF))
                        .border(LiquidGlassPillBorder, RoundedCornerShape(12.dp))
                        .clickable {
                            if (urlInput.isNotBlank()) {
                                onAddBookmark(titleInput, urlInput)
                                titleInput = ""
                                urlInput = ""
                                onDismiss()
                            }
                        }
                        .padding(horizontal = 18.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Add",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }
            }

            // Section 3: Application Updates (Plan A GitHub Releases)
            Text(
                text = "Application Updates",
                fontSize = 13.sp,
                fontWeight = FontWeight.Normal,
                color = TextSecondary,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0x2E252834))
                    .border(LiquidGlassPillBorder, RoundedCornerShape(12.dp))
                    .padding(14.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (updateInfo.isAvailable) "Update Available" else "Ben Browser",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = if (updateInfo.isAvailable) {
                                    "New build ${updateInfo.remoteTag.ifBlank { "ready to install" }}"
                                } else {
                                    "Up to date with GitHub main"
                                },
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                        }

                        if (updateInfo.isChecking) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else if (updateInfo.isDownloading) {
                            Text(
                                text = "Downloading...",
                                fontSize = 13.sp,
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        } else if (updateInfo.isReadyToInstall) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0x38FFFFFF))
                                    .border(LiquidGlassPillBorder, RoundedCornerShape(8.dp))
                                    .clickable(onClick = onInstallUpdate)
                                    .padding(horizontal = 14.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "Install",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            }
                        } else if (updateInfo.isAvailable) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0x38FFFFFF))
                                    .border(LiquidGlassPillBorder, RoundedCornerShape(8.dp))
                                    .clickable(onClick = onDownloadUpdate)
                                    .padding(horizontal = 14.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "Update",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            }
                        } else {
                            Text(
                                text = "Check",
                                fontSize = 13.sp,
                                color = TextSecondary,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier
                                    .clickable(onClick = onCheckForUpdates)
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    if (!updateInfo.errorMessage.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = updateInfo.errorMessage,
                            fontSize = 11.sp,
                            color = Color(0xFFFF453A)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Section 4: Reset Default Favorites
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0x1F252834))
                    .border(BorderStroke(1.dp, Color(0x14FFFFFF)), RoundedCornerShape(12.dp))
                    .clickable {
                        onResetBookmarks()
                        onDismiss()
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Reset Default Favorites",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Normal,
                    color = TextSecondary
                )
            }

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}
