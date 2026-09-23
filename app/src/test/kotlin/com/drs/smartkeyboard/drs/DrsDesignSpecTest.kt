/*
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
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.shouldBeInstanceOf

/**
 * DRS v1.0.8 — tests for the pure design-spec core: the three visual
 * identities, the motion math, the screen-state vocabulary and the
 * per-system onboarding flow builder.
 */
class DrsDesignSpecTest : FunSpec({

    test("identity resolution covers all three systems and defaults safely") {
        DrsDesignSpec.identityOfName("NORMAL").path shouldBe DrsUserPath.NORMAL
        DrsDesignSpec.identityOfName("TECHNICAL").path shouldBe DrsUserPath.TECHNICAL
        DrsDesignSpec.identityOfName("HYBRID").path shouldBe DrsUserPath.HYBRID
        // Unknown / legacy values fall back to the normal identity.
        DrsDesignSpec.identityOfName("SOMETHING_ELSE") shouldBe DrsDesignSpec.NORMAL
        DrsDesignSpec.identityOfName("") shouldBe DrsDesignSpec.NORMAL
    }

    test("the three identities are genuinely different — not one look with a swapped color") {
        val normal = DrsDesignSpec.NORMAL
        val technical = DrsDesignSpec.TECHNICAL
        val hybrid = DrsDesignSpec.HYBRID

        // Shape language differs.
        normal.cardRadiusDp shouldNotBe technical.cardRadiusDp
        normal.cardRadiusDp shouldBe hybrid.cardRadiusDp + 5
        technical.cardRadiusDp shouldBe hybrid.cardRadiusDp - 5

        // Density differs.
        normal.density shouldBe DrsDensity.COMFORTABLE
        technical.density shouldBe DrsDensity.COMPACT
        hybrid.density shouldBe DrsDensity.BALANCED

        // Card style differs.
        normal.cardStyle shouldBe DrsCardStyle.SOFT
        technical.cardStyle shouldBe DrsCardStyle.DENSE
        hybrid.cardStyle shouldBe DrsCardStyle.BALANCED

        // Motion differs: technical snaps faster than normal, hybrid between.
        (technical.motionScale < hybrid.motionScale) shouldBe true
        (hybrid.motionScale < normal.motionScale) shouldBe true

        // Information rules differ.
        normal.showSummaries shouldBe true
        normal.showLiveValues shouldBe false
        technical.showSummaries shouldBe false
        technical.showLiveValues shouldBe true
        technical.showBreadcrumbs shouldBe true
        normal.showBreadcrumbs shouldBe false
        hybrid.showBreadcrumbs shouldBe false
        technical.useMetricGrid shouldBe true
        normal.useMetricGrid shouldBe false
    }

    test("stagger delay scales with the identity and collapses under reduced motion") {
        val normal = DrsDesignSpec.NORMAL
        val technical = DrsDesignSpec.TECHNICAL

        DrsDesignSpec.staggerDelayMs(normal, index = 4, motionEnabled = true) shouldBe 220
        // Technical moves faster for the same index.
        val techDelay = DrsDesignSpec.staggerDelayMs(technical, index = 4, motionEnabled = true)
        (techDelay < 220) shouldBe true
        // Reduced motion removes the cascade entirely.
        DrsDesignSpec.staggerDelayMs(normal, index = 8, motionEnabled = false) shouldBe 0
        DrsDesignSpec.staggerDelayMs(technical, index = 8, motionEnabled = false) shouldBe 0
    }

    test("entrance travel and transition slide follow the same motion contract") {
        val normal = DrsDesignSpec.NORMAL
        val technical = DrsDesignSpec.TECHNICAL

        DrsDesignSpec.entranceTravelPx(normal, motionEnabled = true) shouldBe 40f
        DrsDesignSpec.entranceTravelPx(technical, motionEnabled = true) shouldBe (40f * technical.motionScale)
        DrsDesignSpec.entranceTravelPx(normal, motionEnabled = false) shouldBe 0f

        DrsDesignSpec.transitionSlideFactor(normal, motionEnabled = true) shouldBe 1.0f
        (DrsDesignSpec.transitionSlideFactor(technical, motionEnabled = true) < 1f) shouldBe true
        DrsDesignSpec.transitionSlideFactor(normal, motionEnabled = false) shouldBe 0f
    }

    test("screen-state vocabulary covers all eight required states") {
        DrsAppStateSpecs.ALL shouldHaveSize 8
        DrsAppStateSpecs.ALL.map { it.kind } shouldContainExactly listOf(
            DrsAppStateKind.LOADING,
            DrsAppStateKind.EMPTY,
            DrsAppStateKind.FIRST_USE,
            DrsAppStateKind.DISABLED,
            DrsAppStateKind.PERMISSION_REQUIRED,
            DrsAppStateKind.ERROR,
            DrsAppStateKind.SUCCESS,
            DrsAppStateKind.RESET,
        )
    }

    test("actionable states always offer a real action; loading never does") {
        DrsAppStateSpecs.LOADING.hasAction shouldBe false
        DrsAppStateSpecs.SUCCESS.hasAction shouldBe false
        DrsAppStateSpecs.RESET.hasAction shouldBe false
        DrsAppStateSpecs.EMPTY.hasAction shouldBe true
        DrsAppStateSpecs.FIRST_USE.hasAction shouldBe true
        DrsAppStateSpecs.DISABLED.hasAction shouldBe true
        DrsAppStateSpecs.PERMISSION_REQUIRED.hasAction shouldBe true
        DrsAppStateSpecs.ERROR.hasAction shouldBe true
        // Terminal outcomes are flagged for UI emphasis.
        DrsAppStateSpecs.ERROR.isTerminal shouldBe true
        DrsAppStateSpecs.SUCCESS.isTerminal shouldBe true
        DrsAppStateSpecs.LOADING.isTerminal shouldBe false
        // Severity ordering: error is the most severe.
        DrsAppStateSpecs.ERROR.severity shouldBe 3
        DrsAppStateSpecs.ERROR.severity shouldNotBe DrsAppStateSpecs.EMPTY.severity
    }

    test("onboarding flow is genuinely different per system") {
        val normal = DrsOnboardingFlow.stepsFor(DrsUserPath.NORMAL)
        val technical = DrsOnboardingFlow.stepsFor(DrsUserPath.TECHNICAL)
        val hybrid = DrsOnboardingFlow.stepsFor(DrsUserPath.HYBRID)

        // The normal system stays minimal: no shortcuts-samples step.
        normal shouldNotBe technical
        normal.contains(DrsOnboardingStepKind.SHORTCUTS) shouldBe false
        // Power systems keep the shortcut samples.
        technical.contains(DrsOnboardingStepKind.SHORTCUTS) shouldBe true
        hybrid.contains(DrsOnboardingStepKind.SHORTCUTS) shouldBe true
        technical shouldBe hybrid
        // Shared core is intact for every system.
        for (flow in listOf(normal, technical, hybrid)) {
            flow.first() shouldBe DrsOnboardingStepKind.WELCOME
            flow.last() shouldBe DrsOnboardingStepKind.DONE
            flow.contains(DrsOnboardingStepKind.PATH) shouldBe true
            flow.contains(DrsOnboardingStepKind.ENABLE) shouldBe true
        }
        // The technical flow is strictly longer (extra power step).
        (technical.size > normal.size) shouldBe true
        // Every returned step is a valid enum member.
        (normal + technical + hybrid).forEach { it.shouldBeInstanceOf<DrsOnboardingStepKind>() }
    }
})
