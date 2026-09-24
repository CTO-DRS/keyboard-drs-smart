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

import com.drs.smartkeyboard.ime.text.key.KeyCode
import com.drs.smartkeyboard.ime.text.key.KeyType

/**
 * DRS v1.0.7 — نظام «كلاهما» الموحد (Unified Dual System).
 *
 * This is the pure-logic core of the unified system: one catalogue of REAL
 * tools (every entry maps to a KeyCode the keyboard engine actually
 * handles), three display levels (simple / advanced / dual), and a scope
 * model (basic / advanced / shared) that resolves conflicts between the
 * normal and technical experiences instead of duplicating them.
 *
 * Nothing in this file touches Android UI or Context, so the whole
 * resolution layer is covered by JVM unit tests. Icon resources and
 * strings are resolved in the UI layer by tool id.
 */

/**
 * The three display levels of the unified system. The level only changes
 * WHAT IS VISIBLE - it never deletes settings, profiles, shortcuts,
 * clipboard data or themes, so switching is always lossless.
 */
enum class DrsHybridViewMode {
    /** الوضع البسيط: basic tools and essential options only. */
    SIMPLE,

    /** الوضع المتقدم: technical tools and expert controls. */
    ADVANCED,

    /** الوضع المزدوج: both worlds in the same session. */
    DUAL;

    companion object {
        /** Safe parse of a persisted name; unknown values fall back to DUAL. */
        fun fromNameSafe(name: String?): DrsHybridViewMode {
            return entries.firstOrNull { it.name == name } ?: DUAL
        }
    }
}

/**
 * Scope of a setting or tool inside the unified system. Scopes prevent
 * conflicts: a basic setting stays available to both levels, an advanced
 * setting is hidden until the user enables Advanced Controls, and a shared
 * setting belongs to the whole system rather than to one level.
 */
enum class DrsSettingScope {
    /** إعداد أساسي — shows for everyone. */
    BASIC,

    /** إعداد متقدم — hidden until Advanced Controls is on. */
    ADVANCED,

    /** إعداد مشترك — belongs to the whole system. */
    SHARED,
}

/** Which display level(s) a tool appears in. */
enum class DrsToolView {
    /** يظهر في الوضع البسيط فقط. */
    NORMAL,

    /** يظهر في الوضع المتقدم فقط. */
    TECHNICAL,

    /** يظهر في كلا الوضعين (والمزدوج). */
    BOTH,
}

/** Display group of a tool inside the unified tools manager. */
enum class DrsToolGroup {
    /** أدوات سريعة: panels, emoji, clipboard, language… */
    TOOLS,

    /** تحرير: undo/redo, copy/cut/paste, selection. */
    EDITING,

    /** المؤشر: word/line cursor movement. */
    CURSOR,
}

/**
 * One entry of the unified tool catalogue. [code] is a KeyCode the
 * keyboard engine already handles (see KeyboardManager), so every tool
 * here performs a REAL action on the live keyboard - there are no
 * placeholder entries. UI-only attributes (icon, localized label) are
 * resolved from [id] in the UI layer.
 */
data class DrsUnifiedTool(
    /** Stable id persisted in DrsState customizations. */
    val id: String,

    /** KeyCode dispatched when the tool is tapped. */
    val code: Int,

    /** Key type of the dispatched event. */
    val type: KeyType,

    /** Scope inside the unified system. */
    val scope: DrsSettingScope,

    /** Default visibility across display levels. */
    val defaultView: DrsToolView,

    /** Default pin state (pinned tools float to the strip head). */
    val defaultPinned: Boolean = false,

    /** Group used by the tools manager. */
    val group: DrsToolGroup,
)

/**
 * The unified tool catalogue. Keys come from the same catalogues the
 * engine handles (TextKeyData/SwipeAction handling paths), so wiring a
 * tool into the strip is exactly as real as the keys of the physical
 * layout itself.
 */
object DrsUnifiedTools {

    val EMOJI = DrsUnifiedTool(
        id = "emoji",
        code = KeyCode.IME_UI_MODE_MEDIA,
        type = KeyType.SYSTEM_GUI,
        scope = DrsSettingScope.BASIC,
        defaultView = DrsToolView.BOTH,
        defaultPinned = true,
        group = DrsToolGroup.TOOLS,
    )

