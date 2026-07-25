#!/usr/bin/env python3
"""Export master-data.json (format v1.x) to a Food You products CSV.

Standalone tool per Milestone 1 Story 10 — lives outside the app codebase and
touches nothing under app/ or shared/. Reads the canonical master data store
(spec: context/wiki/master-data-format.md) and writes the 51-column products
CSV Food You v3.4.9 imports (spec: context/wiki/foodyou-products-csv-schema.md).

Mapping summary (see the Story 10 plan for rationale):
- foods[]            -> one product row each.
- recipes[]          -> one collapsed product row each (a recipe collapses into
                        a single diary entry, so its product row IS that form).
- meals[]            -> one row per distinct meal item (a meal expands into its
                        items on insertion, so the items must exist as
                        products); no meal-total row is emitted.
- groceryProducts[]  -> one row each, with barcode / brand / source URL.

Nutrition basis is per 100 g. Where no gram weight is known the
"1 serving = 100 g" convention applies: per-serving values are written as the
per-100 g values with Serving Weight = 100, so logging "1 serving" (100 g)
yields the correct per-serving figures. Affected rows are flagged in Note.

Stdlib only. Usage (from the repository root):

    python jarryd/scripts/export_foodyou_csv.py
"""

from __future__ import annotations

import argparse
import csv
import datetime
import json
import re
import sys
from pathlib import Path

# The 51 CSV headers, verbatim and in order, from
# context/wiki/foodyou-products-csv-schema.md (verified against the app's
# ProductField.kt / CsvHeaders.kt).
HEADERS = [
    "Name", "Brand", "Barcode", "Note", "Is Liquid",
    "Package Weight (g)", "Serving Weight (g)", "Source URL",
    "Proteins (g)", "Carbohydrates (g)", "Energy (kcal)", "Fats (g)",
    "Saturated Fats (g)", "Trans Fats (g)", "Monounsaturated Fats (g)",
    "Polyunsaturated Fats (g)", "Omega-3 (g)", "Omega-6 (g)", "Sugars (g)",
    "Added Sugars (g)", "Dietary Fiber (g)", "Soluble Fiber (g)",
    "Insoluble Fiber (g)", "Salt (g)", "Cholesterol (g)", "Caffeine (g)",
    "Vitamin A (g)", "Vitamin B1 (g)", "Vitamin B2 (g)", "Vitamin B3 (g)",
    "Vitamin B5 (g)", "Vitamin B6 (g)", "Vitamin B7 (g)", "Vitamin B9 (g)",
    "Vitamin B12 (g)", "Vitamin C (g)", "Vitamin D (g)", "Vitamin E (g)",
    "Vitamin K (g)", "Manganese (g)", "Magnesium (g)", "Potassium (g)",
    "Calcium (g)", "Copper (g)", "Zinc (g)", "Sodium (g)", "Iron (g)",
    "Phosphorus (g)", "Selenium (g)", "Iodine (g)", "Chromium (g)",
]

STRING_COLUMNS = {"Name", "Brand", "Barcode", "Note", "Source URL"}

# Master nutrient key -> (CSV column, divisor). Master stores sodium,
# cholesterol and potassium in mg; the CSV wants grams everywhere.
NUTRIENT_MAP = {
    "protein": ("Proteins (g)", 1),
    "carbs": ("Carbohydrates (g)", 1),
    "energyKcal": ("Energy (kcal)", 1),
    "fat": ("Fats (g)", 1),
    "saturatedFat": ("Saturated Fats (g)", 1),
    "transFat": ("Trans Fats (g)", 1),
    "monounsaturatedFat": ("Monounsaturated Fats (g)", 1),
    "polyunsaturatedFat": ("Polyunsaturated Fats (g)", 1),
    "sugars": ("Sugars (g)", 1),
    "fiber": ("Dietary Fiber (g)", 1),
    "salt": ("Salt (g)", 1),
    "cholesterol": ("Cholesterol (g)", 1000),
    "sodium": ("Sodium (g)", 1000),
    "potassium": ("Potassium (g)", 1000),
}

