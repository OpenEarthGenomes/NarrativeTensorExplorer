plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.meaning.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.meaning.app"
        minSdk = 26
        targetSdk = 35
        versionCode = 11
        versionName = "1.1-PRO"

        externalNativeBuild {
            cmake {
                cppFlags("-std=c++20")
                arguments("-DANDROID_STL=c++_shared")
                abiFilters("arm64-v8a")
            }
        }
    }

    packaging {
        jniLibs {
            // Kritikus javítás Android 16 / Galaxy A35 stabilitáshoz
            useLegacyPackaging = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("debug")
        }
        debug {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlin {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        }
    }

    buildFeatures {
        compose = true
    }

    externalNativeBuild {
        cmake {
            // Ellenőrizd, hogy az útvonal pontosan ez-e a GitHubon!
            path = file("src/main/cpp/CMakeLists.txt")
            version = "3.22.1"
        }
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation(platform("androidx.compose:compose-bom:2024.11.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.material3:material3:1.3.1")
    
    val roomVersion = "2.6.1"
    implementation("androidx.room:room-runtime:$roomVersion")
    ksp("androidx.room:room-compiler:$roomVersion")
}
