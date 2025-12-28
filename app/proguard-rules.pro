# ==============================================
# RELEASE PROGUARD RULES - MEANING TENSOR EXPLORER
# ==============================================

# JNI & Native - KRITIKUS: Megakadályozza a NEON metódusok átnevezését
-keepclasseswithmembernames class * {
    native <methods>;
}

-keep class com.meaning.app.kernel.QuantizationEngine {
    native <methods>;
    public *;
}

# Room Database
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keepclassmembers class * {
    @androidx.room.* *;
}

# Kotlin Coroutines & Serialization
-keep class kotlinx.coroutines.** { *; }
-keepattributes Signature, *Annotation*, EnclosingMethod

# Compose Runtime
-keep class androidx.compose.runtime.** { *; }
-dontwarn androidx.compose.**

# KSP Generated Code
-keep class *$$* { *; }

# Remove Logs in Release (Teljesítmény optimalizálás)
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
    public static *** w(...);
}

# Egyéb fontos Android alapok
-keep class com.meaning.app.MainActivity { *; }
-keep class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}
