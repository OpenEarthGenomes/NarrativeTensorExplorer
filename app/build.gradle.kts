plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-kapt")
    id("com.google.devtools.ksp")
    id("kotlin-parcelize")
    id("androidx.benchmark")
    id("org.jetbrains.kotlin.plugin.compose") // Kotlin 2.1.0-hoz szükséges
}

android {
    namespace = "com.meaning.app"
    compileSdk = 35 // Android 15/16-hoz javasolt

    defaultConfig {
        applicationId = "com.meaning.app"
        minSdk = 26 // A Samsung A35-höz és a modern API-khoz ideális
        targetSdk = 35
        versionCode = 11
        versionName = "1.1-PRO"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        testInstrumentationRunnerArguments["androidx.benchmark.output.enable"] = "true"
        
        vectorDrawables {
            useSupportLibrary = true
        }

        externalNativeBuild {
            cmake {
                cppFlags += "-std=c++20 -fexceptions -frtti"
                arguments += listOf(
                    "-DANDROID_STL=c++_shared",
                    "-DANDROID_ARM_NEON=TRUE",
                    "-DANDROID_PLATFORM=android-26",
                    "-DCMAKE_VERBOSE_MAKEFILE=ON"
                )
                abiFilters += listOf("arm64-v8a", "armeabi-v7a") // Samsung A35 főleg 64-bit
            }
        }
    }

    buildTypes {
        getByName("debug") {
            applicationIdSuffix = ".debug"
            isDebuggable = true
            isJniDebuggable = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules-debug.pro")
        }
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            signingConfig = signingConfigs.getByName("debug") // Ideiglenesen, amíg nincs saját kulcsod
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        isCoreLibraryDesugaringEnabled = true
    }

    kotlinOptions {
        jvmTarget = "17"
        freeCompilerArgs = freeCompilerArgs + listOf("-Xcontext-receivers", "-Xjvm-default=all")
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "/META-INF/gradle/incremental.annotation.processors"
        }
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
    val roomVersion = "2.6.1"
    val composeBom = "2024.10.01"
    val coroutinesVersion = "1.9.0"

    // ✅ CORE & COMPOSE
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.1.2")
    implementation("androidx.core:core-ktx:1.15.0")
    implementation(platform("androidx.compose:compose-bom:$composeBom"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui-tooling-preview")
    debugImplementation("androidx.compose.ui:ui-tooling")

    // ✅ ROOM (A legújabb 2.6.1-es verzióval)
    implementation("androidx.room:room-runtime:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")
    implementation("androidx.room:room-paging:$roomVersion")
    ksp("androidx.room:room-compiler:$roomVersion") // Ez váltja fel a kapt-ot a Room-nál

    // ✅ NATIVE & KERNEL SUPPORT
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:$coroutinesVersion")
    implementation("com.jakewharton.timber:timber:5.0.1")

    // ✅ TESTING & BENCHMARK
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.benchmark:benchmark-macro-junit4:1.3.3")
}
