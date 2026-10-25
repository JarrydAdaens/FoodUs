# FoodUs local library format, version 1

This is FoodUs's local, additive library format. It contains Food definitions (wire
`products`, internal `Product`), Recipes and saved Meals. It contains no diary,
settings, credentials or account schema. Source-specific adapters and personal
packages belong outside the checkout. Examples and public test fixtures are synthetic.

The implemented DTOs are `importexport/structuredlibrary/domain/LibraryBundle.kt`;
`StructuredLibraryParser` is the authoritative strict validator. The importer must
also validate resolved definitions and current identity mappings before atomic writes.
This document describes v1; it does not claim old APKs can import it.

## Envelope and identity

Use UTF-8 JSON without a BOM:

```json
{
  "format": "foodus-library",
  "formatVersion": 1,
  "namespace": "synthetic-library",
  "products": [],
  "recipes": [],
  "meals": []
}
```

The three arrays default to empty. Every definition requires `externalId` and `name`.
Identity is the exact case-sensitive tuple namespace/kind/externalId. Equal Food and
Recipe keys remain distinct. IDs are opaque strings; no name, nutrition or fuzzy
matching occurs. References are `{"kind":"PRODUCT","externalId":"food-1"}` or
kind `RECIPE`. Every referenced definition must occur in the same bundle. References
to Meals, duplicate identities, missing references and Recipe reference cycles fail.
Forward array references are allowed. All names are trimmed for display; raw portion
text, notes and provenance retain their supplied text. IDs are not trimmed or folded.

An identical canonical record reimport is skipped, preserving subsequent local edits.
Changed content for an existing identity or a deleted/missing mapped target is a
conflict and rejects the whole import. A new derived Recipe cannot silently calculate
against an existing dependency whose local content differs from the supplied bundle.
Existing flattened CSV Foods retain their rows and may overlap; v1 cannot establish
their original exact identities. Reconciliation is separate work.

## Nutrition, quantities and metadata

`nutrition` always has `"units":"kcal-g"` and a `values` object. Energy is kilocalories;
**all other nutrient fields are grams**, including vitamins, minerals, cholesterol and
caffeine. UI/display and database encoded milligram/microgram units are not wire units.
Values are finite nonnegative JSON numbers or null. Omitted and null facts are unknown;
explicit zero remains zero. Supplied energy is authoritative, never calculated from
macros. The following exact keys are supported:

```text
energy proteins carbohydrates fats saturatedFats transFats monounsaturatedFats
polyunsaturatedFats omega3 omega6 sugars addedSugars dietaryFiber solubleFiber
insolubleFiber salt cholesterol caffeine vitaminA vitaminB1 vitaminB2 vitaminB3
vitaminB5 vitaminB6 vitaminB7 vitaminB9 vitaminB12 vitaminC vitaminD vitaminE vitaminK
manganese magnesium potassium calcium copper zinc sodium iron phosphorus selenium
iodine chromium
```

Finite canonical values must also fit existing encoded database units; overflow when
converting a supported mass to milligrams/micrograms rejects the import. There is no
arbitrary nutrient cap or clamping. Percentage daily values never become masses.

A `PortionDescription` is optional descriptive context:

```json
{"amount":2,"unitText":"pieces","text":"2 pieces"}
```

Its optional `metricAmount` and `metricUnit` must be supplied together. `metricUnit`
is exactly `GRAM` or `MILLILITER`; amount is finite and positive. This is independent
known evidence for the described reference, never an inferred density or parsed text.
The optional `amount` is a supplied positive numeric quantity; absent amount remains
unknown. `unitText` and `text` are opaque and do not enable conversions.

Optional `metadata` contains only these fields:

```json
{
  "notes":"Synthetic note",
  "provenance":"Synthetic captured panel",
  "percentDailyValues":{"proteins":0},
  "observations":[{"kind":"energy-claim","value":150,"unitText":"kcal","text":"Separate reported claim"}],
  "textTruncated":true
}
```

All fields are optional. Percentage maps preserve finite nonnegative percentages
separately. Observations preserve bounded secondary claims/unsupported nutrient context
without changing active facts. Their optional numeric values must be finite; optional
kind/unit/text describe evidence, not a calculation instruction. `textTruncated`
records an existing truncation annotation; it cannot reconstruct missing text. Do not
embed arbitrary source objects, local database IDs or account fields.

## Foods (`products`)

```json
{
  "externalId":"food-1",
  "name":"Synthetic reference Food",
  "basis":"REFERENCE_SERVING",
  "reference":{"amount":2,"unitText":"pieces","text":"2 pieces"},
  "nutrition":{"units":"kcal-g","values":{"energy":123,"proteins":0,"fats":null}}
}
```

`REFERENCE_SERVING` means one selected Serving is the entire declared reference above:
1 serving = 2 pieces = supplied panel. Two servings double facts. Unknown metric mass
stays unknown; grams/mL conversions are unavailable. Adding an explicitly known
`metricAmount`/`metricUnit` to that reference enables only compatible metric conversions.

`METRIC_100` means per 100 g/mL facts, and requires `reference.metricAmount:100` with
explicit `metricUnit`. This declaration must be supported by the source. Optional
`packageMetric` and `servingMetric` use `{"amount":50,"unit":"GRAM"}`. All known
metric units for a Food must agree. In `REFERENCE_SERVING`, a supplied `servingMetric`
must agree exactly with the reference's known metric amount/unit. Package conversions
need a known reference-to-package ratio; descriptive package text is insufficient.
No liquid status is inferred from opaque unit text; only explicit structured metric
evidence can enable a compatible metric basis.

