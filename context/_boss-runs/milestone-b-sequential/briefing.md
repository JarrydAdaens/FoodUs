# Milestone B sequential delivery

Source and sole Story status owner: [Milestone B](../../milestones/milestone-b.md).
Authorized queue: FOODUS-001, 002, 003, 005, 004, 006, 007, 008, 009, 010.
Dependency order places 005 before 004. One implementation worker at a time.

User authority (2026-10-24): implement all B Stories sequentially, test each completed
Story on the attached Samsung, then invoke commit-log --commit before the next.
No pull, push, tag, publication, or release. Preserve private device data; use the
side-by-side debug application and synthetic inputs. Release legacy version remains
until the policy's release transition is performed; closures do not publish builds.

Baseline HEAD: 4151d6a4bf9f90077987cb8309d858e248a7133f.
Pre-existing untracked `.codex-remote-attachments/` is excluded from all commits.
No staged or modified tracked files at setup.

Checks: targeted regression tests, debug assemble, matching Samsung runtime evidence
for every Story. Harness lists no installed modules. Independent scoped review for
consequential data, navigation, and privacy changes. Missing checks block closure.
Completion receipts live here as `<foodus-id>-receipt.json`; plans/evidence live in
`context/implementation-plans/milestone-b/<foodus-id>/plan.md`.

## Dispatch FOODUS-001

Intent: reuse existing AFCD management/import UI inside onboarding; preserve existing
source and skip paths. Worker owns onboarding files and onboarding regression tests,
and its plan only; coordinator owns milestone, Design index, receipts, commits, and
Samsung runtime verification. No other Stories in this dispatch.
Requested model/effort: inherit. Observed identity: unknown (host does not expose it).
Worker identity: canonical task `/root/foodus_001`; actual runtime ID follows dispatch.
Stop conditions: missing required acceptance evidence, architectural conflict, or
need to access private device data. Worker returns changes uncommitted.

FOODUS-001 acceptance: 120 unit tests, debug assemble, 2/2 Samsung Android15
instrumentation tests, and whitespace check passed. Initial test-container lifecycle
failure was corrected with private Koin context and cleanup identity assertion.
Receipt: `foodus-001-receipt.json`; completion commit pending at seal time.
No optional independent review selected for this bounded screen reuse.

## Dispatch FOODUS-002

FOODUS-001 reconciled COMPLETED_COMMITTED at `874f80bc`; only unrelated attachments
were dirty before this dispatch. FOODUS-002 worker owns main Settings, Database
Settings, their relevant navigation callbacks/wiring, regression test files, and
`context/implementation-plans/milestone-b/foodus-002/plan.md`. Move the existing
external-database action to main Settings; retain import/export/backup and Back.
Coordinator owns trackers, device execution, receipt, commit. No other Story edits.
One worker `/root/foodus_002`; requested model/effort inherit, observed unknown.
Return uncommitted; preserve other edits. Required checks: deterministic regression,
debug build, Samsung route/absence/Back/retained-action evidence. Stop on privacy or
architecture conflict; no device data clearing, pull, push, or release.

FOODUS-002 acceptance: debug/test APK builds and one Samsung Android15 navigation
test passed; actual production callback relocation and whitespace inspected.
Missing language stub in first fixture run was corrected; no production repair.
Receipt `foodus-002-receipt.json`; completion commit pending at seal time.

## Dispatch FOODUS-003

FOODUS-002 reconciled COMPLETED_COMMITTED at `a3782461`. Only unrelated attachments
were dirty. Worker `/root/foodus_003` owns scanner focus/scroll/inset correction,
targeted Android keyboard layout regression, and its Story plan. Initial assignment
is inspection plus synthetic reproducible device test; coordinator runs baseline
before authorizing the proposed fix. No other Story behavior changes. Coordinator
owns trackers, Samsung operations (including restoring temporary viewport overrides),
receipt, and commit. Tests must use synthetic scanner content without AI calls or
private photos/credentials; preserve installed data. Check short/long hint, keyboard
open/dismiss, scrolling and control reachability on normal and compact viewports.
Requested model/effort inherit, observed unknown. Return uncommitted; no pull/push.

