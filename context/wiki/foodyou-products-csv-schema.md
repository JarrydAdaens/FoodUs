---
name: foodyou-products-csv-schema
description: Knowledge doc — Food You's product CSV import/export schema (columns, units, formatting, import behavior) and the repo's meal-validation CI. Reference for Milestone 1 Story 9, input to Story 10.
metadata:
  version: "1.0"
  owner: "Jarryd Adaens"
  repo: "FoodYou (fork of maksimowiczm/FoodYou)"
---

# Food You — Product CSV Import/Export Schema

> Knowledge doc for **Milestone 1 Story 9** (determine Food You's CSV import schema) and the
> foundation for **Story 10** (export `jarryd/working-data/master-data.json` → Food You CSV).
> [Back to wiki home](home.md)

## Sources of truth

Verified against the app source and a real device export, not guessed:

| What | Where |
| ---- | ----- |
| Column order (enum) | `app/src/commonMain/kotlin/com/maksimowiczm/foodyou/importexport/domain/entity/ProductField.kt` |
| Column headers | `.../importexport/domain/entity/CsvHeaders.kt` (`ProductField.csvHeader()`) |
| Import parsing & rules | `.../importexport/domain/usecase/ImportCsvProductUseCase.kt` |
| Export writing | `.../importexport/domain/usecase/ExportCsvProductsUseCase.kt` |
| Sample export | `E:\shareo\Food You 3.4.9-products-2026-07-24T11_12_33.895579.csv` (app v3.4.9; 2,973 data rows) |

## TL;DR for Story 10

- The CSV is a **products (custom foods) table only** — **no** recipes, meals, or diary entries.
  Of our master data, only `foods[]` maps directly; `recipes[]` / `meals[]` / `groceryProducts[]`
  each need transformation into product rows (or a different mechanism).
- **51 columns, fixed order** (Name → Chromium). Header row is present.
- **Nutrition basis is per 100 g** of the product (implicit — there is no basis column). Confirmed
  against sample values (Apple, fresh = 52 kcal/100 g; Almond = 624 kcal/100 g).
- **Units: every nutrient is in GRAMS except `Energy (kcal)`.** Vitamins and minerals too — e.g.
  `Vitamin A (g) = 1.5E-4` means 0.15 mg. Our master data stores `sodium` in **mg**, so it must be
  divided by 1000 for this CSV; `salt` is already in grams.
- Only **`Name` is strictly required**; everything else may be blank.
- Escape embedded quotes as `""` (the app itself does **not** — see [Known bug](#known-export-bug)).

## What the file represents

A flat export of the app's `Product` catalog — both the seeded food database (the Swiss Food
Composition Database import; ~1,880 rows in the sample have no brand/barcode) and user/scanned
products (1,093 rows have a Brand, 1,117 have a Barcode). Each row is one product; its nutrition
values are **per 100 g** and the app scales them using `Package Weight` / `Serving Weight`.

## Columns (51, in order)

Types: **S** = string, **D** = double, **B** = boolean. Unit `g` unless noted. All nutrient
columns are optional (blank ⇒ unknown).

| # | Header | Field | Type | Req? | Unit / notes |
|---|--------|-------|------|------|--------------|
| 1 | `Name` | Name | S | **Yes** | Import errors if missing. |
| 2 | `Brand` | Brand | S | no | |
| 3 | `Barcode` | Barcode | S | no | Stored as string. |
| 4 | `Note` | Note | S | no | |
| 5 | `Is Liquid` | IsLiquid | B | no | `1`/`true` ⇒ true; `0`/`false`/blank/other ⇒ false. |
| 6 | `Package Weight (g)` | PackageWeight | D | no | grams |
| 7 | `Serving Weight (g)` | ServingWeight | D | no | grams |
| 8 | `Source URL` | SourceUrl | S | no | Becomes `FoodSource.url`. |
| 9 | `Proteins (g)` | Proteins | D | no | g / 100 g |
| 10 | `Carbohydrates (g)` | Carbohydrates | D | no | g |
| 11 | `Energy (kcal)` | Energy | D | no | **kcal** (the only non-gram nutrient) |
| 12 | `Fats (g)` | Fats | D | no | g |
| 13 | `Saturated Fats (g)` | SaturatedFats | D | no | g |
| 14 | `Trans Fats (g)` | TransFats | D | no | g |
| 15 | `Monounsaturated Fats (g)` | MonounsaturatedFats | D | no | g |
| 16 | `Polyunsaturated Fats (g)` | PolyunsaturatedFats | D | no | g |
| 17 | `Omega-3 (g)` | Omega3 | D | no | g |
| 18 | `Omega-6 (g)` | Omega6 | D | no | g |
| 19 | `Sugars (g)` | Sugars | D | no | g |
| 20 | `Added Sugars (g)` | AddedSugars | D | no | g |
| 21 | `Dietary Fiber (g)` | DietaryFiber | D | no | g |
| 22 | `Soluble Fiber (g)` | SolubleFiber | D | no | g |
| 23 | `Insoluble Fiber (g)` | InsolubleFiber | D | no | g |
| 24 | `Salt (g)` | Salt | D | no | g |
| 25 | `Cholesterol (g)` | Cholesterol | D | no | g (so 96 mg ⇒ 0.096) |
| 26 | `Caffeine (g)` | Caffeine | D | no | g |
| 27 | `Vitamin A (g)` | VitaminA | D | no | g |
| 28 | `Vitamin B1 (g)` | VitaminB1 | D | no | g |
| 29 | `Vitamin B2 (g)` | VitaminB2 | D | no | g |
| 30 | `Vitamin B3 (g)` | VitaminB3 | D | no | g |
| 31 | `Vitamin B5 (g)` | VitaminB5 | D | no | g |
| 32 | `Vitamin B6 (g)` | VitaminB6 | D | no | g |
| 33 | `Vitamin B7 (g)` | VitaminB7 | D | no | g |
| 34 | `Vitamin B9 (g)` | VitaminB9 | D | no | g |
| 35 | `Vitamin B12 (g)` | VitaminB12 | D | no | g |
| 36 | `Vitamin C (g)` | VitaminC | D | no | g |
| 37 | `Vitamin D (g)` | VitaminD | D | no | g |
| 38 | `Vitamin E (g)` | VitaminE | D | no | g |
| 39 | `Vitamin K (g)` | VitaminK | D | no | g |
| 40 | `Manganese (g)` | Manganese | D | no | g |
| 41 | `Magnesium (g)` | Magnesium | D | no | g |
| 42 | `Potassium (g)` | Potassium | D | no | g |
| 43 | `Calcium (g)` | Calcium | D | no | g |
| 44 | `Copper (g)` | Copper | D | no | g |
| 45 | `Zinc (g)` | Zinc | D | no | g |
| 46 | `Sodium (g)` | Sodium | D | no | g (our master data is mg ⇒ ÷1000) |
| 47 | `Iron (g)` | Iron | D | no | g |
| 48 | `Phosphorus (g)` | Phosphorus | D | no | g |
| 49 | `Selenium (g)` | Selenium | D | no | g |
| 50 | `Iodine (g)` | Iodine | D | no | g |
| 51 | `Chromium (g)` | Chromium | D | no | g |

## Formatting rules

- **Delimiter:** comma. **Header row present** (import can skip it via `skipHeader`).
- **Quoting:** string fields are wrapped in double quotes (`"Almond"`); numbers and booleans are
  written **bare** (unquoted); a null/blank field is written as an **empty** field (nothing
  between the commas).
- **Numbers:** decimal point `.`; **scientific notation is used and accepted** (Kotlin
  `Double.toString()` emits e.g. `1.5E-4`, `2.0000000000000002E-7`). Booleans export as `1`/`0`.
- **Column count is strict:** every data row must have exactly as many columns as the mapper
  (51 for a full export). A wrong count throws `Invalid number of columns`.

## Import behavior (`ImportCsvProductUseCase`)

- Import is **mapper-driven**: the UI supplies `List<ProductField?>` describing what each column
  is; `null` entries ignore that column and columns can be reordered. A full canonical file uses
  the 51-field order above.
- `Name` missing ⇒ hard error. All other fields tolerate blanks.
- **Number parsing** (`String.toDouble()` helper) is lenient: it trims, **strips leading `<` and
  `<=`** (so `<0.1` ⇒ `0.1`), and treats `-`, `null`, or blank as **null** (unknown nutrient).
  Anything else unparseable also becomes null.
- **De-duplication:** rows are inserted via `insertUniqueProduct(...)`; an existing/duplicate
  product returns no id and is **skipped** (no history entry). Import is therefore safe to re-run.
- Imported products are tagged with a `FoodHistory.Imported` timestamp and the chosen
  `FoodSource.Type`.

## Known export bug

The exporter's `CsvWriter.writeString` is `"\"$value\""` — it wraps the value in quotes **but does
not escape embedded quotes** (should double them to `""`). In the sample export, two rows
(`Potatoes au gratin dauphinois"`, `Potatoes au gratin savoyard"`) contain a literal `"` in the
name, producing **52-column rows** that would fail re-import. **Our Story 10 exporter must escape
`"` as `""`** (and RFC-4180-quote any field containing `"`, `,`, or a newline) to avoid the same
defect.

## The testing GitHub Action (investigated)

`.github/workflows/validate-meals.yml` is the repo's only test-like CI. It runs
`dev/test-meals-localization.bash`, which validates the app's **bundled preset "meal" time-slots**
(`app/src/commonMain/composeResources/files/meals/meals-*.json`) — each entry must be an object
with `name`, `from`, `to`, where `from`/`to` are `HH:MM` (00:00–23:59). These "meals" are the
diary's meal *time windows* (Breakfast/Lunch/…), **not** food meals and **unrelated to CSV
import**. It only triggers on changes to those meal files.

**Implication:** there is **no automated test covering the CSV import/export path**. A Story 10
exporter cannot be validated by existing CI; validate it by **round-trip import into the app**
(export from app → confirm our generator reproduces the same shape → import our file and verify
product counts/values). The other workflows — `docs.yml` (docs site) and `release-apk.yml`
(release build) — are build/publish, not tests.

## Implications for Story 10 (master JSON → Food You CSV)

1. Target = **products only**. Map `foods[]` → product rows directly. Decide how
   `recipes[]` / `meals[]` / `groceryProducts[]` become products (e.g. a recipe/meal → one
   product using its total or per-serving nutrition), per the domain model's collapse/expand rule.
2. **Normalize to per-100 g** before writing (much of our data is per-serving or per-package).
3. **Unit conversions:** sodium mg → g (÷1000); keep salt in g; cholesterol mg → g; energy stays
   kcal; the micronutrient `%DV` values we captured do **not** map (CSV wants grams) — leave blank
   unless a gram value is known.
4. Emit all 51 columns in order with a header row; bare numbers, quoted strings, empty for unknown.
5. **Escape quotes** (`""`) — do not reproduce the app's export bug.
