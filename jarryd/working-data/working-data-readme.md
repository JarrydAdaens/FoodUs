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

## Staging history

This folder began as a deliberately fast-and-messy staging area: messy copy-pastes from the
source apps were LLM-transformed into `master-data.json`, and the format grew organically
(v0.x) as each source's data arrived. Once the data settled, Story 5 promoted the working
format to canonical v1.0.0. The file keeps its original path for git-history continuity.
