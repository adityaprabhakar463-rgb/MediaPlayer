# Backend 13 — Client Integration and Local Runtime

**Status:** first vertical slice wired (2026-11-06)
**Client:** `app/…/data/remote/CelestialApiClient.kt`, `LibraryStateHolder.kt`, `PlayerController.kt`
**Server:** `backend/…/Application.kt`, `auth/`, `catalog/`, `library/`, `sync/`

## What is actually connected

| Area | Client behaviour | Server behaviour |
|---|---|---|
| Device identity | Generates one installation id + secret per install, stores it in app-private preferences | Registers once per installation id; repeat registration returns the same account |
| Auth | Stores access/refresh tokens; refreshes once on `401`; re-registers if refresh is rejected | HMAC-signed access + refresh JWTs, single-use refresh rotation, `401` as `application/problem+json` |
| Catalog | Fetches `/api/v1/catalog` on sync and replaces the offline sample library | Curated prototype catalog mirrors the app's sample catalog ids/metadata |
| Search | Debounced request to `/api/v1/catalog/search`; falls back to local filter | Case-insensitive search over title, artist and album |
| Playlists | List/create/rename/delete/add/remove with optimistic UI | Per-user list, create, patch, delete |
| Favourites | Optimistic toggle; pending operations retried on next sync | Per-user set with create/delete endpoints |
| Recents | Local reorder immediately; POST to backend | Per-user deduplicated list, capped at 100 |
| Playback | Resolves `/api/v1/catalog/playback/{trackId}`, then plays the URL through Media3/ExoPlayer; surfaces provider and error state in Now Playing | Returns the Audius stream endpoint for curated tracks; no fake URLs |
| Audius | Never calls Audius directly | Curated catalog maps Celestial ids to Audius ids; stream URLs are resolved per play request |
| Connection UI | Settings shows server status, endpoint, and a manual **Sync now** action; failed sync retries with backoff | `/health`, `/ready`, `/version` |

All authenticated requests converge on one path in `CelestialApiClient`:

1. use the stored access token;
2. on `401`, refresh through `/api/v1/auth/token/refresh` (single-flight mutex);
3. if refresh is rejected, re-register the same device identity.

## Running locally

```bash
# from the repository root
./gradlew :backend:run            # listens on :8080 (override with PORT)
```

The default `BuildConfig.CELESTIAL_API_BASE_URL` is `http://127.0.0.1:8080`. The Gradle
`adbReverse` task runs automatically before debug builds and maps the device port to the host, so
this works on emulators and USB devices without relying on the host firewall or `10.0.2.2`.
Physical device over Wi-Fi (no ADB tunnel):

```bash
./gradlew :app:installDebug -PcelestialApiBaseUrl=http://<your-lan-ip>:8080
```

If a device cannot use ADB reverse, run it manually:

```bash
adb reverse tcp:8080 tcp:8080
```

Cleartext HTTP is enabled for local development. Production must use HTTPS and a
`network_security_config` that disables cleartext, as described in `05_AUTH_AND_SECURITY.md`.

## Verification performed

- `:app:testDebugUnitTest`, `:backend:test` and `:app:assembleDebug` pass.
- The debug APK was installed on an API 37 emulator. The app reached the backend over the ADB
  reverse tunnel; device registration succeeded and the persisted session contained access,
  refresh, user and installation ids.
- `GET /api/v1/catalog/playback/{trackId}` returns a real Audius stream endpoint for every curated
  track (`provider: "audius"`). The endpoint was checked with an authenticated request and a ranged
  audio request (`206 audio/mpeg`).
- Real playback was confirmed on the emulator: `dumpsys audio` reports the app's `AudioTrack` as
  `state:started` with `usage=USAGE_MEDIA`, `content=CONTENT_TYPE_MUSIC`, and 48 kHz output. The
  seek ring advanced across a full track with no `PlaybackException`, and queue completion advanced
  only on `Player.STATE_ENDED`. Settings showed `Connected · http://127.0.0.1:8080` and `Online`.
- A missing backend or unreachable host is handled by the offline fallback; the UI keeps the local
  sample library instead of blocking, and retries the connection with backoff.

## Sync semantics

- The UI never blocks on the network: local sample data is visible immediately.
- A successful sync replaces catalog and library snapshots with the server state.
- Local playlists created while offline (ids prefixed `local-`) are uploaded on the next sync.
- Pending favourites and recent plays are retried; failed requests do not discard local intent.
- Failed sync keeps the last known good state and reports `Offline · local library`.

## Security notes

- Tokens and the device secret live in app-private preferences for this prototype. Move them to
  Android Keystore-backed storage before shipping (`05_AUTH_AND_SECURITY.md`).
- Tokens are never logged. Server logs contain no token values.
- The development JWT secret has a fallback so local runs work; set `CELESTIAL_JWT_SECRET` in any
  shared or deployed environment.

## Known gaps / next milestones

1. **Persistence:** `LibraryStore` is in-memory. `DatabaseFactory`, Flyway and Docker Compose exist
   but the routes do not use them yet. Restarting the backend resets accounts and libraries.
2. **Catalog operations:** the Audius catalog is a manually curated set of 7 artists and 8 albums.
   Refreshing it, verifying removed tracks and running the allowlist workflow are still manual.
3. **Background playback:** Media3 plays in the foreground. A `MediaSessionService`, notification
   controls and audio-focus loss handling for calls are not wired yet.
4. **Delta sync:** `/api/v1/sync/changes` and `/api/v1/sync/ack` are implemented server-side but the
   Android client performs full snapshot sync.
5. **Hardening:** per-user rate limits, OpenAPI generation, Postgres FTS and contract tests against a
   running PostgreSQL instance remain open. The Audius shared budget is not throttled yet.
