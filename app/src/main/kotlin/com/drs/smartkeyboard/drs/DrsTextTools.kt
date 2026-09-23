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

import java.text.Collator
import java.util.Locale

/**
 * DRS v1.0.6: pure text transformation engine for the technical user system.
 *
 * Every tool operates on a plain [String] and returns a new [String] - no
 * input-connection or UI dependency here, so all transformations are fully
 * unit-testable. The editor glue lives in
 * [com.drs.smartkeyboard.ime.editor.EditorInstance.performTextTool].
 *
 * Design rules:
 *  - Never throws: callers pass arbitrary user text; every function is
 *    written defensively and returns the input unchanged when it cannot be
 *    transformed.
 *  - Never reorders or drops content except where the tool's purpose IS
 *    removal (empty lines, duplicate lines, diacritics, extra spaces).
 *  - Locale-aware casing through [Locale] so Arabic/Turkish/etc. behave
 *    correctly on the user's device.
 */

/** Horizontal whitespace: spaces, tabs and NBSP (never newlines). */
private val HORIZONTAL_SPACES = Regex("[ \\t\\u00A0\\u2007\\u202F]+")

/** Arabic diacritics (harakat), small quranic marks and superscript alef. */
private val ARABIC_DIACRITICS = Regex("[\\u064B-\\u065F\\u0670\\u06D6-\\u06ED]")

/** Punctuation that must never keep a space BEFORE it (Latin + Arabic). */
private val SPACE_BEFORE_PUNCT = Regex("[ \\t\\u00A0]+([,;:.!?،؛…؟)\\]\\}»])")

/**
 * Punctuation that should be followed by one space when the next character is
 * a letter. '.' is intentionally excluded for Latin text (domains, files);
 * it is handled separately for Arabic letters only.
 */
private val SPACE_AFTER_PUNCT = Regex("([,;:!?،؛…])(?=[\\p{IsAlphabetic}])")

/** Arabic letter following a '.' (sentence dot in Arabic text). */
private val DOT_BEFORE_ARABIC = Regex("\\.(?=[\\u0600-\\u06FF])")

enum class DrsTextTool(
    val code: Int,
    /** Info-only tools show a result message instead of modifying text. */
    val isInfoOnly: Boolean = false,
    /** Editor-level tools that need the cursor position, not a transform. */
    val isEditorOp: Boolean = false,
) {
    // ---- Case transformation (applies to selection, else whole field) ----
    UPPERCASE(-601),
    LOWERCASE(-602),
    TITLE_CASE(-603),
    SENTENCE_CASE(-604),

    // ---- Spacing and lines ----
    TRIM_SPACES(-611),
    TRIM_LINE_EDGES(-612),
    REMOVE_EMPTY_LINES(-613),
    REMOVE_LINE_BREAKS(-614),
    SORT_LINES(-615),
    REMOVE_DUPLICATE_LINES(-616),

    // ---- Arabic-specific ----
    REMOVE_DIACRITICS(-621),

    // ---- Punctuation and full clean-up ----
    NORMALIZE_PUNCTUATION(-631),
    CLEAN_TEXT(-632),

    // ---- Info (never modifies text) ----
    COUNT(-641, isInfoOnly = true),

    // ---- Editor-level line operations (cursor-relative) ----
    DELETE_LINE(-651, isEditorOp = true),
    DELETE_TO_LINE_START(-652, isEditorOp = true),
    DELETE_TO_LINE_END(-653, isEditorOp = true);

    companion object {
        /** Inclusive range covering every tool code, for fast dispatch. */
        val CODE_RANGE = -653..-601

        fun fromCode(code: Int): DrsTextTool? = entries.firstOrNull { it.code == code }
    }
}

object DrsTextTools {

