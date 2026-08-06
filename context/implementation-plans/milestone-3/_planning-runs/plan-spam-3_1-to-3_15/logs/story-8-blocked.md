# BLOCKED — STORY 3.8 Message envelope & E2E pipeline

Date: 2026-07-28
Run: plan-spam-3_1-to-3_15

## Why blocked

`context/milestones/milestone-3.md` Story 8 dependency note: "The unknown-version envelope
disposition question (see Relay Contract Conformance) must be settled in the contract before
this story is planned."

Verified in the foodus-relay repo (read-only, `D:\forked-projects\FoodUs-Server`):
wire contract v1 is that repo's Milestone 3 Story 1, **Status: Not Started**. The disposition
question is therefore unsettled and the planning gate is unmet.

## Unblock condition

Owner carries across foodus-relay's contract v1 (or at minimum its settled answer to the
unknown-version envelope disposition: acknowledged-off-mailbox vs left-queued). Then requeue
STORY 3.8 (`BLOCKED` → `TODO`) and dispatch a planning worker.

## No plan file was created

Per workflow policy, a blocked story must not pretend its plan exists.
`context/implementation-plans/milestone-3/story-10-envelope-e2e-pipeline/` is intentionally absent.
