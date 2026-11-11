# Celestial

**An offline, space-themed music player for Android.**

Celestial turns a single folder of audio files into an interactive 3D solar system. Each planet is a
destination, the currently playing track orbits with you, and all playback stays on the device. No
account, no server, no streaming.

![Platform](https://img.shields.io/badge/platform-Android%2026%2B-3DDC84)
![Language](https://img.shields.io/badge/language-Kotlin-7F52FF)
![UI](https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4)
![Status](https://img.shields.io/badge/status-alpha-F59E0B)

---

## Overview

Celestial is a native Android application built with Kotlin and Jetpack Compose. Users select one
music folder through the Android Storage Access Framework; the app indexes it recursively and
presents the library through the existing planetary navigation and glass UI. Playback is handled by
Media3 and continues in the background with a media notification and lockscreen controls.

The product deliberately avoids conventional tab bars and dashboards. The 3D universe is the
navigation layer; destination screens use familiar glass lists for content.

## Features

- **Offline by design** — no network permission, no accounts, no sync, no API keys.
- **Folder-scoped library** — one user-selected folder, scanned recursively; subfolders included.
- **Embedded metadata** — title, artist, album, duration, track number and album art read from tags.
- **Graceful fallbacks** — filename (`Artist - Title`, leading track numbers) and parent-folder
  album names when tags are missing.
- **Room persistence** — tracks, playlists, favourites and recents survive rescans; entries for
  files that disappeared are pruned.
- **Background playback** — Media3 `MediaSessionService` with audio focus, noisy-headset handling,
  notification and lockscreen/headset controls.
- **Interactive 3D universe** — custom Canvas renderer, free rotation, pinch zoom, inertial camera,
  planet labels and a Big Bang formation sequence.
- **Five destinations** — Playlists, All Songs, Search, Favourites and Recently Played.
- **Local search** — instant search over title, artist, album and folder path.
- **Accessible navigation** — a glass Destinations sheet and TalkBack actions mirror every planet.
- **Reduced-effects mode** — calms ambient motion and glow.

## Navigation

| Destination | Purpose |
|---|---|
| Playlists | User-created playlists stored in Room |
| All Songs | Complete indexed library, with sort options |
| Search | Local search over songs and playlists |
| Favourites | Room-backed favourites |
| Recently Played | Playback history, trimmed to the latest 100 |

Settings opens as a floating glass panel, not a planet. The persistent mini player is available on
every screen and expands into Now Playing.

## Technology

| Area | Choice |
|---|---|
| Language | Kotlin 2.4.10 |
| UI | Jetpack Compose (BOM 2026.09.00), Material 3 |
| 3D | Custom Compose Canvas renderer (no third-party engine) |
| Persistence | Room 2.8.5 (KSP) |
| Playback | Media3 ExoPlayer + MediaSession 1.11.1 |
| Images | Coil 3.6.3 |
| Build | Gradle 9.8.2, AGP 9.5.0, JDK 17 |
| SDK | minSdk 26, targetSdk/compileSdk 37 |

## Architecture

```text
app/src/main/java/com/celestial/player/
  MainActivity.kt
  CelestialAppViewModel.kt        # Activity-scoped library, playback and navigation state
  data/
    LibraryStateHolder.kt         # Compose state facade over Room and the SAF scanner
    db/                           # Room entities, DAO, database
    local/                        # FolderMediaScanner, LibraryPreferences
  domain/model/MediaModels.kt     # Track, Album, Playlist, RecentPlay
  playback/
    PlaybackService.kt            # MediaSessionService, ExoPlayer, notification
    PlayerController.kt           # MediaController facade -> PlayerUiState
    PlayerUiState.kt
  scene/                          # 3D math, camera, planets, formation sequence
  ui/                             # theme, glass components, screens, navigation
```

Scanning runs on `Dispatchers.IO`; Room flows update Compose state on the main thread. A generation
token invalidates stale scans, and a failed root read keeps the previous index intact instead of
replacing it with an empty library.

### Data model

| Table | Purpose |
|---|---|
| `tracks` | Indexed files, metadata, folder path, artwork path |
| `playlists` | User-created playlists |
| `playlist_tracks` | Ordered playlist membership |
| `favourites` | Favourited track IDs and timestamps |
| `recents` | Playback timestamps, trimmed to 100 entries |

Track IDs are SAF document URIs. Album and artist identifiers are deterministic hashes of normalised
names, so rescans preserve user data for files that still exist.

## Requirements

- Android Studio with JDK 17
- Android SDK 37
- Android device or emulator running Android 8.0 (API 26) or newer

`local.properties` only needs the SDK path; there is no backend, database server or API key to
configure.

## Build and test

```bash
# Debug APK
./gradlew :app:assembleDebug

# Unit tests (Robolectric)
./gradlew :app:testDebugUnitTest

# Android Lint
./gradlew :app:lintDebug

# Release build (minified)
./gradlew :app:assembleRelease
```

The debug APK is written to `app/build/outputs/apk/debug/app-debug.apk`.

Unit tests cover the Room library (rescan preservation and pruning), navigation state, the
destination registry, scene math and Roborazzi design screenshots.

## Documentation

The design and implementation record lives in `docs/frontend/`:

| Document | Contents |
|---|---|
| `01_DESIGN_VISION.md` | Product concept and visual philosophy |
| `02_SKILL_USAGE_AND_OPENCODE_RULES.md` | Skill activation rules for design work |
| `03_DESIGN_SYSTEM.md` | Palette, typography, glass tokens |
| `04_SOLAR_SYSTEM_AND_3D_SCENE.md` | Planets, orbits, camera interaction |
| `05_FIRST_LAUNCH_UNIVERSE_CREATION.md` | Big Bang and formation sequence |
| `06_NAVIGATION_AND_SCREEN_SPECIFICATIONS.md` | Destination and screen behaviour |
| `07_NOW_PLAYING_AND_MEDIA_CONTROLS.md` | Seek ring, UFO controls, queue |
| `08_COMPONENT_LIBRARY.md` | Reusable Compose and 3D components |
| `09_MOTION_AND_INTERACTION.md` | Motion language and gestures |
| `10_RESPONSIVE_ACCESSIBILITY_PERFORMANCE.md` | Adaptation, accessibility, performance |
| `11_FRONTEND_IMPLEMENTATION_GUIDE.md` | Phases and acceptance criteria |
| `15_NAVIGATION_UX_AND_PLANET_LABELS.md` | Labels and accessible fallback |
| `16_LIGHTWEIGHT_PERFORMANCE.md` | Performance constraints |
| `17_LOCAL_LIBRARY_AND_FOLDERS.md` | Authoritative local-library specification |

`PROGRESS.md` tracks completed work, verification results and known gaps.

## Project conventions

- Confirmed decisions in `docs/frontend/` are requirements; do not replace them with generic
  mobile-app patterns.
- Do not reintroduce streaming, accounts, sync or a backend.
- Details marked **Open decision** must be raised with the project owner before implementation.
- Working rules for contributors and AI agents are in `AGENTS.md`.

## Status and limitations

Frontend phases 1–8 are complete and the app builds and passes its test suite. On-device validation
of folder permission persistence, notification controls and background playback is still pending.

Known limitations:

- Full rescan on launch; no incremental or background file observer.
- No folder-tree browser; folders are searchable but only All Songs lists content.
- No tag or artwork editing, sleep timer, equaliser or playback speed.
- `mid`/`midi` files are indexed but codec support varies by device.

See `PROGRESS.md` for the detailed engineering log.