KJ_PER_KCAL = 4.184

# Former workplace name (PII, redacted repository-wide). The pattern is built
# non-literally so the name itself never appears in this repository; it matches
# the redacted string with or without an internal space, case-insensitively.
PII_PATTERN = re.compile(r"big\s*ant", re.IGNORECASE)

GRAM_UNITS = {"g", "gram", "grams"}
# Meal-item portions are verbatim source strings; only unambiguous pure-gram
# shapes like "40 g" or "100/1 gram" are treated as a known gram weight.
GRAM_PORTION_RE = re.compile(r"^(\d+(?:\.\d+)?)\s*(?:/1)?\s*(?:g|gram|grams)$", re.IGNORECASE)
GRAM_SERVING_SIZE_RE = re.compile(r"^(\d+(?:\.\d+)?)\s*g$", re.IGNORECASE)

CONVENTION_NOTE = "serving=100g convention"


def map_nutrients(source: dict) -> tuple[dict[str, float], list[str]]:
    """Convert master nutrient keys/units to CSV columns. Returns (values, notes)."""
    values: dict[str, float] = {}
    notes: list[str] = []
    for key, raw in source.items():
        if key in ("basis", "servingSize"):
            continue
        if key in NUTRIENT_MAP:
            column, divisor = NUTRIENT_MAP[key]
            values[column] = raw / divisor
        elif key == "energyKj":
            if "energyKcal" not in source:
                values["Energy (kcal)"] = raw / KJ_PER_KCAL
        elif key == "unsaturatedFat":
            # One combined figure; the CSV splits mono/poly, so an honest
            # mapping is impossible — dropped, flagged instead.
            notes.append(f"unsaturated fat {raw} g not split into mono/poly")
        # micronutrientPercentDV blocks are %DV, not grams — never mapped.
    return values, notes


def scale_to_100g(values: dict[str, float], grams: float) -> dict[str, float]:
    return {column: value * 100 / grams for column, value in values.items()}


def make_row(name: str, **fields) -> dict:
    row = {"Name": name}
    row.update(fields)
    return row


def rows_from_foods(foods: list[dict]) -> list[dict]:
    rows = []
    for food in foods:
        values, notes = map_nutrients(food["nutrients"])
        serving = food["serving"]
        row = make_row(food["name"], Brand=food.get("brand"))
        if str(serving["unit"]).lower() in GRAM_UNITS:
            grams = serving["amount"]
            row.update(scale_to_100g(values, grams))
            row["Serving Weight (g)"] = grams
        else:
            row.update(values)
            row["Serving Weight (g)"] = 100
            notes.append(
                f"{CONVENTION_NOTE} (actual serving: {serving['amount']} {serving['unit']})"
            )
        if food.get("notes"):
            notes.append(food["notes"])
        row["Note"] = "; ".join(notes) or None
        rows.append(row)
    return rows


def rows_from_recipes(recipes: list[dict]) -> list[dict]:
    rows = []
    for recipe in recipes:
        nutrition = recipe["nutrition"]
        basis = nutrition["basis"]
        values, notes = map_nutrients(nutrition)
        ingredients = "; ".join(item["name"] for item in recipe["ingredients"])
        summary = f"Recipe ({recipe['source']}): {len(recipe['ingredients'])} ingredients: {ingredients}"
        row = make_row(recipe["name"])

        serving_grams = None
        serving_size = nutrition.get("servingSize")
        if serving_size:
            match = GRAM_SERVING_SIZE_RE.match(serving_size.strip())
            if match:
                serving_grams = float(match.group(1))

        if basis == "not shown":
            values = {}
            notes.append("no nutrition shown in source")
        elif serving_grams is not None:
            values = scale_to_100g(values, serving_grams)
            row["Serving Weight (g)"] = serving_grams
        else:
            row["Serving Weight (g)"] = 100
            if basis == "total":
                notes.append(f"nutrition is the recipe TOTAL; {CONVENTION_NOTE} (1 serving = whole recipe)")
            elif basis == "unspecified":
                notes.append(f"nutrition basis unspecified in source; {CONVENTION_NOTE}")
            else:  # per serving, gram weight unknown
                notes.append(CONVENTION_NOTE)
            if recipe.get("servings"):
                notes.append(f"makes {recipe['servings']} servings")

        row.update(values)
        row["Note"] = "; ".join([summary] + notes)
        rows.append(row)
    return rows


