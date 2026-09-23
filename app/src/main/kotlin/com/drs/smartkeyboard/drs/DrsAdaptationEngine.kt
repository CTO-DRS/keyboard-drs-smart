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

import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong

/**
 * A single adaptation suggestion computed from local usage statistics.
 * Suggestions are OPTIONAL: the user accepts or dismisses them from the
 * DRS Control Center; settings are never changed automatically.
 */
data class DrsSuggestion(
    val id: String,
    val metric: Long,
)

/**
 * Local, privacy-first adaptation engine. Counts anonymous usage signals
 * (numbers, symbols, clipboard, gestures, tech tools) in memory and flushes
 * aggregated counters to [DrsStore] periodically. No keystroke content is
 * ever stored or transmitted.
 */
object DrsAdaptationEngine {

    private const val FLUSH_INTERVAL = 128L

    private val keyPresses = AtomicLong()
    private val numberPresses = AtomicLong()
    private val symbolPresses = AtomicLong()
    private val emojiUses = AtomicLong()
    private val clipboardUses = AtomicLong()
    private val shortcutUses = AtomicLong()
    private val techToolUses = AtomicLong()
    private val gestureUses = AtomicLong()
    // DRS v1.0.5: per-tool counts for the "most used tools" surface. Only
    // tool KeyCodes are counted (never characters), in-memory, flushed
    // aggregated with the rest of the usage stats.
    private val toolCounts = java.util.concurrent.ConcurrentHashMap<Int, Long>()
    private val toolUseTotal = AtomicLong()
    private val flushing = AtomicBoolean(false)

    /** Cheap per-keystroke recording. Must never block or throw. */
    fun recordKey(code: Int) {
        val state = DrsStore.state.value
        if (!state.adaptationEnabled) return
        keyPresses.incrementAndGet()
        when {
            code in '0'.code..'9'.code -> numberPresses.incrementAndGet()
            code >= 0x1F300 -> emojiUses.incrementAndGet()
            code > 0x20 && !Character.isLetterOrDigit(code) -> symbolPresses.incrementAndGet()
        }
        maybeFlush(keyPresses)
    }

    fun recordClipboardUse() {
        DrsEconomy.recordFeatureUse("earn_feature_clipboard")
        if (DrsStore.state.value.adaptationEnabled) {
            clipboardUses.incrementAndGet()
            maybeFlush(clipboardUses)
        }
    }

    fun recordShortcutUse() {
        DrsEconomy.recordFeatureUse("earn_feature_shortcut")
        if (DrsStore.state.value.adaptationEnabled) {
            shortcutUses.incrementAndGet()
            maybeFlush(shortcutUses)
        }
    }

    fun recordTechToolUse() {
        DrsEconomy.recordFeatureUse("earn_feature_tech_tool")
        if (DrsStore.state.value.adaptationEnabled) {
            techToolUses.incrementAndGet()
            maybeFlush(techToolUses)
        }
    }

    fun recordGestureUse() {
        DrsEconomy.recordFeatureUse("earn_feature_gesture")
        if (DrsStore.state.value.adaptationEnabled) {
            gestureUses.incrementAndGet()
            maybeFlush(gestureUses)
        }
    }

    /**
     * DRS v1.0.5: records one use of a smart tool (quick action) by its
     * KeyCode. Anonymous count only — the tool code tells which button was
     * pressed, never what was typed. Powers the "most used tools" section.
     */
    fun recordToolUse(code: Int) {
        toolCounts.merge(code, 1L, Long::plus)
        if (DrsStore.state.value.adaptationEnabled) {
            maybeFlush(toolUseTotal)
        }
    }

    private fun maybeFlush(counter: AtomicLong) {
        if (counter.get() % FLUSH_INTERVAL != 0L) return
        if (!flushing.compareAndSet(false, true)) return
        DrsStore.update { state ->
            state.copy(usage = state.usage.merge(drain()))
        }
        flushing.set(false)
    }

    private fun drain(): DrsUsageStats {
        val drainedTools: Map<Int, Long> = if (toolCounts.isEmpty()) {
            emptyMap()
        } else {
            val snapshot = HashMap(toolCounts)
            toolCounts.clear()
            snapshot
        }
        return DrsUsageStats(
            keyPresses = keyPresses.getAndSet(0),
            numberPresses = numberPresses.getAndSet(0),
            symbolPresses = symbolPresses.getAndSet(0),
            emojiUses = emojiUses.getAndSet(0),
            clipboardUses = clipboardUses.getAndSet(0),
            shortcutUses = shortcutUses.getAndSet(0),
            techToolUses = techToolUses.getAndSet(0),
            gestureUses = gestureUses.getAndSet(0),
            toolUses = drainedTools,
        )
    }

