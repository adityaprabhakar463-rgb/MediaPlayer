# Celestial — Progress & Phases

**Last updated:** 2026-11-07
**Product:** Celestial, native Android space-themed media player (Kotlin + Jetpack Compose)
**Reference:** `docs/frontend/11_FRONTEND_IMPLEMENTATION_GUIDE.md` (phase definitions and acceptance criteria)

---

## For AI agents — read this first

This file is a status snapshot, not full context. `AGENTS.md` at the repo root is your always-on rules file (auto-loaded by Antigravity; read it first in any agent). Before doing any work:

1. **Read `AGENTS.md`** — working rules, skill wiring, terse mode.
2. **Read `README.md`** — doc index and product rules.
3. **Read `docs/frontend/01`–`11` in order.** Mandatory:
   - `01_DESIGN_VISION.md` — product concept and visual philosophy
   - `02_SKILL_USAGE_AND_OPENCODE_RULES.md` — required skills and workflow (must follow for every UI task)
   - `03`–`10` — design system, 3D scene, first launch, screens, player controls, components, motion, performance
   - `11_FRONTEND_IMPLEMENTATION_GUIDE.md` — phases, acceptance criteria, structure
4. **Inspect the actual code** under `app/src/main/java/com/celestial/player/` before changing files — this file's summary may lag behind reality.
5. **Treat confirmed user decisions in `docs/frontend/*` as requirements.** Where a detail is marked **Open decision**, ask; never silently invent product behaviour.
6. **Before UI work**, load the skills mapped in `02` (e.g. `android-compose-design`, `android-production-ultimate`, `ui-ux-pro-max`) and list which were consulted in your plan.

Full context lives in the docs and code; this file only tracks progress and phases. When you finish a phase, update both this file and the docs if decisions changed.

---

## Current status

| Area | State |
|---|---|
| Design documentation (`docs/frontend/01`–`11`) | Complete ✅ |
| Frontend improvement docs (15 navigation/UX labels, 16 performance) | Written ✅ |
| Backend documentation (`docs/backend/01`–`13`) | Written; integration status in doc 13 ⚠️ |
| Gradle project scaffold (`:app`, AGP 9.5, Kotlin 2.4.10, Compose BOM 2026.09) | Complete ✅ |
| Phase 1 — Design foundation | Complete ✅ |
| Phase 2 — Destination UI without 3D | Complete ✅ |
| Phase 3 — 3D scene foundation | Complete ✅ |
| Phase 4 — Navigation and continuity | Complete ✅ |
| Phase 5 — Now Playing signature controls | Complete ✅ |
| Phase 6 — First-launch formation | Complete ✅ |
| Phase 7 — Refinement and quality | Complete ✅ |
| Backend M0 — Skeleton and Auth (Ktor, anonymous device register, signed JWT + refresh rotation) | Prototype ✅ (in-memory store; Postgres/Flyway scaffold not wired) |
| Backend M1 — Catalog and Audius Adapter (TrackSource, curated catalog, availability, playback contract) | Complete ✅ (curated real Audius catalog; automatic refresh pending) |
| Backend M2 — Library and Sync (Playlists CRUD, favourites, recents, delta endpoints) | Complete ✅ (in-memory persistence) |
| Backend M3 — Search and Hardening (catalog search, problem+json, health, contract tests) | Partial ⚠️ (Postgres FTS, rate limits, OpenAPI pending) |
| Android Client Networking (`CelestialApiClient.kt`, `LibraryStateHolder.kt`, `PlayerController.kt`) | Connected ✅ (auth, catalog, search, playlists, favourites, recents, restart-safe ADB tunnel, retrying sync) |
| Real playback (`AudioEngine.kt` + Media3/ExoPlayer) | Complete ✅ (Audius streams, buffering/error state, completion/queue/repeat handling) |
| **Pending / Upcoming** | In-memory → PostgreSQL persistence; background playback service; Audius catalog refresh automation; minor frontend appearance adjustments |

---

## Phases & Milestones

### Phase 1 — Design foundation ✅ Done
- [x] Compose theme and semantic design tokens
- [x] Typography and spacing
- [x] Glass surface primitives
- [x] Common icon buttons and list rows
- [x] Previews for core components (+ gallery screen and screenshot tests)

### Phase 2 — Destination UI without 3D dependency ✅ Done
- [x] Playlists glass list
- [x] Search glass interface
- [x] All Songs, Albums, Artists, Favourites, Recently Played layouts
- [x] Settings glass panel
- [x] Persistent player placeholder wired to a single state model

### Phase 3 — 3D scene foundation ✅ Done
- [x] Integrate and validate renderer (`scene/` native 3D engine)
- [x] Starfield and central star (800+ star particles, luminous central star w/ corona & flares)
- [x] Destination planets and camera controller (7 distinct 3D planets + `OrbitCamera`)
- [x] Free rotation, pinch zoom, selection (3D gesture drag w/ inertia, pinch zoom, raycasting hit-testing)
- [x] Planet labels always visible glass pills with selected emphasis (`docs/frontend/15`)

