# Celestial — Engineering Log and Status

**Last updated:** 2026-11-11
**Product:** Celestial, a native Android space-themed local music player
**Stack:** Kotlin, Jetpack Compose, custom Canvas 3D renderer, Room, Media3 `MediaSessionService`
**Library model:** one user-selected folder, indexed recursively through the Storage Access Framework
**Offline:** no backend, no network permission, no accounts or API keys

This document is the shared status record for the project. It explains where the codebase started,
what has been built since, what is verified, and what remains. `README.md` covers setup and usage;
`docs/frontend/17_LOCAL_LIBRARY_AND_FOLDERS.md` is the authoritative local-library specification.

---

## 1. Project history

Celestial started as an Audius streaming client. The repository contained:

- an Android app with a Kotlin/Compose frontend, a Ktor API client and a direct Audius client;
- a separate Ktor/Postgres backend project (`backend/`, `celestial-backend/`);
- streaming-oriented design documentation, including a full `docs/backend/` set;
- network permissions, API-key configuration and remote catalogue models.

On **2026-11-11** the project direction changed to an offline player:

- the user selects one music folder on the device;
- the app indexes it recursively and stores the library in Room;
- playback uses Media3 with a background media session;
- all streaming, backend and network code was removed;
- the 3D universe and glass frontend were retained.

The backend was deleted rather than archived in the repository: `backend/`, `celestial-backend/`,
`docs/backend/`, server scripts and logs, the Ktor/Audius clients, `SampleLibrary` and the old
`AudioEngine` are no longer present.

## 2. What has been built

### 2.1 Frontend (phases 1–7)

| Phase | Delivered |
|---|---|
| 1 — Design foundation | Compose theme, semantic tokens, typography, spacing, glass surfaces, shared icon buttons and list rows |
| 2 — Destination UI | Playlists, All Songs, Search, Favourites, Recently Played layouts, settings panel, persistent player placeholder |
| 3 — 3D scene | Custom Canvas renderer, starfield, central star, destination planets, orbit camera with rotation and pinch zoom |
| 4 — Navigation | Planet approach transitions, reverse camera return, restored orientation, persistent mini player |
| 5 — Now Playing | Album planet, playback-driven seek ring, UFO play/pause, asteroid previous/next, queue, shuffle, repeat, favourite controls |
| 6 — First launch | Big Bang, dust and nebula formation, planet formation, palette transition, persisted completion, replay from Settings |
| 7 — Refinement | Reduced-effects mode, label collision handling, accessibility actions, responsive layout, performance tuning |

### 2.2 Local library migration (phase 8)

**Folder access**

- `ACTION_OPEN_DOCUMENT_TREE` picker with persisted read permission.
- No broad storage permission is requested.
- Folder URI and display name persisted in `LibraryPreferences`.
- Folder prompt after the formation sequence; skippable and reopenable from Settings.

**Scanning**

- `FolderMediaScanner` walks the SAF tree breadth-first and skips hidden entries.
- Accepts `audio/*` plus common extensions: mp3, m4a, m4b, aac, flac, ogg, oga, opus, wav, wma,
  mka, aiff, aif, amr, mid, midi.
- Reads tags with `MediaMetadataRetriever`; falls back to filename and parent folder.
- Leading filename numbers (`01 - Artist - Title.mp3`) are used as the track index when tags lack one.
- Embedded artwork is extracted once to `filesDir/artwork/<sha1>.jpg` and shared between identical
  images; orphaned artwork is deleted after a scan.
- Scans run on `Dispatchers.IO` with progress reported to a UI pill.
- A generation token prevents stale scans from committing.
- Cancellation is checked between directories and files.
- If the root folder cannot be read (revoked grant, unplugged volume), the scan fails and the
  previous index is preserved instead of being replaced with an empty library.

**Persistence**

- Room database `celestial-library.db` (version 1), built with KSP.
- Tables: `tracks`, `playlists`, `playlist_tracks`, `favourites`, `recents`.
- Rescans replace the track index atomically but preserve favourites, playlist membership and recents
  for files that still exist; entries for missing files are pruned.
- Recents are trimmed to the latest 100 entries.
- There are deliberately no foreign keys between user data and `tracks`, so a rescan can never
  cascade-delete favourites or playlists.

**Playback**

- `PlaybackService` owns ExoPlayer and a `MediaSession`, with audio focus, noisy-headset handling,
  a media notification and lockscreen/headset controls.
- `PlayerController` is a MediaController facade that mirrors the queue into `PlayerUiState`.
- `PlayerUiState` carries local preparation, buffering and playback-error states; streaming fields
  were removed.
- Files play directly from their SAF URI; nothing is copied or transcoded.
- Removing the library folder stops playback and clears the queue.

**UI integration**

- Settings → Library: folder name, status, indexed track count, change folder, rescan.
- All Songs empty state offers folder selection or rescan.
- Playlists shows a local-library status banner.
- Embedded artwork renders through Coil in lists and the mini player, with the existing gradient
  fallback when no art exists.

