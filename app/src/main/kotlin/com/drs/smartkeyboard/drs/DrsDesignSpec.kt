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

/**
 * DRS v1.0.8 — the pure (JVM-safe) half of the DRS Design System.
 *
 * The three DRS systems are three products with three independent visual
 * identities — NOT one look with a swapped accent color. This file is the
 * single source of truth for everything that differs visually between the
 * systems, expressed as pure data so it can be unit tested without any
 * Android dependency:
 *
 *  - النظام العادي   → SOFT identity: large rounded cards, comfortable
 *    density, generous spacing, calm motion, summaries visible and live
 *    values hidden. Built for instant comprehension.
 *  - النظام التقني   → DENSE identity: tighter radii, compact density,
 *    outlined cards, faster motion, summaries replaced by live measured
 *    values and status chips. Built for information density.
 *  - النظام المتكامل → BALANCED identity: the middle of both worlds so
 *    switching levels never feels like switching apps.
 *
 * The Compose half lives in drs/ui/DrsDesignSystem.kt and reads every
 * value from here, so a change in one place updates all three systems
 * everywhere in the app.
 */

/** Visual card language of a system. */
enum class DrsCardStyle {
    /** Rounded, elevated, soft-bordered cards (normal system). */
    SOFT,

    /** Sharper, outlined, dense cards with a visible header bar (technical). */
    DENSE,

    /** The balanced middle used by the unified system. */
    BALANCED,
}

/** Information density of a system's UI. */
enum class DrsDensity {
    /** Comfortable rows and larger touch targets (normal system). */
    COMFORTABLE,

    /** Compact rows that show more per screen (technical system). */
    COMPACT,

    /** Balanced rows (unified system). */
    BALANCED,
}

/**
 * One complete visual identity. Everything the Compose layer needs to
 * render a system's own look — colors come from [DrsSystems] specs.
 */
data class DrsVisualIdentity(
    val path: DrsUserPath,
    val cardStyle: DrsCardStyle,
    val density: DrsDensity,

    // ----- shape tokens (dp) -----
    /** Corner radius of big cards. */
    val cardRadiusDp: Int,
    /** Corner radius of tiles and inner blocks. */
    val tileRadiusDp: Int,
    /** Corner radius of chips and status badges. */
    val chipRadiusDp: Int,
    /** Corner radius of hero/gradient headers. */
    val heroRadiusDp: Int,

    // ----- spacing tokens (dp) -----
    /** Horizontal padding around screen content. */
    val screenPaddingDp: Int,
    /** Vertical gap between cards. */
    val cardSpacingDp: Int,
    /** Inner padding inside cards. */
    val cardInnerPaddingDp: Int,

    // ----- motion tokens -----
    /**
     * Multiplier applied to stagger delays and transition distances.
     * The normal system gets a calmer, larger motion; the technical
     * system snaps faster so power users never wait on animation.
     */
    val motionScale: Float,

    // ----- information rules -----
    /** Whether tile summary lines are shown under titles. */
    val showSummaries: Boolean,
    /** Whether live measured values (counts, timings) are surfaced inline. */
    val showLiveValues: Boolean,
    /** Whether section headers carry a status/live badge. */
    val showStatusChips: Boolean,
    /** Whether navigation shows breadcrumbs (technical drill-down). */
    val showBreadcrumbs: Boolean,
    /** Whether the dashboard uses a metric grid (technical control center). */
    val useMetricGrid: Boolean,
) {
    val isDense: Boolean get() = density == DrsDensity.COMPACT
}

/** The three identities, resolved once per system. */
object DrsDesignSpec {

    // النظام العادي — soft, roomy, calm.
    val NORMAL = DrsVisualIdentity(
        path = DrsUserPath.NORMAL,
        cardStyle = DrsCardStyle.SOFT,
        density = DrsDensity.COMFORTABLE,
        cardRadiusDp = 22,
        tileRadiusDp = 16,
        chipRadiusDp = 10,
        heroRadiusDp = 28,
        screenPaddingDp = 12,
        cardSpacingDp = 14,
        cardInnerPaddingDp = 14,
        motionScale = 1.0f,
        showSummaries = true,
        showLiveValues = false,
        showStatusChips = false,
        showBreadcrumbs = false,
        useMetricGrid = false,
    )

