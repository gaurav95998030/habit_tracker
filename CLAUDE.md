# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

Android habit-tracking app. Kotlin + Jetpack Compose (Material3), Hilt DI, Room (local persistence), DataStore (preferences), Firebase Firestore (remote — wired as dependencies, but `HabitRepositoryImpl` currently backs everything with the Room DAO only). Single-module Gradle build under `:app` with Kotlin DSL.

- `compileSdk=36`, `minSdk=24`, `targetSdk=36`, JVM target 11
- Package / namespace: `io.github.gaurav95998030.habittracker`
- Application class: `HabitTrackerApplication` (annotated `@HiltAndroidApp`)
- Entry point: `MainActivity` (`@AndroidEntryPoint`) → `Habit_TrackerTheme` → `MainScreen` (Scaffold + bottom nav + `AppNavHost`)

## Commands

Use the Gradle wrapper. Firebase is enabled via `app/google-services.json` (checked in).

**JDK:** Gradle/Kotlin 2.0.21 cannot run on JDK 25 (fails with a bare `25.0.4` error) — and Android Studio's bundled JBR on this machine is 25. Build with JDK 21: `export JAVA_HOME=~/Library/Java/JavaVirtualMachines/jbr-21.0.11/Contents/Home`.

The build has a **product-flavor dimension** (`dev`, `prod`) crossed with build types (`debug`, `release`), so variants are `devDebug`, `devRelease`, `prodDebug`, `prodRelease`. `dev` gets a `.dev` applicationId suffix; only `prod` matches the checked-in `google-services.json`. The umbrella `assembleDebug` builds every flavor's debug; prefer the flavor-specific task when installing.

```bash
./gradlew assembleDevDebug          # build the dev debug APK (use prod* to match Firebase)
./gradlew installDevDebug           # install dev/debug on attached device/emulator
./gradlew test                      # JVM unit tests (src/test)
./gradlew connectedAndroidTest      # instrumented tests (src/androidTest, needs device)
./gradlew lint                      # Android lint
./gradlew clean
```

Run a single unit test class or method (note the flavor in the task name):

```bash
./gradlew :app:testDevDebugUnitTest --tests "io.github.gaurav95998030.habittracker.util.DateRangeCalculatorTest"
./gradlew :app:testDevDebugUnitTest --tests "io.github.gaurav95998030.habittracker.domain.mapper.CompletionMapperTest.someMethod"
```

Release signing is read from `~/.gradle/gradle.properties` (`RELEASE_STORE_FILE`, `RELEASE_STORE_PASSWORD`, `RELEASE_KEY_ALIAS`, `RELEASE_KEY_PASSWORD`); if unset, release falls back to the debug key. Room schemas are exported to `app/schemas/`. Note: `local.properties` pins `sdk.dir` to a machine-specific path and is not portable — each developer must regenerate it.

## Architecture

Feature-first Clean Architecture. Everything lives under `io.github.gaurav95998030.habittracker.features.<feature>.{data,domain,presentation}` with Hilt binding layers in `di/`, cross-feature navigation in `navigation/`, shared UI atoms in `commons/ui/`, and theming in `ui/theme/`.

Features: **`habits`** (the core), **`insights`** (stats screen), **`reminders`** (alarm-driven notifications + Notification Center), **`settings`** (theme, profile name, master notification switch, quiet hours), **`onboarding`** (4-step first run), **`motivation`** (stories & reviews, comeback mode, miss logging, energy check-ins, habit stacks, starter plans), **`widget`** (RemoteViews home-screen widget + Compose "add the widget" nudge).

The UI implements the **HabitForge "Aurora"** design (source: a bundled React prototype, `HabitForge-All-Screens.html`). Screens: onboarding (welcome/name/categories/first habit), Home (loading/empty/list), habit detail + add/edit sheets, Insights, Settings, Notification Center, widget nudge sheet, and the reminder notification (Mark done / Snooze 1h). A second design file (`HabitForge-Motivation-Screens.html`) added the `motivation` feature screens.

### Navigation

