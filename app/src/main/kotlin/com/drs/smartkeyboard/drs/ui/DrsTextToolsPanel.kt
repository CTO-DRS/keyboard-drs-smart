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

package com.drs.smartkeyboard.drs.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Compress
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.FirstPage
import androidx.compose.material.icons.filled.FormatClear
import androidx.compose.material.icons.filled.FormatLineSpacing
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.HighlightOff
import androidx.compose.material.icons.filled.KeyboardTab
import androidx.compose.material.icons.filled.LastPage
import androidx.compose.material.icons.filled.Rule
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.ShortText
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.SortByAlpha
import androidx.compose.material.icons.filled.SpaceBar
import androidx.compose.material.icons.filled.SubdirectoryArrowRight
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.TextIncrease
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.WrapText
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.drs.smartkeyboard.R
import com.drs.smartkeyboard.drs.DrsAdaptationEngine
import com.drs.smartkeyboard.drs.DrsTextTool
import com.drs.smartkeyboard.ime.ImeUiMode
import com.drs.smartkeyboard.ime.keyboard.DrsImeSizing
import com.drs.smartkeyboard.ime.text.key.KeyCode
import com.drs.smartkeyboard.ime.text.key.KeyType
import com.drs.smartkeyboard.ime.text.keyboard.TextKeyData
import com.drs.smartkeyboard.ime.theme.DrsImeUi
import com.drs.smartkeyboard.keyboardManager
import org.drs.lib.compose.rippleClickable
import org.drs.lib.compose.stringRes
import org.drs.lib.snygg.ui.SnyggBox
import org.drs.lib.snygg.ui.SnyggColumn
import org.drs.lib.snygg.ui.SnyggIcon
import org.drs.lib.snygg.ui.SnyggIconButton
import org.drs.lib.snygg.ui.SnyggRow
import org.drs.lib.snygg.ui.SnyggText

/**
 * One actionable row of the text tools panel. [code] is either a real
 * [KeyCode] (quick editor actions like undo/copy) or a [DrsTextTool] code,
 * both dispatched through the standard input event pipeline so feedback,
 * adaptation stats and the economy hooks all apply.
 */
private data class DrsTextToolsPanelItem(
    val code: Int,
    val labelRes: Int,
    val descRes: Int,
    val icon: ImageVector,
)

private data class DrsTextToolsPanelSection(
    val titleRes: Int,
    val items: List<DrsTextToolsPanelItem>,
)

/** Builds a panel item for a [DrsTextTool] from its string resources. */
private fun toolItem(tool: DrsTextTool, labelRes: Int, descRes: Int, icon: ImageVector) =
    DrsTextToolsPanelItem(code = tool.code, labelRes = labelRes, descRes = descRes, icon = icon)

