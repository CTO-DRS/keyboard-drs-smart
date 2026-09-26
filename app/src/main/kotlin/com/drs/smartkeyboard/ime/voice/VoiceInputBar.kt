/*
 * Copyright (C) 2025-2026 The DRS Smart Keyboard Project
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

package com.drs.smartkeyboard.ime.voice

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardVoice
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.drs.smartkeyboard.R
import com.drs.smartkeyboard.ime.keyboard.DrsImeSizing
import com.drs.smartkeyboard.ime.theme.DrsImeUi
import com.drs.smartkeyboard.keyboardManager
import org.drs.lib.snygg.ui.SnyggBox
import org.drs.lib.snygg.ui.SnyggIcon
import org.drs.lib.snygg.ui.SnyggIconButton
import org.drs.lib.snygg.ui.SnyggText

/**
 * DRS v1.23.0: the live dictation bar — «شريط الاستماع». While the
 * built-in recognizer owns the session it replaces the Smartbar: a
 * microphone glyph, the live partial transcript (or the honest
 * «تحدث الآن…» hint before any words arrive, or the localized error),
 * and a cancel button that stops listening immediately. The keyboard
 * stays visible below, so the user never loses their typing context.
 * Themed through the exact smartbar snygg elements, so every user theme
 * paints it consistently.
 */
@Composable
fun VoiceInputBar() {
    val context = LocalContext.current
    val keyboardManager by context.keyboardManager()
    val uiState by DrsVoiceInputBus.uiState.collectAsState()

    val hintText = stringResource(R.string.voice__listening)
    val message = when (val state = uiState) {
        is VoiceUiState.Listening -> state.partial.ifBlank { hintText }
        is VoiceUiState.Error -> stringResource(state.resId)
        VoiceUiState.Idle -> hintText
    }
    val stopLabel = stringResource(R.string.voice__cancel)

    SnyggBox(
        elementName = DrsImeUi.Smartbar.elementName,
        modifier = Modifier
            .fillMaxWidth()
            .height(DrsImeSizing.smartbarHeight),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = DrsImeSizing.smartbarHeight / 5f),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SnyggIcon(
                elementName = DrsImeUi.SmartbarActionTileIcon.elementName,
                imageVector = Icons.Default.KeyboardVoice,
                contentDescription = null,
            )
            Spacer(modifier = Modifier.height(DrsImeSizing.smartbarHeight / 8f))
            SnyggText(
                elementName = DrsImeUi.SmartbarActionTileText.elementName,
                modifier = Modifier.weight(1f),
                text = message,
            )
            SnyggIconButton(
                elementName = DrsImeUi.SmartbarSharedActionsToggle.elementName,
                onClick = { keyboardManager.voiceController.stop() },
            ) {
                SnyggIcon(
                    elementName = DrsImeUi.SmartbarActionTileIcon.elementName,
                    imageVector = Icons.Default.Close,
                    contentDescription = stopLabel,
                )
            }
        }
    }
}