FOODUS-003 independent review intent: `/root/review_foodus_003` is read-only over
the scanner inset diff, synthetic keyboard test, and accepted Story scope. Review
checks code/privacy/test validity; coordinator separately owns final physical and
compact Samsung gates. Required review in the Story plan is adopted. Requested
model/effort inherit; observed unknown. No source/status/commit/device writes.

FOODUS-003 continuation: Samsung portrait reproduced hint y1401..1683 below IME
top1392. Insets alone passed field bounds but visual proof exposed hidden last
lines. Character-bound accessibility API is unsupported, so accepted verification
uses real key/Enter input, touch scrolling/events, measured post-dismiss gap, and
synthetic-only PNG inspection (no behavioral waiver). Custom trailing-edge scrolling
made append text visible but reviewer identified earlier-line editing risk; current
worker ownership expands to `AiScanViewModel.kt` to adopt the existing UI-layer
TextFieldState pattern and remove the custom tail anchoring. Earlier-line edit and
normal/compact visual checks remain required before closure. No FOODUS-003 receipt
or completion commit exists yet. All temporary display overrides have been restored
to physical1080x2400/free rotation after completed runs. Only FOODUS-001/002 are
closed/committed; the milestone remains the sole Story status owner.

Further FOODUS-003 device investigation: the merged Android EditText semantics
include helper content below the visual input. Focus screenshots show the input
border above the IME, so the test is being corrected to measure the actual input
container and text region, retaining visual caret proof. Immediate IME-visible
capture can precede settled rendering; fresh settled captures remain required.
No acceptance waiver or completion transaction has been made.

FOODUS-003 final acceptance: stable three-line state-based editor, consumed system
insets and IME padding, focus/IME-only whole-field relocation. Debug/test builds,
independent static review and whitespace passed. Samsung physical1080x2400 and
compact900x1600 each passed both tests (36.262s/38.519s). Six fresh active-line
screenshots inspected. Native touch input and fresh accessibility cache replaced
unreliable test drivers; supported selection action verifies real earlier-line
editing. Original physical size, default fixed-rotation policy and free rotation
restored. Portrait sizes verified; landscape remains outside accepted evidence.
Receipt `foodus-003-receipt.json` prepared before terminal metadata; commit pending
at seal time. No private persistent data was changed by the synthetic fixtures.

## Dispatch FOODUS-005

FOODUS-003 reconciled COMPLETED_COMMITTED at `6e44b942`; only unrelated attachments
were dirty before dispatch. FOODUS-005 worker owns end-to-end scan estimate parsing,
review, typed route/prefill, Quick Add form and mapping, any necessary domain/Room
representation or migration, relevant unit/instrumentation regression files, and
`context/implementation-plans/milestone-b/foodus-005/plan.md`. Keep the accepted
FOODUS-003 editor/inset behavior. No collection, action visibility, completion
navigation, camera-retake or credential-display changes in this Story. Preserve
manual Quick Add energy controls, unknown-vs-zero nutrients, immutable history and
servings/weight metadata semantics from Story 2.18. Exact observed sample has
780 kcal,35g protein,38g fat,6g fibre,12g sugar; other sample fields are unknown.
Coordinator owns trackers, receipt, Samsung execution, independent review and
commit. Worker returns uncommitted; no device operations, pull/push or releases.
Required checks: parser/mapping/form/persistence/readback with sample, optional
fields and units, direct manual behavior; synthetic isolated Samsung review-to-
entry-to-diary evidence. Use an in-memory/temporary database, never the owner's
stored diary/settings. Stop on privacy/architecture conflict, unsupported external
contract or missing migration evidence. One implementation worker `/root/foodus_005`;
requested model/effort inherit, observed unknown. Independent data-integrity review
is required before completion.

FOODUS-005 independent review dispatch: `/root/review_foodus_005` is read-only over
the final parser, route, form, promotion/snapshot mapping, display and regression
diff, relevant configuration and acceptance. One implementation worker remains
active; reviewer must distinguish intermediate observations from final frozen
identity and wait for coordinator's final identity notification before approval.
Check nullable/zero/energy/units/privacy/history boundaries and evidence gaps.
Coordinator separately owns builds and physical Samsung acceptance. Model/effort
inherit; observed unknown. No source/status/commit/device writes.

