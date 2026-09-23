/*
 * Copyright (C) 2021-2025 The DRS Smart Keyboard Project
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

package com.drs.smartkeyboard.app.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Gesture
import androidx.compose.material.icons.filled.Healing
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SentimentSatisfiedAlt
import androidx.compose.material.icons.filled.SmartButton
import androidx.compose.material.icons.filled.Spellcheck
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Keyboard
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.drs.smartkeyboard.R
import com.drs.smartkeyboard.app.Routes
import org.drs.lib.compose.stringRes
import java.text.Normalizer

/**
 * DRS smart search: an advanced, Arabic-aware settings search engine.
 *
 * The query is normalized (diacritics stripped, hamza/alef forms unified,
 * taa marbuta and alef maqsura folded) so that typing "سمات", "السمة" or
 * "ثيم" all find the theme screen. Results navigate directly to the target
 * screen — one tap from thought to setting.
 */
data class DrsSearchEntry(
    val title: String,
    val keywords: List<String>,
    val icon: ImageVector,
    val route: Any,
)

private val drsMnRegex = Regex("\\p{Mn}+")
private val drsNonLetterRegex = Regex("[^\\p{L}\\p{Nd}\\s]")

/** Normalize an Arabic/Latin query for forgiving, smart matching. */
fun drsSearchNormalize(raw: String): String {
    val decomposed = Normalizer.normalize(raw.lowercase(), Normalizer.Form.NFD)
    return decomposed
        .replace(drsMnRegex, "")
        .replace('أ', 'ا')
        .replace('إ', 'ا')
        .replace('آ', 'ا')
        .replace('ة', 'ه')
        .replace('ى', 'ي')
        .replace(drsNonLetterRegex, " ")
        .replace(Regex("\\s+"), " ")
        .trim()
}

private fun DrsSearchEntry.matches(normalizedQuery: String): Boolean {
    if (normalizedQuery.isEmpty()) return false
    if (drsSearchNormalize(title).contains(normalizedQuery)) return true
    return keywords.any { drsSearchNormalize(it).contains(normalizedQuery) }
}

/** The full search index of the settings app (titles resolved for the current locale). */
@Composable
private fun rememberDrsSearchEntries(): List<DrsSearchEntry> {
    return listOf(
        DrsSearchEntry(
            title = stringRes(R.string.drs__control_center__title),
            keywords = listOf("control center", "مركز", "تحكم", "شامل", "كل شيء"),
            icon = Icons.Default.Dashboard,
            route = Routes.Settings.DrsControlCenter,
        ),
        DrsSearchEntry(
            title = stringRes(R.string.drs__shortcuts__title),
            keywords = listOf("shortcuts", "templates", "اختصار", "اختصارات", "قالب", "قوالب", "سريع"),
            icon = Icons.AutoMirrored.Filled.Assignment,
            route = Routes.Settings.DrsShortcuts,
        ),
        DrsSearchEntry(
            title = stringRes(R.string.drs__profiles__title),
            keywords = listOf("profile", "profiles", "بروفايل", "بروفيل", "ملف شخصي", "ملفات", "حساب"),
            icon = Icons.Default.People,
            route = Routes.Settings.DrsProfiles,
        ),
        DrsSearchEntry(
            title = stringRes(R.string.drs__gestures__title),
            keywords = listOf("drs gestures", "حركة", "حركات", "لمسة", "لمس ذكي"),
            icon = Icons.Default.TouchApp,
            route = Routes.Settings.DrsGestures,
        ),
        DrsSearchEntry(
            title = stringRes(R.string.drs__diagnostics__title),
            keywords = listOf("diagnostics", "تشخيص", "تشخيصات", "فحص", "مشاكل", "مشكلة", "إصلاح", "صيانة"),
            icon = Icons.Default.Healing,
            route = Routes.Settings.DrsDiagnostics,
        ),
        DrsSearchEntry(
            title = stringRes(R.string.settings__localization__title),
            keywords = listOf("languages", "layouts", "لغة", "لغات", "تخطيط", "تخطيطات", "عربي", "إنجليزي", "لوحات"),
            icon = Icons.Default.Language,
            route = Routes.Settings.Localization,
        ),
        DrsSearchEntry(
            title = stringRes(R.string.settings__theme__title),
            keywords = listOf("themes", "appearance", "theme", "سمة", "سمات", "ثيم", "لون", "ألوان", "مظهر", "شكل"),
            icon = Icons.Outlined.Palette,
            route = Routes.Settings.Theme,
        ),
        DrsSearchEntry(
            title = stringRes(R.string.settings__keyboard__title),
            keywords = listOf("keyboard", "لوحة المفاتيح", "ارتفاع", "أصوات", "صوت", "اهتزاز", "طول"),
            icon = Icons.Outlined.Keyboard,
            route = Routes.Settings.Keyboard,
        ),
        DrsSearchEntry(
            title = stringRes(R.string.settings__smartbar__title),
            keywords = listOf("smartbar", "toolbar", "الشريط الذكي", "شريط", "أدوات", "صف الأدوات"),
            icon = Icons.Default.SmartButton,
            route = Routes.Settings.Smartbar,
        ),
        DrsSearchEntry(
            title = stringRes(R.string.settings__typing__title),
            keywords = listOf("typing", "correction", "كتابة", "تصحيح", "اقتراح", "اقتراحات", "تلميح", "إملائي"),
            icon = Icons.Default.Spellcheck,
            route = Routes.Settings.Typing,
        ),
        DrsSearchEntry(
            title = stringRes(R.string.settings__gestures__title),
            keywords = listOf("gestures", "swipe", "إيماءة", "إيماءات", "سحب", "تمرير"),
            icon = Icons.Default.Gesture,
            route = Routes.Settings.Gestures,
        ),
        DrsSearchEntry(
            title = stringRes(R.string.settings__clipboard__title),
            keywords = listOf("clipboard", "paste", "حافظة", "لصق", "نسخ"),
            icon = Icons.Default.ContentPaste,
            route = Routes.Settings.Clipboard,
        ),
        DrsSearchEntry(
            title = stringRes(R.string.settings__media__title),
            keywords = listOf("media", "emoji", "emoticon", "إيموجي", "وسائط", "رموز", "صور"),
            icon = Icons.Default.SentimentSatisfiedAlt,
            route = Routes.Settings.Media,
        ),
        DrsSearchEntry(
            title = stringRes(R.string.ext__home__title),
            keywords = listOf("extensions", "addons", "packages", "store", "ملحقات", "إضافات", "حزم", "إضافة", "متجر", "ثيمات"),
            icon = Icons.Default.Extension,
            route = Routes.Ext.Home,
        ),
        DrsSearchEntry(
            title = stringRes(R.string.accessories__title),
            keywords = listOf("accessories", "modules", "modules", "glide", "clipboard", "ملحقات ذكية", "موديول", "انزلاق", "حافظة"),
            icon = Icons.Default.SmartButton,
            route = Routes.Ext.Accessories,
        ),
        DrsSearchEntry(
            title = stringRes(R.string.settings__other__title),
            keywords = listOf("other", "advanced", "backup", "أخرى", "أخرى متقدمة", "متقدم", "احتياطي", "نسخ احتياطي"),
            icon = Icons.Default.Build,
            route = Routes.Settings.Other,
        ),
        DrsSearchEntry(
            title = stringRes(R.string.about__title),
            keywords = listOf("about", "version", "حول", "نسخة", "إصدار", "معلومات", "عن التطبيق"),
            icon = Icons.Outlined.Info,
            route = Routes.Settings.About,
        ),
    )
}

