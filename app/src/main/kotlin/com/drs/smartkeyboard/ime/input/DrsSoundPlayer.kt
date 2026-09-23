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

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import com.drs.smartkeyboard.lib.devtools.flogDebug
import java.util.concurrent.ConcurrentHashMap

/**
 * Plays the DRS bundled key sound packs through a single shared SoundPool.
 * All samples are tiny (<2 KB) mono WAV files in res/raw, loaded once per
 * process; playback is fully local and adds no latency to key presses
 * (unloaded samples are silently skipped until ready).
 */
object DrsSoundPlayer {

    private const val MAX_STREAMS = 4
    private const val TAG = "DrsSoundPlayer"

    @Volatile
    private var soundPool: SoundPool? = null

    /** resId -> SoundPool soundId. */
    private val soundIds = ConcurrentHashMap<Int, Int>()

    /** SoundPool soundIds that finished async loading and are safe to play. */
    private val readyIds = ConcurrentHashMap.newKeySet<Int>()

    fun ensureInitialized(context: Context) {
        if (soundPool != null) return
        synchronized(this) {
            if (soundPool != null) return
            try {
                val attrs = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
                val pool = SoundPool.Builder()
                    .setMaxStreams(MAX_STREAMS)
                    .setAudioAttributes(attrs)
                    .build()
                pool.setOnLoadCompleteListener { _, sampleId, status ->
                    if (status == 0) {
                        readyIds.add(sampleId)
                    }
                }
                soundPool = pool
                DrsSoundStyle.entries.filterNotNullRes().forEach { resId ->
                    val id = pool.load(context, resId, 1)
                    soundIds[resId] = id
                }
            } catch (t: Throwable) {
                // Custom key sounds are a nice-to-have and must never crash the keyboard.
                flogDebug { "$TAG init failed: $t" }
                soundPool = null
            }
        }
    }

    private fun Iterable<DrsSoundStyle>.filterNotNullRes(): List<Int> =
        mapNotNull { it.resId }

    /** Plays one bundled sample at the given volume (0..1). Silently no-ops when not ready. */
    fun play(resId: Int, volume: Float) {
        val pool = soundPool ?: return
        val soundId = soundIds[resId] ?: return
        if (soundId !in readyIds) return
        try {
            val v = volume.coerceIn(0f, 1f)
            pool.play(soundId, v, v, 1, 0, 1f)
        } catch (_: Throwable) {
            // Never break typing because of a sound glitch.
        }
    }

    fun release() {
        synchronized(this) {
            try {
                soundPool?.release()
            } catch (_: Throwable) {
                // ignore
            }
            soundPool = null
            soundIds.clear()
            readyIds.clear()
        }
    }
}
