# Plan Spam Questions — Milestone 2, Run plan-spam-2_21-to-2_23

Milestone: `context/implementation-plans/milestone-2/`
Run: `context/implementation-plans/milestone-2/_planning-runs/plan-spam-2_21-to-2_23/`
Date: `2026-07-27`
Source: `context/milestones/milestone-2.md` (Stories 21–23), `context/dictations-tier-0/2026-07-26_addendum_ai-settings-branding-foodus.md`

Per-plan questions live inside each plan's `## Questions / Unknowns` section under
`context/implementation-plans/milestone-2/`. This file indexes them and carries
cross-run Boss questions.

## Per-Plan Questions Index

| Story | Plan Folder | Questions | Status |
| --- | --- | --- | --- |
| STORY 2.22 | `context/implementation-plans/milestone-2/story-22-ai-settings-screen/` | [plan.md#questions--unknowns](../../story-22-ai-settings-screen/plan.md#questions--unknowns) | OPEN |
| STORY 2.23 | `context/implementation-plans/milestone-2/story-23-provider-website-links/` | [plan.md#questions--unknowns](../../story-23-provider-website-links/plan.md#questions--unknowns) | OPEN |
| STORY 2.21 | `context/implementation-plans/milestone-2/story-21-three-layer-ai-prompts/` | [plan.md#questions--unknowns](../../story-21-three-layer-ai-prompts/plan.md#questions--unknowns) | OPEN |

## Boss Questions

Cross-cutting questions that apply to multiple plans, affect sequencing, or must be answered before workers proceed.

- Q: When the user has entered an AI key on-device (Story 22) and a developer BuildConfig fallback also exists, which wins — and is the fallback surfaced in the UI at all?
  Affects: `STORY 2.22, STORY 2.21`
  Assumption: User-entered DataStore config always wins; blank-by-default BuildConfig values are a silent developer fallback only, never shown in the settings UI.
  Status: OPEN
  Answer:

- Q: Story 21's worker flagged that `AiScanViewModel.aiConfigured` / `PlaceholderMetaViewModel.aiConfigured` may still read BuildConfig instead of runtime config after Story 22 — with a user-entered key and blank BuildConfig, AI buttons would be wrongly disabled. Which story owns the fix?
  Affects: `STORY 2.22, STORY 2.21`
  Assumption: Story 22's execution fixes the gating alongside its runtime-config plumbing; Story 21 re-verifies and inherits the fix only if 22 missed it.
  Status: OPEN
  Answer:
