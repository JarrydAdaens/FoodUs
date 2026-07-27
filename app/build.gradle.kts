import java.util.Properties
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSetTree

// Escapes a value so it is safe to embed inside a generated Kotlin String literal.
fun String.buildConfigStringLiteral(): String =
    "\"" + replace("\\", "\\\\").replace("\"", "\\\"") + "\""

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.gmazzo.buildconfig)
    alias(libs.plugins.ksp)
    alias(libs.plugins.room)
}

room { schemaDirectory("$projectDir/schemas") }

buildConfig {
    packageName("com.maksimowiczm.foodyou.app")
    className("BuildConfig")

    val versionName = libs.versions.version.name.get()
    buildConfigField("String", "VERSION_NAME", "\"$versionName\"")

    // Fork version, layered on top of the upstream Food You version above.
    val forkVersionName = libs.versions.fork.version.name.get()
    buildConfigField("String", "FORK_VERSION_NAME", "\"$forkVersionName\"")

    // AI configuration developer fallbacks (Milestone 2, Stories 6 and 22). All three values default
    // to blank and are superseded at runtime by the user-entered settings on the AI settings screen.
    // They exist only as an optional developer convenience for local builds: set them in
    // local.properties (untracked) or an environment variable to pre-fill a device without typing
    // them in the UI. The key is a credential and must never be committed or logged; it stays blank
    // in every public build. When no user value and no fallback are set, the AI slice falls back to
    // its public endpoint/model domain defaults, and a blank effective key disables the AI actions.
    val localProperties =
        Properties().apply {
            val file = rootProject.file("local.properties")
            if (file.exists()) file.inputStream().use { load(it) }
        }
    fun secret(propertyKey: String, envKey: String, default: String = ""): String =
        localProperties.getProperty(propertyKey) ?: System.getenv(envKey) ?: default

    val aiApiKey = secret("foodus.ai.apiKey", "FOODUS_AI_API_KEY")
    val aiEndpoint = secret("foodus.ai.endpoint", "FOODUS_AI_ENDPOINT")
    val aiModel = secret("foodus.ai.model", "FOODUS_AI_MODEL")
    buildConfigField("String", "AI_API_KEY", aiApiKey.buildConfigStringLiteral())
    buildConfigField("String", "AI_ENDPOINT", aiEndpoint.buildConfigStringLiteral())
    buildConfigField("String", "AI_MODEL", aiModel.buildConfigStringLiteral())
}

kotlin {
    sourceSets.all {
        languageSettings.enableLanguageFeature("ExpectActualClasses")
        languageSettings.enableLanguageFeature("ContextParameters")
    }

    compilerOptions {
        optIn.add("androidx.compose.ui.ExperimentalComposeUiApi")
        optIn.add("androidx.compose.material3.ExperimentalMaterial3Api")
        optIn.add("androidx.compose.material3.ExperimentalMaterial3ExpressiveApi")
        optIn.add("kotlinx.coroutines.ExperimentalCoroutinesApi")
        freeCompilerArgs.add("-Xreturn-value-checker=check")
    }

    androidTarget {
        compilerOptions { jvmTarget.set(JvmTarget.JVM_21) }

        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        instrumentedTestVariant.sourceSetTree.set(KotlinSourceSetTree.test)
    }

    listOf(iosArm64(), iosSimulatorArm64()).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "App"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(projects.shared.resources)
            implementation(projects.shared.barcodescanner)

            implementation(libs.jetbrains.compose.runtime)
            implementation(libs.jetbrains.compose.foundation)
            implementation(libs.jetbrains.compose.material3)
            implementation(libs.jetbrains.compose.material.icons.extended)
            implementation(libs.jetbrains.compose.ui)
            implementation(libs.jetbrains.compose.components.resources)
            implementation(libs.jetbrains.compose.navigationevent.compose)

            implementation(libs.jetbrains.compose.navigation.compose)

            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)

            implementation(libs.androidx.datastore.preferences.core)

            implementation(libs.material.kolor)

            implementation(libs.kotlinx.serialization.json)

            implementation(libs.kotlinx.datetime)

            implementation(libs.androidx.room.runtime)
            implementation(libs.androidx.room.paging)

            // Ktor
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.client.serialization.kotlinx.json)

            implementation(libs.androidx.paging.common)
            implementation(libs.androidx.paging.compose)

            implementation(libs.reorderable)

            implementation(libs.compose.shimmer)

            implementation(libs.colorpicker.compose)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.androidx.room.testing)
            implementation(libs.androidx.sqlite.bundled)
        }

        androidMain.dependencies {
            implementation(libs.androidx.activity.compose)
            implementation(libs.androidx.appcompat)
            implementation(libs.koin.android)
            implementation(libs.ktor.client.okhttp)
            implementation(libs.sqlite.android)
        }

        androidInstrumentedTest.dependencies {
            implementation(libs.androidx.testCore)
            implementation(libs.androidx.testCore.ktx)
            implementation(libs.androidx.testRunner)
            implementation(libs.androidx.testExt.junit)
        }

        iosMain.dependencies { implementation(libs.ktor.client.darwin) }
    }
}

