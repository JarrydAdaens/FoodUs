---
name: provider-quickadd-architecture
description: Knowledge doc — how Food You implements food providers, bulk imports, local persistence/provenance, search + enablement, the immutable diary-snapshot model, and the Quick Add component; plus AFCD/FoodSwitch data-access feasibility and the resolved Section 13 decisions. Spec-of-record for Milestone 2 Stories 15-19.
metadata:
  version: "1.0"
  owner: "Jarryd Adaens"
  repo: "FoodYou (fork of maksimowiczm/FoodYou)"
---

# Food You — Provider & Quick Add Architecture

> Spike deliverable for **Milestone 2 Story 14**. This is the authoritative technical note for
> **Stories 15-19** (AFCD provider, FoodSwitch provider, provider update UI, Quick Add expansion,
> Quick Add promotion). Every code claim below was read against the tree on 2026-07-25; every
> external claim cites a URL fetched the same day. [Back to wiki home](home.md)

## How to read the citations

All source lives under the KMP module base:

```
app/src/commonMain/kotlin/com/maksimowiczm/foodyou/
```

Paths below are written **relative to that base** unless prefixed with `androidMain/` (the Android
source set) or `app/` (build/infra outside the package). Line numbers are current as of the spike.

---

## 1. TL;DR for the implementer

- **No provider interface exists.** Provider identity is a hard-coded 4-member enum pair
  (`FoodSource.Type` ↔ `FoodSourceType`). Adding a provider fans out across ~8 sites. Story 15 must
  either accept that fan-out or introduce a light provider-definition seam.
- **One shared `Product` table** holds every provider's rows. Provenance is only `sourceType` +
  `sourceUrl` — **no source-record-ID column and no barcode/sourceType index.**
- **The Swiss import is the reuse seam.** `ImportCsvProductUseCase` is already a transactional,
  column-mapped bulk-insert engine. It is reusable as the *insert* core; it lacks download,
  staging, delete-by-source, version metadata, and FTS-rebuild — those are the pieces Story 15 adds.
- **Diary entries are already immutable snapshots** (spec §7.5 satisfied). No snapshot-conversion
  work is needed before safe provider refreshes.
- **Quick Add nutrition is an absolute total** for the entry (no serving/weight scaling). Fibre
  already exists in the domain model and is already embedded on the Quick Add entity — adding fibre
  is **UI-only, no migration**.
- **AFCD is feasible** (public CC BY-SA download). **FoodSwitch is blocked** (no public
  API/bulk download; licence forbids reproducing the data). See §9.

---

## 2. Sources of truth

| Concern | File(s) |
| --- | --- |
| Provider identity (domain) | `common/domain/food/FoodSource.kt` |
| Provider identity (Room) | `common/infrastructure/room/FoodSourceType.kt`, `FoodSourceTypeConverter.kt` |
| Online provider search | `food/search/infrastructure/{openfoodfacts,usda}/*RemoteMediator.kt`; `food/search/domain/FoodSearchUseCase.kt` |
| Swiss bulk import | `importexport/swissfoodcompositiondatabase/**` |
| Shared import engine | `importexport/domain/usecase/ImportCsvProductUseCase.kt`; `importexport/domain/entity/ProductField.kt` |
| Product persistence | `food/infrastructure/room/ProductEntity.kt`, `ProductDao.kt` |
| Search / FTS | `food/infrastructure/room/ProductFts.kt`; `food/search/infrastructure/room/FoodSearchDao.kt` |
| Enablement prefs | `food/search/domain/FoodSearchPreferences.kt`; `food/search/infrastructure/repository/DataStoreFoodSearchPreferencesRepository.kt` |
| Database + migrations | `app/infrastructure/room/FoodYouDatabase.kt`; `app/infrastructure/room/migration/**` |
| Diary snapshot | `androidMain/.../migration/UnlinkDiaryMigration.android.kt`; `food/infrastructure/room/DiaryProductEntity.kt` |
| Quick Add | `app/ui/food/diary/quickadd/{QuickAddForm,QuickAddFormState,QuickAddScreen,CreateQuickAddScreen,UpdateQuickAddScreen}.kt` |
| Quick Add entity | `fooddiary/infrastructure/room/ManualDiaryEntryEntity.kt` |
| Diary weight scaling | `fooddiary/domain/entity/FoodDiaryEntry.kt` |
| Nutrition model | `common/domain/food/NutritionFacts.kt` |
| Recipe model | `food/domain/entity/Recipe.kt`; `food/infrastructure/room/RecipeEntity.kt` |
| Provenance UI | `app/ui/food/component/FoodSource.kt` |

