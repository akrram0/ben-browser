package com.benbrowser.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
    onOpenBookmarks: () -> Unit,
    onEditUrl: (Boolean) -> Unit,
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

    // Animated loading progress with smooth glide
    val animatedProgress by animateFloatAsState(
        targetValue = if (state.isLoading) state.progress.coerceIn(0.08f, 1f) else 0f,
        animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing),
        label = "url_load_progress"
    )

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
            modifier = Modifier.fillMaxWidth()
        ) {
            // Apple Safari Loading Progress Bar
            AnimatedVisibility(
                visible = state.isLoading && state.progress < 1f && !state.isStartPage,
                enter = fadeIn(animationSpec = tween(200)),
                exit = fadeOut(animationSpec = tween(300)),
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .padding(bottom = 6.dp)
            ) {
                LinearProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.5.dp)
                        .clip(CircleShape),
                    color = AccentBlue,
                    trackColor = Color(0x26FFFFFF)
                )
            }

            // Safari Liquid Glass Bar using Haze hazeGlass
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(CircleShape)
                    .hazeGlass(
                        input = HazeInput.Sources(hazeState),
                        style = GlassStyle.regular.then {
                            shape(CircleShape)
                            tint(Color(0x3316181D))
                        }
                    )
                    .background(Color(0x7316181D), CircleShape)
                    .border(BorderStroke(1.dp, GlassBorder), CircleShape)
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Navigation buttons (Back & Forward) - smoothly collapse when entering URL edit mode
                AnimatedVisibility(
                    visible = !state.isEditingUrl,
                    enter = fadeIn(animationSpec = tween(200)) + expandHorizontally(
                        animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
                    ),
                    exit = fadeOut(animationSpec = tween(150)) + shrinkHorizontally(
                        animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
                    )
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
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
                            modifier = Modifier.size(38.dp)
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_apple_forward),
                                contentDescription = "Forward",
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                // Center Dynamic Capsule: smoothly expands to full width when editing
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(42.dp)
                        .padding(horizontal = 4.dp)
                        .clip(CircleShape)
                        .background(InputFieldBg)
                        .border(
                            BorderStroke(
                                1.dp,
                                if (state.isEditingUrl) Color(0x400A84FF) else Color(0x1FFFFFFF)
                            ),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Crossfade(
                        targetState = state.isEditingUrl,
                        animationSpec = tween(200),
                        label = "capsule_mode_crossfade"
                    ) { isEditing ->
                        if (isEditing) {
                            // Active Editing View
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
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            }
                        } else {
                            // Apple Safari Browsing Capsule
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onEditUrl(true) }
                                    .padding(horizontal = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (state.isStartPage) {
                                    Icon(
                                        painter = painterResource(R.drawable.ic_apple_search),
                                        contentDescription = "Search",
                                        tint = TextSecondary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Search or enter website",
                                        fontSize = 15.sp,
                                        color = TextSecondary,
                                        fontWeight = FontWeight.Normal,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f)
                                    )
                                } else {
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
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
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
                }

                // Bookmarks Book Icon (hidden in editing mode)
                AnimatedVisibility(
                    visible = !state.isEditingUrl,
                    enter = fadeIn(animationSpec = tween(200)) + expandHorizontally(
                        animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
                    ),
                    exit = fadeOut(animationSpec = tween(150)) + shrinkHorizontally(
                        animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
                    )
                ) {
                    IconButton(
                        onClick = onOpenBookmarks,
                        colors = IconButtonDefaults.iconButtonColors(contentColor = TextPrimary),
                        modifier = Modifier.size(38.dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_apple_book),
                            contentDescription = "Bookmarks",
                            tint = if (state.isCurrentBookmarked) AccentBlue else TextPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // Cancel Action Button (smoothly slides/expands in editing mode)
                AnimatedVisibility(
                    visible = state.isEditingUrl,
                    enter = fadeIn(animationSpec = tween(200)) + expandHorizontally(
                        animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
                    ),
                    exit = fadeOut(animationSpec = tween(150)) + shrinkHorizontally(
                        animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
                    )
                ) {
                    Text(
                        text = "Cancel",
                        color = AccentBlue,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier
                            .clickable {
                                onEditUrl(false)
                                keyboardController?.hide()
                                focusManager.clearFocus()
                            }
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}
