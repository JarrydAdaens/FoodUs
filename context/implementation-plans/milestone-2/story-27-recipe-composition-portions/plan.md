---
name: story-27-recipe-composition-portions
description: Reconcile Recipe composition, yield, explicit save, and portioned diary snapshots.
metadata:
  version: "1.0"
  owner: "Jarryd Adaens"
  repo: "FoodUs (fork of maksimowiczm/FoodYou)"
---
# Plan: Recipe composition, yield, and diary portions

Execution state: implementation released after accepted/reconciled Story 26. Story 27 is In Progress; coordinator owns terminal status.
Authority: [Milestone 2 Story 27](../../../milestones/milestone-2.md#story-27),
[Design vocabulary](../../../design.md#domain-model-shared-vocabulary), and the
[serial run briefing](../../../_boss-runs/milestone-2-domain-meals-sequential/briefing.md).
The milestone owns Story status; this plan owns implementation decisions, checks,
evidence, and continuation. Commit policy: none. No pull, push, tag, or publication.

## Outcome and scope

Reconcile the existing Recipe editor and diary flow so a saved Recipe combines its
Product/Recipe ingredients into one composite nutritional item. Yield is its reference
serving count; a portion is the quantity actually eaten. A three-serving Recipe logged
as two servings contributes two thirds of its ingredient totals, exactly once. Ordinary
logging produces one captured DiaryEntry at the selected date and DiarySlot. Unpack
remains a separate deliberate action that preserves the same amounts and nutrition.

Retain integer positive yield, fractional ingredient/diary measurements, existing nested
Recipes, stable typed IDs, notes, liquid status, and current navigation/editor foundations.
Saved Meal remains a reusable bundle: a Recipe portion is one constituent beside other
entries, never automatically expanded or converted into a Meal definition.

The current planning lease permits writing this file only. No code/resource/test changes,
builds, device actions, native approvals, tracker/Design/catalogue edits, or commits are
permitted. Story 26 is the sole active implementation worker; its source can evolve.
After release, worker ownership is relevant app/src/**, shared/resources/**, and this plan.
The coordinator owns status/version changes, independent review, final build timing,
receipts, and all emulator operations. Preserve all unrelated edits and accepted evidence.

No new unit/density model, output-weight field, fractional yield storage, catalogue hub,
web/bulk import, photo, sharing, relay, or Meal-to-Recipe conversion is selected. Keep
upstream namespaces, historical paths, serialized/wire contracts, and storage identities.

## Inspected behavior and defects

Paths below are relative to app/src/commonMain/kotlin/com/maksimowiczm/foodyou/.
Inspection is a planning observation of evolving Story 26 source, not a frozen baseline.

- food/domain/entity/Recipe.kt computes totalWeight by mapNotNull(weight).sum(), then
  canonical nutrition by omitted ingredient facts divided by that total. Missing bases
  can disappear or become zero; empty/invalid totals and yield cause invalid division.
  RecipeIngredient computes amounts through the shared item weight method.
- RecipeSnapshot and RecipeIngredientSnapshot repeat the calculation; snapshot Product
  weight currently throws if a serving/package basis is missing. Snapshot and definition
  calculations must agree without reading the live catalogue after capture.
- CreateRecipeUseCase and UpdateRecipeUseCase validate blank name, positive Int servings,
  and nonempty composition, but do not validate each positive finite quantity or its
  resolvability. Existing circular checks flatten constructed objects, and update checks
  the old recipe instead of the proposed composition. Recursive repository hydration is
  unsafe if a self/cyclic graph is written. Search excludes the edited recipe and its
  dependants, but is not an authoritative write-boundary safeguard.
- RecipeFormState already holds ingredient changes in memory; add/edit/remove do not
  persist the Recipe. Back/discard screens offer cancellation. Preserve that structure.
  Create/UpdateRecipeViewModel throw on domain errors; no save-in-progress guard protects
  repeated taps. Recipe name uses product_name, and summary does not state total versus
  per-serving nutrition clearly.
- CreateRecipeScreen and UpdateRecipeScreen remember preview flow only by ingredient list.
  Yield/name/note/liquid changes alone can retain the old constructed Recipe. Build the
  preview from the complete draft and update yield and ingredient previews immediately.
- MeasureIngredientScreen falls back to 100 g/mL when a stored measurement cannot resolve,
  allows Save without explicitly gating the current valid amount, and throws for failed
  preview calculations. Preserve the requested quantity visibly and let the user repair
  it; no invented replacement amount or crash.
- AddEntryViewModel.addEntry captures a RecipeSnapshot and creates one entry. Its explicit
  unpack path currently creates each constituent through separate create transactions;
  a later error can leave partial entries. RecipeModel repeats scaling arithmetic.
- UnpackNutritionalItemDiaryEntryUseCase wraps replacement in TransactionProvider, but
  deletes the original before computing/validating all replacement rows. Validate the
  complete operation first, then replace atomically; failures preserve the original.
- RoomRecipeRepository recursively observes live ingredient definitions. RecipeDao has
  @Transaction insertRecipeWithIngredients and updateRecipeWithIngredients; update replaces
  the recipe metadata and all its ingredient rows inside that transaction. These are real
  aggregate-write seams, not a reason to replace repositories or add another framework.
- FoodYouDatabase implements TransactionProvider using useWriterConnection and
  immediateTransaction; Domain TransactionScope.rollback delegates to Room rollback.
  Returning Err alone does not imply rollback after writes. Room diary repository insert
  snapshots nested composition and Measurement inside its own immediateTransaction.
- SaveMealUseCase captures one total-nutrition MealItem per diary entry. ApplyMealUseCase
  creates one manual snapshot entry per saved item in a single outer transaction. Thus a
  Recipe portion currently retains its one-constituent boundary, but legacy Meal data does
  not retain live Recipe identity or original quantity/unit. Story 24 owns that editor model.

## Settled calculation and validation decisions

Use Story 26's NutritionalItem / NutritionalItemId and explicit NutritionalItemSnapshot,
ProductSnapshot, RecipeSnapshot, RecipeIngredientSnapshot, and NutritionalItemDiaryEntry
families. Product(7) and Recipe(7) remain distinct. Reconcile actual accepted source and
MeasurementResolver contract after Story 26 receipt acceptance before any source edit.

Keep a small shared domain calculation seam for Recipe composition and portion fractions,
used by definitions, snapshots, UI models/previews, diary boundaries, and unpack. Reuse
MeasurementResolver for Product metric resolution and finite/basis checks. Do not create
another Portion entity or put persistence into entities. Exact helper placement/name is
an implementation detail to settle against the accepted predecessor, not a new domain type.

1. Compute each ingredient's total nutrition for its explicit measurement first. Product
   nutrition is per-100 canonical facts times resolved amount / 100. A nested Recipe
   serving portion uses the nested Recipe total times quantity / its own yield. Parent
   yield is not applied until resolving a parent portion. Preserve repeated ingredients
   and shared descendant recipes; they are amounts, not duplicate IDs to discard.
2. Aggregate metric basis is known only when every ingredient amount has a positive finite,
   compatible metric basis for that Recipe's mass/volume dimension. Never sum g and mL
   as though interchangeable or infer density from liquid status. Mixed or unresolved
   bases remain nullable unknown; the liquid flag and stored quantities are retained.
3. Total ingredient facts and aggregate metric basis are separate. Serving(q) uses q / yield;
   the existing Package(q) whole-Recipe measurement uses q times total. These fractions do
   not need an aggregate gram/mL basis when ingredient nutrition resolves. Direct metric
   portions require a complete compatible aggregate basis; their fraction is metricAmount /
   totalMetricAmount. For known bases, serving and equivalent metric portions agree.
4. Canonical Recipe per-100 facts are derived only from a known compatible basis. They must
   not be the mandatory intermediate for serving totals. No fake totalWeight=0, 1, or 100
   may bridge unavailable data. Relevant callers need the recipe-aware nutrition/portion
   path; do not weaken Product validation or Quick Add absolute-total semantics.
5. Missing nutrients remain unknown/incomplete through aggregation and scaling. An unknown
   nutrient is not silently replaced with a partial sum or a complete zero. In particular,
   all-unknown inputs remain unknown. Apply Recipe-specific aggregation semantics rather
   than globally changing Quick Add/provider behavior. UI explains unavailable totals or
   nutrition using existing incomplete-data patterns.
6. New writes require nonblank name, nonempty ingredients, yield > 0 in current Int storage,
   and each quantity > 0 and finite. Reject overflow, incompatible Product units, missing
   reference bases needed by the chosen ingredient measurement, absent ingredients, and
   invalid computed known facts before any mutation. Unknown nutrient fields are allowed;
   they are not invalid numbers. Fractional portions are supported; fractional Recipe yield
   is outside scope. Large/overflowing or unparsable yield text receives a recoverable error.
7. Legacy invalid/unresolvable compositions remain readable as explicit unavailable data;
   never add throwing constructor requirements that prevent database reopen. New save/log/
   unpack operations that cannot resolve their necessary amounts fail without writes.
   Where required facts resolve but only aggregate output weight is unknown, serving logging
   and serving-based nested ingredients remain supported under decisions 1–3.
8. Validate candidate Recipe edges with typed Recipe IDs and a path-local visiting set
   before recursive hydration or writing. Reject direct self-reference and indirect cycles
   introduced by an update; allow valid repeated/shared branches. Use the proposed graph,
   not oldRecipe.flatIngredients(). Expose the smallest repository/DAO read of stored recipe
   edges needed for this check, within the mutation transaction. Guard recursive legacy
   reads from unbounded cyclic hydration and return unavailable/error state for malformed
   graphs. Search exclusions remain an aid; avoid changing the persisted recursive view
   unless a demonstrated compatibility requirement forces it.

The coordinator accepted decisions 1–3 during planning: mixed/unknown aggregate metric
basis must not block a serving fraction when ingredient nutrition resolves. This decision
adds no density feature and changes no stored yield type. The Story 26 worker was informed
of the intended helper responsibility seam; reconcile any response at implementation release.

## Save, cancel, and transaction contract

The complete draft (including name, yield, note, liquid flag and ingredient quantities)
feeds previews. Clearly label Recipe name, yield/reference servings, total Recipe nutrition,
and per-serving preview. Selected diary amount remains separate. Retain ingredient search
and existing edit/remove navigation; use minimal stable semantics only for actual-screen
acceptance where controls otherwise lack reliable selectors.

Explicit Save validates a captured draft once and commits metadata plus ingredient rows
as one operation. Keep create plus creation history in TransactionProvider; update existence,
reference reads, candidate validation, and the DAO replacement share its transaction.
No repository writes occur during draft editing, validation failure, or Cancel. Disable
repeat Save while saving. Catch expected persistence failures at the established boundary
and present shared-resource errors while retaining the editable draft and prior stored
Recipe; propagate coroutine CancellationException so Room rolls back. No error string may
contain private recipe data or credentials. Successful save alone navigates away.

For explicit unpack, build and validate all direct constituent snapshots/measurements
first. Scale original ingredient measurements by the resolved Recipe fraction; do not
flatten nested Recipes automatically. A nested Recipe remains one immediate constituent
unless the user explicitly unpacks that entry later. Add-screen unpack becomes a domain
use case with one TransactionProvider transaction for all inserts. Existing-entry unpack
uses the same calculation/validation and atomically deletes/replaces the original. Use
rollback or propagated failure if a later write fails; never return Err and accidentally
commit earlier writes. Preserve destination/date, captured facts and notes, and deliberate
createdAt behavior. Publish success/history events only after the whole operation commits.
Both paths expose recoverable errors and prevent duplicate submissions.

## Compatibility and likely affected files

Expected surface: food/domain Recipe/RecipeIngredient and create/update validation;
RecipeRepository and RoomRecipeRepository/RecipeDao for bounded graph reads;
common/domain calculation helpers only where shared with accepted Story 26;
app/ui/food/recipe forms/screens/view models/events; diary add/update RecipeModel,
snapshot capture, preview and explicit unpack paths; fooddiary domain Recipe snapshot,
portion creation/update and unpack use cases/DI; shared resource strings; focused tests.

No schema change is intended. Preserve Recipe, RecipeIngredient, DiaryRecipe,
DiaryRecipeIngredient, DiaryProduct, Measurement, legacy mealId mapping, IDs, discriminator
columns, quantities, measurement enum names, serving Int storage, annotations, and indices.
Keep existing serialized route class/key identities and productId/recipeId discriminators;
do not globally rename friendly Food labels or external/backup/CSV contracts. Existing
snapshots contain captured composition, not catalogue IDs, and must stay isolated from
live definition edits/deletion. No migration simply for Kotlin helpers or nullable computed
properties. Compare generated schema to the accepted Story 26 baseline; if storage must
change, stop that change and record a data-preserving migration and upgrade acceptance
before proceeding. Do not silently enlarge this plan into Story 24 storage/editor work.

## Sequential delivery and release gates

| Step | Deliverable and evidence | Prerequisite |
| --- | --- | --- |
| 1 | Re-read accepted Story 26 plan/receipt/source; reconcile helper, typed families and snapshot behavior; coordinator explicitly releases Story 27 | Story 26 accepted and reconciled |
| 2 | Focused failing/passing composition, yield, nullable-basis, nested-portion and candidate-cycle regressions; shared calculation integrated into definition/snapshot paths | Step 1 |
| 3 | Validated atomic create/update and recoverable editor/ingredient errors; complete-draft reactive previews; Save/Cancel regressions | Step 2 |
| 4 | One-entry Recipe logging and atomic explicit unpack integration; snapshot, Meal-boundary and real Room persistence regressions | Steps 2–3 |
| 5 | Synthetic actual-screen instrumentation workflow; independent read-only review of calculation, graph, snapshot and save/transaction boundaries; repair findings | Steps 2–4 |
| 6 | Check UTF-8/changed-line whitespace/schema, freeze source/resources/tests; request coordinator final build with current version configuration | Step 5 |
| 7 | Coordinator runs final frozen checks and emulator acceptance, prepares/seals/reconciles receipt and status/version metadata | Step 6, native/device lease |

No delivery estimate, model override, or external waiting-time estimate is asserted.
Implementation remains on hold at Step 1 until coordinator release.

## Required checks

- Composition math: synthetic ingredients total 300 g and 600 energy units, yield 3:
  one serving 100 g/200 units; two servings 200 g/400 units; Gram(200) and Serving(2)
  agree; Recipe total remains 600. Ingredient edit, removal, addition, and yield-only
  edits update total/per-serving previews coherently. Test fractional portions and whole
  Recipe package fractions, nested yields and same-number typed Product/Recipe IDs.
- Unknown/mixed cases: missing ingredient reference basis does not vanish from totals;
  aggregate mass versus volume remains unknown rather than summed; serving fractions
  still work when constituent facts resolve. All/partly missing nutrients remain unknown
  as appropriate, never fabricated 0. No NaN/infinity escapes preview, snapshot, or totals.
  Include liquid-compatible metric equivalents and explicit incompatible-metric errors.
- Boundary failures: blank name, empty composition, zero/negative/unparsable/overflow yield;
  zero/negative/NaN/infinite/overflow quantity; unsupported basis; missing/deleted ingredient;
  direct and indirect cycles; valid shared branches. Domain failures make no writes and UI
  keeps the draft recoverable. Test saving twice while in-flight and failure then correction.
- Save/cancel: change name/yield/note/liquid/ingredient amount, discard and reopen; original
  metadata/rows/ID remain exact. Inject failure after metadata or intermediate ingredient
  replacement and prove rollback through real Room, including cancellation. Create/history
  failure leaves no orphan Recipe/history. Ensure failed editor Save does not navigate.
- Logging/unpack: ordinary Recipe portion creates exactly one entry at requested date/slot;
  explicit unpack uses scaled original quantities and equal totals. Add-path second-insert
  failure leaves no constituents; replacement-path second-insert failure/cancellation leaves
  the original snapshot and no replacement rows. Invalid unpack fails before deleting.
  Nested Recipe remains a direct constituent. Test both definition and snapshot paths.
- Snapshot/Meal persistence: log a Recipe including nested composition, edit its yield,
  ingredients/name/note/liquid, and delete live definitions; old captured name, notes, facts,
  amount and totals remain exact across database reopen. New logs use changed definitions.
  Save a bundle containing that Recipe portion plus another Product/Quick Add entry; load
  to another date/slot and assert two entries with the Recipe total as one captured item.
  Preserve legacy Meal limitations; no invented original measurement or live Recipe link.
- Synthetic visible workflow, coordinator-only emulator-5580: create uniquely named Recipe,
  add two seeded Products through actual search/measurement screens, edit one ingredient,
  remove/re-add another, set yield 3, verify total/per-serving preview, Save, search/select,
  log two servings into a known synthetic date/slot, and verify one 2/3-total diary entry.
  Edit the definition/yield and confirm old diary remains unchanged; reopen draft, modify
  and Cancel then verify stored definition. Exercise invalid quantity/yield and recover.
  Deliberately unpack a separate synthetic entry and verify scaled constituent amounts.
  Add one nested/mixed-basis serving case and saved-Meal boundary assertion. Prefer targeted
  Compose instrumentation driving real screens with Room; no physical phone/private data.
- Run focused commonTest math/use-case tests and real Room instrumentation tests, plus
  existing Story 25 slot/Meal compatibility, Story 26 Product portion/identity/persistence,
  and Quick Add promotion/conversion regressions affected by shared calculation changes.
- Final coordinator-timed JDK 21 Windows command: .\gradlew.bat --no-daemon
  :app:testDebugUnitTest :app:assembleDebug :app:assembleDebugAndroidTest. No global clean.
  Preserve app/build/m2-domain-meals/baseline and predecessor accepted APK/receipts. Compare
  schema and reopen accepted baseline synthetic data in place; if schema changes, run a
  mandatory preserved-baseline upgrade fixture before acceptance. Root invokes all ADB.

No installed harness modules exist per harness/README-HARNESS.md. Required unit, Room,
and actual-screen runtime results remain acceptance gates; inability to run is incomplete,
not a passing claim. Keep detailed temporary evidence under coordinator-owned app/build
paths and record sanitized results here. Do not introduce third-party screenshots/UI dumps
or reference extracts anywhere in the repository, including ignored directories.

## Recovery and execution evidence

- 2026-10-25 planning: read AGENTS, laws, harness, agentic workflow, Design domain and
  architecture vocabulary, full Story 27, serial briefing, predecessor Story 26 plan,
  rails-planning skill/template, and relevant Recipe/Room/diary/Meal source above.
- Wrote this plan only. No application/resource/test edits, build/test/device execution,
  shell approval, tracker/catalogue/Design update, commit, or runtime claim. A shell read
  was unavailable; continued read-only inspection via Node filesystem APIs without escalation.
- Root accepted serving-fraction computation independent of aggregate metric basis and
  retention of existing positive Int yield. Communicated proposed helper responsibility
  seam to Story 26 worker; source contract remains subject to its accepted final handoff.
- Story 26 worker confirmed the proposed seam fits its accepted boundaries: MeasurementResolver
  is metric-only; Product normalization stays 100 / metricAmount; Recipe legacy getters are
  unchanged by Story 26. Recipe-only fraction handling integrates at diary create/update
  metricAmount plus canonical-facts validation after release, preserving Product checks.
- Implementation recovery: preserve predecessor baseline/receipts and unrelated edits.
  Revert only owned Story 27 changes if necessary; never global reset/clean. Transaction
  tests bind rollback claims; do not rely on an Err return to imply rollback.
- Next action: remain ON HOLD. After accepted/reconciled Story 26 and explicit coordinator
  release, read its final handoff, reconcile this plan to the accepted code, then begin Step 2.

### Released implementation and preservation decision (2026-10-25)

Story 26 was accepted/sealed/reconciled VERIFIED_UNCOMMITTED, diff identity
df7dc734198f2b21e7358dec64cae8a5c8307445cdbdae0d5b232c30d3c1e276. Coordinator
adopted the Recipe calculation contract and released implementation; candidate
3.2.35.3/code131 is coordinator-owned. Exact predecessor bytes/hashes for 891 scoped
source/resource files were captured under ignored app/build/m2-domain-meals/story27/before
and predecessor.json before edits. No cumulative HEAD diff will stand in for this boundary.

Inspection proved a necessary persistence distinction: legacy RecipeSnapshot sums 100 g
at 100 energy/100 plus 100 mL at 200 energy/100 into a numeric 200 basis; Gram(100)
displays 150 energy. A 10 protein contributor plus unknown contributor displays legacy
Incomplete(10). Strict mixed-basis/unknown-preserving math changes both historical results.
The same stored composition/measurement cannot distinguish old and new contracts.

Coordinator explicitly authorized the smallest preserving schema boundary before storage
edits: add internal DiaryRecipe calculation contract/version, SQL default 0 for all existing
schema39 rows, current value1 for fresh captures. Copy/read/unpack preserve each snapshot's
contract, including nested snapshots; never upgrade historical calculations implicitly.
Legacy0 retains old known mixedmetric and partialnutrient results; current1 uses this plan's
strict total-first semantics. Add focused 39→40 migration and generated40 schema only;
39 bytes and original canonical fixture/baseline remain unchanged. This authorization
supersedes this plan's earlier no-schema assumption only at this snapshot boundary.
Inspect external mappings before editing; no relay/CSV/wire contract change is authorized.
Required proof includes150-energy mixed Gram legacy,10-protein partial legacy,new mixed
Serving(2)/yield3,real Room39→40/reopen/copy and source-edit/delete isolation. Coordinator
will run version-aware compatibility separately from untouched raw schema39 fixture.
Story24 remains held and will rebase its later metadata migration onto40.

### Initial implementation freeze

Implemented total-first Recipe/snapshot calculation, nullable compatible metric basis,
recipe-aware shared nutritionFor/supportedMeasurementTypes methods, candidate raw-edge
cycle validation and guarded Room hydration, validated transactional definition saves,
complete-draft reactive preview, recoverable Save errors, and atomic direct-constituent
unpack. Product historical scalar reads remain unchanged; unavailable snapshot bases
are nullable. Internal DiaryRecipe version0/1 and focused39→40migration preserve legacy
readmath; no external serialized/wire/CSV RecipeSnapshot mappings were found.

Added RecipePortionTest (6 focused math/graph tests), RecipePersistenceTest (2 real Room
migration/transaction workflows), and RecipeWorkflowTest (2 actual-screen workflows).
Predecessor Product instrumentation tests only adapt nullable weight assertions and
AddEntryViewModel's unpack dependency. No device commands or runtime checks performed.

Initial source freeze identity1c55fa1103200549c136eedd62f4aac02bf277b29d914932bd5de15cc95858c1
covers54 source/resource paths relative to exact predecessor bytes, without deletions or
renames. UTF-8 and introduced-line whitespace checks passed. Requested independent
consequential review and coordinated three-task Gradle lease; build/runtime pending.

### First compile and demonstrated legacy graph hazard

First frozen build compiled production but failed test compilation: coroutine-test is not
a dependency, and three Quick Add assertions assumed non-null Recipe weights. Fixed the
unit coroutine wrapper to existing runBlocking, retained explicit known-weight assertions,
and strengthened current Recipe promotion unknown assertions from Incomplete(0) to
Incomplete(null). No production compiler error occurred. Four now-redundant nullable
preview conditions were removed; canonical40 projection verifier and removable unavailable
ingredient state were added after the build exited, never during compilation.

A bounded host SQLite probe loaded the exact observeFoodCount DAO query and preserved
schema39 view: Product1 as an ingredient of Recipe4 wrongly excludes Recipe4 while editing
Recipe1 (2 results instead of3). Legacy cycle Recipe2→3→2 does not terminate; a SQLite
progress handler interrupted after1000 callbacks of100 instructions. No device or private
data was involved; script/evidence are under ignored story27/graph-probe.py.

Coordinator conditionally authorized a demonstrated view correction in the same39→40
boundary. Use the same RecipeAllIngredientsView name/columns but traverse Recipe edges
only, UNION distinct without depth, so overlap stays typed and cycles terminate. Recreate
that view in the migration; preserve old39 schema and fixtures. Add real Room DAO and
migration assertions. This is a focused exclusion fix, not a search feature or tuning knob.

### Passing deterministic pass and final visible integration

Attempt02 passed all three Gradle tasks in2m31s:165 unit tests in34 suites, no
failures/errors/skips; debug and Android-test APK assembled. No Android test has been
executed by this worker. Passing source identitya1e4c176933d75f25e9e89ea8a2f5b816da937c6fec683fedc1d8b1497e869f9
is retained in attempt02-source-manifest.json alongside attempt02-verification.log.
Schema39 remains SHA256666ff1773c463a92bb8c1d5512b96760d6ce3a7e0b08a2a441a1265d97ed29e3;
generated40 includes the one new DiaryRecipe column and verified ancestry view.

Read-only display audit found home DiarySlotFoodListItem still classified valid mixed
Recipe servings as measurement errors when no metric suffix exists. After the build
exited, added exact raw serving display for Recipe rows (Product behavior retained),
and extended the mixed actual-screen fixture to render/assert the real diary row.
Current review freeze71ada91ce8ac2c28ab7e9c86cbf482871e1276e59a43925f680e4dc5dc8db36d
covers59 paths against accepted predecessor; UTF-8/introduced-line whitespace clean.
Independent selected review and final frozen three-task rerun remain pending.
No runtime, canonical upgrade, terminal metadata, or commit claim is made.

### Final self-review corrections before independent review

A compatible known aggregate basis can overflow even when all nutrient fields are unknown.
Added an explicit invalid-aggregate check shared by Recipe/current snapshots, enforced at
create/update and portion boundaries, with pure regression. Genuine mixed/unknown bases
remain supported for serving fractions. Added real Room failure after creation-history
insert (proving Recipe/history rollback) and successful existing-entry unpack after reopen
and live-definition deletion, complementing append/failure/cancellation checks.

Final review identity8f141004b1bdc35b9147ca02ffbec9823a4fe5f95bdf14a62acc168eccfdac3b
supersedes71ada91c and covers59 paths. UTF-8 and introduced-line whitespace passed; old
schema39 identity unchanged. This additional source has not yet been compiled; independent
review and final frozen three-task verification remain required before handoff.

### Final verified worker handoff

Selected independent review passed the initial boundary and exact incremental8f141004
boundary without must-fix findings. Final frozen JDK21 three-task build passed in2m9s:
165 unit tests,34 suites,0 failures/errors/skips; debug and instrumentation APK built.
Full output is ignored story27/final-verification.log. No postbuild source changes; final
source identity remains8f141004b1bdc35b9147ca02ffbec9823a4fe5f95bdf14a62acc168eccfdac3b
(59 paths, exact accepted Story26 before bytes, no renames/deletions). UTF-8 and
introduced-line whitespace checks clear. All43 nutrient fields participate in strict
Recipe aggregation. Schema39 remains unchanged; schema40 SHA256
6392aeff08f3291136480faa479eda13729a726d4779a4f6006f16dd9b0bd31b.

Production app/build/outputs/apk/debug/foodus-dev-3_2_35_3.apk is package
io.github.jarrydadaens.foodus.dev,3.2.35.3-dev/code131, SHA256
17f66afc914382e6602d669b8e1aca3f5a6f89c8e0a5cad1487497fb1d4b61ba.
Test APK app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk SHA256
f6f8374f607bdff8a6f738f8ba3e0dd4695471fb40147d0bb3dcf75f62b908e3.

Exact setup, five new isolated Room/screen method invocations, separate canonical40
projection/migration trigger, preserved evidence paths and package/runner identities are
in ignored app/build/m2-domain-meals/story27/handoff.json. Coordinator alone runs
emulator5580 acceptance and existing Product/Quick Add/slot/Meal regressions. No runtime
method has been run by this worker, so Story acceptance remains incomplete pending those
results. No further source change, native approval, device command, tracker/catalogue
change or commit is pending from this worker.

### Runtime attempt01: exact Room view SQL correction

Coordinator installed131 in place and launchedMainActivity; canonical projection found39.
Startup diagnostics confirmed Room rejected the recreated view SQL: generated40 expects
a backtick-quoted view name and outer-trimmed SQL, while migration used an unquoted
name and raw leading whitespace. The transaction rolled back; preserved canonical39
backup and original beforeJSON remain intact. No later runtime method ran.

Corrected migration CREATE to quote the identifier and apply trim() to the shared SQL.
Full551-character CREATE string exactly equals exported40 createSql after substituting
the view name. Offline proof shows trimIndent() would NOT match because it removes
interior indentation. No schema40semantic or byte change. Strengthened existing real
Room migration test to compare sqlite_master.sql against the independent bundled40
schema JSON's full CREATE string. Failure evidence is retained, not recorded as pass.

The only source changes from reviewed8f are migration and RecipePersistenceTest; exact
two-path correction hashes/proof are migration-fix-manifest.json and migration-sql-proof.json
under ignored story27. Targeted review and rebuilt APK/runtime retry are required.

### Corrected migration verified worker handoff

Independent targeted review passed exact daab874161c664c554f4545c6102a2d4c1e305d2d7a6aee2db3b11f2860b0d6b
with all59 paths verified and no remaining source findings. Frozen JDK21 three-task
rebuild passed in1m52s:165 unit tests/34 suites,0 failures/errors/skips, both APKs built.
Postbuild manifest remains the exact reviewed identity; UTF-8 and introduced whitespace
checks clear. Schema39 and40 hashes remain unchanged. Log:migration-fix-verification.log.

Renewed production131 APK SHA256275c13ea6b7a90adfaf69d9d8552dbeae2474d710cfee2138b7387f2552ce3a4.
Renewed test APK SHA256b84bf7da89114da8310121b85d59476a31c8ab181bd516b07007903224809b80.
Updated ignored story27/handoff.json contains corrected hashes and unchanged exact runtime
invocations/setup. Previous handoff and actual failed runtime evidence remain retained.
Coordinator must rerun canonical39→40 projection and all selected runtime regressions;
no post-correction Android method has yet run. No source edits, native approvals, device
commands, terminal metadata or commits are pending from this worker.

### Runtime attempt02: ingredient Save accessibility correction prepared

Coordinator reports canonical39→40 projection and all3RecipePersistence methods pass
against275c13/b84bf7 artifacts. Both actual Recipe screen methods stop at the first
ingredient Save lookup. Conversion resolves (nutrition preview rendered); no fixture
crash is recorded. Remaining predecessor regressions are running; native builds remain held.

Read-only inspection of the exact installed Material3 binary confirms the large extended
FAB delegates to the helper that clears its text semantics; current official AndroidX
source corroborates that behavior. Its default expanded state is true. Ingredient Save
icon had no content description, while the working diary Save supplies localized Save.
No dependency source/binary extract was copied into the repository.

Prepared the smallest correction: localized Save description on the ingredient action
icon, retaining all amount validation. The actual-screen test now asserts invalid amount
has no active Save, corrects it, resolves the real clickable Save ancestor, asserts enabled
and click action, and submits through that control. All draft/persistence/nutrition/cancel
assertions remain; no timeout or numeric assertion is weakened. Two-path delta recorded
in save-action-fix-manifest.json; current review identity
c4363fdb83c06118f0822880e8a171d6670866dd57592fdb354a4a59af1e757c.
UTF-8/introduced-line whitespace clear; schemas unchanged. Runtime APK outputs retain
exact275c13/b84bf7 hashes while the coordinator batch runs. No build/approval/device
action performed. Targeted review and released rebuild required before retry.

Coordinator finished attempt02:13/15 selected runtime methods passed on reviewed daab8741
source and production275c13ea6b7a90adfaf69d9d8552dbeae2474d710cfee2138b7387f2552ce3a4 /
testb84bf7da89114da8310121b85d59476a31c8ab181bd516b07007903224809b80 APKs.
Only the two Recipe workflows failed on the missing accessible action. Final canonical
projection remains exact. Coordinator authorized retaining the13 unaffected passes with
this explicit provenance and rerunning both affected workflows plus canonical check after
review/rebuild of the two-file accessibility correction.

Bounded follow-through audit: Create/UpdateRecipe parent Save uses a FilledIconButton
with localized Save description; DiaryAdd Save also already has its label. The same
workflow's explicit Unpack ExtendedFAB had the identical null icon description. Coordinator
authorized adding existing localized action_unpack there before retry. No wider sweep.
Current three-file correction freeze714412a671d262ac2d57b45ab886b671e6708f5cd6c8ef64e8f85dc1de999bc7 replacesc436; root owns quick incremental
review and combined rebuild/two-workflow/canonical retry. No worker build request or device
operation; installed/built275c13/b84bf7 artifacts remain unchanged.

### Accessibility retry: bounded stage diagnostics prepared

Root rebuilt714412a6 successfully in1m43s. First Recipe workflow exceeded the unchanged
outer240s bound without a runner result; root stopped only the synthetic dev app.
This is an incomplete check, not a passing workflow or evidence to raise the bound.
Outputs remain production507d069ea025d9da7fbac967b997ce279fe1e993a805ec0b087a18e083fd4a60 /
test98011be95fc148e7d5599c313eec49b250af26017b8ba2522ac00b791e129899.

Prepared a test-only diagnostic delta: RecipeWorkflowTest now detects accessibility
ancestor cycles and caps parent depth32, caps its local subtree search4096 nodes, and
bounds recipe()/entries() cold Room first reads at15s. Existing action/outcome/nutrition/
persistence assertions remain. No production behavior or timeout is weakened.

Static BEGIN/END/FAIL stage markers surround ingredient actions, amount writes/readback,
main-idle waits, database reads, activity closure, Koin closure and Room closure. Markers
contain static IDs/counters/time/error class only, no user values, names or diary contents.
A96-event ring and latest stage are written to synthetic test cache
cache/story27-workflow-progress.json; matching FoodUsRecipeWorkflow logcat messages and
instrumentation stream make live capture possible. No native/device action performed.

Current frozen identity18858161e7c53262292075d35e7a9c74093bb3bb456ad2b2aa188c6ed908b65b
changes only the test from714412a6; delta in stage-diagnostics-manifest.json.
UTF-8/introduced-line whitespace clear; production/domain/migration/schemas untouched.
Root owns quick review, test-APK compilation and live bounded retry/thread capture.

Focused diagnostic review corrected a false-absence risk: local AX search now collects
up to4097 nodes and explicitly fails if more than4096 exist, before searching for Save.
Exhaustion can never count as absence. Only RecipeWorkflowTest changed; all existing
assertions/markers/guards/read bounds remain. Refreshed freeze7787b6350a1a507418dab67be1058427a27310931419969088731eb9ca01290d;
no production/schema/build/device/approval changes. Root owns compilation/live retry.

### Staged first workflow: current Search result selection correction

Coordinator's staged first workflow completed177.292s with a concrete selector assertion,
not a Room/main-idle hang. It passed composition edits, invalid→valid ingredients, Save
failure/retry, ordinary portion logging, definition yield edit, Cancel, and prior-snapshot
assertions. It failed on the second Search selection before explicit Unpack.

Fixture inspection proves duplicate labels: after logging, hub renders a nonclickable
real diary row carrying the Recipe name; SearchBar's editable initial query also equals
that name. The old helper clicked Search and immediately globally selected matching
text without waiting for the new context/result. First search had no existing hub entry.

Test-only correction gives the actual FoodSearchApp fixture a static context marker,
waits its current editable query, excludes editable ancestors, finds a visible enabled
clickable result WITHIN that context, asserts ACTION_CLICK and uses it. Fresh callback
count and typed Recipe ID are asserted after the actual portion screen appears. All
existing quantities/totals/draft/Cancel/snapshot/unpack assertions remain. Tree exhaustion,
ancestor guards,15s reads and the unchanged outer240s bound remain enforced.

Only RecipeWorkflowTest changed; current freeze
5b8242ff08d4fef117263f3a8ade09b6808c25847c75c73a1ff9be87713c292a; delta recorded in
search-selector-fix-manifest.json. No production/schema/build/device/approval changes.
Root owns targeted review, test compilation and both-workflow retry.

Coordinator approved the scoped selector. Fresh callback count and typed identity now
use AtomicInteger/AtomicReference for explicit cross-thread observation; UI navigation
continues through its existing main-thread selected fields. Refreshed review freeze
6cbbe81b6db32b7321a20d84c3eb2786bf76696e3c4a7dbf5d5b762821247d53 supersedes5b8242ff; still a single test-file delta only.
