/*
 * Copyright (C) 2021-2025 The DRS Smart Keyboard Project
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

package com.drs.smartkeyboard.ime.input

import com.drs.smartkeyboard.R

/**
 * DRS key sound styles. SYSTEM keeps the upstream behavior (Android system
 * key press sound effects); every other entry plays a bundled, locally
 * synthesized sound pack through [DrsSoundPlayer].
 */
enum class DrsSoundStyle(val resId: Int?) {
    /** Use the classic system sound effects (upstream behavior). */
    SYSTEM(null),

    /** Soft sine tick - the comfortable default. */
    CLASSIC(R.raw.drs_snd_classic),

    /** Clean bright modern click. */
    MODERN(R.raw.drs_snd_modern),

    /** Warm muffled tap. */
    SOFT(R.raw.drs_snd_soft),

    /** Sharp short mechanical click. */
    CLICK(R.raw.drs_snd_click),

    /** Typewriter-style snap with a low thump. */
    TYPER(R.raw.drs_snd_typer),

    /** Playful bubble pop. */
    POP(R.raw.drs_snd_pop),
}
