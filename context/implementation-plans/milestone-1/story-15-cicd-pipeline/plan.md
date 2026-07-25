# Plan: Establish Fork CI/CD Pipeline

## Metadata

- Task Type: `STORY`
- Status: `Draft`
- Owner: Jarryd Adaens
- Last Updated: 25 July 2026

## Linked Context

- Milestone: [context/milestones/milestone-1.md](../../../milestones/milestone-1.md)
- Story: [Story 15: Establish fork CI/CD pipeline](../../../milestones/milestone-1.md#story-15)
- Backlog source: none — synthesized directly from the owner's CI/CD plan
- Dictation source: [2026-07-25 CI/CD plan](../../../dictations-tier-0/2026-07-25_cicd_plan_github-actions.md)
  (owner-authored; its `VERIFY` items are resolved in this plan)
- Related Plans:
  - [Story 11: App update mechanism](../story-11-app-update-mechanism/plan.md) — owns the keystore
    and the signing/distribution decision; this story consumes its secrets and delivers its
    release channel
  - [Story 13: Own project infrastructure](../own-project-infrastructure/plan.md) — consumes this
    story's workflows for README badge verification; no longer owns any workflow files
  - Story 14 (remove the docs site, no plan yet) — owns deleting `.github/workflows/docs.yml` and
    `docs/`; explicitly **out of this story's scope**
- External Tooling: `actionlint` (or YAML parse) for workflow validation; `commit-log` skill

## CER

- Complexity: 3
- Effort: 4
- Risk: 5
- Notes: Inline estimate by the planning agent, not a formal `rails-grade-cer` grade. Complexity
  is moderate — three workflow files with well-understood mechanics, but cross-story boundaries
  (11/13/14) and a branch-model change need care. Effort is moderate — one new workflow, one
  rework, one retarget, plus an owner checklist. Risk is elevated because everything
  remote-side (Actions enablement, default-branch switch, secrets, actual runs) can only be
  verified by the owner after push — the agent may never push — and the signing step handles
  private key material.

## Objective

Give the fork a real CI/CD pipeline: every push to the fork's trunk branches is compiled and
tested (`ci.yml`, which upstream never had), a `v*` tag or manual dispatch produces a signed APK
published as a durable GitHub Release (`release-apk.yml` rework), and the meals-validation
workflow can no longer fire on the upstream-mirror branch (`validate-meals.yml` retarget) — all
without touching app source or Gradle configuration.

## Scope

### In Scope

- `.github/workflows/ci.yml` — new compile-and-test workflow on `jarryd/main` / `jarryd/develop`
  pushes and PRs, plus manual dispatch.
- `.github/workflows/release-apk.yml` — rework: tag trigger, `contents: write` permission,
  hardened signing (keystore in `$RUNNER_TEMP`, quoted expansions, `printf` not `echo`, cleanup
  step), real GitHub Release via `gh release create`, artifact retention 30 days, JDK 17 → 21.
- `.github/workflows/validate-meals.yml` — add `jarryd/main` / `jarryd/develop` branch filters to
  both triggers; job body untouched.
- A written owner checklist of remote-side prerequisites (enable Actions, default branch →
  `jarryd/main`, create signing secrets) — enumerated in this plan's Execution Steps, executed by
  the owner.

### Out Of Scope

- Deleting `docs.yml` and `docs/` — Story 14 owns the entire docs-site delete-set.
- Keystore generation, custody, and the distribution-channel decision — Story 11.
- README badge wiring/verification, issue templates, `metadata/` — Story 13.
- Any change under `app/`, `shared/`, `build.gradle.kts` files, or the version catalog. Signing
  stays post-build via `apksigner`; no Gradle `signingConfig` is added (source plan §6.5).
- Instrumented/emulator tests in CI (slow, brittle; `androidInstrumentedTest` exists but is
  deliberately not wired up — revisit later if needed).
- §8 hardening options (branch protection, Dependabot, SHA-pinned actions, version-bump
  automation, Play App Signing) — on record in the dictation, deliberately deferred.

## Non-Goals

- No static-analysis step: **verified** — the version catalog has no detekt/ktlint/spotless and no
  lint configuration beyond AGP defaults. Per the source plan's own rule, the step is left out
  rather than invented.
- No iOS build/test in CI: the KMP iOS targets need macOS runners and serve no Milestone 1 goal.
- No GitHub Pages enablement.

## Current Understanding

Verified against the repository on 2026-07-25:

- Likely files or directories:
  - `.github/workflows/docs.yml` — fires on push to `main`, deploys upstream's docs site. Owned
    by Story 14; untouched here.
  - `.github/workflows/release-apk.yml` — `workflow_dispatch` only; JDK 17; installs
    `build-tools;36.0.0` via `$ANDROID_HOME/cmdline-tools/16.0/bin/sdkmanager`; runs
    `assembleRelease` → `app/build/outputs/apk/release/app-release-unsigned.apk`; zipaligns and
    signs with `apksigner` using secrets `KEYSTORE` (base64), `KEY_ALIAS`, `KEYSTORE_PASSWORD`
    (only `--ks-pass`); uploads a 7-day artifact. The keystore is decoded into the **workspace**
    (`> keystore`) with an unquoted `echo` — both fixed by this story.
  - `.github/workflows/validate-meals.yml` — path-filtered on meal JSON + its script; **no branch
    filter**, so it can fire on `main`; body (`jq` + `dev/test-meals-localization.bash`) is fine.
- Toolchain facts (source-plan `VERIFY` items resolved):
  - **JDK is 21, not 17**: `app/build.gradle.kts` sets `jvmTarget = JVM_21` and
    source/target compatibility `VERSION_21` (same in `shared/barcodescanner`). The inherited
    workflow's JDK 17 cannot compile this project; `ci.yml` uses 21 and the rework bumps
    `release-apk.yml` to 21.
  - **`assembleDebug`**: the `:app` module is a KMP module with `androidTarget()`, so
    `:app:assembleDebug` exists and compiles `commonMain` + `androidMain` (shared modules build
    as dependencies). Cheapest full-compile check available; confirm exact task list at execution
    with `./gradlew :app:tasks --all`.
  - **Unit tests exist**: `app/src/commonTest/` holds real kotlin-test sources (Room migration
    tests, `RfcCsvParserTest`, `CsvParserImplTest`), so the `testDebugUnitTest` step stays.
    Confirm exact task name (`:app:testDebugUnitTest`) at execution.
- Branch facts: only `main` and `jarryd/main` exist (locally and on origin). **`jarryd/develop`
  does not exist yet** — see Questions. `origin/HEAD` currently points at `main`, confirming the
  default branch still needs switching for `workflow_dispatch` to be usable.
- Existing behaviors to preserve: `validate-meals.yml` job body byte-identical; local build and
  deploy flow (Story 1) unaffected; `main` stays a pristine upstream mirror with zero fork CI.
- Interfaces, data contracts, or external dependencies: GitHub Actions on
  `JarrydAdaens/FoodYou`; existing secret names `KEYSTORE`, `KEY_ALIAS`, `KEYSTORE_PASSWORD`
  (kept — Story 11's keystore feeds them); pre-installed `gh` CLI on ubuntu runners.
- Assumptions and constraints:
  - Agents never push (`AGENTS.md`); every remote-side check is owner-executed.
  - Fork philosophy: workflow edits conflict with upstream on rebase; keep `ci.yml` additive and
    the `release-apk.yml` / `validate-meals.yml` diffs minimal and well-isolated.
  - Public repo: runner minutes are free; GitHub Releases are publicly downloadable (see
    Questions).

## Questions / Unknowns

- Q: [STORY 15] Create `jarryd/develop` now, or leave the CI triggers naming it dormant until the
  branch is created?
  Impact: The source plan's branch model assumes a develop branch that does not exist yet;
  triggers naming a nonexistent branch are harmless but unexercised.
  Assumption: Keep both branches in the triggers as the source plan specifies; the owner creates
  `jarryd/develop` when day-to-day integration warrants it. No workflow change needed then.
  Status: OPEN
- Q: [STORY 15] Does the keystore's key password equal the keystore password (source plan §4.3
  gotcha — current signing passes only `--ks-pass`)?
  Impact: If they differ, a fourth secret `KEY_PASSWORD` and `--key-pass "pass:${KEY_PASSWORD}"`
  must be added or signing fails.
  Assumption: Same password (single-key store generated that way by Story 11); decide when the
  keystore actually exists — blocked on Story 11's keystore creation.
  Status: OPEN
- Q: [STORY 15] Are publicly downloadable GitHub Releases acceptable as the release channel?
  Impact: The repo is public, so tagged releases and their APKs are world-visible. Fine for
  Milestone 1's unmodified builds; Story 11's plan already flags that Milestone 2's AI-key-bearing
  builds can never ship this way.
  Assumption: Yes for Milestone 1; the private-channel question stays owned by Story 11 / the
  Milestone 2 AI story.
  Status: OPEN
- Q: [STORY 15] Should manual (non-tag) dispatch of the release workflow also require the tag
  naming convention, or keep producing artifact-only builds?
  Impact: Determines whether ad-hoc builds ever become Releases or stay 30-day artifacts.
  Assumption: Ad-hoc dispatch keeps producing artifacts only; `gh release create` runs only on
  `refs/tags/` (as the source plan's `if:` already encodes).
  Status: OPEN

## Execution Steps

1. Owner prerequisites (remote-side, owner-executed; agent lists, never performs).
   - Why: Nothing runs until Actions is enabled; `workflow_dispatch` buttons only appear for the
     default branch's workflow files.
   - Edits: none in-repo. Checklist: (a) enable Actions on the fork; (b) Settings → General →
     default branch → `jarryd/main`; (c) after Story 11 creates the keystore: add secrets
     `KEYSTORE` (base64 one-liner), `KEY_ALIAS`, `KEYSTORE_PASSWORD` (+ `KEY_PASSWORD` if Q2
     lands that way).
   - Dependencies: (c) blocks step 3's live verification only; steps 2-4 can merge before it.

2. Add `.github/workflows/ci.yml` (new file, additive).
   - Why: The compile-and-test safety net upstream lacks; also what Story 13's Build badge will
     point at.
   - Edits: new workflow per the dictation's §5 Task 2 skeleton with these adaptations — JDK
     **21** (not 17); `./gradlew --no-daemon :app:assembleDebug` then
     `./gradlew --no-daemon :app:testDebugUnitTest`; no static-analysis step (none exists);
     concurrency group `ci-${{ github.ref }}` with cancel-in-progress; `permissions:
     contents: read`; 45-minute timeout; failure-only upload of `app/build/reports/`.
   - Dependencies: none.

3. Rework `.github/workflows/release-apk.yml` (edit, minimal diff).
   - Why: Releases must be durable and tag-driven; the signing step currently writes the keystore
     into the workspace where an artifact glob could exfiltrate it.
   - Edits: add `push: tags: ['v*']` trigger (keep `workflow_dispatch`); add `permissions:
     contents: write`; bump JDK 17 → 21; harden signing — `printf '%s' "$KEYSTORE_BASE64" |
     base64 -d > "$RUNNER_TEMP/release.jks"`, quote all secret expansions, `if: always()` cleanup
     `rm -f "$RUNNER_TEMP/release.jks"`; on tag refs, `mv signed.apk
     "FoodYou-${GITHUB_REF_NAME}.apk"` and `gh release create "$GITHUB_REF_NAME" ...
     --generate-notes` with `GH_TOKEN: ${{ github.token }}`; keep the artifact upload for non-tag
     dispatch, retention 30 days. Keep the `build-tools;36.0.0` sdkmanager step and post-build
     `apksigner` approach unchanged.
   - Dependencies: live verification needs step 1(c) and Story 11's keystore; the file edit
     itself does not.

4. Retarget `.github/workflows/validate-meals.yml` (edit, trigger block only).
   - Why: Without a branch filter it can fire on the upstream-mirror `main`.
   - Edits: add `branches: [jarryd/main, jarryd/develop]` to both `push` and `pull_request`,
     keeping the existing `paths` lists verbatim; job body untouched.
   - Dependencies: none.

5. Local validation pass (agent-executed).
   - Why: Everything provable without pushing must be proven before handoff.
   - Edits: none. Run `actionlint` on all three changed/added files (fallback: YAML parse);
     locally run the exact CI commands — `./gradlew --no-daemon :app:assembleDebug` and
     `./gradlew --no-daemon :app:testDebugUnitTest` — to confirm task names and green results;
     `git diff` review confirming `main`-mirror cleanliness and minimal-diff discipline.
   - Dependencies: steps 2-4.

6. Owner remote verification (owner-executed; closes the story).
   - Why: Runs, buttons, releases, and badges only exist on the remote.
   - Edits: none. Owner pushes the branch, confirms: CI runs green on `jarryd/main` push; CI does
     **not** run on `main`; manual dispatch of Release APK produces an installable signed APK
     artifact; pushing a `v*` tag produces a GitHub Release with the APK attached;
     `validate-meals.yml` stays dormant on unrelated pushes.
   - Dependencies: steps 1-5; tag/release checks additionally on Story 11's keystore.

## Validation

### Automated Checks

- `actionlint` (or YAML parse) clean on `ci.yml`, `release-apk.yml`, `validate-meals.yml`.
- Local `./gradlew --no-daemon :app:assembleDebug` and `:app:testDebugUnitTest` succeed — proves
  the CI commands are valid before they ever run remotely.

### Manual Checks

1. Owner: CI green on push to `jarryd/main`; no run appears for a push to `main`.
2. Owner: manual Release APK dispatch yields an installable signed APK (verify install on the
   S22 Ultra).
3. Owner: `v*` tag push yields a GitHub Release with `FoodYou-<tag>.apk` attached.
4. Owner: Actions log shows no keystore bytes or passwords; no keystore file in any uploaded
   artifact.

### Acceptance Criteria

- `ci.yml` exists, runs green on the fork's trunk branches, and never runs on `main`.
- Every source-plan `VERIFY` item is resolved with repo evidence (JDK 21; tasks confirmed; tests
  present; no static analysis — recorded in Current Understanding).
- `release-apk.yml` produces a durable signed APK: 30-day artifact on dispatch, real GitHub
  Release on `v*` tags.
- No keystore material is ever written to the workspace, committed, or uploaded.
- `validate-meals.yml` job body is unchanged and the workflow stays dormant unless meal files
  change on a `jarryd/*` trunk branch.
- No file under `app/`, `shared/`, or any Gradle configuration is modified.

## Risk Mitigation

- Risk: Remote behavior unverifiable by the agent (push forbidden).
  Mitigation: Split validation into agent-local (actionlint + running the exact Gradle commands)
  and owner-remote checks; plan stays In Progress until the owner confirms runs.
- Risk: Keystore/private-key mishandling in CI (laws.md §2).
  Mitigation: Keystore only ever decoded into `$RUNNER_TEMP`, quoted expansions, `if: always()`
  cleanup, no secrets in the repo; keystore custody itself is Story 11's question.
- Risk: Upstream rebase conflicts on edited workflow files.
  Mitigation: `ci.yml` is purely additive; `release-apk.yml` and `validate-meals.yml` diffs are
  kept minimal and isolated; `docs.yml` regression-on-rebase is Story 14's recorded concern.
- Risk: JDK/toolchain drift (upstream workflow said 17, project needs 21).
  Mitigation: Both workflows pinned to 21 with the repo's own `jvmTarget`/compatibility settings
  as evidence; local Gradle runs in step 5 prove it before any remote run.
- Risk: `workflow_dispatch` silently unavailable.
  Mitigation: Default-branch switch is an explicit owner prerequisite (step 1b), called out as
  load-bearing per GitHub's documented behavior.
- Risk: Hardcoded `cmdline-tools/16.0` path in the sdkmanager step breaks if the runner image
  changes.
  Mitigation: Accepted — inherited from upstream and working today; noted for the execution log
  if it bites.

## Phase Split

Not needed — CER is under threshold; single-pass story.

## Evidence / References

- Planning inputs: owner's CI/CD plan (preserved as
  [dictation](../../../dictations-tier-0/2026-07-25_cicd_plan_github-actions.md)); direct reads of
  all three workflow files; `app/build.gradle.kts` (JVM 21 at lines 41/167-168);
  `gradle/libs.versions.toml` (no static-analysis tooling); `app/src/commonTest/` listing (6 test
  files); `git branch -a` (no `jarryd/develop`; `origin/HEAD` → `main`).
- Unverified claims: exact Gradle task names (`:app:assembleDebug`, `:app:testDebugUnitTest`) are
  inferred from the KMP androidTarget config and upstream's use of `assembleRelease` — confirmed
  at execution step 5, not yet run; all remote-side behavior is unverified until the owner pushes.

## Complaints / Friction

### Source plan assumed a branch and a JDK the repo does not have

**What happened:** The owner's CI/CD plan assumed `jarryd/develop` exists (it does not) and
carried JDK 17 over from upstream's workflow, while the project compiles against JVM 21.
**Why this made the task harder:** Both would have produced dead triggers and a red first CI run.
**What was tried:** Verified branches via `git branch -a` and the toolchain via
`app/build.gradle.kts`; corrected in this plan per the source plan's own VERIFY discipline.
**What would improve this:** Nothing — the source plan explicitly instructed verification, which
worked as designed.