    /**
     * Applies [tool] to [text]. Pure function; never throws.
     */
    fun apply(tool: DrsTextTool, text: String, locale: Locale = Locale.getDefault()): String {
        if (text.isEmpty() && tool != DrsTextTool.COUNT) return text
        return try {
            when (tool) {
                DrsTextTool.UPPERCASE -> text.uppercase(locale)
                DrsTextTool.LOWERCASE -> text.lowercase(locale)
                DrsTextTool.TITLE_CASE -> titleCase(text, locale)
                DrsTextTool.SENTENCE_CASE -> sentenceCase(text, locale)
                DrsTextTool.TRIM_SPACES -> collapseHorizontalSpaces(text).trim(' ', '\t', '\u00A0')
                DrsTextTool.TRIM_LINE_EDGES -> text.lines().joinToString("\n") { line ->
                    line.trim(' ', '\t', '\u00A0')
                }
                DrsTextTool.REMOVE_EMPTY_LINES -> text.lines()
                    .filter { it.isNotBlank() }
                    .joinToString("\n")
                DrsTextTool.REMOVE_LINE_BREAKS -> collapseHorizontalSpaces(
                    text.replace(Regex("\\s*\\n+\\s*"), " "),
                )
                DrsTextTool.SORT_LINES -> {
                    val trailingNewline = text.endsWith("\n")
                    val collator = Collator.getInstance(locale)
                    val sorted = text.lines()
                        .let { if (trailingNewline) it.dropLast(1) else it }
                        .sortedWith(compareBy(collator) { it })
                        .joinToString("\n")
                    if (trailingNewline) "$sorted\n" else sorted
                }
                DrsTextTool.REMOVE_DUPLICATE_LINES -> {
                    val seen = HashSet<String>()
                    text.lines()
                        .filter { line ->
                            val key = line.trim(' ', '\t', '\u00A0')
                            seen.add(key)
                        }
                        .joinToString("\n")
                }
                DrsTextTool.REMOVE_DIACRITICS -> text.replace(ARABIC_DIACRITICS, "")
                DrsTextTool.NORMALIZE_PUNCTUATION -> normalizePunctuation(text)
                DrsTextTool.CLEAN_TEXT -> normalizePunctuation(
                    collapseHorizontalSpaces(
                        text.lines()
                            .filter { it.isNotBlank() }
                            .joinToString("\n") { it.trim(' ', '\t', '\u00A0') },
                    ),
                ).trim(' ', '\t', '\u00A0')
                // COUNT never transforms; callers use [countInfo] instead.
                DrsTextTool.COUNT -> text
                // Editor ops need the live cursor; handled by EditorInstance.
                DrsTextTool.DELETE_LINE, DrsTextTool.DELETE_TO_LINE_START,
                DrsTextTool.DELETE_TO_LINE_END,
                -> text
            }
        } catch (_: Throwable) {
            text
        }
    }

    /**
     * Real character/word/line counts for the [DrsTextTool.COUNT] info tool.
     * Words = whitespace-separated tokens; lines = number of lines.
     */
    fun countInfo(text: String): Triple<Int, Int, Int> {
        val chars = text.length
        val words = text.split(Regex("\\s+")).count { it.isNotEmpty() }
        val lines = when {
            text.isEmpty() -> 0
            else -> text.lines().size
        }
        return Triple(chars, words, lines)
    }

    /** Uppercases the first letter of every word (rest of the word kept). */
    private fun titleCase(text: String, locale: Locale): String {
        val sb = StringBuilder(text.length)
        var atWordStart = true
        for (ch in text) {
            if (ch.isLetterOrDigit()) {
                if (atWordStart) {
                    sb.append(ch.uppercase(locale))
                    atWordStart = false
                } else {
                    sb.append(ch)
                }
            } else {
                sb.append(ch)
                if (ch != '-' && ch != '\'' && ch != '\u2019' && ch != '\u00A0') {
                    // Word boundary: anything that is not a word character and
                    // not an intra-word separator (hyphen, apostrophe, NBSP).
                    atWordStart = true
                }
            }
        }
        return sb.toString()
    }

    /**
     * Lowercases the text, then uppercases the first letter after sentence
     * boundaries ('.', '!', '?', '؟', '…', newline) and at the very start.
     */
    private fun sentenceCase(text: String, locale: Locale): String {
        val lower = text.lowercase(locale)
        val sb = StringBuilder(lower.length)
        var capitalizeNext = true
        for (ch in lower) {
            if (capitalizeNext && ch.isLetter()) {
                sb.append(ch.uppercase(locale))
                capitalizeNext = false
            } else {
                sb.append(ch)
                if (ch in ".!?؟…" || ch == '\n') {
                    capitalizeNext = true
                }
            }
        }
        return sb.toString()
    }

    private fun collapseHorizontalSpaces(text: String): String =
        text.replace(HORIZONTAL_SPACES, " ")

    /**
     * Fixes spacing around punctuation in both directions:
     *  - removes spaces before punctuation and closing brackets,
     *  - guarantees exactly one space after sentence punctuation when the
     *    next character is a letter ('.' only for Arabic letters so URLs,
     *    file names and decimals are never broken).
     */
    private fun normalizePunctuation(text: String): String {
        var out = text.replace(SPACE_BEFORE_PUNCT, "$1")
        out = out.replace(SPACE_AFTER_PUNCT, "$1 ")
        out = out.replace(DOT_BEFORE_ARABIC, ". ")
        return out
    }
}
