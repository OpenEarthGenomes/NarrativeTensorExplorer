plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp") // Ez kell a Room adatbázishoz!
}

android {
    namespace = "com.meaning.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.meaning.app"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0-NEON"

        // NDK beállítás a C++ kernelhez
        externalNativeBuild {
            cmake {
                cppFlags("-std=c++20 -O3 -mfpu=neon") 
            }
        }
    }

    buildFeatures { compose = true }
    composeOptions { kotlinCompilerExtensionVersion = "1.5.14" }

    // Itt mondjuk meg az Androidnak, hol a C++ kód
    externalNativeBuild {
        cmake {
            path = file("src/main/cpp/CMakeLists.txt")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    // Room + KSP (Vektorok tárolásához)
    val roomVersion = "2.6.1"
    implementation("androidx.room:room-runtime:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")
    ksp("androidx.room:room-compiler:$roomVersion")

    // UI és Core
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.activity:activity-compose:1.9.0")
    implementation(platform("androidx.compose:compose-bom:2024.06.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")
}
