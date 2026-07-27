# Rails Boss Execute Briefing — Milestone 2 addendum run (Stories 21-23)

Date: 2026-07-27
Run scope: Stories 21, 22, 23 of `context/milestones/milestone-2.md` (the 2026-07-26 addendum work), executed sequentially.
Previous runs (Milestone 1; Milestone 2 Stories 1-19) are recorded in this file's git history.

## Source documents read

| Document | Why it matters |
| --- | --- |
| `context/milestones/milestone-2.md` | Story text, statuses, and Interdependency Order note 11 (Story 22 precedes Story 21) |
| `context/laws.md` | Constitutional code/security/architecture constraints (§2 Secrets is central to Stories 21-22) |
| `context/design.md` | Fork philosophy, "Secrets and Credentials", and "AI boundary" — design authority for the three-layer prompt and user-entered key |
| `context/agenticworkflow.md` | Plan-artifact conventions (`plan.md` Execution Log / Completion Review) |
| `context/implementation-plans/milestone-2/story-22-ai-settings-screen/plan.md` | Story 22 execution plan (CER 4/5/4) |
| `context/implementation-plans/milestone-2/story-21-three-layer-ai-prompts/plan.md` | Story 21 execution plan (CER 4/4/3); declares Story 22 a hard dependency |
| `context/implementation-plans/milestone-2/story-23-provider-website-links/plan.md` | Story 23 execution plan (CER 2/2/1); independent |

## Constraints extracted

- **Sequential execution (Parallelism: 0)** — user-mandated; one worker at a time.
- **Execution order 22 → 21 → 23** — the user listed "21, 22, 23" as scope; both plans and the
  milestone's Interdependency Order state Story 22 must execute before Story 21 (21 consumes 22's
  `AiSettings` repository and scanner-injection seams). Story 23 is independent and runs last.
- **Workers run on Opus** — user-mandated.
- **Emulator validation for every story** — user-mandated: compile, build, deploy to AVD `foodyou`,
  launch, and briefly verify the story's behavior before declaring done.
- **A real commit per story via the `commit-log` skill** — user-mandated. **No pushes, ever** (AGENTS.md).
- **Path-scoped staging** — workers stage only the files they changed (never `git add -A`), so
  Boss-owned state files can never leak into a worker commit.
- **Boss owns status updates** — workers do not touch `milestone-2.md` or `progress.md`; the Boss
  updates story status and progress after accepting each result, in a separate Boss commit.
- **Toolchain** — JDK 21 at `C:\Java\jdk-21.0.12+8`; SDK at `C:\Users\Jarry\AppData\Local\Android\Sdk`;
  `./gradlew.bat :app:assembleDebug`; APK at `app/build/outputs/apk/debug/app-debug.apk`; AVD
  `foodyou` (Pixel 6, API 36) launched detached via `Start-Process emulator.exe -avd foodyou`;
  applicationId `io.github.jarrydadaens.foodus`. Background shell jobs get reaped when a turn
  yields — long builds run foreground (Gradle resumes from cache on timeout).
- **Secrets (laws §2)** — no AI credential may be committed, logged, or echoed into context files.
  No real API key is available to this run: live-call checks (Validate green tick, layer-2 steering
  probes) are documented as unverified; error paths are exercised with garbage keys instead.
- **Open questions in plans** — no owner is available mid-run; workers proceed on each plan's
  documented assumptions and record what they did in the plan's `## Execution Log`.
- **Fork philosophy** — additive overlays, minimal upstream merge-conflict surface, source
  namespace `com.maksimowiczm.foodyou` untouched.
- No specialist agent persona is mandated by any source document; workers are general-purpose on Opus.

## Ordered queue

| Order | Story | Plan | Dependency note |
| --- | --- | --- | --- |
| 1 | STORY 2.22 — AI settings screen | `story-22-ai-settings-screen/plan.md` | None; provides `AiSettings` + runtime-config seams for 21 |
| 2 | STORY 2.21 — Three-layer AI prompts | `story-21-three-layer-ai-prompts/plan.md` | Hard dependency on 22 |
| 3 | STORY 2.23 — Provider website links | `story-23-provider-website-links/plan.md` | Independent |
