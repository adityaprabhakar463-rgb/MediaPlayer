# Celestial — Frontend Design Documentation

**Product:** Celestial, a native Android space-themed media player  
**Platform:** Android  
**Implementation direction:** Kotlin + Jetpack Compose, with a real-time 3D scene integrated into the Compose UI  
**Status:** Design vision and interaction requirements; visual details not explicitly decided are marked as proposals.

## Start here

1. Read `docs/frontend/01_DESIGN_VISION.md`.
2. Read `docs/frontend/02_SKILL_USAGE_AND_OPENCODE_RULES.md` before doing any design or UI implementation.
3. Read the remaining documents in numerical order.
4. Treat confirmed user decisions as requirements. Do not silently replace them with conventional mobile-app patterns.
5. Where a detail is marked **Open decision**, ask before inventing a product-level behaviour. You may use a temporary implementation assumption only if it is clearly documented.

## Document index

- `01_DESIGN_VISION.md` — product concept and visual philosophy
- `02_SKILL_USAGE_AND_OPENCODE_RULES.md` — mandatory skill activation and workflow
- `03_DESIGN_SYSTEM.md` — palette, typography, glass surfaces and visual tokens
- `04_SOLAR_SYSTEM_AND_3D_SCENE.md` — fictional solar system, planets, camera and interaction
- `05_FIRST_LAUNCH_UNIVERSE_CREATION.md` — Big Bang, star and planet formation, seamless transition
- `06_NAVIGATION_AND_SCREEN_SPECIFICATIONS.md` — planet destinations and screen behaviour
- `07_NOW_PLAYING_AND_MEDIA_CONTROLS.md` — album planet, seek ring, UFO, asteroids and standard controls
- `08_COMPONENT_LIBRARY.md` — reusable Compose and 3D components
- `09_MOTION_AND_INTERACTION.md` — motion language, gestures, states and accessibility alternatives
- `10_RESPONSIVE_ACCESSIBILITY_PERFORMANCE.md` — device adaptation, accessibility and performance
- `11_FRONTEND_IMPLEMENTATION_GUIDE.md` — implementation sequence, acceptance criteria and handoff

## Important distinction

Celestial has two complementary layers:

- **Outside a destination:** the interactive 3D fictional solar system. Planets are the primary navigation.
- **Inside a destination:** familiar, minimalist, glass-style Android UI over a deep-space background.

Do not turn every list row, button or setting into a planet. The planetary metaphor is the navigation system and the signature player controls; everyday content remains easy to use.
