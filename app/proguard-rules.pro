# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Preserve line number information for debugging stack traces
-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# ===== Kotlin Serialization =====
-keepattributes Signature
-keepattributes *Annotation*
-keepattributes RuntimeVisibleAnnotations
-keepattributes AnnotationDefault
-keepattributes EnclosingMethod
-keepattributes InnerClasses

# Keep kotlinx.serialization classes
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Keep serializers and companion objects
-keep,includedescriptorclasses class com.trueedu.spac.**$$serializer { *; }
-keepclassmembers class com.trueedu.spac.** {
    *** Companion;
}
-keepclasseswithmembers class com.trueedu.spac.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Keep @Serializable classes
-keep @kotlinx.serialization.Serializable class com.trueedu.spac.** {
    *;
}

# ===== Firebase Realtime Database =====

# Keep Firebase Database models - must preserve all fields, methods, and constructors
-keep class com.trueedu.spac.dart.model.** {
    *;
}

-keep class com.trueedu.spac.api.model.dto.firebase.** {
    *;
}

# StockInfo classes need special attention for Kotlin properties and companion objects
-keepclassmembers class com.trueedu.spac.api.model.dto.firebase.StockInfo** {
    *;
}

# Keep companion objects in Firebase models
-keepclassmembers class com.trueedu.spac.api.model.dto.firebase.** {
    public static ** Companion;
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Keep DAO classes
-keep class com.trueedu.spac.api.model.dao.** {
    *;
}

# Keep local database models
-keep class com.trueedu.spac.db.** {
    *;
}

# Keep GenericTypeIndicator and its usage
-keepclassmembers class * extends com.google.firebase.database.GenericTypeIndicator {
    *;
}

# Keep all anonymous classes that extend GenericTypeIndicator
-keep class * extends com.google.firebase.database.GenericTypeIndicator {
    *;
}

# Preserve generic signatures for collections used with Firebase
-keepattributes Signature
-keep class * implements java.util.List { *; }
-keep class * implements java.util.Map { *; }
-keep interface java.util.List { *; }
-keep interface java.util.Map { *; }

# Keep all Firebase related classes
-keep class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**

# Keep Firebase database deserialization
-keepclassmembers class * {
    @com.google.firebase.database.PropertyName <methods>;
    @com.google.firebase.database.Exclude <methods>;
}

# ===== Kotlin Metadata =====
# Preserve Kotlin metadata for proper reflection and serialization
-keep class kotlin.Metadata { *; }
-keep class kotlin.reflect.** { *; }
-dontwarn kotlin.reflect.**

# ===== Retrofit & OkHttp =====
# Keep generic signature of Call, Response (R8 full mode strips signatures from non-kept items)
-keep,allowobfuscation,allowshrinking interface retrofit2.Call
-keep,allowobfuscation,allowshrinking class retrofit2.Response
-keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation

# ===== Room Database =====
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# ===== Hilt & Dagger =====
-keepnames @dagger.hilt.android.lifecycle.HiltViewModel class * extends androidx.lifecycle.ViewModel

# Keep Dagger assisted injection
-keep class dagger.assisted.** { *; }
-keep @dagger.assisted.AssistedInject class * {
    public <init>(...);
}
-keep @dagger.assisted.AssistedFactory interface * {
    *;
}

# Keep @Singleton classes - important for Managers
-keep @javax.inject.Singleton class * {
    *;
}

# Keep Hilt injected classes
-keep @javax.inject.Inject class * {
    public <init>(...);
}

# ===== Compose =====
# Keep CompositionLocal
-keep class androidx.compose.runtime.CompositionLocal { *; }
-keep class androidx.compose.runtime.ProvidableCompositionLocal { *; }
-keepclassmembers class * {
    androidx.compose.runtime.CompositionLocal *;
}

# Keep Compose state
-keep class androidx.compose.runtime.State { *; }
-keep class androidx.compose.runtime.MutableState { *; }
-keepclassmembers class * {
    androidx.compose.runtime.State *;
    androidx.compose.runtime.MutableState *;
}

# ===== Additional Kotlin rules =====
# Keep Kotlin data class copy() and componentN() methods
-keepclassmembers class * {
    *** copy(...);
    *** component1();
    *** component2();
    *** component3();
    *** component4();
    *** component5();
    *** component6();
    *** component7();
    *** component8();
    *** component9();
}

# Prevent issues with Kotlin default parameters
-keepclassmembers class * {
    public <init>(...);
}

# ===== Coroutines & Flow =====
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# Keep Flow and StateFlow
-keep class kotlinx.coroutines.flow.** { *; }
-keepclassmembers class kotlinx.coroutines.flow.StateFlow {
    *;
}
-keepclassmembers class kotlinx.coroutines.flow.MutableStateFlow {
    *;
}
-keepclassmembers class kotlinx.coroutines.flow.SharedFlow {
    *;
}
-keepclassmembers class kotlinx.coroutines.flow.MutableSharedFlow {
    *;
}

# ===== WorkManager =====
-keep class * extends androidx.work.Worker
-keep class * extends androidx.work.CoroutineWorker
-keep class * extends androidx.work.ListenableWorker {
    public <init>(...);
}
-keep class androidx.work.** { *; }
-keepclassmembers class * extends androidx.work.Worker {
    public <init>(android.content.Context, androidx.work.WorkerParameters);
}
-keepclassmembers class * extends androidx.work.CoroutineWorker {
    public <init>(android.content.Context, androidx.work.WorkerParameters);
}

# Keep Hilt-assisted workers
-keep @androidx.hilt.work.HiltWorker class * extends androidx.work.ListenableWorker

# ===== Amplitude Analytics =====
-keep class com.amplitude.** { *; }
-dontwarn com.amplitude.**
-keepclassmembers class * {
    @com.amplitude.api.Identify *;
}

# ===== Timber =====
# No special rules needed - Timber is ProGuard-friendly

# ===== Coil Image Loading =====
-keep class coil.** { *; }
-keep interface coil.** { *; }
-dontwarn coil.**

# ===== Google Credentials & Sign In =====
-keep class com.google.android.gms.auth.** { *; }
-keep class com.google.android.libraries.identity.googleid.** { *; }
-dontwarn com.google.android.gms.auth.**

# Keep Google Play Services
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.android.gms.**

# ===== Firebase Messaging =====
-keep class com.google.firebase.messaging.** { *; }
-keep class com.google.firebase.iid.** { *; }
-dontwarn com.google.firebase.messaging.**

# ===== OkHttp & Chucker =====
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }

# Chucker (network debugging)
-keep class com.chuckerteam.chucker.** { *; }
-dontwarn com.chuckerteam.chucker.**

# ===== JSON & org.json =====
# Keep JSONObject for Amplitude
-keep class org.json.** { *; }
-keepclassmembers class org.json.** { *; }

# ===== Prevent obfuscation issues =====
# If debugging shows W4.c or W4.h, you can temporarily disable obfuscation to verify
# -dontobfuscate