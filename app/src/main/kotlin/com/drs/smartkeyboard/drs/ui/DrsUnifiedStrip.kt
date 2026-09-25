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

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.automirrored.outlined.Backspace
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Abc
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloseFullscreen
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.ContentPasteGo
import androidx.compose.material.icons.filled.Dialpad
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.East
import androidx.compose.material.icons.filled.EmojiEmotions
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Spellcheck
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.FirstPage
import androidx.compose.material.icons.filled.Functions
import androidx.compose.material.icons.filled.KeyboardDoubleArrowLeft
import androidx.compose.material.icons.filled.KeyboardDoubleArrowRight
import androidx.compose.material.icons.filled.KeyboardHide
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LastPage
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.material.icons.filled.OpenInFull
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PictureInPictureAlt
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.MergeType
import androidx.compose.material.icons.filled.Splitscreen
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.TextFormat
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.VerticalAlignBottom
import androidx.compose.material.icons.filled.VerticalAlignTop
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material.icons.filled.West
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.drs.smartkeyboard.R
import com.drs.smartkeyboard.drs.DrsAdaptationEngine
import com.drs.smartkeyboard.drs.DrsContextMode
import com.drs.smartkeyboard.drs.DrsHybridViewMode
import com.drs.smartkeyboard.drs.DrsRuntimeState
import com.drs.smartkeyboard.drs.DrsStore
import com.drs.smartkeyboard.drs.DrsSystems
import com.drs.smartkeyboard.drs.DrsTechToolbarKeys
import com.drs.smartkeyboard.drs.DrsUnified
import com.drs.smartkeyboard.drs.DrsUnifiedTools
import com.drs.smartkeyboard.drs.DrsUserPath
import com.drs.smartkeyboard.app.DrsPreferenceStore
import com.drs.smartkeyboard.ime.ImeUiMode
import com.drs.smartkeyboard.ime.keyboard.DrsImeSizing
import com.drs.smartkeyboard.ime.text.keyboard.TextKeyData
import com.drs.smartkeyboard.ime.text.key.KeyCode
import com.drs.smartkeyboard.ime.text.key.KeyType
import com.drs.smartkeyboard.ime.theme.DrsImeUi
import com.drs.smartkeyboard.ime.window.ImeWindowSpec
import com.drs.smartkeyboard.ime.window.LocalWindowController
import com.drs.smartkeyboard.keyboardManager
import androidx.compose.material3.Text
import org.drs.jetpref.datastore.model.collectAsState
import org.drs.lib.compose.stringRes
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

/** Resolves the icon for a catalogue tool (all ids covered, shared with the slot editor). */
internal fun iconForTool(id: String) = when (id) {
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
    // DRS v1.1.0: one-handed window toggle + number-row toggle.
    "one_handed" -> Icons.Default.CloseFullscreen
    "number_row" -> Icons.Default.Dialpad
    // DRS v1.2.0: incognito + autocorrect toggles.
    "incognito" -> Icons.Default.VisibilityOff
    "autocorrect" -> Icons.Default.Spellcheck
    // DRS v1.3.0: clipboard clear + voice input.
    "clipboard_clear" -> Icons.Default.DeleteSweep
    "voice_input" -> Icons.Default.Mic
    // DRS v1.4.0: floating window + smartbar visibility toggles.
    "floating_mode" -> Icons.Default.PictureInPictureAlt
    "smartbar_toggle" -> Icons.Default.ViewAgenda
    // DRS v1.5.0: history wipe + next language + resize mode.
    "clipboard_history_clear" -> Icons.Default.History
    "next_language" -> Icons.Default.Translate
    "resize_mode" -> Icons.Default.OpenInFull
    // DRS v1.6.0: full history wipe + previous language + one-handed sides
    // + next keyboard app.
    "clipboard_full_clear" -> Icons.Default.DeleteSweep
    "prev_language" -> Icons.Default.Replay
    "one_handed_left" -> Icons.Default.West
    "one_handed_right" -> Icons.Default.East
    "next_keyboard_app" -> Icons.Default.SwapHoriz
    // DRS v1.7.0: the active-clip pin toggle.
    "clipboard_pin" -> Icons.Default.PushPin
    // DRS v1.8.0: quick actions overflow + the actions editor.
    "quick_actions" -> Icons.Default.Apps
    "actions_editor" -> Icons.Default.Tune
    // DRS v1.15.0: the three smart panels.
    "diacritics_panel" -> Icons.Default.TextFormat
    "smart_symbols" -> Icons.Default.Functions
    "arabic_letters" -> Icons.Default.Abc
    // DRS v1.19.0: the split/merge keyboard toggles.
    "split_keyboard" -> Icons.Default.Splitscreen
    "merge_keyboard" -> Icons.Default.MergeType
    else -> Icons.Default.Build
}

