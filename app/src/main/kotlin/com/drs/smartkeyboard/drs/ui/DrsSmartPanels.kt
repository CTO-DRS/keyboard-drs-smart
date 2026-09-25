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

package com.drs.smartkeyboard.drs.ui

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FormatClear
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.drs.smartkeyboard.R
import com.drs.smartkeyboard.app.DrsPreferenceStore
import com.drs.smartkeyboard.drs.DrsHarakat
import com.drs.smartkeyboard.drs.DrsStore
import com.drs.smartkeyboard.drs.HarakaInsertMode
import com.drs.smartkeyboard.drs.HarakatSmartInsert
import com.drs.smartkeyboard.drs.PanelUsageTracker
import com.drs.smartkeyboard.drs.DrsSystems
import com.drs.smartkeyboard.drs.SymbolSmartSuggestor
import com.drs.smartkeyboard.ime.ImeUiMode
import com.drs.smartkeyboard.ime.editor.OperationUnit
import com.drs.smartkeyboard.ime.keyboard.DrsImeSizing
import com.drs.smartkeyboard.ime.text.key.KeyCode
import com.drs.smartkeyboard.ime.text.key.KeyType
import com.drs.smartkeyboard.ime.text.keyboard.TextKeyData
import com.drs.smartkeyboard.ime.theme.DrsImeUi
import com.drs.smartkeyboard.keyboardManager
import com.drs.smartkeyboard.editorInstance
import kotlinx.serialization.json.Json
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import org.drs.jetpref.datastore.model.collectAsState
import org.drs.lib.compose.rippleClickable
import org.drs.lib.compose.stringRes
import org.drs.lib.snygg.ui.SnyggBox
import org.drs.lib.snygg.ui.SnyggColumn
import org.drs.lib.snygg.ui.SnyggIcon
import org.drs.lib.snygg.ui.SnyggIconButton
import org.drs.lib.snygg.ui.SnyggRow
import org.drs.lib.snygg.ui.SnyggText

/**
 * DRS v1.15.0 — الوحات الذكية الثلاث.
 *
 *  - لوحة الحركات (DrsDiacriticsPanel): شبكة الحركات التسع + التطويل،
 *    صف الأكثر استخدامًا، ورقاقات التشكيل المزدوج (شدّة + حركة)، مع
 *    الدمج الذكي: حركة فوق حركة تستبدلها بدل أن تتراكم.
 *  - لوحة الرموز الذكية (DrsSmartSymbolsPanel): صف اقتراحات سياقية يقرأ
 *    النص قبل المؤشر، ثم فئات الرموز الكاملة، وصف الأكثر استخدامًا.
 *  - لوحة الحروف الموسعة (DrsArabicLettersPanel): همزات ومشتقات وحروف
 *    الفارسية/الأردية/الكردية مع صف الأكثر استخدامًا.
 *
 * كل لوحة تُرسل إدخالًا حقيقيًا عبر المحرر نفسه الذي ترسل إليه لوحات
 * المفاتيح، وتتذكر أكثر ما يستخدمه المستخدم محليًا فقط (الوضع الخفي لا
 * يسجّل شيئًا إطلاقًا)، ويمكن تثبيت أيٍّ منها ضمن المهام العشر.
 */

/** The persisted panel-usage namespaces (per panel, local only). */
private const val USAGE_PANEL_HARAKAT = "harakat"
private const val USAGE_PANEL_SYMBOLS = "symbols"
private const val USAGE_PANEL_LETTERS = "letters"

/**
 * Thin SharedPreferences-backed store for the panels' most-used counters.
 * The growth/trim/top logic itself lives in the pure [PanelUsageTracker]
 * so it stays unit-testable; this object only loads and saves the map.
 * Local only — counts of which TILES were pressed, never any text.
 */
object DrsPanelUsageStore {
    private const val PREFS_NAME = "drs_panel_usage"
    private val json = Json { ignoreUnknownKeys = true }
    private val serializer = MapSerializer(String.serializer(), Int.serializer())

    private fun prefs(context: Context) =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun load(context: Context, panel: String): Map<String, Int> {
        return runCatching {
            json.decodeFromString(serializer, prefs(context).getString(panel, "{}") ?: "{}")
        }.getOrDefault(emptyMap())
    }

    fun record(context: Context, panel: String, key: String) {
        val updated = PanelUsageTracker.record(load(context, panel), key)
        prefs(context).edit().putString(panel, json.encodeToString(serializer, updated)).apply()
    }
}

