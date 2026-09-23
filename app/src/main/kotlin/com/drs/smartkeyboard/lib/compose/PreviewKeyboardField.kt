/*
 * Copyright (C) 2021-2025 The DRS Smart Keyboard Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.drs.smartkeyboard.lib.compose

import android.content.Context
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.drs.smartkeyboard.R
import com.drs.smartkeyboard.lib.util.InputMethodUtils
import org.drs.lib.android.showShortToastSync
import org.drs.lib.android.showShortToast
import org.drs.lib.compose.stringRes
import org.drs.lib.compose.verticalTween
import kotlin.math.roundToInt

private const val AnimationDuration = 200

private val PreviewEnterTransition = EnterTransition.verticalTween(AnimationDuration)
private val PreviewExitTransition = ExitTransition.verticalTween(AnimationDuration)

private val PreviewPillShape = RoundedCornerShape(50)
private val PreviewFieldShape = RoundedCornerShape(28.dp)
private val PreviewHintShape = RoundedCornerShape(20.dp)

val LocalPreviewFieldController = staticCompositionLocalOf<PreviewFieldController?> { null }

/**
 * DRS: resolved safe drag bounds of the pill overlay (in px), already taking
 * system bars / display cutout into account so the pill can be moved anywhere
 * on the screen yet can never end up unreachable behind them.
 */
private data class DrsPillDragBounds(
    val minX: Float,
    val minY: Float,
    val maxX: Float,
    val maxY: Float,
)

@Composable
fun rememberPreviewFieldController(): PreviewFieldController {
    return remember { PreviewFieldController() }
}

class PreviewFieldController {
    val focusRequester = FocusRequester()
    var isVisible by mutableStateOf(false)
    var text by mutableStateOf(TextFieldValue(""))

    /**
     * DRS: true while the preview text field is focused or explicitly expanded.
     * Drives the floating pill <-> floating field switch of [PreviewKeyboardField].
     */
    var isEditing by mutableStateOf(false)

    /**
     * DRS: when true, the pill renders inline inside the caller's bottom slot
     * (used by the theme editor screen) instead of the draggable activity-level
     * overlay, so it can never collide with that screen's own FAB.
     */
    var usesInlinePill by mutableStateOf(false)

    /**
     * DRS: expands the preview into the floating field and focuses it so the
     * keyboard opens. Safe to call at any time, even while the field is not
     * composed yet (unlike raw [FocusRequester.requestFocus]).
     */
    fun expandAndFocus() {
        isEditing = true
    }
}

private fun isPreviewFieldMode(controller: PreviewFieldController): Boolean =
    controller.isEditing || controller.text.text.isNotEmpty()

/**
 * DRS bottom slot renderer: shows the floating rounded field while editing,
 * an inline centered pill for screens that opt in ([PreviewFieldController.usesInlinePill]),
 * or nothing (the draggable activity-level overlay hosts the pill instead).
 */
@Composable
fun PreviewKeyboardField(
    controller: PreviewFieldController,
    modifier: Modifier = Modifier,
    hint: String = stringRes(R.string.settings__preview_keyboard),
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    AnimatedVisibility(
        visible = controller.isVisible,
        enter = PreviewEnterTransition,
        exit = PreviewExitTransition,
    ) {
        AnimatedContent(
            targetState = isPreviewFieldMode(controller),
            transitionSpec = {
                (fadeIn(tween(AnimationDuration)) + scaleIn(
                    initialScale = 0.9f,
                    animationSpec = tween(AnimationDuration),
                )).togetherWith(
                    fadeOut(tween(AnimationDuration)) + scaleOut(
                        targetScale = 0.95f,
                        animationSpec = tween(AnimationDuration),
                    ),
                )
            },
            label = "DrsPreviewFieldMode",
        ) { isFieldMode ->
            if (isFieldMode) {
                PreviewFloatingField(
                    controller = controller,
                    hint = hint,
                    focusManager = focusManager,
                    context = context,
                )
            } else if (controller.usesInlinePill) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 14.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    DrsPreviewPillContent(hint = hint, onClick = { controller.expandAndFocus() })
                }
            } else {
                Spacer(modifier = Modifier)
            }
        }
    }
}

