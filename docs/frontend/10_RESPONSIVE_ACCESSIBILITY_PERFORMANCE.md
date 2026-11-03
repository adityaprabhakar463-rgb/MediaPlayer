# 10 — Responsive Design, Accessibility and Performance

## 1. Responsive layout

- Prioritise portrait Android phones.
- Adapt to different screen sizes and aspect ratios.
- Respect status/navigation bars, display cutouts and system insets.
- Keep primary controls reachable.
- Do not use fixed screen-coordinate layouts for standard UI.
- Use adaptive content grids where appropriate.
- Ensure the persistent mini music planet does not obstruct content or text entry.
- Landscape support should be considered and tested; exact landscape composition is an open decision.

## 2. Accessibility

- Minimum 48dp touch targets for interactive controls.
- TalkBack labels for planets, UFO, asteroids, seek ring and icon-only controls.
- Expose meaningful state descriptions: playing/paused, selected destination, playback progress.
- Ensure screen-reader users can navigate destinations without relying on 3D spatial gestures.
- Provide conventional accessible controls for all signature 3D actions.
- Maintain text contrast over variable backgrounds.
- Support font scaling.
- Do not rely on colour alone to communicate active/paused state.
- Respect reduced-motion preferences and in-app reduced-effects setting.
- Avoid flashing/strobing in Big Bang animation.

## 3. Performance

Targets:
- Aim for smooth 60 FPS interactions on supported devices.
- Keep touch response immediate.
- Keep audio playback independent of renderer lifecycle.
- Use level of detail for distant planets.
- Cull objects outside the camera view.
- Bound star/particle counts.
- Avoid expensive real-time blur and excessive dynamic lighting.
- Cache and efficiently load planet/album assets.
- Use lazy lists for large music collections.
- Avoid unnecessary Compose recompositions.
- Profile actual release builds on representative devices.

## 4. Degraded-device behaviour

Provide a reduced-effects mode:
- Lower particle density.
- Reduce dynamic lighting and glow.
- Reduce/disable ambient movement.
- Simplify camera transitions.
- Preserve all navigation and playback functionality.

If the 3D renderer cannot initialise, show a graceful fallback that still allows access to music and controls. Do not leave the user on a blank screen.

## 5. Lifecycle and audio independence

- Audio playback must continue when the app navigates between screens.
- Background playback/session service must not depend on the 3D scene staying alive.
- Pause or reduce nonessential rendering when app is backgrounded.
- Restore the scene safely when returning.
- Handle configuration changes and process recreation without corrupting playback state.

## 6. Testing

Test:
- Small and large phones.
- Different aspect ratios.
- Font scaling.
- TalkBack navigation.
- Reduced motion.
- Low-memory conditions.
- Renderer unavailable/failure.
- Large music libraries.
- Missing artwork/metadata.
- Playback during navigation and app backgrounding.
- Notification, headset and Bluetooth controls.