    val CLIPBOARD = DrsUnifiedTool(
        id = "clipboard",
        code = KeyCode.IME_UI_MODE_CLIPBOARD,
        type = KeyType.SYSTEM_GUI,
        scope = DrsSettingScope.BASIC,
        defaultView = DrsToolView.BOTH,
        defaultPinned = true,
        group = DrsToolGroup.TOOLS,
    )

    val NUMBERS = DrsUnifiedTool(
        id = "numbers",
        code = KeyCode.VIEW_NUMERIC,
        type = KeyType.SYSTEM_GUI,
        scope = DrsSettingScope.BASIC,
        defaultView = DrsToolView.BOTH,
        group = DrsToolGroup.TOOLS,
    )

    val SYMBOLS = DrsUnifiedTool(
        id = "symbols",
        code = KeyCode.VIEW_SYMBOLS,
        type = KeyType.SYSTEM_GUI,
        scope = DrsSettingScope.BASIC,
        defaultView = DrsToolView.BOTH,
        group = DrsToolGroup.TOOLS,
    )

    val LANGUAGE = DrsUnifiedTool(
        id = "language",
        // DRS v1.5.0 fix: this tool pointed at IME_SUBTYPE_PICKER (-224),
        // a code with NO handler — pressing it silently logged "unknown
        // key". SHOW_SUBTYPE_PICKER is the real in-keyboard subtype picker
        // path (KeyboardManager sets isSubtypeSelectionVisible).
        code = KeyCode.SHOW_SUBTYPE_PICKER,
        type = KeyType.FUNCTION,
        scope = DrsSettingScope.BASIC,
        defaultView = DrsToolView.BOTH,
        group = DrsToolGroup.TOOLS,
    )

    val SETTINGS = DrsUnifiedTool(
        id = "settings",
        code = KeyCode.SETTINGS,
        type = KeyType.FUNCTION,
        scope = DrsSettingScope.BASIC,
        defaultView = DrsToolView.NORMAL,
        group = DrsToolGroup.TOOLS,
    )

    val PASTE = DrsUnifiedTool(
        id = "paste",
        code = KeyCode.CLIPBOARD_PASTE,
        type = KeyType.SYSTEM_GUI,
        scope = DrsSettingScope.BASIC,
        defaultView = DrsToolView.NORMAL,
        group = DrsToolGroup.EDITING,
    )

    val SHARE = DrsUnifiedTool(
        id = "share",
        code = KeyCode.CLIPBOARD_SHARE,
        type = KeyType.SYSTEM_GUI,
        scope = DrsSettingScope.BASIC,
        defaultView = DrsToolView.NORMAL,
        group = DrsToolGroup.TOOLS,
    )

    val TEXT_TOOLS = DrsUnifiedTool(
        id = "text_tools",
        code = KeyCode.IME_UI_MODE_TEXT_TOOLS,
        type = KeyType.SYSTEM_GUI,
        scope = DrsSettingScope.SHARED,
        defaultView = DrsToolView.BOTH,
        defaultPinned = true,
        group = DrsToolGroup.TOOLS,
    )

    val UNDO = DrsUnifiedTool(
        id = "undo",
        code = KeyCode.UNDO,
        type = KeyType.SYSTEM_GUI,
        scope = DrsSettingScope.SHARED,
        defaultView = DrsToolView.BOTH,
        group = DrsToolGroup.EDITING,
    )

    val REDO = DrsUnifiedTool(
        id = "redo",
        code = KeyCode.REDO,
        type = KeyType.SYSTEM_GUI,
        scope = DrsSettingScope.SHARED,
        defaultView = DrsToolView.BOTH,
        group = DrsToolGroup.EDITING,
    )

    val SELECT_ALL = DrsUnifiedTool(
        id = "select_all",
        code = KeyCode.CLIPBOARD_SELECT_ALL,
        type = KeyType.SYSTEM_GUI,
        scope = DrsSettingScope.SHARED,
        defaultView = DrsToolView.TECHNICAL,
        group = DrsToolGroup.EDITING,
    )

    val COPY = DrsUnifiedTool(
        id = "copy",
        code = KeyCode.CLIPBOARD_COPY,
        type = KeyType.SYSTEM_GUI,
        scope = DrsSettingScope.SHARED,
        defaultView = DrsToolView.TECHNICAL,
        group = DrsToolGroup.EDITING,
    )

