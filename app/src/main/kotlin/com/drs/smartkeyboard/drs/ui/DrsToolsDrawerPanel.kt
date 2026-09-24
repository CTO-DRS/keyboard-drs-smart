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
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.drs.smartkeyboard.R
import com.drs.smartkeyboard.drs.DrsSystems
import com.drs.smartkeyboard.drs.DrsStore
import com.drs.smartkeyboard.drs.DrsToolGroup
import com.drs.smartkeyboard.drs.DrsUnified
import com.drs.smartkeyboard.drs.DrsUnifiedTools
import com.drs.smartkeyboard.ime.keyboard.DrsImeSizing
import com.drs.smartkeyboard.ime.theme.DrsImeUi
import com.drs.smartkeyboard.keyboardManager
import org.drs.lib.compose.rippleClickable
import org.drs.lib.compose.stringRes
import org.drs.lib.snygg.ui.SnyggBox
import org.drs.lib.snygg.ui.SnyggColumn
import org.drs.lib.snygg.ui.SnyggIcon
import org.drs.lib.snygg.ui.SnyggIconButton
import org.drs.lib.snygg.ui.SnyggRow
import org.drs.lib.snygg.ui.SnyggText

/**
 * DRS v1.8.0 — درج المهام المثبتة (the pinned-tools drawer).
 *
 * Opened by the side-pull handle (زر السحب الجانبي) that now leads the
 * tasks bar above the suggestions strip. It replaces the keyboard area
 * for a moment (the same swap the quick-actions overflow uses) and gives
 * EVERY user system — العادي والتقني وكلاهما — direct, in-keyboard
 * control over the bar:
 *  - «المثبتة الآن»: the pinned tools in their strip order, with real
 *    reorder (up/down) and unpin controls.
 *  - «جميع المهام»: the whole catalogue filtered by group, where pinning
 *    a tool also guarantees it is visible on the CURRENT level (a
 *    technical-only tool pinned from the simple level widens its view
 *    override via [DrsUnifiedTools.ensureVisibleOverride]), plus
 *    show/hide per tool.
 *  - the tasks-bar master switch and the reset-to-defaults action.
 *
 * Every control mutates DrsState through the same [DrsUnified] helpers
 * the settings manager uses — nothing is simulated, everything survives
 * restarts, and the strip itself re-resolves on the very next frame.
 */

/** Filter of the all-tools list (mirrors the catalogue groups + all). */
private enum class DrawerFilter(val labelRes: Int) {
    ALL(R.string.drs__tools_drawer__filter_all),
    TOOLS(R.string.drs__unified__group_tools),
    EDITING(R.string.drs__unified__group_editing),
    CURSOR(R.string.drs__unified__group_cursor),
}

