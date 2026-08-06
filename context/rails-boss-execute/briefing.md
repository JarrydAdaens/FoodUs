# Rails Boss Execute Briefing — Milestone 3 run (2026-07-28)

Run scope: all Milestone 3 stories that have implementation plans, executed sequentially.
Previous runs (Milestone 1; Milestone 2) are recorded in this file's git history.

## Source documents read

| Document | Why it matters |
| --- | --- |
| `context/laws.md` | Constitutional constraints; §2 Secrets/Data Boundaries binds Story 3.3 directly |
| `context/agenticworkflow.md` | Plan-artifact conventions (`plan.md` Execution Log / Completion Review) |
| `context/milestones/milestone-3.md` (v3.1) | Story scope, Relay Contract Conformance, interdependency order |
| `_planning-runs/plan-spam-3_1-to-3_15/progress.md` | Plan readiness: 3.1/3.2/3.13 Ready; rest Draft; 3.8 BLOCKED (no plan) |
| `story-06-crypto-identity/plan.md` (full read) | Draft-gate analysis: local key work explicitly NOT blocked |
| Status/gate lines of all 12 `plan.md` files | Grep-verified readiness and migration assumptions |

## Scope decision

Dispatch (dependency order): **3.1 → 3.2 → 3.3 (partial) → 3.15 (partial) → 3.13**.

- 3.1, 3.2, 3.13 are `Ready` — full stories.
- 3.3 is `Draft` but its plan states "The local key work below is NOT blocked"; executed **to the
  documented seam** (local Keystore identity only; relay registration stays out of scope). Plan
  stays `Draft`; milestone status becomes In Progress, not Complete.
- 3.15 is `Draft` solely because the connection check "cannot be finished — only seamed";
  executed **to the seam** (settings UI, persistence, URL validation; the capability checker is
  an interface whose implementation reports unreachable/unknown — no invented endpoint paths,
  the wire contract is unwritten). Plan stays `Draft`; milestone status In Progress.

Blocked, not dispatched: 3.6, 3.7 (Story 5 relay-deployment gate unreleased); 3.8 (no plan —
unknown-version disposition unsettled in the contract); 3.9–3.12 (ride Story 8's pipeline,
which does not exist; plans held Draft on owner decisions); 3.14 (manual two-phone proof
against the live relay). The foodus-relay repo is mid-implementation tonight by another agent —
workers in this run do not read that repo.

## Boss rulings (cross-plan reconciliation)

1. **Room schema sequencing:** every worker rebases its schema bump onto the then-current
   `FoodYouDatabase.VERSION` (story-12 plan's own rule). Expected: 3.2 → 36→37, 3.3 → 37→38,
   3.13 → 38→39 (its planned 36→37 AutoMigration is stale — rebase it).
2. **Story 3.3 Q3 (key-column ownership):** each story owns its additive migration — 3.2 ships
   `ProfileEntity` **without** key columns (its own documented assumption; laws §7
   traceability); 3.3 adds nullable public-key/algorithm columns in its own migration.
3. **Story 3.3 re-key crash-consistency (critic finding):** a single fixed Keystore alias
   cannot keep the old key intact until the new pair exists — do not claim atomicity the
   Keystore cannot provide. The worker implements a mismatch-recovery strategy (e.g. Keystore
   as source of truth: if the stored public key disagrees with the alias's current key,
   reconcile the record) and documents the chosen strategy in the plan's Execution Log.

## Constraints extracted

- **Parallelism: 0** (serial, max 1 active subagent) — user-mandated; all five stories also
  touch shared seams (`FoodYouDatabase.kt`, DI modules, shell UI).
- **Workers run on Opus** — user-mandated.
- **Per worker: compile + unit tests + a real commit via the `commit-log` skill** —
  user-mandated. Instrumented tests are written where the plan requires them; running them
  needs the emulator and is optional — document the gap if not run. **No pushes, ever.**
- **Path-scoped staging** — workers stage only files they changed (never `git add -A`).
- **Boss owns status updates** — workers do not touch `milestone-3.md` or boss `progress.md`;
  the Boss updates statuses after accepting each result, in a separate Boss commit.
- **Toolchain** — JDK 21 at `C:\Java\jdk-21.0.12+8`; SDK at
  `C:\Users\Jarry\AppData\Local\Android\Sdk`; `./gradlew.bat :app:assembleDebug`; AVD `foodyou`
  (Pixel 6, API 36) launched detached via `Start-Process emulator.exe -avd foodyou`;
  applicationId `io.github.jarrydadaens.foodus`, source namespace `com.maksimowiczm.foodyou`.
  Background shell jobs get reaped when a turn yields — long builds run foreground; Gradle
  resumes from cache on timeout.
- **Open questions in plans** — no owner available mid-run; workers proceed on each plan's
  documented assumptions (as amended by the Boss rulings above) and record decisions in the
  plan's `## Execution Log`.
- **Fork philosophy** — additive overlays, minimal upstream merge surface.
- No specialist agent persona is mandated by any source document; workers are general-purpose
  on Opus.

## Ordered queue

| Order | Story | Plan | Dependency note |
| --- | --- | --- | --- |
| 1 | STORY 3.1 — Tabbed UI shell | `story-01-tabbed-ui-shell/plan.md` | None; every other surface hangs off it |
| 2 | STORY 3.2 — Profile | `story-02-profile/plan.md` | Needs 3.1's Groups tab |
| 3 | STORY 3.3 — Crypto identity (partial) | `story-06-crypto-identity/plan.md` | Hooks 3.2's create-profile flow; local seam only |
| 4 | STORY 3.15 — Relay URL setting (partial) | `story-07-relay-url-setting/plan.md` | Local seam only; checker stubbed |
| 5 | STORY 3.13 — Notification Center tab | `story-05-notification-center/plan.md` | Fills 3.1's Notifications stub; emitters 3.9-3.12 absent — wire existing local events only |