    val CUT = DrsUnifiedTool(
        id = "cut",
        code = KeyCode.CLIPBOARD_CUT,
        type = KeyType.SYSTEM_GUI,
        scope = DrsSettingScope.SHARED,
        defaultView = DrsToolView.TECHNICAL,
        group = DrsToolGroup.EDITING,
    )

    val SELECT_WORD = DrsUnifiedTool(
        id = "select_word",
        code = KeyCode.CLIPBOARD_SELECT,
        type = KeyType.SYSTEM_GUI,
        scope = DrsSettingScope.SHARED,
        defaultView = DrsToolView.TECHNICAL,
        group = DrsToolGroup.EDITING,
    )

    val WORD_LEFT = DrsUnifiedTool(
        id = "word_left",
        code = KeyCode.MOVE_WORD_LEFT,
        type = KeyType.NAVIGATION,
        scope = DrsSettingScope.SHARED,
        defaultView = DrsToolView.TECHNICAL,
        group = DrsToolGroup.CURSOR,
    )

    val WORD_RIGHT = DrsUnifiedTool(
        id = "word_right",
        code = KeyCode.MOVE_WORD_RIGHT,
        type = KeyType.NAVIGATION,
        scope = DrsSettingScope.SHARED,
        defaultView = DrsToolView.TECHNICAL,
        group = DrsToolGroup.CURSOR,
    )

    val LINE_START = DrsUnifiedTool(
        id = "line_start",
        code = KeyCode.MOVE_START_OF_LINE,
        type = KeyType.NAVIGATION,
        scope = DrsSettingScope.SHARED,
        defaultView = DrsToolView.TECHNICAL,
        group = DrsToolGroup.CURSOR,
    )

    val LINE_END = DrsUnifiedTool(
        id = "line_end",
        code = KeyCode.MOVE_END_OF_LINE,
        type = KeyType.NAVIGATION,
        scope = DrsSettingScope.SHARED,
        defaultView = DrsToolView.TECHNICAL,
        group = DrsToolGroup.CURSOR,
    )

    val DELETE_WORD = DrsUnifiedTool(
        id = "delete_word",
        code = KeyCode.DELETE_WORD,
        type = KeyType.ENTER_EDITING,
        scope = DrsSettingScope.SHARED,
        defaultView = DrsToolView.TECHNICAL,
        group = DrsToolGroup.EDITING,
    )

    val HIDE_KEYBOARD = DrsUnifiedTool(
        id = "hide_keyboard",
        code = KeyCode.IME_HIDE_UI,
        type = KeyType.FUNCTION,
        scope = DrsSettingScope.BASIC,
        defaultView = DrsToolView.BOTH,
        group = DrsToolGroup.TOOLS,
    )

    /**
     * DRS v1.0.8: cycles the keyboard theme of the currently effective
     * day/night slot (real pref write — the live keyboard re-styles).
     */
    val THEME_CYCLE = DrsUnifiedTool(
        id = "theme_cycle",
        code = KeyCode.THEME_CYCLE,
        type = KeyType.FUNCTION,
        scope = DrsSettingScope.BASIC,
        defaultView = DrsToolView.BOTH,
        group = DrsToolGroup.TOOLS,
    )

    /**
     * DRS v1.0.8: commits the current date & time formatted with the
     * active subtype's locale through the normal commitText path.
     */
    val INSERT_DATE_TIME = DrsUnifiedTool(
        id = "insert_date_time",
        code = KeyCode.INSERT_DATE_TIME,
        type = KeyType.FUNCTION,
        scope = DrsSettingScope.BASIC,
        defaultView = DrsToolView.BOTH,
        group = DrsToolGroup.TOOLS,
    )

    /** DRS v1.0.8: moves the cursor to the very start of the whole text. */
    val TEXT_START = DrsUnifiedTool(
        id = "text_start",
        code = KeyCode.MOVE_START_OF_PAGE,
        type = KeyType.NAVIGATION,
        scope = DrsSettingScope.SHARED,
        defaultView = DrsToolView.TECHNICAL,
        group = DrsToolGroup.CURSOR,
    )