Optional strings are `brand`, `barcode`, `note`; `metadata` uses the shape above.

## Recipes (`recipes`)

```json
{
  "externalId":"recipe-1",
  "name":"Synthetic reported Recipe",
  "mode":"REPORTED",
  "yield":null,
  "reportedPanel":{"basis":"WHOLE_ITEM","nutrition":{"units":"kcal-g","values":{"energy":456}}},
  "components":[
    {"name":"Synthetic inline component","portion":{"amount":0.5,"unitText":"bowl","text":"about half a bowl"}}
  ]
}
```

`yield` is an optional supplied positive integer count of reference servings. Missing
yield remains unknown. `REPORTED` requires a `reportedPanel` and no derived `ingredients`.
Its `components` are ordered inline captures with required name, optional `portion`,
optional `nutrition` scoped to that complete component quantity, optional typed
`reference`, and optional `metadata`. A link is provenance; it does not read live facts.
No missing ingredient facts are derived from the panel. No dummy yield is stored.
An optional reported Recipe `metadata` object retains definition-level context
separately from its reportedPanel.metadata; their notes/claims are not merged.

A panel has required `nutrition` and `basis`, optional `reference` and `metadata`.
Panel basis is `WHOLE_ITEM`, `REFERENCE_SERVING`, `UNSPECIFIED` or `UNAVAILABLE`.
Whole-item facts resolve Package fractions; Serving conversion requires known yield.
Reference-serving facts resolve their declared Serving; Package conversion requires
known yield. Metric conversion requires an explicit compatible metric amount for the
same panel scope. Unspecified/unavailable panels remain readable but cannot be logged.
Reported Recipe unpack is unavailable in v1. Editing composition/quantity/yield/basis
marks its retained panel as needing review until the user confirms or replaces it.

`DERIVED` requires positive known yield and nonempty ordered `ingredients`, with no
reportedPanel/components. Each ingredient is:

```json
{"reference":{"kind":"PRODUCT","externalId":"food-1"},"portion":{"amount":1,"unit":"SERVING"}}
```

Structured portion units are `GRAM`, `MILLILITER`, `OUNCE`, `FLUID_OUNCE`, `SERVING`,
`PACKAGE`; amounts must be finite positive and resolvable against their typed definition.
Derived Recipes retain FoodUs's existing ingredient calculation and unpack behavior;
v1 derived definitions support `note`, with no separate metadata/panel payload.
An optional Recipe `note` is retained. Unknown/incomplete compositions use reported
mode honestly rather than pretending to be linked derived Recipes.

## Meals (`meals`)

```json
{
  "externalId":"meal-1",
  "name":"Synthetic saved Meal",
  "items":[
    {"name":"Synthetic captured constituent","nutrition":{"units":"kcal-g","values":{"energy":90}},
     "portion":{"amount":3,"unitText":"pieces","text":"3 pieces"},"servingCount":3}
  ],
  "reportedPanel":{"basis":"WHOLE_ITEM","nutrition":{"units":"kcal-g","values":{"energy":95}}}
}
```

Items are ordered **absolute** snapshots. Three descriptive pieces/servings above do
not turn 90 kcal into 270 kcal. Optional item fields: `portion`, `capturedPortion`
(structured unit/amount as for ingredients), positive `servingCount`, positive
`weightGrams`, typed provenance-only `reference`, and `metadata`. Name/nutrition are
required. A reported Meal panel and Meal metadata are optional and independent of the
constituent sum; differences and incomplete facts remain visible. Loading creates one
entry per absolute constituent, without balancing against the reported panel. Later
definition edits/deletion cannot change these snapshots or their captured provenance.

## Limits, canonicalization and failure behavior

Limits are 16 MiB UTF-8 bytes; 20,000 definitions; 100,000 total components/ingredients/
Meal items; 10,000 items per definition; Recipe/JSON nesting depth 64. Namespace is at
most 128 characters, IDs 256, names/brand 512, barcode 256, individual note/description
text 16,384, unit text 256. Metadata totals at most 65,536 encoded UTF-8 bytes per
definition, including panel/component/item metadata. Each metadata object permits
256 percentage entries and 256 observations; percentage labels/observation kind are
at most 128 characters. Unknown structural fields, duplicate decoded JSON keys,
unsupported versions/enums/units, malformed UTF-8/JSON, invalid numeric values and
invalid graphs fail before persistent writes.
Numbers and booleans must be JSON tokens of their declared type, not quoted strings.
Numbers use JSON's decimal grammar; leading zeroes, a leading plus, hexadecimal,
type suffixes, NaN and Infinity are rejected before numeric decoder coercion.
Numbers outside the finite Double range, including a nonzero token that would
underflow to zero, are rejected instead of being changed into another fact.

Canonical per-record SHA-256 includes all retained semantic fields and list order.
It normalizes object/map order, explicit versus omitted defaults/nulls, omitted versus
null unknown nutrient fields, signed numeric zero and trimmed display names. It does
not hash JSON formatting. Namespace and kind remain separate ledger key fields.

Preview/cancel does not modify the library. Confirm applies the validated immutable
draft in one transaction with identity mappings and history. Conflicts, validation
failure, cancellation or an intermediate write failure leave no partial imported rows.
Existing diary/settings/credentials remain unchanged. Errors contain only a stable
generic code and optional record kind/index, never input values or raw decoder errors.
The legacy CSV exporter remains a metric-only format; unconvertible reference facts
must fail clearly and direct the user to native database backup rather than mislabeled
per-100 nutrition. Structured export is outside v1.
