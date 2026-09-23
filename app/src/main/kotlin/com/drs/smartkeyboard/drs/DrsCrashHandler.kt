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

package com.drs.smartkeyboard.drs

import android.content.Context
import android.os.Build
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Complementary crash diagnostics for the DRS layer. Writes a sanitized,
 * local-only crash log (never any typed/user content) that the
 * DRS Keyboard Diagnostics screen can display. The previously installed
 * handler (e.g. CrashUtility) still runs afterwards.
 */
object DrsCrashHandler {

    private const val FILE_NAME = "drs_crash_log.txt"
    private const val MAX_LOG_CHARS = 24_000

    private var defaultHandler: Thread.UncaughtExceptionHandler? = null
    private var logFile: File? = null

    fun install(context: Context) {
        if (defaultHandler != null) return
        synchronized(this) {
            if (defaultHandler != null) return
            logFile = File(context.noBackupFilesDir, FILE_NAME)
            defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
            Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
                try {
                    append(thread, throwable)
                } catch (_: Throwable) {
                    // Logging must never break crash handling.
                }
                defaultHandler?.uncaughtException(thread, throwable)
            }
        }
    }

    private fun append(thread: Thread, throwable: Throwable) {
        val f = logFile ?: return
        val stamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())
        val sw = StringWriter()
        throwable.printStackTrace(PrintWriter(sw))
        val stack = sw.toString().lines().take(24).joinToString("\n")
        val entry = buildString {
            append("==== $stamp ====\n")
            append("thread: ${thread.name}\n")
            append("app: ${com.drs.smartkeyboard.BuildConfig.VERSION_NAME} (${com.drs.smartkeyboard.BuildConfig.VERSION_CODE})\n")
            append("android: ${Build.VERSION.SDK_INT}\n")
            append("$stack\n\n")
        }
        synchronized(this) {
            val existing = if (f.isFile) f.readText() else ""
            var combined = existing + entry
            if (combined.length > MAX_LOG_CHARS) {
                combined = combined.substring(combined.length - MAX_LOG_CHARS)
            }
            f.writeText(combined)
        }
    }

    fun readLog(): String {
        val f = logFile ?: return ""
        return try {
            if (f.isFile) f.readText() else ""
        } catch (_: Throwable) {
            ""
        }
    }

    fun clearLog() {
        try {
            logFile?.delete()
        } catch (_: Throwable) {
        }
    }
}
