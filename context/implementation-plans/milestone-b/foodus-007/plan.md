# FOODUS-007 implementation plan

Owner: [Milestone B](../../../milestones/milestone-b.md#foodus-007), the sole Story
status owner. Authority: sequential Milestone B implementation, Samsung acceptance
and per-Story commit requested by the user. Coordinator owns builds/device checks,
independent review, trackers, receipt and commit. Worker returns uncommitted edits.

## Decision before implementation

Retain the existing successful-insertion event and back-stack acknowledgement.
Quick Add still pops to its scanner after a successful scan-origin insertion; the
scanner validates that acknowledgement against the current generation and queue
head. Return the updated review session only for a newly accepted acknowledgement,
so invalid/repeated IDs cannot masquerade as a completed queue. Open the next item
when present. Otherwise invoke a completion callback which uses the production
handoff helper to pop to the existing canonical Home route, removing scanner and
any intervening entry routes. Expose that existing route internally for tests.
The first save of a multi-item queue cannot complete the flow. Unselected items
are deliberately excluded; cancellation preserves review and saved-item guards.

No persistence, form, conversion, camera, release/version, dependency or schema
change. Direct Quick Add keeps the existing form-only pop and cannot trigger the
scanner completion callback. Failed inserts never emit the existing Saved event,
so feedback and editable values remain on the form. FOODUS-004/005/006 contracts
remain binding, except the accepted final-item destination changes from review to
Home/Log here.

## Delivery and required acceptance

1. Implement the bounded acknowledgement/completion callback and actual Home pop.
2. Update the existing MultiFoodScanTest fixture to use canonical Home below the
   scanner, real production handoff/queue/screen/VM and private in-memory Room.
   Preserve its exact count, snapshot, cancel, retry and duplicate assertions;
   inspect saved review IDs through the retained test model once Home is reached.
3. Add bounded single failure/edit/retry and direct Quick Add predecessor tests;
   assert Home route, missing scanner/add Back entries, persistence before any
   completion, next selected item after intermediate save, unchanged direct pop,
   cancel/discard returning review, partial count and retry with no duplicates.
4. Appropriate common tests, debug and test APK builds, Samsung runtime suite,
   prior data preservation and conversion regressions as selected by coordinator,
   plus independent navigation/integrity review. Harness has no installed modules.

Use ScanTestDriver, private Koin, generated JPEG and synthetic estimates. Bound
database reads and UI waits. No global diary/settings writes, live AI/camera,
private photos, credentials, uninstall or data clearing. Home fixture renders
synthetic content on the canonical route rather than resolving owner Home data.

## Evidence and continuation

Baseline HEAD: `0b3f3e5c`. The four production files now implement the decision
above. The existing Android `MultiFoodScanTest` has six methods: its original four
retain snapshot/count/cancel/failure/duplicate assertions with final Home/back-stack
expectations, plus focused single-save failure/edit/retry and direct-predecessor
cases. Its cancellation case injects repeated and stale-generation acknowledgements
and proves they are consumed without completion or queue advancement. Final-save
callbacks assert exact persisted counts before navigation. The single pending
insertion case proves form retention and zero completion before insertion finishes.
Home renders only synthetic in-memory readback on the canonical production route.
Final route assertions exclude both scanner and Quick Add entries, then invoke the
controller's Back pop and verify neither completed route can be reopened. The direct
case returns to its distinct predecessor, then Back reaches Home normally.

One common queue regression explicitly covers selected-queue completion with an
unselected sibling, exact saved IDs, repeated and stale acknowledgements. Existing
common handoff, queue and save-state regressions remain unchanged.

`git diff --check` passes (line-ending normalization advisory only). No build,
Samsung or independent review result is claimed by this worker; those required
gates remain with the coordinator. All changes are uncommitted. Preserve the
coordinator's tracker/briefing edits and unrelated remote attachments.

## Coordinator final acceptance (2026-10-25)

Final combined command ` .\gradlew.bat --offline --no-daemon --console=plain -q
:app:testDebugUnitTest :app:assembleDebug :app:assembleDebugAndroidTest` passed:
141 tests in30 suites, zero failures/errors; both APKs built. First compile attempt
identified reversed kotlin.test assertion arguments and an incorrect typed-route
extension import in tests only; those two lines were corrected without changing
production, and independent focused review renewed the source identity.

Samsung SM-A057F Android15 physical1080x2400 portrait: `MultiFoodScanTest` six cases
plus all four `ScanDataPreservationTest` regressions passed together,10 tests in
112.954s. Actual production helpers/queue/form and real private in-memory Room prove
persistence before completion, intermediate continuation, final canonical Home pop,
missing completed Back entries, failed edit/retry, cancellation, stale IDs and direct
predecessor navigation. The fixture uses synthetic Home contents on the production
route, so owner Home/diary/settings are never read or written. Original rotation
policy restored. No live AI, camera, credential, version or release operation.

Independent reviewer `/root/review_foodus_007` final PASS, no findings. Six-source
aggregate `14aaadf020753dd72e72d4252e0083eb40778d0fe54b8fda89c1b5e0f042aea1`.
Checked identity `6b581e95cf29e0b206a08718cc8d41607d000ba0c5f182aff8af83ba980a37a9`.
Whitespace passed. No conversion data-path change warranted repeating FOODUS-006's
full promotion suite; its stored-value contract and existing tests remain unchanged.
[Completion receipt](../../../_boss-runs/milestone-b-sequential/foodus-007-receipt.json)
prepared before terminal metadata; seal and authorized completion commit follow.
