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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.drs.smartkeyboard.R
import com.drs.smartkeyboard.drs.DrsShortcuts
import com.drs.smartkeyboard.drs.DrsStore
import com.drs.smartkeyboard.lib.compose.DrsScreen
import org.drs.lib.compose.stringRes

private data class DrsShortcutEditorState(
    val id: Long? = null,
    val shortcut: String = "",
    val expansion: String = "",
)

/**
 * DRS Shortcut Manager: create, edit and remove text shortcuts
 * (abbreviation + space -> full text) with template variable support.
 * A shortcut expansion may embed variables like {date}, {time}, {hijri},
 * {clipboard} and {newline} which are resolved at typing time, locally.
 */
@Composable
fun DrsShortcutsScreen() = DrsScreen {
    title = stringRes(R.string.drs__shortcuts__title)
    navigationIconVisible = true
    previewFieldVisible = false

    val drsState by DrsStore.state.collectAsState()

    var editorState by remember { mutableStateOf<DrsShortcutEditorState?>(null) }
    var showRemoveAllDialog by remember { mutableStateOf(false) }

    content {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            // ---------------- Master switch ----------------
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.6f),
                ),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringRes(R.string.drs__shortcuts__enable),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = stringRes(R.string.drs__shortcuts__enable_summary),
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Switch(
                        checked = drsState.shortcutsEnabled,
                        onCheckedChange = { checked -> DrsShortcuts.setEnabled(checked) },
                    )
                }
            }

            // ---------------- Presets ----------------
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.6f),
                ),
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = stringRes(R.string.drs__shortcuts__presets_section),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = stringRes(R.string.drs__shortcuts__presets_hint),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(8.dp))
                    DrsShortcutPresets.forEach { preset ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = preset.titleRes?.let { stringRes(it) }.orEmpty(), fontSize = 14.sp)
                                Text(
                                    text = preset.body
                                        .replace("\n", " ⏎ "),
                                    fontSize = 11.sp,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            TextButton(onClick = {
                                DrsShortcuts.add(preset.shortcut, preset.body, preset.isTechnical)
                            }) {
                                Text(text = stringRes(R.string.drs__shortcuts__add_action))
                            }
                        }
                    }
                }
            }

            // ---------------- Add button ----------------
            OutlinedButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = { editorState = DrsShortcutEditorState() },
            ) {
                Text(text = stringRes(R.string.drs__shortcuts__add_new))
            }

            // ---------------- Existing shortcuts ----------------
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.6f),
                ),
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            modifier = Modifier.weight(1f),
                            text = stringRes(R.string.drs__shortcuts__list_section),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                        )
                        if (drsState.shortcuts.isNotEmpty()) {
                            TextButton(onClick = { showRemoveAllDialog = true }) {
                                Text(text = stringRes(R.string.drs__shortcuts__remove_all))
                            }
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                    if (drsState.shortcuts.isEmpty()) {
                        Text(
                            text = stringRes(R.string.drs__shortcuts__empty),
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    } else {
                        drsState.shortcuts.forEach { sc ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = sc.shortcut,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary,
                                        )
                                        Spacer(Modifier.padding(horizontal = 4.dp))
                                        Text(text = "→", fontSize = 12.sp)
                                        Spacer(Modifier.padding(horizontal = 4.dp))
                                        Text(
                                            text = sc.expansion.replace("\n", " ⏎ "),
                                            fontSize = 13.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                        )
                                    }
                                    if (DrsShortcuts.isTemplate(sc.expansion)) {
                                        Text(
                                            text = stringRes(R.string.drs__shortcuts__template_badge),
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.tertiary,
                                        )
                                    }
                                }
                                TextButton(onClick = {
                                    editorState = DrsShortcutEditorState(sc.id, sc.shortcut, sc.expansion)
                                }) {
                                    Text(text = stringRes(R.string.drs__shortcuts__edit_action))
                                }
                                IconButton(onClick = { DrsShortcuts.remove(sc.id) }) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = stringRes(R.string.drs__shortcuts__delete_action),
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }

    // ---------------- Editor dialog ----------------
    editorState?.let { editing ->
        DrsShortcutEditorDialog(
            initial = editing,
            onDismiss = { editorState = null },
            onSave = { shortcut, expansion ->
                DrsShortcuts.add(shortcut, expansion, editing.id != null)
                editorState = null
            },
        )
    }

    if (showRemoveAllDialog) {
        AlertDialog(
            onDismissRequest = { showRemoveAllDialog = false },
            title = { Text(stringRes(R.string.drs__shortcuts__remove_all)) },
            text = { Text(stringRes(R.string.drs__shortcuts__remove_all_confirm)) },
            confirmButton = {
                TextButton(onClick = {
                    DrsShortcuts.removeAll()
                    showRemoveAllDialog = false
                }) {
                    Text(stringRes(R.string.drs__shortcuts__confirm_action))
                }
            },
            dismissButton = {
                TextButton(onClick = { showRemoveAllDialog = false }) {
                    Text(stringRes(R.string.drs__shortcuts__cancel_action))
                }
            },
        )
    }
}

