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

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.shouldBe
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * DRS v1.0.7: unit tests for the unified «كلاهما» layer - the catalogue
 * integrity, display-level resolution, tool customization, shortcut
 * availability scopes, and backward-compatible state migration.
 */
class DrsUnifiedTest : FunSpec({

    // -----------------------------------------------------------
    // Catalogue integrity
    // -----------------------------------------------------------

    test("catalogue ids are unique and non-empty") {
        val ids = DrsUnifiedTools.ALL.map { it.id }
        ids.size shouldBe ids.toSet().size
        ids.none { it.isBlank() } shouldBe true
    }

    test("every catalogue tool maps to a real action code") {
        DrsUnifiedTools.ALL.all { it.code != 0 } shouldBe true
    }

    test("byId resolves every catalogue entry and rejects unknown") {
        DrsUnifiedTools.ALL.all { DrsUnifiedTools.byId(it.id) === it } shouldBe true
        DrsUnifiedTools.byId("nope") shouldBe null
    }

    // -----------------------------------------------------------
    // Display level resolution
    // -----------------------------------------------------------

    test("viewForSystem maps systems to levels") {
        DrsUnifiedTools.viewForSystem(DrsUserPath.NORMAL.name, DrsHybridViewMode.ADVANCED.name) shouldBe
            DrsHybridViewMode.SIMPLE
        DrsUnifiedTools.viewForSystem(DrsUserPath.TECHNICAL.name, DrsHybridViewMode.SIMPLE.name) shouldBe
            DrsHybridViewMode.ADVANCED
        DrsUnifiedTools.viewForSystem(DrsUserPath.HYBRID.name, DrsHybridViewMode.SIMPLE.name) shouldBe
            DrsHybridViewMode.SIMPLE
        DrsUnifiedTools.viewForSystem(DrsUserPath.HYBRID.name, DrsHybridViewMode.DUAL.name) shouldBe
            DrsHybridViewMode.DUAL
        // unknown stored values fall back to DUAL for the hybrid system
        DrsUnifiedTools.viewForSystem(DrsUserPath.HYBRID.name, "garbage") shouldBe DrsHybridViewMode.DUAL
    }

    test("simple level excludes technical-only tools") {
        val resolved = DrsUnifiedTools.resolveFor(
            view = DrsHybridViewMode.SIMPLE,
            hidden = emptyList(),
            pinned = emptyList(),
            order = emptyList(),
            viewOverrides = emptyMap(),
        )
        resolved.none { it.defaultView == DrsToolView.TECHNICAL } shouldBe true
        resolved.map { it.id } shouldContain "emoji"
        resolved.map { it.id } shouldContain "undo"
        resolved.map { it.id } shouldNotContain "select_all"
    }

    test("advanced level excludes normal-only tools") {
        val resolved = DrsUnifiedTools.resolveFor(
            view = DrsHybridViewMode.ADVANCED,
            hidden = emptyList(),
            pinned = emptyList(),
            order = emptyList(),
            viewOverrides = emptyMap(),
        )
        resolved.none { it.defaultView == DrsToolView.NORMAL } shouldBe true
        resolved.map { it.id } shouldContain "select_all"
        resolved.map { it.id } shouldContain "undo"
        // BOTH tools remain visible in the advanced level by design...
        resolved.map { it.id } shouldContain "emoji"
        // ...while NORMAL-only tools (settings, paste, share) stay out.
        resolved.map { it.id } shouldNotContain "settings"
        resolved.map { it.id } shouldNotContain "paste"
    }

    test("dual level shows the union of both levels") {
        val simple = DrsUnifiedTools.resolveFor(
            DrsHybridViewMode.SIMPLE, emptyList(), emptyList(), emptyList(), emptyMap(),
        )
        val advanced = DrsUnifiedTools.resolveFor(
            DrsHybridViewMode.ADVANCED, emptyList(), emptyList(), emptyList(), emptyMap(),
        )
        val dual = DrsUnifiedTools.resolveFor(
            DrsHybridViewMode.DUAL, emptyList(), emptyList(), emptyList(), emptyMap(),
        )
        val union = simple.map { it.id }.toSet() + advanced.map { it.id }.toSet()
        dual.size shouldBe union.size
        dual.map { it.id } shouldContain "emoji"
        dual.map { it.id } shouldContain "select_all"
    }

    test("per-tool view override really changes visibility") {
        // settings is NORMAL-only by default: pinning it to NORMAL keeps it
        // out of the advanced level...
        val advanced = DrsUnifiedTools.resolveFor(
            view = DrsHybridViewMode.ADVANCED,
            hidden = emptyList(),
            pinned = emptyList(),
            order = emptyList(),
            viewOverrides = mapOf("settings" to DrsToolView.NORMAL.name),
        )
        resolvedNoneOf(advanced, "settings") shouldBe true
        // ...but a BOTH override surfaces it in the advanced level too.
        val advancedBoth = DrsUnifiedTools.resolveFor(
            view = DrsHybridViewMode.ADVANCED,
            hidden = emptyList(),
            pinned = emptyList(),
            order = emptyList(),
            viewOverrides = mapOf("settings" to DrsToolView.BOTH.name),
        )
        resolvedNoneOf(advancedBoth, "settings") shouldBe false
        // unknown override names fall back to the default view.
        val fallback = DrsUnifiedTools.resolveFor(
            view = DrsHybridViewMode.ADVANCED,
            hidden = emptyList(),
            pinned = emptyList(),
            order = emptyList(),
            viewOverrides = mapOf("settings" to "garbage"),
        )
        resolvedNoneOf(fallback, "settings") shouldBe true
    }

    test("hidden tools are excluded from every level") {
        DrsHybridViewMode.entries.forEach { view ->
            val resolved = DrsUnifiedTools.resolveFor(
                view = view,
                hidden = listOf("clipboard"),
                pinned = emptyList(),
                order = emptyList(),
                viewOverrides = emptyMap(),
            )
            resolved.map { it.id } shouldNotContain "clipboard"
        }
    }

    test("pinned tools float to the head and order is respected") {
        val resolved = DrsUnifiedTools.resolveFor(
            view = DrsHybridViewMode.DUAL,
            hidden = emptyList(),
            pinned = listOf("settings"),
            order = listOf("undo", "redo"),
            viewOverrides = emptyMap(),
        )
        // the head is the default pins (emoji, clipboard, text_tools) plus
        // the user-pinned settings, in catalogue order among the pins...
        resolved.take(4).map { it.id } shouldBe
            listOf("emoji", "clipboard", "text_tools", "settings")
        // ...then the unpinned remainder, led by the explicit order.
        resolved.drop(4).take(2).map { it.id } shouldBe listOf("undo", "redo")
        // order entries for hidden tools never resurrect them
        val withHidden = DrsUnifiedTools.resolveFor(
            view = DrsHybridViewMode.DUAL,
            hidden = listOf("undo"),
            pinned = emptyList(),
            order = listOf("undo", "redo"),
            viewOverrides = emptyMap(),
        )
        withHidden.map { it.id } shouldNotContain "undo"
        withHidden.first().id shouldBe "emoji"
    }

    test("order with unknown ids never crashes resolution") {
        val resolved = DrsUnifiedTools.resolveFor(
            view = DrsHybridViewMode.DUAL,
            hidden = emptyList(),
            pinned = emptyList(),
            order = listOf("does_not_exist", "clipboard", "emoji"),
            viewOverrides = emptyMap(),
        )
        resolved.first().id shouldBe "clipboard"
        resolved[1].id shouldBe "emoji"
        resolved.size shouldBe DrsUnifiedTools.ALL.size
    }

    // -----------------------------------------------------------
    // Shortcut availability scopes
    // -----------------------------------------------------------

    fun stateOf(userPath: String, hybridViewMode: String = DrsHybridViewMode.DUAL.name): DrsState {
        return DrsState(userPath = userPath, hybridViewMode = hybridViewMode)
    }

    test("normal system expands only normal and both shortcuts") {
        val state = stateOf(DrsUserPath.NORMAL.name)
        DrsShortcuts.isShortcutAvailable(state, DrsShortcutScope.NORMAL.name) shouldBe true
        DrsShortcuts.isShortcutAvailable(state, DrsShortcutScope.BOTH.name) shouldBe true
        DrsShortcuts.isShortcutAvailable(state, DrsShortcutScope.TECHNICAL.name) shouldBe false
    }

    test("technical system expands only technical and both shortcuts") {
        val state = stateOf(DrsUserPath.TECHNICAL.name)
        DrsShortcuts.isShortcutAvailable(state, DrsShortcutScope.TECHNICAL.name) shouldBe true
        DrsShortcuts.isShortcutAvailable(state, DrsShortcutScope.BOTH.name) shouldBe true
        DrsShortcuts.isShortcutAvailable(state, DrsShortcutScope.NORMAL.name) shouldBe false
    }

    test("hybrid dual level expands everything") {
        val state = stateOf(DrsUserPath.HYBRID.name, DrsHybridViewMode.DUAL.name)
        DrsShortcutScope.entries.forEach { scope ->
            DrsShortcuts.isShortcutAvailable(state, scope.name) shouldBe true
        }
    }

    test("hybrid simple and advanced levels filter like their bases") {
        val simple = stateOf(DrsUserPath.HYBRID.name, DrsHybridViewMode.SIMPLE.name)
        DrsShortcuts.isShortcutAvailable(simple, DrsShortcutScope.NORMAL.name) shouldBe true
        DrsShortcuts.isShortcutAvailable(simple, DrsShortcutScope.TECHNICAL.name) shouldBe false

        val advanced = stateOf(DrsUserPath.HYBRID.name, DrsHybridViewMode.ADVANCED.name)
        DrsShortcuts.isShortcutAvailable(advanced, DrsShortcutScope.TECHNICAL.name) shouldBe true
        DrsShortcuts.isShortcutAvailable(advanced, DrsShortcutScope.NORMAL.name) shouldBe false
    }

    test("legacy shortcuts without a scope expand everywhere") {
        val normal = stateOf(DrsUserPath.NORMAL.name)
        val technical = stateOf(DrsUserPath.TECHNICAL.name)
        DrsShortcuts.isShortcutAvailable(normal, "legacy_value") shouldBe true
        DrsShortcuts.isShortcutAvailable(technical, "legacy_value") shouldBe true
    }

    // -----------------------------------------------------------
    // State migration (old states parse with safe defaults)
    // -----------------------------------------------------------

    test("state from v1.0.6 parses with unified defaults") {
        val legacyJson = """
            {
              "version": 1,
              "onboardingDone": true,
              "userPath": "TECHNICAL",
              "activeProfileId": "abc",
              "profiles": [],
              "shortcuts": [
                {"id": 1, "shortcut": "brb", "expansion": "be right back", "isTechnical": false, "enabled": true}
              ],
              "techToolbarKeys": ["tab", "esc"]
            }
        """.trimIndent()
        val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }
        val state = json.decodeFromString<DrsState>(legacyJson)

        state.userPath shouldBe DrsUserPath.TECHNICAL.name
        // new unified fields get safe defaults
        state.hybridViewMode shouldBe DrsHybridViewMode.DUAL.name
        state.advancedControlsEnabled shouldBe false
        state.unifiedStripForNormal shouldBe false
        state.unifiedToolOrder.isEmpty() shouldBe true
        state.hiddenUnifiedTools.isEmpty() shouldBe true
        state.pinnedUnifiedTools.isEmpty() shouldBe true
        state.unifiedToolViews.isEmpty() shouldBe true
        // legacy shortcut keeps BOTH scope -> still expands
        DrsShortcuts.isShortcutAvailable(state, state.shortcuts.first().scope) shouldBe true
        // round-trip keeps the new fields
        val encoded = json.encodeToString(state)
        encoded.contains("hybridViewMode") shouldBe true
    }

    test("cycle order covers all three levels") {
        // SIMPLE -> ADVANCED -> DUAL -> SIMPLE (pure mapping check)
        val sequence = mutableListOf<DrsHybridViewMode>()
        var current = DrsHybridViewMode.SIMPLE
        repeat(3) {
            current = when (current) {
                DrsHybridViewMode.SIMPLE -> DrsHybridViewMode.ADVANCED
                DrsHybridViewMode.ADVANCED -> DrsHybridViewMode.DUAL
                DrsHybridViewMode.DUAL -> DrsHybridViewMode.SIMPLE
            }
            sequence += current
        }
        sequence shouldBe
            listOf(DrsHybridViewMode.ADVANCED, DrsHybridViewMode.DUAL, DrsHybridViewMode.SIMPLE)
    }
})

/** Small helper: does the resolved list omit the given tool id? */
private fun resolvedNoneOf(tools: List<DrsUnifiedTool>, id: String): Boolean {
    return tools.none { it.id == id }
}
