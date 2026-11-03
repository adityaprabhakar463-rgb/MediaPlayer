# 11 — Frontend Implementation Guide

## 1. Before coding

OpenCode must:
1. Read all frontend docs.
2. Follow `02_SKILL_USAGE_AND_OPENCODE_RULES.md`.
3. Inspect the current project and existing architecture before changing files.
4. Confirm the chosen 3D renderer and Compose integration approach against current dependencies and Android support.
5. Identify missing assets and create an asset plan.
6. Present a phased implementation plan before large-scale changes.

Do not replace the user's existing project structure without inspecting it.

## 2. Recommended implementation phases

### Phase 1 — Design foundation
- Compose theme and semantic design tokens.
- Typography and spacing.
- Glass surface primitives.
- Common icon buttons and list rows.
- Previews for core components.

### Phase 2 — Destination UI without 3D dependency
- Playlists glass list.
- Search glass interface.
- All Songs, Albums, Artists, Favourites and Recently Played layouts.
- Settings glass panel.
- Persistent player placeholder wired to a single state model.

### Phase 3 — 3D scene foundation
- Integrate and validate renderer.
- Create starfield and central star.
- Add destination planets and camera controller.
- Implement free rotation, pinch zoom and selection.
- Add planet labels on approach/selection.

### Phase 4 — Navigation and continuity
- Planet approach transition.
- Destination screen integration.
- Reverse camera navigation.
- Restore previous camera orientation.
- Keep persistent mini music planet visible.

### Phase 5 — Now Playing signature controls
- Album artwork planet.
- Playback-driven seek ring.
- UFO play/pause.
- Asteroid previous/next.
- Standard glass queue, shuffle, repeat, favourite, volume and sleep timer controls.
- Synchronise with actual Media3 state.

### Phase 6 — First-launch formation
- Big Bang.
- Dust/nebula formation.
- Central star formation.
- Planet formation.
- Palette transition.
- Seamless handoff to the same interactive scene.
- Skip, persist completion and replay from Settings.

### Phase 7 — Refinement and quality
- Motion polish.
- Reduced-effects mode.
- Accessibility.
- Responsive layouts.
- Performance profiling.
- Device testing and visual review.

## 3. Suggested project organisation

Adapt to the existing project; do not impose this blindly.

```text
app/
  ui/
    theme/
    components/
    navigation/
    screens/
      universe/
      playlists/
      search/
      library/
      albums/
      artists/
      favourites/
      recently_played/
      settings/
      now_playing/
  scene/
    host/
    camera/
    objects/
    formation/
    assets/
  playback/
  domain/
  data/
```

## 4. Acceptance criteria

### Universe
- Pitch-black space remains dominant.
- Central star is bright and softly luminous.
- Planets are fictional and visually distinct.
- Orbital movement is subtle.
- Free 3D rotation and pinch zoom work.
- Labels appear on approach/selection, not permanently.
- Planet tap triggers cinematic travel.
- Back reverses travel and restores orientation.

### Destination screens
- All major destinations are represented by planets in the universe.
- Inside screens use familiar glass UI over deep-space backgrounds.
- Playlist rows are Apple Music-inspired in usability, not copied in branding.
- Search is clear and responsive.
- Settings opens as a floating glass panel.
- Persistent music planet remains visible.

### Now Playing
- Album artwork is the planet surface.
- Ring is a real seek control and reflects actual progress.
- UFO illumination reflects actual play/pause state.
- Asteroid taps change tracks and animate.
- Standard glass controls work.
- Notification/headset controls remain synchronised.

### First launch
- Big Bang, star formation and planet formation are animated.
- Animation transitions into the actual interactive palette and scene.
- No abrupt scene replacement or blank loading screen.
- Skip works.
- Subsequent launches bypass formation.
- Replay is available in Settings.

### Quality
- No fake or inert controls.
- No avoidable jank.
- Reduced-effects mode works.
- TalkBack and touch targets are supported.
- Audio continues independently of scene rendering.

## 5. Implementation discipline

- Build incrementally and run the app frequently.
- Do not implement every screen as one giant composable.
- Do not use static screenshots or videos as substitutes for required interactive 3D.
- Do not use placeholder buttons in a supposedly finished screen.
- Do not add unrequested features that change the product concept.
- Keep a short implementation status log and update it as phases are completed.
- When a requirement is ambiguous, ask rather than silently inventing a product decision.
