// Top-level build file — plugin declarations for all sub-projects.
// "apply false" means declared here but NOT applied to the root project;
// each module (e.g. app/) applies them individually in its own build.gradle.kts.
plugins {
    alias(libs.plugins.android.application) apply false
    // kotlin-android is NOT listed — AGP 9.x has built-in Kotlin, no separate plugin needed
    // kotlin.plugin.compose is required for Kotlin 2.x + Jetpack Compose
    alias(libs.plugins.kotlin.compose) apply false
}