/**
 * DRS draggable floating pill overlay: place it above screen content (activity
 * level). Defaults to the visually-left bottom corner, can be dragged anywhere,
 * and shows a first-open coach hint explaining what it does.
 */
@Composable
fun DrsFloatingPreviewPill(
    controller: PreviewFieldController,
    modifier: Modifier = Modifier,
    hint: String = stringRes(R.string.settings__preview_keyboard),
    hintTitle: String = stringRes(R.string.drs__preview_pill__hint__title),
    hintText: String = stringRes(R.string.drs__preview_pill__hint__text),
    showHint: Boolean = false,
    savedAnchorX: Float? = null,
    savedAnchorY: Float? = null,
    onAnchorChanged: (Float, Float) -> Unit = { _, _ -> },
    onHintDismiss: () -> Unit = {},
) {
    val pillVisible = controller.isVisible && !isPreviewFieldMode(controller) && !controller.usesInlinePill

    AnimatedVisibility(
        visible = pillVisible,
        enter = fadeIn(tween(AnimationDuration)) + scaleIn(
            initialScale = 0.85f,
            animationSpec = tween(AnimationDuration),
        ),
        exit = fadeOut(tween(AnimationDuration)) + scaleOut(
            targetScale = 0.9f,
            animationSpec = tween(AnimationDuration),
        ),
        modifier = modifier,
    ) {
        val dismissHint = rememberUpdatedState(onHintDismiss)

        LaunchedEffect(showHint) {
            if (showHint) {
                kotlinx.coroutines.delay(700)
                kotlinx.coroutines.delay(8000)
                dismissHint.value()
            }
        }

        DraggablePillBody(
            controller = controller,
            hint = hint,
            hintTitle = hintTitle,
            hintText = hintText,
            showHint = showHint,
            savedAnchorX = savedAnchorX,
            savedAnchorY = savedAnchorY,
            onAnchorChanged = onAnchorChanged,
            onHintDismiss = { dismissHint.value() },
        )
    }
}

