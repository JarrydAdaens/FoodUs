# Plan Spam Briefing — Milestone 2, Run plan-spam-2_21-to-2_23

- Milestone: `milestone-2` (Customisation, reopened 2026-07-26)
- Run slug: `plan-spam-2_21-to-2_23`
- Date: 2026-07-27

## Source documents

| Document | Why it matters |
| --- | --- |
| `context/milestones/milestone-2.md` | Owns Stories 21–23 (authoritative story bodies, dependencies, statuses) |
| `context/dictations-tier-0/2026-07-26_addendum_ai-settings-branding-foodus.md` | Raw owner dictation the three stories were synthesized from (items 1, 2, 4) |
| `context/design.md` | Updated AI-boundary/secrets prose (user-entered key, three-layer prompting) that the plans must respect |
| `context/wiki/provider-quickadd-architecture.md` | Story 14's architecture spike — provider registration/UI landscape Story 23 builds on |
| `context/laws.md` | Constitutional constraints (secrets never committed/logged; additive-overlay fork philosophy) |

## Template

`C:/Users/Jarry/.claude/skills/rails-planning/references/plan-template.md` (unified rails-planning
template — same source used by the prior milestone-2 planning runs).

## Queue (ordered)

1. STORY 2.22 — AI settings screen → `context/implementation-plans/milestone-2/story-22-ai-settings-screen/`
2. STORY 2.23 — Provider website info links → `context/implementation-plans/milestone-2/story-23-provider-website-links/`
3. STORY 2.21 — Three-layer AI prompt architecture → `context/implementation-plans/milestone-2/story-21-three-layer-ai-prompts/`

## Sequencing / batching decisions

- One plan per story; no batching.
- Story 21 depends on Story 22 (its layer-2 user system prompt field lives on the settings
  screen), so the Story 21 worker dispatches **after** Story 22's plan is accepted and reads it
  as a source. Stories 22 and 23 are independent and dispatch in parallel.
- Story 20 (identity) is Complete and out of scope for this run.

## Constraints

- Plans only — no implementation code.
- Fork philosophy: additive overlays, minimal upstream merge-conflict surface, source namespace
  `com.maksimowiczm.foodyou` untouched.
- laws.md §2: no AI credential may ever exist in the repo, CI, or any build (Story 22's whole
  point); user AI config persists on-device only.
- Questions index: `_planning-runs/plan-spam-2_21-to-2_23/questions.md`
