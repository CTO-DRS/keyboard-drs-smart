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

package com.drs.smartkeyboard

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.IntentFilter
import android.os.Handler
import android.util.Log
import androidx.core.os.UserManagerCompat
import com.drs.smartkeyboard.app.DrsPreferenceModel
import com.drs.smartkeyboard.app.DrsPreferenceStore
import com.drs.smartkeyboard.ime.clipboard.ClipboardManager
import com.drs.smartkeyboard.ime.core.SubtypeManager
import com.drs.smartkeyboard.ime.dictionary.DictionaryManager
import com.drs.smartkeyboard.ime.editor.EditorInstance
import com.drs.smartkeyboard.ime.keyboard.KeyboardManager
import com.drs.smartkeyboard.ime.media.emoji.DrsEmojiCompat
import com.drs.smartkeyboard.ime.nlp.NlpManager
import com.drs.smartkeyboard.ime.text.gestures.GlideTypingManager
import com.drs.smartkeyboard.ime.theme.ThemeManager
import com.drs.smartkeyboard.lib.cache.CacheManager
import com.drs.smartkeyboard.lib.crashutility.CrashUtility
import com.drs.smartkeyboard.lib.devtools.Flog
import com.drs.smartkeyboard.lib.devtools.LogTopic
import com.drs.smartkeyboard.lib.devtools.flogError
import com.drs.smartkeyboard.lib.ext.ExtensionManager
import org.drs.jetpref.datastore.runtime.initAndroid
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.drs.lib.kotlin.io.deleteContentsRecursively
import org.drs.lib.kotlin.tryOrNull
import org.drs.libnative.dummyAdd
import java.io.File
import java.lang.ref.WeakReference

/**
 * Global weak reference for the [DrsApplication] class. This is needed as in certain scenarios an application
 * reference is needed, but the Android framework hasn't finished setting up
 */
private var DrsApplicationReference = WeakReference<DrsApplication?>(null)

@Suppress("unused")
class DrsApplication : Application() {
    companion object {
        init {
            try {
                System.loadLibrary("drs_native")
            } catch (_: Throwable) {
            }
        }
    }

    private val mainHandler by lazy { Handler(mainLooper) }
    private val scope = CoroutineScope(Dispatchers.Default)
    val preferenceStoreLoaded = MutableStateFlow(false)

    val cacheManager = lazy { CacheManager(this) }
    val clipboardManager = lazy { ClipboardManager(this) }
    val editorInstance = lazy { EditorInstance(this) }
    val extensionManager = lazy { ExtensionManager(this) }
    val glideTypingManager = lazy { GlideTypingManager(this) }
    val keyboardManager = lazy { KeyboardManager(this) }
    val nlpManager = lazy { NlpManager(this) }
    val subtypeManager = lazy { SubtypeManager(this) }
    val themeManager = lazy { ThemeManager(this) }

    override fun onCreate() {
        super.onCreate()
        DrsApplicationReference = WeakReference(this)
        try {
            Flog.install(
                context = this,
                isFloggingEnabled = BuildConfig.DEBUG,
                flogTopics = LogTopic.ALL,
                flogLevels = Flog.LEVEL_ALL,
                flogOutputs = Flog.OUTPUT_CONSOLE,
            )
            CrashUtility.install(this)
            com.drs.smartkeyboard.drs.DrsStore.init(this)
            com.drs.smartkeyboard.drs.DrsCrashHandler.install(this)
            com.drs.smartkeyboard.drs.DrsEconomy.onProcessStart()
            DrsEmojiCompat.init(this)
            flogError { "dummy result: ${dummyAdd(3,4)}" }

            if (!UserManagerCompat.isUserUnlocked(this)) {
                cacheDir?.deleteContentsRecursively()
                extensionManager.value.init()
                registerReceiver(BootComplete(), IntentFilter(Intent.ACTION_USER_UNLOCKED))
                return
            }

            init()
        } catch (t: Throwable) {
            CrashUtility.stageException(t)
            return
        }
    }

    fun init() {
        cacheDir?.deleteContentsRecursively()
        scope.launch {
            try {
                val result = DrsPreferenceStore.initAndroid(
                    context = this@DrsApplication,
                    datastoreName = DrsPreferenceModel.NAME,
                )
                Log.i("PREFS", result.toString())
                preferenceStoreLoaded.value = true
            } catch (t: Throwable) {
                // DRS: a failed preference store must never leave the splash screen
                // hanging forever — surface the crash through the crash handlers so
                // the process dies visibly and the user gets a crash notification.
                CrashUtility.stageException(t)
                throw t
            }
        }
        extensionManager.value.init()
        clipboardManager.value.initializeForContext(this)
        DictionaryManager.init(this)
    }


    private inner class BootComplete : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent == null) return
            if (intent.action == Intent.ACTION_USER_UNLOCKED) {
                try {
                    unregisterReceiver(this)
                } catch (e: Exception) {
                    flogError { e.toString() }
                }
                mainHandler.post { init() }
            }
        }
    }
}

private tailrec fun Context.drsApplication(): DrsApplication {
    return when (this) {
        is DrsApplication -> this
        is ContextWrapper -> when {
            this.baseContext != null -> this.baseContext.drsApplication()
            else -> DrsApplicationReference.get()!!
        }
        else -> tryOrNull { this.applicationContext as DrsApplication } ?: DrsApplicationReference.get()!!
    }
}

fun Context.appContext() = lazyOf(this.drsApplication())

fun Context.cacheManager() = this.drsApplication().cacheManager

fun Context.clipboardManager() = this.drsApplication().clipboardManager

fun Context.editorInstance() = this.drsApplication().editorInstance

fun Context.extensionManager() = this.drsApplication().extensionManager

fun Context.glideTypingManager() = this.drsApplication().glideTypingManager

fun Context.keyboardManager() = this.drsApplication().keyboardManager

fun Context.nlpManager() = this.drsApplication().nlpManager

fun Context.subtypeManager() = this.drsApplication().subtypeManager

fun Context.themeManager() = this.drsApplication().themeManager