FOODUS-005 final acceptance: 128 unit tests in 26 suites, debug/test APK builds,
independent data-integrity review and whitespace passed. Samsung SM-A057F Android15
portrait physical1080x2400 passed all four final cases (36.654s), after a focused
optional case passed (14.155s). Exact 780 kcal sample and unknowns survive production
review/typed route/Quick Add/Room historical readback; optional units/quantity edits,
finite input validation, deliberate calculation and manual defaults are covered.
Fixture-only driver issues were repaired with fresh host-window access, safe inset
geometry, actual button semantics, explicit IME dismissal and accessibility scrolling;
no assertion was waived. Original rotation policy restored. Receipt prepared before
terminal metadata; `foodus-005-receipt.json` will seal before the authorized commit.
No owner diary/settings were changed. FOODUS-006 must verify target promotion editors;
FOODUS-007 owns full Log/home completion. No release/version transition was performed.

## Dispatch FOODUS-004

FOODUS-005 reconciled COMPLETED_COMMITTED at `2f3fe1f1`; only unrelated remote
attachments were dirty. One implementation worker owns collection prompt/parser/
result contracts, scan review selection, per-item Quick Add handoff/navigation,
relevant regression tests and `context/implementation-plans/milestone-b/foodus-004/plan.md`.
First gate is a recorded smallest selection/handoff design covering edit/save/cancel,
partial failure and exact saved counts; report it before implementation. Preserve
FOODUS-003 hint/IME and FOODUS-005 nullable snapshot/energy/storage contracts. No
general split/merge editor, unrelated conversion UI, home completion, retake or keys.
Coordinator owns trackers, builds, Samsung acceptance, independent review, receipt
and commit; worker returns uncommitted. Private Koin/in-memory Room/generated JPEG
only for runtime checks, no global data/settings writes or AI calls. Existing
ScanDataPreservationTest supplies tested accessibility/IME/inset driver patterns.
Required synthetic parser cases: single/coherent dish/multiple/empty/malformed/
partly-invalid; selection, per-item edit/save/cancel, partial failure and exact count.
Required independent review for collection/queue/persistence integrity. Requested
model/effort inherit; observed unknown. No pull/push, releases or device data clearing.

Worker `/root/foodus_004` recorded its pre-code design; coordinator accepted it
before implementation. Use ordered complete snapshots, legacy singleton compatibility,
visible rejected-member count, selectable cards and a scanner-scoped queue. IDs and
acknowledgements must include the result generation as well as item ordinal. Only
successful insertion acknowledges/advances; cancel returns review, failure remains
editable, saved items cannot be submitted again. Guard in-flight/completed insertion.
Last item returns to review summary here; FOODUS-007 owns home completion. Existing
Align stays an explicit alternative, never an inferred saved acknowledgement or
silent queue advance. No general editor expansion. Candidate implementation is now
authorized within the dispatch ownership and original user request.

FOODUS-004 independent review intent: read-only reviewer `/root/review_foodus_004`
owns collection/input validation, review queue/IDs, navigation acknowledgement,
save/cancel/failure/duplicate guards and test validity/privacy. Production/common
tests/resources froze before coordinator core build; Android fixtures remain active.
Wait for final fixture freeze and identity before final approval. Coordinator owns
Samsung/build results and terminal metadata. No reviewer edits or device operations.
Requested model/effort inherit; observed unknown.

FOODUS-004 final acceptance: final unit/debug/test-APK command passed, 140 tests
in30 suites with zero failures/errors. Independent static integrity review and
whitespace passed, final source aggregate recorded in its plan/receipt. Samsung
physical1080x2400 portrait passed four new MultiFoodScanTest methods plus all four
FOODUS-005 preservation methods together (8tests,130.05s). Actual handoff helper,
private Koin and real in-memory Room prove selection, complete per-item values,
cancel/discard, failed later insertion/retry and exact counts; repeated handoff/
pending-save actions cannot duplicate the live insertion. Original rotation policy
restored. No owner diary/settings writes, paid AI, private photos or data clearing.
Receipt prepared before terminal metadata; commit pending at seal time. Last-item
home completion remains FOODUS-007. Next authorized Story after commit is FOODUS-006.
