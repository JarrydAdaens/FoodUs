---
name: master-data-format
description: Canonical specification of the master data format v1.0.0 - the owner's app-independent JSON store for foods, recipes, meals, diary entries, and grocery products, from which app-specific exports are generated.
metadata:
  version: "1.0"
  agentic_rails_source_version: "3.0"
  owner: "Jarryd Adaens"
  repo: "FoodYou (fork of maksimowiczm/FoodYou)"
---

# Master Data Format v1.0.0

[Back to Wiki Home](home.md)

This is the **spec of record** for the owner's master food data. The format is
app-independent: it is the canonical store the owner controls, and app-specific formats
(e.g. the Food You products CSV, Story 10) are **generated exports** from it. Data ownership
is a founding goal of the fork (see `context/design.md`, "Why This Fork Exists").

- **Data file:** `jarryd/working-data/master-data.json` (single JSON file, human-diffable,
  mutated in place; git history is the rollback mechanism).
- **Schema:** `jarryd/working-data/master-data.schema.json` (JSON Schema draft 2020-12).
- **Validation one-liner** (run from the repository root):

  ```sh
  uv run --with jsonschema python -c "import json, jsonschema; jsonschema.validate(json.load(open('jarryd/working-data/master-data.json', encoding='utf-8')), json.load(open('jarryd/working-data/master-data.schema.json', encoding='utf-8'))); print('valid')"
  ```

Run the validation after every data mutation (paste session, export prep, manual edit).

## Version Policy (`formatVersion`)

`formatVersion` is semver, interpreted for a data format:

| Bump | When |
| --- | --- |
| **Major** | A shape change: fields renamed, moved, retyped, or removed; a collection restructured. Consumers must be updated. |
| **Minor** | Additive only: new optional fields, new nutrient keys, new collections. Existing consumers keep working. |
| **Patch** | Reserved. **Data-only changes never bump the version** — adding, editing, or removing entries within the existing shape is not a format change. |

