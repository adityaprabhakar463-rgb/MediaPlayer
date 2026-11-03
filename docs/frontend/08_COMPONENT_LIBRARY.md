# 08 — Component Library

Build a reusable native component system. Components should have clear state models, previews, semantics and test coverage.

## 1. Scene components

| Component | Responsibility |
|---|---|
| `CelestialSceneHost` | Hosts the 3D scene and exposes state/events to Compose |
| `UniverseStarfield` | Distant star field and restrained twinkle |
| `CentralStar` | Fictional bright star and corona |
| `DestinationPlanet` | A planet with unique material/identity and destination ID |
| `PlanetLabel` | Appears on approach/selection only |
| `OrbitalPath` | Subtle orbit visual, if used |
| `UniverseCameraController` | Camera rotation, zoom, focus and restoration |
| `UniverseFormationController` | Scripted first-launch formation sequence |
| `PlanetTravelTransition` | Camera approach and reverse return |
| `PersistentMusicPlanet` | Floating mini planet showing current album artwork |

## 2. Glass UI components

| Component | Responsibility |
|---|---|
| `CelestialGlassSurface` | Reusable translucent surface |
| `GlassListRow` | Standard content row |
| `GlassSearchField` | Search input |
| `GlassSettingsPanel` | Floating settings surface |
| `GlassIconButton` | Accessible icon control |
| `GlassSectionHeader` | Destination/list heading |
| `GlassSlider` | Volume or other standard slider |
| `GlassEmptyState` | Empty library/search state |
| `GlassLoadingState` | Loading feedback |
| `GlassErrorState` | Recoverable error display |

## 3. Playlist components

- `PlaylistRow`
- `PlaylistArtwork`
- `PlaylistOverflowMenu`
- `PlaylistDetailHeader`
- `PlaylistTrackList`
- `CreatePlaylistAction`

## 4. Search components

- `SearchDestination`
- `RecentSearches`
- `SearchCategoryTabs` (only if categories are needed)
- `SearchResultRow`
- `SearchEmptyState`
- `SearchLoadingState`

## 5. Now Playing components

| Component | Responsibility |
|---|---|
| `AlbumPlanet` | Current album artwork as a planet |
| `PlanetarySeekRing` | Interactive progress ring bound to playback |
| `UfoPlaybackControl` | Play/pause with engine illumination |
| `AsteroidSkipControl` | Previous/next with poke animation |
| `TrackMetadata` | Title, artist and album |
| `GlassPlaybackAction` | Queue, shuffle, repeat, favourite |
| `GlassVolumeControl` | Volume adjustment |
| `GlassSleepTimerControl` | Sleep timer |
| `NowPlayingScreen` | Composes the complete experience |

## 6. Component rules

- Keep components small and composable.
- Use immutable UI state models.
- Keep playback/business logic out of composables.
- Expose meaningful callbacks/events.
- Provide `@Preview` variants for default, selected, disabled and edge states where relevant.
- Use stable keys in lazy lists.
- Provide semantics and accessible descriptions.
- Avoid hard-coded screen coordinates.
- Do not build a component solely for a single decorative effect unless it meaningfully isolates complexity.
