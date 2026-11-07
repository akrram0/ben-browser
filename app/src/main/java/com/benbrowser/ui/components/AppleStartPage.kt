package com.benbrowser.ui.components

import android.os.Build
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.benbrowser.R
import com.benbrowser.data.Bookmark
import com.benbrowser.data.BookmarkRepository
import com.benbrowser.data.UpdateInfo
import com.benbrowser.theme.AccentCyan
import com.benbrowser.theme.AccentCyanGlow
import com.benbrowser.theme.BgCanvas
import com.benbrowser.theme.CardSurface
import com.benbrowser.theme.TextPrimary
import com.benbrowser.theme.TextSecondary
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy
import com.kyant.backdrop.highlight.Highlight
import com.kyant.backdrop.shadow.Shadow
import com.kyant.shapes.Capsule
import com.kyant.shapes.RoundedRectangle
import java.util.Calendar

private val LiquidGlassPillBorder = BorderStroke(
    width = 1.dp,
    brush = Brush.verticalGradient(
        colors = listOf(
            Color(0x60FFFFFF),
            Color(0x18FFFFFF)
        )
    )
)

private val LiquidGlassFillBrush = Brush.verticalGradient(
    colors = listOf(
        Color(0x352E3444),
        Color(0x1E1A1E28)
    )
)

private val TileShape = RoundedCornerShape(18.dp)

private fun Modifier.safeGlass(
    backdrop: Backdrop?,
    shape: Shape,
    backdropShape: () -> com.kyant.shapes.Shape,
    blurRadius: Dp = 8.dp,
    lensNear: Dp = 10.dp,
    lensFar: Dp = 14.dp,
    highlightAlpha: Float = 0.75f,
    shadowColor: Color = Color.Black.copy(alpha = 0.35f),
    fillBrush: Brush = LiquidGlassFillBrush
): Modifier {
    return if (backdrop != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        try {
            this.drawBackdrop(
                backdrop = backdrop,
                shape = backdropShape,
                effects = {
                    vibrancy()
                    blur(blurRadius.toPx())
                    lens(lensNear.toPx(), lensFar.toPx())
                },
                highlight = { Highlight(width = 1.dp, alpha = highlightAlpha) },
                shadow = { Shadow(radius = 14.dp, color = shadowColor) },
                onDrawSurface = { drawRect(fillBrush) }
            )
        } catch (_: Throwable) {
            this
                .background(fillBrush, shape)
                .border(LiquidGlassPillBorder, shape)
        }
    } else {
        this
            .background(fillBrush, shape)
            .border(LiquidGlassPillBorder, shape)
    }
}

