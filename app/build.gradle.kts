/*
 * Copyright (C) 2022-2025 The DRS Smart Keyboard Project
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

import com.android.build.api.dsl.ApplicationExtension
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.agp.application)
    alias(libs.plugins.kotlin.plugin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotest)
    alias(libs.plugins.kotlinx.kover)
}

val projectMinSdk: String by project
val projectTargetSdk: String by project
val projectCompileSdk: String by project
val projectVersionCode: String by project
val projectVersionName: String by project
val projectVersionNameSuffix = projectVersionName.substringAfter("-", "").let { suffix ->
    if (suffix.isNotEmpty()) {
        "-$suffix"
    } else {
        suffix
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_11)
        freeCompilerArgs.set(listOf(
            "-opt-in=kotlin.contracts.ExperimentalContracts",
            "-jvm-default=enable",
            "-Xwhen-guards",
            "-Xexplicit-backing-fields",
            "-Xcontext-parameters",
            "-XXLanguage:+LocalTypeAliases",
        ))
    }
}

configure<ApplicationExtension> {
    namespace = "com.drs.smartkeyboard"
    compileSdk = projectCompileSdk.toInt()
    buildToolsVersion = tools.versions.buildTools.get()
    ndkVersion = tools.versions.ndk.get()

    signingConfigs {
        create("release") {
            // The release keystore is provisioned OUTSIDE version control:
            // CI decodes it from the DRS_KEYSTORE_B64 secret into the path
            // given by DRS_KEYSTORE_PATH (see .github/workflows/release.yml).
            // Local builds fall back to the (git-ignored) local keystore file
            // so the same signing key is used everywhere for update continuity.
            storeFile = System.getenv("DRS_KEYSTORE_PATH")?.let { rootProject.file(it) }
                ?: rootProject.file("keystore/DRS-Release-Key.keystore")
            storePassword = System.getenv("DRS_KEYSTORE_PASSWORD") ?: "android"
            keyAlias = System.getenv("DRS_KEY_ALIAS") ?: "androiddebugkey"
            keyPassword = System.getenv("DRS_KEY_PASSWORD") ?: "android"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    defaultConfig {
        applicationId = "com.drs.smartkeyboard"
        minSdk = projectMinSdk.toInt()
        targetSdk = projectTargetSdk.toInt()
        versionCode = projectVersionCode.toInt()
        versionName = projectVersionName.substringBefore("-")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "BUILD_COMMIT_HASH", "\"${getGitCommitHash().get()}\"")
        buildConfigField("String", "DRS_ADDONS_API_VERSION", "\"v~draft2\"")
        buildConfigField("String", "DRS_ADDONS_URL", "\"github.com/CTO-DRS/keyboard-drs-smart\"")
        buildConfigField("String", "DRS_RELEASES_API", "\"https://api.github.com/repos/CTO-DRS/keyboard-drs-smart/releases/latest\"")
        buildConfigField("String", "DRS_PACKAGES_MANIFEST_URL", "\"https://raw.githubusercontent.com/CTO-DRS/keyboard-drs-smart/main/packages/manifest.json\"")

        sourceSets {
            maybeCreate("main").apply {
                assets.directories += "src/main/assets"
            }
        }
    }

    bundle {
        language {
            // We disable language split because DRS Keyboard does not use
            // runtime Google Play Service APIs and thus cannot dynamically
            // request to download the language resources for a specific locale.
            enableSplit = false
        }
    }

    buildFeatures {
        buildConfig = true
        compose = true
    }

    buildTypes {
        named("debug") {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug+${getGitCommitHash(short = true).get()}"

            isDebuggable = true
            isJniDebuggable = false
        }

        create("beta") {
            applicationIdSuffix = ".beta"
            versionNameSuffix = projectVersionNameSuffix

            // DRS: R8 (AGP 9.0.32 / R8 9.0.32) deterministically stripped most of the
            // program (every Compose screen, material3, JetPref runtime, the KSP-generated
            // DrsPreferenceModelImpl) even though everything is referenced by kept entry
            // points — the shipped APK contained a 3.1MB dex without any UI and booted
            // into a permanently blank screen. Minification stays OFF until the AGP-9
            // R8 pipeline issue is root-caused and re-validated end to end.
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            isMinifyEnabled = false
            isShrinkResources = false
        }

        named("release") {
            versionNameSuffix = projectVersionNameSuffix

            signingConfig = signingConfigs.getByName("release")
            // DRS: see the beta block comment — R8 must stay disabled for now.
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            isMinifyEnabled = false
            isShrinkResources = false
        }

        create("benchmark") {
            initWith(getByName("release"))

            applicationIdSuffix = ".bench"
            versionNameSuffix = "-bench+${getGitCommitHash(short = true).get()}"

            signingConfig = signingConfigs.getByName("debug")
            matchingFallbacks += listOf("release")
        }
    }

    lint {
        baseline = file("lint.xml")
        // Workaround for a known AGP lint tool crash (not a code defect):
        // the UElementAsPsi detector throws while resolving KtLambdaExpression
        // in Flog.kt under the K2 frontend. Lint itself suggests disabling it.
        disable += "UElementAsPsi"
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
        unitTests.all {
            it.useJUnitPlatform()
        }
    }
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
    arg("room.incremental", "true")
    arg("room.expandProjection", "true")
}

tasks.withType<Test> {
    testLogging {
        events = setOf(TestLogEvent.FAILED, TestLogEvent.PASSED, TestLogEvent.SKIPPED)
    }
    useJUnitPlatform()
}

kover {
    useJacoco()
}

dependencies {
    val composeBom = platform(libs.androidx.compose.bom)
    implementation(composeBom)
    // testImplementation(composeBom)
    // androidTestImplementation(composeBom)

    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.autofill)
    implementation(libs.androidx.collection.ktx)
    implementation(libs.androidx.compose.material.icons)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.runtime.livedata)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.emoji2)
    implementation(libs.androidx.emoji2.views)
    implementation(libs.androidx.exifinterface)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.profileinstaller)
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.window.core)
    implementation(libs.cache4k)
    implementation(libs.kotlin.reflect)
    implementation(libs.kotlinx.coroutines)
    implementation(libs.kotlinx.serialization.json)
    implementation(projects.lib.jetpref.datastoreModel)
    ksp(projects.lib.jetpref.datastoreModelProcessor)
    implementation(projects.lib.jetpref.datastoreUi)
    implementation(projects.lib.jetpref.materialUi)

    implementation(projects.lib.android)
    implementation(projects.lib.color)
    implementation(projects.lib.compose)
    implementation(projects.lib.kotlin)
    implementation(projects.lib.native)
    implementation(projects.lib.snygg)

    testImplementation(libs.kotest.assertions.core)
    testImplementation(libs.kotest.property)
    testImplementation(libs.kotest.runner.junit5)
    testImplementation(libs.kotlin.test.junit5)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)
    androidTestImplementation(libs.androidx.test.ext)
    androidTestImplementation(libs.androidx.test.espresso.core)
}

fun getGitCommitHash(short: Boolean = false): Provider<String> {
    if (!File(".git").exists()) {
        return providers.provider { "null" }
    }

    val execProvider = providers.exec {
        if (short) {
            commandLine("git", "rev-parse", "--short", "HEAD")
        } else {
            commandLine("git", "rev-parse", "HEAD")
        }
    }
    return execProvider.standardOutput.asText.map { it.trim() }
}
