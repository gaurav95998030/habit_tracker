# =============================================================================
# Habit Tracker — R8 / ProGuard rules
#
# Strategy: rely on consumer rules shipped by AndroidX / Hilt / Room / Firebase /
# Coroutines wherever possible, and add only the project-specific keeps + known
# defensive rules for libraries that use reflection.
# =============================================================================

# --- Stack traces ------------------------------------------------------------
# Keep line numbers so production stack traces remain readable; hide original
# source file names by collapsing them to "SourceFile".
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# --- Attributes required by reflection-using libraries ----------------------
# Signature: generic type info (Gson, Firestore, Retrofit-style reflection).
# *Annotation*: keep all annotations (Hilt, Room, Firestore, Compose).
# InnerClasses / EnclosingMethod: required for Kotlin reflection and nested classes.
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod,Exceptions

# --- Kotlin ------------------------------------------------------------------
# kotlin.Metadata is consulted by Kotlin reflection (and indirectly by Firestore
# when materialising Kotlin data classes).
-keep class kotlin.Metadata { *; }
-keepclassmembers class **$WhenMappings { <fields>; }
-keepclassmembers class kotlin.coroutines.SafeContinuation { volatile <fields>; }

# Keep companion objects (Kotlin generates them as static INSTANCE fields).
-keepclassmembers class * {
    public static ** Companion;
}

# Default-argument synthetic methods follow the "name$default" naming convention;
# R8 keeps them automatically when the original is kept, but be explicit:
-keepclassmembernames class * {
    *** *$default(...);
}

# --- Standard Android --------------------------------------------------------
# Activities, Services, BroadcastReceivers, ContentProviders, Application,
# native methods, View constructors, and Parcelable CREATOR fields are all
# covered by the default Android proguard file (proguard-android-optimize.txt)
# applied in build.gradle.kts. Re-listing for safety:
-keepclasseswithmembernames class * {
    native <methods>;
}
-keepclassmembers class * implements android.os.Parcelable {
    public static final ** CREATOR;
}
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# --- Hilt / Dagger -----------------------------------------------------------
# Hilt + Dagger ship comprehensive consumer rules. The pieces below are belt-
# and-braces for project-specific entry points + Kotlin's @Inject constructors.
-keepclasseswithmembers class * {
    @javax.inject.Inject <init>(...);
}
-keepclassmembers,allowobfuscation class * {
    @javax.inject.Inject <fields>;
    @javax.inject.Inject <methods>;
}
-keep @dagger.hilt.android.HiltAndroidApp class * { *; }
-keep @dagger.hilt.android.AndroidEntryPoint class * { *; }
-keep @dagger.hilt.android.lifecycle.HiltViewModel class * { *; }
-keep @dagger.Module class * { *; }
-keep,allowobfuscation @interface dagger.hilt.*
-keep,allowobfuscation @interface javax.inject.*

# Hilt-generated *_HiltModules, *_Factory, *_MembersInjector live alongside
# the originals and are referenced by reflection from the generated entry-point.
-keep class **_HiltModules { *; }
-keep class **_HiltModules$* { *; }
-keep class **_Factory { *; }
-keep class **_MembersInjector { *; }
-keep class **_Impl { *; }
-keep class hilt_aggregated_deps.** { *; }
-keep class dagger.hilt.internal.aggregatedroot.codegen.** { *; }
-keep class dagger.hilt.internal.processedrootsentinel.codegen.** { *; }

# --- Room --------------------------------------------------------------------
# Room ships consumer rules; entity field names must survive because @Query
# strings reference column names at runtime. TypeConverters are referenced by
# name from generated DAO impls.
-keep class io.github.gaurav95998030.habittracker.features.habits.data.local.HabitEntity { *; }
-keep class io.github.gaurav95998030.habittracker.features.habits.data.local.ListConverter { *; }
-keep class * extends androidx.room.RoomDatabase { *; }
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao class * { *; }
-keep @androidx.room.Database class * { *; }
-keep @androidx.room.TypeConverter class * { *; }
-keepclassmembers class * {
    @androidx.room.* *;
}
-dontwarn androidx.room.paging.**

# --- Firebase Firestore ------------------------------------------------------
# Firestore reflectively materialises POJOs/data classes via toObject() and
# serialises via set(). It needs the no-arg constructor, field names, and
# accessor methods of any class passed through those APIs.
-keep class io.github.gaurav95998030.habittracker.features.habits.domain.model.** { *; }
-keep class io.github.gaurav95998030.habittracker.features.habits.data.remote.model.** { *; }
-keepclassmembers class io.github.gaurav95998030.habittracker.features.habits.domain.model.** {
    <init>(...);
    <fields>;
}
-keepclassmembers class io.github.gaurav95998030.habittracker.features.habits.data.remote.model.** {
    <init>(...);
    <fields>;
}
-keepclassmembers class * {
    @com.google.firebase.firestore.PropertyName <methods>;
    @com.google.firebase.firestore.PropertyName <fields>;
    @com.google.firebase.firestore.DocumentId <methods>;
    @com.google.firebase.firestore.DocumentId <fields>;
    @com.google.firebase.firestore.Exclude <methods>;
    @com.google.firebase.firestore.Exclude <fields>;
    @com.google.firebase.firestore.ServerTimestamp <methods>;
    @com.google.firebase.firestore.ServerTimestamp <fields>;
}
# Firestore + gRPC pull in references to optional Java EE / Checker Framework
# classes that aren't on Android. Silence the warnings so R8 doesn't fail.
-dontwarn com.google.firebase.firestore.**
-dontwarn com.google.protobuf.**
-dontwarn io.grpc.**
-dontwarn io.opencensus.**
-dontwarn javax.naming.**
-dontwarn javax.lang.model.**
-dontwarn java.lang.management.**
-dontwarn org.checkerframework.**
-dontwarn afu.org.checkerframework.**
-dontwarn com.google.android.gms.**
-dontwarn com.google.errorprone.annotations.**

# --- Coroutines --------------------------------------------------------------
# kotlinx-coroutines-android ships consumer rules; keep the volatile internals
# as an extra safety net (older R8 versions stripped them too aggressively).
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.android.AndroidDispatcherFactory {}
-dontwarn kotlinx.coroutines.flow.**internal**

# --- Compose -----------------------------------------------------------------
# Compose ships consumer rules and works well with R8 out of the box.
# Nothing project-specific to add.

# --- Lifecycle / ViewModel ---------------------------------------------------
# androidx.lifecycle ships consumer rules. SavedStateHandle introspects
# constructor parameters; the Hilt-generated factory bridges that.
-keep class * extends androidx.lifecycle.ViewModel { <init>(...); }
-keep class * extends androidx.lifecycle.AndroidViewModel { <init>(...); }

# --- BuildConfig -------------------------------------------------------------
# buildConfigField values (API_BASE_URL, ENVIRONMENT) are read by name in some
# paths; keep the generated class so referenced constants don't get inlined
# inconsistently across modules.
-keep class io.github.gaurav95998030.habittracker.BuildConfig { *; }