/** Arabic name resource for every harakat tile (grid order). */
private fun harakatNameRes(char: Char): Int = when (char) {
    DrsHarakat.FATHA -> R.string.panel__harakat__name_fatha
    DrsHarakat.DAMMA -> R.string.panel__harakat__name_damma
    DrsHarakat.KASRA -> R.string.panel__harakat__name_kasra
    DrsHarakat.SUKUN -> R.string.panel__harakat__name_sukun
    DrsHarakat.SHADDA -> R.string.panel__harakat__name_shadda
    DrsHarakat.FATHATAN -> R.string.panel__harakat__name_fathatan
    DrsHarakat.DAMMATAN -> R.string.panel__harakat__name_dammatan
    DrsHarakat.KASRATAN -> R.string.panel__harakat__name_kasratan
    DrsHarakat.SUPERSCRIPT_ALEF -> R.string.panel__harakat__name_superscript_alef
    DrsHarakat.TATWEEL -> R.string.panel__harakat__name_tatweel
    else -> R.string.general__empty_string
}

/** The smart symbols panel catalogue (pure data, panel order). */
object DrsSymbolsCatalog {
    val MATH: List<String> = listOf(
        "+", "−", "×", "÷", "=", "≠", "≈", "<", ">", "≤", "≥",
        "±", "√", "∞", "π", "Σ", "∫", "°", "%",
    )
    val CURRENCY: List<String> = listOf(
        "$", "€", "£", "¥", "₽", "₹", "₺", "﷼", "ر.س", "د.إ",
    )
    val ARROWS: List<String> = listOf("←", "→", "↑", "↓", "↔", "⇒", "⇐", "➤")
    val PUNCTUATION: List<String> = listOf(
        "،", "؛", "؟", "…", "—", "–", "«", "»",
        "\"", "'", "•", "@", "#", "&", "*", "~", "^", "_", "|", "/", "\\",
    )
    val BRACKETS: List<String> = listOf("(", ")", "[", "]", "{", "}", "<", ">")
}

/** The extended Arabic letters catalogue (hamza variants + فارسية/أردية/كردية). */
object DrsArabicLettersCatalog {
    val LETTERS: List<String> = listOf(
        "ء", "أ", "إ", "آ", "ٱ", "ؤ", "ئ", "ة", "ى",
        "پ", "چ", "ژ", "گ", "ڤ", "ک", "ی", "ۋ", "ڭ", "ۀ", "ھ",
    )
}

/** The shared panel-switcher chips row (الحركات / الرموز / الحروف). */
@Composable
private fun PanelSwitcherChips(current: ImeUiMode, keyboardManager: com.drs.smartkeyboard.ime.keyboard.KeyboardManager, accent: androidx.compose.ui.graphics.Color) {
    val options = listOf(
        ImeUiMode.DIACRITICS to R.string.panel__switcher_harakat,
        ImeUiMode.SMART_SYMBOLS to R.string.panel__switcher_symbols,
        ImeUiMode.ARABIC_LETTERS to R.string.panel__switcher_letters,
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        options.forEach { (mode, labelRes) ->
            val selected = mode == current
            SnyggText(
                elementName = DrsImeUi.ClipboardSubheader.elementName,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(if (selected) accent.copy(alpha = 0.22f) else accent.copy(alpha = 0.06f))
                    .rippleClickable { keyboardManager.activeState.imeUiMode = mode }
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                text = stringRes(labelRes),
            )
        }
    }
}

/** The shared header of the three smart panels (back + title + actions). */
@Composable
private fun SmartPanelHeader(
    titleRes: Int,
    keyboardManager: com.drs.smartkeyboard.ime.keyboard.KeyboardManager,
    trailing: @Composable () -> Unit = {},
) {
    SnyggRow(
        DrsImeUi.ClipboardHeader.elementName,
        modifier = Modifier
            .fillMaxWidth()
            .height(DrsImeSizing.smartbarHeight),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val sizeModifier = Modifier
            .sizeIn(maxHeight = DrsImeSizing.smartbarHeight)
            .aspectRatio(1f)
        SnyggIconButton(
            elementName = DrsImeUi.ClipboardHeaderButton.elementName,
            onClick = { keyboardManager.activeState.imeUiMode = ImeUiMode.TEXT },
            modifier = sizeModifier,
        ) {
            SnyggIcon(imageVector = Icons.AutoMirrored.Filled.ArrowBack)
        }
        SnyggText(
            elementName = DrsImeUi.ClipboardHeaderText.elementName,
            modifier = Modifier.weight(1f),
            text = stringRes(titleRes),
        )
        trailing()
    }
}

