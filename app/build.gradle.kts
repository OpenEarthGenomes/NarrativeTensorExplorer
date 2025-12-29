plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    // Ez kell ide is:
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.meaning.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.meaning.app"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        externalNativeBuild {
            cmake {
                cppFlags("-std=c++20")
                arguments("-DANDROID_STL=c++_shared", "-DCMAKE_SHARED_LINKER_FLAGS=-Wl,-z,max-page-size=16384")
                abiFilters("arm64-v8a")
            }
        }
    }

    buildFeatures {
        compose = true
    }

    // FONTOS: A régi composeOptions blokkot (kotlinCompilerExtensionVersion) TELJESEN TÖRÖLD KI! 
    // Kotlin 2.0 alatt már nincs rá szükség.

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    packaging {
        jniLibs {
            useLegacyPackaging = true
        }
    }

    externalNativeBuild {
        cmake {
            path = file("src/main/cpp/CMakeLists.txt")
            version = "3.22.1"
        }
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.activity:activity-compose:1.8.2")
    implementation(platform("androidx.compose:compose-bom:2024.02.00")) // Frissítve
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
}