    // النظام التقني — dense, sharp, fast, information-first.
    val TECHNICAL = DrsVisualIdentity(
        path = DrsUserPath.TECHNICAL,
        cardStyle = DrsCardStyle.DENSE,
        density = DrsDensity.COMPACT,
        cardRadiusDp = 12,
        tileRadiusDp = 9,
        chipRadiusDp = 6,
        heroRadiusDp = 14,
        screenPaddingDp = 8,
        cardSpacingDp = 8,
        cardInnerPaddingDp = 10,
        motionScale = 0.62f,
        showSummaries = false,
        showLiveValues = true,
        showStatusChips = true,
        showBreadcrumbs = true,
        useMetricGrid = true,
    )

    // النظام المتكامل — the balanced middle of both worlds.
    val HYBRID = DrsVisualIdentity(
        path = DrsUserPath.HYBRID,
        cardStyle = DrsCardStyle.BALANCED,
        density = DrsDensity.BALANCED,
        cardRadiusDp = 17,
        tileRadiusDp = 12,
        chipRadiusDp = 8,
        heroRadiusDp = 21,
        screenPaddingDp = 10,
        cardSpacingDp = 11,
        cardInnerPaddingDp = 12,
        motionScale = 0.82f,
        showSummaries = true,
        showLiveValues = true,
        showStatusChips = true,
        showBreadcrumbs = false,
        useMetricGrid = false,
    )

    val ALL: List<DrsVisualIdentity> = listOf(NORMAL, TECHNICAL, HYBRID)

    /** Resolves the identity for a raw stored path name (defaults to NORMAL). */
    fun identityOfName(pathName: String): DrsVisualIdentity {
        return ALL.firstOrNull { it.path.name == pathName } ?: NORMAL
    }

    /** Resolves the identity for a [DrsUserPath]. */
    fun identityOf(path: DrsUserPath): DrsVisualIdentity {
        return ALL.firstOrNull { it.path == path } ?: NORMAL
    }

    /**
     * Effective stagger delay for item [index] in milliseconds, combining
     * the system's motion scale with the user's reduce-motion choice.
     * Reduced motion collapses the cascade to a single short fade so
     * nothing distracts or delays.
     */
    fun staggerDelayMs(identity: DrsVisualIdentity, index: Int, motionEnabled: Boolean): Int {
        if (!motionEnabled) return 0
        return (index * 55f * identity.motionScale).toInt().coerceAtLeast(0)
    }

    /**
     * Effective entrance travel distance in pixels for a system. Reduced
     * motion removes the travel entirely (pure fade, no movement).
     */
    fun entranceTravelPx(identity: DrsVisualIdentity, motionEnabled: Boolean): Float {
        if (!motionEnabled) return 0f
        return 40f * identity.motionScale
    }

    /**
     * Screen transition scale: the horizontal slide distance factor of
     * route transitions. Reduced motion removes slides completely (the
     * caller falls back to a fast fade).
     */
    fun transitionSlideFactor(identity: DrsVisualIdentity, motionEnabled: Boolean): Float {
        if (!motionEnabled) return 0f
        return identity.motionScale
    }
}

// ---------------------------------------------------------------------------
// Real screen-state vocabulary (البند 12): every screen must be able to
// render all of these states for real — never a static image.
// ---------------------------------------------------------------------------

/** The complete set of states a DRS screen can truthfully be in. */
enum class DrsAppStateKind {
    /** Work is happening (loading a pack, running diagnostics…). */
    LOADING,

    /** There is genuinely nothing to show yet (first use of a list). */
    EMPTY,

    /** A real failure occurred and the user can retry. */
    ERROR,

    /** The last action finished successfully. */
    SUCCESS,

    /** The feature is switched off by the user. */
    DISABLED,

