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
import androidx.compose.foundation.border
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
import com.benbrowser.theme.AccentBlue
import com.benbrowser.theme.DisabledTint
import com.benbrowser.theme.TextPrimary
import com.benbrowser.theme.TextSecondary
import com.benbrowser.ui.BrowserState
import kotlin.math.abs

private val BitChordDpSpring = spring<Dp>(dampingRatio = 0.72f, stiffness = 320f)
private const val STRETCH = 0.16f
private const val SQUASH = 0.5f

val FLOATING_BAR_MAX_WIDTH = 440.dp
private val PillShape = RoundedCornerShape(percent = 50)

// Liquid Glass: translucent refraction fill with specular highlight border
private val LiquidGlassFillBrush = Brush.verticalGradient(
    colors = listOf(
        Color(0x991E2230),
        Color(0xBB12141D)
    )
)

private val LiquidGlassSpecularBorder = BorderStroke(
    width = 1.dp,
    brush = Brush.verticalGradient(
        colors = listOf(
            Color(0x60FFFFFF),
            Color(0x18FFFFFF)
        )
    )
)

private val BottomScrimBrush = Brush.verticalGradient(
    colors = listOf(
        Color.Transparent,
        Color(0x80050608)
    )
)

private enum class BarMode {
    Expanded,
    Collapsed,
    Editing
}

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
        label = "safari_progress"
    )

    val isMinimized = state.isBarMinimized && !state.isStartPage && !state.isEditingUrl
    val mode = when {
        state.isEditingUrl -> BarMode.Editing
        isMinimized -> BarMode.Collapsed
        else -> BarMode.Expanded
    }

    LaunchedEffect(mode) {
        hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
    }

    val screenWidthDp = LocalConfiguration.current.screenWidthDp.dp
    val expandedWidth = minOf(screenWidthDp - 28.dp, FLOATING_BAR_MAX_WIDTH)
    val collapsedWidth = 176.dp

    val targetWidth = when (mode) {
        BarMode.Collapsed -> collapsedWidth
        else -> expandedWidth
    }
    val targetHeight = when (mode) {
        BarMode.Collapsed -> 38.dp
        else -> 52.dp
    }

    // BitChord spring physics for bar morph
    val animatedWidth by animateDpAsState(
        targetValue = targetWidth,
        animationSpec = BitChordDpSpring,
        label = "bitchord_bar_width"
    )
    val animatedHeight by animateDpAsState(
        targetValue = targetHeight,
        animationSpec = BitChordDpSpring,
        label = "bitchord_bar_height"
    )

    // BitChord momentum squash & stretch while morphing
    val travelDistance = abs(targetWidth.value - animatedWidth.value)
    val lag = (travelDistance / 140f).coerceIn(0f, 1f)
    val morphScaleX = 1f + lag * (STRETCH * 0.35f)
    val morphScaleY = 1f - lag * (STRETCH * SQUASH * 0.35f)

    val floatingBarInsets = WindowInsets.navigationBars.union(WindowInsets(bottom = 10.dp))

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(BottomScrimBrush)
            .imePadding()
            .windowInsetsPadding(floatingBarInsets)
            .padding(horizontal = 14.dp, vertical = if (isMinimized) 4.dp else 6.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Box(
            modifier = Modifier
                .width(animatedWidth)
                .height(animatedHeight)
                .graphicsLayer {
                    scaleX = morphScaleX
                    scaleY = morphScaleY
                }
                .clip(PillShape)
                .background(LiquidGlassFillBrush, PillShape)
                .border(LiquidGlassSpecularBorder, PillShape)
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
                        // Collapsed Pill: Minimalist domain view, tap anywhere to expand
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(PillShape)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    onClick = onRestoreBar
                                )
                                .padding(horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_apple_lock),
                                contentDescription = "Secure",
                                tint = TextSecondary,
                                modifier = Modifier.size(11.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = state.displayUrl.ifBlank { "Search" },
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
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
                                tint = AccentBlue,
                                modifier = Modifier.size(16.dp)
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
                                cursorBrush = SolidColor(AccentBlue),
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
                                color = AccentBlue,
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
                        // Normal Expanded Dock: 5 controls
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
                                horizontalArrangement = Arrangement.spacedBy(1.dp)
                            ) {
                                IconButton(
                                    onClick = onBack,
                                    enabled = state.canGoBack || !state.isStartPage,
                                    colors = IconButtonDefaults.iconButtonColors(
                                        contentColor = TextPrimary,
                                        disabledContentColor = DisabledTint
                                    ),
                                    modifier = Modifier.size(38.dp)
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.ic_apple_back),
                                        contentDescription = "Back",
                                        modifier = Modifier.size(16.dp)
                                    )
                                }

                                IconButton(
                                    onClick = onForward,
                                    enabled = state.canGoForward,
                                    colors = IconButtonDefaults.iconButtonColors(
                                        contentColor = TextPrimary,
                                        disabledContentColor = DisabledTint
                                    ),
                                    modifier = Modifier.size(38.dp)
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.ic_apple_forward),
                                        contentDescription = "Forward",
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            // Center: Liquid Glass URL Capsule
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(36.dp)
                                    .padding(horizontal = 4.dp)
                                    .clip(PillShape)
                                    .background(Color(0x22FFFFFF), PillShape)
                                    .border(BorderStroke(0.5.dp, Color(0x35FFFFFF)), PillShape)
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
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
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Search or enter address",
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
                                            tint = TextSecondary,
                                            modifier = Modifier.size(11.dp)
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

                                        Box(
                                            modifier = Modifier
                                                .size(24.dp)
                                                .clip(CircleShape)
                                                .clickable(
                                                    interactionSource = remember { MutableInteractionSource() },
                                                    indication = null,
                                                    onClick = onReload
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                painter = painterResource(R.drawable.ic_apple_reload),
                                                contentDescription = "Reload",
                                                tint = TextSecondary,
                                                modifier = Modifier.size(13.dp)
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
                                        color = AccentBlue,
                                        trackColor = Color.Transparent
                                    )
                                }
                            }

                            // Right: Bookmarks & Home (Start Page)
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(1.dp)
                            ) {
                                IconButton(
                                    onClick = onOpenBookmarks,
                                    colors = IconButtonDefaults.iconButtonColors(
                                        contentColor = if (state.isCurrentBookmarked) AccentBlue else TextPrimary,
                                        disabledContentColor = DisabledTint
                                    ),
                                    modifier = Modifier.size(38.dp)
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.ic_apple_star),
                                        contentDescription = "Bookmarks",
                                        modifier = Modifier.size(16.dp)
                                    )
                                }

                                IconButton(
                                    onClick = onOpenStartPage,
                                    colors = IconButtonDefaults.iconButtonColors(
                                        contentColor = if (state.isStartPage) AccentBlue else TextPrimary,
                                        disabledContentColor = DisabledTint
                                    ),
                                    modifier = Modifier.size(38.dp)
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.ic_apple_home),
                                        contentDescription = "Start Page / Home",
                                        modifier = Modifier.size(16.dp)
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
