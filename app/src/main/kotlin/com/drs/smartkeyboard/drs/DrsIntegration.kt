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

import com.drs.smartkeyboard.ime.editor.EditorInstance
import com.drs.smartkeyboard.ime.keyboard.KeyboardState
import com.drs.smartkeyboard.ime.text.key.KeyCode

/**
 * Thin integration glue between the DRS adaptive layer and the input
 * pipeline. Called from KeyboardManager.handleSpace() BEFORE any other space
 * handling. Returns true when a shortcut expansion consumed the space press.
 *
 * Safety guards mirror the codebase's own incognito/password rules:
 * expansion is skipped for password fields, incognito mode and raw editors.
 */
object DrsIntegration {

    fun handleSpaceShortcut(
        editorInstance: EditorInstance,
        state: KeyboardState,
        clipboardText: String? = null,
    ): Boolean {
        try {
            if (!state.isComposingEnabled) return false
            if (state.keyVariation == com.drs.smartkeyboard.ime.text.key.KeyVariation.PASSWORD) return false
            if (state.isIncognitoMode) return false
            val word = editorInstance.activeContent.currentWordText
            if (word.isEmpty() || word.length > 32) return false
            val rawExpansion = DrsShortcuts.findExpansion(word) ?: return false
            val placesCursor = DrsShortcuts.hasCursorMarker(rawExpansion)
            val expansion = DrsShortcuts.expandTemplate(rawExpansion, clipboardText)
            // DRS v1.0.6: a {cursor} marker inside the template moves the
            // cursor there after insertion instead of appending a trailing
            // space - essential for letter templates with a fill-in gap.
            val (finalText, cursorOffset) = if (placesCursor) {
                DrsShortcuts.extractCursorMarker(expansion)
            } else {
                expansion to -1
            }
            repeat(word.length) {
                editorInstance.deleteBackwards(com.drs.smartkeyboard.ime.editor.OperationUnit.CHARACTERS)
            }
            editorInstance.commitText(finalText)
            if (placesCursor && cursorOffset >= 0) {
                val selectionEnd = editorInstance.activeContent.selection.end
                val target = (selectionEnd - finalText.length + cursorOffset).coerceAtLeast(0)
                editorInstance.setSelection(target, target)
            } else {
                editorInstance.commitText(KeyCode.SPACE.toChar().toString())
            }
            DrsAdaptationEngine.recordShortcutUse()
            return true
        } catch (t: Throwable) {
            // The DRS layer must never break normal typing.
            runCatching {
                DrsEventLog.recordError(
                    DrsEventLog.Categories.SHORTCUTS,
                    DrsEventLog.throwableDetail(t),
                )
            }
            return false
        }
    }
}
