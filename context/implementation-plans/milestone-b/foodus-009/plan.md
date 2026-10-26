# Plan: FOODUS-009 — verified FoodUs What's New

Status: Verified; completion commit pending
Authority: Serial Milestone B delivery; [FOODUS-009](../../../milestones/milestone-b.md#foodus-009) owns Story status.

## Outcome and scope

Replace upstream history in the existing static changelog with exactly three FoodUs
entries, newest first. Match installed and remembered versions using the shared fork
identity. Preserve About attribution, licensing, credits, and existing history-sheet
display/dismiss behavior. No version/catalog, dependency, release, tag, or schema changes.

## Verified content decision (before implementation, 2026-10-25)

The live GitHub releases collection (100 per page; second page empty, checked by the
coordinating agent) contains only v2.23.0, v2.19.1, and v2.19.0. Catalog 3.5.0 is an
unpublished legacy draft, not proof of publication. Use UTC publication dates:

| Entry | Date | Verified basis and summary |
| --- | --- | --- |
| 2.23.0 | 2026-07-27 | [Release](https://github.com/JarrydAdaens/FoodUs/releases/tag/v2.23.0), published 12:24:17Z; [comparison](https://github.com/JarrydAdaens/FoodUs/compare/v2.19.1...v2.23.0), 77befdf1..59534d10. Actual diffs: 02ec0391 adds on-device AI key/endpoint/model settings and validation; 4ec602d4 adds personal system prompts and per-scan hints; ebb28703 adds provider website links. |
| 2.19.1 | 2026-07-26 | [Release](https://github.com/JarrydAdaens/FoodUs/releases/tag/v2.19.1), published 23:29:28Z; [comparison](https://github.com/JarrydAdaens/FoodUs/compare/v2.19.0...v2.19.1), 4535ef23..77befdf1. Actual diffs: b7423b1e aligns Android versionName with the fork release identity; 4e0ed878 adds the FoodUs launcher artwork; 6d1b9a10 fixes the published privacy-policy destination. |
| 1.0 announcement | 2026-07-24 | [Owner seed](../../../dictations-tier-0/2026-07-24_initial_project_seed_acme-food-app.md), lines 154–157, announces the fork as ACME Food App starting at 1.0. Commit dc683e1d on July 25 implements 1.0.0; 7dc9adc7 on July 26 rebrands FoodUs. This entry explicitly describes a founding announcement under the interim name, later renamed FoodUs, **not a published 1.0 release**. No tag is invented. |

The announcement label/date and automatic-sheet behavior were explicitly accepted by
the coordinator before coding. Release content describes shipped comparison commits,
never today's unshipped B work. No ongoing duplicate version counter is introduced.

## Implementation and checks

1. Replace `StaticChangelog` data, matching `AppConfig.forkVersionName` exactly.
   BuildConfig `FORK_VERSION_NAME` has no build-type suffix; Android package suffixes
   `-dev`/`-preview` do not require string stripping here. Exact identity comparison
   accepts arbitrary component counts without truncation or implicit fallback.
2. Correct `AppUpdateChangelogModalBottomSheet` seen-version comparison/write to the
   same fork value. Keep the existing full-history sheet for unseen unknown builds;
   unknown 3.5.0 and future four-part builds have no installed badge or invented entry.
   Preserve dismissal, preview opt-out, and history reopening semantics.
3. Add common regression checks for exactly three entries, factual dates/order/content,
   no placeholders, upstream mismatch, exact published matches, and unknown/future names.
4. Add Android synthetic-host checks exercising actual About → What's New, all history,
   dismissal/back and re-entry, upstream credit link callback, installed badge, and
   automatic fork-version seen tracking. Reuse DeveloperActivity/ScanTestDriver with
   safe drawing insets, isolated Koin and in-memory settings/config. No external URL
   launch, owner-setting writes, credentials, diary, or private-photo access. The host's
   inherited read of the general secureScreen preference is accepted.
5. Coordinator runs relevant common/Android checks, Samsung runtime acceptance and an
   independent content/integrity review before tracker closure and authorized commit.

## Evidence and continuation

Instructions, relevant laws/Design, milestone, version policy and harness read. No
installed harness module matches; use direct tests/runtime evidence. Local original
source/diffs and live release-ledger evidence confirm the table above.

Implementation frozen for coordinating acceptance: only `StaticChangelog.kt` and the
one-line automatic-sheet fork identity correction change production behavior. The
announcement uses `changes`, ensuring its historical disclaimer is preserved by the
existing copy-release-notes formatter as well as displayed history. About and upstream
credit/license code are unchanged. `StaticChangelogTest` has four common regression
methods; `FoodUsChangelogTest` has three Android methods (including two unknown-version
host runs) covering real About/history/back, credit callback, installed badge,
automatic display, exact seen-version writes, opt-out preservation and dismissed
sheet re-entry. `git diff --check` passes; existing Git newline-conversion warnings
are informational. No build or device check has yet been run by this worker.

Next: coordinator builds/runs these tests on Samsung, obtains independent content and
integrity review, then records acceptance and final Story status/commit evidence.
Worker does not build, operate devices, commit,
pull, push, publish, or change Story status. Coordinator owns those delivery steps.

## Coordinator final acceptance (2026-10-25)

Full unit/debug/test-APK command passed145 tests/31 suites, zero failures/errors.
The initial compile attempt found an unavailable coroutine-test import in the new
common test. Switching that immediate flowOf/first check to the repository's
existing runBlocking pattern fixed compilation without adding dependencies or
changing production. Focused independent review renewed the source aggregate.

Samsung SM-A057F Android15 physical1080x2400 portrait: all three FoodUsChangelogTest
methods passed19.991s. Actual About/history/back/re-entry, intercepted upstream
credit callback, published-fork Installed badge, exact fork seen/dismiss tracking,
and unknown legacy/future four-part builds without fabricated installed entries
are covered. Private synthetic settings/Koin; inherited secureScreen host read
accepted, no owner writes, diary, credential/photo access or external URI launch.
Original rotation policy restored. No release/tag/version/dependency/schema change.

Reviewer `/root/review_foodus_009` PASS, no findings, including runBlocking correction.
Four-source aggregate `fc75f892699d24f750553f531b3afabbf569d444371f55048eb7a23a37a2d8b5`;
checked identity `cf4e068ae403a5fed6c4d6999d1fd08630914819b128e6ff576b645f63da40b3`.
Whitespace passed. Primary source URLs, exact shipped commits and historical scope
are recorded above; catalog3.5.0 remains the authorized legacy exception, not a
new publication. [Receipt](../../../_boss-runs/milestone-b-sequential/foodus-009-receipt.json)
prepared before terminal metadata; seal and authorized completion commit follow.