@Composable
fun StartPage(
    bookmarks: List<Bookmark>,
    searchEngine: String,
    isUpdateAvailable: Boolean = false,
    onSelectBookmark: (String) -> Unit,
    onOpenSearch: () -> Unit,
    onOpenSettings: () -> Unit,
    backdrop: Backdrop? = null,
    modifier: Modifier = Modifier
) {
    val allItems = remember(bookmarks) {
        bookmarks + Bookmark("fav_add_custom", "+ Add", "", 0L)
    }
    val rows = remember(allItems) { allItems.chunked(4) }

    val currentHour = remember { Calendar.getInstance().get(Calendar.HOUR_OF_DAY) }
    val (greetingTitle, greetingSubtitle) = remember(currentHour) {
        when (currentHour) {
            in 5..11 -> "Good Morning" to "Fast, private, and distraction-free."
            in 12..17 -> "Good Afternoon" to "Ready to explore the open web."
            in 18..22 -> "Good Evening" to "Unwind, research, and discover."
            else -> "Late Night Mode" to "Pure speed in the dark."
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BgCanvas)
            .statusBarsPadding(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = FLOATING_BAR_MAX_WIDTH)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 20.dp)
                .padding(bottom = 110.dp)
        ) {
            // Section 1: Ben Browser Brand Header + Settings Action
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Ben Glowing Brand Monogram Pill
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .safeGlass(
                                backdrop = backdrop,
                                shape = RoundedCornerShape(12.dp),
                                backdropShape = { RoundedRectangle(12f.dp) },
                                blurRadius = 8.dp,
                                lensNear = 10.dp,
                                lensFar = 14.dp,
                                highlightAlpha = 0.8f,
                                shadowColor = AccentCyanGlow
                            )
                            .clip(RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "B",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = AccentCyan
                        )
                    }

                    Column {
                        Text(
                            text = greetingTitle,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            lineHeight = 28.sp
                        )
                        Text(
                            text = greetingSubtitle,
                            fontSize = 12.sp,
                            color = TextSecondary,
                            fontWeight = FontWeight.Normal
                        )
                    }
                }

                val settingsSource = remember { MutableInteractionSource() }
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .safeGlass(
                            backdrop = backdrop,
                            shape = CircleShape,
                            backdropShape = { Capsule() },
                            blurRadius = 8.dp,
                            lensNear = 10.dp,
                            lensFar = 14.dp,
                            highlightAlpha = 0.7f,
                            shadowColor = Color.Black.copy(alpha = 0.3f)
                        )
                        .clip(CircleShape)
                        .clickable(
                            interactionSource = settingsSource,
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
                                .padding(top = 4.dp, end = 4.dp)
                                .clip(CircleShape)
                                .background(AccentCyan)
                        )
                    }
                }
            }

            // Section 2: Hero Liquid Glass Search Capsule
            val searchSource = remember { MutableInteractionSource() }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .safeGlass(
                        backdrop = backdrop,
                        shape = CircleShape,
                        backdropShape = { Capsule() },
                        blurRadius = 10.dp,
                        lensNear = 12.dp,
                        lensFar = 18.dp,
                        highlightAlpha = 0.75f,
                        shadowColor = Color.Black.copy(alpha = 0.35f)
                    )
                    .clip(CircleShape)
                    .clickable(
                        interactionSource = searchSource,
                        indication = null,
                        onClick = onOpenSearch
                    )
                    .padding(horizontal = 18.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_apple_search),
                        contentDescription = "Search",
                        tint = AccentCyan,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Search with $searchEngine or enter address...",
                        fontSize = 14.sp,
                        color = TextSecondary,
                        fontWeight = FontWeight.Normal,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Section 3: Favorites Section Title + Count Chip
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Favorites",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )

                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color(0x18FFFFFF))
                        .padding(horizontal = 10.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "${bookmarks.size}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextSecondary
                    )
                }
            }

            // Section 4: 4-Column App Icons Grid
            rows.forEach { rowItems ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    for (i in 0 until 4) {
                        val item = rowItems.getOrNull(i)
                        if (item != null) {
                            if (item.id == "fav_add_custom") {
                                AddTile(
                                    backdrop = backdrop,
                                    onClick = onOpenSettings,
                                    modifier = Modifier.weight(1f)
                                )
                            } else {
                                FavoriteTile(
                                    bookmark = item,
                                    backdrop = backdrop,
                                    onClick = { onSelectBookmark(item.url) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        } else {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FavoriteTile(
    bookmark: Bookmark,
    backdrop: Backdrop?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val brandDrawableId = remember(bookmark.url, bookmark.title) {
        val cleanUrl = bookmark.url.lowercase()
        val cleanTitle = bookmark.title.lowercase()
        when {
            cleanUrl.contains("duckduckgo") || cleanTitle.contains("duckduckgo") -> R.drawable.ic_brand_duckduckgo
            cleanUrl.contains("github") || cleanTitle.contains("github") -> R.drawable.ic_brand_github
            cleanUrl.contains("youtube") || cleanTitle.contains("youtube") -> R.drawable.ic_brand_youtube
            cleanUrl.contains("apple") || cleanTitle.contains("apple") -> R.drawable.ic_brand_apple
            cleanUrl.contains("wikipedia") || cleanTitle.contains("wikipedia") -> R.drawable.ic_brand_wikipedia
            cleanUrl.contains("ycombinator") || cleanTitle.contains("hacker") -> R.drawable.ic_brand_hackernews
            cleanUrl.contains("reddit") || cleanTitle.contains("reddit") -> R.drawable.ic_brand_reddit
            cleanUrl.contains("google") || cleanTitle.contains("google") -> R.drawable.ic_brand_google
            cleanUrl.contains("x.com") || cleanUrl.contains("twitter") || cleanTitle.contains("twitter") || cleanTitle == "x" -> R.drawable.ic_brand_x
            else -> null
        }
    }

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1f,
        animationSpec = spring(dampingRatio = 0.72f, stiffness = 320f),
        label = "tile_press"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
    ) {
        Box(
            modifier = Modifier
                .size(62.dp)
                .safeGlass(
                    backdrop = backdrop,
                    shape = TileShape,
                    backdropShape = { RoundedRectangle(18f.dp) },
                    blurRadius = 8.dp,
                    lensNear = 12.dp,
                    lensFar = 16.dp,
                    highlightAlpha = 0.75f,
                    shadowColor = Color.Black.copy(alpha = 0.35f)
                )
                .clip(TileShape),
            contentAlignment = Alignment.Center
        ) {
            if (brandDrawableId != null) {
                Icon(
                    painter = painterResource(brandDrawableId),
                    contentDescription = bookmark.title,
                    tint = Color.Unspecified,
                    modifier = Modifier.size(32.dp)
                )
            } else {
                val monogram = bookmark.title.trim().take(2).uppercase().ifBlank { "B" }
                Text(
                    text = monogram,
                    color = TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(7.dp))

        Text(
            text = bookmark.title,
            color = TextSecondary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun AddTile(
    backdrop: Backdrop?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1f,
        animationSpec = spring(dampingRatio = 0.72f, stiffness = 320f),
        label = "add_press"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
    ) {
        Box(
            modifier = Modifier
                .size(62.dp)
                .safeGlass(
                    backdrop = backdrop,
                    shape = TileShape,
                    backdropShape = { RoundedRectangle(18f.dp) },
                    blurRadius = 8.dp,
                    lensNear = 12.dp,
                    lensFar = 16.dp,
                    highlightAlpha = 0.5f,
                    fillBrush = Brush.verticalGradient(
                        colors = listOf(Color(0x22FFFFFF), Color(0x10FFFFFF))
                    )
                )
                .clip(TileShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "+",
                fontSize = 24.sp,
                fontWeight = FontWeight.Light,
                color = AccentCyan
            )
        }

        Spacer(modifier = Modifier.height(7.dp))

        Text(
            text = "Add",
            color = TextSecondary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            maxLines = 1,
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
        containerColor = CardSurface,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 10.dp)
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
                .padding(horizontal = 20.dp)
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
                    text = "Preferences",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "Done",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = AccentCyan,
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
                fontWeight = FontWeight.Medium,
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
                                        .border(0.5.dp, AccentCyan, CircleShape)
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
                            color = if (isSelected) AccentCyan else TextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Section 2: Add Favorite (+)
            Text(
                text = "Add to Favorites",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
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
                        text = "Site Name (e.g. GitHub, Reddit)",
                        fontSize = 14.sp,
                        color = TextSecondary
                    )
                }
                BasicTextField(
                    value = titleInput,
                    onValueChange = { titleInput = it },
                    singleLine = true,
                    textStyle = TextStyle(color = TextPrimary, fontSize = 14.sp),
                    cursorBrush = SolidColor(AccentCyan),
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
                            text = "URL (e.g. github.com)",
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
                        cursorBrush = SolidColor(AccentCyan),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Box(
                    modifier = Modifier
                        .height(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0x38FFFFFF))
                        .border(BorderStroke(1.dp, AccentCyan), RoundedCornerShape(12.dp))
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
                        color = AccentCyan
                    )
                }
            }

            // Section 3: Application Updates
            Text(
                text = "Application Updates",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
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
                                color = AccentCyan,
                                strokeWidth = 2.dp
                            )
                        } else if (updateInfo.isDownloading) {
                            Text(
                                text = "Downloading...",
                                fontSize = 13.sp,
                                color = AccentCyan,
                                fontWeight = FontWeight.Bold
                            )
                        } else if (updateInfo.isReadyToInstall) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(AccentCyan)
                                    .clickable(onClick = onInstallUpdate)
                                    .padding(horizontal = 14.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "Install",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BgCanvas
                                )
                            }
                        } else if (updateInfo.isAvailable) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(AccentCyan)
                                    .clickable(onClick = onDownloadUpdate)
                                    .padding(horizontal = 14.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "Update",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BgCanvas
                                )
                            }
                        } else {
                            Text(
                                text = "Check",
                                fontSize = 13.sp,
                                color = AccentCyan,
                                fontWeight = FontWeight.SemiBold,
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

// Backward compatibility alias
@Composable
fun AppleStartPage(
    bookmarks: List<Bookmark>,
    searchEngine: String,
    isUpdateAvailable: Boolean = false,
    onSelectBookmark: (String) -> Unit,
    onOpenSearch: () -> Unit,
    onOpenSettings: () -> Unit,
    backdrop: Backdrop? = null,
    modifier: Modifier = Modifier
) = StartPage(
    bookmarks = bookmarks,
    searchEngine = searchEngine,
    isUpdateAvailable = isUpdateAvailable,
    onSelectBookmark = onSelectBookmark,
    onOpenSearch = onOpenSearch,
    onOpenSettings = onOpenSettings,
    backdrop = backdrop,
    modifier = modifier
)
