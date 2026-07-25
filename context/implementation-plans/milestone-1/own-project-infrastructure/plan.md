# Plan: Own Project Infrastructure

## Metadata

- Task Type: `STORY`
- Status: `Draft`
- Owner: Jarryd Adaens
- Last Updated: 25 July 2026

## Linked Context

- Milestone: [context/milestones/milestone-1.md](../../../milestones/milestone-1.md)
- Story: [Story 13: Own project infrastructure](../../../milestones/milestone-1.md#story-13)
- Backlog source: none — story created directly from the 2026-07-25 README/infrastructure repointing session
- Related Plans: [Story 11: App update mechanism plan](../story-11-app-update-mechanism/plan.md)
  ([story](../../../milestones/milestone-1.md#story-11)). Agreed boundary: Story 11 owns the
  signing/distribution decision; Story 13 provides the CI artifact and release channel that
  Story 11 consumes.
- External Tooling: `commit-log` skill for commits; `rails-grade-cer` if formal grading is wanted before dispatch

## CER

- Complexity: 3
- Effort: 5
- Risk: 4
- Notes: Inline estimate by the planning agent, not a formal `rails-grade-cer` grade. Complexity is low — mostly config and documentation edits with few decision points. Effort is moderate — many small files across `.github/`, `docs/`, and `metadata/`. Risk sits at 4 because CI signing/secrets and GitHub Pages behavior can only be verified on the remote (which the agent cannot push to), and a wrong `metadata/` or workflow edit could create merge friction with upstream.

## Objective

Replace every upstream-pointing piece of project infrastructure with the fork's own (or an
explicit, documented placeholder), so that the README's Build and Release badges render real
results, the docs site and issue templates describe this fork, and the repository no longer
presents itself as `maksimowiczm/FoodYou`.

## Scope

### In Scope

- A working GitHub Actions build workflow for the fork (assemble a debug or dev-release APK on
  push/dispatch) so the README Build badge goes green.
- The fork's release channel: adapt or replace `.github/workflows/release-apk.yml` (it assumes
  upstream's signing secrets) and cut at least one fork release so the Release badge resolves.
- `docs/zensical.toml`: repoint `site_url`, `repo_url`, and the Discord social link away from
  upstream; decide whether the docs site deploys to the fork's GitHub Pages or is parked.
- `docs/docs/*.md` (`index.md`, `contribute.md`, `privacy-policy.md`): rewrite fork-relevant
  content, keep upstream attribution, remove upstream install/donation calls-to-action.
- `.github/ISSUE_TEMPLATE/*.yaml`: keep or trim for a personal fork; ensure nothing routes to
  upstream.
- `metadata/en-US/` review (`title.txt`, descriptions, changelog): store metadata is F-Droid
  material for upstream — decide to leave untouched (mergeability) or annotate; record the
  decision.
- Verify the README badges added on 2026-07-25 render correctly once workflows/releases exist.

### Out Of Scope

- App identity rename (ACME Food App in-app name, icons, application id) — Milestone 2 identity
  story.
- The actual device update/distribution mechanism — Story 11 owns the signing/distribution
  decision and delivery to phones; this story only provides the CI artifact and release channel
  it can consume.
- Play Store publication.
- Any production code changes.

## Non-Goals

- Do not build a full custom docs site; adapting the existing zensical config is enough.
- Do not delete upstream history, credits, or license material — attribution is constitutional.

## Current Understanding

- Likely files or directories:
  - `.github/workflows/docs.yml` — deploys docs to GitHub Pages on push to `main`; fork branch is
    `jarryd/main`, so it currently never fires on the fork.
  - `.github/workflows/release-apk.yml` — manual-dispatch APK release; expects upstream signing
    setup (JDK 17, build-tools 36) and will fail without fork secrets.
  - `.github/workflows/validate-meals.yml` — path-triggered meals validation; works as-is on any
    repo, keep unchanged.
  - `.github/FUNDING.yml` — already neutralized (2026-07-25).
  - `README.md` — already repointed at `JarrydAdaens/FoodYou` with placeholder Build/Release
    badges (2026-07-25).
  - `docs/zensical.toml`, `docs/docs/`, `docs/development/` — all still upstream-branded.
  - `metadata/en-US/` — upstream store metadata.
- Existing behaviors to preserve: the local Gradle build and deploy flow (Story 1) must be
  unaffected; upstream files not owned by this story stay byte-identical for mergeability.
- Interfaces / external dependencies: GitHub Actions on the `JarrydAdaens/FoodYou` repo,
  GitHub Pages (optional), GitHub Releases.
- Assumptions and constraints:
  - The fork repo has Actions enabled and the owner can add repository secrets and enable Pages.
  - The agent must never push (`AGENTS.md`); all remote-side verification is the owner's step.
  - Fork philosophy applies: prefer additive changes; where upstream files must be edited, keep
    edits small and isolated to minimize merge conflicts on upstream pulls.

## Questions / Unknowns

- Q: Is a debug/dev-release APK from CI enough for the badge and for the artifact Story 11
  consumes, or does the release channel need Story 11's chosen signing from day one?
  Impact: Story 11 owns the signing/distribution decision; this only determines whether Story 13
  ships its CI artifact before or after that decision lands.
  Assumption: Debug-signed CI artifact is enough here; real signing follows Story 11's decision.
  Status: OPEN
- Q: Does the owner want the docs site actually deployed to `jarrydadaens.github.io`, or parked
  (workflow disabled, config repointed but unpublished)?
  Impact: Deployed means enabling Pages and rewriting more content now; parked is cheaper.
  Assumption: Parked — repoint config and neutralize the deploy trigger, publish later if wanted.
  Status: OPEN
- Q: Leave `metadata/` (F-Droid/fastlane) untouched for mergeability, or annotate it as
  upstream-owned?
  Impact: Touching it invites merge conflicts on every upstream release; leaving it means the repo
  still carries upstream store copy.
  Assumption: Leave content untouched; record the decision in the execution log.
  Status: OPEN
- Q: Which branch is the fork's CI trunk — `main` or `jarryd/main`?
  Impact: Workflow triggers and the README build badge's `branch` parameter must match.
  Assumption: `jarryd/main` is the working trunk; badge and triggers should target it.
  Status: OPEN

## Execution Steps

1. Resolve the four open questions with the owner (one short exchange).
   - Why: Signing, Pages, metadata, and branch answers change the edits below.
   - Edits: none.
   - Dependencies: blocks steps 2-6.

2. Add a fork build workflow `.github/workflows/build.yml` (new file, additive).
   - Why: Gives the README Build badge a real workflow and proves the repo builds in CI.
   - Edits: new workflow — JDK 21 (matches the project toolchain, unlike upstream's JDK 17
     release workflow), `assembleDevRelease` (or debug) on push to the trunk branch and manual
     dispatch, upload APK as an artifact.
   - Dependencies: step 1 (branch, signing answers).

3. Adapt the release channel.
   - Why: The README Release badge dangles until one fork release exists.
   - Edits: either repair `release-apk.yml` for fork use (signing per step 1 answer) or point it
     at the new build workflow's artifact; owner then dispatches it once and publishes a
     `v0.1.0`-style pre-release tagged from the fork.
   - Dependencies: step 2.

4. Repoint the docs site.
   - Why: `docs/zensical.toml` still declares upstream's site/repo URLs; content pages tell users
     to install upstream from F-Droid and message upstream's Crowdin.
   - Edits: `zensical.toml` (`site_url`, `repo_url`, social links), `docs/docs/index.md`,
     `docs/docs/contribute.md`, `docs/docs/privacy-policy.md` contact line; per step 1, either
     retarget `docs.yml`'s branch trigger or disable it (`workflow_dispatch` only).
   - Dependencies: step 1 (Pages answer).

5. Sweep `.github/ISSUE_TEMPLATE/` and remaining `.github/` surfaces.
   - Why: Templates are generic but sit under a fork that doesn't triage public issues the same
     way; confirm nothing links upstream.
   - Edits: minor wording or none; keep files if the owner wants issue intake on the fork.
   - Dependencies: none.

6. Verify badges and record the metadata decision.
   - Why: Close the loop on the placeholder note in the README.
   - Edits: remove/soften the README placeholder-badge note once Build and Release badges render;
     set the build badge's branch parameter; note the `metadata/` decision here in the plan.
   - Dependencies: steps 2-4; remote-side runs are owner-executed (agent cannot push).

## Validation

### Automated Checks

- `actionlint` (or GitHub's workflow editor validation) on changed/added workflow files — if
  actionlint is unavailable locally, YAML-parse the workflows as a minimum.
- Local `gradlew assembleDevRelease` still succeeds (proves the CI command is valid before it
  ever runs remotely).
- `zensical build --clean -f docs/zensical.toml` builds the docs site locally if the docs are
  reworked.

### Manual Checks

1. Owner pushes the branch and confirms the build workflow runs green on the fork.
2. Owner dispatches the release workflow (or publishes the release) and confirms the README
   Release badge resolves.
3. README viewed on GitHub: all four badges render, none point at `maksimowiczm/FoodYou`.
4. Docs config/pages spot-checked for remaining upstream URLs:
   `grep -ri "maksimowiczm" docs/ .github/` returns only intentional attribution.

### Acceptance Criteria

- README Build and Release badges show real fork results; the placeholder note is gone.
- No workflow, docs-site config, issue template, or funding file routes to upstream accounts or
  URLs except as explicit attribution.
- `validate-meals.yml` and the local build/deploy flow are unchanged and still work.
- All changes are additive or minimally invasive per the fork-philosophy mergeability rule.

## Risk Mitigation

- Risk: CI signing secrets mishandled (committed keystore or leaked key).
  Mitigation: No secrets in the repo, ever (laws.md §2); if real signing is chosen, keystore lives
  only in GitHub repository secrets and Story 11's records; default assumption avoids it entirely.
- Risk: Remote-side steps can't be verified by the agent (push is forbidden).
  Mitigation: Split validation into local (agent) and remote (owner) checks as above; plan status
  stays In Progress until the owner confirms badge rendering.
- Risk: Upstream merge conflicts from edited upstream files (`docs/`, `release-apk.yml`).
  Mitigation: Prefer new files (`build.yml`) over edits; keep unavoidable edits small; leave
  `metadata/` untouched under the default assumption.
- Risk: Docs deploy workflow fires unexpectedly once branch triggers change.
  Mitigation: Default to `workflow_dispatch`-only until the Pages question is answered.

## Phase Split

Not needed — CER is under threshold; single-pass story.

## Evidence / References

- Planning inputs: `README.md` and `.github/FUNDING.yml` repointing (2026-07-25 session, this
  branch); survey of `.github/workflows/*.yml`, `docs/zensical.toml`, `docs/docs/*.md`,
  `.github/ISSUE_TEMPLATE/*.yaml`, `metadata/en-US/`.
- Unverified claims: badge rendering and workflow behavior on the remote repo are unverified
  until the owner pushes; `JarrydAdaens/FoodYou` Actions/Pages availability is assumed.
