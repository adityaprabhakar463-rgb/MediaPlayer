# Celestial — Backend Concept (on-device)

**Status:** Concept. No implementation. UI contracts frozen. Backend means the on-device engine (library, playback, persistence, search), not a remote server.
**Date:** 2026-11-05 · **Scope:** Replace sample placeholders with real device media without changing screens, navigation, or signature controls.

---

## 1. Purpose (what and why)

Celestial already looks finished — it just pretends. This backend makes the pretense real: it finds the listener's music on the phone, remembers what they saved, and plays it reliably while the universe spins. Nothing here changes how the app looks; only where the facts come from. If the screen asks "what is playing?", the answer now comes from the device, not from a sample list.

## 2. Responsibilities

What this is: the four questions the app must answer, and who owns each answer.

| Question app must answer | Backend layer | Plain-language role |
|---|---|---|
| What music exists? | Library scanner + catalog | Inventory: what songs, albums and artists are really on the phone. |
| What is playing? | Playback service | Engine: the one place that knows if a song is playing, where it is, and what is next. |
| What did the user save? | Persistence (favourites, playlists, recents, settings) | Memory: hearts, lists and listening history survive restarts. |
| How do I find things? | Search (over the catalog) | Finder: matches typed words against real titles, artists, albums and playlists. |

## 3. Architecture (what and why)

What this is: who talks to whom, and where truth lives. UI never touches files or playback directly.

```
                          ┌─────────────────────────────────┐
                          │  Compose UI (screens, 3D,      │
                          │  mini planet, seek ring, UFO)   │
                          └───────────────┬─────────────────┘
                                          │ reads state, sends intents
                                          ▼
                          ┌─────────────────────────────────┐
                  ┌───────│  State holders (single truth)   │────────┐
                  │       │  PlayerUiState   Library state  │        │
                  │       └───────────────┬─────────────────┘        │
                  │                       │ intents                    │
                  │                       ▼                            │
                  │       ┌─────────────────────────────────┐        │
                  │       │  Domain interfaces              │        │
                  │       │  LibraryRepository  Search      │        │
                  │       │  PlaylistRepo       PlaybackEngine
                  │       └───────────────┬─────────────────┘        │
                  │                       │                            │
                  │                       ▼                            │
                  │     ┌──────────────────────────────────────────┐   │
                  └─────│  data / playback / persistence         │   │
                        │  MediaStoreScanner · Room DB ·         │   │
                        │  Media3 PlaybackService · SearchIndex  │   │
                        └──────────────────────────────────────────┘
                                  ▲                   ▲
                                  │ MediaStore        │ persisted tables
                                  │ (device files)    │ (on disk)
```

- `PlayerUiState` and the library state remain the one thing the UI reads. Replacements produce the same shapes; no new UI state appears.
- Playback lives in a foreground `PlaybackService` (Media3 session), so music survives navigation, configuration change, and the 3D scene being destroyed (docs 07 §7/§9, 10 §5).

Short interface sketch (concept only):

```kotlin
interface LibraryRepository { fun library(): Flow<LibrarySnapshot> } // tracks, albums, artists
interface PlaybackEngine {
    fun play(track: Track, queue: List<Track>); fun togglePlayPause()
    fun next(); fun previous(); fun seekTo(positionMs: Long)
    fun setShuffle(enabled: Boolean); fun cycleRepeatMode()
}
```

## 4. Components

What this is: the six pieces that do the work, what each one replaces, and what flows in and out.

| Component | One-line job | Replaces placeholder | Inputs → outputs |
|---|---|---|---|
| Library scanner | Read real audio files and metadata into the catalog. | `SampleLibrary` object | Device MediaStore → `LibrarySnapshot(tracks, albums, artists)` |
| Persistence | Keep favourites, playlists, recents and settings on disk. | `LibraryStateHolder` in-memory sets/lists | User actions → durable tables; reload after restart |
| Playback service | Actually decode and play audio, survive backgrounding. | `PlayerController` 500 ms tick loop | `Track`+queue → real position, buffering, queue state |
| Controller adapter | Turn service events into today's `PlayerUiState`/`LibraryStateHolder` shapes. | `PlayerController` direct UI wiring in `CelestialAppShell` | Service callbacks → `PlayerUiState` flow (unchanged fields) |
| Search | Answer text queries against the catalog. | Inline `filter { contains }` in `SearchDestinationScreen` | Query + category → matching tracks/albums/artists/playlists |
| Settings | Persist reduced-effects and other toggles. | `remember { mutableStateOf(false) }` in `MainActivity` | Toggle → persisted flag; restored on launch |

