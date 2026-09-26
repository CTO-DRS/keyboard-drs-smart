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

package com.drs.smartkeyboard.ime.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.annotation.StringRes
import com.drs.smartkeyboard.R
import com.drs.smartkeyboard.lib.devtools.flogError
import com.drs.smartkeyboard.lib.devtools.flogInfo
import com.drs.smartkeyboard.lib.devtools.flogWarning
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.drs.lib.android.AndroidVersion

/**
 * DRS v1.23.0: built-in voice dictation — «الميكروفون يستيقظ». Until this
 * round the mic key ended in one of two dead ends: switch to an external
 * voice IME if the ROM ships one, or a toast saying none was found. The
 * controller below drives the platform recognizer directly, shows the
 * live partial text in a bar above the keyboard, and commits the final
 * transcript into the host editor at the cursor — the same surface any
 * mainstream keyboard offers, with zero network code of our own (the
 * platform recognition service owns its own transport).
 *
 * Privacy contract (mirrors the v1.20 forced-privacy rule):
 * - password / incognito contexts never reach the recognizer;
 * - the RECORD_AUDIO permission is requested through a dedicated
 *   translucent trampoline ([VoicePermissionActivity]) because an IME
 *   service cannot host a permission dialog;
 * - the external voice-IME switch remains the honest fallback when the
 *   ROM has no recognition service at all.
 */

/** The listening UI state observed by the voice bar above the keyboard. */
sealed interface VoiceUiState {
    data object Idle : VoiceUiState
    data class Listening(val partial: String) : VoiceUiState
    data class Error(@StringRes val resId: Int) : VoiceUiState
}

/**
 * Process-wide bus between the permission trampoline activity and the
 * IME-side controller (same process, so a plain shared flow suffices).
 * The activity cannot reach the KeyboardManager directly — it emits a
 * start request after the user grants the microphone, and the controller
 * picks it up.
 */
object DrsVoiceInputBus {
    val uiState = MutableStateFlow<VoiceUiState>(VoiceUiState.Idle)

    /**
     * DRS v1.25.0: the live microphone amplitude channel — [rmsToAmplitude]
     * quantizes every [android.speech.RecognitionListener.onRmsChanged]
     * report into `0f..1f` so the bar can breathe with the user's actual
     * voice instead of a fixed animation. Many services never deliver RMS
     * at all, so consumers must keep the v1.24 pulse as the fallback and
     * only trust this channel while it actually flows.
     */
    val rmsAmplitude = MutableStateFlow(0f)

    internal val startRequests = MutableSharedFlow<String>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    /** The permission trampoline calls this after a successful grant. */
    fun requestStart(languageTag: String) {
        if (languageTag.isNotBlank()) startRequests.tryEmit(languageTag)
    }

    /** Clears any error/listening state (keyboard hidden, cancelled…). */
    fun reset() {
        uiState.value = VoiceUiState.Idle
        rmsAmplitude.value = 0f
    }
}

/** The honest route a mic-key press must take, decided purely. */
enum class VoiceInputRoute {
    /** Permission granted + recognition service present → listen now. */
    START_INTERNAL,
    /** Recognition service present but microphone not granted yet. */
    REQUEST_PERMISSION,
    /** No recognition service on the ROM → legacy external voice IME. */
    FALLBACK_EXTERNAL,
    /** Password fields and incognito mode never see the microphone. */
    DISABLED_SENSITIVE,
    /**
     * DRS v1.24.0: the user switched built-in dictation off in the typing
     * settings — an explicit choice the mic key must honor with an honest
     * toast, never silently resurrecting the external-IME switch either.
     */
    DISABLED_BY_SETTING,
    /**
     * DRS v1.25.0: the user demanded the strictly on-device recognizer
     * ([VoiceRecognizerMode.ON_DEVICE_ONLY]) but this ROM cannot honor
     * that demand — the honest answer is a toast, never a silent cloud
     * fallback the user explicitly refused.
     */
    ON_DEVICE_UNAVAILABLE,
}

/**
 * DRS v1.25.0: which recognizer may listen — «المستخدم يختار أين يُسمع
 * كلامه». [AUTO] keeps the v1.23 behavior (on-device when the ROM offers
 * it, standard otherwise); [ON_DEVICE_ONLY] refuses to speak to any
 * network-backed service (privacy-first users, offline ROMs); [STANDARD]
 * pins the classic recognizer for users whose on-device engine quality
 * disappoints. Stored by name in the settings datastore.
 */
