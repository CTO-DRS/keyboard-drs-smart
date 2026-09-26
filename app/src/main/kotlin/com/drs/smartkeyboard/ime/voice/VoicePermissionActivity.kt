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

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.core.content.ContextCompat
import com.drs.smartkeyboard.R

/**
 * DRS v1.23.0: the microphone permission trampoline — «بوابة الإذن
 * الشفافة». An IME service cannot host a permission dialog, so the first
 * mic-key press launches this translucent activity (nothing paints but a
 * system dialog), which requests RECORD_AUDIO, then either emits a start
 * request on the [DrsVoiceInputBus] (granted — the controller begins
 * listening immediately) or shows an honest toast (denied) and finishes.
 * Single-session safety: onDeviceAvailable checks happen in the
 * controller, not here.
 */
class VoicePermissionActivity : ComponentActivity() {

    private var languageTag: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        languageTag = intent?.getStringExtra(EXTRA_LANGUAGE_TAG).orEmpty()

        if (hasMicrophonePermission()) {
            // A previous grant survived — never re-prompt, just start.
            DrsVoiceInputBus.requestStart(languageTag)
            finish()
            return
        }
        requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO), REQUEST_CODE)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode != REQUEST_CODE) return
        if (grantResults.firstOrNull() == PackageManager.PERMISSION_GRANTED) {
            DrsVoiceInputBus.requestStart(languageTag)
        } else {
            Toast.makeText(this, R.string.voice__permission_denied, Toast.LENGTH_SHORT).show()
        }
        finish()
    }

    private fun hasMicrophonePermission(): Boolean =
        ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) ==
            PackageManager.PERMISSION_GRANTED

    companion object {
        private const val REQUEST_CODE = 7231
        const val EXTRA_LANGUAGE_TAG = "drs:extra:voice_language_tag"

        fun createIntent(context: Context, languageTag: String): Intent =
            Intent(context, VoicePermissionActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_NO_ANIMATION)
                putExtra(EXTRA_LANGUAGE_TAG, languageTag)
            }
    }
}
