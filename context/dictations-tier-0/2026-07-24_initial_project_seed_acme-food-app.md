# 2026-07-24 - Initial Project Seed: ACME Food App

## Source

- Captured from: owner-authored design document (`acme-food-app-design.md`, delivered from the owner's local Downloads folder on 2026-07-24), itself synthesized from earlier dictation sessions
- Related project area: whole project — purpose, domain model, Milestones 1-3, and implementation notes
- Note: this repository already contained a partially enriched `../design.md` (fork motivation, first-session build/deploy learnings) when this seed arrived; this seed extends, and does not replace, that content

## Raw Notes

The full source document is preserved verbatim below.

---

# ACME Food App — Design Document

**Project:** ACME Food App (fork of Food You)
**Fork repository:** https://github.com/JarrydAdaens/FoodYou
**Upstream:** https://github.com/maksimowiczm/FoodYou (GPL-3.0, Android 9+, Kotlin / Jetpack Compose / Material 3, Room, Ktor)
**Author:** Jarryd Adaens
**Status of fork:** Unchanged from upstream as of this document

---

## 1. Purpose & Intent

This project is a personal fork of **Food You**, an open-source, privacy-focused Android food diary and nutrition tracker maintained by a single developer. The upstream maintainer does not accept external changes, so this fork exists to produce a **custom version of the application tailored to my needs**.

### Why this project exists

- Replace **MyFitnessPal** and **Lose It** as my nutrition trackers. Both suffer from too many taps to log food, and constant subscription/lifetime upsell interruptions.
- Own my data. Years of paid data currently lives in other providers; this project recovers it and stores it in a format I control.
- Reduce logging friction over time, adding AI-assisted logging and quality-of-life changes on top of a solid local-first base.
- The app must work for **two users**: my phone and my wife's phone.

### Fork philosophy (critical — applies to ALL work)

- All work happens on **my own branch**.
- All changes must be **complementary and additive**, and only **very rarely structural**.
- Changes must be built so they are **easy to reapply** when the upstream maintainer pushes updates. Minimise merge conflicts by design.
- An LLM (Claude Code) is doing the implementation, so we do not need to go to extreme lengths for mergeability — but mergeability must always be **kept in mind** when changes are made.

---

## 2. Domain Model (shared vocabulary)

These concepts underpin the data work in Milestone 1 and the UI work in Milestone 2:

| Concept | Definition |
|---|---|
| **Custom food** | A food I've built from scratch by specifying its fundamentals — macros, minerals, fats, proteins, etc. — and given a name. |
| **Recipe** | A collection of distinct ingredient foods that make up one final food item. When used, it stays **collapsed** into a single entry (e.g. "birthday cake"). |
| **Meal** | Also built from components (e.g. a particular tomato, an egg, three cups of flour, mayonnaise), but when inserted into the diary it **expands**: each sub-item is added as its own entry (e.g. "chicken sandwich" inserts bread + mayonnaise + 100 g chicken + 50 g cheese). |
| **Diary entry** | A reference to a food that was eaten — not the definition of the food. Links to a food and gives it a portion size. |

Recipes and meals overlap conceptually; the difference is collapse-vs-expand behaviour at insertion time.

---

## 3. Milestones Overview

Work is broken into **three distinct milestones**:

1. **Milestone 1 — Initialization:** Get Food You built, deployed, populated with my historical data, and in daily use — plus a repeatable update/deployment mechanism.
2. **Milestone 2 — Customisation:** Begin changing how the application works: identity/branding, AI integrations, logging ergonomics, and adopted upstream bug fixes.
3. **Milestone 3 — TBD:** Not yet defined. To be dictated later.

---

## 4. Milestone 1 — Initialization

**Goal:** First grab of Food You, get it working and deployed onto my phone, populate it with my data, and live with it. Everything in this milestone serves that.

### Story 1.1 — Build & deploy Food You

Compile the unmodified fork and deploy it to my phone. (Prior work: fork pulled locally; first compile targets the emulator, then the physical phone.)

### Story 1.2 — Fill in project context & goals

Write down all project context, goals, and direction — figuring out where this custom version of the app is going. (This document is the primary artefact of this story.)

### Story 1.3 — Obtain USDA FoodData Central API key

The app can pull food data from the FDA/USDA (FoodData Central) in the United States if an API key is provided. Obtain that key.

### Story 1.4 — Obtain Open Food Facts login

The app also uses the Open Food Facts open-source data collection. With a login you can **contribute data back** — I want to do that. Create an account / obtain credentials.

### Story 1.5 — Define the master data format

Define a **master JSON format, in full detail**, that will hold all of my food data. Properties:

- It is **my own format**, independent of any one app.
- It is the canonical store from which app-specific exports (e.g. Food You CSV) are generated.
- It must accommodate **custom foods**, **recipes**, **meals**, and **diary entries** (per the domain model above).
- It is **mutable**: it will be defined alongside the MyFitnessPal export (Story 1.6) and extended/changed as needed to accommodate the Lose It data (Story 1.7).

### Story 1.6 — Extract my MyFitnessPal data

I had a paid MyFitnessPal account for years; that data is mine and I want it back.

- **Approach:** deliberately fast and messy. **Do not** use the third-party exporters available online.
- Instead: go to MyFitnessPal, **copy-paste all the data into prompts**, and rely on an LLM to brute-force it into the master JSON format.
- The master format (Story 1.5) is being defined at the same time as this export happens.

### Story 1.7 — Extract my Lose It data

Once MyFitnessPal is done, log into **Lose It**, find my data there, and repeat the same brute-force approach: paste the data into the LLM and have it stored into the master format.

- The format may need to be **extended or changed** to accommodate both datasets — that's fine, it's still mutable at this stage.

### Story 1.8 — Determine Food You's CSV import schema

Food You imports data via CSV in a particular format. Find out exactly what that format is.

- **Starting point:** an export produced from Food You itself, which is already located in the repository. Use it to establish the schema.

### Story 1.9 — Build the export script (master JSON → Food You CSV)

Have the LLM produce a script that exports from the master JSON format into the Food You CSV import format. The output CSV must carry all three data groups:

1. **Custom foods** — built from macro/mineral fundamentals.
2. **Recipes** — built from other foods, stays collapsed.
3. **Meals** — built from components, expands on insertion.

After import, all of my data lives in a platform I own, in my own custom version of Food You.

### Story 1.10 — App update mechanism (moved into Milestone 1)

*(Originally conceived during Milestone 2 planning, but required from the start.)*

Establish the ability to provide app updates **without needing a public store release**:

- I can build an APK whenever I want and get it onto phones.
- Updates must **not lose local data** when a new build is installed.
- Publishing privately to the **Google Play Store is acceptable** if that's the cleanest mechanism; otherwise any workable direct-deploy mechanism is fine.
- Must support **two devices**: my phone and my wife's phone.

### Story 1.11 — Use the app for a while

Use the base app daily for a few days to a week to get to know it. This story is marked done when I judge it done. *(Side purpose from earlier planning: record friction — logging flows, navigation, search and barcode behaviour, screens worth keeping.)*

---

## 5. Milestone 2 — Customisation

**Goal:** Start actually changing how the application works — making it easier for myself and adding integrations.

### Story 2.1 — Identity shift / rename

Give the app its own distinct identity so it is unmistakably my fork:

- **Name:** `ACME Food App` (in the Looney Tunes "ACME" sense), replacing "Food You" everywhere user-visible, including the About screen.
- **Package / application ID** and descriptor changed.
- Assorted branding touches.
- **Version:** starts at **1.0**. Versioning scheme: first number = **major releases**, second = **minor releases**, third = reserved (probably a **deploy counter** — to be decided).
- The About screen must state that the app is **derived from Food You** and show **which upstream Food You version** it is derived from. My skin/fork has its own versioning that sits **on top of** the Food You version.
- Implementation constraint: do this as an easy-to-overlay change on top of the original app to minimise merge conflicts with upstream.

### Story 2.2 — Relocate the Sponsor button

The settings menu contains a "Sponsor — support the app's growth" button. I love that button, but it's in the wrong spot and in the way. **Move it to the About screen** where it belongs.

### Story 2.3 — Replace the privacy policy link

Stop linking to the Food You privacy policy. Link instead to **my own privacy policy**, which I already have online for my apps.

### Story 2.4 — About screen link swaps

Rewire the About screen's outbound links:

- **Email button:** no longer emails the original creator — emails **me** (address to be specified).
- **Lightbulb (suggestions) button:** links to **my GitHub issues page** instead of upstream's.
- **"What's new" section:** can keep pointing at the same place (believed to be in-app data). I will **add my own entries** so I can publish my own changelogs.
- **Leftmost (GitHub) button:** points to **my GitHub repo**, not his.
- **Attribution:** further down the screen, beneath where it says Food You and credits the original creator, add a **button linking to his GitHub** — so anyone can see where this came from.

### Story 2.5 — Per-meal add buttons rework

Each day shows meal sections (breakfast, lunch, dinner, snacks, plus user-defined). Each meal control currently has two buttons; it will end up with **four**:

1. **Search (magnifying glass)** — currently a plus button opening search (barcode scan or text search across Open Food Facts, Swiss food data, USDA FoodData Central, and my local data). Behaviour unchanged, but **swap the plus icon for a magnifying glass**: every one of these buttons adds something, so "plus" is meaningless — this one is specifically a *search*.
2. **Quick add (lightning bolt)** — unchanged. Add by name plus quick protein/fat/carb values. Fine as-is; it's the sort of thing you do at day's end after shopping.
3. **AI scan (robot icon)** — **new**, see Story 2.6.
4. **Fast text placeholder (pencil icon)** — **new**, see Story 2.8.

### Story 2.6 — AI scanning (robot button)

Tapping the robot button opens a dedicated **"AI scanning"** sub-screen:

**Capture flow**

- A big button on the screen launches the camera to photograph the food in front of you.
- After taking the photo you return to the AI scanner screen and see a **preview** of the photo.
- Tapping the preview prompts **"discard and retry"** — the picture is deleted and you take another.
- Below the preview: an **"Ask AI"** button.

**Ask AI behaviour**

- An **API key is baked into the app** (mine — likely an **OpenRouter** key), so my deployed builds just work with my key.
- The photo is **downscaled to a sensible minimum size** before upload — no massive high-resolution uploads.
- The image is sent to a model endpoint with a **custom embedded prompt**: we are Australians, in Victoria, Australia; this is my food; please identify it.
- **Response is JSON** containing:
  - what the model thinks the food is,
  - a **certainty value**,
  - estimated **calories, protein, fat, fibre, and sugar**.

**Result handling — two actions**

- **Tick (save):** saves what the AI thinks it saw. Same flow as quick add (name, protein, fats, carbs, etc.), except the fields are **pre-filled from the AI's JSON**.
- **Align:** instead of saving the raw guess, the app tries to **align the result with a food already saved locally**, searching my custom foods.
  - If no matching custom food exists, it **creates one** — now the dish exists on-device.
  - **The point:** for a recurring dish (e.g. lasagna every Friday night), the AI guesses first; later I hand-edit that custom food — fifteen minutes with the cupboard working out exactly what's in it — and it becomes a trusted, reliable record. From then on, photographing the same dish and hitting **Align** connects the new photo to the known-true data. Convenience *and* reliability, given a little preparation.

### Story 2.7 — Gallery source for AI scanning

On the AI scanning screen, alongside the big take-a-photo button, add a **small gallery button** nearby: pick an existing photo of your food from the gallery instead of shooting a new one (backfilling past meals, or using a better photo you already took).

### Story 2.8 — Fast text placeholder (pencil button)

Tapping the pencil opens the fastest possible logging path:

- Enter just a **name and a description**. That's it.
- Use case: out at lunch, camera is awkward (weird social situation, bad lighting, no time) — you need something *even faster* than the AI flow.
- Saves a **zero-calorie placeholder entry with no nutritional data** — purely a marker that you ate something, to be straightened out later when you have time.

### Story 2.9 — Placeholder edit flow (meta screen)

Placeholder entries are indistinguishable from normal entries in the diary — tapping one offers the usual **edit entry / delete entry** options. The difference: choosing **edit** on a placeholder opens a **new meta screen** instead of the regular editor.

- The meta screen greets you with the same add options again — **search (plus/magnifier), lightning, and AI** — but now with the **saved name/description on screen as context** (e.g. "roast dinner at my uncle's house").
- From here you resolve the placeholder into real data, by whichever route fits:
  - **Search:** manually add elements — search for roast beef, add some potatoes, etc.
  - **Lightning:** shove in an estimate — "call it 600 calories and 30 g protein".
  - **AI:** see below — behaves differently here.

**AI button in the placeholder flow (different behaviour):**

- It does **not** photograph or estimate macros.
- It silently sends context behind the scenes — a prompt the user never sees: *I am Australian, in Melbourne; this is what I had for lunch* — plus the name/description I typed.
- The model's job is to produce a **better search query** describing the meal (e.g. "lunch with my husband at a cafe" → it reasons about what this person typically eats → "a pasty and coffee").
- That query is placed into the search box, the **search is executed**, and you land on the **search results screen with results already populated**. It automates part of the workflow so you're only doing the deciding.

### Story 2.10 — Graph style setting (bar / pie)

- Add a settings option **"Graphs"**: default **Bar**, alternative **Pie**.
- When **Pie** is selected, the view at the top of the daily food-entry screen — the one showing calories eaten for the day plus the macro breakdown, currently as bar graphs — is exchanged for a **pie chart** presenting the same data.
- Scope note: **this is the only place charts are used** in the app.

### Story 2.11 — Week layout setting (fixed / scrolling)

- Add a settings option **"Week layout"** with two values: **Fixed** or **Scrolling** (current behaviour).
- **Current behaviour (Scrolling):** the day scroller at the top moves day-by-day — pull across one day into the past or future; tap to open the Android-style calendar picker to jump to a date; the selected day is visible. It works, but it makes time feel fluffy.
- **Fixed:** the week strip is pinned — **Monday is always on the far left**, always showing Mon Tue Wed Thu Fri Sat Sun. The **current day stays highlighted**. Swiping across moves to the **next/previous whole week**. Time is anchored to a particular week and you scroll week-by-week.

### Story 2.12 — Upstream issue triage & bug adoption

Go through the **open issues** on the original Food You repository and decide which to adopt:

- **Enhancements: generally not interested.** They make the app fuzzy — e.g. a shopping-list feature has been requested upstream; that belongs in a shopping-list app, not here. Don't pollute one app with another app's job.
- **Bugs: yes.** Write down genuine bugs, create stories for each adopted one, and fix them in the fork.

---

## 6. Milestone 3 — TBD

Not yet defined. Placeholder for future dictation.

---

## 7. Implementation Notes for Claude Code

- **Repo:** work in `JarrydAdaens/FoodYou`, on my own branch — never assume upstream will take patches.
- **Stack:** Kotlin, Jetpack Compose, Material 3, Room (local storage), Ktor (HTTP). Android 9+. GPL-3.0 (fork must remain compliant).
- **Merge discipline:** prefer additive overlays over structural rewrites; when a structural touch is unavoidable, keep it small and well-isolated so upstream pulls rebase cleanly. LLM assistance means perfection isn't required — awareness is.
- **Data is local-first:** diary entries and custom foods live on-device (Room); food discovery hits Open Food Facts, USDA FoodData Central, and Swiss food-composition data, then caches locally.
- **Secrets:** the OpenRouter (or similar) API key is baked into my private builds. Keep it out of anything that could flow back upstream or into a public repo.
- **Locale context for AI prompts:** Australian, Victoria/Melbourne — embed in the AI-scan and search-query prompts.
- **Two-device reality:** every deployment/update decision must hold for both my phone and my wife's phone without data loss.

---

*(End of preserved source document.)*

## Important Signals

- New decision: the fork has its own product identity — **ACME Food App** — with the in-app rename landing as Milestone 2 Story 2.1; its versioning (1.0, major.minor.reserved) sits on top of the upstream Food You version.
- New decision: three milestones — 1 Initialization (build, data recovery, update mechanism, daily use), 2 Customisation (identity, AI logging, ergonomics, adopted bug fixes), 3 TBD.
- New decision: a **master JSON format** owned by Jarryd is the canonical food-data store; app-specific formats (Food You CSV) are generated exports from it.
- New decision: fork philosophy is constitutional — additive, overlay-style changes; mergeability with upstream always kept in mind; work on own branch only.
- New decision: AI-assisted logging (photo scan via OpenRouter-style endpoint, placeholder resolution, query generation) is core roadmap, with the owner's API key baked into private builds only.
- Changed assumption: the app serves **two users** (Jarryd's and his wife's phones), not one; updates must never lose local data on either device.
- Changed assumption: dictation describes the stack as Android-only Kotlin/Jetpack Compose; the repository is actually Kotlin Multiplatform + Compose Multiplatform (Android-first). Maintained design keeps the accurate KMP description.
- New risk: baking a paid API key into an APK distributed to two devices — must never reach the public repo or upstream; storage/injection mechanism undecided.
- Open question: exact update/distribution mechanism (private Play track vs direct APK) — Story 1.10 decides.
- Open question: third version-number component (deploy counter?) — deferred to Story 2.1.
- Open question: Milestone 3 scope — awaiting future dictation.
- Possible milestone or story impact: Story 2.12 (upstream issue triage) will spawn new bug stories into the backlog.

## Integration Notes

- Update `../design.md` (and its Milestones Index): enriched with ACME Food App identity, expanded fork motivation and fork philosophy principles, domain model, master-data pipeline, planned AI integration and its privacy boundary, secrets policy, and a three-milestone index — done 2026-07-24.
- Update `../milestones/`: `milestone-1.md` (Initialization, Stories 1.1-1.11), `milestone-2.md` (Customisation, Stories 2.1-2.12), `milestone-3.md` (TBD stub) — done 2026-07-24.
- Update `../backlog/`: `backlog-1.md` converted to a real, currently empty staging pool (all known stories mapped directly into milestones by this seed; upstream bug triage will feed it) — done 2026-07-24.
- Update `../implementation-plans/`: none created — no story is at execution point requiring a plan yet.
