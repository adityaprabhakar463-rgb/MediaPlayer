# 05 — First Launch: Universe Creation

## 1. Intent

The first launch is a cinematic creation sequence that becomes the real interactive home scene. It is not a separate intro video followed by a disconnected app screen.

The final frame of the animation must be the actual interactive solar-system scene.

## 2. Sequence

### Stage 1 — The Void
- Begin in complete blackness.
- A few extremely faint distant stars may appear.
- No conventional UI.
- Establish silence/space visually; audio treatment is not yet specified.

### Stage 2 — The Big Bang
- A concentrated point of light intensifies.
- A powerful but elegant cosmic expansion spreads light, energy and particles.
- Avoid harsh flashing or strobe effects.
- Use controlled brightness and accessibility-safe motion.

### Stage 3 — Cosmic dust and nebula
- Dust and gas swirl and gather.
- Muted violet, blue and silver emerge gradually.
- The black background remains dominant.

### Stage 4 — Central star formation
- Particles converge at the centre.
- The fictional star ignites.
- A white core and soft violet-blue corona form.
- The star becomes the source of light for the scene.

### Stage 5 — Planet formation
- Accretion-like discs and dust structures gather around the star.
- Dust condenses into the destination planets.
- Planets develop their distinct surfaces, rings and atmospheres.
- Their final orbital positions are established.

### Stage 6 — Palette transition
- The colour, lighting, dust and atmosphere settle into the actual Celestial design system.
- Pitch-black space, subtle starfield and restrained violet-blue/silver accents become the permanent environment.
- Avoid a sudden cut or palette swap.

### Stage 7 — Reveal and activation
- Camera pulls back to the final home composition.
- Subtle orbital motion begins.
- Labels can appear when planets are approached/selected.
- Touch interaction activates.
- The user is now in the actual interactive universe.

## 3. Technical continuity requirement

The intro and home must use the same underlying 3D scene and scene objects wherever feasible.

Conceptual transition:
`UniverseFormationState → InteractiveUniverseState`

Do not:
- Replace the animation with a screenshot.
- Switch from a video to an unrelated 3D scene.
- reload/recreate all planets at the transition if it can be avoided.
- abruptly reposition the camera.
- introduce a blank loading screen after the animation.

The scene should transition from scripted formation animation to user-controlled camera and planet interaction.

## 4. Skip and replay

- The intro must be skippable.
- Skipping should resolve cleanly into the completed universe state, not abruptly remove all visuals.
- Subsequent app launches go directly to the completed solar system.
- The introduction can be replayed from Settings.
- Persist first-launch completion reliably.

## 5. Accessibility and performance

- Avoid strobing, rapid flashes and extreme brightness changes.
- Respect reduced-motion preferences.
- If reduced motion is enabled, provide a shorter, gentler formation sequence or direct reveal.
- Avoid blocking app startup indefinitely on asset loading.
- Provide graceful fallback if 3D assets fail to load.
- Do not let the intro prevent the user from accessing the app due to an animation/rendering failure.
