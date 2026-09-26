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

package com.drs.smartkeyboard.ime.keyboard

// DRS v1.23.0: the three @Deprecated("TODO: remove") entries (EDITING=1,
// SMARTBAR_CLIPBOARD_CURSOR_ROW=8, SMARTBAR_NUMBER_ROW=9) are gone. The
// audit verified none of them has a producer anywhere (no writer of the
// keyboard-mode register ever stored these ints, the register is
// runtime-only and never restored from prefs/bundles), no persisted state
// references them, no UI selector lists them and no test asserts them —
// their only consumers were three dead branches in LayoutManager which
// shipped in the same round. Unknown ints keep falling back to
// CHARACTERS via fromInt, so even a hypothetical stale value is honest.
enum class KeyboardMode(val value: Int) {
    UNSPECIFIED(-1),
    CHARACTERS(0),
    SYMBOLS(2),
    SYMBOLS2(3),
    NUMERIC(4),
    NUMERIC_ADVANCED(5),
    PHONE(6),
    PHONE2(7),
    SMARTBAR_QUICK_ACTIONS(10);

    companion object {
        fun fromInt(int: Int) = entries.firstOrNull { it.value == int } ?: CHARACTERS
    }

    override fun toString() = name.lowercase()

    fun toInt() = value
}
