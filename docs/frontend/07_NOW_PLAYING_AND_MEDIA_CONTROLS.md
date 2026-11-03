# 07 — Now Playing and Media Controls

## 1. Concept

The expanded Now Playing screen is built around a living celestial planet. Signature media controls are physical-looking celestial objects, while all other controls use the standard glass UI language.

## 2. Album planet

- The current track's album artwork becomes the planet's surface.
- Present it as a spherical/circular 3D object.
- Subtle rotation is acceptable; do not distract from the controls.
- Add restrained atmospheric edge lighting.
- On track change, transition smoothly to the new artwork.
- If artwork is missing, use a designed celestial fallback.
- Artwork changes must be driven by actual Media3 metadata.

## 3. Orbital ring as seek bar

The ring is the actual playback progress indicator.

Visual states:
- At track start: ring is present but dull/unilluminated.
- As playback progresses: the illuminated portion grows progressively around the ring.
- The unplayed portion remains subtly visible.
- At completion: the full ring is illuminated.

Interaction:
- Tap or drag along the ring to seek.
- Convert touch position to a track position accurately.
- Reflect seek changes in actual playback position.
- Use a sufficiently large touch target, even if the visible ring is thin.
- Handle seeking while buffering or when duration is unknown.
- Reset/retarget progress correctly when the track changes.

The ring must not be a decorative animation disconnected from the playback engine.

## 4. UFO as Play/Pause

- A UFO/spaceship is the primary play/pause control.
- When playing, its engine/light is illuminated with a restrained blue-white glow.
- When paused, the lights turn off; the UFO remains visible.
- Tapping toggles actual playback.
- Visual state always reflects the real player state, including changes from notification, headset, Bluetooth or external controls.
- Use a quick, satisfying light transition without delaying the action.

## 5. Asteroids as Previous/Next

- Two distinct asteroids represent previous and next.
- Tapping an asteroid triggers a small poke/impact motion.
- Previous/next command is sent to the actual player.
- Track artwork transitions to the new album planet.
- Seek ring updates to the new track's progress.
- UFO illumination reflects the new playback state.
- Avoid exaggerated movement that could obscure the control.

## 6. Standard glass controls

All non-signature controls use the established glass UI system:
- Queue
- Shuffle
- Repeat (off / repeat all / repeat one)
- Favourite
- Volume
- Sleep timer
- More options / track information

Use familiar icons, accessible labels, active states and standard touch behaviour. Additional audio features should only be shown if actually supported.

## 7. Playback synchronisation

All player visuals must derive from a single source of truth: the actual Media3 playback/session state.

Synchronise:
- Current track metadata and artwork
- Play/pause state
- Playback position and duration
- Buffering state
- Queue
- Shuffle and repeat modes
- Volume where available
- Favourite state from app data

Do not create independent UI-only playback state that can drift from Media3.

## 8. Persistent mini planet

- Remains visible while navigating destinations.
- Shows current album artwork.
- Can show a compact ring/progress state if legible.
- Tapping opens expanded Now Playing.
- Updates immediately on track changes and playback state changes.
- Must not block key screen interactions.

## 9. Essential behaviour

- Background playback continues while the 3D scene changes or is paused.
- System media notification and lock-screen controls stay synchronised.
- Audio focus interruptions and headset actions update the UI.
- A renderer crash or screen navigation must not terminate playback.
- Handle empty queue, missing artwork, unknown duration, buffering and playback errors.
