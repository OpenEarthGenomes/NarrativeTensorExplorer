// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "8.7.2" apply false
    id("org.jetbrains.kotlin.android") version "2.1.0" apply false
    id("org.jetbrains.kotlin.kapt") version "2.1.0" apply false
    id("com.android.library") version "8.7.2" apply false
    id("androidx.benchmark") version "1.2.0-alpha16" apply false
    id("org.jetbrains.kotlin.plugin.parcelize") version "2.1.0" apply false
    id("com.google.devtools.ksp") version "2.1.0-1.0.29" apply false  // ✅ KSP PLUGIN
    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin") version "2.0.1" apply false
}

// KSP extension configuration
extensions.findByName("buildScan")?.withGroovyBuilder {
    setProperty("termsOfServiceUrl", "https://gradle.com/terms-of-service")
    setProperty("termsOfServiceAgree", "yes")
}

// Clean task for native builds
tasks.register("cleanNative", Delete::class) {
    delete(
        fileTree("app/src/main/jniLibs"),
        fileTree("build-native"),
        fileTree(".externalNativeBuild"),
        fileTree("**/build/generated/ksp"),
        fileTree("**/.kotlin"),
        fileTree("**/build/tmp/kapt")
    )
}

// Task to build native libraries
tasks.register("buildNative") {
    dependsOn(":app:externalNativeBuildDebug")
    doLast {
        println("✅ Native libraries built")
    }
}

// KSP source set configuration
subprojects {
    afterEvaluate {
        // Configure KSP for each module
        if (project.plugins.hasPlugin("com.google.devtools.ksp")) {
            configure<com.google.devtools.ksp.gradle.KspExtension> {
                // Allow KSP to use incremental processing
                arg("room.schemaLocation", "${project.projectDir}/schemas")
                arg("room.incremental", "true")
                arg("room.expandProjection", "true")
            }
            
            // Add KSP generated sources to the source sets
            android?.sourceSets?.getByName("main") {
                java.srcDir("build/generated/ksp/main/kotlin")
            }
            android?.sourceSets?.getByName("test") {
                java.srcDir("build/generated/ksp/test/kotlin")
            }
            android?.sourceSets?.getByName("androidTest") {
                java.srcDir("build/generated/ksp/androidTest/kotlin")
            }
        }
    }
}

// Custom task for performance testing
tasks.register("runBenchmarks") {
    dependsOn(":benchmark:connectedCheck")
    doLast {
        println("📊 Benchmarks completed")
    }
}

// Task to check APK size
tasks.register("checkApkSize") {
    doLast {
        val apkFiles = fileTree("app/build/outputs/apk") {
            include("**/*.apk")
        }
        
        apkFiles.forEach { apk ->
            val sizeMB = apk.length() / (1024.0 * 1024.0)
            val formattedSize = "%.2f".format(sizeMB)
            println("📦 ${apk.name}: ${formattedSize}MB")
            
            if (sizeMB > 100) {
                println("⚠️  WARNING: ${apk.name} exceeds 100MB (${formattedSize}MB)")
            }
        }
    }
}

// Task to run KSP processing
tasks.register("kspDebug") {
    dependsOn(":app:kspDebugKotlin")
    doLast {
        println("🔧 KSP processing completed for debug")
    }
}

tasks.register("kspRelease") {
    dependsOn(":app:kspReleaseKotlin")
    doLast {
        println("🔧 KSP processing completed for release")
    }
}

// Repository configuration
allprojects {
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
        maven { url = uri("https://maven.google.com") }
        maven { url = uri("https://oss.sonatype.org/content/repositories/snapshots/") }
        maven { url = uri("https://androidx.dev/storage/compose-compiler/repository/") }
    }
    
    // Configure all Android projects
    plugins.withId("com.android.application") {
        configure<com.android.build.gradle.AppExtension> {
            compileSdk = 34
            
            defaultConfig {
                minSdk = 24
                targetSdk = 34
                versionCode = 11  // 1.1-PRO
                versionName = "1.1-PRO"
                
                testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
                testInstrumentationRunnerArguments["androidx.benchmark.output.enable"] = "true"
                
                vectorDrawables {
                    useSupportLibrary = true
                }
                
                // KSP arguments
                javaCompileOptions {
                    annotationProcessorOptions {
                        arguments += mapOf(
                            "room.schemaLocation" to "$projectDir/schemas",
                            "room.incremental" to "true",
                            "room.expandProjection" to "true"
                        )
                    }
                }
            }
            
            compileOptions {
                sourceCompatibility = JavaVersion.VERSION_17
                targetCompatibility = JavaVersion.VERSION_17
                isCoreLibraryDesugaringEnabled = true
            }
            
            kotlinOptions {
                jvmTarget = "17"
                freeCompilerArgs = freeCompilerArgs + listOf(
                    "-opt-in=kotlin.RequiresOptIn",
                    "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
                    "-opt-in=androidx.compose.animation.ExperimentalAnimationApi",
                    "-opt-in=androidx.compose.foundation.ExperimentalFoundationApi",
                    "-opt-in=androidx.compose.material3.ExperimentalMaterial3Api",
                    "-opt-in=androidx.compose.ui.ExperimentalComposeUiApi",
                    "-Xcontext-receivers"
                )
            }
            
            buildFeatures {
                compose = true
                buildConfig = true
                viewBinding = false
                aidl = false
                renderScript = false
                shaders = false
            }
            
            composeOptions {
                kotlinCompilerExtensionVersion = "1.5.4"
            }
            
            packaging {
                resources {
                    excludes += "/META-INF/{AL2.0,LGPL2.1}"
                    excludes += "/META-INF/gradle/incremental.annotation.processors"
                    excludes += "/META-INF/INDEX.LIST"
                    excludes += "/META-INF/io.netty.versions.properties"
                }
                jniLibs {
                    useLegacyPackaging = true
                }
            }
            
            namespace = "com.meaning.app"
            
            // Signing configs
            signingConfigs {
                create("release") {
                    storeFile = file("keystore/meaning-archive.jks")
                    storePassword = System.getenv("KEYSTORE_PASSWORD") ?: ""
                    keyAlias = System.getenv("KEY_ALIAS") ?: ""
                    keyPassword = System.getenv("KEY_PASSWORD") ?: ""
                }
                getByName("debug") {
                    storeFile = file("keystore/debug.keystore")
                    storePassword = "android"
                    keyAlias = "androiddebugkey"
                    keyPassword = "android"
                }
            }
            
            buildTypes {
                getByName("debug") {
                    applicationIdSuffix = ".debug"
                    isDebuggable = true
                    isJniDebuggable = true
                    isMinifyEnabled = false
                    isShrinkResources = false
                    signingConfig = signingConfigs.getByName("debug")
                    proguardFiles(
                        getDefaultProguardFile("proguard-android-optimize.txt"),
                        "proguard-rules-debug.pro"  // ✅ DEBUG PROGUARD
                    )
                }
                getByName("release") {
                    isMinifyEnabled = true
                    isShrinkResources = true
                    isDebuggable = false
                    isJniDebuggable = false
                    signingConfig = signingConfigs.getByName("release")
                    proguardFiles(
                        getDefaultProguardFile("proguard-android-optimize.txt"),
                        "proguard-rules.pro"  // ✅ RELEASE PROGUARD
                    )
                }
            }
        }
    }
}
