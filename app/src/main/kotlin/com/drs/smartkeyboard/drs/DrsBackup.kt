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

package com.drs.smartkeyboard.drs

import android.content.Context
import android.net.Uri
import com.drs.smartkeyboard.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.serialization.json.Json

/**
 * DRS v1.0.6: export/import of the DRS layer state (profiles, shortcuts,
 * wallet, usage counters, technical toolbar arrangement) as a single JSON
 * file chosen via the system file picker.
 *
 * Import is strictly validated BEFORE anything is applied:
 *  1. The file must parse as JSON.
 *  2. It must deserialize into [DrsState] (unknown keys ignored).
 *  3. Structural sanity: version >= 1 and userPath must be a known system.
 *
 * A file failing any check is REJECTED with an error message - a corrupt or
 * malicious file can never crash the app or replace the state with garbage.
 * Only the state fields listed in [DrsState] are touched; nothing else in
 * the app is modified by an import.
 */
object DrsBackup {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        isLenient = false
    }

    fun defaultFileName(): String {
        val stamp = SimpleDateFormat("yyyyMMdd_HHmm", Locale.US).format(Date())
        return "drs-keyboard-state-$stamp.json"
    }

    /** Serializes the current DRS state. Never throws. */
    fun exportJson(): String = try {
        json.encodeToString(DrsStore.state.value)
    } catch (_: Throwable) {
        "{}"
    }

    /** Outcome of a validated import attempt. */
    data class ImportResult(
        val success: Boolean,
        val errorRes: Int? = null,
    )

    fun importFrom(context: Context, uri: Uri): ImportResult {
        val raw = try {
            context.contentResolver.openInputStream(uri)?.use { stream ->
                stream.readBytes().toString(Charsets.UTF_8)
            } ?: return ImportResult(false, R.string.drs__diagnostics__backup_error_read)
        } catch (_: Throwable) {
            return ImportResult(false, R.string.drs__diagnostics__backup_error_read)
        }
        if (raw.length > MAX_IMPORT_BYTES) {
            return ImportResult(false, R.string.drs__diagnostics__backup_error_invalid)
        }
        val parsed = try {
            json.decodeFromString<DrsState>(raw)
        } catch (_: Throwable) {
            return ImportResult(false, R.string.drs__diagnostics__backup_error_invalid)
        }
        // Structural sanity: a known user path and a plausible version.
        if (parsed.version < 1) {
            return ImportResult(false, R.string.drs__diagnostics__backup_error_invalid)
        }
        if (parsed.userPath !in listOf(
                DrsUserPath.NORMAL.name,
                DrsUserPath.TECHNICAL.name,
                DrsUserPath.HYBRID.name,
            ) && parsed.userPath !in listOf("CUSTOM")
        ) {
            return ImportResult(false, R.string.drs__diagnostics__backup_error_invalid)
        }
        return try {
            // Preserve runtime-only flags that must not travel with a backup:
            // the onboarding flow is NOT re-triggered by a restore.
            val restored = parsed.copy(onboardingDone = DrsStore.state.value.onboardingDone)
            DrsStore.update { restored }
            DrsEventLog.recordInfo(DrsEventLog.Categories.STORE, "state imported (${parsed.shortcuts.size} shortcuts)")
            ImportResult(true)
        } catch (_: Throwable) {
            ImportResult(false, R.string.drs__diagnostics__backup_error_invalid)
        }
    }

    private const val MAX_IMPORT_BYTES = 4 * 1024 * 1024
}
