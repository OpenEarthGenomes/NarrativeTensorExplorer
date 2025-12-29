plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android") // Verziószám nélkül, hogy a rendszerét használja
    id("org.jetbrains.kotlin.plugin.compose")
    // Frissítve Kotlin 2.0-hoz:
    id("com.google.devtools.ksp") version "2.0.0-1.0.21" 
}

android {
    // ... a többi rész változatlan ...
    namespace = "com.meaning.app"
    compileSdk = 34
    // ...
}
