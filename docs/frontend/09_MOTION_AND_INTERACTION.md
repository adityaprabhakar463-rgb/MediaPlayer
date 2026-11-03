# 09 — Motion and Interaction

## 1. Motion philosophy

Motion should feel weightless, deliberate and atmospheric. It must communicate state and spatial continuity rather than decorate every element.

## 2. Universe motion

- Stars twinkle subtly and independently.
- Occasional distant comets move gently through the background.
- The central star pulses very softly.
- Planets orbit almost imperceptibly.
- Camera movement follows user gestures with controlled inertia and damping.
- Avoid constant large movements that make targets hard to select.

## 3. Gestures

| Gesture | Action |
|---|---|
| Drag | Free 3D camera rotation |
| Pinch | Zoom |
| Tap planet | Select and travel |
| Tap mini music planet | Open Now Playing |
| Tap/drag seek ring | Seek |
| Tap UFO | Play/pause |
| Tap next/previous asteroid | Change track |
| Android back | Reverse camera journey |

Gesture arbitration is important: a tap must not accidentally become a camera drag. Provide accessible alternatives for all important gestures.

## 4. Planet travel

- Focus selected planet.
- Subtly de-emphasise distant environment.
- Camera moves towards the planet.
- Surface detail/atmosphere becomes more visible.
- Destination UI enters smoothly.
- Reverse the same journey on back navigation.
- Restore the exact previous camera orientation where possible.

Avoid abrupt screen replacement.

## 5. First-launch motion

Follow `05_FIRST_LAUNCH_UNIVERSE_CREATION.md`. The formation animation and interactive universe must share scene continuity.

## 6. Player motion

- Seek ring fills progressively according to actual playback position.
- UFO light turns on while playing and off while paused.
- Asteroids perform a brief poke/impact response when tapped.
- Artwork transitions smoothly on track change.
- Mini planet remains spatially stable while changing destinations.

## 7. Glass UI motion

- Gentle fade/slide for list and panel entry.
- Subtle pressed-state feedback.
- Smooth selection highlights.
- Avoid large spring overshoots or excessive bounce.
- Comets must not cross text or controls in a way that harms legibility.

## 8. Reduced motion

Provide a reduced-effects setting:
- Reduce or stop ambient movement.
- Simplify camera transitions.
- Reduce particle count and glow animation.
- Preserve clear feedback and all functionality.
- Offer a gentler/shorter first-launch sequence.

## 9. Motion quality checks

- No animation should block a primary action.
- Do not animate large numbers of Compose nodes independently if a single scene-level effect is more efficient.
- Avoid excessive recomposition from frame-by-frame state.
- Ensure animations stop or reduce when the screen is not visible, as appropriate.
- Playback progress must be driven by player position, not a free-running visual timer.
