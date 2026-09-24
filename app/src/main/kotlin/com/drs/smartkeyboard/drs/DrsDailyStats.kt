/*
 * Copyright (C) 2025-2026 The DRS Smart Keyboard Project
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

import java.time.LocalDate

/**
 * DRS v1.0.8: pure logic of the daily usage statistics — day buckets,
 * merge/rollover, retention pruning and sanity validation. Everything here
 * is side-effect free and JVM-testable; the Android layers (adaptation
 * engine, stats screen, diagnostics) only call these helpers.
 *
 * Privacy: the buckets hold COUNTS only (how many keys, tools, gestures…),
 * never text, never timestamps of individual keystrokes, and the map never
 * leaves the device.
 */
object DrsDailyStats {

    /** How many day buckets are retained (oldest dropped on flush). */
    const val KEEP_DAYS = 60

    /** ISO day stamp of "now", e.g. "2026-09-24". */
    fun todayStamp(): String = LocalDate.now().toString()

    /** True when [day] is a valid ISO day stamp (yyyy-MM-dd). */
    fun isValidDay(day: String): Boolean {
        return try {
            LocalDate.parse(day).toString() == day
        } catch (_: Throwable) {
            false
        }
    }

    /** True when the delta adds nothing (avoids rewriting the state file for no reason). */
    private fun DrsUsageStats.isZero(): Boolean =
        keyPresses == 0L && emojiUses == 0L && clipboardUses == 0L &&
            shortcutUses == 0L && techToolUses == 0L && gestureUses == 0L &&
            toolUses.isEmpty()

    /**
     * Merges [delta] (the drained adaptation counters) into [current]'s
     * TODAY bucket, performing the day rollover implicitly (a new day gets
     * a fresh bucket) and pruning buckets older than [KEEP_DAYS]. The
     * per-tool map of the delta is summed into the day's tool press count —
     * the drained map is uncapped, so the sum is exact for the window.
     */
    fun mergeInto(current: Map<String, DrsDayStats>, delta: DrsUsageStats): Map<String, DrsDayStats> {
        if (delta.isZero()) return current
        val today = todayStamp()
        val existing = current[today] ?: DrsDayStats(day = today)
        val merged = existing.copy(
            keyPresses = existing.keyPresses + delta.keyPresses,
            toolUses = existing.toolUses + delta.toolUses.values.sum(),
            techToolUses = existing.techToolUses + delta.techToolUses,
            gestureUses = existing.gestureUses + delta.gestureUses,
            emojiUses = existing.emojiUses + delta.emojiUses,
            clipboardUses = existing.clipboardUses + delta.clipboardUses,
            shortcutUses = existing.shortcutUses + delta.shortcutUses,
        )
        return prune(current + (today to merged))
    }

    /**
     * Keeps only the newest [keepDays] buckets. ISO day stamps sort
     * lexicographically == chronologically, so ordering is trivial and
     * stable.
     */
    fun prune(stats: Map<String, DrsDayStats>, keepDays: Int = KEEP_DAYS): Map<String, DrsDayStats> {
        if (stats.size <= keepDays) return stats
        return stats.entries
            .sortedByDescending { it.key }
            .take(keepDays)
            .associate { it.toPair() }
    }

    /**
     * Sanity of a persisted map, used by the diagnostics runner: every key
     * must be a valid ISO day, the bucket's own day field must match its
     * key, all counters non-negative, and the size within the retention
     * bound (+1 tolerance for a state written by a slightly newer build).
     */
    fun isSane(stats: Map<String, DrsDayStats>): Boolean {
        if (stats.size > KEEP_DAYS + 1) return false
        return stats.all { (day, bucket) ->
            isValidDay(day) &&
                bucket.day == day &&
                bucket.keyPresses >= 0 &&
                bucket.toolUses >= 0 &&
                bucket.techToolUses >= 0 &&
                bucket.gestureUses >= 0 &&
                bucket.emojiUses >= 0 &&
                bucket.clipboardUses >= 0 &&
                bucket.shortcutUses >= 0
        }
    }

    /** Sum of several buckets (used by the last-7-days aggregate row). */
    fun sum(buckets: Collection<DrsDayStats>): DrsDayStats {
        return buckets.fold(DrsDayStats()) { acc, bucket ->
            acc.copy(
                keyPresses = acc.keyPresses + bucket.keyPresses,
                toolUses = acc.toolUses + bucket.toolUses,
                techToolUses = acc.techToolUses + bucket.techToolUses,
                gestureUses = acc.gestureUses + bucket.gestureUses,
                emojiUses = acc.emojiUses + bucket.emojiUses,
                clipboardUses = acc.clipboardUses + bucket.clipboardUses,
                shortcutUses = acc.shortcutUses + bucket.shortcutUses,
            )
        }
    }

    /**
     * The buckets of the last [days] days (today included), newest first.
     * Days without data are simply absent from the result — the UI shows
     * the real recorded days only.
     */
    fun lastDays(stats: Map<String, DrsDayStats>, days: Int, today: String = todayStamp()): List<DrsDayStats> {
        if (days <= 0) return emptyList()
        val todayDate = try {
            LocalDate.parse(today)
        } catch (_: Throwable) {
            return emptyList()
        }
        return (0 until days)
            .map { todayDate.minusDays(it.toLong()).toString() }
            .mapNotNull { stamp -> stats[stamp]?.takeIf { bucket -> bucket.hasActivity() } }
    }

    /**
     * DRS v1.1.0: a continuous, ASCENDING (oldest -> today) window of the
     * last [days] day buckets with every missing day zero-filled — the
     * exact shape the bar chart and the CSV export need. Returns exactly
     * [days] buckets when [today] parses (oldest first, today last).
     */
    fun lastDaysZeroFilled(
        stats: Map<String, DrsDayStats>,
        days: Int,
        today: String = todayStamp(),
    ): List<DrsDayStats> {
        if (days <= 0) return emptyList()
        val todayDate = try {
            LocalDate.parse(today)
        } catch (_: Throwable) {
            return emptyList()
        }
        return ((days - 1) downTo 0)
            .map { offset -> todayDate.minusDays(offset.toLong()).toString() }
            .map { stamp -> stats[stamp] ?: DrsDayStats(day = stamp) }
    }

    /** True when at least one counter of the bucket is non-zero. */
    fun DrsDayStats.hasActivity(): Boolean =
        keyPresses > 0L || toolUses > 0L || techToolUses > 0L || gestureUses > 0L ||
            emojiUses > 0L || clipboardUses > 0L || shortcutUses > 0L
}
