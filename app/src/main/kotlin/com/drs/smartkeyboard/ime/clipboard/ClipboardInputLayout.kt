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

package com.drs.smartkeyboard.ime.clipboard

import android.content.ContentUris
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.media.ThumbnailUtils
import android.provider.MediaStore
import android.util.Size
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridScope
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.automirrored.outlined.Backspace
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.SaveAlt
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.FilterListOff
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.ToggleOff
import androidx.compose.material.icons.filled.ToggleOn
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.outlined.ContentPasteGo
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateSetOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.drs.smartkeyboard.R
import com.drs.smartkeyboard.app.DrsPreferenceStore
import com.drs.smartkeyboard.clipboardManager
import com.drs.smartkeyboard.ime.ImeUiMode
import com.drs.smartkeyboard.ime.clipboard.provider.ClipboardFileStorage
import com.drs.smartkeyboard.ime.clipboard.provider.ClipboardItem
import com.drs.smartkeyboard.ime.clipboard.provider.ItemType
import com.drs.smartkeyboard.ime.keyboard.DrsImeSizing
import com.drs.smartkeyboard.ime.media.KeyboardLikeButton
import com.drs.smartkeyboard.ime.smartbar.AnimationDuration
import com.drs.smartkeyboard.ime.smartbar.VerticalEnterTransition
import com.drs.smartkeyboard.ime.smartbar.VerticalExitTransition
import com.drs.smartkeyboard.ime.text.keyboard.TextKeyData
import com.drs.smartkeyboard.ime.theme.DrsImeUi
import com.drs.smartkeyboard.keyboardManager
import com.drs.smartkeyboard.lib.observeAsTransformingState
import com.drs.smartkeyboard.lib.util.NetworkUtils
import org.drs.jetpref.datastore.model.collectAsState
import java.time.Instant
import java.time.ZoneId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.drs.lib.android.AndroidKeyguardManager
import org.drs.lib.android.AndroidVersion
import org.drs.lib.android.showShortToastSync
import org.drs.lib.android.systemService
import org.drs.lib.compose.LocalLocalizedDateTimeFormatter
import org.drs.lib.compose.autoMirrorForRtl
import org.drs.lib.compose.drsHorizontalScroll
import org.drs.lib.compose.drsVerticalScroll
import org.drs.lib.compose.rippleClickable
import org.drs.lib.compose.stringRes
import org.drs.lib.snygg.SnyggQueryAttributes
import org.drs.lib.snygg.ui.SnyggBox
import org.drs.lib.snygg.ui.SnyggButton
import org.drs.lib.snygg.ui.SnyggChip
import org.drs.lib.snygg.ui.SnyggColumn
import org.drs.lib.snygg.ui.SnyggIcon
import org.drs.lib.snygg.ui.SnyggIconButton
import org.drs.lib.snygg.ui.SnyggRow
import org.drs.lib.snygg.ui.SnyggText
import org.drs.lib.snygg.ui.rememberSnyggThemeQuery

private val ItemWidth = 200.dp
private val DialogWidth = 240.dp
private val EditDialogWidth = 320.dp

const val CLIPBOARD_HISTORY_NUM_GRID_COLUMNS_AUTO: Int = 0

