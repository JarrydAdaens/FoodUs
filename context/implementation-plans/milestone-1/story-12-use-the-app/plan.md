# Plan: Use the App for a While

## Metadata

- Task Type: `STORY`
- Status: `Ready`
- Owner: Jarryd Adaens (this story is owner-executed daily use; agents only synthesize findings)
- Last Updated: 25 July 2026

## Linked Context

- Milestone: [context/milestones/milestone-1.md](../../../milestones/milestone-1.md)
- Story: [Story 12: Use the app for a while](../../../milestones/milestone-1.md#story-12)
- Backlog source: none — mapped directly from the 2026-07-24 initial project seed
- Dictation source: [2026-07-24 initial project seed](../../../dictations-tier-0/2026-07-24_initial_project_seed_acme-food-app.md)
- Related Plans: Story 10 (export script) and Story 11 (update mechanism) have no plans yet;
  their outputs enrich but do not block this story. Findings feed Milestone 2
  ([context/milestones/milestone-2.md](../../../milestones/milestone-2.md)) planning and
  [context/backlog/backlog-1.md](../../../backlog/backlog-1.md).
- External Tooling: none required; `rails-planning` conventions for this plan itself

## CER

- Complexity: 1
- Effort: 2
- Risk: 1
- Notes: Inline estimate by the planning agent, not a formal `rails-grade-cer` grade. This is a
  no-code research story: complexity is near zero (structured observation only), effort is
  calendar time rather than work volume (a few days to a week of ordinary daily use plus one
  dictation/synthesis pass), and the only real risk is observations evaporating uncaptured.

## Objective

The owner uses the unmodified fork as his real daily food tracker for a few days to a week,
becomes familiar with the baseline app, and durably records what works, what causes friction,
and what is worth keeping — so Milestone 2's customisation choices (AI-assisted logging,
ergonomics fixes, identity) are grounded in lived use rather than assumption.

## Scope

### In Scope

- Daily use of the app on the owner's phone for the guideline period (a few days to a week).
- A lightweight observation protocol: what to pay attention to while using the app (below).
- Durable capture: observations dictated into `context/dictations-tier-0/`, then synthesized
  into backlog stories and Milestone 2 planning input per the context-tier workflow.
- The owner's completion judgment — the story is done when he says the baseline is understood.

### Out Of Scope

- Any code, configuration, or app changes — friction found here becomes backlog stories, never
  in-place fixes during this story.
- Importing the historical data (Story 10 owns the export/import pipeline).
- The wife's phone — two-device rollout belongs to Story 11; one daily driver is enough to learn
  the baseline.
- Formal usability testing, metrics, or timing instrumentation. This is one informed user living
  with an app, not a study.

## Non-Goals

- Do not attempt exhaustive coverage of every screen or setting; attention goes where daily use
  naturally leads, guided by the checklist below.
- Do not pre-write Milestone 2 stories from speculation — only from observations that actually
  occurred.

## Current Understanding

- The app is built and deployed to the owner's Galaxy S22 Ultra (Story 1, complete), so daily
  use can start immediately.
- Historical data is not yet imported (Story 10 not started), so early use runs on manually
  logged and searched foods. Per the milestone's Interdependency Order this story "runs in
  parallel once data is imported" — but Stories 10/11 are related, not strict blockers, so
  starting now on an empty diary is legitimate and even useful (it exercises the from-scratch
  logging flow that new foods will always need).
- Relevant app surfaces (from `design.md`): the modular Material You home screen, diary/meal
  logging, local food catalog search, opt-in remote databases (Open Food Facts, USDA — Stories
  3/4 unlock these), camera barcode scanning, recipe creation, goals screens, and CSV
  import/export in settings.
- Capture pipeline (from the context-tier system): raw observations land as dictation files in
  `context/dictations-tier-0/` (named per its README, e.g.
  `YYYY-MM-DD_addendum_daily-use-observations.md`), then get synthesized into
  `context/backlog/backlog-1.md` stories and Milestone 2 planning. `backlog-1.md` already
  declares "Friction findings from Milestone 1's daily-use story" as an expected inflow.
- Assumptions and constraints: observations are the owner's subjective experience — that is the
  point; no attempt to make them objective. Dictation is raw intake, not authoritative until
  synthesized.

### Observation Protocol

While using the app normally, pay attention to — and note when something stands out, good or
bad:

1. **Logging flows.** Tap count and speed to log a typical meal versus MyFitnessPal/Lose It
   (the founding complaint was "too many taps"). Repeat-logging of the same foods. Portion
   entry. Where a placeholder-style "I ate something, details later" entry is missed
   (Milestone 2 plans exactly this feature — confirm or refute the need).
2. **Navigation.** How many steps common journeys take (open app → log; find yesterday's
   entry; adjust a goal). Anything that feels buried, and anything that is pleasantly direct.
3. **Search behaviour.** Local catalog search quality on manually created foods; remote-source
   search quality and latency once USDA/Open Food Facts are enabled (Stories 3/4); where AI
   search-query generation (Milestone 2) would actually have helped.
4. **Barcode behaviour.** Scan speed and hit rate on real household groceries — this doubles as
   the first-scan verification pass for the `groceryProducts[]` catalog confidence flags
   (backlog-1 Story 2).
5. **Screens worth keeping.** Which home-screen cards, goal views, and settings earn their
   place; which are noise. This directly informs Milestone 2 ergonomics and identity work.
