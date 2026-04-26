# Keep line numbers in stack traces (and rename source file to "SourceFile" to hide it).
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Kotlin metadata — required by reflection-using libraries (Firestore, etc).
-keep class kotlin.Metadata { *; }
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod

# --- Hilt / Dagger ---
# Hilt and Dagger ship their own consumer ProGuard rules; nothing extra needed for typical use.
# Keep @Inject constructors so generated code can find them after R8.
-keepclasseswithmembers class * {
    @javax.inject.Inject <init>(...);
}

# --- Room ---
# Room ships consumer rules for runtime reflection on @Database/@Dao.
# Keep entities so generated DAO implementations + @Query column refs survive minification.
-keep class io.github.gaurav95998030.habittracker.features.habits.data.local.HabitEntity { *; }

# --- Firebase Firestore ---
# Firestore uses reflection (toObject / set) against POJOs/data classes — keep field names.
-keep class io.github.gaurav95998030.habittracker.features.habits.domain.model.** { *; }
-keep class io.github.gaurav95998030.habittracker.features.habits.data.remote.model.** { *; }
-keepclassmembers class * {
    @com.google.firebase.firestore.PropertyName <methods>;
    @com.google.firebase.firestore.PropertyName <fields>;
}

# --- Coroutines ---
# Already handled by kotlinx-coroutines consumer rules, but keep the volatile field just in case.
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}
