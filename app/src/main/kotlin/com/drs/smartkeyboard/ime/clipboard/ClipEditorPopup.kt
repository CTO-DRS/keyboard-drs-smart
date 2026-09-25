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

package com.drs.smartkeyboard.ime.clipboard

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.SaveAlt
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.drs.smartkeyboard.R
import com.drs.smartkeyboard.app.DrsPreferenceStore
import com.drs.smartkeyboard.app.apptheme.DrsAppTheme
import com.drs.smartkeyboard.clipboardManager
import com.drs.smartkeyboard.ime.clipboard.provider.ClipboardItem
import com.drs.smartkeyboard.ime.clipboard.provider.ItemType
import java.time.ZoneId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.drs.jetpref.datastore.model.collectAsState
import org.drs.lib.android.showShortToastSync
import org.drs.lib.compose.ProvideLocalizedResources
import org.drs.lib.compose.stringRes

/**
 * DRS v1.11.0: the popup clipboard editor window. The user asked for the
 * editor to live in a real floating popup window above the whole screen —
 * like a system alert — and NOT inside the clipboard panel anymore.
 *
 * The route is a dialog-styled translucent [ClipEditorPopupActivity]: the
 * underlying app stays visible behind a dimmed scrim, the editor card floats
 * centered on top, and the system keyboard (any IME) can open inside it for
 * text input. The in-panel editor stays wired as the honest fallback for the
 * rare case the activity launch is blocked by the system.
 */

/** Where a clipboard edit request is served. Pure — JVM-testable. */
enum class ClipEditorRoute {
    /** The floating popup window above the whole screen. */
    POPUP_WINDOW,

    /** The legacy in-panel editor (fallback path only). */
    IN_PANEL,
}

/**
 * DRS v1.11.0: decides where an edit request goes. Only real text items are
 * served by the popup window — images/videos and the defensive null-text
 * case stay with the in-panel editor. Pure and JVM-testable.
 */
object ClipEditorPopupPolicy {
    fun routeFor(type: ItemType, text: String?): ClipEditorRoute {
        return if (type == ItemType.TEXT && text != null) {
            ClipEditorRoute.POPUP_WINDOW
        } else {
            ClipEditorRoute.IN_PANEL
        }
    }

    /**
     * The honest fallback contract: an intended popup launch that failed
     * degrades to the in-panel editor; a failed panel route stays panel
     * (never escalates on its own).
     */
    fun fallbackAfterLaunch(intended: ClipEditorRoute, launchSucceeded: Boolean): ClipEditorRoute {
        return when {
            intended == ClipEditorRoute.POPUP_WINDOW && launchSucceeded -> ClipEditorRoute.POPUP_WINDOW
            intended == ClipEditorRoute.POPUP_WINDOW -> ClipEditorRoute.IN_PANEL
            else -> ClipEditorRoute.IN_PANEL
        }
    }
}

/**
 * DRS v1.11.0: the popup window geometry contract. The card covers a fixed
 * fraction of the screen so the dimmed app stays visible around it, exactly
 * like a system alert. Fractions are pinned and JVM-tested.
 */
object ClipEditorPopupSpec {
    /** Card width as a fraction of the screen width. */
    const val WIDTH_FRACTION: Float = 0.92f

    /** Card height as a fraction of the screen height. */
    const val HEIGHT_FRACTION: Float = 0.86f

    /** The scrim dimming the underlying app. */
    const val SCRIM_ALPHA: Float = 0.55f

    /** Card width in px for a given screen width (guarded). */
    fun cardWidthPx(screenWidthPx: Int): Int {
        return if (screenWidthPx <= 0) 0 else (screenWidthPx * WIDTH_FRACTION).toInt()
    }

    /** Card height in px for a given screen height (guarded). */
    fun cardHeightPx(screenHeightPx: Int): Int {
        return if (screenHeightPx <= 0) 0 else (screenHeightPx * HEIGHT_FRACTION).toInt()
    }
}

/**
 * DRS v1.11.0: one pending edit handed from the IME to the popup activity.
 * Both components live in the same process, so the item travels by
 * reference — no Intent size limits even at the 50,000-char cap.
 */
