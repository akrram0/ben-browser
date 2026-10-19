package com.benbrowser.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.input.pointer.pointerInput
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
import com.benbrowser.theme.GlassBorder
import com.benbrowser.theme.GlassSurfaceFallback
import com.benbrowser.theme.TextPrimary
import com.benbrowser.theme.TextSecondary
import com.benbrowser.ui.BrowserState
import dev.chrisbanes.haze.ExperimentalHazeApi
import dev.chrisbanes.haze.HazeInput
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.glass.GlassStyle
import dev.chrisbanes.haze.glass.hazeGlass
import kotlin.math.abs

private enum class BarLayoutState {
    Expanded,
    Collapsed,
    Editing
}

/**
 * BitChord-inspired spring motion:
 * Damping ratio 0.72 = settles in one soft pass beyond the mark without endless wobbling.
 * Stiffness 320 = crisp arrival at roughly 300ms.
 */
private val BitChordDpSpring = spring<Dp>(dampingRatio = 0.72f, stiffness = 320f)

private const val STRETCH = 0.16f
private const val SQUASH = 0.5f

private val PillShape = RoundedCornerShape(percent = 50)

private val GlassFillBrush = Brush.verticalGradient(
    colors = listOf(
        Color(0xE625272F),
        GlassSurfaceFallback
    )
)

private val GlassSpecularBorder = BorderStroke(
    width = 1.dp,
    brush = Brush.verticalGradient(
        colors = listOf(
            Color(0x36FFFFFF),
            GlassBorder
        )
    )
)

private val BottomScrimBrush = Brush.verticalGradient(
    colors = listOf(
        Color.Transparent,
        Color(0x80050608)
    )
)