    /** DRS v1.0.8: moves the cursor to the very end of the whole text. */
    val TEXT_END = DrsUnifiedTool(
        id = "text_end",
        code = KeyCode.MOVE_END_OF_PAGE,
        type = KeyType.NAVIGATION,
        scope = DrsSettingScope.SHARED,
        defaultView = DrsToolView.TECHNICAL,
        group = DrsToolGroup.CURSOR,
    )

    /**
     * DRS v1.1.0: toggles the compact one-handed window mode through the
     * real window-controller action (same path as the existing on-panel
     * one-handed controls and its KeyCode dispatch).
     */
    val ONE_HANDED = DrsUnifiedTool(
        id = "one_handed",
        code = KeyCode.TOGGLE_COMPACT_LAYOUT,
        type = KeyType.SYSTEM_GUI,
        scope = DrsSettingScope.BASIC,
        defaultView = DrsToolView.BOTH,
        group = DrsToolGroup.TOOLS,
    )

    /**
     * DRS v1.1.0: toggles the top number row on the character layout —
     * a real pref write; the LayoutManager recomputes the layout through
     * the existing pref collector.
     */
    val NUMBER_ROW = DrsUnifiedTool(
        id = "number_row",
        code = KeyCode.TOGGLE_NUMBER_ROW,
        type = KeyType.FUNCTION,
        scope = DrsSettingScope.BASIC,
        defaultView = DrsToolView.BOTH,
        group = DrsToolGroup.TOOLS,
    )

    /**
     * DRS v1.2.0: toggles incognito typing through the real engine action
     * (same KeyCode the existing quick actions use — handled in
     * KeyboardManager.handleToggleIncognitoMode).
     */
    val INCOGNITO = DrsUnifiedTool(
        id = "incognito",
        code = KeyCode.TOGGLE_INCOGNITO_MODE,
        type = KeyType.FUNCTION,
        scope = DrsSettingScope.BASIC,
        defaultView = DrsToolView.BOTH,
        group = DrsToolGroup.TOOLS,
    )

    /**
     * DRS v1.2.0: toggles auto-correction through the real engine action
     * (same KeyCode the existing quick actions use — handled in
     * KeyboardManager.handleToggleAutocorrect).
     */
    val AUTOCORRECT = DrsUnifiedTool(
        id = "autocorrect",
        code = KeyCode.TOGGLE_AUTOCORRECT,
        type = KeyType.FUNCTION,
        scope = DrsSettingScope.BASIC,
        defaultView = DrsToolView.BOTH,
        group = DrsToolGroup.TOOLS,
    )

    /**
     * DRS v1.3.0: clears the primary clipboard through the real engine
     * action (KeyCode.CLIPBOARD_CLEAR_PRIMARY_CLIP — handled in
     * KeyboardManager with the existing history-aware path + toast).
     */
    val CLIPBOARD_CLEAR = DrsUnifiedTool(
        id = "clipboard_clear",
        code = KeyCode.CLIPBOARD_CLEAR_PRIMARY_CLIP,
        type = KeyType.FUNCTION,
        scope = DrsSettingScope.BASIC,
        defaultView = DrsToolView.BOTH,
        group = DrsToolGroup.EDITING,
    )

    /**
     * DRS v1.3.0: switches to the voice input method through the real
     * engine action (KeyCode.VOICE_INPUT — handled in KeyboardManager via
     * DrsImeService.switchToVoiceInputMethod).
     */
    val VOICE_INPUT = DrsUnifiedTool(
        id = "voice_input",
        code = KeyCode.VOICE_INPUT,
        type = KeyType.FUNCTION,
        scope = DrsSettingScope.BASIC,
        defaultView = DrsToolView.BOTH,
        group = DrsToolGroup.TOOLS,
    )

    /**
     * DRS v1.4.0: toggles the floating-window mode through the real engine
     * action (KeyCode.TOGGLE_FLOATING_WINDOW — handled in KeyboardManager
     * via windowController.actions.toggleFloatingWindow, the same path the
     * existing quick action uses).
     */
    val FLOATING_MODE = DrsUnifiedTool(
        id = "floating_mode",
        code = KeyCode.TOGGLE_FLOATING_WINDOW,
        type = KeyType.FUNCTION,
        scope = DrsSettingScope.BASIC,
        defaultView = DrsToolView.BOTH,
        group = DrsToolGroup.TOOLS,
    )

