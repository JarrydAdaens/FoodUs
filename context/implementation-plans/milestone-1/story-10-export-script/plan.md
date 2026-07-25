# Plan: Build the export script (master JSON → Food You CSV)

## Metadata

- Task Type: `STORY`
- Status: `Draft`
- Owner: Jarryd Adaens (execution agent TBD)
- Last Updated: 25 July 2026

## Linked Context

- Milestone: [context/milestones/milestone-1.md](../../../milestones/milestone-1.md)
- Story: [Milestone 1, Story 10](../../../milestones/milestone-1.md#story-10)
- Backlog source: none (story mapped directly from the 2026-07-24 project seed)
- Dictation source: [2026-07-24 initial project seed](../../../dictations-tier-0/2026-07-24_initial_project_seed_acme-food-app.md) (shaped the master-JSON → CSV pipeline concept)
- Related Plans: none yet (Story 5's canonical-format spec does not exist; see Questions)
- External Tooling: none required; `commit-log` skill at commit time

## CER

- Complexity: 4
- Effort: 4
- Risk: 3
- Notes: Inline self-estimate by the planning agent — **not** a formal `rails-grade-cer` grade.
  Complexity comes from the mapping decisions (three source shapes → one products-only schema,
  per-100 g normalization with missing gram weights), not from the code itself. Risk is moderate:
  the script is standalone and cannot break the app, but a bad mapping silently corrupts the
  owner's recovered data on import; mitigated by import-time dedup being re-runnable and by
  round-trip validation before real use.

## Objective

Produce a standalone script, outside the app codebase, that reads
`jarryd/working-data/master-data.json` (format v0.8.0) and writes a Food You products CSV
(51 fixed columns, per-100 g basis) that imports cleanly into the app v3.4.9, carrying all three
data groups — custom foods, recipes (collapsed), and meals (expand on insertion) — plus the
grocery-product catalog, with the "Take Out:" PII redaction preserved and the app's
unescaped-quote export bug not reproduced.

## Scope

### In Scope

- One export script at `jarryd/scripts/export_foodyou_csv.py` (Python 3, stdlib only).
- Mapping rules for `foods[]`, `recipes[]`, `meals[]`, and `groceryProducts[]` into product rows.
- Unit conversion (mg → g for sodium/cholesterol/potassium; drop %DV micronutrients; kJ ignored
  where kcal exists) and per-100 g normalization where a gram basis is known.
- RFC-4180-correct quoting (escape `"` as `""`; quote fields containing `"`, `,`, or newline).
- Built-in output self-checks (column count, required Name, PII scan, quote correctness).
- A short usage note appended to `jarryd/working-data/working-data-readme.md`.
- Round-trip validation by importing the generated CSV into the app on the emulator.

### Out Of Scope

- Any change to the app codebase (`app/`, `shared/`) — the script is standalone by story decree.
- Exporting `diary[]` (currently empty; the CSV format carries no diary entries anyway).
- Normalizing or cross-linking the source-shaped `recipes[]`/`meals[]` inside master-data.json
  (deferred to Story 5 / backlog-1 Story 3).
- Recreating recipe/meal *entities* inside the app (CSV import is products-only; in-app recipe
  assembly, if wanted, is manual follow-up work).
- Fixing the app's own export quote bug (upstream code; fork philosophy says leave it).

## Non-Goals

- No general-purpose "any app" exporter; this targets Food You v3.4.9's schema only.
- No GUI, no CLI option surface beyond input/output paths — no speculative knobs.
- No automated app-side tests; the repo has no CSV CI (confirmed in Story 9) and adding one is
  not this story.

## Current Understanding

- Likely files or directories:
  - Input: `jarryd/working-data/master-data.json` — 100 KB, format v0.8.0; live counts:
    6 foods, 23 recipes (8 anylist / 5 loseit / 10 myfitnesspal), 49 meals, 0 diary,
    31 groceryProducts (24 with nutrition basis "per 100 g", 7 "per 100 mL").
  - Format doc: `jarryd/working-data/working-data-readme.md` (units: sodium/cholesterol/potassium
    in **mg**, everything else grams, energy kcal; `micronutrientPercentDV` is percent, not mass).
  - Target schema doc: `context/wiki/foodyou-products-csv-schema.md` (Story 9 output — 51 columns,
    per-100 g, grams everywhere except `Energy (kcal)`, only `Name` required, blank = unknown,
    strict column count, and the exporter quote bug to avoid).
  - App truth (read-only reference): `app/src/commonMain/kotlin/com/maksimowiczm/foodyou/importexport/domain/entity/ProductField.kt`,
    `.../entity/CsvHeaders.kt`, `.../usecase/ImportCsvProductUseCase.kt`.
  - New: `jarryd/scripts/export_foodyou_csv.py`; output
    `jarryd/working-data/exports/foodyou-products-<yyyy-mm-dd>.csv`.
- Existing behaviors to preserve:
  - Import dedup (`insertUniqueProduct`) skips duplicates, so re-running an import is safe.
  - Import number parsing treats blank/`-`/`null` as unknown — so "leave blank" is the correct
    encoding for absent nutrients, matching the master data's "absent ≠ zero" rule.
  - PII redaction: meal names already redacted to "Take Out:" must pass through untouched.
- Interfaces, data contracts, or external dependencies: the 51-column products CSV is the only
  contract; no network, no third-party libraries.
- Known tests, build steps, or observability points: none for CSV (Story 9 confirmed
  `validate-meals.yml` is unrelated). Validation is script self-checks + manual app import.
- Assumptions and constraints:
  - The script consumes the **working** v0.8.0 format directly, since Story 5's canonical spec
    does not yet exist (see Q1). The milestone lists Story 5 as a dependency, but the working
    format is the only real data and the readme says it "feeds … the Food You CSV export".
  - Fork philosophy: everything lands under `jarryd/`, zero app-code churn, trivially mergeable.

### The central mapping tension: three data groups → a products-only CSV

The Food You CSV carries **products only** — no recipe, meal, or diary structures. The story
demands all three data groups reach the app. Resolution: every group is flattened to product
rows, and the collapse/expand semantics are preserved *behaviorally*, not structurally:

| Master group | CSV mapping | How the domain semantics survive |
| --- | --- | --- |
| `foods[]` (6) | One product row each. | Direct fit — a custom food *is* a product. |
| `recipes[]` (23) | **One product row per recipe** ("the collapsed form as a product"). Nutrition from `perServingNutrients` (MFP) / `nutrition` (AnyList, when basis is usable) / `totalCalories` (Lose It, energy only). `Note` = `Recipe (<source>): <n> ingredients` + ingredient summary. | A recipe collapses into a single diary entry by definition — logging its product row *is* the collapsed behavior. Full ingredient structure stays in master JSON (canonical store); the app row is a generated export, not the source of truth. |
| `meals[]` (49) | **One product row per distinct meal item** (deduped across meals; items repeat). `Note` = `From meal: <meal name>` (accumulating all owning meals). Item nutrients (kcal/protein/carbs/fat/sugars/fiber) map directly. **No row for the meal itself** — a meal-total row would collapse what must expand. | A meal expands into individual entries on insertion — so the *items* must exist as products; the user inserts each item (or later rebuilds the grouping with whatever in-app meal/recipe feature exists). The grouping is preserved in the Note and canonically in master JSON. |
| `groceryProducts[]` (31) | One product row each, with `Barcode`, `Brand`, `Source URL` = `nutritionSource`. Best structural fit of all groups. | N/A — they are literal retail products. |

This flattening is lossy by design (ingredient links and meal grouping don't survive the CSV),
and that is acceptable because master JSON remains canonical and the CSV is a generated export
(design.md, Historical Data Recovery). The loss and the "meal items only, no meal-total row"
decision are surfaced as Q3/Q4 for owner sign-off rather than silently chosen.

### Per-100 g normalization problem

The CSV's nutrition basis is per 100 g (implicit, no basis column), but much of the master data
is per-serving with **no gram weight** (e.g. `food-0001` serving = "8 slices", MFP recipes give
`caloriesPerServing` with no serving grams, meal items have verbatim portions like `2 SLICE`).
Normalization policy, per row:

1. **Gram basis known** (serving unit `g`, grocery `per 100 g`): scale nutrients to 100 g;
   write `Serving Weight (g)` = actual gram serving.
2. **`per 100 mL` grocery items**: write values as-is with `Is Liquid` = `1` and a Note
   recording the mL basis (the app has no per-mL column; density ≈ 1 is assumed — Q5).
3. **Gram basis unknown**: adopt the **"1 serving = 100 g" convention** — write the per-serving
   values *as* the per-100 g values and set `Serving Weight (g)` = `100`. Logging "1 serving"
   (100 g) then yields exactly the correct per-serving energy/macros even though the gram figure
   is fictional. Mark such rows with `Note` containing `serving=100g convention`. (Q2 — owner
   must confirm this convention.)
4. **No usable nutrition at all** (AnyList `basis: "not shown"`, Lose It recipes' energy-only
   data still produces an energy value; grocery items with `nutrition: null`): emit the row with
   `Name` + `Note` only, or skip — default: emit name-only rows so the catalog entry exists (Q4).

### Unit conversions (master → CSV)

- `sodium`, `cholesterol`, `potassium`: **mg → g** (÷ 1000).
- `salt`: already grams — copy.
- `energyKcal` → `Energy (kcal)` unchanged; `energyKj` ignored when kcal exists, else ÷ 4.184.
- `micronutrientPercentDV` / `perServingMicronutrientPercentDV` (%DV, not mass): **left blank** —
  the CSV wants grams and a %DV→gram guess would fabricate data.
- All other keys (`fat`, `saturatedFat`, `transFat`, mono/poly, `carbs`, `fiber`, `sugars`,
  `protein`, `unsaturatedFat`*) are grams → copy. *`unsaturatedFat` (one combined AnyList figure)
  has no single CSV column — left blank rather than arbitrarily split (noted in row Note).

## Questions / Unknowns

- Q: **[STORY 10] Does the export target the working v0.8.0 format directly, or must Story 5's canonical master format land first?**
  Impact: The milestone lists Story 5 as a dependency, but Story 5 is Not Started while the working format holds all the real data. Waiting blocks the milestone's import goal; not waiting means the script may need rework when Story 5 normalizes the format.
  Assumption: Target v0.8.0 directly (the readme says the staging file "feeds … the Food You CSV export"); the script checks `formatVersion` and fails loudly on anything but `0.8.x`, making later rework explicit rather than silent.
  Status: OPEN
  Answer: —

- Q: **[STORY 10] Is the "1 serving = 100 g" convention acceptable for foods/recipes/meal items with no gram weight?**
  Impact: Determines whether most recipe and meal rows carry usable numbers or are left nutrition-blank. The convention gives correct per-serving energy when logging 100 g, but the gram figure is fictional (misleads if the user logs by real weight).
  Assumption: Yes — correct-when-logged-as-a-serving beats blank, and affected rows are flagged in `Note`.
  Status: OPEN
  Answer: —

- Q: **[STORY 10] Meals: items-only (no meal-total row), with the grouping preserved only in the Note and in master JSON — acceptable?**
  Impact: This is the crux of "expand on insertion" vs a products-only import. An additional meal-total row would let one-tap logging of a whole meal but violates expand semantics and double-represents the data.
  Assumption: Items-only. Rebuilding meal groupings in-app (via Food You's own recipe/meal features) is manual follow-up outside this story.
  Status: OPEN
  Answer: —

- Q: **[STORY 10] Emit name-only rows for entries with no usable nutrition, or skip them?**
  Impact: Name-only rows put every known item in the catalog (searchable, annotatable later) but pollute it with zero-value entries the app treats as 0-kcal-lookalikes (blank = unknown, so honest — but visually empty).
  Assumption: Emit them, with `Note` explaining the gap; the import is products-only and re-runnable, so pruning later is easy.
  Status: OPEN
  Answer: —

- Q: **[STORY 10] Per-100 mL grocery items: write values as per-100 g with `Is Liquid = 1` (density ≈ 1), or leave nutrition blank?**
  Impact: 7 of 31 grocery products. Density ≈ 1 is near-exact for milk/water-based liquids, wrong for oils.
  Assumption: Write as-is with `Is Liquid = 1` and a Note; flag any obviously non-aqueous item during execution.
  Status: OPEN
  Answer: —

- Q: **[STORY 10] Should the generated CSV be committed to the repo, or stay untracked output?**
  Impact: Reproducibility vs churn. master-data.json is already committed, so no new privacy exposure either way (PII already redacted).
  Assumption: Commit the script; leave `jarryd/working-data/exports/` untracked (add to `.gitignore` under `jarryd/`) since the CSV is derived output regenerable at will.
  Status: OPEN
  Answer: —

## Execution Steps

1. **Scaffold the script**
   - Why: Establish the standalone home per story decree ("outside the app codebase").
   - Edits: New `jarryd/scripts/export_foodyou_csv.py` — Python 3, stdlib only (`json`, `argparse`,
     `datetime`, `pathlib`). Args: `--input` (default `jarryd/working-data/master-data.json`),
     `--output` (default `jarryd/working-data/exports/foodyou-products-<date>.csv`). Loads JSON,
     asserts `formatVersion` starts with `0.8`.
   - Dependencies: none.

2. **Implement the CSV writer (hand-rolled, not `csv` module defaults)**
   - Why: Must reproduce Food You's exact shape — strings quoted, numbers bare, blanks truly
     empty — and fix the quote bug (Python's `csv` quoting modes can't express "quote strings,
     bare numbers, empty for null" cleanly).
   - Edits: A small `write_row(fields)` that: quotes string fields with `"` doubled to `""`;
     writes numbers bare with plain decimal formatting (no scientific notation needed — import
     accepts both); writes `None` as empty; asserts exactly 51 fields per row. Emit the header
     row from a hardcoded 51-header list copied verbatim from the wiki schema doc.
   - Dependencies: step 1.

3. **Implement the nutrient mapper**
   - Why: One shared conversion path keeps all four groups consistent (DRY).
   - Edits: `map_nutrients(nutrients_dict) -> dict[ProductColumn, float]` applying the unit
     table above (mg→g for sodium/cholesterol/potassium, kJ fallback, %DV dropped), plus
     `scale_to_100g(values, gram_basis)` and the serving=100 g convention fallback.
   - Dependencies: step 1.

4. **Map the four groups to rows**
   - Why: The core deliverable — the mapping table in Current Understanding, made executable.
   - Edits: `rows_from_foods`, `rows_from_recipes` (per-source handling: MFP
     `perServingNutrients`, AnyList `nutrition` + `basis` switch, Lose It energy-only from
     `totalCalories`/`servings`), `rows_from_meals` (dedupe items by normalized name; accumulate
     owning-meal names into `Note`), `rows_from_grocery` (barcode, brand, `Source URL`,
     `Is Liquid` for per-100 mL). Final cross-group dedupe on (Name, Brand).
   - Dependencies: steps 2-3.

5. **Built-in self-checks (fail the run, not just warn)**
   - Why: The import path has no CI; the script must be its own gate. Also enforces the PII law.
   - Edits: Post-generation assertions on the produced file: (a) every row parses back to exactly
     51 columns under RFC-4180 rules; (b) every row has a non-blank Name; (c) no occurrence of
     `BigAnt` / `Big Ant` (case-insensitive) anywhere in the output; (d) every numeric field
     round-trips `float()`; (e) row-count report per group printed to stdout.
   - Dependencies: step 4.

6. **Run and eyeball the output**
   - Why: Manual sanity before touching a device — catch mapping absurdities (e.g. a 936 kcal
     "per 100 g" pizza row from the serving convention) by reading the file.
   - Edits: none (generated CSV only). Spot-check ~10 rows against master JSON by hand, including
     one from each group and every basis case (gram-known, serving-convention, per-100 mL,
     nutrition-absent).
   - Dependencies: step 5.

7. **Round-trip import into the app (emulator)**
   - Why: Story 9's finding — the only real validation is importing into the app. Emulator first
     so a bad file never touches a real phone's database.
   - Edits: none. Deploy the unmodified app to the Android 16 emulator (toolchain per agent
     memory), import the CSV via the app's import screen with the canonical 51-column mapping,
     verify: import completes without error, product count matches the script's report, spot-check
     values (a food, a recipe, a meal item, a barcode-bearing grocery product; scan-by-barcode
     test if practical). Re-run the import once to confirm dedup-skip behavior.
   - Dependencies: step 6.

8. **Document and close out**
   - Why: Durable knowledge per repository law; the readme already promises this pipeline.
   - Edits: Append a short "Export to Food You CSV" section to
     `jarryd/working-data/working-data-readme.md` (how to run, where output lands, the mapping
     conventions and their flags). Update Story 10 status in
     `context/milestones/milestone-1.md` and this plan's Execution Log / Completion Review.
     Add `jarryd/working-data/exports/` to `.gitignore` (pending Q6). Draft commit log via the
     `commit-log` skill; commit only on explicit request.
   - Dependencies: step 7.

## Validation

### Automated Checks

- Script self-checks (step 5): 51-column parse-back, Name presence, PII scan, numeric
  round-trip — run on every invocation, non-zero exit on failure.
- `python jarryd/scripts/export_foodyou_csv.py` completes cleanly with per-group row counts.
- No app-side automated tests exist for CSV (Story 9-confirmed gap) and none are added — the
  test gap is explicitly accepted per the unit-testing rule (validation lives at the
  round-trip level, where it belongs).

### Manual Checks

1. Spot-check ~10 output rows against master-data.json values and unit conversions (step 6).
2. Emulator round-trip import: no errors, counts match, values correct, barcode lookup works
   (step 7).
3. Re-run the same import; confirm duplicates are skipped, not doubled.
4. Search the output file for `"` occurrences and confirm all are structural or doubled.

### Acceptance Criteria

- The generated CSV imports into Food You v3.4.9 without errors and yields one product per
  mapped row (foods + recipes + deduped meal items + grocery products).
- All three story data groups are represented per the mapping table; grocery products carry
  their barcodes.
- No output field contains an unescaped embedded quote (the app's export bug is not reproduced).
- No `BigAnt`/`Big Ant` text appears anywhere in the output; "Take Out:" names pass through
  verbatim.
- Zero changes under `app/` or `shared/`.
- Re-importing the same file is a no-op (dedup verified).

## Risk Mitigation

- Risk: The serving=100 g convention produces misleading per-100 g figures if a user logs by
  real weight.
  Mitigation: Flag affected rows in `Note`; surface as Q2 for explicit owner sign-off; master
  JSON keeps the true serving text for later correction.
- Risk: Story 5's eventual canonical format diverges from v0.8.0 and silently breaks the mapper.
  Mitigation: Hard `formatVersion` check with a loud failure; mapper functions are per-group and
  small, so rework is localized.
- Risk: Meal-item dedup by name merges genuinely different foods (source names are messy,
  e.g. "Egg" vs "Eggs").
  Mitigation: Dedupe only on exact normalized (Name, Brand); print merged pairs in the run
  report for human review; when in doubt, keep both (import-side dedup catches true duplicates).
- Risk: Importing a flawed file dirties a real phone's database.
  Mitigation: Emulator-first round trip (step 7); real-device import only after emulator pass.
  Import-side dedup means a re-import after fixes does not duplicate rows, but a *wrong-valued*
  first import persists — so the emulator gate is the real protection.
- Risk: Python `csv` module quoting quirks produce `""` for blanks or quote numbers.
  Mitigation: Hand-rolled writer (step 2) with parse-back self-check (step 5).
- Risk: %DV micronutrients and `unsaturatedFat` data are dropped, losing captured detail.
  Mitigation: Accepted — the CSV cannot represent them honestly; the data survives in master
  JSON, which remains canonical.

## Phase Split

Not needed — CER is below phasing thresholds; single-plan execution.

## Evidence / References

- Target schema + export bug + import behavior: `context/wiki/foodyou-products-csv-schema.md`
  (Story 9, verified against `ProductField.kt`, `CsvHeaders.kt`, `ImportCsvProductUseCase.kt`,
  `ExportCsvProductsUseCase.kt` and a real v3.4.9 export).
- Source format: `jarryd/working-data/working-data-readme.md` (v0.8.0) and live inspection of
  `master-data.json` (2026-07-25): 6 foods, 23 recipes, 49 meals, 0 diary, 31 groceryProducts;
  foods' serving units `g`×4 / `serving`×1 / `slices`×1; grocery bases per-100 g ×21 (readme
  claims 24 with nutrition; 7 per-100 mL) — counts to re-verify at execution time since the file
  is mutated in place.
- Import slice confirmed present at
  `app/src/commonMain/kotlin/com/maksimowiczm/foodyou/importexport/domain/` (entity + usecase
  files as cited by the wiki doc).
- No existing scripts under `jarryd/`; no prior plan for this story
  (only `milestone-1/own-project-infrastructure/`).

## Complaints / Friction

### Story 5 dependency is unresolved at planning time

**What happened:** The milestone declares Story 10 depends on Stories 5-9, but Story 5 (define
the master data format) is Not Started; the only master format that exists is the working
v0.8.0 staging shape.
**Why this made the task harder:** The plan must choose an input contract without a canonical
spec, risking rework.
**What was tried:** Grounded the plan in v0.8.0 (the only real data) with a hard version check,
and raised Q1 for the owner.
**What would improve this:** Either mark Story 5 as satisfied-by-v0.8.0 (promote the readme to
the spec) or schedule it before Story 10 execution.
**What I think:** v0.8.0 is de facto the master format; formalizing it as Story 5's deliverable
is cheaper than writing a second format and migrating.

## Execution Log

Not yet started — filled in during execution.

## Completion Review

Not yet started — filled in after execution.
