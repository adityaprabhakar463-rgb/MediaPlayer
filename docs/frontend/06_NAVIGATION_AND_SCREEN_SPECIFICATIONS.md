# 06 — Navigation and Screen Specifications

## 1. Navigation model

The solar system is the primary navigation. Each major destination is a planet. Selecting a planet triggers a camera zoom into that planet, followed by its dedicated screen.

Back navigation reverses the camera journey and returns to the previous solar-system orientation.

The currently playing mini planet remains visible across destinations.

## 2. Destination visual rule

Inside every destination:
- Use a hybrid presentation: 3D planetary environment/space atmosphere plus minimalist glass UI.
- The environment provides identity and ambience.
- Familiar glass components present functional content.
- Keep the persistent mini music planet visible.
- Avoid turning content rows or standard controls into additional planets.

## 3. Playlists destination

Visual direction:
- Familiar, Apple Music-inspired list layout.
- Glassy/translucent dark playlist rows.
- Deep outer-space background with stars and occasional comets.
- Playlist artwork on the left.
- Playlist title and secondary metadata (such as track count) beside it.
- Minimal overflow menu.
- Readable typography and comfortable spacing.

Interactions:
- Tap a playlist to open its detail screen.
- Overflow menu may offer standard playlist actions, subject to supported functionality.
- Create playlist flow.
- Persistent music planet remains visible.

Do not copy Apple Music branding or assets; use it only as a reference for familiar list usability.

## 4. Search destination

Visual direction:
- Large elegant floating glass search field.
- Recent searches.
- Discovery/suggested content.
- Search results in familiar glass list rows.
- Deep-space background with subtle stars and occasional distant comets.

Behaviour:
- Search as user types, with sensible debounce.
- Clear search.
- Categorised results for songs, artists, albums and playlists where supported.
- Tapping a song plays it; tapping an artist/album/playlist opens the relevant destination/detail.
- Show useful empty, loading and no-results states.
- Persistent music planet remains visible.

## 5. All Songs destination

- Familiar glass list over a deep-space background.
- Show track title, artist and relevant metadata.
- Support search/filter/sort if implemented.
- Use efficient scrolling for large libraries.
- Artwork thumbnails where available.
- Empty and permission-denied states must be designed.

Exact row metadata and sorting options are open decisions.

## 6. Albums destination

- Familiar glass-based album collection.
- Use artwork-led rows or grid depending on screen width and final design review.
- Show album name, artist and relevant metadata.
- Tapping opens album detail with track list.
- Do not invent album artwork when none exists; use a designed fallback.

Grid-vs-list final choice is open.

## 7. Artists destination

- Familiar glass list or responsive collection.
- Show artist name and available artwork.
- Tapping opens artist detail and associated tracks/albums where metadata exists.
- Handle unknown artist metadata gracefully.

## 8. Favourites destination

- Familiar glass list of favourited tracks.
- Clear empty state explaining how to add favourites.
- Standard play/shuffle actions where supported.

## 9. Recently Played destination

- Familiar glass list of recently played tracks.
- Show useful recency information if available.
- Clear empty state for new users.

## 10. Settings panel

Settings is not a planet. It opens as a floating glass panel over the current environment.

Visuals:
- Translucent dark glass.
- Subtle silver/violet border.
- Soft backdrop blur where performant.
- Rounded corners and minimalist icons.
- Preserve current camera position and destination context.
- Closing restores exactly where the user was.

Proposed categories:
- Appearance
- Universe (star density, orbital speed, animation intensity)
- Playback
- Audio
- Library/storage
- Experience (haptics, gestures, reduced motion)
- About

These are proposed categories; final options depend on actual implemented features.

## 11. Persistent player placement

The mini music planet remains visible across all destination screens. Its exact screen anchor, size and behaviour around system insets should be established in responsive prototypes. It must not obstruct list scrolling, search input, important buttons or system navigation.
