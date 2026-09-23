/*
 * Copyright (C) 2025 The DRS Smart Keyboard Project
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

package com.drs.smartkeyboard.app.drsupdater

import android.content.Context
import com.drs.smartkeyboard.BuildConfig
import com.drs.smartkeyboard.lib.devtools.LogTopic
import com.drs.smartkeyboard.lib.devtools.flogError
import com.drs.smartkeyboard.lib.devtools.flogInfo
import com.drs.smartkeyboard.lib.ext.Extension
import com.drs.smartkeyboard.lib.ext.ExtensionJsonConfig
import com.drs.smartkeyboard.lib.ext.ExtensionManager
import com.drs.smartkeyboard.lib.io.ZipUtils
import java.io.File
import java.io.IOException
import java.net.URL
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.drs.lib.kotlin.io.FsDir
import org.drs.lib.kotlin.io.FsFile
import org.drs.lib.kotlin.io.readJson

/** Real extension types a remote package may target (matches ExtensionManager paths). */
object PackageTypes {
    const val THEME = "ime/theme"
    const val KEYBOARD = "ime/keyboard"
    const val LANGUAGEPACK = "ime/languagepack"

    val ALL = setOf(THEME, KEYBOARD, LANGUAGEPACK)
}

/** Lifecycle status of a remote package relative to the on-device state. */
enum class PackageStatus {
    /** Known remotely, not installed on device. */
    AVAILABLE,

    /** Installed and identical version to the remote catalog. */
    INSTALLED,

    /** Installed but the remote catalog offers a newer version. */
    UPDATE_AVAILABLE,

    /** Package requires a newer app version than the installed one. */
    INCOMPATIBLE,
}

/** A catalog entry of the official package manifest. */
data class RemotePackage(
    val id: String,
    val name: String,
    val description: String,
    val version: String,
    val type: String,
    val sizeBytes: Long,
    val sha256: String,
    val url: String,
    val releaseDate: String,
    val author: String,
    val minAppVersion: String,
    /** Real palette colors of the package content, used as an offline preview. */
    val colors: List<String>,
)

/**
 * DRS Package Center — a real, manifest-driven package distribution layer on
 * top of the app's existing extension (.flex) system.
 *
 * The catalog source of truth is a signed-by-HTTPS manifest committed to the
 * official repository (`packages/manifest.json`); each entry points to a
 * `.flex` release asset published on GitHub Releases. Every install enforces,
 * in order: HTTPS-only fetch → exact byte size → SHA-256 checksum → package
 * id/type whitelist → native extension import (which itself validates the
 * archive structure). Nothing is executed — packages are pure data (themes,
 * layouts, language packs) consumed by the existing engines.
 */
object DrsPackageManager {
    private const val CONNECT_TIMEOUT_MS = 15_000
    private const val READ_TIMEOUT_MS = 30_000

    private val json = Json { ignoreUnknownKeys = true }

    data class InstallProgress(
        val phase: Phase,
        val received: Long,
        val total: Long,
        val pkgId: String? = null,
    ) {
        enum class Phase { IDLE, DOWNLOADING, VERIFYING, INSTALLING, DONE, FAILED }
    }

    private val _progress = MutableStateFlow(InstallProgress(InstallProgress.Phase.IDLE, 0L, 0L))
    val progress: StateFlow<InstallProgress> = _progress.asStateFlow()

    // -------------------------------------------------------------------------
    // Catalog (manifest.json from the official repository)
    // -------------------------------------------------------------------------

    suspend fun fetchPackages(): Result<List<RemotePackage>> = withContext(Dispatchers.IO) {
        runCatching {
            val connection = openConnection(BuildConfig.DRS_PACKAGES_MANIFEST_URL)
                ?: throw IOException("no connection")
            val body = connection.inputStream.use { it.readBytes().decodeToString() }
            connection.disconnect()
            parseManifest(body)
        }
    }