    /** Flush whatever is pending (called from diagnostics/control center). */
    fun flushNow() {
        DrsStore.update { state -> state.copy(usage = state.usage.merge(drain())) }
    }

    private fun DrsUsageStats.merge(delta: DrsUsageStats): DrsUsageStats {
        val mergedTools = if (delta.toolUses.isEmpty()) {
            toolUses
        } else {
            val merged = HashMap(toolUses)
            for ((code, count) in delta.toolUses) {
                merged[code] = (merged[code] ?: 0L) + count
            }
            // Cap so the stored map stays tiny: keep the 32 most used tools.
            merged.entries
                .sortedByDescending { it.value }
                .take(32)
                .associate { it.toPair() }
        }
        return DrsUsageStats(
            keyPresses = keyPresses + delta.keyPresses,
            numberPresses = numberPresses + delta.numberPresses,
            symbolPresses = symbolPresses + delta.symbolPresses,
            emojiUses = emojiUses + delta.emojiUses,
            clipboardUses = clipboardUses + delta.clipboardUses,
            shortcutUses = shortcutUses + delta.shortcutUses,
            techToolUses = techToolUses + delta.techToolUses,
            gestureUses = gestureUses + delta.gestureUses,
            toolUses = mergedTools,
        )
    }

    /**
     * Computes optional suggestions from usage statistics. Suggestions fire
     * only when a clear preference signal exists AND the corresponding
     * feature is currently disabled.
     */
    fun computeSuggestions(state: DrsState): List<DrsSuggestion> {
        if (!state.adaptationEnabled) return emptyList()
        val usage = state.usage
        val activeProfile = DrsProfileManager.activeProfile(state)
        val suggestions = mutableListOf<DrsSuggestion>()

        if (usage.numberPresses > 60 &&
            usage.numberPresses * 20 > usage.keyPresses &&
            activeProfile?.numberRow != true
        ) {
            suggestions.add(DrsSuggestion(DrsSuggestionIds.NUMBER_ROW, usage.numberPresses))
        }
        if (usage.symbolPresses > 40 &&
            usage.symbolPresses * 20 > usage.keyPresses &&
            activeProfile?.techStripEnabled != true &&
            state.userPath == DrsUserPath.NORMAL.name
        ) {
            suggestions.add(DrsSuggestion(DrsSuggestionIds.TECH_STRIP, usage.symbolPresses))
        }
        if (usage.clipboardUses > 15 && activeProfile?.clipboardHistoryEnabled != true) {
            suggestions.add(DrsSuggestion(DrsSuggestionIds.CLIPBOARD_HISTORY, usage.clipboardUses))
        }
        if (usage.symbolPresses > 120 && state.userPath == DrsUserPath.NORMAL.name) {
            suggestions.add(DrsSuggestion(DrsSuggestionIds.TECHNICAL_PATH, usage.symbolPresses))
        }
        if (usage.keyPresses > 300 && state.shortcuts.isEmpty() && usage.shortcutUses == 0L) {
            suggestions.add(DrsSuggestion(DrsSuggestionIds.SHORTCUTS, usage.keyPresses))
        }
        return suggestions.filter { it.id !in state.dismissedSuggestionIds }
    }

    fun accept(suggestion: DrsSuggestion) {
        when (suggestion.id) {
            DrsSuggestionIds.NUMBER_ROW -> {
                val state = DrsStore.state.value
                DrsProfileManager.activeProfile(state)?.let { profile ->
                    DrsProfileManager.applyProfile(profile.copy(numberRow = true))
                }
            }
            DrsSuggestionIds.TECH_STRIP -> {
                val state = DrsStore.state.value
                DrsProfileManager.activeProfile(state)?.let { profile ->
                    DrsProfileManager.applyProfile(profile.copy(techStripEnabled = true))
                }
            }
            DrsSuggestionIds.CLIPBOARD_HISTORY -> {
                val state = DrsStore.state.value
                DrsProfileManager.activeProfile(state)?.let { profile ->
                    DrsProfileManager.applyProfile(profile.copy(clipboardHistoryEnabled = true))
                }
            }
            DrsSuggestionIds.TECHNICAL_PATH -> setUserPath(DrsUserPath.TECHNICAL)
        }
        dismiss(suggestion.id)
    }

    fun dismiss(suggestionId: String) {
        DrsStore.update { state ->
            state.copy(dismissedSuggestionIds = state.dismissedSuggestionIds + suggestionId)
        }
    }

    /**
     * Changes the active system. Delegates to [DrsProfileManager.applySystem]
     * so the system's own profile (with its own full configuration) is
     * applied and the path is stored in one coherent step, keeping data.
     */
    fun setUserPath(path: DrsUserPath) {
        DrsProfileManager.applySystem(path)
    }
}
