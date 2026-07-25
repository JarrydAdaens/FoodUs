# jarryd/working-data

Home of the owner's **master food data**, recovered from old apps (MyFitnessPal, Lose It,
AnyList) during Milestone 1, Stories 5-8.

- **`master-data.json`** — the single living data file, the **canonical store** from which
  app-specific exports (e.g. the Food You CSV, Story 10) are generated. It is mutated in
  place; git history is the rollback mechanism.
- **`master-data.schema.json`** — the JSON Schema (draft 2020-12) the data file must
  validate against.

## Format

The format is fully specified in
[`context/wiki/master-data-format.md`](../../context/wiki/master-data-format.md) — the spec
of record (Story 5). It covers the five collections, the recipe envelope and per-source
shapes, unit conventions, explicit-zero semantics, verbatim-preservation and
anti-fabrication rules, id rules, the PII redaction rule, and the `formatVersion` semver
policy. The spec also carries the validation one-liner to run after every mutation.

Do not restate format rules here; the spec is the single definition.

## Export to Food You CSV (Story 10)

Generate the app-import CSV from the repository root:

```sh
python jarryd/scripts/export_foodyou_csv.py
```

Output lands in `exports/foodyou-products-<yyyy-mm-dd>.csv` (untracked — derived
output, regenerable at will). The script validates `formatVersion` (targets 1.x),
maps foods, recipes (one collapsed row each), meal items (deduped, no meal-total
rows), and grocery products (with barcodes) onto Food You's 51-column products
CSV, and runs self-checks (column count, Name presence, PII scan, numeric
round-trip) on every run. Rows whose gram weight is unknown use the
"1 serving = 100 g" convention and are flagged in their `Note`; per-100 mL
grocery items are written as-is with `Is Liquid = 1`. Validate the data file
against the schema before exporting (one-liner in the format spec).

## Staging history

This folder began as a deliberately fast-and-messy staging area: messy copy-pastes from the
source apps were LLM-transformed into `master-data.json`, and the format grew organically
(v0.x) as each source's data arrived. Once the data settled, Story 5 promoted the working
format to canonical v1.0.0. The file keeps its original path for git-history continuity.