/** One tile of the char grids: the big glyph plus its small local name. */
@Composable
private fun SmartTile(
    glyph: String,
    name: String,
    onApply: () -> Unit,
) {
    SnyggBox(
        elementName = DrsImeUi.ClipboardItem.elementName,
        modifier = Modifier
            .aspectRatio(1.4f)
            .padding(2.dp),
        clickAndSemanticsModifier = Modifier.rippleClickable { onApply() },
    ) {
        Column(
            modifier = Modifier.padding(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            SnyggText(text = glyph)
            SnyggText(
                elementName = DrsImeUi.ClipboardItemDescription.elementName,
                text = name,
            )
        }
    }
}

/** A full-span subheader inside the LazyVerticalGrid of a panel. */
@Composable
private fun gridSubheader(text: String) {
    SnyggText(
        elementName = DrsImeUi.ClipboardSubheader.elementName,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, top = 10.dp, bottom = 2.dp),
        text = text,
    )
}

/**
 * لوحة الحركات — the smart harakat panel. Smart insert: a mark over a
 * mark replaces it (idempotent on itself, shadda+haraka appends), the
 * shadda combos commit two characters in one tap, the recents row leads
 * with what this user actually uses, and the header carries the real
 * remove-diacritics text tool.
 */
@Composable
fun DrsDiacriticsPanel(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val keyboardManager by context.keyboardManager()
    val editorInstance by context.editorInstance()
    val prefs by DrsPreferenceStore

    val smartReplace by prefs.panels.harakatSmartReplace.collectAsState()
    val recentsEnabled by prefs.panels.panelRecents.collectAsState()
    var recents by remember { mutableStateOf(DrsPanelUsageStore.load(context, USAGE_PANEL_HARAKAT)) }

    fun recordUse(key: String) {
        if (keyboardManager.activeState.isIncognitoMode) return
        DrsPanelUsageStore.record(context, USAGE_PANEL_HARAKAT, key)
        recents = DrsPanelUsageStore.load(context, USAGE_PANEL_HARAKAT)
    }

    fun commitText(text: String, usageKey: String) {
        editorInstance.commitText(text)
        recordUse(usageKey)
    }

    fun insertHaraka(haraka: Char) {
        val glyph = haraka.toString()
        val replaced = if (keyboardManager.activeState.isSelectionMode) {
            HarakaInsertMode.APPEND
        } else {
            val previous = editorInstance.run { activeContent.getTextBeforeCursor(1) }.lastOrNull()
            HarakatSmartInsert.decide(previous, haraka, smartReplace)
        }
        if (replaced == HarakaInsertMode.REPLACE_PREVIOUS) {
            editorInstance.deleteBackwards(OperationUnit.CHARACTERS)
        }
        commitText(glyph, glyph)
    }

    fun dispatchRemoveDiacritics() {
        keyboardManager.inputEventDispatcher.sendDownUp(
            TextKeyData(type = KeyType.FUNCTION, code = KeyCode.TEXT_TOOL_REMOVE_DIACRITICS, label = "drs_text_tool"),
        )
    }

    val systemSpec = DrsSystems.specOfName(DrsStore.state.value.userPath)
    val accent = if (isSystemInDarkTheme()) systemSpec.accentNight else systemSpec.accent
    val recentsRow = remember(recents, recentsEnabled) {
        if (recentsEnabled) PanelUsageTracker.topRecents(recents, DrsHarakat.GRID.map { it.toString() }) else emptyList()
    }

    SnyggColumn(
        modifier = modifier
            .fillMaxWidth()
            .height(DrsImeSizing.imeUiHeight()),
    ) {
        SmartPanelHeader(
            titleRes = R.string.panel__harakat__title,
            keyboardManager = keyboardManager,
        ) {
            val sizeModifier = Modifier
                .sizeIn(maxHeight = DrsImeSizing.smartbarHeight)
                .aspectRatio(1f)
            SnyggIconButton(
                elementName = DrsImeUi.ClipboardHeaderButton.elementName,
                onClick = { dispatchRemoveDiacritics() },
                modifier = sizeModifier,
            ) {
                SnyggIcon(imageVector = Icons.Default.FormatClear)
            }
        }
        PanelSwitcherChips(ImeUiMode.DIACRITICS, keyboardManager, accent)

        SnyggBox(DrsImeUi.ClipboardContent.elementName, modifier = Modifier.fillMaxWidth()) {
            LazyVerticalGrid(
                modifier = Modifier.fillMaxWidth(),
                columns = GridCells.Adaptive(DrsImeSizing.smartbarHeight * 1.35f),
            ) {
                if (recentsRow.isNotEmpty()) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        gridSubheader(stringRes(R.string.panel__recents_title))
                    }
                    items(recentsRow, key = { "recent_$it" }) { key ->
                        SmartTile(
                            glyph = key,
                            name = "",
                            onApply = { insertHaraka(key.first()) },
                        )
                    }
                }
                item(span = { GridItemSpan(maxLineSpan) }) {
                    gridSubheader(stringRes(R.string.panel__harakat__combos_title))
                }
                items(DrsHarakat.COMBOS, key = { "combo_$it" }) { combo ->
                    SmartTile(
                        glyph = combo,
                        name = stringRes(R.string.panel__harakat__combo_name),
                        onApply = { commitText(combo, combo) },
                    )
                }
                item(span = { GridItemSpan(maxLineSpan) }) {
                    gridSubheader(stringRes(R.string.panel__harakat__grid_title))
                }
                items(DrsHarakat.GRID, key = { "haraka_$it" }) { haraka ->
                    SmartTile(
                        glyph = haraka.toString(),
                        name = stringRes(harakatNameRes(haraka)),
                        onApply = { insertHaraka(haraka) },
                    )
                }
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Spacer(Modifier.height(12.dp))
                }
            }
        }
    }
}