    internal fun parseManifest(payload: String): List<RemotePackage> {
        val root = json.parseToJsonElement(payload).jsonObject
        val packages = root["packages"]?.jsonArray ?: return emptyList()
        return packages.mapNotNull { element ->
            val pkg = runCatching { element.jsonObject }.getOrNull() ?: return@mapNotNull null
            fun str(key: String) = pkg[key]?.jsonPrimitive?.content
            RemotePackage(
                id = str("package_id") ?: return@mapNotNull null,
                name = str("name") ?: return@mapNotNull null,
                description = str("description") ?: "",
                version = str("version") ?: "0.0.0",
                type = str("type") ?: return@mapNotNull null,
                sizeBytes = str("file_size")?.toLongOrNull() ?: 0L,
                sha256 = str("sha256") ?: return@mapNotNull null,
                url = str("download_url") ?: return@mapNotNull null,
                releaseDate = str("release_date") ?: "",
                author = str("author") ?: "",
                minAppVersion = str("min_app_version") ?: "0.0.0",
                colors = (pkg["colors"]?.jsonArray ?: kotlinx.serialization.json.JsonArray(emptyList()))
                    .mapNotNull { runCatching { it.jsonPrimitive.content }.getOrNull() },
            )
        }
    }

    // -------------------------------------------------------------------------
    // Status resolution against the installed extension index
    // -------------------------------------------------------------------------

    fun installedVersionOf(pkgId: String, extensionManager: ExtensionManager): String? =
        extensionManager.extensions.value.find { it.meta.id == pkgId }?.meta?.version

    fun statusOf(pkg: RemotePackage, extensionManager: ExtensionManager): PackageStatus {
        if (DrsUpdateCenter.compareVersions(
                current = BuildConfig.VERSION_NAME,
                latest = pkg.minAppVersion,
            ) < 0
        ) {
            return PackageStatus.INCOMPATIBLE
        }
        val installed = installedVersionOf(pkg.id, extensionManager) ?: return PackageStatus.AVAILABLE
        return when {
            DrsUpdateCenter.compareVersions(current = installed, latest = pkg.version) < 0 ->
                PackageStatus.UPDATE_AVAILABLE
            else -> PackageStatus.INSTALLED
        }
    }

    // -------------------------------------------------------------------------
    // Install / remove (verified download → native extension import)
    // -------------------------------------------------------------------------

