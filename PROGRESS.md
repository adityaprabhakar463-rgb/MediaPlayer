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
| Gradle project scaffold (`:app`, AGP 9.5, Kotlin 2.4.10, Compose BOM 2026.09) | Complete |
| Phase 1 — Design foundation | Complete |
| Phase 2 — Destination UI without 3D | **In progress — all code written, `compileDebugKotlin` failing (12 errors, list below) |
| Phase 3+ | Not started |
| 3D renderer dependency | Not yet chosen/integrated |
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

### Known compile errors (`:app:compileDebugKotlin`, 12) — next session start point
1. `data/SampleLibrary.kt:15–19` — `Artist` constructor calls miss `albumIds`/`trackIds` params (artist seeds use 2-arg call)
2. `data/SampleLibrary.kt:107,123` — `==` between `ArtistId` and `String` (compare against `ArtistId(...)` value class)
3. `ui/CelestialAppShell.kt:105` — `onTrackPlayed` receives `Track`, caller passes wrong type (record path)
4. `ui/CelestialAppShell.kt:168` — `Text` unresolved in `PlanetHintChip` (missing material3 Text import)
5. `ui/screens/artists/ArtistDetailScreen.kt:15,16` — two conflicting `Alignment` imports (both `androidx.compose.foundation.layout` and `androidx.compose.ui`)
6. `ui/screens/artists/ArtistDetailScreen.kt:48` — `PaddingValues` import missing in `ArtistDetailScreen`
7. `ui/screens/playlists/PlaylistDetailScreen.kt:110` — `QueueMusic` unresolved (autoMirrored import missing in that file)
8. `ui/screens/playlists/PlaylistDetailScreen.kt:119` — `Text` unresolved
9. `ui/screens/search/SearchCategoryTabs.kt:33` — `Text` unresolved (missing import)
10. `ui/screens/search/SearchDestinationScreen.kt:115ff` — `items(items=…)` wrong overload usage + `TextFieldValue(recent)` (`recent` is Int index misuse — lambda param typed as index; use `{ recent -> }` not destructured wrong arg) + `Icon` unresolved import
11. `GlassSettingsPanel.kt` — verify `Icons.Filled.Close` import + remove unused `Box` import (minor)
12. `universe/UniversePlaceholderScreen.kt` — review whole file after other fixes (`displayLarge` may be typography default absent from `CelestialTypography` — falls back silently; confirm acceptable)

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

### Phase 2 — Destination UI without 3D dependency ⬜ Next
- [ ] Playlists glass list
- [ ] Search glass interface
- [ ] All Songs, Albums, Artists, Favourites, Recently Played layouts
- [ ] Settings glass panel
- [ ] Persistent player placeholder wired to a single state model

### Phase 3 — 3D scene foundation ⬜
- [ ] Integrate and validate renderer (confirm choice against dependencies/support first)
- [ ] Starfield and central star
- [ ] Destination planets and camera controller
- [ ] Free rotation, pinch zoom, selection
- [ ] Planet labels on approach/selection only

### Phase 4 — Navigation and continuity ⬜
- [ ] Planet approach transition
- [ ] Destination screen integration
- [ ] Reverse camera navigation
- [ ] Restore previous camera orientation
- [ ] Persistent mini music planet stays visible

### Phase 5 — Now Playing signature controls ⬜
- [ ] Album artwork planet
- [ ] Playback-driven seek ring
- [ ] UFO play/pause
- [ ] Asteroid previous/next
- [ ] Standard glass queue, shuffle, repeat, favourite, volume, sleep timer
- [ ] Synchronised with actual Media3 state

### Phase 6 — First-launch formation ⬜
- [ ] Big Bang → dust/nebula → star → planets, palette transition
- [ ] Seamless handoff to the same interactive scene
- [ ] Skip, persist completion, replay from Settings

### Phase 7 — Refinement and quality ⬜
- [ ] Motion polish
- [ ] Reduced-effects mode
- [ ] Accessibility (TalkBack, touch targets)
- [ ] Responsive layouts
- [ ] Performance profiling
- [ ] Device testing and visual review

---

## Working agreements
- Treat confirmed user decisions in `docs/frontend/*` as requirements; do not silently substitute conventional app patterns.
- Ask before inventing behaviour marked **Open decision**.
- No fake/inert controls; no screenshots/videos standing in for interactive 3D.
- Build incrementally, run the app often; keep this status log updated as phases complete.
- Documented convention: keep implementation under `app/src/main/java/com/celestial/player/` (`ui/`, then later `scene/`, `playback/`, `domain/`, `data/` — see §3 of the implementation guide; adapt rather than impose).
