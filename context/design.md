---
name: design
description: Design specification for FoodUs - a personal, privacy-first fork of the Food You KMP/Compose food diary - covering architecture, principles, constraints, the context tier system, and the embedded milestones index.
metadata:
  version: "3.6"
  agentic_rails_source_version: "3.0"
  owner: "Jarryd Adaens"
  repo: "FoodYou (fork of maksimowiczm/FoodYou)"
---
# FoodUs - Design Specification

## Purpose of This File

This repository is **FoodUs**: Jarryd Adaens's personal fork of **Food You**, a free,
open-source, privacy-focused food diary and nutrition tracker (Kotlin Multiplatform + Compose
Multiplatform, Android-first). The repository and app were renamed from the upstream `FoodYou`
(interim brand "ACME Food App") to FoodUs on 2026-07-26, with applicationId
`io.github.jarrydadaens.foodus`; only the source namespace keeps upstream's
`com.maksimowiczm.foodyou` for clean merges. This file is the maintained design specification
for the fork.

This file is the Design tier: the maintained design specification covering the whole deliverable and
how it breaks into its largest pieces. It should synthesize relevant Dictation into stable project
direction. The **Milestones Index** lives as a subsection of this file (see below); the actual
Milestone documents are separate files under `milestones/`.

## Why This Fork Exists