    /**
     * DRS v1.4.0: shows/hides the smart bar through the real engine action
     * (KeyCode.TOGGLE_SMARTBAR_VISIBILITY — handled in KeyboardManager on
     * the same path the swipe action uses).
     */
    val SMARTBAR_TOGGLE = DrsUnifiedTool(
        id = "smartbar_toggle",
        code = KeyCode.TOGGLE_SMARTBAR_VISIBILITY,
        type = KeyType.FUNCTION,
        scope = DrsSettingScope.BASIC,
        defaultView = DrsToolView.BOTH,
        group = DrsToolGroup.TOOLS,
    )

    /**
     * DRS v1.5.0: clears the UNPINNED clipboard history through the real
     * engine action (KeyCode.CLIPBOARD_CLEAR_HISTORY — handled in
     * KeyboardManager via clipboardManager.clearHistory). Pinned items
     * survive, so the tool is a safe one-tap history wipe.
     */
    val CLIPBOARD_HISTORY_CLEAR = DrsUnifiedTool(
        id = "clipboard_history_clear",
        code = KeyCode.CLIPBOARD_CLEAR_HISTORY,
        type = KeyType.FUNCTION,
        scope = DrsSettingScope.BASIC,
        defaultView = DrsToolView.BOTH,
        group = DrsToolGroup.EDITING,
    )

    /**
     * DRS v1.5.0: switches to the NEXT configured subtype through the real
     * engine action (KeyCode.IME_NEXT_SUBTYPE — handled in KeyboardManager
     * via subtypeManager.switchToNextSubtype), the same switch the
     * language key performs under its "next language" utility action.
     */
    val NEXT_LANGUAGE = DrsUnifiedTool(
        id = "next_language",
        code = KeyCode.IME_NEXT_SUBTYPE,
        type = KeyType.FUNCTION,
        scope = DrsSettingScope.BASIC,
        defaultView = DrsToolView.BOTH,
        group = DrsToolGroup.TOOLS,
    )

    /**
     * DRS v1.5.0: enters/leaves the window-resize mode through the real
     * engine action (KeyCode.TOGGLE_RESIZE_MODE — handled in
     * KeyboardManager via windowController.editor.toggleEnabled, the same
     * path the existing quick action uses).
     */
    val RESIZE_MODE = DrsUnifiedTool(
        id = "resize_mode",
        code = KeyCode.TOGGLE_RESIZE_MODE,
        type = KeyType.FUNCTION,
        scope = DrsSettingScope.BASIC,
        defaultView = DrsToolView.BOTH,
        group = DrsToolGroup.TOOLS,
    )

    /** The full basic/shared catalogue in default display order. */
    val ALL: List<DrsUnifiedTool> = listOf(
        EMOJI, CLIPBOARD, TEXT_TOOLS, NUMBERS, SYMBOLS, LANGUAGE,
        UNDO, REDO, SELECT_ALL, COPY, CUT, PASTE, SHARE,
        SELECT_WORD, WORD_LEFT, WORD_RIGHT, LINE_START, LINE_END,
        DELETE_WORD, HIDE_KEYBOARD, SETTINGS,
        // DRS v1.0.8: appended at the tail so every existing persisted
        // order/pin arrangement keeps its exact meaning.
        THEME_CYCLE, INSERT_DATE_TIME, TEXT_START, TEXT_END,
        // DRS v1.1.0: same tail-append contract for the new tools.
        ONE_HANDED, NUMBER_ROW,
        // DRS v1.2.0: same tail-append contract for the new tools.
        INCOGNITO, AUTOCORRECT,
        // DRS v1.3.0: same tail-append contract for the new tools.
        CLIPBOARD_CLEAR, VOICE_INPUT,
        // DRS v1.4.0: same tail-append contract for the new tools.
        FLOATING_MODE, SMARTBAR_TOGGLE,
        // DRS v1.5.0: same tail-append contract for the new tools.
        CLIPBOARD_HISTORY_CLEAR, NEXT_LANGUAGE, RESIZE_MODE,
    )

    private val BY_ID = ALL.associateBy { it.id }

    /** Looks a catalogue tool up by id (null for unknown ids). */
    fun byId(id: String): DrsUnifiedTool? = BY_ID[id]

