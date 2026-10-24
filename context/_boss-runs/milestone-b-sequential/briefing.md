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

## Dispatch FOODUS-006

FOODUS-004 reconciled COMPLETED_COMMITTED at `691fdf49`; only unrelated remote
attachments were dirty before dispatch. One worker `/root/foodus_006` owns visible
Product/Recipe actions in shared Quick Add, necessary promotion-runtime repairs,
targeted regression tests and its Story plan. Preserve Save, manual/scan/create/
edit/historical use, cancellation, source snapshots and existing Story19 mapping.
Remove redundant overflow actions and use a compact accessible layout, without
forcing all actions into the toolbar. Existing completed Story19 decisions and
implementation supersede its historical plan's stale OPEN questions; no new product
decision is required. No home-completion, retake, release-history or key edits.

FOODUS-005 recorded a source lead: ProductFormState may enable automatic energy
when any macro is unknown, even with explicit780 kcal. Required scan-origin
promotion regression must reproduce or disprove this before any repair; keep a
repair scoped to promotion prefill where possible, preserving normal product forms.
Test actual production editors/mapping and cancellation over isolated repositories,
not only route/seed serialization. Keep direct manual behavior and original history.
Use shared ScanTestDriver for Samsung fixtures; generated images, private Koin and
in-memory/temporary DB only, no global diary/settings writes, AI/private photos/keys.

Coordinator owns trackers, builds, Samsung checks (physical and compact viewport),
independent review, receipt and commit. Worker returns uncommitted, preserves other
edits and unrelated attachments, and performs no builds/device/commit/pull/push.
Independent review required for any promotion data-path repair. Requested model/
effort inherit; observed unknown. Do not edit later Stories or publish a release.

FOODUS-006 baseline reproduced on Samsung: initial Product prefill retained780,
then filling its required unknown carbohydrate with0 changed energy to482
(one failed assertion,26.415s). Coordinator authorized a promotion-prefill-only
manual-energy initialization flag; normal Product/RemoteProduct defaults remain.
The final seven-file implementation/test/plan candidate is frozen. Required
independent reviewer `/root/review_foodus_006` is read-only over shared action layout,
scoped energy initialization, accessibility descriptions and the five-method
synthetic production-editor/Room fixture. Coordinator owns builds and physical/
compact Samsung checks plus eight prior data/queue regressions on the physical
viewport. No review build/device/edit/commit actions; model/effort inherit,
observed unknown. Final source identity must be recorded before completion.

FOODUS-006 final acceptance:140 unit tests/30 suites, debug/test APK builds,
independent review and whitespace passed. Physical five full conversion cases
passed338.054s; compact four passed plus final reported-scan case65.289s. Scoped
Android status records and unchanged production/shared-driver/prior-test hashes
verify the earlier eight FOODUS-004/005 passes. Final five-source layout method
passed physical21.101s/compact39.103s. Ten final synthetic action screenshots
inspected; display overrides restored. Failed intermediate whole invocations remain
recorded, never represented as overall passes. Receipt prepared before terminal
metadata; seal/commit next. No owner data writes or release/version change.

## Dispatch FOODUS-007

FOODUS-006 reconciled COMPLETED_COMMITTED at0b3f3e5c; only unrelated attachments
dirty before dispatch. One worker `/root/foodus_007` owns successful scan completion
navigation, queue-aware production handoff changes, relevant regression files and
its Story plan. Preserve 004/005/006 persistence, selected queue, retries, counts,
conversions and direct Quick Add navigation. Complete only after successful final
selected save; remove completed scanner/add from Back stack to actual Home/Log.
Use production navigation helper in private synthetic Samsung fixture and real
in-memory Room; no owner settings/data, live AI, camera, keys or data clearing.
Coordinator owns builds/device, milestone, receipt/commit and independent review.
Return uncommitted; no later Story changes, pull/push, release/version/dependency.
Read-only navigation/integrity review required. Inherit model/effort; observed unknown.

FOODUS-007 production freeze: accepted design keeps successful insertion and typed
handoff acknowledgement, then scanner validates generation/queue head. Only a new
accepted final transition emits completion; helper pops canonical Home inclusive=false.
Independent reviewer `/root/review_foodus_007` is read-only over navigation, queue,
privacy and regression validity. Production review can begin while Android fixture
is finalized; final approval requires frozen complete source identity. Coordinator
owns runtime/build checks and terminal metadata. Model/effort inherit, observed unknown.

