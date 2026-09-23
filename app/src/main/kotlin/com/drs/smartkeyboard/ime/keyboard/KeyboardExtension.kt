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

package com.drs.smartkeyboard.ime.keyboard

import com.drs.smartkeyboard.ime.core.SubtypePreset
import com.drs.smartkeyboard.ime.nlp.PunctuationRule
import com.drs.smartkeyboard.ime.popup.PopupMappingComponent
import com.drs.smartkeyboard.ime.text.composing.Composer
import com.drs.smartkeyboard.lib.ext.Extension
import com.drs.smartkeyboard.lib.ext.ExtensionComponent
import com.drs.smartkeyboard.lib.ext.ExtensionComponentName
import com.drs.smartkeyboard.lib.ext.ExtensionEditor
import com.drs.smartkeyboard.lib.ext.ExtensionMeta
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@SerialName(KeyboardExtension.SERIAL_TYPE)
@Serializable
data class KeyboardExtension(
    override val meta: ExtensionMeta,
    override val dependencies: List<String>? = null,
    val composers: List<Composer> = listOf(),
    val currencySets: List<CurrencySet> = listOf(),
    val layouts: Map<String, List<LayoutArrangementComponent>> = mapOf(),
    val punctuationRules: List<PunctuationRule> = listOf(),
    val popupMappings: List<PopupMappingComponent> = listOf(),
    val subtypePresets: List<SubtypePreset> = listOf(),
) : Extension() {

    companion object {
        const val SERIAL_TYPE = "ime.extension.keyboard"
    }

    override fun serialType() = SERIAL_TYPE

    override fun components(): List<ExtensionComponent> {
        return emptyList()
    }

    override fun edit(): ExtensionEditor {
        throw UnsupportedOperationException(
            "Keyboard extensions are data-driven packages and have no visual editor. " +
                "Edit the .flex package files directly instead."
        )
    }
}

@Suppress("NOTHING_TO_INLINE")
inline fun extCoreComposer(id: String): ExtensionComponentName {
    return ExtensionComponentName(
        extensionId = "org.drs.composers",
        componentId = id,
    )
}

@Suppress("NOTHING_TO_INLINE")
inline fun extCoreCurrencySet(id: String): ExtensionComponentName {
    return ExtensionComponentName(
        extensionId = "org.drs.currencysets",
        componentId = id,
    )
}

@Suppress("NOTHING_TO_INLINE")
inline fun extCoreLayout(id: String): ExtensionComponentName {
    return ExtensionComponentName(
        extensionId = "org.drs.layouts",
        componentId = id,
    )
}

@Suppress("NOTHING_TO_INLINE")
inline fun extCorePunctuationRule(id: String): ExtensionComponentName {
    return ExtensionComponentName(
        extensionId = "org.drs.localization",
        componentId = id,
    )
}

@Suppress("NOTHING_TO_INLINE")
inline fun extCorePopupMapping(id: String): ExtensionComponentName {
    return ExtensionComponentName(
        extensionId = "org.drs.localization",
        componentId = id,
    )
}