    /** A system permission is required before this can work. */
    PERMISSION_REQUIRED,

    /** The user has never used this feature before. */
    FIRST_USE,

    /** Settings were restored to factory defaults. */
    RESET,
}

/**
 * Pure model of one screen-state presentation so tests can assert the
 * mapping rules without Android. The Compose renderer in
 * drs/ui/DrsDesignSystem.kt turns these into real interactive views.
 */
data class DrsAppStateSpec(
    val kind: DrsAppStateKind,
    /** True when this state must offer a real, working action button. */
    val hasAction: Boolean,
    /** True when this state is a terminal outcome (success/error). */
    val isTerminal: Boolean,
    /** Suggested severity weight used by tests to guard the ordering. */
    val severity: Int,
)

object DrsAppStateSpecs {
    val LOADING = DrsAppStateSpec(DrsAppStateKind.LOADING, hasAction = false, isTerminal = false, severity = 0)
    val EMPTY = DrsAppStateSpec(DrsAppStateKind.EMPTY, hasAction = true, isTerminal = false, severity = 1)
    val FIRST_USE = DrsAppStateSpec(DrsAppStateKind.FIRST_USE, hasAction = true, isTerminal = false, severity = 1)
    val DISABLED = DrsAppStateSpec(DrsAppStateKind.DISABLED, hasAction = true, isTerminal = false, severity = 2)
    val PERMISSION_REQUIRED = DrsAppStateSpec(DrsAppStateKind.PERMISSION_REQUIRED, hasAction = true, isTerminal = false, severity = 2)
    val ERROR = DrsAppStateSpec(DrsAppStateKind.ERROR, hasAction = true, isTerminal = true, severity = 3)
    val SUCCESS = DrsAppStateSpec(DrsAppStateKind.SUCCESS, hasAction = false, isTerminal = true, severity = 0)
    val RESET = DrsAppStateSpec(DrsAppStateKind.RESET, hasAction = false, isTerminal = true, severity = 1)

    val ALL: List<DrsAppStateSpec> = listOf(
        LOADING, EMPTY, FIRST_USE, DISABLED, PERMISSION_REQUIRED, ERROR, SUCCESS, RESET,
    )
}

// ---------------------------------------------------------------------------
// Onboarding flow builder (البند 6/7/8): the onboarding is genuinely
// different per system — the pure step list lives here so tests can pin it.
// ---------------------------------------------------------------------------

/** Logical onboarding steps (mirrors the UI enum in DrsOnboardingActivity). */
enum class DrsOnboardingStepKind {
    WELCOME, EXPLANATION, PATH, PREFERENCES, PROFILE, THEMES, SOUNDS, SHORTCUTS, TEST, ENABLE, DONE,
}

object DrsOnboardingFlow {

    /**
     * Builds the onboarding step list for a system:
     *  - every system gets the shared core (welcome → explanation → path →
     *    preferences → profile → themes → sounds → typing test → enable → done);
     *  - the shortcuts-samples step only appears for the technical and
     *    unified systems, because text-expansion is a power tool — the
     *    normal system's onboarding stays minimal by design.
     */
    fun stepsFor(path: DrsUserPath): List<DrsOnboardingStepKind> {
        val core = listOf(
            DrsOnboardingStepKind.WELCOME,
            DrsOnboardingStepKind.EXPLANATION,
            DrsOnboardingStepKind.PATH,
            DrsOnboardingStepKind.PREFERENCES,
            DrsOnboardingStepKind.PROFILE,
            DrsOnboardingStepKind.THEMES,
            DrsOnboardingStepKind.SOUNDS,
        )
        val tail = listOf(
            DrsOnboardingStepKind.TEST,
            DrsOnboardingStepKind.ENABLE,
            DrsOnboardingStepKind.DONE,
        )
        val middle = when (path) {
            DrsUserPath.NORMAL -> emptyList()
            DrsUserPath.TECHNICAL, DrsUserPath.HYBRID -> listOf(DrsOnboardingStepKind.SHORTCUTS)
        }
        return core + middle + tail
    }
}