FOODUS-007 final acceptance:141 unit tests/30 suites, debug/test APK builds,
independent navigation/integrity review and whitespace passed. Samsung six final
queue/completion/direct/failure cases plus four scan data regressions passed10/10,
112.954s. Test-only assertion/import compile corrections re-reviewed; production
unchanged. Canonical Home Back-stack and persisted count checked through production
helper with private synthetic Home contents/Room. No owner data writes; original
rotation policy restored. Receipt prepared before terminal metadata; commit next.

## Dispatch FOODUS-008

FOODUS-007 reconciled COMPLETED_COMMITTED at63eab5af; only unrelated attachments
dirty before dispatch. Worker `/root/foodus_008` owns preview-to-existing-camera
retake wiring, related expect/actual/callers if required, acquisition error retention,
regression fixtures and its plan. Tap launches retake directly; preserve prior JPEG
until successful replacement, including gallery-origin, cancel, denial and errors.
Reuse existing capture/downscale path; no camera or gallery private content access.
Coordinator owns builds/device, independent scoped review, milestone, receipt and
commit. Device fixture uses generated JPEG/Bitmap and controlled ActivityResult
registry/permission context; never launches real camera/picker or changes owner
permissions/settings. No later Story, version, release, dependency or schema edits.
Return uncommitted, no builds/device/commit/pull/push. Model/effort inherit, observed
unknown. Required independent review for acquisition/permission/state boundaries.

FOODUS-008 final source/fixture frozen. Reviewer `/root/review_foodus_008` owns
read-only acquisition/permission/error/state/privacy and actual-contract regression
review across five production files and new Android class. Coordinator owns builds,
physical/compact Samsung gates and completion metadata. System camera/picker/hardware
are explicitly simulated; generated results preserve owner privacy. Inherit identity.

FOODUS-008 final acceptance:141 unit tests, debug/test APK builds, independent
review and whitespace passed. Four actual-composable/state/acquisition cases each
passed Samsung physical6.884s/compact6.616s; overrides restored. Test-only described
click-target repair re-reviewed; production unchanged. Hardware/system acquisition
UI are simulated; no private photos/data or permission changes. Existing host's base
lifecycle observes secureScreen preference: this qualifies earlier zero-global-read
shorthand across these DeveloperActivity fixtures, without mutating sealed receipts.
No diary/AI credential/photo reads or owner writes introduced. Receipt prepared;
seal/commit next, no release/version change.

## Dispatch FOODUS-009

FOODUS-008 reconciled COMPLETED_COMMITTED atce6bfff; only unrelated attachments
dirty before dispatch. GitHub plugin GET verified full release collection (page2
empty): latest published v2.23.0 UTC2026-07-27, previous v2.19.1 UTC2026-07-26,
first v2.19.0 UTC2026-07-26. Comparisons retrieved and exact commit identities
recorded in generated app/build/foodus-009-release-ledger.json. Local catalog3.5.0
is not published; do not invent its release or treat it as latest shipped. Initial
seed says1.0 on2026-07-24 under interim ACME identity; worker must verify history
and label it as historical fork announcement, never a release tag.
Worker `/root/foodus_009` owns fact verification, three-entry existing changelog,
fork-version lookup and relevant tests/plan. Root owns trackers, builds/Samsung,
independent fact/integrity review, receipt and commit. No publication/tag/pull/push,
version change, rewriting old release records, later Story or private device data.
Use UTC publication dates consistently and explicit unknown-version lookup without
false shipped claim. Preserve upstream credit/license/About attribution. One worker,
model/effort inherit, observed unknown. Content fact review required before closure.

FOODUS-009 production freeze: StaticChangelog exact three verified entries, fork
current lookup; automatic sheet seen identity now uses fork version. Independent
reviewer `/root/review_foodus_009` audits facts/announcement qualification, upstream
credit, shared version/unknown/transition behavior and regression validity. Common
test ready; Android fixture still active, so final approval waits complete freeze.
Read-only review; no build/device/edits/commit. Coordinator owns runtime/content gates.

FOODUS-009 final acceptance: live full release ledger/page2 and shipped comparisons
verified; qualified1.0 announcement uses owner seed and historical commits.145 unit
tests/31 suites, APK builds, independent content/version review and whitespace passed.
Samsung three actual About/history/automatic-sheet cases passed19.991s. Initial
test-only unavailable runTest import corrected to existing runBlocking and re-reviewed.
Fork installed/seen identity, unknown draft/four-part behavior, credit and Back pass.
No publication/version changes or owner writes; receipt prepared before terminal
metadata, seal/commit next. Next and last authorized Story is FOODUS-010.

