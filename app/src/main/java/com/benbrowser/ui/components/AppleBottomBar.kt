package com.benbrowser.ui.components

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.ui.platform.LocalFocusManager
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
import com.benbrowser.theme.TextPrimary
import com.benbrowser.theme.TextSecondary
import com.benbrowser.ui.BrowserState
import dev.chrisbanes.haze.ExperimentalHazeApi
import dev.chrisbanes.haze.HazeInput
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.glass.GlassStyle
import dev.chrisbanes.haze.glass.hazeGlass

private enum class CenterPillMode {
    StartPage,
    Browsing,
    Minimized,
    Editing
}

private val GlassSpecularBorderBrush = Brush.verticalGradient(
    colors = listOf(
        Color(0x36FFFFFF),
        GlassBorder
    )
)

private val BottomScrimBrush = Brush.verticalGradient(
    colors = listOf(
        Color.Transparent,
        Color(0x80050608)
    )
)

@OptIn(ExperimentalHazeApi::class)
private fun Modifier.liquidGlassIsland(hazeState: HazeState): Modifier = this
    .hazeGlass(
        input = HazeInput.Sources(hazeState),
        style = GlassStyle.regular.then {
            shape(CircleShape)
            tint(Color(0x5916181D))
        }
    )
    .border(
        border = BorderStroke(1.dp, GlassSpecularBorderBrush),
        shape = CircleShape
    )
    .clip(CircleShape)

