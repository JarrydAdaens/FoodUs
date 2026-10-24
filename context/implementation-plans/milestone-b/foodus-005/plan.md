# FOODUS-005 implementation plan

Owner: [Milestone B](../../../milestones/milestone-b.md#foodus-005).
Execution status remains in that milestone. Worker owns implementation and this plan;
coordinator owns independent review, Samsung execution, receipt and commit.

## Representation decision before implementation

Reuse existing nullable NutritionFacts values and embedded Room columns. No schema
change, migration or historical rewrite is needed. Expand the serializable scan item
with description, carbohydrates, serving count, total grams and optional nutrients
already supported by NutritionFacts. All nutrition is the absolute total for the
pictured portion; energy is kcal and nutrient masses are grams. Explicit kJ/kg input
converts once at parsing. Missing values remain unknown; real zero remains zero.
Validate finite nonnegative nutrients and positive finite portion metadata at parsing.

Pass the complete accepted snapshot as one JSON string on the typed route, avoiding
another partial positional handoff. Existing manual prefill arguments remain valid.
Scan-origin explicit energy disables automatic calculation initially, even with
missing carbohydrates. Missing scan energy stays unknown until a user deliberately
chooses calculation or enters energy. Manual Quick Add keeps its existing automatic
calculation and blank-to-zero save semantics. Editing existing rows preserves nullable
values and explicit energy when nutrients or energy are unknown, preventing a later edit from
reintroducing this bug. Fully known manual edits retain their prior matching-energy
automatic calculation mode. Historical serving null stays null because it is metadata
and no calculation needs the legacy display fallback of one serving.

Add an editable sugar field alongside fiber. Preserve other supplied supported
nutrients in the form's snapshot, expose them through the existing nutrient display,
and copy them through save/update. Do not build a full new nutrient editor. Quantity
metadata never scales totals. Existing promotion behavior remains available. The
promotion seed and its typed routes also carry the full nullable snapshot, so sugar
and optional nutrients cannot vanish in the promotion seed/route handoff. Existing
scalar-only callers keep their defaults. A local flat nullable nutrition DTO
serializes route payloads, avoiding polymorphic inline nutrient serialization.
Shared domain/relay contracts and Room are unchanged. Target editor runtime behavior
remains unverified here; FOODUS-006 owns the required promotion regression.
Quick Add displays only supplied extras below the editable fields, avoiding a
second duplicate macro/energy/fiber/sugar summary.

## Required evidence

- Exact observed sample: 780 kcal, 35 g protein, 38 g fat, 6 g fiber, 12 g sugar;
  description, carbohydrates, servings and weight unknown (no invented sample data).
- Parser/model/route/form/mapping regressions with absent, zero, present, invalid,
  kJ/kg conversion, fractional servings and optional nutrients.
- Synthetic Samsung production review -> typed route -> Quick Add -> real in-memory
  Room save/readback; no global diary/config writes, AI call, credentials or photos.
- Direct manual auto-calculation, explicit calculation control, unit display/save,
  and historical metadata edits do not alter stored nutrition unexpectedly.
- Debug/test APK build, relevant unit tests, independent integrity review.

## Continuation

Coordinator acceptance is complete; source is frozen for the completion transaction.
The milestone is provisional until its sealed receipt reconciles with the commit.
The worker ran no builds or phone operations; coordinator evidence follows below.

Common regression class: `ScanNutritionPreservationTest` covers the exact sample,
unknown/zero, optional values, explicit kJ/kg conversion, manual initialization and
save semantics, malformed optionals, all 43 distinct supported nutrient sentinels
through the actual scan route/parser/Room column mapping, and both actual promotion
route roundtrips with the local flat snapshot.

Samsung class: `com.maksimowiczm.foodyou.app.ui.food.diary.quickadd.ScanDataPreservationTest`.
Four tests use production scan review, typed route, create/update screens and a real
in-memory Room repository: reported sample and unchanged historical save; optional
nutrients/kJ/kg/portion metadata edit without scaling; unknown energy persisted then
explicit calculation; direct manual auto energy and blank-to-zero compatibility.
Fixture owns its private Koin lifetime, preserves global Koin, generates a JPEG and
returns synthetic parser output from a fake scanner. No camera, AI, credentials,
global diary or settings writes. Readback is through production Room repository and
the existing nutrient display; full home navigation belongs to FOODUS-007.

Coordinator commands: `:app:testDebugUnitTest :app:assembleDebug
:app:assembleDebugAndroidTest`, then instrumentation class above on attached Samsung.
No schema/entity/repository storage changes were made, so no migration is introduced.
Existing columns remain nullable; historical quantities stay metadata only.


Independent-review repair: the generic nullable Double parser accepts NaN, Infinity
and numeric overflow, which could enter Quick Add storage or make promotion JSON
serialization fail. A Quick Add-local wrapper now rejects all non-finite numbers
before form state acceptance, applied to every numeric field (macros, energy, fiber,
sugar, servings and weight). Existing sign validators and shared forms are unchanged.
`QuickAddNumberParserTest` covers NaN/positive and negative infinity/overflow plus
finite, negative, zero and blank compatibility. The existing optional Samsung case
now enters synthetic NaN sugar, verifies Save disabled, restores zero and saves.
No new phone test class or additional device scope. Source refrozen after repair;
coordinator must rerun final relevant checks. Worker ran only whitespace inspection.

Integration boundary for FOODUS-006: this Story verifies full promotion route/seed
mapping, not runtime Product/Recipe editor outcomes. Source inspection found the
existing ProductFormState energy initializer still enables calculation when any
macro is absent (both create/edit initializers). FOODUS-006's required scan-origin
promotion runtime regression must check explicit-energy preservation there. This
is a source lead, not a reproduced Product-editor failure or a claim of completion
for FOODUS-006. The original Quick Add diary snapshot must remain intact.


First coordinator Samsung run (4 tests) reached the fixture but failed before the
nutrition/save assertions: active-window lookup could select the IME, and scrolling
used full-window coordinates that could start on the keyboard. This is missing
acceptance evidence, not a production-data failure or a waived check. The test now
temporarily enables interactive-window retrieval (restored in cleanup), selects the
target package's application window with a fresh cache, waits through navigation
transitions, and swipes within the first non-editable content scroll viewport.
Viewport bounds are clamped above the measured host IME inset; the native swipe
uses the host's measured display ID. Bounded failure diagnostics contain only node
class/bounds/actions, never UI text or settings. Production code is unchanged.
Test/plan refrozen for coordinator rebuild and Samsung rerun; no worker device run.


Second Samsung run still lacked a target application accessibility root after Quick
Add navigation (and at the direct manual screen). No production change is inferred.
The diagnostic candidate now records decor visibility/focus/attachment/display and
bounds; known target/activity package equality; bounded default-display and all-display
accessibility window type/id/layer/display/bounds/root-present/package-match metadata;
and active-root class/presence/package equality. A temporary uncaught-exception
handler records only exception class and 12 stack frames in memory and delegates to
the original handler, restored on cleanup. No exception messages or UI text are
captured. This distinguishes virtual-display mismatch, window access and crashes
before further changes. Coordinator confirmed Android's `windows()` is context-display
scoped and observed an additional virtual display. The fixture now selects application
windows from API30 `windowsOnAllDisplays`, keyed by the measured decor display ID,
instead of assuming UiAutomation's context display. Source is refrozen for one
reported-method diagnostic run; no worker build/phone operations or production change.


The single reported-method diagnostic found an attached/visible display0 host with
lost focus, no recorded fixture crash, and a focused fullscreen system window rather
than an application window. Coordinator filtered shell evidence found the device
awake and unlocked. The next diagnostic adds bounded package identifiers and the
first 25 active-system node class/resource IDs only, to identify the overlay. No
labels, text, descriptions, screenshots or broad device logs are added. A reverse
swipe beginning too close to the status bar could open the notification shade;
this remains a hypothesis pending these identifiers. Diagnostic test/plan refrozen.


Coordinator and worker independently identified the reverse-swipe geometry error:
Scaffold's LazyColumn uses content padding but its accessibility bounds can begin
at y0, so a downward gesture from top+16dp can open the system shade. The driver now
clamps both gesture endpoints below measured system-bar top inset plus the 64dp top
bar, and above IME, with explicit bounds checks. Field matching additionally accepts
exact accessibility hintText labels, retaining independent editable-value assertions.
Bounded target-fixture diagnostics report known field-label match booleans/keys,
classes and bounds only, never editable values or active-system text. Production
code and acceptance assertions are unchanged. Test/plan refrozen for single-method
rerun, with all four tests required after the driver is verified.


Single reported-method rerun passed every editable-value assertion and invoked Save.
Its next fixture-only failure was the readback heading rendered beneath the system
status bar (heading0..68, status bar through73) by the synthetic readback Column.
The readback host now fills the window and applies safeDrawingPadding before scrolling,
keeping its heading and edit action reachable. Production code and acceptance
assertions are unchanged; test/plan refrozen for the complete four-test Samsung run.


The next full Samsung run (58.981s) passed the reported sample and direct manual
cases. Two fixture issues remain: invalid-sugar disabled-state inspection read an
enabled Icon child instead of the actual Button ancestor, and a swipe guard compared
IME geometry across autofocus animation frames. The test now resolves the actionable
Save Button for enabled-state assertions, and snapshots gesture geometry atomically
on the main thread, requiring two equal measurements and retrying within the bounded
search if insets change. No production changes or relaxed data assertions. Test/plan
refrozen for all-four rerun; no worker build/device operations.


Latest four-test run (120.934s) passed reported sample, direct manual and unknown
energy/calculation. Optional data inspection still could not scroll to carbohydrates
with IME open; no data-value assertion failed. FOODUS-005 does not claim keyboard
behavior (covered by FOODUS-003), so each field inspection now waits for delayed
autofocus and explicitly hides the IME via its window controller, without Back
navigation. Scrolling first uses the actual content viewport accessibility action,
with the existing bounded native-touch fallback when unavailable. Acceptance values,
Save validation and persistence assertions are unchanged. Test/plan refrozen for
single optional-method rerun, followed by all four if it passes; no worker device run.

## Final coordinator acceptance

- JDK21 offline `:app:testDebugUnitTest :app:assembleDebug
  :app:assembleDebugAndroidTest` passed: 128 tests, 26 suites, no failures/errors.
  Subsequent changes were Android fixture-only; its final APK was rebuilt successfully.
- Samsung SM-A057F Android15 physical1080x2400 portrait: optional method passed
  (14.155s), then the complete final four-test class passed (36.654s). Exact sample
  retains 780/35/38/6/12 and unknown carbohydrates/description/quantity through
  production review, typed route, editable form, save and historical Room readback.
  Optional values, kJ/kg, true zeros, metadata edits, invalid NaN sugar rejection,
  unknown energy plus deliberate calculation, and direct manual defaults passed.
- Independent reviewer `/root/review_foodus_005` final static PASS, no findings;
  sorted-path SHA256 aggregate across 22 application/test files:
  `42c7fce707e62fa92b19f924cba1bde90900030ff5a69669a0a265519d41f9fa`.
  Coordinator recomputed the same identity. Whitespace passed.
- Fixtures use only generated image/synthetic values, private Koin and in-memory
  Room. No global diary/settings writes or AI calls. Default fixed-rotation policy
  and free rotation restored; no display-size override was used for this Story.
- Existing SDK XML tooling-version warning remains; it did not fail the builds.
  Full home navigation and Product/Recipe destination runtime remain the explicit
  FOODUS-007/006 boundaries described above. No schema or release/version change.

Receipt prepared before terminal metadata:
`context/_boss-runs/milestone-b-sequential/foodus-005-receipt.json`.
Commit is pending at seal time. Next step: reconcile the authorized completion
commit before dispatching FOODUS-004.
