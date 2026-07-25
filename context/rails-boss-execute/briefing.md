# Rails Boss Execute Briefing — Milestone 1 incomplete stories

Date: 2026-07-25
Run scope: all incomplete Milestone 1 stories, starting with Story 5, executed sequentially.

## Source documents read

| Document | Why it matters |
| --- | --- |
| `context/milestones/milestone-1.md` | Story list, statuses, and Interdependency Order for this run |
| `context/laws.md`, `context/design.md`, `context/agenticworkflow.md` | Constitutional constraints, domain model, plan-artifact conventions |
| `context/implementation-plans/milestone-1/story-5-master-data-format/plan.md` | Story 5 execution plan |
| `context/implementation-plans/milestone-1/story-10-export-script/plan.md` | Story 10 execution plan |
| `context/implementation-plans/milestone-1/story-11-app-update-mechanism/plan.md` | Story 11 execution plan |
| `context/implementation-plans/milestone-1/own-project-infrastructure/plan.md` | Story 13 execution plan |
| `context/implementation-plans/milestone-1/story-15-cicd-pipeline/plan.md` | Story 15 execution plan |
| `context/wiki/foodyou-docs-site-zensical.md` | Story 14 delete-set reconnaissance (Story 14 has no plan; the wiki doc + milestone section are its spec) |

## Constraints extracted

- **Sequential execution (Parallelism: 0)** — user-mandated; one worker at a time.
- **A real commit after every story** — user-mandated ("ensure commits are done between runs");
  workers use the installed `commit-log` skill. **No pushes, ever** (AGENTS.md).
- **Emulator for deployment/testing** — AVD `foodyou` (Pixel 6, API 36), JDK 21 at
  `C:\Java\jdk-21.0.12+8`, SDK at `C:\Users\Jarry\AppData\Local\Android\Sdk`;
  `./gradlew.bat :app:assembleDebug`; background shell jobs get reaped — long builds run
  foreground; emulator launched detached via `Start-Process`.
- **Open plan questions**: the owner is not available mid-run; workers proceed on each plan's
  recorded assumptions and log the assumption-based decisions in the plan's `## Execution Log`.
- **PII rule**: no `BigAnt` / `Big Ant` string may appear in any artifact.
- Owner-only remote/device steps (GitHub settings, secrets, phone installs, account signups)
  are out of worker reach: workers complete everything local and list the owner checklist.
- No specialist agent persona is mandated by any source document for this run.

## Ordered queue

| Order | Story | Why this position |
| --- | --- | --- |
| 1 | STORY 5 — master data format | User-mandated start; unblocks Story 10 |
| 2 | STORY 10 — export script | Needs Story 5's canonical format; emulator import validation |
| 3 | STORY 14 — remove docs site | Independent; removes `docs.yml` before Story 15 touches workflows |
| 4 | STORY 15 — CI/CD pipeline | After 14 (docs.yml gone); local workflow authoring + local validation |
| 5 | STORY 11 — update mechanism | Automatable slice (procedure, scripts, versioning); keystore/device steps owner-gated |
| 6 | STORY 13 — own infrastructure | Consumes Story 15's workflows; local sweep + owner badge checklist |
| — | STORY 3 — USDA API key | BLOCKED: external account signup, owner-only |
| — | STORY 4 — Open Food Facts login | BLOCKED: external account signup, owner-only |
