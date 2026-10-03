package com.benbrowser.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.benbrowser.R
import com.benbrowser.data.Bookmark
import com.benbrowser.theme.AccentBlue
import com.benbrowser.theme.GlassBorder
import com.benbrowser.theme.TextPrimary
import com.benbrowser.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookmarksSheet(
    bookmarks: List<Bookmark>,
    isCurrentBookmarked: Boolean,
    currentUrl: String,
    onToggleBookmark: () -> Unit,
    onOpenStartPage: () -> Unit,
    onSelectBookmark: (String) -> Unit,
    onDeleteBookmark: (String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF16181D),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 10.dp)
                    .width(36.dp)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(Color(0x40FFFFFF))
            )
        },
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .navigationBarsPadding()
        ) {
            // Header: Apple Safari title and Done action
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Bookmarks",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Default,
                    color = TextPrimary
                )

                Text(
                    text = "Done",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AccentBlue,
                    modifier = Modifier
                        .clickable(onClick = onDismiss)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }

            // Quick Actions: Bookmark Current Page
            if (currentUrl.isNotBlank()) {
                val toggleInteraction = remember { MutableInteractionSource() }
                val togglePressed by toggleInteraction.collectIsPressedAsState()
                val toggleScale by animateFloatAsState(
                    targetValue = if (togglePressed) 0.98f else 1f,
                    animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                    label = "toggle_scale"
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 14.dp)
                        .graphicsLayer {
                            scaleX = toggleScale
                            scaleY = toggleScale
                        }
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF22242B))
                        .border(BorderStroke(1.dp, GlassBorder), RoundedCornerShape(16.dp))
                        .clickable(
                            interactionSource = toggleInteraction,
                            indication = null,
                            onClick = onToggleBookmark
                        )
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(
                            if (isCurrentBookmarked) R.drawable.ic_apple_star_filled else R.drawable.ic_apple_star
                        ),
                        contentDescription = "Bookmark",
                        tint = if (isCurrentBookmarked) AccentBlue else TextPrimary,
                        modifier = Modifier.size(20.dp)
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = if (isCurrentBookmarked) "Remove from Bookmarks" else "Bookmark This Page",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (isCurrentBookmarked) AccentBlue else TextPrimary,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Quick Action: Return to Start Page
            val homeInteraction = remember { MutableInteractionSource() }
            val homePressed by homeInteraction.collectIsPressedAsState()
            val homeScale by animateFloatAsState(
                targetValue = if (homePressed) 0.98f else 1f,
                animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                label = "home_scale"
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .graphicsLayer {
                        scaleX = homeScale
                        scaleY = homeScale
                    }
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF1E2026))
                    .border(BorderStroke(1.dp, GlassBorder), RoundedCornerShape(16.dp))
                    .clickable(
                        interactionSource = homeInteraction,
                        indication = null
                    ) {
                        onOpenStartPage()
                        onDismiss()
                    }
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_apple_book),
                    contentDescription = "Favorites",
                    tint = AccentBlue,
                    modifier = Modifier.size(18.dp)
                )

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = "Favorites & Start Page",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary,
                    modifier = Modifier.weight(1f)
                )
            }

            Text(
                text = "SAVED BOOKMARKS",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextSecondary,
                modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
            )

            if (bookmarks.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No saved bookmarks",
                        fontSize = 14.sp,
                        color = TextSecondary
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(bookmarks, key = { it.id }) { bookmark ->
                        BookmarkItemRow(
                            bookmark = bookmark,
                            onClick = {
                                onSelectBookmark(bookmark.url)
                                onDismiss()
                            },
                            onDelete = { onDeleteBookmark(bookmark.id) }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun BookmarkItemRow(
    bookmark: Bookmark,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val rowInteraction = remember { MutableInteractionSource() }
    val rowPressed by rowInteraction.collectIsPressedAsState()
    val rowScale by animateFloatAsState(
        targetValue = if (rowPressed) 0.98f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "bookmark_row_scale"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = rowScale
                scaleY = rowScale
            }
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF1E2026))
            .border(BorderStroke(1.dp, GlassBorder), RoundedCornerShape(16.dp))
            .clickable(
                interactionSource = rowInteraction,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val initial = bookmark.title.trim().take(1).uppercase().ifBlank { "?" }
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(Color(0xFF2C2D35)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = initial,
                color = AccentBlue,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = bookmark.title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = bookmark.url,
                fontSize = 12.sp,
                color = TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        IconButton(
            onClick = onDelete,
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Remove Bookmark",
                tint = TextSecondary,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}
