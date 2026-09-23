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

package com.drs.smartkeyboard.drs.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Healing
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Spellcheck
import androidx.compose.material.icons.filled.Swipe
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.Extension
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Keyboard
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.TextFields
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.drs.smartkeyboard.R
import com.drs.smartkeyboard.app.DrsPreferenceStore
import com.drs.smartkeyboard.app.LocalNavController
import com.drs.smartkeyboard.app.Routes
import com.drs.smartkeyboard.drs.DrsDesignSpec
import com.drs.smartkeyboard.drs.DrsStore
import com.drs.smartkeyboard.drs.DrsUserPath
import com.drs.smartkeyboard.lib.compose.DrsScreen
import org.drs.jetpref.datastore.model.collectAsState
import org.drs.lib.compose.stringRes

/**
 * DRS v1.0.8 (البند 10/16) — the per-system TOOLS hub and SETTINGS root.
 *
 * Both screens read the active system and render a genuinely different
 * layout and depth for it:
 *
 *  - Tools hub: the normal system gets a short, friendly tool list; the
 *    technical system gets the full power surface (tech toolbar, text
 *    tools, gestures, diagnostics, performance) with live counts; the
 *    unified system gets the unified tools manager plus the shared tools.
 *  - Settings root: the normal system exposes only the essentials and
 *    keeps every advanced page behind one clearly-labeled entry; the
 *    technical system exposes the complete section map (including
 *    privacy, storage and diagnostics) with breadcrumbs; the unified
 *    system links its own unified settings screens first.
 */

