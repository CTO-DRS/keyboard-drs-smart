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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

/**
 * Local-only store for the DRS adaptive layer (user path, profiles,
 * shortcuts, usage stats). All data stays on-device in a single small JSON
 * file inside noBackupFilesDir; nothing is ever sent to any server.
 *
 * The app is single-process (no android:process attributes in the manifest),
 * so this object is safely shared between the IME service and app activities.
 */
object DrsStore {
    private const val FILE_NAME = "drs_state.json"
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }
    private val mutex = Mutex()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _state = MutableStateFlow(DrsState())
    val state: StateFlow<DrsState> = _state.asStateFlow()

    /** Process-wide guard so the onboarding is only launched once per launch request. */
    @Volatile
    var onboardingLaunchGuard: Boolean = false

    @Volatile
    private var file: File? = null

    /**
     * Loads the persisted state synchronously. The file is tiny (a few KB at
     * most) so the one-time read at process start is negligible.
     */
    fun init(context: Context) {
        if (file != null) return
        synchronized(this) {
            if (file != null) return
            val f = File(context.noBackupFilesDir, FILE_NAME)
            val loaded = if (f.isFile) {
                try {
                    json.decodeFromString<DrsState>(f.readText())
                } catch (_: Throwable) {
                    DrsState()
                }
            } else {
                DrsState()
            }
            // Publish state BEFORE exposing the file, so a racing update() can
            // never write the default state over the loaded one.
            _state.value = loaded
            file = f
        }
    }

    /** Asynchronous state transformation (safe to call from anywhere). */
    fun update(transform: (DrsState) -> DrsState) {
        scope.launch { updateNow(transform) }
    }

    /** Synchronous (suspend) state transformation with atomic file write. */
    suspend fun updateNow(transform: (DrsState) -> DrsState) {
        mutex.withLock {
            val next = transform(_state.value)
            _state.value = next
            write(next)
        }
    }

    private fun write(state: DrsState) {
        val f = file ?: return
        try {
            val tmp = File(f.parentFile, f.name + ".tmp")
            tmp.writeText(json.encodeToString(state))
            if (!tmp.renameTo(f)) {
                f.writeText(tmp.readText())
                tmp.delete()
            }
        } catch (_: Throwable) {
            // Persisting the adaptive layer must never crash the keyboard.
        }
    }

    /** Wipes all DRS-layer local data (profiles, shortcuts, stats). */
    suspend fun resetAll() {
        updateNow { DrsState(onboardingDone = true) }
    }

    /**
     * Self-check used by the diagnostics screen: reports whether the store
     * is initialized and its backing directory is writable. Never throws.
     */
    fun storageHealthy(): Boolean {
        return try {
            val f = file ?: return false
            f.parentFile?.canWrite() == true
        } catch (_: Throwable) {
            false
        }
    }
}