private val PANEL_SECTIONS: List<DrsTextToolsPanelSection> = listOf(
    DrsTextToolsPanelSection(
        titleRes = R.string.drs__text_tools__section_case,
        items = listOf(
            toolItem(
                DrsTextTool.UPPERCASE,
                R.string.drs__text_tools__tool_uppercase,
                R.string.drs__text_tools__desc_uppercase,
                Icons.Default.FormatSize,
            ),
            toolItem(
                DrsTextTool.LOWERCASE,
                R.string.drs__text_tools__tool_lowercase,
                R.string.drs__text_tools__desc_lowercase,
                Icons.Default.TextFields,
            ),
            toolItem(
                DrsTextTool.TITLE_CASE,
                R.string.drs__text_tools__tool_title_case,
                R.string.drs__text_tools__desc_title_case,
                Icons.Default.TextIncrease,
            ),
            toolItem(
                DrsTextTool.SENTENCE_CASE,
                R.string.drs__text_tools__tool_sentence_case,
                R.string.drs__text_tools__desc_sentence_case,
                Icons.Default.ShortText,
            ),
            // DRS v1.5.0: inverts the case of every cased letter.
            toolItem(
                DrsTextTool.TOGGLE_CASE,
                R.string.drs__text_tools__tool_toggle_case,
                R.string.drs__text_tools__desc_toggle_case,
                Icons.Default.SwapVert,
            ),
        ),
    ),
    DrsTextToolsPanelSection(
        titleRes = R.string.drs__text_tools__section_spaces,
        items = listOf(
            toolItem(
                DrsTextTool.TRIM_SPACES,
                R.string.drs__text_tools__tool_trim_spaces,
                R.string.drs__text_tools__desc_trim_spaces,
                Icons.Default.SpaceBar,
            ),
            toolItem(
                DrsTextTool.TRIM_LINE_EDGES,
                R.string.drs__text_tools__tool_trim_line_edges,
                R.string.drs__text_tools__desc_trim_line_edges,
                Icons.Default.ClearAll,
            ),
            // DRS v1.5.0: whitespace family additions.
            toolItem(
                DrsTextTool.TABS_TO_SPACES,
                R.string.drs__text_tools__tool_tabs_to_spaces,
                R.string.drs__text_tools__desc_tabs_to_spaces,
                Icons.Default.KeyboardTab,
            ),
            toolItem(
                DrsTextTool.REMOVE_ZERO_WIDTH,
                R.string.drs__text_tools__tool_remove_zero_width,
                R.string.drs__text_tools__desc_remove_zero_width,
                Icons.Default.VisibilityOff,
            ),
            toolItem(
                DrsTextTool.REMOVE_EMPTY_LINES,
                R.string.drs__text_tools__tool_remove_empty_lines,
                R.string.drs__text_tools__desc_remove_empty_lines,
                Icons.Default.FormatLineSpacing,
            ),
            // DRS v1.3.0: collapse runs of blank lines into one.
            toolItem(
                DrsTextTool.COLLAPSE_EMPTY_LINES,
                R.string.drs__text_tools__tool_collapse_empty_lines,
                R.string.drs__text_tools__desc_collapse_empty_lines,
                Icons.Default.Compress,
            ),
            toolItem(
                DrsTextTool.REMOVE_LINE_BREAKS,
                R.string.drs__text_tools__tool_remove_line_breaks,
                R.string.drs__text_tools__desc_remove_line_breaks,
                Icons.Default.SubdirectoryArrowRight,
            ),
            toolItem(
                DrsTextTool.SORT_LINES,
                R.string.drs__text_tools__tool_sort_lines,
                R.string.drs__text_tools__desc_sort_lines,
                Icons.Default.Sort,
            ),
            toolItem(
                DrsTextTool.REMOVE_DUPLICATE_LINES,
                R.string.drs__text_tools__tool_remove_duplicate_lines,
                R.string.drs__text_tools__desc_remove_duplicate_lines,
                Icons.Default.FilterList,
            ),
            // DRS v1.1.0: line-ordering additions.
            toolItem(
                DrsTextTool.NUMBER_LINES,
                R.string.drs__text_tools__tool_number_lines,
                R.string.drs__text_tools__desc_number_lines,
                Icons.Default.FormatListNumbered,
            ),
            toolItem(
                DrsTextTool.REVERSE_LINES,
                R.string.drs__text_tools__tool_reverse_lines,
                R.string.drs__text_tools__desc_reverse_lines,
                Icons.Default.SwapVert,
            ),
            // DRS v1.4.0: descending counterpart of SORT_LINES.
            toolItem(
                DrsTextTool.SORT_LINES_DESC,
                R.string.drs__text_tools__tool_sort_lines_desc,
                R.string.drs__text_tools__desc_sort_lines_desc,
                Icons.Default.SortByAlpha,
            ),
            // DRS v1.5.0: stable sort by line length.
            toolItem(
                DrsTextTool.SORT_LINES_BY_LENGTH,
                R.string.drs__text_tools__tool_sort_lines_by_length,
                R.string.drs__text_tools__desc_sort_lines_by_length,
                Icons.Default.FormatLineSpacing,
            ),
            // DRS v1.5.0: word-level dedup (first occurrence wins).
            toolItem(
                DrsTextTool.REMOVE_DUPLICATE_WORDS,
                R.string.drs__text_tools__tool_remove_duplicate_words,
                R.string.drs__text_tools__desc_remove_duplicate_words,
                Icons.Default.WrapText,
            ),
        ),
    ),
    DrsTextToolsPanelSection(
        titleRes = R.string.drs__text_tools__section_arabic,
        items = listOf(
            toolItem(
                DrsTextTool.REMOVE_DIACRITICS,
                R.string.drs__text_tools__tool_remove_diacritics,
                R.string.drs__text_tools__desc_remove_diacritics,
                Icons.Default.FormatClear,
            ),
            // DRS v1.3.0: tatweel removal + digit conversions.
            toolItem(
                DrsTextTool.REMOVE_TATWEEL,
                R.string.drs__text_tools__tool_remove_tatweel,
                R.string.drs__text_tools__desc_remove_tatweel,
                Icons.Default.HighlightOff,
            ),
            toolItem(
                DrsTextTool.TO_ARABIC_DIGITS,
                R.string.drs__text_tools__tool_to_arabic_digits,
                R.string.drs__text_tools__desc_to_arabic_digits,
                Icons.Default.Translate,
            ),
            toolItem(
                DrsTextTool.TO_WESTERN_DIGITS,
                R.string.drs__text_tools__tool_to_western_digits,
                R.string.drs__text_tools__desc_to_western_digits,
                Icons.Default.SwapHoriz,
            ),
            // DRS v1.4.0: unify Arabic letter variants for copy/search.
            toolItem(
                DrsTextTool.NORMALIZE_ARABIC,
                R.string.drs__text_tools__tool_normalize_arabic,
                R.string.drs__text_tools__desc_normalize_arabic,
                Icons.Default.TextFields,
            ),
            // DRS v1.5.0: Latin sentence punctuation → Arabic marks.
            toolItem(
                DrsTextTool.TO_ARABIC_PUNCTUATION,
                R.string.drs__text_tools__tool_to_arabic_punctuation,
                R.string.drs__text_tools__desc_to_arabic_punctuation,
                Icons.Default.QuestionMark,
            ),
        ),
    ),
    DrsTextToolsPanelSection(
        titleRes = R.string.drs__text_tools__section_punctuation,
        items = listOf(
            toolItem(
                DrsTextTool.NORMALIZE_PUNCTUATION,
                R.string.drs__text_tools__tool_normalize_punctuation,
                R.string.drs__text_tools__desc_normalize_punctuation,
                Icons.Default.Rule,
            ),
            toolItem(
                DrsTextTool.CLEAN_TEXT,
                R.string.drs__text_tools__tool_clean_text,
                R.string.drs__text_tools__desc_clean_text,
                Icons.Default.AutoFixHigh,
            ),
            // DRS v1.2.0: locale-aware quote wrapping.
            toolItem(
                DrsTextTool.WRAP_QUOTES,
                R.string.drs__text_tools__tool_wrap_quotes,
                R.string.drs__text_tools__desc_wrap_quotes,
                Icons.Default.FormatQuote,
            ),
            // DRS v1.5.0: paren wrapping + one sentence per line.
            toolItem(
                DrsTextTool.WRAP_PARENS,
                R.string.drs__text_tools__tool_wrap_parens,
                R.string.drs__text_tools__desc_wrap_parens,
                Icons.Default.Code,
            ),
            toolItem(
                DrsTextTool.SENTENCE_PER_LINE,
                R.string.drs__text_tools__tool_sentence_per_line,
                R.string.drs__text_tools__desc_sentence_per_line,
                Icons.Default.FormatListBulleted,
            ),
        ),
    ),
    DrsTextToolsPanelSection(
        titleRes = R.string.drs__text_tools__section_lines,
        items = listOf(
            toolItem(
                DrsTextTool.DELETE_LINE,
                R.string.drs__text_tools__tool_delete_line,
                R.string.drs__text_tools__desc_delete_line,
                Icons.Default.DeleteSweep,
            ),
            toolItem(
                DrsTextTool.DELETE_TO_LINE_START,
                R.string.drs__text_tools__tool_delete_to_line_start,
                R.string.drs__text_tools__desc_delete_to_line_start,
                Icons.Default.FirstPage,
            ),
            toolItem(
                DrsTextTool.DELETE_TO_LINE_END,
                R.string.drs__text_tools__tool_delete_to_line_end,
                R.string.drs__text_tools__desc_delete_to_line_end,
                Icons.Default.LastPage,
            ),
        ),
    ),
    DrsTextToolsPanelSection(
        titleRes = R.string.drs__text_tools__section_quick,
        items = listOf(
            DrsTextToolsPanelItem(
                code = KeyCode.UNDO,
                labelRes = R.string.quick_action__undo,
                descRes = R.string.drs__text_tools__desc_undo,
                icon = Icons.AutoMirrored.Filled.Undo,
            ),
            DrsTextToolsPanelItem(
                code = KeyCode.REDO,
                labelRes = R.string.quick_action__redo,
                descRes = R.string.drs__text_tools__desc_redo,
                icon = Icons.AutoMirrored.Filled.Redo,
            ),
            DrsTextToolsPanelItem(
                code = KeyCode.CLIPBOARD_SELECT_ALL,
                labelRes = R.string.quick_action__clipboard_select_all,
                descRes = R.string.drs__text_tools__desc_select_all,
                icon = Icons.Default.SelectAll,
            ),
            DrsTextToolsPanelItem(
                code = KeyCode.CLIPBOARD_COPY,
                labelRes = R.string.quick_action__clipboard_copy,
                descRes = R.string.drs__text_tools__desc_copy,
                icon = Icons.Default.ContentCopy,
            ),
            DrsTextToolsPanelItem(
                code = KeyCode.CLIPBOARD_CUT,
                labelRes = R.string.quick_action__clipboard_cut,
                descRes = R.string.drs__text_tools__desc_cut,
                icon = Icons.Default.ContentCut,
            ),
            DrsTextToolsPanelItem(
                code = KeyCode.CLIPBOARD_PASTE,
                labelRes = R.string.quick_action__clipboard_paste,
                descRes = R.string.drs__text_tools__desc_paste,
                icon = Icons.Default.ContentPaste,
            ),
            toolItem(
                DrsTextTool.COUNT,
                R.string.drs__text_tools__tool_count,
                R.string.drs__text_tools__desc_count,
                Icons.Default.Calculate,
            ),
        ),
    ),
)