enum class VoiceRecognizerMode {
    AUTO,
    ON_DEVICE_ONLY,
    STANDARD,
}

/**
 * The pure mic-key decision — JVM-tested. Order matters: sensitivity
 * first (privacy beats everything), then the explicit user setting
 * (DRS v1.24.0 — a chosen-off feature stays off), then the recognizer
 * mode demand (DRS v1.25.0 — a chosen-strict on-device gate answers
 * honestly when the ROM cannot honor it), then availability, then
 * permission. Every call site must pass the pref-backed [userEnabled]
 * and [recognizerMode] so the settings gates are the real gates. The
 * two v1.25.0 parameters default to the v1.23/v1.24 behavior so older
 * call sites and tests stay honest without edits.
 */
fun decideVoiceInputRoute(
    recognitionAvailable: Boolean,
    permissionGranted: Boolean,
    isSensitive: Boolean,
    userEnabled: Boolean = true,
    recognizerMode: VoiceRecognizerMode = VoiceRecognizerMode.AUTO,
    onDeviceAvailable: Boolean = false,
): VoiceInputRoute = when {
    isSensitive -> VoiceInputRoute.DISABLED_SENSITIVE
    !userEnabled -> VoiceInputRoute.DISABLED_BY_SETTING
    recognizerMode == VoiceRecognizerMode.ON_DEVICE_ONLY && !onDeviceAvailable ->
        VoiceInputRoute.ON_DEVICE_UNAVAILABLE
    !recognitionAvailable -> VoiceInputRoute.FALLBACK_EXTERNAL
    permissionGranted -> VoiceInputRoute.START_INTERNAL
    else -> VoiceInputRoute.REQUEST_PERMISSION
}

/**
 * DRS v1.25.0: normalizes the recognizer's raw RMS dB report onto the
 * `0f..1f` amplitude the bar's live wave renders. Android services
 * typically report roughly `-2..12` dB with silence wobbling around
 * `0`, so the linear window below maps that band to the full range and
 * clamps everything else. The result is quantized to 2% steps to keep
 * the StateFlow from recomposing the bar on every jitter of the last
 * decimal. Pure — JVM-tested.
 */
fun rmsToAmplitude(rmsdB: Float): Float {
    val normalized = (rmsdB - RMS_DB_FLOOR) / (RMS_DB_CEIL - RMS_DB_FLOOR)
    val amplitude = normalized.coerceIn(0f, 1f)
    return (amplitude * 50f).toInt() / 50f
}

private const val RMS_DB_FLOOR = -2f
private const val RMS_DB_CEIL = 12f

/**
 * Owns the platform [SpeechRecognizer] lifecycle. Every recognizer call
 * is posted to the main thread (the recognizer is main-thread-only),
 * results commit through [onCommit] and the bar state flows through
 * [DrsVoiceInputBus.uiState]. A single listening session at a time — a
 * new start tears the previous session down first.
 */