// ---------------- Presets ----------------

private data class DrsPreset(
    val titleRes: Int?,
    val shortcut: String,
    val body: String,
    val isTechnical: Boolean = false,
)

private val DrsShortcutPresets = listOf(
    DrsPreset(R.string.drs__shortcuts__preset_date_title, "تاريخ", "اليوم {hijri} - {date}"),
    DrsPreset(R.string.drs__shortcuts__preset_time_title, "الوقت", "الساعة {time}"),
    DrsPreset(
        R.string.drs__shortcuts__preset_signature_title,
        "توقيع",
        "مع أطيب التحيات{newline}لوحة المفاتيح DRS",
    ),
    DrsPreset(R.string.drs__shortcuts__preset_paste_title, "لصق", "{clipboard}"),
    DrsPreset(
        R.string.drs__shortcuts__preset_email_title,
        "بريد",
        "السلام عليكم{newline}أرجو التفضل بالإطلاع على المرفق، ومع بيانات أي ملاحظات في أقرب وقت.{newline}شاكرين لكم حسن تعاونكم.",
    ),
)

// ---------------- Editor dialog ----------------

@Composable
private fun DrsShortcutEditorDialog(
    initial: DrsShortcutEditorState,
    onDismiss: () -> Unit,
    onSave: (shortcut: String, expansion: String) -> Unit,
) {
    var shortcut by remember { mutableStateOf(initial.shortcut) }
    var expansion by remember { mutableStateOf(initial.expansion) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                stringRes(
                    if (initial.id == null) R.string.drs__shortcuts__add_new
                    else R.string.drs__shortcuts__edit_title,
                )
            )
        },
        text = {
            Column {
                OutlinedTextField(
                    value = shortcut,
                    onValueChange = { shortcut = it },
                    label = { Text(stringRes(R.string.drs__shortcuts__shortcut_label)) },
                    supportingText = { Text(stringRes(R.string.drs__shortcuts__shortcut_hint)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = expansion,
                    onValueChange = { expansion = it },
                    label = { Text(stringRes(R.string.drs__shortcuts__expansion_label)) },
                    supportingText = { Text(stringRes(R.string.drs__shortcuts__expansion_hint)) },
                    minLines = 2,
                    maxLines = 5,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = stringRes(R.string.drs__shortcuts__template_section),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                )
                Spacer(Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    DrsShortcuts.TEMPLATE_VARIABLES.take(3).forEach { (variable, _) ->
                        AssistChip(
                            onClick = { expansion = expansion + variable },
                            label = { Text(variable, fontSize = 11.sp) },
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    DrsShortcuts.TEMPLATE_VARIABLES.drop(3).forEach { (variable, _) ->
                        AssistChip(
                            onClick = { expansion = expansion + variable },
                            label = { Text(variable, fontSize = 11.sp) },
                        )
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = stringRes(R.string.drs__shortcuts__template_hint),
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = shortcut.isNotBlank() && expansion.isNotBlank(),
                onClick = { onSave(shortcut, expansion) },
            ) {
                Text(stringRes(R.string.drs__shortcuts__save_action))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringRes(R.string.drs__shortcuts__cancel_action))
            }
        },
    )
}
