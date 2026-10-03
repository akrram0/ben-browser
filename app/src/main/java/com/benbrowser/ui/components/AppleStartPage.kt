package com.benbrowser.ui.components

import androidx.compose.animation.core.Spring
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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.benbrowser.R
import com.benbrowser.data.Bookmark
import com.benbrowser.theme.AccentBlue
import com.benbrowser.theme.BgCanvas
import com.benbrowser.theme.GlassBorder
import com.benbrowser.theme.InputFieldBg
import com.benbrowser.theme.TextPrimary
import com.benbrowser.theme.TextSecondary

@Composable
fun AppleStartPage(
    bookmarks: List<Bookmark>,
    onSelectBookmark: (String) -> Unit,
    onSearchClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val searchInteraction = remember { MutableInteractionSource() }
    val searchPressed by searchInteraction.collectIsPressedAsState()
    val searchScale by animateFloatAsState(
        targetValue = if (searchPressed) 0.97f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "search_pill_scale"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BgCanvas)
            .statusBarsPadding()
            .padding(horizontal = 24.dp)
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        // Safari-Style Hero Search Pill with tactile spring scale
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .graphicsLayer {
                    scaleX = searchScale
                    scaleY = searchScale
                }
                .clip(CircleShape)
                .background(InputFieldBg)
                .border(BorderStroke(1.dp, GlassBorder), CircleShape)
                .clickable(
                    interactionSource = searchInteraction,
                    indication = null,
                    onClick = onSearchClick
                )
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_apple_search),
                contentDescription = "Search",
                tint = TextSecondary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "Search or enter website name",
                color = TextSecondary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Normal,
                fontFamily = FontFamily.Default
            )
        }

        Spacer(modifier = Modifier.height(36.dp))

        // Apple Safari Large Title
        Text(
            text = "Favorites",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Default,
            color = TextPrimary
        )

        Text(
            text = "Frequently visited & quick access",
            fontSize = 13.sp,
            fontWeight = FontWeight.Normal,
            fontFamily = FontFamily.Default,
            color = TextSecondary,
            modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
        )

        // Safari-Style Favorites Grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            contentPadding = PaddingValues(bottom = 120.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(bookmarks, key = { it.id }) { bookmark ->
                FavoriteTile(
                    bookmark = bookmark,
                    onClick = { onSelectBookmark(bookmark.url) }
                )
            }
        }
    }
}

@Composable
private fun FavoriteTile(
    bookmark: Bookmark,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "tile_scale"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
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
        // Glass Icon Container (radius_md 16.dp) with Apple Dark Slate Gradient
        Box(
            modifier = Modifier
                .size(68.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF24262E),
                            Color(0xFF16181D)
                        )
                    )
                )
                .border(BorderStroke(1.dp, GlassBorder), RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            // Stylized initial badge in Apple typography
            val initial = bookmark.title.trim().take(1).uppercase().ifBlank { "?" }
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF2C2E38)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initial,
                    color = AccentBlue,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Title
        Text(
            text = bookmark.title,
            color = TextPrimary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
