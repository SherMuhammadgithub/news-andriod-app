plugins {
    alias(libs.plugins.android.application)
    // NOTE: Do NOT add kotlin-android here — AGP 9.x has built-in Kotlin support.
    // Adding it separately causes "extension 'kotlin' already registered" error.
    // Required with Kotlin 2.x to enable the Compose compiler plugin
    alias(libs.plugins.kotlin.compose)
    // Processes google-services.json → generates Firebase config resources
    // ⚠️  Requires app/google-services.json from your Firebase Console project
    alias(libs.plugins.google.services)
    // KSP runs at compile time to generate Room DAO / database code
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.example.quiz"
    compileSdk {
        // Android 16 QPR1 (API 36.1) — uses AGP 9.x major.minor API level syntax
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.sher.quiz"  // must match the package_name in google-services.json
        minSdk = 24          // Android 7.0+ (supports ~95% of devices)
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    // Enable Jetpack Compose in this module
    buildFeatures {
        compose = true
    }
}

// AGP 9.x built-in Kotlin: configure JVM target via jvmToolchain instead of kotlinOptions
kotlin {
    jvmToolchain(11)
}

dependencies {
    // ── AndroidX Core ──────────────────────────────────────────────────────────
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)

    // ── Jetpack Compose ────────────────────────────────────────────────────────
    // BOM (Bill of Materials) ensures all Compose libs use compatible versions
    val composeBom = platform(libs.androidx.compose.bom)
    implementation(composeBom)
    androidTestImplementation(composeBom)

    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    debugImplementation(libs.androidx.ui.tooling)          // Layout Inspector support
    debugImplementation(libs.androidx.ui.test.manifest)    // Compose test manifests

    // Material Design 3 — Google's latest design system
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)  // All Material icons

    // ── Lifecycle & ViewModel ──────────────────────────────────────────────────
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose) // viewModel() in Compose
    implementation(libs.androidx.activity.compose)            // setContent {} support

    // ── Navigation ─────────────────────────────────────────────────────────────
    // Compose Navigation replaces Android's Intents/FragmentManager
    implementation(libs.androidx.navigation.compose)

    // ── Firebase ───────────────────────────────────────────────────────────────
    // BOM ensures all Firebase libs are version-compatible
    val firebaseBom = platform(libs.firebase.bom)
    implementation(firebaseBom)
    implementation(libs.firebase.firestore)   // Cloud Firestore — our main database
    implementation(libs.firebase.analytics)   // Firebase Analytics (optional)

    // ── Retrofit — REST API Networking ─────────────────────────────────────────
    implementation(libs.retrofit)             // HTTP client
    implementation(libs.retrofit.gson)        // JSON ↔ Kotlin data class converter
    implementation(libs.okhttp.logging)       // Log requests/responses for debugging

    // ── Room — Local SQLite Database ───────────────────────────────────────────
    implementation(libs.room.runtime)         // Core Room library
    implementation(libs.room.ktx)             // Coroutine + Flow extensions
    ksp(libs.room.compiler)                   // KSP generates DAO implementation code

    // ── Coil — Image Loading ───────────────────────────────────────────────────
    // AsyncImage() composable that loads images from URLs in background
    implementation(libs.coil.compose)

    // ── Coroutines ─────────────────────────────────────────────────────────────
    implementation(libs.kotlinx.coroutines.android)

    // ── Testing ────────────────────────────────────────────────────────────────
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.ui.test.junit4)
}
