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

package com.drs.smartkeyboard.ime.text

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import com.drs.smartkeyboard.R
import com.drs.smartkeyboard.app.DrsPreferenceStore
import com.drs.smartkeyboard.drs.ui.DrsUnifiedStrip
import com.drs.smartkeyboard.ime.smartbar.IncognitoDisplayMode
import com.drs.smartkeyboard.ime.smartbar.InlineSuggestionsStyleCache
import com.drs.smartkeyboard.ime.smartbar.Smartbar
import com.drs.smartkeyboard.ime.smartbar.quickaction.QuickActionsOverflowPanel
import com.drs.smartkeyboard.ime.text.keyboard.TextKeyboardLayout
import com.drs.smartkeyboard.ime.theme.DrsImeUi
import com.drs.smartkeyboard.keyboardManager
import org.drs.jetpref.datastore.model.collectAsState
import org.drs.lib.snygg.ui.SnyggIcon

@Composable
fun TextInputLayout(
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val keyboardManager by context.keyboardManager()

    val prefs by DrsPreferenceStore

    val state by keyboardManager.activeState.collectAsState()
    val evaluator by keyboardManager.activeEvaluator.collectAsState()

    InlineSuggestionsStyleCache()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight(),
    ) {
        // DRS v1.0.7: unified strip for all three systems - renders the
        // right tools per active display level (بسيط/تقني/مزدوج) and
        // supersedes the former technical-only toolbar.
        DrsUnifiedStrip()
        Smartbar()
        if (state.isActionsOverflowVisible) {
            QuickActionsOverflowPanel()
        } else {
            Box {
                val incognitoDisplayMode by prefs.keyboard.incognitoDisplayMode.collectAsState()
                val showIncognitoIcon = evaluator.state.isIncognitoMode &&
                    incognitoDisplayMode == IncognitoDisplayMode.DISPLAY_BEHIND_KEYBOARD
                if (showIncognitoIcon) {
                    SnyggIcon(
                        DrsImeUi.IncognitoModeIndicator.elementName,
                        modifier = Modifier
                            .matchParentSize()
                            .align(Alignment.Center),
                        painter = painterResource(R.drawable.ic_incognito),
                    )
                }
                TextKeyboardLayout(evaluator = evaluator)
            }
        }
    }
}