6. **Anything else.** Delight, surprise, crashes, data oddities — the checklist guides
   attention, it does not limit it.

## Questions / Unknowns

- Q: [STORY 12] Should daily use start now (empty diary, manual logging) or wait until the
  Story 10 import lands so the app is populated with the recovered data?
  Impact: Determines the story's start date and whether observations cover the from-scratch
  experience, the data-rich experience, or both.
  Assumption: Start now; add a short second observation pass after the import lands so search
  and catalog behaviour are also observed against the full dataset. The milestone marks
  Stories 10/11 as related but not strict blockers.
  Status: OPEN
- Q: [STORY 12] Should remote sources (USDA key — Story 3, Open Food Facts login — Story 4) be
  set up before or during this story?
  Impact: Search-behaviour observations are incomplete without the remote sources the app will
  actually run with.
  Assumption: Do them opportunistically during the use period — they are no-code account
  signups and enabling them mid-story is itself a useful observation of the settings flow.
  Status: OPEN
- Q: [STORY 12] One dictation at the end, or rolling notes during the week?
  Impact: End-of-period recall loses small frictions; rolling capture is more faithful but more
  ceremony.
  Assumption: Owner's choice; recommend rolling quick notes (any medium) rolled up into one
  dictation file at the end, so `dictations-tier-0/` receives a single coherent artifact.
  Status: OPEN

## Execution Steps

1. Owner begins using the app as his real daily tracker.
   - Why: Lived use is the entire deliverable.
   - Edits: none (in-app data only, on-device).
   - Dependencies: Story 1 (complete). Optionally enable USDA/OFF mid-period (Stories 3/4).

2. Owner keeps rough notes against the Observation Protocol above as things stand out.
   - Why: Frictions are forgettable; capture at the moment of annoyance beats recall.
   - Edits: none yet — notes in whatever medium is frictionless (voice notes, phone notes).
   - Dependencies: step 1 ongoing.

3. After the guideline period (a few days to a week), owner dictates the roll-up; an agent
   transcribes it into `context/dictations-tier-0/YYYY-MM-DD_addendum_daily-use-observations.md`
   using the intake template from that folder's README.
   - Why: Dictation is the durable raw layer the context-tier workflow synthesizes from.
   - Edits: one new dictation file.
   - Dependencies: step 2.

4. Synthesize the dictation: file each concrete friction/keep/idea finding as a backlog story in
   `context/backlog/backlog-1.md` (its Expected Inflows section already reserves this slot), and
   surface findings that confirm, refute, or reshape Milestone 2 stories as notes for Milestone 2
   planning (update `context/milestones/milestone-2.md` only where a finding clearly changes a
   story's premise).
   - Why: Dictation is not authoritative until promoted into maintained context.
   - Edits: backlog-1.md entries; possibly small Milestone 2 story annotations.
   - Dependencies: step 3.

5. Owner declares the baseline understood; mark Story 12 Complete in
   `context/milestones/milestone-1.md` and fill this plan's Completion Review.
   - Why: The story's completion criterion is explicitly the owner's judgment.
   - Edits: milestone status line; this file.
   - Dependencies: steps 3-4.

## Validation

### Automated Checks

- None — no code is produced. (Markdown lint/link checks on the edited context files if the
  repo's harness gates trigger on them.)

### Manual Checks

1. The dictation file exists in `context/dictations-tier-0/`, follows the naming guidance and
   intake template, and covers each Observation Protocol category (or notes it had nothing).
2. Every actionable finding in the dictation is traceable to either a backlog-1 story or a
   Milestone 2 planning note — nothing actionable stranded in raw dictation.
3. Owner confirms he can describe the baseline app's strengths and frictions from experience.

### Acceptance Criteria

- The owner has used the app daily for roughly a few days to a week and judges the baseline
  understood (the story's own completion criterion — his call, not a checklist).
- Observations are durably captured in `dictations-tier-0/` and synthesized into backlog
  stories / Milestone 2 input per the context-tier workflow.
- No app or code changes were made under this story.

## Risk Mitigation

- Risk: Observations never get written down and the week's learning evaporates.
  Mitigation: Rolling low-ceremony notes (step 2) plus a single scheduled roll-up dictation
  (step 3); the Observation Protocol gives recall a scaffold even if rolling notes lapse.
- Risk: Friction findings get "fixed on the spot" instead of filed, breaking scope discipline.
  Mitigation: Explicit out-of-scope rule — every fix idea becomes a backlog story; nothing ships
  from this story.
- Risk: Observing an empty app misrepresents the data-rich experience.
  Mitigation: Second short observation pass after the Story 10 import (per the open question's
  assumption); note in the dictation which state each observation was made against.
- Risk: The story drags open-ended because "done" is a judgment call.
  Mitigation: Accepted — the milestone explicitly makes owner judgment the completion criterion;
  the few-days-to-a-week guideline bounds it socially, not mechanically.

## Phase Split

Not needed — well under any threshold.

## Evidence / References

- Planning inputs: `context/laws.md`, `context/design.md` (Core Principles, Domain Model,
  Milestone 2 preview), `context/milestones/milestone-1.md` (Story 12, Interdependency Order),
  `context/backlog/README-BACKLOG.md`, `context/backlog/backlog-1.md` (Expected Inflows),
  `context/dictations-tier-0/README-DICTATIONS-TIER-0.md` (naming + intake template).
- Known unverified claims: none — no runtime behavior is asserted by this plan.
