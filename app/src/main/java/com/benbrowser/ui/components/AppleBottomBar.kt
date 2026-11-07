package com.benbrowser.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.benbrowser.R
import com.benbrowser.theme.AccentCyan
import com.benbrowser.theme.DisabledTint
import com.benbrowser.theme.TextPrimary
import com.benbrowser.theme.TextSecondary
import com.benbrowser.ui.BrowserState
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy
import com.kyant.backdrop.highlight.Highlight
import com.kyant.backdrop.shadow.Shadow
import com.kyant.shapes.Capsule
import kotlin.math.abs

private val BenDpSpring = spring<Dp>(dampingRatio = 0.74f, stiffness = 340f)
private const val STRETCH = 0.14f
private const val SQUASH = 0.45f

val FLOATING_BAR_MAX_WIDTH = 440.dp
private val PillShape = RoundedCornerShape(percent = 50)

// Liquid Glass: translucent refraction fill with specular highlight border
private val LiquidGlassFillBrush = Brush.verticalGradient(
    colors = listOf(
        Color(0x99181B26),
        Color(0xCC0E1018)
    )
)

private val BottomScrimBrush = Brush.verticalGradient(
    colors = listOf(
        Color.Transparent,
        Color(0x99050608)
    )
)

private enum class BarMode {
    Expanded,
    Collapsed,
    Editing
}

