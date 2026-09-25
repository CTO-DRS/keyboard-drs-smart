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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.systemBarsPadding
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
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
        var text by remember { mutableStateOf(pending.text) }
        val history = remember { ClipEditorHistory() }
        var font by remember { mutableStateOf(ClipFontOption.DEFAULT) }
        var fontSize by remember { mutableStateOf(ClipFontSizeOption.NORMAL) }
        var findQuery by remember { mutableStateOf("") }
        var replaceQuery by remember { mutableStateOf("") }
        var matchCase by remember { mutableStateOf(false) }
        var activeMatch by remember { mutableStateOf(0) }
        var showSaveAsFile by remember { mutableStateOf(false) }
        var saveName by remember { mutableStateOf("") }

        val matches = remember(text, findQuery, matchCase) {
            ClipSearchEngine.findMatches(text, findQuery, ignoreCase = !matchCase)
        }
        val activeIndex = if (matches.isEmpty()) -1 else activeMatch.coerceIn(0, matches.size - 1)

        fun applyTransform(newText: String) {
            if (newText != text) {
                history.push(text)
                text = ClipboardTextPolicy.truncateForStorage(newText)
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
                                    activeMatch = ClipSearchEngine.prevMatchIndex(matches.size, activeIndex)
                                }
                            }
                            PopupIconButton(icon = Icons.Default.KeyboardArrowDown, description = "next") {
                                if (matches.isNotEmpty()) {
                                    activeMatch = ClipSearchEngine.nextMatchIndex(matches.size, activeIndex)
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
                            .verticalScroll(rememberScrollState()),
                    ) {
                        BasicTextField(
                            value = text,
                            onValueChange = { text = ClipboardTextPolicy.truncateForStorage(it) },
                            modifier = Modifier.fillMaxWidth(),
                            textStyle = TextStyle(
                                fontFamily = editorFontFamily,
                                fontSize = fontSize.spValue.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                            ),
                        )
                    }

                    // Storage-policy counter + undo/redo + cancel/save.
                    Text(
                        text = stringRes(
                            R.string.clip__char_limit_counter,
                            "used" to text.length,
                            "max" to ClipboardTextPolicy.MAX_TEXT_CHARS,
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
}
