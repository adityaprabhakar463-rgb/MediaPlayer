# Celestial — Progress & Phases

**Last updated:** 2026-11-07
**Product:** Celestial, native Android space-themed media player (Kotlin + Jetpack Compose); remote backend (Ktor + PostgreSQL + curated Audius catalog prototype)
**Reference:** `docs/frontend/11_FRONTEND_IMPLEMENTATION_GUIDE.md` (frontend phases); `docs/backend/01`–`12` (backend vision/architecture/contract/auth/Audius/sync/ops/testing/impl/catalog); `docs/backend/11_BACKEND_IMPLEMENTATION_GUIDE.md` (backend milestones M0–M3)

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
| Design documentation (`docs/frontend/01`–`11`) | Complete (committed: `b1a1044`) |
| Frontend improvement docs (15 navigation/UX labels, 16 performance) | Written |
| Backend documentation (`docs/backend/00`–`12`) | Written; **`01`–`12` + PROGRESS authoritative** (remote Ktor/Postgres/curated Audius). **`00_BACKEND_CONCEPT.md` legacy/conflicting** (on-device) — confirmed 2026-11-07 |
| Gradle project scaffold (`:app`, AGP 9.5, Kotlin 2.4.10, Compose BOM 2026.09) | Complete |
| Phase 1 — Design foundation | Complete ✅ |
| Phase 2 — Destination UI without 3D | Complete ✅ |
| Phase 3 — 3D scene foundation | Complete ✅ |
| Phase 4 — Navigation and continuity | Complete ✅ |
| Phase 5 — Now Playing signature controls | Complete ✅ |
| Phase 6 — First-launch formation | Complete ✅ |
| Phase 7 — Refinement and quality | Complete ✅ |
| 3D renderer engine | Built native 3D math & projection engine under `scene/` |
| Playback (Media3) | Not yet integrated — single state model + sample simulation placeholder in `playback/` |
| Backend server implementation (`celestial-backend/`, remote) | **M0 complete** (2026-11-07: scaffold + auth skeleton, local compile + smoke test). **M1 next**, awaiting "go". |

### What exists today

**Theme / design tokens** (`app/src/main/java/com/celestial/player/ui/theme/`)
- `CelestialTheme.kt` — `CelestialTheme` entry point; CompositionLocals for colors, spacing, effects; Material3 dark color scheme (space palette)
- `CelestialColors.kt`, `Color.kt` — semantic space color tokens (SpaceVoid, DeepSpace, Starlight, CosmicViolet, Nebula, …)
- `Type.kt` — `CelestialTypography`
- `Spacing.kt` — spacing tokens (xs … screenEdge)
- `Shape.kt` — `CelestialShapes`
- `Effects.kt` — `CelestialEffects` (carries reduced-effects capability)

**Glass components** (`ui/components/glass/`)
- `CelestialGlassSurface` — base glass surface primitive
- `GlassIconButton`
- `GlassListRow` + `GlassArtworkPlaceholder`
- `GlassSectionHeader`
- `GlassStates` — `GlassEmptyState`, `GlassLoadingState`, `GlassErrorState`
- `GlassComponentPreviews` — `@Preview`s for all of the above

