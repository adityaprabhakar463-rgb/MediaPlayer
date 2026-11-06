# Backend 05 — Auth and Security

## Account model

- Anonymous device account created on first app launch; no credentials required.
- Device identified by installation ID + device secret stored in Android Keystore.
- Optional upgrade: link email + password (Argon2id) or Google Sign-In (later) to the anonymous device; favourites/playlists/recents merge into the durable account.
- JWT access token (short-lived) + refresh token (rotated, hashed storage).

## Token lifecycle

- Access token: short TTL (minutes).
- Refresh token: longer TTL, single-use rotation, revocation on logout/password change.
- Device sessions listed and revocable from Settings.

## Rate limits and abuse

- Per-device and per-IP rate limits on auth and search.
- Audius free budget (10 rps / 500k requests/month) is a shared server-side budget; client must not call Audius directly.
- Backend enforces per-user search quotas and returns 429 with `Retry-After`.

## Secrets and transport

- No secrets in code, BuildConfig, or resources; server-side proxy for Audius keys.
- HTTPS only; `network_security_config` with `cleartextTrafficPermitted=false`.
- Tokens never logged; PII scrubbed from analytics/crash logs.
- `allowBackup=false` + `dataExtractionRules` on Android.

## Compliance

- Audius API terms: API "as is", no warranty, revocable at any time; no ML/AI training on content; session-only caching; user responsible for rights.
- App must surface Audius attribution where required by provider terms.
- GDPR/Data Safety: account deletion in-app + web URL, real data deletion, data minimization.
- Android data extraction/backup rules match actual behavior for Data Safety form.
