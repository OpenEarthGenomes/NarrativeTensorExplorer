# ==============================================
# DEBUG PROGUARD RULES FOR MEANING ARCHIVE
# ==============================================

# Debug build - NO OBFUSCATION, NO OPTIMIZATION
# Only keep minimal rules for compilation

# Don't obfuscate anything
-dontobfuscate
-dontoptimize
-dontshrink
-dontpreverify

# Keep all source file names and line numbers
-keepattributes SourceFile,LineNumberTable

# Keep all annotations
-keepattributes *Annotation*

# Keep all class and member names
-keepnames class ** { *; }

# Keep all enum values
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Keep Parcelable implementations
-keep class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}

# Keep Room components (for debugging)
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keepclassmembers class * {
    @androidx.room.* *;
}

# Keep JNI native methods
-keepclasseswithmembernames class * {
    native <methods>;
}

# Keep our app classes
-keep class com.meaning.app.** { *; }

# Keep Kotlin metadata
-keep class kotlin.Metadata { *; }

# Keep R classes
-keepclassmembers class **.R$* {
    public static <fields>;
}

# Keep debug logging (important for debug builds)
# DON'T remove logs in debug!

# Keep all inner classes for debugging
-keepclasseswithmembers class ** {
    class *;
}

# Keep lambda classes for debugging
-keep class **$$Lambda$* { *; }

# Keep Coroutine continuation classes
-keep class * extends kotlin.coroutines.jvm.internal.SuspendLambda { *; }

# Keep StackTrace elements readable
-keepattributes StackTrace

# Keep signature for reflection in debug
-keepattributes Signature

# Keep runtime visible annotations
-keepattributes RuntimeVisibleAnnotations
-keepattributes RuntimeInvisibleAnnotations

# Keep Kotlin type aliases
-keep class **$$$* { *; }

# Keep Room generated classes
-keep class * extends androidx.room.RoomDatabase { *; }
-keep class *$$Impl { *; }
-keep class *$$Impl$* { *; }

# Keep KSP generated code visible
-keep class *$$* { *; }

# Keep all interfaces for debugging
-keep interface ** { *; }

# Keep all inner classes
-keep class **$* { *; }

# For easier debugging - keep method parameter names
-keepattributes MethodParameters

# Keep Kotlin companion objects
-keepclassmembers class **$Companion { *; }

# Keep Kotlin default impls
-keepclassmembers class **$DefaultImpls { *; }

# Keep synthetic methods for debugging
-keepattributes SyntheticSignature

# Keep all exceptions for stack traces
-keep public class * extends java.lang.Exception

# Keep View binding
-keep class * extends android.view.View {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
    public void set*(***);
    *** get*();
}

# Don't warn about anything in debug
-dontwarn **

# Keep everything visible for debugging
-keepattributes Exceptions,InnerClasses,Signature,Deprecated,
                SourceFile,LineNumberTable,*Annotation*,EnclosingMethod

# Explicitly keep debug info
-keepattributes SourceFile,LineNumberTable

# For Room debugging
-keep class * extends androidx.room.RoomDatabase {
    public static ** getInstance(**);
}

# For LiveData debugging
-keep class * extends androidx.lifecycle.LiveData {
    public void observe(**);
}

# For Coroutine debugging
-keep class kotlinx.coroutines.debug.** { *; }

# Keep all inline functions expanded
-keep,allowobfuscation class * {
    @kotlin.jvm.JvmOverloads <methods>;
}

# Keep all extension functions
-keepclassmembers class ** {
    @kotlin.jvm.JvmStatic <methods>;
}

# Keep all suspending functions
-keepclassmembers class ** {
    suspend ** *(...);
}

# For database inspection
-keep class * extends androidx.room.Entity { *; }
-keep class * extends androidx.room.Dao { *; }

# Keep all composable functions
-keep @androidx.compose.runtime.Composable class ** { *; }

# Keep Preview functions
-keep @androidx.compose.ui.tooling.preview.Preview class ** { *; }

# Keep Test functions
-keep @org.junit.Test class ** { *; }