def rows_from_meals(meals: list[dict]) -> tuple[list[dict], list[str]]:
    """One row per distinct meal item (deduped); no meal-total rows.

    Dedup key is (normalized name, per-100 g nutrient values), so the same item
    logged at different gram weights merges once scaled, while same-named items
    with genuinely different nutrition stay separate rows.
    """
    deduped: dict[tuple, dict] = {}
    merged_names: list[str] = []
    for meal in meals:
        for item in meal["items"]:
            values, notes = map_nutrients(item["nutrients"])
            portion = item["portion"].strip()
            match = GRAM_PORTION_RE.match(portion)
            if match:
                grams = float(match.group(1))
                values = scale_to_100g(values, grams)
                serving_weight = grams
                convention = None
            else:
                serving_weight = 100
                convention = f"{CONVENTION_NOTE} (source portion: {portion})"

            name = " ".join(item["name"].split())
            key = (name.lower(), tuple(sorted((c, round(v, 6)) for c, v in values.items())))
            if key in deduped:
                entry = deduped[key]
                if meal["name"] not in entry["meals"]:
                    entry["meals"].append(meal["name"])
                merged_names.append(name)
                continue
            deduped[key] = {
                "name": name,
                "values": values,
                "serving_weight": serving_weight,
                "convention": convention,
                "extra_notes": notes,
                "meals": [meal["name"]],
            }

    rows = []
    for entry in deduped.values():
        notes = [f"From meal(s): {', '.join(entry['meals'])}"]
        if entry["convention"]:
            notes.append(entry["convention"])
        notes.extend(entry["extra_notes"])
        row = make_row(entry["name"], Note="; ".join(notes))
        row["Serving Weight (g)"] = entry["serving_weight"]
        row.update(entry["values"])
        rows.append(row)
    return rows, merged_names


def rows_from_grocery(grocery_products: list[dict]) -> list[dict]:
    rows = []
    for grocery in grocery_products:
        product = grocery.get("product")
        name = product["name"] if product else grocery["listItem"]
        row = make_row(
            name,
            Brand=product.get("brand") if product else None,
            Barcode=grocery.get("barcode"),
        )
        row["Source URL"] = grocery.get("nutritionSource") or grocery.get("barcodeSource")
        notes = [f"Grocery catalog: {grocery['listItem']} ({grocery['category']})"]

        nutrition = grocery.get("nutrition")
        if nutrition is None:
            notes.append("no nutrition researched")
        else:
            values, extra = map_nutrients(nutrition)
            row.update(values)
            notes.extend(extra)
            if nutrition["basis"] == "per 100 mL":
                row["Is Liquid"] = 1
                notes.append("nutrition per 100 mL (density ~1 assumed)")
        row["Note"] = "; ".join(notes)
        rows.append(row)
    return rows


def format_number(value) -> str:
    text = f"{float(value):.6f}".rstrip("0").rstrip(".")
    return text if text not in ("", "-0") else "0"


def format_field(column: str, value) -> str:
    """RFC-4180 field: strings quoted with " doubled; numbers bare; None empty."""
    if value is None:
        return ""
    if column in STRING_COLUMNS:
        text = str(value)
        if '"' in text or "," in text or "\n" in text or "\r" in text:
            text = '"' + text.replace('"', '""') + '"'
        else:
            text = f'"{text}"'  # match the app's own export shape: strings always quoted
        return text
    if column == "Is Liquid":
        return "1" if value else "0"
    return format_number(value)


