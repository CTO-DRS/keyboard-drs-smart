/*
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

package com.drs.smartkeyboard.app.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Healing
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.SentimentSatisfiedAlt
import androidx.compose.material.icons.filled.SmartButton
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Spellcheck
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Swipe
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.outlined.Extension
import androidx.compose.material.icons.outlined.Keyboard
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.drs.smartkeyboard.R
import com.drs.smartkeyboard.app.DrsPreferenceStore
import com.drs.smartkeyboard.app.LocalNavController
import com.drs.smartkeyboard.app.Routes
import com.drs.smartkeyboard.drs.DrsPerformance
import com.drs.smartkeyboard.drs.DrsProfileManager
import com.drs.smartkeyboard.drs.DrsStore
import com.drs.smartkeyboard.drs.DrsSystems
import com.drs.smartkeyboard.drs.DrsUnifiedTools
import com.drs.smartkeyboard.drs.ui.DrsIdentityCard
import com.drs.smartkeyboard.drs.ui.chipShape
import com.drs.smartkeyboard.drs.ui.DrsIdentityTile
import com.drs.smartkeyboard.drs.ui.DrsIdentityTileDivider
import com.drs.smartkeyboard.drs.ui.DrsIdentitySectionHeader
import com.drs.smartkeyboard.drs.ui.DrsIdentityStaggerIn
import com.drs.smartkeyboard.drs.ui.DrsMetricTile
import com.drs.smartkeyboard.drs.ui.DrsStatusChip
import com.drs.smartkeyboard.drs.ui.pathTitleRes
import com.drs.smartkeyboard.lib.util.InputMethodUtils
import com.drs.smartkeyboard.clipboardManager
import com.drs.smartkeyboard.subtypeManager
import com.drs.smartkeyboard.themeManager
import org.drs.jetpref.datastore.model.collectAsState
import org.drs.lib.compose.stringRes

/**
 * DRS v1.0.8 (البند 9) — the three per-system dashboards.
 *
 * No single dashboard is shared by all systems anymore. Each system owns a
 * dashboard that matches its identity and its level of information:
 *
 *  - [DrsNormalDashboard]   — the simple daily panel: status, current
 *    language, theme, quick tools, recent clipboard and basic settings.
 *  - [DrsTechnicalDashboard] — the Advanced Keyboard Control Center: a
 *    live metric grid over the engine, layout, themes, gestures,
 *    performance, diagnostics, tools and system state, with breadcrumbs.
 *  - [DrsHybridDashboard]   — the unified panel: daily tools and advanced
 *    tools side by side, quick controls, and fast access to both worlds.
 *
 * Every number is measured from live state; every tile opens a real screen.
 */

// ---------------------------------------------------------------------------
// Shared small pieces
// ---------------------------------------------------------------------------

@Composable
private fun identityAccent(): Color {
    val spec = DrsSystems.specOfName(DrsStore.state.value.userPath)
    return if (isSystemInDarkTheme()) spec.accentNight else spec.accent
}