/**
 * DRS v1.5.0: the panel title of every text tool, shared with the smart
 * bar's most-used tiles so a heavy text-tool user sees real names instead
 * of the invalid-fatal placeholder. Mirrors [PANEL_SECTIONS] labels.
 */
fun textToolTitleRes(tool: DrsTextTool): Int = when (tool) {
    DrsTextTool.UPPERCASE -> R.string.drs__text_tools__tool_uppercase
    DrsTextTool.LOWERCASE -> R.string.drs__text_tools__tool_lowercase
    DrsTextTool.TITLE_CASE -> R.string.drs__text_tools__tool_title_case
    DrsTextTool.SENTENCE_CASE -> R.string.drs__text_tools__tool_sentence_case
    DrsTextTool.TOGGLE_CASE -> R.string.drs__text_tools__tool_toggle_case
    DrsTextTool.TRIM_SPACES -> R.string.drs__text_tools__tool_trim_spaces
    DrsTextTool.TRIM_LINE_EDGES -> R.string.drs__text_tools__tool_trim_line_edges
    DrsTextTool.TABS_TO_SPACES -> R.string.drs__text_tools__tool_tabs_to_spaces
    DrsTextTool.REMOVE_ZERO_WIDTH -> R.string.drs__text_tools__tool_remove_zero_width
    DrsTextTool.REMOVE_EMPTY_LINES -> R.string.drs__text_tools__tool_remove_empty_lines
    DrsTextTool.COLLAPSE_EMPTY_LINES -> R.string.drs__text_tools__tool_collapse_empty_lines
    DrsTextTool.REMOVE_LINE_BREAKS -> R.string.drs__text_tools__tool_remove_line_breaks
    DrsTextTool.SORT_LINES -> R.string.drs__text_tools__tool_sort_lines
    DrsTextTool.SORT_LINES_DESC -> R.string.drs__text_tools__tool_sort_lines_desc
    DrsTextTool.SORT_LINES_BY_LENGTH -> R.string.drs__text_tools__tool_sort_lines_by_length
    DrsTextTool.REMOVE_DUPLICATE_LINES -> R.string.drs__text_tools__tool_remove_duplicate_lines
    DrsTextTool.REMOVE_DUPLICATE_WORDS -> R.string.drs__text_tools__tool_remove_duplicate_words
    DrsTextTool.NUMBER_LINES -> R.string.drs__text_tools__tool_number_lines
    DrsTextTool.REVERSE_LINES -> R.string.drs__text_tools__tool_reverse_lines
    DrsTextTool.REMOVE_DIACRITICS -> R.string.drs__text_tools__tool_remove_diacritics
    DrsTextTool.REMOVE_TATWEEL -> R.string.drs__text_tools__tool_remove_tatweel
    DrsTextTool.TO_ARABIC_DIGITS -> R.string.drs__text_tools__tool_to_arabic_digits
    DrsTextTool.TO_WESTERN_DIGITS -> R.string.drs__text_tools__tool_to_western_digits
    DrsTextTool.TO_ARABIC_PUNCTUATION -> R.string.drs__text_tools__tool_to_arabic_punctuation
    DrsTextTool.NORMALIZE_ARABIC -> R.string.drs__text_tools__tool_normalize_arabic
    DrsTextTool.NORMALIZE_PUNCTUATION -> R.string.drs__text_tools__tool_normalize_punctuation
    DrsTextTool.CLEAN_TEXT -> R.string.drs__text_tools__tool_clean_text
    DrsTextTool.WRAP_QUOTES -> R.string.drs__text_tools__tool_wrap_quotes
    DrsTextTool.WRAP_PARENS -> R.string.drs__text_tools__tool_wrap_parens
    DrsTextTool.SENTENCE_PER_LINE -> R.string.drs__text_tools__tool_sentence_per_line
    DrsTextTool.COUNT -> R.string.drs__text_tools__tool_count
    DrsTextTool.DELETE_LINE -> R.string.drs__text_tools__tool_delete_line
    DrsTextTool.DELETE_TO_LINE_START -> R.string.drs__text_tools__tool_delete_to_line_start
    DrsTextTool.DELETE_TO_LINE_END -> R.string.drs__text_tools__tool_delete_to_line_end
}

