package io.github.gaurav95998030.habittracker.features.settings.domain.model

/**
 * Privacy policy text shown in-app (Settings → Privacy policy). The public copy for the
 * Play Console lives at docs/privacy-policy.html — keep the two in sync.
 */
data class PolicySection(val title: String, val paragraphs: List<String>, val bullets: List<String>)

object PrivacyPolicy {
    const val EFFECTIVE_DATE = "November 24, 2026"
    const val CONTACT_EMAIL = "glowstreak0802@gmail.com"

    val sections: List<PolicySection> = listOf(
        PolicySection(
            "Overview",
            listOf("Glowstreak (“the app”, “we”) is a habit tracker. This policy explains what information the app handles, why, and the choices you have. Glowstreak has no accounts, shows no ads, and we never sell your data."),
            listOf(),
        ),
        PolicySection(
            "Information stored only on your device",
            listOf("Everything you enter stays on your phone, in the app’s private storage. We do not upload it to our servers, and we cannot see it:"),
            listOf("Your habits — names, descriptions, categories, colours, icons and reminder times", "Your completion history (the days you checked a habit off)", "The name you choose to be greeted with, and the focus areas you picked", "Reasons you log for missed days, and your daily energy check-ins", "Habit stacks, starter plans you started, saved quotes and app settings"),
        ),
        PolicySection(
            "Device backups",
            listOf("Android may include the app’s data in your device backup to your Google account (Auto Backup), so it can be restored on a new phone. This is controlled by your device’s backup settings and handled by Google; we have no access to it."),
            listOf(),
        ),
        PolicySection(
            "Information collected automatically",
            listOf("To keep Glowstreak stable and fast, it uses Google Firebase services. They collect the information below only while “Share usage & crash data” is turned on (Settings → Privacy). It is on by default and you can turn it off at any time. This data never includes your habit names, notes, check-ins or the name you entered."),
            listOf("Firebase Analytics — how the app is used (for example app opens, screens viewed, session length, app updates), device model, operating-system version, language, app version, an app-instance identifier, how the app was installed (Google Play install referrer), and an approximate country or region derived from your IP address. The Advertising ID is not collected.", "Firebase Crashlytics — crash and error reports: the technical stack trace, device model, operating-system version, app version, memory and disk state at the time of the crash, an installation identifier, and short technical log lines from just before the problem.", "Firebase Performance Monitoring — how long the app takes to start and load data, screen-rendering speed and network timing, with device model, operating-system version, app version and an approximate country derived from your IP address."),
        ),
        PolicySection(
            "How we use this information",
            listOf("We use automatically collected information only to find and fix crashes, improve performance, and understand which features are used so we can improve the app. We do not use it for advertising or profiling, and we do not sell it or share it with anyone except Google, which processes it on our behalf as our service provider."),
            listOf("Google’s privacy policy: https://policies.google.com/privacy", "How Firebase handles data: https://firebase.google.com/support/privacy"),
        ),
        PolicySection(
            "What we do not collect",
            listOf("The app does not ask for or collect your email, phone number, contacts, precise location, photos, files, payment details or Advertising ID."),
            listOf(),
        ),
        PolicySection(
            "Permissions",
            listOf("The app uses these permissions:"),
            listOf("Notifications — to show the habit reminders you set and the optional morning “missed yesterday” check-in", "Run at startup — to restore your reminders after the phone restarts", "Internet, network state and wake lock — so Firebase can reliably send crash, performance and usage data (when enabled)"),
        ),
        PolicySection(
            "Lock-screen card (optional)",
            listOf("If you turn on “Today on lock screen”, the app shows a quiet notification with today’s habit names and progress on your lock screen, so anyone who can see your screen can see them. It is off unless you choose to turn it on, and you can turn it off in Settings → Privacy at any time. This information stays on your device."),
            listOf(),
        ),
        PolicySection(
            "Sharing",
            listOf("When you share a review card or a quote, the app creates an image or text and hands it to the Android share sheet. You choose where it goes; we do not receive a copy."),
            listOf(),
        ),
        PolicySection(
            "Your choices and deleting your data",
            listOf(),
            listOf("Turn off “Share usage & crash data” in Settings → Privacy to stop Analytics, Crashlytics and Performance collection. Turning it off also resets the analytics identifier on your device.", "Delete any habit in the app at any time; you can also clear the app’s storage from Android Settings → Apps → Glowstreak → Storage.", "Uninstalling the app permanently removes all data stored on your device.", "Because the app has no accounts, we cannot link automatically collected data to you by name. To request deletion of diagnostic data associated with your device, email glowstreak0802@gmail.com and we will delete what we can identify."),
        ),
        PolicySection(
            "How long data is kept",
            listOf("Data on your device is kept until you delete it or uninstall the app. Firebase keeps analytics data for up to 14 months and crash reports for about 90 days, after which it is deleted or kept only in aggregated form."),
            listOf(),
        ),
        PolicySection(
            "Security",
            listOf("Data sent to Firebase is encrypted in transit (HTTPS). Data on your device is protected by Android’s app sandbox, which prevents other apps from reading it."),
            listOf(),
        ),
        PolicySection(
            "Children",
            listOf("Glowstreak is not directed at children under 13, and we do not knowingly collect personal information from children."),
            listOf(),
        ),
        PolicySection(
            "Changes to this policy",
            listOf("If we change this policy we will update the effective date below and, for significant changes, tell you in the app."),
            listOf(),
        ),
        PolicySection(
            "Contact",
            listOf("Questions or requests about privacy: glowstreak0802@gmail.com"),
            listOf(),
        ),
    )
}
