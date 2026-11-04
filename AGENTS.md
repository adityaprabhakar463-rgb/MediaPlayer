# AGENTS.md — Celestial project rules

Always-on rules for every AI agent working in this repo (Antigravity auto-loads this file; OpenCode and others read it explicitly). Status and phases: `PROGRESS.md`.

## 1. Context bootstrap (read before any work)

1. `PROGRESS.md` — current status and phase checklist.
2. `README.md` — doc index and product rules.
3. `docs/frontend/01`–`11` in numerical order:
   - `01_DESIGN_VISION.md` — product concept and visual philosophy
   - `02_SKILL_USAGE_AND_OPENCODE_RULES.md` — mandatory skill activation and workflow
   - `03`–`10` — design system, 3D scene, first launch, navigation/screens, media controls, components, motion, responsive/accessibility/performance
   - `11_FRONTEND_IMPLEMENTATION_GUIDE.md` — phases, acceptance criteria, structure
4. Inspect the actual code under `app/src/main/java/com/celestial/player/` before changing files. The docs and `PROGRESS.md` may lag behind reality.

Confirmed user decisions in `docs/frontend/*` are requirements. A detail marked **Open decision** means ask — never silently invent product behaviour. Do not replace the confirmed design with conventional mobile-app patterns.

## 2. Skills (Antigravity + OpenCode + Claude)

Skill source of truth: `~/.agents/skills/<name>/SKILL.md`. Antigravity discovers them via junctions in `~/.gemini/config/skills/`. If a skill is not discovered, read it directly from `~/.agents/skills/`.

Before any UI, design, or visual task:

1. Consult the skill-to-task mapping in `docs/frontend/02_SKILL_USAGE_AND_OPENCODE_RULES.md`.
2. Read the relevant `SKILL.md` files and apply them (project requirements override skill defaults).
3. List which skills you consulted in your plan — never claim a skill you did not read.

Primary skills for this project: `android-compose-design`, `android-production-ultimate`, `mobile-android-design`, `ui-ux-pro-max`, `ui-styling`, `design-system`, `brand`, `design`.

## 3. Terse mode (caveman-style — say less, do more)

Default communication style for agent replies. Compression is for chat output only.

- Fire tool calls directly. No preamble, progress notes, or narration between calls. After a result: next call or final answer — never announce what comes next.
- Drop filler, pleasantries, hedging, articles where grammar allows, and decorative tables/emoji. Fragments are OK. Short synonyms (big not extensive, fix not "implement a solution for").
- Pattern: `[thing] [action] [reason]. [next step].` — not: "Sure! I'd be happy to help with that. The issue you're experiencing is likely..."
- Never drop not/never/no/only/except — flipping meaning costs more than any token saved. Keep numbers, units, and technical terms exact.
- Code, API names, CLI commands, error strings: always verbatim. Do not invent abbreviations (cfg/impl/req/fn tokenize to the same cost as full words — zero savings, worse clarity).
- One idea per sentence, active voice, imperative instructions ("Run X", not "X should be run").
- Auto-clarity: drop compression entirely for security warnings, irreversible-action confirmations, and multi-step sequences where order matters. Resume terse mode after.
- Written artifacts (docs, commit messages, issue text, comments) stay normal English. Compress replies, not files.

## 4. Work discipline

- Execute the agreed plan; do not re-plan what is already decided. Ask only for Open decisions or irreversible actions.
- Build incrementally; run the app/tests frequently.
- No fake or inert controls. No static screenshots or videos as substitutes for interactive 3D. No unrequested features that change the product concept.
- Do not implement every screen as one giant composable. Follow the package structure in `docs/frontend/11` §3 — adapt to the existing project, do not impose blindly.
- After completing a phase: update `PROGRESS.md` (check off items, refresh status table) and update the design docs if any decision changed.
- Report at the end of each meaningful UI task: skills consulted, files changed, decisions made, accessibility/performance checks, known gaps.
