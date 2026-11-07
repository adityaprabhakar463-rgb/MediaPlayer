# Backend 06 — Audius Integration and Streaming

## Why Audius

Only free option found that returns actual full-length stream URLs. Spotify/Deezer/iTunes give metadata plus 30-second previews at best; Audius hosts recordings under artist-granted terms.

## Caveats

- Community-operated discovery nodes; no SLA; API "as is".
- Catalog is indie/electronic heavy; mainstream artists like Navaan Sandhu are not reliably present.
- Some uploads are unofficial reuploads; curated allowlist mitigates quality/rights risk.
- Terms restrict caching to session scope; no offline audio downloads.
- Free tier: 10 requests/second, 500,000 requests/month; shared budget managed server-side.

## Integration flow

1. Backend fetches healthy discovery nodes from `https://api.audius.co` (returns node hostnames).
2. Client requests search/playback through backend, never Audius directly.
3. Backend calls a healthy node: `/v1/tracks/search?query=...&app_name=celestial`, filters to curated artists, applies availability flags (`is_streamable`, `is_available`, `is_delete`).
4. For play, backend calls `/v1/tracks/{id}/stream?app_name=celestial`; Audius returns 302 to a signed creator-node stream URL.
5. Backend either proxies the stream or returns the 302 URL to the client for direct Media3 playback.
6. Stream URL is short-lived; resolved per play, never persisted.
7. Negative results (deleted/unavailable/unstreamable) cached with short TTL; retried on next request.

## Availability filtering

A track is playable only when:
- `is_streamable == true`
- `is_available == true`
- `is_delete == false`
- artist is in curated allowlist

Otherwise client shows "unavailable" state; no broken player.

## Adapter boundary

`TrackSource.search`, `TrackSource.getTrack`, `TrackSource.resolveStream`. Audius implementation first; Jamendo/licensed/self-hosted can be added behind the same interface.

## Open

- Proxy vs redirect for stream URLs (decide after measuring latency and CORS on Android).
- Rate-limit budget allocation per user vs global.
