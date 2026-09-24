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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.automirrored.outlined.Backspace
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.ContentPasteGo
import androidx.compose.material.icons.filled.EmojiEmotions
import androidx.compose.material.icons.filled.FirstPage
import androidx.compose.material.icons.filled.KeyboardDoubleArrowLeft
import androidx.compose.material.icons.filled.KeyboardDoubleArrowRight
import androidx.compose.material.icons.filled.KeyboardHide
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LastPage
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.VerticalAlignBottom
import androidx.compose.material.icons.filled.VerticalAlignTop
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
import com.drs.smartkeyboard.drs.DrsHybridViewMode
import com.drs.smartkeyboard.drs.DrsProfileManager
import com.drs.smartkeyboard.drs.DrsRuntimeState
import com.drs.smartkeyboard.drs.DrsStore
import com.drs.smartkeyboard.drs.DrsTechToolbarKeys
import com.drs.smartkeyboard.drs.DrsUnified
import com.drs.smartkeyboard.drs.DrsUnifiedTools
import com.drs.smartkeyboard.drs.DrsUserPath
import com.drs.smartkeyboard.ime.ImeUiMode
import com.drs.smartkeyboard.ime.keyboard.DrsImeSizing
import com.drs.smartkeyboard.ime.text.keyboard.TextKeyData
import com.drs.smartkeyboard.ime.text.key.KeyCode
import com.drs.smartkeyboard.ime.text.key.KeyType
import com.drs.smartkeyboard.ime.theme.DrsImeUi
import com.drs.smartkeyboard.keyboardManager
import androidx.compose.material3.Text
import org.drs.lib.snygg.ui.SnyggIcon
import org.drs.lib.snygg.ui.SnyggIconButton
import org.drs.lib.snygg.ui.SnyggRow

/**
 * DRS v1.0.7 — الشريط الموحد لنظام «كلاهما».
 *
 * One strip that serves all three systems and renders the RIGHT tools for
 * the active display level:
 *  - النظام العادي: hidden by default, opt-in from the unified tools
 *    manager; shows the simple level's tools.
 *  - النظام التقني: shows under the same conditions as the former
 *    technical toolbar (profile toggle or coding-context auto show) and
 *    renders the advanced level (user's technical keys + shared editing
 *    tools).
 *  - نظام كلاهما: adds the leading level-cycle button (بسيط/تقني/مزدوج)
 *    and renders the stored level - DUAL merges both worlds in one strip.
 *
 * Every button dispatches a REAL KeyCode through the input event
 * dispatcher (the same pipeline the physical layout uses), and the strip
 * never renders in password fields. Customization (order / pins / hidden
 * / per-level visibility) is persisted in DrsState.
 */

/** Resolves the icon for a catalogue tool (all ids covered). */
private fun iconForTool(id: String) = when (id) {
    "emoji" -> Icons.Default.EmojiEmotions
    "clipboard" -> Icons.Default.ContentPaste
    "paste" -> Icons.Default.ContentPasteGo
    "numbers" -> Icons.Default.Numbers
    "language" -> Icons.Default.Language
    "settings" -> Icons.Default.Settings
    "share" -> Icons.Default.Share
    "undo" -> Icons.AutoMirrored.Filled.Undo
    "redo" -> Icons.AutoMirrored.Filled.Redo
    "select_all" -> Icons.Default.SelectAll
    "copy" -> Icons.Default.ContentCopy
    "cut" -> Icons.Default.ContentCut
    "select_word" -> Icons.Default.TextFields
    "word_left" -> Icons.Default.KeyboardDoubleArrowLeft
    "word_right" -> Icons.Default.KeyboardDoubleArrowRight
    "line_start" -> Icons.Default.FirstPage
    "line_end" -> Icons.Default.LastPage
    "delete_word" -> Icons.AutoMirrored.Outlined.Backspace
    "hide_keyboard" -> Icons.Default.KeyboardHide
    // DRS v1.0.8: the new unified tools.
    "theme_cycle" -> Icons.Default.Palette
    "insert_date_time" -> Icons.Default.Schedule
    "text_start" -> Icons.Default.VerticalAlignTop
    "text_end" -> Icons.Default.VerticalAlignBottom
    else -> Icons.Default.Build
}

