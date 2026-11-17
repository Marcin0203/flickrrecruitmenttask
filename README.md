# Flickr Recruitment Task

![Kotlin](https://img.shields.io/badge/Kotlin-2.2.10-7F52FF?logo=kotlin&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-BOM%202026.02.01-4285F4?logo=jetpackcompose&logoColor=white)
![Material 3](https://img.shields.io/badge/Material-3-757575?logo=materialdesign&logoColor=white)
![Min SDK](https://img.shields.io/badge/minSdk-24-3DDC84?logo=android&logoColor=white)
![Target SDK](https://img.shields.io/badge/targetSdk-36-3DDC84?logo=android&logoColor=white)

An Android app for searching public Flickr photos by tags. It is built with **Jetpack Compose** as a recruitment task for the second interview round.

> **Status:** 🚧 Early development. The project is a fresh Android Studio Compose template, and the features below are being implemented step by step.

---

## Table of contents

- [About the task](#about-the-task)
- [Flickr API](#flickr-api)
- [Tech stack](#tech-stack)
- [Architecture](#architecture)
- [Project structure](#project-structure)
- [Getting started](#getting-started)
- [Testing](#testing)
- [Decisions log](#decisions-log)
- [AI assistance](#ai-assistance)
- [Author](#author)

---

## About the task

Build a simple Android application that uses Flickr's publicly accessible [photos feed](https://www.flickr.com/services/feeds/docs/photos_public/). No API key is needed. The design is up to the developer.

### Requirements

| # | Requirement | Status |
|---|-------------|:------:|
| 1 | The user can enter one or more **tags** to search for | 🚧 (input UI) |
| 2 | Matching images are shown in a **list or grid** | ⬜ |
| 3 | Each list item shows the **title** of its image | ⬜ |
| 4 | Tapping an image opens it in a **full-screen view** | ⬜ |
| 5 | The UI is built with **Jetpack Compose** | ✅ (project setup) |

Legend: ✅ done · 🚧 in progress · ⬜ not started

---

## Flickr API

**Endpoint:** public photos feed. It needs no authentication and no API key.

```
GET https://api.flickr.com/services/feeds/photos_public.gne?format=json&nojsoncallback=1
```

### Query parameters

| Parameter | Example | Description |
|-----------|---------|-------------|
| `format` | `json` | Response format |
| `nojsoncallback` | `1` | Returns raw JSON instead of a JSONP wrapper (`jsonFlickrFeed(...)`) |
| `tags` | `cat,dog` | Comma-separated list of tags to filter by |
| `tagmode` | `all` / `any` | Whether photos must match **all** tags (default) or **any** of them |

**Example:**

```
https://api.flickr.com/services/feeds/photos_public.gne?format=json&nojsoncallback=1&tags=cat,dog&tagmode=any
```

### Response shape (simplified)

```json
{
  "title": "Recent Uploads tagged cat",
  "link": "https://www.flickr.com/photos/tags/cat/",
  "modified": "2026-11-17T12:00:00Z",
  "items": [
    {
      "title": "Photo title",
      "link": "https://www.flickr.com/photos/<user>/<id>/",
      "media": { "m": "https://live.staticflickr.com/<server>/<id>_<secret>_m.jpg" },
      "date_taken": "2026-11-17T10:00:00-08:00",
      "description": "<p>HTML description</p>",
      "published": "2026-11-17T18:00:00Z",
      "author": "nobody@flickr.com (\"username\")",
      "author_id": "12345678@N00",
      "tags": "cat cute"
    }
  ]
}
```

### Notes and quirks

- The feed returns only the **20 most recent** matching photos. It has no pagination.
- `media.m` is a small image (about 240 px). Larger sizes come from swapping the size suffix in the URL, for example `_m` → `_b` (1024 px).
- `title` can be empty, so the UI needs a fallback.
- `description` contains HTML.
- The feed's JSON has been known to sometimes contain invalid escape sequences (such as `\'`). The parser should be lenient.

---

## Tech stack

| Area | Technology |
|------|------------|
| Language | Kotlin 2.2.10 |
| UI | Jetpack Compose (BOM 2026.02.01), Material 3 |
| Architecture | MVI on `StateFlow` / `SharedFlow`, AndroidX ViewModel, Lifecycle Compose 2.11.0 (`collectAsStateWithLifecycle`) |
| Dependency injection | Hilt 2.60.1 (KSP 2.3.12), `androidx.hilt` 1.4.0 (`hiltViewModel()`) |
| Build | Gradle 9.4.1 (Kotlin DSL), Android Gradle Plugin 9.2.1, version catalog (`gradle/libs.versions.toml`) |
| JDK | Toolchain 21 (Gradle daemon), Java 11 bytecode target |
| SDK | `compileSdk` 37, `targetSdk` 36, `minSdk` 24 |
| Testing | JUnit 4, kotlinx-coroutines-test 1.10.2, MockK 1.14.11, Google Truth 1.4.4, AndroidX Test, Espresso, Compose UI Test, Paparazzi 2.0.0-alpha05.1 (screenshot tests) |
| Code coverage | JaCoCo 0.8.14 |

> Networking, image loading and navigation libraries will be listed here once they are added.

---

## Architecture

The app follows **Clean Architecture** with layers kept as packages in the single `:app` module:

| Layer | Package | Responsibility |
|-------|---------|----------------|
| Presentation | `presentation/` | Compose screens, ViewModels, MVI contracts, theme |
| Domain | `domain/` | Use cases, models and repository interfaces (pure Kotlin) |
| Data | `data/` | Repository implementations, network |
| DI | `di/` | Hilt modules that bind the layers together |

> The `domain` and `data` packages are created once they get their first code (photo search). `di` holds `DispatcherProvider` so far.

### Coroutine dispatchers

Code never uses `Dispatchers.*` directly. It gets a `DispatcherProvider` (`main`, `io`, `default`) through Hilt. `DefaultDispatchers` is the production implementation, bound in `DispatcherProviderModule`. Unit tests pass test dispatchers instead, so coroutines run in a controlled, deterministic way.

### MVI

Every screen has a **contract** with three types:

- **`UiState`**: a `data class` that holds the whole screen state. Derived values (for example `isSearchEnabled`) are computed properties.
- **`Event`**: a `sealed interface` of user actions. Events are the only thing the view sends to the ViewModel.
- **`Action`**: a `sealed interface` of one-off messages for the view (for example hiding the keyboard).

```
 View ──Event──▶ ViewModel.onEvent()
  ▲                    │
  │◀── StateFlow<UiState> (always the latest state)
  │◀── SharedFlow<Action> (one-off, no replay)
```

`BaseViewModel<S, E, A>` (`presentation/base`) is the base class. It takes the initial state and a `DispatcherProvider`, exposes `state` and `actions`, and offers `setState { copy(...) }` and `sendAction(...)` to subclasses.

Each screen is split into:

- **`XxxRoute`**: stateful. It gets the ViewModel through `hiltViewModel()`, collects state with `collectAsStateWithLifecycle()` and handles actions in a `LaunchedEffect`.
- **`XxxScreen`**: stateless. It takes `state` and `onEvent` only, so it is easy to preview and test.

---

## Project structure

```
FlickrRecruitmentTask/
├── app/
│   ├── build.gradle.kts
│   └── src/
│       ├── main/
│       │   ├── AndroidManifest.xml
│       │   ├── java/pl/marcin/malocha/flickrrecruitmenttask/
│       │   │   ├── FlickrApp.kt             # @HiltAndroidApp Application
│       │   │   ├── MainActivity.kt          # @AndroidEntryPoint, Compose host
│       │   │   ├── di/                      # Hilt modules, DispatcherProvider
│       │   │   └── presentation/
│       │   │       ├── base/                # BaseViewModel (MVI base class)
│       │   │       ├── search/              # Tag search: contract, ViewModel, screen
│       │   │       └── theme/               # Material 3 theme (Color, Theme, Type)
│       │   └── res/                         # Resources (icons, strings, themes)
│       ├── test/                            # Local unit tests (JVM)
│       │   ├── java/.../flickrrecruitmenttask/
│       │   │   ├── di/providers/            # Test DispatcherProvider implementations
│       │   │   ├── rules/                   # MainDispatcherRule
│       │   │   ├── utils/                   # Test helpers (collectToList, snapshotLightAndDark)
│       │   │   └── presentation/search/     # SearchViewModelTest, SearchScreenScreenshotTest
│       │   └── snapshots/images/            # Paparazzi golden PNGs
│       └── androidTest/                     # Instrumented tests (device/emulator)
│           └── java/.../flickrrecruitmenttask/
│               └── presentation/search/     # SearchScreenTest (UI → event)
├── gradle/
│   ├── libs.versions.toml                   # Dependency version catalog
│   └── wrapper/                             # Gradle wrapper
├── build.gradle.kts                         # Root build script
├── settings.gradle.kts
└── gradle.properties
```

---

## Getting started

### Prerequisites

- **Android Studio**, a recent stable version that supports AGP 9.2
- **JDK 21**. Android Studio's bundled JBR works, or Gradle resolves the toolchain automatically through Foojay.
- An Android device or emulator running **Android 7.0 (API 24)** or newer, with internet access

### Clone

```bash
git clone git@github.com:Marcin0203/flickrrecruitmenttask.git
cd flickrrecruitmenttask
```

### Build and run

From Android Studio, open the project, wait for the Gradle sync to finish and run the `app` configuration.

From the command line:

```bash
# Build the debug APK
./gradlew assembleDebug

# Install it on a connected device or emulator
./gradlew installDebug
```

The debug APK is written to `app/build/outputs/apk/debug/app-debug.apk`.

---

## Testing

Unit tests cover the ViewModels. They use:

- **`MainDispatcherRule`**: a JUnit rule that replaces `Dispatchers.Main` with a test dispatcher for each test.
- **Test `DispatcherProvider`s** (`StandardTestDispatcher`, `UnconfinedTestDispatcher`): they are passed to the ViewModel in place of `DefaultDispatchers`, sharing one `TestCoroutineScheduler`.
- **Google Truth** for assertions and **MockK** for mocking dependencies.
- State is checked through `state.value`. One-off actions are collected into a list with the `collectToList(flow)` helper (`utils/FlowTestUtils.kt`), which runs in `backgroundScope`.

UI tests (`androidTest`, Compose UI Test) check that **user interactions send the right events**. They do not check how the screen looks:

- They test the stateless `XxxScreen`, not the `XxxRoute`. The test passes a fixed `state` and an `onEvent` lambda that records events into a list, so neither Hilt nor a ViewModel is needed.
- Nodes are found by **test tags** (an `XxxScreenTestTags` object next to each screen, shared by the screen and the test), not by text. Then the test performs an action (`performTextInput`, `performClick`, `performImeAction`) and asserts the recorded events with Truth.
- Visual appearance is covered by screenshot tests (below).

Screenshot tests (**Paparazzi**) cover how screens look. They render the stateless `XxxScreen` on the JVM with layoutlib, so they need no device or emulator:

- `SearchScreenScreenshotTest` has one test per important UI state (empty query with a disabled button, filled query) on a `PIXEL_5` device config.
- Each test calls the generic `Paparazzi.snapshotLightAndDark { ... }` helper (`utils/PaparazziTestUtils.kt`). It takes two named snapshots (`light`, `dark`) of any composable in the app theme with `dynamicColor = false`, so tests only describe what to render and the result does not depend on the wallpaper or API level.
- Golden images live in `app/src/test/snapshots/images/` and are committed. After an intended UI change, record them again and review the PNG diff.

Code coverage is measured with **JaCoCo**. Two tasks in `app/build.gradle.kts` write an HTML and XML report to `app/build/reports/jacoco/<task name>/`:

- `jacocoTestReport` runs the debug unit tests (including the Paparazzi tests).
- `jacocoFullTestReport` also runs the UI tests (`connectedDebugAndroidTest`) and merges both results, so it needs a connected device or emulator.

Code that is not meant to be tested is excluded, so the number reflects hand-written logic and UI only:

- Generated code: `R`, `BuildConfig`, `Manifest`, Hilt/Dagger output (`Hilt_*`, `*_Factory`, `*_MembersInjector`, `*_HiltModules`, `hilt_aggregated_deps`, ...), Compose `ComposableSingletons` and lambdas, and Kotlin `$WhenMappings`.
- App entry points and DI wiring: `MainActivity`, `FlickrApp` and the `di` package.
- Compose previews. They live in the same file as their screen, so they cannot be excluded by class name. Instead they are marked with `@ExcludeFromJacocoGeneratedReport` (`presentation/utils/`), and JaCoCo skips any code with an annotation whose name contains "Generated".

```bash
# Local unit tests (JVM)
./gradlew test

# Record screenshot goldens (after an intended UI change)
./gradlew recordPaparazziDebug

# Compare screenshots against the goldens
./gradlew verifyPaparazziDebug

# Instrumented and UI tests (needs a connected device or emulator)
./gradlew connectedAndroidTest

# Coverage report for unit and screenshot tests
./gradlew jacocoTestReport

# Coverage report that also includes UI tests (needs a connected device or emulator)
./gradlew jacocoFullTestReport

# Android Lint
./gradlew lint
```

---

## Decisions log

Key technical decisions and their reasoning, in the order they were made.

| # | Decision | Rationale |
|---|----------|-----------|
| 1 | Project bootstrapped from the Android Studio *Empty Activity (Compose)* template | Up-to-date Gradle / AGP / Compose setup with a version catalog |
| 2 | Clean Architecture layers as **packages** in `:app`, not separate Gradle modules | The app is small. Packages keep the layer boundaries clear without extra Gradle setup. |
| 3 | **Hilt** for dependency injection | Compile-time checked graph, the standard Android DI with first-class ViewModel support |
| 4 | **MVI** with `StateFlow` for state and `SharedFlow` for actions; the view sends only events | One-way data flow, a single source of truth for the screen, and one-off actions that are not replayed after configuration changes |
| 5 | `UiState` is a **data class**, not a sealed class | Fields such as the query and the results live side by side and are updated with `copy()`, without being lost when switching states |
| 6 | `compileSdk` raised to 37 (`targetSdk` stays 36) | Current AndroidX libraries (`core` 1.19, `lifecycle` 2.11, `androidx.hilt` 1.4) need to compile against API 37 |
| 7 | Coroutine dispatchers injected through **`DispatcherProvider`** | Tests can swap in test dispatchers and control time. The test implementations live in `src/test`, so the app does not depend on `kotlinx-coroutines-test`. |
| 8 | UI tests check **interaction → event** on the stateless `XxxScreen` with a recording `onEvent` lambda | Keeps UI tests independent of the ViewModel (already covered by unit tests) and of Hilt. The look of the UI is left to screenshot tests. |
| 9 | UI elements get **test tags** (`XxxScreenTestTags`) and UI tests find nodes by tag | Stable against copy and localisation changes and against new elements of the same type. The tests do not depend on how the UI looks. |
| 10 | **Paparazzi** for screenshot tests, version `2.0.0-alpha05.1` | Runs on the JVM with no emulator, so it is fast and CI-friendly. The 2.0 alpha line is the only one that supports AGP 9. Light/dark and `dynamicColor = false` are handled once in a test util (`snapshotLightAndDark`), so there is one test per UI state and snapshots are deterministic. |
| 11 | **JaCoCo** with two custom tasks (`jacocoTestReport`, `jacocoFullTestReport`) and an exclude list | The plain JaCoCo setup works with AGP 9 without extra plugins. One task works without a device, the other merges UI test coverage. Excluding generated code, entry points, DI wiring and previews keeps the coverage number meaningful. Previews are excluded with an annotation, so they can stay next to their screen. |

---

## AI assistance

This project is developed with the help of an AI coding assistant. The task explicitly allows AI tooling.

| | |
|---|---|
| Tool | [Claude Code](https://claude.com/claude-code), Anthropic's agentic coding CLI |
| Model | Claude Opus 5.5 (`claude-opus-5-5`), Claude 5 family by [Anthropic](https://www.anthropic.com) |
| Model knowledge cutoff | June 2026 |

### What the AI helped with so far

- Git repository setup: `.gitignore`, `.gitattributes` and remote configuration
- This README: its structure, the Flickr API documentation and the project overview
- Architecture setup: Hilt and KSP configuration, the MVI base class and the tag search screen
- Unit test setup: `DispatcherProvider`, `MainDispatcherRule` and the `SearchViewModel` tests
- UI tests: `SearchScreenTest`, which checks that user interactions send the right `SearchEvent`s
- Screenshot tests: Paparazzi setup, the `snapshotLightAndDark` helper and `SearchScreenScreenshotTest`
- Code coverage: JaCoCo setup with the `jacocoTestReport` and `jacocoFullTestReport` tasks, the exclude list and the `@ExcludeFromJacocoGeneratedReport` annotation for previews

### How it is used

- The AI works as a pair programmer. It proposes changes, and the author reviews, adjusts and approves each one before it lands in the repository.
- All commits are authored by the author. The author understands the code and takes responsibility for it.
- This section is updated as the project progresses.

---

## Author

**Marcin Małocha**: [GitHub @Marcin0203](https://github.com/Marcin0203)
