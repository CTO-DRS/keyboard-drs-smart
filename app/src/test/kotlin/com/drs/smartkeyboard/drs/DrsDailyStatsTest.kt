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

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import java.time.LocalDate

/**
 * DRS v1.0.8: unit tests for the pure daily statistics layer — merge and
 * day rollover, retention pruning, sanity validation and the last-days
 * window used by the statistics screen.
 */
class DrsDailyStatsTest : FunSpec({

    fun bucket(day: String, keys: Long = 0, tools: Long = 0): DrsDayStats =
        DrsDayStats(day = day, keyPresses = keys, toolUses = tools)

    // -----------------------------------------------------------
    // mergeInto
    // -----------------------------------------------------------

    test("zero delta never modifies the map") {
        val current = mapOf("2026-09-24" to bucket("2026-09-24", keys = 10))
        val merged = DrsDailyStats.mergeInto(current, DrsUsageStats())
        merged shouldBe current
    }

    test("delta lands in today's bucket and rolls over on day change") {
        val today = DrsDailyStats.todayStamp()
        val current = mapOf("2020-01-01" to bucket("2020-01-01", keys = 5))
        val merged = DrsDailyStats.mergeInto(
            current,
            DrsUsageStats(keyPresses = 7, toolUses = mapOf(-35 to 2L)),
        )
        // today's bucket exists now with the merged counters
        merged[today] shouldBe DrsDayStats(day = today, keyPresses = 7, toolUses = 2)
        // the old day is untouched
        merged["2020-01-01"] shouldBe bucket("2020-01-01", keys = 5)
    }

    test("repeated merges accumulate into the same day bucket") {
        val today = DrsDailyStats.todayStamp()
        var stats = DrsDailyStats.mergeInto(emptyMap(), DrsUsageStats(keyPresses = 10))
        stats = DrsDailyStats.mergeInto(stats, DrsUsageStats(keyPresses = 15, gestureUses = 3))
        stats[today] shouldBe DrsDayStats(day = today, keyPresses = 25, gestureUses = 3)
    }

    // -----------------------------------------------------------
    // prune
    // -----------------------------------------------------------

    test("prune keeps only the newest buckets within the retention window") {
        val stats = (1..DrsDailyStats.KEEP_DAYS + 10).associate { index ->
            val day = LocalDate.of(2026, 1, 1).plusDays((index - 1).toLong()).toString()
            day to bucket(day, keys = index.toLong())
        }
        val pruned = DrsDailyStats.prune(stats)
        pruned.size shouldBe DrsDailyStats.KEEP_DAYS
        // the ten oldest days must be gone; the newest sixty kept
        val oldestDropped = LocalDate.of(2026, 1, 1).plusDays(9L).toString()
        val oldestKept = LocalDate.of(2026, 1, 1).plusDays(10L).toString()
        val newestKept = LocalDate.of(2026, 1, 1).plusDays((DrsDailyStats.KEEP_DAYS + 10 - 1).toLong()).toString()
        pruned.containsKey(oldestDropped) shouldBe false
        pruned.containsKey(oldestKept) shouldBe true
        pruned.containsKey(newestKept) shouldBe true
    }

    test("prune is a no-op below the retention bound") {
        val stats = mapOf(
            "2026-09-01" to bucket("2026-09-01", keys = 1),
            "2026-09-02" to bucket("2026-09-02", keys = 2),
        )
        DrsDailyStats.prune(stats) shouldBe stats
    }

    // -----------------------------------------------------------
    // isSane
    // -----------------------------------------------------------

    test("sane map with valid days passes") {
        val stats = mapOf(
            "2026-09-23" to bucket("2026-09-23", keys = 100, tools = 4),
            "2026-09-24" to bucket("2026-09-24", keys = 50),
        )
        DrsDailyStats.isSane(stats) shouldBe true
    }

    test("invalid day key, day mismatch or negative value fails sanity") {
        DrsDailyStats.isSane(mapOf("24-09-2026" to bucket("24-09-2026"))) shouldBe false
        DrsDailyStats.isSane(mapOf("2026-09-24" to bucket("2026-09-23"))) shouldBe false
        DrsDailyStats.isSane(mapOf("2026-09-24" to bucket("2026-09-24", keys = -1))) shouldBe false
        // oversized map fails too
        val oversized = (1..DrsDailyStats.KEEP_DAYS + 2).associate { index ->
            val day = LocalDate.of(2025, 1, 1).plusDays(index.toLong()).toString()
            day to bucket(day)
        }
        DrsDailyStats.isSane(oversized) shouldBe false
    }

    test("isValidDay rejects garbage and accepts ISO stamps") {
        DrsDailyStats.isValidDay("2026-09-24") shouldBe true
        DrsDailyStats.isValidDay("24/09/2026") shouldBe false
        DrsDailyStats.isValidDay("") shouldBe false
        DrsDailyStats.isValidDay("garbage") shouldBe false
    }

    // -----------------------------------------------------------
    // lastDays + sum
    // -----------------------------------------------------------

    test("lastDays returns only recorded active days, newest first") {
        val today = DrsDailyStats.todayStamp()
        val yesterday = LocalDate.parse(today).minusDays(1).toString()
        val stats = mapOf(
            today to bucket(today, keys = 10, tools = 1),
            yesterday to bucket(yesterday, keys = 20),
        )
        val last7 = DrsDailyStats.lastDays(stats, 7)
        last7.shouldHaveSize(2)
        last7.first().day shouldBe today
        last7.last().day shouldBe yesterday
        // days without buckets are absent, not zero-filled
        DrsDailyStats.lastDays(emptyMap(), 7).shouldBeEmpty()
    }

    test("lastDays ignores all-zero buckets") {
        val today = DrsDailyStats.todayStamp()
        val stats = mapOf(today to bucket(today, keys = 0, tools = 0))
        DrsDailyStats.lastDays(stats, 7).shouldBeEmpty()
    }

    test("sum aggregates counters across buckets") {
        val total = DrsDailyStats.sum(
            listOf(
                bucket("2026-09-22", keys = 100, tools = 2),
                bucket("2026-09-23", keys = 50, tools = 3),
            ),
        )
        total.keyPresses shouldBe 150
        total.toolUses shouldBe 5
    }
})