@Composable
fun DrsUnifiedStrip(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val keyboardManager by context.keyboardManager()

    val drsState by DrsStore.state.collectAsState()
    val contextMode by DrsRuntimeState.contextMode.collectAsState()

    val isHybrid = drsState.userPath == DrsUserPath.HYBRID.name

    // DRS v1.8.0: the tasks bar (شريط المهام) sits ABOVE the suggestions
    // strip for ALL three user systems — العادي والتقني وكلاهما. Exactly
    // two gates: the persisted master switch (unifiedStripEnabled, on by
    // default, toggled from the tools drawer) and the password-field
    // guard.
    val visible = drsState.unifiedStripEnabled &&
        contextMode != DrsContextMode.PASSWORD
    if (!visible) return

    // DRS v1.8.0: real on/off state of the toggle tools, read from the
    // same engine sources that own them (IME flags, jetpref settings and
    // the window controller) so the active dot never lies.
    val windowController = LocalWindowController.current
    val windowSpec by windowController.activeWindowSpec.collectAsState()
    val prefs by DrsPreferenceStore
    val suggestionEnabled by prefs.suggestion.enabled.collectAsState()
    val numberRowEnabled by prefs.keyboard.numberRow.collectAsState()
    val smartbarEnabled by prefs.smartbar.enabled.collectAsState()
    val toggleStates = remember(
        keyboardManager.activeState.isIncognitoMode,
        suggestionEnabled, numberRowEnabled, smartbarEnabled,
        windowSpec,
        keyboardManager.activeState.inputCtrlState,
        keyboardManager.activeState.inputAltState,
    ) {
        DrsUnifiedTools.ToggleStates(
            incognito = keyboardManager.activeState.isIncognitoMode,
            autocorrect = suggestionEnabled,
            numberRow = numberRowEnabled,
            smartbarVisible = smartbarEnabled,
            floatingWindow = windowSpec.props is ImeWindowSpec.Floating,
            // DRS v1.22.0: the revived CTRL/ALT latches show the truth —
            // the tile dot reads the same latch state the input pipeline
            // arms and consumes.
            ctrlArmed = keyboardManager.activeState.inputCtrlState.isArmed,
            altArmed = keyboardManager.activeState.inputAltState.isArmed,
        )
    }
    // Accent for the active dot, from the active user system's palette
    // (same identity the settings screens use).
    val systemSpec = DrsSystems.specOfName(drsState.userPath)
    val accentColor = if (isSystemInDarkTheme()) systemSpec.accentNight else systemSpec.accent

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

    // DRS v1.16.0: «شريط المهام ثابت يعرض 10 مهام فقط» — the FIXED
    // slots. The user's pins lead in pin order, the visible defaults
    // fill the next slots, and the resolved visible catalogue pads the
    // tail so the bar always renders exactly ten tasks (or less only
    // when the whole catalogue is smaller). Hidden tools never occupy a
    // slot, honoring the manager's show/hide switches.
    val hiddenSet = remember(drsState.hiddenUnifiedTools) {
        drsState.hiddenUnifiedTools.toHashSet()
    }
    val visiblePins = drsState.pinnedUnifiedTools.filter { it !in hiddenSet }
    val visibleDefaults = DrsUnifiedTools.ALL
        .filter { it.defaultPinned && it.id !in hiddenSet }
        .filter { DrsUnifiedTools.isVisibleIn(it, view, drsState.unifiedToolViews[it.id]) }
        .map { it.id }
    val slots = remember(view, visiblePins, visibleDefaults, tools) {
        DrsUnifiedTools.fixedSlots(visiblePins, visibleDefaults, tools.map { it.id })
    }

    // Advanced technical keys ride along in the advanced and dual levels,
    // preserving the user's persisted arrangement from the toolbar editor.
    // They are KEYS, not tasks — the ten task slots stay ten either way;
    // the bar only scrolls when this tail exists.
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
    val barModifier = if (techKeys.isEmpty()) {
        modifier
            .fillMaxWidth()
            .height(stripHeight)
    } else {
        modifier
            .fillMaxWidth()
            .height(stripHeight)
            .horizontalScroll(rememberScrollState())
    }
    SnyggRow(
        DrsImeUi.Smartbar.elementName,
        modifier = barModifier,
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
    ) {
        // 0) the side-pull handle (زر السحب الجانبي). Always first so the
        //    pinned-tools drawer stays reachable — the drawer is where
        //    tasks are reordered (up/down), pinned and unpinned.
        SnyggIconButton(
            elementName = DrsImeUi.SmartbarActionKey.elementName,
            onClick = {
                val state = keyboardManager.activeState
                DrsRuntimeState.closeStripSlotEditor()
                state.isActionsOverflowVisible = false
                state.isToolsDrawerVisible = !state.isToolsDrawerVisible
            },
            modifier = Modifier.sizeIn(minWidth = 40.dp).height(stripHeight),
        ) {
            SnyggIcon(imageVector = Icons.Default.DragHandle)
        }

        // 1) the ten FIXED task slots — every slot dispatches its real
        //    KeyCode, and a LONG-PRESS opens the slot editor (change the
        //    task occupying that slot, «إمكانية تغييرها»).
        slots.forEachIndexed { index, toolId ->
            val tool = DrsUnifiedTools.byId(toolId) ?: return@forEachIndexed
            val isTextTools = tool.code == KeyCode.IME_UI_MODE_TEXT_TOOLS
            val toggleOn = DrsUnifiedTools.toggleStateOf(tool.id, toggleStates)
            val slotModifier = if (techKeys.isEmpty()) {
                Modifier.weight(1f).height(stripHeight)
            } else {
                Modifier.sizeIn(minWidth = 38.dp).height(stripHeight)
            }
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
                onLongClick = {
                    keyboardManager.activeState.isToolsDrawerVisible = false
                    keyboardManager.activeState.isActionsOverflowVisible = false
                    DrsRuntimeState.openStripSlotEditor(index)
                },
                modifier = slotModifier,
            ) {
                androidx.compose.foundation.layout.Box(
                    contentAlignment = androidx.compose.ui.Alignment.Center,
                ) {
                    when {
                        isTextTools -> SnyggIcon(
                            imageVector = if (isTextToolsOpen) Icons.Default.Close else Icons.Default.Build,
                        )
                        tool.id == "symbols" -> Text(text = "&#", fontSize = 14.sp, maxLines = 1)
                        else -> SnyggIcon(imageVector = iconForTool(tool.id))
                    }
                    // DRS v1.8.0: the real on/off state of toggle tools
                    // (incognito/autocorrect/number row/smartbar/floating)
                    // as a small accent dot — the tile shows the truth.
                    if (toggleOn == true) {
                        androidx.compose.foundation.layout.Box(
                            modifier = Modifier
                                .align(androidx.compose.ui.Alignment.TopEnd)
                                .padding(top = 6.dp, end = 5.dp)
                                .size(6.dp)
                                .clip(androidx.compose.foundation.shape.CircleShape)
                                .background(accentColor),
                        )
                    }
                }
            }
        }

        // 2) the user's technical keys (advanced/dual levels only).
        techKeys.forEach { key ->
            // DRS v1.22.0: the modifier latch keys show their live armed
            // state the same way the toggle tools do — the tile shows the
            // truth, LATCHED and LOCKED alike.
            val latchOn = when (key.id) {
                "ctrl" -> toggleStates.ctrlArmed
                "alt" -> toggleStates.altArmed
                else -> null
            }
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
                androidx.compose.foundation.layout.Box(
                    contentAlignment = androidx.compose.ui.Alignment.Center,
                ) {
                    Text(
                        text = key.label,
                        fontSize = 14.sp,
                        maxLines = 1,
                    )
                    if (latchOn == true) {
                        androidx.compose.foundation.layout.Box(
                            modifier = Modifier
                                .align(androidx.compose.ui.Alignment.TopEnd)
                                .padding(top = 6.dp, end = 5.dp)
                                .size(6.dp)
                                .clip(androidx.compose.foundation.shape.CircleShape)
                                .background(accentColor),
                        )
                    }
                }
            }
        }
    }
}

/**
 * Short on-strip label of each display level. DRS v1.19.0: the labels were
 * hardcoded Arabic literals ("بسيط/تقني/مزدوج") that would leak into every
 * non-Arabic locale the day someone calls this — they now resolve the same
 * drs__unified__level_* strings the tools drawer uses. The function is
 * composable so the resource lookup stays legal.
 */
@Composable
internal fun viewModeLabel(mode: DrsHybridViewMode): String = when (mode) {
    DrsHybridViewMode.SIMPLE -> stringRes(R.string.drs__unified__level_simple)
    DrsHybridViewMode.ADVANCED -> stringRes(R.string.drs__unified__level_advanced)
    DrsHybridViewMode.DUAL -> stringRes(R.string.drs__unified__level_dual)
}