---

## 3. Provider architecture

### 3.1 Registration model — enum pair, no interface

There is **no provider interface, abstract class, or service contract**. A provider is an enum member:

- **Domain:** `common/domain/food/FoodSource.kt:3-10` — `data class FoodSource(val type: Type, val url: String? = null)` with nested `enum class Type { User, OpenFoodFacts, USDA, SwissFoodCompositionDatabase }`.
- **Room:** `common/infrastructure/room/FoodSourceType.kt:5-10` — mirror `enum class FoodSourceType`. `toDomain()`/`toEntity()` (lines 12-26) are exhaustive `when` with **no `else`** — so adding a member forces every mapping to be updated (a useful compile-time safety net).
- **Converter:** `FoodSourceTypeConverter.kt` persists to `Int` via `FoodSourceTypeSQLConstants` (`USER=0, OPEN_FOOD_FACTS=1, USDA=2, SWISS_FOOD_COMPOSITION_DATABASE=3`, lines 29-34). The `Int → enum` direction *does* have `else -> error(...)` (line 25) because the input is an arbitrary Int.

### 3.2 The enum fan-out checklist (what "add a provider" touches)

Adding `AustralianFoodCompositionDatabase` (and later `FoodSwitchAustralia`) requires edits at:

1. `FoodSource.Type` (domain enum).
2. `FoodSourceType` (Room enum) + both `toDomain`/`toEntity` branches.
3. `FoodSourceTypeConverter` / `FoodSourceTypeSQLConstants` (new stable Int).
4. `FoodSearchUseCase.remoteMediatorFactory` `when` (only if the provider fetches remotely; AFCD is a local bulk import, so **no** remote mediator — it falls to `else -> null` like Swiss).
5. `FoodSearchPreferences` + DataStore keys (only if the provider is user-toggleable — see §7).
6. DI wiring for the new import use case.
7. Settings/provider UI (list row, enable checkbox, update controls — Story 17).
8. Provenance UI: `app/ui/food/component/FoodSource.kt` `Icon(...)` + `stringResource()`.

### 3.3 Online vs local providers

- **Online (OFF, USDA):** Ktor data sources + Paging3 `RemoteMediator`s (`food/search/infrastructure/openfoodfacts/OpenFoodFactsRemoteMediator.kt:24`, `usda/USDARemoteMediator.kt:23`). Selected by a hard-coded `when(source)` in `food/search/domain/FoodSearchUseCase.kt:65-75`: only `OpenFoodFacts` (if enabled) and `USDA` (if enabled) get a mediator; everything else → `else -> null`. Mediators write rows via `insertUniqueProduct(...)` and record `FoodHistory.Downloaded`.
- **Local (User, Swiss):** no mediator; their rows simply sit in `Product` and are always searched locally. **AFCD belongs in this local family** — a bulk import, not a per-query network call. This matches the spec's offline-first goal (§2.2).

---

## 4. Import pipeline (Swiss = the worked example)

### 4.1 Swiss reads a bundled file, not a download

`importexport/swissfoodcompositiondatabase/infrastructure/ComposeSwissFoodCompositionDatabaseRepository.kt:20-26`
reads a **bundled** CSV via `Res.readBytes("files/swiss-food-composition-database/data.csv")` (+ de/fr/it
variants) and exposes it as `Flow<Byte>`. There is **no HTTP** — the dataset ships inside the APK.
This is the single biggest gap vs. Milestone 2: **no download infrastructure exists anywhere** (see §4.4).

### 4.2 The shared engine — `ImportCsvProductUseCase`

`importexport/domain/usecase/ImportCsvProductUseCase.kt` is the reusable core:

- **Transactional:** `transactionProvider.withTransaction { ... }` wraps the entire parse + insert (line 53).
- **Parse:** streams `Flow<Byte>` through `csvParser.parse(stream)` (line 54, RFC-style CSV); optional header drop `if (skipHeader) it.drop(1)` (line 58).
- **Column mapping:** a caller-supplied ordered `List<ProductField>` is zipped against each row —
  `mapper.zip(line).associate { (field, value) -> field to value }` (line 76); a size mismatch is a
  hard `error(...)` (line 73).
- **Insert:** `insertUniqueProduct(...)` (lines 172-183); on a real insert it records
  `FoodHistory.Imported` (lines 185-190).

Swiss drives it in `.../domain/ImportSwissFoodCompositionDatabaseUseCase.kt:27-82` with
`source = FoodSource.Type.SwissFoodCompositionDatabase` and a fixed `ProductField` column order.
`ProductField` (`importexport/domain/entity/ProductField.kt`) already includes
`DietaryFiber, SolubleFiber, InsolubleFiber`.

### 4.3 Append-only dedup — no update, no delete-by-source

`insertUniqueProduct` (`food/infrastructure/room/ProductDao.kt:38-77`) checks existence on
**(name, brand, barcode, sourceType)** (`existsProductByNameAndBrand`, lines 43-46) and inserts
only if absent, else returns null. It is **append-only**: it will not update changed nutrients on
re-import, and there is **no `DELETE FROM Product WHERE sourceType = :source`** anywhere in runtime
code (the only bulk delete is a one-time legacy migration `LegacyMigrations.MIGRATION_7_8`,
`app/infrastructure/room/migration/LegacyMigrations.kt:187-219`, which purges *unused* OFF rows).
`ProductDao` otherwise exposes only per-row `@Delete deleteProduct(product)` (line 36).

**Consequence for Story 15/17:** a true "replace this provider's dataset" needs a new,
`User`-guarded delete-by-source DAO run *inside* the import transaction (see §8.2).

### 4.4 No background/download infrastructure

Repo-wide grep for `WorkManager|androidx.work|OneTimeWorkRequest|CoroutineWorker` matches **only
planning docs — zero hits in `app/src`**. Provider fetching today is synchronous Paging3
`RemoteMediator.load()` during active search. Milestone 2's download-to-temp → validate → replace
flow (spec §4 import safety) is **net-new** and must be built (Story 15 foundation, Story 17 UI).

### 4.5 Progress & error surfacing today

Import is a single transactional call with no progress channel; errors surface as thrown
exceptions / `error(...)`. Story 17's progress + last-error requirements have no existing plumbing
to reuse and will need a small state/result type.

---

## 5. Persistence & provenance

- **One shared table.** `food/infrastructure/room/ProductEntity.kt:11` — `@Entity(tableName = "Product")`.
  Every provider (Swiss import, OFF/USDA download, manual creation) inserts here; `FoodYouDatabase.kt:55`
  lists a single `ProductEntity`. Custom products are `sourceType = User` rows in the **same** table.
- **Provenance = `sourceType` + `sourceUrl` only** (`ProductEntity.kt:23-24`). **No source-record-ID
  column** — the app cannot currently point at "AFCD food 12345". Spec §7.4 wants one; Story 15
  should add a nullable `sourceRecordId` (see §8.1).
- **Barcode is `String?`** (`ProductEntity.kt:15`), and `ProductEntity` declares **no `indices` at
  all** — no barcode index, no sourceType index. Barcode is matched with partial
  `p.barcode LIKE '%' || :barcode || '%'` (`FoodSearchDao.kt:159,174`); the *recent-food* path uses
  exact `p.barcode = :barcode` (lines 340,356). At provider scale (AFCD ≈1,588 rows; a packaged-food
  set would be far larger) an index on `(sourceType)` and `(barcode)` is advisable.

---

## 6. Search & enablement

- **FTS4.** `food/infrastructure/room/ProductFts.kt:7-13` — `@Fts4(contentEntity = ProductEntity::class,
  tokenizer = unicode61, tokenizerArgs=["remove_diacritics=2"])`, columns `name, brand, note`. Because
  it is a **content-backed** FTS table, a bulk row replacement (delete-by-source + re-insert) must
  keep the FTS shadow table in sync — plan an FTS rebuild/repopulate step after a full refresh
  (spec §4.14 / Section 13 item 14).