### 2.3 Follow-up hardening

- `CelestialAppViewModel` now owns the library, player controller and navigation stack, so rotating
  the device no longer restarts the scan, resets navigation or reconnects playback.
- Scan state, folder prompt and settings state survive configuration changes.
- Folder permission matching is normalised to tolerate trailing-slash differences from providers.
- A failed root read aborts the scan instead of wiping the indexed library.
- Removing the folder clears extracted artwork and stops playback.

### 2.4 Navigation pruning

Albums and Artists were removed as destinations and their detail screens deleted. The universe now
has five planets:

1. Playlists
2. All Songs
3. Search
4. Favourites
5. Recently Played

Album grouping remains internal for artwork and colour seeds; rows still display artist and album
text. Search categories are now Songs and Playlists. Orbits were re-spaced and the visible label cap
lowered to five.

### 2.5 Removed

- `data/remote/` — Ktor API client, direct Audius client, session store, DTOs.
- `SampleLibrary`, old `AudioEngine`, API-key and network settings.
- `backend/`, `celestial-backend/`, `docs/backend/`, backend build logic, server scripts and logs.
- Network permissions and streaming-only tests.
- `AlbumsScreen`, `AlbumDetailScreen`, `ArtistsScreen`, `ArtistDetailScreen`, unused
  `UniversePlaceholderScreen`, and the `Artist` domain model.

## 3. Current capabilities

| Capability | State |
|---|---|
| Folder selection and persisted permission | Implemented; on-device validation pending |
| Recursive scan with tags and artwork | Implemented |
| Room-backed library, playlists, favourites, recents | Implemented; tests pass |
| Background playback, notification, lockscreen controls | Implemented; on-device validation pending |
| Five-planet navigation and Destinations sheet | Implemented |
| Local search over the index | Implemented |
| Configuration-change survival | Implemented |
| Remaining streaming or backend code | None |

## 4. Architecture snapshot

```text
MainActivity
  └─ CelestialAppViewModel (survives configuration changes)
       ├─ LibraryStateHolder ── Room ── SAF folder (FolderMediaScanner)
       ├─ PlayerController ── MediaController ── PlaybackService (ExoPlayer + MediaSession)
       └─ CelestialNavState
            └─ CelestialAppShell
                 ├─ CelestialUniverseScene (5 planets)
                 └─ destination screens + folder prompt and scan status
```

### Stable identity

- Track ID = SAF document URI.
- Album ID = hash(artist + album); artist IDs remain part of track metadata.
- Because IDs are stable, favourites, playlists and recents survive rescans for files that still
  exist.

## 5. Verification

| Check | Result |
|---|---|
| `./gradlew :app:compileDebugKotlin` | Pass |
| `./gradlew :app:testDebugUnitTest` | Pass |
| `./gradlew :app:assembleDebug` | Pass |
| `./gradlew :app:assembleRelease` | Pass |
| `./gradlew :app:lintDebug` | Pass (warnings only) |

Unit tests cover:

- `LibraryDatabaseTest` — rescan preservation, pruning of missing files, recents trimming.
- `CelestialNavStateTest` — navigation stack behaviour.
- `DestinationRegistryTest` — the registry matches the five destinations and every entry has labels,
  accessibility text and orbital parameters.
- `Scene3DMathTest` — camera projection and ray maths.
- Roborazzi screenshot tests for the design foundation.

Lint warnings are limited to dependency version notices, the exported Media3 service and Compose
modifier ordering. They do not fail the build.

**Not yet verified on hardware:** folder picking, persisted permission across process death,
notification and lockscreen controls, headset buttons, background playback and permission revocation.
No emulator or device was attached during development.

## 6. Known gaps

- No folder-tree browser; folders are searchable but only All Songs lists content.
- Full rescan on every launch; no incremental or changed-file scan.
- No background file observer, so changes while the app is open require a manual rescan.
- No tag or artwork editing.
- No sleep timer, equaliser or playback speed.
- Album and artist detail screens do not exist by design.
- `mid`/`midi` files are indexed but codec support varies by device.

## 7. Next steps

1. Run a manual device pass: choose a folder, scan, play in the background, verify the notification,
   lockscreen and headset controls, then revoke permission and confirm the error path.
2. Profile a large folder scan and a long list on a low-end device.
3. Optional, only if requested: folder browsing inside All Songs, incremental scanning, sleep timer.

## 8. Working agreements

- Read `AGENTS.md` before changing the project.
- Confirmed decisions in `docs/frontend/` are requirements; do not replace them with generic
  mobile-app patterns.
- Do not reintroduce streaming, accounts, sync or a backend.
- Ask before implementing anything marked **Open decision**.
- For UI work, follow the skill mapping in `docs/frontend/02_SKILL_USAGE_AND_OPENCODE_RULES.md`.
- Update this file when a phase is completed or a decision changes.
