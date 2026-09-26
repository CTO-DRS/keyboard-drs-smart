/*
 * Copyright (C) 2025-2026 The DRS Smart Keyboard Project
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

package com.drs.smartkeyboard.ime.input

/**
 * DRS v1.22.0: the latch state of the revived CTRL/ALT modifier keys —
 * «إحياء المفاتيح الميتة». Until this round the CTRL/ALT key codes fell
 * into the unknown-key branch of the input pipeline (drawn if a layout
 * declares them, dead on press, logged as an error). The latching
 * behavior mirrors the shift family users already know:
 *
 * - OFF: the modifier does nothing.
 * - LATCHED (one-shot): armed for the next consuming key — an arrow
 *   becomes word/line/page navigation, DELETE becomes word deletion —
 *   then releases itself.
 * - LOCKED: armed until the key is tapped again (the CTRL_LOCK/ALT_LOCK
 *   codes jump straight here, exactly like caps lock vs. shift).
 *
 * Typing a normal character keeps the character honest (no host-side
 * ctrl+letter emulation, which IMEs cannot deliver reliably) and
 * consumes a latched modifier so it can never surprise the user later.
 */
enum class InputModifierState(val value: Int) {
    OFF(0),
    LATCHED(1),
    LOCKED(2),
    ;

    /** Whether the modifier currently arms the next consuming key. */
    val isArmed: Boolean
        get() = this != OFF

    /**
     * The state after one consuming press: a one-shot latch releases,
     * a lock persists — that is the whole point of locking.
     */
    fun consumed(): InputModifierState = if (this == LATCHED) OFF else this

    companion object {
        fun fromInt(int: Int) = entries.firstOrNull { it.value == int } ?: OFF
    }

    override fun toString() = name.lowercase()

    fun toInt() = value
}

/**
 * The pure latch cycle — DRS v1.22.0. A plain modifier tap walks
 * OFF -> LATCHED -> LOCKED -> OFF; the *_LOCK codes jump straight to
 * [InputModifierState.LOCKED] in one press. Pure and testable: the
 * same function feeds the input pipeline and the unit contracts.
 */
fun cycleModifierLatch(current: InputModifierState, lock: Boolean): InputModifierState = when {
    lock -> InputModifierState.LOCKED
    current == InputModifierState.OFF -> InputModifierState.LATCHED
    current == InputModifierState.LATCHED -> InputModifierState.LOCKED
    else -> InputModifierState.OFF
}

/**
 * DRS v1.23.0: the FN latch's real work — mapping the digit keys onto
 * the function keys F1–F10 of the host (android.view.KeyEvent codes
 * KEYCODE_F1=131 … KEYCODE_F12=142; only F1–F10 have digits). The on-screen
 * digit keys carry the ASCII digit codes ('0'=48 … '9'=57), so the honest
 * contract is: '1'→F1 … '9'→F9, '0'→F10 — exactly what a physical
 * keyboard's Fn row delivers in terminals, remote-desktop clients and
 * console emulators. Returns null for anything that is not a digit key:
 * the caller then keeps the normal digit commit. Pure and JVM-tested.
 */
fun fnFunctionKeyCodeOf(digitCode: Int): Int? {
    val digit = digitCode - 48 // '0'
    return when {
        digitCode !in 48..57 -> null
        digit == 0 -> 140      // KEYCODE_F10
        else -> 130 + digit    // KEYCODE_F1 + (1..9) → F1..F9
    }
}