    /**
     * Resolves the effective visibility of [tool] for the display level
     * [view], honoring the user's per-tool override. DUAL shows everything
     * the two single levels show, so a tool visible in BOTH simple and
     * advanced levels is visible in dual as well.
     */
    fun isVisibleIn(
        tool: DrsUnifiedTool,
        view: DrsHybridViewMode,
        viewOverride: String?,
    ): Boolean {
        val effective = when (viewOverride) {
            DrsToolView.NORMAL.name -> DrsToolView.NORMAL
            DrsToolView.TECHNICAL.name -> DrsToolView.TECHNICAL
            DrsToolView.BOTH.name -> DrsToolView.BOTH
            else -> tool.defaultView
        }
        return when (view) {
            DrsHybridViewMode.SIMPLE -> effective != DrsToolView.TECHNICAL
            DrsHybridViewMode.ADVANCED -> effective != DrsToolView.NORMAL
            DrsHybridViewMode.DUAL -> true
        }
    }

    /**
     * Orders the catalogue tools for the strip: pinned first (in pin
     * order), then the rest. [order] may be a partial permutation - tools
     * missing from it keep their catalogue order afterwards. Unknown ids
     * are dropped defensively so a corrupt customization can never break
     * the strip.
     */
    fun applyOrderAndPins(
        tools: List<DrsUnifiedTool>,
        order: List<String>,
        pinned: List<String>,
    ): List<DrsUnifiedTool> {
        val byId = tools.associateBy { it.id }
        val ordered = ArrayList<DrsUnifiedTool>(tools.size)
        // 1) explicit order (only known ids, in given order)
        for (id in order) {
            val tool = byId[id] ?: continue
            if (tool !in ordered) ordered += tool
        }
        // 2) catalogue order for the rest
        for (tool in tools) {
            if (tool !in ordered) ordered += tool
        }
        // 3) pins float to the head (most recently pinned first is not
        //    required - stable pin order keeps the strip predictable)
        val pinnedSet = pinned.toHashSet()
        val (pinnedTools, unpinned) = ordered.partition { it.id in pinnedSet }
        return pinnedTools + unpinned
    }

    /**
     * Full resolution of the unified strip content for one display level:
     * filters by visibility + hidden list, then applies order and pins.
     * Tools marked [DrsUnifiedTool.defaultPinned] float to the head unless
     * the user explicitly unpinned them. Pure and side-effect free - the
     * same function feeds the IME strip, the tools manager preview and the
     * unit tests.
     */
    fun resolveFor(
        view: DrsHybridViewMode,
        hidden: List<String>,
        pinned: List<String>,
        order: List<String>,
        viewOverrides: Map<String, String>,
    ): List<DrsUnifiedTool> {
        val hiddenSet = hidden.toHashSet()
        val defaultPinnedIds = ALL.filter { it.defaultPinned }.map { it.id }
        val effectivePinned = (pinned + defaultPinnedIds).distinct()
        val visible = ALL.filter { tool ->
            tool.id !in hiddenSet &&
                isVisibleIn(tool, view, viewOverrides[tool.id])
        }
        return applyOrderAndPins(visible, order, effectivePinned)
    }

    /**
     * Resolves which display level applies right now: the normal system
     * always lives in the simple level, the technical system in the
     * advanced level, and the hybrid system uses the user's stored level.
     */
    fun viewForSystem(userPath: String, hybridViewMode: String): DrsHybridViewMode {
        return when (userPath) {
            DrsUserPath.NORMAL.name -> DrsHybridViewMode.SIMPLE
            DrsUserPath.TECHNICAL.name -> DrsHybridViewMode.ADVANCED
            else -> DrsHybridViewMode.fromNameSafe(hybridViewMode)
        }
    }
}

/**
 * State mutation helpers for the unified layer. Every change is persisted
 * inside DrsState (the same atomic local JSON file as the rest of the DRS
 * layer) and never touches profiles, shortcuts, clipboard data or themes,
 * so level switching and customization are lossless by construction.
 */
object DrsUnified {

    /** Applies a new display level (persisted; no other state is touched). */
    fun setViewMode(mode: DrsHybridViewMode) {
        DrsStore.update { it.copy(hybridViewMode = mode.name) }
    }