**Motivation (owner's words, 2026-07-24).** Paid food trackers frustrate and disrespect the
customer. Apps like MyFitnessPal and LoseIt constantly nag for more money: after you have already
paid for a year of access or a feature, they hard-sell lifetime licenses, coaching upsells, and other
add-ons — and both take too many taps to log food. That treatment leaves the customer feeling
exhausted and disrespected rather than valued and served.

FoodUs exists to be the opposite: a food tracker that respects the user. Food You is already
free, open-source, privacy-first (no account required, all data stored locally), and ad-free, which
makes it the right base to build on. The upstream maintainer does not accept external changes, so
this fork produces a custom version of the application tailored to the owner's needs:

- **Replace MyFitnessPal and Lose It** as the household's nutrition trackers.
- **Own the data.** Years of paid diary data lives in those providers; this project recovers it into
  a master JSON format the owner controls, from which app-specific imports are generated.
- **Reduce logging friction over time** — AI-assisted logging (photo scanning, placeholder entries,
  smarter search) and quality-of-life changes layered on the solid local-first base.
- **Serve two users:** the owner's phone and his wife's phone. Every deployment and update decision
  must hold for both devices without data loss.

**Fork philosophy (constitutional — applies to all work):**

- All work happens on the fork's own branch; never assume upstream will take patches.
- Changes are complementary and additive, only very rarely structural.
- Changes must be easy to reapply when upstream pushes updates — minimize merge conflicts by
  design. LLM-driven implementation means perfection isn't required, but mergeability must always
  be kept in mind; unavoidable structural touches stay small and well-isolated.

**Guiding stance for this fork:**

- Respect the user. No nagging, no dark patterns, no upsell pressure.
- Preserve the privacy-first, local-first, no-account model.
- Keep it free and open-source (GPL-3.0 compliant).

**First working session (2026-07-24, complete).** Local Android toolchain stood up; app built and
deployed to an Android 16 emulator and to a physical Galaxy S22 Ultra. The live baseline exists;
the roadmap below (Milestones Index) captures the concrete scope synthesized from the initial
project seed dictation.

## Context Hierarchy

This is one of only two files in the framework allowed to state the numbered tier table (the other is [agenticworkflow.md](agenticworkflow.md)). Elsewhere, refer to tiers by name only.

| Tier | Document | Purpose |
| --- | --- | --- |
| 0 - Dictation | [dictations-tier-0/](dictations-tier-0/) | Raw dictation and supplemental design changes, before structure is imposed |
| 1 - Design | `design.md` | The whole deliverable, its largest pieces, and the Milestones Index subsection |
| 2 - Milestone | [milestones/](milestones/) | One coherent macro-feature or delivery outcome; contains the story list needed to deliver it |
| 3 - Story | Inside milestone docs, optionally staged first in [backlog/](backlog/) | Discrete work units (features, bugs, refactors) |
| 4 - Implementation Plan | `implementation-plans/<milestone-slug>/<story-slug>/plan.md` | The approach, acceptance, evidence, and continuation for substantial Story work |
| 5 - Phase *(optional)* | `implementation-plans/<milestone-slug>/<story-slug>/` | A safe slice of an over-large story |
| Support | [laws.md](laws.md) | Binding constitutional code quality and security laws; load relevant sections for implementation, security, architecture, or conflicts |
| Support | [agenticworkflow.md](agenticworkflow.md) | Context ownership and conditional workflow; load for planning, routing, status, or continuation |
| Support | [agent-thinking.md](agent-thinking.md) | Optional temporary scratchpad for long tasks |
| Support | [wiki/home.md](wiki/home.md) | Operational reference notes and cheat sheets |

The backlog (`backlog/`) is not a numbered tier. It is a staging pool — informally "Milestone -1" — of unscheduled stories before they are pulled into a milestone document. See the Milestones Index below and [agenticworkflow.md](agenticworkflow.md) for how milestone planning draws from it.

---

## Milestones Index

> This index is the table of contents for the project's milestones. It lives inside Design because a standalone milestones table is the same tier as Design. Each entry links down to a separate Milestone document under `milestones/`, which directly contains that milestone's story list.

| Milestone | Document | Status | Why it matters | What it unlocks |
| --- | --- | --- | --- | --- |
| Milestone 1: Initialization | [milestones/milestone-1.md](milestones/milestone-1.md) | Complete | Gets the fork built, deployed, populated with the owner's recovered historical data, and in daily use, with a repeatable two-device update mechanism | A live, data-complete daily driver that Milestone 2 can safely customize |
| Milestone 2: Customisation | [milestones/milestone-2.md](milestones/milestone-2.md) | In Progress (reopened 2026-07-26) | Makes the app the owner's own: FoodUs identity, AI-assisted logging, ergonomics fixes, adopted upstream bug fixes, a unified personal library in Settings, diary-slot bulk actions, and a developer UI name bar | An app that is faster to log with, unmistakably this fork, with source-oriented UI identification for agent collaboration |
| Milestone 3: Multiplayer | [milestones/milestone-3.md](milestones/milestone-3.md) | In Progress (started 2026-07-28) | Cross-diary logging for the household pair — log a shared meal once and it lands in both diaries — via profiles, friends, two-person trusted groups, and a dumb encrypted relay | A multi-user FoodUs that stays local-first: the island broken in exactly one controlled place |
| Milestone B: Bugs (supplementary) | [milestones/milestone-b.md](milestones/milestone-b.md) | Complete (2026-10-25) | Ten reported bugs and UX improvements covering onboarding, AI scanning, Quick Add, release history, and API-key masking | A dependable scan-to-diary workflow and clearer daily-use controls; closed B Stories add to the current numbered milestone's version count |

Keep this index in sync as milestones are added, completed, reordered, or reclassified. When a backlog Story is too large for one coherent work unit, promote it into this index as a new milestone.

### Product generation and delivery milestone

The owner's 2026-10-25 versioning decision identifies original Food You as Generation 1,
this FoodUs fork as Generation 2, and the multiplayer FoodUs direction with foodus-relay
as Generation 3. Current development advances to Generation 3 now; this does not assert
that multiplayer is complete or the relay is deployed. Those Milestone 3 gates remain open.

For product version derivation, the owner selects **Milestone 2** as the current delivery
milestone while its remaining Stories are open. Its closed Stories and Milestone B's closed
Stories contribute under the [versioning policy](versioning-policy.md). Milestone 3 work
can remain In Progress without changing that selection. Advancing the delivery milestone
requires an explicit owner decision recorded here; generation and milestone are independent.

---

## Executive Summary

**What it is.** Food You is a free, open-source, privacy-focused food diary and calorie/nutrition
tracker built with Kotlin Multiplatform and Compose Multiplatform. Android is the shipping platform
(Google Play alternative-free: distributed via F-Droid and GitHub releases); iOS targets exist in the
build but Android is primary. Current upstream version: 3.4.9 (GPL-3.0).

**Creator.** The upstream project is created and maintained by Mateusz Maksimowicz
([maksimowiczm](https://github.com/maksimowiczm) on GitHub). This repository is Jarryd Adaens's fork
of it — FoodUs — with its own shipped identity and upstream attribution. The
[versioning policy](versioning-policy.md) defines its adopted four-part product version
and the transition from historical three-part releases; upstream versioning remains attribution.

**Who it is for.** People who want to log what they eat and track calories, macros, and
micronutrients without accounts, ads, subscriptions, or upsells.

**What it does (owner's field assessment, 2026-07-24).** Fast, minimal calorie tracking with no
bullshit. No social features, no login, no sharing beyond CSV import/export. (Milestone 3
deliberately revises the first of these: FoodUs adds its own household multiplayer — cross-diary
logging over an encrypted relay — while keeping no-login and local-first intact; see "The
Multiplayer Exception" under Security and Privacy.) Easy to use; it
deliberately does not do exercise tracking, water tracking, or other adjacent concerns. It has
camera barcode scanning, optional open-data remote food databases (Open Food Facts, USDA FoodData
Central, Swiss Food Composition Database), recipe creation, personalized nutrition goals, and a
modular Material You home screen. All data stays on-device.

**Why this architecture is appropriate.** A single Kotlin Multiplatform codebase with Compose
Multiplatform UI keeps nearly all logic and UI in `commonMain`, with thin platform layers for
Android/iOS. Vertical feature slices with domain/infrastructure separation keep the app modular and
approachable despite living mostly in one Gradle module.

### Core Principles

- **Respect the user.** No nagging, no dark patterns, no upsells, no ads. (Fork's founding
  principle — see "Why This Fork Exists".)
- **Privacy-first, local-first.** No account, no telemetry; all diary data lives in a local Room
  (SQLite) database. Remote food databases are opt-in and read-only.
- **Minimal scope.** Food logging done well. Adjacent trackers (exercise, water, shopping
  lists) are intentionally out of scope — upstream enhancement requests that make the app fuzzy
  are not adopted. Social scope is the one deliberate expansion: Milestone 3's own multiplayer
  (cross-diary logging), designed under this fork's privacy rules rather than adopted from
  upstream requests.
- **Mergeable by design.** Fork changes are additive overlays that reapply cleanly over upstream
  updates (see "Fork philosophy").
- **Two-device reality.** Everything must work on both household phones, and updates must never
  lose local data.
- **Common code first.** Logic and UI live in `commonMain`; platform-specific code is a thin
  `expect`/`actual` and infrastructure layer.

### Domain Model (shared vocabulary)

The owner accepted this vocabulary on 2026-10-25. This section owns the target domain
language; source identifiers that still use earlier names are implementation debt.

| Domain/code concept | User-facing wording | Definition |
| --- | --- | --- |
| **Product** | Food | An individual nutritional item, including ordinary ingredients and packaged products. Nutrition is defined directly; brand, barcode, package weight, serving weight, and notes are optional metadata. Existing storage uses nutrition per 100 g or 100 mL. |
| **Recipe** | Recipe | Ingredients and their amounts combined into a new composite item with a serving yield. Its nutrition is derived from its ingredients. Ordinary logging creates one portioned diary entry for the Recipe. |
| **Meal** | Meal / Meals | A named, saved, reusable bundle. Loading it appends each constituent as a separate diary entry; it does not merge the bundle into a new Recipe. A constituent may represent a Product, a portion of a Recipe, or a captured Quick Add entry. |
| **DiarySlot** | The configured name, such as Breakfast or Lunch | A named diary category with its time window and display order. It is a destination for entries, independent of a reusable Meal definition. |
| **DiaryEntry** | Entry, usually displayed by its item name | A dated record assigned to a DiarySlot, carrying a nutritional snapshot of the amount logged. It is distinct from the reusable definition and must preserve historical nutrition when definitions change. |

**Food** is friendly user-facing vocabulary, not a fourth domain entity. **Template**
is not a separate product concept. The accepted reusable-bundle type is now **Meal**,
superseding the earlier `MealTemplate` target name. Product, Recipe, and Meal remain
distinct concepts when presented together in the selected shared library. The owner
selected **Products** as that library's atomic-item tab label in Story 38; friendly
Food wording elsewhere remains valid.

For example, a Meal named "Usual breakfast" may contain an omelette Recipe and toast
from a Product. Loading it into tomorrow's Breakfast DiarySlot creates two DiaryEntries;
the omelette's ingredients remain combined inside its Recipe entry. Viewing or editing
a saved definition is separate from logging it.

#### Serving, portion, and quantity

A **serving** is a reference quantity: one serving of a Recipe or a defined unit of a
Product. A **portion** is the amount actually used or eaten, such as two servings or
150 g. Quantity and unit must be explicit where known. Recipe yield is how many
servings the complete Recipe makes; a diary portion is how much was logged.

These are semantic decisions, not authorization to introduce a new Portion class or
change numeric storage automatically. Existing `Measurement` types already represent
amounts in weight, volume, servings, or packages. Preserve unknown weights and units;
never invent a gram basis for a saved entry. Quick Add stores absolute nutrition totals;
its optional serving count and weight are descriptive metadata and must not silently
rescale those totals. A placeholder is an unresolved DiaryEntry, not another reusable
catalogue type.

#### Accepted Recipe calculation and editing contract

Story 27's [plan](implementation-plans/milestone-2/story-27-recipe-composition-portions/plan.md)
selects ingredient totals as the starting point. A Recipe portion of q reference
servings scales those totals by q / yield; a whole-Recipe/package fraction scales
its complete totals directly. Do not require a fabricated gram/mL basis as an
intermediate for serving nutrition. Nested Recipe serving portions use the same
rule, once at each composition boundary. Existing positive integer yield storage
remains; portions and ingredient measurements can be fractional.

A Recipe's aggregate metric basis is known only when all constituent amounts have
a complete compatible mass/volume basis. Never sum grams and millilitres or infer
density. Metric portions and per-100 Recipe facts require that known basis; mixed
ingredient Recipes can still use serving fractions when their ingredient totals
resolve. Unknown nutrient fields remain incomplete, not a partial sum or invented
zero. Legacy unavailable compositions stay readable as unavailable data. New
save/log/unpack operations reject invalid or unresolvable required amounts before
writes, with recoverable feedback.

The complete editor draft drives previews. Save validates candidate graph edges
and metadata/ingredients atomically, rejecting self/indirect cycles while retaining
valid shared branches; Cancel and failed saves preserve the prior definition.
Ordinary logging remains one captured Recipe entry. Explicit unpack validates and
atomically appends/replaces direct constituents using the Recipe portion fraction;
nested Recipes remain single constituents until explicitly unpacked themselves.
Historical Recipe snapshots require an internal calculation-contract discriminator.
Concrete baseline inspection showed that a mixed 100 g / 100 mL Recipe logged as
100 g displayed 150 energy units under the old calculation, while strict new basis
rules would make it unavailable. Unknown-nutrient aggregation also changes results.
The persisted composition and measurement alone cannot identify the intended rule.

Story 27 therefore owns an additive schema 39 → 40 migration adding a DiaryRecipe
snapshot calculation contract: existing rows default to legacy 0, fresh captures
from live definitions use current 1, and copying existing snapshots retains their
contract. Preserve recorded legacy nutrition through its frozen legacy calculation;
apply strict total-first/compatible-basis rules to new captures. Do not silently
reinterpret old contracts during reads, copy or unpack. New density/unit models,
live diary references and external relay/CSV/wire changes remain outside scope.
Keep schema 39 intact, verify all existing columns/IDs and new default 0 through
real migration/reopen tests. Story 24's separate nullable amount-metadata migration
will follow the accepted predecessor schema, expected 40 → 41.
The same migration also corrects the proven Recipe dependency-view defect: traversal
follows typed Recipe IDs, terminates on cycles, and cannot confuse a same-number
Product with a Recipe. Preserve the view identity/output contract and stored rows;
verify actual DAO queries and all consumers against the corrected ancestry meaning.
Implementation/acceptance remains owned by Story 27.

#### Current source names and implementation transition

| Existing identifier/role | Accepted target or planning requirement |
| --- | --- |
| `Product` and `Recipe` | Keep their distinct roles and existing creation/editing foundations. |
| `MealTemplate` and its related item, ID, repository, use-case, and UI names | Adopt the `Meal` family for reusable bundles, including `MealItem` and related names. Remove template terminology from this feature's UI. |
| Former diary-category `Meal` and category `mealId` references | Adopt `DiarySlot` and corresponding slot identifiers. Free the `Meal` name for the reusable bundle without confusing the two identities. |
| `DiaryMeal`, the category-and-entries aggregate | Story 25's plan selects `DiarySlotEntries` for this diary grouping/projection. It is not the saved reusable Meal. |
| Shared `Food` interface and related polymorphic APIs | Story 26 implements `NutritionalItem` / `NutritionalItemId` for the sealed Product-or-Recipe definition and typed identity. Product and Recipe remain distinct variants; synthetic Product/identity/snapshot acceptance is recorded in Story 26. |

Story 26's [plan](implementation-plans/milestone-2/story-26-product-terminology-portions/plan.md)
selects **NutritionalItem** as a shared reusable-definition role, not a fourth user
concept. It owns typed identity, display headline, canonical nutrition, liquid status,
optional known reference/total basis and supported portion resolution. Captured
definitions use **NutritionalItemSnapshot**, **ProductSnapshot**, **RecipeSnapshot**,
and **RecipeIngredientSnapshot** without live catalogue IDs. A dated, portioned
snapshot uses **NutritionalItemDiaryEntry**, distinct from generic DiaryEntry and
ManualDiaryEntry. Product-only provenance and remote errors use Product names.
Shared search/history/suggestion APIs use the selected role; UI Food wording,
upstream namespaces, legacy storage/serialized identities and external names remain.

[Story 25](milestones/milestone-2.md#story-25) delivers the DiarySlot/Meal separation and
DiarySlotEntries projection, with legacy persistence/serialization preserved and
synthetic upgrade/backup/navigation acceptance.
[Story 26](milestones/milestone-2.md#story-26) delivers the shared typed/snapshot
terminology and Product serving/basis/portion reconciliation with unit, real-Room,
actual-screen and historical-data acceptance.
[Story 27](milestones/milestone-2.md#story-27) delivers Recipe composition/yield, ordinary
snapshot logging and explicit unpack, with historical calculation preservation and
synthetic unit, Room, actual-screen and upgrade acceptance. [Story 24](milestones/milestone-2.md#story-24) delivers the selected Meals library/editor
and captured-portion model, with atomic save/update/load, preserved legacy data,
actual-screen workflows and production process-restart acceptance recorded in the
[batch completion record](_boss-runs/milestone-2-domain-meals-sequential/domain-meals-batch-receipt.json).
Implementation must handle the existing name collision coherently, update relevant
APIs/state/navigation/resources/tests, and preserve stable identities and serialized
meaning. Kotlin type renames must not implicitly rename Room tables or columns, break
backups, reassign entries to different slots, or lose saved bundles. Keep explicit
legacy storage mappings or use a tested data-preserving migration where needed.
Historical Story evidence, paths, and upstream source namespaces retain their identities.

A central Meals library/editor is selected in
[Milestone 2 Story 24](milestones/milestone-2.md#story-24), extending Story 13's delivered
save/apply/delete flow. It must share one saved collection with diary actions, support
rename and constituent editing with explicit Save/Cancel, and preserve existing diary
snapshots when saved Meals change. The [accepted implementation plan](implementation-plans/milestone-2/story-24-meals-library-editor/plan.md)
delivered an always-visible Meals action beside Settings in Home, a private Save/Cancel
draft, and one shared saved collection. MealItem stores absolute captured totals.
Retained item IDs/order and aggregate creation time survive atomic updates. Loading
validates one committed Meal and its destination, then appends each constituent as a
separate manual snapshot in one transaction, guarded against duplicate submissions.

Story 24 adds six nullable quantity-context columns in schema 40 → 41: MealTemplateItem
gets capturedPortionType/value plus descriptive servingCount/weightGrams;
ManualDiaryEntry gets capturedPortionType/value. Existing rows remain null. Known
portions use the existing Measurement types; legacy original amounts stay unknown.
A same-unit amount edit or explicit whole-portion scale changes captured totals once;
direct descriptive count/weight edits leave nutrition unchanged and clear a conflicting
known captured portion with explanation. Name/nutrient edits and no-op context edits
preserve quantity metadata. No density, missing basis, live source link or amount is
invented. New captures normalize incomplete nutrient fields to unknown before preview
and flat persistence, across all 43 fields; existing saved numeric values and Recipe
historical contracts remain unchanged. Schema 39 and accepted 40 exports are immutable.

The owner's 2026-10-26 decision selects a combined **Recipes, Meals & Products** library
in [Milestone 2 Story 38](milestones/milestone-2.md#story-38), currently Not Started.
It replaces the Home Meals shortcut with a Settings row immediately below Daily goals.
The destination has search, Recipes / Meals / Products tabs, compact rows with nutrition
summaries and explicit reference quantities, and a bottom creation action for the active
tab. Products lists personal/custom items, including personal imports; existing provider
search remains separate. Definition creation/editing from Settings requires no diary
destination, while loading a Meal explicitly selects a day and DiarySlot. Existing
repositories, captured nutrition, Save/Cancel behavior and diary snapshot isolation remain
binding. This extends Story 24 without reopening it. The shared abstraction and diary-group
projection are selected above; the editor's unknown-portion contract is selected in its plan.
No third-party studies or extracted reference material belong in this context.

The owner also selected diary-slot bulk actions in
[Milestone 2 Story 39](milestones/milestone-2.md#story-39), Complete on 2026-10-26.
The DiarySlot ellipsis menu provides Delete All, Move to..., Copy to... and Copy From...
for all entries in the selected slot on the displayed diary day. Source/destination
selection allows a different day, slot or both. Copies append independent snapshots;
moves transfer the entries atomically; Delete All confirms and removes only that day's
entries in that slot. Existing destination entries, saved definitions, recorded nutrition
and unrelated history are preserved. Copy From also works into an empty slot. Same
source/destination selection is rejected, and existing Save Meal/Load Meal remain available.

The owner selected implementation in Milestone 2, ordered
[25: DiarySlot/Meal separation](milestones/milestone-2.md#story-25) →
[26: Product terminology and portions](milestones/milestone-2.md#story-26) →
[27: Recipe composition and portions](milestones/milestone-2.md#story-27) →
[24: Meals library/editor](milestones/milestone-2.md#story-24). Story 25 owns the existing
diary-group projection's naming decision, Story 26 the shared interface's naming and
Product portion contract, Story 27 Recipe yield/composition, and Story 24 legacy saved-
Meal editing semantics. The milestone owns each Story's acceptance and current status.
This selection records implementation work without changing the app version or closing
Stories. A combined catalogue hub and additional import/sharing features remain outside
this batch.


After the original batch and private APK delivery, the owner authorized
[Story 28](milestones/milestone-2.md#story-28) for faithful structured personal-library
import because the existing CSV path flattens Products/Recipes/Meals. The planned
extension must preserve declared nonmetric serving references, reported Recipe
nutrition independently of ingredient-derived facts, unknown yields and ordered inline
components, and Meal absolute snapshots/portion text. It remains local and additive;
existing diary/settings and legacy definitions/snapshots retain their meaning. A
FoodUs-owned format and synthetic fixtures belong in source; account-specific adapters,
raw records and derived personal packages remain private outside the checkout.
The [adopted plan](implementation-plans/milestone-2/story-28-structured-personal-import/plan.md)
selects explicit legacy-metric/reference-serving Product bases captured in snapshots;
separate reported Recipe panels and ordered inline components with nullable yields;
preserved calculation 0/1 plus an explicit reported snapshot mode; independent Meal
reported panels and immutable descriptive provenance; and a typed source-identity ledger
with atomic conflict rejection. Reported composition/reference edits require nutrition
review; ordinary derived behavior remains unchanged. Reported unpack is unavailable
in v1, snapshots never depend on live catalogue foreign keys, and CSV export cannot
mislabel unsupported serving-only facts as metric facts. The source uses a versioned
FoodUs-owned format, separate from private account adapters. Story28 passed Android
acceptance on 2026-10-26 with schema42 and private APK delivery. Original schema39–41
exports and historical calculation contracts remain unchanged. The package imports
the library, not diary history. Reported Unpack/structured export remain unavailable;
metric CSV rejects unsupported reference facts, and full native backup retains them.
See the adopted plan and final receipt for evidence and remaining platform/identity limits.

---

## System Architecture

### How the Pieces Fit Together

Three Gradle modules:

- `:app` — the application. Almost everything lives here, organized as vertical feature slices
  under `com.maksimowiczm.foodyou`: `food` (food catalog, search, remote database integrations),
  `fooddiary` (meal logging), `goals` (nutrition targets), `importexport` (CSV), `settings`,
  `changelog`, `poll` (in-app feedback polls), `sponsorship`, `theme`, `common` (shared
  utilities), and `app` (DI wiring, navigation, root UI, platform infrastructure).
- `:shared:barcodescanner` — camera barcode scanning (ZXing-based on Android).
- `:shared:resources` — shared localized string/image resources (Compose Multiplatform resources).

Each feature slice separates `domain` (entities, events, repository interfaces, use cases) from
`infrastructure` (Room persistence, Ktor network clients, remote-source adapters), with Koin
modules (`<Feature>Module.kt`) composing them. UI is Compose Multiplatform Material 3 with
Navigation Compose; state flows through ViewModels provided by Koin.

Kotlin targets: `androidTarget` (JVM 21), `iosArm64`, `iosSimulatorArm64`.

**External services and dependencies:**

- Open Food Facts — opt-in remote food product database (community open data; the owner intends to
  hold an account and contribute data back)
- USDA FoodData Central — opt-in remote food composition database (user-supplied API key)
- Swiss Food Composition Database — opt-in imported food composition data
- AI model endpoint (Milestone 2) — any OpenAI-compatible endpoint (OpenRouter default), called
  with a **user-entered** API key, endpoint, and model configured in the AI settings screen
  (2026-07-26 addendum; supersedes the original baked-into-private-builds key design), for
  photo-based food identification and search-query generation. See "Security and Privacy" for
  the boundary this creates.
- FoodUs relay (Milestone 3, planned) — a self-hosted, deliberately dumb store-and-forward
  message relay ("post office") that ferries sealed, end-to-end-encrypted envelopes between
  household devices for cross-diary logging. It holds only GUIDs, public keys, friend codes,
  block relationships, and per-GUID queues of ciphertext (swept after 30 days) — no accounts,
  no login, no diary data in the clear. The relay lives in its own repository,
  **foodus-relay** (ASP.NET minimal API + SQLite behind Caddy TLS on a self-hosted droplet),
  which owns the wire contract this app conforms to. The relay endpoint URL is user-entered in
  settings and never ships in code or repo. See "The Multiplayer Exception" under Security and
  Privacy.
- No other backends. No analytics, crash reporting, or account services.

### Repository Structure

```text
FoodYou/
|-- app/                        # main KMP application module
|   `-- src/{commonMain,commonTest,androidMain,androidInstrumentedTest}/
|-- shared/
|   |-- barcodescanner/         # camera barcode scanning module
|   `-- resources/              # shared localized resources module
|-- gradle/libs.versions.toml   # version catalog (single source of dependency truth)
|-- context/                    # agentic rails context tiers (this framework)
|-- harness/                    # verifiers, gates, sensors for agent workflows
|-- dev/                        # upstream dev scripts/assets
|-- docs/                       # upstream docs
|-- metadata/                   # F-Droid/fastlane store metadata and screenshots
|-- flake.nix / justfile        # upstream nix + just developer tooling
`-- README.md
```

---

## Processing Pipelines

### Food Logging (core journey)

1. User picks a meal/day in the diary and searches for a food (local catalog first; opt-in remote
   sources via Ktor if enabled) or scans a barcode with the camera.
2. Selected food + portion is written to the local Room database as a diary entry.
3. Home screen cards and goal screens recompute calories/macros/micros reactively (Flow → Compose).

### Data Portability

1. Export: diary/food data serialized to CSV and shared via the platform share/storage APIs.
2. Import: CSV parsed and merged into the local Room database.
3. No cloud sync exists; a device's database is the single copy.

### Historical Data Recovery (Milestone 1)

The owner's years of MyFitnessPal and Lose It data are recovered into a **master JSON format** —
an app-independent canonical store the owner controls, covering custom foods, recipes, meals, and
diary entries per the domain model. The format is deliberately mutable while both datasets are
ingested (LLM-assisted, fast-and-messy copy-paste extraction; no third-party exporters). An export
script then generates Food You-format CSV from the master JSON for import into the app. The master
JSON remains the canonical source; app-specific formats are generated exports.

---

## Configuration

### Primary Configuration

- `gradle/libs.versions.toml` — version catalog: fork version (`fork-version-name`,
  the shipped versionName under the adopted policy and its legacy transition), upstream version-name kept as
  derived-from metadata, versionCode, SDK levels
  (min 28, compile/target 36), and every dependency version. Change dependencies here, not in
  build files. [FoodUs Versioning Policy](versioning-policy.md) owns the release version rules.
- `app/build.gradle.kts` — KMP targets, build types (`release` minified, `devRelease`,
  `miniDevRelease`, `preview` with `.preview` app-id suffix), Room schema dir, BuildConfig.
- `gradle.properties` — JVM memory, configuration cache, build cache.

### Machine-Level Configuration

- `local.properties` — `sdk.dir` pointing at the local Android SDK (untracked).
- JDK 21 required. Local toolchain paths for this machine are recorded in agent memory, not in the
  repo.

### Secrets and Credentials

None in the repository. Credentials in the system:

- The optional USDA FoodData Central API key, which the user enters in-app and which is stored
  on-device.
- The AI endpoint key: **user-entered in the AI settings screen and stored on-device**, like the
  USDA key (2026-07-26 addendum). This supersedes the original baked-into-private-builds design
  and dissolves its open secret-injection question — no AI credential ever exists in the
  repository, CI, or any build. The build-time `foodus.ai.*` BuildConfig fallbacks remain for
  developer convenience only and must stay blank in anything public.
- The relay endpoint URL (Milestone 3): **user-entered in settings and stored on-device**, in
  the same spirit as the AI endpoint configuration. Not a credential, but deliberately private:
  the owner's relay address is never published in code, repo, docs, or builds (2026-07-27 relay
  seed dictation). Anyone else running the published app points it at their own relay.

Release signing is the app distributor's concern (upstream signs F-Droid/GitHub releases; this
fork uses debug signing locally until the Milestone 1 update-mechanism story decides distribution).

---

## Application Layers

Within each feature slice (and the app as a whole):

### UI (Compose Multiplatform)

Material 3 (Expressive) screens, ViewModels, and navigation in `commonMain`. Theming via
MaterialKolor dynamic color; reorderable home cards; shimmer loading states.

### Domain

Pure Kotlin entities, events, repository interfaces, and use cases per feature slice. No Android or
framework types.

### Infrastructure

Room database (+ Paging), DataStore preferences, Ktor HTTP clients (OkHttp engine on Android,
Darwin on iOS), and adapters for Open Food Facts / USDA. Koin modules bind infrastructure to domain
interfaces.

### Platform (`androidMain` / `iosMain`)

Activity/entry points, permissions, camera/barcode integration, platform SQLite driver
(requery sqlite-android), share/file APIs.

---

## Security and Privacy

- All user data is stored locally in Room (SQLite) and DataStore on the device. No account, no
  cloud sync, no telemetry, no ads. (Once Milestone 3 lands, the relay additionally holds
  profile metadata — GUIDs, usernames, public keys, friend codes, block relationships — and
  transient sealed ciphertext; never diary data in the clear. See "The Multiplayer Exception"
  below.)
- Remote food databases are opt-in, disclosed at onboarding with their own terms, and used
  read-only over HTTPS.
- Secrets are limited to the user's optional USDA API key and the user-entered AI endpoint key,
  both stored on-device (see Configuration; the baked-key design was superseded 2026-07-26).
- **AI boundary (Milestone 2).** The AI logging features send data off-device by design:
  downscaled food photos and short meal descriptions go to the user-configured model endpoint
  under the user's own API key. Prompting is three-layered (2026-07-26 addendum): a baked
  developer prompt containing **no personal data** (task framing and output shape only), an
  optional user-authored system prompt holding all personal context (stored on-device), and an
  optional per-scan hint. This is a deliberate, owner-chosen exception to the local-only stance,
  acceptable because the key is the user's own and both users are informed household members. No
  other data leaves the device, and nothing is sent without an explicit user action (Ask AI /
  AI search / Submit). (Milestone 3 adds a second explicit-action send path: Save to Group
  transmits sealed food entries via the relay — see "The Multiplayer Exception" below.)
- **The Multiplayer Exception (Milestone 3).** The app's "island" stance — fully local, no
  external services of its own — is broken in exactly one controlled place: a self-hosted,
  deliberately dumb **store-and-forward relay** that ferries messages between household devices
  so a trusted pair can log into each other's diaries. The durable decisions
  (2026-07-27 dictation):
  - **Relay, not cloud.** Each person's Room database stays the sole source of truth on their
    device. The server is a transmission vector between databases — a post office parking sealed
    envelopes addressed to a GUID until the recipient collects them. No accounts, no login, no
    server-side backup, no web/companion clients. Undelivered messages sweep after 30 days.
  - **End-to-end encryption.** Every profile generates an asymmetric key pair; senders encrypt
    each packet with the recipient's public key before it leaves the device. The server stores
    only ciphertext it cannot read (a breach yields ciphertext, usernames, and GUIDs). Key
    distribution rides the friend-code lookup: becoming friends *is* the key exchange. Known,
    accepted weakness: a malicious server could substitute public keys (MITM) — theoretical for
    a self-hosted household relay; Signal-style safety numbers are a backlog stretch item.
  - **Private keys in the Android Keystore** — hardware-backed, never in the Room database,
    DataStore, any file, or any backup. Database backups stay fully portable; the private key
    deliberately is not.
  - **Keys live and die with the device.** On device loss, restore the database backup (keeping
    GUID and social graph), generate a fresh key pair, and re-announce it; friends' devices pick
    up the new key on next poll. In-flight messages to the old key are unopenable and lost —
    acceptable by design for throwaway diet packets under the 30-day sweep. The security model
    is never weakened to protect data already judged disposable.
  - **Poll on app wake, no push.** No background polling service, no Firebase Cloud Messaging —
    Google stays out of the privacy story. Latency (you see the shared meal next time you open
    the app) is acceptable for non-time-critical diet data.
  - **Separate repo owns the contract (2026-07-27 relay seed dictation).** The relay is built
    in its own repository, **foodus-relay** — ASP.NET (C#) minimal API + SQLite behind a Caddy
    TLS front door on a self-hosted DigitalOcean droplet (Sydney). That repo owns the wire
    contract as a written specification (envelope shape, endpoints, auth scheme); this app is a
    client that conforms. App-side envelope data classes are hand-written to match the spec —
    no submodules, no shared schema machinery. When the contract changes, both sides change in
    the same sitting.
  - **User-entered relay URL.** The relay endpoint is configured by the user in a settings
    surface (same spirit as the AI endpoint settings) and the owner's instance address stays
    private — never in code, repo, or docs. Strangers running the published app stand up their
    own relay.
  - **Versioned, tolerant wire protocol.** Every packet carries an envelope version stamp; the
    app refuses an unknown version loudly (a Notification Center event, never a silent drop).
    Deserialisation ignores unknown fields and treats absent fields as "not provided", so the
    contract can evolve additively without breaking un-updated phones. The relay exposes a
    version/capability endpoint; the app hides or greys relay-backed features the connected
    relay doesn't report — a phone that updates before the server deploys waits gracefully.
  - **HTTPS only, server leads.** The app talks to the relay exclusively over HTTPS via its
    existing Ktor client stack. Deployment ordering is constitutional: the relay deploys first,
    the app follows — new server capability sits dormant until the app consumes it.
  - **Two rooted agents, owner as human relay.** Each repo has its own agent rooted in its own
    laws and context. Agents read across the repo boundary freely but edit only at home;
    contract changes are proposed via reports, adjudicated and carried across by the owner, who
    also enforces deployment sequencing. Cross-repo features are paired stories sharing a
    common slug, with a one-way versioned dependency note pointing app → server.
  - **Open question (owned by foodus-relay):** relay endpoint authentication — "no accounts"
    still requires proof of GUID ownership (likely request signing with the device key pair,
    plus replay protection) so a known GUID cannot be drained, re-keyed, or impersonated. Once
    open in app Story 3.3 (relay architecture decisions; Story 3.4 before the 2026-07-29
    re-enumeration), this now resolves in the foodus-relay repo's first milestone, along
    with friend-code minting authority, the friend-code charset, and the exact wire contract.
    Stack and hosting, formerly part of the same open set, were resolved 2026-07-27.
- These properties are constitutional for this fork: changes that add tracking, accounts, or
  nagging violate its founding purpose.

---

## Observability

No analytics or crash reporting by design. Debugging is standard Android tooling: logcat, Compose
UI tooling in debug builds, and adb against a device/emulator. The only server-side component is
the planned Milestone 3 relay; its operational needs (health checks, sweep verification,
monitoring) are owned by the foodus-relay repository.

[Milestone 2 Story 29](milestones/milestone-2.md#story-29) adds an off-by-default developer
UI name bar available in released builds. A roughly three-second deep press identifies a
visible element; Copy exports its identifier, optionally with detailed hierarchy, fully
qualified component/composable and owning symbols, and repository source locators. The output
prioritizes agent comprehension and finding the implementation; verbose structured text such
as XML is acceptable. While enabled, holds are reserved for inspection; disabling the bar
restores normal long presses, per the owner's 2026-10-26 interaction decision. The naming pass
and runtime proof of suppression/restoration belong to that Story.
Export describes UI structure without credentials, private relay addresses, or diary/field
contents, and introduces no automatic transmission. Supplemental agent tooling is conditional
on demonstrated need.

---

## Testing Policy

Follows the repository's unit-testing limits rule: tests serve the change, not coverage numbers.

- `commonTest` — kotlin-test with Room testing + bundled SQLite for platform-neutral logic
  (nutrition math, parsing, mapping, use cases).
- `androidInstrumentedTest` — AndroidX test runner/JUnit for behavior needing a real Android
  runtime.
- Bug fixes should carry a reproducing test when practical; UI/visual behavior is validated
  manually on emulator or device.

---

## Performance

The app is fast in daily use on real hardware (owner-validated on a Galaxy S22 Ultra); performance
is not currently a concern. Release builds are R8-minified. Revisit only if profiling shows a
regression.

---

## Context Maintenance

Use Dictation to revise this design when the project vision changes. Do not leave important decisions stranded in raw notes, chats, or addenda. Promote durable decisions into this file, the Milestones Index, milestone docs, stories, or implementation plans as appropriate.

When an older design statement is superseded, update it directly and preserve only the rationale needed for future agents to understand the decision.

---

## Navigation

### Specification Hierarchy

- [dictations-tier-0/README-DICTATIONS-TIER-0.md](dictations-tier-0/README-DICTATIONS-TIER-0.md) - Dictation, raw and unstructured
- [design.md](design.md) - Design overview and Milestones Index
- [milestones/](milestones/) - Milestone documents, each directly containing its story list
- [backlog/](backlog/) - Story inventory / Milestone -1, the unscheduled staging pool
- `implementation-plans/*/` - Implementation Plans, optional Phases, and execution records

### Reference Documents

- [agenticworkflow.md](agenticworkflow.md) - AI collaboration workflow
- [agent-thinking.md](agent-thinking.md) - optional temporary agent scratchpad

### Wiki

- [wiki/home.md](wiki/home.md) - wiki navigation hub