- `MainActivity` gates on `SettingsViewModel.onboardingCompleted` (`null` = loading → blank; `false` → `OnboardingScreen`; `true` → `MainScreen`). Installs that already have habits are auto-marked onboarded.
- `MainScreen` owns the `NavController`, derives the selected tab from the current backstack entry, and renders a `Scaffold` with `AppBottomBar` + `AppNavHost`.
- `TopLevelDestination` (enum) is the single source of truth for the three tabs: `Home` (route `home`, labelled "Today" → `HabitListScreen`), `Insights` (→ `InsightsScreen`), `Settings` (→ `SettingsScreen`). Keep the bar and the NavHost in sync through this enum.
- The tab-switch transaction in `MainScreen.onTabSelected` uses the standard `popUpTo(start){saveState} + launchSingleTop + restoreState` recipe — those flags are load-bearing (per-tab state preservation, no backstack growth). Don't strip them.
- The bottom bar is hidden on non-tab routes (`NOTIFICATIONS_ROUTE`, `PLANS_ROUTE`, `MISSES_ROUTE`, `CHECKIN_ROUTE`, `stack/{stackId}`).
- `MainScreen` also hosts two overlays: the full-screen story viewer (`MotivationStory`, opened from the Home story row or Insights' year card; closing marks the story key seen) and the miss-reason sheet (`MissLogSheet`, opened by the 8 AM notification via `ACTION_OPEN_MISS_LOG` or from "Why you miss"). `MainScreen` also hosts the widget nudge (`WidgetPromoSheet`, once per install + from Settings).
- `HabitViewModel`, `SettingsViewModel` and `MotivationViewModel` are created by the Activity (`by viewModels()`) and **passed down** into `AppNavHost`, so `HabitListScreen` and `InsightsScreen` share one instance and deep-link events delivered to the Activity reach the same ViewModel the UI observes. (`navigation/NavGraph.kt` is a dead empty stub.)

### Layering (feature: `habits`)

- **domain/model** — plain data classes (`Habit`). No framework types. Includes `reminderEnabled: Boolean`, `reminderTime: String?` ("HH:mm"), `isActive` (false = paused: excluded from progress/insights/reminders) and `icon: String?` (Phosphor name, e.g. `"ph-drop"`; null → category icon via `HabitCatalog.iconFor`).
- **`HabitCatalog`** — the 8 design categories + icons, onboarding starters, quick reminder times, legacy-category → icon fallback. **`domain/HabitStats`** — streaks, heat levels, `createdOn` (createdDate is an ISO *instant*, not `yyyy-MM-dd`).
- **domain/repository** — abstract `HabitRepository` (contract).
- **domain/usecases** — one class per action: `AddHabitUseCase`, `GetHabitsUseCase`, `ToggleHabitCompletionUseCase`, `UpdateHabitUseCase`, `DeleteHabitUseCase`. ViewModels depend on use cases, not the repository directly.
- **data/local** — Room: `HabitDatabase` (version 3, `RoomDatabase.kt`; v3 added the nullable `icon` column), `HabitDao`, `HabitEntity`, `ListConverter` (in `TypeConvertor.kt` — stores `List<String>` as a comma-joined string, so date strings must not contain commas). `HabitMapper.kt` here holds `Habit.toEntity()` / `HabitEntity.toDomain()` extension functions.
- **data/remote/firestore** — `HabitRemoteDataSource` interface + impl, plus `HabitDto`. Wired in dependencies but not currently called from the repository implementation.
- **data/repository** — `HabitRepositoryImpl` implements `HabitRepository` against the Room DAO.
- **presentation** — MVVM with Hilt-injected `HabitViewModel` (`@HiltViewModel`). State is split between `HabitState` (StateFlow `uiState`, in `states/HabitListState.kt`) for the habit list and `HabitFormState` (Compose `mutableStateOf formState`) for the add/edit form. `HabitListScreen` + `habit_list/organisms/{AddHabitSheet,HabitDetailSheet}`; reusable pieces in `presentation/components/` (`HabitTile`, `HabitViz`: `WeeklyGrid`, `CheckCircle`, `YearHeatmap`, `MonthCalendar`, `StreakBadge`).

### Other feature layers

- **`insights`** — `InsightsScreen` reads the shared `HabitViewModel.uiState` and runs `InsightsCalculator` (pure: 8 rolling-week completion % counted only from each habit's creation date, all-time best weekday, best-streak leaderboard; active habits only). No repository/use cases of its own.
- **`reminders`** — `HabitReminderScheduler` (`@Singleton`) arms per-habit alarms via `AlarmManagerCompat.setAndAllowWhileIdle` (inexact, doze-friendly, no exact-alarm permission needed); `HabitReminderReceiver` (BroadcastReceiver) posts the notification; `BootReceiver` re-schedules after reboot/app-update; `ReminderConstants` holds channel + intent-extra + deep-link keys. Notification taps deep-link into `MainActivity.handleDeepLink` → `HabitViewModel.openHabitDetail(id)`, which stashes `pendingDetailHabitId` in state for the list screen to open the detail sheet. Notification actions: "Mark done" and "Snooze 1h" (`scheduleSnooze` — a separate one-shot alarm, `ACTION_FIRE_SNOOZED`). The receiver skips posting for paused habits and inside quiet hours (but still re-arms the daily alarm). `presentation/NotificationCenterBuilder` derives the last 9 days of reminders (Upcoming/Pending/Done/Missed) from the habit list — there is no history table; `pendingCount` drives the Home bell badge.
- **`settings`** — `SettingsRepository` over DataStore Preferences exposes `themePreference` (Light/Dark/System), `notificationsEnabled` (master reminder switch), `userName`, `onboardingCompleted`, `focusCategories` (float first in the add sheet), `widgetPromoSeen`, and `quietHours` (`QuietHours`, window may wrap midnight), with suspend setters. `SettingsViewModel` feeds the theme flow into `MainActivity`, which chooses `darkTheme` for `Habit_TrackerTheme`.

- **`motivation`** — `domain/ReviewStats` (pure: week/month/year reviews, daily-boost content, comeback detection, story keys), `domain/MotivationInsights` (missed-yesterday, miss patterns + suggestion, energy vs completion, stack progress), `domain/PlanCatalog`, `domain/Quotes`. Persistence: Room tables `miss_logs`, `energy_checkins`, `habit_stacks`, `stack_steps` (DB v4, `MIGRATION_3_4`) via `MotivationDao` → `MotivationRepository` → use cases in `domain/usecases/MotivationUseCases.kt`; DataStore holds seen story keys, saved quotes and active plans (`planId|startDate`). Stack steps / miss logs are **not** cascaded on habit delete (so undo works) — readers drop rows whose habit is gone. `HabitReminderScheduler.scheduleMissCheck()` (armed from `HabitTrackerApplication` and `BootReceiver`, re-armed on fire) posts "You missed X yesterday". Stories are always dark (`StoryPalette`) and share cards export via `ShareCard` + `rememberGraphicsLayer` → `shareBitmap` (FileProvider `${applicationId}.fileprovider`, `res/xml/file_paths.xml`).

### Dependency injection

Hilt modules in `di/`:

- `DatabaseModule` (`SingletonComponent`) — provides `HabitDatabase` (with `MIGRATION_1_2`, `MIGRATION_2_3`, `MIGRATION_3_4`), `HabitDao` and `MotivationDao`.
- `RepositoryModule` (`SingletonComponent`) — binds `HabitRepository` → `HabitRepositoryImpl` and `MotivationRepository` → `MotivationRepositoryImpl`.
- `SettingsModule` (`SingletonComponent`) — provides the `DataStore<Preferences>` and `SettingsRepository`.
- `UseCaseModule` (`ViewModelComponent`, `@ViewModelScoped`) — provides each habit use case. When adding a new use case, register it here, not at the injection site.

`HabitReminderScheduler` is constructor-injected as a `@Singleton` (no explicit module).

### Theming

Custom theme wrapper in `ui/theme/`. `Habit_TrackerTheme(darkTheme = …)` is driven by the persisted `ThemePreference` (the M3 `ColorScheme` is derived from the same tokens; no dynamic color). `MainActivity` sets status/nav-bar icon contrast from the in-app theme.

- `AppColors` — Aurora tokens incl. `isDark`, `surface2`, `glassFill`, `glassBorder`, `heatEmpty`, `scrim`, `fieldFill`. `AppTextStyles` — Clash Display (display/titles) + Space Grotesk (body/labels) in `res/font/`.
- `Tokens.kt` — `Accents` (8 habit hues + legible `ink`; `colorOf(hex)`/`inkFor` tolerate legacy colors), `AppShapes`, `AppMotion`, and `Modifier.glow(...)` (dark-mode colored glow). Put `glow` **outside** any `alpha()` — an alpha layer clips it.
- Icons are **Phosphor** glyphs from subset icon fonts (`res/font/phosphor_{regular,bold,fill}.ttf`), referenced via `PhIcons` constants / `PhIcons.byName("ph-x")` and drawn with `commons/ui/PhIcon`. The fonts contain only the glyphs listed in `PhIcons.kt`; adding an icon means re-subsetting the fonts from the Phosphor release and adding the constant.
- Shared primitives in `commons/ui/` (`Aurora.kt`: `HfButton`, `HfChip`, `HfField`, `HfToggle`, `HfSwatches`, `HfSegRadio`, `HfCard`, `HfShimmer`, …; `HfSheet.kt`, `HfTimePickerDialog.kt`, `Charts.kt`: `RingStat`, `WeekBars`; `Logo.kt`).

Access values via the `AppTheme` singleton:

```kotlin
style = AppTheme.textStyles.titleLarge
color = AppTheme.colors.textPrimary
```

These resolve from `LocalAppColors` / `LocalAppTextStyles` composition locals. Do not hardcode colors or text styles in composables — go through `AppTheme`.

### Add / edit habit flow

1. FAB in `HabitListScreen` opens `AddHabitSheet` (ModalBottomSheet); editing an existing habit (Edit in the detail sheet) calls `HabitViewModel.startEditingHabit(habit)` first to prefill `formState`. Blank form defaults: "Mind", Lime, reminder on at 07:30.
2. Form fields call `onNameChanged` / `onDescriptionChanged` / `onCategoryChanged` / `onColorSelected` / `onReminderEnabledChanged` / `onReminderTimeSelected` (quick chips) / `onReminderTimeChanged`, each mutating `formState`.
3. Submit calls `HabitViewModel.saveHabit { … }` — one method handles **both** add and edit (keyed on `formState.editingHabitId` + `editingHabitSnapshot`). It builds/updates the `Habit` (new ids via `HabitUtil.generateUniqueId()`), invokes `AddHabitUseCase`/`UpdateHabitUseCase`, then **cancels and re-arms the reminder alarm** (gated on both the habit's own `reminderEnabled` and the global `settingsRepository.notificationsEnabled`), resets the form, and re-fetches via `getHabits()`.

### Completion toggle

`HabitViewModel.updateHabit(habitId, date)` optimistically rewrites the in-memory `uiState.habits` list (toggling the `yyyy-MM-dd` date string in `completedDates`), then persists via `ToggleHabitCompletionUseCase` → `HabitRepository.updateHabitCompletion` → `HabitDao` (targeted `UPDATE ... SET completedDates = ...`, keyed on business `id`, not Room's `pId`). `deleteHabit` similarly removes optimistically then persists + cancels the alarm; `restoreHabit` re-adds (used for undo). `toggleToday(id)` / `toggleDay(id, "yyyy-MM-dd")` wrap it for the check circle and the detail calendar (future days rejected). `setHabitActive(habit, active)` pauses/resumes (cancels / re-arms the alarm).

## Conventions worth preserving

- Dates are stored as `yyyy-MM-dd` strings everywhere (domain, entity, UI); reminder times are `"HH:mm"` strings. Do not introduce `LocalDate`/`LocalTime` into persisted models.
- `HabitEntity` has both `pId` (auto-generated Room PK) and `id` (business UUID). Queries and mappers use `id`; `pId` is Room-internal.
- **Room migrations are real now** — bump the DB version and add a `Migration` (see `MIGRATION_1_2`, which added the reminder columns) plus register it in `DatabaseModule`. Do not fall back to destructive migration.
- `HabitUtil` centralises date helpers (`getDatesOfCurrentYear`, `getLast7Days`, etc.) and color ⇄ hex conversion, plus `generateUniqueId()`. Reuse these rather than re-deriving in composables.
- Use cases expose `suspend operator fun invoke(...)` — call as `useCase(arg)` or `useCase.invoke(arg)`.
- Screens/ViewModel code using `java.time.*` are annotated `@RequiresApi(Build.VERSION_CODES.O)` because `minSdk=24`. Maintain this annotation when introducing new date-based code, or use desugaring if that changes.
- The `habits` `HabitViewModel` is intentionally shared across the Home and Insights tabs — don't swap it for a per-screen `hiltViewModel()` without accounting for deep-link delivery and shared state.

## Notable design docs

`IMPLEMENTATION_SUMMARY.md` and `HEATMAP_IMPLEMENTATION.md` describe a planned `HabitCompletion` / `HeatmapRange` / `HeatmapViewModel` subsystem. Most of those files are **not yet present in `src/`** — only the unit tests (`DateRangeCalculatorTest`, `CompletionMapperTest`) exist, and **they don't compile**, which breaks `./gradlew test` as a whole. Treat those docs as a design target, not current state; check the tree before citing filenames from them. The redesign's logic is covered by `src/test/.../redesign/` (`RedesignLogicTest`, `ReviewStatsTest`, `MotivationInsightsTest`) — move the two broken tests aside to run them.
