plugins {
    alias(libs.plugins.android.application)
    // NOTE: Do NOT add kotlin-android — AGP 9.x has built-in Kotlin support.
    // Required with Kotlin 2.x to enable the Compose compiler plugin
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.example.quiz"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.example.quiz"
        minSdk = 24
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

    buildFeatures {
        compose = true
    }
}

// AGP 9.x: configure JVM target via jvmToolchain, not kotlinOptions
kotlin {
    jvmToolchain(11)
}

dependencies {
    // ── AndroidX Core ──────────────────────────────────────────────────────────
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)

    // ── Jetpack Compose ────────────────────────────────────────────────────────
    val composeBom = platform(libs.androidx.compose.bom)
    implementation(composeBom)
    androidTestImplementation(composeBom)

    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // Material Design 3
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)

    // ── Lifecycle & ViewModel ──────────────────────────────────────────────────
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)

    // ── Navigation ─────────────────────────────────────────────────────────────
    implementation(libs.androidx.navigation.compose)

    // ── Retrofit — REST API Networking ─────────────────────────────────────────
    implementation(libs.retrofit)
    implementation(libs.retrofit.gson)
    implementation(libs.okhttp.logging)

    // ── Coil — Image Loading ───────────────────────────────────────────────────
    implementation(libs.coil.compose)

    // ── SplashScreen — branded launch screen before first Compose frame ────────
    implementation(libs.androidx.core.splashscreen)

    // ── Coroutines ─────────────────────────────────────────────────────────────
    implementation(libs.kotlinx.coroutines.android)

    // ── Testing ────────────────────────────────────────────────────────────────
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.ui.test.junit4)
}