class DrsVoiceInputController(
    private val context: Context,
    private val onCommit: (String) -> Unit,
    /**
     * DRS v1.25.0: read lazily at every start so a settings change
     * between sessions is honored without recreating the controller.
     */
    private val recognizerMode: () -> VoiceRecognizerMode = { VoiceRecognizerMode.AUTO },
) {
    private val mainHandler = Handler(Looper.getMainLooper())
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var recognizer: SpeechRecognizer? = null

    init {
        // The permission trampoline grants the mic and emits a start
        // request — this collector turns it into a real listening session
        // on the main thread.
        scope.launch {
            DrsVoiceInputBus.startRequests.collect { tag -> start(tag) }
        }
    }

    /** Starts a dictation session in [languageTag] (BCP-47, e.g. ar-SA). */
    fun start(languageTag: String) {
        mainHandler.post {
            // One session at a time — a re-start tears the old one down.
            destroyRecognizer()
            val sr = try {
                createRecognizer()
            } catch (e: Throwable) {
                flogError { "recognizer creation failed: $e" }
                DrsVoiceInputBus.uiState.value = VoiceUiState.Error(R.string.voice__error)
                return@post
            }
            if (sr == null) {
                flogWarning { "recognizer unavailable at start time" }
                DrsVoiceInputBus.uiState.value = VoiceUiState.Error(R.string.voice__error)
                return@post
            }
            recognizer = sr
            sr.setRecognitionListener(Listener())
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(
                    RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM,
                )
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, languageTag)
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)
            }
            // DRS v1.25.0: a fresh session starts from silence, never
            // from the previous session's last amplitude.
            DrsVoiceInputBus.rmsAmplitude.value = 0f
            DrsVoiceInputBus.uiState.value = VoiceUiState.Listening(partial = "")
            try {
                sr.startListening(intent)
                flogInfo { "listening started lang=$languageTag" }
            } catch (e: Throwable) {
                flogError { "startListening failed: $e" }
                DrsVoiceInputBus.uiState.value = VoiceUiState.Error(R.string.voice__error)
                destroyRecognizer()
            }
        }
    }

    /** Cancels any live session and clears the bar state. */
    fun stop() {
        mainHandler.post {
            try {
                recognizer?.cancel()
            } catch (_: Throwable) {
            }
            destroyRecognizer()
            DrsVoiceInputBus.reset()
        }
    }

    /** Final teardown (service destroyed) — stops the collector too. */
    fun destroy() {
        mainHandler.post {
            try {
                recognizer?.destroy()
            } catch (_: Throwable) {
            }
            recognizer = null
            DrsVoiceInputBus.reset()
        }
        scope.cancel()
    }

    private fun destroyRecognizer() {
        try {
            recognizer?.destroy()
        } catch (_: Throwable) {
        }
        recognizer = null
    }

    private fun createRecognizer(): SpeechRecognizer? {
        val onDevicePossible = AndroidVersion.ATLEAST_API31_S &&
            SpeechRecognizer.isOnDeviceRecognitionAvailable(context)
        return when (recognizerMode()) {
            // DRS v1.25.0: the user demanded on-device only — a ROM that
            // cannot honor the demand gets null, and the honest error
            // path below speaks instead of a silent cloud fallback.
            VoiceRecognizerMode.ON_DEVICE_ONLY ->
                if (onDevicePossible) SpeechRecognizer.createOnDeviceSpeechRecognizer(context) else null
            // The user pinned the classic recognizer (typically because
            // the on-device engine quality disappoints on their ROM).
            VoiceRecognizerMode.STANDARD ->
                if (SpeechRecognizer.isRecognitionAvailable(context)) {
                    @Suppress("DEPRECATION") // the direct-call deprecation note targets API 31+
                    SpeechRecognizer.createSpeechRecognizer(context)
                } else {
                    null
                }
            // AUTO — the exact v1.23.0 preference order: on-device when
            // the ROM offers it, standard otherwise, null when neither.
            VoiceRecognizerMode.AUTO ->
                if (onDevicePossible) {
                    SpeechRecognizer.createOnDeviceSpeechRecognizer(context)
                } else if (SpeechRecognizer.isRecognitionAvailable(context)) {
                    @Suppress("DEPRECATION") // the direct-call deprecation note targets API 31+
                    SpeechRecognizer.createSpeechRecognizer(context)
                } else {
                    null
                }
        }
    }

    private inner class Listener : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) {
            DrsVoiceInputBus.uiState.value = VoiceUiState.Listening(partial = "")
        }

        override fun onBeginningOfSpeech() = Unit

        // DRS v1.25.0: the live amplitude channel — quantized to 2%
        // steps so the bar recomposes with the voice, not with jitter.
        override fun onRmsChanged(rmsdB: Float) {
            DrsVoiceInputBus.rmsAmplitude.value = rmsToAmplitude(rmsdB)
        }

        override fun onBufferReceived(buffer: ByteArray?) = Unit

        override fun onEndOfSpeech() = Unit

        override fun onError(error: Int) {
            flogWarning { "recognition error=$error" }
            DrsVoiceInputBus.uiState.value = VoiceUiState.Error(errorResOf(error))
            destroyRecognizer()
        }

        override fun onResults(results: Bundle?) {
            val text = results
                ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                ?.firstOrNull()
                .orEmpty()
                .trim()
            destroyRecognizer()
            if (text.isEmpty()) {
                DrsVoiceInputBus.uiState.value = VoiceUiState.Error(R.string.voice__error_no_match)
            } else {
                DrsVoiceInputBus.reset()
                onCommit(text)
            }
        }

        override fun onPartialResults(partialResults: Bundle?) {
            val partial = partialResults
                ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                ?.firstOrNull()
                .orEmpty()
            DrsVoiceInputBus.uiState.value = VoiceUiState.Listening(partial = partial)
        }

        override fun onEvent(eventType: Int, params: Bundle?) = Unit
    }

    private companion object {
        fun errorResOf(error: Int): Int = when (error) {
            SpeechRecognizer.ERROR_NO_MATCH -> R.string.voice__error_no_match
            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> R.string.voice__error_no_speech
            else -> R.string.voice__error
        }
    }
}
