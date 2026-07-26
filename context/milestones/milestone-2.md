---
name: milestone-2
description: Milestone 2 - Customisation. Change how the application works - FoodUs identity, AI-assisted logging, ergonomics, adopted upstream bug fixes, Australian food-data providers with offline-first local storage, and an expanded Quick Add with promotion to custom products/recipes.
metadata:
  version: "3.0"
  agentic_rails_source_version: "3.0"
  owner: "Jarryd Adaens"
  repo: "FoodYou (fork of maksimowiczm/FoodYou)"
---
# Milestone 2: Customisation

> Milestone. A coherent macro-feature or delivery outcome. (Tier numbers live only in `design.md` / `agenticworkflow.md`.)
>
> Related: [design.md (Milestones Index)](../design.md#milestones-index), [../backlog/](../backlog/)
>
> Source dictation: [2026-07-24 initial project seed](../dictations-tier-0/2026-07-24_initial_project_seed_acme-food-app.md)

---

## Intent

Start actually changing how the application works — making logging easier for the household and
adding the fork's own identity and AI integrations. It also broadens the app's food data — adding
first-class Australian providers stored locally for offline-first search — and expands Quick Add so
estimated foods carry more detail and can later graduate into reusable custom products or recipes.

## Why it matters

- Logging friction is the core complaint against MyFitnessPal and Lose It; the AI and ergonomics
  stories attack it directly.
- The FoodUs identity (superseding the interim ACME Food App name) makes the fork unmistakably its own while attributing upstream.
- Adopting genuine upstream bugs keeps the base healthy without scope creep.
- The household eats Australian foods that Open Food Facts and USDA cover poorly; first-class
  Australian datasets stored locally make search genuinely useful — and offline-first.
- Historical integrity is non-negotiable: provider refreshes and Quick Add promotion must never
  rewrite what was already logged, so diary entries have to be immutable nutritional snapshots.

## Outcome / Definition of Done

The app carries the FoodUs identity (with upstream attribution and layered versioning), the
four-button per-meal logging surface (search, quick add, AI scan, fast placeholder) works end to
end including placeholder resolution, the settings ergonomics options (graphs, week layout) ship,
reusable meal templates let a day's entries be saved and re-applied on other days, and adopted
upstream bugs are fixed. In addition, the Australian Food Composition Database provider works
through the normal provider UI with locally stored, offline-first search; FoodSwitch is either
integrated or its access blocker is documented; provider update-checks persist the last successful
check and full refreshes never mutate history; and Quick Add supports description, fibre, serving
count, and weight, plus promotion of new and historical entries into custom products and recipes
without altering the original diary snapshot.

## Status

In Progress — reopened 2026-07-26 by the
[owner's addendum](../dictations-tier-0/2026-07-26_addendum_ai-settings-branding-foodus.md) after
daily use. 20/23 stories complete; Story 16 (FoodSwitch) blocked on an external data licence,
documented per its own gate; Stories 21–23 are the new addendum work. The original 19-story run
was delivered 2026-07-25 by rails-boss-execute (see `../rails-boss-execute/progress.md`).

---

## Story Index

| # | Status | Story | Type | Complexity | Effort | Risk | Plan |
| --- | --- | --- | --- | --- | --- | --- | --- |
| 1 | Complete | [Identity shift / rename to ACME Food App](#story-1) | Feature | — | — | — | — |
| 2 | Complete | [Relocate the Sponsor button](#story-2) | Feature | — | — | — | — |
| 3 | Complete | [Replace the privacy policy link](#story-3) | Feature | — | — | — | — |
| 4 | Complete | [About screen link swaps](#story-4) | Feature | — | — | — | — |
| 5 | Complete | [Per-meal add buttons rework](#story-5) | Feature | — | — | — | — |
| 6 | Complete | [AI scanning (robot button)](#story-6) | Feature | — | — | — | — |
| 7 | Complete | [Gallery source for AI scanning](#story-7) | Feature | — | — | — | — |
| 8 | Complete | [Fast text placeholder (pencil button)](#story-8) | Feature | — | — | — | — |
| 9 | Complete | [Placeholder edit flow (meta screen)](#story-9) | Feature | — | — | — | — |
| 10 | Complete | [Graph style setting (bar / pie)](#story-10) | Feature | — | — | — | — |
| 11 | Complete | [Week layout setting (fixed / scrolling)](#story-11) | Feature | — | — | — | — |
| 12 | Complete | [Upstream issue triage & bug adoption](#story-12) | Research | — | — | — | — |
| 13 | Complete | [Reusable meal templates](#story-13) | Feature | — | — | — | — |
| 14 | Complete | [Provider & Quick Add architecture spike](#story-14) | Research | 5 | 4 | 2 | [plan](../implementation-plans/milestone-2/story-14-provider-quickadd-architecture-spike/plan.md) |
| 15 | Complete | [Australian Food Composition Database provider](#story-15) | Feature | 7 | 7 | 6 | [plan](../implementation-plans/milestone-2/story-15-australian-food-composition-database-provider/plan.md) |
| 16 | Blocked (documented) | [FoodSwitch provider (feasibility-gated)](#story-16) | Feature | 4 | 3 | 5 | [plan](../implementation-plans/milestone-2/story-16-foodswitch-provider/plan.md) |
| 17 | Complete | [Provider update-check & refresh UI](#story-17) | Feature | 7 | 7 | 6 | [plan](../implementation-plans/milestone-2/story-17-provider-update-check-refresh-ui/plan.md) |
| 18 | Complete | [Quick Add expansion — new fields & migration](#story-18) | Feature | 3 | 3 | 3 | [plan](../implementation-plans/milestone-2/story-18-quickadd-expansion-fields/plan.md) |
| 19 | Complete | [Quick Add promotion workflow](#story-19) | Feature | 6 | 6 | 5 | [plan](../implementation-plans/milestone-2/story-19-quickadd-promotion-workflow/plan.md) |
| 20 | Complete | [FoodUs identity — rename, versioning, icon](#story-20) | Feature | — | — | — | — |
| 21 | Not Started | [Three-layer AI prompt architecture](#story-21) | Feature | 4 | 4 | 3 | [plan](../implementation-plans/milestone-2/story-21-three-layer-ai-prompts/plan.md) |
| 22 | Not Started | [AI settings screen](#story-22) | Feature | 4 | 5 | 4 | [plan](../implementation-plans/milestone-2/story-22-ai-settings-screen/plan.md) |
| 23 | Not Started | [Provider website info links](#story-23) | Feature | 2 | 2 | 1 | [plan](../implementation-plans/milestone-2/story-23-provider-website-links/plan.md) |

---

## Stories

<a id="story-1"></a>

### Story 1: Identity shift / rename to ACME Food App

**Type:** Feature

**Summary:** Give the app its own distinct identity so it is unmistakably this fork:

- Name: `ACME Food App` (in the Looney Tunes "ACME" sense), replacing "Food You" everywhere
  user-visible, including the About screen.
- Package / application ID and descriptor changed.
- Assorted branding touches.
- Version starts at **1.0**. Scheme: first number = major releases, second = minor releases,
  third = reserved (probably a deploy counter — to be decided).
- The About screen must state the app is **derived from Food You** and show **which upstream
  Food You version** it is derived from. The fork's versioning sits on top of the Food You version.

**Why / value:** The fork deserves its own face; clear upstream attribution keeps it honest.

**Rough scope:** App name resources, application ID, About screen, version wiring. Implementation
constraint: do this as an easy-to-overlay change on top of the original app to minimize merge
conflicts with upstream.

**Status:** Complete (2026-07-25, commit `dc683e1d`) — applicationId `com.acme.foodapp` (namespace/source packages untouched for mergeability); fork version 1.0.0 layered over untouched upstream 3.4.9; About screen shows "Derived from Food You 3.4.9". Third version component reserved at 0 (deploy-counter decision still open).

---

<a id="story-2"></a>

### Story 2: Relocate the Sponsor button

**Type:** Feature

**Summary:** The settings menu contains a "Sponsor — support the app's growth" button. The owner
loves that button, but it's in the wrong spot and in the way. Move it to the About screen where it
belongs.

**Why / value:** Declutters settings while keeping upstream support visible.

**Rough scope:** Settings screen, About screen (`settings` / `sponsorship` slices).

**Status:** Complete (2026-07-25, commit `0d853d16`) — Sponsor button now lives at the bottom of the About screen; Settings entry and its orphaned wavy divider removed; sponsor navigation preserved and emulator-verified.

---

<a id="story-3"></a>

### Story 3: Replace the privacy policy link

**Type:** Feature

**Summary:** Stop linking to the Food You privacy policy; link instead to the owner's own privacy
policy, already online for his apps.

**Why / value:** The fork's data practices (notably the AI features) are the owner's to declare,
not upstream's.

**Rough scope:** Single link swap in settings/About.

**Status:** Complete (2026-07-25, commit `5feda67a`) — single `privacyPolicyUri` constant swapped; all consumers (Settings, onboarding, login) resolve through it. Owner follow-up resolved 2026-07-26: the real published URL `https://jarrydadaens.github.io/privacy.html` (supplied in the addendum, item 3) now replaces the placeholder.

---

<a id="story-4"></a>

### Story 4: About screen link swaps

**Type:** Feature

**Summary:** Rewire the About screen's outbound links:

- Email button emails the owner (address to be specified), not the original creator.
- Lightbulb (suggestions) button links to the fork's GitHub issues page.
- "What's new" section keeps pointing at the same place (believed in-app data); the owner adds his
  own entries to publish his own changelogs.
- Leftmost (GitHub) button points to the fork's GitHub repo.
- Attribution: beneath the Food You credit to the original creator, add a button linking to his
  GitHub — so anyone can see where this came from.

**Why / value:** Feedback about the fork should reach the fork's owner; upstream still gets clear
credit.

**Rough scope:** About screen link targets; changelog data entries.

**Status:** Complete (2026-07-25, commit `0ace5923`) — email → jarryd.adaens@outlook.com.au, GitHub/lightbulb → fork repo/issues, "Original creator on GitHub" attribution button added under the derived-from credit, What's-new untouched. All targets emulator-verified via intent capture.

---

<a id="story-5"></a>

### Story 5: Per-meal add buttons rework

**Type:** Feature

**Summary:** Each meal section (breakfast, lunch, dinner, snacks, plus user-defined) currently has
two buttons; it ends up with four:

1. **Search (magnifying glass)** — the current plus button opening search (barcode or text across
   remote sources and local data). Behavior unchanged; swap the plus icon for a magnifying glass —
   every button here adds something, so "plus" is meaningless; this one is specifically a *search*.
2. **Quick add (lightning bolt)** — unchanged.
3. **AI scan (robot icon)** — new, Story 6.
4. **Fast text placeholder (pencil icon)** — new, Story 8.

**Why / value:** Makes each add path self-describing and hosts the two new logging flows.

**Rough scope:** Diary meal header UI (`fooddiary` slice); icon assets.

**Status:** Complete (2026-07-25, commit `332e4cae`) — four-button family per meal card (Search magnifier, Quick add lightning, AI scan SmartToy, Fast text EditNote) on its own row; robot/pencil navigate to stub screens on routes `FoodDiaryAiScan`/`FoodDiaryFastText` (carrying epochDay + mealId) for Stories 6/8 to fill. Emulator-verified.

---

<a id="story-6"></a>

### Story 6: AI scanning (robot button)

**Type:** Feature

**Summary:** The robot button opens a dedicated "AI scanning" sub-screen.

*Capture flow:* a big button launches the camera; after shooting you return to a preview; tapping
the preview prompts "discard and retry"; below the preview sits an **Ask AI** button.

*Ask AI:* the photo is downscaled to a sensible minimum size, then sent to a model endpoint (owner's
API key — likely OpenRouter — baked into private builds) with a custom embedded prompt: we are
Australians, in Victoria, Australia; this is my food; please identify it. The response is JSON:
what the model thinks the food is, a certainty value, and estimated calories, protein, fat, fibre,
and sugar.

*Result handling — two actions:*

- **Tick (save):** saves what the AI thinks it saw — the quick-add flow with fields pre-filled
  from the AI's JSON.
- **Align:** instead of saving the raw guess, align the result with a food already saved locally
  (searching custom foods); if none matches, create one. For a recurring dish (lasagna every
  Friday night) the AI guesses first; the owner later hand-edits that custom food into a trusted
  record, and from then on photographing the dish and hitting Align connects the photo to
  known-true data. Convenience *and* reliability, given a little preparation.

**Why / value:** The flagship friction-killer — photo-to-logged-entry in seconds.

**Rough scope:** New sub-screen, camera integration, image downscaling, Ktor call to the AI
endpoint, JSON parsing, quick-add prefill, custom-food alignment/creation. Secret-injection
mechanism for the baked key is an open question (must never reach the public repo).

**Status:** Complete (2026-07-25, commit `ef7421d8`) — camera capture/preview/discard flow, reusable `ai` slice (`AiFoodScanner` behind OpenRouter Ktor client, shared with Story 9), key injected from gitignored `local.properties`/env via BuildConfig ("AI not configured" state when absent), Tick→Quick Add prefill, Align→local custom-food search/create. Emulator-verified incl. real 401 round-trip with a throwaway key. Known limits: Quick Add persists no fibre/sugar (shown in result card only); create-custom-food opens unprefilled; happy-path result UI not E2E-tested without a real key.

**Design revision (2026-07-26 addendum):** the single embedded prompt with personal context and the baked-key approach are superseded — [Story 21](#story-21) replaces the prompt with the three-layer architecture (and adds the hint field + Submit button here), and [Story 22](#story-22) replaces the baked key with user-entered key/endpoint/model.

---

<a id="story-7"></a>

### Story 7: Gallery source for AI scanning

**Type:** Feature

**Summary:** On the AI scanning screen, alongside the big take-a-photo button, add a small gallery
button: pick an existing photo of your food instead of shooting a new one.

**Why / value:** Backfilling past meals, or using a better photo you already took.

**Rough scope:** Gallery picker on the Story 6 screen; same downstream flow.

**Status:** Complete (2026-07-25, commit `d4e2e38f`) — "Pick from gallery" button (modern photo picker, no storage permission) beside the capture card; picked photos reuse the identical downscale→preview→Ask AI flow. Emulator-verified. iOS remains the Story 6 Android-only placeholder.

---

<a id="story-8"></a>

### Story 8: Fast text placeholder (pencil button)

**Type:** Feature

**Summary:** The pencil opens the fastest possible logging path: enter just a name and a
description. Saves a zero-calorie placeholder entry with no nutritional data — purely a marker
that you ate something, to be straightened out later.

**Why / value:** Out at lunch, camera awkward (weird social situation, bad lighting, no time) —
you need something even faster than the AI flow.

**Rough scope:** Minimal entry form; placeholder entry representation in the diary (feeds Story 9).

**Status:** Complete (2026-07-25, commit `9fc241fb`) — placeholders are `ManualDiaryEntry` rows with zero nutrition plus new `isPlaceholder` + `description` columns (Room 32→33 additive migration, upgrade-verified with surviving data). Name required / description optional, one-tap save. Story 9 must branch the edit flow on `isPlaceholder`.

---

<a id="story-9"></a>

### Story 9: Placeholder edit flow (meta screen)

**Type:** Feature

**Summary:** Placeholder entries look like normal entries in the diary, with the usual edit/delete
options — but choosing **edit** on a placeholder opens a new meta screen instead of the regular
editor. It greets you with the same add options (search, lightning, AI) with the saved
name/description on screen as context (e.g. "roast dinner at my uncle's house"). You resolve the
placeholder by whichever route fits: search manually, lightning-estimate ("call it 600 calories
and 30 g protein"), or AI.

*AI button here behaves differently:* no photo, no macro estimation. It silently sends a hidden
prompt — *I am Australian, in Melbourne; this is what I had for lunch* — plus the typed
name/description. The model produces a **better search query** describing the meal; that query is
placed into the search box, the search executes, and you land on the results screen already
populated. It automates part of the workflow so you're only doing the deciding.

**Why / value:** Completes the placeholder loop — fast capture now, honest data later.

**Rough scope:** New meta screen, placeholder detection on edit, AI query-generation call, search
handoff with pre-executed query.

**Status:** Complete (2026-07-25, commit `8b7d70e7`) — edit on a placeholder opens the Resolve-placeholder meta screen (search / quick add / AI routes; name+description shown as context); AI route reuses the Story 6 `ai` slice for text-only query generation into a pre-executed search (`initialQuery` nav arg, additive). Resolution is an explicit "Remove placeholder" affordance (no silent deletion). Emulator-verified; AI round-trip pending a real key.

---

<a id="story-10"></a>

### Story 10: Graph style setting (bar / pie)

**Type:** Feature

**Summary:** Add a settings option "Graphs": default **Bar**, alternative **Pie**. When Pie is
selected, the view at the top of the daily food-entry screen — calories eaten plus macro
breakdown, currently bar graphs — is exchanged for a pie chart presenting the same data.

**Why / value:** Owner presentation preference; cheap ergonomic win.

**Rough scope:** One setting; one chart component swap. This is the only place charts are used in
the app.

**Status:** Complete (2026-07-25, commit `ab39e193`) — "Graphs" option in Personalization (default Bar); Pie renders a Compose-Canvas macro-breakdown pie with the same colors/legend, preference persisted in DataStore and survives restart. Angle math unit-tested; emulator-verified both directions.

---

<a id="story-11"></a>

### Story 11: Week layout setting (fixed / scrolling)

**Type:** Feature

**Summary:** Add a settings option "Week layout": **Fixed** or **Scrolling** (current behavior).
Scrolling moves day-by-day with a calendar picker — it works, but makes time feel fluffy. Fixed
pins the week strip: Monday always far left (Mon-Sun), current day highlighted, swiping moves
whole weeks.

**Why / value:** Anchors time to a particular week instead of a fluffy day-stream.

**Rough scope:** One setting; day-scroller component alternative in the diary home.

**Status:** Complete (2026-07-25, commit `787ff547`) — "Week layout" in Personalization (default Scrolling, untouched); Fixed mode renders a new Monday-first whole-week pager strip with current-day highlight; day taps drive the diary; survives restart. Known nit: in Fixed mode the month/year header tracks the selected date, not the swiped-but-untapped week.

---

<a id="story-12"></a>

### Story 12: Upstream issue triage & bug adoption

**Type:** Research

**Summary:** Go through the open issues on the upstream Food You repository and decide which to
adopt. Enhancements: generally not interested — they make the app fuzzy (e.g. a requested
shopping-list feature belongs in a shopping-list app; don't pollute one app with another app's
job). Bugs: yes — write down genuine bugs and create a story for each adopted one.

**Why / value:** Keeps the base healthy without scope creep.

**Rough scope:** Upstream issue review; adopted bugs become new stories in the backlog (staged
there, then pulled into a milestone).

**Status:** Complete (2026-07-25, commit `a831dbb7`) — all 96 open upstream issues triaged; 5 genuine bugs adopted as backlog stories in `../backlog/backlog-1.md` (upstream #437 CSV-import nutrient loss, #420 export FileNotFoundException, #364 values-per recalc, #325 settings back-stack wedge, #312 prefix-only search). Full adopt/reject table appended there; enhancements rejected under minimal-scope.

---

<a id="story-13"></a>

### Story 13: Reusable meal templates

**Type:** Feature

**Summary:** Add the ability to save a day's logged meal as a reusable, named template and re-apply
it to another day with its individual items intact — the "save this meal, stamp it onto another
day" convenience MyFitnessPal and Lose It both have and Food You lacks.

*Confirmed gap (2026-07-25 code investigation):* Food You has no such feature. In its model a `Meal`
is only a **time-window category** (Breakfast, Lunch…) and a `DiaryMeal` is that category's entries
for **one specific day** — day-bound, not reusable. The closest existing capability is a `Recipe`
(a reusable food you build from ingredients), but it is semantically a single composed food:
adding it collapses to **one** diary entry and it must be deliberately constructed with servings and
ingredient weights, so it does not capture "what I actually ate at breakfast today" as a re-stampable
set of separate entries.

*Desired behavior:* from a day's meal, save its entries as a named template; later, apply that
template to any day/meal so each original item is recreated as its own diary entry (foods, recipes,
quick-adds preserved). Removes the item-by-item re-entry the owner does for recurring meals.

**Why / value:** Recurring meals (the same breakfast most mornings) are re-entered item by item
today; a template turns that into one action — a direct hit on Milestone 2's core logging-friction
goal.

**Rough scope:** New reusable "meal template" concept + persistence in the `fooddiary` slice; a
save-as-template action on a day's meal; an apply-template flow that fans a template back out into
individual `DiaryEntry` rows for the chosen day/meal. Additive overlay per the fork philosophy —
keep the merge-conflict surface small. Open question for planning: whether templates are a new
first-class entity or built on the existing recipe/`DiaryMeal` machinery.

**Status:** Complete (2026-07-25, commit `0e699bbb`) — new first-class `MealTemplate`/`MealTemplateItem` tables (Room 33→34, additive). Snapshot semantics: applying recreates each item as its own manual diary entry with the exact stored nutrition (survives later product deletion; tradeoff: re-applied items lose the live-product link). UX: meal-card overflow menu — Save as template / Apply template (bottom sheet with delete). Full save→apply→delete loop emulator-verified; use-case tests pass.

---

<a id="story-14"></a>

### Story 14: Provider & Quick Add architecture spike

**Type:** Research

**Summary:** Investigate how Food You implements food providers, the manual-database-import pipeline,
local persistence and search, provider enablement, the historical diary-entry data model, and the
Quick Add create/edit component and its nutrition semantics — the shared groundwork the whole
provider + Quick Add capability group depends on. Also verify **FoodSwitch data-access feasibility**
(usable API or bulk download, authentication, personal-use terms, rate limits, whether bulk
acquisition is permitted, barcodes/nutrition panels, and dataset version metadata). Produce a
technical note covering: relevant classes/files, the provider registration flow, the import
pipeline, the persistence + provenance model, search integration, the enablement model, the
historical diary snapshot model, a recommended reusable provider-import pipeline, any required
schema migrations, reusable code to extract from Manual Database Import, and risks/blockers. The
note must resolve the [spec's Section 13 decisions](../dictations-tier-0/2026-07-25_milestone-2_australian-providers-and-quickadd-spec.md).

**Why / value:** Every provider and Quick Add story below rests on these answers. Guessing risks the
two worst outcomes: corrupting previously-logged history, or copying a large block of
provider-specific code twice instead of sharing one pipeline.

**Rough scope:** Read-only investigation across `food/`, `food/search/`, `importexport/` (especially
`swissfoodcompositiondatabase` as the closest local-bulk-import analog), `fooddiary/`, and the Room
migrations; plus an external FoodSwitch feasibility check. Output is a wiki knowledge doc. Per the
spec this spike is **part of feature delivery, not an optional research task**.

**Source:** [Milestone 2 feature spec](../dictations-tier-0/2026-07-25_milestone-2_australian-providers-and-quickadd-spec.md)
(Phase 1).

**Status:** Complete (2026-07-25, commit `5ab699ed`) — knowledge doc at `../wiki/provider-quickadd-architecture.md`; all 15 spec §13 decisions resolved (see plan Completion Review). Headlines: diary entries confirmed already-immutable snapshots; reusable pipeline = wrap `ImportCsvProductUseCase` insert core with download→validate→parse→transactional replace + FTS rebuild; Quick Add stores absolute totals, fibre field already exists; **FoodSwitch = blocked** (commercial licence forbids local storage/derivation — owner would need a written data licence); **AFCD = feasible** (FSANZ Release 3 xlsx, no key, CC BY-SA 3.0 AU).

---

<a id="story-15"></a>

### Story 15: Australian Food Composition Database provider

**Type:** Feature

**Summary:** Add `australian_food_composition_database` as a first-class, selectable provider using
shared provider-import infrastructure (extracted per Story 14's recommendation, rather than a
one-off script). It downloads/imports the AFCD dataset into local storage; has an enable/disable
checkbox; participates in normal search when enabled and is excluded when disabled (without deleting
its data); and displays import state, update state, and last successful check date. Data mapping
covers source id, name, description, energy (kcal), protein/fat/carbohydrate/fibre (g), serving
amount/unit where supplied, per-100 g basis, brand, category, barcode **only where supplied**, and
provider provenance — inventing no absent values. Unit normalization into Food You's canonical
representation is deterministic and test-covered. Import safety: download to temp → validate → parse
→ transactional/staged replace → roll back on failure, always leaving the last-good dataset intact.

**Why / value:** Makes Australian food searches substantially more useful offline — the milestone's
headline food-data goal.

**Rough scope:** New provider definition plus extraction of the reusable download/parse/map/replace
pipeline; Room provenance columns; deterministic unit-conversion tests. **Depends on Story 14.**

**Source:** [Milestone 2 feature spec](../dictations-tier-0/2026-07-25_milestone-2_australian-providers-and-quickadd-spec.md)
(Phase 2).

**Status:** Complete (2026-07-25, commit `1c615992`) — first-class AFCD provider (FoodSource id 4, "AU" provenance badge): dependency-free xlsx reader, download→validate→parse→single-transaction replace pipeline, `ProviderMetadata` table (Room 34→35), enable/disable search filter that retains data. 16/16 unit tests (unit conversion, xlsx, mapping); emulator E2E imported 1,588 foods, "vegemite" searchable, disable/re-enable without re-import, prior data intact. Category column and runtime release detection deferred (latter to Story 17).

---

<a id="story-16"></a>

### Story 16: FoodSwitch provider (feasibility-gated)

**Type:** Feature

**Summary:** Add `foodswitch_australia` on the **same** provider infrastructure — **only if** Story
14 confirms a usable and permitted data-access mechanism (API or downloadable dataset, with
authentication, rate limits, bulk-acquisition permission, barcode/nutrition coverage, and version
metadata all verified). If direct access is unavailable, record the concrete blocker and stop —
**do not** substitute scraped supermarket data (an explicit non-goal). When feasible the provider
downloads/stores the packaged-food dataset locally, participates in search when enabled, supports
barcode lookup where the source provides barcodes, shows source version / last-check date, and
supports full refreshes. Equivalent records from multiple providers may appear separately, each with
inspectable provenance (no automatic cross-provider merging this milestone).

**Why / value:** Adds Australian packaged-food and barcode coverage that composition databases lack.

**Rough scope:** Provider definition reusing Story 15's pipeline, gated on Story 14's feasibility
verdict. **Depends on Story 14** (feasibility) **and Story 15** (shared pipeline).

**Source:** [Milestone 2 feature spec](../dictations-tier-0/2026-07-25_milestone-2_australian-providers-and-quickadd-spec.md)
(Phase 3).

**Status:** Blocked — documented external licence blocker (2026-07-25, commit `5819da3e`; gate evaluated by Story 14). No public API/bulk download, and FoodSwitch Global Terms of Use forbid storing/deriving the data. Per this story's own instruction the blocker is recorded and work stopped; no scraped substitute. Unblock: owner obtains a written data licence (`foodswitch@georgeinstitute.org.au`), then reuse Story 15's pipeline.

---

<a id="story-17"></a>

### Story 17: Provider update-check & refresh UI

**Type:** Feature

**Summary:** Add per-provider update controls: provider name, enable/disable, install/import state,
current local dataset version or publication date, last **successful** update-check date/time, a
`Check for updates` button, download/update status, and an error state. Check outcomes are
up-to-date, new-available (with an explicit download-and-replace action — no silent auto-replace),
or failure (show a useful error, keep the local database, and do **not** record the attempt as a
successful check). A full refresh keeps the old dataset usable until the new one validates, shows
progress where practical, is cancellable where the infrastructure supports it, replaces **only** the
selected provider's rows, preserves every other provider / custom product / recipe / Quick Add entry
/ diary record, and persists the new version + import date. Version comparison uses the strongest
stable identifier available (explicit version → publication date → revision id → checksum + mtime →
checksum). Replacement is atomic — the new dataset goes live only after download, validation, parse,
required-field checks, persistence, and index build all succeed.

**Why / value:** Offline-first datasets need a controlled, safe update path that can never damage
logged history or leave the user with no working database.

**Rough scope:** Provider-settings UI, update-check/refresh use cases, and provider-metadata
persistence (installed version, dates, checksum, error summary). **Depends on Story 14 and Story 15**
(needs a real downloadable provider to drive it).

**Source:** [Milestone 2 feature spec](../dictations-tier-0/2026-07-25_milestone-2_australian-providers-and-quickadd-spec.md)
(Phase 4).

**Status:** Complete (2026-07-25, commit `40d1c7b4`) — provider-generic `compareDatasetVersion` cascade (version → publication date → revision id → checksum+mtime → checksum; unit-tested), non-destructive Check for updates (Ktor HEAD vs stored metadata), explicit Download-and-replace only after new-available, failure path keeps local data and never records a successful check. Full-refresh E2E proved AFCD-only delete+re-insert with diary data and FTS intact. Cancellability not offered — single-transaction pipeline has no resumable infra (documented per story's "where supported").

---

<a id="story-18"></a>

### Story 18: Quick Add expansion — new fields & migration

**Type:** Feature

**Summary:** Add four optional fields to Quick Add: **Description** (free text), **Fibre** (grams,
decimal, non-negative), **Number of servings** (decimal > 0, default 1), and **Weight** (grams
canonical, decimal > 0 when supplied). Provide a backward-compatible schema migration — existing
entries stay readable, defaults are description = null, fibre = null/0 per existing nutrient
convention, servings = 1 where absence would break calculations, weight = null — and the migration
**never rewrites existing nutritional totals**. The same fields are visible and editable when
editing historical entries. Form order and validation follow the spec (non-negative nutrients;
reject zero/negative servings and weight; locale-aware decimals; empty optionals allowed;
kcal-vs-macro consistency is a non-blocking concern, not a save blocker). Existing Quick Add
nutrition semantics — confirmed by Story 14 — are preserved unchanged, and diary entries remain
immutable snapshots.

**Why / value:** Captures more useful detail for estimated or uncertain foods without forcing the
user to build a full product or recipe first.

**Rough scope:** Quick Add form / form-state / view model, the Quick Add Room entity, the migration,
validation, and unit tests. **Depends on Story 14** (nutrition semantics and diary-snapshot
findings).

**Source:** [Milestone 2 feature spec](../dictations-tier-0/2026-07-25_milestone-2_australian-providers-and-quickadd-spec.md)
(§5, §7.1–7.2).

**Status:** Complete (2026-07-25, commit `e50987fa`) — Description (existing column), Dietary fiber (existing `dietaryFiber`), Servings and Weight (new nullable REAL columns, Room 35→36 additive) in the shared Quick Add form for create/edit incl. historical entries. Servings/weight are metadata only — stored totals never scaled (proven on-device); migration left prior rows byte-identical; invalid inputs rejected, empty optionals fine; placeholder flow undisturbed. Note: app uses American "fiber" labels per house style.

---

<a id="story-19"></a>

### Story 19: Quick Add promotion workflow

**Type:** Feature

**Summary:** Add `Promote to Product` and `Promote to Recipe` actions to the Quick Add UI, available
while creating a new entry, editing an existing one, and editing a **historical** diary entry that
originated from Quick Add. Promotion validates the fields, preserves/saves the original diary entry
as an immutable snapshot (**never** replaced by a live reference to the new item), creates a draft
prefilled from the mapped Quick Add fields, opens the existing product/recipe editor, and commits the
new custom item **only when that editor is saved**. Field mapping follows the spec's tables. The
recipe-nutrition seeding approach (initial manual-nutrition mode, metadata estimate until ingredients
replace it, or a single placeholder ingredient) is chosen per Story 14's findings and documented so
it does not corrupt Food You's ingredient-derived recipe model. Cancelling the editor leaves the
source entry and its edits intact and creates nothing; repeated promotion is allowed (no hidden
one-time lock without explicit UX).

**Why / value:** Lets fast estimates graduate into reusable products and recipes later — the
"capture now, refine later" workflow at the heart of the Quick Add expansion.

**Rough scope:** Promotion actions in the shared Quick Add component, prefill entry points into the
product and recipe editors, and source-entry preservation. **Depends on Story 18** (maps its new
fields) **and Story 14** (recipe model and snapshot model).

**Source:** [Milestone 2 feature spec](../dictations-tier-0/2026-07-25_milestone-2_australian-providers-and-quickadd-spec.md)
(§6).

**Status:** Complete (2026-07-25, commit `761fc966`) — Promote to Product / Promote to Recipe in the Quick Add overflow menu (create, edit, and historical entries). Product drafts map totals to per-100 g via real weight (1:1 fallback, no invented values); recipes seed via a single placeholder ingredient (real backing product) so ingredient-derived totals reproduce the estimate; cancel deletes the placeholder and creates nothing; original diary snapshots proven unchanged; repeated promotion allowed. All E2E paths emulator-verified; mapping unit-tested.

---

<a id="story-20"></a>

### Story 20: FoodUs identity — rename, versioning, icon

**Type:** Feature

**Summary:** The app becomes **FoodUs**, superseding both "Food You" and the interim
"ACME Food App" identity from [Story 1](#story-1). Rationale: Milestone 3's spine is multi-user
support — "Us", not "You" (addendum item 5). The addendum floated Milestone 3 placement, but the
rename was executed immediately because the applicationId is permanent and had to change before
the first release-signed phone install — the only free moment. Scope: app name, applicationId,
About/config URLs, user agent, repo rename, versioning scheme, and the owner's hand-made icon.

**Why / value:** The permanent identity, locked in while changing it was still free; the
`<milestone>.<story>.<build>` versioning makes releases, tags, and Obtainium agree.

**Rough scope:** Identity resources, build config, CI workflows, README/docs, launcher icon
assets. Source namespace `com.maksimowiczm.foodyou` deliberately untouched for upstream merges.

**Status:** Complete (2026-07-26, commits `7dc9adc7` + `b7423b1e` + icon commit) — name "FoodUs";
applicationId `io.github.jarrydadaens.foodus`; repo renamed to `FoodUs`; versioning
`<milestone>.<story>.<build>` (shipped versionName = fork version, upstream 3.4.9 demoted to
derived-from metadata; release workflow guards tag↔version agreement); v2.19.0 released and
installed via Obtainium on the owner's phone. Owner's 256×256 icon wired as legacy + round +
adaptive launcher icons (background sampled from the art; old-brand monochrome layer removed, so
themed-icon launchers show the colored icon). About-screen credits carrying the owner's name:
verify on-device; upstream attribution from Stories 1/4 retained.

---

<a id="story-21"></a>

### Story 21: Three-layer AI prompt architecture

**Type:** Feature

**Summary:** Replace Story 6's single embedded prompt ("we're Australians in Victoria, this is my
food") with three additive layers (addendum item 1): **(1) developer prompt** baked into the app,
stripped of ALL user data — pure machinery (task framing, JSON output shape); **(2) user system
prompt** — optional, user-authored in the AI settings screen (Story 22), holding all personal
context (locale, diet, allergies, camera), applied to every diet-related AI call; **(3) per-scan
hint** — optional free-text field on the AI scanning screen ("I'm at McDonald's"), applied to
that call only. Layers are additive. The scanning screen also gains a big **Submit** button —
image required, hint optional.

**Why / value:** Nothing personal ships in the codebase — the baked prompt becomes merge-safe
and public-repo-safe; users steer the AI without code changes.

**Rough scope:** `ai` slice prompt assembly, AI scanning screen (hint field + Submit), Story 9's
query-generation call inherits the same layering. **Depends on Story 22** for the layer-2 field.

**Status:** Not Started.

---

<a id="story-22"></a>

### Story 22: AI settings screen

**Type:** Feature

**Summary:** A settings surface (near the existing key entry) with four fields (addendum item 2):
**OpenAI-compatible API key** (user-entered); **endpoint** (defaults to OpenRouter, any
OpenAI-compatible endpoint allowed); **model** (free text) with a **Validate** button; and the
**user system prompt** (Story 21's layer 2). Validate behavior is decided, not open: one live
call to the configured endpoint/model/key with a trivial one-token prompt — green tick on
success, the returned error string on failure. One code path, no provider-specific handling;
deliberately advanced-user.

**Why / value:** Replaces the baked-key design (the Story 6 secret-injection open question
dissolves — no AI credential ever exists in the repo, CI, or any build) and makes the AI stack
fully user-configurable.

**Rough scope:** New settings screen + on-device persistence (DataStore, like the USDA key), the
`ai` slice reads runtime config instead of BuildConfig (BuildConfig `foodus.ai.*` values remain
as blank-by-default developer fallbacks), validation call.

**Status:** Not Started.

---

<a id="story-23"></a>

### Story 23: Provider website info links

**Type:** Feature

**Summary:** Every remote/composition food-data provider's import/update screen gets a button
linking out to that provider's website for more info (addendum item 4). Applies to **all**
providers — AFCD, Swiss FCD, Open Food Facts, USDA — not just one.

**Why / value:** Users can check what a dataset actually is before importing or trusting it.

**Rough scope:** One outbound-link affordance per provider screen; provider metadata gains a
website URL. The addendum's open observation (owner described an *Australian* database screen
not in older docs) is resolved by repo truth: that screen is this fork's own Story 15 AFCD
provider — docs were not stale and upstream added nothing.

**Status:** Not Started.

---

## Interdependency Order

1. Story 5 (four-button rework) hosts Stories 6 and 8; build the button surface first or alongside.
2. Story 6 (AI scanning) precedes Story 7 (gallery source).
3. Story 8 (placeholder) precedes Story 9 (meta screen); Story 9's AI mode shares endpoint
   plumbing with Story 6.
4. Identity/About stories (1-4) are independent of the logging stories and of each other, though
   3 and 4 touch the same About screen as 1 — sequence them to avoid churn.
5. Stories 10-12 are independent.
6. Story 13 (meal templates) is independent of all other stories.
7. The provider + Quick Add group (Stories 14-19) is a self-contained capability set, independent of
   the identity / AI / ergonomics stories (1-13).
8. Story 14 (architecture spike) blocks Stories 15-19 — it resolves the spec's Section 13 decisions
   and settles the reusable-pipeline shape and the diary-snapshot model before any code is written.
9. Story 15 (AFCD) establishes the reusable provider-import pipeline; Story 16 (FoodSwitch) reuses it
   and is gated on Story 14's feasibility verdict; Story 17 (update UI) needs a real downloadable
   provider (Story 15) to drive it.
10. Story 18 (Quick Add fields) precedes Story 19 (promotion), which maps those fields; both rely on
    Story 14's diary-snapshot and nutrition-semantics findings.
11. Addendum stories (2026-07-26): Story 22 (AI settings) precedes Story 21 (three-layer prompts —
    its layer-2 field lives on the settings screen); Story 20 (identity) is complete and
    independent; Story 23 (provider links) is independent.

---

## Backlog Sources

- Stories 1-13 were mapped directly from the 2026-07-24 initial project seed. Story 12 feeds new
  bug stories into [../backlog/](../backlog/).
- Stories 14-19 were synthesized from the
  [2026-07-25 Milestone 2 feature spec](../dictations-tier-0/2026-07-25_milestone-2_australian-providers-and-quickadd-spec.md)
  (Australian food-data providers + Quick Add expansion). Supermarket scraping is an explicit
  non-goal of that spec and is not represented by any story.
- Stories 20-23 were synthesized from the
  [2026-07-26 addendum](../dictations-tier-0/2026-07-26_addendum_ai-settings-branding-foodus.md)
  (items 1, 2, 4, 5; item 3 completed Story 3 in place). The addendum's Milestone 3 definition
  (multi-user) is a separate effort and is not represented here.

---

## Deferred / Follow-up Work

- Decide the third version-number component (deploy counter?) — during Story 1.
- Decide the AI-key injection mechanism for private builds — during Story 6 planning.
- FoodSwitch data-access feasibility is unresolved until Story 14; Story 16 is gated on its verdict
  and may be recorded as a documented external blocker rather than implemented.
- Diary-snapshot prerequisite (spec §7.5): read-only recon on 2026-07-25 found diary entries are
  **already** immutable nutritional snapshots — product/recipe entries were unlinked from live rows
  by Room migration 25→26 (`UnlinkDiaryMigration`) into `DiaryProduct`/`DiaryRecipe` snapshot tables,
  and manual/Quick Add entries store embedded nutrient columns. So the prerequisite appears
  satisfied and no foundational conversion story is expected; Story 14 confirms it formally.

---

## Notes

- Keep this Story Index in sync with the [Milestones Index](../design.md#milestones-index) in Design.
- Fork philosophy applies to every story here: additive overlays, minimal merge-conflict surface.
