# Rails Boss Execute Briefing — Milestone 2 (all stories)

Date: 2026-07-25
Run scope: every story in `context/milestones/milestone-2.md` (Stories 1–19), executed sequentially.
Previous run (Milestone 1) is recorded in git history of this file.

## Source documents read

| Document | Why it matters |
| --- | --- |
| `context/milestones/milestone-2.md` | The 19-story queue, dependencies (Interdependency Order), and acceptance notes |
| `context/laws.md` | Constitutional code/security/architecture constraints |
| `context/design.md` | Fork philosophy (additive overlays, mergeability), domain model, architecture |
| `context/agenticworkflow.md` | Plan-artifact conventions (`plan.md` Execution Log / Completion Review) |
| `context/implementation-plans/milestone-2/story-14…19/plan.md` | Execution plans for Stories 14–19 (read by their workers at dispatch) |

## Constraints extracted

- **Sequential execution (Parallelism: 0)** — user-mandated; one worker at a time, numeric story order 1→19 (this order satisfies every dependency in the milestone's Interdependency Order).
- **Workers run on Opus** — user-mandated; escalate to the Boss only if a worker is truly stuck.
- **Emulator validation for every story** — user-mandated: build, deploy to AVD `foodyou`, launch, and validate the story's behavior. For research stories (12, 14) with no app code change, validation is the produced document plus an unchanged-build check.
- **A real commit per story via the `commit-log` skill** — user-mandated. Skill lives at `C:\Users\Jarry\.claude\skills\commit-log\SKILL.md` (template in `references/commit-log-template.md`). **No pushes, ever** (AGENTS.md).
- **Toolchain** — JDK 21 at `C:\Java\jdk-21.0.12+8`; SDK at `C:\Users\Jarry\AppData\Local\Android\Sdk`; `./gradlew.bat :app:assembleDebug`; APK at `app/build/outputs/apk/debug/app-debug.apk`; AVD `foodyou` (Pixel 6, API 36) launched detached via `Start-Process emulator.exe -avd foodyou`. Background shell jobs get reaped when a turn yields — long builds run foreground (Gradle resumes from cache on timeout).
- **Secrets** — the AI endpoint key (Stories 6, 7, 9) must never be committed or appear in context files; implement a local injection seam (e.g. `local.properties` / env var read at build time) with graceful no-key behavior. Emulator validation of the actual AI call is limited to the no-key path unless a key is locally present.
- **PII rule** — no `BigAnt` / `Big Ant` string in any artifact (use "Take Out:" replacement convention for food data).
- **Fork philosophy** — additive overlays, minimal merge-conflict surface with upstream, on every story.
- **Owner-unavailable policy** — workers proceed on documented assumptions and record them (plan `## Execution Log` when a plan exists, otherwise the worker report + progress notes). Owner-only externals (e.g. real API keys, store listings) become checklists, not blockers, where local work can still land.
- No specialist agent persona is mandated by any source document for this run; workers are general-purpose on Opus.

## Ordered queue

Stories 1–19 of `context/milestones/milestone-2.md`, in numeric order. Key dependency notes:

| Order | Story | Dependency note |
| --- | --- | --- |
| 1–4 | Identity / About stories | 1 first; 3 and 4 touch the same About screen after 1 |
| 5 | Four-button meal rework | Hosts 6 and 8 |
| 6, 7 | AI scanning, then gallery source | 7 extends 6 |
| 8, 9 | Placeholder, then meta screen | 9 resolves 8; shares AI plumbing with 6 |
| 10, 11 | Graph / week-layout settings | Independent |
| 12 | Upstream issue triage (Research) | Output: backlog stories, no app code |
| 13 | Reusable meal templates | Independent |
| 14 | Provider/Quick Add spike (Research) | Blocks 15–19; resolves spec §13 decisions |
| 15 | AFCD provider | Needs 14; establishes shared pipeline |
| 16 | FoodSwitch provider | Gated on 14's feasibility verdict; reuses 15 |
| 17 | Provider update-check UI | Needs 14 + 15 |
| 18 | Quick Add fields + migration | Needs 14 |
| 19 | Quick Add promotion | Needs 14 + 18 |