/**
 * لوحة الرموز الذكية — the context-aware smart symbols panel. The
 * suggestions row recomputes from the text before the cursor after every
 * commit made from inside the panel, then the full catalogue follows by
 * category with the user's recents leading.
 */
@Composable
fun DrsSmartSymbolsPanel(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val keyboardManager by context.keyboardManager()
    val editorInstance by context.editorInstance()
    val prefs by DrsPreferenceStore

    val suggestionsEnabled by prefs.panels.symbolSmartSuggestions.collectAsState()
    val recentsEnabled by prefs.panels.panelRecents.collectAsState()
    var recents by remember { mutableStateOf(DrsPanelUsageStore.load(context, USAGE_PANEL_SYMBOLS)) }
    var commitStamp by remember { mutableIntStateOf(0) }

    fun commitText(text: String) {
        editorInstance.commitText(text)
        if (!keyboardManager.activeState.isIncognitoMode) {
            DrsPanelUsageStore.record(context, USAGE_PANEL_SYMBOLS, text)
            recents = DrsPanelUsageStore.load(context, USAGE_PANEL_SYMBOLS)
        }
        commitStamp++
    }

    val suggestions = remember(suggestionsEnabled, commitStamp) {
        if (suggestionsEnabled) {
            SymbolSmartSuggestor.suggest(editorInstance.run { activeContent.getTextBeforeCursor(8) })
        } else {
            emptyList()
        }
    }
    val recentsRow = remember(recents, recentsEnabled) {
        if (recentsEnabled) {
            PanelUsageTracker.topRecents(recents, DrsSymbolsCatalog.MATH + DrsSymbolsCatalog.CURRENCY +
                DrsSymbolsCatalog.ARROWS + DrsSymbolsCatalog.PUNCTUATION + DrsSymbolsCatalog.BRACKETS)
        } else {
            emptyList()
        }
    }

    val systemSpec = DrsSystems.specOfName(DrsStore.state.value.userPath)
    val accent = if (isSystemInDarkTheme()) systemSpec.accentNight else systemSpec.accent

    SnyggColumn(
        modifier = modifier
            .fillMaxWidth()
            .height(DrsImeSizing.imeUiHeight()),
    ) {
        SmartPanelHeader(R.string.panel__symbols__title, keyboardManager)
        PanelSwitcherChips(ImeUiMode.SMART_SYMBOLS, keyboardManager, accent)

        SnyggBox(DrsImeUi.ClipboardContent.elementName, modifier = Modifier.fillMaxWidth()) {
            LazyVerticalGrid(
                modifier = Modifier.fillMaxWidth(),
                columns = GridCells.Adaptive(DrsImeSizing.smartbarHeight * 1.35f),
            ) {
                if (suggestions.isNotEmpty()) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        gridSubheader(stringRes(R.string.panel__symbols__suggestions_title))
                    }
                    items(suggestions, key = { "sugg_$it" }) { symbol ->
                        SmartTile(
                            glyph = symbol,
                            name = "",
                            onApply = { commitText(symbol) },
                        )
                    }
                }
                if (recentsRow.isNotEmpty()) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        gridSubheader(stringRes(R.string.panel__recents_title))
                    }
                    items(recentsRow, key = { "recent_$it" }) { key ->
                        SmartTile(
                            glyph = key,
                            name = "",
                            onApply = { commitText(key) },
                        )
                    }
                }
                item(span = { GridItemSpan(maxLineSpan) }) {
                    gridSubheader(stringRes(R.string.panel__symbols__math_title))
                }
                items(DrsSymbolsCatalog.MATH, key = { "math_$it" }) { symbol ->
                    SmartTile(glyph = symbol, name = "", onApply = { commitText(symbol) })
                }
                item(span = { GridItemSpan(maxLineSpan) }) {
                    gridSubheader(stringRes(R.string.panel__symbols__currency_title))
                }
                items(DrsSymbolsCatalog.CURRENCY, key = { "cur_$it" }) { symbol ->
                    SmartTile(glyph = symbol, name = "", onApply = { commitText(symbol) })
                }
                item(span = { GridItemSpan(maxLineSpan) }) {
                    gridSubheader(stringRes(R.string.panel__symbols__arrows_title))
                }
                items(DrsSymbolsCatalog.ARROWS, key = { "arrow_$it" }) { symbol ->
                    SmartTile(glyph = symbol, name = "", onApply = { commitText(symbol) })
                }
                item(span = { GridItemSpan(maxLineSpan) }) {
                    gridSubheader(stringRes(R.string.panel__symbols__brackets_title))
                }
                items(DrsSymbolsCatalog.BRACKETS, key = { "brk_$it" }) { symbol ->
                    SmartTile(glyph = symbol, name = "", onApply = { commitText(symbol) })
                }
                item(span = { GridItemSpan(maxLineSpan) }) {
                    gridSubheader(stringRes(R.string.panel__symbols__punct_title))
                }
                items(DrsSymbolsCatalog.PUNCTUATION, key = { "pun_$it" }) { symbol ->
                    SmartTile(glyph = symbol, name = "", onApply = { commitText(symbol) })
                }
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Spacer(Modifier.height(12.dp))
                }
            }
        }
    }
}

