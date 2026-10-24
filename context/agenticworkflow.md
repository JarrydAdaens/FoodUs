---
name: agenticworkflow
description: FoodUs context ownership, conditional routing, and delivery continuity.
metadata:
  version: "4.0"
  owner: "Jarryd Adaens"
  repo: "FoodUs (fork of maksimowiczm/FoodYou)"
---
# Agentic Workflow

Start with [AGENTS.md](../AGENTS.md). Load the context relevant to the current work; the existing milestones, Stories, and plans are the project's delivery records. [Design](design.md) owns product direction and the Milestones Index. This document owns how those records are used.

## The Context Tier System

FoodUs is backlog-informed and milestone-centric. This file and Design are the two places that define the numbered tiers; elsewhere use their names. `goal` and `task` are not tier names. The retired Sprint tier and old goal-file paths are historical, not new work locations.

| Tier | Name | Owner and use |
| --- | --- | --- |
| 0 | Dictation | `dictations-tier-0/` preserves raw intent and rationale before synthesis. |
| 1 | Design | `design.md` maintains the whole product, architecture, and Milestones Index. |
| 2 | Milestone | `milestones/*.md` owns an outcome, its Story Index, dependencies, and status. |
| 3 | Story | A discrete work unit inside its milestone, or staged in `backlog/*.md` before scheduling. |
| 4 | Implementation Plan | `implementation-plans/<milestone>/<story>/plan.md` tracks a substantial Story's approach, acceptance, evidence, and continuation. |
| 5 | Phase | Optional independently verifiable slices of one plan. |

The backlog is an unscheduled Story pool, informally Milestone -1, rather than another tier. Keep populated records, stable IDs, and historical paths. A new accepted decision belongs in its maintained owner; a raw dictation or old plan does not silently override it.

## Work routing

| Work | Read and update |
| --- | --- |
| Bounded correction | Affected files, relevant laws, and the applicable check. Update a Story only if its acceptance or status changes. |
| Architecture, privacy, relay, or version decision | Relevant Design sections and actual configuration; route release details to the adopted runbook. |
| Milestone planning or Story selection | Design's Milestones Index, the relevant milestone, and backlog entries when choosing unscheduled work. |
| Existing substantial Story | Its milestone Story section, current plan, and handover if present. |
| Story closure, reopening, milestone advancement, or release | [Versioning policy](versioning-policy.md) and authoritative milestone statuses; follow its transition and release mappings. |
| Harness-triggered behavior | Harness index and the matching module contract; record a missing check as an explicit limit. |
| Intent recovery or scope revision | Relevant Dictation, then reconcile the accepted decision into maintained context. |

The milestone contains the authoritative Story status. A plan carries its own execution state and evidence; reconcile both when completion changes. Do not create a duplicate tracker. Existing plans and acceptance gates continue to bind their work even when a new plan would be optional. Build only the context needed now; leave distant backlog items rough until selected.

## Planning and execution

Use one current `plan.md` for a Story that needs durable acceptance, dependencies, verification, or continuation. Keep objective, scope, decisions, concrete checks, blockers, and the next step together. Phase only when a safe sequential split helps execution; a handover belongs beside the plan when a later session needs one. Record completed work and unresolved checks before stopping an interrupted phase. Extra logs, reviews, scratchpads, and other sidecars are optional unless an active plan, user request, or actual consequence requires them.

The project may use shared planning and execution skills, but installation does not make their entire process mandatory. The owner and current project records decide scope. Resolve dependencies before executing dependent work. For the multiplayer work, `foodus-relay` owns the contract and deploys first; the FoodUs app conforms after the owner releases the documented gate. Never invent a server endpoint to bypass that dependency.

## Scoring model: Complexity, Effort, Risk

Complexity describes reasoning difficulty, effort describes work volume, and risk describes possible consequences. Existing numeric CER entries remain historical estimates. New scores are optional unless a current plan or user request calls for them. Use qualitative evidence to choose a safe scope and meaningful checks; an uncalibrated number does not select an executor model or force a phase. High-consequence work needs explicit recovery, required checks, and an independent review when the applicable project contract or consequence calls for it.

## Evidence and handoff

Meaningful checks follow the changed artifact: tests for new behavior and bug fixes, relevant structure checks for documentation and configuration, and device or emulator evidence for runtime claims. Run a matching harness gate when its trigger applies. If a required check cannot run, leave the result incomplete and explain the limit. Keep secrets and private data out of plans and logs.

At a Story boundary, update its milestone status and plan evidence. When a Story closes or reopens, or the active milestone advances, maintain the shared product version under the [versioning policy](versioning-policy.md), including its legacy-version transition. For an interrupted plan, record what changed, what remains, and the next concrete step so a fresh session can continue from repository state. Historical implementation logs and completion reviews remain at their original paths. Do not require a new postmortem, estimate dashboard, advisor, persona, or per-Story commit for routine work.

<!-- rails-lint-allow: VC001 VC002 VC003 VC004 -->
