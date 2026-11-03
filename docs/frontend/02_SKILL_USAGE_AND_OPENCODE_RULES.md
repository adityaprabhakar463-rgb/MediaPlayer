# 02 — Mandatory Skill Usage and OpenCode Rules

This document is a project instruction. OpenCode must follow it for every frontend design, UI implementation, visual review and refinement task.

## Required installed skills

The user has installed or intends to use these skills. OpenCode must actively discover and invoke the relevant installed skill instructions—not merely mention their names.

### Android / Compose skills

- `android-compose-design`
- `mobile-android-design`
- `android-production-ultimate`

### UI UX Pro Max skill set

Use the relevant skill or mode from this set:
- `banner-design`
- `brand`
- `design`
- `design-system`
- `slides`
- `ui-styling`
- `ui-ux-pro-max`

Some items may be separate skills, modes, commands or capabilities within the installed UI UX Pro Max package. Inspect the installed package and its `SKILL.md` / documentation to determine the exact supported invocation. Do not invent commands or assume all names are independently installable.

## Mandatory activation workflow

Before starting any UI-related task:

1. Inspect the project's OpenCode skill configuration and installed skill directories.
2. Find the actual skill files and read the relevant `SKILL.md` instructions.
3. Explicitly activate/use the relevant skills in the current task.
4. In the implementation plan, list which skills were consulted and what guidance was applied.
5. If a requested skill is not installed or cannot be invoked, report that clearly and continue only with available guidance. Never pretend a skill was used.
6. Re-check skill instructions when beginning a materially different UI task.

## Skill-to-task mapping

| Task | Required skills |
|---|---|
| Overall visual direction and art direction | `brand`, `design`, `ui-ux-pro-max` |
| Design tokens, colour, type, spacing and component rules | `design-system`, `ui-styling`, `android-compose-design` |
| Native Android screen composition and interaction | `android-compose-design`, `mobile-android-design` |
| Compose implementation quality and production readiness | `android-production-ultimate`, `android-compose-design` |
| Glass UI, lists, search and settings | `ui-styling`, `design-system`, `mobile-android-design` |
| 3D/space visual composition and scene integration | `design`, `brand`, `android-compose-design`; supplement with project-specific 3D engineering guidance |
| Intro sequence / cinematic presentation | `brand`, `design`, `banner-design` only if relevant to a hero/banner asset; `android-compose-design` for implementation |
| UI critique and refinement | `ui-ux-pro-max`, `ui-styling`, `android-production-ultimate` |
| Slides or presentation assets | `slides` only when a slide deck is explicitly requested; it is not a default app UI skill |

## Priority and conflict resolution

1. The user's confirmed Celestial requirements in `01_DESIGN_VISION.md` and the screen-specific documents take precedence over generic skill defaults.
2. Native Android platform conventions, accessibility and functional correctness must be preserved.
3. Skills provide expertise and implementation patterns; they must not override the product's distinctive art direction.
4. If skills disagree, explain the conflict, prefer the most Android-specific and current guidance for implementation, and preserve the confirmed design intent.
5. Do not use web-dashboard patterns or React web components as substitutes for native Compose UI.

## Required design workflow

For each screen or component:

1. Read the relevant design documents.
2. Consult the mapped skills.
3. Produce a short design/implementation plan.
4. Build reusable Compose components and state-driven interactions.
5. Preview on multiple screen sizes.
6. Test interactions and accessibility.
7. Perform a visual critique against the Celestial design principles.
8. Refine until it meets the acceptance criteria.

## Required reporting

At the end of each meaningful UI task, report:
- Skills actually consulted.
- Screens/components changed.
- Visual and interaction decisions made.
- Accessibility and performance checks performed.
- Known gaps or skills unavailable.

Do not claim "pixel-perfect" without checking actual rendered UI. Do not claim 3D interactions work if only a static mockup or video is present.
