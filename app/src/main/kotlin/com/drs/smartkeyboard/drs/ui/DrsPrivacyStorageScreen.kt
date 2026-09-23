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

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SystemUpdateAlt
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.outlined.Extension
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Storage
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.drs.smartkeyboard.R
import com.drs.smartkeyboard.app.DrsPreferenceStore
import com.drs.smartkeyboard.app.LocalNavController
import com.drs.smartkeyboard.app.Routes
import com.drs.smartkeyboard.clipboardManager
import com.drs.smartkeyboard.drs.DrsStore
import com.drs.smartkeyboard.lib.compose.DrsScreen
import kotlinx.coroutines.launch
import org.drs.jetpref.datastore.model.collectAsState
import org.drs.lib.compose.stringRes

/**
 * DRS v1.0.8 (البند 10: Privacy / Permissions) — real privacy and
 * permission screens, not decorative ones.
 *
 * The permission list is read from the app's ACTUAL declared permissions
 * with their LIVE grant state, each bound to the real feature that needs
 * it. The privacy section states only verifiable architectural facts
 * (local-only storage, no text logging) and exposes the REAL switches
 * that control sensitive behavior (clipboard history, sensitive auto-
 * clean) plus the real data-management entries (storage, backup/restore).
 */

private data class DrsPermissionInfo(
    val titleRes: Int,
    val purposeRes: Int,
    val granted: Boolean,
)

@Composable
private fun rememberAppPermissions(): List<DrsPermissionInfo> {
    val context = LocalContext.current
    return remember(context) {
        buildList {
            add(
                DrsPermissionInfo(
                    titleRes = R.string.drs__perm__vibrate,
                    purposeRes = R.string.drs__perm__vibrate_purpose,
                    granted = context.checkSelfPermission(Manifest.permission.VIBRATE) ==
                        android.content.pm.PackageManager.PERMISSION_GRANTED,
                ),
            )
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                add(
                    DrsPermissionInfo(
                        titleRes = R.string.drs__perm__notifications,
                        purposeRes = R.string.drs__perm__notifications_purpose,
                        granted = context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) ==
                            android.content.pm.PackageManager.PERMISSION_GRANTED,
                    ),
                )
            }
            add(
                DrsPermissionInfo(
                    titleRes = R.string.drs__perm__internet,
                    purposeRes = R.string.drs__perm__internet_purpose,
                    // Normal-scope permission: granted at install, cannot be revoked.
                    granted = true,
                ),
            )
            add(
                DrsPermissionInfo(
                    titleRes = R.string.drs__perm__install,
                    purposeRes = R.string.drs__perm__install_purpose,
                    granted = try {
                        context.packageManager.canRequestPackageInstalls()
                    } catch (_: Throwable) {
                        false
                    },
                ),
            )
        }
    }
}