/**
 * لوحة الحروف الموسعة — the extended Arabic letters panel: the hamza
 * variants and the Persian/Urdu/Kurdish letters the base layout has no
 * room for, with the user's most-used letters leading the grid.
 */
@Composable
fun DrsArabicLettersPanel(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val keyboardManager by context.keyboardManager()
    val editorInstance by context.editorInstance()
    val prefs by DrsPreferenceStore

    val recentsEnabled by prefs.panels.panelRecents.collectAsState()
    var recents by remember { mutableStateOf(DrsPanelUsageStore.load(context, USAGE_PANEL_LETTERS)) }

    fun commitText(text: String) {
        editorInstance.commitText(text)
        if (!keyboardManager.activeState.isIncognitoMode) {
            DrsPanelUsageStore.record(context, USAGE_PANEL_LETTERS, text)
            recents = DrsPanelUsageStore.load(context, USAGE_PANEL_LETTERS)
        }
    }

    val recentsRow = remember(recents, recentsEnabled) {
        if (recentsEnabled) PanelUsageTracker.topRecents(recents, DrsArabicLettersCatalog.LETTERS) else emptyList()
    }

    val systemSpec = DrsSystems.specOfName(DrsStore.state.value.userPath)
    val accent = if (isSystemInDarkTheme()) systemSpec.accentNight else systemSpec.accent

    SnyggColumn(
        modifier = modifier
            .fillMaxWidth()
            .height(DrsImeSizing.imeUiHeight()),
    ) {
        SmartPanelHeader(R.string.panel__letters__title, keyboardManager)
        PanelSwitcherChips(ImeUiMode.ARABIC_LETTERS, keyboardManager, accent)

        SnyggBox(DrsImeUi.ClipboardContent.elementName, modifier = Modifier.fillMaxWidth()) {
            LazyVerticalGrid(
                modifier = Modifier.fillMaxWidth(),
                columns = GridCells.Adaptive(DrsImeSizing.smartbarHeight * 1.35f),
            ) {
                if (recentsRow.isNotEmpty()) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        gridSubheader(stringRes(R.string.panel__recents_title))
                    }
                    items(recentsRow, key = { "recent_$it" }) { key ->
                        SmartTile(glyph = key, name = "", onApply = { commitText(key) })
                    }
                }
                item(span = { GridItemSpan(maxLineSpan) }) {
                    gridSubheader(stringRes(R.string.panel__letters__grid_title))
                }
                items(DrsArabicLettersCatalog.LETTERS, key = { "letter_$it" }) { letter ->
                    SmartTile(glyph = letter, name = "", onApply = { commitText(letter) })
                }
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Spacer(Modifier.height(12.dp))
                }
            }
        }
    }
}
