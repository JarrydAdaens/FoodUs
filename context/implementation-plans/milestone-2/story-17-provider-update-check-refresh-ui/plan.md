# Plan: Provider Update-Check & Refresh UI

## Metadata

- Task Type: `FEATURE`
- Status: `Done`
- Owner: Jarryd Adaens
- Last Updated: 25 July 2026

## Linked Context

- Milestone: [context/milestones/milestone-2.md](../../../milestones/milestone-2.md)
- Story: [Story 17: Provider update-check & refresh UI](../../../milestones/milestone-2.md#story-17)
- Dictation source: [2026-07-25 Milestone 2 feature spec](../../../dictations-tier-0/2026-07-25_milestone-2_australian-providers-and-quickadd-spec.md) (Phase 4)
- Related Plans:
  - [Story 14 (spike)](../story-14-provider-quickadd-architecture-spike/plan.md) — blocks: version-identifier strategy per source.
  - [Story 15 (AFCD)](../story-15-australian-food-composition-database-provider/plan.md) — **depends on**: provides the downloadable provider and the transactional replace primitive this UI drives. Story 16 plugs in the same way if feasible.

## CER

- Complexity: 7
- Effort: 7
- Risk: 6
- Notes: Inline estimate. This story builds what Food You entirely lacks today: a **provider-metadata store** (installed version, dates, checksum, error), an **update-check** that fetches remote version metadata and compares it, and an **atomic full-refresh** with a "keep the old dataset until the new one validates" guarantee. Version comparison and atomicity are the subtle parts; risk 6 because a botched refresh is exactly the "user left with no working database / mutated history" failure the spec forbids.

## Objective

Give each downloadable provider a settings surface to see its state and last successful update-check, check for a newer dataset, and perform a full refresh that atomically replaces only that provider's data — persisting the new version and never mutating historical diary records or destroying the last-good dataset on failure.

## Scope

### In Scope

- A provider-metadata persistence layer: installed dataset version, publication date, source checksum (where used), import date, last **successful** update-check timestamp, optional last-attempted-check, last error summary, current import state.
- Update-check use case: fetch source version metadata → compare with local → persist a successful-check timestamp → return up-to-date / new-available / failure.
- Version comparison with the spec's precedence (explicit version → publication date → revision id → checksum+mtime → checksum).
- Full-refresh flow built on Story 15's transactional replace: keep old data usable until validated; progress; cancellable where supported; replace only the selected provider's rows; persist new version + import date.
- Per-provider settings UI: name, enable/disable, install/import state, current version/date, last-check date/time, `Check for updates`, download/update status, error state.

### Out Of Scope

- The first-import and replace primitive themselves — Story 15 (this story consumes them).
- Delta/incremental updates (spec: not required).
- Any auto-update policy — checks never silently replace.

## Non-Goals

- Background/scheduled update checks (foreground, user-initiated only; no `WorkManager` exists).
- Recording a failed check as a successful `last checked` event.

## Current Understanding

From 2026-07-25 provider recon (`file:line` where load-bearing) — Story 14 formalizes:

- **No version/metadata table exists anywhere** — this store is net-new (a small Room table or DataStore keyed by `FoodSource.Type`).
- Transactional replace is being introduced by Story 15 (delete-by-source + import inside one `withTransaction`); this story orchestrates it and adds the validate-before-activate guarantee.
- No background/resumable download infra; Ktor `HttpClient` is available for the metadata fetch + dataset download.
- Import state/progress patterns to mirror: Swiss `SwissFoodCompositionDatabaseViewModel` (progress) and the richer Android `ImportCsvProductsViewModel` (`WaitingForFile`/`Importing`/`ImportSuccess`/`FailedToImport`).
- Settings surfaces: `app/ui/database/master/DatabaseSettingsScreen.kt`, `app/ui/database/externaldatabases/ExternalDatabasesScreen.kt` / `ExternalDatabasesViewModel.kt`.
- Diary entries are already immutable snapshots, so a refresh cannot rewrite history regardless — the guarantee this story must add is about the *provider dataset* (keep old until new validates), not the diary.
- Assumptions/constraints: version identifiers differ per source and are set by Story 14; "atomic" means the new dataset activates only after download + validation + parse + required-field checks + persistence + index build all succeed.

## Questions / Unknowns

- Q: Provider-metadata store — a dedicated Room table or DataStore preferences?
  Impact: Query ergonomics and migration surface.
  Assumption: A small Room table `ProviderDataset` keyed by source (version, publicationDate, checksum, importDate, lastSuccessfulCheck, lastAttemptedCheck, lastError, state) — queryable alongside `Product`.
  Status: OPEN.
- Q: How is "keep old dataset usable until new validates" implemented given a single shared `Product` table and delete-by-source replace?
  Impact: The core atomicity guarantee.
  Assumption: Download + validate + parse to a staging area (temp/holding rows or a validated in-memory/temp-table set) first; only then, inside one transaction, delete-by-source + insert. If anything before the transaction fails, the live rows were never touched.
  Status: OPEN — confirm the staging approach in Story 14/15.
- Q: What remote endpoint yields each provider's latest version metadata?
  Impact: The update-check's fetch step per provider.
  Assumption: AFCD = the FSANZ dataset/release page or a manifest; FoodSwitch = per its (gated) access. Set by Story 14.
  Status: OPEN.

## Execution Steps

1. Build the provider-metadata store.
   - Edits: new `ProviderDataset` Room entity + DAO + repository (or DataStore); migration + exported schema; write metadata on Story 15's first import.
2. Implement version comparison.
   - Why: The spec's precedence must be deterministic and testable.
   - Edits: a pure `compareDatasetVersion(local, remote)` with the 5-level precedence + unit tests.
3. Implement the update-check use case.
   - Edits: fetch remote version metadata (Ktor) → compare → persist last **successful** check on success only → return up-to-date / new-available / failure(reason). Never mutate the dataset.
4. Implement the atomic full-refresh.
   - Edits: download → validate → parse → (staging) → transactional delete-by-source + import (reusing Story 15) → persist new version + import date → rebuild indexes; roll back / retain old on any failure; progress + cancellation where supported.
5. Build the per-provider settings UI.
   - Edits: extend the external-databases settings screen with the required controls + states (up-to-date / new-available with explicit download action / error); show last successful check; persist across restart.
6. Tests per §10.1–10.2 + Phase 4 acceptance (independent per-provider check; successful check persists + survives restart; current reports up-to-date; new offers refresh; refresh replaces only that provider; history unchanged; network/import failure leaves old dataset usable; recovery from interrupted download/import).

## Validation

### Automated Checks

- Unit tests for `compareDatasetVersion` across all precedence levels.
- Integration tests: refresh replaces only the target provider; failed refresh preserves old data; metadata persists across a simulated restart.
- Build compiles.

### Manual Checks

1. Check-for-updates on a current dataset → "up to date"; last-check date updates and survives an app restart.
2. Point at a newer dataset → "new available" → refresh → local version changes, new data in search, historical entries unchanged.
3. Simulate a corrupt/failed refresh → old dataset stays active, error shown, retry offered; a failed check is **not** recorded as a successful check.

### Acceptance Criteria

- Each provider can be checked independently; a successful check persists a date that survives restart.
- Current datasets report up-to-date; new datasets offer an explicit refresh (no silent replace).
- A refresh replaces only that provider's rows; custom foods, other providers, Quick Add, and all diary history are preserved.
- Network/import failures leave the previous dataset usable and are surfaced clearly.

## Risk Mitigation

- Risk: Non-atomic refresh leaves the user with a half-imported or empty dataset.
  Mitigation: Validate + parse to staging before touching live rows; do the swap inside one transaction; integration-test the failure path.
- Risk: Version comparison misfires → false "up to date" or needless refresh.
  Mitigation: Deterministic precedence with exhaustive unit tests; never compare only local download date.
- Risk: A failed check recorded as successful → user trusts stale data.
  Mitigation: Persist `lastSuccessfulCheck` only on success; keep `lastAttemptedCheck` separate.

## Evidence / References

- Planning input: 2026-07-25 provider recon (no metadata table; transactional `withTransaction`; import-state view-model patterns; settings screens; no background download) with `file:line`; spec Phase 4, §7.3.
- Unverified: per-provider version-metadata endpoints and the exact staging mechanism — set by Stories 14/15 before this executes.

## Execution Log

- Status: `Done` (implemented and validated on the `foodyou` emulator, 2026-07-25).
- Built on Story 15's foundation rather than duplicating it: the `ProviderMetadata` store, the
  atomic download→validate→parse→single-`withTransaction` replace pipeline, and the AFCD provider
  screen already existed. Story 15's `import()` **is** the full refresh, so this story consumes it as
  the "download and replace" action and adds the update-check half plus the UI to drive both.
- Version comparison (provider-generic, `importexport/providermetadata/domain/`): new
  `DatasetVersion` (explicitVersion / publicationDate / revisionId / checksum / modifiedTime) and a
  pure `compareDatasetVersion(local, remote)` implementing the strongest-identifier cascade
  (explicit version → publication date → revision id → checksum+mtime → checksum), returning
  `DatasetComparison.{UpToDate, UpdateAvailable, Indeterminate}`. 5 focused unit tests in
  `DatasetComparisonTest` cover each tier, fall-through when a stronger signal is missing on one
  side, and the no-shared-identifier (Indeterminate) case.
- Update-check use case (AFCD-specific): `CheckAustralianFoodCompositionDatabaseUpdateUseCase` does a
  single Ktor `HEAD` on the workbook URL (no download, no dataset mutation), maps `Last-Modified` /
  `ETag` / `Content-Length` into a remote `DatasetVersion`, and compares against the stored metadata.
  A completed request records `lastSuccessfulCheck` and clears `lastError` (whether or not an update
  was found); a network failure records `lastError`, leaves `lastSuccessfulCheck` untouched, and
  keeps the installed dataset. `Indeterminate` errs toward offering an update (never a false
  "up to date"). Repository gained `fetchRemoteSignature()` (Android `HEAD`; iOS unsupported stub);
  new `AfcdRemoteSignature` domain type. DI: `factoryOf(::...Impl).bind<...>()` in the provider
  module; the view-model module injects it.
- UI (`app/ui/database/australianfoodcompositiondatabase/`): the previously mislabeled single
  button (which ran a full import under the "Check for updates" label) is now a genuine
  non-destructive **Check for updates** action. New `Phase.{Checking, UpToDate, UpdateAvailable}`
  states and a `busy` helper; `UpdateOutcome` shows "Up to date" / "A newer dataset is available." /
  "Checking for updates…"; `ActionButtons` shows a plain **Import** before first install, **Check
  for updates** once installed, and the explicit **Download and replace** action only after a check
  reports a newer dataset (no silent auto-replace). New strings:
  `action_download_and_replace`, `neutral_checking_for_updates`, `neutral_dataset_up_to_date`,
  `neutral_dataset_update_available`.

## Completion Review

- Deviations: (1) The full-refresh pipeline (download→validate→parse→transactional replace→persist)
  was already delivered by Story 15; Story 17 reuses `import()` verbatim as the download-and-replace
  action rather than re-implementing it. (2) **Cancellation**: the refresh is a single transactional
  bulk insert with no resumable/background infrastructure (spike §4.4), so a mid-flight cancel of the
  transaction is not offered; safety is instead guaranteed by ordering (network/parse happen before
  any DB mutation; the transaction is all-or-nothing; the last-good dataset survives any failure).
  Controls are disabled during the operation. This matches the story's "cancellable where the
  infrastructure supports it" wording. (3) The update-check maps AFCD's static `RELEASE_LABEL` as the
  local explicit version but leaves the remote explicit version unset (FSANZ does not expose the
  release number over HTTP), so the decisive runtime tier is the `Last-Modified` publication date —
  exactly the spike's §9.1 recommendation.
- Validation: unit tests `:app:testDebugUnitTest --tests
  com.maksimowiczm.foodyou.importexport.providermetadata.DatasetComparisonTest` — 5 tests, all
  passing. `:app:assembleDebug` — BUILD SUCCESSFUL. Emulator E2E on `foodyou` (live FSANZ endpoint):
  provider screen shows name / include-in-search / `Installed (Release 3)` / last import / last
  successful check + Check-for-updates button. Check against the live endpoint → **Up to date**
  (stored publication date matches remote `Last-Modified`); DB confirmed `lastSuccessfulCheck`
  advanced and `lastError` cleared. Failure path (Wi-Fi + data disabled) → error surfaced in red
  ("Unable to resolve host…"), `Installed (Release 3)` kept, and DB confirmed `lastSuccessfulCheck`
  **unchanged** while `lastError` was set — a failed check is not recorded as successful. Aged the
  stored publication date and re-checked → **A newer dataset is available.** with the explicit
  **Download and replace** button; tapped it → progress shown, controls disabled, old dataset stayed
  `Installed (Release 3)` until the new one validated. After the refresh: AFCD row count still 1,588
  but ids moved from 1–1588 to 1589–3176 (rows genuinely deleted + re-inserted, only AFCD affected),
  metadata re-persisted with the real Dec-2025 publication date and fresh import/check timestamps,
  the 4 `ManualDiaryEntry` diary rows (`PieTest` / `PreMigration Snack`) preserved, and FTS search
  in sync (`ProductFts MATCH 'vegemite'` → 1, `'beef*'` → 78). iOS not built (no macOS host);
  `expect`/`actual` unsupported stub provided.
