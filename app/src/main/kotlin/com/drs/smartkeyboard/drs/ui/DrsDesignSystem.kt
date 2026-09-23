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

@file:OptIn(ExperimentalMaterial3Api::class)

package com.drs.smartkeyboard.drs.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.drs.smartkeyboard.R
import com.drs.smartkeyboard.app.DrsPreferenceStore
import com.drs.smartkeyboard.drs.DrsAppStateKind
import com.drs.smartkeyboard.drs.DrsDesignSpec
import com.drs.smartkeyboard.drs.DrsStore
import com.drs.smartkeyboard.drs.DrsSystems
import com.drs.smartkeyboard.drs.DrsVisualIdentity
import org.drs.jetpref.datastore.model.collectAsState
import org.drs.lib.compose.stringRes

/**
 * DRS v1.0.8 — the Compose half of the DRS Design System.
 *
 * Everything here reads its tokens from [DrsDesignSpec] (pure data) and the
 * active system from [DrsStore], so all three systems render through the
 * same components but with genuinely different identities: shape language,
 * density, spacing, motion and information rules all follow the system.
 */

/** CompositionLocal carrying the active system's visual identity. */
val LocalDrsIdentity = androidx.compose.runtime.staticCompositionLocalOf<DrsVisualIdentity> {
    DrsDesignSpec.NORMAL
}

/**
 * Resolves the active identity live: follows the stored user path, the
 * day/night mode for accent selection, and the user's reduce-motion choice.
 */
@Composable
fun rememberDrsIdentity(): DrsVisualIdentity {
    val drsState by DrsStore.state.collectAsState()
    return DrsDesignSpec.identityOfName(drsState.userPath)
}

/** The accent color of the active identity for the current day/night mode. */
@Composable
fun DrsVisualIdentity.accentForMode(): Color {
    val spec = DrsSystems.specOf(path)
    return if (isSystemInDarkTheme()) spec.accentNight else spec.accent
}

/** Accent of the currently active system (convenience for hub screens). */
@Composable
fun accentForActiveSystem(): Color {
    return rememberDrsIdentity().accentForMode()
}

/**
 * The one adaptive navigation tile used by the dashboards, the tools hub
 * and the settings root: identical API everywhere, per-system shape,
 * density and summary rules from the active identity.
 */
@Composable
fun DrsIdentityTile(
    icon: ImageVector,
    accent: Color,
    title: String,
    summary: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    showSummary: Boolean = true,
) {
    val identity = LocalDrsIdentity.current
    val colorScheme = MaterialTheme.colorScheme
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp, vertical = if (identity.isDense) 7.dp else 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(identity.tileShape)
                .background(accent.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accent,
                modifier = Modifier.size(21.dp),
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (showSummary && summary.isNotBlank()) {
                Text(
                    text = summary,
                    style = MaterialTheme.typography.bodySmall,
                    color = colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        Spacer(modifier = Modifier.width(6.dp))
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = colorScheme.onSurfaceVariant.copy(alpha = 0.55f),
            modifier = Modifier.size(20.dp),
        )
    }
}

/** Hairline divider between identity tiles, aligned with the text edge. */
@Composable
fun DrsIdentityTileDivider() {
    androidx.compose.material3.HorizontalDivider(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 54.dp, end = 4.dp),
        thickness = 1.dp,
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
    )
}

/** True while the user allows animations (real persisted preference). */
@Composable
fun rememberMotionEnabled(): Boolean {
    val prefs by DrsPreferenceStore
    val reduced by prefs.other.reducedMotion.collectAsState()
    return !reduced
}

/** System-tuned corner radius for cards. */
val DrsVisualIdentity.cardShape: RoundedCornerShape
    get() = RoundedCornerShape(cardRadiusDp.dp)

/** System-tuned corner radius for tiles. */
val DrsVisualIdentity.tileShape: RoundedCornerShape
    get() = RoundedCornerShape(tileRadiusDp.dp)

/** System-tuned corner radius for chips. */
val DrsVisualIdentity.chipShape: RoundedCornerShape
    get() = RoundedCornerShape(chipRadiusDp.dp)

// ---------------------------------------------------------------------------
// Identity card: the ONE card component used across dashboards and hubs.
// SOFT → elevated, generous; DENSE → outlined with accent header bar;
// BALANCED → middle. Same API, three different looks.
// ---------------------------------------------------------------------------

