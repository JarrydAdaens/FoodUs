---
name: milestone-1
description: Milestone 1 - Initialization. Get the fork built, deployed, populated with the owner's recovered historical data, and in daily use, with a repeatable two-device update mechanism.
metadata:
  version: "3.0"
  agentic_rails_source_version: "3.0"
  owner: "Jarryd Adaens"
  repo: "FoodYou (fork of maksimowiczm/FoodYou)"
---
# Milestone 1: Initialization

> Milestone. A coherent macro-feature or delivery outcome. (Tier numbers live only in `design.md` / `agenticworkflow.md`.)
>
> Related: [design.md (Milestones Index)](../design.md#milestones-index), [../backlog/](../backlog/)
>
> Source dictation: [2026-07-24 initial project seed](../dictations-tier-0/2026-07-24_initial_project_seed_acme-food-app.md)

---

## Intent

First grab of Food You: get it working and deployed onto the owner's phone, populate it with his
recovered historical data, establish a repeatable two-device update mechanism, and live with the
app daily. Everything in this milestone serves that.

## Why it matters

- The fork is only worth customizing once it is the household's real daily driver with real data in it.
- Years of paid MyFitnessPal / Lose It data belongs to the owner; recovering it into an owned
  master format is the point of independence from those providers.
- Without a data-safe update mechanism for both phones, no later customisation can ship.

## Outcome / Definition of Done

The unmodified fork runs on both household phones, contains the owner's historical data imported
via the master JSON → CSV pipeline, can be updated with new builds without data loss, and has been
used daily long enough for the owner to judge the baseline understood.

## Status

In Progress — 1/11 stories complete.

---

## Story Index

| # | Story | Type | Complexity | Effort | Risk | Plan | Status |
| --- | --- | --- | --- | --- | --- | --- | --- |
| 1 | [Build & deploy Food You](#story-1) | Tooling | — | — | — | — | Complete |
| 2 | [Fill in project context & goals](#story-2) | Docs | — | — | — | — | In Progress |
| 3 | [Obtain USDA FoodData Central API key](#story-3) | Tooling | — | — | — | — | Not Started |
| 4 | [Obtain Open Food Facts login](#story-4) | Tooling | — | — | — | — | Not Started |
| 5 | [Define the master data format](#story-5) | Feature | — | — | — | — | Not Started |
| 6 | [Extract MyFitnessPal data](#story-6) | Feature | — | — | — | — | Not Started |
| 7 | [Extract Lose It data](#story-7) | Feature | — | — | — | — | Not Started |
| 8 | [Determine Food You's CSV import schema](#story-8) | Research | — | — | — | — | Not Started |
| 9 | [Build the export script (master JSON → Food You CSV)](#story-9) | Feature | — | — | — | — | Not Started |
| 10 | [App update mechanism](#story-10) | Tooling | — | — | — | — | Not Started |
| 11 | [Use the app for a while](#story-11) | Research | — | — | — | — | Not Started |

---

## Stories

<a id="story-1"></a>

### Story 1: Build & deploy Food You

**Type:** Tooling

**Summary:** Compile the unmodified fork and deploy it to the owner's phone. First compile targets
the emulator, then the physical phone.

**Why / value:** Nothing else can happen without a live baseline.

**Rough scope:** Local Android toolchain (JDK 21, Android SDK), Gradle build, adb deploy.

**Status:** Complete — 2026-07-24. Built and deployed to an Android 16 emulator and a physical
Galaxy S22 Ultra. Toolchain paths recorded in agent memory.

---

<a id="story-2"></a>

### Story 2: Fill in project context & goals

**Type:** Docs

**Summary:** Write down all project context, goals, and direction — figuring out where this custom
version of the app is going. The initial project seed dictation and the maintained context tiers
synthesized from it are the artifacts of this story.

**Why / value:** Durable direction lets any future agent or session build from the repository
instead of from memory.

**Rough scope:** `context/` tiers — dictation seed, design, milestones, backlog.

**Status:** In Progress — context tiers initialized from the 2026-07-24 seed. Remains open until
the owner judges the direction fully captured (Milestone 3 is still TBD by design).

---

<a id="story-3"></a>

### Story 3: Obtain USDA FoodData Central API key

**Type:** Tooling

**Summary:** The app can pull food data from USDA FoodData Central (United States) if an API key
is provided. Obtain that key and enter it in-app.

**Why / value:** Unlocks a large, authoritative food-composition source for search.

**Rough scope:** External account signup; in-app settings entry. No code.

**Status:** Not Started

---

<a id="story-4"></a>

### Story 4: Obtain Open Food Facts login

**Type:** Tooling

**Summary:** Create an Open Food Facts account / obtain credentials. With a login the owner can
contribute data back to the open-source collection — which he wants to do.

**Why / value:** Gives back to the open-data commons the app depends on.

**Rough scope:** External account signup. No code.

**Status:** Not Started

---

<a id="story-5"></a>

### Story 5: Define the master data format

**Type:** Feature

**Summary:** Define a master JSON format, in full detail, holding all of the owner's food data. It
is his own format, independent of any one app; the canonical store from which app-specific exports
(e.g. Food You CSV) are generated. It must accommodate custom foods, recipes, meals, and diary
entries per the design's domain model.

**Why / value:** Data ownership is a founding goal; an app-independent canonical format survives
any future app change.

**Rough scope:** Format specification (and likely a schema document). Deliberately **mutable**: it
is defined alongside the MyFitnessPal export (Story 6) and extended/changed as needed for the
Lose It data (Story 7).

**Status:** Not Started

---

<a id="story-6"></a>

### Story 6: Extract MyFitnessPal data

**Type:** Feature

**Summary:** Recover the owner's years of paid MyFitnessPal data. Approach is deliberately fast
and messy: do **not** use third-party exporters; instead copy-paste the data from MyFitnessPal into
prompts and have an LLM brute-force it into the master JSON format.

**Why / value:** That data is the owner's and he wants it back.

**Rough scope:** Manual copy-paste sessions + LLM transformation into master JSON. The master
format (Story 5) is being defined at the same time.

**Status:** Not Started

---

<a id="story-7"></a>

### Story 7: Extract Lose It data

**Type:** Feature

**Summary:** Once MyFitnessPal is done, log into Lose It, find the data there, and repeat the same
brute-force approach: paste into the LLM and store into the master format.

**Why / value:** Completes the historical-data recovery.

**Rough scope:** Same as Story 6. The master format may be extended or changed to accommodate both
datasets — that's fine, it is still mutable at this stage.

**Status:** Not Started

---

<a id="story-8"></a>

### Story 8: Determine Food You's CSV import schema

**Type:** Research

**Summary:** Food You imports data via CSV in a particular format. Find out exactly what that
format is.

**Why / value:** The export script (Story 9) cannot be written without the exact target schema.

**Rough scope:** Starting point: an export produced from Food You itself, already located in the
repository. Confirm against the import/export code in the `importexport` feature slice.

**Status:** Not Started

---

<a id="story-9"></a>

### Story 9: Build the export script (master JSON → Food You CSV)

**Type:** Feature

**Summary:** Produce a script that exports from the master JSON format into the Food You CSV
import format. The output must carry all three data groups: custom foods (built from
macro/mineral fundamentals), recipes (stay collapsed), and meals (expand on insertion).

**Why / value:** After import, all of the owner's data lives in a platform he owns, in his own
custom version of Food You.

**Rough scope:** Standalone script outside the app codebase; depends on Stories 5-8.

**Status:** Not Started

---

<a id="story-10"></a>

### Story 10: App update mechanism

**Type:** Tooling

**Summary:** Establish the ability to provide app updates without needing a public store release:
build an APK at will and get it onto phones. *(Originally conceived during Milestone 2 planning,
but required from the start.)*

**Why / value:** Updates are pointless if they wipe diary data; two-device support is a hard
requirement.

**Rough scope:** Signing/distribution decision — publishing privately to the Google Play Store is
acceptable if that's the cleanest mechanism; otherwise any workable direct-deploy mechanism.
Constraints: updates must not lose local data; must support both the owner's and his wife's phones.

**Status:** Not Started

---

<a id="story-11"></a>

### Story 11: Use the app for a while

**Type:** Research

**Summary:** Use the base app daily for a few days to a week to get to know it. Marked done when
the owner judges it done.

**Why / value:** Familiarity with the baseline grounds Milestone 2's customisation choices. Side
purpose: record friction — logging flows, navigation, search and barcode behaviour, screens worth
keeping.

**Rough scope:** No code. Observations feed Dictation and the backlog.

**Status:** Not Started

---

## Interdependency Order

1. Story 1 (build/deploy) — done; unblocks everything.
2. Stories 2-4 are independent and can happen anytime.
3. Data pipeline chain: Story 5 (master format) is co-developed with Story 6 (MyFitnessPal), then
   Story 7 (Lose It) may mutate the format; Story 8 (CSV schema) is independent research; Story 9
   (export script) needs 5-8.
4. Story 10 (update mechanism) is independent but must land before customized builds ship to both
   phones.
5. Story 11 (daily use) runs in parallel once data is imported.

---

## Backlog Sources

- All stories were mapped directly from the 2026-07-24 initial project seed; none staged in the
  backlog. Friction findings from Story 11 may spawn new backlog stories.

---

## Deferred / Follow-up Work

- None yet.

---

## Notes

- Keep this Story Index in sync with the [Milestones Index](../design.md#milestones-index) in Design.
