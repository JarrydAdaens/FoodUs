# Plan Spam Briefing — Milestone 3 (Multiplayer), Run plan-spam-3_1-to-3_15

- **Milestone:** `context/milestones/milestone-3.md` (v3.1)
- **Run slug:** `plan-spam-3_1-to-3_15`
- **Date:** 2026-07-28
- **Requested scope:** all stories in Milestone 3 (client repo only)

## Source documents read

| Document | Why it matters |
| --- | --- |
| `context/laws.md` | Constitutional constraints binding every plan (security, architecture, scope discipline) |
| `context/agenticworkflow.md` | Tier system, plan.md conventions, CER scoring model |
| `context/design.md` (v3.1) | The Multiplayer Exception: relay model, E2E, Keystore, poll-on-wake, relay repo split, contract ownership |
| `context/milestones/milestone-3.md` (v3.1) | The 15 stories, Relay Contract Conformance obligations, interdependency order, staged open decisions |
| `D:\forked-projects\FoodUs-Server\context\milestones\milestone-3.md` (read-only) | Server-side milestone; confirms wire contract v1 is that repo's Story 1, **Not Started** — contract v1 does not exist yet |

## Template source

`C:\Users\Jarry\.claude\skills\rails-planning\references\plan-template.md` (unified rails-planning template, v2.0). This matches the established local pattern — all existing milestone-1/-2 plans follow it.

## Queue (13 candidate stories; 12 queued, 1 blocked)

Stories 4 and 5 are **Externalized** (External Dependency, delivered by foodus-relay; milestone says "Plan: n/a") — deliberately excluded, not silently dropped.

Story 8 is **BLOCKED at queue time**: its dependency note says the unknown-version envelope disposition "must be settled in the contract before this story is planned", and the foodus-relay repo shows contract v1 is Not Started. See `logs/story-8-blocked.md`.

Ordered queue (milestone interdependency order): 1, 2, 3, 15, 6, 7, 9, 10, 11, 12, 13, 14.

## Output folder

`context/implementation-plans/milestone-3/<story-slug>/plan.md` — slugs exactly as pre-declared in the milestone's per-story Plan links.

## Run-level questions index

`context/implementation-plans/milestone-3/_planning-runs/plan-spam-3_1-to-3_15/questions.md`

## Hard constraints for all workers

1. **Client repo only.** The relay server lives in `D:\forked-projects\FoodUs-Server` (foodus-relay). Workers may read it, never edit it. No server-side scope in any plan.
2. **Contract v1 does not exist yet.** No plan may invent concrete wire-contract details (endpoint paths, envelope field names, auth handshake). Relay-facing surfaces are described as contract-dependent seams referencing the foodus-relay contract-v1 deliverable, with uncertainty marked.
3. **Relay Contract Conformance** (milestone section) binds every relay-consuming plan: version stamping, refuse-loudly, two-way tolerance, capability-aware UI, HTTPS-only via Ktor, app→server dependency notes, server-leads ordering.
4. **Staged open decisions** in Stories 9, 10, 11 are carried into those plans as explicit OPEN decision points (owner input where marked); those plans stay `Status: Draft` until resolved.
5. Plans with unresolved owner/contract gates: `Status: Draft`. Local-only plans with no gates: `Status: Ready`.
6. Fork philosophy: additive overlays, minimal merge-conflict surface with upstream.

## Batching decision

One plan per story (no grouping). Parallel dispatch in batches respecting no file collisions; planning artifacts are independent files, so milestone interdependency order constrains execution, not plan authorship — but workers for downstream stories are told which upstream plans exist.
