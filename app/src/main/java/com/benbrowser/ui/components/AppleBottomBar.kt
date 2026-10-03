package com.benbrowser.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
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
import com.benbrowser.theme.AccentBlue
import com.benbrowser.theme.DisabledTint
import com.benbrowser.theme.GlassBorder
import com.benbrowser.theme.GlassSurfaceFallback
import com.benbrowser.theme.InputFieldBg
import com.benbrowser.theme.TextPrimary
import com.benbrowser.theme.TextSecondary
import com.benbrowser.ui.BrowserState

@Composable
fun AppleBottomBar(
    state: BrowserState,
    onBack: () -> Unit,
    onForward: () -> Unit,
    onReload: () -> Unit,
    onHome: () -> Unit,
    onSubmitUrl: (String) -> Unit,
    onToggleBookmark: () -> Unit,
    onOpenBookmarks: () -> Unit,
    onEditUrl: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }

    // Internal text field value for selection & typing
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
            // Subtle embedded progress indicator right above bar when page is loading
            AnimatedVisibility(
                visible = state.isLoading && state.progress < 1f && !state.isStartPage,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .padding(bottom = 4.dp)
            ) {
                LinearProgressIndicator(
                    progress = { state.progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.5.dp)
                        .clip(CircleShape),
                    color = AccentBlue,
                    trackColor = Color(0x33FFFFFF)
                )
            }

            // Safari Floating Glass Capsule
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .clip(CircleShape)
                    .background(GlassSurfaceFallback, CircleShape)
                    .border(BorderStroke(1.dp, GlassBorder), CircleShape)
                    .padding(horizontal = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // If in full URL editing mode, show cancel on left or clean input
                if (state.isEditingUrl) {
                    // In editing mode: clean expanded text capsule with Go and Cancel
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .clip(CircleShape)
                            .background(InputFieldBg)
                            .padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = AppleIcons.Search,
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

                    Spacer(modifier = Modifier.width(6.dp))

                    // Cancel button
                    Text(
                        text = "Cancel",
                        color = AccentBlue,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier
                            .clickable {
                                onEditUrl(false)
                                keyboardController?.hide()
                                focusManager.clearFocus()
                            }
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                    )
                } else {
                    // Normal Browsing Mode: Back, Forward, Address Pill, Bookmarks, Star, Menu

                    // Back Button
                    IconButton(
                        onClick = onBack,
                        enabled = state.canGoBack || !state.isStartPage,
                        colors = IconButtonDefaults.iconButtonColors(
                            contentColor = TextPrimary,
                            disabledContentColor = DisabledTint
                        ),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            modifier = Modifier.size(19.dp)
                        )
                    }

                    // Forward Button
                    IconButton(
                        onClick = onForward,
                        enabled = state.canGoForward,
                        colors = IconButtonDefaults.iconButtonColors(
                            contentColor = TextPrimary,
                            disabledContentColor = DisabledTint
                        ),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Forward",
                            modifier = Modifier.size(19.dp)
                        )
                    }

                    // Center Address / Domain Capsule
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(38.dp)
                            .padding(horizontal = 4.dp)
                            .clip(CircleShape)
                            .background(InputFieldBg)
                            .clickable { onEditUrl(true) }
                            .padding(horizontal = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (state.isStartPage) {
                                Icon(
                                    imageVector = AppleIcons.Search,
                                    contentDescription = "Search",
                                    tint = TextSecondary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Search or enter URL",
                                    fontSize = 15.sp,
                                    color = TextSecondary,
                                    fontWeight = FontWeight.Normal,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                            } else {
                                Icon(
                                    imageVector = AppleIcons.Lock,
                                    contentDescription = "Secure",
                                    tint = TextSecondary,
                                    modifier = Modifier.size(12.dp)
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
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Filled.Refresh,
                                    contentDescription = "Reload",
                                    tint = TextSecondary,
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clickable { onReload() }
                                )
                            }
                        }
                    }

                    // Bookmark Star Toggle
                    IconButton(
                        onClick = onToggleBookmark,
                        enabled = !state.isStartPage && state.currentUrl.isNotBlank(),
                        colors = IconButtonDefaults.iconButtonColors(
                            contentColor = if (state.isCurrentBookmarked) AccentBlue else TextPrimary,
                            disabledContentColor = DisabledTint
                        ),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = if (state.isCurrentBookmarked) AppleIcons.BookmarkFilled else AppleIcons.BookmarkOutline,
                            contentDescription = "Toggle Bookmark",
                            tint = if (state.isCurrentBookmarked) AccentBlue else TextPrimary,
                            modifier = Modifier.size(19.dp)
                        )
                    }

                    // Start Page / Home button or Reload
                    if (!state.isStartPage) {
                        IconButton(
                            onClick = onHome,
                            colors = IconButtonDefaults.iconButtonColors(contentColor = TextPrimary),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = AppleIcons.Home,
                                contentDescription = "Home",
                                modifier = Modifier.size(19.dp)
                            )
                        }
                    }

                    // Bookmarks Library Sheet button
                    IconButton(
                        onClick = onOpenBookmarks,
                        colors = IconButtonDefaults.iconButtonColors(contentColor = TextPrimary),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = AppleIcons.BookmarksList,
                            contentDescription = "Bookmarks",
                            modifier = Modifier.size(19.dp)
                        )
                    }
                }
            }
        }
    }
}
