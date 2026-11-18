# Flickr Recruitment Task

![Kotlin](https://img.shields.io/badge/Kotlin-2.2.10-7F52FF?logo=kotlin&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-BOM%202026.02.01-4285F4?logo=jetpackcompose&logoColor=white)
![Material 3](https://img.shields.io/badge/Material-3-757575?logo=materialdesign&logoColor=white)
![Min SDK](https://img.shields.io/badge/minSdk-24-3DDC84?logo=android&logoColor=white)
![Target SDK](https://img.shields.io/badge/targetSdk-36-3DDC84?logo=android&logoColor=white)

An Android app for searching public Flickr photos by tags. It is built with **Jetpack Compose** as a recruitment task for the second interview round.

> **Status:** 🚧 In development. The architecture, the tag input screen, the test setup and CI/CD are in place. Photo search and the remaining requirements below are being implemented step by step.

---

## Table of contents

- [About the task](#about-the-task)
- [Flickr API](#flickr-api)
- [Tech stack](#tech-stack)
- [Architecture](#architecture)
- [Project structure](#project-structure)
- [Getting started](#getting-started)
- [Testing](#testing)
- [CI/CD](#cicd)
- [Decisions log](#decisions-log)
- [AI assistance](#ai-assistance)
- [Author](#author)

---

## About the task

Build a simple Android application that uses Flickr's publicly accessible [photos feed](https://www.flickr.com/services/feeds/docs/photos_public/). No API key is needed. The design is up to the developer.

### Requirements

| # | Requirement | Status |
|---|-------------|:------:|
| 1 | The user can enter one or more **tags** to search for | 🚧 (input UI, search in the ViewModel) |
| 2 | Matching images are shown in a **list or grid** | 🚧 (API client) |
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
- Older versions of the feed were known to put invalid escape sequences (such as `\'`) in the JSON. The live feed returns valid JSON (checked on 2026-11-18), so the app uses a standard parser that only skips unknown keys.

---

## Tech stack

| Area | Technology |
|------|------------|
| Language | Kotlin 2.2.10 |
| UI | Jetpack Compose (BOM 2026.02.01), Material 3 |
| Architecture | MVI on `StateFlow` / `SharedFlow`, AndroidX ViewModel, Lifecycle Compose 2.11.0 (`collectAsStateWithLifecycle`) |
| Dependency injection | Hilt 2.60.1 (KSP 2.3.12), `androidx.hilt` 1.4.0 (`hiltViewModel()`) |
| Networking | Retrofit 3.0.0, OkHttp 5.5.1, kotlinx.serialization 1.9.0 (Retrofit converter) |
| Build | Gradle 9.4.1 (Kotlin DSL), Android Gradle Plugin 9.2.1, version catalog (`gradle/libs.versions.toml`) |
| JDK | Toolchain 21 (Gradle daemon), Java 11 bytecode target |
| SDK | `compileSdk` 37, `targetSdk` 36, `minSdk` 24 |
| Testing | JUnit 4, kotlinx-coroutines-test 1.10.2, MockK 1.14.11 (`mockk` for unit tests, `mockk-android` for UI tests), Google Truth 1.4.4, AndroidX Test, Espresso, Compose UI Test, Paparazzi 2.0.0-alpha05.1 (screenshot tests), OkHttp MockWebServer 5.5.1 (API tests) |
| Code coverage | JaCoCo 0.8.14 |

> Image loading and navigation libraries will be listed here once they are added.

---

## Architecture

The app follows **Clean Architecture** with layers kept as packages in the single `:app` module:

| Layer | Package | Responsibility |
|-------|---------|----------------|
| Presentation | `presentation/` | Compose screens, ViewModels, MVI contracts, theme |
| Domain | `domain/` | Use cases, models and repository interfaces (pure Kotlin) |
| Data | `data/` | Repository implementations, data sources, network |
| DI | `di/` | Hilt modules that bind the layers together |

> `data` holds the Flickr API client, the remote data source, the DTO → `Photo` mapper and `PhotoRepositoryImpl` so far. `domain` holds the `Photo` model, the `PhotoRepository` interface and `SearchPhotosUseCase` so far. `di` holds `DispatcherProvider`, `NetworkModule`, `DataSourceModule` and `RepositoryModule`.

### Networking

`data/remote` holds the Flickr API client:

- **`FlickrApi`**: a Retrofit interface with one call, `getPublicPhotos(tags)`. The fixed `format=json&nojsoncallback=1` parameters are part of the path, and `tags` is added as a query parameter.
- **DTOs** (`data/remote/dto`): `@Serializable` classes that declare only the fields the app uses (`title`, `link`, `media.m`). The parser skips the rest (`ignoreUnknownKeys`).
- **`FlickrApiFactory`**: builds `FlickrApi` from an `OkHttpClient` and a base URL (`https://api.flickr.com/` by default). It lives outside the Hilt module, so tests use exactly the same Retrofit and JSON setup against a mock server.
- **`NetworkModule`** (`di`) provides a singleton `OkHttpClient` and `FlickrApi`.

### Remote data source

The data layer is split into the API, data sources and a repository:

```
FlickrApi ──▶ PhotoRemoteDataSource ──▶ PhotoRepository ──▶ rest of the app
```

`data/remote/datasource/PhotoRemoteDataSource` is the only class that talks to `FlickrApi`. `PhotoRemoteDataSourceImpl`:

- takes the tags as a `List<String>` and joins them with `,`, because the feed expects one comma-separated `tags` parameter. The Flickr request format stays in one place.
- runs the call on `DispatcherProvider.io`.
- returns the raw `PublicPhotosFeedDto` and lets errors (no network, HTTP errors) propagate. Mapping to `Photo` and error handling are the repository's job.

`DataSourceModule` (`di`) binds `PhotoRemoteDataSourceImpl` to `PhotoRemoteDataSource` as a singleton. There is no local data source, because the task needs no caching. A local source could be added later as another dependency of the repository, without changing the remote one.

`data/mapper/PhotoMapper.kt` maps the DTOs to the domain `Photo`: the photo's `link` becomes its `id`, `media.m` is the thumbnail, and the full-size URL is the same URL with the `_m` size suffix swapped for `_b` (1024 px). A URL without the `_m` suffix is kept as it is.

### Repository

`domain/repository/PhotoRepository` is the interface the rest of the app uses to search photos: `suspend fun searchPhotos(tags: List<String>): Result<List<Photo>>`. It lives in `domain` and knows nothing about Retrofit, DTOs or data sources. `data/repository/PhotoRepositoryImpl` implements it:

- It decides where the photos come from. Today that is only `PhotoRemoteDataSource`.
- It maps the feed to `Photo`s with `toDomain()`.
- It returns `Result.success` with the photos, or `Result.failure` with the exception (for example no network or an HTTP error), so callers do not need to know data-layer exception types.
- It rethrows `CancellationException` instead of wrapping it in a failure. Otherwise a cancelled search (for example when the screen is closed) would be reported as an error and coroutine cancellation would break. This is why it uses `try`/`catch` and not `runCatching`, which catches everything.
- It does not switch dispatchers, because the data source already runs the call on `io`.

`RepositoryModule` (`di`) binds `PhotoRepositoryImpl` to `PhotoRepository` as a singleton.

### Use case

`domain/usecase/SearchPhotosUseCase` is what the presentation layer calls to search: `suspend operator fun invoke(query: String): Result<List<Photo>>`. It takes the text from the search field as it is and turns it into tags:

- It splits the query on commas and whitespace, so `cat, dog`, `cat dog` and `cat,dog` all give `["cat", "dog"]`.
- It skips empty tags (for example in `cat,,dog`) and passes each tag only once.
- If no tags are left (for example `" , "`), it returns an empty list and does not call the repository. Without tags the feed would return the most recent photos of any kind, which is not a search result.
- Otherwise it returns the repository's `Result` unchanged.

It has an `@Inject` constructor, so Hilt creates it without a module.

### Coroutine dispatchers

Code never uses `Dispatchers.*` directly. It gets a `DispatcherProvider` (`main`, `io`, `default`) through Hilt. `DefaultDispatchers` is the production implementation, bound in `DispatcherProviderModule`. Unit tests pass test dispatchers instead, so coroutines run in a controlled, deterministic way.

### MVI

Every screen has a **contract** with three types:

- **`UiState`**: a `data class` that holds the whole screen state. Derived values (for example `isSearchEnabled`, `hasNoResults`) are computed properties, so the UI only reads flags and has no logic of its own.
- **`Event`**: a `sealed interface` of user actions. Events are the only thing the view sends to the ViewModel.
- **`Action`**: a `sealed interface` of one-off messages for the view (for example hiding the keyboard).

```
 View ──Event──▶ ViewModel.onEvent()
  ▲                    │
  │◀── StateFlow<UiState> (always the latest state)
  │◀── SharedFlow<Action> (one-off, no replay)
```

`BaseViewModel<S, E, A>` (`presentation/base`) is the base class. It takes the initial state and a `DispatcherProvider`, exposes `state` and `actions`, and offers `setState { copy(...) }` and `sendAction(...)` to subclasses.

### Search screen

`SearchViewModel` gets `SearchPhotosUseCase` through Hilt. `SearchUiState` holds the query and the result of the last search as flat fields:

| Field | Meaning |
|-------|---------|
| `query` | Text typed into the search field |
| `photos` | Photos found by the last successful search |
| `isLoading` | A search is in progress |
| `isError` | The last search failed (for example no network) |
| `hasSearched` | At least one search has finished, so empty `photos` means "no results", not "not searched yet" |

On `SearchClicked` with a non-blank query the ViewModel hides the keyboard and starts a search:

- It sets `isLoading = true` and clears `isError`. The previous photos stay until the new result arrives.
- On success it sets the new `photos`. On failure it clears `photos` and sets `isError`. Both set `hasSearched` and end loading.
- A new search cancels the one still running, so a slow older response never overwrites a newer result.

Below the form the screen shows the first state that applies:

1. `isLoading`: a progress indicator.
2. `isError`: the message "Couldn't load photos. Check your connection and try again." To try again, the user taps Search.
3. `hasNoResults` (derived: the last search finished without error and found no photos): the message "No photos found for these tags."
4. Otherwise nothing (before the first search).

Each screen is split into:

- **`XxxScreen`**: public and stateful. It gets the ViewModel through `hiltViewModel()`, collects state with `collectAsStateWithLifecycle()` and handles actions in a `LaunchedEffect`.
- **`XxxScreenLayout`**: `internal` and stateless. It takes `state` and `onEvent` only, so previews and screenshot tests can render any state without a ViewModel.

The `Route` suffix is kept for navigation and is not used for screens.

---

## Project structure

```
FlickrRecruitmentTask/
├── .github/workflows/                       # CI (ci.yml) and release (release.yml) workflows
├── app/
│   ├── build.gradle.kts
│   └── src/
│       ├── main/
│       │   ├── AndroidManifest.xml
│       │   ├── java/pl/marcin/malocha/flickrrecruitmenttask/
│       │   │   ├── FlickrApp.kt             # @HiltAndroidApp Application
│       │   │   ├── MainActivity.kt          # @AndroidEntryPoint, Compose host
│       │   │   ├── data/
│       │   │   │   ├── mapper/              # DTO → domain model mapping
│       │   │   │   ├── repository/          # PhotoRepositoryImpl
│       │   │   │   └── remote/              # FlickrApi (Retrofit), FlickrApiFactory, DTOs
│       │   │   │       └── datasource/      # PhotoRemoteDataSource
│       │   │   ├── di/                      # Hilt modules (dispatchers, network, data sources, repositories), DispatcherProvider
│       │   │   ├── domain/
│       │   │   │   ├── model/               # Photo (domain model)
│       │   │   │   ├── repository/          # PhotoRepository (interface)
│       │   │   │   └── usecase/             # SearchPhotosUseCase
│       │   │   └── presentation/
│       │   │       ├── base/                # BaseViewModel (MVI base class)
│       │   │       ├── search/              # Tag search: contract, ViewModel, screen
│       │   │       ├── theme/               # Material 3 theme (Color, Theme, Type)
│       │   │       └── utils/               # @ExcludeFromJacocoGeneratedReport (previews)
│       │   └── res/                         # Resources (icons, strings, themes)
│       ├── test/                            # Local unit tests (JVM)
│       │   ├── java/.../flickrrecruitmenttask/
│       │   │   ├── data/mapper/             # PhotoMapperTest
│       │   │   ├── data/remote/             # FlickrApiTest (MockWebServer), datasource/PhotoRemoteDataSourceImplTest
│       │   │   ├── data/repository/         # PhotoRepositoryImplTest
│       │   │   ├── di/providers/            # Test DispatcherProvider implementations
│       │   │   ├── domain/usecase/          # SearchPhotosUseCaseTest
│       │   │   ├── rules/                   # MainDispatcherRule
│       │   │   ├── utils/                   # Test helpers (collectToList, snapshotLightAndDark)
│       │   │   └── presentation/search/     # SearchViewModelTest, SearchScreenScreenshotTest
│       │   ├── resources/feed/              # JSON fixtures of the Flickr feed
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

Unit tests cover the ViewModels (`SearchViewModelTest` mocks `SearchPhotosUseCase`). They use:

- **`MainDispatcherRule`**: a JUnit rule that replaces `Dispatchers.Main` with a test dispatcher for each test.
- **Test `DispatcherProvider`s** (`StandardTestDispatcher`, `UnconfinedTestDispatcher`): they are passed to the ViewModel in place of `DefaultDispatchers`, sharing one `TestCoroutineScheduler`.
- **Google Truth** for assertions and **MockK** for mocking dependencies.
- State is checked through `state.value`. One-off actions are collected into a list with the `collectToList(flow)` helper (`utils/FlowTestUtils.kt`), which runs in `backgroundScope`.
- A search that is still running is simulated with a `CompletableDeferred` that the mocked use case awaits. This is how the loading state and the cancellation of an older search are tested.

`SearchViewModelTest` checks the query and the search button state, that a blank query does not search, the loading state, success, no results, failure (photos cleared), a success after a failure (error cleared) and that a new search cancels the previous one.

API tests (`FlickrApiTest`) check `FlickrApi` built by `FlickrApiFactory` against an OkHttp **MockWebServer**, so they need no network:

- The request: the path, the fixed `format` and `nojsoncallback` parameters and the `tags` parameter.
- Parsing: JSON fixtures in `src/test/resources/feed/` have the same shape as the real feed, including unknown fields, escaped slashes and an empty title. One fixture has no items.
- Errors: an HTTP error response ends in a Retrofit `HttpException`.

Data source tests (`PhotoRemoteDataSourceImplTest`) use a MockK `FlickrApi` and a test `DispatcherProvider`. They check that the tags are joined with `,` (and that a single tag is passed as it is), that the API response is returned unchanged and that an API error propagates to the caller.

Repository tests (`PhotoRepositoryImplTest`) use a MockK `PhotoRemoteDataSource`. They check that the tags are passed to the data source, that the feed is mapped to `Photo`s, that an empty feed gives an empty list, that a data source error becomes `Result.failure` and that `CancellationException` is rethrown, not wrapped.

Use case tests (`SearchPhotosUseCaseTest`) use a MockK `PhotoRepository`. They check how the query is split into tags (commas, spaces, empty tags, duplicates), that a query without tags returns an empty list without calling the repository, and that the repository's success and failure are returned unchanged.

Mapper tests (`PhotoMapperTest`) check that every DTO field ends up in the right `Photo` field, including an empty title, the `_m` → `_b` swap (only right before the file extension, for any extension) and a URL without the `_m` suffix, which is kept as it is.

UI tests (`androidTest`, Compose UI Test) check **the whole screen's behaviour**: state flowing from the ViewModel to the UI and handling of one-off actions. They do not check how the screen looks:

- There is one `XxxScreenTest` per screen. It renders the public `XxxScreen` with a real ViewModel created by hand, so Hilt is not needed in tests. `SearchScreenTest` passes a MockK `SearchPhotosUseCase` (`mockk-android`), so the tests never call Flickr and each test can set its own result.
- Nodes are found by **test tags** (an `XxxScreenTestTags` object next to each screen, shared by the screen and the test), not by text. Then the test performs an action (`performTextInput`, `performClick`, `performImeAction`) and asserts the result in the UI, for example the button is enabled or the field lost focus after a search (the keyboard is hidden).
- `SearchScreenTest` also checks the search states: nothing is shown at first, a search that never finishes (a `CompletableDeferred` the mocked use case awaits) shows the progress indicator, a failed search shows the error message, and a search without photos shows the "no results" message.
- Visual appearance is covered by screenshot tests (below).

Screenshot tests (**Paparazzi**) cover how screens look. They render the stateless `XxxScreenLayout` on the JVM with layoutlib, so they need no device or emulator:

- `SearchScreenScreenshotTest` has one test per important UI state (empty query with a disabled button, filled query, loading, error, no results) on a `PIXEL_5` device config.
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

## CI/CD

The project uses **GitHub Actions** (`.github/workflows/`).

### Branching

```
feature/* ──PR──▶ develop ──PR──▶ main
```

- Work is committed only on `feature/*` branches.
- A feature branch is merged into `develop` through a pull request (squash merge).
- `develop` is merged into `main` through a pull request (merge commit).
- `develop` and `main` are protected: no direct pushes, and a pull request can be merged only when CI is green.

### CI (`ci.yml`)

Runs on every pull request to `develop` or `main` (and can be started by hand). A new push to the same branch cancels the run in progress.

| Job | What it runs | Artifacts |
|-----|--------------|-----------|
| **Build & Lint** | `./gradlew assembleDebug lintDebug` | Debug APK, Lint HTML report |
| **Unit & screenshot tests** | `./gradlew verifyPaparazziDebug`: all JVM unit tests, with Paparazzi screenshots compared against the committed goldens | Unit test report; on failure, screenshot diff images |
| **UI tests & coverage** | Starts an Android emulator (API 34) and runs `./gradlew jacocoFullTestReport`: unit and UI tests with merged JaCoCo coverage. The coverage is posted as a comment on the pull request. | UI test report, JaCoCo HTML/XML report |

The three jobs run in parallel. They are required status checks for merging into `develop` and `main`.

### CD (`release.yml`)

Runs on every push to `main`, which means every merged `develop → main` pull request:

1. Builds the debug APK (`./gradlew assembleDebug`).
2. Creates a git tag `v<versionName>-<run number>`, for example `v1.0-7`, so every release has a unique tag without bumping the version by hand.
3. Publishes a **GitHub Release** with the APK (`flickr-<tag>.apk`) and release notes generated from the merged pull requests.

The APK is debug-signed, so it can be installed directly on a device. It can be downloaded from the repository's **Releases** page.

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
| 8 | ~~UI tests check **interaction → event** on the stateless screen with a recording `onEvent` lambda~~ (replaced by #12) | Kept UI tests independent of the ViewModel and of Hilt, but left the stateful part of the screen (state collection, action handling) untested. |
| 9 | UI elements get **test tags** (`XxxScreenTestTags`) and UI tests find nodes by tag | Stable against copy and localisation changes and against new elements of the same type. The tests do not depend on how the UI looks. |
| 10 | **Paparazzi** for screenshot tests, version `2.0.0-alpha05.1` | Runs on the JVM with no emulator, so it is fast and CI-friendly. The 2.0 alpha line is the only one that supports AGP 9. Light/dark and `dynamicColor = false` are handled once in a test util (`snapshotLightAndDark`), so there is one test per UI state and snapshots are deterministic. |
| 11 | **JaCoCo** with two custom tasks (`jacocoTestReport`, `jacocoFullTestReport`) and an exclude list | The plain JaCoCo setup works with AGP 9 without extra plugins. One task works without a device, the other merges UI test coverage. Excluding generated code, entry points, DI wiring and previews keeps the coverage number meaningful. Previews are excluded with an annotation, so they can stay next to their screen. |
| 12 | Screens are split into a public stateful **`XxxScreen`** and an `internal` stateless **`XxxScreenLayout`**; UI tests cover the whole `XxxScreen` with a real ViewModel | The screen is one unit, so one test file checks it end to end, including hiding the keyboard. `Route` is kept for navigation. The layout is `internal`, not `private`, so screenshot tests can render any state without a ViewModel. |
| 13 | **GitHub Actions** CI with a `feature/* → develop → main` flow and protected `develop`/`main` | Every change is built, linted and tested before it reaches `develop`, and `main` always holds a working app. GitHub Actions is built into the repository, so no external CI service is needed. |
| 14 | **Retrofit** + **OkHttp** + **kotlinx.serialization** for the Flickr feed; kotlinx.serialization stays on 1.9.0 | The standard, well-known networking stack. Retrofit 3 ships a kotlinx.serialization converter, so no reflection-based parser is needed. 1.9.0 is the newest version built for Kotlin 2.2 (1.10+ needs Kotlin 2.3+). |
| 15 | The full-screen view uses the **`_b` (1024 px)** image, built from the feed's `_m` URL in the mapper | The feed only returns the small image. `_b` is sharp on phone screens and still quick to load. Building the URL in the mapper keeps the UI unaware of Flickr's URL format. |
| 16 | A **remote data source** (`PhotoRemoteDataSource`) sits between `FlickrApi` and the repository; there is no local data source | Only the data source knows the Flickr request format and does the IO switch. The repository will decide which source to use and map to the domain, so a cache can be added later without touching the network code. No local source is added now, because the task needs no caching. |
| 17 | `PhotoRepository` is an interface in `domain`, implemented in `data`; `searchPhotos` returns **`Result<List<Photo>>`** and rethrows `CancellationException` | The domain does not depend on the data layer, and the presentation layer can be tested with a fake repository. `Result` makes failure part of the contract, so the presentation layer does not catch Retrofit/IO exceptions. Cancellation is rethrown so structured concurrency keeps working, which `runCatching` would break. |
| 18 | `SearchPhotosUseCase` takes the **raw query** and splits it into tags; a query without tags returns an empty list without a network call | Turning user input into tags is app logic, so it lives in the pure-Kotlin domain and is unit tested without Android. The ViewModel stays thin, and the use case is more than a pass-through to the repository. Skipping the call avoids showing unrelated recent photos as results. |
| 19 | `SearchUiState` keeps the search result in **flat fields** (`photos`, `isLoading`, `isError`, `hasSearched`), not in a sealed result type | The photos can be read and transformed (for example sorted or filtered) directly, without checking for a success state first, so the ViewModel and the UI stay simple. `hasSearched` tells "no results" apart from "not searched yet". |
| 20 | A new search **cancels** the previous one (`searchJob`) | Only the latest query matters. Without cancelling, a slow response for an older query could overwrite the newer result. |
| 21 | UI tests mock **`SearchPhotosUseCase`** with `mockk-android` instead of using a fake repository | The UI test checks the screen, not how the query is split into tags (that is unit tested). Mocking works the same way as in unit tests, and each test can set its own result (photos, error, loading) with one `coEvery`. Mocking a final class on a device needs API 28+, and the CI emulator runs API 34. |
| 22 | A failed search shows an **error message in the screen content**, from `isError` in the state, not a Snackbar; there is no separate Retry button | The message comes from the state, so it stays after rotation and needs no one-off action. Tapping Search again retries, so a Retry button would add nothing the task needs. |

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
- UI tests: `SearchScreenTest`, which checks the whole screen with a real `SearchViewModel` (state updates and hiding the keyboard after a search)
- Screenshot tests: Paparazzi setup, the `snapshotLightAndDark` helper and `SearchScreenScreenshotTest`
- Code coverage: JaCoCo setup with the `jacocoTestReport` and `jacocoFullTestReport` tasks, the exclude list and the `@ExcludeFromJacocoGeneratedReport` annotation for previews
- CI/CD: the GitHub Actions workflows and the branching flow
- Networking: the Retrofit/OkHttp/kotlinx.serialization setup, `FlickrApi`, its DTOs and `FlickrApiTest` with MockWebServer
- Domain layer: the `Photo` model, the DTO → `Photo` mapper and `PhotoMapperTest`
- Remote data source: `PhotoRemoteDataSource`, its Hilt binding and `PhotoRemoteDataSourceImplTest`
- Repository: the `PhotoRepository` interface, `PhotoRepositoryImpl`, its Hilt binding and `PhotoRepositoryImplTest`
- Use case: `SearchPhotosUseCase` (splitting the query into tags) and `SearchPhotosUseCaseTest`
- Search in the ViewModel: the search state in `SearchUiState`, calling the use case from `SearchViewModel`, cancelling an older search, and the updated `SearchViewModelTest` and `SearchScreenTest`
- Search states on the screen: the loading indicator, the error and "no results" messages, their UI tests and screenshot tests

### How it is used

- The AI works as a pair programmer. It proposes changes, and the author reviews, adjusts and approves each one before it lands in the repository.
- All commits are authored by the author. The author understands the code and takes responsibility for it.
- This section is updated as the project progresses.

---

## Author

**Marcin Małocha**: [GitHub @Marcin0203](https://github.com/Marcin0203)
