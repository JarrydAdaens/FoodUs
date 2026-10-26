---
name: milestone-2
description: Milestone 2 - Customisation. FoodUs identity, AI-assisted logging, OCR-assisted label capture and evaluation, ergonomics, providers, expanded Quick Add, and distinct Product/Recipe/Meal/DiarySlot roles.
metadata:
  version: "3.9"
  agentic_rails_source_version: "3.0"
  owner: "Jarryd Adaens"
  repo: "FoodUs (fork of maksimowiczm/FoodYou)"
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
Saved reusable bundles are presented as Meals, with a central place to view and edit them.
Settings hosts a unified Recipes, Meals & Products library directly below Daily goals,
with searchable tabs, nutrition summaries, and creation actions.
Products, Recipes, Meals, DiarySlots, and DiaryEntries have distinct code roles and
consistent serving/portion behavior across creation, editing, and logging.
Diary-slot menus support bulk deletion, moving and copying entries across days and slots.
An opt-in developer UI name bar makes elements identifiable when discussing changes with agents.
OCR-assisted nutrition-label capture preserves printed facts, with a repeatable local evaluation
harness for comparing vision models, OCR providers and combined pipelines.

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
reusable Meals let a day's entries be saved and re-applied on other days, a central
Recipes, Meals & Products library in Settings supports searching, creating and editing
saved items through distinct tabs, replacing the Home Meals shortcut while retaining
bundle management and the accepted domain vocabulary,
Products and Recipes retain their distinct entry/portion semantics, and adopted
upstream bugs are fixed. Diary-slot menus can delete all entries, move or copy them to
another day/slot, and copy entries from another day/slot into the current one.
In addition, the Australian Food Composition Database provider works
through the normal provider UI with locally stored, offline-first search; FoodSwitch is either
integrated or its access blocker is documented; provider update-checks persist the last successful
check and full refreshes never mutate history; and Quick Add supports description, fibre, serving
count, and weight, plus promotion of new and historical entries into custom products and recipes
without altering the original diary snapshot. The release-available UI name bar can identify
visible elements and copy source-oriented locators and detailed hierarchy for agent collaboration.
OCR adds an optional label path through classification, provider-neutral extraction and strict
text-to-nutrition parsing, with correct units/reference bases, explicit stage failures, protected
settings and existing review/diary semantics. Ordinary food photos remain one AI call. A controlled
approximately ten-case corpus, config/WinForms runner, independent grading, stability metrics
and offline comparisons must make accuracy, cost and latency measurable without exposing keys.

## Status

