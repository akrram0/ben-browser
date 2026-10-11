package com.benbrowser.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.ui.draw.shadow
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
import com.benbrowser.theme.GlassSurfaceFallback
import com.benbrowser.theme.TextPrimary
import com.benbrowser.theme.TextSecondary
import com.benbrowser.ui.BrowserState

private val GlassFillBrush = Brush.verticalGradient(
    colors = listOf(
        Color(0xE625272F),
        GlassSurfaceFallback
    )
)

private val GlassSpecularBorderBrush = Brush.verticalGradient(
    colors = listOf(
        Color(0x38FFFFFF),
        GlassBorder
    )
)

private val BottomScrimBrush = Brush.verticalGradient(
    colors = listOf(
        Color.Transparent,
        Color(0xB3050608)
    )
)

private fun Modifier.liquidGlassIsland(isFocused: Boolean = false): Modifier = this
    .shadow(
        elevation = 12.dp,
        shape = CircleShape,
        ambientColor = Color.Black,
        spotColor = Color.Black
    )
    .clip(CircleShape)
    .background(GlassFillBrush, CircleShape)
    .border(
        border = if (isFocused) {
            BorderStroke(1.dp, AccentBlue.copy(alpha = 0.6f))
        } else {
            BorderStroke(1.dp, GlassSpecularBorderBrush)
        },
        shape = CircleShape
    )

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
        animationSpec = tween(durationMillis = 160, easing = FastOutSlowInEasing),
        label = "safari_progress"
    )

    val isMinimized = state.isBarMinimized && !state.isStartPage && !state.isEditingUrl

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
        when {
            // STATE 1: Instant URL Editing Pill (0ms mount)
            state.isEditingUrl -> {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .liquidGlassIsland(isFocused = true)
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

            // STATE 2: Scrolled-Down Micro-Pill (iOS 26 Compact Collapsed State)
            isMinimized -> {
                Box(
                    modifier = Modifier
                        .height(30.dp)
                        .liquidGlassIsland()
                        .clickable(onClick = onRestoreBar)
                        .padding(horizontal = 20.dp),
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

            // STATE 3: Expanded iOS 26 3-Pill Liquid Glass Layout
            else -> {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Island 1: Left Back / Forward Pill (<  >)
                    Row(
                        modifier = Modifier
                            .height(48.dp)
                            .width(88.dp)
                            .liquidGlassIsland(),
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

                    // Island 2: Center Address Capsule ([Menu]  github.com  [↻])
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .liquidGlassIsland(),
                        contentAlignment = Alignment.Center
                    ) {
                        if (state.isStartPage) {
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
                        } else {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .fillMaxHeight()
                                    .padding(horizontal = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Left Page Menu / Bookmarks Trigger
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

                                // Center Domain Label
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

                                // Right Reload Button
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

                    // Island 3: Right Tabs / Start Page Circle (⧉)
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .liquidGlassIsland()
                            .clickable(onClick = onOpenStartPage),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_apple_tabs),
                            contentDescription = "Tabs / Start Page",
                            tint = if (state.isStartPage) AccentBlue else TextPrimary,
                            modifier = Modifier.size(19.dp)
                        )
                    }
                }
            }
        }
    }
}