@Composable
fun DrsToolsHubScreen() = DrsScreen {
    title = stringRes(R.string.drs__nav__tools)
    navigationIconVisible = false
    previewFieldVisible = false

    val navController = LocalNavController.current
    val drsState by DrsStore.state.collectAsState()
    val path = drsState.userPath
    val accent = accentForActiveSystem()

    Column {
        if (DrsDesignSpec.identityOfName(path).showBreadcrumbs) {
            DrsBreadcrumbBar(
                crumbs = listOf(
                    stringRes(R.string.drs__nav__home) to { navController.popBackStack(Routes.Settings.Home, false) },
                    stringRes(R.string.drs__nav__tools) to {},
                ),
                modifier = Modifier.padding(horizontal = 12.dp),
            )
        }

        if (path == DrsUserPath.TECHNICAL.name) {
            // Technical: the full power tool surface with live counts.
            DrsIdentitySectionHeader(stringRes(R.string.drs__hub__tools_power))
            DrsIdentityCard {
                DrsIdentityTile(
                    icon = Icons.Default.Tune,
                    accent = accent,
                    title = stringRes(R.string.drs__tech_toolbar__title),
                    summary = stringRes(R.string.drs__tech_toolbar__summary),
                    showSummary = true,
                    onClick = { navController.navigate(Routes.Settings.DrsTechToolbar) },
                )
                DrsIdentityTileDivider()
                DrsIdentityTile(
                    icon = Icons.Default.Edit,
                    accent = Color(0xFFB45309),
                    title = stringRes(R.string.drs__text_tools__title),
                    summary = stringRes(R.string.drs__text_tools__summary),
                    showSummary = true,
                    onClick = { navController.navigate(Routes.Settings.DrsUnifiedTools) },
                )
                DrsIdentityTileDivider()
                DrsIdentityTile(
                    icon = Icons.Default.Swipe,
                    accent = Color(0xFF7C3AED),
                    title = stringRes(R.string.drs__gestures__title),
                    summary = stringRes(R.string.drs__gestures__home_summary),
                    showSummary = true,
                    onClick = { navController.navigate(Routes.Settings.DrsGestures) },
                )
                DrsIdentityTileDivider()
                DrsIdentityTile(
                    icon = Icons.AutoMirrored.Filled.Assignment,
                    accent = Color(0xFF0E7490),
                    title = stringRes(R.string.drs__shortcuts__title),
                    summary = stringRes(R.string.drs__dash__shortcuts_count, "count" to drsState.shortcuts.size),
                    showSummary = true,
                    onClick = { navController.navigate(Routes.Settings.DrsShortcuts) },
                )
                DrsIdentityTileDivider()
                DrsIdentityTile(
                    icon = Icons.Outlined.Extension,
                    accent = accent,
                    title = stringRes(R.string.drs__unified__tools_title),
                    summary = stringRes(R.string.drs__unified__tools_home_summary),
                    showSummary = true,
                    onClick = { navController.navigate(Routes.Settings.DrsUnifiedTools) },
                )
            }

            DrsIdentitySectionHeader(stringRes(R.string.drs__hub__tools_system))
            DrsIdentityCard {
                DrsIdentityTile(
                    icon = Icons.Default.Healing,
                    accent = Color(0xFFB91C1C),
                    title = stringRes(R.string.drs__diagnostics__title),
                    summary = stringRes(R.string.drs__diagnostics__home_summary),
                    showSummary = true,
                    onClick = { navController.navigate(Routes.Settings.DrsDiagnostics) },
                )
                DrsIdentityTileDivider()
                DrsIdentityTile(
                    icon = Icons.Default.Speed,
                    accent = Color(0xFF0E9488),
                    title = stringRes(R.string.drs__performance__title),
                    summary = stringRes(R.string.drs__performance__summary),
                    showSummary = true,
                    onClick = { navController.navigate(Routes.Settings.DrsPerformance) },
                )
            }
        } else {
            // Normal & unified: a clear, friendly tool list — every entry real.
            DrsIdentitySectionHeader(stringRes(R.string.drs__hub__tools_main))
            DrsIdentityCard {
                DrsIdentityTile(
                    icon = Icons.Default.TouchApp,
                    accent = accent,
                    title = stringRes(R.string.drs__unified__tools_title),
                    summary = stringRes(R.string.drs__unified__tools_home_summary),
                    showSummary = true,
                    onClick = { navController.navigate(Routes.Settings.DrsUnifiedTools) },
                )
                DrsIdentityTileDivider()
                DrsIdentityTile(
                    icon = Icons.Default.ContentPaste,
                    accent = Color(0xFF0E9488),
                    title = stringRes(R.string.settings__clipboard__title),
                    summary = stringRes(R.string.settings__clipboard__summary),
                    showSummary = true,
                    onClick = { navController.navigate(Routes.Settings.Clipboard) },
                )
                DrsIdentityTileDivider()
                DrsIdentityTile(
                    icon = Icons.AutoMirrored.Filled.Assignment,
                    accent = Color(0xFF4F5BD5),
                    title = stringRes(R.string.drs__shortcuts__title),
                    summary = stringRes(R.string.drs__dash__shortcuts_count, "count" to drsState.shortcuts.size),
                    showSummary = true,
                    onClick = { navController.navigate(Routes.Settings.DrsShortcuts) },
                )
                DrsIdentityTileDivider()
                DrsIdentityTile(
                    icon = Icons.Default.Spellcheck,
                    accent = Color(0xFF7C3AED),
                    title = stringRes(R.string.settings__typing__title),
                    summary = stringRes(R.string.settings__typing__summary),
                    showSummary = true,
                    onClick = { navController.navigate(Routes.Settings.Typing) },
                )
                DrsIdentityTileDivider()
                DrsIdentityTile(
                    icon = Icons.Outlined.TextFields,
                    accent = Color(0xFFB45309),
                    title = stringRes(R.string.drs__text_tools__title),
                    summary = stringRes(R.string.drs__text_tools__summary),
                    showSummary = true,
                    onClick = { navController.navigate(Routes.Settings.DrsUnifiedWriting) },
                )
            }

            DrsIdentitySectionHeader(stringRes(R.string.drs__hub__tools_more))
            DrsIdentityCard {
                DrsIdentityTile(
                    icon = Icons.Default.Swipe,
                    accent = Color(0xFF0E7490),
                    title = stringRes(R.string.drs__gestures__title),
                    summary = stringRes(R.string.drs__gestures__home_summary),
                    showSummary = true,
                    onClick = { navController.navigate(Routes.Settings.DrsGestures) },
                )
                DrsIdentityTileDivider()
                DrsIdentityTile(
                    icon = Icons.Default.Healing,
                    accent = Color(0xFFB91C1C),
                    title = stringRes(R.string.drs__diagnostics__title),
                    summary = stringRes(R.string.drs__diagnostics__home_summary),
                    showSummary = true,
                    onClick = { navController.navigate(Routes.Settings.DrsDiagnostics) },
                )
                if (path == DrsUserPath.NORMAL.name) {
                    DrsIdentityTileDivider()
                    DrsIdentityTile(
                        icon = Icons.Default.Speed,
                        accent = accent,
                        title = stringRes(R.string.drs__tech_toolbar__title),
                        summary = stringRes(R.string.drs__tech_toolbar__summary),
                        showSummary = true,
                        onClick = { navController.navigate(Routes.Settings.DrsTechToolbar) },
                    )
                }
            }
        }
    }
}

