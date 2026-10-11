package com.benbrowser.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
import com.benbrowser.theme.AccentBlue
import com.benbrowser.theme.BgCanvas
import com.benbrowser.theme.GlassBorder
import com.benbrowser.theme.TextPrimary
import com.benbrowser.theme.TextSecondary

private val TileGradientBrush = Brush.verticalGradient(
    colors = listOf(
        Color(0xFF24262E),
        Color(0xFF16181D)
    )
)

private val TileShape = RoundedCornerShape(16.dp)
private val CardBorder = BorderStroke(1.dp, GlassBorder)

@Composable
fun AppleStartPage(
    bookmarks: List<Bookmark>,
    searchEngine: String,
    onSelectBookmark: (String) -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val rows = remember(bookmarks) { bookmarks.chunked(4) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BgCanvas)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(
                start = 24.dp,
                end = 24.dp,
                top = 28.dp,
                bottom = 140.dp
            )
    ) {
        // Section 1: Favorites Header + Settings / Quick Add (+) Button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Favorites",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Default,
                color = TextPrimary
            )

            Row(
                modifier = Modifier
                    .height(34.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1C1E26))
                    .border(CardBorder, CircleShape)
                    .clickable(onClick = onOpenSettings)
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Settings & Add",
                    tint = AccentBlue,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Settings",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Normal,
                    color = TextPrimary
                )
            }
        }

        // Section 1: Zero-Subcomposition 4-Column Safari Favorites Grid
        rows.forEach { rowItems ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
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

        Spacer(modifier = Modifier.height(8.dp))

        // Section 2: Safari Privacy Report Card
        Text(
            text = "Privacy Report",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Default,
            color = TextPrimary,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(TileShape)
                .background(Color(0xFF16181D))
                .border(CardBorder, TileShape)
                .clickable(onClick = onOpenSettings)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0x1F0A84FF)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_apple_lock),
                    contentDescription = "Privacy",
                    tint = AccentBlue,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "$searchEngine Search • Gecko WebRender",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Global Privacy Control and native dark mode are active across all websites.",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal,
                    color = TextSecondary,
                    lineHeight = 17.sp
                )
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
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(TileShape)
                .background(TileGradientBrush)
                .border(CardBorder, TileShape),
            contentAlignment = Alignment.Center
        ) {
            val initial = bookmark.title.trim().take(1).uppercase().ifBlank { "?" }
            Text(
                text = initial,
                color = AccentBlue,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
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
    onSelectSearchEngine: (String) -> Unit,
    onAddBookmark: (String, String) -> Unit,
    onResetBookmarks: () -> Unit,
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
        containerColor = Color(0xFF16181D),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 8.dp)
                    .width(36.dp)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(Color(0x40FFFFFF))
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
                    text = "Settings & Quick Add",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "Done",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = AccentBlue,
                    modifier = Modifier
                        .clickable(onClick = onDismiss)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }

            // Section 1: Default Search Engine Selector
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
                    .padding(bottom = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                engines.forEach { engine ->
                    val isSelected = searchEngine == engine
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) AccentBlue else Color(0xFF22242B))
                            .border(
                                BorderStroke(1.dp, if (isSelected) AccentBlue else GlassBorder),
                                RoundedCornerShape(12.dp)
                            )
                            .clickable { onSelectSearchEngine(engine) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = engine,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }
                }
            }

            // Section 2: Add New Favorite (+)
            Text(
                text = "Add Favorite to Home Screen (+)",
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
                    .background(Color(0xFF22242B))
                    .border(CardBorder, RoundedCornerShape(12.dp))
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
                    cursorBrush = SolidColor(AccentBlue),
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
                        .background(Color(0xFF22242B))
                        .border(CardBorder, RoundedCornerShape(12.dp))
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
                        cursorBrush = SolidColor(AccentBlue),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Box(
                    modifier = Modifier
                        .height(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(AccentBlue)
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
                        text = "+ Add",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }
            }

            // Section 3: Reset Default Favorites
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF22242B))
                    .border(CardBorder, RoundedCornerShape(12.dp))
                    .clickable {
                        onResetBookmarks()
                        onDismiss()
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Reset Default Favorites (DuckDuckGo First)",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    color = TextSecondary
                )
            }

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}