**Gallery & verification**
- `ui/gallery/DesignFoundationGallery.kt` — scrollable showcase of every Phase 1 component (currently the app's content, labelled "Design foundation · Phase 1")
- `MainActivity.kt` — edge-to-edge, sets `CelestialTheme { DesignFoundationGallery() }`
- `DesignFoundationScreenshotsTest.kt` — Robolectric + Roborazzi screenshot tests; **2/2 passing**, goldens recorded at `app/build/outputs/roborazzi/gallery_{top,bottom}.png`
- Debug APK builds: `app/build/outputs/apk/debug/app-debug.apk`

### Not yet in place
- No `ui/screens/`, `ui/navigation/`, `scene/`, `playback/`, `domain/`, `data/` packages
- No 3D library (Filament/Sceneform/etc.) in `gradle/libs.versions.toml`
- No Media3 dependency, no real playback state model
- Only one commit of source; `app/` and Gradle files are still untracked in git

---

## Phase 2 work log (2026-11-05 session)

Skills consulted for Phase 2 (per `docs/frontend/02`): `android-compose-design`, `android-production-ultimate`, `ui-ux-pro-max`, `design-system`, `ui-styling`, `mobile-android-design`. Project docs override skill defaults where they conflict (locked palette/tokens from docs 01/03; no new typeface chosen).

### Written this session (source complete, does not compile yet)

**Domain / data / playback**
- `domain/model/MediaModels.kt` — `Track`, `Album`, `Artist`, `Playlist`, `RecentPlay`, value-class ids; `@Immutable`
- `data/SampleLibrary.kt` — 5 artists, 8 albums, 27 tracks, 5 playlists (sample library, see temporary assumptions)
- `data/LibraryStateHolder.kt` — favourites (set), playlists (mutable list, `createPlaylist`), recents (insert-on-play + 8 seeded sample entries), 100-entry cap
- `playback/PlayerUiState.kt` — single state model: current track, isPlaying, position/duration, buffering, queue+index, shuffle, `RepeatMode`, derived `progressFraction`
- `playback/PlayerController.kt` — state holder + play/pause/next/previous/seek/shuffle/repeat + 500ms sample-position ticking loop in caller-provided `CoroutineScope`. **Temporary: replaced by Media3 in later phase; no UI-only drift beyond this class.**
- Doc 07 §7 rules honored: one source of truth, immutable state, logic out of composables.

**New glass components** (`ui/components/glass/`)
- `GlassArtwork.kt` — token-derived album/artist artwork (gradient from `colorSeed`, `artworkColors()`), initials option
- `GlassSearchField.kt` — BasicTextField in glass surface, clear button, IME Action.Search
- `GlassSettingsPanel.kt` — `GlassSettingsPanel` + `SettingsSection` + `SettingsEntryValue.Toggle/Info` (disabled entries dim), Switch colors on tokens
- `GlassTrackRow.kt` — shared row: artwork + title + subtitle + trailing favourite toggle (48dp)
- `GlassComponentPreviewsPhase2.kt` — previews for the above + mini planet

**Player placeholder**
- `ui/components/player/MiniPlayerPlanet.kt` — mini planet placeholder: artwork disc + progress ring arcs, semantics `contentDescription`/`stateDescription` (playing %/paused). Tap is wired at shell level (hint action, see decisions). Swappable for 3D `PersistentMusicPlanet` in Phase 4/5.

**Backdrop**
- `ui/components/backdrop/DeepSpaceBackdrop.kt` — deep-space gradient, 110 seeded stars, twinkle only when `ambientMotion && !reducedMotion`, 2 comets in top band only, static fallback otherwise (doc 09 §7/§8)

**Screens** (`ui/screens/`)
- `playlists/PlaylistsScreen.kt`, `PlaylistRow.kt` (artwork + title + track count + overflow: Play/Open), `CreatePlaylistAction.kt` (glass inline input), `PlaylistDetailScreen.kt` (`PlaylistDetailHeader` + track list, empty state)
- `search/SearchDestinationScreen.kt` (`RecentSearches` list w/ clear, `Suggested` section, debounced 280ms query, results), `SearchCategory.kt`, `SearchCategoryTabs.kt` (FilterChips) — category tabs exist but filtering currently lists song matches only regardless of tab
- `library/AllSongsScreen.kt` — A–Z default sort + sort menu (Title/Artist/Longest)
- `albums/AlbumsScreen.kt` (`AlbumCard` 2-col adaptive grid ≥150dp), `AlbumDetailScreen.kt`
- `artists/ArtistsScreen.kt`, `ArtistDetailScreen.kt` (albums + songs sections)
- `favourites/FavouritesScreen.kt` (empty state + shuffle), `recentplay/RecentlyPlayedScreen.kt` (recency subtitle + empty state)
- `universe/UniversePlaceholderScreen.kt` — **temporary** destination launcher until 3D solar system (Phase 3/4)
- `common/DestinationScreen.kt` (shared scaffold, title header, `formatTrackDuration`) + `TrackListItems.kt` (shared lazy items w/ stable keys)

**Shell / navigation**
- `ui/navigation/NavTarget.kt` — sealed `NavTarget` (8 destinations + 3 detail types), `CelestialNavState` backstack
- `ui/CelestialAppShell.kt` — `AnimatedContent` transitions (slide+fade; fade-only when reduced motion), `BackHandler`, persistent `MiniPlayerPlanet` bottom-end single instance (doc 04 §6 compliance), tap-hint transient chip, Settings floating glass panel + scrim (z 3/4)
- `MainActivity.kt` — owns `reducedEffects` state; `CelestialTheme(effects=…)` recomposes on toggle (Settings toggle → real theme change)
- Phase 1 `DesignFoundationGallery` no longer shown at startup (kept for screenshot tests / previews)

### User decisions resolved today (were doc 11 §2 Phase 2 open areas)
- Sample **in-memory** library data feeds screens until real media scan lands
- Default layouts: **glass list rows** for All Songs/Artists/Favourites/Recently Played; **Albums = adaptive grid**; All Songs default sort **Title A–Z**
- Mini planet tap (Phase 2): **disabled hint action** — shows "Now Playing opens with the 3D universe" chip; no fake navigation
- Settings scope: **reduced effects = real toggle**; "Replay intro" shown as disabled "Coming soon" (doc 05 §4); About = real app info

### Temporary assumptions (per README §5 — clearly documented, revisit later)
1. Sample library + position simulation in `playback/PlayerController` — replaced by Media3 + real scan
2. `UniversePlaceholderScreen` destination chips — replaced by 3D solar system + planet navigation (Phase 3/4)
3. Recents/favourites/playlists are in-memory only (no Room/DataStore yet)
4. Search state (recent searches, query) session-scoped
5. Category tabs shown but not yet wired to switch result type (song matches only)

### Known compile errors (`:app:compileDebugKotlin`) — Resolved
1. `data/SampleLibrary.kt` — default empty lists in `Artist` + string comparison via `.id.value` + fixed `artistSeeds.map` recursion in `val artists`.
2. `ui/CelestialAppShell.kt` — fixed `onToggleFavourite` argument to pass `it.id` + added `Text` import.
3. `ui/screens/artists/ArtistDetailScreen.kt` — removed duplicate `Alignment` import + added `PaddingValues` import.
4. `ui/screens/playlists/PlaylistDetailScreen.kt` — added `QueueMusic` and `Text` imports.
5. `ui/screens/search/SearchCategoryTabs.kt` — added `Text` import.
6. `ui/screens/search/SearchDestinationScreen.kt` — added `items` and `Icon` imports.
7. `ui/screens/common/DestinationScreen.kt` — wired `onBack` back button in header row.

Also verify after fixes: `MiniPlayerPlanet` uses `3.dp` inside `Canvas` stroke (use `3.dp.toPx()` from DrawScope — legal), screenshot test still compiles (`DesignFoundationGallery` untouched), `playback/PlayerController.kt` tick loop test.

### Acceptance not yet verified (doc 11 §4)
- Playlist rows Apple-Music-inspired usability — visual review pending
- Search clear + responsive — after compile fix
- Settings floating panel — after compile fix
- Persistent mini planet visible — after compile fix
- Quality: no fake/inert controls (play/shuffle/favourites/queue wiring real against state model), TalkBack labels, touch targets 48dp, reduced-effects works

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

Locked backend decisions (per docs 01–12 / prior lock):
- Catalog source: Audius free API (prototype); curated verified artists only.
- Stack: Kotlin + Ktor + PostgreSQL, Docker deployment.
- Auth: anonymous device account first, optional upgrade/link to email + password.
- No audio hosting/transcoding; no offline audio downloads in prototype.
- No code changes in that docs-only task; backend implementation now proceeds by approved phases (Step 1 done, Step 2 pending approval).

---

## Backend (remote server) — architecture, phases, and implementation

**Authoritative docs:** `docs/backend/01–12` + PROGRESS locked decisions. `docs/backend/00_BACKEND_CONCEPT.md` is **legacy/conflicting** (on-device scope) — do not use as backend source of truth (confirmed 2026-11-07).
**Implementation location:** `celestial-backend/` (standalone Gradle project at repo root; no `app/` / Android frontend changes).
**Status (2026-11-07):** M0 complete (local compile + smoke test; Docker/Testcontainers not available in this env). M1 awaiting "go".

### Architecture (docs 01–03, 11)

- **Style:** Ktor modular monolith (domain pure Kotlin, no Android imports). Modules point inward (doc 02): `auth`, `catalog`, `library`, `audius-adapter`, `search`, `admin`.
- **Provider boundary:** `TrackSource` interface (`search`, `getTrack`, `resolveStream`) — Audius implementation first; swappable to Jamendo/licensed/self-hosted without client contract churn (doc 02, 06, 11 M3).
- **Data boundaries:** backend never stores/transcodes/CDN/proxies audio bytes in prototype; never persists Audius stream URLs long-term (resolved per play, short-lived). Client stores metadata + external IDs (doc 01, 02, 06).
- **Catalog quality:** curated verified artists only; no global/unfiltered Audius search (doc 01, 06, 07, 12).

### Stack (doc 02; M0 actuals noted)

| Layer | Planned (doc 02) | M0 actual (2026-11-07) |
|---|---|---|
| Language | Kotlin 2.4.10 (K2) | **Kotlin 2.1.0** plugin (Gradle 8.10.2 wrapper in `celestial-backend/`) for reliable compile in this env — revisit/pin with toolchain stabilization |
| Framework | Ktor (REST, kotlinx.serialization) | Ktor **3.0.3** (Netty, content negotiation/JSON, status pages, call logging/default headers deps, auth/JWT deps) |
| Database | PostgreSQL + Flyway; Exposed or plain SQL | PostgreSQL + **Flyway 10.20.1** + **Exposed 0.57.0** (core/dao/jdbc/java-time) + **HikariCP 6.2.1** |
| Auth | JWT access + rotated refresh; Argon2id for upgraded passwords | JWT access + **rotated refresh** via `java-jwt` 4.4.0; device secret/refresh tokens stored as **SHA-256 hashes**; **Argon2id** (`argon2-jvm` 2.11) dep added for future password upgrade (not used in M0 routes) |
| DI | Koin or manual | Manual/minimal wiring in `Application.module` |
| Runtime | Docker Compose dev (Postgres + backend), CI/CD | `docker-compose.yml` (Postgres 16 + backend `app` profile, healthcheck), `Dockerfile`, `.env.example` present; **Docker not available in this env** (compose/Testcontainers not executed here) |
| Contract | OpenAPI source of truth; RFC 9457 errors | RFC 9457 `application/problem+json` implemented (`problem/`); **OpenAPI/contract tests not yet generated** (gap — M1/M3) |
| Build | — | JVM toolchain 21; `layout.buildDirectory` = `${user.home}/.celestial-build/celestial-backend` (Windows OneDrive lock workaround) |

### API contract highlights (doc 04)

- Base: `REST v1` under `/api/v1/*`. Cursor pagination (`next_cursor`, `limit`), `Idempotency-Key` for state-changing writes, ISO-8601 UTC timestamps. Auth via Bearer access token; anonymous device bootstrap per device credentials.
- **Auth:** `POST /auth/device/register`, `POST /auth/token/refresh`, `POST /auth/upgrade` (M2), `POST /auth/revoke`.
- **Catalog:** `GET /catalog/search`, `GET /catalog/tracks/{id}`, `GET /catalog/artists/{id}`, `GET /catalog/playback/{trackId}` (short-lived stream URL/redirect metadata; client/Media3 fetches direct — confirmed).
- **Library:** playlists CRUD, favourites toggle/list, recents list/record.
- **Sync:** `GET /sync/changes?since_version=`, `POST /sync/ack` (delta + tombstones + ack, optimistic version).
- **Ops:** `GET /health`, `GET /ready`, `GET /version`.

### Auth & security (doc 05)

- Anonymous device account on first launch; device identified by installation ID + device secret (Android Keystore on client). Optional upgrade/link merges anonymous data. **Linked account confirmed:** email + password first (Argon2id); Google Sign-In deferred.
- Access token short TTL; refresh token rotated, hashed, revocable. Per-device/IP limits on auth/search planned; Audius keys server-side only.
- No secrets/tokens/PII in logs; HTTPS. Audius terms: as-is/revocable, no ML training on content, **session-only caching, no offline audio downloads**; attribution as required.

### Audius integration & streaming (doc 06)

- Audius = prototype source for full-length streams (not previews). Caveats: no SLA, indie-heavy catalog, possible unofficial reuploads, 10 rps / 500k req/month free tier, session-scope caching only.
- Flow (server-side only): resolve healthy discovery nodes (`api.audius.co`) → backend proxies search/lookup through healthy node with `app_name=celestial` → filter curated allowlist + availability → per-play stream resolution (`/v1/tracks/{id}/stream`, 302 signed URL). **Client never calls Audius directly.**
- Playable only if `is_streamable && is_available && !is_delete` and artist in curated allowlist; else unavailable state. Negative-cache unavailable/deleted with short TTL.
- **Playback delivery confirmed:** resolve and return short-lived stream URL/redirect metadata for client/Media3 direct fetch (no backend proxy). Proxy deferred; revisit if Android/CORS/range issues appear.

### Sync & offline (doc 08)

- Syncs favourites/playlists/playlist items/recents (optional prefs later) via device registration + delta cursor (`sync_changes`/`version`), tombstones, `/sync/ack`, optimistic `version`/`updated_at` last-write-wins per entity.
- Offline = metadata only (Room/DataStore on Android, WorkManager sync); **no offline audio downloads** (Audius terms). Caps align with client.

### Phases — milestones M0–M3 (doc 11)

| Phase | Goal | Status (2026-11-07) | Key areas (per docs) | Acceptance (per docs) | Tests / verification |
|---|---|---|---|---|---|
| **M0 — Skeleton + auth** | Runnable Ktor + Postgres/Flyway + Docker Compose; anonymous device register; JWT access/refresh rotation; revoke; health/ready/version. | **Complete** | `celestial-backend/`; migrations; config/db/problem/auth/api modules | Compiles; health/version OK (smoke, no DB path); register→creds+tokens; refresh rotates+revokes old; revoke invalidates device refresh tokens; no secrets committed | `./gradlew.bat test --tests app.celestial.backend.AppSmokeTest` ✅ (passed). `AuthIntegrationTest` (Testcontainers Postgres) written; **skipped here (Docker unavailable)**. Docker compose not executed in this env. |
| **M1 — Catalog + Audius adapter** | `TrackSource` + Audius impl; discovery-node failover; curated allowlist; search/track/playback resolution; availability filter; negative-cache TTL; server-side budget. | **Awaiting "go"** | `audius-adapter`/`catalog`/`search` pkgs, curated seed (doc 12), catalog+playback routes, budget/rate component, OpenAPI | Only curated+available+streamable playable; client never calls Audius; 429+`Retry-After` on exhaustion; failover works | Planned: mocked adapter/HTTP unit tests, mock Audius integration, contract tests, budget/load smoke |
| **M2 — Library + sync** | Playlists/favourites/recents CRUD; delta sync + tombstones + ack; optimistic versions; upgrade/link (email+password) merge. | Pending | Migrations, `library`/`sync` routes, merge logic, idempotency | Anonymous→upgrade preserves data; delta/tombstones/ack correct; last-write-wins | Planned |
| **M3 — Search + hardening** | Postgres FTS/`pg_trgm`, ranking, quotas, observability, CI quality gates, swappable adapter readiness. | Pending | Search indexes/SQL, metrics/logging, Detekt/ktlint/CI, load/security tests | Search quality/perf; budget protected; health accurate; gates pass; no PII/token logs | Planned |

### M0 — what was done (2026-11-07)

- **Scaffolded `celestial-backend/`** (standalone Gradle, no Android changes): `build.gradle.kts`, `settings.gradle.kts`, `gradle.properties`, Gradle wrapper (`gradlew*`, `gradle/`), `.env.example`, `.gitignore`, `Dockerfile`, `docker-compose.yml` (Postgres 16-alpine + backend service on `app` profile, healthcheck).
- **Migrations:** `src/main/resources/db/migration/V1__m0_auth.sql` — `pgcrypto`, `users` (anonymous/linked fields), `devices` (installation_id unique, secret hash, revoked_at), `refresh_tokens` (hash unique, expires/revoked/replaced_by, indexes).
- **Config/DB:** `config/AppConfig.kt` (env-driven `PORT`, `DATABASE_*`, `JWT_*` incl. access 15m / refresh 30d defaults); `db/DatabaseFactory.kt` (Hikari + Flyway migrate + Exposed `Database.connect`).
- **Errors:** `problem/Problem.kt` — RFC 9457 `ProblemResponse` + `ApiException` + helpers (`unauthorized`, `conflict`, `validation`, `internal`).
- **Auth:** `auth/AuthModels.kt` (DTOs), `AuthTables.kt` (Exposed tables), `Security.kt` (SHA-256 hex hashing for device secret/refresh tokens), `JwtService.kt` (access/refresh JWT generation), `AuthService.kt` (register device+user+tokens, refresh rotation + revoke old, revoke by access token / device tokens).
- **API routes:** `api/HealthRoutes.kt` (`GET /health`, `/ready` w/ DB check, `/version` = `0.1.0-m0`); `api/AuthRoutes.kt` (`POST /api/v1/auth/device/register` → 201, `/token/refresh` → 200, `/revoke` via Bearer access token → 200).
- **App wiring:** `Application.kt` — Netty server, JSON (ignore unknown, encode defaults), `StatusPages` → `application/problem+json` for `ApiException`/unhandled, routing; `skipDb` flag for smoke tests.
- **Tests:** `AppSmokeTest` (Ktor `testApplication`, `skipDb=true`) covers `/health`, `/version`, `/ready`; `AuthIntegrationTest` (Testcontainers Postgres) written for register+refresh flow — **not executed in this env (Docker unavailable)**.
- **Not in M0 (by plan/docs):** `/auth/upgrade` (M2), catalog/Audius adapter (M1), library/sync (M2), OpenAPI generation/contract tests (M1/M3), enforced rate-limit/audit middleware, device-credential auth path beyond bootstrap tokens.

### Deviations, gaps, and environment notes

- Kotlin plugin **2.1.0** + Ktor **3.0.3** chosen for compile reliability here (doc 02 cites Kotlin 2.4.10 target) — pin/align when stabilized.
- Build dir moved out of OneDrive-synced workspace to avoid Windows file locks.
- **OpenAPI not yet generated** for implemented M0 endpoints — add with contract tests (doc 10/11).
- **Docker/Testcontainers unavailable in this environment:** `docker compose up` and `AuthIntegrationTest` not run here; verify locally.
- `/api/v1/auth/revoke` implemented as Bearer-access-token revoke of current device refresh tokens (minimal M0); refine scope/semantics with full session management as needed.

### Rate-limit / budget defaults (confirmed 2026-11-07; configurable, not hardcoded)

- **Global Audius/server budget:** 10 req/s, 500,000 req/month (shared upstream cap).
- **Throttling:** token-bucket/rate-limiter per scope; RFC 9457 `429` + `Retry-After`. Enforce global budget before upstream call; per-user + per-device both apply where identity known.
- **Per-device initial:** auth 10/min; search 30/min + burst 10; playback resolve 20/min + burst 5; library/sync writes 60/min.
- **Per-user initial (authenticated):** search 60/min + burst 15; playback resolve 40/min + burst 10; library/sync writes 120/min.
- **Config keys:** `RATE_LIMIT_*`, `AUDIUS_BUDGET_*` (env/config). Revisit after M3 load tests.

### Decisions log (backend)

- **2026-11-07 (confirmed):** `docs/backend/01–12` + PROGRESS authoritative; `docs/backend/00_BACKEND_CONCEPT.md` legacy/conflicting (on-device).
- **2026-11-07 (confirmed):** Backend at `celestial-backend/` (repo root, standalone Gradle); no `app/`/Android frontend changes.
- **2026-11-07 (confirmed):** Linked account = **email + password first** (Argon2id); Google Sign-In deferred.
- **2026-11-07 (confirmed):** Playback delivery = short-lived stream URL/redirect metadata for client/Media3 direct fetch (no backend proxy yet).
- **2026-11-07 (confirmed):** Conservative configurable rate-limit/budget defaults as above.

### Working agreements

- Treat confirmed user decisions in `docs/frontend/*` as requirements; do not silently substitute conventional app patterns.
- Ask before inventing behaviour marked **Open decision**.
- No fake/inert controls; no screenshots/videos standing in for interactive 3D.
- Build incrementally, run the app often; keep this status log updated as phases complete.
- Documented convention: keep implementation under `app/src/main/java/com/celestial/player/` (`ui/`, then later `scene/`, `playback/`, `domain/`, `data/` — see §3 of the implementation guide; adapt rather than impose).
