# 15 — Navigation UX and Planet Labels

## 1. Purpose

Define how users move between the solar system home and destination screens, how destination planets are labelled so users know what each planet opens, and how to keep navigation predictable, visually clear, and accessible.

## 2. Current state

- `NavTarget` (ui/navigation/NavTarget.kt) lists five destinations plus the playlist detail type.
- `CelestialNavState` holds a back stack; `navigate` pushes, `back` pops.
- Home is the interactive 3D universe (`CelestialUniverseScene`); planets and labels are the primary navigation.
- A Destinations sheet provides an accessible non-3D fallback.

## 3. Navigation model

- The solar system universe is the root.
- Tapping a planet or its label triggers the approach journey, then opens the destination screen.
- Back navigation reverses the journey and restores the previous camera orientation.
- Settings opens as a floating glass panel, not as a destination planet.
- Detail screens navigate back to their parent destination.
- Predictive back is supported.
- A single destination registry is the source of truth for the 3D scene, planet labels, fallback list, and screen-reader navigation.

## 4. Destination registry

| Destination | Icon | Route |
|---|---|---|
| Playlists | queue | NavTarget.Playlists |
| Search | search | NavTarget.Search |
| All Songs | library | NavTarget.AllSongs |
| Favourites | favourite | NavTarget.Favourites |
| Recently Played | history | NavTarget.RecentlyPlayed |

Each destination keeps: id, label, icon, colour seed, route, and accessibility text. All UI that opens a destination reads this registry; no hardcoded duplicate lists.

## 5. Planet labels

Labels are the wayfinding layer for the solar system.

- Labels are always visible while their planet is on screen (locked decision).
- Each label is a compact glass pill floating above its planet.
- Labels scale and fade with distance; they are never fully hidden until the planet goes off-screen.
- The selected planet gets a brighter halo and a stronger label.
- Labels avoid collisions, clamp to screen bounds, and do not overlap the central star, mini player, system bars, or the Destinations sheet.
- Max five labels; early-out when no planet is visible.
- Tapping a label does the same thing as tapping its planet: select and travel.
- Touch targets are at least 48dp; TalkBack gets content descriptions such as "Playlists planet, button".
- Positioning comes from scene projection; positions are read in the draw phase to avoid per-frame recomposition.
- "Hover" means persistent floating labels, because Android touch has no pointer hover.
- Reduced effects keeps labels static: no bob, parallax, or continuous motion.
- Label styling uses existing glass tokens: small text, contrast scrim over bright planets, no new colours.

## 6. Accessible navigation fallback

- A Destinations button opens a small glass sheet listing all five destinations.
- The sheet is a conventional list: screen-reader traversable, clear tap targets, no 3D gestures required.
- It is the fallback when the 3D renderer fails or the user prefers list navigation.
- Closing the sheet restores the previous universe state exactly.

## 7. Gestures and feedback

- Tap is separated from drag so a planet tap never accidentally rotates the camera.
- Haptics fire on planet and label selection.
- First-run hint is dismissible: "Tap a planet or its label"; it does not repeat indefinitely.
- The persistent mini music planet stays visible and never obstructs labels, list scrolling, search input, or system navigation.

## 8. Acceptance criteria

- A first-time user can name every planet destination.
- Tapping a planet or its label opens the correct screen.
- Back navigation returns to the universe and restores camera orientation.
- Labels stay readable with at least 4.5:1 contrast over bright planets and do not overlap.
- The Destinations sheet opens and works without the 3D renderer.
- TalkBack can reach every destination without relying on spatial gestures.
- Reduced-effects mode keeps labels legible and navigation functional.

## 9. Open decisions

- Exact label font size, pill shape, and distance-fade curve validated on device using existing theme tokens.
- Whether labels fade fully at extreme distance or stay faint.
- Planet-to-destination visual identities and planet names remain open decisions per doc 01.
