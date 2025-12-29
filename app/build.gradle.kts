plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android") version "1.9.0" 
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.devtools.ksp") version "1.9.0-1.0.13"
}

android {
    namespace = "com.meaning.app"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.meaning.app"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.1-FIX"

        externalNativeBuild {
            cmake {
                cppFlags("-std=c++17")
                abiFilters("arm64-v8a")
            }
        }
    }
    // ... a többi marad ...
}
