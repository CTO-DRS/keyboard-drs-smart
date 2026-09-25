/*
 * Copyright (C) 2022-2025 The DRS Smart Keyboard Project
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

package com.drs.smartkeyboard.ime.media

import android.annotation.SuppressLint
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Backspace
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.drs.smartkeyboard.R
import com.drs.smartkeyboard.app.DrsPreferenceStore
import com.drs.smartkeyboard.ime.input.InputEventDispatcher
import com.drs.smartkeyboard.ime.input.LocalInputFeedbackController
import com.drs.smartkeyboard.ime.keyboard.DrsImeSizing
import com.drs.smartkeyboard.ime.keyboard.KeyData
import com.drs.smartkeyboard.ime.media.emoji.EmojiData
import com.drs.smartkeyboard.ime.media.emoji.EmojiPaletteView
import com.drs.smartkeyboard.ime.media.emoji.EmojiSkinTone
import com.drs.smartkeyboard.ime.text.keyboard.TextKeyData
import com.drs.smartkeyboard.ime.theme.DrsImeUi
import com.drs.smartkeyboard.keyboardManager
import com.drs.smartkeyboard.subtypeManager
import org.drs.jetpref.datastore.model.collectAsState
import kotlinx.coroutines.launch
import org.drs.lib.snygg.SnyggSelector
import org.drs.lib.snygg.ui.SnyggBox
import org.drs.lib.snygg.ui.SnyggColumn
import org.drs.lib.snygg.ui.SnyggRow

@SuppressLint("MutableCollectionMutableState")
@Composable
fun MediaInputLayout(
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val keyboardManager by context.keyboardManager()
    // DRS v1.19.0: the palette follows the active subtype's locale so the emoji
    // metadata (names + keywords) matches the user's language — native search in
    // ar/en/de/es/fr/it/pt, honest English fallback elsewhere. Previously the
    // palette was hard-wired to "root.txt" whose metadata columns are empty for
    // ~99.8% of its lines, which made emoji search permanently return no results.
    val subtypeManager by context.subtypeManager()
    val activeSubtype by subtypeManager.activeSubtypeFlow.collectAsState()

    var emojiLayoutDataMap by remember { mutableStateOf(EmojiData.Fallback) }
    LaunchedEffect(activeSubtype.id) {
        emojiLayoutDataMap = EmojiData.get(context, activeSubtype.primaryLocale)
    }

    SnyggColumn(
        elementName = DrsImeUi.Media.elementName,
        modifier = modifier
            .fillMaxWidth()
            .height(DrsImeSizing.imeUiHeight()),
    ) {
        EmojiPaletteView(
            modifier = Modifier.weight(1f),
            fullEmojiMappings = emojiLayoutDataMap,
        )
        SnyggRow(
            elementName = DrsImeUi.MediaBottomRow.elementName,
            modifier = Modifier
                .fillMaxWidth()
                .height(DrsImeSizing.keyboardRowBaseHeight * 0.8f),
        ) {
            KeyboardLikeButton(
                elementName = DrsImeUi.MediaBottomRowButton.elementName,
                inputEventDispatcher = keyboardManager.inputEventDispatcher,
                keyData = TextKeyData.IME_UI_MODE_TEXT,
                modifier = Modifier.fillMaxHeight(),
            ) {
                Text(
                    text = "ABC",
                    fontWeight = FontWeight.Bold,
                )
            }
            // DRS v1.21.0: the in-palette skin-tone selector — a tap cycles
            // the preferred tone right here (Gboard parity); it used to
            // require a trip to the app settings. The dot paints the tone
            // the palette renders people emoji with.
            val prefs by DrsPreferenceStore
            val skinTone by prefs.emoji.preferredSkinTone.collectAsState()
            val toneScope = rememberCoroutineScope()
            val toneCd = androidx.compose.ui.res.stringResource(R.string.emoji__skin_tone__cd)
            SnyggBox(
                elementName = DrsImeUi.MediaBottomRowButton.elementName,
                modifier = Modifier
                    .fillMaxHeight()
                    .aspectRatio(1f)
                    .clickable {
                        toneScope.launch { prefs.emoji.preferredSkinTone.set(skinTone.next()) }
                    }
                    .semantics { contentDescription = toneCd },
            ) {
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(22.dp)
                        .background(
                            androidx.compose.ui.graphics.Color(skinTone.swatchColor()),
                            CircleShape,
                        ),
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            KeyboardLikeButton(
                elementName = DrsImeUi.MediaBottomRowButton.elementName,
                inputEventDispatcher = keyboardManager.inputEventDispatcher,
                keyData = TextKeyData.DELETE,
                modifier = Modifier.fillMaxHeight(),
            ) {
                Icon(imageVector = Icons.AutoMirrored.Outlined.Backspace, contentDescription = null)
            }
        }
    }
}

@Composable
internal fun KeyboardLikeButton(
    modifier: Modifier = Modifier,
    inputEventDispatcher: InputEventDispatcher,
    keyData: KeyData,
    elementName: String = DrsImeUi.MediaEmojiKey.elementName,
    content: @Composable () -> Unit,
) {
    val inputFeedbackController = LocalInputFeedbackController.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val selector = if (isPressed) {
        SnyggSelector.PRESSED
    } else {
        SnyggSelector.NONE
    }

    SnyggBox(
        elementName = elementName,
        attributes = mapOf(DrsImeUi.Attr.Code to keyData.code),
        selector = selector,
        clickAndSemanticsModifier = modifier
            .indication(interactionSource, ripple())
            .pointerInput(Unit) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false).also {
                        if (it.pressed != it.previousPressed) it.consume()
                    }
                    val press = PressInteraction.Press(down.position)
                    interactionSource.tryEmit(press)
                    inputEventDispatcher.sendDown(keyData)
                    inputFeedbackController.keyPress(keyData)
                    val up = waitForUpOrCancellation()
                    if (up != null) {
                        interactionSource.tryEmit(PressInteraction.Release(press))
                        inputEventDispatcher.sendUp(keyData)
                    } else {
                        interactionSource.tryEmit(PressInteraction.Cancel(press))
                        inputEventDispatcher.sendCancel(keyData)
                    }
                }
            },
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}
