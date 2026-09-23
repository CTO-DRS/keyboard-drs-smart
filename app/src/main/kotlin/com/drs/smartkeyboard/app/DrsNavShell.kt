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

package com.drs.smartkeyboard.app

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Extension
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.drs.smartkeyboard.R
import org.drs.lib.compose.stringRes

/**
 * DRS v1.0.8 (البند 11) — the new adaptive navigation shell.
 *
 * A clear, never-lose-your-way structure:
 *   Home → Section → Feature → Settings
 *
 * The four top-level destinations are reachable from a bottom
 * NavigationBar on phones, and from a NavigationRail on wide screens
 * (tablets / landscape) — the layout follows the screen size exactly as
 * the product brief requires. All deeper screens keep the existing
 * drill-down with a working back stack; nothing is duplicated without a
 * reason: the tabs are the four real hubs, everything else stays a
 * sub-screen of one of them.
 */

/** One top-level navigation tab. */
private data class DrsNavTab(
    val route: Any,
    val labelRes: Int,
    val icon: ImageVector,
)

private val drsNavTabs = listOf(
    DrsNavTab(Routes.Settings.Home, R.string.drs__nav__home, Icons.Default.Dashboard),
    DrsNavTab(Routes.Settings.DrsToolsHub, R.string.drs__nav__tools, Icons.Outlined.Extension),
    DrsNavTab(Routes.Settings.Theme, R.string.drs__nav__appearance, Icons.Outlined.Palette),
    DrsNavTab(Routes.Settings.DrsSettingsRoot, R.string.drs__nav__settings, Icons.Default.Settings),
)

/** Route classes that are allowed to show the persistent navigation shell. */
private val topLevelRoutes: Set<kotlin.reflect.KClass<*>> = drsNavTabs.map { it.route::class }.toSet()

@Composable
private fun currentRouteClass(navController: NavHostController): kotlin.reflect.KClass<*>? {
    val backStackEntry by navController.currentBackStackEntryAsState()
    return backStackEntry?.destination?.route
        ?.let { routePattern ->
            // Typed routes serialize as the fully-qualified class name
            // (optionally with arguments); match the destination against the
            // tab route classes by their serialized pattern.
            drsNavTabs.firstOrNull { tab ->
                routePattern.contains(tab.route::class.qualifiedName ?: "\u0000")
            }?.route?.let { it::class }
        }
}

@Composable
private fun currentTabIndex(navController: NavHostController): Int {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val route = backStackEntry?.destination?.route ?: return -1
    return drsNavTabs.indexOfFirst { tab ->
        route.contains(tab.route::class.qualifiedName ?: "\u0000")
    }
}

private fun navigateToTab(navController: NavHostController, route: Any) {
    navController.navigate(route) {
        // Pop to start so tabs never stack on top of each other — the
        // classic single-top tab behavior with a stable back to Home.
        popUpTo(navController.graph.findStartDestination().id) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}

/**
 * Hosts the NavHost with the adaptive navigation shell: bottom bar on
 * compact widths, side rail on wide ones. The shell appears only on the
 * four top-level hubs so drill-down screens stay immersive.
 */
@Composable
fun DrsAppNavShell(
    navController: NavHostController,
    startDestination: kotlin.reflect.KClass<*>,
    modifier: Modifier = Modifier,
) {
    val activeClass = currentRouteClass(navController)
    val showShell = activeClass != null && activeClass in topLevelRoutes

    BoxWithConstraintsShell(modifier = modifier) { isWide ->
        if (isWide) {
            Row(modifier = Modifier.fillMaxSize()) {
                if (showShell) {
                    DrsNavRail(
                        navController = navController,
                        modifier = Modifier,
                    )
                }
                Box(modifier = Modifier.weight(1f)) {
                    Routes.AppNavHost(
                        modifier = Modifier.fillMaxSize(),
                        navController = navController,
                        startDestination = startDestination,
                    )
                }
            }
        } else {
            Column(modifier = Modifier.fillMaxSize()) {
                Box(modifier = Modifier.weight(1f)) {
                    Routes.AppNavHost(
                        modifier = Modifier.fillMaxSize(),
                        navController = navController,
                        startDestination = startDestination,
                    )
                }
                if (showShell) {
                    DrsBottomNavBar(navController = navController)
                }
            }
        }
    }
}

@Composable
private fun BoxWithConstraintsShell(
    modifier: Modifier = Modifier,
    content: @Composable (Boolean) -> Unit,
) {
    androidx.compose.foundation.layout.BoxWithConstraints(modifier = modifier) {
        val isWide = maxWidth >= 600.dp
        content(isWide)
    }
}

@Composable
private fun DrsBottomNavBar(navController: NavHostController) {
    val selected = currentTabIndex(navController)
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 3.dp,
    ) {
        drsNavTabs.forEachIndexed { index, tab ->
            NavigationBarItem(
                selected = index == selected,
                onClick = { navigateToTab(navController, tab.route) },
                icon = { Icon(imageVector = tab.icon, contentDescription = null) },
                label = {
                    Text(
                        text = stringRes(tab.labelRes),
                        fontWeight = if (index == selected) FontWeight.Bold else FontWeight.Normal,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                alwaysShowLabel = true,
            )
        }
    }
}

@Composable
private fun DrsNavRail(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    val selected = currentTabIndex(navController)
    NavigationRail(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        drsNavTabs.forEachIndexed { index, tab ->
            NavigationRailItem(
                selected = index == selected,
                onClick = { navigateToTab(navController, tab.route) },
                icon = { Icon(imageVector = tab.icon, contentDescription = null) },
                label = {
                    Text(
                        text = stringRes(tab.labelRes),
                        fontWeight = if (index == selected) FontWeight.Bold else FontWeight.Normal,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                alwaysShowLabel = true,
            )
        }
    }
}
