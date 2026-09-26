/*
 * Copyright (C) 2020-2025 The DRS Smart Keyboard Project
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

package com.drs.smartkeyboard.ime.media.emoticon

import android.content.Context
import com.drs.smartkeyboard.lib.io.DrsRef
import com.drs.smartkeyboard.lib.io.loadJsonAsset
import kotlinx.serialization.Serializable

typealias EmoticonLayoutDataArrangement = List<List<EmoticonKeyData>>

@Serializable
data class EmoticonLayoutData(
    var type: String,
    var name: String,
    var direction: String,
    var arrangement: EmoticonLayoutDataArrangement = listOf()
) {
    companion object {
        /**
         * DRS v1.23.0: the loader finally exists — until this round the
         * function was a stubbed `return null` while the 21-entry
         * emoticons.json shipped dead inside the APK. Loads the kaomoji
         * arrangement through the same DrsRef asset pipeline every other
         * layout uses, null on any parse failure (the palette then stays
         * honest instead of showing an empty tab).
         */
        fun fromJsonFile(context: Context, path: String): EmoticonLayoutData? {
            return DrsRef.assets(path)
                .loadJsonAsset(context, EmoticonLayoutData.serializer())
                .getOrNull()
        }
    }
}