## Dispatch FOODUS-010

FOODUS-009 reconciled COMPLETED_COMMITTED at958da852; only unrelated attachments
were dirty. Last worker `/root/foodus_010` owns AI key display correction, relevant
resources/regression tests and its Story plan. Mask by default on opening/re-entry/
recreation; explicit reveal/hide with local non-restored visibility. Preserve draft
editing/paste, validation/save and all storage/provider precedence. Reuse native
state-based secure field pattern and localized password actions where suitable.
Dummy keys and isolated repositories/validator only; no real key, live network,
owner settings writes, clipboard disclosure, credential logs/screenshots or data
clearing. Existing host secureScreen preference observation remains qualified.
Coordinator owns builds, physical/compact Samsung, required independent privacy
review, trackers/Design B closure, receipt and final per-Story commit. No version
transition/release/publication/tag/pull/push or other product changes. Inherit model/
effort, observed unknown. Worker returns frozen uncommitted code, no device/build.

FOODUS-010 production freeze: only AiSettingsScreen.kt, native resolved outlined
secure editor, screen-local non-saveable visibility, lifecycle pause/stop reset,
existing localized eye actions and same VM draft state. Reviewer `/root/review_foodus_010`
performs read-only privacy/state/API/fixture review. Final test freeze/hash pending;
coordinator separately runs builds and physical/compact Samsung gates. No reviewer
edits, build/device or commit. Model/effort inherit, observed unknown.

FOODUS-010 final acceptance: 145 unit tests/31 suites, debug/test APK builds,
independent privacy review and whitespace passed. The initial fixture confused
native editable accessibility text with rendered obfuscation; exact cached binary
provenance established separate codepoint rendering. No production change followed.
Final test APK and all three phone tests passed physical 19.795s/compact 50.955s.
All 22 bounded synthetic-field images were inspected: 14 bullet-masked, eight
legibly revealed dummy keys. Private paste, real recreation/restoration sentinel,
retained/fresh navigation, background, edit/validate/save passed. Final display
readback confirms physical 1080x2400/default fixed policy/free rotation restored.

All ten authorized Stories now have acceptance evidence. Milestone B and its Design
index close in this final Story bundle, with no next implementation dispatch.
The legacy catalog exception remains; no release/version/tag/push/pull or publication
occurred. Owner data and clipboard are preserved; only pre-existing unrelated
attachments remain excluded. Final receipt prepared before terminal metadata;
seal/commit/reconciliation complete delivery, without a bookkeeping commit.

## Owner-requested version follow-up (2026-10-25)

After the ten Story commits, the owner explicitly requested updating the version
as part of this task. This authorizes ending the temporary legacy catalog exception
and starting canonical four-part draft tracking now. The catalog remains the sole
current version source. Story statuses and the numbered milestone remain unchanged.
The active milestone and combined terminal Story count are derived from Design,
Milestone 3 and Milestone B, with Externalized gates excluded and distinct IDs counted.

This bounded coordinator follow-up changes only catalog values/comments and the
policy/runbook transition wording. Existing commit authority carries forward.
Deterministic derivation, current primary release/tag ledger, generated BuildConfig,
APK metadata and data-preserving Samsung installation are the selected checks;
no separate optional reviewer is needed for this configuration-only correction.
No publication, tag, release, pull/push or source-history rewrite is authorized.
First-publication Obtainium preflight, both-phone code inspection and signed-release
rollout remain separate policy gates; one Samsung is currently attached. Its prior
dev code was126; the release application ID was not installed on this phone.
New receipt `version-followup-receipt.json` supersedes the legacy-version decision
in the final Story receipt without modifying any sealed Story receipt.

Version follow-up acceptance: narrow debug assemble passed. Automated tracker
index/detail agreement derived the canonical value; generated BuildConfig preserved
the upstream attribution and used the complete fork value. APK badging and installed
Samsung package metadata matched code127 and the four-part debug version. The same-
package install succeeded without uninstalling or clearing data. The current primary
release/tag ledger had no collision. Whitespace passed. First-publication/second-
phone/Obtainium/signing gates remain pending separate publication work, not blockers
to the requested local draft update. New receipt prepared before final metadata;
seal and authorized follow-up commit finish this request. No sealed Story receipt
was changed and no new Story closure was counted.
