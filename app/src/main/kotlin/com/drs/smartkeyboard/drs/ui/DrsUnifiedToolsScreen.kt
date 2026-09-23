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

@file:OptIn(ExperimentalMaterial3Api::class)

package com.drs.smartkeyboard.drs.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.drs.smartkeyboard.R
import com.drs.smartkeyboard.app.Routes
import com.drs.smartkeyboard.drs.DrsHybridViewMode
import com.drs.smartkeyboard.drs.DrsSettingScope
import com.drs.smartkeyboard.drs.DrsStore
import com.drs.smartkeyboard.drs.DrsSystems
import com.drs.smartkeyboard.drs.DrsToolGroup
import com.drs.smartkeyboard.drs.DrsToolView
import com.drs.smartkeyboard.drs.DrsUnified
import com.drs.smartkeyboard.drs.DrsUnifiedTools
import com.drs.smartkeyboard.lib.compose.DrsScreen
import org.drs.lib.compose.stringRes
import androidx.navigation.NavHostController
import com.drs.smartkeyboard.app.LocalNavController

/**
 * DRS v1.0.7 — مركز الأدوات الذكية الموحد.
 *
 * Full manager over the unified strip's catalogue: every tool here is a
 * real keyboard action (see [DrsUnifiedTools]), and every control on this
 * screen really changes the strip:
 *  - إظهار/إخفاء per tool, تثبيت to the strip head, and ترتيب up/down.
 *  - نطاق الظهور per tool: العادي / التقني / كلاهما.
 *  - إظهار الشريط للوضع العادي (opt-in) and إعادة الضبط to defaults.
 *
 * The advanced technical keys (Tab, braces, arrows…) keep their dedicated
 * editor and ride along in the advanced/dual levels.
 */
@Composable
fun DrsUnifiedToolsScreen() = DrsScreen {
    title = stringRes(R.string.drs__unified__tools_title)
    navigationIconVisible = true
    previewFieldVisible = false

    val navController = LocalNavController.current
    val drsState by DrsStore.state.collectAsState()
    val spec = DrsSystems.specOfName(drsState.userPath)
    val accent = if (isSystemInDarkTheme()) spec.accentNight else spec.accent
    val view = DrsUnifiedTools.viewForSystem(drsState.userPath, drsState.hybridViewMode)
    val resolved = remember(
        view, drsState.unifiedToolOrder, drsState.hiddenUnifiedTools,
        drsState.pinnedUnifiedTools, drsState.unifiedToolViews,
    ) {
        DrsUnifiedTools.resolveFor(
            view, drsState.hiddenUnifiedTools, drsState.pinnedUnifiedTools,
            drsState.unifiedToolOrder, drsState.unifiedToolViews,
        )
    }

    content {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            // ---------------- intro ----------------
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.6f),
                ),
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = stringRes(R.string.drs__unified__tools_hint),
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = stringRes(
                            R.string.drs__unified__tools_live_count,
                            "count" to resolved.size.toString(),
                        ),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = accent,
                    )
                }
            }

            // ---------------- level selector ----------------
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.6f),
                ),
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = stringRes(R.string.drs__unified__level_title),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        DrsHybridViewMode.entries.forEach { mode ->
                            FilterChip(
                                selected = view == mode,
                                onClick = { DrsUnified.setViewMode(mode) },
                                label = {
                                    Text(
                                        when (mode) {
                                            DrsHybridViewMode.SIMPLE -> stringRes(R.string.drs__unified__level_simple)
                                            DrsHybridViewMode.ADVANCED -> stringRes(R.string.drs__unified__level_advanced)
                                            DrsHybridViewMode.DUAL -> stringRes(R.string.drs__unified__level_dual)
                                        }
                                    )
                                },
                            )
                        }
                    }
                }
            }

            // ---------------- strip for normal system opt-in ----------------
            ToolSwitchCard(
                title = stringRes(R.string.drs__unified__tools_strip_for_normal),
                summary = stringRes(R.string.drs__unified__tools_strip_for_normal_summary),
                checked = drsState.unifiedStripForNormal,
                onChange = { DrsUnified.setStripForNormal(it) },
            )

            // ---------------- catalogue by group ----------------
            DrsToolGroup.entries.forEach { group ->
                val groupTools = DrsUnifiedTools.ALL.filter { it.group == group }
                if (groupTools.isEmpty()) return@forEach
                Text(
                    text = stringRes(
                        when (group) {
                            DrsToolGroup.TOOLS -> R.string.drs__unified__group_tools
                            DrsToolGroup.EDITING -> R.string.drs__unified__group_editing
                            DrsToolGroup.CURSOR -> R.string.drs__unified__group_cursor
                        },
                    ),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 4.dp, top = 4.dp),
                )
                groupTools.forEach { tool ->
                    UnifiedToolRow(
                        tool = tool,
                        hidden = tool.id in drsState.hiddenUnifiedTools,
                        pinned = tool.id in drsState.pinnedUnifiedTools,
                        viewOverride = drsState.unifiedToolViews[tool.id],
                    )
                }
            }

            // ---------------- advanced keys note ----------------
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.6f),
                ),
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = stringRes(R.string.drs__unified__tools_advanced_keys_title),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = stringRes(R.string.drs__unified__tools_advanced_keys_body),
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    TextButton(onClick = { navController.navigate(Routes.Settings.DrsTechToolbar) }) {
                        Text(stringRes(R.string.drs__tech_toolbar__title))
                    }
                }
            }

            // ---------------- reset ----------------
            OutlinedButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = { DrsUnified.resetCustomization() },
            ) {
                Text(stringRes(R.string.drs__unified__tools_reset))
            }

            Spacer(Modifier.height(8.dp))
        }
    }
}

