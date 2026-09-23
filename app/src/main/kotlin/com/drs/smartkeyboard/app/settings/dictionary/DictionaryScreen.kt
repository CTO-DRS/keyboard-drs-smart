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

package com.drs.smartkeyboard.app.settings.dictionary

import androidx.compose.runtime.Composable
import com.drs.smartkeyboard.R
import com.drs.smartkeyboard.app.LocalNavController
import com.drs.smartkeyboard.app.Routes
import com.drs.smartkeyboard.lib.compose.DrsScreen
import org.drs.jetpref.datastore.ui.Preference
import org.drs.jetpref.datastore.ui.SwitchPreference
import org.drs.lib.compose.stringRes

@Composable
fun DictionaryScreen() = DrsScreen {
    title = stringRes(R.string.settings__dictionary__title)
    previewFieldVisible = true

    val navController = LocalNavController.current

    content {
        SwitchPreference(
            prefs.dictionary.enableSystemUserDictionary,
            title = stringRes(R.string.pref__dictionary__enable_system_user_dictionary__label),
            summary = stringRes(R.string.pref__dictionary__enable_system_user_dictionary__summary),
        )
        Preference(
            title = stringRes(R.string.pref__dictionary__manage_system_user_dictionary__label),
            summary = stringRes(R.string.pref__dictionary__manage_system_user_dictionary__summary),
            onClick = { navController.navigate(Routes.Settings.UserDictionary(UserDictionaryType.SYSTEM)) },
            enabledIf = { prefs.dictionary.enableSystemUserDictionary isEqualTo true },
        )
        SwitchPreference(
            prefs.dictionary.enableDrsUserDictionary,
            title = stringRes(R.string.pref__dictionary__enable_internal_user_dictionary__label),
            summary = stringRes(R.string.pref__dictionary__enable_internal_user_dictionary__summary),
        )
        Preference(
            title = stringRes(R.string.pref__dictionary__manage_drs_user_dictionary__label),
            summary = stringRes(R.string.pref__dictionary__manage_drs_user_dictionary__summary),
            onClick = { navController.navigate(Routes.Settings.UserDictionary(UserDictionaryType.DRS)) },
            enabledIf = { prefs.dictionary.enableDrsUserDictionary isEqualTo true },
        )
        SwitchPreference(
            prefs.dictionary.learnFromSuggestions,
            title = stringRes(R.string.pref__dictionary__learn_from_suggestions__label),
            summary = stringRes(R.string.pref__dictionary__learn_from_suggestions__summary),
        )
        SwitchPreference(
            prefs.dictionary.learnTypedWords,
            title = stringRes(R.string.pref__dictionary__learn_typed_words__label),
            summary = stringRes(R.string.pref__dictionary__learn_typed_words__summary),
        )
    }
}