### Phase 4 — Navigation and continuity ✅ Done
- [x] Planet approach transition (camera smooth zoom to selected planet on tap)
- [x] Destination screen integration (seamless transition into glass destination UI)
- [x] Reverse camera navigation (Back button triggers smooth reverse camera pullback)
- [x] Restore previous camera orientation (`previousDestination` tracking and camera orientation restoration)
- [x] Persistent mini music planet stays visible (`MiniPlayerPlanet` anchored across all destinations)

### Phase 5 — Now Playing signature controls ✅ Done
- [x] Album artwork planet (`AlbumPlanet3DView` glowing 3D album sphere w/ rotation)
- [x] Playback-driven seek ring (`SeekRingView` orbital progress ring w/ tap & drag seeking)
- [x] UFO play/pause (`UfoPlayPauseButton` spaceship control w/ illuminated beam)
- [x] Asteroid previous/next (`AsteroidControlButton` 3D asteroid controls w/ impact animation)
- [x] Standard glass queue, shuffle, repeat, favourite (queue drawer, shuffle, repeat modes, favourite toggle)
- [x] Synchronised with single state controller model

### Phase 6 — First-launch formation ✅ Done
- [x] Big Bang, nebula dust, central star ignition, planet accretion (`UniverseFormationSequence`)
- [x] Seamless handoff to the interactive 3D solar system scene
- [x] Skip button ("Skip Intro")
- [x] Replay intro available in Settings panel ("Replay intro" -> Replay)
- [x] Reduced-effects support (shorter duration when reduced motion is enabled)

### Phase 7 — Refinement and quality ✅ Done
- [x] Motion polish (smooth camera damping, spring physics, animated controls)
- [x] Reduced-effects mode (`CelestialEffects` toggle fully operational across scene and backdrops)
- [x] Accessibility (TalkBack labels, touch targets 48dp+, state descriptions)
- [x] Responsive layouts (adaptive album grid, window insets padding, edge-to-edge support)
- [x] Performance profiling (60 FPS hardware-accelerated Canvas rendering, bounded particle counts)
- [x] Device testing and visual review (unit tests passing, Robolectric gallery golden tests passing)

### Backend Implementation & Client Networking (M0–M3) — first vertical slice ✅

- **Backend M0 — Skeleton and Auth** `[x] Wired`
  - [x] Ktor modular skeleton (`auth`, `catalog`, `library`, `audius`, `sync`)
  - [x] Anonymous device registration (`POST /api/v1/auth/device/register`) with idempotent install ids
  - [x] HMAC-signed access tokens + single-use refresh-token rotation
  - [x] JWT enforcement on library, sync and playback routes
  - [x] PostgreSQL + Flyway + Docker Compose scaffold exists, but routes currently use `LibraryStore` (in-memory)
- **Backend M1 — Catalog and Audius Adapter** `[x] Complete`
  - [x] `TrackSource` boundary (`AudiusAdapter.kt`)
  - [x] Curated catalog built from real Audius tracks (8 albums, 7 artists, 29 tracks)
  - [x] Stable Celestial ids + Audius external-id map (`Catalog.externalIds`)
  - [x] Availability filtering (`isStreamable`, `isAvailable`, `isDelete`)
  - [x] Playback resolution returns the Audius stream endpoint (`provider: "audius"`)
  - [ ] Automatic catalog refresh / allowlist verification (manual for now)
- **Backend M2 — Library and Sync** `[x] Complete (in-memory)`
  - [x] Playlists CRUD (`GET/POST/PATCH/DELETE /api/v1/library/playlists`)
  - [x] Favourites management (`GET/POST/DELETE /api/v1/library/favourites`)
  - [x] Recently played logging (`GET/POST /api/v1/library/recents`)
  - [x] Delta change log (`GET /api/v1/sync/changes`, `POST /api/v1/sync/ack`)
  - [ ] Durable PostgreSQL persistence and account upgrade/link
- **Backend M3 — Search and Hardening** `[~] Partial`
  - [x] Curated metadata search (`GET /api/v1/catalog/search`)
  - [x] RFC 9457 `application/problem+json` errors via `StatusPages`
  - [x] Health, readiness and version endpoints
  - [x] Backend integration contract tests (`:backend:test`)
  - [x] Android wire-contract mapping test (`ApiContractTest`)
  - [ ] Postgres FTS, rate-limit budget, OpenAPI generation