@Composable
private fun DraggablePillBody(
    controller: PreviewFieldController,
    hint: String,
    hintTitle: String,
    hintText: String,
    showHint: Boolean,
    savedAnchorX: Float?,
    savedAnchorY: Float?,
    onAnchorChanged: (Float, Float) -> Unit,
    onHintDismiss: () -> Unit,
) {
    val density = androidx.compose.ui.platform.LocalDensity.current
    val layoutDirection = androidx.compose.ui.platform.LocalLayoutDirection.current
    val haptic = LocalHapticFeedback.current

    var boxSize by remember { mutableStateOf(IntSize.Zero) }
    var pillSize by remember { mutableStateOf(IntSize.Zero) }
    var placed by rememberSaveable { mutableStateOf(false) }
    var px by rememberSaveable { mutableFloatStateOf(0f) }
    var py by rememberSaveable { mutableFloatStateOf(0f) }

    // DRS: free movement over the WHOLE screen, but never behind system
    // bars or the display cutout (ime excluded: the pill hides while editing).
    val safeInsets = WindowInsets.safeDrawing.exclude(WindowInsets.ime)
    val safeLeftPx = safeInsets.getLeft(density, layoutDirection).toFloat()
    val safeTopPx = safeInsets.getTop(density).toFloat()
    val safeRightPx = safeInsets.getRight(density, layoutDirection).toFloat()
    val safeBottomPx = safeInsets.getBottom(density).toFloat()
    val gripMargin = with(density) { 8.dp.toPx() }

    LaunchedEffect(boxSize, pillSize, savedAnchorX, savedAnchorY) {
        if (!placed && boxSize.width > 0 && pillSize.width > 0) {
            val minBoundX = safeLeftPx + gripMargin
            val minBoundY = safeTopPx + gripMargin
            val maxBoundX = (boxSize.width - pillSize.width - safeRightPx - gripMargin).coerceAtLeast(minBoundX)
            val maxBoundY = (boxSize.height - pillSize.height - safeBottomPx - gripMargin).coerceAtLeast(minBoundY)
            val anchorX = savedAnchorX
            val anchorY = savedAnchorY
            if (anchorX != null && anchorY != null) {
                // DRS: restore the last persisted drag position (stored as
                // fractions of the safe area, so it survives restarts and
                // screen size/orientation changes).
                px = (minBoundX + anchorX * (maxBoundX - minBoundX)).coerceIn(minBoundX, maxBoundX)
                py = (minBoundY + anchorY * (maxBoundY - minBoundY)).coerceIn(minBoundY, maxBoundY)
            } else {
                // DRS: default placement — visually left bottom corner.
                px = minBoundX
                py = maxBoundY
            }
            placed = true
        }
    }

    val bounds = rememberUpdatedState(
        DrsPillDragBounds(
            minX = safeLeftPx + gripMargin,
            minY = safeTopPx + gripMargin,
            maxX = (boxSize.width - pillSize.width - safeRightPx - gripMargin).coerceAtLeast(safeLeftPx + gripMargin),
            maxY = (boxSize.height - pillSize.height - safeBottomPx - gripMargin).coerceAtLeast(safeTopPx + gripMargin),
        ),
    )
    val b = bounds.value
    val cx = px.coerceIn(b.minX, b.maxX).roundToInt()
    val cy = py.coerceIn(b.minY, b.maxY).roundToInt()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .onSizeChanged { boxSize = it },
    ) {
        AnimatedVisibility(
            visible = showHint,
            enter = fadeIn(tween(350)) + slideInVertically(tween(350)) { it / 2 },
            exit = fadeOut(tween(250)),
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 12.dp, end = 12.dp, bottom = 92.dp),
        ) {
            Surface(
                shape = PreviewHintShape,
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f)),
                shadowElevation = 8.dp,
                modifier = Modifier
                    .widthIn(max = 320.dp)
                    .clickable(onClick = onHintDismiss),
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp),
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = hintTitle,
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = hintText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .offset { IntOffset(cx, cy) }
                .onSizeChanged { pillSize = it },
        ) {
            DrsPreviewPillContent(
                hint = hint,
                onClick = {
                    onHintDismiss()
                    controller.expandAndFocus()
                },
                onDragStart = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onHintDismiss()
                },
                onDrag = { amount ->
                    val boundsNow = bounds.value
                    px = (px + amount.x).coerceIn(boundsNow.minX, boundsNow.maxX)
                    py = (py + amount.y).coerceIn(boundsNow.minY, boundsNow.maxY)
                },
                onDragEnd = {
                    // DRS: persist the drop position as fractions of the safe
                    // area so the pill stays exactly where the user left it,
                    // even after closing and reopening the app.
                    val boundsNow = bounds.value
                    val spanX = (boundsNow.maxX - boundsNow.minX).coerceAtLeast(1f)
                    val spanY = (boundsNow.maxY - boundsNow.minY).coerceAtLeast(1f)
                    onAnchorChanged(
                        ((px - boundsNow.minX) / spanX).coerceIn(0f, 1f),
                        ((py - boundsNow.minY) / spanY).coerceIn(0f, 1f),
                    )
                },
            )
        }
    }
}

/**
 * The luxurious pill visual: soft gradient, thin border, elevated shadow,
 * keyboard icon in a tinted badge, bold label and a subtle press-scale.
 */
