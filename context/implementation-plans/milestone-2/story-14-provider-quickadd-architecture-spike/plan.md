# Plan: Provider & Quick Add Architecture Spike

## Metadata

- Task Type: `SPIKE`
- Status: `Draft`
- Owner: Jarryd Adaens
- Last Updated: 25 July 2026

## Linked Context

- Milestone: [context/milestones/milestone-2.md](../../../milestones/milestone-2.md)
- Story: [Story 14: Provider & Quick Add architecture spike](../../../milestones/milestone-2.md#story-14)
- Dictation source: [2026-07-25 Milestone 2 feature spec](../../../dictations-tier-0/2026-07-25_milestone-2_australian-providers-and-quickadd-spec.md) (Phase 1; §13 decisions)
- Related Plans: this spike **gates** [Story 15](../story-15-australian-food-composition-database-provider/plan.md), [Story 16](../story-16-foodswitch-provider/plan.md), [Story 17](../story-17-provider-update-check-refresh-ui/plan.md); its Quick-Add-relevant findings feed [Story 18](../story-18-quickadd-expansion-fields/plan.md) and [Story 19](../story-19-quickadd-promotion-workflow/plan.md)
- External Tooling: `WebSearch`/`WebFetch` for the FoodSwitch + AFCD feasibility check

## CER

- Complexity: 5
- Effort: 4
- Risk: 2
- Notes: Inline estimate. Most of the *code* investigation is already done (two read-only recon passes on 2026-07-25, captured below). The remaining weight is the **external** feasibility work (AFCD dataset acquisition, FoodSwitch access terms) and turning findings into a durable spec-of-record. Risk is low — the spike writes a document and edits no production code — with the one real hazard being an over-confident recommendation that later corrupts data, mitigated by grounding every claim in `file:line` evidence.

## Objective

Produce a durable technical note (a `context/wiki/` knowledge doc) that maps how Food You implements food providers, imports, local persistence, search, provider enablement, and the historical diary-snapshot model; confirms the Quick Add create/edit component and its nutrition semantics; verifies FoodSwitch data-access feasibility and AFCD dataset acquisition; and resolves the spec's §13 decisions — so Stories 15–19 can be executed without reconstructing intent or guessing at data-safety.

## Scope

### In Scope

- A written technical note covering: provider registration flow, the import pipeline (Swiss as the worked example), the persistence + provenance model, search + enablement, the diary-snapshot model, the Quick Add component + nutrition semantics, required schema migrations, reusable code to extract, and risks/blockers.
- **External feasibility**: confirm the AFCD (Australian Food Composition Database) dataset's canonical download URL, file format, licence, and a stable version/publication identifier; and determine whether FoodSwitch exposes a usable, permitted bulk-download or API (auth, personal-use terms, rate limits, barcode/nutrition coverage, version metadata).
- Explicit answers to the spec's §13 decision list, each with evidence or a documented open blocker.

### Out Of Scope

- Any production code, schema change, or UI. This spike writes a document only.
- Implementing either provider, the update UI, or the Quick Add changes — those are Stories 15–19.

## Non-Goals

- Deciding supermarket scraping (an explicit spec non-goal).
- Designing the master JSON data format (Milestone 1 Story 5) — unrelated; that is the owner's export store, not the app's provider store.

## Current Understanding

Two read-only recon passes on 2026-07-25 already answered most of the *code-side* spike questions. Recorded here so the note can be written from evidence; the spike's remaining job is external feasibility + AFCD acquisition + write-up.

**Provider architecture (code):**
- No unified provider interface. Identity is an enum pair: `common/domain/food/FoodSource.kt` (`FoodSource.Type { User, OpenFoodFacts, USDA, SwissFoodCompositionDatabase }`) mirrored by `common/infrastructure/room/FoodSourceType.kt` (+ `toDomain`/`toEntity`, exhaustive `when`, no `else`). Adding a provider fans out across both enums, `FoodSourceTypeConverter`, the search prefs, DI, navigation, and settings UI.
- Online providers (OFF, USDA): Ktor + Paging3 `RemoteMediator`s under `food/infrastructure/{openfoodfacts,usda}` and `food/search/infrastructure/...`; selected by a hard-coded `when(source)` in `food/search/domain/FoodSearchUseCase.kt:65-75`.
- **Swiss bulk import is the closest analog and the reuse seam**: `importexport/swissfoodcompositiondatabase/...` reads a *bundled* CSV (`Res.readBytes(...)`, not a download) and feeds the shared engine `importexport/domain/usecase/ImportCsvProductUseCase.kt` (transactional `withTransaction`, RFC CSV parse, `ProductField` column mapping, `insertUniqueProduct`, `FoodHistory.Imported`).
- Persistence: single shared `food/infrastructure/room/ProductEntity.kt` (`Product` table) for all providers. Provenance = `sourceType` + `sourceUrl` only. **No source-record-ID column; no `barcode`/`sourceType` index; barcode is `String?` searched with `LIKE '%…%'`.**
- Search: SQLite FTS4 (`ProductFts`), per-query `source` filter in `FoodSearchDao`. Enable/disable flags live in DataStore (`DataStoreFoodSearchPreferencesRepository`) and exist **only for OFF + USDA**; local providers (Swiss/Manual) are always searchable because their rows just sit in `Product`.
- Transactionality: imports are transactional, but **there is no delete-by-source** (`DELETE FROM Product WHERE sourceType = :source` does not exist) and **no dataset-version metadata table**. `insertUniqueProduct` dedups on `(name, brand, barcode, sourceType)` — append-only, won't update changed nutrients.
- Migrations: `app/infrastructure/room/FoodYouDatabase.kt` (`VERSION = 32`, `exportSchema = true`, schemas under `app/schemas/`); manual migrations in `app/infrastructure/room/migration/`.
- No `WorkManager`/background or resumable download infra exists anywhere.

**Quick Add / diary (code):**
- Quick Add captures exactly `name, proteins, carbohydrates, fats, energy`. Shared form/state/screen (`app/ui/food/diary/quickadd/QuickAddForm.kt`, `QuickAddFormState.kt`, `QuickAddScreen.kt`) reused by both `CreateQuickAddScreen` and `UpdateQuickAddScreen` (different thin wrappers + view models).
- Quick Add persists as a distinct entity `ManualDiaryEntryEntity` (`fooddiary/infrastructure/room/`), with embedded `Nutrients/Vitamins/Minerals`. Its nutrition is the **absolute total for the entry** — no serving/weight/quantity scaling (contrast `FoodDiaryEntry.nutritionFacts = food.nutritionFacts * weight/100`).
- **Diary entries are already immutable snapshots** (spec §7.5 satisfied): product/recipe entries were unlinked from live rows by migration 25→26 (`UnlinkDiaryMigration`) into `DiaryProduct`/`DiaryRecipe` tables; manual entries embed their own nutrient columns.
- **Fibre already exists**: `common/domain/food/NutritionFacts.kt` has `dietaryFiber/solubleFiber/insolubleFiber`, already embedded on `ManualDiaryEntryEntity` — wiring fibre into Quick Add needs **no schema change**.
- Recipe nutrition is 100% derived from ingredients (`food/domain/entity/Recipe.kt`), with no manual-override field; seeding a recipe from a single estimate is architecturally hard.

**External (not yet done — the spike's real remaining work):**
- AFCD dataset canonical URL / format / licence / version identifier — believed to be an FSANZ-published downloadable workbook, **to be confirmed**.
- FoodSwitch bulk/API access terms — believed restricted (The George Institute), **to be confirmed**; this decides whether Story 16 is buildable or a documented blocker.

## Questions / Unknowns

- Q: Does FoodSwitch expose a usable, permitted bulk download or API for personal use?
  Impact: Decides whether Story 16 is implemented or recorded as a concrete external blocker.
  Assumption: Access is restricted and Story 16 will likely be blocked — to be verified, not assumed final.
  Status: OPEN
- Q: What is the AFCD dataset's canonical download URL, file format, licence, and strongest stable version identifier (explicit version vs publication date)?
  Impact: Grounds Story 15's parser, mapper, and Story 17's version-comparison strategy.
  Assumption: An FSANZ-published downloadable workbook (Excel/CSV) with a release name/date usable as the version identifier.
  Status: OPEN
- Q: Should the two AU providers be user-toggleable (like OFF/USDA) or always-on locals (like Swiss)?
  Impact: Determines whether new `FoodSearchPreferences` flags + DataStore keys + toggle UI are in scope for Story 15.
  Assumption: Toggleable — the spec requires enable/disable checkboxes and search participation control.
  Status: OPEN — product decision for the owner.
- Q: For safe full-refresh, do we add a source-record-ID column + delete-by-source, or replace the whole `Product` provider partition another way?
  Impact: Central data-safety design for Stories 15 and 17.
  Assumption: Add a nullable `sourceRecordId` column and a `DELETE FROM Product WHERE sourceType = :source` DAO, guarded so `User` (custom) rows are never touched; account for diary FK cascade by relying on the existing snapshot tables.
  Status: OPEN — confirm in the note with a migration sketch.

## Execution Steps

1. Consolidate the two 2026-07-25 recon passes into a single evidence base.
   - Why: The note must be one source-grounded artifact, not two chat transcripts.
   - Edits: none yet (gathering).
2. Run the external feasibility check.
   - Why: The only spike questions the code cannot answer; gates Stories 15 and 16.
   - Edits: none — `WebSearch`/`WebFetch` for AFCD (FSANZ) dataset acquisition + FoodSwitch access terms; capture URLs, formats, licences, version identifiers, and access limitations verbatim.
3. Write `context/wiki/foodyou-provider-architecture.md`.
   - Why: The spike's deliverable and the spec-of-record for Stories 15–17.
   - Edits: new wiki page — provider registration flow, the Swiss-worked import pipeline, persistence/provenance, search + enablement, the reusable-pipeline recommendation (extend `ImportCsvProductUseCase` + add download→temp→validate→replace + version metadata), the enum/switch fan-out checklist, required migrations, and risks/blockers. Link from `context/wiki/home.md`.
4. Write (or fold into the same note) the Quick Add + diary-snapshot findings.
   - Why: Grounds Stories 18–19; records that snapshots and fibre already exist.
   - Edits: a Quick Add section (or a second short wiki doc) covering the entity model, TOTAL nutrition semantics, snapshot proof, fibre reuse, and the recipe-seeding constraint.
5. Answer the spec's §13 decision list explicitly.
   - Why: The spec requires each decision resolved, not guessed.
   - Edits: a "§13 decisions" table in the note, each row Answered-with-evidence or Blocked-with-reason.
6. Update the milestone's Deferred/Follow-up and story dependencies to reflect resolved unknowns.
   - Why: Keep maintained context in sync (the snapshot prerequisite is already recorded resolved).
   - Edits: `context/milestones/milestone-2.md` if any story's dependency changes (e.g. Story 16 confirmed blocked).

## Validation

### Automated Checks

- None (no code). Optionally run the repo's markdown lint/`oracle-generate-stats` on the new wiki doc.

### Manual Checks

1. Every code claim in the note carries a `file:line` citation that still resolves in the tree.
2. The AFCD download URL actually returns a dataset of the stated format; the FoodSwitch verdict cites the source page/terms.
3. A reader can execute Story 15 from the note alone, without this plan or chat history.

### Acceptance Criteria

- The technical note exists under `context/wiki/`, is linked from the wiki home, and covers every §13 decision.
- FoodSwitch feasibility is a clear yes-with-mechanism or no-with-documented-blocker.
- AFCD acquisition is concrete: URL, format, licence, version identifier.
- The reusable-provider-import-pipeline recommendation names the exact files to extend vs create.
- No production code changed.

## Risk Mitigation

- Risk: A confident-but-wrong recommendation later corrupts logged history.
  Mitigation: Every design claim grounded in `file:line`; the note's data-safety section leans on the already-proven snapshot model and an explicit `User`-rows-untouched guard for delete-by-source.
- Risk: External sources (AFCD/FoodSwitch) change or gate access after the note is written.
  Mitigation: Record retrieval dates and exact URLs; frame version identifiers as "confirm at implementation time."
- Risk: Scope creep into implementation.
  Mitigation: Hard rule — this spike edits no production code; findings flow to Stories 15–19.

## Evidence / References

- Planning inputs: two read-only recon passes (2026-07-25) over `food/`, `food/search/`, `importexport/`, `fooddiary/`, and Room migrations (provider architecture; Quick Add + diary-snapshot model), each citing `file:line`. Spec: `context/dictations-tier-0/2026-07-25_milestone-2_australian-providers-and-quickadd-spec.md`.
- Unverified claims: AFCD source (FSANZ workbook) and FoodSwitch access terms are not yet confirmed — that verification is Step 2 of this plan.