In Progress — reopened 2026-07-26 by the
[owner's addendum](../dictations-tier-0/2026-07-26_addendum_ai-settings-branding-foodus.md) after
daily use. **29 of 39 Stories Complete, 1 Blocked, and 9 Not Started.** All addendum
Stories 21–23 landed 2026-07-27. Story 16 (FoodSwitch) remains blocked on an external
data licence. Stories 25 (domain separation), 26 (Product terminology/portions), and 27 (Recipe
composition/portions) passed acceptance under the [serial run](../_boss-runs/milestone-2-domain-meals-sequential/briefing.md). Story 24 (Meals library/editor) also passed acceptance.
The original Product/Recipe/Meal batch is delivered. Story 28 adds the owner-requested
faithful personal-library import and passed acceptance on 2026-10-26 under its adopted implementation plan.
Story 29 delivered the owner-requested UI name bar for agent collaboration, with debug,
minified runtime, source lookup, and coverage acceptance on 2026-10-26.
Stories 30–37 schedule the owner's OCR nutrition-label and evaluation addendum on 2026-10-26.
They are Not Started; this context work authorizes neither their implementation nor paid/live
evaluation. Existing closed counts and the policy-derived app version remain unchanged.
Story 38 schedules the owner's 2026-10-26 request to move library access into Settings
and replace the basic Meals list with a unified Recipes, Meals & Products library.
Story 39 adds the owner's diary-slot bulk menu actions. Status: Complete (2026-10-26; 213 unit tests, 26 executed emulator checks, schema 43 migration, both layouts, snapshot/rollback and process-restart acceptance passed).
The milestone is not yet closable:
the new OCR and unified-library acceptance is outstanding, and its Definition of Done's
"adopted upstream bugs are fixed" clause is unmet — the Story 12 bug stories are still staged in
`../backlog/backlog-1.md` and were never pulled into this milestone's story list. Closing requires
the owner to either schedule those fixes or revise that DoD clause. The original 19-story run
was delivered 2026-07-25 by rails-boss-execute (see `../rails-boss-execute/progress.md`).

### History reconciliation — 20 October 2026

Reviewed the available local Git history through `ad4ca612`, the Story plans, and the current
source. The count remains **22 Complete, 1 Blocked**. No additional Story completion is supported
by later commits. The latest app implementation is Milestone 3's `f6229b0b`; the subsequent
commits update context and plan organization.

| Evidence | Current conclusion |
| --- | --- |
| Story implementation commits `dc683e1d` through `761fc966`, with the blocked-path record `5819da3e` | Stories 1–19 retain their recorded results: 18 complete; Story 16 blocked on a written FoodSwitch data licence. Recording the blocker fulfils the fallback deliverable, but does not deliver a provider. |
| `7dc9adc7`, `b7423b1e`, `4e0ed878` | Story 20's rename, shipped version wiring, and launcher icon are implemented. The icon commit is now identified explicitly below. |
| `02ec0391`, `4ec602d4`, `ebb28703`; status reconciliation `d843bc1b` | Stories 21–23 are complete. Story 21 also closed Story 22's runtime configuration UI follow-up. Both current ViewModels consume `ObserveAiConfigured`. |
| `a831dbb7` and [backlog Stories 5–9](../backlog/backlog-1.md#story-5) | Story 12 completed triage and adoption. The five adopted fixes remain unscheduled; no later fix commits were found. The milestone's bug-fix closure condition remains open. |

The plans retain the recorded unit, build, and emulator evidence from implementation. This
reconciliation did not rerun those checks. Successful AI scan, query, and validation calls still
have no recorded live success evidence; do not treat the failure-path checks as that proof.
The next closure step is to schedule the adopted fixes or obtain an owner revision of the
Definition of Done. Story 16 can resume only after its licence gate is released.

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
| 21 | Complete | [Three-layer AI prompt architecture](#story-21) | Feature | 4 | 4 | 3 | [plan](../implementation-plans/milestone-2/story-21-three-layer-ai-prompts/plan.md) |
| 22 | Complete | [AI settings screen](#story-22) | Feature | 4 | 5 | 4 | [plan](../implementation-plans/milestone-2/story-22-ai-settings-screen/plan.md) |
| 23 | Complete | [Provider website info links](#story-23) | Feature | 2 | 2 | 1 | [plan](../implementation-plans/milestone-2/story-23-provider-website-links/plan.md) |
| 24 | Complete | [Meals library and editor](#story-24) | Feature | — | — | — | [plan](../implementation-plans/milestone-2/story-24-meals-library-editor/plan.md) |
| 25 | Complete | [DiarySlot and Meal domain separation](#story-25) | Feature | — | — | — | [plan](../implementation-plans/milestone-2/story-25-diary-slot-meal-separation/plan.md) |
| 26 | Complete | [Product terminology and portioned entry](#story-26) | Feature | — | — | — | [plan](../implementation-plans/milestone-2/story-26-product-terminology-portions/plan.md) |
| 27 | Complete | [Recipe composition, yield, and diary portions](#story-27) | Feature | — | — | — | [plan](../implementation-plans/milestone-2/story-27-recipe-composition-portions/plan.md) |
| 28 | Complete | [Structured personal-library import](#story-28) | Feature | — | — | — | [plan](../implementation-plans/milestone-2/story-28-structured-personal-import/plan.md) |
| 29 | Complete | [Developer UI name bar for agent collaboration](#story-29) | Feature | — | — | — | [plan](../implementation-plans/milestone-2/story-29-developer-ui-name-bar/plan.md) |
| 30 | Not Started | [Shared OCR routing, nutrition-basis and trace contracts](#story-30) | Feature | — | — | — | — |
| 31 | Not Started | [OCR.space provider adapter](#story-31) | Feature | — | — | — | — |
| 32 | Not Started | [OCR settings and safe configuration testing](#story-32) | Feature | — | — | — | — |
| 33 | Not Started | [Single-call vision classification and food estimation](#story-33) | Feature | — | — | — | — |
| 34 | Not Started | [Strict OCR-text nutrition parser](#story-34) | Feature | — | — | — | — |
| 35 | Not Started | [OCR-assisted capture, review and recovery](#story-35) | Feature | — | — | — | — |
| 36 | Not Started | [Controlled AI/OCR corpus and evaluation runner](#story-36) | Tooling | — | — | — | — |
| 37 | Not Started | [Nutrition grading, stability and offline comparisons](#story-37) | Tooling | — | — | — | — |
| 38 | Not Started | [Recipes, Meals & Products library in Settings](#story-38) | Feature | — | — | — | [plan](../implementation-plans/milestone-2/story-38-recipes-meals-products-library/plan.md) |
| 39 | Complete | [Diary-slot bulk delete, move and copy actions](#story-39) | Feature | — | — | — | [plan](../implementation-plans/milestone-2/story-39-diary-slot-bulk-actions/plan.md) |

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

**Status:** Complete (2026-07-25, commit `dc683e1d`) — applicationId `com.acme.foodapp` (namespace/source packages untouched for mergeability); fork version 1.0.0 layered over untouched upstream 3.4.9; About screen shows "Derived from Food You 3.4.9". This is the historical delivery state; [Story 20](#story-20) supersedes the interim identity, and [the versioning policy](../versioning-policy.md) resolves the third component as the build number.

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

**Follow-up:** [Story 24](#story-24) adds the Meals library, editing, and the owner's revised
UI terminology. This extends the delivered feature; Story 13 remains Complete.

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

**Status:** Blocked (documented) — external licence blocker (2026-07-25, commit `5819da3e`; gate evaluated by Story 14). No public API/bulk download, and FoodSwitch Global Terms of Use forbid storing/deriving the data. Per this story's own instruction the blocker is recorded and work stopped; no scraped substitute. Unblock: owner obtains a written data licence (`foodswitch@georgeinstitute.org.au`), then reuse Story 15's pipeline.

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

**Status:** Complete (implemented 2026-07-26–27, commits `7dc9adc7` + `b7423b1e` + `4e0ed878`) — name "FoodUs";
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

**Status:** Complete (2026-07-27, commit `4ec602d4`) — both baked prompts rewritten to pure machinery (`AiScanPrompt` domain object keeps the verbatim seven-key JSON contract; grep-proven zero personal tokens in the `ai` slice and aiscan UI, locked by inverted unit tests); user system prompt sent as a system message on both the scan and query calls when set; per-scan hint field + Submit button (image required, hint optional, in-memory and scan-call-only; Submit replaces Ask AI). Also closed Story 22's recorded follow-up: both ViewModel `aiConfigured` gates now observe runtime config via `ObserveAiConfigured`. Emulator-verified incl. reactive gating and the 401 failure path; live steering/success round-trips await a real key.

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

**Status:** Complete (2026-07-27, commit `02ec0391`) — AI settings screen (key/endpoint/model/user system prompt) persisted in DataStore with explicit Save (blank→null); runtime precedence user → blank-by-default BuildConfig fallback → domain default, unit-tested; both OpenRouter scanners read runtime config; Validate = one `max_tokens=1` probe with verbatim error surfacing (failure paths emulator-verified; green-tick path awaits a real key). The two ViewModel `aiConfigured` gates were subsequently fixed by [Story 21](#story-21), commit `4ec602d4`: both now observe runtime configuration through `ObserveAiConfigured`. The original gap in Story 22's execution record is historical, not an outstanding task.

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

**Status:** Complete (2026-07-27, commit `ebb28703`) — shared `WebsiteChip` (AssistChip + open-in-new) on all four provider surfaces: OFF card → world.openfoodfacts.org, USDA card → fdc.nal.usda.gov, Swiss FCD (language-pick state only) → naehrwertdaten.ch, AFCD screen → the FSANZ page (reuses `SOURCE_URL`). URLs live once in `AppConfig`/`FoodYouConfig` as static config — no schema change (the rough scope's "provider metadata gains a website URL" is satisfied in configuration, not a Room column). All four outbound intents emulator-verified.

---

<a id="story-24"></a>

### Story 24: Meals library and editor

**Type:** Feature

**Status:** Complete (2026-10-25; central Meals library/editor, captured portions, atomic operations, and runtime acceptance verified).

**Delivery evidence:** [Batch completion record](../_boss-runs/milestone-2-domain-meals-sequential/domain-meals-batch-receipt.json),
[implementation plan](../implementation-plans/milestone-2/story-24-meals-library-editor/plan.md).
The final implementation passed 173 unit tests in 35 suites, debug/test and unsigned
release builds, release vital lint, and independent final identity review. The batch
records 29 distinct passing instrumentation methods with explicit artifact provenance;
affected workflows passed after their fixes and unchanged behavior retains reviewed
prior-artifact results. Both registered schema 39→40→41 and 40→41 paths preserve the
original synthetic data; the real app navigation and saved edit survived process restart.
Schema 41 adds only six nullable quantity-context columns. Previously stored facts, IDs,
order, unknown amounts and Recipe calculation contracts remain preserved. Verified
candidate: `3.2.36.3-dev` / code `132`. No commit or public release was made.

**Summary:** Present saved reusable bundles of food entries to users as **Meals**, and give
them one central place to view, edit, and manage every saved Meal. Preserve the existing
save-and-load workflow: save a bundle once, then add its separate entries to a chosen diary
day and DiarySlot whenever it is eaten again. Use the accepted
[Design vocabulary](../design.md#domain-model-shared-vocabulary): Product, Recipe, Meal,
DiarySlot, and DiaryEntry, with serving and portion given separate meanings.

**Why / value:** Recurring meals need maintenance as portions or ingredients change. Today
the saved bundles can be applied or deleted from a meal-card sheet, but cannot be edited
in a dedicated library, and the UI exposes the implementation word "template".

**Naming contract:** Use **Meal** / **Meals** in labels, titles, dialogs, accessibility text,
and user-facing messages for this feature; examples are "Save Meal", "Load Meal", and
"Meal name". The owner's later 2026-10-25 decision supersedes the earlier `MealTemplate`
target naming: the reusable bundle becomes **Meal** in code too, while the existing
diary-category `Meal` becomes **DiarySlot**. Align related components, state, routes,
use cases, and repository APIs with their roles. Product remains the atomic nutritional
item and Recipe the composite item; Food is UI vocabulary. The common `Food` interface
needs its own role-based naming decision, since it also covers Recipes. Do not globally
replace Food with Product or rename unrelated template concepts and historical records.

**Transition ownership:** [Story 25](#story-25) owns the coupled diary-category/reusable-
bundle rename and compatibility baseline; [Story 26](#story-26) owns the common Product/
Recipe abstraction and Product portion contract; [Story 27](#story-27) owns Recipe yield
and composition behavior. This Story consumes those foundations and owns the Meals
library/editor and any additional bundle-editing metadata/migration it needs. Preserve
IDs and existing entries; naming alone never authorizes destructive storage changes.

**Existing implementation evidence (2026-10-25 source inspection):** Story 13 already owns
the `fooddiary` domain types, snapshot persistence, and save/apply use cases. Its
`MealTemplateRepository` exposes observe/read/save/delete, with no update operation.
The diary home `MealCard.kt` contains `SaveTemplateDialog` and `ApplyTemplateSheet`;
the shared English strings say "Save as template", "Apply template", and "Template name".
`MealTemplateItem` stores a name and portion-adjusted nutrition totals, rather than a live
food reference or an explicit per-100 g serving model. Build on these contracts.

**Rough scope / acceptance criteria:**

- Provide a clearly discoverable **Meals** library reachable independently of a populated
  diary meal card. Show all saved Meals, a useful empty state, their names and constituent
  entries, and a path to edit or delete a selected Meal. The central library and existing
  diary save/load actions observe the same persisted collection.
- A user can rename a Meal and add, remove, or edit its constituent entries and their
  portions/nutrition. Changes can be saved or cancelled. Reject blank names and empty or
  invalid bundles with clear feedback; failed saves preserve the prior saved Meal. Support
  the existing snapshot model and do not invent gram weights or per-100 g values that the
  saved data does not contain.
- Saving a diary bundle still creates a reusable Meal. Loading a Meal into a selected
  date and diary slot appends each constituent as its own diary entry with the intended
  amounts and nutrition; it does not replace existing entries or collapse the bundle
  into a Recipe. An explicit load applies it once; a failed operation leaves no partial
  bundle. Persist edits across navigation and app restart.
- Apply Meal/Meals wording across this feature's entry points, library/editor, save/load
  dialogs, errors, and accessibility text using shared string resources. Internal naming
  follows the accepted Product/Recipe/Meal/DiarySlot/DiaryEntry contract. Breakfast/Lunch
  remain diary destinations; a Recipe within a loaded Meal remains one constituent entry.
  A serving is a reference quantity and a portion the amount used or eaten; preserve
  Quick Add absolute totals and unknown original amount/unit values.
- Existing saved Meals survive an in-place upgrade. Editing or deleting a saved Meal
  affects future loads only; previously logged diary snapshots and source foods/recipes
  remain unchanged. Keep stable identities and use a data-preserving migration if the
  editor needs schema changes; avoid destructive storage renames for terminology alone.
- Add meaningful regression checks for update/cancel/validation, persistence, transactional
  load, and historical snapshot isolation. Verify the visible save → library → edit →
  load → delete workflow on a device or emulator with synthetic data, including existing
  saved Meals and the empty-library path. No cloud or relay dependency is introduced.

**Dependencies and planning seam:** Builds on completed [Story 13](#story-13); implementation
follows [Stories 25](#story-25), [26](#story-26), and [27](#story-27) so the library/editor
can use settled names and Product/Recipe portion contracts. No FoodSwitch licence or
multiplayer/server gate blocks this work. Its plan must choose the library's navigation
entry point and the editor's handling of legacy captured totals, then identify migration
and upgrade checks. A new unified catalogue hub is not required by this Story.
The [implementation plan](../implementation-plans/milestone-2/story-24-meals-library-editor/plan.md)
owns the selected editor/migration choices and execution evidence; this Story owns
status and acceptance. Implementation follows accepted Stories 25–27.

---

<a id="story-25"></a>

### Story 25: DiarySlot and Meal domain separation

**Type:** Feature

**Status:** Complete (2026-10-25; synthetic upgrade, persistence, visible navigation, and independent review passed).

**Delivery evidence:** [Completion receipt](../_boss-runs/milestone-2-domain-meals-sequential/story-25-receipt.json),
[implementation plan](../implementation-plans/milestone-2/story-25-diary-slot-meal-separation/plan.md).
The receipt is provisional until sealed and reconciled. Final source passed 149 unit
tests, debug/test APK builds, unchanged schema 39, an in-place code 128 → 129 upgrade
with byte-identical synthetic rows, copied-backup/save/load/delete/slot-edit tests,
and configured-slot navigation. Verified candidate: `3.2.33.3-dev` / code `129`.
No commit or public release was made.

**Problem / outcome:** The source currently calls a diary category `Meal` and a reusable
bundle `MealTemplate`. Implement Design's accepted distinction: `DiarySlot` is the
destination (Breakfast, Lunch, etc.), `Meal` is the saved bundle, and `DiaryEntry` is a
dated nutritional snapshot. This provides a safe naming foundation for the entry and
Meals-library work without changing the meaning of existing data.

**Scope and acceptance:**

- Rename the diary-category domain/API family to DiarySlot, including category identifiers,
  repository/use-case parameters, UI state/navigation, and relevant tests. Preserve each
  slot's stable ID, configured name, time window, order, and existing entry assignments.
- Move the reusable MealTemplate family to Meal, including items, IDs, persistence-facing
  APIs, and existing save/load/delete flows. Keep its identity separate from DiarySlot.
  Resolve the type-name collision in a coherent, buildable sequence.
- Inspect `DiaryMeal`'s category-plus-entries projection and choose a precise name for that
  role in the plan before renaming it. It must not become the reusable Meal definition.
  Update current feature labels/accessibility text that confuse slots and saved Meals;
  unrelated template concepts, historical records, and upstream namespaces retain identity.
- Explicitly retain legacy Room table/column and serialized/backup names where possible.
  If any storage or serialized change is necessary, provide a data-preserving migration
  or adapter. A type rename must not recreate tables, merge slot/bundle IDs, or silently
  change external contracts. Relay contract changes are outside this Story.
- Preserve the existing behavior: saving captures a bundle, loading appends its constituent
  snapshots separately to the chosen date/slot, and deletion affects future loads only.
  Product, Recipe, Quick Add, placeholder, and existing snapshot behavior must survive.
- Verify an in-place upgrade with synthetic existing slots, saved bundles, and all relevant
  entry kinds. Compare IDs, assignments, ordering, and nutritional snapshots before/after;
  exercise save/load/delete and configured slot navigation on a device or emulator. Check
  backup compatibility and run meaningful domain/persistence tests and the affected build.

**Dependencies and ownership:** Builds on completed [Stories 13](#story-13),
[18](#story-18), and [19](#story-19). It is the
first implementation step in this batch. Story 26 owns the polymorphic Food/Product/
Recipe interface work; Story 24 owns the new Meals manager/editor. This milestone owns
status; create its implementation plan before the coupled refactor. Risk is concentrated
in name collisions and persistence/backup compatibility; no time estimate is asserted.

---

<a id="story-26"></a>

### Story 26: Product terminology and portioned entry

**Type:** Feature

**Status:** Complete (2026-10-25; typed terminology, Product portion/basis rules, and synthetic runtime acceptance verified).

**Delivery evidence:** [Completion receipt](../_boss-runs/milestone-2-domain-meals-sequential/story-26-receipt.json),
[implementation plan](../implementation-plans/milestone-2/story-26-product-terminology-portions/plan.md).
The linked per-Story receipt records its dated acceptance boundary; the current
[batch record](../_boss-runs/milestone-2-domain-meals-sequential/domain-meals-batch-receipt.json) supersedes shared artifacts. Final production passed 159 unit
tests, debug/test APK builds, unchanged schema 39, and independent 200-path review.
Ten selected emulator methods passed: real-Room persistence, two actual-screen Product
workflows, six Quick Add/scan regressions, and byte-identical existing-data verification.
The test-only editable-field correction retained exact assertions and timeout; failed
attempt logs remain preserved. Verified candidate: `3.2.34.3-dev` / code `130`.
No commit or public release was made.

**Problem / outcome:** Food is UI vocabulary, but the current `Food`/`FoodId` abstraction
and related APIs mix Product and Recipe roles. Make the atomic item explicitly Product
in code, retain friendly Food wording in the UI, and make the reference-serving versus
actual-portion contract consistent through the existing Product create/edit/select/log
flows. The Product editor already exists; this is reconciliation, not a replacement app.

**Scope and acceptance:**

- Settle the common abstraction's responsibilities and a precise role-based name in the
  plan and Design before implementation. Preserve support for both Products and Recipes;
  never rename the whole shared interface or its polymorphic IDs to Product. Keep typed
  Product and Recipe identities distinct even if their numeric IDs happen to match.
- Apply that decision to the relevant entities, identifiers, repositories, use cases,
  search/selection, provider/import adapters, DI, navigation, and tests. Use Product for
  Product-only roles and the selected shared name for genuinely polymorphic roles. Do not
  blindly replace the Food substring in FoodUs branding, namespaces, history, or external
  CSV/backup/wire fields. Preserve or explicitly map their established contracts.
- Preserve Product name, nutrition, brand/barcode/notes, source, package/serving metadata,
  liquid status, and existing IDs. Keep canonical per-100 g or per-100 mL nutrition while
  making the editor's basis, reference serving, and amount being logged understandable.
  Food remains appropriate user-facing wording and shared string resources own the labels.
- A known 40 g serving logged twice must give the same portion and nutrition as logging
  80 g directly. Verify weight and liquid-volume cases, edits, and valid fractional
  portions. Unknown serving/package weights remain unknown; unsupported conversions and
  invalid amounts fail clearly rather than inventing a gram basis or corrupting values.
- Product edits affect future use, not historical DiaryEntry snapshots. Quick Add's optional
  serving count and weight remain descriptive metadata for its absolute totals; do not
  apply Product scaling to manual entries or alter existing promotion behavior.
- Run meaningful conversion, typed-identity, persistence, and snapshot regressions. Verify
  create → edit → select portion → log → edit definition again with synthetic data on a
  device/emulator, including cancellation/failure and an existing-data upgrade where the
  implementation changes storage. The affected build and relevant tests must pass.

**Dependencies and ownership:** Implementation follows [Story 25](#story-25); existing
Product/provider/Quick Add foundations remain in place. This Story owns the shared
abstraction decision and Product portion contract; Story 27 applies those contracts to
Recipes, and Story 24 to saved Meals. No new catalogue hub, serving-unit catalogue,
photo/import feature, or external data licence is selected here. The plan must bound the
identifier/API refactor and any required compatibility work before execution; effort and
elapsed delivery remain unestimated.

---

<a id="story-27"></a>

### Story 27: Recipe composition, yield, and diary portions

**Type:** Feature

**Status:** Complete (2026-10-25; Recipe composition, historical calculation preservation, and synthetic runtime acceptance verified).

**Delivery evidence:** [Completion receipt](../_boss-runs/milestone-2-domain-meals-sequential/story-27-receipt.json),
[implementation plan](../implementation-plans/milestone-2/story-27-recipe-composition-portions/plan.md).
Final acceptance passed 165 unit tests in 34 suites, debug/test APK builds, independent
62-path identity review, and 15 distinct emulator methods. Both actual Recipe screen
workflows and the final canonical verifier passed current artifacts; twelve unaffected
checks retain explicit prior-artifact provenance. Schema 39→40 preserves legacy snapshot
calculations through contract 0, while fresh captures use contract 1. The projected old
columns exactly match the original synthetic fixture. Verified candidate: `3.2.35.3-dev` /
code `131`. No commit or public release was made.

**Problem / outcome:** Reconcile the existing Recipe create/edit/log workflow with the
accepted model: a Recipe combines ingredients into one composite item; yield defines
reference servings, while a diary portion defines how much was eaten. Keep this distinct
from the saved Meal bundle and from DiarySlot, using the renamed shared infrastructure.

**Scope and acceptance:**

- Use Recipe consistently for its types, IDs, creation/editing, ingredient selection,
  navigation, state, resources, and tests. Reuse existing Product/Recipe ingredient and
  measurement models, including currently supported nested Recipes; keep them separate
  from saved Meal definitions. Preserve existing Recipe identities, notes, and liquid status.
- Make name, ingredients and amounts, yield, and portion-preview meaning clear in the
  existing forms. Adding/removing/changing an ingredient or yield must update the saved
  definition coherently. Explicit Save commits validated changes; Cancel and failed saves
  preserve the prior definition. Reject invalid yields and quantities at their boundaries.
- Verify ingredient totals and per-serving calculations without double scaling: a Recipe
  yielding three servings, logged as two servings, represents two thirds of its total
  nutrition. Weight-based portions must agree with their known basis; missing weights or
  nutrition are preserved and handled explicitly, not manufactured to make a preview work.
- Ordinary Recipe logging creates one portioned DiaryEntry in the selected date/DiarySlot.
  Preserve the existing explicit unpack action as a separate deliberate operation and
  verify its constituent amounts/totals; do not automatically expand ordinary Recipe logs.
- Changing or deleting the saved definition must not rewrite existing diary snapshots.
  In a saved Meal, a Recipe portion remains one constituent; loading that Meal adds it
  beside the other items. Verify this boundary before Story 24 builds its editor on it.
- Run meaningful yield/portion, composition, failure/cancel, persistence, and snapshot
  tests. Verify create → add/edit ingredients → set yield → save → log → edit definition
  on a device/emulator with synthetic data, including existing-data upgrade checks where
  storage changes. No new web/bulk import, photo, sharing, or Meal-to-Recipe conversion
  feature is included.

**Dependencies and ownership:** Implementation follows [Stories 25](#story-25) and
[26](#story-26). Product lookup and the shared abstraction/portion contract come from
Story 26; Story 27 owns Recipe composition/yield behavior; Story 24 owns the Meals library
and legacy-bundle editing model. Its plan must inspect the existing Recipe validation,
save/cancel and unpack behavior before choosing changes. No implementation estimate or
external waiting time is asserted; no relay or FoodSwitch gate applies.

---

<a id="story-28"></a>
### Story 28: Structured personal-library import

**Type:** Feature

**Status:** Complete (2026-10-26; structured additive import, private package, schema42 upgrades, editing/logging and final private APK delivery verified).

**Problem / outcome:** The existing CSV importer creates Products only and the historical
personal-data exporter flattened Recipe and Meal structure. Database restore replaces
the whole database. The recovered personal library also contains reference-serving Food
facts without metric mass, ordered unlinked Recipe components, reported nutrition with
unknown yields, and Meal snapshots with original portion text. A transport wrapper around
the current strictly linked/metric-only models cannot preserve these records faithfully.
Provide a FoodUs-owned structured additive import and the minimum definition/snapshot
support needed to use this library without fabricating missing facts or replacing diary
and settings. This was explicitly requested by the owner if the late migration task found
missing support; it extends rather than reopens Stories 24–27.

**Acceptance and bounds:**

- Define one versioned FoodUs-owned local format for Products, Recipes and Meals, using
  the maintained vocabulary. Source-specific account adapters, originals, inventories,
  generated packages and private validation stay outside this public repository. Public
  fixtures are synthetic; no extracted third-party app material enters implementation.
- Support declared serving-reference Food nutrition with unknown grams/milliliters.
  Preserve supplied reference descriptions and known metric facts independently; log
  only resolvable requested portions. Never invent a 100 g basis, density or weight.
- Preserve a Recipe's reported nutrition separately from ingredient-derived nutrition,
  optional genuinely unknown yield, ordered inline ingredient descriptions/amount text,
  and optional typed links. Do not derive missing component facts from totals, guess
  identity links, or silently replace an authoritative panel with an incomplete sum.
  Existing linked Recipe calculations and historical snapshot contracts remain stable.
  Show the nutrition basis clearly; unsupported portion conversions remain unavailable.
  Planning must explicitly define editing/invalidation rules for reported definitions.
- Import saved Meals as ordered absolute constituent snapshots, preserving original
  portion text and any separately reported whole-Meal totals without silently adjusting
  constituent facts. Loading retains one diary entry per constituent and does not
  reinterpret descriptive counts as nutrition multipliers or collapse a Meal into Recipe.
- Preserve explicit zero, unknowns, all supported nutrient fields, supplied notes and
  declared reference bases. Percent daily values remain separate from nutrient masses;
  no mass is inferred from percentages. Bound and retain relevant provenance without
  implying uncalculated metadata participates in nutrition calculation.
- Validate format/version/limits, finite numeric and encoded units, typed references,
  duplicate identities and cycles before committing. Use explicit namespace/kind/source
  identity; identical reimport is a no-op, conflicting imports cannot silently overwrite
  local edits. Names and approximate nutrition never establish identity matches.
- Provide a usable local file selection and preview/result flow with useful sanitized
  failures. Apply the import additively in one transaction, including identity mappings;
  invalid input, intermediate failure and cancellation leave no partial catalog/Meal
  writes and preserve existing diary entries, snapshots, settings and credentials.
- Inspect legacy flattened-import identity evidence. Reuse an exact documented identity
  only when demonstrable; otherwise preserve existing rows and explain the unresolved
  overlap rather than deleting or fuzzily matching them. Existing CSV and database
  backup behavior keep their established contracts.
- Any required storage change uses a tested non-destructive migration from accepted
  schema 41 and older registered paths. Retain old schema exports, IDs, facts, nulls,
  Recipe calculation 0/1 history and quantity metadata; new imported snapshots remain
  isolated from later definition edits/deletion. Verify create/edit/search/log/load paths
  for imported records and preserve existing normal definitions and Meal workflows.
- Produce the owner's faithful package through a private offline converter and validate
  it against the actual importer in an isolated/private environment. Preserve immutable
  originals. Keep new personal artifacts outside the checkout, including ignored paths;
  preserve any pre-existing real source privately before working-tree hygiene. Historical
  Git exposure is a separate disclosed limitation, not permission to rewrite history.
  No owner-phone import/reset, account access, new scraping or publication is authorized.
- Verify meaningful synthetic model, conversion, atomicity, idempotence, migration and
  actual-screen import/use regressions. After acceptance apply version policy, rebuild
  APKs and deliver the updated Dev build to the earlier private delivery folder.

**Dependencies and ownership:** Builds on accepted Stories 24–27 and the existing
Milestone 1 recovery/export work. No relay, server, FoodSwitch or account/network
prerequisite. The milestone owns status; the implementation plan selects the minimum
model/storage/UI changes and natural verification seams. No diary import or grocery-list
feature is inferred from source collections; retain unavailable/unselected material in
private archives. The requested personal-account sources are packaged; additional recovered collections
and groceries remain privately archived outside this Story.

**Acceptance evidence:** 202 unit tests/38 suites;26 distinct staged Android methods
(22 new plus4 existing regressions); real39/40/41→42 upgrades preserve the original
canonical projection; actual file-picker/import and Product/Recipe/Meal use/restart
pass. The private package passes the production parser/import/readback/reimport.
Final133 installs in place and retains synthetic history; Dev and unsigned release
APKs plus the private import package are delivered with verified hashes. See the
[plan](../implementation-plans/milestone-2/story-28-structured-personal-import/plan.md)
and [final receipt](../_boss-runs/milestone-2-personal-data-migration/story-28-receipt.json)
for bound evidence and limits. No public release or commit.

---

<a id="story-29"></a>

### Story 29: Developer UI name bar for agent collaboration

**Type:** Feature

**Status:** Complete (2026-10-26; delegated implementation, independent review, debug and
minified runtime acceptance, and blind source lookup passed).

**Implementation plan:** [Developer UI name bar](../implementation-plans/milestone-2/story-29-developer-ui-name-bar/plan.md)
— planning, implementation decisions, exact acceptance evidence, and runtime limits.

**Delivery:** Settings now has an off-by-default **Developer tools: UI name bar** toggle.
The reserved top bar shows a deep-pressed element's stable name and copies either that name
or detailed structural XML with fully qualified composable/owner symbols, source paths,
shared-component callers, and snapshot instance context. A three-second hold briefly outlines
the selected target. Enabled mode owns holds; disabling restores ordinary long presses.
Preferences persist, while captures remain session-local and contain no diary or entered values.
App-wide naming includes additional Activity roots, shared components, scanner content, and
app-owned modal surfaces. A registered harness detects missing surveyed names and duplicate IDs.

**Acceptance evidence:** Full debug unit tests passed (208 tests, 39 suites). The final debug
inspection/security matrix passed across the recorded 22-of-23 run and corrected four-check
follow-up; unchanged Product/Recipe/Meals and AI-key privacy regressions also passed. Actual
unsigned Release assembly passed with R8, and its DEX retained the source metadata. A separate
Android/Java platform driver passed against the fully optimized, debug-signed miniDevRelease
app on the isolated API 36 emulator: actual Settings enablement, three-second shared-icon
selection, short/full system clipboard output, normal navigation, a dropdown selection retained
after dismissal, hierarchy reset, and disablement. This is representative minified runtime
evidence, not a passing 20-test minified JUnit suite; that runner failed before test discovery
because shared dependencies had been optimized away. No feature keep rules were added to
obtain runtime acceptance. A fresh-context agent located both minified copies' exact source
declarations and recovered the shared caller without screenshots or an interpreter skill.
The naming harness and its omission self-tests passed: 348 files, 2,793 surveyed call sites,
zero missing names or duplicate identifiers. The plan records artifacts, hashes, individual
failures/fixes, and the independent reviews.

**Coverage limits:** Source coverage is lexical and runtime coverage is representative.
Framework-generated decoration and decorative Canvas contents identify their named owning
component; system-owned keyboard, picker, and browser windows are outside the app inventory.
All four Activity roots are named; share/crash roots have rendered-bar/secure-window evidence
rather than complete workflow/deep-selection runs. Hierarchy describes declared source
structure, not the complete Compose implementation tree or a literal call stack; occurrence
is a runtime snapshot index. Android is the acceptance target; no iOS runtime claim is made.

**Problem / outcome:** The owner needs to point an agent at an exact FoodUs UI element
while using the app, including released versions. Screenshots and visual descriptions
can be ambiguous. Provide an opt-in developer UI name bar whose copied output lets an
agent find the selected element's implementation in this repository and distinguish it
from similar or repeated elements. Prioritize fidelity to source code and agent understanding;
technical names, fully qualified symbols, and verbose nested output are desirable.

**Acceptance and bounds:**

- Add a developer setting, off by default, that enables the UI name bar in shipped release
  builds as well as development builds. The bar appears at the top of the app, with most
  of its width devoted to a selectable text field and a Copy button on the right. Reserve
  layout space so the bar does not obscure the app's controls.
- A deliberate deep press of about three seconds on a visible UI element selects it and
  populates the bar with its identifier. Make the selected target unambiguous, for example
  with a brief outline. Cover text, icons, controls, containers, repeated list items, and
  elements in dialogs, popups, and bottom sheets, as well as ordinary screens.
- Preserve ordinary taps, scrolling, and text entry. While the UI name bar is enabled,
  holds are reserved for inspection and ordinary long-press actions are suppressed;
  disabling the feature restores normal long presses. This is the owner's explicit
  2026-10-26 decision after code inspection showed card long presses navigating away
  before three seconds. Verify suppression/restoration on cards, text fields, and modal
  controls; no separate Pick mode or delayed ordinary long-press action is selected.
- Copy defaults to the selected identifier. An Include hierarchy checkbox or equivalent
  adds detailed ancestry and source-oriented element information to the clipboard output.
  Include stable names, fully qualified component/composable symbols, application owning
  functions/types, repository-relative source paths, and structural instance context that
  distinguishes repeated elements. Make framework component and application owner distinct.
- In Compose, record the useful source-level composable/function identity rather than
  assuming every element has a corresponding Android View subclass. Inspect what metadata
  is available automatically in release builds and explicitly supply the missing locator
  information. Do not promise source line numbers or depend on debug-only inspection data.
- Perform an app-wide naming/instrumentation pass so every visible element the owner can
  select is identifiable, including the meaningful containers needed for ancestry. Establish
  consistent, searchable identifiers and shared-component/call-site naming rules; distinguish
  repeated instances without deriving names from private content. Make missing coverage
  detectable and retain useful identifiers across recomposition and language changes.
- A structured, verbose format such as XML is acceptable. Preserve namespaces, source
  identifiers, and hierarchy detail rather than simplifying the dump for nontechnical readers.
  Escape/serialize it correctly. Exact schema and implementation API are planning decisions.
  Structural export excludes credentials, private relay addresses, entered field values,
  and diary contents; no telemetry or automatic transmission is introduced.
- The copied output must be useful when pasted into an ordinary agent conversation with
  repository access. Add a supplemental skill or other tooling only if investigation or
  actual use demonstrates it is needed; a new skill is not an assumed prerequisite.

**Verification required:** Test target resolution, repeated-instance context, hierarchy
serialization, clipboard contents, and exclusion of sensitive values with synthetic data.
Use device/emulator checks for the actual bar, selection feedback, deep-press timing and
cancellation, gesture coexistence, scrolling, text fields, and modal surfaces. Verify the
feature and useful source metadata in a release build, including minification. Demonstrate
that an agent given only the copied output and repository can locate representative selected
elements and distinguish repeated instances. Check app-wide naming coverage and normal
behavior with the feature disabled; document any unsupported surface or failed acceptance.

**Dependencies and ownership:** Independent of FoodSwitch and relay delivery. The milestone
owns Story status. Create an implementation plan before substantial implementation to settle
gesture handling, metadata collection, naming coverage, and verification. This Story records
the whole requested capability; the discussion's possible work slices are not separate Stories.

---

<a id="story-30"></a>

### Story 30: Shared OCR routing, nutrition-basis and trace contracts

**Type:** Feature

**Status:** Not Started

**Outcome:** Establish the common contracts used by both image routes, the OCR adapter, review
and evaluation, without introducing an OCR-specific nutrient model.

**Acceptance:**

- Map the intake's conceptual `NutritionResult` onto actual `AiFoodEstimate`,
  `AiScanResult.Success(estimates)` and shared `NutritionFacts`; preserve multi-food results,
  nullable nutrients and explicit zero values. Define the discriminated label/food response
  and reject unknown modes, contradictory payloads and malformed required fields.
- Resolve the source-model seam before dependent code: `AiFoodEstimate` contains absolute
  pictured-portion totals, while labels provide per-serving, per-100 g, per-100 mL or another
  explicit reference. Reuse Story 28's `ReportedNutrition`, `PortionDescription`, `MetricUnit`
  and reference-serving contracts. Specify additive basis/source context and review mapping
  where necessary; never silently reinterpret per-100 facts as totals or create a parallel
  nutrient vocabulary. Missing basis/quantity/name has an explicit review/presentation rule.
- Define provider-neutral recognised text, processing metadata and safe error categories for
  transport, provider/API, parse, unusable text, AI parsing and invalid nutrition output.
  Define meaningful usable-text criteria beyond merely nonblank OCR.
- Define per-submission configuration consistency, cancellation and content-free call traces:
  step/provider/model, start/duration/status, available usage, separate AI/OCR costs with currency
  and price/plan provenance, provider processing time and image bytes. Unknown metrics stay
  unavailable; OCR has no AI token count.
- Keep the existing AI-only contract available when OCR is disabled/unconfigured. Specify
  prompt/schema versioning and shared execution parity for later harness lanes.

**Verification:** Deterministic contract/mapping tests for null versus zero, missing names,
ambiguous/mixed columns, g versus mL, serving-only references, invalid routes and incompatible
quantities; confirm no invented density/serving and no historical snapshot change. Record
the selected mapping and any additive metadata/storage implications in the implementation plan.

**Dependencies / seams:** Delivered Stories 6, 21, 22, 28 and Milestone B's multi-food/scan
preservation work; `ai/domain`, shared food/measurement models and scan review boundary.
This is the first implementation prerequisite, not an additional research milestone.

---

<a id="story-31"></a>

### Story 31: OCR.space provider adapter

**Type:** Feature

**Status:** Not Started

**Outcome:** Convert a submitted label image into bounded provider-neutral text and metadata,
with reliable error handling and no OCR.space DTO leakage into the feature.

**Acceptance:**

- Implement the Story 30 boundary using HTTPS POST multipart image upload and an `apikey`
  header to the configured endpoint; default `https://api.ocr.space/parse/image`. Recheck
  current official API/privacy documentation before implementation. Keep language/engine
  choices as justified internal defaults, overlays/searchable PDF off, and provider limits
  and compression inside the adapter.
- Reuse existing image preparation where readable; evaluate label detail before changing
  generic preparation. Validate/bound image input, response and text. Do not fetch arbitrary
  image URLs or expose credentials through redirects, raw exceptions or response bodies.
- Check provider-level and per-file outcomes before extracting `ParsedResults[].ParsedText`.
  Handle malformed responses, partial failure, API errors, timeouts and empty/brand-only/
  otherwise unusable text distinctly. Only usable text reaches a parser.
- Preserve provider processing time, supplied image bytes and measured call timing/status;
  unsupported metrics remain unavailable. Cancellation propagates; no hidden paid retries.

**Verification:** HTTP fake/fixture tests for multipart/auth, valid and partial responses,
error combinations, size limits, useless text, cancellation, malicious echoed keys and
sanitised errors. Controlled live provider acceptance requires a separately supplied key and
non-sensitive fixture; no credential is committed. Do not mark live behavior passed from mocks.

**Dependencies / seams:** Story 30; existing Ktor/DI patterns and image preparation. Adapter
tests can proceed independently of the vision/parser implementations after the common contract.

---

<a id="story-32"></a>

### Story 32: OCR settings and safe configuration testing

**Type:** Feature

**Status:** Not Started

**Outcome:** Let the user explicitly configure and test OCR in its own Settings sub-page,
without introducing another AI endpoint/model configuration.

**Acceptance:**

- Add Settings → OCR with default-off enablement, provider (initially OCR.space), editable
  default endpoint, API key and explicit Test action. Persist through existing preferences
  conventions; blank/clear, restart and reactive readiness behavior are defined and tested.
  Do not present unsupported providers or internal tuning controls as available features.
- Follow the existing AI-key DataStore/protection pattern without claiming unsupported
  encryption. Mask by default, reveal only by explicit eye action, and reset reveal state
  consistently with AI settings. Keep values out of logs, UI-name dumps and error text.
- Validate provider/HTTPS endpoint/key readiness locally. Where practical, Test performs a
  disclosed tiny synthetic provider request via Story 31, never uploads the current photo,
  and distinguishes what was validated from OCR accuracy. No requests on page opening.
- Explain the second image recipient, subsequent recognised-text AI request and provider
  privacy terms. Show disabled/unconfigured status while keeping AI-only scanning usable.
  Keys are user-entered on-device; no OCR build-time/public credential fallback.

**Verification:** Preferences defaults/persistence/clear and validator tests; device/emulator
navigation, test/success/error, masking/reveal/lifecycle and no-key-leak checks in both UI-name
bar modes. Name new controls and run the installed UI-name coverage gate. Use synthetic keys
and provider responses for automated checks; report any unrun live reachability check.

**Dependencies / seams:** Stories 30–31; AI settings reveal/validation patterns, Settings
navigation, DataStore and source naming. The configuration-test action must not depend on
the later end-to-end scan orchestrator, avoiding a cycle.

---

<a id="story-33"></a>

### Story 33: Single-call vision classification and food estimation

**Type:** Feature

**Status:** Not Started

**Outcome:** One configured vision request classifies an image and, for ordinary food,
returns the complete existing multi-food estimate in that same response.

**Acceptance:**

- Implement Story 30's route response and prompt: a real readable nutrition panel routes
  to `nutrition_label` without nutrition; otherwise return `food_estimate` with the existing
  collection. Branding, incidental text and logos alone must not trigger OCR.
- Preserve current image preparation and configured AI endpoint/model; no dedicated router
  request/model setting and no second analysis call for normal food. Keep the legacy AI-only
  mode available for disabled/unconfigured OCR.
- Preserve machine/user-context/per-scan-hint separation, partial valid food handling and
  safe failure/cancellation behavior. Capture available per-call usage/timing/cost metadata;
  the current chat response DTO has only choices, so usage support needs explicit mapping.
- Initial AI failure/invalid route cannot invoke OCR. Do not activate a production label
  route that discards the image or stops at a null result before Story 35 is ready.

**Verification:** Request-body and response fixtures for food, label, mixed image, incidental
text, invalid/missing/contradictory modes, malformed nutrition and multiple foods. Assert one
image request on success and zero OCR calls for food/failure. Grade real routing through the
later controlled corpus; mocks alone cannot establish classification accuracy.

**Dependencies / seams:** Story 30; `AiScanPrompt`, OpenRouter-compatible scanner/transport,
chat response DTO and existing multi-food parser. Independent of Story 31 implementation.

---

<a id="story-34"></a>

### Story 34: Strict OCR-text nutrition parser

**Type:** Feature

**Status:** Not Started

**Outcome:** Map recognised label text into the same FoodUs nutrient/reference and review
contract, preserving printed facts without estimation.

**Acceptance:**

- Send text only to the existing configured AI endpoint/model, with the authoritative
  schema and strict parsing rules. No image, separate parser-model settings or second
  nutrient schema. Respect one submission's configuration snapshot.
- Enforce source-only mapping: missing/unreadable values remain unknown, printed zero stays
  zero, serving sizes are not invented, units/references and column associations survive.
  Reuse deterministic conversions for explicit compatible units rather than model arithmetic.
- Treat OCR as untrusted data, not instructions; personal prompt/hint context cannot supply
  missing label facts or override the no-estimation/basis contract. Validate structured output
  and the shared basis mapping before review/ingestion; ambiguous basis is visibly unresolved.
- Distinguish parser/network/schema failure from successful OCR extraction. Preserve safe
  timing/usage/cost data; never persist/log production OCR text or provider bodies.

**Verification:** Text fixtures for missing values/serving, exact zero, kJ/kcal and mg/g,
per-serving versus per-100 g/mL columns, corrupted text, ambiguous reference, unsupported
values and prompt injection. Assert text-only requests and correct model/config, and exercise
failure/malformed output. Evaluate factual correctness against manually checked label truth.

**Dependencies / seams:** Story 30; shared nutrition/reference validation and AI transport.
Can be implemented with recognised-text fixtures independently of the OCR adapter.

---

<a id="story-35"></a>

### Story 35: OCR-assisted capture, review and recovery

**Type:** Feature

**Status:** Not Started

**Outcome:** Wire both image paths into existing capture/gallery, multi-food review, editing
and diary ingestion without changing historical nutrition semantics.

**Acceptance:**

- Enabled/configured food path executes one AI classify/estimate call and no OCR; label
  path executes AI classify → OCR → text-only AI parse with the same AI endpoint/model.
  Disabled/unconfigured OCR uses the existing AI-only path upfront and displays availability.
- Preserve printed source/basis context through review. Never pass per-reference numbers as
  absolute totals without the explicit shared mapping/quantity resolution. Keep missing facts
  unknown and require review where basis cannot be resolved; no fabricated density or serving.
- Initial AI failure calls no OCR; OCR failure/useless text calls no parser; parser/invalid
  structured output calls no ingestion. Provide stage-specific recoverable errors, retry and
  retake with retained image/draft. No silent AI-only fallback after a label-stage failure.
- Retain multi-food selection/partial rejection, Quick Add prefill/editing, Save/Cancel,
  successful navigation and duplicate-save protection from delivered scan work. Cancellation,
  stale responses or changed settings cannot produce mixed-config or duplicate results.
- Join content-free stage traces without telemetry. New UI explains estimated versus
  label-derived results and the external boundary, follows source naming, and masks secrets.
  Barcode lookup and AI text/search behavior remain unchanged.

**Verification:** Deterministic call-order/count, stage-error, cancellation and stale-result
tests plus device/emulator capture/gallery → food/label review → edit/save/cancel/retry flows.
Assert actual saved values, unknowns, basis and historical snapshot preservation; repeat
multi-food queue/navigation and AI-key/secure-screen regressions. Run UI-name coverage and
representative minified runtime. Record any live-provider or device gap explicitly.

**Dependencies / seams:** Stories 30–34; existing `AiScanViewModel`, scan/review session,
camera/gallery preparation, Quick Add and diary snapshot boundaries. This is the production
activation gate; component stories must not independently ship an incomplete label path.

---

<a id="story-36"></a>

### Story 36: Controlled AI/OCR corpus and evaluation runner

**Type:** Tooling

**Status:** Not Started

**Outcome:** Run reproducible vision, OCR and combined evaluations against controlled images,
with shared production behavior, explicit credentials and immutable raw evidence.

**Acceptance:**

- Create approximately five label and five ordinary-food cases at
  `harness/eval-data/<case>/{image.jpg,expected.json}`. Collect lawfully reusable non-sensitive
  images with provenance/hashes and manually checked facts/bases. Food photos have route/
  semantic truth or justified broad tolerances, not invented exact nutrient truth.
- Implement one `harness/nutrition-eval/` module with config/README/PowerShell entry point.
  Config-file mode is noninteractive; no-config mode opens a small WinForms launcher for
  lane, endpoint/model, provider/endpoint, cases and repeat count. Both use one execution core.
- Support A vision route/direct estimate, B OCR only, C OCR + text parser and D direct-vision
  label baseline. Reuse or prove parity with production schema/prompts/adapters. Report
  which steps/timing boundaries each lane includes; D is evaluation-only.
- Resolve keys from named environment variables; validate setup before live requests.
  Save redacted effective config plus per-case `actual.json`, call-level `trace.json` and
  applicable raw `ocr.txt` at `harness/eval-results/{models,ocr,pipelines}/<safe-subject>/<unique-timestamp>/`.
  Keep raw outputs immutable, results ignored and secrets absent; no accidental overwrite or
  traversal through model/case slugs. Support repeats with explicit bounded call counts.
- Trace actual steps separately with start/duration/status, provider/model, available tokens,
  separate AI/OCR estimated costs with currency and price/plan provenance, OCR processing time
  and image sizes. Missing metrics stay null/unavailable. Record versions/hashes; no provider
  calls or truth edits during grading.
- Meet the harness contract: stable exit codes, compact machine-readable output, offline
  self-test/mock execution, config seam, module index and AGENTS standing trigger. Retain the
  owner's data/result roots as the documented layout exception; keep machinery in the module.

**Verification:** Offline parser/config, secret-redaction, path safety, repeat/run isolation and
all-lane request/trace fixtures; run config and WinForms modes against the same controlled cases.
Confirm fixture hashes/expected truth unchanged and that missing credentials fail before calls.
Controlled live runs need external credentials and images; their availability is not assumed.

**Dependencies / seams:** Story 30 contracts and Stories 31, 33, 34 for complete real lanes.
Corpus/manual truth and offline runner setup can be prepared earlier without claiming live
acceptance. No corpus or evaluation module is installed by this planning-only change.

---

<a id="story-37"></a>

### Story 37: Nutrition grading, stability and offline comparisons

**Type:** Tooling

**Status:** Not Started

**Outcome:** Compare raw model/OCR/pipeline results by accuracy, critical basis mistakes,
latency and cost without paying to rerun APIs when grading changes.

**Acceptance:**

- Separate grade from run; write per-case `grade.json` and machine-readable `summary.json`
  from saved evidence and immutable expected truth. Neither grade nor compare calls providers.
- Score routes exactly; label fields distinguish correct, missing, wrong value, wrong unit
  and wrong basis. Wrong-basis errors carry substantially greater penalties than optional
  omissions and remain visible as critical errors independent of the aggregate score.
- Grade OCR by printed numeric/unit/reference facts with harmless whitespace/punctuation/
  ordering normalisation, retaining raw text for diagnosis. Grade food images by their stated
  schema/semantic/tolerance expectations, not false exact nutrition. Preserve per-field reasons.
- Produce 0–100 case scores, configurable threshold, mean/minimum and pass rate; group failures
  by stage. Include case/pass counts, total/mean latency, available AI token totals, separate
  AI/OCR cost components, combined cost completeness and currency/price/plan provenance,
  OCR counts/latency and critical errors. A documented free component may be zero; unknown
  cost stays unavailable and cannot disappear into a purported complete pipeline total.
  Conventional OCR has no token metric.
- Use repeat N evidence to report text identity, fact recovery each time, score variance and
  latency variance. Compare saved models, providers and vision-only versus OCR+parser results
  offline, with per-case detail and explicit incompatible inputs/prompts/config/price sources.
  Do not hide failed routes/calls, unknown measurements or critical errors behind a mean.
- Run the controlled corpus and record the comparison/selection rationale and regression
  baseline, with thresholds fixed before evaluating candidates. Accuracy advantage is a
  measured result, not assumed OCR superiority. Keep production success and evaluation-only
  baseline behavior separate, and record any unrun live acceptance as outstanding.

**Verification:** Golden offline grades/summaries for wrong basis versus missing optional
facts, unit conversions, route failures, noisy OCR, food tolerances, repeats and unavailable
usage. Regrade/compare with network disabled and prove raw outputs/truth unchanged. Complete
representative live comparisons only with supplied fixture/credential prerequisites.

**Dependencies / seams:** Story 36 raw artifacts plus Story 35 accepted production orchestration
for the final pipeline comparison. Core offline scorer work can use raw fixtures earlier;
final live acceptance cannot be inferred from mock grades. Extends the same evaluation module.

---

<a id="story-38"></a>

### Story 38: Recipes, Meals & Products library in Settings

**Type:** Feature

**Status:** Not Started

**Outcome:** Replace the basic Meals-only management page with a polished, searchable
personal library for Recipes, Meals and Products. Move its entry from the Home top bar
to Settings, immediately below Daily goals.

**Why / value:** Reusable food definitions belong together in one management destination.
Compact rows with nutrition summaries make saved items easier to recognise and maintain.

**Acceptance:**

- Remove the Home top-bar Meals button. Add a Settings list item labelled
  **Recipes, Meals & Products** immediately below **Daily goals**, matching adjacent
  Settings rows. It opens the library even when the diary or library is empty; Back
  returns to Settings. Preserve diary-specific Save Meal and Load Meal actions.
- Give the library a title and Back action, a search field below the title, and tabs
  ordered **Recipes**, **Meals**, **Products**, with a clear selected-tab indicator.
  Use Products for this library's atomic-item tab, as requested by the owner; this does
  not rename the domain or require a global replacement of friendly Food wording.
- Search filters the active tab's saved items by name without requiring a network
  request. Retain the selected tab, query and list position when returning from an
  editor. Products covers the personal/custom collection, including imported personal
  items; provider catalogue discovery remains in the existing food-search flow.
- Present items as compact, consistently spaced rows: a leading thumbnail or FoodUs
  fallback icon, item name, carbohydrate/fat/protein summary, and trailing energy.
  Show energy units and an explicit nutrition basis: the saved bundle for Meals,
  one serving for Recipes, and the defined reference quantity for Products. Preserve
  unknown values and missing serving weights; never display unknown as zero or invent
  conversions. Keep captured/reported Meal nutrition distinct from calculated totals.
- Anchor a prominent full-width creation action at the bottom, labelled for the active
  tab: **Create Recipe**, **Create Meal**, or **Create Product**. It remains reachable
  while scrolling and above navigation/keyboard insets without obscuring the last row.
  Creation opens the corresponding editor without requiring a diary date or DiarySlot;
  saving returns to the library and cancelling creates nothing.
- Selecting a row opens its appropriate editor/detail flow. Preserve explicit Save/Cancel,
  rename, constituent/portion editing and confirmed deletion where supported. Retain
  Meal loading with an explicit destination day and DiarySlot; entering from Settings
  must not silently choose a diary destination or log food when viewing/saving a definition.
- Provide loading, empty-collection, no-search-results and recoverable error states.
  Use FoodUs typography, theme colours and accessible controls across light/dark themes
  and larger text. New or changed UI surfaces retain developer inspection names.
- Reuse existing Product, Recipe and Meal repositories and editors. Existing IDs,
  imported records, captured amounts, nutrition provenance, and saved Meals survive.
  Editing library definitions continues to leave historical DiaryEntry snapshots intact.

**Verification:** Add meaningful regression coverage for collection separation, search,
nutrition bases/unknowns, create/save/cancel and Settings-origin navigation without a
diary destination. Exercise Settings → library → each tab → create/edit → return on a
device/emulator, including search, empty/no-results states, scrolling, Back, Meal loading,
and diary-history preservation. Check light/dark themes and enlarged text. Run the
UI-name coverage gate and inspect the changed surfaces at runtime, alongside the
relevant unit tests and Android debug build. Record any unavailable device checks in
the implementation plan rather than claiming visual acceptance from source alone.

**Dependencies / seams:** Builds on completed Stories 24–28 and retains Story 29's
inspection contract. Likely seams are `HomeScreen.kt`, `SettingsScreen.kt`,
`FoodYouAppNavHost.kt`, `food/meals/MealsScreen.kt`, the Product/Recipe editor entry
points, library state/projections, and localized resources. No OCR, FoodSwitch licence,
or relay prerequisite. The implementation plan is linked below; this Story does not
reopen Story 24, and planning does not begin application implementation.

**Planning:** [Implementation plan](../implementation-plans/milestone-2/story-38-recipes-meals-products-library/plan.md)
prepared 2026-10-27 after the owner-authorized private reference review and repository
inspection. It covers destination-free creation, personal collection separation,
truthful nutrition projections, retained navigation state and synthetic runtime acceptance.
Story status remains Not Started; application implementation is not begun by this plan.

**Source:** Owner's direct 2026-10-26 request and supplied UI selections. Requirements
are expressed in FoodUs terms; third-party screenshots, studies and extracted material
must remain outside the repository.

---

<a id="story-39"></a>

### Story 39: Diary-slot bulk delete, move and copy actions

**Type:** Feature

**Status:** Complete (2026-10-26; 213 unit tests, 26 executed emulator checks, schema 43 migration, both layouts, snapshot/rollback and process-restart acceptance passed).

**Implementation plan:** [Diary-slot bulk actions](../implementation-plans/milestone-2/story-39-diary-slot-bulk-actions/plan.md) — Complete; records atomic snapshot handling, persisted append order, stale-source validation, and regression/runtime acceptance.

**Outcome:** From Diary → a meal slot → ellipsis menu, manage all entries in that
slot on the displayed diary day with **Delete All**, **Move to...**, **Copy to...**
and **Copy From...**. A source or destination is a day plus a DiarySlot, so either
coordinate or both can change independently.

**Acceptance:**

- Add the four actions to the existing slot context menu, retaining Save Meal and
  Load Meal. Scope every action to the menu's DiarySlot and displayed diary date,
  including when viewing a historical or future day, rather than assuming today.
- **Delete All:** Confirm with the slot name, date and affected entry count, then
  delete every DiaryEntry in that slot on that day. Preserve the configured DiarySlot,
  other slots/days, and saved Product, Recipe and Meal definitions. Cancel changes nothing.
- **Move to...:** Let the user choose a destination day and slot. Transfer all source
  entries there, appending after existing destination entries in source order. After
  success the source is empty; a failed transfer leaves both collections unchanged.
- **Copy to...:** Let the user choose a destination day and slot. Append independent
  copies of all current source entries there, preserving their order and the source.
- **Copy From...:** Let the user choose a source day and slot, then append independent
  copies of all entries there into the slot whose menu was opened. Preserve the selected
  source and every entry already in the current destination. This action is available
  when the current slot is empty.
- The selector exposes both date and slot, initially using the menu's day/slot, and
  allows same-day/different-slot, different-day/same-slot, or different-day/different-slot
  selection. Show the source, destination and entry count before submission. Disable
  submission when source and destination are identical or no source entries exist;
  Delete All, Move to and Copy to are disabled for an empty current slot. Dismissing
  a selector or cancelling an action changes nothing.
- Include all supported DiaryEntry kinds: Product and Recipe snapshots, Quick Add,
  and unresolved placeholders. Preserve recorded nutrition, quantities, units, notes,
  unknown values, captured context and unresolved state. Copy assigns new entry identities
  so subsequent edits/deletions are independent; Move retains the entry's recorded facts
  while changing its diary placement. Never recalculate historical nutrition from current
  library definitions or silently rescale amounts. Recompute affected diary summaries.
- Each bulk operation commits atomically, with protection against duplicate submission.
  On failure, show a recoverable error and leave source/destination data intact. Revalidate
  selected slots and source entries at submission; stale selections must not act on an
  unintended collection. Preserve unrelated diary data and dependent snapshot integrity.
- Give the menu, selectors, confirmation and errors accessible labels and developer
  inspection names consistent with the existing UI contract.

**Verification:** Add regression tests for all entry kinds, the three combinations of
day/slot changes, append order and existing destination contents, independent copied
identities, recorded nutrition/unknowns, placeholder state, empty sources, same-source
selection, cancellation, duplicate submission, stale data and transaction rollback.
Verify Delete All affects only the selected day/slot, and library definitions and unrelated
history remain unchanged. On a device/emulator exercise each menu action from populated
and empty slots and a non-today diary date; check totals and data after restart. Run
relevant unit/database tests, the Android debug build, UI-name coverage and runtime
inspection of new surfaces. Record any unavailable runtime evidence in the implementation plan.

**Dependencies / seams:** Uses the delivered DiarySlot/DiaryEntry separation and snapshot
contracts from Stories 25–28, plus Story 29 inspection naming. Likely seams include
`ui/home/meals/card/DiarySlotCard.kt` (`MealMenu`), Home state/callbacks, diary domain
use cases, repository transactions/DAOs, date/slot selectors and localized resources.
Independent of Story 38's library redesign, OCR and relay delivery. Create an implementation
plan when taken up, including transaction and recovery checks for these data mutations.

**Source:** Owner's direct 2026-10-26 request and supplied `MealMenu.DropdownMenu.1`
selection. This schedules work; no existing Story is reopened or implementation begun.

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
12. The owner-selected Product/Recipe/Meal batch executes **25 → 26 → 27 → 24**. Story 25
    separates DiarySlot and reusable Meal identities; Story 26 settles the common interface
    and Product portion contract; Story 27 reconciles Recipe composition/yield; Story 24
    builds the Meals library/editor on those foundations. Preserve Story IDs rather than
    renumbering the feature to match this execution order. Planning can inspect later work,
    but dependent implementation waits for its prerequisites to pass acceptance.
13. Stories 24–27 have no FoodSwitch licence or multiplayer/server prerequisite. No current
    dependency cycle is selected. Common-interface/projection names and legacy Meal portion
    handling are explicit planning decisions, not external blockers. Do not infer time or
    capacity estimates from the four-Story count; implementation plans must size actual work.
14. Story 29 is independent of the existing feature batches. Its gesture/metadata investigation
    precedes the app-wide naming pass and final release-build acceptance within its own plan.
15. Story 38 follows completed Stories 24–28 and preserves Story 29's naming contract.
    It can proceed independently of the OCR batch and changes the library's navigation
    and presentation without reopening the delivered domain Stories.
16. Story 39 uses the delivered diary and snapshot contracts from Stories 25–28 and
    Story 29's naming contract. It does not depend on Story 38; coordinate edits to
    shared Home/menu code if those Stories are later executed together.

### OCR delivery order

The [owner's addendum](../dictations-tier-0/2026-10-25_milestone-2_ocr-assisted-nutrition-capture.md)
is incorporated in [Design's OCR pipeline](../design.md#ocr-assisted-nutrition-capture) and
[evaluation contract](../design.md#aiocr-evaluation-harness). Its provisional OCR-1 through
OCR-8 map to Stories **30, 32, 31, 33, 34, 35, 36, 37** respectively: the provider precedes
settings validation, while stable new Story IDs follow this milestone's existing sequence.

| Story | Prerequisites for completion | Readiness / execution boundary |
| --- | --- | --- |
| 30 | Existing AI/multi-food behavior and Story 28 | First: settle absolute totals versus label reference bases, common schema, errors and traces |
| 31 | 30 | OCR adapter and mock fixtures; external key/controlled image for live acceptance |
| 32 | 30, 31 | Preferences/UI may be developed after 30; complete Test action uses adapter, not orchestrator |
| 33 | 30 | Vision classification/direct estimation; production label route stays inactive until 35 |
| 34 | 30 | Text parser can use fixture text independently of OCR implementation |
| 35 | 30–34 | Activate complete runtime and verify review/ingestion/recovery |
| 36 | 30, 31, 33, 34 | Corpus/offline work may start earlier; complete real-lane execution needs all adapters |
| 37 | 35, 36 | Offline grading can use fixtures; final comparison uses accepted pipeline and real evidence |

Critical runtime path: **30 → 31 → 32 → 35**, with **33 and 34** also required before 35.
Evaluation completion joins **35 + 36 → 37**. There is no dependency on FoodSwitch licensing
or relay deployment, and no selected cycle. Shared AI/scan code means interface readiness does
not automatically make concurrent editing safe; any later delegation needs explicit file ownership.
Create one implementation plan when each substantial Story is taken up, not placeholder plans now.

The highest uncertainty is safe reference-basis convergence, then real-world routing/OCR quality
and live provider behavior. There is no credible elapsed estimate before Story 30's mapping and
controlled fixture/provider checks. Corpus collection, user-supplied keys, network/quota and live
evaluation are external waits; they must not be disguised as implementation completion. All eight
Stories remain Not Started. This refinement creates no implementation, commit or publication run.

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
- Story 24 was requested directly by the owner on 2026-10-25 for a central editor/manager
  and Meal/Meals UI wording. The owner's subsequent vocabulary decision replaces the
  initial MealTemplate target with Meal, distinguishes DiarySlot and DiaryEntry, and
  reserves Product and Recipe for their separate roles. It extends Story 13 without
  reopening or renumbering it; Design owns the complete vocabulary and transition.
- After the context commit, the owner requested implementation Stories for the accepted
  Product/Recipe/Meal change. Stories 25–27 were selected directly on 2026-10-25, with
  Story 24 retained for the Meals library/editor. No unrelated backlog items were pulled
  in and no Story closed. The closed-count contribution and app version are unchanged.
- Story 29 was requested directly by the owner on 2026-10-26 during a rails-pair-programming
  discussion: a release-available developer UI name bar, deep-press selection, and detailed
  source-oriented copying for agent communication. Scheduling initially changed neither
  the closed-Story count nor app version; subsequent completion is recorded above.
- Stories 30–37 were selected on 2026-10-26 from the owner's attached 2026-10-25 OCR nutrition
  addendum. Its [exact intake copy](../dictations-tier-0/2026-10-25_milestone-2_ocr-assisted-nutrition-capture.md)
  is retained; maintained Design resolves actual source contracts and the harness path exception.
  No existing Story is reopened or renumbered. No unrelated backlog item is selected, no Story
  closes, and the app version is unchanged by this planning work.
- Story 38 was requested directly by the owner on 2026-10-26: relocate Home's Meals
  entry below Daily goals in Settings and expand it into a Recipes, Meals & Products
  library. This schedules new work without reopening delivered Stories or changing
  the closed-Story count or app version.
- Story 39 was requested directly by the owner on 2026-10-26 for Delete All, Move to,
  Copy to and Copy From in the diary-slot ellipsis menu, across days and/or slots.
  Scheduling it changes neither closed-Story counts nor the app version.

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