- **Per-query source filter.** Every `FoodSearchDao` query takes `source: FoodSourceType?` and filters
  `:source IS NULL OR p.sourceType = :source` (e.g. lines 33, 91, 128); FTS match is
  `ProductFts MATCH :query || '*'`.
- **Enablement flags exist only for OFF + USDA.** `FoodSearchPreferences.kt` models only
  `OpenFoodFacts(enabled)` and `Usda(enabled, apiKey)`; `DataStoreFoodSearchPreferencesRepository.kt:35-37`
  stores only `food:use_open_food_facts`, `food:use_usda`, `food:usda_api_key` (both flags default
  `false`). **Local providers (Swiss/User) have no enable flag and are always searchable** — their
  rows just live in `Product`. To make AFCD toggleable (spec requires an enable/disable checkbox),
  Story 15 must add a preference flag **and** teach the search query to exclude a disabled local
  source (today "disabled" only means "no remote mediator", which does nothing for local rows).
- **Provenance is shown in results.** `app/ui/food/component/FoodSource.kt` renders a per-source
  `Icon(...)` (Person / OFF logo / USDA logo / `"CH"` text badge, lines 17-43) and localized
  `stringResource()` names (lines 45-53). A new AU provider needs an icon/badge + a string here.

---

## 7. Provider enablement model — decision needed