The schema is updated **in the same commit** as any data that needs a new shape. Consumers
(e.g. Story 10's exporter) guard on `formatVersion` and fail loudly on a major version they
do not target.

Format history: v0.x was the fast-and-messy staging format grown during extraction
(Stories 6-8). v1.0.0 is the first canonical version; the only shape change from v0.8.0 is
the recipe nutrition envelope (see "Recipes").

## Core Principles (normative)

These rules bind every entry in every collection:

- **Verbatim preservation.** The extractions were LLM-assisted copy-paste from source-app
  UIs; parsing was unreliable, so verbatim source strings are the ground truth and are never
  altered, re-parsed in place, or dropped: MyFitnessPal ingredient `raw` lines, meal-item
  `portion` strings, AnyList ingredient `quantity` strings, and AnyList/grocery `servingSize`
  / `sizeText` strings.
- **Anti-fabrication.** A value appears only when the source actually provided it. Absent
  data is **omitted** (or `null` where the field is structurally present, as in
  `groceryProducts`); it is never guessed, interpolated, or defaulted.
- **Explicit zero is real.** A source value of `--` (blank/no data) is left out. An explicit
  `0` is a **real reported zero** and is kept — the two are distinct.
- **PII redaction (constitutional).** Any entry name containing the former workplace name
  (redacted from this repository) is renamed with a `Take Out:` prefix, and the original text
  is stored nowhere in this repository. Apply this to every future paste. See agent memory
  "Food data PII redaction" for the literal strings to scan for.
- **Provenance.** Every entry carries `source` naming where it came from: `loseit`,
  `myfitnesspal`, `anylist`, or `shopping-list` (grocery catalog). `groceryProducts` entries
  additionally carry per-value source URLs and a `confidence` rating.

## Unit Conventions (normative)

Nutrient units are implied by key name — values are plain JSON numbers, never strings:

| Key(s) | Unit |
| --- | --- |
| `energyKcal` | kilocalories |
| `energyKj` | kilojoules (kept alongside `energyKcal` where the source gave kJ) |
| `cholesterol`, `sodium`, `potassium` | milligrams |
| `fat`, `saturatedFat`, `polyunsaturatedFat`, `monounsaturatedFat`, `unsaturatedFat`, `transFat`, `carbs`, `fiber`, `sugars`, `protein`, `salt` | grams |

- `unsaturatedFat` is used where the source gives one combined unsaturated figure; `salt`
  where the source panel lists salt rather than (or beside) sodium.
- **Percent-DV micronutrients are never mixed with masses.** Sources that report `vitaminA`,
  `vitaminC`, `calcium`, `iron` as a percent of daily value keep them in a separate
  `micronutrientPercentDV` / `perServingMicronutrientPercentDV` object; values are the
  percent number (`0` means "0 %").
- Dates are `YYYY-MM-DD`.

## Identity Rules

- Ids are `<prefix>-NNNN` (zero-padded to four digits): `food-`, `recipe-`, `meal-`,
  `diary-`, `grocery-`.
- Ids are **stable and never reused**, including after deletions. `meta.idCounters` holds
  the count of ids ever issued per collection; the next id is that value + 1.
- Moving an entry between collections (e.g. an AnyList "recipe" reclassified as a meal) is a
  data change: it takes a new id in the target collection and retires the old id.

## Top-Level Shape

```json
{
  "formatVersion": "1.0.0",
  "meta": { "description": "...", "owner": "Jarryd Adaens", "lastSource": "myfitnesspal",
            "idCounters": { "food": 6, "recipe": 23, "meal": 49, "diary": 0, "grocery": 31 } },
  "foods": [], "recipes": [], "meals": [], "diary": [], "groceryProducts": []
}
```

Five collections, all first-class. `foods`, `recipes`, `meals`, and `diary` come from the
design's domain model; `groceryProducts` is first-class by decision of Story 5 — it is
sourced, consumed by the Story 10 export mapping, and carries the strongest provenance
conventions in the file. `meta.lastSource` records the last app pasted from.

## Cross-Linking (`foodId`)

Recipe ingredients and meal items are captured **inline** (name + portion + nutrition as the
source showed them), not linked to `foods[]`. The format defines an **optional** `foodId`
field on every recipe ingredient and meal item, referencing a `foods[]` or
`groceryProducts[]` id. No current entry populates it; populating it is future
de-duplication **data** work, not a format change.

## `foods[]` — custom foods

A food defined by its nutrition fundamentals (domain model: custom food).

```json
{
  "id": "food-0002",
  "name": "Banana Flavour Bread",
  "brand": "Happy Rich",
  "icon": "Banana",
  "serving": { "amount": 1, "unit": "serving" },
  "nutrients": { "energyKcal": 101.3, "fat": 2.2, "sodium": 74, "carbs": 17, "sugars": 5.9, "protein": 3.2 },
  "source": "loseit",
  "notes": ""
}
```

| Field | Req | Notes |
| --- | --- | --- |
| `id`, `name`, `serving`, `nutrients`, `source` | yes | `serving` is a parsed portion `{ amount: number, unit: string }`. |
| `brand` | yes (nullable) | Restaurant/brand; `null` if none. |
| `icon` | no (nullable) | Lose It icon label, kept as-is. |
| `notes` | no | Free text. |
| `micronutrientPercentDV` | no | Percent-DV block (see Unit Conventions). |

Only nutrient keys the source actually provided appear (anti-fabrication / explicit-zero
rules above).

## `recipes[]` — collapse into one diary entry

A recipe is a collection of ingredient foods that stays **collapsed** into a single diary
entry when used (domain model).

### The canonical envelope

Recipes were extracted from three apps whose UIs expose different data, so entries are
**envelope + faithful source detail**: every recipe has the required common fields below,
and additionally keeps its source-specific fields exactly as extracted. Consumers read the
envelope and may ignore the rest; nothing from the source is discarded.

| Envelope field | Notes |
| --- | --- |
| `id`, `name`, `source` | As per Identity/Provenance rules. |
| `ingredients[]` | Source-shaped items (below). |
| `nutrition` | Object with a required self-describing **`basis`** so a total is never mislabelled as per-serving: `"per serving"`, `"total"`, `"unspecified"` (values shown but basis unknown), or `"not shown"` (no nutrition in the source; only `basis` present). Other keys: nutrient keys per Unit Conventions, plus optional verbatim `servingSize` string (e.g. `"99.6 g"`, `"1 serving"`). |
| `servings` | Optional integer, present when the source states it. |

### Per-source detail

**Lose It** (`"source": "loseit"`) — the source shows a recipe total and per-ingredient
calories only. `nutrition` holds the recipe total (`basis: "total"`); ingredients carry a
parsed `portion` and `energyKcal`:

```json
{ "id": "recipe-0001", "name": "Breakfast Toasty",
  "nutrition": { "basis": "total", "energyKcal": 400 },
  "ingredients": [ { "name": "Bread, White", "portion": { "amount": 2, "unit": "Slices" }, "energyKcal": 133 } ],
  "source": "loseit" }
```

**MyFitnessPal** (`"source": "myfitnesspal"`) — the source shows a full per-serving panel.
`nutrition` holds it (`basis: "per serving"`); source detail kept beside the envelope:
`servings`, `caloriesPerServing` (the source's own headline figure, verbatim), and
`perServingMicronutrientPercentDV`. Ingredients have no per-ingredient calories:

```json
{ "id": "recipe-0007", "name": "Coffee, Full", "servings": 1, "caloriesPerServing": 74,
  "ingredients": [ { "name": "Full Cream Milk", "portion": { "amount": 100, "unit": "ml" }, "raw": "100 ml, Full Cream Milk" } ],
  "nutrition": { "basis": "per serving", "energyKcal": 74, "fat": 4 },
  "perServingMicronutrientPercentDV": { "vitaminA": 0, "vitaminC": 0, "calcium": 15, "iron": 0 },
  "source": "myfitnesspal" }
```

**AnyList** (`"source": "anylist"`) — loosely-measured ingredients and variable nutrition;
this source's shape **is** the envelope (it originated the `basis` convention). Ingredients
use a single verbatim `quantity` string, not a parsed portion:

```json
{ "id": "recipe-0016", "name": "Creamy Carrot Soup",
  "ingredients": [
    { "name": "Carrots, thinly sliced", "quantity": "1,000 g" },
    { "name": "Thyme", "quantity": "½ small bunch — weight not specified" }
  ],
  "nutrition": { "basis": "per serving", "energyKcal": 417, "fat": 15, "salt": 1.73 },
  "source": "anylist" }
```

### Ingredient conventions

| Source | Item shape |
| --- | --- |
| `loseit` | `name`, `portion` (parsed), `energyKcal`; optional `truncatedName`, `foodId`. |
| `myfitnesspal` | `name`, `portion` (parsed), `raw` (verbatim); optional `truncatedName`, `foodId`. |
| `anylist` | `name`, `quantity` (verbatim); optional `foodId`. |

- Parsed `portion` keeps the source's exact `unit` wording (`Grams`, `Slices`,
  `container (402 mls ea.)`, ...); fractions become decimals (`1/4 Cup` → `0.25`).
- `raw` (MyFitnessPal) holds the original ingredient line verbatim. It is the source of
  truth when the parsed `name`/`portion` is ambiguous — e.g. a prep modifier between measure
  and food (`"4 tsp, unpacked, Brown sugar"`), parsed as name `Brown sugar` with the
  modifier preserved only in `raw`.
- AnyList `quantity` is verbatim: a leading `≈` marks a value AnyList converted from
  volume/item counts; `"Quantity not specified"` / `"1 head; weight not specified"` are kept
  as written rather than dropped or guessed.
- `"truncatedName": true` marks an ingredient whose name the source UI cut off (e.g.
  `"Spring Onion, Bulb and Stalk,..."`) so it can be recovered later.

## `meals[]` — expand into individual diary entries

A meal is a set of already-portioned items that **expands** into individual diary entries
when inserted (domain model). Captured from MyFitnessPal "My Meals".

```json
{ "id": "meal-0001", "name": "Greens",
  "items": [
    { "name": "Brussel sprouts", "portion": "100/1 g",
      "nutrients": { "energyKcal": 43, "protein": 3, "carbs": 9, "fat": 0, "sugars": 2, "fiber": 4 } }
  ],
  "total": { "energyKcal": 165, "protein": 9, "carbs": 29, "fat": 0, "sugars": 10, "fiber": 12 },
  "source": "myfitnesspal" }
```

- Each item carries the six values that source lists per row — `energyKcal` plus
  `protein`/`carbs`/`fat`/`sugars`/`fiber` (grams).
- Item `portion` is the source string **verbatim** — no parsing, because these are highly
  irregular (`100/1 gram`, `4/1 oz (112g)`, `5/4 serving(s)`, `1 pat (1 inch sq, 1/2 inch
  high)`, and even a float artifact `5404319552844595/18014398509481984 larger` ≈ 0.3).
- Item `name` keeps the source's `Brand - Food` wording as one string. Optional `foodId`
  per Cross-Linking.
- `total` is the source's **stated** total row, preserved verbatim — not recomputed.

## `diary[]` — a reference to a food that was eaten

A diary entry links a food to a date, meal slot, and portion (domain model). The collection
is currently empty; historical diary recovery is future work. Defined shape:

```json
{ "id": "diary-0001", "date": "2024-01-15", "meal": "breakfast",
  "foodId": "food-0001", "portion": { "amount": 1, "unit": "serving" }, "source": "loseit" }
```

All six fields required; `foodId` references a `foods[]`, `recipes[]`, or `meals[]` id.

## `groceryProducts[]` — the household grocery catalog

The household's usual shopping-list items, each researched to a real Coles/Woolworths
product so they can be pre-loaded (barcode + nutrition) and scanned less often. Built via
web research (primarily Open Food Facts, where the numeric code in a product URL **is** the
barcode/GTIN). `source` is `"shopping-list"`.

```json
{ "id": "grocery-0001", "listItem": "Halloumi", "category": "Dairy & Eggs",
  "product": { "brand": "Woolworths", "name": "Haloumi Cheese", "retailer": "Woolworths", "sizeText": "180g" },
  "barcode": "9300633735500", "barcodeSource": "https://world.openfoodfacts.org/product/9300633735500",
  "nutrition": { "basis": "per 100 g", "energyKcal": 284.4, "energyKj": 1191.9, "protein": 18.3, "sodium": 1130, "salt": 2.825 },
  "nutritionSource": "https://world.openfoodfacts.org/product/9300633735500", "confidence": "high",
  "grocery": { "status": "checked", "quantity": null, "price": "$21.00 each", "notes": null },
  "researchNotes": "…", "source": "shopping-list" }
```

| Field | Req | Notes |
| --- | --- | --- |
| `id`, `listItem`, `category`, `source` | yes | `listItem` is the original shopping-list wording. |
| `product` | yes (nullable) | `{ brand (nullable), name, retailer, sizeText (nullable, verbatim) }`; `null` when no confident product match exists. |
| `barcode`, `barcodeSource` | yes (nullable) | Anti-fabrication: present only when found on a real source, else `null`. |
| `nutrition`, `nutritionSource` | yes (nullable) | `nutrition.basis` is `"per 100 g"` or `"per 100 mL"`. Same nullability rule. |
| `confidence` | yes | `high` / `medium` / `low` (low = representative pick or weak source). |
| `grocery` | yes | Original shopping-list context, preserved: `{ status: "checked"\|"unchecked", quantity (nullable), price (nullable), notes (nullable) }`. |
| `researchNotes` | yes | Product-match reasoning and source caveats. |

Units follow this file's conventions: `sodium` in **mg** (source panels that gave grams were
×1000 — cross-checked against `salt = sodium × 2.5`); `energyKcal` derived from `energyKj`
(÷4.184) where a source gave only kJ (noted in `researchNotes`).