@Composable
fun DrsToolsDrawerPanel(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val keyboardManager by context.keyboardManager()

    val drsState by DrsStore.state.collectAsState()
    var filter by remember { mutableStateOf(DrawerFilter.ALL) }

    val view = DrsUnifiedTools.viewForSystem(drsState.userPath, drsState.hybridViewMode)
    val pinnedIds = drsState.pinnedUnifiedTools.toHashSet()
    val defaultPinnedIds = DrsUnifiedTools.ALL.filter { it.defaultPinned }.map { it.id }.toSet()
    val hiddenIds = drsState.hiddenUnifiedTools.toHashSet()

    // The pinned section mirrors the strip head: explicit pins first, then
    // default pins, in resolve order for the CURRENT level.
    val pinnedTools = remember(
        view, drsState.unifiedToolOrder, drsState.hiddenUnifiedTools,
        drsState.pinnedUnifiedTools, drsState.unifiedToolViews,
    ) {
        DrsUnifiedTools.resolveFor(
            view = view,
            hidden = drsState.hiddenUnifiedTools,
            pinned = drsState.pinnedUnifiedTools,
            order = drsState.unifiedToolOrder,
            viewOverrides = drsState.unifiedToolViews,
        ).filter { it.id in pinnedIds || it.id in defaultPinnedIds }
    }

    val catalogTools = DrsUnifiedTools.ALL.filter { tool ->
        when (filter) {
            DrawerFilter.ALL -> true
            DrawerFilter.TOOLS -> tool.group == DrsToolGroup.TOOLS
            DrawerFilter.EDITING -> tool.group == DrsToolGroup.EDITING
            DrawerFilter.CURSOR -> tool.group == DrsToolGroup.CURSOR
        }
    }

    val systemSpec = DrsSystems.specOfName(drsState.userPath)
    val accent = if (isSystemInDarkTheme()) systemSpec.accentNight else systemSpec.accent

    fun close() {
        keyboardManager.activeState.isToolsDrawerVisible = false
    }

    SnyggColumn(
        modifier = modifier
            .fillMaxWidth()
            .height(DrsImeSizing.imeUiHeight()),
    ) {
        // ---------------- header ----------------
        SnyggRow(
            DrsImeUi.ClipboardHeader.elementName,
            modifier = Modifier
                .fillMaxWidth()
                .height(DrsImeSizing.smartbarHeight),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val sizeModifier = Modifier
                .sizeIn(maxHeight = DrsImeSizing.smartbarHeight)
                .aspectRatio(1f)
            SnyggIconButton(
                elementName = DrsImeUi.ClipboardHeaderButton.elementName,
                onClick = { close() },
                modifier = sizeModifier,
            ) {
                SnyggIcon(imageVector = Icons.AutoMirrored.Filled.ArrowBack)
            }
            SnyggText(
                elementName = DrsImeUi.ClipboardHeaderText.elementName,
                modifier = Modifier.weight(1f),
                text = stringRes(R.string.drs__tools_drawer__title),
            )
            SnyggText(
                elementName = DrsImeUi.ClipboardHeaderText.elementName,
                modifier = Modifier.padding(end = 12.dp),
                text = stringRes(
                    R.string.drs__tools_drawer__count,
                    "count" to pinnedTools.size.toString(),
                    "total" to DrsUnifiedTools.ALL.size.toString(),
                ),
            )
        }

        // ---------------- group filters ----------------
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(6.dp),
        ) {
            DrawerFilter.entries.forEach { option ->
                val selected = filter == option
                SnyggText(
                    elementName = DrsImeUi.ClipboardSubheader.elementName,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(
                            if (selected) accent.copy(alpha = 0.22f) else accent.copy(alpha = 0.06f),
                        )
                        .rippleClickable { filter = option }
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    text = stringRes(option.labelRes),
                )
            }
        }

        // ---------------- body ----------------
        SnyggBox(
            DrsImeUi.ClipboardContent.elementName,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
            ) {
                // ----- pinned now (strip head order) -----
                SnyggText(
                    elementName = DrsImeUi.ClipboardSubheader.elementName,
                    modifier = Modifier.padding(start = 16.dp, top = 6.dp, bottom = 2.dp),
                    text = stringRes(R.string.drs__tools_drawer__pinned_section),
                )
                if (pinnedTools.isEmpty()) {
                    SnyggText(
                        elementName = DrsImeUi.ClipboardItemDescription.elementName,
                        modifier = Modifier.padding(start = 20.dp, bottom = 4.dp),
                        text = stringRes(R.string.drs__tools_drawer__pinned_empty),
                    )
                }
                pinnedTools.forEach { tool ->
                    DrawerRow(
                        id = tool.id,
                        hidden = tool.id in hiddenIds,
                        pinned = true,
                        trailing = {
                            SnyggIconButton(
                                elementName = DrsImeUi.ClipboardHeaderButton.elementName,
                                onClick = { DrsUnified.moveTool(tool.id, up = true) },
                                modifier = Modifier.sizeIn(minWidth = 38.dp).height(36.dp),
                            ) {
                                SnyggIcon(imageVector = Icons.Default.KeyboardArrowUp)
                            }
                            SnyggIconButton(
                                elementName = DrsImeUi.ClipboardHeaderButton.elementName,
                                onClick = { DrsUnified.moveTool(tool.id, up = false) },
                                modifier = Modifier.sizeIn(minWidth = 38.dp).height(36.dp),
                            ) {
                                SnyggIcon(imageVector = Icons.Default.KeyboardArrowDown)
                            }
                            SnyggIconButton(
                                elementName = DrsImeUi.ClipboardHeaderButton.elementName,
                                onClick = { DrsUnified.setToolPinned(tool.id, false) },
                                modifier = Modifier.sizeIn(minWidth = 38.dp).height(36.dp),
                            ) {
                                SnyggIcon(
                                    imageVector = Icons.Filled.PushPin,
                                )
                            }
                        },
                    )
                }

                // ----- the whole catalogue -----
                SnyggText(
                    elementName = DrsImeUi.ClipboardSubheader.elementName,
                    modifier = Modifier.padding(start = 16.dp, top = 10.dp, bottom = 2.dp),
                    text = stringRes(R.string.drs__tools_drawer__all_section),
                )
                catalogTools.forEach { tool ->
                    val isPinned = tool.id in pinnedIds || tool.id in defaultPinnedIds
                    DrawerRow(
                        id = tool.id,
                        hidden = tool.id in hiddenIds,
                        pinned = isPinned,
                        trailing = {
                            SnyggIconButton(
                                elementName = DrsImeUi.ClipboardHeaderButton.elementName,
                                onClick = {
                                    if (isPinned) {
                                        DrsUnified.setToolPinned(tool.id, false)
                                    } else {
                                        // pin + guarantee visibility on the
                                        // CURRENT level (the v1.8.0 contract).
                                        DrsUnified.setToolPinnedEnsureVisible(tool.id, view)
                                    }
                                },
                                modifier = Modifier.sizeIn(minWidth = 38.dp).height(36.dp),
                            ) {
                                SnyggIcon(
                                    imageVector = if (isPinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                                )
                            }
                            SnyggIconButton(
                                elementName = DrsImeUi.ClipboardHeaderButton.elementName,
                                onClick = {
                                    DrsUnified.setToolHidden(tool.id, tool.id !in hiddenIds)
                                },
                                modifier = Modifier.sizeIn(minWidth = 38.dp).height(36.dp),
                            ) {
                                SnyggIcon(
                                    imageVector = if (tool.id in hiddenIds) {
                                        Icons.Default.VisibilityOff
                                    } else {
                                        Icons.Default.Visibility
                                    },
                                )
                            }
                        },
                    )
                }

                // ----- bar master switch + reset -----
                SnyggBox(
                    elementName = DrsImeUi.ClipboardItem.elementName,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                        .rippleClickable { DrsUnified.setStripEnabled(!drsState.unifiedStripEnabled) },
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        SnyggText(
                            modifier = Modifier.weight(1f),
                            text = stringRes(R.string.drs__tools_drawer__bar_switch),
                        )
                        SwitchInline(checked = drsState.unifiedStripEnabled, accent = accent)
                    }
                }
                SnyggBox(
                    elementName = DrsImeUi.ClipboardItem.elementName,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                        .rippleClickable { DrsUnified.resetCustomization() },
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        SnyggIcon(imageVector = Icons.Default.RestartAlt)
                        Spacer(Modifier.width(10.dp))
                        SnyggText(text = stringRes(R.string.drs__tools_drawer__reset))
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

/** One catalogue row: identity on the left, real controls on the right. */
@Composable
private fun DrawerRow(
    id: String,
    hidden: Boolean,
    pinned: Boolean,
    trailing: @Composable androidx.compose.foundation.layout.RowScope.() -> Unit,
) {
    SnyggBox(
        elementName = DrsImeUi.ClipboardItem.elementName,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 2.dp),
    ) {
        SnyggRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                SnyggText(text = toolTitle(id))
                val desc = toolDesc(id)
                if (desc.isNotBlank()) {
                    SnyggText(
                        elementName = DrsImeUi.ClipboardItemDescription.elementName,
                        text = desc,
                    )
                }
            }
            trailing()
        }
    }
}

/** Tiny theme-safe state pill (avoids Material switches inside the IME). */
@Composable
private fun SwitchInline(checked: Boolean, accent: androidx.compose.ui.graphics.Color) {
    SnyggBox(
        modifier = Modifier
            .clip(CircleShape)
            .background(accent.copy(alpha = if (checked) 0.3f else 0.12f))
            .padding(horizontal = 14.dp, vertical = 4.dp),
    ) {
        SnyggText(
            text = stringRes(
                if (checked) R.string.drs__tools_drawer__on else R.string.drs__tools_drawer__off,
            ),
        )
    }
}
