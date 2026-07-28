# Plan: Notification Center tab

## Metadata

- Task Type: `STORY`
- Status: `Ready`
- Owner: Jarryd Adaens
- Last Updated: 28 July 2026

Status rationale: no owner-gated or contract-gated decision blocks planning-level choices here. The store, the tab UI, and the emission contract are all local-only. The open questions below are carried as marked assumptions; emitter wiring lands with the emitter stories (sequencing, not a gate).

## Linked Context

- Milestone: [context/milestones/milestone-3.md](../../../milestones/milestone-3.md)
- Story: [Story 13: Notification Center tab](../../../milestones/milestone-3.md#story-13) `[STORY 3.13]`
- Design authority: `context/design.md` — "The Multiplayer Exception" (poll-on-wake, refuse-loudly rule routes unknown envelope versions here) and Core Principles ("Respect the user" — this tab is deliberately the app's only broadcast surface)
- Related Plans:
  - Story 3.1 (tabbed UI shell) — **depends on it**: the Notifications tab slot ships there (possibly as a stub); this story fills it. `context/implementation-plans/milestone-3/story-1-tabbed-ui-shell/`
  - Stories 3.6–3.12 — emitters. Each publishes events through the contract this story defines; their plans own their emission call sites. Story 3.8 (message pipeline) is BLOCKED on the wire contract this run — the poll-time materialization call site is planned there, not here.
- External Tooling: none required.

## CER

- Complexity: 4
- Effort: 5
- Risk: 3
- Notes: Inline estimate. The repository already contains every pattern needed: a domain `EventBus`/`IntegrationEvent` seam with a `eventHandlerOf` Koin registration helper (`common/infrastructure/koin/EventHandlerOf.kt`), Room entity/DAO conventions (`sponsorship/infrastructure/room/` is a minimal model), and feature-slice + Koin module composition (`fooddiary/FoodDiaryModule.kt`, `app/di/InitKoin.kt`). Complexity sits in designing a notification taxonomy that stays stable while most emitters (Stories 3.6–3.12) do not exist yet. Risk sits on the `FoodYouDatabase` schema change (upstream-shared file; version bump + migration must be additive and correct) and on taxonomy churn if later stories need payload shapes this plan did not anticipate.

## Objective

Ship a persistent, local Notification Center: a Room-backed event store, a stable domain contract that any feature can emit events through, and the rightmost-tab UI that lists undismissed notifications by default with a control exposing the dismissed/read history — so that once Stories 3.6–3.12 land, every cross-device and social event the system generates has exactly one place where the user sees it.

## Scope

### In Scope

- New fork-additive feature slice `notification` under `com.maksimowiczm.foodyou.notification` with `domain` / `infrastructure` separation and a `NotificationModule.kt` Koin module, registered in `InitKoin.kt` (one additive line).
- Domain model: `AppNotification` (id, type, occurred-at timestamp, read/dismissed flag, and a small typed payload for display), `AppNotificationRepository` interface, and a `RecordNotification` use case.
- Emission contract: new `IntegrationEvent` subtypes for notification-worthy occurrences, published on the existing `EventBus`; a `NotificationRecordingEventHandler` (an `IntegrationEventHandler` registered via `eventHandlerOf`, created at start) persists them. This is the stable seam Stories 3.6–3.12 call — emitters publish an event; they never touch the store directly.
- Persistence: `NotificationEntity` + `NotificationDao` (Room, Flow-based observation, mark-read / dismiss / dismiss-all updates), added to `FoodYouDatabase` with a version bump and `AutoMigration`.
- UI: `NotificationsTabContent` composable + `NotificationsViewModel` under `app/ui/notifications/`, mounted in the Notifications tab slot created by Story 3.1. Default view: undismissed notifications, newest first. A history control (toggle/filter) exposes dismissed/read items. Per-item dismiss and a dismiss-all action.
- New English strings in the shared resources base `strings.xml` (additive, fork-owned keys), including text for the initial event types this story can already name (e.g. "entry added to your diary", "entry failed to add", "unknown message version refused").
- Seed emitters that exist today: none are required to land with this story — the tab may render an empty state until Stories 3.6–3.12 emit. The empty state is in scope.

### Out Of Scope

- The tab shell and bottom navigation (Story 3.1).
- All actual emission call sites in Stories 3.6–3.12 (friend added you, group invites/renames, entry added/failed, suggestion arrived, unknown envelope version refused at poll time). Each emitter story publishes the events; this story only guarantees they are recorded and displayed.
- The poll-on-wake drain step and message router (Story 3.8, BLOCKED this run) — including how drained relay messages become events. This plan only fixes the contract they will publish through.
- Any wire-contract, envelope, or relay detail — owned by foodus-relay; nothing here talks to the network.

## Non-Goals

- No OS-level (system tray) notifications, no notification permission requests, no badges — in-app surface only (see Questions; this follows the story text and the no-nagging principle).
- No push transport of any kind (FCM is explicitly rejected by design; backlog item).
- No notification "actions" beyond dismiss (accepting a suggestion happens in Story 3.12's queue UI, not from a notification row).
- No cross-device sync of notification read state.

## Current Understanding

All paths verified in the working tree on 28 July 2026.

- **Event seam (reuse, do not reinvent):** `common/domain/event/` defines `IntegrationEvent`, `EventBus` (publish + `events: Flow`), `IntegrationEventHandler`, and `subscribe`; `common/infrastructure/inmemory/SharedFlowEventBus.kt` is the bus implementation (50-event buffer, DROP_LATEST — see Risk); `common/infrastructure/koin/EventHandlerOf.kt` provides `eventHandlerOf(...)` registering handlers `createdAtStart` against the application coroutine scope. `InMemoryModule.kt` binds the bus.
- **Feature-slice pattern:** each slice (e.g. `fooddiary/`) has `domain/` + `infrastructure/` + `<Feature>Module.kt` composing sub-modules; slices register in `app/di/InitKoin.kt:28-43`. The new `notification` slice follows this exactly.
- **Room pattern:** `sponsorship/infrastructure/room/` shows the minimal shape — `@Entity` data class with indices, `@Dao` with Flow queries and suspend mutations, a per-slice `*Database` interface exposing DAOs (`fooddiary/infrastructure/room/FoodDiaryDatabase.kt`), all composed into `app/infrastructure/room/FoodYouDatabase.kt` (currently `VERSION = 36`, `exportSchema = true`, AutoMigrations list). Adding `NotificationEntity` means: new entity in the slice, new `NotificationDatabase` interface, `FoodYouDatabase` implements it, entity registered, version 36 → 37, `AutoMigration(from = 36, to = 37)` (pure additive table — auto-migration suffices).
- **UI/ViewModel pattern:** screens + ViewModels live under `app/ui/<area>/` with their own small Koin module (e.g. `app/ui/database/externaldatabases/ExternalDatabasesModule.kt`), composed via `uiModule`. Navigation is `FoodYouAppNavHost.kt` with `@Serializable` route objects — but this story's surface is a *tab*, not a pushed route; it mounts inside Story 3.1's shell. If Story 3.1 ships the shell as sibling tab composables (its plan owns that decision), this story provides `NotificationsTabContent` and does not touch `FoodYouAppNavHost.kt`.
- **Strings:** English base strings in `shared/resources/src/commonMain/composeResources/values/strings.xml`, additive fork-owned keys (established by Milestone 2 stories).
- **Existing behaviors to preserve:** `EventBus` consumers already exist (e.g. `DiaryFoodSearchViewModel`); the new handler must only subscribe to its own event types and never affect existing subscribers. `FoodYouDatabase` migration chain and schema export must remain valid for both household phones (no data loss on update — two-device constitutional constraint).
- **Assumptions and constraints:** fork philosophy — the slice, DAO, UI, and strings are new files; the only upstream-shared files touched are `FoodYouDatabase.kt` (additive entity + version + auto-migration), `InitKoin.kt` (one line), and `strings.xml` (appended keys). Poll-on-wake means notifications materialize from drained messages plus local events; there is no push and therefore no delivery guarantee beyond what the drain step records.

## Questions / Unknowns

- Q: `[STORY 3.13]` Are OS-level (Android system) notifications in scope at all, now or later?
  Impact: Decides whether the slice needs any platform (`androidMain`) code and a permission story; shapes the domain model (an OS bridge would want per-type channels).
  Assumption: No — in-app tab only. The story text describes a tab, the design doc calls it the app's only broadcast surface, and no-nagging is constitutional. Everything in this plan is `commonMain`.
  Status: OPEN
  Answer: —

- Q: `[STORY 3.13]` What is the retention policy for the persistent notification history — unbounded, count-capped, or age-capped?
  Impact: Unbounded growth is plausible junk-data accumulation in a household app used daily for years; a cap needs a trim step (DAO delete on insert or periodic).
  Assumption: Age-capped pruning of *dismissed* notifications after 90 days, executed on insert (cheap DAO delete). Undismissed items are never pruned. Cheap to change; flagged for owner taste.
  Status: OPEN
  Answer: —

- Q: `[STORY 3.13]` Exact event taxonomy: the story lists examples ("meal added, meal failed to add, friend added you, you joined a group, a group changed its name, ..."), but most emitters (Stories 3.6–3.12) are unplanned or blocked (3.8), so the full closed list cannot be fixed now.
  Impact: The `NotificationType` enum and payload shape must be extensible without schema migrations every time an emitter story adds a type.
  Assumption: Store the type as a string column (not an ordinal), with a versionless free-text title/body resolved at *display* time from the typed payload where possible; unknown/legacy types still render their stored text. Emitter stories add enum entries + strings additively.
  Status: OPEN
  Answer: —

- Q: `[STORY 3.13]` Does "meal added" mean only cross-device insertions (Story 3.11's auto-insert) or also purely local diary saves?
  Impact: Notifying on the user's own local saves would be noise and borders on nagging.
  Assumption: Cross-device and social/system events only; the user's own local actions never generate notifications.
  Status: OPEN
  Answer: —

- Q: `[STORY 3.13]` Should notification rows ride the existing backup/export path?
  Impact: None structurally — the entity lives in the Room DB so it is backed up with everything else; the question is only whether that is *desired*.
  Assumption: Yes by default (free), acceptable because notifications contain no secrets and the DB stays on-device.
  Status: OPEN
  Answer: —

## Execution Steps

1. Create the `notification` slice domain layer.
   - Why: Stable, framework-free contract before any persistence or UI exists.
   - Edits: `notification/domain/AppNotification.kt` (id, type, occurredAt, isDismissed, display payload), `notification/domain/AppNotificationRepository.kt` (observe undismissed / observe all / record / markDismissed / dismissAll), `notification/domain/NotificationEvents.kt` — the `IntegrationEvent` subtypes this story can already name (entry-added, entry-add-failed, unknown-message-version-refused as placeholders for Story 3.8/3.11 emitters; friend/group types land with their stories).
   - Dependencies: none.

2. Add Room persistence.
   - Why: Notifications must survive process death and ride backup.
   - Edits: `notification/infrastructure/room/NotificationEntity.kt` (indexed on `occurredAtEpochSeconds`, `isDismissed`; type stored as string), `NotificationDao.kt` (Flow queries newest-first, filtered/unfiltered; suspend dismiss/dismissAll/prune), `NotificationDatabase.kt` interface; register entity in `app/infrastructure/room/FoodYouDatabase.kt`, implement the interface, bump `VERSION` 36 → 37, add `AutoMigration(from = 36, to = 37)`.
   - Dependencies: step 1 (entity mirrors domain model).

3. Implement repository + recording handler + Koin wiring.
   - Why: This is the emission contract in action — publish an event anywhere, it becomes a persistent notification.
   - Edits: `notification/infrastructure/RoomAppNotificationRepository.kt` (includes the insert-time prune per the retention assumption), `notification/infrastructure/NotificationRecordingEventHandler.kt` (an `IntegrationEventHandler` over the notification event types), `notification/NotificationModule.kt` (repository binding + `eventHandlerOf(::NotificationRecordingEventHandler)`), one additive line in `app/di/InitKoin.kt`.
   - Dependencies: steps 1–2.

4. Build the tab UI.
   - Why: The user-facing surface — the story's visible deliverable.
   - Edits: `app/ui/notifications/NotificationsTabContent.kt` (undismissed list newest-first, per-item dismiss, dismiss-all, history toggle revealing dismissed items, empty state), `NotificationsViewModel.kt`, small `NotificationsUiModule.kt` composed into `uiModule`; mount into Story 3.1's Notifications tab slot (coordinate with that plan — if 3.1 shipped a stub composable, replace its body).
   - Dependencies: step 3; Story 3.1's shell merged first (or same sitting).

5. Strings + validation pass.
   - Why: User-visible text and the story's Definition-of-Done evidence.
   - Edits: additive keys in `shared/resources/.../values/strings.xml` (tab title, empty state, history toggle, dismiss actions, per-type templates for the seeded types); run automated checks below.
   - Dependencies: steps 1–4.

## Validation

### Automated Checks

- `./gradlew :app:compileDebugKotlinAndroid` (or the repo's standard build task) — compiles across targets.
- Unit tests (`commonTest`): recording handler persists an event → repository observes it; dismiss hides from default query but appears in history query; prune deletes only dismissed items older than the cutoff; unknown/legacy type string still maps to a displayable notification. (3–4 focused tests; the store is stable business logic.)
- Room schema export diff shows exactly one new table at version 37; migration test via existing Room testing setup if the repo has one for prior migrations (check `androidInstrumentedTest`).

### Manual Checks

1. Fresh install: Notifications tab shows empty state.
2. Publish a seeded test event (temporary debug hook or test): row appears newest-first; dismiss hides it; history toggle reveals it; dismiss-all clears the default list.
3. Upgrade path: install previous build, add diary data, update to this build — data intact, DB at version 37 (two-device update constraint).

### Acceptance Criteria

- Notifications persist across app restarts; dismissed/read items hidden by default with a working control exposing the historical list.
- Any code in the app can create a notification by publishing a single `IntegrationEvent` — no direct store access needed by emitter stories.
- No OS notifications, no polling, no network I/O introduced by this story.
- Existing `EventBus` consumers and Room data unaffected (schema change is purely additive).

## Risk Mitigation

- Risk: `FoodYouDatabase` version bump / migration breaks existing installs (upstream-shared file, two household phones).
  Mitigation: Purely additive table via `AutoMigration`; schema export diff reviewed; manual upgrade check in Validation; edits to the shared file are append-style to preserve merge surface.
- Risk: `SharedFlowEventBus` drops events on buffer overflow (documented DROP_LATEST behavior), so a notification could silently never be recorded.
  Mitigation: Accepted for now — notification-worthy events are low-frequency (human-scale social actions), far below the 50-event buffer. Noted here so Story 3.8's drain step (which could batch many messages at once) revisits it: if a poll drains > buffer-size messages, its plan must record notifications transactionally rather than via the bus, or the bus buffer is raised. Carried as an explicit note for Story 3.8's future plan.
- Risk: Taxonomy churn — later emitter stories need payload fields this schema lacks.
  Mitigation: Type-as-string plus display-text materialized at record time; new types are additive enum + string entries; schema holds no per-type columns.
- Risk: Tab-mount seam mismatch with Story 3.1 (planned in parallel).
  Mitigation: This story exposes one self-contained `NotificationsTabContent()` composable with no parameters beyond modifier/callbacks; whatever shell shape 3.1 lands, mounting is a one-line change on its side. Coordinate at execution time.

## Phase Split

Not needed. CER within thresholds; single-pass executable.

## Evidence / References

- Pattern sources verified 28 July 2026: `common/domain/event/*`, `common/infrastructure/inmemory/SharedFlowEventBus.kt`, `common/infrastructure/koin/EventHandlerOf.kt`, `sponsorship/infrastructure/room/*`, `fooddiary/infrastructure/room/FoodDiaryDatabase.kt`, `app/infrastructure/room/FoodYouDatabase.kt` (VERSION 36), `app/di/InitKoin.kt`, `app/navigation/FoodYouAppNavHost.kt`.
- Planning inputs: milestone-3.md Story 13 + Relay Contract Conformance; design.md "The Multiplayer Exception" (poll-on-wake, refuse-loudly), Core Principles.
- Unverified claims: none — no wire-contract details were assumed (none are needed; this story is fully local).

## Complaints / Friction

None worth recording. The one structural tension — most emitters do not exist yet — is inherent to the milestone's sequencing (stub with Story 1, finalize after 9–12) and is handled by the extensible-taxonomy assumption rather than being a planning defect.