/** DRS v1.5.0: the panel description of every text tool (tile tooltips). */
fun textToolDescRes(tool: DrsTextTool): Int = when (tool) {
    DrsTextTool.UPPERCASE -> R.string.drs__text_tools__desc_uppercase
    DrsTextTool.LOWERCASE -> R.string.drs__text_tools__desc_lowercase
    DrsTextTool.TITLE_CASE -> R.string.drs__text_tools__desc_title_case
    DrsTextTool.SENTENCE_CASE -> R.string.drs__text_tools__desc_sentence_case
    DrsTextTool.TOGGLE_CASE -> R.string.drs__text_tools__desc_toggle_case
    DrsTextTool.TRIM_SPACES -> R.string.drs__text_tools__desc_trim_spaces
    DrsTextTool.TRIM_LINE_EDGES -> R.string.drs__text_tools__desc_trim_line_edges
    DrsTextTool.TABS_TO_SPACES -> R.string.drs__text_tools__desc_tabs_to_spaces
    DrsTextTool.REMOVE_ZERO_WIDTH -> R.string.drs__text_tools__desc_remove_zero_width
    DrsTextTool.REMOVE_EMPTY_LINES -> R.string.drs__text_tools__desc_remove_empty_lines
    DrsTextTool.COLLAPSE_EMPTY_LINES -> R.string.drs__text_tools__desc_collapse_empty_lines
    DrsTextTool.REMOVE_LINE_BREAKS -> R.string.drs__text_tools__desc_remove_line_breaks
    DrsTextTool.SORT_LINES -> R.string.drs__text_tools__desc_sort_lines
    DrsTextTool.SORT_LINES_DESC -> R.string.drs__text_tools__desc_sort_lines_desc
    DrsTextTool.SORT_LINES_BY_LENGTH -> R.string.drs__text_tools__desc_sort_lines_by_length
    DrsTextTool.REMOVE_DUPLICATE_LINES -> R.string.drs__text_tools__desc_remove_duplicate_lines
    DrsTextTool.REMOVE_DUPLICATE_WORDS -> R.string.drs__text_tools__desc_remove_duplicate_words
    DrsTextTool.NUMBER_LINES -> R.string.drs__text_tools__desc_number_lines
    DrsTextTool.REVERSE_LINES -> R.string.drs__text_tools__desc_reverse_lines
    DrsTextTool.REMOVE_DIACRITICS -> R.string.drs__text_tools__desc_remove_diacritics
    DrsTextTool.REMOVE_TATWEEL -> R.string.drs__text_tools__desc_remove_tatweel
    DrsTextTool.TO_ARABIC_DIGITS -> R.string.drs__text_tools__desc_to_arabic_digits
    DrsTextTool.TO_WESTERN_DIGITS -> R.string.drs__text_tools__desc_to_western_digits
    DrsTextTool.TO_ARABIC_PUNCTUATION -> R.string.drs__text_tools__desc_to_arabic_punctuation
    DrsTextTool.NORMALIZE_ARABIC -> R.string.drs__text_tools__desc_normalize_arabic
    DrsTextTool.NORMALIZE_PUNCTUATION -> R.string.drs__text_tools__desc_normalize_punctuation
    DrsTextTool.CLEAN_TEXT -> R.string.drs__text_tools__desc_clean_text
    DrsTextTool.WRAP_QUOTES -> R.string.drs__text_tools__desc_wrap_quotes
    DrsTextTool.WRAP_PARENS -> R.string.drs__text_tools__desc_wrap_parens
    DrsTextTool.SENTENCE_PER_LINE -> R.string.drs__text_tools__desc_sentence_per_line
    DrsTextTool.COUNT -> R.string.drs__text_tools__desc_count
    DrsTextTool.DELETE_LINE -> R.string.drs__text_tools__desc_delete_line
    DrsTextTool.DELETE_TO_LINE_START -> R.string.drs__text_tools__desc_delete_to_line_start
    DrsTextTool.DELETE_TO_LINE_END -> R.string.drs__text_tools__desc_delete_to_line_end
}