@OptIn(ExperimentalHazeApi::class)
private fun Modifier.bitChordGlassSurface(
    hazeState: HazeState,
    useLiveShader: Boolean
): Modifier = if (useLiveShader) {
    this
        .hazeGlass(
            input = HazeInput.Sources(hazeState),
            style = GlassStyle.regular.then {
                shape(PillShape)
                tint(Color(0x6616181D))
            }
        )
        .border(GlassSpecularBorder, PillShape)
        .clip(PillShape)
} else {
    this
        .clip(PillShape)
        .background(GlassFillBrush, PillShape)
        .border(GlassSpecularBorder, PillShape)
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppleBottomBar(
    state: BrowserState,
    hazeState: HazeState,
    onBack: () -> Unit,
    onForward: () -> Unit,
    onReload: () -> Unit,
    onSubmitUrl: (String) -> Unit,
    onOpenBookmarks: () -> Unit,
    onOpenStartPage: () -> Unit,
    onEditUrl: (Boolean) -> Unit,
    onRestoreBar: () -> Unit,
    liveGlassEnabled: Boolean,
    onToggleLiveGlass: () -> Unit,
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
    val barState = when {
        state.isEditingUrl -> BarLayoutState.Editing
        isMinimized -> BarLayoutState.Collapsed
        else -> BarLayoutState.Expanded
    }

    // Trigger subtle tactile feedback on state transition (expanded <-> collapsed <-> editing)
    LaunchedEffect(barState) {
        hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
    }

    val useLiveShader = liveGlassEnabled && !state.isStartPage && !state.isEditingUrl

    val screenWidthDp = LocalConfiguration.current.screenWidthDp.dp
    val expandedWidth = (screenWidthDp - 28.dp).coerceAtLeast(280.dp)
    val collapsedWidth = 180.dp

    val targetWidth = when (barState) {
        BarLayoutState.Collapsed -> collapsedWidth
        else -> expandedWidth
    }
    val targetHeight = when (barState) {
        BarLayoutState.Collapsed -> 38.dp
        else -> 52.dp
    }

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

    // BitChord liquid momentum deformation (squash & stretch while traveling)
    val travelDistance = abs(targetWidth.value - animatedWidth.value)
    val lag = (travelDistance / 140f).coerceIn(0f, 1f)
    val morphScaleX = 1f + lag * (STRETCH * 0.35f)
    val morphScaleY = 1f - lag * (STRETCH * SQUASH * 0.35f)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(BottomScrimBrush)
            .imePadding()
            .navigationBarsPadding()
            .padding(
                start = 14.dp,
                end = 14.dp,
                top = 6.dp,
                bottom = if (isMinimized) 8.dp else 10.dp
            ),
        contentAlignment = Alignment.BottomCenter
    ) {
        Box(
            modifier = Modifier
                .pointerInput(Unit) { detectTapGestures(onLongPress = { onToggleLiveGlass() }) }
                .width(animatedWidth)
                .height(animatedHeight)
                .graphicsLayer {
                    scaleX = morphScaleX
                    scaleY = morphScaleY
                }
                .bitChordGlassSurface(hazeState, useLiveShader = useLiveShader),
            contentAlignment = Alignment.Center
        ) {
            AnimatedContent(
                targetState = barState,
                transitionSpec = {
                    (fadeIn(tween(160, easing = FastOutSlowInEasing)) +
                        scaleIn(tween(160, easing = FastOutSlowInEasing), initialScale = 0.95f))
                        .togetherWith(
                            fadeOut(tween(100, easing = FastOutSlowInEasing)) +
                                scaleOut(tween(100, easing = FastOutSlowInEasing), targetScale = 0.95f)
                        )
                },
                contentAlignment = Alignment.Center,
                label = "bitchord_content_state"
            ) { mode ->
                when (mode) {
                    BarLayoutState.Collapsed -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .combinedClickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    onClick = onRestoreBar,
                                    onLongClick = onToggleLiveGlass
                                )
                                .padding(horizontal = 14.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
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
                                    text = state.displayUrl.ifBlank { state.currentUrl },
                                    fontSize = 13.sp,
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    textAlign = TextAlign.Center
                                )
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
                    }

                    BarLayoutState.Expanded -> {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 6.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Left: Navigation
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
                                    modifier = Modifier.size(38.dp)
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.ic_apple_back),
                                        contentDescription = "Back",
                                        modifier = Modifier.size(17.dp)
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
                                        modifier = Modifier.size(17.dp)
                                    )
                                }
                            }

                            // Center: URL Pill Capsule
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(38.dp)
                                    .padding(horizontal = 4.dp)
                                    .clip(PillShape)
                                    .background(Color(0x18FFFFFF))
                                    .border(0.5.dp, Color(0x22FFFFFF), PillShape)
                                    .clickable { onEditUrl(true) }
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
                                            fontSize = 13.sp,
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
                                            fontWeight = FontWeight.Normal,
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
                                                .clickable(onClick = onReload),
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

                            // Right: Bookmarks & Tabs
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(2.dp)
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
                                        painter = painterResource(R.drawable.ic_apple_page_menu),
                                        contentDescription = "Bookmarks & Menu",
                                        modifier = Modifier.size(17.dp)
                                    )
                                }

                                IconButton(
                                    onClick = onOpenStartPage,
                                    colors = IconButtonDefaults.iconButtonColors(
                                        contentColor = TextPrimary,
                                        disabledContentColor = DisabledTint
                                    ),
                                    modifier = Modifier.size(38.dp)
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.ic_apple_tabs),
                                        contentDescription = "Tabs / Start Page",
                                        modifier = Modifier.size(17.dp)
                                    )
                                }
                            }
                        }
                    }

                    BarLayoutState.Editing -> {
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
                                .padding(horizontal = 12.dp, vertical = 6.dp),
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
                                    fontSize = 15.sp,
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
                                        }
                                        onEditUrl(false)
                                        keyboardController?.hide()
                                        focusManager.clearFocus()
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
                                        modifier = Modifier.size(15.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(4.dp))

                            Text(
                                text = "Cancel",
                                color = AccentBlue,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Normal,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        onEditUrl(false)
                                        keyboardController?.hide()
                                        focusManager.clearFocus()
                                    }
                                    .padding(horizontal = 6.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