@Composable
fun DrsPrivacyScreen() = DrsScreen {
    title = stringRes(R.string.drs__privacy__title)
    previewFieldVisible = false

    val context = LocalContext.current
    val navController = LocalNavController.current
    val prefs by DrsPreferenceStore
    val scope = rememberCoroutineScope()
    val permissions = rememberAppPermissions()

    val clipboardHistoryEnabled by prefs.clipboard.historyEnabled.collectAsState()
    val sensitiveCleanEnabled by prefs.clipboard.historyAutoCleanSensitiveEnabled.collectAsState()
    val useInternalClipboard by prefs.clipboard.useInternalClipboard.collectAsState()

    content {
        // ----- Permissions: real state, real purposes -----
        DrsIdentitySectionHeader(stringRes(R.string.drs__privacy__permissions_section))
        DrsIdentityCard {
            permissions.forEachIndexed { index, perm ->
                if (index > 0) DrsIdentityTileDivider()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringRes(perm.titleRes),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = stringRes(perm.purposeRes),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    DrsStatusChip(
                        text = if (perm.granted) {
                            stringRes(R.string.drs__perm__granted)
                        } else {
                            stringRes(R.string.drs__perm__not_granted)
                        },
                        accent = if (perm.granted) {
                            MaterialTheme.colorScheme.tertiary
                        } else {
                            MaterialTheme.colorScheme.error
                        },
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(
                onClick = {
                    runCatching {
                        context.startActivity(
                            Intent(
                                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                Uri.fromParts("package", context.packageName, null),
                            ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringRes(R.string.drs__perm__open_system_settings))
            }
        }

        // ----- Privacy truths (verifiable architecture) -----
        DrsIdentitySectionHeader(stringRes(R.string.drs__privacy__facts_section))
        DrsIdentityCard {
            PrivacyFactRow(stringRes(R.string.drs__privacy__fact_local))
            DrsIdentityTileDivider()
            PrivacyFactRow(stringRes(R.string.drs__privacy__fact_no_text_log))
            DrsIdentityTileDivider()
            PrivacyFactRow(stringRes(R.string.drs__privacy__fact_ime_offline))
        }

        // ----- Real data controls -----
        DrsIdentitySectionHeader(stringRes(R.string.drs__privacy__controls_section))
        DrsIdentityCard {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringRes(R.string.drs__privacy__clipboard_history),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = stringRes(R.string.drs__privacy__clipboard_history_summary),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                androidx.compose.material3.Switch(
                    checked = clipboardHistoryEnabled,
                    onCheckedChange = { checked ->
                        scope.launch { prefs.clipboard.historyEnabled.set(checked) }
                    },
                )
            }
            DrsIdentityTileDivider()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringRes(R.string.drs__privacy__sensitive_clean),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = stringRes(R.string.drs__privacy__sensitive_clean_summary),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                androidx.compose.material3.Switch(
                    checked = sensitiveCleanEnabled,
                    onCheckedChange = { checked ->
                        scope.launch { prefs.clipboard.historyAutoCleanSensitiveEnabled.set(checked) }
                    },
                )
            }
            DrsIdentityTileDivider()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringRes(R.string.drs__privacy__internal_clipboard),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = stringRes(R.string.drs__privacy__internal_clipboard_summary),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                androidx.compose.material3.Switch(
                    checked = useInternalClipboard,
                    onCheckedChange = { checked ->
                        scope.launch { prefs.clipboard.useInternalClipboard.set(checked) }
                    },
                )
            }
        }

        // ----- Data management: real links -----
        DrsIdentitySectionHeader(stringRes(R.string.drs__privacy__data_section))
        DrsIdentityCard {
            DrsIdentityTile(
                icon = Icons.Outlined.Storage,
                accent = accentForActiveSystem(),
                title = stringRes(R.string.drs__storage__title),
                summary = stringRes(R.string.drs__storage__summary),
                onClick = { navController.navigate(Routes.Settings.DrsStorage) },
            )
            DrsIdentityTileDivider()
            DrsIdentityTile(
                icon = Icons.Default.SystemUpdateAlt,
                accent = MaterialTheme.colorScheme.secondary,
                title = stringRes(R.string.backup_and_restore__back_up__title),
                summary = stringRes(R.string.drs__storage__backup_summary),
                onClick = { navController.navigate(Routes.Settings.Backup) },
            )
            DrsIdentityTileDivider()
            DrsIdentityTile(
                icon = Icons.Outlined.Language,
                accent = MaterialTheme.colorScheme.tertiary,
                title = stringRes(R.string.backup_and_restore__restore__title),
                summary = stringRes(R.string.drs__storage__restore_summary),
                onClick = { navController.navigate(Routes.Settings.Restore) },
            )
        }
    }
}

@Composable
private fun PrivacyFactRow(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        androidx.compose.material3.Icon(
            imageVector = Icons.Default.VerifiedUser,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.tertiary,
            modifier = Modifier.width(22.dp),
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
fun DrsStorageScreen() = DrsScreen {
    title = stringRes(R.string.drs__storage__title)
    previewFieldVisible = false

    val navController = LocalNavController.current
    val drsState by DrsStore.state.collectAsState()
    val context = LocalContext.current
    val clipboardManager by context.clipboardManager()
    val clipboardHistory by clipboardManager.historyFlow.collectAsState()

    content {
        // ----- Local DRS data: real measured numbers -----
        DrsIdentitySectionHeader(stringRes(R.string.drs__storage__local_section))
        DrsIdentityCard {
            StorageRow(
                label = stringRes(R.string.drs__storage__state_file),
                value = stringRes(R.string.drs__dash__bytes, "bytes" to DrsStore.fileSizeBytes()),
            )
            DrsIdentityTileDivider()
            StorageRow(
                label = stringRes(R.string.drs__storage__profiles),
                value = drsState.profiles.size.toString(),
            )
            DrsIdentityTileDivider()
            StorageRow(
                label = stringRes(R.string.drs__storage__shortcuts),
                value = drsState.shortcuts.size.toString(),
            )
            DrsIdentityTileDivider()
            StorageRow(
                label = stringRes(R.string.drs__storage__clipboard_items),
                value = clipboardHistory.all.size.toString(),
            )
            DrsIdentityTileDivider()
            StorageRow(
                label = stringRes(R.string.drs__storage__health),
                value = if (DrsStore.storageHealthy()) {
                    stringRes(R.string.drs__dash__health_ok)
                } else {
                    stringRes(R.string.drs__dash__health_error)
                },
            )
        }

        // ----- Usage counters (anonymous, local only) -----
        DrsIdentitySectionHeader(stringRes(R.string.drs__storage__usage_section))
        DrsIdentityCard {
            StorageRow(
                label = stringRes(R.string.drs__storage__key_presses),
                value = drsState.usage.keyPresses.toString(),
            )
            DrsIdentityTileDivider()
            StorageRow(
                label = stringRes(R.string.drs__storage__emoji_uses),
                value = drsState.usage.emojiUses.toString(),
            )
            DrsIdentityTileDivider()
            StorageRow(
                label = stringRes(R.string.drs__storage__clipboard_uses),
                value = drsState.usage.clipboardUses.toString(),
            )
            DrsIdentityTileDivider()
            StorageRow(
                label = stringRes(R.string.drs__storage__shortcut_uses),
                value = drsState.usage.shortcutUses.toString(),
            )
        }

        // ----- Data management links -----
        DrsIdentitySectionHeader(stringRes(R.string.drs__privacy__data_section))
        DrsIdentityCard {
            DrsIdentityTile(
                icon = Icons.Default.SystemUpdateAlt,
                accent = accentForActiveSystem(),
                title = stringRes(R.string.backup_and_restore__back_up__title),
                summary = stringRes(R.string.drs__storage__backup_summary),
                onClick = { navController.navigate(Routes.Settings.Backup) },
            )
            DrsIdentityTileDivider()
            DrsIdentityTile(
                icon = Icons.Outlined.Extension,
                accent = MaterialTheme.colorScheme.tertiary,
                title = stringRes(R.string.backup_and_restore__restore__title),
                summary = stringRes(R.string.drs__storage__restore_summary),
                onClick = { navController.navigate(Routes.Settings.Restore) },
            )
        }
    }
}

@Composable
private fun StorageRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = accentForActiveSystem(),
        )
    }
}
