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

package com.drs.smartkeyboard.app.apptheme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.drs.smartkeyboard.app.AppTheme
import com.drs.smartkeyboard.app.DrsPreferenceStore
import org.drs.jetpref.datastore.model.collectAsState
import org.drs.lib.color.neutralDynamicColorScheme
import org.drs.lib.color.systemAccentOrDefault


@Composable
fun getColorScheme(
    theme: AppTheme,
): ColorScheme {
    val prefs by DrsPreferenceStore
    val accentColor by prefs.other.accentColor.collectAsState()

    val seedColor = systemAccentOrDefault(accentColor)

    return when (theme) {
        AppTheme.AUTO, AppTheme.AUTO_AMOLED -> {
            neutralDynamicColorScheme(
                primary = seedColor,
                isDark = isSystemInDarkTheme(),
                isAmoled = theme == AppTheme.AUTO_AMOLED,
            )
        }

        AppTheme.DARK, AppTheme.LIGHT -> {
            neutralDynamicColorScheme(primary = seedColor, isDark = theme == AppTheme.DARK)
        }

        AppTheme.AMOLED_DARK -> {
            neutralDynamicColorScheme(primary = seedColor, isDark = true, isAmoled = true)
        }
    }
}

@Composable
fun DrsAppTheme(
    theme: AppTheme,
    content: @Composable () -> Unit,
) {
    val colors = getColorScheme(theme = theme)

    val darkTheme =
        theme == AppTheme.DARK
            || theme == AppTheme.AMOLED_DARK
            || (theme == AppTheme.AUTO && isSystemInDarkTheme())
            || (theme == AppTheme.AUTO_AMOLED && isSystemInDarkTheme())

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            // DRS fix: unwrap + safe cast so non-activity contexts never crash.
            var unwrappedContext = view.context
            while (unwrappedContext is android.content.ContextWrapper && unwrappedContext !is Activity) {
                unwrappedContext = unwrappedContext.baseContext
            }
            val window = (unwrappedContext as? Activity)?.window ?: return@SideEffect
            WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colors,
        typography = Typography,
        // DRS v1.0.8 (البند 13): the shape system is now actually part of the
        // theme so dialogs, sheets and cards share one corner-radius language.
        shapes = Shapes,
        content = content,
    )
}