@Composable
fun DrsUnifiedStrip(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val keyboardManager by context.keyboardManager()

    val drsState by DrsStore.state.collectAsState()
    val contextMode by DrsRuntimeState.contextMode.collectAsState()

    val profile = DrsProfileManager.activeProfile(drsState)
    val isHybrid = drsState.userPath == DrsUserPath.HYBRID.name
    val isNormal = drsState.userPath == DrsUserPath.NORMAL.name
    val isTechnical = drsState.userPath == DrsUserPath.TECHNICAL.name

    // Same auto-show rule the technical toolbar has always used: when the
    // context detector sees a coding/technical field, technical and hybrid
    // users get the strip even if the profile toggle is off.
    val autoTechStrip = drsState.contextModesEnabled &&
        (contextMode == DrsContextMode.CODING || contextMode == DrsContextMode.TECHNICAL) &&
        (isTechnical || isHybrid)
    val legacyVisible = ((profile?.techStripEnabled ?: false) || autoTechStrip) &&
        contextMode != DrsContextMode.PASSWORD
    val normalOptIn = isNormal && drsState.unifiedStripForNormal &&
        contextMode != DrsContextMode.PASSWORD
    val visible = if (isNormal) normalOptIn else legacyVisible
    if (!visible) return

    val view = DrsUnifiedTools.viewForSystem(drsState.userPath, drsState.hybridViewMode)
    val tools = remember(
        view, drsState.unifiedToolOrder, drsState.hiddenUnifiedTools,
        drsState.pinnedUnifiedTools, drsState.unifiedToolViews,
    ) {
        DrsUnifiedTools.resolveFor(
            view = view,
            hidden = drsState.hiddenUnifiedTools,
            pinned = drsState.pinnedUnifiedTools,
            order = drsState.unifiedToolOrder,
            viewOverrides = drsState.unifiedToolViews,
        )
    }

    // Advanced technical keys ride along in the advanced and dual levels,
    // preserving the user's persisted arrangement from the toolbar editor.
    val techKeys = if (view != DrsHybridViewMode.SIMPLE) {
        remember(drsState.techToolbarKeys) {
            DrsTechToolbarKeys.resolve(drsState.techToolbarKeys)
        }
    } else {
        emptyList()
    }

    val isTextToolsOpen = keyboardManager.activeState.imeUiMode == ImeUiMode.TEXT_TOOLS
    // DRS v1.0.8: the strip tracks the real Smartbar height (which follows
    // the keyboard height scale) instead of a hardcoded 40.dp, so growing
    // the keyboard grows the strip consistently.
    val stripHeight = DrsImeSizing.smartbarHeight
    SnyggRow(
        DrsImeUi.Smartbar.elementName,
        modifier = modifier
            .fillMaxWidth()
            .height(stripHeight)
            .horizontalScroll(rememberScrollState()),
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
    ) {
        // 1) نظام كلاهما: quick display-level cycle (بسيط -> تقني -> مزدوج).
        //    Persisted in DrsState; only visibility ever changes.
        if (isHybrid) {
            SnyggIconButton(
                elementName = DrsImeUi.SmartbarActionKey.elementName,
                onClick = { DrsUnified.cycleViewMode() },
                modifier = Modifier.sizeIn(minWidth = 44.dp).height(stripHeight),
            ) {
                Text(
                    text = viewModeLabel(view),
                    fontSize = 13.sp,
                    maxLines = 1,
                )
            }
        }

        // 2) the unified catalogue tools for the active level. The text
        //    tools entry toggles its panel (Close icon while open), so
        //    hiding it in the manager also really removes the button.
        tools.forEach { tool ->
            val isTextTools = tool.code == KeyCode.IME_UI_MODE_TEXT_TOOLS
            SnyggIconButton(
                elementName = DrsImeUi.SmartbarActionKey.elementName,
                onClick = {
                    keyboardManager.inputEventDispatcher.sendDownUp(
                        TextKeyData(type = tool.type, code = tool.code, label = tool.id),
                    )
                    if (isTextTools) {
                        DrsAdaptationEngine.recordTechToolUse()
                    } else {
                        DrsAdaptationEngine.recordToolUse(tool.code)
                    }
                },
                modifier = Modifier.sizeIn(minWidth = 38.dp).height(stripHeight),
            ) {
                when {
                    isTextTools -> SnyggIcon(
                        imageVector = if (isTextToolsOpen) Icons.Default.Close else Icons.Default.Build,
                    )
                    tool.id == "symbols" -> Text(text = "&#", fontSize = 14.sp, maxLines = 1)
                    else -> SnyggIcon(imageVector = iconForTool(tool.id))
                }
            }
        }

        // 3) the user's technical keys (advanced/dual levels only).
        techKeys.forEach { key ->
            SnyggIconButton(
                elementName = DrsImeUi.SmartbarActionKey.elementName,
                onClick = {
                    keyboardManager.inputEventDispatcher.sendDownUp(
                        TextKeyData(type = key.type, code = key.code, label = key.label),
                    )
                    DrsAdaptationEngine.recordTechToolUse()
                },
                modifier = Modifier.sizeIn(minWidth = 38.dp).height(stripHeight),
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

/** Short on-strip label of each display level (Arabic-first). */
private fun viewModeLabel(mode: DrsHybridViewMode): String = when (mode) {
    DrsHybridViewMode.SIMPLE -> "بسيط"
    DrsHybridViewMode.ADVANCED -> "تقني"
    DrsHybridViewMode.DUAL -> "مزدوج"
}