@OptIn(ExperimentalHazeApi::class)
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
    modifier: Modifier = Modifier
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }

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
        animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing),
        label = "safari_progress"
    )

    val isMinimized = state.isBarMinimized && !state.isStartPage && !state.isEditingUrl
    val showSideIslands = !state.isEditingUrl && !isMinimized

    val pillMode = when {
        state.isEditingUrl -> CenterPillMode.Editing
        isMinimized -> CenterPillMode.Minimized
        state.isStartPage -> CenterPillMode.StartPage
        else -> CenterPillMode.Browsing
    }

    val dpSpring = spring<Dp>(
        dampingRatio = 0.8f,
        stiffness = Spring.StiffnessLow
    )
    val floatSpring = spring<Float>(
        dampingRatio = 0.8f,
        stiffness = Spring.StiffnessLow
    )

    // Continuous spring-driven dimensions so hazeGlass islands physically morph without clipping layers
    val leftIslandWidth by animateDpAsState(
        targetValue = if (showSideIslands) 88.dp else 0.dp,
        animationSpec = dpSpring,
        label = "left_island_width"
    )
    val leftGap by animateDpAsState(
        targetValue = if (showSideIslands) 8.dp else 0.dp,
        animationSpec = dpSpring,
        label = "left_gap"
    )
    val rightIslandWidth by animateDpAsState(
        targetValue = if (showSideIslands) 48.dp else 0.dp,
        animationSpec = dpSpring,
        label = "right_island_width"
    )
    val rightGap by animateDpAsState(
        targetValue = if (showSideIslands) 8.dp else 0.dp,
        animationSpec = dpSpring,
        label = "right_gap"
    )
    val sideIslandAlpha by animateFloatAsState(
        targetValue = if (showSideIslands) 1f else 0f,
        animationSpec = floatSpring,
        label = "side_island_alpha"
    )

    val cancelWidth by animateDpAsState(
        targetValue = if (state.isEditingUrl) 64.dp else 0.dp,
        animationSpec = dpSpring,
        label = "cancel_width"
    )
    val cancelAlpha by animateFloatAsState(
        targetValue = if (state.isEditingUrl) 1f else 0f,
        animationSpec = floatSpring,
        label = "cancel_alpha"
    )

    val pillHeight by animateDpAsState(
        targetValue = if (isMinimized) 32.dp else 48.dp,
        animationSpec = dpSpring,
        label = "pill_height"
    )
    val barHorizontalPadding by animateDpAsState(
        targetValue = if (isMinimized) 104.dp else 16.dp,
        animationSpec = dpSpring,
        label = "bar_horizontal_padding"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(BottomScrimBrush)
            .imePadding()
            .navigationBarsPadding()
            .padding(
                start = barHorizontalPadding,
                end = barHorizontalPadding,
                top = 12.dp,
                bottom = if (isMinimized) 8.dp else 10.dp
            ),
        contentAlignment = Alignment.BottomCenter
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Island 1: Left Back / Forward Liquid Glass Pill (<  >)
            if (leftIslandWidth > 1.dp) {
                Row(
                    modifier = Modifier
                        .height(48.dp)
                        .width(leftIslandWidth)
                        .graphicsLayer {
                            alpha = sideIslandAlpha
                            scaleX = 0.85f + (0.15f * sideIslandAlpha)
                            scaleY = 0.85f + (0.15f * sideIslandAlpha)
                        }
                        .liquidGlassIsland(hazeState),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBack,
                        enabled = (state.canGoBack || !state.isStartPage) && showSideIslands,
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
                        enabled = state.canGoForward && showSideIslands,
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
                Spacer(modifier = Modifier.width(leftGap))
            }

            // Island 2: Center Morphing Liquid Glass Capsule (Normal <-> Search Edit <-> Scrolled Micro-Pill)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(pillHeight)
                    .liquidGlassIsland(hazeState),
                contentAlignment = Alignment.Center
            ) {
                Crossfade(
                    targetState = pillMode,
                    animationSpec = tween(durationMillis = 160, easing = FastOutSlowInEasing),
                    label = "center_pill_crossfade"
                ) { mode ->
                    when (mode) {
                        CenterPillMode.Editing -> {
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
                                    .fillMaxWidth()
                                    .fillMaxHeight()
                                    .padding(horizontal = 16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_apple_search),
                                    contentDescription = "Search",
                                    tint = TextSecondary,
                                    modifier = Modifier.size(15.dp)
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
                            }
                        }

                        CenterPillMode.Minimized -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .fillMaxHeight()
                                    .clickable(onClick = onRestoreBar)
                                    .padding(horizontal = 14.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = state.displayUrl.ifBlank { state.currentUrl },
                                    fontSize = 12.sp,
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Normal,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        CenterPillMode.StartPage -> {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .fillMaxHeight()
                                    .clickable { onEditUrl(true) }
                                    .padding(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_apple_search),
                                    contentDescription = "Search",
                                    tint = TextSecondary,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Search or enter website",
                                    fontSize = 15.sp,
                                    color = TextSecondary,
                                    fontWeight = FontWeight.Normal,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        CenterPillMode.Browsing -> {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .fillMaxHeight()
                                    .padding(horizontal = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .clickable(onClick = onOpenBookmarks),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.ic_apple_page_menu),
                                        contentDescription = "Page Menu & Bookmarks",
                                        tint = if (state.isCurrentBookmarked) AccentBlue else TextPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                        .clickable { onEditUrl(true) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = state.displayUrl.ifBlank { state.currentUrl },
                                        fontSize = 15.sp,
                                        color = TextPrimary,
                                        fontWeight = FontWeight.Normal,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        textAlign = TextAlign.Center
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .clickable(onClick = onReload),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.ic_apple_reload),
                                        contentDescription = "Reload",
                                        tint = TextPrimary,
                                        modifier = Modifier.size(17.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Subtle Page Load Progress Line along bottom rim of Center Pill
                if (state.isLoading && state.progress < 1f && !state.isStartPage) {
                    LinearProgressIndicator(
                        progress = { animatedProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(2.dp)
                            .align(Alignment.BottomCenter),
                        color = AccentBlue,
                        trackColor = Color.Transparent
                    )
                }
            }

            // Island 3: Right Tabs Circle (⧉)
            if (rightIslandWidth > 1.dp) {
                Spacer(modifier = Modifier.width(rightGap))
                Box(
                    modifier = Modifier
                        .height(48.dp)
                        .width(rightIslandWidth)
                        .graphicsLayer {
                            alpha = sideIslandAlpha
                            scaleX = 0.85f + (0.15f * sideIslandAlpha)
                            scaleY = 0.85f + (0.15f * sideIslandAlpha)
                        }
                        .liquidGlassIsland(hazeState)
                        .clickable(enabled = showSideIslands, onClick = onOpenStartPage),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_apple_tabs),
                        contentDescription = "Tabs / Start Page",
                        tint = TextPrimary,
                        modifier = Modifier.size(19.dp)
                    )
                }
            }

            // Right Cancel Action (expands smoothly when editing URL)
            if (cancelWidth > 1.dp) {
                Box(
                    modifier = Modifier
                        .width(cancelWidth)
                        .graphicsLayer { alpha = cancelAlpha },
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Text(
                        text = "Cancel",
                        color = AccentBlue,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Normal,
                        maxLines = 1,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable(enabled = state.isEditingUrl) {
                                onEditUrl(false)
                                keyboardController?.hide()
                                focusManager.clearFocus()
                            }
                            .padding(horizontal = 4.dp, vertical = 8.dp)
                    )
                }
            }
        }
    }
}
