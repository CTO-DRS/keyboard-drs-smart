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

import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.drs.smartkeyboard.BuildConfig
import com.drs.smartkeyboard.R
import com.drs.smartkeyboard.app.DrsPreferenceStore
import com.drs.smartkeyboard.drs.DrsCrashHandler
import com.drs.smartkeyboard.drs.DrsBackup
import com.drs.smartkeyboard.drs.DrsEventLog
import com.drs.smartkeyboard.drs.DrsEconomy
import com.drs.smartkeyboard.drs.DrsHybridViewMode
import com.drs.smartkeyboard.drs.DrsProfileManager
import com.drs.smartkeyboard.drs.DrsRewardCatalog
import com.drs.smartkeyboard.drs.DrsStore
import com.drs.smartkeyboard.drs.DrsSystems
import com.drs.smartkeyboard.drs.DrsToolView
import com.drs.smartkeyboard.drs.DrsUnifiedTools
import com.drs.smartkeyboard.ime.input.DrsSoundStyle
import com.drs.smartkeyboard.lib.compose.DrsScreen
import com.drs.smartkeyboard.lib.ext.ExtensionComponentName
import com.drs.smartkeyboard.lib.util.InputMethodUtils
import com.drs.smartkeyboard.appContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.drs.lib.compose.stringRes

/**
 * DRS Keyboard Diagnostics: in-app self checks for the IME status, service
 * wiring, layouts, themes, profiles, shortcuts, clipboard state and a
 * sanitized local crash log viewer. Also hosts the privacy "clear local DRS
 * data" action. No typed user content is ever collected here.
 */