@Composable
fun ClipboardInputLayout(
    modifier: Modifier = Modifier,
) {
    val prefs by DrsPreferenceStore
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val clipboardManager by context.clipboardManager()
    val keyboardManager by context.keyboardManager()
    val androidKeyguardManager = remember { context.systemService(AndroidKeyguardManager::class) }

    val deviceLocked = androidKeyguardManager.let { it.isDeviceLocked || it.isKeyguardLocked }
    val historyEnabled by prefs.clipboard.historyEnabled.collectAsState()

    var isFilterRowShown by remember { mutableStateOf(false) }
    var isSearchRowShown by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    val activeFilterTypes = remember { mutableStateSetOf<ItemType>() }

    val unfilteredHistory by clipboardManager.historyFlow.collectAsState()
    val filteredHistory = remember(unfilteredHistory, activeFilterTypes.toSet(), searchQuery) {
        var items = unfilteredHistory.all
        if (activeFilterTypes.isNotEmpty()) {
            items = items.filter { activeFilterTypes.contains(it.type) }
        }
        if (searchQuery.isNotBlank()) {
            items = items.filter { it.text?.contains(searchQuery, ignoreCase = true) == true }
        }
        ClipboardHistory(items)
    }

    val gridState = rememberLazyStaggeredGridState()
    var popupItem by remember(filteredHistory) { mutableStateOf<ClipboardItem?>(null) }
    var showClearAllHistory by remember { mutableStateOf(false) }
    // DRS v1.9.0: the integrated clipboard system — an in-panel text editor
    // and a save-as-file dialog with a user-named file.
    var editingItem by remember { mutableStateOf<ClipboardItem?>(null) }
    var editingText by remember { mutableStateOf("") }
    var savingItem by remember { mutableStateOf<ClipboardItem?>(null) }
    var saveName by remember { mutableStateOf("") }
    // DRS v1.10.0: the popup editor's smart state — bounded undo/redo,
    // font customization, and the find/replace engine.
    val editorHistory = remember { ClipEditorHistory() }
    var editorFont by remember { mutableStateOf(ClipFontOption.DEFAULT) }
    var editorFontSize by remember { mutableStateOf(ClipFontSizeOption.NORMAL) }
    var findQuery by remember { mutableStateOf("") }
    var replaceQuery by remember { mutableStateOf("") }
    var matchCase by remember { mutableStateOf(false) }
    var activeMatch by remember { mutableStateOf(0) }

    fun isPopupSurfaceActive() = popupItem != null || showClearAllHistory ||
        editingItem != null || savingItem != null

    // DRS v1.10.0: shares any text through the system share sheet. The
    // chooser activity needs FLAG_ACTIVITY_NEW_TASK because the IME
    // service context is not an activity context.
    fun shareText(text: String) {
        val send = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
        runCatching {
            context.startActivity(
                Intent.createChooser(send, context.getString(R.string.clip__share_item))
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
            )
        }
    }

    LaunchedEffect(isFilterRowShown) {
        delay(AnimationDuration.toLong())
        if (!isFilterRowShown) {
            activeFilterTypes.clear()
        }
    }

    LaunchedEffect(activeFilterTypes.toSet()) {
        gridState.scrollToItem(0)
    }

    @Composable
    fun HeaderRow() {
        SnyggRow(DrsImeUi.ClipboardHeader.elementName,
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
                SnyggIcon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                )
            }
            SnyggText(
                elementName = DrsImeUi.ClipboardHeaderText.elementName,
                modifier = Modifier.weight(1f),
                text = stringRes(R.string.clipboard__header_title),
            )
            SnyggIconButton(
                elementName = DrsImeUi.ClipboardHeaderButton.elementName,
                onClick = { scope.launch { prefs.clipboard.historyEnabled.set(!historyEnabled) } },
                modifier = sizeModifier.autoMirrorForRtl(),
                enabled = !deviceLocked && !isPopupSurfaceActive(),
            ) {
                SnyggIcon(
                    imageVector = if (historyEnabled) {
                        Icons.Default.ToggleOn
                    } else {
                        Icons.Default.ToggleOff
                    },
                )
            }
            SnyggIconButton(
                elementName = DrsImeUi.ClipboardHeaderButton.elementName,
                onClick = { showClearAllHistory = true },
                modifier = sizeModifier.autoMirrorForRtl(),
                enabled = !deviceLocked && historyEnabled && filteredHistory.all.isNotEmpty() && !isPopupSurfaceActive(),
            ) {
                SnyggIcon(
                    imageVector = Icons.Default.DeleteSweep,
                )
            }
            SnyggIconButton(
                elementName = DrsImeUi.ClipboardHeaderButton.elementName,
                onClick = {
                    isSearchRowShown = !isSearchRowShown
                    if (!isSearchRowShown) searchQuery = ""
                },
                modifier = sizeModifier,
                enabled = !deviceLocked && historyEnabled && unfilteredHistory.all.isNotEmpty() && !isPopupSurfaceActive(),
            ) {
                SnyggIcon(
                    imageVector = if (!isSearchRowShown) {
                        Icons.Default.Search
                    } else {
                        Icons.Default.SearchOff
                    },
                )
            }
            SnyggIconButton(
                elementName = DrsImeUi.ClipboardHeaderButton.elementName,
                onClick = { isFilterRowShown = !isFilterRowShown },
                modifier = sizeModifier,
                enabled = !deviceLocked && historyEnabled && unfilteredHistory.all.isNotEmpty() && !isPopupSurfaceActive(),
            ) {
                SnyggIcon(
                    imageVector = if (!isFilterRowShown) {
                        Icons.Default.FilterList
                    } else {
                        Icons.Default.FilterListOff
                    },
                )
            }
            KeyboardLikeButton(
                modifier = sizeModifier,
                inputEventDispatcher = keyboardManager.inputEventDispatcher,
                keyData = TextKeyData.DELETE,
                elementName = DrsImeUi.ClipboardHeaderButton.elementName,
            ) {
                SnyggIcon(imageVector = Icons.AutoMirrored.Outlined.Backspace)
            }
        }
    }

    @Composable
    fun ClipItemView(
        elementName: String,
        item: ClipboardItem,
        contentScrollInsteadOfClip: Boolean,
        modifier: Modifier = Modifier,
    ) {
        val attributes = remember(item) {
            mapOf("type" to item.type.toString().lowercase())
        }
        SnyggBox(
            elementName = elementName,
            attributes = attributes,
            modifier = modifier.fillMaxWidth(),
            clickAndSemanticsModifier = Modifier.combinedClickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(),
                enabled = popupItem == null,
                onLongClick = {
                    popupItem = item
                },
                onClick = {
                    clipboardManager.pasteItem(item)
                },
            ),
        ) {
            if (item.type == ItemType.IMAGE) {
                val id = ContentUris.parseId(item.uri!!)
                val file = ClipboardFileStorage.getFileForId(context, id)
                val bitmap = remember(id) {
                    runCatching {
                        check(file.exists()) { "Unable to resolve image at ${file.absolutePath}" }
                        val rawBitmap = BitmapFactory.decodeFile(file.absolutePath)
                        checkNotNull(rawBitmap) { "Unable to decode image at ${file.absolutePath}" }
                        rawBitmap.asImageBitmap()
                    }
                }
                if (bitmap.isSuccess) {
                    Image(
                        modifier = Modifier.fillMaxWidth(),
                        bitmap = bitmap.getOrThrow(),
                        contentDescription = null,
                        contentScale = ContentScale.FillWidth,
                    )
                } else {
                    SnyggText(
                        modifier = Modifier.fillMaxWidth(),
                        text = bitmap.exceptionOrNull()?.message ?: "Unknown error",
                    )
                }
            } else if (item.type == ItemType.VIDEO) {
                val id = ContentUris.parseId(item.uri!!)
                val file = ClipboardFileStorage.getFileForId(context, id)
                val bitmap = remember(id) {
                    runCatching {
                        check(file.exists()) { "Unable to resolve video at ${file.absolutePath}" }
                        val rawBitmap = if (AndroidVersion.ATLEAST_API29_Q) {
                            val dataRetriever = MediaMetadataRetriever()
                            dataRetriever.setDataSource(file.absolutePath)
                            val width = dataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)
                            val height = dataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)
                            ThumbnailUtils.createVideoThumbnail(file, Size(width!!.toInt(), height!!.toInt()), null)
                        } else {
                            @Suppress("DEPRECATION")
                            ThumbnailUtils.createVideoThumbnail(file.absolutePath, MediaStore.Video.Thumbnails.MINI_KIND)
                        }
                        checkNotNull(rawBitmap) { "Unable to decode video at ${file.absolutePath}" }
                        rawBitmap.asImageBitmap()
                    }
                }
                if (bitmap.isSuccess) {
                    Image(
                        modifier = Modifier.fillMaxWidth(),
                        bitmap = bitmap.getOrThrow(),
                        contentDescription = null,
                        contentScale = ContentScale.FillWidth,
                    )
                    Icon(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(start = 4.dp, bottom = 4.dp)
                            .background(Color.White, CircleShape),
                        imageVector = Icons.Default.Videocam,
                        contentDescription = null,
                        tint = Color.Black,
                    )
                } else {
                    SnyggText(
                        modifier = Modifier.fillMaxWidth(),
                        text = bitmap.exceptionOrNull()?.message ?: "Unknown error",
                    )
                }
            } else {
                val text = item.stringRepresentation()
                Column {
                    ClipTextItemDescription(
                        elementName = DrsImeUi.ClipboardItemDescription.elementName,
                        attributes = attributes,
                        text = text,
                    )
                    SnyggText(
                        modifier = Modifier
                            .fillMaxWidth()
                            .run { if (contentScrollInsteadOfClip) this.drsVerticalScroll() else this },
                        text = item.displayText(),
                    )
                }
            }
            // DRS v1.9.0: a real state badge on pinned tiles — the pin is
            // the first-class citizen of the smart clipboard system.
            if (item.isPinned) {
                Icon(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                        .background(Color.White, CircleShape),
                    imageVector = Icons.Outlined.PushPin,
                    contentDescription = null,
                    tint = Color.Black,
                )
            }
        }
    }

    @Composable
    fun SearchRow() {
        if (!isSearchRowShown) return
        // DRS v1.7.0: the search text used to be hardcoded WHITE — on any
        // light theme (drs_day and friends) it was invisible while typing.
        // It follows the themed window foreground now, falling back to a
        // luminance-derived readable color when the theme leaves it open.
        val windowStyle = rememberSnyggThemeQuery(DrsImeUi.Window.elementName)
        val themedForeground = windowStyle.foreground()
        val searchTextColor = if (themedForeground.isSpecified) {
            themedForeground
        } else {
            windowStyle.background()
                .takeIf { it.isSpecified }
                ?.let { readableTextColor(it) }
                ?: Color.White
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BasicTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.weight(1f),
                singleLine = true,
                textStyle = LocalTextStyle.current.copy(color = searchTextColor, fontSize = 14.sp),
                decorationBox = { innerTextField ->
                    Box {
                        if (searchQuery.isEmpty()) {
                            Text(
                                text = stringRes(R.string.clipboard__search_hint),
                                color = searchTextColor.copy(alpha = 0.6f),
                                fontSize = 14.sp,
                            )
                        }
                        innerTextField()
                    }
                },
            )
            if (searchQuery.isNotEmpty()) {
                SnyggIconButton(
                    elementName = DrsImeUi.ClipboardHeaderButton.elementName,
                    onClick = { searchQuery = "" },
                ) {
                    SnyggIcon(imageVector = Icons.Default.Close)
                }
            }
        }
    }

    @Composable
    fun HistoryMainView() {
        SnyggBox(DrsImeUi.ClipboardContent.elementName,
            modifier = Modifier.fillMaxSize(),
        ) {
            val historyAlpha by animateFloatAsState(targetValue = if (isPopupSurfaceActive()) 0.12f else 1f)
            val staggeredGridCells by prefs.clipboard.historyNumGridColumns()
                .observeAsTransformingState { numGridColumns ->
                    if (numGridColumns == CLIPBOARD_HISTORY_NUM_GRID_COLUMNS_AUTO) {
                        StaggeredGridCells.Adaptive(160.dp)
                    } else {
                        StaggeredGridCells.Fixed(numGridColumns)
                    }
                }

            fun LazyStaggeredGridScope.clipboardItems(
                items: List<ClipboardItem>,
                key: String,
                @StringRes title: Int,
            ) {
                if (items.isNotEmpty()) {
                    item(key, span = StaggeredGridItemSpan.FullLine) {
                        ClipCategoryTitle(text = stringRes(title))
                    }
                    items(items) { item ->
                        ClipItemView(
                            elementName = DrsImeUi.ClipboardItem.elementName,
                            item = item,
                            contentScrollInsteadOfClip = false,
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .matchParentSize()
                    .alpha(historyAlpha),
            ) {
                AnimatedVisibility(
                    visible = isFilterRowShown,
                    enter = VerticalEnterTransition,
                    exit = VerticalExitTransition,
                ) {
                    SnyggRow(
                        elementName = DrsImeUi.ClipboardFilterRow.elementName,
                        modifier = Modifier.fillMaxWidth(),
                        clickAndSemanticsModifier = Modifier.drsHorizontalScroll(),
                    ) {
                        @Composable
                        fun FilterChip(
                            imageVector: ImageVector,
                            text: String,
                            itemType: ItemType,
                        ) {
                            val active = activeFilterTypes.contains(itemType)
                            val attributes = remember(active) {
                                mapOf(
                                    "state" to if (active) "active" else "inactive",
                                    "type" to itemType.toString().lowercase(),
                                )
                            }
                            SnyggChip(
                                elementName = DrsImeUi.ClipboardFilterChip.elementName,
                                attributes = attributes,
                                onClick = {
                                    if (!activeFilterTypes.add(itemType)) {
                                        activeFilterTypes.remove(itemType)
                                    }
                                },
                                imageVector = imageVector,
                                text = text,
                            )
                        }

                        FilterChip(
                            imageVector = Icons.Default.TextFields,
                            text = "Text",
                            itemType = ItemType.TEXT,
                        )
                        FilterChip(
                            imageVector = Icons.Default.Image,
                            text = "Images",
                            itemType = ItemType.IMAGE,
                        )
                        FilterChip(
                            imageVector = Icons.Default.Movie,
                            text = "Videos",
                            itemType = ItemType.VIDEO,
                        )
                    }
                }
                SnyggBox(DrsImeUi.ClipboardGrid.elementName,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                ) {
                    LazyVerticalStaggeredGrid(
                        modifier = Modifier.fillMaxSize(),
                        state = gridState,
                        columns = staggeredGridCells,
                    ) {
                        clipboardItems(
                            items = filteredHistory.pinned,
                            key = "pinned-header",
                            title = R.string.clipboard__group_pinned,
                        )
                        clipboardItems(
                            items = filteredHistory.recent,
                            key = "recent-header",
                            title = R.string.clipboard__group_recent,
                        )
                        clipboardItems(
                            items = filteredHistory.other,
                            key = "other-header",
                            title = R.string.clipboard__group_other,
                        )
                    }
                }
            }

            if (popupItem != null) {
                SnyggRow(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            detectTapGestures { popupItem = null }
                        },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceAround,
                ) {
                    SnyggColumn(modifier = Modifier.weight(0.5f)) {
                        ClipItemView(
                            elementName = DrsImeUi.ClipboardItemPopup.elementName,
                            modifier = Modifier
                                .widthIn(max = ItemWidth)
                                .weight(1f, fill = false),
                            item = popupItem!!,
                            contentScrollInsteadOfClip = true,
                        )
                        SnyggBox(DrsImeUi.ClipboardItemTimestamp.elementName) {
                            val formatter = LocalLocalizedDateTimeFormatter.current
                            SnyggText(
                                modifier = Modifier.fillMaxWidth(),
                                text = formatter.format(Instant.ofEpochMilli(popupItem!!.creationTimestampMs)),
                            )
                        }
                    }
                    SnyggColumn(modifier = Modifier.weight(0.5f)) {
                        SnyggColumn(DrsImeUi.ClipboardItemActions.elementName) {
                            // DRS v1.9.0: the organized action ladder of the
                            // smart clipboard — paste, copy back, pin, edit,
                            // save as file, delete (edit/save for text only).
                            PopupAction(
                                icon = Icons.Outlined.ContentPasteGo,
                                text = stringRes(R.string.clip__paste_item),
                            ) {
                                clipboardManager.pasteItem(popupItem!!)
                                popupItem = null
                            }
                            // DRS v1.0.6: copy the item back to the system
                            // clipboard without inserting it anywhere.
                            PopupAction(
                                icon = Icons.Default.ContentCopy,
                                text = stringRes(R.string.clip__copy_item_again),
                            ) {
                                clipboardManager.copyItemBack(popupItem!!)
                                popupItem = null
                            }
                            PopupAction(
                                icon = Icons.Outlined.PushPin,
                                text = stringRes(if (popupItem!!.isPinned) {
                                    R.string.clip__unpin_item
                                } else {
                                    R.string.clip__pin_item
                                }),
                            ) {
                                if (popupItem!!.isPinned) {
                                    clipboardManager.unpinClip(popupItem!!)
                                } else {
                                    clipboardManager.pinClip(popupItem!!)
                                }
                                popupItem = null
                            }
                            if (popupItem!!.type == ItemType.TEXT) {
                                // DRS v1.10.0: share straight from the item
                                // ladder through the system share sheet.
                                PopupAction(
                                    icon = Icons.Default.Share,
                                    text = stringRes(R.string.clip__share_item),
                                ) {
                                    shareText(popupItem!!.text.orEmpty())
                                    popupItem = null
                                }
                                PopupAction(
                                    icon = Icons.Default.Edit,
                                    text = stringRes(R.string.clip__edit_item),
                                ) {
                                    editingItem = popupItem!!
                                    editingText = popupItem!!.text.orEmpty()
                                    // DRS v1.10.0: a fresh editor session.
                                    editorHistory.clear()
                                    editorFont = ClipFontOption.DEFAULT
                                    editorFontSize = ClipFontSizeOption.NORMAL
                                    findQuery = ""
                                    replaceQuery = ""
                                    matchCase = false
                                    activeMatch = 0
                                    popupItem = null
                                }
                                PopupAction(
                                    icon = Icons.Default.SaveAlt,
                                    text = stringRes(R.string.clip__save_item_as_file),
                                ) {
                                    savingItem = popupItem!!
                                    saveName = ClipFileNamer.defaultFileName(
                                        System.currentTimeMillis(), ZoneId.systemDefault(),
                                    )
                                    popupItem = null
                                }
                            }
                            PopupAction(
                                icon = Icons.Default.Delete,
                                text = stringRes(R.string.clip__delete_item),
                            ) {
                                clipboardManager.deleteClip(popupItem!!, onlyIfUnpinned = false)
                                popupItem = null
                            }
                        }
                    }
                }
            }

            // DRS v1.10.0: the small v1.9.0 edit dialog was promoted to the
            // full-panel popup smart editor (ClipEditorScreen) that takes
            // over the whole clipboard surface — see the root composition.

            // DRS v1.9.0: save-as-file with a user-named file — the name is
            // normalized by ClipFileNamer and written by ClipFileSaver
            // (MediaStore Downloads on Android 10+, private dir before).
            if (savingItem != null) {
                SnyggRow(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            detectTapGestures { savingItem = null }
                        },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceAround,
                ) {
                    SnyggColumn(
                        elementName = DrsImeUi.ClipboardClearAllDialog.elementName,
                        modifier = Modifier
                            .width(EditDialogWidth)
                            .pointerInput(Unit) {
                                detectTapGestures { /* Do nothing */ }
                            },
                    ) {
                        SnyggText(
                            elementName = DrsImeUi.ClipboardHeaderText.elementName,
                            text = stringRes(R.string.clip__save_file_title),
                        )
                        BasicTextField(
                            value = saveName,
                            onValueChange = { saveName = it },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            textStyle = LocalTextStyle.current.copy(fontSize = 14.sp),
                            decorationBox = { innerTextField ->
                                Box {
                                    if (saveName.isEmpty()) {
                                        Text(
                                            text = stringRes(R.string.clip__save_file_name_hint),
                                            color = LocalTextStyle.current.color.copy(alpha = 0.6f),
                                            fontSize = 14.sp,
                                        )
                                    }
                                    innerTextField()
                                }
                            },
                        )
                        SnyggText(
                            elementName = DrsImeUi.ClipboardItemTimestamp.elementName,
                            text = stringRes(R.string.clip__save_file_note),
                        )
                        SnyggRow(DrsImeUi.ClipboardClearAllDialogButtons.elementName) {
                            Spacer(modifier = Modifier.weight(1f))
                            SnyggButton(
                                elementName = DrsImeUi.ClipboardClearAllDialogButton.elementName,
                                attributes = mapOf("action" to "no"),
                                onClick = { savingItem = null },
                            ) {
                                SnyggText(text = stringRes(R.string.action__cancel))
                            }
                            SnyggButton(
                                elementName = DrsImeUi.ClipboardClearAllDialogButton.elementName,
                                attributes = mapOf("action" to "yes"),
                                onClick = {
                                    val item = savingItem!!
                                    val name = ClipFileNamer.sanitize(saveName)
                                    savingItem = null
                                    scope.launch {
                                        val result = withContext(Dispatchers.IO) {
                                            ClipFileSaver.save(context, item.text.orEmpty(), name)
                                        }
                                        val message = when (result) {
                                            is ClipFileSaver.Result.PublicDownloads ->
                                                R.string.clip__saved_to_downloads
                                            is ClipFileSaver.Result.PrivateFiles ->
                                                R.string.clip__saved_to_app_files
                                            ClipFileSaver.Result.Failed ->
                                                R.string.clip__save_failed
                                        }
                                        context.showShortToastSync(message)
                                    }
                                },
                            ) {
                                SnyggText(text = stringRes(R.string.action__save))
                            }
                        }
                    }
                }
            }

            if (showClearAllHistory) {
                SnyggRow(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            detectTapGestures { showClearAllHistory = false }
                        },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceAround,
                ) {
                    SnyggColumn(
                        elementName = DrsImeUi.ClipboardClearAllDialog.elementName,
                        modifier = Modifier
                            .width(DialogWidth)
                            .pointerInput(Unit) {
                                detectTapGestures { /* Do nothing */ }
                            },
                    ) {
                        SnyggText(
                            elementName = DrsImeUi.ClipboardClearAllDialogMessage.elementName,
                            text = stringRes(
                                if (isFilterRowShown) {
                                    R.string.clipboard__confirm_clear_filtered_history__message
                                } else {
                                    R.string.clipboard__confirm_clear_unfiltered_history__message
                                }
                            ),
                        )
                        SnyggRow(DrsImeUi.ClipboardClearAllDialogButtons.elementName) {
                            Spacer(modifier = Modifier.weight(1f))
                            SnyggButton(
                                elementName = DrsImeUi.ClipboardClearAllDialogButton.elementName,
                                attributes = mapOf("action" to "no"),
                                onClick = {
                                    showClearAllHistory = false
                                },
                            ) {
                                SnyggText(
                                    text = stringRes(R.string.action__no),
                                )
                            }
                            SnyggButton(
                                elementName = DrsImeUi.ClipboardClearAllDialogButton.elementName,
                                attributes = mapOf("action" to "yes"),
                                onClick = {
                                    clipboardManager.clearExactHistory(filteredHistory.unpinned)
                                    context.showShortToastSync(R.string.clipboard__cleared_history)
                                    showClearAllHistory = false
                                },
                            ) {
                                SnyggText(
                                    text = stringRes(R.string.action__yes),
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // DRS v1.10.0: the popup smart editor — a full-panel takeover replacing
    // the small v1.9.0 dialog. Find/replace with match navigation and case
    // control, sixteen smart transform/extract chips, font family and size
    // customization, live statistics, bounded undo/redo, share, and
    // save-as-file — all on top of the same engine-owned pure core.
    @Composable
    fun ClipEditorScreen() {
        val matches = remember(editingText, findQuery, matchCase) {
            ClipSearchEngine.findMatches(editingText, findQuery, ignoreCase = !matchCase)
        }
        val activeIndex = if (matches.isEmpty()) -1 else activeMatch.coerceIn(0, matches.size - 1)

        fun applyTransform(newText: String) {
            if (newText != editingText) {
                editorHistory.push(editingText)
                editingText = ClipboardTextPolicy.truncateForStorage(newText)
            }
        }

        SnyggColumn(
            elementName = DrsImeUi.ClipboardContent.elementName,
            modifier = Modifier.fillMaxSize(),
        ) {
            // Header: back, title, share, save-as-file.
            SnyggRow(
                elementName = DrsImeUi.ClipboardHeader.elementName,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(DrsImeSizing.smartbarHeight),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                val headerButton = Modifier
                    .sizeIn(maxHeight = DrsImeSizing.smartbarHeight)
                    .aspectRatio(1f)
                SnyggIconButton(
                    elementName = DrsImeUi.ClipboardHeaderButton.elementName,
                    onClick = { editingItem = null },
                    modifier = headerButton,
                ) {
                    SnyggIcon(imageVector = Icons.AutoMirrored.Filled.ArrowBack)
                }
                SnyggText(
                    elementName = DrsImeUi.ClipboardHeaderText.elementName,
                    modifier = Modifier.weight(1f),
                    text = stringRes(R.string.clip__editor_title),
                )
                SnyggIconButton(
                    elementName = DrsImeUi.ClipboardHeaderButton.elementName,
                    onClick = { shareText(editingText) },
                    modifier = headerButton,
                ) {
                    SnyggIcon(imageVector = Icons.Default.Share)
                }
                SnyggIconButton(
                    elementName = DrsImeUi.ClipboardHeaderButton.elementName,
                    onClick = {
                        savingItem = editingItem
                        saveName = ClipFileNamer.defaultFileName(
                            System.currentTimeMillis(), ZoneId.systemDefault(),
                        )
                        editingItem = null
                    },
                    modifier = headerButton,
                ) {
                    SnyggIcon(imageVector = Icons.Default.SaveAlt)
                }
            }

            // Live statistics.
            val stats = ClipTextStats.of(editingText)
            SnyggText(
                elementName = DrsImeUi.ClipboardItemTimestamp.elementName,
                modifier = Modifier.fillMaxWidth(),
                text = stringRes(
                    R.string.clip__stats_label,
                    "chars" to stats.chars,
                    "words" to stats.words,
                    "lines" to stats.lines,
                ),
            )

            // Find row: query, case toggle, match counter, navigation.
            SnyggRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                BasicTextField(
                    value = findQuery,
                    onValueChange = {
                        findQuery = it
                        activeMatch = 0
                    },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    textStyle = LocalTextStyle.current.copy(fontSize = 14.sp),
                    decorationBox = { innerTextField ->
                        Box {
                            if (findQuery.isEmpty()) {
                                Text(
                                    text = stringRes(R.string.clip__editor_find_hint),
                                    color = LocalTextStyle.current.color.copy(alpha = 0.6f),
                                    fontSize = 14.sp,
                                )
                            }
                            innerTextField()
                        }
                    },
                )
                SnyggChip(
                    elementName = DrsImeUi.ClipboardFilterChip.elementName,
                    attributes = mapOf("state" to if (matchCase) "active" else "inactive"),
                    onClick = {
                        matchCase = !matchCase
                        activeMatch = 0
                    },
                    text = stringRes(R.string.clip__editor_match_case),
                )
                if (findQuery.isNotEmpty()) {
                    SnyggText(
                        elementName = DrsImeUi.ClipboardItemTimestamp.elementName,
                        text = if (matches.isEmpty()) {
                            stringRes(R.string.clip__editor_no_matches)
                        } else {
                            stringRes(
                                R.string.clip__editor_matches_counter,
                                "active" to (activeIndex + 1),
                                "count" to matches.size,
                            )
                        },
                    )
                    SnyggIconButton(
                        elementName = DrsImeUi.ClipboardHeaderButton.elementName,
                        onClick = {
                            if (matches.isNotEmpty()) {
                                activeMatch = ClipSearchEngine.prevMatchIndex(matches.size, activeIndex)
                            }
                        },
                    ) {
                        SnyggIcon(imageVector = Icons.Default.KeyboardArrowUp)
                    }
                    SnyggIconButton(
                        elementName = DrsImeUi.ClipboardHeaderButton.elementName,
                        onClick = {
                            if (matches.isNotEmpty()) {
                                activeMatch = ClipSearchEngine.nextMatchIndex(matches.size, activeIndex)
                            }
                        },
                    ) {
                        SnyggIcon(imageVector = Icons.Default.KeyboardArrowDown)
                    }
                    SnyggIconButton(
                        elementName = DrsImeUi.ClipboardHeaderButton.elementName,
                        onClick = {
                            findQuery = ""
                            replaceQuery = ""
                            activeMatch = 0
                        },
                    ) {
                        SnyggIcon(imageVector = Icons.Default.Close)
                    }
                }
            }

            // Replace row: appears only while a find query is active.
            if (findQuery.isNotEmpty()) {
                SnyggRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    BasicTextField(
                        value = replaceQuery,
                        onValueChange = { replaceQuery = it },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        textStyle = LocalTextStyle.current.copy(fontSize = 14.sp),
                        decorationBox = { innerTextField ->
                            Box {
                                if (replaceQuery.isEmpty()) {
                                    Text(
                                        text = stringRes(R.string.clip__editor_replace_hint),
                                        color = LocalTextStyle.current.color.copy(alpha = 0.6f),
                                        fontSize = 14.sp,
                                    )
                                }
                                innerTextField()
                            }
                        },
                    )
                    SnyggChip(
                        elementName = DrsImeUi.ClipboardFilterChip.elementName,
                        onClick = {
                            if (activeIndex in matches.indices) {
                                applyTransform(
                                    ClipSearchEngine.replaceOne(
                                        editingText, matches[activeIndex], replaceQuery,
                                    ),
                                )
                            }
                        },
                        text = stringRes(R.string.clip__editor_replace_one),
                    )
                    SnyggChip(
                        elementName = DrsImeUi.ClipboardFilterChip.elementName,
                        onClick = {
                            if (matches.isNotEmpty()) {
                                val (newText, count) = ClipSearchEngine.replaceAll(
                                    editingText, findQuery, replaceQuery, ignoreCase = !matchCase,
                                )
                                applyTransform(newText)
                                context.showShortToastSync(
                                    R.string.clip__editor_replace_done, "count" to count,
                                )
                            }
                        },
                        text = stringRes(R.string.clip__editor_replace_all),
                    )
                }
            }

            // Smart algorithms row: the case family (language-neutral
            // labels), the whitespace/line surgeries, the Arabic-aware
            // normalization, and the smart extractors.
            SnyggRow(
                elementName = DrsImeUi.ClipboardFilterRow.elementName,
                modifier = Modifier.fillMaxWidth(),
                clickAndSemanticsModifier = Modifier.drsHorizontalScroll(),
            ) {
                @Composable
                fun EditorChip(text: String, onClick: () -> Unit) {
                    SnyggChip(
                        elementName = DrsImeUi.ClipboardFilterChip.elementName,
                        onClick = onClick,
                        text = text,
                    )
                }

                EditorChip("AA") { applyTransform(ClipTextTransforms.toUpper(editingText)) }
                EditorChip("aa") { applyTransform(ClipTextTransforms.toLower(editingText)) }
                EditorChip("Aa") { applyTransform(ClipTextTransforms.toTitleCase(editingText)) }
                EditorChip("aA") { applyTransform(ClipTextTransforms.invertCase(editingText)) }
                EditorChip(stringRes(R.string.clip__transform_trim)) {
                    applyTransform(ClipTextTransforms.trimLines(editingText))
                }
                EditorChip(stringRes(R.string.clip__transform_collapse)) {
                    applyTransform(ClipTextTransforms.collapseHorizontalWhitespace(editingText))
                }
                EditorChip(stringRes(R.string.clip__transform_remove_empty)) {
                    applyTransform(ClipTextTransforms.removeEmptyLines(editingText))
                }
                EditorChip(stringRes(R.string.clip__transform_dedupe)) {
                    applyTransform(ClipTextTransforms.removeDuplicateLines(editingText))
                }
                EditorChip(stringRes(R.string.clip__transform_sort_asc)) {
                    applyTransform(ClipTextTransforms.sortLinesAscending(editingText))
                }
                EditorChip(stringRes(R.string.clip__transform_sort_desc)) {
                    applyTransform(ClipTextTransforms.sortLinesDescending(editingText))
                }
                EditorChip(stringRes(R.string.clip__transform_reverse)) {
                    applyTransform(ClipTextTransforms.reverseLines(editingText))
                }
                EditorChip(stringRes(R.string.clip__transform_no_diacritics)) {
                    applyTransform(ClipTextTransforms.removeArabicDiacritics(editingText))
                }
                EditorChip(stringRes(R.string.clip__transform_normalize)) {
                    applyTransform(ClipTextTransforms.normalizeArabicLetters(editingText))
                }
                // The extractors replace the text with the found lines
                // (undoable) and report the count honestly via a toast.
                EditorChip(stringRes(R.string.clip__extract_urls)) {
                    val found = ClipTextTransforms.extractUrls(editingText)
                    if (found.isEmpty()) {
                        context.showShortToastSync(R.string.clip__extract_none)
                    } else {
                        applyTransform(found.joinToString("\n"))
                        context.showShortToastSync(R.string.clip__extract_done, "count" to found.size)
                    }
                }
                EditorChip(stringRes(R.string.clip__extract_emails)) {
                    val found = ClipTextTransforms.extractEmails(editingText)
                    if (found.isEmpty()) {
                        context.showShortToastSync(R.string.clip__extract_none)
                    } else {
                        applyTransform(found.joinToString("\n"))
                        context.showShortToastSync(R.string.clip__extract_done, "count" to found.size)
                    }
                }
                EditorChip(stringRes(R.string.clip__extract_phones)) {
                    val found = ClipTextTransforms.extractPhoneNumbers(editingText)
                    if (found.isEmpty()) {
                        context.showShortToastSync(R.string.clip__extract_none)
                    } else {
                        applyTransform(found.joinToString("\n"))
                        context.showShortToastSync(R.string.clip__extract_done, "count" to found.size)
                    }
                }
            }

            // Font row: five families + four size steps.
            SnyggRow(
                elementName = DrsImeUi.ClipboardFilterRow.elementName,
                modifier = Modifier.fillMaxWidth(),
                clickAndSemanticsModifier = Modifier.drsHorizontalScroll(),
            ) {
                for (font in ClipFontOption.entries) {
                    SnyggChip(
                        elementName = DrsImeUi.ClipboardFilterChip.elementName,
                        attributes = mapOf("state" to if (font == editorFont) "active" else "inactive"),
                        onClick = { editorFont = font },
                        text = stringRes(
                            when (font) {
                                ClipFontOption.DEFAULT -> R.string.clip__font_default
                                ClipFontOption.SANS_SERIF -> R.string.clip__font_sans
                                ClipFontOption.SERIF -> R.string.clip__font_serif
                                ClipFontOption.MONOSPACE -> R.string.clip__font_mono
                                ClipFontOption.CURSIVE -> R.string.clip__font_cursive
                            },
                        ),
                    )
                }
                for (size in ClipFontSizeOption.entries) {
                    SnyggChip(
                        elementName = DrsImeUi.ClipboardFilterChip.elementName,
                        attributes = mapOf("state" to if (size == editorFontSize) "active" else "inactive"),
                        onClick = { editorFontSize = size },
                        text = size.spValue.toString(),
                    )
                }
            }

            // The editor field itself — takes the remaining height and
            // scrolls under the current editor font.
            val editorFontFamily = when (editorFont) {
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
                    .padding(horizontal = 8.dp)
                    .drsVerticalScroll(),
            ) {
                BasicTextField(
                    value = editingText,
                    onValueChange = { editingText = ClipboardTextPolicy.truncateForStorage(it) },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = LocalTextStyle.current.copy(
                        fontFamily = editorFontFamily,
                        fontSize = editorFontSize.spValue.sp,
                    ),
                )
            }

            // Storage-policy counter + undo/redo + cancel/save.
            SnyggText(
                elementName = DrsImeUi.ClipboardItemTimestamp.elementName,
                modifier = Modifier.fillMaxWidth(),
                text = stringRes(
                    R.string.clip__char_limit_counter,
                    "used" to editingText.length,
                    "max" to ClipboardTextPolicy.MAX_TEXT_CHARS,
                ),
            )
            SnyggRow(DrsImeUi.ClipboardClearAllDialogButtons.elementName) {
                SnyggIconButton(
                    elementName = DrsImeUi.ClipboardHeaderButton.elementName,
                    onClick = { editorHistory.undo(editingText)?.let { editingText = it } },
                    enabled = editorHistory.canUndo,
                ) {
                    SnyggIcon(imageVector = Icons.AutoMirrored.Filled.Undo)
                }
                SnyggIconButton(
                    elementName = DrsImeUi.ClipboardHeaderButton.elementName,
                    onClick = { editorHistory.redo(editingText)?.let { editingText = it } },
                    enabled = editorHistory.canRedo,
                ) {
                    SnyggIcon(imageVector = Icons.AutoMirrored.Filled.Redo)
                }
                Spacer(modifier = Modifier.weight(1f))
                SnyggButton(
                    elementName = DrsImeUi.ClipboardClearAllDialogButton.elementName,
                    attributes = mapOf("action" to "no"),
                    onClick = { editingItem = null },
                ) {
                    SnyggText(text = stringRes(R.string.action__cancel))
                }
                SnyggButton(
                    elementName = DrsImeUi.ClipboardClearAllDialogButton.elementName,
                    attributes = mapOf("action" to "yes"),
                    onClick = {
                        val edited = editingItem
                        editingItem = null
                        if (edited != null) {
                            clipboardManager.editClipText(edited, editingText)
                        }
                    },
                ) {
                    SnyggText(text = stringRes(R.string.action__save))
                }
            }
        }
    }

    @Composable
    fun HistoryEmptyView() {
        SnyggColumn(DrsImeUi.ClipboardContent.elementName,
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            SnyggText(
                text = stringRes(R.string.clipboard__empty__title),
            )
            SnyggText(
                text = stringRes(R.string.clipboard__empty__message),
            )
        }
    }

    @Composable
    fun HistoryDisabledView() {
        SnyggColumn(DrsImeUi.ClipboardContent.elementName,
            modifier = Modifier.fillMaxSize(),
        ) {
            SnyggText(
                elementName = DrsImeUi.ClipboardHistoryDisabledTitle.elementName,
                modifier = Modifier.padding(bottom = 8.dp),
                text = stringRes(R.string.clipboard__disabled__title),
            )
            SnyggText(
                elementName = DrsImeUi.ClipboardHistoryDisabledMessage.elementName,
                text = stringRes(R.string.clipboard__disabled__message),
            )
            SnyggButton(DrsImeUi.ClipboardHistoryDisabledButton.elementName,
                onClick = { scope.launch { prefs.clipboard.historyEnabled.set(true) } },
                modifier = Modifier.align(Alignment.End),
            ) {
                SnyggText(
                    text = stringRes(R.string.clipboard__disabled__enable_button),
                )
            }
        }
    }

    @Composable
    fun HistoryLockedView() {
        SnyggColumn(DrsImeUi.ClipboardContent.elementName,
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            SnyggText(
                elementName = DrsImeUi.ClipboardHistoryLockedTitle.elementName,
                text = stringRes(R.string.clipboard__locked__title),
            )
            SnyggText(
                elementName = DrsImeUi.ClipboardHistoryLockedMessage.elementName,
                text = stringRes(R.string.clipboard__locked__message),
            )
        }
    }

    SnyggColumn(
        modifier = modifier
            .fillMaxWidth()
            .height(DrsImeSizing.imeUiHeight()),
    ) {
        if (editingItem != null) {
            // DRS v1.10.0: the popup smart editor takes over the whole panel.
            ClipEditorScreen()
        } else {
            HeaderRow()
            SearchRow()
            if (deviceLocked) {
                HistoryLockedView()
            } else {
                if (historyEnabled) {
                    if (filteredHistory.all.isNotEmpty() || !activeFilterTypes.isEmpty() || searchQuery.isNotBlank()) {
                        HistoryMainView()
                    } else {
                        HistoryEmptyView()
                    }
                } else {
                    HistoryDisabledView()
                }
            }
        }
    }
}

/**
 * DRS v1.7.0: picks a readable text color for [background] by relative
 * luminance (the same threshold the system-bar contrast logic uses).
 * Pure and JVM-testable — the clipboard search row was hardcoded white
 * before, which was invisible on light themes.
 */
fun readableTextColor(background: Color): Color =
    if (background.luminance() >= 0.5f) Color.Black else Color.White

@Composable
private fun ClipCategoryTitle(
    text: String,
    modifier: Modifier = Modifier,
) {
    SnyggText(DrsImeUi.ClipboardSubheader.elementName,
        modifier = modifier.fillMaxWidth(),
        text = text.uppercase(),
    )
}

@Composable
private fun ClipTextItemDescription(
    elementName: String,
    attributes: SnyggQueryAttributes,
    text: String,
    modifier: Modifier = Modifier,
): Unit = with(LocalDensity.current) {
    val icon: ImageVector?
    val description: String?
    when {
        NetworkUtils.isEmailAddress(text) -> {
            icon = Icons.Outlined.Email
            description = stringRes(R.string.clipboard__item_description_email)
        }
        NetworkUtils.isUrl(text) -> {
            icon = Icons.Default.Link
            description = stringRes(R.string.clipboard__item_description_url)
        }
        NetworkUtils.isPhoneNumber(text) -> {
            icon = Icons.Default.Phone
            description = stringRes(R.string.clipboard__item_description_phone)
        }
        else -> {
            icon = null
            description = null
        }
    }
    if (icon != null && description != null) {
        SnyggRow(
            elementName = elementName,
            attributes = attributes,
            modifier = modifier,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SnyggIcon(
                imageVector = icon,
            )
            SnyggText(
                modifier = Modifier.weight(1f),
                text = description,
            )
        }
    }
}

@Composable
private fun PopupAction(
    icon: ImageVector,
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    SnyggRow(DrsImeUi.ClipboardItemAction.elementName,
        modifier = modifier.rippleClickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SnyggIcon(DrsImeUi.ClipboardItemActionIcon.elementName,
            imageVector = icon,
        )
        SnyggText(DrsImeUi.ClipboardItemActionText.elementName,
            modifier = Modifier.weight(1f),
            text = text,
        )
    }
}
