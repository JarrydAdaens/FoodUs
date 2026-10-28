# Prepare the accepted phone-review release

Status: Complete (local release preparation, 2026-10-28). Unpublished.
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

## Local Git completion

The owner's release request authorizes the local commits, release merge and tag.
Story 40 is preserved in 4f660754; candidate metadata and validation are committed
in 7da71eb9f7f331f8056ec99492a9f29595d25cbe. This release merge integrates the complete accepted development history
into jarryd/main and closes local preparation. Annotated v3.2.41.5 identifies the
merge and records its exact source revision, intended cumulative count 5 and code
143. Develop is fast-forwarded to the same baseline; main is left selected.

Finalization checks require a two-parent merge, matching main/develop/tag tips,
matching tag/catalog version, preserved existing tags, stash and remote-tracking
refs, retained ancestry and an empty working tree. The local finalization receipt
is ignored app/build/release-phone-review-git.json. No pull, push, remote write,
publication, signing or owner-phone installation is performed. CI signing and
publication remain pending the owner's tag push; published ReleaseCount remains
four until that succeeds.

## Changelog follow-up — 2026-10-28

The owner found the in-app history stopped at 2.23.0 and explicitly requested
highlights for 3.2.39.4 and the current candidate, durable plain-English writing
rules, automatic maintenance when agents prepare release tags, and moving the
unpublished local tag so the next CI build includes the notes. This authorizes
the required local commits and tag replacement; no push is authorized.

Fresh read-only GitHub release/tag audit confirms 3.2.41.5 is still absent remotely.
Keep the published 3.2.39.4 tag and older releases unchanged. Keep candidate version
3.2.41.5/code 143: its release APK remains undistributed and this correction closes
no new Story. The earlier artifact hash above is historical and must not be used
as evidence for the corrected candidate.

Added five user-facing highlights for each missing entry, with published UTC date
2026-10-26 for the prior release and planned 2026-10-28 for this candidate. Older
entries remain intact. The source and provenance rules live in the new changelog
wiki guide; version policy and release runbook require updating the notes before
every release tag. A small release-changelog gate catches a missing/stale entry
before tag CI builds it; self-test covers missing, stale, duplicate, empty and
invalid-date candidates. Common and native history checks are updated for both
four-part entries and Installed badges. Build/runtime verification and final local
tag movement are pending. No owner device is used for these checks.


### Changelog correction acceptance (authoritative for the corrected candidate)

- JDK 21 unit/debug/test/release build passed in **3m58s**, 241 tasks. Unit
  XML: **230 tests, zero failures/errors/skips**. Native history acceptance on
  disposable emulator-5584: **all three methods PASS**, 23.9s. Covered history
  scrolling, preserved upstream credit, Back/re-entry, exact four-part Installed
  badges and exact-version dismissal/seen tracking. No owner phone was modified.
- Synthetic screenshots for both four-part badges are under ignored
  `app/build/changelog-followup-visuals/`; current highlights are readable and the
  prior entry remains below. No application UI layout or naming API changed.
- Release-changelog gate and **eight self-test fixtures PASS**. The gate rejects
  the original tagged source lacking these releases. YAML parsing confirms the
  tag-only CI gate runs before APK compilation. Configuration owns project paths
  and the static-entry pattern. Existing mandatory release-policy/runbook routing
  supplies the standing trigger. Root AGENTS.md remains untouched.
- UI naming inventory PASS, zero missing/duplicate names:
  `harness/ui-name-coverage/runs/20261028T030645189662Z/report.json`.
  Independent content/source/runtime/artifact evaluation: **PASS**, no blocker.
- Corrected unsigned app APK: **foodus-3_2_41_5-unsigned.apk**, **18,015,574 bytes**,
  SHA-256 **07acc18a40156fe663a78be56f67f337a44bca1aefcd6ac40936b3cdae2dbd0c**.
  Current AGP metadata and SDK aapt agree on release package, version **3.2.41.5**
  and Android code **143**. DEX inspection confirms highlights from **both** new
  entries are present. Generated AI developer fallbacks are blank. This hash
  supersedes the previous local candidate's hash for the corrected app content.
- The debug APK was used only for synthetic emulator verification. No new owner
  testing delivery was requested. AGP refreshed its debug output directory; the
  historical accepted testing APK was restored from its verified, unchanged archive.
  No personal delivery location or owner data is part of repository history.
- Sanitized receipt: ignored `app/build/changelog-followup-acceptance.json`.
  No source changes followed the successful app build; only gate configuration
  and release documentation were finalized. `git diff --check` passed.

Local follow-up commit, release merge and authorized replacement of the unpublished
annotated tag are the final steps. Keep both branch tips aligned on the corrected
release merge and preserve every published tag. No pull, push or publication.
