---
name: story-39-diary-slot-bulk-actions-plan
description: Atomic diary-slot delete, move and copy implementation and acceptance plan.
metadata:
  version: "1.1"
  owner: "Jarryd Adaens"
  milestone: 2
  story: 39
---

# Plan: Diary-slot bulk delete, move and copy actions

Status: Complete (2026-10-26)
Authority: Owner's 2026-10-26 request to plan Story 39 with one sub-agent, implement it with a different sub-agent, then use `commit-log --commit`.
Status owner: [Milestone 2 Story 39](../../../milestones/milestone-2.md#story-39). The milestone owns Story status; this plan owns implementation steps and acceptance evidence.

## Outcome and scope

Add **Delete All**, **Move to...**, **Copy to...**, and **Copy From...** to the diary-slot ellipsis menu. Preserve Save Meal and Load Meal. Operations act on all entry kinds in a specific displayed date plus configured DiarySlot. The other date, slot, or both can change. Copy From works into an empty current slot. Move/Copy append after destination entries, retaining source order. Delete All confirms the slot, date, and count. Selecting the identical source/destination, an empty source, or a deleted slot cannot submit.

The selectors display both endpoints and source count before submission. Dismissal/cancellation performs no write. A recoverable error leaves data intact. New copies have independent entry and mutable snapshot identities. Moves preserve entry identity and recorded facts. All supported fields, unknowns, quantity/context metadata, notes, placeholder state, and Recipe calculation versions survive without recalculation or live-definition lookups. Affected summaries refresh through existing Room-backed flows.

Scope includes domain/repository contracts, atomic Room mutation and snapshot cloning, the minimum persistent ordering needed to fulfill append semantics, UI state and selectors, localization resources, migration/regression tests, runtime inspection names, and Story closure/version reconciliation when evidence permits. It excludes Story 38, library redesign, OCR, relay, remote services, new dependencies, per-item bulk selection, undo, and release/distribution. Existing dirty work must remain intact. Never pull or push. Only the coordinator performs the explicitly requested final commit.

## Current understanding

- [Design](../../../design.md) owns privacy and Product/Recipe/Meal/DiarySlot/DiaryEntry distinctions; [laws](../../../laws.md) require domain/presentation separation, boundary validation, meaningful regressions, and preservation of unrelated work. [Workflow](../../../agenticworkflow.md) keeps Story status in the milestone and execution evidence here.
- UI ownership is `app/ui/home/meals/card/DiarySlotCard.kt` (`MealMenu`), `DiarySlotsCards.kt`, `DiarySlotsCardsViewModel.kt`, `HorizontalDiarySlotsCards.kt`, and `VerticalDiarySlotsCards.kt`. `HomeViewModel` only owns card order; it is not the diary mutation owner. `DiarySlotsCardsViewModel.dateState` is mutable: new commands must capture the opened menu's date/slot rather than consulting a later date value at submission. Both layouts must forward the same callbacks and immutable context.
- Current `MealMenu` has Save/Load actions and already uses `UiSourceScope` for popup ownership. Existing Save/Load behavior must remain usable. Entry rendering uses IDs from two distinct tables: keys must include entry kind so food ID 1 and manual ID 1 do not collide.
- `DiaryEntry` has two concrete kinds. `NutritionalItemDiaryEntry` references private Product or Recipe snapshots through `Measurement`. Quick Add, loaded Meal items, and unresolved placeholders use `ManualDiaryEntry`; their absolute nutrients and descriptive context must remain unchanged. Manual rows include `capturedPortionType/value`, `portionDescription`, and `nutritionMetadata` from Stories 24/28.
- The live schema is **42**. Storage uses historical `meal`/`mealId`, `Measurement`, `ManualDiaryEntry`, `DiaryProduct`, `DiaryRecipe`, `DiaryRecipeIngredient`, and `DiaryReportedRecipeComponent` names. Product snapshots include basis, reference portion, source, and metadata. Recipes include calculation versions 0/1/2, nullable servings, nested derived ingredients, or reported panels/components. Historical schema exports 39–42 must remain immutable.
- `MeasurementDao` and `ManualDiaryEntryDao` provide per-table queries/inserts/updates; current nutrient-entry updates delete/recreate snapshots. Do not implement Move by that update method: it changes more than placement. Do not copy using saved Meal capture or conversion to manual rows: that loses entry kind, snapshot structure, and placeholder semantics. Do not reuse snapshot IDs across copied entries: subsequent edits can delete a shared snapshot graph.
- `FoodYouDatabase` exposes `TransactionProvider`, `useWriterConnection`/`immediateTransaction`, and registered migrations. Existing atomic Meal flows demonstrate the established transaction pattern. A focused diary bulk repository can own the raw cross-table transaction; domain types/use cases expose intent/errors without Room dependencies. Keep persistence work outside composables/ViewModels.
- **Ordering gap:** `ObserveDiarySlotEntriesUseCase` currently merges manual/food entries and uses `.sortedBy { it.name }`. Neither diary table has persisted cross-kind ordering. Merely inserting rows in a loop cannot fulfill destination append/source-order acceptance, particularly after restart. Ordering persistence and projection changes are required.
- Available tests include common `MealUseCasesTest` and Android real-Room `MealPersistenceTest`, Recipe/Product persistence tests, canonical upgrade tests, and actual-screen `MealsWorkflowTest` with isolated Koin composition. These are infrastructure/patterns, not Story 39 evidence. The only installed relevant harness is [UI name coverage](../../../../harness/ui-name-coverage/README.md), supplemented by actual runtime inspection selection.

## Decisions and contracts

### Placement and ordering

Represent each endpoint explicitly as `(LocalDate, DiarySlotId)`, not a slot label or today's date. Validate configured slot existence inside the transaction immediately before writing. Same endpoint is rejected for transfers; Delete has only one endpoint.

Use a minimal additive schema **42 → 43** with nullable persisted ordering on both diary entry tables, or an equally small explicitly documented equivalent if implementation evidence favors it. No drag/reorder UI is requested. Preserve legacy alphabetic ordering for untouched collections. On first mutation of an unordered source/destination collection, materialize its current deterministic cross-kind order before assigning append positions. Define a stable typed-ID tiebreak for equal names; do not rely on unspecified SQL ordering. The common diary projection must use the same ordering function as preview and mutation.

Destination existing entries retain relative order; transferred/copied entries receive successive destination positions in source order. Move retains original entry IDs; Copy creates new IDs. Ordinary entry insertions into an explicitly ordered collection must append coherently, and edits retain position. Cover Quick Add/placeholder insertion, Product/Recipe logging, Apply Meal, and Recipe unpack/replacement so those paths do not erase bulk order. Nullable legacy columns allow an additive migration without rewriting historical nutrition or guessing original timestamps. Do not repurpose `createdAt` to fake order. Include relevant native-backup/schema behavior in migration acceptance.

### Atomic preparation and submission

Add a focused diary bulk repository interface and domain request/preview/result types under `fooddiary/domain`, implemented in `fooddiary/infrastructure/repository`, wired through existing Koin domain/infrastructure modules. One database writer transaction performs source/destination validation, ordering updates, the entire mutation, and any owned snapshot cleanup. No outer per-entry transactions in the ViewModel and no independent manual/food commits.

Prepare an immutable source snapshot/version for the preview. On submit re-read the actual source in the writer transaction and compare with the prepared version, including typed identities, placement, quantities, notes/placeholder fields, and nested snapshot facts/context. Reject stale additions/deletions/edits/moves and require refresh/reconfirmation. An entry-ID list or second-resolution `updatedAt` alone cannot detect all edits. A preparation token may remain opaque to presentation; do not log diary content. For Copy From, prepare/re-prepare against the selected remote source as its selectors change. Added destination entries may remain and precede the append; a deleted destination slot fails validation. Renames/reconfiguration must not silently redirect by name.

Capture both endpoints, expected source version, and action in one dialog/session request. A date change in Home must never retarget it; dismiss/reset obsolete menu state or retain explicit original endpoints visibly. Late preview responses must not overwrite a newer source selection. Guard repeated submit synchronously before launching a coroutine; disable confirm while running and consume successful session requests. Retry after a failed transaction uses refreshed preview. A separately opened copy is a deliberate new action, not a duplicate. No persistent operation ledger or background retry is required. Propagate coroutine cancellation correctly; never swallow it as a normal error or automatically replay a possibly committed copy.

### Exact snapshot integrity

Move updates placement/order only, preserving stored snapshots and entry identities. Copy clones raw stored rows/graphs without running nutrition normalization, rescaling, or definition lookup. Manual copies preserve every flat stored field except new ID/placement/order and any explicitly documented operation timestamp policy. Product/Recipe copies create fresh independent root and descendant IDs and preserve all scalar facts, metadata, nullable fields, ingredient measurements/order, reported panels/components, and calculation versions. Preserve existing recorded timestamps unless a necessary update timestamp policy is explicitly recorded and tested.

Use focused suspend DAO reads for transaction preparation/cloning instead of assembling a changing collection of reactive flows mid-write. Reuse/extract existing snapshot persistence helpers only where they preserve raw facts; current Product insert normalization must not erase historical incomplete numeric values. Reject missing/corrupt snapshot dependencies without partial writes. Bound or detect cyclic malformed graph traversal. Preserve repeated source graph structure/order without accidentally linking a new copy to the source. Delete only selected entry rows and snapshot nodes proven owned/unreferenced by surviving entries or ingredients; do not perform a global orphan sweep. Never delete configured slots or Product/Recipe/Meal library definitions.

### UI and errors

Keep the exact requested menu labels and existing Save/Load. Delete All, Move to..., and Copy to... are disabled on empty slots; Copy From... remains available. Add a focused selector/confirmation composable instead of expanding the already large card with persistence logic. Reuse existing date and DiarySlot selector patterns (`ChipsDatePicker`, Meals load destination), initializing from the opened date/slot. Both selectors remain visible/editable. Use localized date formatting and clear source/destination/count labels; require explicit confirmation for Delete All.

Show validation failures in the sheet/dialog with a recoverable refresh/retry path; preserve selected endpoints when useful. Distinguish empty source, identical endpoints, stale source/deleted slot, and generic persistence failure without exposing exception SQL or diary details. No write occurs while merely changing selectors or cancelling. Apply accessible labels and `uiName` to new call sites; wrap dialogs, sheets, dropdowns, and nested calendar surfaces with the existing `UiSourceScope` pattern. Runtime evidence must inspect actual popup/dialog source ancestry, not only lexical inventory.

## Dependencies and delivery steps

| Step | Change and independently checkable evidence | Prerequisites | Size / uncertainty |
| --- | --- | --- | --- |
| 1 | Record scoped baseline; add typed endpoint/action/preview/error contracts and shared ordering semantics. Add meaningful common tests for validation/order/state transitions, duplicate sessions, and cancellation. Inspect all direct diary insertion/replacement call sites before choosing order plumbing. | This plan; Stories 25–28 already delivered | Medium; insertion seams discovered but all call sites need final inventory |
| 2 | Add nullable ordering persistence and migration 42→43; preserve legacy ordering until touched. Update entity mappings, creation/edit/replacement paths, and observation ordering. Add real-Room preservation/reopen tests and a registered 42→43 upgrade test. | Step 1 contracts | Medium; schema regeneration and legacy fixture tests can expose hard-coded current version assumptions |
| 3 | Implement prepared-source validation and atomic Delete/Move/Copy persistence across both entry kinds; clone full snapshot graphs independently. Add mixed-entry tests, failure/cancellation rollback, staleness, copied-identity independence, all day/slot combinations, preserved definitions/history, and field-level fidelity. | Steps 1–2 | Large; nested/reported snapshots and partial legacy facts are the main data risk |
| 4 | Add menu actions and selector/confirmation state through both card layouts and ViewModel. Capture opened date/slot, expose pending/errors, handle late previews and duplicate submits, retain Save/Load, localize resources and add inspection names. Add actual-screen workflow tests and relevant ViewModel tests. | Step 3 stable domain contract | Medium; modal lifecycle/date changes and small-screen usability require runtime evidence |
| 5 | Run scoped then complete relevant checks, debug build and Android persistence/UI acceptance on an isolated emulator. Exercise runtime inspection, non-today dates, empty/populated slots, cancel/failure, source/destination totals, and process restart. Obtain independent review against Story acceptance and data boundaries; repair findings and recheck affected paths. | Steps 1–4 | Medium; emulator/native tool availability is an external wait |
| 6 | Record exact acceptance/results/limits here; reconcile milestone index/detail and Design status if closure is earned. Read fresh version trackers and update only the applicable version property under version policy, preserving unrelated catalog edits. Coordinator uses commit-log and commits only session-owned changes; no push/release. | Step 5 evidence and reviewer findings resolved | Small; closure depends on actual results, not code completion |

Critical path is contracts/order → atomic persistence → UI → emulator/review → status/version/commit. UI layout preparation and test-fixture design may proceed after contracts stabilize; do not split the atomic data guarantee into independently shippable partial behavior. No additional phase or sidecar is needed.

## Checks and recovery

Use JDK 21; coordinator identified `C:/Java/jdk-21.0.12+8`. SDK is `D:/dev/AndroidSDK` in `local.properties`. Run commands from the repository root and retain concise sanitized output/evidence paths here:

```powershell
.\gradlew.bat --no-daemon :app:testDebugUnitTest
.\gradlew.bat --no-daemon :app:assembleDebug :app:assembleDebugAndroidTest
.\harness\ui-name-coverage\run.ps1
```

Run focused new unit-test classes during development; the final `testDebugUnitTest` result also protects existing diary/Recipe/Meal/import regressions. Verify existing instrumentation constructor/DI fixtures compile after repository/ViewModel parameter changes. Run new real-Room and actual-screen test classes with `:app:connectedDebugAndroidTest` and Android runner class filtering, **only on the coordinator's isolated emulator-5580** with explicit device selection. If Gradle enumeration could reach another device, use direct `adb -s emulator-5580 shell am instrument` after installing the built debug/test APKs there. Do not operate, uninstall, clear, or seed the owner's physical device. The coordinator owns device/build coordination.

Required automated coverage:

- Mixed Product snapshots (metric and captured reference), nested Recipe calculations 0/1, reported Recipe version 2 with components, manual Quick Add/loaded Meal contexts, and unresolved placeholders. Compare all recorded fields and unknown/incomplete values, including numeric legacy partials, before/after/reopen.
- All three endpoint combinations for Move/Copy/Copy From; initial destination contents and interleaved source kinds; duplicate/equal names; same endpoint and empty-source rejection; new independent identities; edits/deletes on either copy leave the other intact.
- Exact Delete scope and unchanged slot definitions/library/unrelated dates; destination append order after rename/restart and subsequent normal inserts/unpack.
- Stale source addition/deletion/edit/reassignment, selected-slot deletion, preview races/date navigation, duplicate submission, cancellation before commit, and recoverable error/retry behavior.
- Real transaction failure after at least one write, for each mutating operation (for example a test-only SQLite abort trigger on a later affected row). Verify both collections, ordering metadata, cloned graph rows, and summaries roll back; a fake repository assertion alone is insufficient. Do not add production tuning or fault-injection UI.
- Registered 42→43 upgrade, unchanged preexisting IDs/facts/context/definitions, valid foreign keys and matching schema export; retain existing 39–42 exports unchanged. Fresh creation and close/reopen must match migrated schema.

Required runtime workflow evidence: use synthetic data and the actual production menu/selector path for each action from a historical/future diary date. Exercise empty Copy From, Delete cancellation/confirmation, selector defaults, same-endpoint disabled confirmation, each direction combination, existing destination contents, both card layouts, and visible summary changes. Inspect a menu action, selector, calendar, confirmation, and error surface through developer inspection with exact source ownership. Restart the isolated app process and verify data/order/totals again. The harness inventory is supplementary and cannot replace this evidence.

Recovery: keep a scoped diff baseline and synthetic schema42 fixture before migration. Preserve source changes and execution evidence on interruption; resume from this plan. Never use a broad Git reset, destructive migration fallback, or uninstall against an existing diary. Test failures must leave the retained source/destination fixtures intact for diagnosis. Once schema43 is accepted/exported, preserve it and use a forward fix for any later released schema. Do not write private diary values, credentials, relay addresses, or third-party material to evidence. The final commit includes only session-owned changes and leaves unrelated preexisting edits unstaged; split independent Story38 planning changes from Story39 implementation as the coordinator judges appropriate.

## Questions and assumptions

- No product question blocks execution. Source order means the order presented before the action; append order is an explicit acceptance requirement, so schema ordering is justified. The precise nullable ordering helper/API is an implementation choice, not permission to drop that behavior.
- The default selection is the opened menu's date/slot, intentionally disabling transfer confirmation until an endpoint changes. Copy From initially has the same endpoint and likewise requires selecting another source.
- Stale source changes fail and require fresh preview rather than silently including newly arrived entries or acting on a stale count. Concurrent destination additions are preserved and precede the append. Request-scoped duplicate protection covers repeated input within one modal session; no durable exactly-once network job is needed.
- Atomic rollback and snapshot independence require real Room tests. Planning inspected infrastructure but did not run builds/tests or claim behavior complete. Do not close the Story if required checks remain unavailable without documenting their exact limits and actual status.

## Assessment

Complexity is moderate to high because one visible menu spans two entry stores, nested historical snapshot graphs, cross-kind ordering, and stale UI state. Effort is substantial but bounded to a single Story; no server or new library is needed. The inherent consequence is loss or corruption of diary history if mutation/clone/order guarantees fail. The main controls are one writer transaction, immutable prepared sources, exact raw-field copying, additive migration, fault rollback tests, isolated runtime evidence, and independent review. Remaining platform validation is Android; do not claim iOS acceptance from Android-only checks.

## Evidence and execution record

- 2026-10-26 planning: read the invoked `rails-planning` skill and full plan-template reference, repository instructions, laws, workflow, relevant Design/Story sections, version policy, harness index/name contract, current diary domain/Room/UI code, and representative common/Room/actual-screen tests. No prior Story39 plan existed. No implementation, schema, app build, test, or commit was performed by this planning agent.
- Existing unrelated work observed and preserved: `SponsorViewModel.kt`, version catalog, OCR-related Design/milestone/harness edits, untracked OCR dictation and attachment directory. Parent-added Stories38/39 are session-owned context; this plan links only Story39.
- Environment discovery: `local.properties` names SDK `D:/dev/AndroidSDK`; plain shell PATH did not resolve Java. SDK `adb devices -l` failed to start/connect its daemon in the planning sandbox, so device availability is not established by this agent. Coordinator supplied the JDK path and isolated emulator5580 policy and is handling device readiness.
- Coordinator readiness update: escalated SDK discovery confirmed JDK21 and emulator-5580 (`emu64xa`) available, with a physical device also attached. Every runtime command must target emulator-5580 explicitly. Current `fork-version-name` is `3.2.38.3`; a fully accepted Story39 closure yields `3.2.39.3` under the present Milestone2 plus B10 counts. Recount fresh at closure. Preexisting `android-versionCode=134` versus HEAD133 is unrelated dirty work and must be preserved.
- Planning completion: this plan is Ready for the different implementation sub-agent requested by the owner. Story remains Not Started until implementation begins; the Story Index/detail now link this plan. Parent owns final independent review/build/device coordination and the authorized commit.
- Planning handoff (historical): the separate implementation agent took ownership of contracts, ordering, persistence, UI and regressions. Execution and acceptance results follow.

- 2026-10-26 implementation started: baseline preserved all preexisting dirty/untracked work; Story39 and plan marked In Progress. Implemented typed immutable endpoint/preparation contracts, raw graph equality and atomic mutation repository, scoped snapshot cleanup, additive schema43 and automatic migration, persisted ordering plus normal insertion/edit/unpack seams, required Koin dependency wiring, controller session guards, all menu actions and localized selector/error UI. Story38 remains untouched.
- Implementation review repair: shared ChipsDatePicker saver restored today/selection in the wrong order; corrected it and added a non-today save/restore regression. Recipe unpack retains its original ordered entry as an anchor until replacement inserts finish. Expanded menu accessibility now reads Diary slot actions. Production menu and modal remain named and source-scoped.
- Initial compile: `:app:compileDebugKotlinAndroid` passed after resolving transient in-progress file/resource encoding issues. Existing SDK XML/parser and compiler experimental-language warnings remain; unrelated MealsScreen warning remains. Schema43 export contains only nullable default-NULL sortOrder fields on the two diary entry tables; exports39–42 unchanged.
- UI-name coverage PASS: coordinator `harness/ui-name-coverage/runs/20261026T070603599706Z/report.json`; implementation run `20261026T070559403289Z/report.json` also reported 0 unnamed calls and 0 duplicate source identifiers. Runtime long-hold selection checks are included in the new actual-card workflow test, pending emulator execution.
- Added controller/picker regressions and real-Room data/migration/fault rollback tests. All flat nutrient fields and JSON reported facts/context are compared, including numeric incomplete legacy facts. Added a persistent synthetic process-restart seed/verify pair for coordinator-driven force-stop acceptance. Actual workflow tests cover both layouts, historical day, actual Today selector, disabled identical endpoints, source-change error/refresh, empty CopyFrom, cancel/delete and visible energy totals. Full unit/debug/test APK build is running; no final acceptance claimed yet.
- Current next step: resolve any compilation/test failures, run new persistence/workflow suites on explicitly targeted emulator5580, coordinate force-stop restart seed/verify with parent, obtain final independent review and then reconcile Story/version only after acceptance passes.

- Final frozen-source command `:app:testDebugUnitTest :app:assembleDebug :app:assembleDebugAndroidTest` PASS (initial successful build 2m41s; post-edit confirmation 12s,138 tasks). Unit XML: 40 suites,213 tests,0 failures/errors/skips, including five new ordering/controller/picker tests. Debug/test compilation includes existing project deprecation/compiler/SDK warnings; no compile errors remain. The final UI-name inventory PASS is `harness/ui-name-coverage/runs/20261026T071601653063Z/report.json` (0 unnamed/duplicates).
- Emulator installation started with explicit `adb -s emulator-5580 install -r` for the debug and Android-test APKs. Coordinator owns the opt-in `story39RestartPhase=seed` / `verify` sequence with explicit dev-app force-stop between instrumentation processes. Main persistence/workflow and changed legacy suites remain to run; Story stays In Progress.

- Installation completed: both explicit emulator5580 `install -r` calls returned Success. Parent has exclusive runtime control for the process-restart phase; default AndroidJUnitRunner targets `io.github.jarrydadaens.foodus.dev.test` and the opt-in seed/verify methods use only `story39-process-restart.db` plus a synthetic SHA256 receipt.
- Historical `StructuredLibraryCanonicalUpgradeTest` requires its separately staged immutable schema39 main-database fixture and receipt (`story25-before.json`); that fixture-specific test will not be claimed as run here. The new disposable registered42→43 migration test covers this Story's actual additive migration; prior exports remain untouched. Current-version assertions in existing structured-library migration tests now derive from FoodYouDatabase.VERSION instead of hardcoding42.

- First opt-in restart seed launch exposed a JUnit discovery issue before any fixture mutation: the migration test's inferred runBlocking return was Boolean. Corrected all new persistence test wrappers to explicit Unit, rebuilt Android-test APK successfully (48s), and reinstalled only on emulator5580. This is a test declaration repair, not a production change. Also strengthened the migration assertion to compare every original column/value/null across11 diary/snapshot/slot/library tables before/after registered42→43 open, ignoring only the two additive ordering columns.
- New main persistence/migration/transaction suites are running with default restart seed/verify methods deliberately skipped via phase arguments; those two methods run separately under the coordinator's process-restart sequence. Current log: `app/build/reports/story39/persistence.txt` (ignored build evidence).

- Emulator persistence acceptance PASS: `DiaryBulkPersistenceTest`, `MealPersistenceTest`, and `food.StructuredLibraryPersistenceTest` registered20 tests, executed18 successfully, deliberately skipped the2 opt-in restart methods, and reported `OK (20 tests)` in16.377s. Evidence: `app/build/reports/story39/persistence.txt`. New checks passed raw snapshots/known numeric partials, clone identity/deletion independence, source-order append/reopen, normal inserts/Meal/unpack order, every day/slot combination, stale/corrupt/deleted inputs, exact scope and shared-node preservation, all three SQL abort-after-write rollback cases, real-write cancellation rollback, and exact original-column registered42→43 migration preservation. The existing Meal/library persistence regressions also passed.
- Actual-card UI workflow acceptance is running next together with the existing Meals workflow suite; source/test files remain frozen. Coordinator will then repeat the opt-in process-restart seed/force-stop/verify phases on this corrected test APK.

- Actual UI acceptance PASS6/6 in223.272s: both `DiaryBulkWorkflowTest` horizontal/vertical methods and all4 existing `MealsWorkflowTest` methods. Evidence: `app/build/reports/story39/workflow.txt`. The real production card/menu/dialog paths passed on a historical diary date, actual Today day selection, unchanged same-endpoint disabled confirmation, source/destination selection, cancel and Delete All, stale-source error/refresh, Move, Copy and empty-slot Copy From, preserved other-day data, and visible source kcal300→empty→650 refresh. Real3-second holds captured exact menu-action, selector/confirmation, nested calendar and error source names/surfaces/paths in both layouts. Existing Save/Load/editor/summary regression workflows remain usable.
- Device handed exclusively back to coordinator for the final opt-in process-restart sequence; its result and final independent acceptance are recorded below.

### Final acceptance and closure

- Coordinator independently executed `DiaryBulkPersistenceTest#seedProcessRestartFixture`
  with `story39RestartPhase=seed`: PASS 1/1 in 0.843s. Explicitly force-stopped only
  `io.github.jarrydadaens.foodus.dev` on emulator-5580 and checked that no process remained,
  then ran `#verifyProcessRestartFixture` with `story39RestartPhase=verify` in a separate
  instrumentation launch: PASS 1/1 in 0.623s. The full raw database digest, snapshot/entry
  identities, facts, quantities and order matched. Runner termination also ended the target
  process; blank PID readbacks do not represent a retained running process. Evidence:
  `app/build/reports/story39/process-restart-seed.txt`, `process-restart-verify.txt` and
  `process-restart-summary.txt` in that same ignored report directory.
- Final totals: 213 unit tests in 40 suites passed with no failures/errors/skips;
  26 emulator checks executed successfully (18 persistence + 6 workflows + 2 restart
  phases). The ordinary persistence run's two opt-in assumptions were exercised by the
  separate successful phase runs. Debug and Android-test APK builds passed. Final UI-name
  report `harness/ui-name-coverage/runs/20261026T071601653063Z/report.json` passed across
  350 files and 2,815 surveyed calls, with no missing names or duplicate identifiers.
- Independent quality-gate evaluator reviewed source, migration, tests and actual logs:
  PASS WITH NOTES, with no open correctness/privacy/architecture/data-integrity findings.
  Corrected its remaining documentation-only note: empty slots retain their menu for
  Copy From. Earlier date restoration, required dependency wiring, numeric partial facts,
  picker chips, source-scoped energy assertions and opt-in phase independence were repaired
  and verified. No production behavior changed after accepted runtime checks; the last
  source edit corrected a comment.
- Changed scope: diary bulk domain/repository contracts and raw Room transactions;
  nullable ordering columns/schema 43 and normal insertion/unpack order; Home dependency
  wiring, both diary-card layouts, controller/dialog/menu and date-state restoration;
  English resources; controller/Room/UI regressions and current-schema test assertions;
  this plan, Story 39 status and maintained Design. Existing schemas 39–42 remain intact.
- Story 39 is Complete. Milestone 2 now has 29 Complete, 1 Blocked and 9 Not Started
  Stories; Milestone B retains 10 closed Stories. Development catalog version derives as
  `3.2.39.3`; the cumulative ReleaseCount remains 3 and preexisting versionCode 134 stays
  untouched. Story 38 is still Not Started. No release, household install, pull or push.
- Limit: the historical canonical-upgrade test requiring an external staged immutable
  private fixture/receipt was not rerun. Its current-schema assertion compiles; this
  Story's registered synthetic 42→43 test passed exact original-column preservation.
  Runtime evidence is Android on isolated emulator-5580; no iOS or household rollout claim.
- Final metadata build: after closure reconciled the catalog, coordinator ran JDK 21
  `.\gradlew.bat --no-daemon :app:assembleDebug`: PASS in 2m43s (99 tasks; 15 executed).
  Output is `app/build/outputs/apk/debug/foodus-dev-3_2_39_3.apk`, versionCode 134.
  Runtime checks used the preceding `3.2.38.3-dev` candidate with the same accepted
  behavior; only version metadata and the stale menu comment changed afterward.
  Existing SDK-parser, compiler-language and MealsScreen condition warnings remain.
- Completion handoff: implementation acceptance is complete. Coordinator owns the
  user-authorized scoped `commit-log` commits, keeping preexisting OCR context/intake/
  harness edits, Sponsor content, attachment files and the versionCode change unstaged.
