# Backend 01 — Vision and Scope

## Product statement

Celestial backend serves the Celestial Android music player. It provides catalog search and playback resolution through the Audius open music API prototype, user accounts, and cross-device sync of favourites, playlists, and recents. The backend does not host or transcode audio in the prototype; it resolves stream URLs from Audius discovery nodes and caches metadata only.

## Goals

- Let users play tracks from Audius through the Celestial Android app.
- Let users sign in anonymously per device and optionally upgrade/link to an email+password account.
- Sync favourites, playlists, and recently played across devices for signed-in users.
- Keep a curated catalog of verified artists only (no open global search).
- Provide a clean provider-adapter boundary so Audius can be swapped for Jamendo, a licensed catalog, or self-hosted uploads later.

## Non-goals (prototype)

- Audio file storage, transcoding, HLS packaging, CDN, or DRM.
- Mainstream major-label catalog (Spotify/Apple/Deezer full streaming).
- Social features, radio, recommendations, or admin upload tooling.
- Offline audio downloads (Audius terms restrict caching to session scope).

## Locked decisions

- Catalog source: Audius free API (prototype).
- Stack: Kotlin + Ktor + PostgreSQL, Docker deployment.
- Auth: anonymous device account first, optional upgrade/link to email + password.
- Catalog quality: curated verified artists only.
- No code changes in this task — documentation only.

## Open decisions

- Long-term catalog migration path (licensed catalog vs self-hosted uploads).
- Exact rate-limit budget sharing across devices/users for the Audius free tier.
- Whether email+password or Google Sign-In is the first linked-account option.
