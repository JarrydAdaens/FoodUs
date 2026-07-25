# Plan: Australian Food Composition Database Provider

## Metadata

- Task Type: `FEATURE`
- Status: `Draft`
- Owner: Jarryd Adaens
- Last Updated: 25 July 2026

## Linked Context

- Milestone: [context/milestones/milestone-2.md](../../../milestones/milestone-2.md)
- Story: [Story 15: Australian Food Composition Database provider](../../../milestones/milestone-2.md#story-15)
- Dictation source: [2026-07-25 Milestone 2 feature spec](../../../dictations-tier-0/2026-07-25_milestone-2_australian-providers-and-quickadd-spec.md) (Phase 2)
- Related Plans:
  - [Story 14 (spike)](../story-14-provider-quickadd-architecture-spike/plan.md) — **blocks this**: settles the AFCD dataset acquisition, the reusable-pipeline shape, and the delete-by-source/source-record-ID design.
  - [Story 16 (FoodSwitch)](../story-16-foodswitch-provider/plan.md) and [Story 17 (update UI)](../story-17-provider-update-check-refresh-ui/plan.md) build on the pipeline this story extracts.

## CER

- Complexity: 7
- Effort: 7
- Risk: 6
- Notes: Inline estimate. The high numbers come from the **infrastructure gap**: Food You has a reusable *parse→map→persist* engine but no *download→temp→validate→replace* half, no source-record-ID column, and no delete-by-source. This story builds that missing half, plus the enum/switch fan-out across many files, plus a schema migration touching the FK-coupled `Product` table. Risk 6 because a naive replace can drop or duplicate rows and stress the FK cascade.

## Objective

Add `australian_food_composition_database` as a first-class, user-selectable provider whose dataset is downloaded, validated, imported into local storage, and searched offline — reusing (and extracting) a shared provider-import pipeline, retaining provenance on every row, and never mutating historical diary entries on refresh.

## Scope

### In Scope

- Extend the Swiss import engine into a reusable pipeline that adds: download-to-temp, validation, and transactional per-provider replace.
- New provider identity (`FoodSource.Type.AustralianFoodCompositionDatabase` + Room mirror + converters).
- AFCD parser + `ProductField` mapper + deterministic unit normalization (kcal, grams, per-100 g basis), test-covered.
- Schema additions needed for safe replace: a nullable `sourceRecordId` on `ProductEntity` and a `DELETE FROM Product WHERE sourceType = :source` DAO (guarded so `User` rows are untouched); migration to VERSION 33+.
- Provider enable/disable (new `FoodSearchPreferences` flag + DataStore key + settings toggle) and search participation.
- Import/download/update state UI (state model + progress + error), mirroring the Swiss/Manual view-model patterns.

### Out Of Scope

- Update-check "is there a newer dataset" comparison + refresh UI — Story 17 (this story delivers the first import and the replace primitive it will reuse).
- FoodSwitch — Story 16.
- Quick Add — Stories 18/19.

## Non-Goals

- Cross-provider duplicate merging (spec non-goal) — AFCD rows coexist with OFF/USDA, each with its own provenance.
- Barcodes: AFCD is a composition database; import barcodes only if the source actually supplies them (do not fabricate).

## Current Understanding

From 2026-07-25 provider recon (`file:line` where load-bearing) — Story 14 will formalize this:

- Reuse seam: `importexport/domain/usecase/ImportCsvProductUseCase.kt` — transactional (`withTransaction`) parse→map (`List<ProductField>`)→`insertUniqueProduct`→`FoodHistory.Imported`. Swiss (`importexport/swissfoodcompositiondatabase/...`) drives it from a **bundled** CSV via `Res.readBytes` — the AFCD provider must instead supply a `Flow<Byte>` from a **download**.
- Column vocabulary: `importexport/domain/entity/ProductField.kt` (49 fields) + `CsvHeaders.kt`; `ProductField.SourceUrl` for per-row provenance.
- Persistence: single `food/infrastructure/room/ProductEntity.kt` (`Product` table); provenance = `sourceType` + `sourceUrl` only — **no source-record-ID**, **no barcode/sourceType index**, barcode is `String?` via `LIKE '%…%'`. DAO `ProductDao.kt` `insertUniqueProduct` dedups on `(name,brand,barcode,sourceType)` — append-only, no update, **no delete-by-source anywhere**.
- Enum fan-out (all must be edited, exhaustive `when`, no `else`): `common/domain/food/FoodSource.kt`, `common/infrastructure/room/FoodSourceType.kt`, `FoodSourceTypeConverter.kt`; toggle path `food/search/domain/FoodSearchPreferences.kt` + `food/search/infrastructure/repository/DataStoreFoodSearchPreferencesRepository.kt`; DI `app/di/InitKoin.kt` + `app/ui/database/DatabaseModule.kt`; navigation `app/navigation/FoodYouAppNavHost.kt`; settings `app/ui/database/master/DatabaseSettingsScreen.kt` / `externaldatabases/ExternalDatabasesScreen.kt`.
- Migrations: `app/infrastructure/room/FoodYouDatabase.kt` (`VERSION = 32`, `exportSchema`, `app/schemas/`); manual migrations in `app/infrastructure/room/migration/`.
- No background/resumable download infra exists (Ktor `HttpClient` is available; `WorkManager` is not).
- Search: FTS4 `ProductFts`; per-query `source` filter in `food/search/infrastructure/room/FoodSearchDao.kt`; local-provider rows are searchable whenever their source is queried.
- Assumptions/constraints: AFCD is (believed) an FSANZ-published downloadable workbook — **Story 14 confirms URL/format/licence/version**. Diary entries are already snapshots, so replacing provider rows will not rewrite history, but FK-referenced rows (`DiaryProduct` is a separate snapshot, so the live `Product` FK exposure is limited) must be checked before delete.

## Questions / Unknowns

- Q: AFCD exact download URL, file format (xlsx vs CSV), licence, and version identifier?
  Impact: Parser choice and Story 17's version comparison.
  Assumption: FSANZ workbook, convertible to the `ProductField` CSV vocabulary; release name/date as version.
  Status: OPEN — resolved by Story 14.
- Q: Add `sourceRecordId` to `ProductEntity`, or rely on delete-all-by-source before re-import?
  Impact: Idempotent re-import + safe replace + future diary provenance.
  Assumption: Add nullable `sourceRecordId`; on import, delete-by-source then insert (full replace), inside one transaction.
  Status: OPEN — confirm in Story 14's note.
- Q: Is the AFCD dataset small enough for the current whole-file-in-memory, single-giant-transaction import, or does it need streaming/chunking?
  Impact: Memory + transaction duration; could force a pipeline change.
  Assumption: Feasible in one transaction initially; revisit if the dataset is tens of MB.
  Status: OPEN.
- Q: Add a `barcode` index now?
  Impact: AFCD has few/no barcodes, but bulk data slows `LIKE`/`EXISTS`.
  Assumption: Defer the index unless profiling shows a problem; note it for Story 16 (FoodSwitch, barcode-heavy).
  Status: OPEN.

## Execution Steps

1. Land the schema groundwork.
   - Why: Safe replace needs it before any import runs.
   - Edits: `ProductEntity.kt` add nullable `sourceRecordId`; `ProductDao.kt` add `@Query("DELETE FROM Product WHERE sourceType = :source")`; bump `FoodYouDatabase.VERSION` (→33) + migration + exported schema.
2. Extract the reusable download/validate half of the pipeline.
   - Why: Avoid copying Swiss twice (spec's preferred direction).
   - Edits: a new `importexport` infrastructure piece: Ktor download → temp file → `Flow<Byte>`; a validation stage (format/required-column/row-count sanity) ahead of the existing `ImportCsvProductUseCase`; a transactional wrapper that does delete-by-source then import.
3. Add the provider identity.
   - Edits: the full enum fan-out list from Current Understanding.
4. Implement the AFCD provider slice.
   - Edits: `importexport/australianfoodcompositiondatabase/domain/{Import…UseCase, …Repository}.kt`, `infrastructure/…Repository.kt` (download source), `…Module.kt`; the `ProductField` mapper + deterministic unit normalization with tests.
5. Wire enablement + settings + navigation UI.
   - Edits: `FoodSearchPreferences` flag + DataStore key; a provider entry/toggle in the database settings UI; a `app/ui/database/australianfoodcompositiondatabase/{Screen,ViewModel,UiState,Module}.kt` with import/progress/error states.
6. Tests.
   - Edits: unit tests (unit conversion, decimal parse, source-ID mapping, missing optional fields, invalid required fields, duplicate source IDs, enablement filtering) and integration tests (initial import, re-import same version, import newer, rollback after failure, restart after import, enable/disable, search before/after, coexistence with existing providers, provider-specific replacement, preservation of custom foods + diary history) per §10.1–10.2.

## Validation

### Automated Checks

- KMP unit + Room integration tests for the provider and the replace primitive.
- Schema export diff shows only `sourceRecordId` (+ index if added).
- Build compiles (the exhaustive-`when` fan-out catches missed enum sites).

### Manual Checks

1. Enable the provider, run a first import, search an Australian food, confirm results carry AFCD provenance.
2. Disable the provider; confirm its foods vanish from search but the data is retained; re-enable without re-downloading.
3. Re-import; confirm only AFCD rows are replaced and custom products + diary history are untouched.
4. Force a corrupt download; confirm the previous dataset stays intact and an error is surfaced.

### Acceptance Criteria

- Provider visible in settings; enable/disable controls search participation without deleting data or requiring re-download.
- First import succeeds; units correctly normalized; every row retains provider identity (+ source record id).
- Refresh/replace touches only AFCD rows; historical diary entries and custom foods are unchanged.
- A failed import leaves the last-good dataset active.

## Risk Mitigation

- Risk: delete-by-source or FK cascade drops referenced/custom rows.
  Mitigation: `DELETE` filters strictly on the AFCD `sourceType` (never `User`); diary uses separate snapshot tables; integration test asserts custom + diary rows survive a replace.
- Risk: Non-deterministic unit conversion → wrong nutrition.
  Mitigation: Pure conversion functions with exhaustive unit tests (spec §10.1).
- Risk: Large dataset blows memory / holds a long transaction.
  Mitigation: Measure real AFCD size in Story 14; chunk the import if needed rather than one giant transaction.
- Risk: Missed enum-fan-out site (esp. DataStore/UI, not compile-checked).
  Mitigation: Use Story 14's fan-out checklist; smoke-test the toggle and settings entry.

## Evidence / References

- Planning input: 2026-07-25 provider recon (`ImportCsvProductUseCase.kt`, `ProductEntity.kt`, `ProductDao.kt`, `FoodSource.kt`, `FoodSourceType.kt`, `FoodSearchDao.kt`, `DataStoreFoodSearchPreferencesRepository.kt`, `FoodYouDatabase.kt`) with `file:line`; spec Phase 2, §7.3–7.4, §10.1–10.2.
- Unverified: AFCD source specifics (URL/format/licence/version) and dataset size — resolved by Story 14 before this executes.
