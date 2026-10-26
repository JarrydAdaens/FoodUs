# FOODUS-006 implementation plan

Owner: [Milestone B](../../../milestones/milestone-b.md#foodus-006).
The milestone owns status. Worker owns shared action presentation, promotion
regressions and this plan; coordinator owns builds, Samsung checks, independent
review, completion receipt and commit. FOODUS-004/005 are already committed.

## Scope and decision before implementation

Replace the shared Quick Add overflow with two labelled full-width outlined
buttons before the form, vertically stacked so translated labels and compact
displays do not compete with Save in the toolbar. Keep existing validation,
busy/save guards, callbacks and discard behavior. The same component serves direct,
scan, create and historical edit paths. No navigation completion change (FOODUS-007).

Preserve Story 19's accepted mapping: real-weight totals become per-100 g; unknown
weight uses the existing 1:1 fallback without inventing weight; recipe promotion
uses a real backing product and ingredient-derived totals; cancel deletes the
uncommitted placeholder; repeat promotions are permitted. Preserve FOODUS-005's
nullable supported nutrients, explicit energy and quantity-as-metadata contracts.

Product form initialization enables calculation when any macro is absent. Its
Compose effects wait for macro edits before calculating, so source inspection alone
does not establish an initial 780-to-482 overwrite. First add an Android regression
through the production promotion product editor: assert 780, leave unknown
carbohydrates blank until the user explicitly supplies zero, then assert energy
remains 780. Coordinator runs that candidate before any production energy fix.
Required-macro validation remains intact; tests may explicitly fill missing macros.

## Required checks

- Production visible labelled buttons and Save: normal/compact Samsung, direct and
  scan create, current and historical edit; no clipping/overlap or overflow duplicate.
- Actual product editor prefill, required macro validation and explicit calculation;
  optional nutrients/weight conversion/unknown-vs-zero preserved through save.
- Actual recipe editor/backing-product lifecycle: save, cancel cleanup, repeats;
  original diary snapshots and in-progress source edits unchanged.
- Existing promotion mapping and FOODUS-004/005 regression classes; appropriate unit
  tests and debug/test APK build, independent evaluation.
- Synthetic data only, private Koin and in-memory repositories; no owner settings,
  diary, live AI, credentials, camera, dependency/schema or version changes.

## Continuation

Coordinator Samsung baseline reproduced the defect in 26.415s: Quick Add and the
initial Product draft both displayed 780 kcal. Explicitly completing the required
unknown carbohydrate field with zero changed energy to 482 kcal. The regression
failed at its final energy assertion, not an infrastructure step. Baseline output:
`app/build/foodus-006-baseline-device.txt` (generated, untracked build output).

The authorized repair adds an optional preservation flag only to the Product
form's domain-product initializer; promotion prefill passes true. Both explicit
and unknown energy start in manual mode until the user deliberately opts into
calculation. Ordinary blank/product edit/download initializers retain their
existing defaults and required-macro validation. Product Save and the deliberate
energy-mode toggle gain existing accessibility descriptions.

The candidate is now frozen for coordinator builds and acceptance. Shared actions
are stacked full-width buttons above the form; Save stays in the toolbar. No
overflow entries remain. Existing validation, busy/back/discard and save guards
are retained, with guarded conversion callbacks as well as disabled controls.

Android class: `com.maksimowiczm.foodyou.app.ui.food.diary.quickadd.QuickAddConversionTest`
(five methods):

- `scanProductKeepsExplicitEnergyWhenUserCompletesRequiredMissingMacro`: actual
  scan typed route -> Quick Add -> promotion typed route -> production Product
  editor/VM/use case/Room. Required missing carbohydrate remains blank/invalid
  until the user enters zero; 780 survives that edit and Product save. Source
  unknowns stay unknown; repeat/cancel and source save preserve snapshots. A
  second isolated flow verifies unknown Product energy requires deliberate
  calculation and preserves unknown energy in the source form.
- `weightedScanRecipeCancelCleansPlaceholderAndRepeatSavePreservesTotals`: real
  placeholder lifecycle, two cancels with cleanup, repeat/save, ingredient-derived
  total reproduction, nullable nutrients, real zeros and portion metadata.
- `directCreateShowsAccessibleActionsKeepsSaveAndNormalProductCalculation`: direct
  Quick Add validation/Save and normal blank Product automatic calculation plus
  deliberate manual switch remain functional.
- `currentEditPromotionsPreserveSnapshotAndUnsavedEditsAllowingRepeats` and
  `historicalEditPromotionsPreserveSnapshotAndUnsavedEditsAllowingRepeats`: actual
  Update Quick Add, discard of an edited Product draft, recipe cancel cleanup,
  repeated Product saves and recipe save preserve the complete source row until
  explicit source Save. In-progress source name edits survive editor cancellation.

Layout assertions inspect one final accessibility tree for exactly one visible
label per conversion/Save, actual enabled clickable buttons, nonempty full bounds
inside the host window, no overlapping touch targets and no overflow action.
PixelCopy saves only the private synthetic host window to external-files
`foodus-006/<case>-<width>x<height>.png`, supporting the coordinator's physical and
compact viewport visual checks without capturing another app/display.

No worker builds, unit execution or phone operations ran. `git diff --check`
passes; runtime acceptance remains pending. Coordinator runs all five methods on
physical and compact Samsung, then existing four `MultiFoodScanTest` and four
`ScanDataPreservationTest` methods on physical because the shared layout changed.
Existing `QuickAddPromotionMappingTest` and broader unit/debug/test APK checks
remain required. No dependency, schema, release or version changes.

## Coordinator runtime investigation and fixture corrections

The first physical run passed all eight existing FOODUS-004/005 regressions but
stopped all five new methods at a fixture class-name assumption. Compose exposes
the clickable View and Button role separately. The fixture now measures the
enabled ancestor advertising ACTION_CLICK, keeping simultaneous labels, full bounds,
no overlap/overflow and synthetic window captures.

The subsequent five-method physical run completed in 278.297s. Two methods reached
the synthetic Saved destination, but its bare Text sat beneath the system status
bar. That test-only destination now fills the host and applies safeDrawingPadding.
All three Room Flow.first readbacks now have 10-second coroutine timeouts so a
blocked read cannot defeat the enclosing driver's bounded await.

Three methods passed their strict stored Product/placeholder unknown-value and
source-snapshot assertions, then failed the Recipe-derived null comparison.
Existing domain provenance: Recipe.kt:36 sums ingredient facts; RecipeIngredient.kt:25
scales the Product facts while NutrientValue.kt:55/61 preserve null under scaling.
NutritionFacts.kt:331-332 folds the sum from NutritionFacts.Empty, whose fields are
Complete(0) (including trans fat at198). NutrientValue.kt:42 then computes
Complete(0) + Incomplete(null) as Incomplete(0). This is existing partial-total
semantics, not a promotion/storage loss. No global domain arithmetic changed.

The fixture keeps all stored Product and source null assertions strict and adds
the saved recipe ingredient snapshot to that strict check. Recipe-derived totals
must match every supplied value; absent values must retain the established
Incomplete(0) aggregate representation, never a Complete known zero. Recipe
aggregation's numeric null-to-zero representation is an explicit existing limit.
Candidate refrozen for coordinator physical/compact runs; no worker build or
device action ran during these corrections.

Coordinator strengthened the supplied Recipe aggregate comparison to require
Complete status as well as the matching numeric value. The resulting physical
five-method run passed in338.054s. Compact passed current/historical edit, weighted
recipe and direct create; the reported scan method stopped before data assertions
because its conversion target overlapped the pinned toolbar after field lookup.
That compact five-method run completed in258.078s. The raw coordinator outputs
remain unchanged under app/build for archival.

Fixture anchoring now scrolls the actual source viewport backward to its start
(at most twelve accessibility actions) after shared-driver IME dismissal, then
collects all action labels/targets from one fresh tree. Conversion targets must be
fully below the pinned64dp toolbar as well as inside the host and nonoverlapping.
Bounds are included in failure messages and window-only screenshots are captured
before geometry assertions. No production layout change was inferred or made.

New inexpensive layout-only method:
`sourceActionLayoutAcrossDirectScanWeightedCurrentAndHistoricalForms`. It opens all
five source variants and applies the same full action gate without promotion/save
cycles; screenshots use the `layout-` prefix in the existing foodus-006 directory.
Coordinator reruns this method on physical/compact and the reported full scan
method on compact; prior passed data paths remain valid with unchanged production.
Fixture and plan refrozen; no worker build, device command or commit ran.

## Final acceptance and completion

Independent reviewer `/root/review_foodus_006` PASS, no findings, including final
fixture anchoring and five-variant layout method. Final six-source aggregate:
`85f585d964ca1418e8ec9e31efd3b28ceefd1dda1c1dc6b6f358e3acd1c8a718`.
Checked artifact identity:
`d275eee42371a3da242a8fdf02afaabce070eff2201f14244e148870078e0f91`.

Final production core command passed140 unit tests/30 suites, zero failures/errors,
debug and Android test APK builds. Fixture-only changes subsequently rebuilt with
`:app:assembleDebugAndroidTest`; whitespace passes. Production, shared driver and
earlier eight test-class fingerprints stayed unchanged throughout fixture repairs.

Samsung SM-A057F Android15 acceptance:
- Five complete conversion data methods physical1080x2400 passed338.054s.
- Four complete conversion methods compact900x1600 passed in the earlier258.078s
  run; reported scan stopped at positioning only, before its data checks.
- Final five-source layout method physical passed21.101s and compact39.103s.
- Final reported scan full conversion/data method compact passed65.289s.
- All eight existing FOODUS-004/005 data/queue cases passed. Exact status0 records,
  unchanged production/shared driver/prior tests and archived data outcomes verified
  by `app/build/check-foodus-006-preserved-data.py`. Original failing invocations
  are explicitly not claimed overall passes; passed methods are scoped evidence.

Ten final layout screenshots (five source variants at each size) were inspected:
labelled full-width conversions and Save are visible, distinct and unclipped. The
bounded fixture scroll anchors source content above any previous field lookup;
actual target geometry below the toolbar and full containment also pass. Original
size/default fixed-rotation policy/free rotation restored. Tests use private Koin,
synthetic food and actual in-memory Room, without owner diary/settings writes.

No release, version, dependency or schema change. Existing Recipe partial-total
Incomplete(0) semantics are recorded above; stored unknowns remain strict. Landscape
and process-death durable exactly-once behavior are not claimed by this Story.
Receipt [foodus-006-receipt.json](../../../_boss-runs/milestone-b-sequential/foodus-006-receipt.json)
prepared before terminal metadata; seal and authorized completion commit follow.
