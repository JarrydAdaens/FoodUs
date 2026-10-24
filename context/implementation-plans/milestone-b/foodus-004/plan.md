# FOODUS-004 implementation plan

Owner: [Milestone B](../../../milestones/milestone-b.md#foodus-004).
The milestone owns status. The implementation worker owns source, tests and this
plan; the coordinator owns builds, Samsung execution, independent review, receipt
and commit. FOODUS-005 is committed at `2f3fe1f1` before this work.

## Selection and handoff decision before implementation

The smallest result contract is an ordered collection of the existing complete
`AiFoodEstimate` snapshots. The endpoint prompt requests `{"foods":[...]}` and
explicitly distinguishes one coherent dish (for example a pie) from several
separable foods. Each item's nutrients remain absolute totals for its own pictured
portion. Do not split coherent dishes into ingredients, merge separate items, scale
totals from metadata, or fabricate missing values. Accept the legacy single object
as a one-item result. Parse collection members independently with the existing
validated item parser: unusable members are excluded, their count is disclosed,
and usable siblings survive. Empty/all-invalid/malformed responses produce a safe
retryable failure. No raw response or personal data enters logs.

Review uses ordinary cards in the existing scrolling Column, avoiding a nested
unbounded LazyColumn. Every item has a selection checkbox, expandable nutrition
and portion details, and its existing Align action. Initial selection includes all
usable items. The primary action starts the selected unsaved items in response
order. A one-item response retains a straightforward Save action. Saved cards are
marked and cannot be selected or aligned again. A visible saved count and remaining
selection make partial completion explicit. No split/merge editor is introduced.

A small pure review-session state owned by the scanner's existing back-stack
ViewModel tracks stable per-result item IDs, selected IDs, saved IDs and the
currently accepted ordered queue. Each ID combines a per-result generation and
ordinal, preventing acknowledgements from an earlier result matching a later scan.
IDs distinguish identical names/nutrition rather
than deduplicating food by content. Each queued item opens the existing typed Quick
Add route with its complete FOODUS-005 JSON snapshot and an optional scan-item ID.
The route's normal manual callers remain valid. A local navigation handoff helper
binds the optional ID to a successful-save acknowledgement on the preceding scanner
back-stack entry's SavedStateHandle; no global mutable queue or new storage exists.
The scanner consumes an acknowledgement once, validates it against the queue head,
marks that item saved and opens the next accepted item. Repeated/stale
acknowledgements do nothing. Tests exercise this production helper and state.

Each item can be edited using Quick Add before saving. Successful persistence alone
acknowledges and advances. Back/cancel returns to review without acknowledging or
saving that item; saved siblings remain marked, unsaved selections remain available,
and Continue starts a fresh queue from those remaining selections. Existing discard
confirmation still protects edits; confirmed cancellation discards that unsaved
form's edits, and reopening starts from its accepted estimate. A later item failure leaves its editable form
open with a safe retry message; already saved siblings remain intact. Add the
minimal Create Quick Add in-flight/completed guard and failure state needed to
prevent double taps from inserting duplicates. CancellationException propagates;
other failures expose generic feedback, never exception messages or credentials.
Retries occur only after failure, and a successful form cannot insert again while
its navigation event is pending. The common form receives optional busy/error
presentation without changing historical-update behavior.

After the last accepted item, return to scanner review with the exact saved count
and disabled saved items. FOODUS-007 owns final Log/home completion and subsequent
Back-stack cleanup. Align remains an explicit alternative for an individual item;
it does not silently save other selected estimates or claim them saved. Product and
recipe promotion remain the existing per-item Quick Add actions, with destination
runtime validation assigned to FOODUS-006. The FOODUS-003 hint editor and
FOODUS-005 nullable 43-nutrient, explicit-energy and portion contracts remain intact.

## Proposed files and responsibility

- `ai/domain/AiFoodScanner.kt`, `AiScanPrompt.kt`, new result parser/model beside
  `AiFoodEstimateParser.kt`: collection boundary and legacy compatibility. Existing
  item validation remains the single authority for each estimate.
- `ai/infrastructure/OpenRouterAiFoodScanner.kt`: invoke collection parser and
  propagate usable items plus rejected-item count; unchanged endpoint transport.
- `app/ui/food/diary/aiscan/AiScanUiState.kt`, `AiScanViewModel.kt`,
  `AiScanScreen.kt`, new `AiScanReviewSession.kt`: review selection, details,
  saved-count display and ordered queue state.
- `app/navigation/FoodYouAppNavHost.kt` and a small local scan handoff helper:
  optional typed-route item ID and successful-save result delivery/consumption.
- `app/ui/food/diary/quickadd/CreateQuickAddViewModel.kt`,
  `CreateQuickAddScreen.kt`, `QuickAddScreen.kt`: guarded insertion and safe failure
  feedback needed for queue correctness; no persistence schema changes.
- Default Compose string resources: concise selection/progress/failure labels.
- Common parser/prompt/session/handoff/save-guard tests and affected scanner fake
  callers: deterministic behavior and compatibility.
- Android instrumented multi-food test beside `ScanDataPreservationTest.kt` and
  necessary existing fixture callback adaptations: production review/handoff/form
  plus isolated real Room persistence, using the working FOODUS-005 driver pattern.

## Required evidence

- Parser: coherent single dish, multiple distinct foods, legacy object, empty,
  malformed, partially invalid, all invalid, ordered complete snapshots, real zero,
  unknown values and explicit-unit compatibility.
- Session/navigation: selection/deselection, none selected, response order, identical
  item names with independent IDs, cancellation/resume, exactly-once acknowledgements,
  stale acknowledgements, saved-item exclusion and partial completion counts.
- Persistence: successful-save-only advancement, in-flight double-save guard,
  retry after failed insertion and no repeated insertion after success.
- Samsung synthetic production review -> actual handoff helper -> editable Quick
  Add -> in-memory Room: coherent dish single case; several foods with one deselected;
  cancel a pending item after a sibling save; retry a deliberately failed later
  insert; verify exact saved rows and edited nutrition without duplication or loss.
- Relevant unit tests, debug/test APK build, independent integrity review, and
  FOODUS-003/005 cross-story regressions as selected by the coordinator. No live AI,
  private photos, global diary/config writes, new dependencies or schema migration.

## Continuation

Coordinator approved the recorded design before application edits. The candidate
now implements the collection boundary, visible rejection count, expandable item
review, selected ordered queue, and actual typed-route/save acknowledgement helper.
Every result uses a random UUID generation plus item ordinal; identical food names
remain independent. Saved cards cannot be selected or aligned again. Align remains
the existing alternative and never acknowledges a Quick Add save or advances its
queue. The count explicitly says it covers Quick Add saves only.

Create Quick Add now guards pending and completed insertion, provides pinned safe
failure feedback, and blocks leaving/promoting while insertion is unresolved.
Callbacks read the guard synchronously to cover the frame before UI recomposition;
system Back also goes through the existing form/discard handler. Success is marked
before emitting navigation; a later navigation cancellation cannot enable insertion
retry. This prevents duplicates within the live queue. It is not a claim of durable
exactly-once insertion across process death or a storage-contract change.

The production handoff helper opens with launchSingleTop so two rapid review
callbacks share one form/back-stack ViewModel. Android acceptance deliberately opens
the same route twice before rendering, then blocks insertion, checks disabled Save
and Back, attempts stale repeated actions, and requires exactly one persisted row.

Common regression classes are `AiFoodEstimateCollectionParserTest`,
`AiScanReviewSessionTest`, `ScanQuickAddHandoffTest`, and
`CreateQuickAddSaveStateTest`. They cover collection/legacy/invalid boundaries,
selection/order/generation/acknowledgements/cancel, full snapshot route and local
result delivery, and insertion guard transitions. Existing FOODUS-005 tests remain.

Samsung candidate class:
`com.maksimowiczm.foodyou.app.ui.food.diary.quickadd.MultiFoodScanTest` (four methods).
It exercises coherent-dish/full-data/rapid repeated handoff/in-flight guard; inspect
and deselect distinct foods including a visibly omitted invalid member; save one
sibling, cancel/discard another form's edits, deselect it, fail a later insertion,
retry and assert the exact two-row result; and empty-response retry UI with zero
rows. It calls the production handoff helper for route creation, opening and saved
result delivery, and uses the production scanner/review/form plus real in-memory
Room. No global diary/settings writes, AI call, camera or owner image is used.
`ScanTestDriver` extracts the working FOODUS-005 window/IME/scroll/field/control
driver for both instrumented classes; bounded privacy-safe diagnostics are retained.
Run all four FOODUS-005 methods after this extraction, plus the FOODUS-003 keyboard
cases when the coordinator selects the cross-story device checks.

No worker build, unit execution or phone operation ran. Coordinator acceptance is
complete below; source is frozen for the completion transaction. Milestone closure
is provisional until the sealed receipt reconciles with its authorized commit.

## Final coordinator acceptance

- JDK21 offline `:app:testDebugUnitTest :app:assembleDebug
  :app:assembleDebugAndroidTest` passed against the final frozen candidate:
  140 tests in 30 suites, zero failures/errors, both APKs built. An earlier run
  found an exact-equality conversion assertion (100 vs99.99999999999999); the
  assertion now uses1e-6 tolerance, without changing production rounding.
- Samsung SM-A057F Android15 physical1080x2400 portrait: all four
  `MultiFoodScanTest` methods plus all four `ScanDataPreservationTest` methods
  passed together, 8 tests in130.05s. Actual production handoff helper, review,
  forms and real in-memory Room exercised. Selection/inspection, omitted-member
  notice, coherent dish, edited values, double handoff, pending-save repeated
  actions, cancellation/discard, failed later insert/retry and exact saved counts
  passed. Existing780sample/unknown/optional/manual/history contracts also passed.
- Independent `/root/review_foodus_004` static integrity PASS, no findings;
  final sorted-path SHA256 aggregate over23 source/test/resource files:
  `99be2cec1adec1b8a03dd4997eb844bd475b5c35e5a5bc8e54321c0bc58a1146`.
  Coordinator recomputed it; whitespace passed. Configuration is unchanged.
- APK installs used `-r -t`; no data clearing/uninstall. Tests use private Koin,
  generated JPEGs, in-memory Room and controlled synthetic failures. No owner
  diary/settings writes, credentials, camera/photo capture or live AI call.
  Original fixed-rotation policy and free rotation restored. No size override.
- FOODUS-003 keyboard layout is untouched except callback arity adaptation;
  its prior normal/compact evidence remains authoritative. This Story reran the
  directly affected FOODUS-005 cases after shared-driver extraction.
- Existing SDK XML tooling warning remains non-fatal. No schema, dependency,
  release or version change. Home completion stays FOODUS-007; Product/Recipe
  editor runtime remains FOODUS-006. Live-queue scope limits above remain explicit.

Receipt prepared before terminal metadata:
`context/_boss-runs/milestone-b-sequential/foodus-004-receipt.json`.
Commit pending at seal time. Next: reconcile the completion commit, then FOODUS-006.