    /** Cycles SIMPLE -> ADVANCED -> DUAL -> SIMPLE (strip quick toggle). */
    fun cycleViewMode(): DrsHybridViewMode {
        val next = when (DrsHybridViewMode.fromNameSafe(DrsStore.state.value.hybridViewMode)) {
            DrsHybridViewMode.SIMPLE -> DrsHybridViewMode.ADVANCED
            DrsHybridViewMode.ADVANCED -> DrsHybridViewMode.DUAL
            DrsHybridViewMode.DUAL -> DrsHybridViewMode.SIMPLE
        }
        setViewMode(next)
        return next
    }

    /** Gates the advanced settings groups of the unified System screen. */
    fun setAdvancedControls(enabled: Boolean) {
        DrsStore.update { it.copy(advancedControlsEnabled = enabled) }
    }

    /** Opts the normal system's keyboard into the unified strip. */
    fun setStripForNormal(enabled: Boolean) {
        DrsStore.update { it.copy(unifiedStripForNormal = enabled) }
    }

    /** Shows/hides one tool (hiding never deletes the customization). */
    fun setToolHidden(id: String, hidden: Boolean) {
        if (DrsUnifiedTools.byId(id) == null) return
        DrsStore.update { state ->
            state.copy(
                hiddenUnifiedTools = if (hidden) {
                    (state.hiddenUnifiedTools + id).distinct()
                } else {
                    state.hiddenUnifiedTools - id
                },
            )
        }
    }

    /** Pins/unpins one tool; pinned tools float to the strip head. */
    fun setToolPinned(id: String, pinned: Boolean) {
        if (DrsUnifiedTools.byId(id) == null) return
        DrsStore.update { state ->
            state.copy(
                pinnedUnifiedTools = if (pinned) {
                    (state.pinnedUnifiedTools + id).distinct()
                } else {
                    state.pinnedUnifiedTools - id
                },
            )
        }
    }

    /** Overrides which level(s) a tool appears in. */
    fun setToolView(id: String, view: DrsToolView) {
        if (DrsUnifiedTools.byId(id) == null) return
        DrsStore.update { state ->
            state.copy(unifiedToolViews = state.unifiedToolViews + (id to view.name))
        }
    }

    /** Moves a tool one slot up (towards the strip head) in the order. */
    fun moveTool(id: String, up: Boolean) {
        if (DrsUnifiedTools.byId(id) == null) return
        DrsStore.update { state ->
            val catalogueIds = DrsUnifiedTools.ALL.map { it.id }
            val current = catalogueIds.filter { it !in state.hiddenUnifiedTools }
            val ordered = if (state.unifiedToolOrder.isEmpty()) {
                current
            } else {
                // merge: explicit order first, then remaining catalogue ids
                val known = state.unifiedToolOrder.filter { it in catalogueIds }
                val rest = current.filter { it !in known }
                known + rest
            }
            val index = ordered.indexOf(id)
            if (index == -1) return@update state
            val target = if (up) index - 1 else index + 1
            if (target < 0 || target >= ordered.size) return@update state
            val mutable = ordered.toMutableList()
            val item = mutable.removeAt(index)
            mutable.add(target, item)
            state.copy(unifiedToolOrder = mutable)
        }
    }

    /** Clears ALL strip customizations back to the catalogue defaults. */
    fun resetCustomization() {
        DrsStore.update {
            it.copy(
                unifiedToolOrder = emptyList(),
                hiddenUnifiedTools = emptyList(),
                pinnedUnifiedTools = emptyList(),
                unifiedToolViews = emptyMap(),
            )
        }
    }

    /** DRS v1.0.8: master switch of the daily usage statistics recording. */
    fun setDailyStatsEnabled(enabled: Boolean) {
        DrsStore.update { it.copy(dailyStatsEnabled = enabled) }
    }

    /** DRS v1.0.8: wipes all recorded daily usage buckets (local privacy action). */
    fun resetDailyStats() {
        DrsStore.update { it.copy(dailyStats = emptyMap()) }
    }

    /**
     * DRS v1.1.0: stamps the moment of the last successful DRS-state backup
     * export (epoch millis). Consumed by the diagnostics backup-age check.
     */
    fun markBackupExported() {
        DrsStore.update { it.copy(lastBackupAt = System.currentTimeMillis()) }
    }
}