def write_csv(path: Path, rows: list[dict]) -> None:
    lines = [",".join(f'"{header}"' for header in HEADERS)]
    for row in rows:
        unknown = set(row) - set(HEADERS)
        if unknown:
            raise SystemExit(f"internal error: unmapped columns {unknown}")
        fields = [format_field(column, row.get(column)) for column in HEADERS]
        if len(fields) != 51:
            raise SystemExit("internal error: row does not have 51 fields")
        lines.append(",".join(fields))
    path.write_text("\n".join(lines) + "\n", encoding="utf-8", newline="\n")


def self_check(path: Path) -> None:
    """Fail the run (exit 1) if the produced file violates the CSV contract."""
    content = path.read_text(encoding="utf-8")
    if PII_PATTERN.search(content):
        raise SystemExit("SELF-CHECK FAILED: redacted workplace name found in output")

    with path.open(encoding="utf-8", newline="") as handle:
        parsed = list(csv.reader(handle))
    if parsed[0] != HEADERS:
        raise SystemExit("SELF-CHECK FAILED: header row mismatch")
    numeric_indexes = [
        i for i, header in enumerate(HEADERS)
        if header not in STRING_COLUMNS and header != "Is Liquid"
    ]
    for line_number, record in enumerate(parsed[1:], start=2):
        if len(record) != 51:
            raise SystemExit(
                f"SELF-CHECK FAILED: line {line_number} has {len(record)} columns, expected 51"
            )
        if not record[0].strip():
            raise SystemExit(f"SELF-CHECK FAILED: line {line_number} has a blank Name")
        for index in numeric_indexes:
            if record[index]:
                try:
                    float(record[index])
                except ValueError:
                    raise SystemExit(
                        f"SELF-CHECK FAILED: line {line_number} column "
                        f"{HEADERS[index]!r} is not numeric: {record[index]!r}"
                    )
    print(f"Self-checks passed: {len(parsed) - 1} data rows, 51 columns, no PII, all names present.")


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__.splitlines()[0])
    default_output = (
        f"jarryd/working-data/exports/foodyou-products-{datetime.date.today():%Y-%m-%d}.csv"
    )
    parser.add_argument("--input", default="jarryd/working-data/master-data.json")
    parser.add_argument("--output", default=default_output)
    args = parser.parse_args()

    data = json.loads(Path(args.input).read_text(encoding="utf-8"))
    version = data.get("formatVersion", "")
    if not version.startswith("1."):
        raise SystemExit(
            f"Unsupported master data formatVersion {version!r}: this exporter targets 1.x. "
            "Review the format change before exporting."
        )

    food_rows = rows_from_foods(data["foods"])
    recipe_rows = rows_from_recipes(data["recipes"])
    meal_rows, merged = rows_from_meals(data["meals"])
    grocery_rows = rows_from_grocery(data["groceryProducts"])

    # Cross-group dedupe on exact (Name, Brand) — first group wins. Same-name
    # rows within one group are intentional nutrient variants (kept per plan);
    # only a collision with an earlier group drops a row.
    all_rows: list[dict] = []
    seen: dict[tuple, str] = {}
    groups = [("foods", food_rows), ("recipes", recipe_rows),
              ("meal items", meal_rows), ("grocery products", grocery_rows)]
    counts = {}
    for group_name, rows in groups:
        kept = 0
        for row in rows:
            key = (row["Name"].lower(), (row.get("Brand") or "").lower())
            if key in seen and seen[key] != group_name:
                print(f"  cross-group duplicate skipped: {row['Name']!r} "
                      f"({group_name}, already emitted from {seen[key]})")
                continue
            seen[key] = group_name
            all_rows.append(row)
            kept += 1
        counts[group_name] = kept

    output_path = Path(args.output)
    output_path.parent.mkdir(parents=True, exist_ok=True)
    write_csv(output_path, all_rows)

    print(f"Wrote {output_path} ({len(all_rows)} rows)")
    for group_name, kept in counts.items():
        print(f"  {group_name}: {kept}")
    if merged:
        print(f"  meal-item occurrences merged by dedup: {len(merged)}")
    self_check(output_path)


if __name__ == "__main__":
    main()
