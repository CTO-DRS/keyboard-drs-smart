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

import com.drs.smartkeyboard.app.DrsPreferenceStore
import com.drs.smartkeyboard.lib.ext.ExtensionComponentName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.drs.lib.kotlin.tryOrNull
import java.util.UUID

/**
 * Applies a DRS profile to the underlying keyboard preferences. Profiles are
 * stored inside [DrsStore]; switching a profile only rewrites the affected
 * preference values - no user data is ever lost.
 */
object DrsProfileManager {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    const val DEFAULT_DAY_THEME = "org.drs.themes:drs_day"
    const val DEFAULT_NIGHT_THEME = "org.drs.themes:drs_night"
    const val BORDERLESS_DAY_THEME = "org.drs.themes:drs_day_borderless"
    const val BORDERLESS_NIGHT_THEME = "org.drs.themes:drs_night_borderless"

    /**
     * Builds the default profile for a user path (used by onboarding).
     * Defaults come from [DrsSystems] - the single source of truth for the
     * three systems' own keyboard configuration.
     */
    fun defaultProfileFor(path: DrsUserPath): DrsProfile {
        return DrsSystems.profileFor(path, UUID.randomUUID().toString())
    }

    /**
     * Activates one of the three systems: guarantees the system owns its
     * own profile (created from the system defaults on first use), applies
     * it to the live keyboard, and stores the system as the user path.
     * Each system keeps its own tuning across switches - experimenting
     * with another system never destroys a system's configuration.
     */
    fun applySystem(path: DrsUserPath) {
        val profile = DrsSystems.ensureProfile(DrsStore.state.value, path)
        applyProfile(profile)
        DrsStore.update { s -> s.copy(userPath = path.name) }
    }

    /**
     * Provisions a dedicated profile for every system that does not own
     * one yet (idempotent). Called when the control center opens so each
     * system always has its own ready-to-activate profile.
     */
    fun ensureSystemProfiles() {
        val state = DrsStore.state.value
        val missing = DrsUserPath.entries.any { path ->
            state.profiles.none { it.path == path.name }
        }
        if (!missing) return
        DrsStore.update { s ->
            val profiles = s.profiles.toMutableList()
            DrsUserPath.entries.forEach { path ->
                if (profiles.none { it.path == path.name }) {
                    profiles += DrsSystems.profileFor(path, UUID.randomUUID().toString())
                }
            }
            s.copy(profiles = profiles)
        }
    }

    /** Creates an empty custom profile based on another profile's settings. */
    fun customProfileFrom(base: DrsProfile, name: String): DrsProfile {
        return base.copy(id = UUID.randomUUID().toString(), name = name, path = "CUSTOM")
    }

    /**
     * Persists the profile and applies its settings to the live keyboard
     * preferences. The active theme ids are written through the same
     * preference path the theme manager observes, so the keyboard re-skins
     * immediately.
     */
    fun applyProfile(profile: DrsProfile) {
        DrsStore.update { state ->
            state.copy(
                activeProfileId = profile.id,
                profiles = if (state.profiles.any { it.id == profile.id }) {
                    state.profiles.map { if (it.id == profile.id) profile else it }
                } else {
                    state.profiles + profile
                },
            )
        }
        val prefs by DrsPreferenceStore
        scope.launch {
            prefs.keyboard.numberRow.set(profile.numberRow)
            prefs.suggestion.enabled.set(profile.suggestionsEnabled)
            prefs.clipboard.historyEnabled.set(profile.clipboardHistoryEnabled)
            prefs.inputFeedback.audioEnabled.set(profile.audioFeedbackEnabled)
            prefs.inputFeedback.hapticEnabled.set(profile.hapticFeedbackEnabled)
            runCatching {
                val day = tryOrNull { ExtensionComponentName.from(profile.dayThemeId) }
                val night = tryOrNull { ExtensionComponentName.from(profile.nightThemeId) }
                if (day != null) prefs.theme.dayThemeId.set(day)
                if (night != null) prefs.theme.nightThemeId.set(night)
            }
        }
    }

    /** Adds a newly created profile and activates it. */
    fun createAndApply(profile: DrsProfile) {
        // applyProfile already inserts the profile when absent - no separate
        // add step, so two racing updates can never duplicate an entry.
        applyProfile(profile)
    }

    /** Returns the currently active profile, or null when none exists yet. */
    fun activeProfile(state: DrsState): DrsProfile? {
        return state.profiles.firstOrNull { it.id == state.activeProfileId }
            ?: state.profiles.firstOrNull()
    }
}
