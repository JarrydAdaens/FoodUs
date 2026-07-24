---
name: milestone-2
description: Milestone 2 - Customisation. Change how the application works - ACME identity, AI-assisted logging, ergonomics, and adopted upstream bug fixes.
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
adding the fork's own identity and AI integrations.

## Why it matters

- Logging friction is the core complaint against MyFitnessPal and Lose It; the AI and ergonomics
  stories attack it directly.
- The ACME Food App identity makes the fork unmistakably its own while attributing upstream.
- Adopting genuine upstream bugs keeps the base healthy without scope creep.

## Outcome / Definition of Done

The app carries the ACME Food App identity (with upstream attribution and layered versioning), the
four-button per-meal logging surface (search, quick add, AI scan, fast placeholder) works end to
end including placeholder resolution, the settings ergonomics options (graphs, week layout) ship,
and adopted upstream bugs are fixed.

## Status

Not Started — 0/12 stories complete. Blocked on Milestone 1 reaching daily use.

---

## Story Index

| # | Story | Type | Complexity | Effort | Risk | Plan | Status |
| --- | --- | --- | --- | --- | --- | --- | --- |
| 1 | [Identity shift / rename to ACME Food App](#story-1) | Feature | — | — | — | — | Not Started |
| 2 | [Relocate the Sponsor button](#story-2) | Feature | — | — | — | — | Not Started |
| 3 | [Replace the privacy policy link](#story-3) | Feature | — | — | — | — | Not Started |
| 4 | [About screen link swaps](#story-4) | Feature | — | — | — | — | Not Started |
| 5 | [Per-meal add buttons rework](#story-5) | Feature | — | — | — | — | Not Started |
| 6 | [AI scanning (robot button)](#story-6) | Feature | — | — | — | — | Not Started |
| 7 | [Gallery source for AI scanning](#story-7) | Feature | — | — | — | — | Not Started |
| 8 | [Fast text placeholder (pencil button)](#story-8) | Feature | — | — | — | — | Not Started |
| 9 | [Placeholder edit flow (meta screen)](#story-9) | Feature | — | — | — | — | Not Started |
| 10 | [Graph style setting (bar / pie)](#story-10) | Feature | — | — | — | — | Not Started |
| 11 | [Week layout setting (fixed / scrolling)](#story-11) | Feature | — | — | — | — | Not Started |
| 12 | [Upstream issue triage & bug adoption](#story-12) | Research | — | — | — | — | Not Started |

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

**Status:** Not Started

---

<a id="story-2"></a>

### Story 2: Relocate the Sponsor button

**Type:** Feature

**Summary:** The settings menu contains a "Sponsor — support the app's growth" button. The owner
loves that button, but it's in the wrong spot and in the way. Move it to the About screen where it
belongs.

**Why / value:** Declutters settings while keeping upstream support visible.

**Rough scope:** Settings screen, About screen (`settings` / `sponsorship` slices).

**Status:** Not Started

---

<a id="story-3"></a>

### Story 3: Replace the privacy policy link

**Type:** Feature

**Summary:** Stop linking to the Food You privacy policy; link instead to the owner's own privacy
policy, already online for his apps.

**Why / value:** The fork's data practices (notably the AI features) are the owner's to declare,
not upstream's.

**Rough scope:** Single link swap in settings/About.

**Status:** Not Started

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

**Status:** Not Started

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

**Status:** Not Started

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

**Status:** Not Started

---

<a id="story-7"></a>

### Story 7: Gallery source for AI scanning

**Type:** Feature

**Summary:** On the AI scanning screen, alongside the big take-a-photo button, add a small gallery
button: pick an existing photo of your food instead of shooting a new one.

**Why / value:** Backfilling past meals, or using a better photo you already took.

**Rough scope:** Gallery picker on the Story 6 screen; same downstream flow.

**Status:** Not Started

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

**Status:** Not Started

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

**Status:** Not Started

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

**Status:** Not Started

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

**Status:** Not Started

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

**Status:** Not Started

---

## Interdependency Order

1. Story 5 (four-button rework) hosts Stories 6 and 8; build the button surface first or alongside.
2. Story 6 (AI scanning) precedes Story 7 (gallery source).
3. Story 8 (placeholder) precedes Story 9 (meta screen); Story 9's AI mode shares endpoint
   plumbing with Story 6.
4. Identity/About stories (1-4) are independent of the logging stories and of each other, though
   3 and 4 touch the same About screen as 1 — sequence them to avoid churn.
5. Stories 10-12 are independent.

---

## Backlog Sources

- All stories were mapped directly from the 2026-07-24 initial project seed. Story 12 feeds new
  bug stories into [../backlog/](../backlog/).

---

## Deferred / Follow-up Work

- Decide the third version-number component (deploy counter?) — during Story 1.
- Decide the AI-key injection mechanism for private builds — during Story 6 planning.

---

## Notes

- Keep this Story Index in sync with the [Milestones Index](../design.md#milestones-index) in Design.
- Fork philosophy applies to every story here: additive overlays, minimal merge-conflict surface.