Formulas used by the playback adapter and ring:

> **Progress fraction** `p = positionMs / durationMs`.
> Symbols: `positionMs` = playback position in ms (≥0), `durationMs` = track length in ms (>0 when known). Range: `p ∈ [0,1]`.
> Edge case: if `durationMs = 0/unknown`, `p = 0` and the ring stays dim; seeking is disabled until prepared.
> User meaning: the seek ring fills exactly as the song plays, and resets when the track changes.

> **Seek conversion** `seekMs = f × durationMs`, where `f = angleRad / (2π)`, `angleRad = atan2(dy, dx) + π/2` normalised into `[0, 2π)`.
> Symbols: `dx, dy` = touch offset from ring centre (px), `f` = fraction of full circle. Range: `f ∈ [0,1]`.
> Edge case: duration unknown → ignore taps; drag state is local, commit on release.
> User meaning: tapping the top of the ring seeks to the start, the right side to a quarter, and so on.

## 5. Data model (what and why)

What this is: the shape of the music world, reusing today's models where possible.

```text
Artist 1 ─── * Album      (album.artistId/name)
Album  1 ─── * Track      (track.albumId, trackIndex)
Track  * ─── * Playlist   (ordered PlaylistTrack junction)
Favourite  ── Track       (flag/table keyed by TrackId)
RecentPlay ── Track       (trackId + playedAtMs)
QueueState ── Tracks      (ordered ids + index + shuffle/repeat)
Settings   ── flags       (reducedEffects, firstLaunchDone, …)
```

| Entity | Reuse today (`domain/model/MediaModels.kt`) | New / changed |
|---|---|---|
| `Track` | `TrackId, title, artistId, albumId, trackIndex, durationMs` — kept, ids now MediaStore ids | `contentUri` (playable address), `dateAdded`, `artworkKey`, missing-file marker |
| `Album` | `AlbumId, title, artistId, colorSeed, trackIds` — kept | `artworkUri`, `year` (nullable), missing-album marker |
| `Artist` | `ArtistId, name, albumIds, trackIds` — kept | fallback name `"Unknown artist"` for empty tags |
| `Playlist` | `PlaylistId, name, trackIds` — kept | `createdAtMs`, `updatedAtMs`; ordered junction with position |
| `RecentPlay` | `trackId + playedAtMs` — kept | persisted table, deduplicated, cap 100 (today's in-code cap), prune > cap |
| `Favourite` | `Set<TrackId>` in holder | persisted set/table, keyed by `TrackId` |
| `QueueState` | `List<Track> + queueIndex + shuffle + repeat` in `PlayerUiState` | persisted (queue ids, index, position, mode) for process-death restore |
| `Settings` | — | `reducedEffects`, `firstLaunchDone`, `lastScanMs` |

No product behaviour is invented here — fallbacks shown (`Unknown artist`) are backend bookkeeping; whether the UI shows them is an open decision (§10).

## 6. Key flows (what and why)

What this is: what happens, step by step, when the listener does the obvious things.

**First launch.** 1) Open app. 2) If `firstLaunchDone` is false, play `UniverseFormationSequence`; otherwise go straight to the universe (doc 05 §4). 3) In parallel, request `READ_MEDIA_AUDIO` (API 33+) — see §9/§10 for ordering vs. the intro. 4) When granted, run scan (§ scan flow). 5) Set `firstLaunchDone = true` only after the scene hands off successfully. 6) Settings → "Replay intro" sets `showFormation = true` again (today's behaviour, kept).

**Scan library.** 1) Query `MediaStore.Audio` (projection: id, title, artist, album, duration, date_added, data/uri). 2) Map rows to `Track`/`Album`/`Artist`; group by album/artist. 3) Emit `LibrarySnapshot` to state holders; all destinations re-render. 4) Diff against persisted tables: drop entries whose files vanished. 5) Store `lastScanMs`.

