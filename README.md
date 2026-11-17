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
| 1 | The user can enter one or more **tags** to search for | ⬜ |
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
| Build | Gradle 9.4.1 (Kotlin DSL), Android Gradle Plugin 9.2.1, version catalog (`gradle/libs.versions.toml`) |
| JDK | Toolchain 21 (Gradle daemon), Java 11 bytecode target |
| SDK | `compileSdk` 36.1, `targetSdk` 36, `minSdk` 24 |
| Testing | JUnit 4, AndroidX Test, Espresso, Compose UI Test |

> Networking, image loading, dependency injection and navigation libraries will be listed here once they are added.

---

## Architecture

> 🚧 This section will describe the architecture (layers, data flow, state management) as it is implemented.

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
│       │   │   ├── MainActivity.kt          # Entry point (Compose host)
│       │   │   └── ui/theme/                # Material 3 theme (Color, Theme, Type)
│       │   └── res/                         # Resources (icons, strings, themes)
│       ├── test/                            # Local unit tests (JVM)
│       └── androidTest/                     # Instrumented tests (device/emulator)
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

```bash
# Local unit tests (JVM)
./gradlew test

# Instrumented and UI tests (needs a connected device or emulator)
./gradlew connectedAndroidTest

# Android Lint
./gradlew lint
```

---

## Decisions log

Key technical decisions and their reasoning, in the order they were made.

| # | Decision | Rationale |
|---|----------|-----------|
| 1 | Project bootstrapped from the Android Studio *Empty Activity (Compose)* template | Up-to-date Gradle / AGP / Compose setup with a version catalog |

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

### How it is used

- The AI works as a pair programmer. It proposes changes, and the author reviews, adjusts and approves each one before it lands in the repository.
- All commits are authored by the author. The author understands the code and takes responsibility for it.
- This section is updated as the project progresses.

---

## Author

**Marcin Małocha**: [GitHub @Marcin0203](https://github.com/Marcin0203)
