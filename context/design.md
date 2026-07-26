---
name: design
description: Design specification for FoodUs - a personal, privacy-first fork of the Food You KMP/Compose food diary - covering architecture, principles, constraints, the context tier system, and the embedded milestones index.
metadata:
  version: "3.0"
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
| 4 - Implementation Plan | `implementation-plans/<milestone-slug>/<story-slug>/plan.md` | The normalized how-to for one story, with scoring and mitigation |
| 5 - Phase *(optional)* | `implementation-plans/<milestone-slug>/<story-slug>/` | A safe slice of an over-large story |
| Support | [laws.md](laws.md) | Constitutional code quality and security laws — loaded first by all agents |
| Support | [agenticworkflow.md](agenticworkflow.md) | Workflow for AI agent collaboration |
| Support | [agent-thinking.md](agent-thinking.md) | Optional temporary scratchpad for long tasks |
| Support | [wiki/home.md](wiki/home.md) | Operational reference notes and cheat sheets |

The backlog (`backlog/`) is not a numbered tier. It is a staging pool — informally "Milestone -1" — of unscheduled stories before they are pulled into a milestone document. See the Milestones Index below and [agenticworkflow.md](agenticworkflow.md) for how milestone planning draws from it.

---

## Milestones Index

> This index is the table of contents for the project's milestones. It lives inside Design because a standalone milestones table is the same tier as Design. Each entry links down to a separate Milestone document under `milestones/`, which directly contains that milestone's story list.

| Milestone | Document | Status | Why it matters | What it unlocks |
| --- | --- | --- | --- | --- |
| Milestone 1: Initialization | [milestones/milestone-1.md](milestones/milestone-1.md) | Complete | Gets the fork built, deployed, populated with the owner's recovered historical data, and in daily use, with a repeatable two-device update mechanism | A live, data-complete daily driver that Milestone 2 can safely customize |
| Milestone 2: Customisation | [milestones/milestone-2.md](milestones/milestone-2.md) | In Progress (reopened 2026-07-26) | Makes the app the owner's own: FoodUs identity, AI-assisted logging, ergonomics fixes, adopted upstream bug fixes | An app that is faster to log with than MyFitnessPal/Lose It ever were, unmistakably this fork |
| Milestone 3: TBD | [milestones/milestone-3.md](milestones/milestone-3.md) | Not Defined | Awaiting future dictation | — |

Keep this index in sync as milestones are added, completed, reordered, or reclassified. When a backlog story scores as epic-sized, promote it into this index as a new milestone.

---

## Executive Summary

**What it is.** Food You is a free, open-source, privacy-focused food diary and calorie/nutrition
tracker built with Kotlin Multiplatform and Compose Multiplatform. Android is the shipping platform
(Google Play alternative-free: distributed via F-Droid and GitHub releases); iOS targets exist in the
build but Android is primary. Current upstream version: 3.4.9 (GPL-3.0).

**Creator.** The upstream project is created and maintained by Mateusz Maksimowicz
([maksimowiczm](https://github.com/maksimowiczm) on GitHub). This repository is Jarryd Adaens's fork
of it — FoodUs — which will carry its own identity, versioning (starting at 1.0, layered on
top of the upstream version), and upstream attribution once Milestone 2's identity story lands.

**Who it is for.** People who want to log what they eat and track calories, macros, and
micronutrients without accounts, ads, subscriptions, or upsells.

**What it does (owner's field assessment, 2026-07-24).** Fast, minimal calorie tracking with no
bullshit. No social features, no login, no sharing beyond CSV import/export. Easy to use; it
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
- **Minimal scope.** Food logging done well. Adjacent trackers (exercise, water, social,
  shopping lists) are intentionally out of scope — upstream enhancement requests that make the
  app fuzzy are not adopted.
- **Mergeable by design.** Fork changes are additive overlays that reapply cleanly over upstream
  updates (see "Fork philosophy").
- **Two-device reality.** Everything must work on both household phones, and updates must never
  lose local data.
- **Common code first.** Logic and UI live in `commonMain`; platform-specific code is a thin
  `expect`/`actual` and infrastructure layer.

### Domain Model (shared vocabulary)

These concepts underpin the Milestone 1 data-recovery work and the Milestone 2 UI work:

| Concept | Definition |
| --- | --- |
| **Custom food** | A food built from scratch by specifying its fundamentals — macros, minerals, fats, proteins, etc. — and given a name. |
| **Recipe** | A collection of distinct ingredient foods making up one final food item. When used, it stays **collapsed** into a single diary entry (e.g. "birthday cake"). |
| **Meal** | Also built from components, but when inserted into the diary it **expands**: each sub-item is added as its own entry (e.g. "chicken sandwich" inserts bread + mayonnaise + 100 g chicken + 50 g cheese). |
| **Diary entry** | A reference to a food that was eaten — not the definition of the food. Links to a food and gives it a portion size. |
| **Placeholder entry** | (Planned, Milestone 2.) A zero-calorie diary entry holding only a name and description — a marker that something was eaten, resolved into real data later. |

Recipes and meals overlap conceptually; the difference is collapse-vs-expand behavior at insertion
time.

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
  `<milestone>.<story>.<build>`, the shipped versionName), upstream version-name kept as
  derived-from metadata, versionCode, SDK levels
  (min 28, compile/target 36), and every dependency version. Change dependencies here, not in
  build files.
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
  cloud sync, no telemetry, no ads.
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
  AI search / Submit).
- These properties are constitutional for this fork: changes that add tracking, accounts, or
  nagging violate its founding purpose.

---

## Observability

No analytics or crash reporting by design. Debugging is standard Android tooling: logcat, Compose
UI tooling in debug builds, and adb against a device/emulator. There are no server-side components
to health-check.

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