@Composable
fun DrsSettingsRootScreen() = DrsScreen {
    title = stringRes(R.string.drs__nav__settings)
    navigationIconVisible = false
    previewFieldVisible = false

    val navController = LocalNavController.current
    val context = LocalContext.current
    val prefs by DrsPreferenceStore
    val drsState by DrsStore.state.collectAsState()
    val path = drsState.userPath
    val identity = DrsDesignSpec.identityOfName(path)
    val accent = accentForActiveSystem()

    Column {
        if (identity.showBreadcrumbs) {
            DrsBreadcrumbBar(
                crumbs = listOf(
                    stringRes(R.string.drs__nav__home) to { navController.popBackStack(Routes.Settings.Home, false) },
                    stringRes(R.string.drs__nav__settings) to {},
                ),
                modifier = Modifier.padding(horizontal = 12.dp),
            )
        }

        // ----- Appearance & languages (all systems) -----
        DrsIdentitySectionHeader(stringRes(R.string.drs__root__section_appearance))
        DrsIdentityCard {
            DrsIdentityTile(
                icon = Icons.Outlined.Palette,
                accent = accent,
                title = stringRes(R.string.settings__theme__title),
                summary = stringRes(R.string.settings__theme__summary),
                showSummary = identity.showSummaries,
                onClick = { navController.navigate(Routes.Settings.Theme) },
            )
            DrsIdentityTileDivider()
            DrsIdentityTile(
                icon = Icons.Default.Tune,
                accent = Color(0xFF7C3AED),
                title = stringRes(R.string.settings__keyboard__title),
                summary = stringRes(R.string.settings__keyboard__summary),
                showSummary = identity.showSummaries,
                onClick = { navController.navigate(Routes.Settings.Keyboard) },
            )
            DrsIdentityTileDivider()
            DrsIdentityTile(
                icon = Icons.Default.Spellcheck,
                accent = Color(0xFF0E7490),
                title = stringRes(R.string.settings__typing__title),
                summary = stringRes(R.string.settings__typing__summary),
                showSummary = identity.showSummaries,
                onClick = { navController.navigate(Routes.Settings.Typing) },
            )
            DrsIdentityTileDivider()
            DrsIdentityTile(
                icon = Icons.Outlined.Info,
                accent = Color(0xFF0891B2),
                title = stringRes(R.string.settings__localization__title),
                summary = stringRes(R.string.settings__localization__summary),
                showSummary = identity.showSummaries,
                onClick = { navController.navigate(Routes.Settings.Localization) },
            )
        }

        // ----- Tools & clipboard -----
        DrsIdentitySectionHeader(stringRes(R.string.drs__root__section_tools))
        DrsIdentityCard {
            DrsIdentityTile(
                icon = Icons.Outlined.Extension,
                accent = accent,
                title = stringRes(R.string.settings__smartbar__title),
                summary = stringRes(R.string.settings__smartbar__summary),
                showSummary = identity.showSummaries,
                onClick = { navController.navigate(Routes.Settings.Smartbar) },
            )
            DrsIdentityTileDivider()
            DrsIdentityTile(
                icon = Icons.Default.ContentPaste,
                accent = Color(0xFF0E9488),
                title = stringRes(R.string.settings__clipboard__title),
                summary = stringRes(R.string.settings__clipboard__summary),
                showSummary = identity.showSummaries,
                onClick = { navController.navigate(Routes.Settings.Clipboard) },
            )
            DrsIdentityTileDivider()
            DrsIdentityTile(
                icon = Icons.AutoMirrored.Filled.Assignment,
                accent = Color(0xFF4F5BD5),
                title = stringRes(R.string.drs__shortcuts__title),
                summary = stringRes(R.string.drs__shortcuts__home_summary),
                showSummary = identity.showSummaries,
                onClick = { navController.navigate(Routes.Settings.DrsShortcuts) },
            )
            DrsIdentityTileDivider()
            DrsIdentityTile(
                icon = Icons.Default.Swipe,
                accent = Color(0xFFB45309),
                title = stringRes(R.string.settings__gestures__title),
                summary = stringRes(R.string.settings__gestures__summary),
                showSummary = identity.showSummaries,
                onClick = { navController.navigate(Routes.Settings.Gestures) },
            )
        }

        if (path == DrsUserPath.TECHNICAL.name) {
            // Technical: the complete system map, dense and honest.
            DrsIdentitySectionHeader(stringRes(R.string.drs__root__section_system))
            DrsIdentityCard {
                DrsIdentityTile(
                    icon = Icons.Outlined.Build,
                    accent = accent,
                    title = stringRes(R.string.settings__other__title),
                    summary = stringRes(R.string.settings__other__summary),
                    showSummary = true,
                    onClick = { navController.navigate(Routes.Settings.Other) },
                )
                DrsIdentityTileDivider()
                DrsIdentityTile(
                    icon = Icons.Default.Healing,
                    accent = Color(0xFFB91C1C),
                    title = stringRes(R.string.drs__diagnostics__title),
                    summary = stringRes(R.string.drs__diagnostics__home_summary),
                    showSummary = true,
                    onClick = { navController.navigate(Routes.Settings.DrsDiagnostics) },
                )
                DrsIdentityTileDivider()
                DrsIdentityTile(
                    icon = Icons.Default.Speed,
                    accent = Color(0xFF0E9488),
                    title = stringRes(R.string.drs__performance__title),
                    summary = stringRes(R.string.drs__performance__summary),
                    showSummary = true,
                    onClick = { navController.navigate(Routes.Settings.DrsPerformance) },
                )
                DrsIdentityTileDivider()
                DrsIdentityTile(
                    icon = Icons.Default.Tune,
                    accent = Color(0xFF7C3AED),
                    title = stringRes(R.string.drs__storage__title),
                    summary = stringRes(R.string.drs__storage__summary),
                    showSummary = true,
                    onClick = { navController.navigate(Routes.Settings.DrsStorage) },
                )
            }

            DrsIdentitySectionHeader(stringRes(R.string.drs__root__section_privacy))
            DrsIdentityCard {
                DrsIdentityTile(
                    icon = Icons.Default.Tune,
                    accent = accent,
                    title = stringRes(R.string.drs__privacy__title),
                    summary = stringRes(R.string.drs__privacy__summary),
                    showSummary = true,
                    onClick = { navController.navigate(Routes.Settings.DrsPrivacy) },
                )
            }
        } else {
            // Normal & unified: only the essentials, plus ONE clearly-labeled
            // advanced entry so nothing technical is forced on them.
            DrsIdentitySectionHeader(stringRes(R.string.drs__root__section_system))
            DrsIdentityCard {
                DrsIdentityTile(
                    icon = Icons.Outlined.Build,
                    accent = accent,
                    title = stringRes(R.string.settings__other__title),
                    summary = stringRes(R.string.settings__other__summary),
                    showSummary = true,
                    onClick = { navController.navigate(Routes.Settings.Other) },
                )
                DrsIdentityTileDivider()
                DrsIdentityTile(
                    icon = Icons.Default.Tune,
                    accent = Color(0xFFB91C1C),
                    title = stringRes(R.string.drs__privacy__title),
                    summary = stringRes(R.string.drs__privacy__summary),
                    showSummary = true,
                    onClick = { navController.navigate(Routes.Settings.DrsPrivacy) },
                )
            }

            if (path == DrsUserPath.HYBRID.name) {
                DrsIdentitySectionHeader(stringRes(R.string.drs__root__section_unified))
                DrsIdentityCard {
                    DrsIdentityTile(
                        icon = Icons.Default.Tune,
                        accent = accent,
                        title = stringRes(R.string.drs__unified__system_title),
                        summary = stringRes(R.string.drs__unified__system_home_summary),
                        showSummary = true,
                        onClick = { navController.navigate(Routes.Settings.DrsUnifiedSystem) },
                    )
                }
            }
        }

        // ----- About & help (all systems) -----
        DrsIdentitySectionHeader(stringRes(R.string.drs__root__section_about))
        DrsIdentityCard {
            DrsIdentityTile(
                icon = Icons.Default.Healing,
                accent = Color(0xFF0891B2),
                title = stringRes(R.string.help__title),
                summary = stringRes(R.string.drs__root__help_summary),
                showSummary = identity.showSummaries,
                onClick = { navController.navigate(Routes.Settings.Help) },
            )
            DrsIdentityTileDivider()
            DrsIdentityTile(
                icon = Icons.Outlined.Info,
                accent = accent,
                title = stringRes(R.string.about__title),
                summary = stringRes(R.string.about__summary),
                showSummary = identity.showSummaries,
                onClick = { navController.navigate(Routes.Settings.About) },
            )
        }
    }
}