/** One catalogue tool row with live controls. */
@Composable
private fun UnifiedToolRow(
    tool: com.drs.smartkeyboard.drs.DrsUnifiedTool,
    hidden: Boolean,
    pinned: Boolean,
    viewOverride: String?,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.6f),
        ),
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = toolTitle(tool.id),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface.copy(
                                alpha = if (hidden) 0.5f else 1f,
                            ),
                        )
                        Spacer(Modifier.padding(horizontal = 4.dp))
                        Text(
                            text = stringRes(
                                when (tool.scope) {
                                    DrsSettingScope.BASIC -> R.string.drs__unified__scope_basic
                                    DrsSettingScope.ADVANCED -> R.string.drs__unified__scope_advanced
                                    DrsSettingScope.SHARED -> R.string.drs__unified__scope_shared
                                },
                            ),
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.tertiary,
                        )
                    }
                    Text(
                        text = toolDesc(tool.id),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                // pin toggle
                IconButton(onClick = { DrsUnified.setToolPinned(tool.id, !pinned) }) {
                    Icon(
                        imageVector = if (pinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                        contentDescription = stringRes(R.string.drs__unified__tools_pin),
                        tint = if (pinned) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp),
                    )
                }
                // reorder
                IconButton(onClick = { DrsUnified.moveTool(tool.id, up = true) }) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowUp,
                        contentDescription = stringRes(R.string.drs__unified__tools_move_up),
                        modifier = Modifier.size(20.dp),
                    )
                }
                IconButton(onClick = { DrsUnified.moveTool(tool.id, up = false) }) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = stringRes(R.string.drs__unified__tools_move_down),
                        modifier = Modifier.size(20.dp),
                    )
                }
                // visibility switch
                Switch(
                    checked = !hidden,
                    onCheckedChange = { checked -> DrsUnified.setToolHidden(tool.id, !checked) },
                )
            }
            Spacer(Modifier.height(4.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                DrsToolView.entries.forEach { option ->
                    FilterChip(
                        selected = viewOverride?.let { it == option.name }
                            ?: (tool.defaultView == option),
                        onClick = { DrsUnified.setToolView(tool.id, option) },
                        label = {
                            Text(
                                when (option) {
                                    DrsToolView.NORMAL -> stringRes(R.string.drs__unified__view_normal)
                                    DrsToolView.TECHNICAL -> stringRes(R.string.drs__unified__view_technical)
                                    DrsToolView.BOTH -> stringRes(R.string.drs__unified__view_both)
                                },
                                fontSize = 11.sp,
                            )
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun ToolSwitchCard(
    title: String,
    summary: String,
    checked: Boolean,
    onChange: (Boolean) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.6f),
        ),
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                Text(
                    text = summary,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Switch(checked = checked, onCheckedChange = onChange)
        }
    }
}

/** Localized title per catalogue tool id. */
@Composable
private fun toolTitle(id: String): String = when (id) {
    "emoji" -> stringRes(R.string.drs__unified__tool_emoji)
    "clipboard" -> stringRes(R.string.drs__unified__tool_clipboard)
    "text_tools" -> stringRes(R.string.drs__unified__tool_text_tools)
    "numbers" -> stringRes(R.string.drs__unified__tool_numbers)
    "symbols" -> stringRes(R.string.drs__unified__tool_symbols)
    "language" -> stringRes(R.string.drs__unified__tool_language)
    "settings" -> stringRes(R.string.drs__unified__tool_settings)
    "paste" -> stringRes(R.string.drs__unified__tool_paste)
    "share" -> stringRes(R.string.drs__unified__tool_share)
    "undo" -> stringRes(R.string.drs__unified__tool_undo)
    "redo" -> stringRes(R.string.drs__unified__tool_redo)
    "select_all" -> stringRes(R.string.drs__unified__tool_select_all)
    "copy" -> stringRes(R.string.drs__unified__tool_copy)
    "cut" -> stringRes(R.string.drs__unified__tool_cut)
    "select_word" -> stringRes(R.string.drs__unified__tool_select_word)
    "word_left" -> stringRes(R.string.drs__unified__tool_word_left)
    "word_right" -> stringRes(R.string.drs__unified__tool_word_right)
    "line_start" -> stringRes(R.string.drs__unified__tool_line_start)
    "line_end" -> stringRes(R.string.drs__unified__tool_line_end)
    "delete_word" -> stringRes(R.string.drs__unified__tool_delete_word)
    "hide_keyboard" -> stringRes(R.string.drs__unified__tool_hide_keyboard)
    else -> id
}

/** Localized description per catalogue tool id (what it really does). */
@Composable
private fun toolDesc(id: String): String = when (id) {
    "emoji" -> stringRes(R.string.drs__unified__tool_emoji_desc)
    "clipboard" -> stringRes(R.string.drs__unified__tool_clipboard_desc)
    "text_tools" -> stringRes(R.string.drs__unified__tool_text_tools_desc)
    "numbers" -> stringRes(R.string.drs__unified__tool_numbers_desc)
    "symbols" -> stringRes(R.string.drs__unified__tool_symbols_desc)
    "language" -> stringRes(R.string.drs__unified__tool_language_desc)
    "settings" -> stringRes(R.string.drs__unified__tool_settings_desc)
    "paste" -> stringRes(R.string.drs__unified__tool_paste_desc)
    "share" -> stringRes(R.string.drs__unified__tool_share_desc)
    "undo" -> stringRes(R.string.drs__unified__tool_undo_desc)
    "redo" -> stringRes(R.string.drs__unified__tool_redo_desc)
    "select_all" -> stringRes(R.string.drs__unified__tool_select_all_desc)
    "copy" -> stringRes(R.string.drs__unified__tool_copy_desc)
    "cut" -> stringRes(R.string.drs__unified__tool_cut_desc)
    "select_word" -> stringRes(R.string.drs__unified__tool_select_word_desc)
    "word_left" -> stringRes(R.string.drs__unified__tool_word_left_desc)
    "word_right" -> stringRes(R.string.drs__unified__tool_word_right_desc)
    "line_start" -> stringRes(R.string.drs__unified__tool_line_start_desc)
    "line_end" -> stringRes(R.string.drs__unified__tool_line_end_desc)
    "delete_word" -> stringRes(R.string.drs__unified__tool_delete_word_desc)
    "hide_keyboard" -> stringRes(R.string.drs__unified__tool_hide_keyboard_desc)
    else -> ""
}