android {
    namespace = "com.maksimowiczm.foodyou"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        // FoodUs fork identity. Namespace stays com.maksimowiczm.foodyou so the fork's
        // source packages, generated resources, and BuildConfig keep upstream's structure for
        // clean merges; only the shipped application ID diverges.
        applicationId = "io.github.jarrydadaens.foodus"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = libs.versions.android.versionCode.get().toInt()
        // The shipped Android versionName is the fork's own <milestone>.<story>.<build> version so
        // installed-version checks (Obtainium) line up with the fork's v* release tags. The
        // upstream Food You version stays available as BuildConfig.VERSION_NAME metadata.
        versionName = libs.versions.fork.version.name.get()

        manifestPlaceholders["applicationIcon"] = "@mipmap/ic_launcher"
        manifestPlaceholders["applicationRoundIcon"] = "@mipmap/ic_launcher_round"
        manifestPlaceholders["applicationLabel"] = "@string/app_name"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    packaging { resources { excludes += "/META-INF/{AL2.0,LGPL2.1}" } }
    buildTypes {
        getByName("debug") {
            // Local sandbox identity: debug builds install side-by-side with the release-signed
            // Obtainium app instead of colliding with its package. Distinct id, label, icon, and
            // versionName suffix keep the two visually and technically separate on one device.
            applicationIdSuffix = ".dev"
            versionNameSuffix = "-dev"
            manifestPlaceholders["applicationLabel"] = "FoodUs Dev"
            manifestPlaceholders["applicationIcon"] = "@mipmap/ic_launcher_preview"
            manifestPlaceholders["applicationRoundIcon"] = "@mipmap/ic_launcher_round_preview"
        }
        getByName("release") {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
        create("devRelease") {
            initWith(getByName("release"))
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("debug")
        }
        create("miniDevRelease") {
            initWith(getByName("devRelease"))
            isMinifyEnabled = true
        }
        create("preview") {
            initWith(getByName("release"))

            applicationIdSuffix = ".preview"
            versionNameSuffix = "-preview"
            manifestPlaceholders["applicationIcon"] = "@mipmap/ic_launcher_preview"
            manifestPlaceholders["applicationRoundIcon"] = "@mipmap/ic_launcher_round_preview"
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
}

dependencies {
    debugImplementation(libs.jetbrains.compose.ui.tooling)

    listOf("kspCommonMainMetadata", "kspAndroid", "kspIosArm64", "kspIosSimulatorArm64").forEach {
        add(it, libs.androidx.room.compiler)
    }
}

compose.resources {
    publicResClass = true
    packageOfResClass = "com.maksimowiczm.foodyou.app.generated.resources"
    generateResClass = always
}
