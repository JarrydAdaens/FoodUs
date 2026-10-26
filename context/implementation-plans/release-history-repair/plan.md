# Plan: Restore the merged release history

Status: Complete
Authority: Owner's 2026-10-26 request to clean up interrupted local work and prepare the next release for an owner-executed push.
Status owner: This plan; milestone documents retain Story status ownership.

## Outcome and scope

Preserve existing Story commits and recovered OCR planning, integrate the stranded release
branch, merge development into the default `jarryd/main` branch, and tag the release merge.
No pull, push, remote deletion, household install, or publication is authorized in this run.
The owner explicitly directed completion of the Git commits and cleanup in the follow-up
message. Local commits, merges and release tagging are authorized; the owner retains the push.

## Current understanding

- GitHub's default branch is `jarryd/main`. Its legacy releases tagged merge commits.
- At intake, `jarryd/develop` was 20 commits ahead of its remote. All those commits stay intact.
- GitHub's complete release/tag lists contain only `v2.19.0`, `v2.19.1`, and `v2.23.0`.
  The misleading annotated `v3.2.38.3` is local only and explicitly describes a snapshot.
- `release/3.2.38.4` exists remotely at `336f508b`. Its successful manual Release APK run
  (`40286415095`) produced a signed artifact, not a published release. Its Android code is 135.
  Preserve that branch's SDK setup fix and ancestry; do not rewrite its published commits.
- Milestone 2 has 29 Complete Stories, Milestone B has 10, generation is 3, and delivery
  milestone is 2: the next publication candidate is `3.2.39.4`, Android code 136.
- Dirty context is the selected OCR design and Stories 30–37, all Not Started; preserve it
  in its own commit with its exact intake. Story 38 is planning only, Story 39 is complete.
- SponsorViewModel has no Git content difference. The dirty version-code 134 is superseded
  by 136. Raw attachments were moved outside the checkout; maintained intake copies remain.
- The preexisting July stash remains untouched. A verified complete Git bundle, original
  dirty patch, original refs, intake and raw attachments are in the owner's local temporary
  recovery folder `FoodUs-repo-cleanup-20261026-204453` outside the repository.

## Delivery steps

1. Verify remote release/ref facts and retain recoverable local snapshots. Complete.
2. Validate and commit the recovered OCR planning separately from release changes.
3. Merge the old release branch into develop, retaining its SDK fix while resolving the
   catalog to the fresh candidate. Add release-ledger notes and document push/tag handling.
4. Run JDK 21 debug assembly, unit tests, release assembly, source naming inventory,
   metadata/privacy checks, and an independent review. Record actual results below.
5. Merge develop into `jarryd/main` with a merge commit, create annotated `v3.2.39.4`
   on that merge, and reconcile develop to that commit. Remove the local snapshot tag and
   merged redundant local branches only after backup and verification.
6. Leave the default branch selected, worktree clean, and annotated tags included by the
   repository's local `push.followTags` setting. The owner performs the push.

## Checks and recovery

Validate Story index/detail agreement, intake byte equality and changed context links.
Verify tag/catalog equality, Android code >135, merge parents and ancestor relationships,
unchanged legacy tags, and an empty final porcelain status. Retain the original bundle and
patch; do not reset published branches or force-push. The remote release branch can remain
as a merged historical ref until the owner deletes it through GitHub.

This repair makes no new UI behavior claim; Story 39's accepted runtime evidence remains
in its existing plan. The first-four-part Obtainium preflight, both-phone installed-code
inspection, real release-certificate update and household rollout are not inferred from
build success. Do not report those checks as completed.

## Evidence and execution record

- Remote read-only audit completed on 2026-10-26 using GitHub CLI: three published releases,
  three remote legacy tags, four branches, default `jarryd/main`, and the successful signed
  candidate artifact. No remote writes, pull or push.
- Recovery bundle verified with `git bundle verify`; original working changes and intake
  copied before edits, raw chat attachments moved outside the checkout without deletion.
- JDK 21 `gradlew.bat --no-daemon :app:testDebugUnitTest :app:assembleDebug
  :app:assembleRelease`: PASS in 4m7s, 208 tasks. Unit XML reports 213 tests, no
  failures/errors/skips. Existing SDK/compiler/MealsScreen and Compose-mapping warnings
  remain; no build errors. AI fallbacks were checked blank before this build.
- UI naming inventory: PASS, zero unnamed calls and duplicate identifiers; report
  `harness/ui-name-coverage/runs/20261026T095135599438Z/report.json` (ignored output).
- Offline checks: PASS for workflow YAML, explicit SDK platform-tools setup, 29+10
  tracker closures with index/detail status agreement, exact OCR intake byte equality,
  matching-tag acceptance and mismatched-tag rejection, generated release-note ledger
  fields/source revision and CLI flags. The publication command was replaced with a
  local argument-capture function: these checks made no GitHub release or network call.
- Built release APK verified with aapt2: application ID `io.github.jarrydadaens.foodus`,
  versionName `3.2.39.4`, versionCode 136. Generated BuildConfig's AI key, endpoint and
  model fallbacks are all empty. This verifies public-build configuration, not signing
  or a device update; the local release artifact remains unsigned.
- Independent quality-gate evaluator: PASS WITH NOTES for the inspected local changes,
  no blocking workflow/configuration finding. Further reviewer shell reads failed with
  process-creation Access denied; it did not independently verify the later runbook,
  byte-equality result, build output or final graph. Main-agent checks cover the first
  three; final graph verification is recorded below.
- `git diff --check`: PASS. Raw attachments are excluded from future accidental adds.
- The owner's follow-up explicitly authorized completion. Recovered OCR planning and release
  preparation received separate commits. The existing release branch was merged into develop,
  preserving all original Story and release-branch commits; its catalog conflict retained the
  already-built `3.2.39.4` / 136 values.
- Finalization restores the legacy shape: a two-parent release merge into `jarryd/main`,
  annotated `v3.2.39.4` on that merge, then develop fast-forwarded to the accepted release.
  The misleading local-only `v3.2.38.3` snapshot tag and the proven-merged local `main` and
  `release/3.2.38.4` branches are removed. Remote refs and the July stash are untouched.
- Final checks cover both release-merge parents, preservation of every original local commit,
  ancestry of the stranded candidate, matching branch/tag tips, unchanged legacy tag targets,
  no local obsolete snapshot tag, no merge conflicts, and an empty porcelain working status.
  Local `push.followTags=true` includes reachable annotated tags on normal Git pushes.
- The recovered OCR intake has an explicit Git binary-text attribute to retain its exact
  original bytes across Windows checkouts; no source text is changed by that safeguard.
- Repository repair is complete. The owner performs `git push origin jarryd/main
  jarryd/develop --follow-tags` or its GUI equivalent. This run performs no pull, push,
  remote-branch deletion or release publication. The recorded phone/Obtainium limits remain.
