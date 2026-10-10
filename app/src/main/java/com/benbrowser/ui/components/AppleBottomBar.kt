package com.benbrowser.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import com.benbrowser.theme.GlassSurfaceHaze
import com.benbrowser.theme.InputFieldBg
import com.benbrowser.theme.TextPrimary
import com.benbrowser.theme.TextSecondary
import com.benbrowser.ui.BrowserState
import dev.chrisbanes.haze.ExperimentalHazeApi
import dev.chrisbanes.haze.HazeInput
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.glass.GlassStyle
import dev.chrisbanes.haze.glass.hazeGlass

@OptIn(ExperimentalHazeApi::class)
@Composable
fun AppleBottomBar(
    state: BrowserState,
    hazeState: HazeState,
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
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        label = "safari_load_progress"
    )

    val isMinimized = state.isBarMinimized && !state.isStartPage && !state.isEditingUrl
    val dockShape = if (isMinimized || state.isEditingUrl) CircleShape else RoundedCornerShape(24.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .imePadding()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth(if (isMinimized) 0.62f else 1f)
                .animateContentSize(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioNoBouncy,
                        stiffness = Spring.StiffnessMediumLow
                    )
                )
                .clip(dockShape)
                .hazeGlass(
                    input = HazeInput.Sources(hazeState),
                    style = GlassStyle.regular.then {
                        shape(dockShape)
                        tint(Color(0x4016181D))
                    }
                )
                .background(GlassSurfaceHaze, dockShape)
                .border(BorderStroke(1.dp, GlassBorder), dockShape)
                .padding(
                    horizontal = if (isMinimized) 16.dp else 12.dp,
                    vertical = if (isMinimized) 8.dp else 10.dp
                )
        ) {
            if (isMinimized) {
                // Safari Minimized Scroll Pill: tap to expand
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onRestoreBar() },
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_apple_lock),
                        contentDescription = "Secure",
                        tint = TextSecondary,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = state.displayUrl.ifBlank { state.currentUrl },
                        fontSize = 13.sp,
                        color = TextPrimary,
                        fontWeight = FontWeight.Normal,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            } else {
                // Row 1: Full-Width Safari Address Capsule
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .clip(CircleShape)
                            .background(InputFieldBg)
                            .border(
                                BorderStroke(
                                    1.dp,
                                    if (state.isEditingUrl) Color(0x590A84FF) else GlassBorder
                                ),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Crossfade(
                            targetState = state.isEditingUrl,
                            animationSpec = tween(180),
                            label = "safari_capsule_mode"
                        ) { editing ->
                            if (editing) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.ic_apple_search),
                                        contentDescription = "Search",
                                        tint = TextSecondary,
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
                                            modifier = Modifier.size(24.dp)
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
                                // Safari Browsing / Start Page Address Pill (Full Width — No Domain Truncation)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onEditUrl(true) }
                                        .padding(horizontal = 14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (state.isStartPage) {
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
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f)
                                        )
                                    } else {
                                        Spacer(modifier = Modifier.width(24.dp))
                                        Row(
                                            modifier = Modifier.weight(1f),
                                            horizontalArrangement = Arrangement.Center,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                painter = painterResource(R.drawable.ic_apple_lock),
                                                contentDescription = "Secure",
                                                tint = TextSecondary,
                                                modifier = Modifier.size(13.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
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
                                        IconButton(
                                            onClick = onReload,
                                            modifier = Modifier.size(24.dp)
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

                        // Embedded Safari Bottom Edge Progress Line
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

                    if (state.isEditingUrl) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Cancel",
                            color = AccentBlue,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Normal,
                            modifier = Modifier
                                .clickable {
                                    onEditUrl(false)
                                    keyboardController?.hide()
                                    focusManager.clearFocus()
                                }
                                .padding(horizontal = 6.dp, vertical = 8.dp)
                        )
                    }
                }

                // Row 2: Authentic Safari 5-Icon Control Bar
                AnimatedVisibility(
                    visible = !state.isEditingUrl,
                    enter = fadeIn(tween(180)) + expandVertically(spring(stiffness = Spring.StiffnessMediumLow)),
                    exit = fadeOut(tween(120)) + shrinkVertically(spring(stiffness = Spring.StiffnessMediumLow))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 6.dp, start = 4.dp, end = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 1. Back
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

                        // 2. Forward
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

                        // 3. Quick Bookmark Star
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
                                contentDescription = "Bookmark Page",
                                modifier = Modifier.size(19.dp)
                            )
                        }

                        // 4. Bookmarks Sheet (Open Book)
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

                        // 5. Start Page / Tabs (Overlapping Squares)
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
    }
}
