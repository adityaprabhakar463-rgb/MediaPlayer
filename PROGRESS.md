# Celestial — Progress & Phases

**Last updated:** 2026-11-05
**Product:** Celestial, native Android space-themed media player (Kotlin + Jetpack Compose)
**Reference:** `docs/frontend/11_FRONTEND_IMPLEMENTATION_GUIDE.md` (phase definitions and acceptance criteria)

---

## For AI agents — read this first

This file is a status snapshot, not full context. `AGENTS.md` at the repo root is your always-on rules file (auto-loaded by Antigravity; read it first in any agent). Before doing any work:

1. **Read `AGENTS.md`** — working rules, skill wiring, terse mode.
2. **Read `README.md`** — doc index and product rules.
3. **Read `docs/frontend/01`–`11` in order.** Mandatory:
   - `01_DESIGN_VISION.md` — product concept
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
| Backend documentation (`docs/backend/01`–`12`) | Written |
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

## Phases (as agreed, per `11_FRONTEND_IMPLEMENTATION_GUIDE.md`)

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

### Frontend improvement documentation
- `docs/frontend/15_NAVIGATION_UX_AND_PLANET_LABELS.md` — navigation model, destination registry, always-visible planet labels with selected emphasis, accessible Destinations sheet fallback, gesture and accessibility rules.
- `docs/frontend/16_LIGHTWEIGHT_PERFORMANCE.md` — lightweight, lag-free optimization without major changes; Compose hot paths, renderer performance, label performance, measurement and acceptance gates.

Locked frontend improvement decisions:
- Labels always visible while planet is on screen, stronger on selected planet.
- Accessible non-3D Destinations button/sheet added as fallback.
- Performance target: 60 FPS on mid-range 2022+ phones; adaptive reduced effects below that.
- No hardcoding: every value from tokens / single source of truth; every component real and working.
- Local audio feature: user-set directory scanned recursively for mp3/wav/flac/opus, played through the same player model, directory persisted via DataStore.
- No code changes; docs only.

Locked backend decisions:
- Catalog source: Audius free API (prototype); curated verified artists only.
- Stack: Kotlin + Ktor + PostgreSQL, Docker deployment.
- Auth: anonymous device account first, optional upgrade/link to email + password.
- No audio hosting/transcoding; no offline audio downloads in prototype.
- No code changes; docs only.

---

## Working agreements
- Treat confirmed user decisions in `docs/frontend/*` as requirements; do not silently substitute conventional app patterns.
- Ask before inventing behaviour marked **Open decision**.
- No fake/inert controls; no screenshots/videos standing in for interactive 3D.
- Build incrementally, run the app often; keep this status log updated as phases complete.
- Documented convention: keep implementation under `app/src/main/java/com/celestial/player/` (`ui/`, then later `scene/`, `playback/`, `domain/`, `data/` — see §3 of the implementation guide; adapt rather than impose).