@Composable
fun BenBottomBar(
    state: BrowserState,
    onBack: () -> Unit,
    onForward: () -> Unit,
    onReload: () -> Unit,
    onSubmitUrl: (String) -> Unit,
    onOpenBookmarks: () -> Unit,
    onOpenStartPage: () -> Unit,
    onEditUrl: (Boolean) -> Unit,
    onRestoreBar: () -> Unit,
    backdrop: Backdrop = rememberLayerBackdrop(),
    modifier: Modifier = Modifier
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }
    val hapticFeedback = LocalHapticFeedback.current

    var textFieldValue by remember {
        mutableStateOf(TextFieldValue(state.currentUrl))
    }

    LaunchedEffect(state.currentUrl) {
        if (!state.isEditingUrl) {
            textFieldValue = TextFieldValue(state.currentUrl)
        }
    }

    val animatedProgress by animateFloatAsState(
        targetValue = if (state.isLoading) state.progress.coerceIn(0.08f, 1f) else 0f,
        animationSpec = tween(durationMillis = 160, easing = FastOutSlowInEasing),
        label = "ben_progress"
    )

    val isMinimized = state.isBarMinimized && !state.isStartPage && !state.isEditingUrl
    val mode = when {
        state.isEditingUrl -> BarMode.Editing
        isMinimized -> BarMode.Collapsed
        else -> BarMode.Expanded
    }

    var hasComposed by remember { mutableStateOf(false) }
    LaunchedEffect(mode) {
        if (hasComposed) {
            hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        } else {
            hasComposed = true
        }
    }

    val screenWidthDp = LocalConfiguration.current.screenWidthDp.dp
    val expandedWidth = minOf(screenWidthDp - 24.dp, FLOATING_BAR_MAX_WIDTH)
    val collapsedWidth = 168.dp

    val targetWidth = when (mode) {
        BarMode.Collapsed -> collapsedWidth
        else -> expandedWidth
    }
    val targetHeight = when (mode) {
        BarMode.Collapsed -> 38.dp
        else -> 52.dp
    }

    // High-performance spring physics for bar morph
    val animatedWidth by animateDpAsState(
        targetValue = targetWidth,
        animationSpec = BenDpSpring,
        label = "ben_bar_width"
    )
    val animatedHeight by animateDpAsState(
        targetValue = targetHeight,
        animationSpec = BenDpSpring,
        label = "ben_bar_height"
    )

    val floatingBarInsets = WindowInsets.navigationBars.union(WindowInsets(bottom = 10.dp))

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(BottomScrimBrush)
            .imePadding()
            .windowInsetsPadding(floatingBarInsets)
            .padding(horizontal = 12.dp, vertical = if (isMinimized) 4.dp else 6.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Box(
            modifier = Modifier
                .width(animatedWidth)
                .height(animatedHeight)
                // GPU-accelerated layer transform: zero recompositions during physics squash & stretch
                .graphicsLayer {
                    val travel = abs(targetWidth.toPx() - animatedWidth.toPx())
                    val l = (travel / 140.dp.toPx()).coerceIn(0f, 1f)
                    scaleX = 1f + l * (STRETCH * 0.35f)
                    scaleY = 1f - l * (STRETCH * SQUASH * 0.35f)
                }
                .drawBackdrop(
                    backdrop = backdrop,
                    shape = { Capsule() },
                    effects = {
                        vibrancy()
                        blur(12f.dp.toPx())
                        lens(16f.dp.toPx(), 24f.dp.toPx(), depthEffect = true, chromaticAberration = true)
                    },
                    highlight = {
                        Highlight(
                            width = 1.dp,
                            alpha = 0.85f
                        )
                    },
                    shadow = {
                        Shadow(
                            radius = 20.dp,
                            color = Color.Black.copy(alpha = 0.5f)
                        )
                    },
                    onDrawSurface = {
                        drawRect(LiquidGlassFillBrush)
                    }
                )
                .clip(PillShape)
                .padding(horizontal = if (mode == BarMode.Collapsed) 8.dp else 4.dp),
            contentAlignment = Alignment.Center
        ) {
            AnimatedContent(
                targetState = mode,
                transitionSpec = {
                    fadeIn(tween(140, easing = FastOutSlowInEasing))
                        .togetherWith(fadeOut(tween(100, easing = FastOutSlowInEasing)))
                },
                label = "bar_content_mode"
            ) { targetMode ->
                when (targetMode) {
                    BarMode.Collapsed -> {
                        // Collapsed Pill: Minimalist domain view, tap anywhere to restore
                        val collapsedSource = remember { MutableInteractionSource() }
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(PillShape)
                                .clickable(
                                    interactionSource = collapsedSource,
                                    indication = null,
                                    onClick = onRestoreBar
                                )
                                .padding(horizontal = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_apple_lock),
                                contentDescription = "Secure",
                                tint = AccentCyan,
                                modifier = Modifier.size(11.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = state.displayUrl.ifBlank { "Search" },
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    BarMode.Editing -> {
                        // Editing Mode: Focused URL search input
                        LaunchedEffect(Unit) {
                            val initialText = if (state.isStartPage) "" else state.currentUrl
                            textFieldValue = TextFieldValue(
                                text = initialText,
                                selection = TextRange(0, initialText.length)
                            )
                            focusRequester.requestFocus()
                            keyboardController?.show()
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_apple_search),
                                contentDescription = "Search",
                                tint = AccentCyan,
                                modifier = Modifier.size(17.dp)
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            BasicTextField(
                                value = textFieldValue,
                                onValueChange = { textFieldValue = it },
                                singleLine = true,
                                textStyle = TextStyle(
                                    color = TextPrimary,
                                    fontSize = 14.sp,
                                    fontFamily = FontFamily.Default,
                                    fontWeight = FontWeight.Normal
                                ),
                                cursorBrush = SolidColor(AccentCyan),
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Uri,
                                    imeAction = ImeAction.Go
                                ),
                                keyboardActions = KeyboardActions(
                                    onGo = {
                                        if (textFieldValue.text.isNotBlank()) {
                                            onSubmitUrl(textFieldValue.text)
                                            keyboardController?.hide()
                                            focusManager.clearFocus()
                                            onEditUrl(false)
                                        }
                                    }
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .focusRequester(focusRequester)
                            )

                            if (textFieldValue.text.isNotEmpty()) {
                                IconButton(
                                    onClick = { textFieldValue = TextFieldValue("") },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Clear",
                                        tint = TextSecondary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(4.dp))

                            Text(
                                text = "Cancel",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = AccentCyan,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        keyboardController?.hide()
                                        focusManager.clearFocus()
                                        onEditUrl(false)
                                    }
                                    .padding(horizontal = 8.dp, vertical = 6.dp)
                            )
                        }
                    }

                    BarMode.Expanded -> {
                        // Normal Expanded Dock: 5 controls with 45% touch target ratio
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Left: Back & Forward
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                IconButton(
                                    onClick = onBack,
                                    enabled = state.canGoBack || !state.isStartPage,
                                    colors = IconButtonDefaults.iconButtonColors(
                                        contentColor = TextPrimary,
                                        disabledContentColor = DisabledTint
                                    ),
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.ic_apple_back),
                                        contentDescription = "Back",
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                IconButton(
                                    onClick = onForward,
                                    enabled = state.canGoForward,
                                    colors = IconButtonDefaults.iconButtonColors(
                                        contentColor = TextPrimary,
                                        disabledContentColor = DisabledTint
                                    ),
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.ic_apple_forward),
                                        contentDescription = "Forward",
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            // Center: Liquid Glass URL Capsule
                            val capsuleSource = remember { MutableInteractionSource() }
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(36.dp)
                                    .padding(horizontal = 4.dp)
                                    .drawBackdrop(
                                        backdrop = backdrop,
                                        shape = { Capsule() },
                                        effects = {
                                            vibrancy()
                                            blur(6f.dp.toPx())
                                            lens(8f.dp.toPx(), 12f.dp.toPx())
                                        },
                                        highlight = {
                                            Highlight(
                                                width = 0.5.dp,
                                                alpha = 0.6f
                                            )
                                        },
                                        onDrawSurface = {
                                            drawRect(Color(0x22FFFFFF))
                                        }
                                    )
                                    .clip(PillShape)
                                    .clickable(
                                        interactionSource = capsuleSource,
                                        indication = null,
                                        onClick = { onEditUrl(true) }
                                    )
                                    .padding(horizontal = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                if (state.isStartPage) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Icon(
                                            painter = painterResource(R.drawable.ic_apple_search),
                                            contentDescription = "Search",
                                            tint = TextSecondary,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Search or type URL",
                                            fontSize = 12.sp,
                                            color = TextSecondary,
                                            fontWeight = FontWeight.Normal,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                } else {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Icon(
                                            painter = painterResource(R.drawable.ic_apple_lock),
                                            contentDescription = "Secure",
                                            tint = AccentCyan,
                                            modifier = Modifier.size(12.dp)
                                        )

                                        Text(
                                            text = state.displayUrl.ifBlank { state.currentUrl },
                                            fontSize = 13.sp,
                                            color = TextPrimary,
                                            fontWeight = FontWeight.Medium,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier
                                                .weight(1f)
                                                .padding(horizontal = 6.dp)
                                        )

                                        val reloadSource = remember { MutableInteractionSource() }
                                        Box(
                                            modifier = Modifier
                                                .size(26.dp)
                                                .clip(CircleShape)
                                                .clickable(
                                                    interactionSource = reloadSource,
                                                    indication = null,
                                                    onClick = onReload
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                painter = painterResource(R.drawable.ic_apple_reload),
                                                contentDescription = "Reload",
                                                tint = TextSecondary,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                }

                                if (state.isLoading && state.progress < 1f && !state.isStartPage) {
                                    LinearProgressIndicator(
                                        progress = { animatedProgress },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(2.dp)
                                            .align(Alignment.BottomCenter)
                                            .clip(RoundedCornerShape(bottomStart = 2.dp, bottomEnd = 2.dp)),
                                        color = AccentCyan,
                                        trackColor = Color.Transparent
                                    )
                                }
                            }

                            // Right: Bookmarks & Home (Start Page)
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                IconButton(
                                    onClick = onOpenBookmarks,
                                    colors = IconButtonDefaults.iconButtonColors(
                                        contentColor = if (state.isCurrentBookmarked) AccentCyan else TextPrimary,
                                        disabledContentColor = DisabledTint
                                    ),
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Icon(
                                        painter = painterResource(
                                            if (state.isCurrentBookmarked) R.drawable.ic_apple_star_filled
                                            else R.drawable.ic_apple_star
                                        ),
                                        contentDescription = "Bookmarks",
                                        tint = if (state.isCurrentBookmarked) AccentCyan else TextPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                IconButton(
                                    onClick = onOpenStartPage,
                                    colors = IconButtonDefaults.iconButtonColors(
                                        contentColor = if (state.isStartPage) AccentCyan else TextPrimary,
                                        disabledContentColor = DisabledTint
                                    ),
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.ic_apple_home),
                                        contentDescription = "Start Page / Home",
                                        tint = if (state.isStartPage) AccentCyan else TextPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// Backward compatibility alias
@Composable
fun AppleBottomBar(
    state: BrowserState,
    onBack: () -> Unit,
    onForward: () -> Unit,
    onReload: () -> Unit,
    onSubmitUrl: (String) -> Unit,
    onOpenBookmarks: () -> Unit,
    onOpenStartPage: () -> Unit,
    onEditUrl: (Boolean) -> Unit,
    onRestoreBar: () -> Unit,
    backdrop: Backdrop = rememberLayerBackdrop(),
    modifier: Modifier = Modifier
) = BenBottomBar(
    state = state,
    onBack = onBack,
    onForward = onForward,
    onReload = onReload,
    onSubmitUrl = onSubmitUrl,
    onOpenBookmarks = onOpenBookmarks,
    onOpenStartPage = onOpenStartPage,
    onEditUrl = onEditUrl,
    onRestoreBar = onRestoreBar,
    backdrop = backdrop,
    modifier = modifier
)
