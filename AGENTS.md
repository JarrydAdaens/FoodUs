---
name: agents
description: FoodUs project instructions and task-based context routing.
metadata:
  version: "4.0"
  owner: "Jarryd Adaens"
  repo: "FoodUs (fork of maksimowiczm/FoodYou)"
---
# FoodUs agent instructions

FoodUs is Jarryd Adaens's privacy-first Kotlin Multiplatform food diary fork. The repository retains upstream source namespaces for mergeability; the shipped Android identity is `io.github.jarrydadaens.foodus`. Project decisions and delivery status belong to this repository. The existing milestone, story, backlog, and plan structure is active; no starter profile or generator baseline has been adopted.

## Commands and constraints

- CI builds with JDK 21 using `./gradlew --no-daemon :app:assembleDebug` and runs `./gradlew --no-daemon :app:testDebugUnitTest`. On Windows use `.\gradlew.bat` with the same arguments. Select narrower checks when the change permits; run device or emulator checks when the claim requires them.
- Keep credentials, diary data, and the owner's private relay address out of source, logs, context, and public builds. No accounts, tracking, ads, or cloud diary. The user enters AI credentials and the relay URL on-device. The relay must receive sealed messages only; `foodus-relay` owns its wire contract and server implementation. See Design for the full privacy and cross-repository boundaries.
- `context/laws.md` remains the binding constitutional authority for code, security, and architecture. A short route here does not weaken it. Read its relevant sections for implementation, security-sensitive work, architectural choices, or a conflict; follow the established patterns and resolve law conflicts before editing.
- Never run VCS pull or push. Commit only when the user explicitly requests a commit. For a requested commit log or commit, use the installed `commit-log` skill and default to a copyable log in chat unless a commit was requested.

## Context routing

Start with this file and the files involved in the request. Read `context/agenticworkflow.md` when changing the context system, planning, status ownership, or phase continuation. Read the relevant sections of `context/design.md` for architecture, product scope, privacy, relay, or version decisions; its Milestones Index is the roadmap entry point. Routine bounded edits do not require a full Design or workflow read.

- For milestone or Story work, read the owning `context/milestones/milestone-*.md` and its Story section. Use `context/backlog/` when selecting or recovering unscheduled work. Preserve established Story IDs and status ownership.
- Continue an existing substantial change from its `context/implementation-plans/<milestone>/<story>/plan.md` and any relevant handover. Record acceptance evidence and the next step there. Create a plan or phases when the work needs durable coordination, recovery, or risk control; existing plans remain authoritative for their stories.
- Read `context/dictations-tier-0/` only to synthesize new raw intent, revise scope, or recover rationale. Promote accepted decisions into maintained Design, milestone, Story, backlog, or plan context. Treat historical records and `context/agent-thinking.md` as evidence, not current authority.
- For app releases or version changes, read `context/versioning-policy.md`, the product version sources in `gradle/libs.versions.toml`, the release workflow, and `context/wiki/foodyou-update-procedure.md`; keep the shipped fork version and release tag aligned. Do not substitute starter version examples for this app's policy.

## 0.2: Repository Expectations

Review `harness/README-HARNESS.md` and the module whose documented trigger matches the change. Run matching gates and verifiers, using an independent evaluator sub-agent when available and applicable. A visible or runtime behavior claim needs the matching harness result or a documented reason it could not run. The harness currently lists no installed modules, so choose meaningful tests and direct runtime evidence for the affected behavior.

Investigate discoverable uncertainty before asking. State consequential assumptions, choose the smallest coherent change, and preserve unrelated edits, project contracts, and historical paths. Refactor when necessary for the requested outcome; do not add speculative features or tuning controls unless requested. Validate external inputs at their boundaries and keep sensitive values out of artifacts. New behavior and bug fixes need meaningful regression checks; document a required check that could not run. Update the owning Story or plan when its status, acceptance, or dependency changes. Report changed files, checks, and material limits. Shared skills supplement repository instructions and do not silently change this project's process.