@Composable
fun DrsIdentityCard(
    title: String? = null,
    accent: Color? = null,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val identity = LocalDrsIdentity.current
    val colorScheme = MaterialTheme.colorScheme
    val resolvedAccent = accent ?: identity.accentForMode()
    val shape = identity.cardShape
    val borderColor = when (identity.cardStyle) {
        com.drs.smartkeyboard.drs.DrsCardStyle.SOFT ->
            colorScheme.outlineVariant.copy(alpha = 0.5f)
        com.drs.smartkeyboard.drs.DrsCardStyle.DENSE ->
            resolvedAccent.copy(alpha = 0.45f)
        com.drs.smartkeyboard.drs.DrsCardStyle.BALANCED ->
            colorScheme.outlineVariant.copy(alpha = 0.6f)
    }
    val container = when (identity.cardStyle) {
        com.drs.smartkeyboard.drs.DrsCardStyle.SOFT -> colorScheme.surface
        com.drs.smartkeyboard.drs.DrsCardStyle.DENSE -> colorScheme.surface.copy(alpha = 0.96f)
        com.drs.smartkeyboard.drs.DrsCardStyle.BALANCED -> colorScheme.surface
    }
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(container)
            .border(1.dp, borderColor, shape)
            .then(
                if (onClick != null) {
                    Modifier.clickable(onClick = onClick)
                } else {
                    Modifier
                },
            ),
    ) {
        if (identity.cardStyle == com.drs.smartkeyboard.drs.DrsCardStyle.DENSE && title != null) {
            // Technical header bar: compact, accent-tinted, uppercase-feel.
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(resolvedAccent.copy(alpha = 0.10f))
                    .padding(horizontal = identity.cardInnerPaddingDp.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .width(3.dp)
                        .height(14.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(resolvedAccent),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        } else if (title != null) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = colorScheme.onSurface,
                modifier = Modifier.padding(
                    start = identity.cardInnerPaddingDp.dp,
                    end = identity.cardInnerPaddingDp.dp,
                    top = identity.cardInnerPaddingDp.dp,
                ),
            )
        }
        Box(
            modifier = Modifier.padding(
                horizontal = identity.cardInnerPaddingDp.dp,
                vertical = (identity.cardInnerPaddingDp / 2).coerceAtLeast(3).dp,
            ),
        ) {
            content()
        }
    }
}

// ---------------------------------------------------------------------------
// Status chip: real live-value chip used by dense/technical surfaces.
// ---------------------------------------------------------------------------

@Composable
fun DrsStatusChip(text: String, accent: Color? = null, modifier: Modifier = Modifier) {
    val identity = LocalDrsIdentity.current
    val resolved = accent ?: identity.accentForMode()
    Box(
        modifier = modifier
            .clip(identity.chipShape)
            .background(resolved.copy(alpha = 0.14f))
            .border(1.dp, resolved.copy(alpha = 0.35f), identity.chipShape)
            .padding(horizontal = 8.dp, vertical = 3.dp),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = resolved,
            maxLines = 1,
        )
    }
}

// ---------------------------------------------------------------------------
// Metric tile: the dense numeric cell of the technical control-center grid.
// Shows REAL measured values only — callers must pass measured data.
// ---------------------------------------------------------------------------

