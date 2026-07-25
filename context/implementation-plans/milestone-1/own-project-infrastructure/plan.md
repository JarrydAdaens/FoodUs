# Plan: Own Project Infrastructure

## Metadata

- Task Type: `STORY`
- Status: `In Progress`
- Owner: Jarryd Adaens
- Last Updated: 25 July 2026

> Revision note (25 July 2026): scope narrowed twice by milestone updates — the docs site moved
> to Story 14 (deleted outright, not re-owned), and the GitHub Actions workflows / release channel
> moved to Story 15 (fork CI/CD pipeline). This story now owns the repo-presentation surfaces:
> issue templates, `metadata/`, and README badge verification against Story 15's workflows.

## Linked Context

- Milestone: [context/milestones/milestone-1.md](../../../milestones/milestone-1.md)
- Story: [Story 13: Own project infrastructure](../../../milestones/milestone-1.md#story-13)
- Backlog source: none — story created directly from the 2026-07-25 README/infrastructure repointing session
- Related Plans:
  - [Story 15: Establish fork CI/CD pipeline](../story-15-cicd-pipeline/plan.md) — owns all of
    `.github/workflows/`; produces the CI runs and GitHub Releases this story's badges report.
  - [Story 11: App update mechanism](../story-11-app-update-mechanism/plan.md)
    ([story](../../../milestones/milestone-1.md#story-11)). Agreed boundary: Story 11 owns the
    signing/distribution decision; Story 15 provides the CI artifact and release channel that
    Story 11 consumes.
  - Story 14 (remove the docs site, no plan yet) — owns deleting `docs/` and
    `.github/workflows/docs.yml`.
- External Tooling: `commit-log` skill for commits; `rails-grade-cer` if formal grading is wanted before dispatch

## CER

- Complexity: 2
- Effort: 2
- Risk: 2
- Notes: Inline estimate by the planning agent, not a formal `rails-grade-cer` grade. Re-graded
  down after the scope narrowed: what remains is issue-template wording, a `metadata/` decision,
  and README badge verification — small, low-decision edits. Residual risk is only that badge
  verification depends on Story 15's remote-side runs, which this repo's agents cannot trigger.

## Objective

Make the repository present itself as the fork, not upstream: issue templates that don't route to
upstream, an explicit recorded decision on store `metadata/`, and README Build / Release badges
verified to render real results from Story 15's workflows — removing the placeholder caveat.

## Scope

### In Scope

- `.github/ISSUE_TEMPLATE/*.yaml`: keep or trim for a personal fork; ensure nothing routes to
  upstream.
- `metadata/en-US/` review (`title.txt`, descriptions, changelog): store metadata is F-Droid
  material for upstream — decide to leave untouched (mergeability) or annotate; record the
  decision.
- Verify the README badges added on 2026-07-25 render correctly once Story 15's workflows and a
  first release exist; set the build badge's branch parameter; remove/soften the placeholder note.

### Out Of Scope

- All `.github/workflows/` files — Story 15 (CI/CD pipeline) owns `ci.yml`, `release-apk.yml`,
  and `validate-meals.yml`.
- The docs site (`docs/`, `docs.yml`) — deleted outright by Story 14, not re-owned.
- App identity rename (ACME Food App in-app name, icons, application id) — Milestone 2 identity
  story.
- The actual device update/distribution mechanism — Story 11 owns the signing/distribution
  decision and delivery to phones.
- Play Store publication.
- Any production code changes.

## Non-Goals

- Do not delete upstream history, credits, or license material — attribution is constitutional.

## Current Understanding

- Likely files or directories:
  - `.github/ISSUE_TEMPLATE/*.yaml` — generic templates under a fork that doesn't triage public
    issues the same way; confirm nothing links upstream.
  - `metadata/en-US/` — upstream F-Droid/fastlane store metadata.
  - `README.md` — already repointed at `JarrydAdaens/FoodYou` with placeholder Build/Release
    badges (2026-07-25).
  - `.github/FUNDING.yml` — already neutralized (2026-07-25).
  - `.github/workflows/` — owned by Story 15; read-only context for badge targets (`ci.yml` for
    the Build badge, releases from `release-apk.yml` for the Release badge).
- Existing behaviors to preserve: upstream files not owned by this story stay byte-identical for
  mergeability; the local Gradle build and deploy flow (Story 1) is unaffected.
- Interfaces / external dependencies: GitHub badge endpoints (actions workflow status, releases)
  on the `JarrydAdaens/FoodYou` repo.
- Assumptions and constraints:
  - The agent must never push (`AGENTS.md`); all remote-side verification is the owner's step.
  - Fork philosophy applies: prefer additive changes; keep unavoidable edits small and isolated
    to minimize merge conflicts on upstream pulls.

## Questions / Unknowns

- Q: Leave `metadata/` (F-Droid/fastlane) untouched for mergeability, or annotate it as
  upstream-owned?
  Impact: Touching it invites merge conflicts on every upstream release; leaving it means the repo
  still carries upstream store copy.
  Assumption: Leave content untouched; record the decision in the execution log.
  Status: ANSWERED
  Answer: Proceeded on the recorded assumption (owner unavailable, 2026-07-25): `metadata/`
  content left byte-identical to upstream for mergeability. Decision recorded in the Execution
  Log below.
- Q: Is a debug/dev-release APK from CI enough for the badge and for the artifact Story 11
  consumes, or does the release channel need Story 11's chosen signing from day one?
  Impact: Originally determined this story's CI-artifact scope.
  Assumption: —
  Status: ANSWERED
  Answer: Moot for this story — workflows moved to Story 15. The Build badge tracks Story 15's
  `ci.yml` (no signing involved); signed artifacts come from Story 15's release workflow using
  Story 11's keystore.
- Q: Does the owner want the docs site actually deployed to `jarrydadaens.github.io`, or parked
  (workflow disabled, config repointed but unpublished)?
  Impact: Originally sized this story's docs work.
  Assumption: —
  Status: ANSWERED
  Answer: Neither — Story 14 deletes the docs site outright; no docs work remains here.
- Q: Which branch is the fork's CI trunk — `main` or `jarryd/main`?
  Impact: The README build badge's `branch` parameter must match the CI trigger branch.
  Assumption: —
  Status: ANSWERED
  Answer: `jarryd/main`, per Story 15's branch model (`main` is a pristine upstream mirror with
  no CI; the default branch switches to `jarryd/main` as a Story 15 prerequisite).

## Execution Steps

1. Resolve the remaining open question with the owner (`metadata/` decision — one short exchange).
   - Why: Determines whether step 3 touches `metadata/` at all.
   - Edits: none.
   - Dependencies: blocks step 3 only.

2. Sweep `.github/ISSUE_TEMPLATE/` and remaining `.github/` surfaces.
   - Why: Templates are generic but sit under a fork that doesn't triage public issues the same
     way; confirm nothing links upstream.
   - Edits: minor wording or none; keep files if the owner wants issue intake on the fork.
   - Dependencies: none.

3. Record and (if chosen) apply the `metadata/` decision.
   - Why: Close out the last upstream-presentation surface deliberately rather than by default.
   - Edits: none under the default leave-untouched assumption; the decision lands in this plan's
     execution log either way.
   - Dependencies: step 1.

4. Verify badges and retire the placeholder note.
   - Why: Close the loop on the placeholder caveat in the README.
   - Edits: set the build badge's `branch` parameter to `jarryd/main`; point badges at Story 15's
     workflow/release endpoints if names differ from the placeholders; remove/soften the README
     placeholder-badge note once both badges render.
   - Dependencies: Story 15's workflows merged and run at least once, and one release published;
     remote-side runs are owner-executed (agent cannot push).

## Validation

### Automated Checks

- `grep -ri "maksimowiczm" .github/ISSUE_TEMPLATE/ README.md` returns only intentional
  attribution after the sweep.

### Manual Checks

1. README viewed on GitHub: all badges render, none point at `maksimowiczm/FoodYou`.
2. Owner confirms the Build badge reflects a real `ci.yml` run on `jarryd/main` and the Release
   badge resolves to a fork release.
3. Issue templates spot-checked: no upstream routing.

### Acceptance Criteria

- README Build and Release badges show real fork results; the placeholder note is gone.
- No issue template or funding file routes to upstream accounts or URLs except as explicit
  attribution.
- The `metadata/` decision is recorded in this plan's execution log.
- All changes are additive or minimally invasive per the fork-philosophy mergeability rule.

## Risk Mitigation

- Risk: Badge verification depends on remote-side runs the agent cannot trigger (push forbidden).
  Mitigation: Split validation into local (agent) and remote (owner) checks as above; plan status
  stays In Progress until the owner confirms badge rendering.
- Risk: Upstream merge conflicts from edited upstream files.
  Mitigation: Remaining scope barely touches upstream files (templates only, possibly nothing);
  leave `metadata/` untouched under the default assumption.

## Phase Split

Not needed — CER is under threshold; single-pass story.

## Evidence / References

- Planning inputs: `README.md` and `.github/FUNDING.yml` repointing (2026-07-25 session, this
  branch); survey of `.github/workflows/*.yml`, `docs/zensical.toml`, `docs/docs/*.md`,
  `.github/ISSUE_TEMPLATE/*.yaml`, `metadata/en-US/`.
- Scope-move provenance: docs work → Story 14 (milestone update, 2026-07-25); workflow/release
  work → [Story 15 plan](../story-15-cicd-pipeline/plan.md) (2026-07-25). The original
  workflow/docs execution steps from this plan's first draft are superseded by those stories.
- Unverified claims: badge rendering on the remote repo is unverified until the owner pushes;
  `JarrydAdaens/FoodYou` Actions availability is assumed (a Story 15 prerequisite).

## Execution Log

- 2026-07-25 — Issue template sweep (step 2). Inspected `.github/ISSUE_TEMPLATE/bug-report.yaml`
  and `feature.yaml`: both are fully generic — no upstream links, contact URLs, or routing.
  `.github/` has no `ISSUE_TEMPLATE/config.yml`, no PR template, and no CODEOWNERS;
  `FUNDING.yml` was already neutralized (2026-07-25) and carries only intentional attribution
  comments. Templates kept as-is so the fork can take issues; zero edits needed.
  Verified: `grep -ri "maksimowiczm" .github/ISSUE_TEMPLATE/ README.md` returns only intentional
  attribution (upstream project links, credits, license, upstream contact) — no template hits.
- 2026-07-25 — `metadata/` decision (steps 1 & 3). Owner unavailable; proceeded on the plan's
  recorded assumption: **leave `metadata/en-US/` content untouched** (byte-identical to upstream)
  to preserve mergeability on upstream pulls. The store metadata is F-Droid/fastlane material
  that only matters to upstream's distribution channels; the fork is not store-published, so
  carrying upstream copy is harmless and conflict-free. Question marked ANSWERED.
- 2026-07-25 — Badge verification, local half (step 4). Repointed the README Build badge from the
  nonexistent shields `build.yml` target to the real Story 15 workflow:
  `https://github.com/JarrydAdaens/FoodYou/actions/workflows/ci.yml/badge.svg?branch=jarryd%2Fmain`,
  linking to the `ci.yml` runs page filtered to `jarryd/main`. The Release badge's shields
  target (`v/release/JarrydAdaens/FoodYou` → releases/latest) was already correct and is
  unchanged. Softened the README placeholder note: badges are now wired to real targets but
  render live results only after the owner's remote-side steps (below).
- Owner's remaining checks (remote half — agent cannot push):
  1. Push `jarryd/main`, enable GitHub Actions on the fork, switch the default branch to
     `jarryd/main` (Story 15 prerequisites).
  2. Confirm `ci.yml` runs green and the README Build badge renders the result.
  3. Publish the first fork release (Story 15's release workflow / Story 11's signing) and
     confirm the Release badge resolves.

## Completion Review

- Steps 2–4 (the agent-executable scope) are done: templates verified clean, `metadata/`
  decision recorded, README badges wired to real targets with the placeholder caveat replaced by
  an accurate pending-remote note.
- Status stays `In Progress`: acceptance criterion "badges show real fork results" is
  remote-gated. The story completes when the owner confirms the Build badge reflects a real
  `ci.yml` run on `jarryd/main` and the Release badge resolves to a fork release.
- No upstream file content was modified except `README.md` (already fork-owned); mergeability
  preserved.
