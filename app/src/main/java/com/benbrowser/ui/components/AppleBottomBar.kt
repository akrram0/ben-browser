package com.benbrowser.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
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

private val GlassSpecularBorderBrush = Brush.verticalGradient(
    colors = listOf(
        Color(0x36FFFFFF),
        GlassBorder
    )
)

private val BottomScrimBrush = Brush.verticalGradient(
    colors = listOf(
        Color.Transparent,
        Color(0x99050608)
    )
)

@OptIn(ExperimentalHazeApi::class)
private fun Modifier.liquidGlassIsland(hazeState: HazeState): Modifier = this
    .clip(CircleShape)
    .hazeGlass(
        input = HazeInput.Sources(hazeState),
        style = GlassStyle.regular.then {
            shape(CircleShape)
            tint(Color(0x6616181D))
        }
    )
    .border(
        border = BorderStroke(1.dp, GlassSpecularBorderBrush),
        shape = CircleShape
    )

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

    LaunchedEffect(state.isEditingUrl) {
        if (state.isEditingUrl) {
            val initialText = if (state.isStartPage) "" else state.currentUrl
            textFieldValue = TextFieldValue(
                text = initialText,
                selection = TextRange(0, initialText.length)
            )
            focusRequester.requestFocus()
            keyboardController?.show()
        }
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

    val pillHeight by animateDpAsState(
        targetValue = if (isMinimized) 32.dp else 48.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "pill_height"
    )

    val islandSpring = spring<androidx.compose.ui.unit.IntSize>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMediumLow
    )
    val floatSpring = spring<Float>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMediumLow
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(BottomScrimBrush)
            .imePadding()
            .navigationBarsPadding()
            .padding(
                start = 16.dp,
                end = 16.dp,
                top = 12.dp,
                bottom = if (isMinimized) 8.dp else 10.dp
            ),
        contentAlignment = Alignment.BottomCenter
    ) {
        // Unified Single-Row Morphing Layout (Left Island + Center Capsule + Right Island / Cancel)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Island 1: Left Back / Forward Liquid Glass Pill (<  >)
            AnimatedVisibility(
                visible = showSideIslands,
                enter = fadeIn(floatSpring) +
                    scaleIn(floatSpring, initialScale = 0.82f) +
                    expandHorizontally(islandSpring, expandFrom = Alignment.Start),
                exit = fadeOut(tween(140)) +
                    scaleOut(tween(140), targetScale = 0.82f) +
                    shrinkHorizontally(islandSpring, shrinkTowards = Alignment.Start)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier
                            .height(48.dp)
                            .width(88.dp)
                            .liquidGlassIsland(hazeState),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
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
                    Spacer(modifier = Modifier.width(8.dp))
                }
            }

            // Island 2: Center Morphing Liquid Glass Capsule (Normal <-> Search Edit <-> Scrolled Micro-Pill)
            Box(
                modifier = Modifier
                    .then(
                        if (isMinimized) Modifier.width(164.dp)
                        else Modifier.weight(1f)
                    )
                    .height(pillHeight)
                    .animateContentSize(animationSpec = islandSpring)
                    .liquidGlassIsland(hazeState),
                contentAlignment = Alignment.Center
            ) {
                when {
                    state.isEditingUrl -> {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
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

                    isMinimized -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight()
                                .clickable(onClick = onRestoreBar)
                                .padding(horizontal = 16.dp),
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

                    state.isStartPage -> {
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

                    else -> {
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
            AnimatedVisibility(
                visible = showSideIslands,
                enter = fadeIn(floatSpring) +
                    scaleIn(floatSpring, initialScale = 0.82f) +
                    expandHorizontally(islandSpring, expandFrom = Alignment.End),
                exit = fadeOut(tween(140)) +
                    scaleOut(tween(140), targetScale = 0.82f) +
                    shrinkHorizontally(islandSpring, shrinkTowards = Alignment.End)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .liquidGlassIsland(hazeState)
                            .clickable(onClick = onOpenStartPage),
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
            }

            // Right Cancel Action (slides in when editing URL)
            AnimatedVisibility(
                visible = state.isEditingUrl,
                enter = fadeIn(tween(160)) +
                    slideInHorizontally(
                        spring(
                            dampingRatio = Spring.DampingRatioNoBouncy,
                            stiffness = Spring.StiffnessMediumLow
                        )
                    ) { it / 2 } +
                    expandHorizontally(islandSpring, expandFrom = Alignment.End),
                exit = fadeOut(tween(120)) +
                    slideOutHorizontally(tween(140)) { it / 2 } +
                    shrinkHorizontally(islandSpring, shrinkTowards = Alignment.End)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Spacer(modifier = Modifier.width(12.dp))
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
                            .padding(horizontal = 4.dp, vertical = 8.dp)
                    )
                }
            }
        }
    }
}