data class PendingClipEdit(
    val item: ClipboardItem,
    val text: String,
    val openedAtMs: Long,
)

/**
 * DRS v1.13.0: adapts the editor's real TextLayoutResult to the pure
 * ClipLineLayout — the wrapped visual rows the direct line jump reads.
 * The transformation is identity, so the field offsets and the layout
 * offsets agree one to one.
 */
internal class ClipTextLineLayout(private val layout: TextLayoutResult) : ClipLineLayout {
    override val textLength: Int get() = layout.layoutInput.text.length
    override val rowCount: Int get() = layout.lineCount
    override fun rowForOffset(offset: Int): Int = layout.getLineForOffset(offset)
    override fun rowTop(row: Int): Int = layout.getLineTop(row).toInt()
    override fun rowBottom(row: Int): Int = layout.getLineBottom(row).toInt()
}

/**
 * DRS v1.11.0: the process-wide handoff slot for the popup editor. The IME
 * puts the request, the activity consumes it exactly once; the slot never
 * survives a finished session. JVM-testable.
 */
object ClipEditorPopupStore {
    @Volatile
    private var pending: PendingClipEdit? = null

    val isActive: Boolean
        get() = pending != null

    fun put(item: ClipboardItem, openedAtMs: Long) {
        pending = PendingClipEdit(item = item, text = item.text.orEmpty(), openedAtMs = openedAtMs)
    }

    /** Reads the pending request without consuming it. */
    fun peek(): PendingClipEdit? = pending

    /** Reads and clears the pending request — exactly-once semantics. */
    fun consume(): PendingClipEdit? {
        val current = pending
        pending = null
        return current
    }

    fun clear() {
        pending = null
    }
}

/**
 * DRS v1.11.0: opens the popup editor window from the IME composition.
 * Returns [ClipEditorRoute.POPUP_WINDOW] on a successful launch and
 * [ClipEditorRoute.IN_PANEL] when the policy says panel or the system
 * blocked the start — the caller then serves the request in-panel instead.
 */
object ClipEditorPopupLauncher {
    fun launch(context: Context, item: ClipboardItem, nowMs: Long = System.currentTimeMillis()): ClipEditorRoute {
        val intended = ClipEditorPopupPolicy.routeFor(item.type, item.text)
        if (intended != ClipEditorRoute.POPUP_WINDOW) {
            return ClipEditorRoute.IN_PANEL
        }
        ClipEditorPopupStore.put(item, nowMs)
        val started = runCatching {
            context.startActivity(
                Intent(context, ClipEditorPopupActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
            )
        }.isSuccess
        return ClipEditorPopupPolicy.fallbackAfterLaunch(intended, started)
    }
}

/**
 * DRS v1.11.0: the floating popup editor window itself. A dialog-styled
 * translucent activity: the app behind stays visible through the scrim, the
 * editor card floats centered, and the system keyboard opens inside the
 * window (adjustResize) for text input. Saving walks the same engine path
 * as the panel editor — [ClipboardManager.editClipText] — so history,
 * primary-clip sync and timestamps behave identically.
 */
class ClipEditorPopupActivity : ComponentActivity() {
    private val clipboardManager by clipboardManager()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val pending = ClipEditorPopupStore.consume()
        if (pending == null) {
            // Defensive: no pending request (e.g. recreated after process
            // death). Nothing to edit — close silently.
            finish()
            return
        }
        setContent {
            val prefs by DrsPreferenceStore
            val theme by prefs.other.settingsTheme.collectAsState()
            ProvideLocalizedResources(
                this,
                appName = R.string.app_name,
            ) {
                DrsAppTheme(theme = theme) {
                    PopupSurface(pending)
                }
            }
        }
    }

