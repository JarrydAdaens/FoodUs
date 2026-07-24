---
name: design
description: Design specification for the Food You fork - a privacy-first KMP/Compose food diary - covering architecture, principles, constraints, the context tier system, and the embedded milestones index.
metadata:
  version: "3.0"
  agentic_rails_source_version: "3.0"
  owner: "Jarryd Adaens"
  repo: "FoodYou (fork of maksimowiczm/FoodYou)"
---
# Food You (Fork) - Design Specification

## Purpose of This File

This repository is a fork of **Food You**, a free, open-source, privacy-focused food diary and
nutrition tracker (Kotlin Multiplatform + Compose Multiplatform, Android-first). This file is the
maintained design specification for the fork.

This file is the Design tier: the maintained design specification covering the whole deliverable and
how it breaks into its largest pieces. It should synthesize relevant Dictation into stable project
direction. The **Milestones Index** lives as a subsection of this file (see below); the actual
Milestone documents are separate files under `milestones/`.

## Why This Fork Exists

**Motivation (owner's words, 2026-07-24).** Paid food trackers frustrate and disrespect the
customer. Apps like MyFitnessPal and LoseIt constantly nag for more money: after you have already
paid for a year of access or a feature, they hard-sell lifetime licenses, coaching upsells, and other
add-ons. That treatment leaves the customer feeling exhausted and disrespected rather than valued and
served.

This fork exists to be the opposite: a food tracker that respects the user. Food You is already free,
open-source, privacy-first (no account required, all data stored locally), and ad-free, which makes
it the right base to build on. The concrete set of changes the owner has in mind will be captured in
Dictation and promoted into milestones and stories once the project's structure is understood.

**Guiding stance for this fork:**

- Respect the user. No nagging, no dark patterns, no upsell pressure.
- Preserve the privacy-first, local-first, no-account model.
- Keep it free and open-source.

**First working session (2026-07-24, complete).** Local Android toolchain stood up; app built and
deployed to an Android 16 emulator and to a physical Galaxy S22 Ultra. The live baseline exists;
concrete feature scope will be captured via Dictation and promoted into milestones.

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
| Milestone 1: *Name* | [milestones/milestone-1.md](milestones/milestone-1.md) | Not Started | *Why this milestone matters* | *What completing it unlocks* |

Keep this index in sync as milestones are added, completed, reordered, or reclassified. When a backlog story scores as epic-sized, promote it into this index as a new milestone.

---

## Executive Summary

**What it is.** Food You is a free, open-source, privacy-focused food diary and calorie/nutrition
tracker built with Kotlin Multiplatform and Compose Multiplatform. Android is the shipping platform
(Google Play alternative-free: distributed via F-Droid and GitHub releases); iOS targets exist in the
build but Android is primary. Current upstream version: 3.4.9 (GPL-3.0).

**Creator.** The upstream project is created and maintained by Mateusz Maksimowicz
([maksimowiczm](https://github.com/maksimowiczm) on GitHub). This repository is Jarryd Adaens's fork
of it.

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
- **Minimal scope.** Food logging done well. Adjacent trackers (exercise, water, social) are
  intentionally out of scope.
- **Common code first.** Logic and UI live in `commonMain`; platform-specific code is a thin
  `expect`/`actual` and infrastructure layer.

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

- Open Food Facts — opt-in remote food product database (community open data)
- USDA FoodData Central — opt-in remote food composition database (user-supplied API key)
- Swiss Food Composition Database — opt-in imported food composition data
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

---

## Configuration

### Primary Configuration

- `gradle/libs.versions.toml` — version catalog: app version (3.4.9 / versionCode 123), SDK levels
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

None in the repository. The only credential in the system is the optional USDA FoodData Central API
key, which the user enters in-app and which is stored on-device. Release signing is the app
distributor's concern (upstream signs F-Droid/GitHub releases; this fork uses debug signing
locally).

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
- The only secret is the user's optional USDA API key, stored on-device.
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