/**
 * DRS v1.0.6: the technical text tools panel, shown when the keyboard UI
 * mode is [ImeUiMode.TEXT_TOOLS]. Every row dispatches a real key event
 * through the input pipeline; transformations run inside the editor's
 * batch-edit against the actual input connection of the focused field.
 *
 * Transformation tools act on the current selection, or on the whole field
 * when nothing is selected. Nothing is ever stored, logged or transmitted -
 * the host app's own undo remains the way back.
 */
@Composable
fun DrsTextToolsPanel(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val keyboardManager by context.keyboardManager()

    fun dispatch(code: Int) {
        keyboardManager.inputEventDispatcher.sendDownUp(
            TextKeyData(type = KeyType.FUNCTION, code = code, label = "drs_text_tool"),
        )
        DrsAdaptationEngine.recordTechToolUse()
    }

    SnyggColumn(
        modifier = modifier
            .fillMaxWidth()
            .height(DrsImeSizing.imeUiHeight()),
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
                text = stringRes(R.string.drs__text_tools__header_title),
            )
            SnyggIconButton(
                elementName = DrsImeUi.ClipboardHeaderButton.elementName,
                onClick = { dispatch(KeyCode.UNDO) },
                modifier = sizeModifier,
            ) {
                SnyggIcon(imageVector = Icons.AutoMirrored.Filled.Undo)
            }
            SnyggIconButton(
                elementName = DrsImeUi.ClipboardHeaderButton.elementName,
                onClick = { dispatch(KeyCode.REDO) },
                modifier = sizeModifier,
            ) {
                SnyggIcon(imageVector = Icons.AutoMirrored.Filled.Redo)
            }
        }

        SnyggBox(
            DrsImeUi.ClipboardContent.elementName,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
            ) {
                PANEL_SECTIONS.forEach { section ->
                    SnyggText(
                        elementName = DrsImeUi.ClipboardSubheader.elementName,
                        modifier = Modifier.padding(start = 16.dp, top = 14.dp, bottom = 2.dp),
                        text = stringRes(section.titleRes),
                    )
                    section.items.forEach { item ->
                        ToolRow(item = item, onApply = ::dispatch)
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun ToolRow(
    item: DrsTextToolsPanelItem,
    onApply: (Int) -> Unit,
) {
    SnyggBox(
        elementName = DrsImeUi.ClipboardItem.elementName,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 2.dp),
        clickAndSemanticsModifier = Modifier.rippleClickable { onApply(item.code) },
    ) {
        SnyggRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SnyggIconButton(
                elementName = DrsImeUi.ClipboardHeaderButton.elementName,
                onClick = { onApply(item.code) },
                modifier = Modifier
                    .sizeIn(minWidth = 40.dp)
                    .height(40.dp),
            ) {
                SnyggIcon(imageVector = item.icon)
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                SnyggText(
                    text = stringRes(item.labelRes),
                )
                SnyggText(
                    elementName = DrsImeUi.ClipboardItemDescription.elementName,
                    text = stringRes(item.descRes),
                )
            }
        }
    }
}
