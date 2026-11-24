plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.services)
    alias(libs.plugins.firebase.crashlytics)
    alias(libs.plugins.firebase.perf)
    alias(libs.plugins.google.dagger)
    alias(libs.plugins.google.ksp)
}

// Release signing pulled from ~/.gradle/gradle.properties (outside the repo).
// Set: RELEASE_STORE_FILE, RELEASE_STORE_PASSWORD, RELEASE_KEY_ALIAS, RELEASE_KEY_PASSWORD
val releaseStoreFile = (project.findProperty("RELEASE_STORE_FILE") as String?)?.let { file(it) }
val releaseStorePassword = project.findProperty("RELEASE_STORE_PASSWORD") as String?
val releaseKeyAlias = project.findProperty("RELEASE_KEY_ALIAS") as String?
val releaseKeyPassword = project.findProperty("RELEASE_KEY_PASSWORD") as String?

android {
    namespace = "io.github.gaurav95998030.habittracker"
    compileSdk = 36

    defaultConfig {
        applicationId = "io.github.gaurav95998030.habittracker"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        if (releaseStoreFile != null) {
            create("release") {
                storeFile = releaseStoreFile
                storePassword = releaseStorePassword
                keyAlias = releaseKeyAlias
                keyPassword = releaseKeyPassword
            }
        }
    }

    buildTypes {
        debug {
            // No applicationIdSuffix — debug & release of the same flavor share an
            // applicationId, so Firebase only needs one registration per flavor.
            // Trade-off: can't install debug + release of the same flavor side-by-side.
            versionNameSuffix = "-DEBUG"
            isMinifyEnabled = false
            isShrinkResources = false
            isDebuggable = true
            // Performance: skip build-time network instrumentation in debug (faster
            // builds). Automatic app-start / screen traces still work.
            configure<com.google.firebase.perf.plugin.FirebasePerfExtension> {
                setInstrumentationEnabled(false)
            }
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            isDebuggable = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.getByName("debug")
            if (releaseStoreFile != null) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }

    // Product flavors layer an "environment" dimension on top of debug/release.
    // Final variants: devDebug, devRelease, prodDebug, prodRelease.
    // Firebase project `habittracker-ad5d6`: app/google-services.json registers both
    // applicationIds (prod `io.github.gaurav95998030.habittracker` and dev `….dev`),
    // so both flavors connect. A new flavor/suffix must be registered there too.
    flavorDimensions += "environment"
    productFlavors {
        create("dev") {
            dimension = "environment"
            applicationIdSuffix = ".dev"
            versionNameSuffix = "-dev"
            resValue("string", "app_name", "Habit Tracker (Dev)")
            buildConfigField("String", "API_BASE_URL", "\"https://dev.api.habittracker.example/\"")
            buildConfigField("String", "ENVIRONMENT", "\"dev\"")
        }
        create("prod") {
            dimension = "environment"
            // No suffix — keeps applicationId == io.github.gaurav95998030.habittracker
            resValue("string", "app_name", "Habit Tracker")
            buildConfigField("String", "API_BASE_URL", "\"https://api.habittracker.example/\"")
            buildConfigField("String", "ENVIRONMENT", "\"prod\"")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
        freeCompilerArgs += listOf(
            "-opt-in=androidx.compose.foundation.layout.ExperimentalLayoutApi"
        )
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)

    // Navigation
    implementation(libs.androidx.navigation.compose)

    // Lifecycle
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)

    // Firebase
    implementation(platform("com.google.firebase:firebase-bom:33.7.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-firestore-ktx")
    implementation("com.google.firebase:firebase-crashlytics")
    implementation("com.google.firebase:firebase-perf")

    // Coroutines for Firebase
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.8.0")

    // Hilt
    implementation("com.google.dagger:hilt-android:2.57.1")
    ksp("com.google.dagger:hilt-compiler:2.57.1")
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")

    // DataStore (user preferences: theme, master reminder switch)
    implementation(libs.androidx.datastore.preferences)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    implementation ("androidx.room:room-runtime:2.6.1")
    implementation ("androidx.room:room-ktx:2.6.1")
    ksp ("androidx.room:room-compiler:2.6.1")

}
