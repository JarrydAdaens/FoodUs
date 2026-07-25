# jarryd/working-data

Scratch home for the owner's **master food data** while it is being recovered from old
apps (MyFitnessPal, Lose It, AnyList) during Milestone 1, Stories 5-8.

- **`master-data.json`** — the single living data file. Messy copy-pastes from the source
  apps get transformed into this. It is **mutated in place** to retain everything already
  captured while making room for new data.

This is deliberately a fast-and-messy staging area, not the final Story 5 spec. The format
here evolves as new data arrives; once it settles it feeds the canonical master format and
the Food You CSV export (Story 10).

## Format (v0.3.0)

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
- **Nutrient units** are implied by key: `energyKcal` in kcal; `cholesterol` and `sodium` in
  milligrams; every other nutrient (`fat`, `saturatedFat`, `carbs`, `fiber`, `sugars`,
  `protein`) in grams.

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

- Ingredients are captured **inline** (name + portion + calories) exactly as the source
  lists them — they are not yet linked to `foods[]` entries. Portion `unit` keeps the
  source's wording (`Grams`, `Slices`, `Each`, `Cup`, ...); fractions become decimals
  (`1/4 Cup` -> `0.25`).
- `"truncatedName": true` marks an ingredient whose name the source UI cut off (e.g.
  `"Spring Onion, Bulb and Stalk,..."`) so it can be recovered later.

`meals[]` is still empty — its shape gets defined when meal data arrives (Story 8).

## Conventions

- Ids are stable and never reused; `meta.idCounters` holds the next number per collection.
- `source` records which app the row came from (`loseit`, `myfitnesspal`, `anylist`).
- Dates are `YYYY-MM-DD`.
