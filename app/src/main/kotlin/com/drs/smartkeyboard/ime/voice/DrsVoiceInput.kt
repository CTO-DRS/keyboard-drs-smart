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
}

/**
 * The pure mic-key decision — JVM-tested. Order matters: sensitivity
 * first (privacy beats everything), then availability, then permission.
 */
fun decideVoiceInputRoute(
    recognitionAvailable: Boolean,
    permissionGranted: Boolean,
    isSensitive: Boolean,
): VoiceInputRoute = when {
    isSensitive -> VoiceInputRoute.DISABLED_SENSITIVE
    !recognitionAvailable -> VoiceInputRoute.FALLBACK_EXTERNAL
    permissionGranted -> VoiceInputRoute.START_INTERNAL
    else -> VoiceInputRoute.REQUEST_PERMISSION
}

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
        return if (AndroidVersion.ATLEAST_API31_S && SpeechRecognizer.isOnDeviceRecognitionAvailable(context)) {
            SpeechRecognizer.createOnDeviceSpeechRecognizer(context)
        } else if (SpeechRecognizer.isRecognitionAvailable(context)) {
            @Suppress("DEPRECATION") // the direct-call deprecation note targets API 31+
            SpeechRecognizer.createSpeechRecognizer(context)
        } else {
            null
        }
    }

    private inner class Listener : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) {
            DrsVoiceInputBus.uiState.value = VoiceUiState.Listening(partial = "")
        }

        override fun onBeginningOfSpeech() = Unit

        override fun onRmsChanged(rmsdB: Float) = Unit

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
