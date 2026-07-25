# jarryd/working-data

Scratch home for the owner's **master food data** while it is being recovered from old
apps (MyFitnessPal, Lose It, AnyList) during Milestone 1, Stories 5-8.

- **`master-data.json`** — the single living data file. Messy copy-pastes from the source
  apps get transformed into this. It is **mutated in place** to retain everything already
  captured while making room for new data.

This is deliberately a fast-and-messy staging area, not the final Story 5 spec. The format
here evolves as new data arrives; once it settles it feeds the canonical master format and
the Food You CSV export (Story 10).

## Format (v0.6.0)

Top level:

| Field | Purpose |
| --- | --- |
| `formatVersion` | Bumped when the shape changes. |
| `meta` | Description, owner, `lastSource` (last app pasted from), `idCounters` (next id per collection). |
| `foods` | Custom foods — a food defined by its nutrition fundamentals. |
| `recipes` | Component foods that **collapse** into one diary entry when used. |
| `meals` | Component foods that **expand** into individual entries when inserted. |
| `diary` | Diary entries — a reference to a food that was eaten, with a portion. |

### `foods[]` entry

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

- `brand` holds the restaurant/brand (null if none). `icon` is the Lose It icon label, kept
  as-is; omit if absent.
- Only include nutrient keys the source actually provides. A field the source shows as
  `Optional` (blank) is left out rather than guessed as zero.
- A source value of `--` (blank/no data) is left out. An explicit `0` is a **real reported
  zero** and is kept — the two are distinct.
- **Nutrient units** are implied by key: `energyKcal` in kcal; `cholesterol`, `sodium`, and
  `potassium` in milligrams; every other `nutrients` key (`fat`, `saturatedFat`,
  `polyunsaturatedFat`, `monounsaturatedFat`, `transFat`, `carbs`, `fiber`, `sugars`,
  `protein`) in grams.
- `micronutrientPercentDV` (optional) holds micronutrients the source reports as a **percent
  of daily value** rather than a mass — `vitaminA`, `vitaminC`, `calcium`, `iron`. Values are
  the percent number (e.g. `0` means "0 %"). Kept separate so percents are never confused
  with masses.

### `diary[]` entry

```json
{
  "id": "diary-0001",
  "date": "2024-01-15",
  "meal": "breakfast",
  "foodId": "food-0001",
  "portion": { "amount": 1, "unit": "serving" },
  "source": "loseit"
}
```

### `recipes[]` entry

```json
{
  "id": "recipe-0001",
  "name": "Breakfast Toasty",
  "totalCalories": 400,
  "ingredients": [
    { "name": "Bread, White", "portion": { "amount": 2, "unit": "Slices" }, "energyKcal": 133 }
  ],
  "source": "loseit"
}
```

Recipe entries are **source-shaped** — they carry whichever fields the source app exposes,
so a Lose It recipe and a MyFitnessPal recipe do not look identical yet. `source` tells them
apart; normalization is deferred. Common to both: `id`, `name`, `ingredients[]`, `source`.

**Lose It recipes** carry `totalCalories` and per-ingredient `energyKcal`, but no other
nutrients:

```json
{ "id": "recipe-0001", "name": "Breakfast Toasty", "totalCalories": 400,
  "ingredients": [ { "name": "Bread, White", "portion": { "amount": 2, "unit": "Slices" }, "energyKcal": 133 } ],
  "source": "loseit" }
```

**MyFitnessPal recipes** carry `servings`, `caloriesPerServing`, a full `perServingNutrients`
panel (same keys/units as a food's `nutrients`), and `perServingMicronutrientPercentDV` (same
keys as a food's `micronutrientPercentDV`). Their ingredients have no per-ingredient
calories:

```json
{ "id": "recipe-0007", "name": "Coffee, Full", "servings": 1, "caloriesPerServing": 74,
  "ingredients": [ { "name": "Full Cream Milk", "portion": { "amount": 100, "unit": "ml" }, "raw": "100 ml, Full Cream Milk" } ],
  "perServingNutrients": { "energyKcal": 74, "fat": 4, "...": "..." },
  "perServingMicronutrientPercentDV": { "vitaminA": 0, "vitaminC": 0, "calcium": 15, "iron": 0 },
  "source": "myfitnesspal" }
```

Ingredient conventions (both sources):

- Captured **inline** — not yet linked to `foods[]`. Portion `unit` keeps the source's exact
  wording (`Grams`, `Slices`, `container (402 mls ea.)`, ...); fractions become decimals
  (`1/4 Cup` -> `0.25`).
- `raw` (MyFitnessPal) holds the original ingredient line verbatim. It is the source of truth
  when the parsed `name`/`portion` is ambiguous — e.g. a prep modifier between measure and
  food (`"4 tsp, unpacked, Brown sugar"`), which is parsed as name `Brown sugar` with the
  modifier preserved only in `raw`.
- `"truncatedName": true` marks an ingredient whose name the source UI cut off (e.g.
  `"Spring Onion, Bulb and Stalk,..."`) so it can be recovered later.

### `meals[]` entry

A meal is a set of already-portioned items that **expands** into individual diary entries when
inserted (domain model). Captured from MyFitnessPal "My Meals". Each item carries the six
values that source lists per row — calories + protein/carbs/fat/sugar/fiber (grams) — and the
meal's own `total` row is preserved verbatim (it is the source's stated total, not recomputed).

```json
{ "id": "meal-0001", "name": "Greens",
  "items": [
    { "name": "Brussel sprouts", "portion": "100/1 g",
      "nutrients": { "energyKcal": 43, "protein": 3, "carbs": 9, "fat": 0, "sugars": 2, "fiber": 4 } }
  ],
  "total": { "energyKcal": 165, "protein": 9, "carbs": 29, "fat": 0, "sugars": 10, "fiber": 12 },
  "source": "myfitnesspal" }
```

- Item `portion` is the source string **verbatim** — no parsing, because these are highly
  irregular (`100/1 gram`, `4/1 oz (112g)`, `5/4 serving(s)`, `1 pat (1 inch sq, 1/2 inch
  high)`, and even a float artifact `5404319552844595/18014398509481984 larger` ≈ 0.3).
- Item `name` keeps the source's `Brand - Food` wording as one string; not yet linked to
  `foods[]`.

**PII redaction rule:** any meal name containing `BigAnt` / `Big Ant` (a former workplace where
lunch was provided) is renamed with a `Take Out:` prefix, and the original text is stored
nowhere in this repository. Apply this to every future paste.

`recipes[]` / `meals[]` remain source-shaped and are not yet cross-linked to `foods[]`;
normalization is deferred to the master-format work (Stories 5/10).

## Conventions

- Ids are stable and never reused; `meta.idCounters` holds the next number per collection.
- `source` records which app the row came from (`loseit`, `myfitnesspal`, `anylist`).
- Dates are `YYYY-MM-DD`.