    fun installPackage(
        context: Context,
        pkg: RemotePackage,
        extensionManager: ExtensionManager,
        onProgress: (InstallProgress) -> Unit = { _progress.value = it },
    ) {
        val appContext = context.applicationContext
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            _progress.value = InstallProgress(InstallProgress.Phase.DOWNLOADING, 0L, pkg.sizeBytes, pkg.id)
            try {
                val dstFile = packageFile(appContext, pkg)
                downloadToFile(pkg.url, dstFile, pkg.sizeBytes) { received, total ->
                    _progress.value = InstallProgress(InstallProgress.Phase.DOWNLOADING, received, total, pkg.id)
                }

                _progress.value = InstallProgress(InstallProgress.Phase.VERIFYING, pkg.sizeBytes, pkg.sizeBytes, pkg.id)
                verifyFile(dstFile, pkg)

                _progress.value = InstallProgress(InstallProgress.Phase.INSTALLING, pkg.sizeBytes, pkg.sizeBytes, pkg.id)
                importVerifiedPackage(appContext, dstFile, pkg, extensionManager)

                dstFile.delete()
                _progress.value = InstallProgress(InstallProgress.Phase.DONE, pkg.sizeBytes, pkg.sizeBytes, pkg.id)
            } catch (error: Throwable) {
                flogError(LogTopic.CRASH_UTILITY) { "package install failed for ${pkg.id}: $error" }
                packageFile(appContext, pkg).delete()
                _progress.value = InstallProgress(InstallProgress.Phase.FAILED, 0L, pkg.sizeBytes, pkg.id)
            }
        }
    }

    fun removePackage(pkgId: String, extensionManager: ExtensionManager): Result<Unit> = runCatching {
        val ext = extensionManager.extensions.value.find { it.meta.id == pkgId }
            ?: throw IOException("package not installed")
        extensionManager.delete(ext)
    }

    /** Re-usable verification exposed for the diagnostics screen. */
    fun verifyFile(file: File, pkg: RemotePackage) {
        if (pkg.sizeBytes > 0 && file.length() != pkg.sizeBytes) {
            throw IOException("size mismatch: got ${file.length()}, expected ${pkg.sizeBytes}")
        }
        val actual = DrsUpdateCenter.sha256Of(file)
        if (!actual.equals(pkg.sha256, ignoreCase = true)) {
            throw IOException("SHA-256 mismatch")
        }
    }

    internal fun importVerifiedPackage(
        context: Context,
        zipFile: File,
        pkg: RemotePackage,
        extensionManager: ExtensionManager,
    ) {
        val staging = FsDir(context.cacheDir, "package_staging_${pkg.id}")
        staging.deleteRecursively()
        staging.mkdirs()
        try {
            ZipUtils.unzip(zipFile, staging)
            val manifestFile = FsFile(staging, "extension.json")
            if (!manifestFile.exists()) throw IOException("invalid package: extension.json missing")
            val ext: Extension = manifestFile.readJson(ExtensionJsonConfig)

            // Security gate: the archive payload must IDENTICALLY match the
            // catalog entry — id and type come from our own manifest only.
            if (ext.meta.id != pkg.id) throw IOException("package id mismatch: ${ext.meta.id}")
            val actualType = when (ext) {
                is com.drs.smartkeyboard.ime.theme.ThemeExtension -> PackageTypes.THEME
                is com.drs.smartkeyboard.ime.keyboard.KeyboardExtension -> PackageTypes.KEYBOARD
                is com.drs.smartkeyboard.ime.nlp.LanguagePackExtension -> PackageTypes.LANGUAGEPACK
                else -> throw IOException("unknown package type")
            }
            if (actualType != pkg.type) throw IOException("package type mismatch")
            if (pkg.type !in PackageTypes.ALL) throw IOException("package type not allowed")

            ext.workingDir = staging
            extensionManager.import(ext)
            flogInfo(LogTopic.CRASH_UTILITY) { "package installed: ${pkg.id} v${pkg.version}" }
        } finally {
            staging.deleteRecursively()
        }
    }

    // -------------------------------------------------------------------------
    // Download plumbing
    // -------------------------------------------------------------------------

    internal fun packageFile(context: Context, pkg: RemotePackage): File {
        val dir = File(context.cacheDir, "packages").apply { mkdirs() }
        return File(dir, pkg.id.substringAfterLast('.') + "-" + pkg.version + ".flex")
    }

    private fun downloadToFile(
        url: String,
        dst: File,
        expectedSize: Long,
        onBytes: (Long, Long) -> Unit,
    ) {
        val connection = openConnection(url) ?: throw IOException("no connection")
        try {
            val total = connection.contentLengthLong.takeIf { it > 0 } ?: expectedSize
            var received = 0L
            connection.inputStream.use { input ->
                dst.outputStream().use { output ->
                    val buffer = ByteArray(64 * 1024)
                    while (true) {
                        val read = input.read(buffer)
                        if (read == -1) break
                        output.write(buffer, 0, read)
                        received += read
                        onBytes(received, total)
                    }
                }
            }
            if (expectedSize > 0 && received != expectedSize) {
                throw IOException("size mismatch: got $received, expected $expectedSize")
            }
        } finally {
            connection.disconnect()
        }
    }

    private fun openConnection(url: String): java.net.HttpURLConnection? {
        return runCatching {
            val connection = URL(url).openConnection() as java.net.HttpURLConnection
            connection.connectTimeout = CONNECT_TIMEOUT_MS
            connection.readTimeout = READ_TIMEOUT_MS
            connection.setRequestProperty("User-Agent", "DRS-Smart-Keyboard/${BuildConfig.VERSION_NAME}")
            connection.setRequestProperty("Accept", "application/octet-stream, application/json, */*")
            connection
        }.getOrElse { error ->
            flogError(LogTopic.CRASH_UTILITY) { "openConnection failed for $url: $error" }
            null
        }
    }
}
