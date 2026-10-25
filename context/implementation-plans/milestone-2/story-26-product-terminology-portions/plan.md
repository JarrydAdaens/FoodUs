---
name: story-26-product-terminology-portions
description: Reconcile Product terminology, typed reusable nutritional items, and portioned diary entry.
metadata:
  version: "1.0"
  owner: "Jarryd Adaens"
  repo: "FoodUs (fork of maksimowiczm/FoodYou)"
---
# Plan: Product terminology and portioned entry

Execution state: reviewed test-helper fix frozen; coordinator rebuild and two Product workflow reruns pending.
Authority: [Milestone 2 Story 26](../../../milestones/milestone-2.md#story-26), accepted
[Design vocabulary](../../../design.md#domain-model-shared-vocabulary), and
[sequential batch briefing](../../../_boss-runs/milestone-2-domain-meals-sequential/briefing.md).
The milestone owns Story status; this plan owns approach, acceptance evidence, and continuation.
Commit policy: none. No pull, push, tag, release, or physical-device operation.

## Outcome and bounds

Make the existing Product create/edit/select/log flow obey a clear reference-serving and
actual-portion contract while retaining friendly Food wording in the UI. Reconcile the
shared Product-or-Recipe APIs without treating a Recipe as an atomic Product. Preserve
existing IDs, names, nutrition, brand/barcode/note, source, liquid status, and optional
package/reference-serving metadata. Canonical nutrition remains per 100 g or 100 mL.

This lease initially permits reading and writing this plan only. Implementation waits
for Story 25 acceptance/reconciliation, coordinator adoption of the naming decision in
Design, and explicit release of the implementation/native-build hold. The coordinator
owns Design, milestone/catalogue metadata, version changes, receipts, independent review,
and emulator operation. The worker later owns relevant app/src/** and shared/resources/**
code/tests plus this plan. Preserve all unrelated edits and the frozen Story 25 baseline.

Story 27 owns Recipe composition/yield/save semantics; Story 24 owns the Meals library
and editor. Story 26 may mechanically reconcile their shared types and call sites, but
must not implement their deferred behavior. No combined catalogue destination, new unit
catalogue, import feature, photo flow, relay work, library upgrade, or branding rewrite.

## Settled naming and responsibilities

The selected shared definition name is **NutritionalItem**, with **NutritionalItemId**.
It is the sealed Product-or-Recipe reusable definition used for lookup, ingredient
selection, portion selection, and capture into a diary snapshot. Its responsibilities
are typed identity, display headline, canonical nutrition, liquid status, optional known
total/reference-serving metric basis, and resolving a supported measurement against that
basis. Product defines nutrition directly; Recipe derives it from composition. This
shared interface is not a Meal bundle, DiarySlot, dated entry, Quick Add total, or a
new fourth entity. It does not own persistence, UI navigation, or editing transactions.

Keep the distinct nested identity variants NutritionalItemId.Product and
NutritionalItemId.Recipe. Product(7) and Recipe(7) remain unequal, route to separate
repositories, and retain separate storage discriminators. Do not collapse IDs to Long
at polymorphic boundaries. Product-only APIs keep Product names and the Product variant.

| Current role | Selected code family / responsibility |
| --- | --- |
| Food / FoodId | NutritionalItem / NutritionalItemId; sealed Product and Recipe variants |
| ObserveFoodUseCase / DeleteFoodUseCase | ObserveNutritionalItemUseCase / DeleteNutritionalItemUseCase; dispatch by typed identity |
| FoodHistory and FoodHistoryRepository | NutritionalItemHistory and NutritionalItemHistoryRepository; definition provenance/history, retaining historical ImportedFromFoodYou2 name |
| FoodMeasurementSuggestionRepository | NutritionalItemMeasurementSuggestionRepository; suggestions indexed by typed definition identity |
| Polymorphic search results/repository/use case | NutritionalItemSearch family; Product and Recipe projections remain distinct |
| DiaryFood | NutritionalItemSnapshot; immutable captured Product-or-Recipe definition without live catalogue IDs |
| DiaryFoodProduct / DiaryFoodRecipe | ProductSnapshot / RecipeSnapshot; canonical facts/basis or captured composition respectively |
| DiaryFoodRecipeIngredient | RecipeIngredientSnapshot; captured item plus ingredient measurement |
| FoodDiaryEntry and ID/repository/use cases/events | NutritionalItemDiaryEntry family; dated portion of a NutritionalItemSnapshot, distinct from generic DiaryEntry and ManualDiaryEntry |
| FoodSource and matching internal adapters | ProductSource family; atomic Product provenance metadata, also copied into ProductSnapshot |
| RemoteFoodException | RemoteProductException if its actual consumers remain remote Product-only |

Update related domain properties, adapters, DI, view-model state, route constructor
arguments, and tests using the actual role. A snapshot property can be named snapshot;
a live shared value can be item; Product-only values remain product. The snapshot family
is distinct from the reusable item precisely to preserve historical behavior.

UI component names that express friendly Food wording are not an indiscriminate rename
scope. Feature/package directories, upstream com.maksimowiczm.foodyou namespaces,
FoodUs/FoodYou branding, OpenFoodFacts, USDA/AFCD source names, historical migration
classes/paths, and external data vocabulary retain their established identities.

## Inspected boundaries and concrete issues

- food/domain/entity/Food.kt currently combines Product and Recipe, with typed FoodId
  variants, headline, nutrition, optional total/serving basis, and weight resolution.
  Product already exists with direct per-100 nutrition and package/serving metadata.
- common/domain/food/WeightCalculator.kt multiplies servings/packages and converts the
  immutable units, but does not validate quantity, known bases, finite results, or
  mass-versus-volume compatibility. Measurement stores doubles and permits fractions.
- CreateProductUseCase and UpdateProductUseCase validate name only. ProductFormState
  and shared float validators use comparison checks which admit NaN/infinity. The
  portion picker has positive-value UI validation but no domain-level finite check.
- CreateFoodDiaryEntryUseCase and UpdateFoodDiaryEntryUseCase reject wrong liquid units
  and absent serving/package bases, but do not reject zero, negative, nonfinite, or
  overflowing amounts. Their UI callers can currently throw on failed saves.
- Product create/update view models duplicate basis normalization: canonical input
  uses multiplier 1; serving/package input uses 100 divided by the known reference
  basis. The Product editor offers 100 g/mL, one serving, or one package. Basis and
  reference metadata need explicit wording; changing what is logged is a separate form.
- Selection and measurement-suggestion code duplicate supported-unit rules; unknown
  metadata is currently nullable. Preserve null and filter unusable suggestions.
- Diary snapshots already live in DiaryProduct/DiaryRecipe tables, separate from live
  Product/Recipe rows. Nutritional entry totals are snapshot facts times metric portion
  divided by 100. Definition edits must continue to affect future selection only.
- Recipe currently has unknown-weight/yield hazards (mapNotNull/sums and division).
  Those composition changes belong to Story 27; record discovered coupling rather than
  silently broadening Story 26 or manufacturing a weight for a Recipe.

## Portion and validation contract

A serving is a reference quantity; a portion is the actual positive finite measurement.
For a Product with serving basis 40 g, Serving(2) resolves to 80 g; its nutrients equal
Gram(80). Serving(0.5) resolves to 20 g. Liquid reference bases are mL and must agree
with direct mL/fluid-ounce portions. Package fractions use the known package basis.
No density model is selected: grams/ounces cannot be converted to liquid mL/fluid ounces
or vice versa. Unit mismatch fails explicitly. Missing serving/package bases remain
unknown and cannot support those portion types; a direct supported metric portion still
works. A known reference basis must be positive and finite. A calculated portion and
normalization factor must also be finite and positive; overflow fails without writes.

Keep the simple existing Measurement representation. Prefer a small shared domain
validation/resolution seam used by Product, snapshots, suggestions, and create/update
boundaries over another Portion entity or broad architectural framework. Preserve existing
nullable read semantics for legacy metadata, while rejecting invalid new user writes.
Do not put unconditional throwing constructor checks on models loaded from old data.
Use a typed existing Result/error path at save boundaries, with shared-resource messages
and a visible, recoverable failure; failed save and Cancel preserve prior records.

Product editor normalization happens once: nutrition entered for a known reference basis
is converted to per-100 canonical facts before persistence. Reopening starts from the
canonical basis. Reference-serving metadata is not a multiplier applied to already
canonical facts. Keep manual/Quick Add absolute totals and optional serving/weight metadata
outside this scaling path; preserve promotion energy/unknown-nutrient behavior and tests.

## Compatibility and likely files

Expected source surface is food/domain, food/search, their infrastructure/DI, affected
common/domain/food and measurement helpers, fooddiary domain/repositories, Product
forms/view models, shared selection/search models, navigation, provider/import Product
adapters, and their tests. Recipe call sites receive typed-name reconciliation only.
Shared resource labels distinguish Nutrition basis, Reference serving, Package amount,
and Amount to log while retaining Food for the user's atomic item.

No schema change is intended. Keep explicit legacy Room table/column/index names and
SQL aliases, including FoodEvent, MeasurementSuggestion, DiaryProduct, DiaryRecipe,
Measurement, productId/recipeId and Story 25's mealId mapping. Storage DTO names can
remain contract names when exact annotations already make their role clear; if a Kotlin
entity/property rename affects inferred schema identifiers, add explicit mappings before
building. Preserve enum string values (including ProductSource Type branding), CSV
headers, backup records, and external/provider payloads. Keep route serial names/keys
explicit if a private @Serializable class/property is renamed. Route identity continues
to carry separate productId and recipeId; reject ambiguous discriminators rather than
coercing a Recipe ID to Product. Existing historical migration fixtures remain historical.

Check schema output against the frozen predecessor. If storage actually changes, stop
and specify a data-preserving migration plus baseline-upgrade acceptance before proceeding;
do not claim rename-only safety from compilation alone.

## Sequential delivery steps

| Step | Change and evidence | Prerequisite |
| --- | --- | --- |
| 1 | Coordinator adopts NutritionalItem, snapshot and dated-entry families in Design and releases hold | Story 25 acceptance/reconciliation |
| 2 | Reconcile typed definition/snapshot/search/provenance/API families and compatibility mappings; add identity/route regressions | Step 1 |
| 3 | Add focused domain resolution/validation and Product normalization regressions; integrate create/update/select/log failures and shared labels | Step 2 |
| 4 | Add repository persistence/snapshot tests and a synthetic visible Product workflow instrumentation test | Steps 2–3 |
| 5 | Independent read-only review of final data/identity/editor boundaries; fix findings, check UTF-8 and changed-line whitespace, freeze sources | Steps 2–4 |
| 6 | Coordinator stages candidate 3.2.34.3 / code 130; run one final frozen unit/build/test-APK pass | Step 5, native-build lease |
| 7 | Coordinator executes isolated-emulator visible workflow and persistence compatibility acceptance, prepares/seals receipt and reconciles status | Step 6 |

Complexity is moderate to high because the identifier and snapshot families cross many
adapters; effort is dominated by coherent call-site reconciliation and meaningful runtime
proof. Main consequences are identity collisions, silent serving scaling, and historical
snapshot mutation. No elapsed delivery estimate or speculative performance work is selected.

## Required checks and acceptance evidence

1. Unit conversion/normalization: 40 g times two equals 80 g; liquid mL equivalents;
   gram/ounce and mL/fluid-ounce supported conversions; positive fractional servings and
   packages; unknown bases; wrong dimensions; zero/negative/NaN/infinity; overflowing
   conversion and normalization. Assert nutrition totals as well as metric amounts,
   with deliberate floating-point tolerance. Unknown nutrients remain unknown.
2. Domain save boundary tests: create/update Product invalid basis and nutrition inputs
   produce typed errors without repository writes; valid metadata round-trips. Create
   and edit nutritional diary entries reject invalid/unresolvable amounts without writes.
   Cancellation and failed Product editing preserve the saved definition.
3. Typed-identity/dispatch: same-number Product and Recipe IDs stay unequal, resolve to
   the correct repository, search projection, suggestion/history records and navigation
   round-trip. Exercise persisted productId/recipeId discriminators, not equality alone.
4. Persistence/snapshot: real Room Product create/edit/read retains metadata/source/IDs;
   log a known portion, edit the definition, and prove previous entry name, facts, basis,
   notes and totals remain captured while a later entry uses the changed definition.
   Existing Quick Add absolute totals remain unchanged by descriptive servings/weight;
   run existing promotion/conversion regressions with reconciled names.
5. Synthetic visible workflow on coordinator-owned emulator-5580: create a uniquely
   named Product with 40 g reference serving; save/edit metadata and nutrition; search
   and select it; choose two servings; verify the 80 g nutrition preview; log to a known
   synthetic date/DiarySlot; edit the saved Product again and verify diary snapshot stays
   unchanged. Reopen editing, change values and Cancel, and submit an invalid/unsupported
   amount to prove recovery and no new row. Include a liquid/fraction case. Prefer one
   targeted Compose instrumentation workflow using actual screens and Room, with minimal
   semantic tags only where existing controls lack stable selectors. No private data.
6. Source/schema compatibility: exact legacy names/keys and generated schema comparison;
   existing-data reopen on the preserved app database. If schema changes, mandatory
   in-place baseline upgrade fixture on the isolated emulator, preserving the baseline APK
   and schema 39 data. Coordinator chooses/executes the device invocation.
7. Final affected checks, JDK 21 on Windows: .\gradlew.bat --no-daemon :app:testDebugUnitTest
   :app:assembleDebug :app:assembleDebugAndroidTest. Freeze all source/resource edits before
   this command and retain source identity through review/runtime acceptance. No global
   clean. app/build/m2-domain-meals/baseline is retained. Root owns native-build timing.

No harness modules are installed per harness/README-HARNESS.md. Meaningful unit,
Room/instrumentation and direct visible runtime evidence replace no acceptance condition;
a missing required check remains incomplete. Keep logs synthetic and sanitized. Review
UTF-8 explicitly with Node fs utf8 writes; avoid PowerShell text round-trip encoding loss.
Record check results here and retain detailed ephemeral output under app/build only.

## Recovery, evidence, and continuation

- 2026-10-25: read AGENTS, laws, harness, workflow, Design vocabulary, full Story 26,
  sequential briefing, planning skill/template, and source boundaries above. Selected
  NutritionalItem/NutritionalItemId and explicit snapshot/dated-entry families. Wrote
  this plan before any code edit. No app/resource/test changes or checks performed.
- Planning lease uses UTF-8 filesystem reads/writes only; no shell approval, build,
  emulator command, tracker/catalogue/Design edit, or commit.
- Recover from this plan and the coordinator's frozen Story 25 handoff. Preserve unrelated
  source changes. Revert only the worker-owned Story 26 slice if needed; never use a global
  reset/clean or delete the existing baseline. No migration rollback is needed for the
  intended schema-preserving rename; revisit this if implementation changes persistence.
- Next action: coordinator reviews/adopts the naming family in Design and explicitly
  releases the implementation hold after Story 25 acceptance and reconciliation.

### Implementation checkpoint (2026-10-25)

Coordinator released Story 26 after the accepted Story 25 receipt and adopted the naming
family in Design. Candidate 3.2.34.3/code130 is coordinator-owned. Captured all 886 scoped
predecessor files and hashes under ignored app/build/m2-domain-meals/story26/before, with
predecessor.json. Exact changed paths, additions/deletions and before/after SHA256 values
are in source-manifest.json; filename moves are recorded separately in file-renames.json.

Automatic approval review rejected an initial all-source rename operation as overly broad
and then a scripted block-replacement/truncation operation as unsafe. Neither rejected
operation wrote source. The safer route used a reviewed 146-path role-token preview,
separate exact domain/adapter/call-site edit groups, reversible checked filename moves,
and targeted apply_patch changes. Coordinator accepted that exact semantic scope.

Implemented role names, focused conversion/normalization validation, Product create/update
write validation, diary create/update validation, supported-unit selection and suggestion
filtering, explicit UI failures, reference basis/actual amount labels, and an accessible
amount input. Historical snapshot model weight getters retain their previous semantics;
new-write and preview validation must not mutate or crash historical reads. No schema,
CSV, wire, legacy SQL/table/column/enum value or serialized route-name change is intended.

Added ProductPortionTest (eight pure regressions); ProductPortionPersistenceTest, using a
unique isolated on-disk database; ProductWorkflowTest, using actual create/update/search/
portion/log/edit screens, isolated Koin and in-memory Room. Its two methods cover the
known 40 g/two-serving workflow, definition edit vs snapshot, invalid input, editor/portion
cancel, logged-portion edit, liquid fractions and unsupported unknown-serving recovery.

Precompile freeze: 197 source/resource paths differ from the captured predecessor,
including 38 old/new filename pairs. Introduced-line whitespace and UTF-8 replacement
character checks passed. All code/resources freeze before compilation; any compile fix
requires a refreshed manifest and fresh affected check. Required checks remain pending.

### Initial checks and final refinements

The first frozen three-task build passed: 157 unit tests in 33 suites, zero failures,
errors or skips; debug and Android-test APKs compiled/assembled. No Android test was
executed by that Gradle command. verification.log retains the complete output. Baseline
SDK XML and experimental compiler/deprecation warnings remain; no Story26 compiler
errors occurred. Coordinator confirms legacy schema39 identity is unchanged.

Read-only audit identified the existing editor basis-switch risk: changing 100 g to a
known 40 gServing previously reinterpreted unchanged canonical values as per-serving. The
Product editor now converts displayed values when switching known bases, preserving
canonical nutrition; an unavailable basis leaves both values and selection unchanged
and displays a shared-resource explanation. Reopening still starts canonical. An explicit
liquid-status edit relabels the canonical unit without pretending to infer density.

Independent review caught an omitted B12 field in the in-progress basis conversion list;
added B12 and expanded the actual-screen fixture to exercise all 43 nutrient fields, with
three deliberately unknown values. Every canonical nutrient and unknown is asserted after
reference-basis conversion/save and a metadata-only edit. Pure regressions cover forward,
reverse and fractional basis conversions plus numeric-form finite validation. Static
coverage check confirms all 43 fields serialized by nutritionFacts participate in basis
switching. Invalid-amount feedback is now explicit shared-resource text. New diary writes
also reject normalization underflow rather than writing zero totals from a positive
portion. Historical snapshot getters remain unchanged. Final frozen checks are pending.

### Final worker handoff

Final source/resources identity: 08e4f6a3b5973600e8fbf4038ffebc3de801ba30bb6bcf4babc0e2ca595eebf8.
Final three-task build passed in 2m 29s: 159 unit tests, 33 suites, 0 failures/errors/skips, debug APK
and Android-test APK assembled. Full output: ignored story26/final-verification.log.
Postbuild source comparison found zero changes from the frozen manifest. Introduced-line
whitespace and UTF-8 replacement-character checks passed before compilation. Schema39
SHA256 is 666ff1773c463a92bb8c1d5512b96760d6ce3a7e0b08a2a441a1265d97ed29e3, unchanged.

Candidate APK: app/build/outputs/apk/debug/foodus-dev-3_2_34_3.apk; package
io.github.jarrydadaens.foodus.dev, versionName 3.2.34.3-dev / code 130; SHA256
7e7748ba567ed92526ae7403ef845b0d61bfbc99d09bb3b7d84aa1d5c3b9dd76.
Test APK: app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk; SHA256
aa7994195b5293180d0d5163fe556cc98847c60e945819b541632acd3ff1c1f3.

Exact scoped manifests, before-bytes, artifact metadata, runtime setup and full Android
class/method invocations are in ignored app/build/m2-domain-meals/story26/handoff.json.
Product tests require no canonical database seed: they own private in-memory or uniquely
named databases. Coordinator alone installs/runs on emulator-5580, including the actual
Product screens, existing-data compatibility, and selected Quick Add regressions. Android
runtime acceptance remains unexecuted by this worker and is required before Story closure.
No source edits, device operations or commits are pending/authorized from this handoff.

### Runtime attempt 01 and bounded driver correction

Coordinator ran the real Room persistence test successfully. The first visible Product
workflow failed at its first setAmount("0") verification with an input-update timeout;
ACTION_SET_TEXT had already returned true. The setter walked from the Amount semantics
label to its editable ancestor, but its await predicate read the freshly found label's
text directly. These were different accessibility nodes. The test driver now resolves
a fresh editable ancestor consistently for both write and read and asserts editability;
the timeout is unchanged. No production-state change is selected from this evidence.
Root retains the device and native-build hold while gathering independent remaining
method results against the existing frozen APK. The previous source/build identity is
now dated evidence; refreshed source/test build and rerun are required before acceptance.

### Refreshed handoff after runtime diagnosis

Coordinator reports Product persistence, all six selected Quick Add/scan regressions,
and raw canonical upgrade verification passed against the unchanged production APK
7e7748ba567ed92526ae7403ef845b0d61bfbc99d09bb3b7d84aa1d5c3b9dd76 and the prior
Android-test APK aa7994195b5293180d0d5163fe556cc98847c60e945819b541632acd3ff1c1f3.
Canonical fixture byte hash remains 496dac9591af7ac6569d014dab94facd7d1b6aa8f6bfef9abd8ace57b9dc0659.
Both new Product workflow methods failed only at the same amount-input test helper.
Coordinator reports independent review accepted the focused helper correction, with
assertions and the 15-second timeout unchanged. These passing unchanged regressions
remain actual evidence; neither failing workflow is recorded as accepted.

Refreshed source identity: 9c0a12de1d166b78ba1f6220169bab1e77133c8c0385057ae7b59f1a8ced247a.
The only delta from the previously built source is ProductWorkflowTest.kt, SHA256
c1bec18991931c64feb74f2beaaab2a6a1a551390d18f2bfae97564d38bfaebb.
source-manifest.json still covers 197 paths relative to the exact Story25 predecessor;
helper-fix-manifest.json isolates this one-path correction. No introduced whitespace
or UTF-8 replacement-character findings. Previous source/handoff manifests are retained.

Coordinator now owns the native build and runtime lease and will run the incremental
three-task verification command, rebuild the test APK, and rerun both Product workflows.
The updated handoff marks that build and runtime acceptance pending. This worker made
no additional source edits, build requests, device operations, tracker changes or commits
after the helper fix. Story closure remains coordinator-owned.