@Composable
fun DrsDiagnosticsScreen() = DrsScreen {
    title = stringRes(R.string.drs__diagnostics__title)
    navigationIconVisible = true
    previewFieldVisible = false

    val context = LocalContext.current
    val appContext by context.appContext()
    val prefs by DrsPreferenceStore

    val isEnabled by InputMethodUtils.observeIsDrsKeyboardEnabled(foregroundOnly = false)
    val isSelected by InputMethodUtils.observeIsDrsKeyboardSelected(foregroundOnly = false)
    val drsState by DrsStore.state.collectAsState()

    var showClearDataDialog by remember { mutableStateOf(false) }
    val crashLog = remember { DrsCrashHandler.readLog() }
    val navController = com.drs.smartkeyboard.app.LocalNavController.current

    val layoutsAvailable = remember {
        try {
            appContext.assets.list("ime/keyboard/org.drs.layouts/layouts/characters").orEmpty().isNotEmpty()
        } catch (_: Throwable) {
            false
        }
    }
    val themesAvailable = remember {
        try {
            appContext.assets.list("ime/theme/org.drs.themes/stylesheets").orEmpty().isNotEmpty()
        } catch (_: Throwable) {
            false
        }
    }
    val drsThemesAvailable = remember {
        try {
            appContext.assets.list("ime/theme/org.drs.themes.drs/stylesheets").orEmpty().size >= 8
        } catch (_: Throwable) {
            false
        }
    }
    val nativeLibAvailable = remember {
        try {
            System.loadLibrary("drs_native")
            true
        } catch (_: Throwable) {
            false
        }
    }
    val soundPacksAvailable = remember {
        try {
            DrsSoundStyle.entries.firstNotNullOfOrNull { it.resId }?.let { resId ->
                appContext.resources.openRawResource(resId).use { true }
            } ?: false
        } catch (_: Throwable) {
            false
        }
    }

    // ---------------- DRS v1.0.6: full technical test runner ----------------
    var testResults by remember { mutableStateOf<List<DrsTestResult>?>(null) }
    fun runFullTest() {
        testResults = computeDrsFullTest(
            imeEnabled = isEnabled,
            imeSelected = isSelected,
            prefsLoaded = appContext.preferenceStoreLoaded.value,
            profileReady = drsState.profiles.any { it.path == drsState.userPath },
            tuningMatch = run {
                val active = DrsProfileManager.activeProfile(drsState)
                active?.path == drsState.userPath || active?.path == "CUSTOM"
            },
            layoutsAvailable = layoutsAvailable,
            themesAvailable = themesAvailable,
            drsThemesAvailable = drsThemesAvailable,
            nativeLibAvailable = nativeLibAvailable,
            soundPacksAvailable = soundPacksAvailable,
            onboardingDone = drsState.onboardingDone,
            shortcutsUnique = drsState.shortcuts.map { it.shortcut }.distinct().size == drsState.shortcuts.size,
            profileThemesValid = drsState.profiles.all { profile ->
                try {
                    ExtensionComponentName.from(profile.dayThemeId)
                    ExtensionComponentName.from(profile.nightThemeId)
                    true
                } catch (_: Throwable) {
                    false
                }
            },
            storeHealthy = DrsStore.storageHealthy(),
            activeProfileOk = DrsProfileManager.activeProfile(drsState) != null,
            walletHealthy = drsState.wallet.ledger.size <= 24 &&
                DrsEconomy.balanceOf(drsState.wallet, DrsSystems.specOfName(drsState.userPath).path) >= 0,
            eventErrorCount = DrsEventLog.snapshot().count { it.level == DrsEventLog.Level.ERROR },
            unifiedStateConsistent = {
                val validView = DrsHybridViewMode.entries.any { it.name == drsState.hybridViewMode }
                val knownToolIds = DrsUnifiedTools.ALL.map { it.id }.toSet()
                val orderKnown = drsState.unifiedToolOrder.all { it in knownToolIds }
                val viewsValid = drsState.unifiedToolViews.all { (id, view) ->
                    id in knownToolIds && DrsToolView.entries.any { it.name == view }
                }
                val hiddenKnown = drsState.hiddenUnifiedTools.all { it in knownToolIds }
                val pinnedKnown = drsState.pinnedUnifiedTools.all { it in knownToolIds }
                validView && orderKnown && viewsValid && hiddenKnown && pinnedKnown
            }(),
        )
    }

    content {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            DrsDiagCard(title = stringRes(R.string.drs__diagnostics__ime_section)) {
                DrsCheckRow(
                    label = stringRes(R.string.drs__diagnostics__check_ime_enabled),
                    passed = isEnabled,
                )
                DrsCheckRow(
                    label = stringRes(R.string.drs__diagnostics__check_ime_selected),
                    passed = isSelected,
                )
                DrsCheckRow(
                    label = stringRes(R.string.drs__diagnostics__check_prefs_loaded),
                    passed = appContext.preferenceStoreLoaded.value,
                )
            }

            // ---------------- full technical test (DRS v1.0.6) ----------------
            DrsDiagCard(title = stringRes(R.string.drs__diagnostics__test_section)) {
                OutlinedButton(onClick = { runFullTest() }) {
                    Text(stringRes(R.string.drs__diagnostics__test_run))
                }
                testResults?.let { results ->
                    val passed = results.count { it.severity == DrsTestSeverity.PASS }
                    val warnings = results.count { it.severity == DrsTestSeverity.WARNING }
                    val errors = results.count { it.severity == DrsTestSeverity.ERROR }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = stringRes(
                            R.string.drs__diagnostics__test_summary,
                            "passed" to passed.toString(),
                            "warnings" to warnings.toString(),
                            "errors" to errors.toString(),
                        ),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = when {
                            errors > 0 -> MaterialTheme.colorScheme.error
                            warnings > 0 -> MaterialTheme.colorScheme.tertiary
                            else -> MaterialTheme.colorScheme.primary
                        },
                    )
                    Spacer(Modifier.height(4.dp))
                    results.forEach { result ->
                        TestResultRow(result)
                    }
                }
            }

            // ---------------- active system ----------------
            val activeSystem = DrsSystems.specOfName(drsState.userPath)
            DrsDiagCard(title = stringRes(R.string.drs__diagnostics__system_section)) {
                Text(
                    text = stringRes(pathTitleRes(activeSystem)),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = stringRes(activeSystem.taglineRes),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 4.dp),
                )
                DrsCheckRow(
                    label = stringRes(R.string.drs__diagnostics__system_profile_ready),
                    passed = drsState.profiles.any { it.path == drsState.userPath },
                )
                DrsCheckRow(
                    label = stringRes(R.string.drs__diagnostics__system_tuning_match),
                    passed = run {
                        val active = DrsProfileManager.activeProfile(drsState)
                        active?.path == drsState.userPath || active?.path == "CUSTOM"
                    },
                )
            }

            DrsDiagCard(title = stringRes(R.string.drs__diagnostics__layouts_section)) {
                DrsCheckRow(
                    label = stringRes(R.string.drs__diagnostics__check_character_layouts),
                    passed = layoutsAvailable,
                )
                DrsCheckRow(
                    label = stringRes(R.string.drs__diagnostics__check_theme_assets),
                    passed = themesAvailable,
                )
            }

            DrsDiagCard(title = stringRes(R.string.drs__diagnostics__extended_section)) {
                DrsCheckRow(
                    label = stringRes(R.string.drs__diagnostics__check_drs_themes),
                    passed = drsThemesAvailable,
                )
                DrsCheckRow(
                    label = stringRes(R.string.drs__diagnostics__check_native_lib),
                    passed = nativeLibAvailable,
                )
                DrsCheckRow(
                    label = stringRes(R.string.drs__diagnostics__check_sound_packs),
                    passed = soundPacksAvailable,
                )
                DrsCheckRow(
                    label = stringRes(R.string.drs__diagnostics__check_onboarding),
                    passed = drsState.onboardingDone,
                )
                DrsCheckRow(
                    label = stringRes(R.string.drs__diagnostics__check_shortcuts_unique),
                    passed = drsState.shortcuts.map { it.shortcut }.distinct().size == drsState.shortcuts.size,
                )
                DrsCheckRow(
                    label = stringRes(R.string.drs__diagnostics__check_profile_themes),
                    passed = drsState.profiles.all { profile ->
                        try {
                            ExtensionComponentName.from(profile.dayThemeId)
                            ExtensionComponentName.from(profile.nightThemeId)
                            true
                        } catch (_: Throwable) {
                            false
                        }
                    },
                )
                DrsCheckRow(
                    label = stringRes(R.string.drs__diagnostics__check_store_file),
                    passed = DrsStore.storageHealthy(),
                )
            }

            DrsDiagCard(title = stringRes(R.string.drs__diagnostics__state_section)) {
                Text(
                    text = stringRes(
                        R.string.drs__diagnostics__profiles_count,
                        "count" to drsState.profiles.size.toString(),
                    ),
                    fontSize = 14.sp,
                )
                Text(
                    text = stringRes(
                        R.string.drs__diagnostics__shortcuts_count,
                        "count" to drsState.shortcuts.size.toString(),
                    ),
                    fontSize = 14.sp,
                )
                Text(
                    text = stringRes(
                        R.string.drs__diagnostics__keypress_count,
                        "count" to drsState.usage.keyPresses.toString(),
                    ),
                    fontSize = 14.sp,
                )
                DrsCheckRow(
                    label = stringRes(R.string.drs__diagnostics__check_active_profile),
                    passed = DrsProfileManager.activeProfile(drsState) != null,
                )
                DrsCheckRow(
                    label = stringRes(R.string.drs__diagnostics__check_clipboard_history),
                    passed = prefs.clipboard.historyEnabled.get(),
                )
                DrsCheckRow(
                    label = stringRes(R.string.drs__diagnostics__check_wallet),
                    passed = drsState.wallet.ledger.size <= 24 &&
                        DrsEconomy.balanceOf(drsState.wallet, DrsSystems.specOfName(drsState.userPath).path) >= 0,
                )
            }

            DrsDiagCard(title = stringRes(R.string.drs__diagnostics__info_section)) {
                Text(
                    text = "DRS Keyboard V" + BuildConfig.VERSION_NAME +
                        " (" + BuildConfig.VERSION_CODE + ")",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = stringRes(R.string.drs__about__release_tagline),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 2.dp),
                )
                DrsRewardCatalog.itemOf(drsState.wallet.equippedTitle)?.let { item ->
                    Text(
                        text = stringRes(
                            R.string.drs__diagnostics__equipped_title,
                            "{title}" to stringRes(item.titleRes),
                        ),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.padding(top = 2.dp),
                    )
                }
                DrsRewardCatalog.itemOf(drsState.wallet.equippedBadge)?.let { item ->
                    Text(
                        text = stringRes(
                            R.string.drs__diagnostics__equipped_badge,
                            "{badge}" to stringRes(item.titleRes),
                        ),
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.tertiary,
                    )
                }
                Text(
                    text = "Android SDK " + Build.VERSION.SDK_INT,
                    fontSize = 14.sp,
                )
            }

            DrsDiagCard(title = stringRes(R.string.drs__diagnostics__crash_section)) {
                if (crashLog.isBlank()) {
                    Text(
                        text = stringRes(R.string.drs__diagnostics__crash_empty),
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else {
                    Text(
                        text = crashLog,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        maxLines = 10,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    OutlinedButton(onClick = { DrsCrashHandler.clearLog() }) {
                        Text(stringRes(R.string.drs__diagnostics__crash_clear))
                    }
                }
            }

            // ---------------- sanitized event log (DRS v1.0.6) ----------------
            DrsDiagCard(title = stringRes(R.string.drs__diagnostics__events_section)) {
                val eventEntries = remember { DrsEventLog.snapshot() }
                if (eventEntries.isEmpty()) {
                    Text(
                        text = stringRes(R.string.drs__diagnostics__events_empty),
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                            .height(180.dp),
                    ) {
                        eventEntries.forEach { entry ->
                            Text(
                                text = eventLine(entry),
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                color = when (entry.level) {
                                    DrsEventLog.Level.ERROR -> MaterialTheme.colorScheme.error
                                    DrsEventLog.Level.WARNING -> MaterialTheme.colorScheme.tertiary
                                    DrsEventLog.Level.INFO -> MaterialTheme.colorScheme.onSurfaceVariant
                                },
                            )
                        }
                    }
                }
                Row {
                    OutlinedButton(onClick = { DrsEventLog.clear() }) {
                        Text(stringRes(R.string.drs__diagnostics__events_clear))
                    }
                    Spacer(Modifier.width(8.dp))
                    OutlinedButton(onClick = { navController.navigate(com.drs.smartkeyboard.app.Routes.Settings.DrsPerformance) }) {
                        Text(stringRes(R.string.drs__performance__title))
                    }
                }
                Text(
                    text = stringRes(R.string.drs__diagnostics__events_privacy),
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            // ---------------- DRS data backup / restore (DRS v1.0.6) --------
            DrsDiagCard(title = stringRes(R.string.drs__diagnostics__backup_section)) {
                Text(
                    text = stringRes(R.string.drs__diagnostics__backup_hint),
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                val exportState = remember { mutableStateOf<String?>(null) }
                val exportLauncher = rememberLauncherForActivityResult(
                    ActivityResultContracts.CreateDocument("application/json"),
                ) { uri ->
                    val payload = exportState.value
                    if (uri != null && payload != null) {
                        runCatching {
                            appContext.contentResolver.openOutputStream(uri)?.use { out ->
                                out.write(payload.toByteArray(Charsets.UTF_8))
                            } ?: throw IllegalStateException("output stream null")
                            DrsEventLog.recordInfo(DrsEventLog.Categories.STORE, "backup exported")
                        }
                    }
                    exportState.value = null
                }
                OutlinedButton(onClick = {
                    exportState.value = DrsBackup.exportJson()
                    exportLauncher.launch(DrsBackup.defaultFileName())
                }) {
                    Text(stringRes(R.string.drs__diagnostics__backup_export))
                }
                var importError by remember { mutableStateOf<String?>(null) }
                val importLauncher = rememberLauncherForActivityResult(
                    ActivityResultContracts.OpenDocument(),
                ) { uri ->
                    if (uri != null) {
                        val result = DrsBackup.importFrom(appContext, uri)
                        importError = result.errorRes?.let { res ->
                            appContext.getString(res)
                        }
                    }
                }
                OutlinedButton(onClick = { importError = null; importLauncher.launch(arrayOf("application/json")) }) {
                    Text(stringRes(R.string.drs__diagnostics__backup_import))
                }
                importError?.let { message ->
                    Text(
                        text = message,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }

            DrsDiagCard(title = stringRes(R.string.drs__diagnostics__privacy_section)) {
                Text(
                    text = stringRes(R.string.drs__diagnostics__privacy_note),
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                OutlinedButton(onClick = { showClearDataDialog = true }) {
                    Text(stringRes(R.string.drs__diagnostics__clear_data))
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }

        if (showClearDataDialog) {
            AlertDialog(
                onDismissRequest = { showClearDataDialog = false },
                title = { Text(stringRes(R.string.drs__diagnostics__clear_data)) },
                text = { Text(stringRes(R.string.drs__diagnostics__clear_data_confirm)) },
                confirmButton = {
                    TextButton(onClick = {
                        showClearDataDialog = false
                        resetDrsData()
                    }) {
                        Text(stringRes(R.string.action__ok))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showClearDataDialog = false }) {
                        Text(stringRes(R.string.action__cancel))
                    }
                },
            )
        }
    }
}

private fun resetDrsData() {
    CoroutineScope(Dispatchers.IO).launch {
        DrsStore.resetAll()
    }
}

@Composable
private fun DrsDiagCard(title: String, content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit) {
    val colorScheme = MaterialTheme.colorScheme
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, colorScheme.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colorScheme.surfaceContainer.copy(alpha = 0.6f)),
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // DRS: a small primary accent bar leading the section title —
                // consistent with the group headers across the app.
                Box(
                    modifier = Modifier
                        .size(width = 3.dp, height = 14.dp)
                        .background(colorScheme.primary, RoundedCornerShape(2.dp)),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.primary,
                )
            }
            content()
        }
    }
}

@Composable
private fun DrsCheckRow(label: String, passed: Boolean) {
    Row {
        Text(
            text = if (passed) "✓" else "✗",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = if (passed) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.error
            },
        )
        Spacer(modifier = Modifier.height(0.dp))
        Text(
            text = "  $label",
            fontSize = 14.sp,
            modifier = Modifier.padding(bottom = 0.dp),
        )
    }
}

// ---------------- DRS v1.0.6: full technical test runner ----------------

private enum class DrsTestSeverity { PASS, WARNING, ERROR }

private data class DrsTestResult(
    val labelRes: Int,
    val severity: DrsTestSeverity,
    val hintRes: Int?,
)

@Composable
private fun TestResultRow(result: DrsTestResult) {
    Column(modifier = Modifier.padding(vertical = 2.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = when (result.severity) {
                    DrsTestSeverity.PASS -> "✓"
                    DrsTestSeverity.WARNING -> "!"
                    DrsTestSeverity.ERROR -> "✗"
                },
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = when (result.severity) {
                    DrsTestSeverity.PASS -> MaterialTheme.colorScheme.primary
                    DrsTestSeverity.WARNING -> MaterialTheme.colorScheme.tertiary
                    DrsTestSeverity.ERROR -> MaterialTheme.colorScheme.error
                },
            )
            Text(
                text = "  " + stringRes(result.labelRes),
                fontSize = 13.sp,
            )
        }
        if (result.severity != DrsTestSeverity.PASS && result.hintRes != null) {
            Text(
                text = stringRes(result.hintRes),
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 20.dp),
            )
        }
    }
}

