/*
 * Copyright (C) 2021-2025 The DRS Smart Keyboard Project
 * Copyright (C) 2025 DRS Smart Keyboard contributors
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

package com.drs.smartkeyboard.drs.ui

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.drs.smartkeyboard.drs.DrsAdaptationEngine
import com.drs.smartkeyboard.drs.DrsContextMode
import com.drs.smartkeyboard.drs.DrsProfileManager
import com.drs.smartkeyboard.drs.DrsRuntimeState
import com.drs.smartkeyboard.drs.DrsStore
import com.drs.smartkeyboard.drs.DrsUserPath
import com.drs.smartkeyboard.ime.text.key.KeyCode
import com.drs.smartkeyboard.ime.text.key.KeyType
import com.drs.smartkeyboard.ime.text.keyboard.TextKeyData
import com.drs.smartkeyboard.ime.theme.DrsImeUi
import com.drs.smartkeyboard.keyboardManager
import org.drs.lib.snygg.ui.SnyggIconButton
import org.drs.lib.snygg.ui.SnyggRow

/**
 * One button of the DRS technical toolbar.
 */
private data class DrsTechKey(val label: String, val code: Int, val type: KeyType)

/**
 * DRS technical toolbar: a horizontally scrollable strip shown above the
 * smartbar whenever the active profile enables it. Provides quick access to
 * coding symbols ({ } [ ] ( ) < > / \ | @ # $ % ^ & *), Tab/Esc and arrow
 * navigation keys. Hidden automatically in password fields.
 *
 * Theme is inherited from the existing smartbar Snygg elements, so it adapts
 * to every installed theme without extra styling rules.
 */
@Composable
fun DrsTechToolbar(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val keyboardManager by context.keyboardManager()

    val drsState by DrsStore.state.collectAsState()
    val contextMode by DrsRuntimeState.contextMode.collectAsState()

    val profile = DrsProfileManager.activeProfile(drsState)

    // The toolbar shows automatically for technical/hybrid users when the
    // context detector sees a coding or technical field, even if the active
    // profile has the strip disabled. It never shows in password fields.
    val autoTechStrip = drsState.contextModesEnabled &&
        (contextMode == DrsContextMode.CODING || contextMode == DrsContextMode.TECHNICAL) &&
        (drsState.userPath == DrsUserPath.TECHNICAL.name || drsState.userPath == DrsUserPath.HYBRID.name)
    val visible = ((profile?.techStripEnabled ?: false) || autoTechStrip) &&
        contextMode != DrsContextMode.PASSWORD
    if (!visible) return

    val keys = remember {
        listOf(
            DrsTechKey("←", KeyCode.ARROW_LEFT, KeyType.NAVIGATION),
            DrsTechKey("→", KeyCode.ARROW_RIGHT, KeyType.NAVIGATION),
            DrsTechKey("↑", KeyCode.ARROW_UP, KeyType.NAVIGATION),
            DrsTechKey("↓", KeyCode.ARROW_DOWN, KeyType.NAVIGATION),
            DrsTechKey("Tab", KeyCode.TAB, KeyType.FUNCTION),
            DrsTechKey("Esc", KeyCode.ESCAPE, KeyType.FUNCTION),
            DrsTechKey("{", '{'.code, KeyType.CHARACTER),
            DrsTechKey("}", '}'.code, KeyType.CHARACTER),
            DrsTechKey("[", '['.code, KeyType.CHARACTER),
            DrsTechKey("]", ']'.code, KeyType.CHARACTER),
            DrsTechKey("(", '('.code, KeyType.CHARACTER),
            DrsTechKey(")", ')'.code, KeyType.CHARACTER),
            DrsTechKey("<", '<'.code, KeyType.CHARACTER),
            DrsTechKey(">", '>'.code, KeyType.CHARACTER),
            DrsTechKey("/", '/'.code, KeyType.CHARACTER),
            DrsTechKey("\\", '\\'.code, KeyType.CHARACTER),
            DrsTechKey("|", '|'.code, KeyType.CHARACTER),
            DrsTechKey("@", '@'.code, KeyType.CHARACTER),
            DrsTechKey("#", '#'.code, KeyType.CHARACTER),
            DrsTechKey("$", '$'.code, KeyType.CHARACTER),
            DrsTechKey("%", '%'.code, KeyType.CHARACTER),
            DrsTechKey("^", '^'.code, KeyType.CHARACTER),
            DrsTechKey("&", '&'.code, KeyType.CHARACTER),
            DrsTechKey("*", '*'.code, KeyType.CHARACTER),
        )
    }

    SnyggRow(
        DrsImeUi.Smartbar.elementName,
        modifier = modifier
            .fillMaxWidth()
            .height(40.dp)
            .horizontalScroll(rememberScrollState()),
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
    ) {
        keys.forEach { key ->
            SnyggIconButton(
                elementName = DrsImeUi.SmartbarActionKey.elementName,
                onClick = {
                    keyboardManager.inputEventDispatcher.sendDownUp(
                        TextKeyData(type = key.type, code = key.code, label = key.label),
                    )
                    DrsAdaptationEngine.recordTechToolUse()
                },
                modifier = Modifier.sizeIn(minWidth = 38.dp).height(40.dp),
            ) {
                Text(
                    text = key.label,
                    fontSize = 14.sp,
                    maxLines = 1,
                )
            }
        }
    }
}
