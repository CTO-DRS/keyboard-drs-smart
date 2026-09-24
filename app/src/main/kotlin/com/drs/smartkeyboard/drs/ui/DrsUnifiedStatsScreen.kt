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

@file:OptIn(ExperimentalMaterial3Api::class)

package com.drs.smartkeyboard.drs.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.drs.smartkeyboard.R
import com.drs.smartkeyboard.drs.DrsDailyStats
import com.drs.smartkeyboard.drs.DrsDayStats
import com.drs.smartkeyboard.drs.DrsStore
import com.drs.smartkeyboard.drs.DrsUnified
import com.drs.smartkeyboard.lib.compose.DrsScreen
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import org.drs.lib.compose.stringRes

/**
 * DRS v1.0.8 — الإحصاءات اليومية (Unified Daily Usage Statistics).
 *
 * A REAL usage statistics screen: every number comes from the local daily
 * buckets recorded by the adaptation engine while typing (counts only —
 * never text, never leaves the device). The screen shows today, the
 * last-7-days aggregate, the recorded day list, a master on/off switch
 * that really stops the recording, and a local reset action.
 */
@Composable
fun DrsUnifiedStatsScreen() = DrsScreen {
    title = stringRes(R.string.drs__unified__stats_title)
    navigationIconVisible = true
    previewFieldVisible = false

    val drsState by DrsStore.state.collectAsState()
    var showResetDialog by remember { mutableStateOf(false) }

    content {
        // ---------------- recording switch (real) ----------------
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.6f),
            ),
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringRes(R.string.drs__unified__stats_enable_switch),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = stringRes(R.string.drs__unified__stats_enable_summary),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Switch(
                    checked = drsState.dailyStatsEnabled,
                    onCheckedChange = { DrsUnified.setDailyStatsEnabled(it) },
                )
            }
        }

        Text(
            text = stringRes(R.string.drs__unified__stats_privacy_note),
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 2.dp),
        )

        if (!drsState.dailyStatsEnabled) {
            Text(
                text = stringRes(R.string.drs__unified__stats_disabled_hint),
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            )
            return@content
        }

        val today = DrsDailyStats.todayStamp()
        val todayStats = drsState.dailyStats[today] ?: DrsDayStats(day = today)
        val last7 = DrsDailyStats.sum(DrsDailyStats.lastDays(drsState.dailyStats, 7, today))

        // ---------------- today ----------------
        StatsCard(title = stringRes(R.string.drs__unified__stats_today)) {
            StatsRow(stringRes(R.string.drs__unified__stats_keys), todayStats.keyPresses)
            StatsRow(stringRes(R.string.drs__unified__stats_tools), todayStats.toolUses)
            StatsRow(stringRes(R.string.drs__unified__stats_tech_tools), todayStats.techToolUses)
            StatsRow(stringRes(R.string.drs__unified__stats_gestures), todayStats.gestureUses)
            StatsRow(stringRes(R.string.drs__unified__stats_emoji), todayStats.emojiUses)
            StatsRow(stringRes(R.string.drs__unified__stats_clipboard), todayStats.clipboardUses)
            StatsRow(stringRes(R.string.drs__unified__stats_shortcuts), todayStats.shortcutUses)
            if (!dayHasActivity(todayStats)) {
                Text(
                    text = stringRes(R.string.drs__unified__stats_empty),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 6.dp),
                )
            }
        }

        // ---------------- last 7 days ----------------
        StatsCard(title = stringRes(R.string.drs__unified__stats_last7)) {
            StatsRow(stringRes(R.string.drs__unified__stats_keys), last7.keyPresses)
            StatsRow(stringRes(R.string.drs__unified__stats_tools), last7.toolUses)
            StatsRow(stringRes(R.string.drs__unified__stats_gestures), last7.gestureUses)
            StatsRow(stringRes(R.string.drs__unified__stats_emoji), last7.emojiUses)
            StatsRow(stringRes(R.string.drs__unified__stats_clipboard), last7.clipboardUses)
        }

        // ---------------- per-day list ----------------
        val recordedDays = DrsDailyStats.lastDays(drsState.dailyStats, 14, today)
        if (recordedDays.isNotEmpty()) {
            StatsCard(title = stringRes(R.string.drs__unified__stats_days_list)) {
                recordedDays.forEach { bucket ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            text = formatDayLabel(bucket.day),
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = stringRes(
                                R.string.drs__unified__stats_day_line,
                                "keys" to bucket.keyPresses.toString(),
                                "tools" to (bucket.toolUses + bucket.techToolUses).toString(),
                            ),
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }

        // ---------------- local reset ----------------
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
        ) {
            OutlinedButton(
                onClick = { showResetDialog = true },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringRes(R.string.drs__unified__stats_reset))
            }
        }
        Spacer(Modifier.height(8.dp))

        if (showResetDialog) {
            AlertDialog(
                onDismissRequest = { showResetDialog = false },
                title = { Text(stringRes(R.string.drs__unified__stats_reset)) },
                text = { Text(stringRes(R.string.drs__unified__stats_reset_confirm)) },
                confirmButton = {
                    TextButton(
                        onClick = {
                            DrsUnified.resetDailyStats()
                            showResetDialog = false
                        },
                    ) { Text(stringRes(R.string.action__ok)) }
                },
                dismissButton = {
                    TextButton(onClick = { showResetDialog = false }) {
                        Text(stringRes(R.string.action__cancel))
                    }
                },
            )
        }
    }
}

/** ISO day stamp -> localized readable label (weekday + date). */
private fun formatDayLabel(isoDay: String): String {
    return try {
        val date = LocalDate.parse(isoDay)
        date.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM))
    } catch (_: Throwable) {
        isoDay
    }
}

/** True when at least one counter is non-zero (member extension via DrsDailyStats). */
private fun dayHasActivity(bucket: DrsDayStats): Boolean = with(DrsDailyStats) { bucket.hasActivity() }

/** A titled statistics section. */
@Composable
private fun StatsCard(
    title: String,
    content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.6f),
        ),
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(Modifier.height(4.dp))
            content()
        }
    }
}

/** One label:value statistics line with real numbers only. */
@Composable
private fun StatsRow(label: String, value: Long) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(text = label, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
        Text(
            text = value.toString(),
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