**Play a track.** 1) UI calls `controller.play(track, queue)` (unchanged signature). 2) Service prepares the `contentUri`, reports buffering, then playing. 3) Adapter emits `PlayerUiState` with real `positionMs`/`durationMs` (progress fraction per §4). 4) On transition, `recordPlayed(track)` once at audible start. 5) Mini planet, Now Playing, notification and lock-screen controls all read the same `PlayerUiState`. 6) Previous uses today's rule: if `positionMs > 3_000` restart, else step back (keep, unless §10 changes it).

**App killed while playing.** 1) Service writes `QueueState` (track, index, position, shuffle, repeat) on change. 2) Process dies. 3) Next launch: restore queue and paused position; do **not** auto-resume (see §10 — suggested default is paused restore). 4) UI state shows the same track, progress `p` as before.

**Add to playlist.** 1) User taps a playlist action for a track (requires a UI entry point — none exists today; see §10). 2) Backend appends `TrackId` to the ordered junction at the tail. 3) Recompute track count; playlist detail updates. 4) If the track is missing on disk, still add but mark missing; playback skips it.

**Search.** 1) User types ≥1 character. 2) After 280 ms debounce, query the catalog for the selected category (Songs/Albums/Artists/Playlists). 3) Return substring matches on title/artist/album/playlist name. 4) Tap: song plays, album/artist/playlist navigates (today's callbacks, kept). 5) Taps (≥2 chars) optionally recorded as recent searches — persisted or session-only per §10.

## 7. Placeholder-to-real swap (what and why)

What this is: each placeholder, its real replacement, and the UI change needed. Every row must end at "none" — no screen changes.

| Current placeholder | Real replacement | UI change required |
|---|---|---|
| `SampleLibrary` (27 hardcoded tracks) | MediaStore scanner + catalog | none |
| `PlayerController` tick loop (fake position) | Media3 `PlaybackService` + adapter to same `PlayerUiState` fields | none |
| `LibraryStateHolder` in-memory favourites/playlists/recents | Persisted Room tables behind the same holder API | none |
| `remember` in `MainActivity` for reduced effects; `remember` `showFormation` | Persisted `Settings` (DataStore/Preferences + flag) | none |
| Search `filter { contains }` + `remember` recents (cap 6) | Search over catalog; recents persisted per §10 | none |
| `colorSeed` gradient artwork (`GlassArtwork`) | Real embedded artwork bitmaps; fall back to today's gradient when absent | none (same composable signature, image inside) |
| `UniversePlaceholderScreen`, hint chip (already unused) | `CelestialUniverseScene` (in place; placeholder deleted) | none (deletion only) |
| Seed recents (8 fake `playedAtMs`) | Empty until real plays are recorded | none |

## 8. Phases (what and why)

What this is: the order to build it so the app always runs.

| # | Phase | Done when |
|---|---|---|
| A | Permissions + read-only scan + catalog | All lists show real device music; sample data unreachable behind flag. |
| B | Media3 service + adapter to existing `PlayerUiState` | Play/pause/next/previous/seek/shuffle/repeat produce real audio; ring follows real `positionMs`. |
| C | Persistence (favourites, playlists, recents, settings) | Survive restart; first-launch flag persisted. |
| D | Search over real catalog + artwork (embedded) | Search categories and artwork reflect device files; fallbacks unchanged. |
| E | Edge-case hardening (missing files, notification, audio focus, process restore) | §9 risks handled; no fake controls. |
| F | Queue/state restore polish + perf (large libraries, paging, cache) | 10k-track library scrolls at 60 FPS; resume after kill restores queue. |

## 9. Risks and edge cases (what and why)

What this is: what can go wrong, and the backend's stance.

- **Permission denial.** 1) Show empty states with explanation and a "try again" that re-requests (today: no denied state exists; §10 decision). 2) Never crash; catalog stays empty. 4. Never invent music.
- **Missing / deleted files.** Track stays in playlists/recents but marked missing; playback skips it and surfaces the existing error surface. Prune on next scan diff.
- **Unknown metadata.** Empty tags → `"Unknown artist"`, `"Unknown album"`, title from file name; never use the real tag of another track. See §10 for display wording.
- **Background service killed.** Persist `QueueState` on change; on relaunch restore paused at saved `positionMs`. Do not auto-play.
- **Duration unknown before prepared.** `PlayerUiState.durationMs = 0` → `p = 0`, ring dim, seek disabled, duration text `"--:--"`. Once `STATE_READY`, fill `durationMs` and enable seek.
- **Audio focus / headset / Bluetooth.** Pause on noisy/focus loss; Media3 session keeps notification and lock-screen controls in sync (doc 07 §9). Adapter must forward external play/pause so UFO/ring stay true.
- **Duplicate tracks / multi-disc / compilations.** Key by media id, not title; album grouping by album id first, name second. Duplicates in a playlist are allowed (see §10) and kept ordered.