The two AU providers *could* be modeled as **always-on locals** (like Swiss) or **user-toggleable**
(like OFF/USDA). The spec (Phase 2/3 user-visible behaviour) explicitly requires an enable/disable
checkbox and search-participation control, so **toggleable is required**. That means Story 15 adds a
`FoodSearchPreferences` flag + DataStore key per AU provider, and — unlike OFF/USDA where the flag
gates a remote mediator — the flag must gate the **local search query** (add an "enabled local
sources" filter to `FoodSearchDao`). This is a genuine new behaviour, not a copy of the OFF path.

---

## 8. Persistence changes required for Stories 15/17

None of these are needed for the Quick Add stories (18/19) — see §10.

### 8.1 Provenance columns (spec §7.4)

Add to `ProductEntity` a nullable `sourceRecordId: String?` (the source's own food id) so a provider
dataset can be replaced deterministically and results stay inspectable. `sourceVersion`/batch id can
live in the provider-metadata table (§8.3) rather than per row, to avoid widening every row.

### 8.2 Delete-by-source, `User`-guarded (safe full refresh)

Add a DAO `DELETE FROM Product WHERE sourceType = :source` and run it **inside** the import
transaction, immediately before re-inserting the new dataset. It must be **impossible** to pass
`User` (custom products) — hard-guard the call site so only provider source types are ever purged.
Because diary entries are already snapshots (§9-diary), deleting `Product` rows cannot corrupt
history; there is no live FK from diary to `Product` after the 25→26 migration.

### 8.3 Provider metadata table (spec §7.3)

New `ProviderMetadata` table keyed by provider id: `enabled`, `installed`, `installedVersion`,
`publicationDate`, `checksum`, `importDate`, `lastSuccessfulCheck`, `lastAttemptedCheck`,
`lastError`, `importState`. There is **no** such table today (`datasetVersion` appears only in
planning docs). This is what Story 17's UI reads.

### 8.4 Enum + preference additions

Per the §3.2 fan-out: new `FoodSourceType`/`FoodSource.Type` member(s), new
`FoodSourceTypeSQLConstants` Int, new `FoodSearchPreferences` flag(s), provenance UI entries.

### 8.5 FTS rebuild after refresh

After a bulk delete-by-source + re-insert, repopulate/rebuild the content-backed `ProductFts`
(Section 13 item 14). Confirm behaviour with a test that searches an imported row after a refresh.

---

## 9. External data-access feasibility

### 9.1 Australian Food Composition Database (AFCD) — FEASIBLE

Verified 2026-07-25.

- **Source:** FSANZ, [AFCD downloadable files](https://www.foodstandards.gov.au/science-data/food-nutrient-databases/afcd/australian-food-composition-database-download-excel-files).
- **Current release:** **Release 3**, page last updated 23 December 2025; ~1,588 foods.
- **Format:** Excel `.xlsx` (not CSV). Files: *Food Details* (1.1 MB), *Nutrient profiles* (2 MB),
  *Nutrient details* (1.1 MB), *Recipes* (107 KB), *Food group information* (886 KB),
  *Reference List* (68 KB), plus an *About* PDF. Nutrient data is per 100 g (per 100 mL for liquids).
- **Access:** direct download, **no registration, no key**. Verified with a `HEAD` on the Food
  Details file — `HTTP/1.1 200 OK`, `Content-Type: application/vnd.openxmlformats-officedocument.spreadsheetml.sheet`,
  `Content-Length: 1137444`, `Last-Modified: Mon, 22 Dec 2025 23:20:49 GMT`:
  `https://www.foodstandards.gov.au/sites/default/files/2025-12/AFCD%20Release%203%20-%20Food%20Details.xlsx`
- **Licence:** [AFCD Data User Licence Agreement](https://www.foodstandards.gov.au/science-data/monitoringnutrients/afcd/datauserlicenceagreement)
  — **Creative Commons Attribution-ShareAlike 3.0 Australia**. Redistribution, adaptation, and
  bundling into a Collection (an app) are **permitted**. Requirements: attribute "Food Standards
  Australia New Zealand", include the **Limitation of Data Statement** with every copy, identify that
  changes were made, do not imply FSANZ endorsement, do not use the FSANZ logo. ShareAlike applies to
  derivative works. Commercial use is not prohibited.
- **Version identifier:** strongest = the explicit **release number** ("Release 3"); secondary =
  page/file publication date and the file `Last-Modified` header (checksum as a tertiary fallback).
  This maps cleanly onto the spec §4 version-comparison order.

**Implication for Story 15:** the shared CSV engine's `csvParser` is CSV-only; AFCD is `.xlsx`, so
Story 15 needs an **XLSX parser** (or a one-time convert-to-CSV step) feeding the same
`ProductField` mapper. The food id / description / energy / macro / fibre columns all map to existing
`NutritionFacts` + `ProductField` fields; energy needs kJ→kcal normalization (AFCD publishes both,
prefer the kcal column where present). No barcodes in AFCD — do **not** fabricate any.

### 9.2 FoodSwitch — BLOCKED (concrete blocker; Story 16 gate)

Verified 2026-07-25. FoodSwitch is a product of The George Institute for Global Health.

- **No public API and no public bulk download.** Web searches for a developer API/endpoint returned
  only the consumer apps, a research-partnership page, and a **commercial** "Food Data" service — no
  free or individual-developer data feed. The George Institute's own page states the data is
  *"licensed from time to time to individual Sponsors to agreed territories"* — i.e. a commercial
  licensing model, not open access
  ([FoodSwitch data page](https://www.georgeinstitute.org/our-research/areas/food-policy/foodswitch-data-on-the-worlds-packaged-foods)).
- **Terms of Use forbid reproducing the data.** [FoodSwitch Global Terms of Use](https://www.georgeinstitute.org/sites/default/files/2025-02/foodswitch-terms-of-use.pdf)
  (Global TC 10 02 2021):
  - **7.1** — licence is *"solely for your personal, non-commercial use."*
  - **7.2(a-b, e-f)** — must not reverse engineer, *"make any … derivative work from FoodSwitch,"*
    or use it to *"create a product, service, or software that is … competitive with or … a
    substitute for FoodSwitch."*
  - **9.2 / 9.3** — must not *"modify, copy, reproduce, republish … transmit or distribute the TGI
    Property"* nor *"store (in any medium) … or create a derivative work … for any other purpose"*
    without prior written permission.
  - **9.10** — *"Any unauthorised use, alteration or dissemination of the information or content on
    FoodSwitch is strictly prohibited."*
  - **9.12** — apart from fair dealing for private study/research, *"no part of FoodSwitch may be
    reproduced without the written permission of The George Institute."*
- **Barcodes/nutrition:** the app is barcode-driven and does carry nutrition panels, but there is no
  *permitted* mechanism to acquire that data in bulk for a local offline database.

**Verdict:** building a FoodSwitch provider that downloads/imports and stores the dataset locally is
**not achievable within the licence** for a personal fork. Bulk-extracting via the app's private
endpoints would breach 7.2/9.2/9.3/9.10. **Story 16 is blocked.** To unblock, the repo owner would
need a **written data-licence agreement** with The George Institute
(`foodswitch@georgeinstitute.org.au`); absent that, record Story 16 as a documented external blocker
and do **not** substitute scraped supermarket data (an explicit spec non-goal, §3).

---

## 10. Quick Add & the diary-snapshot model

### 10.1 Shared create/edit component

`app/ui/food/diary/quickadd/QuickAddScreen.kt` is the shared screen (takes a `QuickAddFormState`).
Both `CreateQuickAddScreen.kt:46` and `UpdateQuickAddScreen.kt:45` wrap it with the same
`rememberQuickAddFormState`, backed by `CreateQuickAddViewModel` / `UpdateQuickAddViewModel`. So the
promotion actions (Story 19) can be added **once** in the shared form and conditionally wired.

### 10.2 Captured fields & TOTAL nutrition semantics

The form captures **exactly**: name, proteins, carbohydrates, fats, energy
(`QuickAddForm.kt:36-66`; `QuickAddFormState.kt:35-41,186-206`, plus a derived
`autoCalculateEnergy`). Nutrition is stored as the **absolute total for the entry** — there is
**no serving/weight/quantity column** on the entity, and `CreateQuickAddViewModel.kt:36-42` inserts
the entered macros verbatim as a `NutritionFacts`.

Contrast the product/recipe diary path: `FoodDiaryEntry.kt:39` computes
`nutritionFacts = food.nutritionFacts * (weight / 100)` where `weight = food.weight(measurement)`
(line 33). Quick Add deliberately **bypasses** that scaling. Per spec §5.3, Milestone 2 must keep
this TOTAL semantics — the new serving/weight fields are **quantity context only**, not multipliers.

### 10.3 Quick Add persistence

`fooddiary/infrastructure/room/ManualDiaryEntryEntity.kt:25-42` embeds `Nutrients`, `Vitamins`,
`Minerals` directly (`@Embedded`). Migration **32→33** `PlaceholderDiaryEntryMigration`
(`app/infrastructure/room/migration/PlaceholderDiaryEntryMigration.kt:17-22`) already added
`description TEXT` and `isPlaceholder INTEGER NOT NULL DEFAULT 0` to `ManualDiaryEntry`.

**So Story 18's schema footprint is small:** `description` **already exists**; **fibre already
exists** (see §10.5). Only `servingCount` and `weightGrams` are genuinely new columns, both nullable
(defaults: serving count logically 1, weight null) — a backward-compatible migration that leaves
existing nutritional totals untouched (spec §7.2).

### 10.4 Diary entries are already immutable snapshots (spec §7.5 SATISFIED)

Migration **25→26** `UnlinkDiaryMigration`
(`androidMain/.../migration/UnlinkDiaryMigration.android.kt:9-10`, `object : Migration(25,26)`)
created snapshot tables `DiaryProduct`, `DiaryRecipe`, `DiaryRecipeIngredient` and **copied** each
measured product/recipe out of the live `Product`/`Recipe` tables into them (`copyProduct` lines
244-258, `copyRecipe` 260-333). Diary rows are therefore independent of the editable catalog. Manual
(Quick Add) entries embed their own nutrient columns. **No snapshot-conversion prerequisite remains
before safe provider refreshes** — a provider dataset can be fully replaced without touching logged
history. Migration **33→34** `MealTemplateMigration` similarly snapshots per-item nutrients into
`MealTemplateItem`. Database is at **VERSION = 34**, `exportSchema = true`
(`app/infrastructure/room/FoodYouDatabase.kt:148,77`).

### 10.5 Fibre already exists — no migration

`common/domain/food/NutritionFacts.kt:17-19` defines `dietaryFiber`, `solubleFiber`,
`insolubleFiber`. These are part of the embedded `Nutrients` already carried by
`ManualDiaryEntryEntity` (`.kt:30`), `DiaryProduct`, `Product`, and `MealTemplateItem`. Wiring fibre
into Quick Add is **UI + view-model only**. Internal naming is American (`fiber`); user-facing text
may use `fibre` per spec §5.2.

### 10.6 Recipes are 100% ingredient-derived (promotion constraint)

`food/domain/entity/Recipe.kt:32-38` — `nutritionFacts` is derived
(`ingredients.sum() / totalWeight * 100`); the constructor has **no nutrition parameter**, and
`RecipeEntity.kt:7-13` stores only `id, name, servings, note, isLiquid` — **no nutrient columns**.
So a recipe cannot carry a manual nutrition override.

**Recommended promotion mapping (Story 19):** to promote a Quick Add estimate into a recipe without
corrupting the derived model, create the recipe draft with a **single synthetic "placeholder"
ingredient** that carries the Quick Add macros/fibre as its nutrition (so derived totals equal the
estimate until the user replaces it with real ingredients). This is the spec §6.3 option most
consistent with existing behaviour and avoids adding a manual-override field to `Recipe`. Alternative
(estimate-as-metadata) is possible but needs a new nullable field on `Recipe` and changes the
derivation contract — prefer the placeholder ingredient. **Promote-to-Product** is a clean map
(`Product` already has energy/macros/fibre + `packageWeight`/`servingWeight`), so no recipe-style
constraint applies there.

### 10.7 Serving/weight representation elsewhere (for promotion mapping)

- **Product:** `ProductEntity.kt:20-21` — `packageWeight: Double?`, `servingWeight: Double?` (grams).
- **Recipe:** `servings: Int` (`RecipeEntity.kt:9`); `totalWeight` and `servingWeight` are derived
  (`Recipe.kt:28-30`).
- **Product/recipe diary rows:** weight/quantity come from a `Measurement` (type + `quantity REAL`).
- **Quick Add today:** none — the new `servingCount`/`weightGrams` fields introduce this concept to
  manual entries for the first time (quantity context only; §10.2).

---

## 11. Recommended reusable provider-import pipeline

**Do not copy the Swiss/CSV path twice.** Keep `ImportCsvProductUseCase` as the transactional
*insert core* and extract a thin provider-import pipeline around it. A **provider definition** supplies
(per spec §4 preferred direction):

- provider id + display name + source-metadata URL,
- dataset acquisition strategy (download URL, or bundled),
- file format + **parser** (CSV parser exists; **AFCD needs an XLSX parser**),
- record **mapper** (ordered `List<ProductField>`, as Swiss already does),
- a **version/publication detector** (AFCD: release number, then publication date, then checksum),
- optional barcode support, validation rules.

**Common infrastructure to build (net-new):**

1. Download to a **temp** location (no download infra exists today, §4.4).
2. Validate the file before touching active data; reject clearly-invalid/incompatible files.
3. Parse the full dataset; map via `ProductField`.
4. Inside one `withTransaction`: **delete-by-source (User-guarded)** → insert new rows → repopulate FTS.
5. Persist provider metadata (installed version, publication date, checksum, import date, last
   successful check, last error) — §8.3.
6. Progress reporting + structured error result (§4.5).

This makes Story 15 (AFCD) the pipeline's first client and lets Story 17 (update UI) sit on the
metadata table. If FoodSwitch ever unblocks, it would be a second provider definition with **zero**
pipeline duplication.

---

## 12. Risks & blockers

| Risk / blocker | Impact | Mitigation |
| --- | --- | --- |
| **FoodSwitch licence** forbids reproducing the data; no public API/bulk feed. | Story 16 cannot be built as specified. | Record as external blocker; owner pursues a written data licence with The George Institute; never substitute scraping. |
| Append-only insert + no delete-by-source. | Naive re-import duplicates rows / can't update nutrients. | Add `User`-guarded delete-by-source inside the import transaction (§8.2). |
| Content-backed FTS4 not auto-synced on bulk delete/insert. | Search stale/broken after a refresh. | Add an FTS rebuild step + a post-refresh search test (§8.5). |
| Local-provider "disable" does nothing today. | AFCD disable checkbox wouldn't exclude it from search. | Add an enabled-local-sources filter to `FoodSearchDao` (§7). |
| AFCD is `.xlsx`, not CSV; energy in kJ. | Existing CSV parser can't read it; unit mismatch. | Add an XLSX parser (or convert step) + deterministic kJ→kcal conversion with tests (spec §4 unit normalization). |
| No source-record-ID / no provider index. | Harder deterministic replacement; slow at scale. | Add nullable `sourceRecordId` + `(sourceType)`/`(barcode)` indices (§8.1, §5). |
| Overconfident data-safety design. | Could corrupt logged history. | History is already snapshotted (§10.4) — lean on it; guard `User` rows explicitly. |
| Confirm-at-implementation: AFCD may publish Release 4; URLs are release-dated. | Broken download URL later. | Detect the current release/file from the FSANZ page at check-time rather than hard-coding a URL. |

---

## 13. Section 13 decisions — resolved

Every decision from the spec's §13, answered with evidence.

| # | Decision | Resolution |
| --- | --- | --- |
| 1 | FoodSwitch usable data-access mechanism? | **No.** No public API/bulk download; commercially licensed to sponsors; ToU 7.1/7.2/9.2/9.3/9.10/9.12 forbid reproducing/distributing/deriving the data. **Story 16 blocked** (§9.2). |
| 2 | Does FoodYou already have a general provider interface? | **No.** Hard-coded 4-member enum pair + `when` dispatch; no interface/contract (§3.1). |
| 3 | Is Manual Database Import reusable or needs refactoring? | **Reusable core, needs a wrapper.** `ImportCsvProductUseCase` is a good transactional insert engine; extract download/staging/delete-by-source/version-metadata/FTS-rebuild around it (§4.2, §11). |
| 4 | One table or separate per-provider storage? | **One shared `Product` table**; provenance = `sourceType` + `sourceUrl` (§5). |
| 5 | Are diary entries already immutable snapshots? | **Yes.** 25→26 `UnlinkDiaryMigration` → `DiaryProduct`/`DiaryRecipe`; manual entries embed nutrients. Spec §7.5 satisfied (§10.4). |
| 6 | Do Quick Add create & edit share one component? | **Yes.** `QuickAddScreen` shared by Create/Update wrappers + VMs (§10.1). |
| 7 | Quick Add nutrition — total, per serving, or per 100 g? | **Absolute total for the entry** (no scaling); contrast weight-scaled `FoodDiaryEntry` (§10.2). |
| 8 | Do recipes support manually supplied nutrition? | **No.** 100% ingredient-derived; no nutrient columns/params (§10.6). |
| 9 | How should Quick Add estimates map into ingredient-derived recipes? | **Single synthetic placeholder ingredient** carrying the estimate, so derived totals match until real ingredients replace it (§10.6). |
| 10 | Does fibre already exist under another name? | **Yes** — `NutritionFacts.dietaryFiber/solubleFiber/insolubleFiber`, already embedded on the Quick Add entity. No migration for fibre (§10.5). |
| 11 | How are serving count & weight represented in products/recipes? | Product: `packageWeight`/`servingWeight` (Double? g). Recipe: `servings: Int` + derived weights. Diary: `Measurement`(type + `quantity`). Quick Add: none today (§10.7). |
| 12 | Which source metadata reliably identifies a new DB version? | AFCD **release number** first, then publication date, then file checksum/`Last-Modified` (§9.1). |
| 13 | Can imports be atomic with the current persistence layer? | **Yes** — `withTransaction` already wraps parse+insert; full-refresh atomicity just needs the delete-by-source folded into the same transaction (§4.2, §8.2). |
| 14 | Do indexes need rebuilding after a full refresh? | **Yes** — `ProductFts` is content-backed FTS4; repopulate/rebuild after bulk delete+insert. Also add `(sourceType)`/`(barcode)` indices (§8.5, §5). |
| 15 | How is provider provenance shown in search results? | Per-source `Icon(...)` + localized `stringResource()` in `app/ui/food/component/FoodSource.kt`; add an AU icon/badge + string (§6). |