/**
 * Severity rules for the full test: every input comes from a REAL check
 * already used elsewhere on this screen. Warnings mean "degraded but
 * usable - user action recommended"; errors mean "a core feature cannot
 * work". Nothing here is simulated.
 */
private fun computeDrsFullTest(
    imeEnabled: Boolean,
    imeSelected: Boolean,
    prefsLoaded: Boolean,
    profileReady: Boolean,
    tuningMatch: Boolean,
    layoutsAvailable: Boolean,
    themesAvailable: Boolean,
    drsThemesAvailable: Boolean,
    nativeLibAvailable: Boolean,
    soundPacksAvailable: Boolean,
    onboardingDone: Boolean,
    shortcutsUnique: Boolean,
    profileThemesValid: Boolean,
    storeHealthy: Boolean,
    activeProfileOk: Boolean,
    walletHealthy: Boolean,
    eventErrorCount: Int,
    unifiedStateConsistent: Boolean,
): List<DrsTestResult> {
    fun result(pass: Boolean, warn: Boolean, label: Int, hint: Int): DrsTestResult =
        DrsTestResult(
            label,
            when {
                pass -> DrsTestSeverity.PASS
                warn -> DrsTestSeverity.WARNING
                else -> DrsTestSeverity.ERROR
            },
            if (pass) null else hint,
        )
    return listOf(
        result(imeEnabled, warn = true, R.string.drs__diagnostics__check_ime_enabled, R.string.drs__diagnostics__hint_ime),
        result(imeSelected, warn = true, R.string.drs__diagnostics__check_ime_selected, R.string.drs__diagnostics__hint_ime),
        result(prefsLoaded, warn = false, R.string.drs__diagnostics__check_prefs_loaded, R.string.drs__diagnostics__hint_restart),
        result(profileReady, warn = false, R.string.drs__diagnostics__system_profile_ready, R.string.drs__diagnostics__hint_profile),
        result(tuningMatch, warn = true, R.string.drs__diagnostics__system_tuning_match, R.string.drs__diagnostics__hint_tuning),
        result(layoutsAvailable, warn = false, R.string.drs__diagnostics__check_character_layouts, R.string.drs__diagnostics__hint_assets),
        result(themesAvailable, warn = false, R.string.drs__diagnostics__check_theme_assets, R.string.drs__diagnostics__hint_assets),
        result(drsThemesAvailable, warn = true, R.string.drs__diagnostics__check_drs_themes, R.string.drs__diagnostics__hint_assets),
        result(nativeLibAvailable, warn = true, R.string.drs__diagnostics__check_native_lib, R.string.drs__diagnostics__hint_native),
        result(soundPacksAvailable, warn = true, R.string.drs__diagnostics__check_sound_packs, R.string.drs__diagnostics__hint_assets),
        result(onboardingDone, warn = true, R.string.drs__diagnostics__check_onboarding, R.string.drs__diagnostics__hint_onboarding),
        result(shortcutsUnique, warn = true, R.string.drs__diagnostics__check_shortcuts_unique, R.string.drs__diagnostics__hint_shortcuts),
        result(profileThemesValid, warn = false, R.string.drs__diagnostics__check_profile_themes, R.string.drs__diagnostics__hint_profile_themes),
        result(storeHealthy, warn = false, R.string.drs__diagnostics__check_store_file, R.string.drs__diagnostics__hint_store),
        result(activeProfileOk, warn = false, R.string.drs__diagnostics__check_active_profile, R.string.drs__diagnostics__hint_profile),
        result(walletHealthy, warn = true, R.string.drs__diagnostics__check_wallet, R.string.drs__diagnostics__hint_restart),
        result(eventErrorCount == 0, warn = true, R.string.drs__diagnostics__check_events_clean, R.string.drs__diagnostics__hint_events),
        // DRS v1.0.7: unified «كلاهما» layer consistency - unknown tool ids
        // or invalid level names in the customization are dropped by the
        // resolver, so this only ever degrades to a warning.
        result(
            unifiedStateConsistent,
            warn = true,
            R.string.drs__diagnostics__check_unified,
            R.string.drs__diagnostics__hint_unified,
        ),
    )
}

private fun eventLine(entry: DrsEventLog.Entry): String {
    val timeFormat = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.US)
    return "${timeFormat.format(java.util.Date(entry.timestampMs))} [${entry.level}] ${entry.category}: ${entry.detail}"
}
