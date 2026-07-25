# Plan: Quick Add Expansion — New Fields & Migration

## Metadata

- Task Type: `FEATURE`
- Status: `Draft`
- Owner: Jarryd Adaens
- Last Updated: 25 July 2026

## Linked Context

- Milestone: [context/milestones/milestone-2.md](../../../milestones/milestone-2.md)
- Story: [Story 18: Quick Add expansion — new fields & migration](../../../milestones/milestone-2.md#story-18)
- Dictation source: [2026-07-25 Milestone 2 feature spec](../../../dictations-tier-0/2026-07-25_milestone-2_australian-providers-and-quickadd-spec.md) (§5, §7.1–7.2, §10.3)
- Related Plans:
  - [Story 14 (spike)](../story-14-provider-quickadd-architecture-spike/plan.md) — its Quick-Add findings already ground this plan.
  - [Story 19 (promotion)](../story-19-quickadd-promotion-workflow/plan.md) — consumes these fields; the `weightGrams` field is a hard dependency of promote-to-Product.

## CER

- Complexity: 3
- Effort: 3
- Risk: 3
- Notes: Inline estimate. Mostly additive — fibre needs **no** schema change (reuses `NutritionFacts.dietaryFiber`), and the one semantic landmine (serving/weight vs the TOTAL nutrition meaning) is defused by the spec's §5.3 rule: serving count and weight are **quantity context only**, not multipliers. Risk sits at 3 because a mistaken multiplier would silently double or halve every meal/day total.

## Objective

Add four optional fields to Quick Add — Description, Fibre (g), Number of servings, Weight (g) — persisted on the manual diary entry, visible and editable on both new and historical entries, with a backward-compatible migration that never rewrites existing nutritional totals and preserves Quick Add's existing "nutrition = entry total" semantics.

## Scope

### In Scope

- New Quick Add fields: `description` (optional free text), `fibre` (grams, decimal, non-negative → maps to existing `NutritionFacts.dietaryFiber`), `servingCount` (decimal > 0, default 1), `weightGrams` (grams, decimal > 0 when supplied).
- Domain + Room changes on `ManualDiaryEntry` / `ManualDiaryEntryEntity` for `description`, `servingCount`, `weightGrams` (fibre already has a column).
- A Room migration (AutoMigration 32→33) adding the three nullable columns; existing rows read back cleanly.
- Form wiring in the shared Quick Add component so create and edit-historical both show/persist the fields, in the spec §5.4 order.
- Validation per §5.5; unit tests per §10.3.

### Out Of Scope

- Promotion to product/recipe — Story 19.
- Any change to how the four existing macros/energy are interpreted (they remain the entry total).
- Provider work.

## Non-Goals

- Making serving count or weight scale the stored nutrition. Per §5.3 they are quantity context; the entry's macros stay absolute totals so `DiaryMeal.sum()` is unaffected.
- Adding new micronutrient fields (fibre already exists; no 42-field fan-out needed).

## Current Understanding

Verified by recon on 2026-07-25 (`file:line` where load-bearing):

- Shared form: `app/ui/food/diary/quickadd/QuickAddForm.kt` (`QuickAddForm(state)`), state `QuickAddFormState.kt` (`rememberQuickAddFormState(name, proteins, carbohydrates, fats, energy)`; class fields + `isModified`/`isValid`/`autoCalculateEnergy` at 186–206), screen shell `QuickAddScreen.kt`. Create (`CreateQuickAddScreen.kt`) and Update-historical (`UpdateQuickAddScreen.kt:36-43`) reuse all three; only the thin wrapper + view model differ.
- Entity: `fooddiary/infrastructure/room/ManualDiaryEntryEntity.kt` (table `ManualDiaryEntry`) — `@Embedded Nutrients/Vitamins/Minerals`, so `dietaryFiber` **already** persists. Domain `fooddiary/domain/entity/ManualDiaryEntry.kt` (`name, nutritionFacts, createdAt, updatedAt`). DAO `ManualDiaryEntryDao.kt`; repository `RoomManualDiaryEntryRepository.kt` (mapping 56–86).
- Nutrition semantics: `ManualDiaryEntry.nutritionFacts` is the **absolute total** for the entry; flows unscaled into `DiaryMeal.nutritionFacts = entries.map { it.nutritionFacts }.sum()`.
- Fibre: `common/domain/food/NutritionFacts.kt:17-19` (`dietaryFiber/solubleFiber/insolubleFiber`), stored in grams (not the ×1000 mineral convention).
- Validation primitives: `QuickAddFormFieldError { Required, InvalidNumber, NegativeNumber }`; parsers `app/ui/common/form/Parser.kt` (`nullableDoubleParser`); validators `app/ui/common/form/Validator.kt` (`nonNegativeFloatValidator`, `positiveFloatValidator`, `nonBlankStringValidator`). Decimal parsing uses `toDoubleOrNull()` — `.` separator only; keyboard `KeyboardType.Decimal`.
- Migrations: `app/infrastructure/room/FoodYouDatabase.kt` (`VERSION = 32`, `exportSchema = true`); the Quick Add table itself arrived as AutoMigration 28→29 — additive nullable columns qualify for the same AutoMigration treatment. Tests under `app/src/commonTest/.../migration/`.
- Assumptions/constraints: Quick Add stores `Double`; blank optional → `null`; `isModified`/`isValid` enumerate every field, so new fields must be threaded through both.

## Questions / Unknowns

- Q: Confirm the §5.3 rule with the owner — serving count and weight are metadata only, not nutrition multipliers?
  Impact: If they were multipliers, `ManualDiaryEntry.nutritionFacts` and all meal totals would need reworking (much larger, higher-risk change).
  Assumption: Metadata only, per spec §5.3 — proceed on that.
  Status: OPEN — cheap to confirm; default is the spec's own recommendation.
- Q: Store fibre via the existing `dietaryFiber` embedded column, or add a dedicated `fibreGrams`?
  Impact: Reusing `dietaryFiber` = zero schema change and consistency with the rest of the app.
  Assumption: Reuse `dietaryFiber`. User-facing label uses Australian "Fibre"; internal name stays `dietaryFiber`.
  Status: OPEN — recommend reuse.
- Q: Should `servingCount` default to 1 be *persisted* on new entries, or left null and treated as 1 on read?
  Impact: Affects the migration default and promotion mapping.
  Assumption: Persist 1 for new entries; migrate old entries as null and treat null as 1 where a value is needed.
  Status: OPEN.

## Execution Steps

1. Extend the domain entity.
   - Why: Carry the new context on the manual entry.
   - Edits: `ManualDiaryEntry.kt` — add `description: String?`, `servingCount: Double?`, `weightGrams: Double?` (fibre travels inside `nutritionFacts.dietaryFiber`).
2. Extend the Room entity + migration.
   - Why: Persist the three new fields backward-compatibly.
   - Edits: `ManualDiaryEntryEntity.kt` — add nullable `description`, `servingCount`, `weightGrams` columns; bump `FoodYouDatabase.VERSION` to 33 and register `AutoMigration(32, 33)`; update the exported schema under `app/schemas/`.
   - Dependencies: step 1.
3. Thread the fields through the repository + DAO mapping.
   - Edits: `RoomManualDiaryEntryRepository.kt` entity↔domain mapping; `ManualDiaryEntryDao` needs no signature change (row-based).
   - Dependencies: step 2.
4. Add the fields to the shared form-state.
   - Why: Create and edit-historical both use it.
   - Edits: `QuickAddFormState.kt` — new `FormField`s: `description` (optional string), `fibre` (`Double?`, `nonNegativeFloatValidator`), `servingCount` (`Double?`, `positiveFloatValidator`), `weightGrams` (`Double?`, `positiveFloatValidator`); thread all into `isModified` and `isValid`; extend `rememberQuickAddFormState(...)` seed params.
   - Dependencies: step 1.
5. Render the fields in the form in §5.4 order.
   - Edits: `QuickAddForm.kt` — order: Name, Description, Servings, Weight (g), Energy, Protein, Fat, Carb, Fibre. Keep the existing energy auto-calculate toggle (fibre does not feed the 4-4-9 energy calc).
   - Dependencies: step 4.
6. Pass the fields through both view models.
   - Edits: `CreateQuickAddViewModel.addEntry(...)` and `UpdateQuickAddViewModel.updateEntry(...)` — carry description/servingCount/weightGrams and write fibre into `NutritionFacts.dietaryFiber`.
   - Dependencies: steps 1–5.
7. Tests.
   - Edits: unit tests per §10.3 (save with only existing fields; with all new fields; empty description; decimal fibre/servings/weight; reject negatives; reject zero servings; reject zero weight when supplied; migrate old entries; edit a migrated entry). A migration test under `app/src/commonTest/.../migration/`.
   - Dependencies: steps 1–6.

## Validation

### Automated Checks

- `./gradlew :app:testDebugUnitTest` (or the project's KMP test task) for the new Quick Add + migration tests.
- Room schema export diff shows only the three additive nullable columns.
- Build compiles (the `isModified`/`isValid` fan-out is the likely break point).

### Manual Checks

1. Create a Quick Add with all new fields; reopen and confirm every value persists and is editable.
2. Open a **pre-migration** historical Quick Add entry; confirm it loads with empty/defaulted new fields and saves without altering its original totals.
3. Confirm a day's total is unchanged after adding a servings/weight value to an existing entry (proves metadata-only semantics).

### Acceptance Criteria

- Description, fibre, servings, weight are saveable, persistent, and editable on new and historical entries.
- Validation rejects negative nutrients/fibre and zero/negative servings/weight; empty optionals save fine; kcal-vs-macro mismatch does **not** block saving.
- Existing Quick Add entries survive migration and keep their exact nutritional totals.
- Meal/day totals are unaffected by serving/weight values.

## Risk Mitigation

- Risk: Serving/weight accidentally treated as a nutrition multiplier → silent total corruption.
  Mitigation: Keep `ManualDiaryEntry.nutritionFacts` returning the stored total unchanged; a manual check asserts day totals are stable; unit test locks it.
- Risk: `isModified`/`isValid` not updated for new fields → broken save button / dirty detection.
  Mitigation: Both are exhaustive today; add fields explicitly and cover with a form-state test.
- Risk: Migration surprises (NOT NULL / default).
  Mitigation: All three columns nullable, no backfill; dedicated migration test; schema export reviewed.

## Evidence / References

- Planning input: 2026-07-25 Quick Add / diary recon (`QuickAddFormState.kt`, `ManualDiaryEntryEntity.kt`, `NutritionFacts.kt`, `FoodYouDatabase.kt`, validator/parser files) with `file:line` citations; spec §5, §7, §10.3.
- Unverified: exact Gradle test task name — confirm at execution (`./gradlew tasks`).
