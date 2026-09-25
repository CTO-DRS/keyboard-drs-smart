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

package com.drs.smartkeyboard.ime

enum class ImeUiMode(val value: Int) {
    TEXT(0),
    MEDIA(1),
    CLIPBOARD(2),

    /** DRS v1.0.6: technical text tools panel (case, spacing, lines, info). */
    TEXT_TOOLS(3),

    /** DRS v1.15.0: the smart harakat panel (لوحة الحركات الذكية). */
    DIACRITICS(4),

    /** DRS v1.15.0: the context-aware smart symbols panel (لوحة الرموز الذكية). */
    SMART_SYMBOLS(5),

    /** DRS v1.15.0: the extended Arabic letters panel (لوحة الحروف الموسعة). */
    ARABIC_LETTERS(6);

    companion object {
        fun fromInt(int: Int) = entries.firstOrNull { it.value == int } ?: TEXT
    }

    fun toInt(): Int = value
}
