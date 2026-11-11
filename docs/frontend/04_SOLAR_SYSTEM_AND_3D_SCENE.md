# 04 — Solar System and 3D Scene

## 1. Scene concept

The home screen is a fictional planetary system around an original bright central star. It is not a depiction of our real Solar System.

The system is the primary navigation interface. Each major destination is represented by a distinct imaginary planet.

## 2. Central star

Appearance:
- Bright, glowing fictional star.
- White luminous core.
- Soft violet-blue corona.
- Gentle, restrained solar flares.
- Soft light contribution to nearby planets.
- Avoid an oversized explosive or harsh neon look.

Behaviour:
- Very subtle brightness pulse.
- Delicate corona movement.
- Stable visual anchor.

## 3. Planets and orbits

- Each destination planet must have a distinct silhouette, surface, palette and atmospheric character.
- Orbital movement is subtle and almost imperceptible.
- Planets may have different orbital speeds and inclinations, but movement must not make navigation confusing.
- Prevent visual intersections and unreadable overlaps.
- The camera and orbital simulation are separate: user drag changes camera orientation, not the orbital parameters.
- Preserve the user's camera orientation when entering and leaving a destination.

Destination planets:
- Playlists
- All Songs
- Search
- Favourites
- Recently Played

Exact planet names and visual identities are open decisions. Proposed names from brainstorming are not locked.

## 4. Camera interaction

Confirmed:
- Free rotation in any direction.
- Horizontal and vertical drag combine into 3D camera rotation.
- Pinch to zoom.
- Release gesture may produce gentle inertia and damping.
- Tapping a planet selects it; gesture handling must distinguish taps from drags.
- Labels appear when a planet is approached or selected, not permanently.
- Camera motion must remain controlled and comfortable.

## 5. Planet selection and travel

On planet selection:
1. Identify and focus the selected planet.
2. Gently de-emphasise distant planets/background.
3. Move the camera towards the selected planet.
4. Increase surface/atmospheric detail as it approaches.
5. Transition into that planet's destination screen.
6. Keep the persistent music planet available.

Back navigation reverses the camera journey:
- Destination UI fades away.
- Camera pulls back from the planet.
- Planet and orbital surroundings return to view.
- Restore the previous solar-system camera orientation.

## 6. Persistent music planet

- A small floating planet represents the current track.
- Current album artwork is mapped to its surface.
- It remains visible across destinations.
- It reflects real playback state.
- Tapping it opens the expanded Now Playing screen.
- It must not be duplicated as a separate fake player per screen.

## 7. Rendering architecture guidance

Use a genuine 3D scene for the solar system and planet objects; do not fake the entire universe with a static image or looping video. Compose should own standard Android UI overlays and accessible controls. The 3D renderer should be isolated behind a scene/view abstraction with a clear state/event interface.

The exact renderer and integration approach must be validated against current Android compatibility and project constraints before implementation. Filament was discussed as a candidate, not a final locked choice. Do not assume a particular Compose wrapper exists without verifying it.

## 8. Scene state model

Suggested conceptual states:
- `UniverseFormation`
- `InteractiveUniverse`
- `PlanetApproach(destinationId)`
- `Destination(destinationId)`
- `ReturningToUniverse`
- `NowPlayingExpanded`

These are design states, not a mandate for a particular code architecture. Ensure transitions are interruptible or safely completed and do not desynchronise the renderer from navigation state.

## 9. Performance fallbacks

- Level of detail for distant planets.
- Culling for objects outside the view.
- Bounded particle counts.
- Avoid expensive real-time blur and excessive dynamic lighting.
- Provide reduced-effects mode.
- Keep playback independent from rendering lifecycle.
