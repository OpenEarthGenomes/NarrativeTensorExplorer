plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.meaning.app" // ✅ VÉGLEGES
    compileSdk = 36

    defaultConfig {
        applicationId = "com.meaning.app" // ✅ VÉGLEGES
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.1-PRO"

        externalNativeBuild {
            cmake {
                cppFlags("-std=c++20")
                arguments("-DANDROID_ARM_NEON=ON")
            }
        }
    }
    // ... többi rész változatlan
}

