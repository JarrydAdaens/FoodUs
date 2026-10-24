---
name: milestone-b
description: Milestone B - Bugs. Ten reported FoodUs defects and UX improvements from the October 2026 app review.
metadata:
  version: "1.0"
  owner: "Jarryd Adaens"
  repo: "FoodUs (fork of maksimowiczm/FoodYou)"
---
# Milestone B: Bugs

Related: [Design's Milestones Index](../design.md#milestones-index),
[milestone guidance](README-MILESTONES.md), [versioning policy](../versioning-policy.md).

## Intent

Make the reported onboarding, AI scan-to-diary, Quick Add, release-history, and credential
editing workflows reliable and understandable for the household's daily use.

## Scope authority and status ownership

The owner supplied the FoodUs bug backlog and requested Milestone B on 2026-10-24. No
Milestone B existed. All ten supplied IDs, `FOODUS-001` through `FOODUS-010`, are selected
here without renumbering. Although called Bugs, this batch includes the requested UX
improvements and the multi-food scanner enhancement. It is a finite review batch;
new unrelated reports go through normal backlog selection rather than silently expanding it.

The [original intake](../dictations-tier-0/2026-10-24_foodus-bug-backlog.md) is an exact-byte
copy of the supplied `FoodUs_Bug_Backlog.md`, preserving reports, reproduction steps,
examples, and uncertainty. Its original status text is intake evidence. **This milestone
owns execution status** for these IDs; do not maintain another live tracker in the raw
intake or backlog. A Story's index row and detailed status must agree. Existing
Milestones 1-3 and their completed Story evidence remain unchanged.

## Status

**In Progress.** Sequential execution started 2026-10-24 with Samsung device checks
and a completion commit required before each next Story. Dependency order is
001, 002, 003, 005, 004, 006, 007, 008, 009, 010. Execution policy and ownership:
[run briefing](../_boss-runs/milestone-b-sequential/briefing.md). Plans and evidence
live under `context/implementation-plans/milestone-b/`; this milestone owns status.

Milestone B is supplementary, not a new numbered milestone or a product generation.
Its closed Stories are added to the current numbered milestone's closed Stories for
the StoriesClosed version component. B's cumulative contribution carries forward
when the numbered milestone advances; the [versioning policy](../versioning-policy.md#reopened-and-supplementary-stories)
owns counting, deduplication, reopening, and release rules. Planning this milestone
closes no Stories and changes no app version.

## Outcome / Definition of Done

- Each selected Story meets its acceptance criteria with recorded regression evidence;
  runtime UI claims have matching emulator/device evidence. A blocked or abandoned item
  does not silently satisfy its requested behavior; reconcile the accepted scope explicitly.
- The accepted scan values survive review, editing, saving, and reading the diary entry.
  Single-food and selected multi-food flows finish without lost fields or duplicate entries.
- Existing manual Quick Add, historical diary snapshots, product/recipe promotion,
  provider configuration, and upstream attribution remain functional and intact.
- API-key visibility is deliberately controlled and defaults to masked. Tests, plans,
  screenshots, and logs use dummy secrets and synthetic food data.
- FoodUs release history uses verified facts and the fork's version identity, with no
  draft placeholders represented as shipped releases.

## Story Index

The IDs below are stable. Priority does not create a prerequisite; the dependency
column distinguishes required sequencing from independent work.

| ID | Status | Story | Type | Depends on / readiness |
| --- | --- | --- | --- | --- |
| FOODUS-001 | Complete | [AFCD in onboarding](#foodus-001) | Missing functionality | Samsung acceptance passed; completion receipt linked below |
| FOODUS-002 | Complete | [External databases in main Settings](#foodus-002) | UX | Samsung acceptance passed; completion receipt linked below |
| FOODUS-003 | Complete | [Visible scanner hint with keyboard](#foodus-003) | Bug | Normal/compact Samsung keyboard acceptance passed |
| FOODUS-004 | Complete | [Review and select multiple scanned foods](#foodus-004) | Enhancement | Samsung selection/queue acceptance and FOODUS-005 regressions passed |
| FOODUS-005 | Complete | [Preserve scan data through Quick Add](#foodus-005) | Data integrity bug | Samsung data-preservation acceptance passed; receipt linked below |
| FOODUS-006 | Not Started | [Visible Quick Add conversion actions](#foodus-006) | UX | No hard B prerequisite; integrate with FOODUS-004/005/007 |
| FOODUS-007 | Not Started | [Finish scan workflow after successful save](#foodus-007) | Navigation bug | FOODUS-005 and FOODUS-004 for final single/multi-item completion proof |
| FOODUS-008 | Not Started | [Retake without discarding the prior photo](#foodus-008) | Workflow bug | No B prerequisite; reuse existing capture mechanism |
| FOODUS-009 | Not Started | [FoodUs-specific What's New](#foodus-009) | Content bug | Actual release facts unverified; content acceptance blocked pending ledger verification |
| FOODUS-010 | Not Started | [Mask AI API key by default](#foodus-010) | Privacy UX bug | No B prerequisite; isolated settings correction |

## Stories

<a id="foodus-001"></a>

### FOODUS-001: Include Australian Food Composition Database in onboarding

**Status:** Complete (2026-10-24; completion provisional until receipt reconciles)

**Delivery:** [Plan and acceptance evidence](../implementation-plans/milestone-b/foodus-001/plan.md),
[completion receipt](../_boss-runs/milestone-b-sequential/foodus-001-receipt.json).
Debug assemble, all 120 unit tests, and both isolated onboarding tests on Samsung
SM-A057F / Android 15 passed. Existing AFCD management/import implementation is reused;
device fixtures simulate import success and failure without changing saved app data.

**Reported problem / value:** A first-time user cannot choose the Australian Food
Composition Database (AFCD) alongside other food sources during setup.

**Acceptance:**

- First-run food-source selection offers AFCD and allows users to complete its setup.
- Availability, configuration, import, and failure behavior agree with the existing
  post-onboarding AFCD management flow; reuse its implementation rather than fork it.
- Existing source choices and skipping onboarding continue to work. Selecting AFCD
  produces the same usable source after onboarding as configuring it later.

**Investigation / ownership:** Onboarding selection state, onboarding completion, and
integration with the existing AFCD setup. [FoodDatabaseScreen.kt](../../app/src/commonMain/kotlin/com/maksimowiczm/foodyou/app/ui/onboarding/FoodDatabaseScreen.kt)
lists OFF, USDA, and Swiss FCD; AFCD already has a separate management/import flow.
[Milestone 2 Story 15](milestone-2.md#story-15) remains completed provider delivery,
not a new prerequisite or a duplicate implementation Story.

**Checks:** Selection/state and setup integration coverage; fresh first-run emulator
flow, AFCD setup success/failure, and regression of existing choices and skip.

<a id="foodus-002"></a>

### FOODUS-002: Move external database navigation to main Settings

**Status:** Complete (2026-10-24; completion provisional until receipt reconciles)

**Delivery:** [Plan and evidence](../implementation-plans/milestone-b/foodus-002/plan.md),
[completion receipt](../_boss-runs/milestone-b-sequential/foodus-002-receipt.json).
Debug/test APK builds and Samsung Android 15 navigation regression passed. The
production route callback was inspected; device fixtures preserve saved app data.

**Reported problem / value:** External database management is buried under Settings
then Database; the owner wants the existing action directly on main Settings.

**Acceptance:**

- Main Settings exposes the entry and opens the existing external database screens.
- Remove the obsolete nested entry from Database; do not create duplicate navigation.
- Underlying configuration and external database behavior remain unchanged. Database
  import/export/backup actions remain available and Back navigation is coherent.

**Investigation / ownership:** Main Settings, Database settings, and their route wiring.
[DatabaseSettingsScreen.kt](../../app/src/commonMain/kotlin/com/maksimowiczm/foodyou/app/ui/database/master/DatabaseSettingsScreen.kt)
currently owns `ExternalDatabasesSettingsListItem`. This is a relocation, not a
database-management redesign. It is distinct from the older Settings back-stack
report in [backlog Story 8](../backlog/backlog-1.md#story-8).

**Checks:** Navigation regression coverage and emulator proof of the new route,
removed duplicate, Back behavior, and retained database actions.

<a id="foodus-003"></a>

### FOODUS-003: Keep the AI Scanner hint visible above the keyboard

**Status:** Complete (2026-10-24; completion provisional until receipt reconciles)

**Delivery:** [Plan and evidence](../implementation-plans/milestone-b/foodus-003/plan.md),
[completion receipt](../_boss-runs/milestone-b-sequential/foodus-003-receipt.json).
Debug/test APK builds, independent static review, and both keyboard tests at each
Samsung viewport (1080x2400 and 900x1600) passed. Six synthetic screenshots confirm
active last-line and earlier-line visibility. Original display settings restored.

**Reported problem / value:** The keyboard covers the optional hint and entered text.

**Acceptance:**

- Focusing the hint brings it into view above the keyboard and keeps typed text visible.
- Users can scroll to and interact with it while the keyboard is open.
- Dismissing the keyboard restores a usable layout without hidden content or excess space.
- Verify different screen sizes and short/long text; keep other scanner controls reachable.

**Investigation / ownership:** Scanner focus, scrolling, window resizing, and keyboard
insets. [AiScanScreen.kt](../../app/src/commonMain/kotlin/com/maksimowiczm/foodyou/app/ui/food/diary/aiscan/AiScanScreen.kt)
has a scrolling Column; runtime inspection must determine which inset/focus change is
needed. The presence of scrolling alone does not resolve the report.

**Checks:** Focus/layout regression where supported and emulator/device proof with
keyboard shown/dismissed on compact and larger viewports.

<a id="foodus-004"></a>

### FOODUS-004: Review and select multiple foods from an AI photo scan

**Status:** Complete (2026-10-24; completion provisional until receipt reconciles)

**Delivery:** [Plan and evidence](../implementation-plans/milestone-b/foodus-004/plan.md),
[completion receipt](../_boss-runs/milestone-b-sequential/foodus-004-receipt.json).
All 140 unit tests, debug/test APK builds, independent integrity review and eight
Samsung Android15 tests passed: four new collection/selection/cancel/retry/count
cases and all four FOODUS-005 preservation regressions. Runtime uses synthetic
images and real in-memory Room; the owner's diary/settings are preserved.

**Reported problem / value:** A mixed plate is collapsed into one structured result,
preventing inspection and choice for separable foods.

**Acceptance:**

- The result contract supports a collection, including one coherent food such as a pie
  and several distinct foods such as breads, pasta, and chicken.
- Users can inspect individual results and select/deselect what to keep. Do not require
  ingredient-level decomposition of a coherent dish.
- Per-item nutrition and portion meaning remain clear; accepted items reach the entry
  flow without silent merging, loss, or duplicate saves. Empty/unusable results are handled.
- Preserve the existing single-food case and the FOODUS-005 field-preservation contract.

**Depends on:** FOODUS-005 establishes a corrected item payload and portion/energy
semantics before expanding it to collections. Before implementation, record the
smallest coherent selection and handoff design, including per-item edit/save, cancel,
and partial failure. The request does not authorize a general split/merge recipe editor.

**Investigation / ownership:** AI prompt/response parser, estimate/result model,
scanner review/selection, and handoff into the existing food-entry flow.
[AiFoodEstimate.kt](../../app/src/commonMain/kotlin/com/maksimowiczm/foodyou/ai/domain/AiFoodEstimate.kt),
the parser, and scanner Result currently represent one estimate. Do not attribute the
single-item restriction solely to the external model without checking this local contract.

**Checks:** Deterministic parser/model and selection/handoff coverage with synthetic
single, multiple, empty, malformed, and partially invalid responses; runtime inspection,
deselection, cancellation, and exact saved-entry count. Avoid live paid AI calls as the
only regression evidence.

<a id="foodus-005"></a>

### FOODUS-005: Preserve accepted scan data through Quick Add and saving

**Status:** Complete (2026-10-24; completion provisional until receipt reconciles)

**Delivery:** [Plan and acceptance evidence](../implementation-plans/milestone-b/foodus-005/plan.md),
[completion receipt](../_boss-runs/milestone-b-sequential/foodus-005-receipt.json).
All 128 unit tests, debug/test APK builds, independent integrity review, and four
isolated Samsung Android 15 tests passed. The exact 780 kcal sample, optional
nutrients and units, unknown energy, direct manual behavior, and historical Room
readback are covered without changing the owner's stored diary or settings.

**Reported problem / value:** Data is dropped or changed between scan review and Quick
Add. The recorded case changed **780 kcal to 482 kcal**, retained **35 g protein / 38 g
fat**, and lost **6 g fibre**; **12 g sugar** was shown at review but its next-screen
value was not confirmed. Earlier description, weight, and carbohydrate values were
not fully observed, so do not fabricate them for that sample.

**Acceptance:**

- Preserve the accepted name and every available supported field through response,
  review, route payload, form, persistence, and diary readback.
- Populate description, serving/weight, energy, macros, fibre, sugar, and other supported
  nutrients when supplied; distinguish unknown fields from real zeros or invented defaults.
- For the same portion and units, values agree across screens and the saved entry,
  allowing only correct explicit conversion/rounding and deliberate user edits.
- The sample's explicit 780 kcal must not become 482 because a missing carbohydrate
  value triggers automatic recalculation. Preserve normal manual Quick Add behavior
  and its deliberate energy calculation controls.
- Automated regression covers that exact observed sample, optional fields present/absent,
  units and portion handling, and save/readback. Do not scale historical diary totals.

**Investigation / ownership:** End-to-end scan-to-entry mapping, energy form initialization,
and any necessary representation/persistence support. [AiScanScreen.kt](../../app/src/commonMain/kotlin/com/maksimowiczm/foodyou/app/ui/food/diary/aiscan/AiScanScreen.kt)
passes only name/calories/protein/fat. [CreateQuickAddScreen.kt](../../app/src/commonMain/kotlin/com/maksimowiczm/foodyou/app/ui/food/diary/quickadd/CreateQuickAddScreen.kt)
does not prefill the expanded optional fields. [QuickAddFormState.kt](../../app/src/commonMain/kotlin/com/maksimowiczm/foodyou/app/ui/food/diary/quickadd/QuickAddFormState.kt)
enables automatic energy calculation if an input macro is absent; 35×4 + 38×9 equals
482 kcal. This is a source-supported lead, not a runtime-confirmed root cause.

Preserve [Milestone 2 Story 18](milestone-2.md#story-18)'s servings/weight-as-metadata and
historical-snapshot contracts. Any needed schema change requires its own migration
evidence and recovery in the implementation plan.

**Checks:** Mapping/form/persistence regressions plus emulator/device comparison of
review → Quick Add → saved diary using synthetic data, including unit changes and
direct manual entry. Establish this correction before the multi-item expansion.

<a id="foodus-006"></a>

### FOODUS-006: Show Quick Add conversion actions as visible buttons

**Status:** Not Started

**Reported problem / value:** Convert to Product and Convert to Recipe are hidden in
the ellipsis menu despite Save being directly visible.

**Acceptance:**

- Clearly labelled, visible buttons expose both conversion actions without opening an
  overflow menu. Save remains available; remove redundant menu entries.
- Use a layout that stays accessible on smaller displays without clipping or overlap;
  all three actions need not be forced into the toolbar.
- Apply consistently to direct and scan-origin Quick Add and its shared edit/historical
  variants. Preserve promotion behavior, cancellation, and original diary snapshots.

**Investigation / ownership:** Shared [QuickAddScreen.kt](../../app/src/commonMain/kotlin/com/maksimowiczm/foodyou/app/ui/food/diary/quickadd/QuickAddScreen.kt)
currently places both promotions in DropdownMenu. Preserve [Milestone 2 Story 19](milestone-2.md#story-19)'s
existing product/recipe mapping and cancellation contracts. No hard dependency on
FOODUS-004/005, but final scan-origin regression runs against their completed flow.

**Checks:** Action visibility/accessibility and existing promotion regressions; runtime
direct/scan/create/edit/historical paths, small-screen layout, Save, and cancellation.

<a id="foodus-007"></a>

### FOODUS-007: Finish the AI scan flow after a successful Quick Add save

**Status:** Not Started

**Reported problem / value:** Saving a scan-derived entry returns to the scanner rather
than the main dashboard, despite completing the operation.

**Acceptance:**

- Persist the intended entry before completing navigation. Failed saves retain an
  editable state and feedback instead of pretending success.
- After successful completion of a scan-origin add, return to the main dashboard
  (the current Log/home destination); Back must not reopen the completed scan flow.
- Respect FOODUS-004's accepted-item flow: do not discard remaining selected items or
  return home prematurely, and do not duplicate already saved entries.
- Test direct Quick Add separately and retain its existing navigation behavior unless
  a further change is explicitly agreed.

**Depends on:** FOODUS-005 and the FOODUS-004 handoff/selection contract for final closure.
Single-item route investigation can start earlier; that alone does not close this Story.

**Investigation / ownership:** Scan-origin route context, successful-save event, and
back-stack completion. [FoodYouAppNavHost.kt](../../app/src/commonMain/kotlin/com/maksimowiczm/foodyou/app/navigation/FoodYouAppNavHost.kt)
currently pops only CreateQuickAdd on save, leaving its scanner predecessor available.

**Checks:** Success/failure/back-stack regressions for single and multiple selected
items, direct Quick Add isolation, and runtime proof of dashboard return and Back behavior.

<a id="foodus-008"></a>

### FOODUS-008: Launch retake directly and retain the old photo on cancel

**Status:** Not Started

**Reported problem / value:** The preview's retake action discards the image and makes
the user manually launch the camera again.

**Acceptance:**

- Tapping the preview launches the existing camera/capture flow without an extra
  manual camera-launch step. A successful capture replaces the preview.
- Cancel, permission denial, or camera error retain the previous usable image.
- Reuse existing acquisition; verify gallery-origin preview behavior where it shares
  the interaction rather than inventing a separate capture mechanism.

**Investigation / ownership:** Android camera launcher and scanner photo state.
[AiScanCameraSection.android.kt](../../app/src/androidMain/kotlin/com/maksimowiczm/foodyou/app/ui/food/diary/aiscan/AiScanCameraSection.android.kt)
currently routes preview confirmation to discard; AiScanViewModel resets to NoPhoto.
Preserve the prior image until a replacement is actually obtained.

**Checks:** State/callback coverage for success/cancel/error and emulator/device camera
permission, retake, cancellation, and gallery-origin preview paths.

<a id="foodus-009"></a>

### FOODUS-009: Show verified FoodUs history in What's New

**Status:** Not Started — release-content verification outstanding

**Reported problem / value:** About → What's New presents upstream Food You notes.
The owner requested three initial FoodUs entries: the fork/1.0 announcement, the
previous release, and the current release, newest first.

**Acceptance:**

- Present those three FoodUs-specific entries in reverse chronological order using
  confirmed version labels, dates, and shipped change summaries.
- Verify the historical fork/1.0 label against project records; do not invent a FoodUs
  release tag from an announcement or an interim app identity.
- Resolve previous/current release facts from release records and commits before
  content acceptance. The source's TBD versions and generic draft prose must not ship.
- Use the existing release-notes mechanism so future entries remain maintainable.
  Current-release lookup follows the fork's shared version, including its transition.
- Preserve required upstream licensing, credits, and attribution elsewhere.

**Prerequisite / owner:** The implementer verifies release history; the project owner
resolves any remaining factual ambiguity. This blocks final content acceptance, not
investigation of the existing changelog implementation. The local catalog alone
does not prove that a build was published.

**Investigation / ownership:** [StaticChangelog.kt](../../app/src/commonMain/kotlin/com/maksimowiczm/foodyou/changelog/infrastructure/StaticChangelog.kt)
contains upstream entries and matches currentVersion against `appConfig.versionName`
(upstream attribution). Check both About's full history and automatic What's New
selection. [Milestone 2 Story 4](milestone-2.md#story-4) explicitly left What's New
untouched, so this is follow-up work rather than retroactively reopening that Story.

**Checks:** Version/current-entry lookup, newest-first ordering, exactly three verified
initial entries, no placeholders, and runtime About/What's New with upstream credit intact.

<a id="foodus-010"></a>

### FOODUS-010: Mask the AI provider API key by default

**Status:** Not Started

**Reported problem / value:** Opening AI Settings exposes the key. The owner requests
masked display with a deliberate reveal/hide control that does not retain visibility.

**Acceptance:**

- Opening or re-entering AI Settings starts masked, including after app/screen recreation.
- The eye control explicitly reveals and hides the key; prior reveal state must not
  restore a visible secret after leaving or recreating the screen.
- Editing, pasting, validation, and saving continue to work. Do not alter storage or
  provider precedence as part of this display correction.
- Use dummy keys for all tests and evidence; never print or capture real credentials.

**Investigation / ownership:** [AiSettingsScreen.kt](../../app/src/commonMain/kotlin/com/maksimowiczm/foodyou/app/ui/settings/ai/AiSettingsScreen.kt)
currently renders the key through generic AiField/OutlinedTextField with no masking
or eye control in this checkout. This differs from the reported eye icon; confirm the
tested build and add/reuse the control as needed to deliver the requested behavior.

**Checks:** Mask/reveal/hide, re-entry, recreation, editing/paste/save regressions and
runtime evidence with a dummy key. Storage-system redesign remains outside scope.

## Interdependency Order and execution selection

Required integration chain: **FOODUS-005 → FOODUS-004 → FOODUS-007**, with FOODUS-007
also directly retaining FOODUS-005's data-preservation acceptance. FOODUS-009 has a
release-fact verification gate. No cyclic dependency or foodus-relay delivery wait
was identified for this batch.

Suggested sequential order within those constraints:

1. FOODUS-010: bounded credential-display correction.
2. FOODUS-005: investigate and fix the data-loss/energy mismatch before expanding scans.
3. FOODUS-003 and FOODUS-008: scanner interaction corrections, one at a time.
4. FOODUS-001 and FOODUS-002: onboarding/source access corrections.
5. FOODUS-006: shared Quick Add action visibility.
6. FOODUS-004: collection/selection behavior on the corrected payload contract.
7. FOODUS-007: final scan-flow completion and combined single/multi-item regression.
8. FOODUS-009: verified release content when its factual prerequisite is satisfied;
   release-history investigation can proceed earlier.

This is a selection order, not authorization to begin implementation. Blocked prerequisites
are not ready execution work. Default to sequential changes in scanner/Quick Add/navigation
because several Stories touch the same files. If work is later delegated in parallel,
assign disjoint files explicitly; one owner must coordinate shared route/model changes.
The isolated AI Settings correction is independent of the scanner chain, but independence
does not grant overlapping ownership of shared infrastructure.

## Verification and delivery assumptions

Keep cross-story regression with the implementing Stories and their plans:

- Scan → review → Quick Add → persistence → dashboard: FOODUS-004/005/007, including
  single/multiple selection, missing data, conversion, cancel, failed save, and no duplicates.
- Shared Quick Add: FOODUS-006 from direct/scan/create/edit/historical entry paths,
  preserving existing diary snapshots and promotion outcomes.
- Scanner interaction: FOODUS-003/008 across compact/larger screens, keyboard, camera
  permission, successful retake, and cancellation without losing the original image.
- Provider setup/navigation: FOODUS-001/002, fresh onboarding and existing configuration.

The project harness has no installed modules. Select meaningful automated checks and
device/emulator evidence per Story; use the documented JDK 21 Gradle checks when scope
warrants. No app build, live AI call, camera run, or device test was performed in this
planning task, and none of the reports is claimed runtime-reproduced here.

The effort range spans bounded single-screen corrections through a coordinated
response-model/route/form/persistence change. The critical integration path is
FOODUS-005 → FOODUS-004 → FOODUS-007; the unresolved multi-item handoff and any schema
requirements dominate its uncertainty. Release-ledger verification can block FOODUS-009
independently; obtaining representative devices or provider setup fixtures may add waiting.
There is no measured throughput or reproduced runtime baseline from which to give a
defensible duration range. Separate implementation effort from those waits and forecast
elapsed delivery after the first mapping investigation and multi-item design decision.

## Deferred work and next planning step

- No supplied ID is dropped or moved back to backlog. FOODUS-009's final content remains
  gated within this milestone until its release facts are verified.
- Unrelated [backlog work](../backlog/backlog-1.md), relay implementation, credential
  storage redesign, and a general food split/merge editor are not selected here.
- On B closure or reopening, maintain the combined current-numbered-milestone plus B
  count under the versioning policy; do not interpret B as a numeric version component.
- Next implementation planning: select FOODUS-010 and FOODUS-005, confirm runtime
  reproduction and affected tests, then create only the useful implementation plans.

## Refinement evidence

Planning source baseline: commit `9f24fe3aea2f417886e090c60e19d607f7554c06`, inspected
2026-10-24. Intake SHA-256:
`8978fbff2dcae05526b42d90ba15d0d8622ac2bb33c7c72a3741bc56ff45d16f`.
Static observations above are distinguished from reported behavior and final root causes.
Preserve the raw intake and historical numbered milestone paths when refining this batch.

Planning validation on 2026-10-24: all ten stable IDs and Not Started index/detail
statuses agree; the prerequisite order is acyclic; 68 local links/anchors resolve;
the intake copy matches the attachment byte for byte; all 1,131 protected tracked
files match their starting hashes; `git diff --check` passes. Independent evaluator
`/root/context_policy_review` returned PASS with no findings, including the generic
supplementary-version mapping rule. These are planning/structure checks, not evidence
of runtime bug reproduction or fixes. No implementation or commit was performed.

Owner clarification on 2026-10-24 supersedes the initial unmapped treatment: StoriesClosed
includes closed Stories in the current numbered milestone plus Milestone B's cumulative
closed Stories. The mapping is now adopted in the versioning policy. No Story status,
acceptance criterion, dependency, or app-version value changes with this clarification.

Commit preparation found three intentional Markdown hard breaks (two trailing spaces)
in lines 3-5 of the exact-byte intake header. Preserve these original bytes. All other
staged files pass the whitespace check; the intake has no other trailing-whitespace
findings. This is a documented source-format exception, not an application change.
