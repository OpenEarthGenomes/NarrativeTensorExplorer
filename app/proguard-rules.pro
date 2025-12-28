# ==============================================
# RELEASE PROGUARD RULES FOR MEANING ARCHIVE
# ==============================================

# Room Database
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keepclassmembers class * {
    @androidx.room.* *;
}
-keep class * extends androidx.room.Entity { *; }
-keep class * extends androidx.room.Dao { *; }

# JNI/Native methods
-keepclasseswithmembernames class * {
    native <methods>;
}
-keep class com.meaning.app.kernel.QuantizationEngine {
    public static *;
}

# NEON functions (keep all native method names)
-keepclasseswithmembernames,includedescriptorclasses class * {
    native <methods>;
}

# GSON reflection
-keep class com.google.gson.** { *; }
-keep class sun.misc.Unsafe { *; }
-keep class com.google.gson.stream.** { *; }
-keep class com.google.gson.reflect.** { *; }

# Keep generic signatures
-keepattributes Signature
-keepattributes *Annotation*
-keepattributes RuntimeVisibleAnnotations
-keepattributes RuntimeInvisibleAnnotations
-keepattributes RuntimeVisibleParameterAnnotations
-keepattributes RuntimeInvisibleParameterAnnotations
-keepattributes EnclosingMethod

# Kotlin specific
-keep class kotlin.** { *; }
-keep class kotlinx.coroutines.** { *; }
-keep class kotlinx.serialization.** { *; }
-keep class kotlin.Metadata { *; }
-keepclassmembers class **$WhenMappings {
    <fields>;
}
-keepclassmembers class kotlin.Metadata {
    public <methods>;
}

# Compose runtime
-keep class androidx.compose.runtime.** { *; }
-dontwarn androidx.compose.**

# Material3
-keep class androidx.compose.material3.** { *; }
-dontwarn androidx.compose.material3.**

# Our app packages - KEEP EVERYTHING
-keep class com.meaning.app.** { *; }
-keep class com.meaning.app.db.** { *; }
-keep class com.meaning.app.kernel.** { *; }
-keep class com.meaning.app.ui.** { *; }

# Keep data classes
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# Keep enum values
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Keep Parcelable implementations
-keep class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}

# Keep R classes
-keepclassmembers class **.R$* {
    public static <fields>;
}

# Keep layout inflater constructors
-keepclassmembers class * extends android.view.View {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

# Keep databinding
-keep class androidx.databinding.** { *; }
-keep class * extends androidx.databinding.DataBinderMapper {
    public <methods>;
}
-keep class * extends androidx.databinding.DataBinderMapper {
    public <methods>;
}

# Keep LiveData observers
-keep class * extends androidx.lifecycle.LiveData {
    public void setValue(java.lang.Object);
    public void postValue(java.lang.Object);
}

# Keep ViewModel
-keep class * extends androidx.lifecycle.ViewModel {
    public <init>(...);
}

# Keep Coroutines
-keep class kotlinx.coroutines.android.** { *; }
-keep class kotlinx.coroutines.CoroutineExceptionHandler { *; }

# Keep Flow operators
-keep,includedescriptorclasses class kotlinx.coroutines.flow.Flow { *; }
-keep,includedescriptorclasses class kotlinx.coroutines.flow.FlowCollector { *; }

# Keep 3D graphics
-keep class android.opengl.** { *; }
-keep class javax.microedition.khronos.** { *; }

# NDK support
-keep class android.renderscript.** { *; }

# Database migration
-keep class com.meaning.app.db.NarrativeDatabase$* { *; }

# Remove debug logs in release
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
    public static *** w(...);
}

# Optimizations
-optimizations !code/simplification/arithmetic,!code/simplification/cast,!field/*,!class/merging/*
-optimizationpasses 5
-allowaccessmodification
-mergeinterfacesaggressively

# Exceptions
-keep public class * extends java.lang.Exception

# Resource fields
-keepclassmembers class **.R$* {
    public static <fields>;
}

# For native methods, see http://proguard.sourceforge.net/manual/examples.html#native
-keepclasseswithmembernames class * {
    native <methods>;
}

# Keep setters in Views so that animations can still work.
-keepclassmembers public class * extends android.view.View {
    void set*(***);
    *** get*();
}

# Keep classes that are entry points
-keep class com.meaning.app.MainActivity { *; }
-keep class com.meaning.app.MainApplication { *; }

# Keep Room database schemas
-keep class * extends androidx.room.RoomDatabase {
    public static <methods>;
}

# Keep KSP generated code
-keep class *$$* { *; }
