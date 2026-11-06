# 16 — Lightweight Performance

## 1. Purpose

Keep Celestial lightweight and lag-free without major architectural changes. Every optimization is measured, additive, and local. No screens, themes, or navigation structures are rewritten.

## 2. Target

- 60 FPS on mid-range 2022+ Android phones.
- Lower device tiers get adaptive reduced effects while the app stays usable.
- Touch response is immediate; audio playback stays independent of renderer lifecycle.

## 3. Guardrails

- Do not rewrite the navigation stack or screens.
- Do not add real-time blur or heavy shaders.
- Do not introduce a new animation library.
- Do not make labels animated 3D objects.
- Keep existing Compose theme, glass components, and `CelestialNavState`.
- Profile release builds; debug builds lie.

## 4. Performance targets

Set baselines with Macrobenchmark, FrameMetrics, and Perfetto on representative devices:

- 60 FPS target during navigation, camera drag, and planet label updates.
- Frame-time p95 under 16.6 ms during primary interactions on mid-range devices.
- Janky frames under 1% of observed frames.
- Startup and resume targets are set after the first baseline; hard caps prevent regressions.
- Memory stays stable across renderer sessions, image caches, and long playback.

## 5. Device tiers

- High: full glass, stars, comets, labels, 60 FPS.
- Mid: target tier; glass surface, 110 seeded stars, two comets, labels.
- Low: reduced effects mode; static labels; fewer stars/comets; renderer fallback that still opens music and controls.

## 6. Compose optimizations (hot paths)

- Stability: mark shared models `@Immutable`; use stable keys and `contentType` in lazy lists.
- Compose sorting/filtering happens in state holders, never inside item blocks.
- Player ticker state is localized; whole-shell recomposition from playback progress is avoided.
- Glass surfaces cache brushes and borders; no per-frame gradient allocation; no blur.
- `DeepSpaceBackdrop` precomputes 110 stars; twinkle runs only when ambient motion is enabled; comets stay in the top band; static fallback otherwise.
- Mini player updates only its own arc and artwork; it does not force the shell to recompose.
- Images are sized to display; artwork gradients and thumbnails are cached.
- Animations use spring for motion and tween for fades; nothing exceeds 600 ms; stagger is capped at 300 ms.

## 7. 3D renderer performance (when integrated)

- The renderer stays isolated behind a scene/view abstraction with a clear state/event interface.
- Level of detail for distant planets; cull objects outside camera view; bounded particle count.
- No expensive real-time blur or excessive dynamic lighting.
- Pause or reduce nonessential rendering when backgrounded; restore safely on return.
- Adaptive resolution and frame pacing for lower tiers.
- Renderer failure shows a graceful fallback that still allows music playback and control; no blank screen.

## 8. Planet label performance

- Planet positions are projected by the scene and read in the draw phase.
- Label positions use `derivedStateOf` wrapped in `remember`; only visible labels are updated.
- Label pill layouts are cached; only opacity and scale animate.
- Labels exit early when the renderer is inactive or the planet is off-screen.

## 9. Navigation performance

- Transitions are short: fade only under reduced motion; slide plus fade around 240 ms otherwise.
- Destination switches avoid full-screen recomposition.
- Camera orientation and previous screen state are preserved across back navigation.
- `nav.back()` is not called as a side effect during composition.

## 10. Measurement and profiling

- Macrobenchmark for app startup and destination navigation.
- FrameMetrics and Perfetto for jank during camera drag, label projection, and transitions.
- Memory profiling for renderer sessions, image caches, and playback state.
- Baseline profiles per release; R8 and shrinkResources enabled.
- Test on low, mid, and high device tiers; font scaling; TalkBack; reduced motion; low memory.

## 11. Acceptance gates

- No navigation jank; planet labels add no measurable per-frame cost.
- Settings panel opens and closes without dropping frames.
- Playback continues while navigating and while the app is backgrounded.
- Low-end devices remain usable with reduced effects.
- CI fails when a release build misses baseline gates.

## 12. Do not do

- Do not rewrite the navigation stack.
- Do not add real-time blur or heavy shaders.
- Do not introduce a new animation library.
- Do not make every label an animated 3D object.
- Do not ship debug-only heuristics as release behaviour.

## 12. No hardcoding — every component works

- Remove all hardcoded values from the app: colors, sizes, spacing, text, durations, sample data, IDs, URLs, and magic numbers.
- Every value must come from theme tokens (`CelestialColors`, `CelestialTypography`, `CelestialSpacing`, `CelestialShapes`, `CelestialEffects`) or from a single source of truth (`SampleLibrary`/`LibraryStateHolder` for data, `NavTarget` for routes).
- No hardcoded sample tracks/artists/albums leaked into screens; screens consume `LibraryStateHolder` / repository state only.
- Every component must be a real, working component: no placeholder buttons, no inert controls, no fake navigation, no stub screens that look finished but do nothing.
- Hardcoded colors must be replaced with semantic color tokens; hardcoded dp/sp must use theme spacing/type tokens; hardcoded strings must be strings resources or theme-driven labels.
- If a value varies by screen, pass it as a parameter or derive it from the model; do not inline it.

## 13. Local audio directory scanning

- Add a local audio feature where the user sets a directory path and the app scans it recursively for playable files: mp3, wav, flac, opus.
- Scan runs off the UI thread (coroutine/dispatcher) so it never blocks navigation or playback.
- Extracted metadata (title, artist, album, duration, artwork) maps to the existing domain models (`Track`, `Album`, `Artist`) through a single source of truth; no parallel hardcoded libraries.
- Local files play through the same player state model as streaming tracks; no separate fake player.
- Directory choice is persisted (DataStore/Preferences) and remembered across launches; user can change or clear it anytime.
- Scan results surface through the normal glass screens (All Songs / Albums / Artists / Recently Played) with empty/loading/error states.
- Respect scoped storage / storage access framework permissions; do not request broad storage access unless required; handle permission denial gracefully.
- No hardcoded music directories, file paths, or file lists anywhere in the app.

## 14. Open decisions

- Exact per-device frame-time and jank thresholds after the baseline run.
- 3D renderer choice and its Compose wrapper API; labels must stay renderer-agnostic.
- Label font size and pill shape validated on device using existing theme tokens.
