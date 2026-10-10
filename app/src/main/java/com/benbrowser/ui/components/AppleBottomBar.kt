package com.benbrowser.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.HorizontalDivider
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

@Composable
fun AppleBottomBar(
    state: BrowserState,
    onBack: () -> Unit,
    onForward: () -> Unit,
    onReload: () -> Unit,
    onSubmitUrl: (String) -> Unit,
    onToggleBookmark: () -> Unit,
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
    val capsuleHeight by animateDpAsState(
        targetValue = if (isMinimized) 32.dp else 44.dp,
        animationSpec = tween(durationMillis = 150, easing = FastOutSlowInEasing),
        label = "capsule_height"
    )

    // Docked Safari Bottom Surface — prevents webpage text bleeding under or behind controls
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(GlassSurfaceFallback)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                if (isMinimized) onRestoreBar()
            }
            .imePadding()
            .navigationBarsPadding()
    ) {
        // Top Specular Hairline + Page Load Progress Bar
        Box(modifier = Modifier.fillMaxWidth()) {
            HorizontalDivider(
                thickness = 0.5.dp,
                color = GlassBorder
            )
            if (state.isLoading && state.progress < 1f && !state.isStartPage) {
                LinearProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp),
                    color = AccentBlue,
                    trackColor = Color.Transparent
                )
            }
        }

        // Row 1: Safari Address Capsule
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = 16.dp,
                    end = 16.dp,
                    top = if (isMinimized) 6.dp else 10.dp,
                    bottom = if (isMinimized) 6.dp else 4.dp
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val capsuleShape = RoundedCornerShape(12.dp)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(capsuleHeight)
                    .clip(capsuleShape)
                    .background(
                        if (isMinimized) Color.Transparent else Color(0xFF23252E),
                        capsuleShape
                    )
                    .border(
                        BorderStroke(
                            1.dp,
                            when {
                                isMinimized -> Color.Transparent
                                state.isEditingUrl -> AccentBlue.copy(alpha = 0.45f)
                                else -> GlassBorder
                            }
                        ),
                        capsuleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (state.isEditingUrl) {
                    // Instant URL Input Mode (0ms transition delay)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp),
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
                } else {
                    // Safari Browsing / Minimized / Start Page Capsule
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (state.isStartPage) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .fillMaxHeight()
                                    .clickable { onEditUrl(true) }
                                    .padding(horizontal = 12.dp),
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
                                    text = "Search or enter website name",
                                    fontSize = 15.sp,
                                    color = TextSecondary,
                                    fontWeight = FontWeight.Normal,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        } else {
                            // Left balancer so domain stays dead-center when Reload icon is visible
                            if (!isMinimized) {
                                Spacer(modifier = Modifier.width(36.dp))
                            }
                            Row(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .clickable {
                                        if (isMinimized) onRestoreBar() else onEditUrl(true)
                                    },
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_apple_lock),
                                    contentDescription = "Secure",
                                    tint = TextSecondary,
                                    modifier = Modifier.size(if (isMinimized) 11.dp else 13.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = state.displayUrl.ifBlank { state.currentUrl },
                                    fontSize = if (isMinimized) 12.sp else 15.sp,
                                    color = if (isMinimized) TextSecondary else TextPrimary,
                                    fontWeight = FontWeight.Normal,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    textAlign = TextAlign.Center
                                )
                            }
                            if (!isMinimized) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .clickable(onClick = onReload),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.ic_apple_reload),
                                        contentDescription = "Reload",
                                        tint = TextSecondary,
                                        modifier = Modifier.size(15.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (state.isEditingUrl) {
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

        // Row 2: Safari 5-Icon Control Strip (collapses smoothly on scroll down or URL edit)
        AnimatedVisibility(
            visible = !state.isEditingUrl && !isMinimized,
            enter = expandVertically(tween(150, easing = FastOutSlowInEasing)) + fadeIn(tween(120)),
            exit = shrinkVertically(tween(150, easing = FastOutSlowInEasing)) + fadeOut(tween(100))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
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
                        modifier = Modifier.size(19.dp)
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
                        modifier = Modifier.size(19.dp)
                    )
                }

                IconButton(
                    onClick = onToggleBookmark,
                    enabled = !state.isStartPage && state.currentUrl.isNotBlank(),
                    colors = IconButtonDefaults.iconButtonColors(
                        contentColor = if (state.isCurrentBookmarked) AccentBlue else TextPrimary,
                        disabledContentColor = DisabledTint
                    ),
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        painter = painterResource(
                            if (state.isCurrentBookmarked) R.drawable.ic_apple_star_filled
                            else R.drawable.ic_apple_star
                        ),
                        contentDescription = "Bookmark",
                        modifier = Modifier.size(19.dp)
                    )
                }

                IconButton(
                    onClick = onOpenBookmarks,
                    colors = IconButtonDefaults.iconButtonColors(contentColor = TextPrimary),
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_apple_book),
                        contentDescription = "Bookmarks",
                        modifier = Modifier.size(19.dp)
                    )
                }

                IconButton(
                    onClick = onOpenStartPage,
                    colors = IconButtonDefaults.iconButtonColors(
                        contentColor = if (state.isStartPage) AccentBlue else TextPrimary
                    ),
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_apple_tabs),
                        contentDescription = "Start Page",
                        modifier = Modifier.size(19.dp)
                    )
                }
            }
        }
    }
}
