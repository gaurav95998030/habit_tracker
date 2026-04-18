# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

Android habit-tracking app. Kotlin + Jetpack Compose (Material3), Hilt DI, Room (local persistence), Firebase Firestore (remote — wired as dependencies, but `HabitRepositoryImpl` currently backs everything with the Room DAO only). Single-module Gradle build under `:app` with Kotlin DSL.

- `compileSdk=36`, `minSdk=24`, `targetSdk=36`, JVM target 11
- Package: `com.example.habit_tracker`
- Application class: `HabitTrackerApplication` (annotated `@HiltAndroidApp`)
- Entry point: `MainActivity` (`@AndroidEntryPoint`) → `HabitListScreen`

## Commands

Use the Gradle wrapper. Firebase is enabled via `app/google-services.json` (checked in).

```bash
./gradlew assembleDebug           # build debug APK
./gradlew installDebug            # install on attached device/emulator
./gradlew test                    # JVM unit tests (src/test)
./gradlew connectedAndroidTest    # instrumented tests (src/androidTest, needs device)
./gradlew lint                    # Android lint
./gradlew clean
```

Run a single unit test class or method:

```bash
./gradlew :app:testDebugUnitTest --tests "com.example.habit_tracker.util.DateRangeCalculatorTest"
./gradlew :app:testDebugUnitTest --tests "com.example.habit_tracker.domain.mapper.CompletionMapperTest.someMethod"
```

Note: `local.properties` pins `sdk.dir` to a machine-specific path and is not portable — each developer must regenerate it.

## Architecture

Feature-first Clean Architecture. Everything lives under `com.example.habit_tracker.features.<feature>.{data,domain,presentation}` with Hilt binding layers in `com.example.habit_tracker.di`.

### Layering (single feature: `habits`)

- **domain/model** — plain data classes (`Habit`). No framework types.
- **domain/repository** — abstract `HabitRepository` (contract).
- **domain/usecases** — one class per action (`AddHabitUseCase`, `GetHabitsUseCase`, `ToggleHabitCompletionUseCase`). ViewModels depend on use cases, not the repository directly.
- **data/local** — Room: `HabitDatabase` (version 2, `fallbackToDestructiveMigration`), `HabitDao`, `HabitEntity`, `ListConverter` (stores `List<String>` as comma-joined string — so date strings must not contain commas). `HabitMapper.kt` here holds `Habit.toEntity()` / `HabitEntity.toDomain()` extension functions.
- **data/remote/firestore** — `HabitRemoteDataSource` interface + `HabitRemoteDataSourceImpl`. Wired in dependencies but not currently called from the repository implementation.
- **data/repository** — `HabitRepositoryImpl` implements `HabitRepository` against the Room DAO.
- **presentation** — MVVM with Hilt-injected `HabitViewModel` (`@HiltViewModel`). State is split between `HabitState` (StateFlow) for the habit list and `HabitFormState` (Compose `mutableStateOf`) for the add-habit form. Compose tree follows **atoms/molecules/organisms** under `presentation/habit_list/`. `MainActivity` obtains the ViewModel via `hiltViewModel()`.

### Dependency injection

Three Hilt modules in `di/`:

- `DatabaseModule` (`SingletonComponent`) — provides `HabitDatabase` and `HabitDao`.
- `RepositoryModule` (`SingletonComponent`) — binds `HabitRepository` → `HabitRepositoryImpl`.
- `UseCaseModule` (`ViewModelComponent`, `@ViewModelScoped`) — provides each use case. When adding a new use case, register it here, not at the injection site.

### Theming

Custom theme wrapper in `ui/theme/`. Access values via the `AppTheme` singleton:

```kotlin
style = AppTheme.textStyles.titleLarge
color = AppTheme.colors.textPrimary
```

These resolve from `LocalAppColors` / `LocalAppTextStyles` composition locals provided by `Habit_TrackerTheme`. Do not hardcode colors or text styles in composables — go through `AppTheme`.

### Add-habit flow (end-to-end example)

1. FAB in `HabitListScreen` opens `AddHabitSheet` (ModalBottomSheet).
2. Form fields call `HabitViewModel.onNameChanged` / `onDescriptionChanged` / `onCategoryChanged` / `onColorSelected`, each mutating `formState: HabitFormState`.
3. Submit calls `HabitViewModel.addHabit { ... }` which builds a `Habit` (id via `HabitUtil.generateUniqueId()` — UUID), invokes `AddHabitUseCase`, resets `formState`, re-fetches via `getHabits()`, then runs the `onHabitAdded` callback.

### Completion toggle

`HabitViewModel.updateHabit(habitId, date)` optimistically rewrites the in-memory `HabitState.habits` list (toggling the yyyy-MM-dd date string in `completedDates`), then persists via `ToggleHabitCompletionUseCase` → `HabitRepository.updateHabitCompletion` → `HabitDao.updateHabitCompletion` (targeted `UPDATE ... SET completedDates = ...` query, keyed on business `id`, not Room's `pId`).

## Conventions worth preserving

- Dates are stored as `yyyy-MM-dd` strings everywhere (domain, entity, UI). Do not introduce `LocalDate` into persisted models.
- `HabitEntity` has both `pId` (auto-generated Room PK) and `id` (business UUID). Queries and mappers use `id`; `pId` is Room-internal.
- `HabitUtil` centralises date helpers (`getDatesOfCurrentYear`, `getLast7Days`, etc.) and color ⇄ hex conversion. Reuse these rather than re-deriving in composables.
- Use cases expose `suspend operator fun invoke(...)` — call as `useCase(arg)` or `useCase.invoke(arg)`.
- Screens that use `java.time.*` APIs are annotated `@RequiresApi(Build.VERSION_CODES.O)` because `minSdk=24`. Maintain this annotation when introducing new date-based UI code, or use desugaring if that changes.

## Notable design docs

The heatmap work has two companion documents (`IMPLEMENTATION_SUMMARY.md`, `HEATMAP_IMPLEMENTATION.md`) describing a planned `HabitCompletion` / `HeatmapRange` / `HeatmapViewModel` subsystem. Most of those files are **not yet present in `src/`** — only the unit tests (`DateRangeCalculatorTest`, `CompletionMapperTest`) exist. Treat those docs as a design target, not current state; check the tree before citing filenames from them.
