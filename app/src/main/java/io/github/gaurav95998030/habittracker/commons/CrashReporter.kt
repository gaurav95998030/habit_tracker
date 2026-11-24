package io.github.gaurav95998030.habittracker.commons

import android.util.Log
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.perf.FirebasePerformance

/**
 * Thin wrapper over Firebase Crashlytics + Performance so call sites stay one-liners
 * and the SDKs are swappable/mocked in one place.
 *
 * - [record]: log to Logcat *and* send a non-fatal to Crashlytics (shows under
 *   "Non-fatals" with the stack trace and the breadcrumbs from [breadcrumb]).
 * - [trace]: time a block as a custom Performance trace.
 */
object CrashReporter {

    private val crashlytics: FirebaseCrashlytics? get() = runCatching { FirebaseCrashlytics.getInstance() }.getOrNull()

    fun record(tag: String, message: String, error: Throwable) {
        Log.e(tag, message, error)
        crashlytics?.apply {
            log("$tag: $message")
            recordException(error)
        }
    }

    /** Breadcrumb attached to the next crash / non-fatal report (not sent on its own). */
    fun breadcrumb(message: String) {
        crashlytics?.log(message)
    }

    fun setKey(key: String, value: String) {
        crashlytics?.setCustomKey(key, value)
    }

    /** Times [block] as Performance trace [name] (letters, digits, underscores; ≤100 chars). */
    suspend fun <T> trace(name: String, block: suspend () -> T): T {
        val t = runCatching { FirebasePerformance.getInstance().newTrace(name) }.getOrNull()
        t?.start()
        try {
            return block()
        } finally {
            t?.stop()
        }
    }
}
