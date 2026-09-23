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

import com.drs.smartkeyboard.ime.editor.DrsEditorInfo
import com.drs.smartkeyboard.ime.editor.InputAttributes
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Derives the [DrsContextMode] from the focused editor (EditorInfo) every time
 * input starts. The mode drives adaptive UI (e.g. hiding the technical strip
 * in password fields, preferring numbers in numeric fields).
 *
 * Detection is intentionally conservative and fully on-device.
 */
object DrsRuntimeState {

    private val _contextMode = MutableStateFlow(DrsContextMode.NORMAL)
    val contextMode: StateFlow<DrsContextMode> = _contextMode.asStateFlow()

    /** Packages whose names hint at technical/coding usage. */
    private val codingPackageHints = listOf(
        "termux", "code", "coder", "ide", "terminal", "kotlin", "studio",
        "github", "gitlab", "git.", "vim", "emacs", "nano", "editor",
    )

    /** Called from EditorInstance.handleStartInputView for every new input. */
    fun onInputStarted(editorInfo: DrsEditorInfo) {
        val state = DrsStore.state.value
        if (!state.contextModesEnabled) {
            _contextMode.value = DrsContextMode.NORMAL
            return
        }
        _contextMode.value = detect(editorInfo, state.userPath)
    }

    private fun detect(editorInfo: DrsEditorInfo, userPath: String): DrsContextMode {
        val variation = editorInfo.inputAttributes.variation
        val type = editorInfo.inputAttributes.type
        return when {
            variation == InputAttributes.Variation.PASSWORD ||
                variation == InputAttributes.Variation.VISIBLE_PASSWORD ||
                variation == InputAttributes.Variation.WEB_PASSWORD ->
                DrsContextMode.PASSWORD

            type == InputAttributes.Type.NUMBER ||
                type == InputAttributes.Type.PHONE ||
                type == InputAttributes.Type.DATETIME ->
                DrsContextMode.NUMBERS

            variation == InputAttributes.Variation.URI ->
                DrsContextMode.SEARCH

            variation == InputAttributes.Variation.EMAIL_ADDRESS ||
                variation == InputAttributes.Variation.WEB_EMAIL_ADDRESS ->
                DrsContextMode.WRITING

            isCodingPackage(editorInfo.packageName) ->
                DrsContextMode.CODING

            userPath == DrsUserPath.TECHNICAL.name ->
                DrsContextMode.TECHNICAL

            else -> DrsContextMode.NORMAL
        }
    }

    private fun isCodingPackage(packageName: String?): Boolean {
        if (packageName.isNullOrEmpty()) return false
        val name = packageName.lowercase()
        return codingPackageHints.any { name.contains(it) }
    }

    /** Manual override from the technical strip toggle (Hybrid mode). */
    fun setTechnicalOverride(enabled: Boolean) {
        if (enabled) {
            _contextMode.value = DrsContextMode.TECHNICAL
        } else {
            _contextMode.value = DrsContextMode.NORMAL
        }
    }
}
