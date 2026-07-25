# Plan: Define the Master Data Format

## Metadata

- Task Type: `STORY`
- Status: `Complete`
- Owner: Jarryd Adaens
- Last Updated: 25 July 2026

## Linked Context

- Milestone: [context/milestones/milestone-1.md](../../../milestones/milestone-1.md)
- Story: [Story 5: Define the master data format](../../../milestones/milestone-1.md#story-5)
- Backlog source: none — story mapped directly from the 2026-07-24 initial project seed
- Related Plans:
  - [Story 10: Build the export script](../story-10-export-script/plan.md) — consumes this
    format; its open Q1 ("target v0.8.0 or wait for Story 5?") is **resolved by this story
    landing first**: the exporter targets the canonical format this story defines.
- External Tooling: `commit-log` skill; `rails-grade-cer` if formal grading is wanted

## CER

- Complexity: 4
- Effort: 3
- Risk: 3
- Notes: Inline estimate by the planning agent, not a formal `rails-grade-cer` grade.
  Complexity carries the real weight — the recipe collections exist in three source-specific
  shapes and the normalization decision has lossy-vs-faithful trade-offs. Effort is moderate: a
  spec document, a JSON Schema, and one in-place migration of a ~100 KB file. Risk is a 3
  because the migration rewrites the only copy of recovered data — fully mitigated by git
  history, schema validation, and zero-loss checks, but it is still the owner's recovered years
  of data being transformed.

## Objective

Promote the de facto working format (`master-data.json` v0.8.0, grown organically through
Stories 6-8) into the **canonical master data format v1.0.0**: a fully specified, app-independent
schema document plus a machine-validatable JSON Schema, with the existing data migrated to
conform — so Story 10's exporter, and any future app's exporter, has a stable contract instead
of a staging file.

## Scope

### In Scope

- The format decisions the working readme explicitly deferred to this story: recipe-shape
  normalization, cross-linking policy, whether `groceryProducts` is first-class, and the
  versioning policy for future mutations (Story 8's remaining AnyList meals still need to land).
- A canonical specification document: `context/wiki/master-data-format.md` (spec of record —
  collections, shapes, unit conventions, id rules, provenance/verbatim rules, version policy).
- A JSON Schema: `jarryd/working-data/master-data.schema.json`, strict enough to catch drift.
- Migration of `jarryd/working-data/master-data.json` from v0.8.0 → 1.0.0 with zero data loss.
- Updating `working-data-readme.md` to point at the spec as the source of truth (the readme's
  format section becomes a pointer, not a second definition — DRY).
- Closing Story 10's Q1 in its plan (answer: target v1.0.0).

### Out Of Scope

- The CSV export itself — Story 10.
- Populating cross-links (matching recipe ingredients / meal items to `foods[]` /
  `groceryProducts[]` entries) — the spec defines the field; filling it is future data work.
- Recovering historical diary entries (the `diary` collection is currently empty; the spec
  defines its shape per the domain model, nothing more).
- Recipe-vs-meal reclassification of AnyList entries
  ([backlog-1 Story 3](../../../backlog/backlog-1.md#story-3)) — a data change, not a format
  change; the format must merely support moving an entry between collections.
- Any app code. This story lives entirely in `jarryd/` and `context/`.

## Non-Goals

- No lossy normalization: the fast-and-messy extractions preserved verbatim strings (`raw`,
  meal-item `portion`, AnyList `quantity`) precisely because parsing was unreliable — the
  canonical format keeps them.
- No premature generalization for hypothetical future apps; the format serves this household's
  data and the Food You export. It stays JSON, single file, human-diffable.

## Current Understanding

Verified against the live file on 2026-07-25:

- `jarryd/working-data/master-data.json` — `formatVersion` 0.8.0; `meta.idCounters`
  `{food:6, recipe:23, meal:49, diary:0, grocery:31}` matching actual counts: 6 foods,
  23 recipes (8 `anylist`, 5 `loseit`, 10 `myfitnesspal`), 49 meals (all `myfitnesspal`),
  0 diary entries, 31 groceryProducts (21 per-100 g, 7 per-100 mL, 3 with no nutrition).
- `jarryd/working-data/working-data-readme.md` — the de facto format definition: unit
  conventions (kcal; mg for sodium/cholesterol/potassium; grams otherwise; percent-DV
  micronutrients kept separate), explicit-zero vs absent-value semantics, verbatim-preservation
  conventions, PII redaction rule ("Take Out:" replaces the former workplace name), stable-id
  and `source` conventions.
- The three recipe shapes (the normalization problem this story must solve):
  - `loseit`: `totalCalories` + per-ingredient `energyKcal`, parsed `portion`.
  - `myfitnesspal`: `servings`, `caloriesPerServing`, full `perServingNutrients` +
    `perServingMicronutrientPercentDV`, ingredients with `raw` verbatim and no calories.
  - `anylist`: verbatim `quantity` strings, self-describing `nutrition.basis`
    (`per serving` / `unspecified` / `not shown`), `energyKj`/`salt`/`unsaturatedFat` keys.
- Downstream contract: Story 10's plan maps foods/recipes/meals/groceryProducts onto Food You's
  51-column products-only CSV (`context/wiki/foodyou-products-csv-schema.md`) and guards on
  `formatVersion` — it fails loudly on anything but the version it targets.
- Design constraints (`context/design.md`): domain model defines custom food / recipe
  (collapses) / meal (expands) / diary entry; data ownership is a founding goal — the master
  format is the canonical store, app formats are generated exports.
- Assumptions and constraints:
  - Story 8 is still In Progress: AnyList meal-plan data will arrive **after** v1.0.0, so the
    version policy must define how additive data lands (minor bump) vs shape changes (major).
  - Git history is the rollback mechanism; the file is committed before and after migration.
  - PII rule is constitutional: the former workplace name (see agent memory "Food data PII
    redaction") may survive into no artifact — entries are renamed with a `Take Out:` prefix.

## Questions / Unknowns

- Q: [STORY 5] Normalize the three recipe shapes into one canonical shape, or keep them
  source-shaped under a common envelope?
  Impact: The central design decision. Full normalization gives Story 10 one code path but
  forces lossy unit/basis coercion; pure source-shapes push per-source logic into every
  consumer forever.
  Assumption: **Canonical envelope + faithful source detail**: every recipe gets required
  common fields (`id`, `name`, `source`, `ingredients[]`, and a normalized `nutrition` block
  with an explicit `basis` like AnyList's), while source-specific fields are kept in place and
  documented per-source in the spec. Nothing is discarded; consumers read the common envelope
  and may ignore the rest.
  Status: ANSWERED — proceeded on assumption, 2026-07-25. Implemented as envelope + faithful
  source detail: `nutrition` (with required `basis`) is now the canonical block on every
  recipe; Lose It `totalCalories` became `nutrition {basis: "total", energyKcal}` and
  MyFitnessPal `perServingNutrients` became `nutrition {basis: "per serving", ...}` (rename,
  not duplication — DRY); all other source fields (`servings`, `caloriesPerServing`,
  `perServingMicronutrientPercentDV`, ingredient shapes) kept in place.
- Q: [STORY 5] Does the canonical file stay at `jarryd/working-data/master-data.json`, or move
  to a non-"working" home (e.g. `jarryd/master-data/master-data.json`)?
  Impact: Path is baked into Story 10's plan and the readme; "working-data" signals staging,
  which the file no longer is once canonical.
  Assumption: Keep the current path — git history continuity beats naming purity; the readme
  and spec state its canonical status.
  Status: ANSWERED — proceeded on assumption, 2026-07-25. File stays at
  `jarryd/working-data/master-data.json`; the readme's staging-history note explains why.
- Q: [STORY 5] Is `groceryProducts` a first-class collection of the master format?
  Impact: It is absent from the original domain model but present, sourced, and consumed by
  Story 10's export mapping.
  Assumption: Yes — first-class, spec'd as-is (it already has the strongest conventions:
  anti-fabrication nulls, source URLs, confidence ratings).
  Status: ANSWERED — proceeded on assumption, 2026-07-25. `groceryProducts` is first-class in
  the spec and schema, specified as-is with its nullability and provenance rules.
- Q: [STORY 5] How should cross-linking (ingredient/meal item → `foods[]`/`groceryProducts[]`)
  be represented?
  Impact: Determines whether future de-duplication work is a format change or a data change.
  Assumption: The spec defines an **optional** `foodId` on ingredients and meal items; all
  current entries omit it. Population is deferred data work, not a version bump.
  Status: ANSWERED — proceeded on assumption, 2026-07-25. Optional `foodId` (pattern
  `^(food|grocery)-\d{4}$`) is defined on all recipe-ingredient and meal-item shapes in the
  spec and schema; no entry populates it.
- Q: [STORY 5] Validation tooling: JSON Schema draft 2020-12 checked via
  `uv run --with jsonschema` (or `check-jsonschema`), or schema-as-documentation only?
  Impact: A runnable check catches drift on every future paste session; documentation-only is
  zero-dependency but rots.
  Assumption: Runnable — a one-line `uv` invocation documented in the spec; no new committed
  tooling beyond the schema file itself.
  Status: ANSWERED — proceeded on assumption, 2026-07-25. The spec documents the
  `uv run --with jsonschema` one-liner; it was run and passes against the migrated file. The
  committed schema is standard JSON Schema draft 2020-12; no tooling committed.

## Execution Steps

1. Confirm the four design assumptions above with the owner (one short exchange).
   - Why: Every downstream artifact (spec, schema, migration) encodes these answers.
   - Edits: none.
   - Dependencies: blocks steps 2-5.

2. Write the canonical specification `context/wiki/master-data-format.md`.
   - Why: The spec of record — Story 5's actual deliverable per the milestone.
   - Edits: new wiki page covering: purpose and canonical-store role; the five collections
     (`foods`, `recipes`, `meals`, `diary`, `groceryProducts`) with required/optional fields;
     the recipe envelope + per-source detail blocks; unit conventions and explicit-zero
     semantics (lifted from the readme, now normative); verbatim-preservation and
     anti-fabrication principles; id and `meta.idCounters` rules; PII redaction rule;
     `formatVersion` semver policy (major = shape change, minor = additive fields/collections,
     patch = data-only never bumps); link from `context/wiki/home.md`.
   - Dependencies: step 1.

3. Author `jarryd/working-data/master-data.schema.json`.
   - Why: Machine-checkable contract; catches drift during Story 8's remaining pastes and
     before every Story 10 export run.
   - Edits: new JSON Schema (draft 2020-12) mirroring the spec — strict on required fields,
     enums (`source`, `basis`, `confidence`), unit-implying key names, and
     `additionalProperties` policy per the step-1 envelope decision.
   - Dependencies: step 2.

4. Migrate `master-data.json` v0.8.0 → 1.0.0.
   - Why: The canonical format is only real once the real data conforms.
   - Edits: `formatVersion` → `1.0.0`; apply the envelope normalization from step 1's answer
     (e.g. ensure every recipe has the common `nutrition`/`basis` block); no value dropped, no
     verbatim string altered. Perform via a throwaway transform run in the scratchpad — nothing
     but the data file and schema land in the repo.
   - Dependencies: steps 1-3.

5. Repoint the readme and close the loop with Story 10.
   - Why: One definition of truth (DRY); Story 10 must not target a dead version.
   - Edits: `working-data-readme.md` — replace the format section with a pointer to the spec +
     schema, keep the staging-history note; Story 10 `plan.md` — mark Q1 ANSWERED (target
     v1.0.0, guard updated accordingly).
   - Dependencies: steps 2-4.

## Validation

### Automated Checks

- Schema validation passes on the migrated `master-data.json`
  (`uv run --with jsonschema ...` one-liner, recorded in the spec).
- Zero-loss checks against the pre-migration file (git `HEAD` copy): collection counts
  unchanged (6/23/49/0/31), id sets identical, and every verbatim field (`raw`, meal-item
  `portion`, AnyList `quantity`) byte-identical.
- PII scan: case-insensitive grep for the former workplace name (per agent memory "Food data
  PII redaction") over all changed files returns nothing.

### Manual Checks

1. Owner reads the spec and confirms the envelope decision reads back as intended.
2. Spot-diff three migrated recipes — one per source — against their v0.8.0 forms.

### Acceptance Criteria

- `context/wiki/master-data-format.md` fully specifies v1.0.0 — a new agent could validate or
  extend the data from the spec alone, without this plan or chat history.
- `master-data.json` declares `formatVersion: "1.0.0"` and validates against the schema.
- No data value, verbatim string, or provenance field was lost or altered in migration.
- The readme no longer duplicates the format definition.
- Story 10's plan targets v1.0.0 (its Q1 closed).

## Risk Mitigation

- Risk: Migration silently corrupts or drops recovered data.
  Mitigation: Git commit before migration; automated zero-loss checks (counts, id sets,
  verbatim-field byte-equality); per-source spot diffs; the transform itself never lands in the
  repo so it cannot be "helpfully" rerun later.
- Risk: Normalization decision proves wrong once Story 10 implements the export.
  Mitigation: The envelope approach is additive over source shapes — reverting it is a minor
  edit, and Story 10's version guard makes any mismatch loud, not silent.
- Risk: Story 8's remaining AnyList meal data arrives and doesn't fit v1.0.0.
  Mitigation: The spec's version policy explicitly reserves minor bumps for additive
  accommodation; the schema is updated in the same commit as the data that needs it.
- Risk: Spec drifts from schema drifts from data over time.
  Mitigation: Single validation one-liner documented in the spec; the readme points at the
  spec instead of restating it.

## Phase Split

Not needed — CER is under threshold; single-pass story.

## Evidence / References

- Planning inputs: live inspection of `master-data.json` (2026-07-25: formatVersion 0.8.0,
  counts 6/23/49/0/31, recipe sources 8+5+10, grocery bases 21+7+3);
  `jarryd/working-data/working-data-readme.md` (full v0.8.0 conventions);
  `context/design.md` (domain model, data-ownership goal);
  `context/milestones/milestone-1.md` (Story 5, Interdependency Order);
  `context/implementation-plans/milestone-1/story-10-export-script/plan.md` (consumer contract
  and its Q1); `context/wiki/foodyou-products-csv-schema.md` (downstream CSV target).
- Unverified claims: none material — all counts and shapes read from the live file this
  session; the file mutates as Story 8 continues, so re-verify counts at execution time.

## Execution Log

- 2026-07-25 — Re-verified the live file before any change: `formatVersion` 0.8.0, counts
  6/23/49/0/31 and recipe sources 8 anylist + 5 loseit + 10 myfitnesspal, matching the
  planning-time inspection exactly. No new shapes had landed since planning (Story 8's final
  state introduced nothing beyond what the plan captured), so all five recorded assumptions
  were proceeded on without owner confirmation (owner unavailable mid-run; each is marked
  ANSWERED above with "proceeded on assumption").
- 2026-07-25 — Wrote the spec of record `context/wiki/master-data-format.md` (v1.0.0:
  collections, recipe envelope + per-source detail, unit conventions, explicit-zero
  semantics, verbatim-preservation/anti-fabrication principles, id rules, PII redaction
  rule, semver policy, validation one-liner) and linked it from `context/wiki/home.md`.
- 2026-07-25 — Authored `jarryd/working-data/master-data.schema.json` (JSON Schema draft
  2020-12): strict `additionalProperties`/`unevaluatedProperties: false` throughout, enums
  for `source`/`basis`/`confidence`/`grocery.status`, id patterns, per-source recipe
  variants via `oneOf`, shared nutrient-key vocabulary with units documented in
  descriptions.
- 2026-07-25 — Migrated `master-data.json` 0.8.0 → 1.0.0 via a surgical text transform run
  from the session scratchpad (script not committed, per plan): `formatVersion` bump,
  `meta.description` updated to canonical wording, 5 Lose It `totalCalories` →
  `nutrition {basis: "total", energyKcal}`, 10 MyFitnessPal `perServingNutrients` →
  `nutrition {basis: "per serving", ...}`. Every untouched byte preserved
  (diff: 27 insertions, 17 deletions across 44 lines).
- 2026-07-25 — Validation (all pass): schema validation via the spec's
  `uv run --with jsonschema` one-liner; zero-loss checks vs the git `HEAD` copy —
  collection counts 6/23/49/0/31 unchanged, id sequences identical, verbatim fields
  byte-identical (69 `raw`, 79 AnyList `quantity`, 219 meal-item `portion`), and full-document
  deep-equality after reverse-transforming the envelope (excluding only `formatVersion` and
  `meta.description`); PII scan over all changed files clean.
- 2026-07-25 — Repointed `working-data-readme.md` at the spec (staging history kept as a
  note) and closed Story 10's Q1 (ANSWERED: exporter targets v1.0.0, guard accepts `1.x`).

## Completion Review

- All acceptance criteria met:
  - The spec fully defines v1.0.0; a new agent can validate or extend the data from
    `context/wiki/master-data-format.md` plus the schema alone.
  - `master-data.json` declares `formatVersion: "1.0.0"` and validates against
    `master-data.schema.json`.
  - Zero loss proven mechanically (counts, id sets, verbatim byte-equality, reverse-transform
    deep-equality) — no value dropped, no verbatim string altered.
  - The readme no longer duplicates the format definition.
  - Story 10's Q1 is closed: the exporter targets v1.0.0.
- Deviations from plan: step 1 (owner confirmation) executed as proceed-on-recorded-assumption
  because the owner was unavailable mid-run; all five decisions are logged in
  Questions / Unknowns for the owner's post-hoc review (manual check 1 remains open for the
  owner: read the spec and confirm the envelope decision reads back as intended).
- Follow-ups: none created — cross-link population and AnyList recipe-vs-meal
  reclassification were already out of scope / backlog items.
