# Plan Spam Questions — Milestone 3 (Multiplayer), Run plan-spam-3_1-to-3_15

Milestone: `context/implementation-plans/milestone-3/`
Run: `context/implementation-plans/milestone-3/_planning-runs/plan-spam-3_1-to-3_15/`
Date: 2026-07-28
Source: `context/milestones/milestone-3.md` (v3.1), `context/design.md` (v3.1)

Per-plan questions live inside each plan's `## Questions / Unknowns` section under
`context/implementation-plans/milestone-3/`. This file indexes them and carries
cross-run Boss questions.

## Per-Plan Questions Index

| Story | Plan Folder | Questions | Status |
| --- | --- | --- | --- |
| STORY 3.1 | `context/implementation-plans/milestone-3/story-01-tabbed-ui-shell/` | [plan.md#questions--unknowns](../../story-01-tabbed-ui-shell/plan.md#questions--unknowns) | OPEN |
| STORY 3.2 | `context/implementation-plans/milestone-3/story-02-profile/` | [plan.md#questions--unknowns](../../story-02-profile/plan.md#questions--unknowns) | OPEN |
| STORY 3.3 | `context/implementation-plans/milestone-3/story-06-crypto-identity/` | [plan.md#questions--unknowns](../../story-06-crypto-identity/plan.md#questions--unknowns) | OPEN |
| STORY 3.6 | `context/implementation-plans/milestone-3/story-08-friend-codes/` | [plan.md#questions--unknowns](../../story-08-friend-codes/plan.md#questions--unknowns) | OPEN |
| STORY 3.7 | `context/implementation-plans/milestone-3/story-09-friends-list/` | [plan.md#questions--unknowns](../../story-09-friends-list/plan.md#questions--unknowns) | OPEN |
| STORY 3.8 | `context/implementation-plans/milestone-3/story-10-envelope-e2e-pipeline/` | n/a — planning blocked | BLOCKED |
| STORY 3.9 | `context/implementation-plans/milestone-3/story-11-groups/` | [plan.md#questions--unknowns](../../story-11-groups/plan.md#questions--unknowns) | OPEN |
| STORY 3.10 | `context/implementation-plans/milestone-3/story-12-save-to-group/` | [plan.md#questions--unknowns](../../story-12-save-to-group/plan.md#questions--unknowns) | OPEN |
| STORY 3.11 | `context/implementation-plans/milestone-3/story-13-receive-into-diary/` | [plan.md#questions--unknowns](../../story-13-receive-into-diary/plan.md#questions--unknowns) | OPEN |
| STORY 3.12 | `context/implementation-plans/milestone-3/story-14-suggestion-queue/` | [plan.md#questions--unknowns](../../story-14-suggestion-queue/plan.md#questions--unknowns) | OPEN |
| STORY 3.13 | `context/implementation-plans/milestone-3/story-05-notification-center/` | [plan.md#questions--unknowns](../../story-05-notification-center/plan.md#questions--unknowns) | OPEN |
| STORY 3.14 | `context/implementation-plans/milestone-3/story-15-household-proof/` | [plan.md#questions--unknowns](../../story-15-household-proof/plan.md#questions--unknowns) | OPEN |
| STORY 3.15 | `context/implementation-plans/milestone-3/story-07-relay-url-setting/` | [plan.md#questions--unknowns](../../story-07-relay-url-setting/plan.md#questions--unknowns) | OPEN |

## Boss Questions

- Q: Room schema sequencing: Stories 3.2, 3.3, 3.6, 3.9, 3.12, and 3.13 each plan an additive migration and several independently assume "version 36 → 37". Execution must serialize these bumps (each story takes the next version number at execution time); plans should not be read as owning a specific version number.
  Affects: `STORY 3.2, STORY 3.3, STORY 3.6, STORY 3.9, STORY 3.12, STORY 3.13`
  Assumption: Whichever storage story executes first takes 37; later ones renumber at execution. No plan revision needed now.
  Status: OPEN
  Answer: —

- Q: Story 3.13's worker flagged that `SharedFlowEventBus` drops events on overflow (50-buffer, DROP_LATEST); Story 3.8's poll-drain could batch many messages at once. Carry this into Story 3.8's plan when it is unblocked (record notifications transactionally or raise the buffer).
  Affects: `STORY 3.8, STORY 3.13`
  Assumption: Handled at Story 3.8 planning time; noted in Story 3.13's Risk Mitigation.
  Status: OPEN
  Answer: —

- Q: Story 8 planning is gated on the unknown-version envelope disposition being settled in the foodus-relay wire contract, and contract v1 is Not Started in that repo. Should Story 8 stay blocked until the owner carries contract v1 across, or does the owner want a provisional plan despite the milestone's explicit gate?
  Affects: `STORY 3.8` (and downstream 3.9–3.12 execution sequencing, not their planning)
  Assumption: Story 8 stays BLOCKED for this run; every other plan references its pipeline as an interface dependency without inventing wire details.
  Status: OPEN
  Answer: —

- Q: Cross-plan schema coordination: Story 3.6's plan recommends Story 3.2's profile entity declare the nullable friend-code column from day one (avoiding an early Room migration), while Story 3.2's plan explicitly assumes each story owns its own additive migration (every changed line traceable to its story). Which convention wins?
  Affects: `STORY 3.2, STORY 3.6` (and STORY 3.3's public-key column by the same logic)
  Assumption: Story 3.2's per-story migration stance stands until the owner says otherwise; Room additive migrations are cheap and the traceability argument aligns with laws.md §7.
  Status: OPEN
  Answer: —

- Q: Story 3.11's never-dropped invariant vs laws.md §2 input validation: the plan sanitizes and still lands nearly everything, but a hard-invalid payload (no name AND no nutrition) produces a loud "failed to add" Notification Center event instead of an insertion. The milestone's own Story 13 event list anticipates "meal failed to add", and laws.md §2 is constitutional, so this reading was accepted — but the owner should confirm this bounded exception to "a delivered entry always lands".
  Affects: `STORY 3.11` (and STORY 3.12's accept path, which reuses the resolver)
  Assumption: Loud-failure on hard-invalid payloads is the intended reading; silent drops remain forbidden in all cases.
  Status: OPEN
  Answer: —

- Q: The staged owner decisions (Story 9: group-block notice wording, meal-plan editing scope; Story 10: None-trust groups hidden vs greyed; Story 11: date carry-over and accept-time matching assumptions) remain unresolved. Their plans are written with these embedded as OPEN decision points and held at `Status: Draft`. Confirm each before execution.
  Affects: `STORY 3.9, STORY 3.10, STORY 3.11, STORY 3.12`
  Assumption: Draft plans are acceptable deliverables for this run; none go `Ready` until the owner resolves their marked decisions.
  Status: OPEN
  Answer: —
