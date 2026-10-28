# Prepare the accepted phone-review release

Status: Validated; local Git finalization pending (2026-10-28).
Authority: The owner accepted the testing build and requested release preparation,
tagging and all local Git work, explicitly excluding push.

## Scope and version

Preserve the accepted Story 38 library, first phone-review batch and Story 40
refinements. Commit the tested work, prepare the next publication candidate,
validate release compilation, merge develop into the default branch with a merge
commit, annotate that merge and fast-forward develop to the same baseline.
No remote write, publication or owner-device installation is part of this task.

The read-only GitHub audit on 2026-10-28 found four published non-draft releases:
v2.19.0, v2.19.1, v2.23.0 and v3.2.39.4. The last release records Android code 136
and source ec4b612450b76a3b8d56b873ccfc6297a7d7a366. The complete remote tag list
contains those same four names. Remote main and develop both point to that last
release merge; the local default branch is also there. No release identifier is
being replaced. Historical refs and the existing stash remain untouched.

Generation 3 and delivery Milestone 2 remain selected. Milestone 2 has 31 terminal
Stories; Milestone B contributes ten, giving StoriesClosed 41. The next intended
publication uses cumulative count 5: candidate 3.2.41.5, exact tag v3.2.41.5,
Android code 143 (above the distributed testing code 142). It remains unpublished
until the owner pushes the tag and the release workflow publishes successfully.
This records a reserved candidate, not a fifth completed publication.

## Acceptance and local checks

The owner accepted the Story 40 testing APK. Its plan records 230 passing unit
tests, 11 passing native methods, source naming coverage and independent review.
Commit 4f660754 preserves that implementation and the testing-delivery record.
This release preparation changes version metadata only; no application behavior,
dependency or schema change is intended.

Before tagging: run JDK 21 unit tests and minified release assembly, inspect the
current AGP app output metadata and embedded application/version identity, confirm
generated AI developer fallbacks are blank, and check naming coverage. Preserve
the previously delivered testing APK. The local release artifact is unsigned;
the established tag workflow aligns/signs it with the existing release identity
and creates the GitHub release ledger with the source revision when pushed.

After checks: commit this preparation, merge into jarryd/main with --no-ff, create
annotated v3.2.41.5 on that merge, fast-forward jarryd/develop, leave jarryd/main
selected, and verify matching branch/tag tips, two merge parents, catalog/tag
equality, preserved history and an empty working tree. No pull or push.

The prior release already established a four-part publication. This run does not
claim a fresh signed-device update or Obtainium/household rollout test. Release
assembly and owner acceptance of the development APK do not prove those outcomes.

## Validation results

- JDK 21 `:app:testDebugUnitTest :app:assembleRelease`: **PASS**, 3m47s,
  183 tasks (54 executed, 129 up-to-date). XML reports **230 tests, zero
  failures/errors/skips**. The existing Compose mapping warnings remain;
  compilation, vital lint and R8 optimization succeeded. Log:
  ignored `app/build/release-phone-review-build.log`.
- Current AGP metadata and independent SDK `aapt` inspection agree:
  `io.github.jarrydadaens.foodus`, versionName **3.2.41.5**, code **143**.
  Actual output: **foodus-3_2_41_5-unsigned.apk**, **18,015,574 bytes**,
  SHA-256 `3df289a8dc07d8217f943f5827e32acc5eaeaa351df8f33b910599c119f77dd0`.
  This is an unsigned local build, not an installable household release.
- Generated AI key, endpoint and model fallbacks are blank. The accepted
  Story 40 testing APK retains its original SHA-256. No new testing APK
  was built or delivered in this release-preparation task.
- Fresh naming gate: **PASS**, zero unnamed calls or duplicate identifiers,
  `harness/ui-name-coverage/runs/20261028T023419843198Z/report.json`.
  The existing Story 40 native/source-selection acceptance remains applicable.
- Milestone 2 index/detail closure statuses agree (31); Milestone B contributes
  ten. No Story or milestone status changed during release preparation.
- Independent release-preparation source review: **PASS**, with correct
  tracker count, reserved publication count, code, blank fallbacks and
  merge/tag plan. Final artifact and Git-state verification are separate.
- `git diff --check`: **PASS**. Sanitized local artifact receipt:
  ignored `app/build/release-phone-review-artifact.json`.

## Continuation

Commit the validated candidate metadata and record, then finalize the release
merge, annotated tag and develop fast-forward. Publication remains owner-executed.