@Composable
private fun DrsPreviewPillContent(
    hint: String,
    onClick: () -> Unit,
    onDragStart: () -> Unit = {},
    onDrag: (androidx.compose.ui.geometry.Offset) -> Unit = {},
    onDragEnd: () -> Unit = {},
) {
    val colorScheme = MaterialTheme.colorScheme
    val pillGradient = Brush.linearGradient(
        listOf(
            colorScheme.primary.copy(alpha = 0.16f),
            colorScheme.tertiary.copy(alpha = 0.16f),
        ),
    )
    val borderColor = colorScheme.outlineVariant.copy(alpha = 0.55f)

    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.95f else 1f,
        animationSpec = tween(AnimationDuration),
        label = "DrsPreviewPillScale",
    )

    Row(
        modifier = Modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .shadow(elevation = 8.dp, shape = PreviewPillShape, clip = false)
            .clip(PreviewPillShape)
            .background(pillGradient)
            .border(BorderStroke(1.dp, borderColor), PreviewPillShape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClickLabel = hint,
                onClick = onClick,
            )
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { onDragStart() },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        onDrag(dragAmount)
                    },
                    onDragEnd = { onDragEnd() },
                )
            }
            .padding(start = 14.dp, end = 22.dp, top = 10.dp, bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(30.dp)
                .clip(CircleShape)
                .background(colorScheme.primary.copy(alpha = 0.16f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Default.Keyboard,
                contentDescription = null,
                tint = colorScheme.primary,
                modifier = Modifier.size(17.dp),
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = hint,
            style = MaterialTheme.typography.titleMedium,
            color = colorScheme.primary,
            fontWeight = FontWeight.Bold,
            overflow = TextOverflow.Ellipsis,
            maxLines = 1,
        )
    }
}

@Composable
private fun PreviewFloatingField(
    controller: PreviewFieldController,
    hint: String,
    focusManager: FocusManager,
    context: Context,
) {
    val colorScheme = MaterialTheme.colorScheme

    LaunchedEffect(controller.isEditing) {
        if (controller.isEditing) {
            controller.focusRequester.requestFocus()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 14.dp),
    ) {
        SelectionContainer {
            TextField(
                modifier = Modifier
                    .height(56.dp)
                    .fillMaxWidth()
                    .shadow(elevation = 6.dp, shape = PreviewFieldShape, clip = false)
                    .onPreviewKeyEvent { event ->
                        if (event.key == Key.Back) {
                            focusManager.clearFocus()
                        }
                        false
                    }
                    .focusRequester(controller.focusRequester)
                    .onFocusChanged { controller.isEditing = it.isFocused },
                value = controller.text,
                onValueChange = { controller.text = it },
                textStyle = LocalTextStyle.current.copy(textDirection = TextDirection.ContentOrLtr),
                placeholder = {
                    Text(
                        text = hint,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1,
                    )
                },
                trailingIcon = {
                    Row {
                        if (controller.text.text.isNotEmpty()) {
                            IconButton(onClick = {
                                controller.text = TextFieldValue("")
                                focusManager.clearFocus()
                            }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = stringRes(R.string.action__cancel),
                                )
                            }
                        }
                        IconButton(onClick = {
                            if (!InputMethodUtils.showImePicker(context)) {
                                context.showShortToastSync(
                                    context.getString(R.string.error__imm_service_unavailable),
                                )
                            }
                        }) {
                            Icon(
                                imageVector = Icons.Default.Keyboard,
                                contentDescription = null,
                            )
                        }
                    }
                },
                keyboardActions = KeyboardActions(
                    onDone = { focusManager.clearFocus() },
                ),
                keyboardOptions = KeyboardOptions(autoCorrectEnabled = true),
                singleLine = true,
                shape = PreviewFieldShape,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = colorScheme.surfaceContainerHigh,
                    unfocusedContainerColor = colorScheme.surfaceContainerHigh,
                    disabledContainerColor = colorScheme.surfaceContainerHigh,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                ),
            )
        }
    }
}
