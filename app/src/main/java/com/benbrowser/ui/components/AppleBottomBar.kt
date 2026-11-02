package com.benbrowser.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.benbrowser.R
import com.benbrowser.theme.AccentBlue
import com.benbrowser.theme.DisabledTint
import com.benbrowser.theme.TextPrimary
import com.benbrowser.theme.TextSecondary
import com.benbrowser.ui.BrowserState

/**
 * BitChord-inspired layout tokens:
 * - Floating bar max width: 440dp centered on screen
 * - Navigation gutter: 16dp
 * - Bottom clearance above system nav: 10dp
 * - Ultra-responsive lightweight liquid glass with zero scroll stall
 */
val FLOATING_BAR_MAX_WIDTH = 440.dp
private val PillShape = RoundedCornerShape(percent = 50)

private val GlassFillBrush = Brush.verticalGradient(
    colors = listOf(
        Color(0xF01C1E28),
        Color(0xE012141C)
    )
)

private val GlassSpecularBorder = BorderStroke(
    width = 0.5.dp,
    brush = Brush.verticalGradient(
        colors = listOf(
            Color(0x45FFFFFF),
            Color(0x14FFFFFF)
        )
    )
)

private val BottomScrimBrush = Brush.verticalGradient(
    colors = listOf(
        Color.Transparent,
        Color(0x80050608)
    )
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

    // Trigger subtle tactile feedback on editing state transition
    LaunchedEffect(state.isEditingUrl) {
        hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
    }

    val floatingBarInsets = WindowInsets.navigationBars.union(WindowInsets(bottom = 10.dp))

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(BottomScrimBrush)
            .imePadding()
            .windowInsetsPadding(floatingBarInsets)
            .padding(horizontal = 16.dp, vertical = 6.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        // BitChord centered dock capsule
        Box(
            modifier = Modifier
                .widthIn(max = FLOATING_BAR_MAX_WIDTH)
                .fillMaxWidth()
                .height(54.dp)
                .clip(PillShape)
                .background(GlassFillBrush, PillShape)
                .border(GlassSpecularBorder, PillShape)
                .padding(horizontal = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            AnimatedContent(
                targetState = state.isEditingUrl,
                transitionSpec = {
                    (fadeIn(tween(140, easing = FastOutSlowInEasing)))
                        .togetherWith(fadeOut(tween(110, easing = FastOutSlowInEasing)))
                },
                label = "bar_mode_transition"
            ) { isEditing ->
                if (isEditing) {
                    // Editing Mode: Focused URL Input
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
                            .padding(horizontal = 8.dp),
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
                } else {
                    // Normal Browsing Dock (5 controls: Back, Forward, URL/Search, Bookmarks, Home)
                    Row(
                        modifier = Modifier.fillMaxSize(),
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

                        // Center: URL Pill Capsule
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                                .padding(horizontal = 4.dp)
                                .clip(PillShape)
                                .background(Color(0x1AFFFFFF), PillShape)
                                .border(BorderStroke(0.5.dp, Color(0x28FFFFFF)), PillShape)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    onClick = { onEditUrl(true) }
                                )
                                .padding(horizontal = 12.dp),
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