@Composable
private fun DrsKeyValueRow(label: String, value: String, accent: Color? = null) {
    val colorScheme = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = accent ?: colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

// ---------------------------------------------------------------------------
// 1) النظام العادي — simple daily dashboard
// ---------------------------------------------------------------------------

@Composable
fun DrsNormalDashboard(navController: NavController) {
    val context = LocalContext.current
    val prefs by DrsPreferenceStore
    val drsState by DrsStore.state.collectAsState()

    val subtypeManager by context.subtypeManager()
    val clipboardManager by context.clipboardManager()
    val activeSubtype by subtypeManager.activeSubtypeFlow.collectAsState()
    val clipboardHistory by clipboardManager.historyFlow.collectAsState()

    val dayThemeId by prefs.theme.dayThemeId.collectAsState()
    val nightThemeId by prefs.theme.nightThemeId.collectAsState()
    val themeManager by context.themeManager()
    val themeLabel = remember(dayThemeId, nightThemeId) {
        val day = themeManager.indexedThemeConfigs.value.first[dayThemeId]?.label
        val night = themeManager.indexedThemeConfigs.value.first[nightThemeId]?.label
        day ?: night ?: dayThemeId.toString()
    }

    val spec = DrsSystems.specOfName(drsState.userPath)
    val accent = identityAccent()
    val languageName = activeSubtype.primaryLocale.base.displayName.ifBlank { "?" }

    // Dashboard: حالة لوحة المفاتيح + اللغة الحالية + الثيم
    DrsIdentityCard {
        DrsKeyValueRow(stringRes(R.string.drs__dash__language), languageName, accent)
        DrsIdentityTileDivider()
        DrsKeyValueRow(stringRes(R.string.drs__dash__theme), themeLabel)
        DrsIdentityTileDivider()
        DrsKeyValueRow(
            stringRes(R.string.drs__dash__clipboard),
            stringRes(R.string.drs__dash__clipboard_count, "count" to clipboardHistory.all.size),
        )
    }

    // الأدوات السريعة — every tile opens a real screen.
    DrsIdentitySectionHeader(stringRes(R.string.drs__dash__quick_tools))
    DrsIdentityCard {
        DrsIdentityTile(
            icon = Icons.Default.SmartButton,
            accent = accent,
            title = stringRes(R.string.settings__smartbar__title),
            summary = stringRes(R.string.settings__smartbar__summary),
            onClick = { navController.navigate(Routes.Settings.Smartbar) },
        )
        DrsIdentityTileDivider()
        DrsIdentityTile(
            icon = Icons.Default.SentimentSatisfiedAlt,
            accent = Color(0xFFD97706),
            title = stringRes(R.string.settings__media__title),
            summary = stringRes(R.string.settings__media__summary),
            onClick = { navController.navigate(Routes.Settings.Media) },
        )
        DrsIdentityTileDivider()
        DrsIdentityTile(
            icon = Icons.Default.ContentPaste,
            accent = Color(0xFF0E9488),
            title = stringRes(R.string.settings__clipboard__title),
            summary = stringRes(R.string.settings__clipboard__summary),
            onClick = { navController.navigate(Routes.Settings.Clipboard) },
        )
        DrsIdentityTileDivider()
        DrsIdentityTile(
            icon = Icons.AutoMirrored.Outlined.Assignment,
            accent = Color(0xFF4F5BD5),
            title = stringRes(R.string.drs__shortcuts__title),
            summary = stringRes(R.string.drs__dash__shortcuts_count, "count" to drsState.shortcuts.size),
            onClick = { navController.navigate(Routes.Settings.DrsShortcuts) },
        )
    }

    // الحافظة الحديثة — real recent items, empty state when none.
    DrsIdentitySectionHeader(stringRes(R.string.drs__dash__recent_clipboard))
    if (clipboardHistory.all.isEmpty()) {
        com.drs.smartkeyboard.drs.ui.DrsAppStateView(
            kind = com.drs.smartkeyboard.drs.DrsAppStateKind.EMPTY,
            title = stringRes(R.string.drs__dash__clipboard_empty_title),
            description = stringRes(R.string.drs__dash__clipboard_empty_desc),
        )
    } else {
        DrsIdentityCard {
            clipboardHistory.all.take(3).forEachIndexed { index, item ->
                if (index > 0) DrsIdentityTileDivider()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = if (item.isPinned) Icons.Default.TouchApp else Icons.Default.ContentPaste,
                        contentDescription = null,
                        tint = accent,
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = item.text ?: stringRes(R.string.drs__dash__clipboard_media_item),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }

    // التخصيص والإعدادات الأساسية.
    DrsIdentitySectionHeader(stringRes(R.string.drs__dash__customize))
    DrsIdentityCard {
        DrsIdentityTile(
            icon = Icons.Outlined.Palette,
            accent = accent,
            title = stringRes(R.string.settings__theme__title),
            summary = stringRes(R.string.drs__dash__theme_summary),
            onClick = { navController.navigate(Routes.Settings.Theme) },
        )
        DrsIdentityTileDivider()
        DrsIdentityTile(
            icon = Icons.Default.People,
            accent = Color(0xFF7C3AED),
            title = stringRes(R.string.drs__profiles__title),
            summary = stringRes(R.string.drs__dash__profiles_summary),
            onClick = { navController.navigate(Routes.Settings.DrsProfiles) },
        )
        DrsIdentityTileDivider()
        DrsIdentityTile(
            icon = Icons.Default.Language,
            accent = Color(0xFF0891B2),
            title = stringRes(R.string.settings__localization__title),
            summary = stringRes(R.string.drs__dash__language_summary),
            onClick = { navController.navigate(Routes.Settings.Localization) },
        )
    }
}

// ---------------------------------------------------------------------------
// 2) النظام التقني — Advanced Keyboard Control Center
// ---------------------------------------------------------------------------

@Composable
fun DrsTechnicalDashboard(navController: NavController) {
    val context = LocalContext.current
    val prefs by DrsPreferenceStore
    val drsState by DrsStore.state.collectAsState()

    val isEnabled by InputMethodUtils.observeIsDrsKeyboardEnabled(foregroundOnly = true)
    val isSelected by InputMethodUtils.observeIsDrsKeyboardSelected(foregroundOnly = true)
    val subtypeManager by context.subtypeManager()
    val subtypes by subtypeManager.subtypesFlow.collectAsState()
    val activeSubtype by subtypeManager.activeSubtypeFlow.collectAsState()

    val dayThemeId by prefs.theme.dayThemeId.collectAsState()
    val nightThemeId by prefs.theme.nightThemeId.collectAsState()
    val themeManager by context.themeManager()
    val dayLabel = remember(dayThemeId) {
        themeManager.indexedThemeConfigs.value.first[dayThemeId]?.label ?: dayThemeId.toString()
    }
    val nightLabel = remember(nightThemeId) {
        themeManager.indexedThemeConfigs.value.first[nightThemeId]?.label ?: nightThemeId.toString()
    }

    val gestureSlots = listOf(
        prefs.gestures.swipeUp, prefs.gestures.swipeDown,
        prefs.gestures.swipeLeft, prefs.gestures.swipeRight,
        prefs.gestures.spaceBarSwipeUp, prefs.gestures.spaceBarSwipeLeft,
        prefs.gestures.spaceBarSwipeRight, prefs.gestures.spaceBarLongPress,
        prefs.gestures.deleteKeySwipeLeft, prefs.gestures.deleteKeyLongPress,
    )
    val boundGestures = gestureSlots.count { it.get() != com.drs.smartkeyboard.ime.text.gestures.SwipeAction.NO_ACTION }

    val accent = identityAccent()
    val activeProfile = DrsProfileManager.activeProfile(drsState)
    val perfSamples = DrsPerformance.sampleCount()
    val engineState = when {
        isEnabled && isSelected -> stringRes(R.string.drs__dash__engine_active)
        isEnabled -> stringRes(R.string.drs__dash__engine_enabled_only)
        else -> stringRes(R.string.drs__dash__engine_off)
    }

    // Breadcrumb: الرئيسية ‹ مركز التحكم المتقدم (البند 11).
    com.drs.smartkeyboard.drs.ui.DrsBreadcrumbBar(
        crumbs = listOf(
            stringRes(R.string.drs__nav__home) to { navController.popBackStack(Routes.Settings.Home, false) },
            stringRes(R.string.drs__dash__control_center) to {},
        ),
    )

    // Keyboard Engine — live system state grid.
    DrsIdentitySectionHeader(
        stringRes(R.string.drs__dash__engine_section),
        trailing = { DrsStatusChip(engineState, accent) },
    )
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        DrsMetricTile(
            label = stringRes(R.string.drs__dash__engine),
            value = if (isEnabled && isSelected) stringRes(R.string.drs__dash__engine_ready) else stringRes(R.string.drs__dash__engine_off),
            icon = Icons.Outlined.Keyboard,
            accent = accent,
            onClick = { navController.navigate(Routes.Setup.Screen) },
            modifier = Modifier.weight(1f),
        )
        DrsMetricTile(
            label = stringRes(R.string.drs__dash__active_language),
            value = activeSubtype.primaryLocale.base.displayName.ifBlank { "?" },
            icon = Icons.Default.Language,
            accent = accent,
            onClick = { navController.navigate(Routes.Settings.Localization) },
            modifier = Modifier.weight(1f),
        )
    }
    Spacer(modifier = Modifier.height(8.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        DrsMetricTile(
            label = stringRes(R.string.drs__dash__subtypes),
            value = subtypes.size.toString(),
            icon = Icons.Default.Tune,
            accent = accent,
            onClick = { navController.navigate(Routes.Settings.Localization) },
            modifier = Modifier.weight(1f),
        )
        DrsMetricTile(
            label = stringRes(R.string.drs__dash__profile),
            value = activeProfile?.name ?: "-",
            icon = Icons.Default.People,
            accent = accent,
            onClick = { navController.navigate(Routes.Settings.DrsProfiles) },
            modifier = Modifier.weight(1f),
        )
    }

    // Theme engine.
    DrsIdentitySectionHeader(stringRes(R.string.drs__dash__theme_section))
    DrsIdentityCard(title = null) {
        DrsKeyValueRow(stringRes(R.string.drs__dash__day_theme), dayLabel, accent)
        DrsIdentityTileDivider()
        DrsKeyValueRow(stringRes(R.string.drs__dash__night_theme), nightLabel)
    }

    // Gestures + tools — real counts.
    DrsIdentitySectionHeader(stringRes(R.string.drs__dash__gestures_tools))
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        DrsMetricTile(
            label = stringRes(R.string.drs__dash__bound_gestures),
            value = boundGestures.toString(),
            icon = Icons.Default.Swipe,
            accent = accent,
            onClick = { navController.navigate(Routes.Settings.DrsGestures) },
            modifier = Modifier.weight(1f),
        )
        DrsMetricTile(
            label = stringRes(R.string.drs__dash__shortcuts),
            value = drsState.shortcuts.size.toString(),
            icon = Icons.AutoMirrored.Outlined.Assignment,
            accent = accent,
            onClick = { navController.navigate(Routes.Settings.DrsShortcuts) },
            modifier = Modifier.weight(1f),
        )
    }
    Spacer(modifier = Modifier.height(8.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        DrsMetricTile(
            label = stringRes(R.string.drs__dash__unified_tools),
            value = DrsUnifiedTools.ALL.size.toString(),
            icon = Icons.Outlined.Extension,
            accent = accent,
            onClick = { navController.navigate(Routes.Settings.DrsUnifiedTools) },
            modifier = Modifier.weight(1f),
        )
        DrsMetricTile(
            label = stringRes(R.string.drs__dash__text_tools),
            value = stringRes(R.string.drs__dash__text_tools_value),
            icon = Icons.Default.Edit,
            accent = accent,
            onClick = { navController.navigate(Routes.Settings.DrsTechToolbar) },
            modifier = Modifier.weight(1f),
        )
    }

    // Performance — measured numbers only.
    DrsIdentitySectionHeader(stringRes(R.string.drs__dash__performance_section))
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        DrsMetricTile(
            label = stringRes(R.string.drs__dash__avg_latency),
            value = if (perfSamples > 0) "${DrsPerformance.averageMicros()}µs" else "—",
            icon = Icons.Default.Speed,
            accent = accent,
            onClick = { navController.navigate(Routes.Settings.DrsPerformance) },
            modifier = Modifier.weight(1f),
        )
        DrsMetricTile(
            label = stringRes(R.string.drs__dash__p95_latency),
            value = if (perfSamples > 0) "${DrsPerformance.percentileMicros(95)}µs" else "—",
            icon = Icons.Default.History,
            accent = accent,
            onClick = { navController.navigate(Routes.Settings.DrsPerformance) },
            modifier = Modifier.weight(1f),
        )
    }

    // System state — storage + diagnostics, all real.
    DrsIdentitySectionHeader(stringRes(R.string.drs__dash__system_state))
    DrsIdentityCard {
        DrsKeyValueRow(
            stringRes(R.string.drs__dash__state_file),
            stringRes(R.string.drs__dash__bytes, "bytes" to DrsStore.fileSizeBytes()),
            accent,
        )
        DrsIdentityTileDivider()
        DrsKeyValueRow(
            stringRes(R.string.drs__dash__storage_health),
            if (DrsStore.storageHealthy()) stringRes(R.string.drs__dash__health_ok) else stringRes(R.string.drs__dash__health_error),
        )
        DrsIdentityTileDivider()
        DrsIdentityTile(
            icon = Icons.Default.Healing,
            accent = accent,
            title = stringRes(R.string.drs__diagnostics__title),
            summary = stringRes(R.string.drs__dash__run_diagnostics),
            onClick = { navController.navigate(Routes.Settings.DrsDiagnostics) },
            showSummary = true,
        )
        DrsIdentityTileDivider()
        DrsIdentityTile(
            icon = Icons.Default.Storage,
            accent = accent,
            title = stringRes(R.string.drs__storage__title),
            summary = stringRes(R.string.drs__storage__summary),
            onClick = { navController.navigate(Routes.Settings.DrsStorage) },
            showSummary = true,
        )
    }
}

// ---------------------------------------------------------------------------
// 3) نظام كلاهما — unified panel with quick controls
// ---------------------------------------------------------------------------

@Composable
fun DrsHybridDashboard(navController: NavController) {
    val context = LocalContext.current
    val prefs by DrsPreferenceStore
    val drsState by DrsStore.state.collectAsState()

    val isEnabled by InputMethodUtils.observeIsDrsKeyboardEnabled(foregroundOnly = true)
    val isSelected by InputMethodUtils.observeIsDrsKeyboardSelected(foregroundOnly = true)
    val subtypeManager by context.subtypeManager()
    val activeSubtype by subtypeManager.activeSubtypeFlow.collectAsState()

    val accent = identityAccent()
    val identity = com.drs.smartkeyboard.drs.ui.rememberDrsIdentity()
    val view = DrsUnifiedTools.viewForSystem(drsState.userPath, drsState.hybridViewMode)
    val activeTools = remember(
        view, drsState.hiddenUnifiedTools, drsState.pinnedUnifiedTools,
        drsState.unifiedToolOrder, drsState.unifiedToolViews,
    ) {
        DrsUnifiedTools.resolveFor(
            view, drsState.hiddenUnifiedTools, drsState.pinnedUnifiedTools,
            drsState.unifiedToolOrder, drsState.unifiedToolViews,
        )
    }

    // الحالة الحالية — live chips row.
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        DrsStatusChip(
            if (isEnabled && isSelected) {
                stringRes(R.string.drs__dash__engine_active)
            } else {
                stringRes(R.string.drs__dash__engine_off)
            },
            accent,
        )
        DrsStatusChip(activeSubtype.primaryLocale.base.displayName.ifBlank { "?" }, accent)
        DrsStatusChip(
            stringRes(
                when (view) {
                    com.drs.smartkeyboard.drs.DrsHybridViewMode.SIMPLE -> R.string.drs__unified__level_simple
                    com.drs.smartkeyboard.drs.DrsHybridViewMode.ADVANCED -> R.string.drs__unified__level_advanced
                    com.drs.smartkeyboard.drs.DrsHybridViewMode.DUAL -> R.string.drs__unified__level_dual
                },
            ),
            accent,
        )
    }

    // Quick Controls (البند 9): level switching + advanced gate — real.
    DrsIdentitySectionHeader(stringRes(R.string.drs__dash__quick_controls))
    DrsIdentityCard {
        Text(
            text = stringRes(R.string.drs__unified__level_switch_title),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            com.drs.smartkeyboard.drs.DrsHybridViewMode.entries.forEach { mode ->
                val selected = view == mode
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(identity.chipShape)
                        .background(if (selected) accent else accent.copy(alpha = 0.10f))
                        .border(
                            1.dp,
                            if (selected) accent else accent.copy(alpha = 0.30f),
                            identity.chipShape,
                        )
                        .clickable {
                            com.drs.smartkeyboard.drs.DrsUnified.setViewMode(mode)
                        }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = stringRes(
                            when (mode) {
                                com.drs.smartkeyboard.drs.DrsHybridViewMode.SIMPLE -> R.string.drs__unified__level_simple
                                com.drs.smartkeyboard.drs.DrsHybridViewMode.ADVANCED -> R.string.drs__unified__level_advanced
                                com.drs.smartkeyboard.drs.DrsHybridViewMode.DUAL -> R.string.drs__unified__level_dual
                            },
                        ),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (selected) Color.White else accent,
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringRes(R.string.drs__unified__advanced_controls),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = stringRes(R.string.drs__unified__advanced_controls_summary),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Switch(
                checked = drsState.advancedControlsEnabled,
                onCheckedChange = { checked ->
                    com.drs.smartkeyboard.drs.DrsUnified.setAdvancedControls(checked)
                },
            )
        }
    }

    // الأدوات اليومية — the simple-level tools with a live count.
    DrsIdentitySectionHeader(
        stringRes(R.string.drs__dash__daily_tools),
        trailing = { DrsStatusChip("${activeTools.size}/${DrsUnifiedTools.ALL.size}", accent) },
    )
    DrsIdentityCard {
        DrsIdentityTile(
            icon = Icons.Outlined.Extension,
            accent = accent,
            title = stringRes(R.string.drs__unified__tools_title),
            summary = stringRes(R.string.drs__unified__tools_home_summary),
            onClick = { navController.navigate(Routes.Settings.DrsUnifiedTools) },
        )
        DrsIdentityTileDivider()
        DrsIdentityTile(
            icon = Icons.Default.SmartButton,
            accent = Color(0xFF0E9488),
            title = stringRes(R.string.settings__smartbar__title),
            summary = stringRes(R.string.settings__smartbar__summary),
            onClick = { navController.navigate(Routes.Settings.Smartbar) },
        )
        DrsIdentityTileDivider()
        DrsIdentityTile(
            icon = Icons.Default.ContentPaste,
            accent = Color(0xFF4F5BD5),
            title = stringRes(R.string.settings__clipboard__title),
            summary = stringRes(R.string.settings__clipboard__summary),
            onClick = { navController.navigate(Routes.Settings.Clipboard) },
        )
    }

    // الأدوات المتقدمة — one tap into the deep layer.
    DrsIdentitySectionHeader(stringRes(R.string.drs__dash__advanced_tools))
    DrsIdentityCard {
        DrsIdentityTile(
            icon = Icons.Default.Edit,
            accent = Color(0xFFB45309),
            title = stringRes(R.string.drs__tech_toolbar__title),
            summary = stringRes(R.string.drs__dash__text_tools_value),
            onClick = { navController.navigate(Routes.Settings.DrsTechToolbar) },
        )
        DrsIdentityTileDivider()
        DrsIdentityTile(
            icon = Icons.Default.Swipe,
            accent = Color(0xFF7C3AED),
            title = stringRes(R.string.drs__gestures__title),
            summary = stringRes(R.string.drs__gestures__home_summary),
            onClick = { navController.navigate(Routes.Settings.DrsGestures) },
        )
        DrsIdentityTileDivider()
        DrsIdentityTile(
            icon = Icons.Default.Speed,
            accent = Color(0xFF0E7490),
            title = stringRes(R.string.drs__performance__title),
            summary = stringRes(R.string.drs__performance__summary),
            onClick = { navController.navigate(Routes.Settings.DrsPerformance) },
        )
    }

    // الوصول السريع إلى النظامين — system switcher + unified hub.
    DrsIdentitySectionHeader(stringRes(R.string.drs__dash__both_systems))
    DrsIdentityCard {
        DrsIdentityTile(
            icon = Icons.Default.Dashboard,
            accent = accent,
            title = stringRes(R.string.drs__control_center__title),
            summary = stringRes(R.string.drs__dash__switch_systems),
            onClick = { navController.navigate(Routes.Settings.DrsControlCenter) },
        )
        DrsIdentityTileDivider()
        DrsIdentityTile(
            icon = Icons.Default.Spellcheck,
            accent = accent,
            title = stringRes(R.string.drs__unified__dashboard_title),
            summary = stringRes(R.string.drs__unified__home_summary),
            onClick = { navController.navigate(Routes.Settings.DrsUnifiedDashboard) },
        )
    }
}