/** Quick suggestions shown before the user types anything. */
private val drsSuggestionIndices = listOf(0, 2, 6, 5, 1, 4)

@Composable
private fun DrsSearchResultRow(entry: DrsSearchEntry, onNavigate: (Any) -> Unit) {
    val colorScheme = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable { onNavigate(entry.route) }
            .padding(horizontal = 8.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(colorScheme.primary.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = entry.icon,
                contentDescription = null,
                tint = colorScheme.primary,
                modifier = Modifier.size(20.dp),
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = entry.title,
            style = MaterialTheme.typography.bodyLarge,
            color = colorScheme.onSurface,
            maxLines = 1,
            modifier = Modifier.weight(1f),
        )
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.size(18.dp),
        )
    }
}

/**
 * DRS smart search popup: a floating, elevated dialog with an auto-focused
 * search field, Arabic-aware live filtering and one-tap navigation to any
 * settings screen. Shows quick suggestions when the query is empty.
 */
@Composable
fun DrsSmartSearchDialog(
    onDismiss: () -> Unit,
    onNavigate: (Any) -> Unit,
) {
    val colorScheme = MaterialTheme.colorScheme
    var query by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }
    val entries = rememberDrsSearchEntries()

    val normalizedQuery = drsSearchNormalize(query)
    val results = if (normalizedQuery.isEmpty()) {
        emptyList()
    } else {
        entries.filter { it.matches(normalizedQuery) }
    }
    val suggestions = drsSuggestionIndices.mapNotNull { entries.getOrNull(it) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            modifier = Modifier
                .padding(horizontal = 20.dp, vertical = 64.dp)
                .fillMaxWidth()
                .heightIn(max = 640.dp),
            shape = RoundedCornerShape(26.dp),
            color = colorScheme.surface,
            tonalElevation = 8.dp,
            border = BorderStroke(1.dp, colorScheme.outlineVariant.copy(alpha = 0.5f)),
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = {
                        Text(text = stringRes(R.string.drs__home__search_hint))
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = colorScheme.primary,
                        )
                    },
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = colorScheme.tertiary,
                            modifier = Modifier.size(18.dp),
                        )
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(22.dp),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(
                        onSearch = {
                            results.firstOrNull()?.let { onNavigate(it.route) }
                        },
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester),
                )
                LaunchedEffect(Unit) {
                    kotlinx.coroutines.delay(120)
                    focusRequester.requestFocus()
                }
                Spacer(modifier = Modifier.padding(top = 12.dp))

                if (normalizedQuery.isEmpty()) {
                    Text(
                        text = stringRes(R.string.drs__home__search_suggestions),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.primary,
                        modifier = Modifier.padding(start = 8.dp, bottom = 4.dp),
                    )
                    LazyColumn(modifier = Modifier.fillMaxWidth()) {
                        items(suggestions) { entry ->
                            DrsSearchResultRow(entry = entry, onNavigate = onNavigate)
                        }
                    }
                } else if (results.isEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier.size(36.dp),
                        )
                        Spacer(modifier = Modifier.padding(top = 12.dp))
                        Text(
                            text = stringRes(R.string.drs__home__search_no_results),
                            style = MaterialTheme.typography.bodyMedium,
                            color = colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                        )
                    }
                } else {
                    LazyColumn(modifier = Modifier.fillMaxWidth()) {
                        items(results) { entry ->
                            DrsSearchResultRow(entry = entry, onNavigate = onNavigate)
                        }
                    }
                }
            }
        }
    }
}