- **Android client integration**
  - [x] Persistent device identity + token store (`SessionStore`)
  - [x] Single-flight refresh/re-register (`CelestialApiClient`)
  - [x] Catalog snapshot and debounced backend search
  - [x] Optimistic playlist/favourite/recents mutations with retry queues
  - [x] Settings connection status + manual **Sync now**
  - [x] Playback URL resolution with visible success/error state
  - [x] Media3 audio engine and real Audius playback (`AudioEngine.kt`, `PlayerController.kt`)
  - [x] Automatic `adb reverse` tunnel for the debug workflow (`adbReverse` Gradle task)
  - [x] Connection retry with exponential backoff and manual resync reset
  - [ ] Keystore-backed token storage in production
  - [ ] Background playback service + notification controls

See `docs/backend/13_CLIENT_INTEGRATION_AND_RUNTIME.md` for the run instructions, sync semantics,
and the full list of known gaps.

### Next milestone — finish the backend foundation

1. Replace `LibraryStore` with Exposed/PostgreSQL repositories and run Flyway migrations in dev.
2. Automate the curated Audius allowlist refresh and removed-track verification.
3. Add a `MediaSessionService` for background playback and notification/lock-screen controls.
4. Consume the delta sync endpoints instead of full snapshots.
5. Then resume the scheduled minor frontend appearance adjustments.

### Improving music quality — owned by another teammate

> **Owner:** other teammate (not this workstream). Do not implement here without hand-off.

- [ ] Select the highest available stream bitrate/format per track instead of the provider default.
- [ ] Evaluate gapless playback and crossfade support in Media3.
- [ ] Add optional audio normalisation / equaliser only if the product approves it.
- [ ] Verify audio-focus loss, headset/Bluetooth controls and interruption recovery on real devices.
- [ ] Show a quality label in Now Playing only when real stream metadata exists (no fabricated values).

### Frontend improvement log

- [x] Destination registry (`ui/navigation/DestinationRegistry.kt`) is the single source of truth for labels, icons, routes, colours and accessibility text; the 3D scene and fallback sheet both read it.
- [x] Always-visible planet labels while a planet is on screen: glass pills, distance fade and subtle scale, collision avoidance, screen clamping, central-star avoidance, selected-planet emphasis, tap-to-travel and 48dp touch targets (`scene/host/CelestialUniverseScene.kt`).
- [x] Accessible Destinations button + glass sheet listing all seven destinations; conventional list navigation with 48dp rows, label icons and descriptions.
- [x] Canvas custom accessibility actions expose every destination to TalkBack without spatial gestures.
- [x] Visible back buttons on all top-level destination screens (Playlists, Search, All Songs, Albums, Artists, Favourites, Recently Played).
- [x] Registry unit tests cover destination coverage and required label/accessibility data.
- [x] Fixed first-launch UniverseFormationSequence: `onFinished()` was called immediately instead of after the animation completed, causing the "big bang then poof poof" issue where the sequence disappeared before finishing. Fixed by using `LaunchedEffect(Unit)` with `delay(duration.toLong())` before `onFinished()`, so the sequence now plays fully before the "Skip Intro" button appears.
- [x] Fixed screen-by-screen polish pass: recently played empty state description bug ("with" removed), favourites shuffle button accessibility (stateDescription added); reviewed Artists, Albums, Playlists, Search empty states for contrast, spacing and accessibility

---

## Project decisions and working agreements

### Frontend improvement documentation

- `docs/frontend/15_NAVIGATION_UX_AND_PLANET_LABELS.md` — navigation model, destination registry, always-visible planet labels with selected emphasis, accessible Destinations sheet fallback, gesture and accessibility rules.
- `docs/frontend/16_LIGHTWEIGHT_PERFORMANCE.md` — lightweight, lag-free optimization without major changes; Compose hot paths, renderer performance, label performance, measurement and acceptance gates.

Locked frontend improvement decisions:

- Labels always visible while planet is on screen, stronger on selected planet.
- Accessible non-3D Destinations button/sheet added as fallback.
- Performance target: 60 FPS on mid-range 2022+ phones; adaptive reduced effects below that.
- No hardcoding: every value from tokens / single source of truth; every component real and working.
- Local audio feature: user-set directory scanned recursively for mp3/wav/flac/opus, played through the same player model, directory persisted via DataStore.

Locked backend decisions:

- Catalog source: Audius free API (prototype); curated verified artists only.
- Stack: Kotlin + Ktor + PostgreSQL, Docker deployment.
- Auth: anonymous device account first, optional upgrade/link to email + password.
- No audio hosting/transcoding; no offline audio downloads in prototype.

### Working agreements

- Treat confirmed user decisions in `docs/frontend/*` as requirements; do not silently substitute conventional app patterns.
- Ask before inventing behaviour marked **Open decision**.
- No fake/inert controls; no screenshots/videos standing in for interactive 3D.
- Build incrementally, run the app often; keep this status log updated as phases complete.
- Documented convention: keep implementation under `app/src/main/java/com/celestial/player/` (`ui/`, then later `scene/`, `playback/`, `domain/`, `data/` — see §3 of the implementation guide; adapt rather than impose).