@Composable
fun DrsMetricTile(
    label: String,
    value: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    accent: Color? = null,
) {
    val identity = LocalDrsIdentity.current
    val colorScheme = MaterialTheme.colorScheme
    val resolved = accent ?: identity.accentForMode()
    Column(
        modifier = modifier
            .clip(identity.tileShape)
            .background(colorScheme.surface.copy(alpha = 0.95f))
            .border(1.dp, resolved.copy(alpha = 0.35f), identity.tileShape)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 9.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = resolved,
                modifier = Modifier.size(15.dp),
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

// ---------------------------------------------------------------------------
// Breadcrumb bar (البند 11): real navigation context for the technical
// system's drill-down screens. Every crumb is a working back-jump target.
// ---------------------------------------------------------------------------

@Composable
fun DrsBreadcrumbBar(
    crumbs: List<Pair<String, () -> Unit>>,
    modifier: Modifier = Modifier,
) {
    val identity = LocalDrsIdentity.current
    val colorScheme = MaterialTheme.colorScheme
    if (!identity.showBreadcrumbs || crumbs.isEmpty()) return
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(identity.chipShape)
            .background(colorScheme.surfaceVariant.copy(alpha = 0.55f))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        crumbs.forEachIndexed { index, (label, action) ->
            if (index > 0) {
                Text(
                    // Direction-neutral separator: reads correctly in RTL and LTR.
                    text = "/",
                    color = colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    fontSize = 12.sp,
                )
            }
            val isLast = index == crumbs.lastIndex
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (isLast) FontWeight.Bold else FontWeight.Medium,
                color = if (isLast) colorScheme.onSurface else colorScheme.primary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .clip(identity.chipShape)
                    .clickable(enabled = !isLast, onClick = action)
                    .padding(horizontal = 4.dp, vertical = 2.dp),
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Real screen states (البند 12): Loading / Empty / Error / Success /
// Disabled / Permission Required / First Use / Reset — every one interactive
// and wired to real actions, never a static picture.
// ---------------------------------------------------------------------------

@Composable
fun DrsAppStateView(
    kind: DrsAppStateKind,
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    val identity = LocalDrsIdentity.current
    val colorScheme = MaterialTheme.colorScheme
    val accent = identity.accentForMode()
    val motionEnabled = rememberMotionEnabled()

    val icon: ImageVector = when (kind) {
        DrsAppStateKind.LOADING -> Icons.Default.HourglassTop
        DrsAppStateKind.EMPTY -> Icons.Default.Inbox
        DrsAppStateKind.ERROR -> Icons.Default.ErrorOutline
        DrsAppStateKind.SUCCESS -> Icons.Default.CheckCircle
        DrsAppStateKind.DISABLED -> Icons.Default.Block
        DrsAppStateKind.PERMISSION_REQUIRED -> Icons.Default.LockOpen
        DrsAppStateKind.FIRST_USE -> Icons.Default.Lightbulb
        DrsAppStateKind.RESET -> Icons.Default.RestartAlt
    }
    val tint: Color = when (kind) {
        DrsAppStateKind.ERROR -> colorScheme.error
        DrsAppStateKind.SUCCESS -> colorScheme.tertiary
        DrsAppStateKind.LOADING, DrsAppStateKind.EMPTY, DrsAppStateKind.FIRST_USE, DrsAppStateKind.RESET -> accent
        DrsAppStateKind.DISABLED, DrsAppStateKind.PERMISSION_REQUIRED -> colorScheme.onSurfaceVariant
    }

    val pulse = remember { Animatable(1f) }
    if (kind == DrsAppStateKind.LOADING && motionEnabled) {
        LaunchedEffect(Unit) {
            while (true) {
                pulse.animateTo(0.82f, spring(stiffness = Spring.StiffnessLow))
                pulse.animateTo(1f, spring(stiffness = Spring.StiffnessLow))
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(identity.cardShape)
            .background(colorScheme.surface.copy(alpha = 0.9f))
            .border(1.dp, tint.copy(alpha = 0.35f), identity.cardShape)
            .padding(vertical = 22.dp, horizontal = 18.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .graphicsLayer {
                    scaleX = pulse.value
                    scaleY = pulse.value
                }
                .clip(CircleShape)
                .background(tint.copy(alpha = 0.13f)),
            contentAlignment = Alignment.Center,
        ) {
            if (kind == DrsAppStateKind.LOADING) {
                CircularProgressIndicator(
                    modifier = Modifier.size(26.dp),
                    strokeWidth = 2.5.dp,
                    color = tint,
                )
            } else {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = tint,
                    modifier = Modifier.size(26.dp),
                )
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = description,
            style = MaterialTheme.typography.bodySmall,
            color = colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        if (actionLabel != null && onAction != null) {
            Spacer(modifier = Modifier.height(12.dp))
            if (kind == DrsAppStateKind.ERROR) {
                Button(onClick = onAction) {
                    Text(actionLabel)
                }
            } else {
                OutlinedButton(onClick = onAction) {
                    Text(actionLabel)
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Section header that adapts: soft for the normal system, chip-bearing for
// the technical system — same API, per-system identity.
// ---------------------------------------------------------------------------

@Composable
fun DrsIdentitySectionHeader(
    text: String,
    modifier: Modifier = Modifier,
    trailing: (@Composable () -> Unit)? = null,
) {
    val identity = LocalDrsIdentity.current
    val colorScheme = MaterialTheme.colorScheme
    val accent = identity.accentForMode()
    Row(
        modifier = modifier
            .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(identity.chipShape)
                .background(accent.copy(alpha = 0.16f)),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(accent),
            )
        }
        Spacer(modifier = Modifier.width(9.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.titleSmall,
            color = colorScheme.onSurface,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(modifier = Modifier.width(10.dp))
        Box(
            modifier = Modifier
                .weight(1f)
                .height(1.dp)
                .background(colorScheme.outlineVariant.copy(alpha = 0.4f)),
        )
        if (trailing != null) {
            Spacer(modifier = Modifier.width(8.dp))
            trailing()
        }
    }
}

/**
 * Staggered entrance driven by the identity's motion tokens and the real
 * reduce-motion preference — every dashboard card lands in a cascade tuned
 * per system, or fades instantly when motion is reduced.
 */
@Composable
fun DrsIdentityStaggerIn(
    index: Int,
    content: @Composable () -> Unit,
) {
    val identity = LocalDrsIdentity.current
    val motionEnabled = rememberMotionEnabled()
    val travel = DrsDesignSpec.entranceTravelPx(identity, motionEnabled)
    val delayMs = DrsDesignSpec.staggerDelayMs(identity, index, motionEnabled)
    val progress = remember { Animatable(if (!motionEnabled) 1f else 0f) }
    LaunchedEffect(Unit) {
        if (motionEnabled) {
            kotlinx.coroutines.delay(delayMs.toLong())
            progress.animateTo(1f, spring(dampingRatio = 0.9f, stiffness = 260f))
        } else {
            progress.snapTo(1f)
        }
    }
    Box(
        modifier = Modifier.graphicsLayer {
            translationY = (1f - progress.value) * travel
            alpha = (progress.value * 1.35f).coerceAtMost(1f)
        },
    ) {
        content()
    }
}