## 10. Open decisions (what and why)

What this is: product choices I could not determine from docs 01–11 or code. Defaults are suggestions only — answer before Phase A.

| Decision | Options | Suggested default | Why it matters |
|---|---|---|---|
| Audio sources | MediaStore all audio vs. only Music folders; include ringtones/notifications/podcasts? | All `MediaStore.Audio`, exclude <30 s and ringtones | Determines what "All Songs" means; hides junk or surprises. |
| Permission timing vs. intro | Request during Big Bang vs. after first universe reveal | After scene loads, as a glass prompt | Blocking the cinematic intro with a dialog breaks doc 05 feel. |
| Denied / empty experience | Silent empty lists vs. explainer + retry | Explainer + Retry (uses `GlassEmptyState`) | First-run trust; today no denied state exists. |
| Queue replace vs. enqueue | Tap replaces queue (today) vs. add to queue | Keep replace-on-tap; add explicit queue actions later | Affects Now Playing expectations and code shape. |
| `previous` threshold (3 000 ms) | Keep vs. change | Keep 3 000 ms | Every player rule differs here; current code is the only spec. |
| Repeat Off at queue end | Stay on last track (today's `moveTo`) vs. stop | Stop (pause at end) | Current behaviour repeats the last track — arguably wrong. |
| Auto-resume after kill | Paused restore vs. resume playing | Paused restore | Auto-play after relaunch surprises users. |
| Unknown metadata labels | `"Unknown artist/album"` vs. hide rows | Use labels, never hide | Real libraries have such rows; hiding loses music. |
| Recents policy | Record at start vs. after 10 s of play; clear-all? | Record at start (matches `recordPlayed`); add Clear in Settings later | Skipping a song shouldn't write history forever; UI to clear doesn't exist. |
| Search matching | Substring (today) vs. prefix vs. fuzzy; min chars 1 vs. 2 | Keep substring, min 1 (today's filter); recents at ≥2 | Typing feel; large libraries need an index cutoff. |
| Recent searches / suggested | Persist recents vs. session-only; suggested = first 5 (today) | Persist recents (cap 6); suggested = top 5 by recent play | Today both are guesses; decide before persistence is built. |
| Sorting | Title/Artist/Longest (today) vs. date added, album | Keep 3 options; store choice in Settings | User asked for it, docs call it open. |
| Playlist CRUD scope | Create + add only vs. rename/delete/reorder; duplicates allowed? | Create + add + delete + rename; duplicates allowed, order kept | No add/remove/reorder UI exists today — backend can support all, UI decides. |
| Add-to-playlist UI entry | New overflow action (needs UI) vs. backend-only | Add "Add to playlist" to track overflow (small UI change, needs approval) | The requested flow has **no** current affordance; without it the backend feature is dead code. |
| Volume + sleep timer | Show now vs. hide until built; doc 07 lists both, code shows neither | Hide both until Phase B+ implements volume (basic), defer sleep timer | No fake controls allowed; adding now forces a UI change. |
| Artwork | Embedded only vs. folder.jpg; cache size | Embedded via MediaStore; OS thumbnail cache, no custom cache in v1 | Memory/perf on large libraries. |
| Large-library strategy | Load all vs. paged/Lazy paging | Emit full snapshot; lazily paged lists (today's `LazyColumn` does paging already) | 10k rows must stay 60 FPS (doc 10 §3). |

---

### Later (explicitly deferred)

Equalizer, lyrics, sleep timer, Android Auto, cloud sync, streaming, gapless/ crossfade controls, widgets, social sharing.

---

*Skills consulted: none — backend concept task; the skill map in `docs/frontend/02` covers UI/design tasks only, and no `~/.agents/skills/` directory exists on this machine (checked). No skill was claimed.*
