# 17 — Local Library and Folder Model

**Status:** Implemented 2026-11-11.
**Direction change:** Celestial is an offline, local-folder music player. The Audius streaming
backend and all network playback code were removed. The 3D universe, planets, glass UI, motion and
Now Playing design remain unchanged.

## 1. Confirmed decisions

- The library is **one user-chosen folder**, scanned **recursively**.
- Folder access uses the Android Storage Access Framework (`ACTION_OPEN_DOCUMENT_TREE`).
  The read permission is persisted; no broad storage permission is requested.
- **No network dependency.** There is no account, sync, streaming, API key or backend server.
- The universe keeps its planet navigation. **Albums and Artists were pruned**, leaving five planets: Playlists, All Songs, Search, Favourites and Recently Played. Folder browsing is not a planet.
- Local persistence uses **Room** for tracks, playlists, favourites and recents.
- Playback uses a **Media3 `MediaSessionService`** so audio continues in the background with a
  media notification and lockscreen/headset controls.
- The folder invitation appears **after the Big Bang formation sequence**. It is skippable and can
  be reopened from Settings.

## 2. Folder access

- Picker: `ActivityResultContracts.OpenDocumentTree`.
- After selection: `ContentResolver.takePersistableUriPermission(..., FLAG_GRANT_READ_URI_PERMISSION)`.
- The tree URI and display name are stored in `celestial_prefs`.
- On launch, if a folder is saved, the app re-indexes it. If the persisted grant was revoked, the
  library reports "Folder access was lost" and asks the user to choose the folder again.
- Settings → Library exposes the folder name, current status, indexed track count, "Change" and
  "Rescan". The folder can be changed without losing the app's other settings.

## 3. Scanning

Implementation: `data/local/FolderMediaScanner.kt`.

- Walks `DocumentsContract` children breadth-first, skipping hidden files and folders.
- Accepts `audio/*` MIME types plus common extensions: mp3, m4a, m4b, aac, flac, ogg, oga, opus,
  wav, wma, mka, aiff, aif, amr, mid, midi.
- Reads tags with `MediaMetadataRetriever`: title, artist/album artist, album, duration, track
  number and embedded album art.
- Missing tags fall back to the filename and parent folder:
  - `01 - Artist - Title.mp3` → artist "Artist", title "Title".
  - A leading number (`01`, `01.`, `01_`) is also used as the track index when the tag has none.
  - No artist tag → "Unknown Artist"; no album tag → parent folder name or "Unknown Album".
- Embedded artwork is extracted once to `filesDir/artwork/<sha1>.jpg`; identical images are shared
  across tracks. Orphaned artwork is deleted after a scan.
- Track IDs are SAF document URIs, which stay stable across rescans.
- Album/artist IDs are deterministic hashes of the normalised names, so regrouping after a rescan
  does not break favourites, playlists or recents.
- Progress is reported to the UI every ten files (`LibraryScanPill`).
- The scan is cancellable between directories and between files. If the root folder itself cannot
  be read (revoked grant, unplugged volume), the scan fails and the existing index is kept instead
  of being replaced with an empty library.

## 4. Persistence

Database: `data/db/CelestialDatabase.kt` (Room, version 1).

| Table | Purpose |
|---|---|
| `tracks` | Indexed audio files and their metadata |
| `playlists` | User-created playlists |
| `playlist_tracks` | Ordered playlist membership |
| `favourites` | Favourited track IDs |
| `recents` | Last-played timestamps (trimmed to 100) |

A rescan replaces the track index but preserves favourites, playlist membership and recents for
files that still exist. Rows pointing at deleted files are pruned. This is covered by
`LibraryDatabaseTest`.

## 5. Refresh policy

- Full rescan on launch when a folder is saved.
- Manual rescan from Settings, the Playlists status banner, or the All Songs empty state.
- No background file observer. A future increment can index only changed documents, but the current
  full scan is deterministic and runs off the main thread.
- Library, playback and navigation state live in `CelestialAppViewModel`, so rotating the device
  does not restart the scan, reset navigation or reconnect playback.

## 6. Screen mapping

No new destinations were added. The five remaining planets keep their meaning:

| Planet | Local behaviour |
|---|---|
| Playlists | User playlists stored in Room |
| All Songs | Every indexed track; empty state offers folder selection/rescan |
| Search | Instant search over the local index (title, artist, album, folder path) |
| Favourites | Room-backed favourites |
| Recently Played | Room-backed recents |

Folder selection also appears as a one-time glass prompt over the universe after formation. If the
user chooses "Later", the universe remains usable and All Songs shows a "Choose folder" action.

## 7. Playback

- `playback/PlaybackService.kt` owns `ExoPlayer` + `MediaSession` and provides the media
  notification, audio focus and noisy-headset handling.
- `playback/PlayerController.kt` is the Compose-facing façade over a `MediaController`. It mirrors
  the service queue and restores the current track after Activity recreation.
- `playback/PlayerUiState.kt` is still the single playback state model used by the existing Now
  Playing controls.
- Audio attributes: music content, media usage, audio focus handled by Media3.
- Files are played directly from their SAF URI; nothing is copied or transcoded.
- Removing the library folder stops playback and clears the queue, because the SAF URIs are no
  longer valid.

## 8. Edge cases

| Case | Behaviour |
|---|---|
| No folder selected | Folder prompt after formation; empty states point to folder selection |
| Folder permission revoked | "Folder access was lost", with Settings → Music folder to re-grant |
| Root folder unreadable at scan start | Scan fails with an error; the previous index stays intact |
| Folder removed while playing | Playback stops and the queue clears |
| Empty folder | Library shows "No audio files" and offers Rescan |
| Untagged files | Filename/parent-folder fallback; "Unknown Artist/Album" |
| File deleted between scans | Track leaves the index; favourites/recents/playlists are pruned |
| Folder changed mid-scan | Old scan is cancelled by token; only the newest result is committed |
| Missing embedded art | Existing token-derived glass artwork is used |
| Notification permission denied | Playback continues; notification may be hidden |

## 9. Accessibility and performance

- Folder prompt, status pill and empty states have text descriptions and are reachable by TalkBack.
- Library scanning runs on `Dispatchers.IO`; Room flows update Compose state on the main thread.
- Lists use stable keys (`track.id`) and the existing `LazyColumn`/grid patterns.
- Embedded art is decoded through Coil with its own cache; the scanner never decodes full-size
  bitmaps for every row.
- Reduced-effects mode still applies to the universe and Now Playing.

## 10. Known gaps / open decisions

- No folder-tree browser yet; folders are searchable but only All Songs lists content.
- Full rescan on each launch; incremental indexing is not implemented.
- No support for editing tags or artwork.
- No sleep timer, equaliser or playback speed in this migration.
- `mid`/`midi` are indexed because providers may report them as audio, but device codec support
  varies.
