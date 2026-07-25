---
name: backlog-1
description: Front backlog file (story inventory / Milestone -1) for ACME Food App. Holds data-recovery follow-ups surfaced during Milestone 1 (diary logs, grocery verification, canonical-format normalization, truncated-name recovery).
metadata:
  version: "3.0"
  agentic_rails_source_version: "3.0"
  owner: "Jarryd Adaens"
  repo: "FoodYou (fork of maksimowiczm/FoodYou)"
---
# Backlog 1

> Story inventory / Milestone -1. The front (highest-priority) backlog file. Maximum **30 stories**; overflow spawns `backlog-2.md`.
>
> Story shape reference: [BACKLOG_TEMPLATE.md](BACKLOG_TEMPLATE.md)

---

## Story Index

The 2026-07-24 initial project seed mapped every known story directly into
[milestone-1](../milestones/milestone-1.md) (Initialization) and
[milestone-2](../milestones/milestone-2.md) (Customisation). The stories below were surfaced on
2026-07-25 during the Milestone 1 data-recovery work (Stories 5-8) as follow-ups and data-quality
issues encountered while extracting the owner's food data into `jarryd/working-data/`.

| # | Story | Type | Priority | Complexity | Effort | Risk | Milestone | Status |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| 1 | [Extract historical diary logs from MyFitnessPal & Lose It](#story-1) | Feature | High | — | — | — | *unscheduled* | Backlog |
| 2 | [Verify & correct grocery product matches](#story-2) | Bug | Medium | — | — | — | *unscheduled* | Backlog |
| 3 | [Normalize working data into canonical format & link ingredients](#story-3) | Refactor | Medium | — | — | — | *unscheduled* | Backlog |
| 4 | [Recover truncated Lose It recipe ingredient names](#story-4) | Bug | Low | — | — | — | *unscheduled* | Backlog |

---

<a id="story-1"></a>

### Story: Extract historical diary logs from MyFitnessPal & Lose It

**Type:** Feature

**Summary:** Milestone 1 Stories 6 (MyFitnessPal) and 7 (Lose It) were marked Complete on
2026-07-25 after capturing custom foods, recipes, and personal meals — but the historical daily
diary / calorie logs (the "years of paid data" those stories center on) were never extracted.
`diary[]` in `jarryd/working-data/master-data.json` is still empty.

**Why / value:** Recovering the historical log is a founding data-ownership goal; without it the
recovery is only partial. Flagged at the time the stories were closed.

**Rough scope:** Further copy-paste sessions from the MyFitnessPal / Lose It diary views into
`diary[]`; will require defining a diary-entry shape that references foods/meals with a date and
portion. Coordinate with Story 3 (canonical format).

**Scores (filled at planning):**

- Complexity: —
- Effort: —
- Risk: —

---

<a id="story-2"></a>

### Story: Verify & correct grocery product matches

**Type:** Bug

**Summary:** The `groceryProducts[]` catalog (31 items) was built by web research against Open
Food Facts / retailer pages with **no physical barcode scans**, so several entries need owner
verification before import: **Cordial** matched a *zero-sugar* Cottee's variant (likely the wrong
product — a sugared cordial would read very differently); **Pancake mix** has `null` nutrition
(the source panel was unreliable); **Onion powder** has `null` barcode and nutrition; **Bega
tasty cheese 1kg** has a `null` barcode (unconfirmed for the 1kg variant); **"Snacks"** is too
vague to match at all. Medium/low-confidence barcodes should be confirmed on first scan.

**Why / value:** The catalog exists to pre-load usual groceries so the household scans fewer
barcodes; a wrong barcode or wrong-variant nutrition undermines that and imports bad data.

**Rough scope:** Owner names the real product (brand/size) for the generic and flagged items;
re-research or correct the affected `groceryProducts[]` entries; confirm barcodes on first scan
and raise `confidence`.

**Scores (filled at planning):**

- Complexity: —
- Effort: —
- Risk: —

---

<a id="story-3"></a>

### Story: Normalize working data into canonical format & link ingredients

**Type:** Refactor

**Summary:** `jarryd/working-data/master-data.json` grew organically to v0.8.0 and is
deliberately **source-shaped**: recipe/meal entries differ by origin (Lose It vs MyFitnessPal vs
AnyList), ingredient portions/quantities are stored as verbatim strings, nutrition sits on mixed
bases (per-serving, per-100 g/mL, "unspecified", "not shown"), and recipe/meal ingredients are
**not cross-linked** to `foods[]`. This is accumulated normalization debt from the fast-and-messy
extraction approach.

**Why / value:** A single canonical schema and resolved food references are needed before the
Food You CSV export (Milestone 1 Story 10) can be generated reliably. Directly feeds Story 5
(define the master data format).

**Rough scope:** Finalize the canonical schema (M1 Story 5), migrate the working data into it,
resolve ingredient references to `foods[]`, reconcile nutrient units/bases, decide recipe-vs-meal
(collapse vs expand) reclassification for borderline entries.

**Scores (filled at planning):**

- Complexity: —
- Effort: —
- Risk: —

---

<a id="story-4"></a>

### Story: Recover truncated Lose It recipe ingredient names

**Type:** Bug

**Summary:** Two Lose It recipe ingredients were captured with names truncated by the Lose It UI
and flagged `truncatedName: true`: `"Simply Heat Slow Cooked Pulle..."` (recipe-0004, Taco Night)
and `"Spring Onion, Bulb and Stalk,..."` (recipe-0005, Work Noodles).

**Why / value:** Complete, accurate ingredient data; small and self-contained.

**Rough scope:** Read the full names from Lose It, patch the two entries in
`jarryd/working-data/master-data.json`, and clear the `truncatedName` flags.

**Scores (filled at planning):**

- Complexity: —
- Effort: —
- Risk: —

---

## Expected Inflows

- **Adopted upstream bugs** from Milestone 2's upstream issue triage story — one story per adopted
  bug.
- **Friction findings** from Milestone 1's daily-use story (logging flows, navigation, search and
  barcode behavior).
- **Milestone 3 candidates** — ideas that surface before that milestone is dictated stage here
  first.
