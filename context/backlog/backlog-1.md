---
name: backlog-1
description: Front backlog file (story inventory / Milestone -1) for FoodUs. Holds data-recovery follow-ups surfaced during Milestone 1, upstream bugs adopted by the Story 2.12 issue triage, and multiplayer stretch items deferred from Milestone 3.
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
| 5 | [Fix CSV product import dropping column data](#story-5) | Bug | High | — | — | — | *unscheduled* | Backlog |
| 6 | [Fix database & CSV product export errors](#story-6) | Bug | High | — | — | — | *unscheduled* | Backlog |
| 7 | [Recalculate nutrition when "values per" changes in the food editor](#story-7) | Bug | Medium | — | — | — | *unscheduled* | Backlog |
| 8 | [Fix wedged in-app back navigation from Settings](#story-8) | Bug | Medium | — | — | — | *unscheduled* | Backlog |
| 9 | [Make food search match word substrings, not just prefixes](#story-9) | Bug | Medium | — | — | — | *unscheduled* | Backlog |
| 10 | [N-person groups (beyond the two-member cap)](#story-10) | Feature | Low | — | — | — | *unscheduled* | Backlog |
| 11 | [Safety-number key verification](#story-11) | Feature | Low | — | — | — | *unscheduled* | Backlog |
| 12 | [Friend bios in the expanded friend row](#story-12) | Feature | Low | — | — | — | *unscheduled* | Backlog |
| 13 | [Push transport for the relay](#story-13) | Feature | Low | — | — | — | *unscheduled* | Backlog |

Stories 5-9 were adopted on 2026-07-25 by the Milestone 2 Story 12 upstream issue triage
(see the [Upstream Issue Triage](#upstream-issue-triage-2026-07-25) section below).
Stories 10-13 are multiplayer stretch items deliberately deferred from
[Milestone 3](../milestones/milestone-3.md) during the 2026-07-27 dictation (its Part C);
none are Milestone 3 scope.

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

<a id="story-5"></a>

### Story: Fix CSV product import dropping column data

**Type:** Bug

**Summary:** Adopted from upstream issue
[#437](https://github.com/maksimowiczm/FoodYou/issues/437). Importing a manually cleaned CSV of
products silently loses data: many imported products end up missing values that were present in the
source file (energy, carbohydrates, and other nutrient columns). The reporter imported a trimmed,
Ciqual-derived CSV (commas replaced with periods, `<` signs removed, `traces` replaced with `0`,
`-` replaced with `0`, exported from Google Sheets) and found numerous rows saved without their
nutrient columns.

**Why / value:** CSV import is a core data-ownership path for this fork — it is how the owner's
recovered Milestone 1 food data is meant to land in the app. A lossy importer corrupts that data at
the moment of entry, which undercuts the whole recovery effort.

**Rough scope:** The `importcsvproducts` module
(`app/src/.../ui/database/importcsvproducts/`, notably `ImportCsvProductsViewModel` and the CSV
parser it drives). Reproduce with a representative multi-column CSV, locate where columns are
dropped (delimiter / quoting / empty-cell / header-mapping handling are the likely suspects), and
add a parsing regression test for the failing shape.

**Scores (filled at planning):**

- Complexity: —
- Effort: —
- Risk: —

---

<a id="story-6"></a>

### Story: Fix database & CSV product export errors

**Type:** Bug

**Summary:** Adopted from upstream issue
[#420](https://github.com/maksimowiczm/FoodYou/issues/420) (reported on 3.4.8, close to this
fork's 3.4.9 base). Both export paths under Settings -> Database fail with no file written:
"Export CSV food products" shows a generic error, and "Database backup" throws a
`RuntimeException` / `java.io.FileNotFoundException` while delivering the storage-picker result to
`DeveloperActivity`. Export reportedly worked around March and regressed since.

**Why / value:** Export is the other half of the fork's data-ownership guarantee (no lock-in). A
broken backup and export path means users cannot get their own data out — a direct hit to a
founding principle.

**Rough scope:** The Android export flow —
`app/src/androidMain/.../infrastructure/android/DeveloperActivity.kt`,
`ui/database/exportcsvproducts/ExportProductsViewModel`, and the SAF (Storage Access Framework)
result handling that writes to the returned `content://` URI. The `FileNotFoundException` on the
picker's returned document Intent points at a URI-handling or write-permission bug. Reproduce
on-device.

**Scores (filled at planning):**

- Complexity: —
- Effort: —
- Risk: —

---

<a id="story-7"></a>

### Story: Recalculate nutrition when "values per" changes in the food editor

**Type:** Bug

**Summary:** Adopted from upstream issue
[#364](https://github.com/maksimowiczm/FoodYou/issues/364). The issue is labeled `enhancement`
upstream but describes a functional defect, so it is adopted as a bug. In the food editor the
"values per" selector defaults to 100 g; switching it to per-serving does **not** recalculate the
displayed nutrition numbers. For foods originally entered per-serving this makes editing
effectively impossible without manually recomputing every nutrient (worst for vitamin- and
mineral-heavy foods). Fix scope is the recalculation defect only — recompute the shown values to
match the selected "values per". The reporter's secondary request (store user-entered foods exactly
as typed to avoid 100 g precision drift) is an enhancement and is out of scope for this bug.

**Why / value:** Editing custom foods is central to the owner's recovered data set; an editor that
cannot switch measurement bases blocks correcting serving-based entries.

**Rough scope:** The food editor screen and its view model (the nutrient input fields and the
`values per` / measurement-base state). Keep the fix to recalculating displayed values on toggle.

**Scores (filled at planning):**

- Complexity: —
- Effort: —
- Risk: —

---

<a id="story-8"></a>

### Story: Fix wedged in-app back navigation from Settings

**Type:** Bug

**Summary:** Adopted from upstream issue
[#325](https://github.com/maksimowiczm/FoodYou/issues/325) (Pixel 5, Android 14). After opening
Settings, the in-app back arrow (top-left) stops responding; once it has been tapped, the Android
system back button also stops working until the app is restarted. The system back button behaves
correctly until the in-app arrow is used — the navigation / back-dispatcher state gets wedged.

**Why / value:** A back stack that requires an app restart to recover is a serious daily-use
navigation defect, and it would hit both household phones equally.

**Rough scope:** Settings navigation and back handling
(`app/src/commonMain/.../navigation/FoodYouAppNavHost.kt`, `NavControllerExt.kt`, and any
`BackHandler` around the Settings graph). The report predates the 3.4.9 base, so first confirm it
still reproduces on `jarryd/develop` before fixing.

**Scores (filled at planning):**

- Complexity: —
- Effort: —
- Risk: —

---

<a id="story-9"></a>

### Story: Make food search match word substrings, not just prefixes

**Type:** Bug

**Summary:** Adopted from upstream issue
[#312](https://github.com/maksimowiczm/FoodYou/issues/312). Search only matches the start of
words: typing "whey" finds an item containing "whey", but typing "hey" does not, even though it
appears inside the name. Confirmed applicable in this fork's tree — `FoodSearchDao` runs
`ProductFts MATCH :query || '*'` (and the same for `RecipeFts`), an FTS **prefix** match, so
mid-word substrings can never match.

**Why / value:** Search that misses substrings adds friction to the fork's core logging flow (the
focus of Milestone 2). The owner may not recall the exact leading word of a food, so prefix-only
matching costs extra taps or a failed lookup.

**Rough scope:** The FTS queries in
`app/src/commonMain/.../food/search/infrastructure/room/FoodSearchDao.kt` (product and recipe
searches). Options include a `LIKE` substring fallback, a different tokenizer, or a trigram
approach — weigh each against FTS performance. Keep scope to the matching behavior; do not redesign
result ranking.

**Scores (filled at planning):**

- Complexity: —
- Effort: —
- Risk: —

---

<a id="story-10"></a>

### Story: N-person groups (beyond the two-member cap)

**Type:** Feature

**Summary:** Milestone 3 deliberately caps groups at two members. This story lifts the cap:
groups of more than two, with invite dynamics, live member-list updates ("group swells from 2 to
8"), and member add/remove flows. It carries three facets the 2026-07-27 dictation explicitly
tied to N-person scope: the **all-pairs friendship rule** (every member must already be friends
with a new addition — known costs: quadratic pair requirements (4 people → 6 pairs, 8 → 28) and
the server exposing third-party friendship data, a privacy decision that must be made
consciously); **per-member trust levels** within a group (deferred as "too complicated too soon",
moot under the cap); and the **group-block notice wording/anonymity** question (the dictated
notice reveals the blocker's identity to the adder — resolve before N-person groups).

**Why / value:** Generalizes multiplayer beyond the household pair once Milestone 3's two-person
foundation is proven. Deferred because the two-member cap eliminates an entire class of privacy
problems; lifting it must confront them deliberately.

**Rough scope:** Group membership model, invite lifecycle, server friendship-lookup API surface,
trust model, and blocklist messaging — all downstream of Milestone 3's Stories 9 and 5.
Reclassification check: this bundles group mechanics, a privacy policy decision, trust semantics,
and notice wording; if CER scoring at planning exposes it as epic-sized, promote it to its own
milestone and split rather than forcing it through as one story.

**Scores (filled at planning):**

- Complexity: —
- Effort: —
- Risk: —

---

<a id="story-11"></a>

### Story: Safety-number key verification

**Type:** Feature

**Summary:** Close the known, accepted MITM hole in Milestone 3's key distribution: the relay
hands out public keys, so a malicious server could substitute its own. Add Signal/WhatsApp-style
safety numbers verified out-of-band so two friends can confirm they hold each other's real keys.

**Why / value:** Theoretical risk for a self-hosted household relay (the operator is the owner),
but the established fix matters if the app is ever handed to friends on someone else's server.

**Rough scope:** Safety-number derivation from key pairs, a compare/verify UI in the friend row,
and a verified flag on stored friend keys.

**Scores (filled at planning):**

- Complexity: —
- Effort: —
- Risk: —

---

<a id="story-12"></a>

### Story: Friend bios in the expanded friend row

**Type:** Feature

**Summary:** The expanded friend row (Milestone 3, Story 7) deliberately ships without bios. If
bios ever happen, that row is where they live — a small free-text field on the profile,
propagated like the username.

**Why / value:** Light social polish; explicitly a non-goal for Milestone 3.

**Rough scope:** Profile field, relay profile-update propagation, expanded-row UI.

**Scores (filled at planning):**

- Complexity: —
- Effort: —
- Risk: —

---

<a id="story-13"></a>

### Story: Push transport for the relay

**Type:** Feature

**Summary:** Milestone 3's transport is poll-on-wake only. If that latency ever grates, add a
push transport — FCM or a self-hosted alternative — weighing the privacy cost (FCM puts Google
back in the story) against immediacy.

**Why / value:** Only worth doing if real household use shows poll-on-wake latency actually
annoys; diet data is non-time-critical by design.

**Rough scope:** Transport layer beside the poll path, push registration on the relay, and the
privacy decision about the push provider.

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
- **Multiplayer stretch items** — follow-ups surfaced while building
  [Milestone 3](../milestones/milestone-3.md) join Stories 10-13 here. (The original "Milestone 3
  candidates" inflow closed on 2026-07-27 when that milestone arrived fully dictated.)

---

<a id="upstream-issue-triage-2026-07-25"></a>

## Upstream Issue Triage (2026-07-25)

Research output of **Milestone 2, Story 12**. All **96 open issues** on upstream
`maksimowiczm/FoodYou` were reviewed via the GitHub REST API (no pull requests among them). Per the
fork's **minimal-scope** principle, enhancements that make the app fuzzy (shopping lists, exercise
and water tracking, body-weight tracking, widgets, graphs, and similar adjacent-tracker requests)
are **not** adopted. Genuine, applicable **bugs** are adopted as Stories 5-9 above.

**Adopted (5 bugs):**

| # | Title | Class | Decision | Reason |
| --- | --- | --- | --- | --- |
| [437](https://github.com/maksimowiczm/FoodYou/issues/437) | Import functionality fails | Bug | Adopt -> Story 5 | CSV import silently drops nutrient columns; import is a core data-ownership path. |
| [420](https://github.com/maksimowiczm/FoodYou/issues/420) | Errors when trying to export database and CSV food products | Bug | Adopt -> Story 6 | Export/backup throws `FileNotFoundException`; export code (`DeveloperActivity`, export module) present in our tree; base 3.4.8 ~ our 3.4.9. |
| [364](https://github.com/maksimowiczm/FoodYou/issues/364) | Nutrition numbers fail to follow "values per" setting when editing foods | Bug (mislabeled `enhancement`) | Adopt -> Story 7 | Title/body describe a functional defect: switching "values per" does not recalculate values, blocking edits of serving-based foods. |
| [325](https://github.com/maksimowiczm/FoodYou/issues/325) | Navigating back from Settings | Bug | Adopt -> Story 8 | In-app back arrow wedges the back stack until app restart; serious navigation defect. Confirm it still repros on 3.4.9. |
| [312](https://github.com/maksimowiczm/FoodYou/issues/312) | Search doesn't work for parts of words | Bug | Adopt -> Story 9 | Confirmed in our code: `FoodSearchDao` uses `MATCH :query \|\| '*'` (prefix only), so substrings never match; hurts core logging search. |

**Notable rejection (bug-labeled but not adopted):**

| # | Title | Class | Decision | Reason |
| --- | --- | --- | --- | --- |
| [239](https://github.com/maksimowiczm/FoodYou/issues/239) | Link meal entries from food database again | By-design / architecture request | Reject | Diary entries deliberately **snapshot** (copy) food data so historical logs stay stable when a food is later edited. The request (live references or versioning so past entries update) is an architectural redesign, not a bug fix, and conflicts with the local-first snapshot model. |

**Rejected in bulk (not adopted):**

- **~88 enhancement-labeled issues** — out of scope under **minimal scope**. Representative
  examples: shopping list (#309), water tracking (#86), manual exercise tracker (#181), body-weight
  tracking (#279), Android widgets (#290), graphs/plots (#182, #39), health-connect (#20),
  multi-user (#67), and many nutrient/UX conveniences. These belong to adjacent-tracker or
  fuzz-inducing scope and are intentionally left upstream. *(Superseded note, 2026-07-27: the
  rejection of upstream multi-user #67 — multiple user accounts sharing one phone — still
  stands, but the fork now builds its own multiplayer in
  [Milestone 3](../milestones/milestone-3.md): separate devices and databases connected by an
  encrypted relay, designed under the fork's privacy rules rather than adopted from upstream.)*
- **3 unlabeled non-bugs** — #413 (availability on the Accrescent app store — distribution
  request), #329 (monthly/weekly report — enhancement), #254 (new carbohydrates hierarchy —
  data-model enhancement/discussion). None are defects; not adopted.

Full per-issue data was pulled live from the API on 2026-07-25; issue numbers link back to the
upstream tracker for anyone re-reviewing a specific rejection.