    @Composable
    private fun PopupSurface(pending: PendingClipEdit) {
        val scope = rememberCoroutineScope()
        // DRS v1.12.0: the editor honors the app settings defaults — the
        // font family/size, the case matching, the editor character limit
        // (up to the 500,000 policy cap), the colored result cards, and
        // the code-line detection.
        val prefs by DrsPreferenceStore
        val cardsEnabled = prefs.clipboard.searchResultCards.get()
        val codeDetection = prefs.clipboard.codeDetection.get()
        val editorLimit = prefs.clipboard.editorCharLimit.get().effectiveLimit()
        var text by remember { mutableStateOf(pending.text) }
        val history = remember { ClipEditorHistory() }
        var font by remember {
            mutableStateOf(prefs.clipboard.editorFont.get())
        }
        var fontSize by remember { mutableStateOf(prefs.clipboard.editorFontSize.get()) }
        var findQuery by remember { mutableStateOf("") }
        var replaceQuery by remember { mutableStateOf("") }
        var matchCase by remember { mutableStateOf(prefs.clipboard.matchCaseByDefault.get()) }
        var activeMatch by remember { mutableStateOf(0) }
        var resultsShown by remember { mutableStateOf(prefs.clipboard.autoResultsPanel.get()) }
        var showSaveAsFile by remember { mutableStateOf(false) }
        var saveName by remember { mutableStateOf("") }

        // DRS v1.13.0: the direct line jump — the editor viewport's scroll
        // state, its laid-out snapshot, its visible height, and the result
        // cards' list state. Tapping a card (or the navigation arrows)
        // animates the field straight to the match's row.
        val textScrollState = rememberScrollState()
        val cardsListState = rememberLazyListState()
        var editorLayout by remember { mutableStateOf<TextLayoutResult?>(null) }
        var editorViewportPx by remember { mutableStateOf(0) }

        fun jumpToMatchLine(offset: Int) {
            val target = ClipResultJump.scrollOffsetFor(
                layout = editorLayout?.let(::ClipTextLineLayout),
                offset = offset,
                viewportPx = editorViewportPx,
                maxScrollPx = textScrollState.maxValue,
            )
            if (target != null) {
                scope.launch { textScrollState.animateScrollTo(target) }
            }
        }

        // DRS v1.12.0: detected code switches to the monospace family
        // once, at open time, when detection is enabled.
        LaunchedEffect(pending.item.id) {
            if (codeDetection &&
                pending.text.length <= ClipCodeDetector.LARGE_TEXT_CHARS &&
                ClipCodeDetector.analyze(pending.text).isCode
            ) {
                font = ClipFontOption.MONOSPACE
            }
        }

        val matches = remember(text, findQuery, matchCase) {
            ClipSearchEngine.findMatches(text, findQuery, ignoreCase = !matchCase)
        }
        val activeIndex = if (matches.isEmpty()) -1 else activeMatch.coerceIn(0, matches.size - 1)

        fun applyTransform(newText: String) {
            if (newText != text) {
                history.push(text)
                text = ClipboardTextPolicy.truncateForStorage(newText, editorLimit)
            }
        }

        fun shareCurrent() {
            val send = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, text)
            }
            runCatching {
                startActivity(
                    Intent.createChooser(send, getString(R.string.clip__share_item)),
                )
            }
        }

        fun saveAsFile(fileName: String) {
            val body = text
            scope.launch {
                val result = withContext(Dispatchers.IO) {
                    ClipFileSaver.save(this@ClipEditorPopupActivity, body, fileName)
                }
                val message = when (result) {
                    is ClipFileSaver.Result.PublicDownloads -> R.string.clip__saved_to_downloads
                    is ClipFileSaver.Result.PrivateFiles -> R.string.clip__saved_to_app_files
                    ClipFileSaver.Result.Failed -> R.string.clip__save_failed
                }
                showShortToastSync(message)
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = ClipEditorPopupSpec.SCRIM_ALPHA))
                .pointerInput(Unit) {
                    detectTapGestures { finish() }
                }
                .systemBarsPadding(),
            contentAlignment = Alignment.Center,
        ) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 12.dp,
                modifier = Modifier
                    .fillMaxWidth(ClipEditorPopupSpec.WIDTH_FRACTION)
                    .fillMaxHeight(ClipEditorPopupSpec.HEIGHT_FRACTION)
                    .pointerInput(Unit) {
                        detectTapGestures { /* swallow */ }
                    },
            ) {
                // DRS v1.11.0: the save-as-file dialog floats over the editor
                // card, so the card content lives in a Box with both layers.
                Box(modifier = Modifier.fillMaxSize()) {
                Column(modifier = Modifier.fillMaxSize().padding(horizontal = 14.dp, vertical = 10.dp)) {
                    // Header: close, title, share, save-as-file.
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        PopupIconButton(icon = Icons.Default.Close, description = "close") {
                            finish()
                        }
                        // DRS v1.11.0: the title plus the honest hint that
                        // this editor is an independent popup window, not
                        // the in-panel surface.
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 6.dp),
                        ) {
                            Text(
                                text = stringRes(R.string.clip__editor_title),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Text(
                                text = stringRes(R.string.clip__popup_window_hint),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        PopupIconButton(icon = Icons.Default.Share, description = "share") {
                            shareCurrent()
                        }
                        PopupIconButton(icon = Icons.Default.SaveAlt, description = "save as file") {
                            saveName = ClipFileNamer.defaultFileName(
                                System.currentTimeMillis(), ZoneId.systemDefault(),
                            )
                            showSaveAsFile = true
                        }
                    }

                    // Live statistics.
                    val stats = ClipTextStats.of(text)
                    Text(
                        text = stringRes(
                            R.string.clip__stats_label,
                            "chars" to stats.chars,
                            "words" to stats.words,
                            "lines" to stats.lines,
                        ),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.fillMaxWidth(),
                    )

                    // DRS v1.12.0: the programming-line badge — language,
                    // code-line share, and the auto monospace switch live
                    // behind the same pure detector as the panel editor.
                    val codeAnalysis = remember(text, codeDetection) {
                        if (codeDetection && text.length <= ClipCodeDetector.LARGE_TEXT_CHARS) {
                            ClipCodeDetector.analyze(text)
                        } else {
                            null
                        }
                    }
                    if (codeAnalysis?.isCode == true) {
                        Text(
                            text = "\uD83D\uDCBB " + stringRes(
                                R.string.clip__code_badge,
                                "lang" to codeLanguageLabel(codeAnalysis.language),
                                "code" to codeAnalysis.codeLines,
                                "total" to codeAnalysis.totalLines,
                            ),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                    // DRS v1.12.0: the slowdown warning for huge texts.
                    if (text.length >= ClipboardTextPolicy.LARGE_TEXT_WARNING_CHARS) {
                        Text(
                            text = "⚠ " + stringRes(R.string.clip__large_text_warning),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }

                    // Find row: query, case toggle, match counter, navigation.
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        PopupTextField(
                            value = findQuery,
                            onValueChange = {
                                findQuery = it
                                activeMatch = 0
                            },
                            hint = stringRes(R.string.clip__editor_find_hint),
                            modifier = Modifier.weight(1f),
                        )
                        PopupChip(
                            label = stringRes(R.string.clip__editor_match_case),
                            active = matchCase,
                            onClick = {
                                matchCase = !matchCase
                                activeMatch = 0
                            },
                        )
                        if (findQuery.isNotEmpty()) {
                            Text(
                                text = if (matches.isEmpty()) {
                                    stringRes(R.string.clip__editor_no_matches)
                                } else {
                                    stringRes(
                                        R.string.clip__editor_matches_counter,
                                        "active" to (activeIndex + 1),
                                        "count" to matches.size,
                                    )
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 6.dp),
                            )
                            PopupIconButton(icon = Icons.Default.KeyboardArrowUp, description = "prev") {
                                if (matches.isNotEmpty()) {
                                    val index = ClipSearchEngine.prevMatchIndex(matches.size, activeIndex)
                                    activeMatch = index
                                    jumpToMatchLine(matches[index].start)
                                }
                            }
                            PopupIconButton(icon = Icons.Default.KeyboardArrowDown, description = "next") {
                                if (matches.isNotEmpty()) {
                                    val index = ClipSearchEngine.nextMatchIndex(matches.size, activeIndex)
                                    activeMatch = index
                                    jumpToMatchLine(matches[index].start)
                                }
                            }
                            PopupIconButton(icon = Icons.Default.Close, description = "clear find") {
                                findQuery = ""
                                replaceQuery = ""
                                activeMatch = 0
                            }
                        }
                    }

                    // Replace row: appears only while a find query is active.
                    if (findQuery.isNotEmpty()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            PopupTextField(
                                value = replaceQuery,
                                onValueChange = { replaceQuery = it },
                                hint = stringRes(R.string.clip__editor_replace_hint),
                                modifier = Modifier.weight(1f),
                            )
                            PopupChip(
                                label = stringRes(R.string.clip__editor_replace_one),
                                active = false,
                                onClick = {
                                    if (activeIndex in matches.indices) {
                                        applyTransform(
                                            ClipSearchEngine.replaceOne(
                                                text, matches[activeIndex], replaceQuery,
                                            ),
                                        )
                                    }
                                },
                            )
                            PopupChip(
                                label = stringRes(R.string.clip__editor_replace_all),
                                active = false,
                                onClick = {
                                    if (matches.isNotEmpty()) {
                                        val (newText, count) = ClipSearchEngine.replaceAll(
                                            text, findQuery, replaceQuery, ignoreCase = !matchCase,
                                        )
                                        applyTransform(newText)
                                        showShortToastSync(
                                            R.string.clip__editor_replace_done, "count" to count,
                                        )
                                    }
                                },
                            )
                        }
                    }

                    // DRS v1.12.0: the colored search-result cards — every
                    // match becomes a card with its line number and the
                    // same-line context; DRS v1.13.0: tapping a card jumps
                    // the editor straight to that match's row, and the
                    // cards list tracks the active card both ways.
                    if (cardsEnabled && matches.isNotEmpty()) {
                        val resultCards = remember(matches, text) {
                            ClipSearchResults.buildCards(text, matches)
                        }
                        Row(modifier = Modifier.fillMaxWidth()) {
                            PopupChip(
                                label = if (resultsShown) {
                                    stringRes(R.string.clip__results_hide)
                                } else {
                                    stringRes(R.string.clip__results_title, "count" to matches.size)
                                },
                                active = resultsShown,
                                onClick = { resultsShown = !resultsShown },
                            )
                        }
                        if (resultsShown) {
                            LazyColumn(
                                state = cardsListState,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 132.dp),
                            ) {
                                items(resultCards.size) { cardIndex ->
                                    val card = resultCards[cardIndex]
                                    PopupResultCard(
                                        card = card,
                                        active = card.matchIndex == activeIndex,
                                        onSelect = {
                                            activeMatch = card.matchIndex
                                            jumpToMatchLine(card.start)
                                        },
                                    )
                                }
                            }
                            // Keep the active card in sight — from the
                            // arrows, from a card tap, and after edits.
                            LaunchedEffect(activeIndex, resultCards) {
                                if (activeIndex >= 0 && resultCards.isNotEmpty()) {
                                    cardsListState.animateScrollToItem(
                                        activeIndex.coerceAtMost(resultCards.size - 1),
                                    )
                                }
                            }
                        }
                    }

                    // Smart algorithms row — the same sixteen chips.
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        PopupChip(label = "AA", active = false) {
                            applyTransform(ClipTextTransforms.toUpper(text))
                        }
                        PopupChip(label = "aa", active = false) {
                            applyTransform(ClipTextTransforms.toLower(text))
                        }
                        PopupChip(label = "Aa", active = false) {
                            applyTransform(ClipTextTransforms.toTitleCase(text))
                        }
                        PopupChip(label = "aA", active = false) {
                            applyTransform(ClipTextTransforms.invertCase(text))
                        }
                        PopupChip(label = stringRes(R.string.clip__transform_trim), active = false) {
                            applyTransform(ClipTextTransforms.trimLines(text))
                        }
                        PopupChip(label = stringRes(R.string.clip__transform_collapse), active = false) {
                            applyTransform(ClipTextTransforms.collapseHorizontalWhitespace(text))
                        }
                        PopupChip(label = stringRes(R.string.clip__transform_remove_empty), active = false) {
                            applyTransform(ClipTextTransforms.removeEmptyLines(text))
                        }
                        PopupChip(label = stringRes(R.string.clip__transform_dedupe), active = false) {
                            applyTransform(ClipTextTransforms.removeDuplicateLines(text))
                        }
                        PopupChip(label = stringRes(R.string.clip__transform_sort_asc), active = false) {
                            applyTransform(ClipTextTransforms.sortLinesAscending(text))
                        }
                        PopupChip(label = stringRes(R.string.clip__transform_sort_desc), active = false) {
                            applyTransform(ClipTextTransforms.sortLinesDescending(text))
                        }
                        PopupChip(label = stringRes(R.string.clip__transform_reverse), active = false) {
                            applyTransform(ClipTextTransforms.reverseLines(text))
                        }
                        PopupChip(label = stringRes(R.string.clip__transform_no_diacritics), active = false) {
                            applyTransform(ClipTextTransforms.removeArabicDiacritics(text))
                        }
                        PopupChip(label = stringRes(R.string.clip__transform_normalize), active = false) {
                            applyTransform(ClipTextTransforms.normalizeArabicLetters(text))
                        }
                        PopupChip(label = stringRes(R.string.clip__extract_urls), active = false) {
                            val found = ClipTextTransforms.extractUrls(text)
                            if (found.isEmpty()) {
                                showShortToastSync(R.string.clip__extract_none)
                            } else {
                                applyTransform(found.joinToString("\n"))
                                showShortToastSync(R.string.clip__extract_done, "count" to found.size)
                            }
                        }
                        PopupChip(label = stringRes(R.string.clip__extract_emails), active = false) {
                            val found = ClipTextTransforms.extractEmails(text)
                            if (found.isEmpty()) {
                                showShortToastSync(R.string.clip__extract_none)
                            } else {
                                applyTransform(found.joinToString("\n"))
                                showShortToastSync(R.string.clip__extract_done, "count" to found.size)
                            }
                        }
                        PopupChip(label = stringRes(R.string.clip__extract_phones), active = false) {
                            val found = ClipTextTransforms.extractPhoneNumbers(text)
                            if (found.isEmpty()) {
                                showShortToastSync(R.string.clip__extract_none)
                            } else {
                                applyTransform(found.joinToString("\n"))
                                showShortToastSync(R.string.clip__extract_done, "count" to found.size)
                            }
                        }
                        // DRS v1.12.0: the line numbering and the code-line
                        // surgery chips — extract keeps only code lines,
                        // remove drops them; both undoable like the rest.
                        PopupChip(label = stringRes(R.string.clip__transform_number_lines), active = false) {
                            applyTransform(ClipTextTransforms.numberLines(text))
                        }
                        PopupChip(label = stringRes(R.string.clip__transform_extract_code), active = false) {
                            val extracted = ClipCodeDetector.extractCodeLines(text)
                            if (extracted.isEmpty()) {
                                showShortToastSync(R.string.clip__extract_none)
                            } else {
                                applyTransform(extracted)
                            }
                        }
                        PopupChip(label = stringRes(R.string.clip__transform_remove_code), active = false) {
                            applyTransform(ClipCodeDetector.removeCodeLines(text))
                        }
                    }

                    // Font row: five families + four size steps.
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        for (f in ClipFontOption.entries) {
                            PopupChip(
                                label = stringRes(
                                    when (f) {
                                        ClipFontOption.DEFAULT -> R.string.clip__font_default
                                        ClipFontOption.SANS_SERIF -> R.string.clip__font_sans
                                        ClipFontOption.SERIF -> R.string.clip__font_serif
                                        ClipFontOption.MONOSPACE -> R.string.clip__font_mono
                                        ClipFontOption.CURSIVE -> R.string.clip__font_cursive
                                    },
                                ),
                                active = f == font,
                                onClick = { font = f },
                            )
                        }
                        for (size in ClipFontSizeOption.entries) {
                            PopupChip(
                                label = size.spValue.toString(),
                                active = size == fontSize,
                                onClick = { fontSize = size },
                            )
                        }
                    }

                    // The editor field itself.
                    val editorFontFamily = when (font) {
                        ClipFontOption.DEFAULT -> FontFamily.Default
                        ClipFontOption.SANS_SERIF -> FontFamily.SansSerif
                        ClipFontOption.SERIF -> FontFamily.Serif
                        ClipFontOption.MONOSPACE -> FontFamily.Monospace
                        ClipFontOption.CURSIVE -> FontFamily.Cursive
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(vertical = 6.dp)
                            .onSizeChanged { editorViewportPx = it.height }
                            .verticalScroll(textScrollState),
                    ) {
                        // DRS v1.12.0: the matches glow inside the text —
                        // each match paints its palette color, the active
                        // one strongest, matching the result cards.
                        val highlightRanges = remember(matches, activeIndex) {
                            ClipSearchResults.highlightRanges(matches, activeIndex)
                        }
                        val highlightTransformation = remember(highlightRanges) {
                            VisualTransformation { fieldText ->
                                if (highlightRanges.isEmpty()) {
                                    TransformedText(
                                        androidx.compose.ui.text.AnnotatedString(fieldText.text),
                                        OffsetMapping.Identity,
                                    )
                                } else {
                                    val annotated = buildAnnotatedString {
                                        append(fieldText.text)
                                        for (range in highlightRanges) {
                                            addStyle(
                                                SpanStyle(
                                                    background = Color(
                                                        ClipResultPalette.COLORS[range.colorSlot],
                                                    ).copy(alpha = if (range.isActive) 0.55f else 0.22f),
                                                ),
                                                range.start,
                                                range.end,
                                            )
                                        }
                                    }
                                    TransformedText(annotated, OffsetMapping.Identity)
                                }
                            }
                        }
                        BasicTextField(
                            value = text,
                            onValueChange = { text = ClipboardTextPolicy.truncateForStorage(it, editorLimit) },
                            modifier = Modifier.fillMaxWidth(),
                            textStyle = TextStyle(
                                fontFamily = editorFontFamily,
                                fontSize = fontSize.spValue.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                            ),
                            visualTransformation = highlightTransformation,
                            onTextLayout = { editorLayout = it },
                        )
                    }

                    // Storage-policy counter + undo/redo + cancel/save.
                    Text(
                        text = stringRes(
                            R.string.clip__char_limit_counter,
                            "used" to text.length,
                            "max" to editorLimit,
                        ),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        PopupIconButton(
                            icon = Icons.AutoMirrored.Filled.Undo,
                            description = "undo",
                            enabled = history.canUndo,
                        ) {
                            history.undo(text)?.let { text = it }
                        }
                        PopupIconButton(
                            icon = Icons.AutoMirrored.Filled.Redo,
                            description = "redo",
                            enabled = history.canRedo,
                        ) {
                            history.redo(text)?.let { text = it }
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        PopupButton(label = stringRes(R.string.action__cancel)) {
                            finish()
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        PopupButton(label = stringRes(R.string.action__save)) {
                            val edited = pending.item
                            clipboardManager.editClipText(edited, text)
                            showShortToastSync(R.string.clip__editor_saved_toast)
                            finish()
                        }
                    }
                }

                // Save-as-file dialog — floats over the editor card.
                if (showSaveAsFile) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.35f))
                            .pointerInput(Unit) {
                                detectTapGestures {
                                    showSaveAsFile = false
                                }
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        Surface(
                            shape = RoundedCornerShape(18.dp),
                            color = MaterialTheme.colorScheme.surface,
                            shadowElevation = 8.dp,
                            modifier = Modifier
                                .fillMaxWidth(0.86f)
                                .pointerInput(Unit) {
                                    detectTapGestures { /* swallow */ }
                                },
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = stringRes(R.string.clip__save_file_title),
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                                PopupTextField(
                                    value = saveName,
                                    onValueChange = { saveName = it },
                                    hint = stringRes(R.string.clip__save_file_name_hint),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp),
                                )
                                Text(
                                    text = stringRes(R.string.clip__save_file_note),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Spacer(modifier = Modifier.weight(1f))
                                    PopupButton(label = stringRes(R.string.action__cancel)) {
                                        showSaveAsFile = false
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    PopupButton(label = stringRes(R.string.action__save)) {
                                        val name = ClipFileNamer.sanitize(saveName)
                                        showSaveAsFile = false
                                        saveAsFile(name)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    }

    @Composable
    private fun PopupIconButton(
        icon: ImageVector,
        description: String,
        enabled: Boolean = true,
        onClick: () -> Unit,
    ) {
        val fg = if (enabled) {
            MaterialTheme.colorScheme.onSurface
        } else {
            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
        }
        Box(
            modifier = Modifier
                .padding(2.dp)
                .clickable(enabled = enabled, onClick = onClick)
                .padding(4.dp),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = description,
                tint = fg,
            )
        }
    }

    @Composable
    private fun PopupChip(
        label: String,
        active: Boolean,
        onClick: () -> Unit,
    ) {
        val bg = if (active) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.surfaceVariant
        }
        val fg = if (active) {
            MaterialTheme.colorScheme.onPrimary
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        }
        Surface(
            shape = RoundedCornerShape(50),
            color = bg,
            modifier = Modifier.padding(vertical = 2.dp),
        ) {
            Text(
                text = label,
                color = fg,
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier
                    .clickable(onClick = onClick)
                    .padding(horizontal = 10.dp, vertical = 6.dp),
            )
        }
    }

    @Composable
    private fun PopupTextField(
        value: String,
        onValueChange: (String) -> Unit,
        hint: String,
        modifier: Modifier = Modifier,
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = modifier,
            singleLine = true,
            textStyle = TextStyle(
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface,
            ),
            decorationBox = { innerTextField ->
                Box {
                    if (value.isEmpty()) {
                        Text(
                            text = hint,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            fontSize = 14.sp,
                        )
                    }
                    innerTextField()
                }
            },
        )
    }

    @Composable
    private fun PopupButton(
        label: String,
        onClick: () -> Unit,
    ) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(2.dp),
        ) {
            Text(
                text = label,
                color = MaterialTheme.colorScheme.onPrimary,
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier
                    .clickable(onClick = onClick)
                    .padding(horizontal = 14.dp, vertical = 8.dp),
            )
        }
    }

    // ------------------------------------------------------------------
    // DRS v1.12.0 — the complete smart clipboard system helpers.
    // ------------------------------------------------------------------

    /** The display label of a detected code language. */
    @Composable
    private fun codeLanguageLabel(language: ClipCodeLanguage): String = stringRes(
        when (language) {
            ClipCodeLanguage.KOTLIN_JAVA -> R.string.clip__lang_kotlin_java
            ClipCodeLanguage.PYTHON -> R.string.clip__lang_python
            ClipCodeLanguage.JAVASCRIPT -> R.string.clip__lang_javascript
            ClipCodeLanguage.JSON -> R.string.clip__lang_json
            ClipCodeLanguage.HTML_XML -> R.string.clip__lang_html_xml
            ClipCodeLanguage.CSS -> R.string.clip__lang_css
            ClipCodeLanguage.SQL -> R.string.clip__lang_sql
            ClipCodeLanguage.C_CPP -> R.string.clip__lang_c_cpp
            ClipCodeLanguage.BASH -> R.string.clip__lang_bash
            ClipCodeLanguage.UNKNOWN -> R.string.clip__lang_unknown
        },
    )

    /**
     * One colored search-result card of the popup editor: the palette
     * color paints the border and the matched span inside the context;
     * tapping makes the match the active one (counter + glow follow).
     */
    @Composable
    private fun PopupResultCard(
        card: ClipResultCard,
        active: Boolean,
        onSelect: () -> Unit,
    ) {
        val color = Color(ClipResultPalette.COLORS[card.colorSlot])
        val annotated = buildAnnotatedString {
            if (card.truncatedBefore) append("…")
            append(card.before)
            withStyle(SpanStyle(color = color, fontWeight = FontWeight.Bold)) {
                append(card.matched)
            }
            append(card.after)
            if (card.truncatedAfter) append("…")
        }
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = color.copy(alpha = if (active) 0.30f else 0.12f),
            border = BorderStroke(1.dp, if (active) color else color.copy(alpha = 0.45f)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 2.dp, vertical = 2.dp),
        ) {
            Row(
                modifier = Modifier
                    .clickable(onClick = onSelect)
                    .padding(horizontal = 8.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringRes(R.string.clip__result_line, "line" to card.lineNumber),
                    color = color,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.labelSmall,
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = annotated,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                )
            }
        }
    }
}
