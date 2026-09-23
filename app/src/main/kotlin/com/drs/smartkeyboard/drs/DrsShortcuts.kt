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

import java.text.DateFormat
import java.util.Date
import java.util.Locale

/**
 * Text expansion engine: an abbreviation committed as the current word is
 * replaced with its full text as soon as the user hits space. All matching is
 * local; nothing is recorded or transmitted.
 *
 * DRS templates: an expansion may contain variables like {date}, {time},
 * {hijri}, {clipboard} or {newline} which are expanded at insertion time
 * (see [expandTemplate]). Unknown variables are kept literally so the user
 * never loses text.
 */
object DrsShortcuts {

    /** Supported template variables, in display order for the editor UI. */
    val TEMPLATE_VARIABLES: List<Pair<String, String>> = listOf(
        "{date}" to "01/01/2030",
        "{time}" to "09:41",
        "{hijri}" to "١٤٥٠ رمضان",
        "{clipboard}" to "...",
        "{newline}" to "",
    )

    private val TEMPLATE_REGEX = Regex("\\{([a-zA-Z_]+)\\}")

    private val HIJRI_MONTHS_AR = listOf(
        "محرم", "صفر", "ربيع الأول", "ربيع الآخر", "جمادى الأولى", "جمادى الآخرة",
        "رجب", "شعبان", "رمضان", "شوال", "ذو القعدة", "ذو الحجة",
    )

    /** Returns true when the given expansion text contains at least one template variable. */
    fun isTemplate(expansion: String): Boolean = TEMPLATE_REGEX.containsMatchIn(expansion)

    /**
     * Expands all supported {variables} in [text]. Never throws - on any
     * failure the original text is returned untouched so typing is safe.
     */
    fun expandTemplate(text: String, clipboardText: String? = null): String {
        if (!isTemplate(text)) return text
        return try {
            TEMPLATE_REGEX.replace(text) { match ->
                when (match.groupValues[1].lowercase(Locale.ROOT)) {
                    "date" -> formatDate()
                    "time" -> formatTime()
                    "hijri" -> formatHijriDate()
                    "clipboard" -> clipboardText.orEmpty()
                    "newline" -> "\n"
                    else -> match.value
                }
            }
        } catch (_: Throwable) {
            text
        }
    }

    private fun formatDate(): String =
        DateFormat.getDateInstance(DateFormat.DEFAULT, Locale.getDefault()).format(Date())

    private fun formatTime(): String =
        DateFormat.getTimeInstance(DateFormat.SHORT, Locale.getDefault()).format(Date())

    /**
     * Hijri (Islamic) date via android.icu, rendered with Arabic month names
     * when the device locale is Arabic. Falls back to an empty string on the
     * rare devices without ICU support so the template still commits.
     */
    private fun formatHijriDate(): String = try {
        val cal = android.icu.util.IslamicCalendar()
        val day = cal.get(android.icu.util.Calendar.DAY_OF_MONTH)
        val month = cal.get(android.icu.util.Calendar.MONTH)
        val year = cal.get(android.icu.util.Calendar.YEAR)
        "$day ${HIJRI_MONTHS_AR[month.coerceIn(0, 11)]} $year هـ"
    } catch (_: Throwable) {
        ""
    }

    /** Looks up an expansion for the given word (case-insensitive, exact). */
    fun findExpansion(word: String): String? {
        if (word.isEmpty()) return null
        val state = DrsStore.state.value
        if (!state.shortcutsEnabled || state.shortcuts.isEmpty()) return null
        val needle = word.lowercase()
        return state.shortcuts.firstOrNull { it.shortcut == needle }?.expansion
    }

    fun add(shortcut: String, expansion: String, isTechnical: Boolean) {
        val trimmedShortcut = shortcut.trim().lowercase()
        val trimmedExpansion = expansion.trim()
        if (trimmedShortcut.isEmpty() || trimmedExpansion.isEmpty()) return
        DrsStore.update { state ->
            val filtered = state.shortcuts.filter { it.shortcut != trimmedShortcut }
            val id = state.nextShortcutId
            state.copy(
                shortcuts = filtered + DrsShortcut(id, trimmedShortcut, trimmedExpansion, isTechnical),
                nextShortcutId = id + 1,
            )
        }
    }

    fun remove(id: Long) {
        DrsStore.update { state ->
            state.copy(shortcuts = state.shortcuts.filter { it.id != id })
        }
    }

    fun setEnabled(enabled: Boolean) {
        DrsStore.update { it.copy(shortcutsEnabled = enabled) }
    }

    /** Removes all shortcuts in one step (used by the shortcuts screen). */
    fun removeAll() {
        DrsStore.update { state ->
            state.copy(shortcuts = emptyList())
        }
    }
}
