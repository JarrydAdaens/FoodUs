---
name: story-24-meals-library-editor
description: Central Meals management, captured-portion editing, and atomic diary loading.
metadata:
  version: "1.0"
  owner: "Jarryd Adaens"
  repo: "FoodUs (fork of maksimowiczm/FoodYou)"
---
# Plan: Meals library and editor

Execution state: implementation and selected runtime acceptance verified on 2026-10-25.
Stories 25–27 are accepted; Story 24 terminal status and final delivery are owned by
the milestone and [batch record](../../../_boss-runs/milestone-2-domain-meals-sequential/domain-meals-batch-receipt.json).
Authority: [Milestone 2 Story 24](../../../milestones/milestone-2.md#story-24),
[Design vocabulary](../../../design.md#domain-model-shared-vocabulary), and the
[serial run briefing](../../../_boss-runs/milestone-2-domain-meals-sequential/briefing.md).
The milestone owns Story status. This plan owns implementation choices, checks, evidence,
and continuation. Commit policy: none; no pull, push, tag, publication, or commit.

## Outcome and bounds

Provide one discoverable Meals library, independently accessible from an empty diary.
The library and existing Save Meal / Load Meal actions observe the same MealRepository.
Users can inspect, rename, add/remove/edit constituents, change a captured portion or
absolute nutrition, explicitly Save or Cancel, and delete a Meal. Loading appends each
constituent exactly once per explicit submission to the selected date and DiarySlot.
A Recipe portion remains one constituent; a Meal never becomes a Recipe.

The initial planning lease permitted writing this file only and no source or runtime
actions; that hold was superseded by the explicit implementation release below.
Current worker ownership is relevant app/src/**, shared/resources/**, this plan, and
the generated Room41 schema. Story24 is the sole active implementation worker.
No tracker/Design/catalogue/receipt/VCS/device edits are delegated. Native builds
require a coordinated frozen-source lease. Preserve other agents' edits.
The coordinator owns prerequisite acceptance/reconciliation, status/version metadata,
independent review, native-build timing, emulator-5580, receipts and final APK delivery.
Never operate the owner's physical device or delete the preserved baseline.

Preserve source/diary isolation, stable Meal and retained item IDs, configured DiarySlot
identities, legacy Room and serialized names, original nutrient unknowns, and existing
Product/Recipe/Quick Add/placeholder behavior. No unified catalogue hub, new unit
catalogue, live source link, new reference basis/density, import/photo/sharing/relay,
dependency upgrade, branding rewrite, or Meal-to-Recipe conversion is selected.
Public context contains product-owned contracts only; third-party studies/extracts,
screenshots/source, private links, credentials and real diary data stay outside this
public repository, including ignored folders.

## Inspected contracts

Paths below are relative to app/src/commonMain/kotlin/com/maksimowiczm/foodyou/.
These are planning observations, not a frozen implementation identity.

- fooddiary/domain/entity/Meal.kt is the reusable aggregate with MealId, name, createdAt
  and ordered items. MealItem.kt stores only name and already portion-adjusted
  NutritionFacts. DiaryEntry.toMealItem discards original measurement, definition kind,
  source IDs and Quick Add quantity context. It must not be interpreted as per-100 facts.
- MealRepository has observeAll/getMeal/save/delete, no update. RoomMealRepository saves
  header and items in immediateTransaction and sorts read items by position. Existing
  item row IDs are real primary keys but are not exposed in the domain projection.
- MealEntity and MealItemEntity explicitly retain MealTemplate and MealTemplateItem
  tables; templateId is the persisted foreign key. MealDao currently inserts/deletes only.
- SaveMealUseCase captures selected date/slot entries, sorts by name and saves totals.
  It rejects blank names and an empty slot, but does not validate each captured value.
- ApplyMealUseCase inserts one ManualDiaryEntry for each MealItem in an outer
  TransactionProvider transaction. It presently reads the Meal before the transaction;
  domain errors cover missing Meal/slot only. A thrown insertion failure rolls back,
  whereas returning Err after writes would not itself roll back.
- app/ui/home/meals/card/DiarySlotCard contains SaveMealDialog and ApplyMealSheet.
  Dialogs currently dismiss immediately on callbacks. DiarySlotsCardsViewModel ignores
  save/load results and has no in-flight submission guard or recoverable failure state.
  It and the future library can share the existing repository, without another collection.
- app/ui/home/master/HomeScreen has an always-present top app bar and Settings action.
  FoodYouAppNavHost owns navigation. This supplies a small independent Meals entry seam.
- ManualDiaryEntry/ManualDiaryEntryEntity already hold nullable servingCount and
  weightGrams plus description/isPlaceholder. Count and grams are descriptive context;
  they do not scale absolute facts. They cannot represent exact mL, fluid ounce, ounce
  or package selections. Description is user text, not a typed-quantity storage channel.
- UpdateQuickAddViewModel uses entry.copy, which can preserve additive captured metadata.
  Its form uses only a subset of nutrients; hidden fields are preserved by
  QuickAddNutritionMapping. Do not repurpose this form as a complete Meal editor.
- ProductForm exposes all nutrient groups but is coupled to Product metadata and nutrition
  basis normalization. Reuse small field components/formatters where useful, not a fake
  Product whose per-100 values actually mean captured totals.
- Measurement supports Gram, Milliliter, Ounce, FluidOunce, Serving and Package.
  MeasurementTypeSQLConstants gives stable integer discriminators 0 through 5. Existing
  Measurement.stringResource formats quantities without a known metric basis;
  stringResourceWithWeight requires one for serving/package display and is unsuitable here.
- NutritionFacts has 43 supported nutrient fields. NutrientValue distinguishes Complete
  from Incomplete(value?), and multiplication retains that distinction. Actual Room
  toEntityNutrients writes each .value only; toNutritionFacts calls toNutrientValue,
  whose nonnull branch constructs Complete. Thus Incomplete(10) would flatten to 10
  and reopen as falsely Complete(10). Nutrients/Vitamins/Minerals have no completeness
  columns. Keep the flat schema, but normalize new Meal captures conservatively as below;
  do not change the global mapper or retrospectively reinterpret old numeric rows.
- MealUseCasesTest covers basic save/load, repeated intentional loads, distinct same-number
  Meal/slot IDs, deletion isolation and Quick Add unknown nutrition. Its no-op transaction
  fake cannot prove rollback. DiarySlotMealPersistenceTest covers real legacy API reads,
  save/apply/delete and copied-database reopen, but not editor updates or failed writes.
- The harness index has no installed modules. Direct meaningful tests and actual-screen
  emulator evidence remain mandatory for runtime acceptance.

## Selected navigation and interaction

Add a visible text action **Meals** to HomeScreen's existing top app bar, beside Settings,
with a shared string and accessible name. It is present regardless of diary entries,
card layout, configured card visibility or saved collection size. Add a dedicated Meals
library route in FoodYouAppNavHost and focused UI/DI within the established feature
convention. This does not introduce a catalogue hub or another navigation system.

The library observes MealRepository.observeAll and shows saved names, constituent names
and quantities where known, item counts, and explicit Edit, Load Meal and Delete actions.
An expandable row or focused detail area can show constituents without another persistent
domain entity. Distinguish loading/error/empty states: empty copy explains saving a diary
slot as a Meal and offers a route back to the diary. Creating a new empty Meal from the
library is not required; adding constituents belongs to an existing Meal's editor.
Delete uses the established confirmation pattern and affects only future loads; Cancel
and failed deletion keep the collection intact.

Library Load Meal asks for a date and an existing configured DiarySlot, with the current
home day as an initial date when available. Show the configured destination name; never
confuse a Meal ID with a slot ID, silently choose an absent slot, or infer a destination
from a Meal name. Existing diary-card Load Meal keeps its already selected day/slot.
Only the explicit load button submits; opening a row, navigation, recomposition or a
Flow emission never logs entries. A later deliberate second load is a valid second append.

The editor loads a saved aggregate once into a private draft. Do not replace dirty input
when the library Flow updates. Changes to name, constituents, amounts or nutrition remain
draft-only until Save. Remove operates on the draft; Cancel, Back/discard confirmation and
cancelled constituent selection perform no writes. Use stable draft row keys, with
ephemeral keys for unsaved new rows. Successful Save alone leaves the editor. Invalid
input and expected write failures keep the draft, prior aggregate and a visible
recoverable error. Disable duplicate Save/Load submissions while each operation is in
flight; re-enable after failure.

## Selected captured-portion and metadata model

The authoritative value of every MealItem remains its **absolute captured nutrition
for the entire constituent**. A nullable known amount is quantity context associated
with that total; it is not canonical nutrition, a serving definition, a live source
reference or an extra multiplier to apply at load.

Retain known quantity/unit from new selections and diary captures. Use the existing
Measurement as nullable **capturedPortion** on MealItem and ManualDiaryEntry. Persist it
as a nullable type/value pair using the established MeasurementType integer encoding.
This minimal additive manual-entry metadata is necessary so loading a known mL,
fluid-ounce or package portion does not erase or mislabel it. Do not overload description,
weightGrams or servingCount to store other units. No productId/recipeId, source kind,
canonical per-100 facts, inferred serving/package basis, output-weight or density is added.

Also add nullable **servingCount** and **weightGrams** to MealItem solely to capture and
round-trip existing ManualDiaryEntry Quick Add context. ManualDiaryEntry already has
these fields. They remain distinct from capturedPortion and never trigger nutrition
scaling. A Product/Recipe amount is not copied into those descriptive fields merely to
avoid the typed representation.

| Constituent origin | Captured facts and quantity |
| --- | --- |
| Legacy saved row | Preserve totals; capturedPortion, servingCount and weightGrams remain null. Show Original amount unknown. Never infer 100 g, one serving/unit, a source ID or a canonical basis. |
| Newly saved NutritionalItemDiaryEntry | Calculate totals under the snapshot's own contract, normalize incomplete fields to unknown for this new flat capture, and retain exact Measurement; no catalogue reference survives. |
| Newly selected Food or Recipe in Meal editor | Use existing search/typed ID and portion UI plus accepted nutritionFor/portion resolver; calculate totals once, normalize incomplete fields to unknown and retain exact Measurement. Confirmation adds a draft constituent only. |
| Newly saved ordinary Quick Add | Capture complete absolute totals, normalize any incomplete facts to unknown, and retain descriptive servingCount/weightGrams unchanged; capturedPortion stays null. Context is never a Product serving basis. |
| Previously loaded Meal manual snapshot | Capture its retained capturedPortion and descriptive context, alongside current absolute totals. Load then save again preserves known quantity. |
| Placeholder | Preserve established name/zero or unknown facts capture without inventing a portion or catalogue item. Source placeholder flags/entries stay untouched. |

For a known capturedPortion, editing the positive finite amount in its **same unit**
scales the current draft totals once by newAmount / oldAmount and updates the captured
amount. Example: 2 captured servings and 400 energy units changed to 1.5 servings gives
300 units, without knowing grams. This also supports a Recipe whose facts resolve but
aggregate metric basis is unknown, using Story 27's serving fraction at first capture.
Reject overflow/underflow or unresolvable new selection rather than fabricating a basis.
Existing captured totals do not consult live definitions.

Keep the captured unit fixed for direct amount editing; another unit requires a fresh
Food/Recipe selection and explicit draft replacement resolved by accepted infrastructure.
Do not offer serving-to-gram or mass-to-volume conversion without the original basis.

A separate explicit **Scale captured portion** action applies a positive finite factor
to captured totals and every existing quantity context together: capturedPortion,
servingCount and weightGrams where nonnull. This is an explicit whole-entry operation,
not an automatic consequence of editing descriptive metadata. Direct known-amount edits
apply the same ratio to any coexisting descriptive context, avoiding contradictory
retained quantities. Null fields remain null. Legacy/null original amounts stay unknown;
a mathematical factor of 1 is never presented as an invented one-unit/one-serving amount.

Editing descriptive Quick Add count/grams directly changes those fields only and never
recalculates nutrition. If such an edit changes quantity context on a loaded/manual row
that has capturedPortion, explicitly clear capturedPortion because its relation to the
revised quantity is no longer reliable; explain that the saved portion context will be
replaced and absolute nutrition stays unchanged. Apply the same rule in the Meal item
editor if both kinds of context coexist. Retain capturedPortion for name, description
or direct nutrition corrections to the same portion. A no-op metadata edit retains it.
Do not accidentally clear metadata by constructing an incomplete default model.

Direct nutrient editing changes absolute totals only and retains associated quantity.
Display Nutrition for this captured portion, with no per-100 label or automatic
macro-derived energy replacement. Expose supported nutrient groups with existing energy
and gram/milli/micro display units. Preserve untouched fields, precision and nulls:
a name-only save must not turn unknowns into zero, round all facts through Float, drop
vitamins/minerals or recalculate authoritative energy. Clearing an edited nutrient makes
it unknown. Keep the existing numeric/null Room semantics described above.


## Recipe historical calculation boundary

Story 27's authorized 39→40 migration distinguishes existing DiaryRecipe snapshots
with calculation contract **0** from fresh live-definition captures with current
contract **1**. Preserve that boundary across reads, nested snapshots, copy and unpack.
The concrete old mixed 100 g plus 100 mL Recipe logged as 100 g must retain 150 energy
under contract 0, and a legacy 10-protein-plus-unknown composition retains its numeric
partial 10 result. Contract 1 uses the accepted strict composition/unknown semantics and
supports serving fractions when ingredient facts resolve without aggregate metric basis.

Meal saving first evaluates DiaryEntry.nutritionFacts under the source snapshot's own
accepted contract, then applies the conservative new-capture normalization below.
Never rebuild a legacy Recipe from a live definition or apply current 1 math to it merely
because the capture is new. Complete(150) mixed-basis energy can capture/load as 150;
legacy Incomplete(10) protein remains Incomplete(10) in the source diary, while its new
flat Meal constituent stores unknown/null protein rather than falsely complete 10.
A saved Meal needs no calculation-contract field because it contains absolute facts,
not Recipe composition. Loading reproduces those stored facts as separate manual
snapshots without creating/reinterpreting RecipeSnapshot. Retained amount is context,
not permission to resolve totals with another contract. Explicit scaling acts on stored
captured totals. Assert source contract 0/1 rows, original completeness and historical
results stay unchanged through capture, editing, load, deletion and database reopen.


## Conservative completeness at new-capture boundaries

The selected flat numeric/null Meal and Manual storage cannot encode a nonnull partial
nutrient and its incompleteness together. For **new Meal captures and new/replaced
constituents**, retain a numeric field only when the evaluated NutrientValue is Complete;
normalize every Incomplete(value), including Incomplete(10), Incomplete(0) and
Incomplete(null), to Incomplete(null) before persistence. Complete(0) remains a known
zero. Validate finite/nonnegative present source numbers before normalization so malformed
numeric input is still a recoverable failure. Apply this rule field-by-field across all
43 supported nutrients, not only macros or energy, and before presenting the captured
draft preview so preview, Save, restart and Load agree. Explain incomplete nutrition as
unknown using shared UI resources. User-entered replacement numbers can become Complete
only through an explicit field edit, never by silently serializing .value.

Keep this normalization at new Meal capture/selection/domain boundaries and guard their
flat write path. Do not globally change toEntityNutrients or read mappings, rewrite
DiaryRecipe history, add a completeness flag/contract field, or alter the six-column
40→41 metadata migration. New selection uses the accepted NutritionalItem.nutritionFor
(Measurement) / snapshot equivalent, then normalizes the result for flat capture.

Existing saved Meal numeric rows have already lost provenance about completeness.
Preserve them as stored, including numeric 10, nulls and untouched IDs: do not guess
which old values were partial and do not null them on migration, read, rename, ordinary
save or load. A name-only save preserves all existing fields exactly. Newly captured
flat manual rows remain complete-or-null through subsequent load/re-save. Relative
scaling preserves nulls; it cannot manufacture completeness or recover a dropped
partial value. Original contract 0 Recipe history remains Incomplete(10) even when a
new Meal can represent that field only as unknown. No extra completeness storage feature
is selected or implied.

## Persistence and migration decision

The coordinator accepted this optional metadata direction during planning. An additive
migration is selected because new known amounts must survive edit, load and restart.
Story 27 now owns schema 39→40 for the historical DiaryRecipe calculation contract.
Against its expected accepted schema 40, Story 24 adds:

| Legacy table | Nullable columns, explicit SQL default NULL |
| --- | --- |
| MealTemplateItem | capturedPortionType INTEGER; capturedPortionValue REAL; servingCount REAL; weightGrams REAL |
| ManualDiaryEntry | capturedPortionType INTEGER; capturedPortionValue REAL |

Implement a focused **40→41** migration and register it using the existing Room migration
pattern; export schema 41 without rewriting schema 39 or Story 27's accepted schema 40.
Reconfirm the actual accepted predecessor schema/version and registered 39→40 migration
at release before source edits. If they differ from this expectation, reconcile the
additive path with the coordinator first. No table recreation, renumbering, destructive
fallback, replacement schema 39 fixture, new foreign key or unit discriminator change.
The six nullable columns above are Story 24's entire schema delta; do not duplicate,
renumber or modify Story 27's DiaryRecipe contract column or backfill its values.

Map a valid nonnull pair to the existing Measurement; both null means unknown. New
writes require both fields together, a supported type and positive finite value.
Do not add throwing constructors that prevent reopening legacy data. Malformed nonnull
metadata is explicitly unavailable/error data, never a silently fabricated amount.
New mutation/load boundaries validate before writes.

Expose retained item primary keys in the editing/domain projection (a small MealItemId
or equivalent scoped ID), with null/unassigned IDs for new draft rows. Add
MealRepository.update and a focused UpdateMealUseCase. In one Room transaction verify
aggregate existence and item ownership, update header name preserving MealId/createdAt,
update retained items with existing IDs/positions, insert new rows and delete removed
rows. Do not delete/recreate retained rows for convenience. Reject duplicate/cross-Meal/
stale retained IDs and missing/deleted aggregates without resurrection. Header, items
and order commit together; errors leave all prior rows/identities/creation time intact.
The existing collection Flow publishes the committed aggregate to library and diary.

Keep legacy tables, templateId, ManualDiaryEntry/mealId, indices, discriminators and
serialized/backup identities. New routes use role-based names; historical serial keys
retain explicit mappings. Backup/copy restore through the registered migration must
preserve metadata. Existing CSV/export formats remain unchanged; inspect their actual
quantity-format limits without enlarging scope into a new import/export contract.

## Validation, save and atomic load

Use small domain validation shared by update, diary-save capture, new constituent
confirmation and load. Require trimmed nonblank Meal/item names, at least one item,
all known nutrition values finite and nonnegative, valid paired captured quantity and
positive finite optional descriptive counts/grams. Unknown nutrients and valid zero
totals remain allowed, including legacy placeholders. Check all 43 supported fields,
display-to-storage conversions, scaling ratios and results. Reject infinity, NaN,
negative values, unparsable text, ratio overflow and loss of a positive value to zero
through underflow. Do not insert fabricated values to make validation succeed.
Legacy invalid rows remain readable and repairable; failed operations never rewrite
them. Use typed existing Result-style errors mapped to shared resource messages.
Errors/logs contain no user-entered diary content.

Save diary capture must take a coherent selected date/slot snapshot and persist validated
new items without touching source rows. Editor Save validates an immutable draft once;
the repository transaction rechecks existence/ownership before mutation. Expected storage
failure produces recoverable UI state and preserves prior saved data. Propagate coroutine
CancellationException through Room so cancellation rolls back. Never swallow an exception
after partial writes and return Err in a transaction that would then commit.

For load, read one committed Meal version and validate all intended output snapshots and
the existing destination within the transaction before inserting. Append one manual
snapshot per constituent with capturedPortion, descriptive context and absolute facts.
There is no second scaling at load. Preserve selected date/DiarySlot and existing
entries; a Recipe portion is one entry even if it contains nested Recipes. Intermediate
insert failure or cancellation rolls back all inserts and leaves saved Meal/source rows
unchanged. Success/UI dismissal follows commit; a callback is not confirmed success.

Show retained quantity through the existing manual diary projection and actual list/
details/editor as appropriate, using Measurement.stringResource without requiring
unknown weight. Preserve it in copy-based Quick Add updates except the deliberate
quantity-context invalidation rule above. No Product normalization or promotion rule
may consume capturedPortion as an inferred metric basis.

## Wording and affected surface

Use shared Meal/Meals strings throughout Home entry, library, editor, row/detail actions,
date/slot load sheet, existing diary Save/Load sheets, empty/loading states, validation,
failure/success messages, delete confirmation and accessibility names. Current English
feature strings already say Meal after Story 25. At release search every localized
override of this feature's keys/values for remaining template wording; update affected
translations where reliable or use the shared fallback rather than retain obsolete
feature wording. Do not globally rewrite unrelated template concepts, historical
migrations/docs, namespaces, configured Breakfast/Lunch slot names or friendly Food
wording. Keep typed Product/Recipe identities through search selection.

Expected edits: MealItem/MealRepository/save/apply/update use cases and domain DI;
MealDao/entities/RoomMealRepository; ManualDiaryEntry/entity/repository mapping;
FoodYouDatabase migration registration and generated schema 41; HomeScreen/navigation/UI
DI; focused Meals library/editor/search/portion/nutrition UI; diary-card save/load state
and feedback; manual diary projection/quantity display and metadata-preserving updates;
shared resources; meaningful unit and Android instrumentation regressions. Restrict
mechanical repository fake/signature adjustments to necessary callers.

## Delivery sequence and gates

| Step | Deliverable and evidence | Prerequisite |
| --- | --- | --- |
| 1 | Re-read accepted 26/27 plans, receipts and helpers; reconfirm Story 27 schema 40/contract migration; coordinator explicitly releases Story 24 | Both prerequisite acceptances/reconciliation |
| 2 | Focused failing/passing captured-total, known/unknown portion, Quick Add context, full-nutrient and validation tests | Step 1 |
| 3 | Nullable metadata 40→41 migration; identity-preserving update; Room rollback/reopen and 39→40→41/40→41 upgrade regressions | Step 2, retained schema 39 and accepted schema 40 baselines |
| 4 | Central library/navigation and private Save/Cancel editor; accepted search/portion capture; shared wording and recoverable errors | Steps 2–3 |
| 5 | Coherent validated diary Save/Load, atomic append and guarded state; known loaded quantity display/preservation | Steps 2–4 |
| 6 | Synthetic actual-screen workflow and failure/empty coverage; independent read-only review of migration, IDs, editor and load transactions; repair findings | Steps 3–5 |
| 7 | Check generated schema scope, UTF-8 and changed-line whitespace; freeze source/resources/tests and report identity | Step 6 |
| 8 | Coordinator runs frozen build/tests and emulator upgrade/runtime acceptance, then status/version/receipt reconciliation and requested APK delivery | Step 7, native/device lease |

Complexity is moderate across editor state, additive quantity metadata and aggregate
transactions. Effort concentrates in visible editor integration and real Room upgrade/
rollback evidence. Main risks are inventing a basis, scaling totals twice, dropping full
or unknown nutrient fields, partial loads and loss of IDs. No elapsed time, capacity
forecast, model override or speculative tuning is asserted.

## Required checks and recovery

1. Domain/editing: blank/whitespace Meal or item name; empty composition; invalid numeric
   text, zero/negative/nonfinite portion or scale/context; invalid known nutrient and
   overflow/underflow. Rejected writes make no repository mutation. Cover original
   quantity null, all six known units, fractions, duplicate named constituents and
   same-number Product/Recipe/Meal/slot identities. All 43 nutrient fields survive
   name-only edits, including B12, explicit zero, authoritative energy and null values.
2. Portion math: Product 40 g serving captured at two servings equals 80 g direct totals;
   change two to 1.5 captured servings scales totals once. Liquid mL/fluid-ounce,
   gram/ounce and package fractions preserve selected unit. Recipe yield 3 captured as
   2 servings retains two-thirds totals as one constituent; include unknown/mixed
   aggregate metric basis where ingredient facts resolve. Later live Product/Recipe
   edits/deletion cannot affect captured Meal or previously loaded diary rows.
   Preserve contract-specific capture without false completeness: legacy mixed 100 g/
   100 mL logged 100 g has Complete(150) energy and saves/loads 150. Legacy protein
   Incomplete(10) becomes unknown/null in a NEW Meal and remains unknown after load,
   restart and re-save; its source Recipe diary still has contract 0/Incomplete(10).
   Current 1 incomplete/unknown nutrient stays unknown through capture/load; current 1
   mixed-basis two-of-three serving totals retain every complete fact. An existing
   legacy Meal numeric 10 row stays 10 through read, rename, load and reopen; an
   explicit factor 0.5 scales it to 5 rather than nulling it based on guessed completeness.
   Source snapshots stay unchanged after Meal edits
   and deletion.
   Parameterize the new-capture normalizer across all 43 fields with Complete(10),
   Complete(0), Incomplete(10), Incomplete(0) and Incomplete(null). Through real Room
   save/reopen/load assert only Complete inputs stay numeric, every incomplete input
   stays null/unknown, and no Incomplete(10) becomes Complete(10). Compare source
   NutritionFacts and DiaryRecipe contract/results before/after to prove no mutation.
3. Quick Add/unknowns: 780 absolute energy units with descriptive 2.5 servings/250 g
   remain 780 through capture, load, re-save and direct descriptive metadata edits;
   null nutrients stay null. Legacy unknown amount remains unknown after rename,
   nutrition edit or relative scale. An explicit whole-portion factor 0.5 changes
   780→390, known descriptive context 2.5→1.25 and 250→125, and a known captured amount
   by the same ratio; no action interprets count/grams as a Product basis. Direct count/
   weight edits on a row with capturedPortion clear that context with visible explanation
   and leave nutrition unchanged; no-op metadata edits and name/nutrition corrections
   retain it. Round-trip both rules through Room and existing Quick Add UI. Existing
   promotion/conversion and placeholder regressions still pass.
4. Save/Cancel/UI state: change draft rename/add/remove/nutrient/portion then Cancel or
   Back/discard and reopen returns original ID/name/items/facts/metadata/order. Failed
   Save retains draft and prior aggregate. Duplicate Save while in-flight commits once;
   corrected retry after failure succeeds. Missing/deleted Meal and stale/cross-Meal
   item IDs fail without resurrection or writes.
5. Real Room update/reopen: header and retained item IDs/createdAt survive; new rows
   get new IDs; removed rows alone disappear; order persists and collection flows update.
   Inject failure after header/retained-item mutation, before final insert/delete, and
   cancellation; prove prior aggregate unchanged after database close/reopen. The
   existing no-op transaction fake cannot establish rollback.
6. Atomic load: seed existing destination rows and a multi-item Meal with a Recipe
   portion and Quick Add context. One explicit action appends exactly N rows at selected
   date/slot, matching totals/quantity, without replacement or Recipe flattening.
   Rapid duplicate UI taps do not append twice; a later intentional load appends N more.
   Missing slot/Meal, invalid item, second-insert failure and cancellation leave no
   new rows and preserve old diary/saved data, proven with real Room.
7. Upgrade: preserve schema 39 JSON, original LegacyDiarySlotMealFixtureTest seed,
   baseline APK code128 and canonical synthetic before JSON under existing ignored
   baseline/run evidence. Also preserve Story 27's accepted schema 40, migration/contract
   identity and synthetic schema 40 pre-Story24 database/old-column projection. Do not
   overwrite these fixtures/schemas or clean app/build. Test both upgrade paths on
   coordinator-owned emulator-5580 without destructive fallback:
   - **39→40→41:** seed original schema 39 with baseline128, retain the original canonical
     old-column projection, install the final candidate in place and run both registered
     migrations. Compare all schema 39 columns/IDs/FKs/order/nutrition/timestamps/unknowns
     to their original projection; separately assert Story 27's added DiaryRecipe
     contract is 0 on every migrated historical row, and all six Story24 columns are NULL.
     Verify the concrete legacy mixed Gram(100)150-energy and partial 10-protein results,
     not only successful database open.
   - **40→41:** seed/copy the accepted Story27 schema 40 fixture containing both legacy 0
     and fresh current 1 Recipe snapshots (including nested snapshots), then upgrade in
     place. Compare every pre-Story24 schema 40 column/value, including the 0/1 contract,
     against its frozen projection. All six added metadata columns must be NULL on
     preexisting Meal/manual rows. Assert legacy 0 results, current 1 strict unknowns and
     serving-fraction totals remain unchanged; fresh post-upgrade captures still use 1.
   Assert schema 41 differs from accepted schema 40 only by the selected six nullable
   columns/defaults; schema 39 and 40 exports stay unchanged. Add a Story24 version-aware
   verifier rather than rewrite the historical fixture's expected shape. Retain original
   before/after evidence and explicit projected-column/new-default results; do not claim
   whole new-schema JSON/database byte equality when schema identities/columns differ.
   Exercise old schema 39 and accepted schema 40 backup-copy restore through their complete
   registered migration paths, then schema 41 backup/reopen with typed known quantities
   and descriptive Quick Add context. Meal Save→edit→Load must reproduce every complete
   legacy 0/current 1 fact and keep newly normalized incomplete facts unknown, without
   changing any source DiaryRecipe contract/completeness or introducing a Meal contract
   field. Separately retain an existing legacy Meal numeric 10 row exactly; no migration
   or ordinary edit may retrospectively replace it with null. Cover source-definition
   edits and deletion too.
8. Actual-screen Compose instrumentation with isolated Room/Koin and synthetic data:
   from an empty diary reach Meals via Home and verify useful empty state. Log synthetic
   Food/Recipe/Quick Add entries, Save Meal via the existing card, open that same Meal
   from the library, rename/add/remove/change known portion/edit totals, exercise Cancel
   and invalid Save, save/reopen, then Load into a selected date/DiarySlot with an
   existing row. Verify count/names/known amounts/totals and Recipe one-entry boundary.
   Delete the Meal, verify library empty, and confirm prior diary/source snapshots
   remain. Include a legacy unknown-amount Meal and missing/failed load feedback.
   Use the real library/editor/card screens with minimal stable semantics where needed.
   Coordinator separately force-stops/relaunches the canonical synthetic app to prove
   persisted edits after process restart and collects visible evidence.
9. Final checks, JDK 21 on Windows: .\gradlew.bat --no-daemon
   :app:testDebugUnitTest :app:assembleDebug :app:assembleDebugAndroidTest, plus focused
   Android migration/update/atomic-load and actual-screen instrumentation. Coordinator
   controls native approvals/device invocations and final version configuration.
   Freeze source before final acceptance; any later fix refreshes identity and reruns
   affected checks. No global clean or owner-phone operation.

No build, automated test, runtime verification or migration has been performed in this
planning lease. Missing required checks remain incomplete, never an inferred pass.
Use synthetic sanitized evidence under existing ignored app/build during execution;
no additional evidence sidecar is needed for this planning pass.

Recovery: before execution retain a scoped predecessor file/hash manifest and accepted
APK/schema identity through the coordinator. Restore only worker-owned changes if needed,
never reset unrelated edits or accepted predecessor work. A schema 41 database cannot
simply be downgraded to schema 40 or schema 39: preserve both synthetic predecessor copies,
original schema 39 baseline and accepted schema 40 identity. Re-seed/reinstall only the
isolated test app when repeating upgrade acceptance. Never
apply this recovery to owner diary data. Failed aggregate transactions must already
preserve prior data without a manual repair path.

## Evidence and next step

- 2026-10-25 planning: read AGENTS, relevant laws, harness index, workflow, Design
  vocabulary, full Story 24, run briefing, 26/27 plans, existing Meal/Manual domain/
  Room/UI/quantity/nutrition code and unit/persistence tests. No private studies or
  extracted external material were used.
- Selected Home top-app-bar Meals entry, one collection, draft Save/Cancel,
  authoritative captured totals, optional exact known portion metadata, separate
  descriptive Quick Add context, legacy relative-scaling fallback and focused additive
  metadata migration, initially planned as schema 39→40 and subsequently rebased to
  schema 40→41 after Story 27's preservation decision. Coordinator accepted this direction
  and required
  explicit context-coherence rules. The earlier totals-only/schema-preserving option
  was rejected before code because it would discard newly known amount/unit.
- Shell availability was intermittent; planning used UTF-8 Node filesystem reads/writes.
  No escalation, build/device, source/test/resource, tracker/catalogue/Design or VCS action.
- Planning update: coordinator authorized Story 27's DiaryRecipe legacy 0/current 1
  discriminator and 39→40 migration. Rebased only this plan's six nullable columns onto
  expected 40→41; retained original schema 39/baseline evidence and all editor decisions.
  Added 39→40→41 and 40→41 projection/default checks plus Meal capture/load regressions
  for both Recipe contracts. No implementation or acceptance result is claimed.
- Completeness planning correction: inspected NutrientValue, NutritionFacts and actual
  Room nutrient/Meal mappings. The mapper writes .value and restores every nonnull
  numeric as Complete, with no stored flag. New capture now normalizes Incomplete
  (nonnull or null) to unknown across all 43 fields; Complete 150 remains 150. This
  supersedes the earlier new-Meal partial 10 round-trip claim. Existing legacy Meal
  numeric 10 and historical Recipe contract 0/Incomplete(10) remain unchanged. The
  six nullable metadata columns and 40→41 decision are retained; no completeness storage
  feature, global mapper change or source implementation was authorized/performed.
- Next: return update readiness and hold. Story 26 is accepted; after Story 27 acceptance/
  reconciliation and explicit release, reconfirm actual schema 40 and contract helpers,
  capture predecessor identity and begin Step 2.

### Released implementation checkpoint

Coordinator explicitly released Story 24 after Story 27 receipt reconciled
VERIFIED_UNCOMMITTED, diff fd054311d0a759c081babfd14e7193d0772fc36c6ea7f21d12c1838dd2061f7f.
Accepted schema40 SHA2566392aeff08f3291136480faa479eda13729a726d4779a4f6006f16dd9b0bd31b
was independently confirmed before edits; schema39/40 remain preserved. Captured939
scoped app/src, resource, schema and plan files in ignored story24/before with
predecessor.json mapping relative paths to SHA256. Root owns candidate3.2.36.3/code132.

Authored domain capture/validation/scaling, metadata migration, identity-preserving
Room update and atomic save/load, dedicated Home Meals navigation, draft library/editor
and precise all43-nutrient item form, shared failure feedback and loaded quantity
preservation. Existing tests receive only necessary API compatibility adjustments.
Initial source checkpoint is frozen for a coordinated narrow compile; new domain/Room/
migration/visible workflow tests remain to be authored after compile feedback. No
build/runtime acceptance is claimed.

### Regression-authoring freeze

The narrow frozen :app:compileDebugKotlinAndroid passed in2m24s; Gradle required
access to its existing user cache after a default-sandbox lock-file denial. Log:
app/build/m2-domain-meals/story24/initial-compile.log. KSP exported41 with exactly
the selected six nullable columns/defaultNULL; every old field/index/FK/view and
original39/40 bytes remain unchanged. This is compile/schema evidence only.

Early independent source review found and then accepted four focused corrections:
lossless QuickAdd count/weight form seeds preserve untouched Double context; malformed
Meal/manual reads stay strict unavailable errors with visible Retry/resubscription;
library failure cannot become permanent loading; immutable editor drafts retain stable
ephemeral row keys. Typed selection keys and numeric no-op comparison were also fixed.
Read-only correction review passed dc8eeac67353cdddac70ee76515427c6bb0eb79578b8a7bc66258eb15d023e0e.
Final identity binding and test/runtime acceptance remain pending.

Authored MealEditingTest (seven common methods); MealMigrationTest (two Android methods
for39→40→41 and40→41 including both nested0/1 fixtures); MealPersistenceTest (four
Android methods for IDs/fullfacts/reopen, intermediate rollback/cancellation, atomic
append and explicit malformed-read recovery); MealsWorkflowTest (three real-screen
methods for save/library/edit/cancel/failure/load/delete, empty/unavailable retry, and
precise loaded quantity retention versus deliberate metadata edit); separate
MealCanonicalUpgradeTest (one original39-projection plus new-default41 verifier).
Historical schema39/40 fixture expectations are retained. Existing Recipe migration
test additionally registers41 for the subsequent production Room reopen, not its40
assertion. New tests have not yet compiled or executed at this freeze.

Next: coordinated frozen unit/debug/test-APK build, repair any proven failure with
refreshed identity, then root runs the exact Android inventory above on emulator5580,
canonical40→41 and baseline39→40→41, process-restart evidence, final independent review
and closure transaction. No commit or device operation by this worker.

### Verification attempt01 and bounded test repairs

Frozen three-task verification at6dabc85e completed production compilation and
172unit tests/35suites with zero failures/errors/skips, then failed Android-test
compilation at the necessary MultiFoodScanTest.insert signature adaptation and a
message-last JUnit assertEquals in MealsWorkflowTest. Process exited1; log remains
verification-01.log. No APK/runtime acceptance is claimed from this attempt.

Corrected both test-only defects and removed one unnecessary test nonnull assertion.
Added explicit removed/stale retained-ID and deleted-aggregate assertions, plus one
real Room/ViewModel test with suspended first Save/Load operations proving repeated
submissions enter persistence once. That test also directly checks new row keys stay
stable through removal/edit/cancel, with no draft writes. Source correction review
remains unchanged; new regression inventory is seven common and eleven Android methods.
Next: re-freeze and rerun the three tasks under the coordinated native lease, then
root-only runtime checks and final identity-bound review.

### Passing build02 and remaining decoder consumers

Verification02 passed all three tasks in48s:172unit tests/35suites, zero failures,
errors or skips. Built debug76358031ba4e2e4091afac77c48fef291a509e575f2394ba888045d05a9f37cf
and testAPK44e2a6c2c2f316c066e73d32969059512106eaace501b509c8b638eed81e1c89
at frozen2bb51a99. Logverification-02.log. These are interim artifacts, not final
closure evidence. Coordinator reports12distinct interim Android passes recorded in
interim-runtime.json, actual canonical40→41 and separate baseline128/schema39→132/41
registered upgrade passes in baseline39-to41-result.json, retaining original fixtures
and failed transfer/fresh-fixture guards with their successful bounded retries.
Final review must explicitly bind any retained unchanged-method evidence to its actual
artifact provenance; this worker has performed no device operation.

A narrow independent consumer trace confirmed three further uncaught strict decoder
collectors: Home summary, daily summary, and placeholder entry/slot-name flows. Added
only explicit unavailable/Retry/resubscription handling and shared diary error copy at
those boundaries, keeping malformed values distinct from unknown and stored rows intact.
Added MealSummaryReadRecoveryTest for both summaries and placeholder through real Room;
a fourth MealsWorkflowTest method exercises actual Home summary unavailable/Retry and
restored780-energy display. Replaced a deprecated primitive-double JUnit assertion with
explicit1e-9 tolerance. Original three UI workflows retain their fixture behavior; the
new summary-specific fixture opts into the real Goals card.

Current new inventory:7common and13Android methods. Pending: new correction compile,
new/affected UI/runtime results, final selected independent review and root closure.
No further source changes are planned without check/review evidence.


### Coordinator acceptance

173 unit tests in 35 suites passed with no failures/errors/skips; debug, test and
unsigned release APK builds and release vital lint passed. The selected batch has
29 distinct passing instrumentation methods, retaining reviewed unchanged behavior
with exact original source/APK/log provenance. All affected Meals/manual/summary and
Quick Add workflows passed after the recorded corrections, including the final full
save/edit/cancel/failure/load/delete case. Both actual upgrade paths preserve the
original schema39 fixture projection; real production Home→Meals navigation and a
saved rename survived force-stop/relaunch, then the original fixture was restored
through the UI and reverified. Numeric conversion overflow is rejected before Room
header/item writes. Final source/test-helper and identity binding passed independently.

The single batch record owns closure and APK delivery evidence; earlier failed runs
remain retained as failures. No commit, public release, owner-device change or personal
account-data import was performed in this Story. Selected requirements above are
unchanged by this acceptance-only update.
