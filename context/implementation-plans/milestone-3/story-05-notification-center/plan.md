# Plan: Notification Center tab

## Metadata

- Task Type: `STORY`
- Status: `Complete`
- Owner: Jarryd Adaens
- Last Updated: 29 July 2026

Status rationale: no owner-gated or contract-gated decision blocks planning-level choices here. The store, the tab UI, and the emission contract are all local-only. The open questions below are carried as marked assumptions; emitter wiring lands with the emitter stories (sequencing, not a gate).

## Linked Context

- Milestone: [context/milestones/milestone-3.md](../../../milestones/milestone-3.md)
- Story: [Story 5: Notification Center tab](../../../milestones/milestone-3.md#story-5) `[STORY 3.5]`
- Design authority: `context/design.md` — "The Multiplayer Exception" (poll-on-wake, refuse-loudly rule routes unknown envelope versions here) and Core Principles ("Respect the user" — this tab is deliberately the app's only broadcast surface)
- Related Plans:
  - Story 3.1 (tabbed UI shell) — **depends on it**: the Notifications tab slot ships there (possibly as a stub); this story fills it. `context/implementation-plans/milestone-3/story-01-tabbed-ui-shell/`
  - Stories 3.8–3.14 — emitters. Each publishes events through the contract this story defines; their plans own their emission call sites. Story 3.10 (message pipeline) is BLOCKED on the wire contract this run — the poll-time materialization call site is planned there, not here.
- External Tooling: none required.

## CER

- Complexity: 4
- Effort: 5
- Risk: 3
- Notes: Inline estimate. The repository already contains every pattern needed: a domain `EventBus`/`IntegrationEvent` seam with a `eventHandlerOf` Koin registration helper (`common/infrastructure/koin/EventHandlerOf.kt`), Room entity/DAO conventions (`sponsorship/infrastructure/room/` is a minimal model), and feature-slice + Koin module composition (`fooddiary/FoodDiaryModule.kt`, `app/di/InitKoin.kt`). Complexity sits in designing a notification taxonomy that stays stable while most emitters (Stories 3.8–3.14) do not exist yet. Risk sits on the `FoodYouDatabase` schema change (upstream-shared file; version bump + migration must be additive and correct) and on taxonomy churn if later stories need payload shapes this plan did not anticipate.

## Objective

Ship a persistent, local Notification Center: a Room-backed event store, a stable domain contract that any feature can emit events through, and the rightmost-tab UI that lists undismissed notifications by default with a control exposing the dismissed/read history — so that once Stories 3.8–3.14 land, every cross-device and social event the system generates has exactly one place where the user sees it.

## Scope

### In Scope

- New fork-additive feature slice `notification` under `com.maksimowiczm.foodyou.notification` with `domain` / `infrastructure` separation and a `NotificationModule.kt` Koin module, registered in `InitKoin.kt` (one additive line).
- Domain model: `AppNotification` (id, type, occurred-at timestamp, read/dismissed flag, and a small typed payload for display), `AppNotificationRepository` interface, and a `RecordNotification` use case.
- Emission contract: new `IntegrationEvent` subtypes for notification-worthy occurrences, published on the existing `EventBus`; a `NotificationRecordingEventHandler` (an `IntegrationEventHandler` registered via `eventHandlerOf`, created at start) persists them. This is the stable seam Stories 3.8–3.14 call — emitters publish an event; they never touch the store directly.
- Persistence: `NotificationEntity` + `NotificationDao` (Room, Flow-based observation, mark-read / dismiss / dismiss-all updates), added to `FoodYouDatabase` with a version bump and `AutoMigration`.
- UI: `NotificationsTabContent` composable + `NotificationsViewModel` under `app/ui/notifications/`, mounted in the Notifications tab slot created by Story 3.1. Default view: undismissed notifications, newest first. A history control (toggle/filter) exposes dismissed/read items. Per-item dismiss and a dismiss-all action.
- New English strings in the shared resources base `strings.xml` (additive, fork-owned keys), including text for the initial event types this story can already name (e.g. "entry added to your diary", "entry failed to add", "unknown message version refused").
- Seed emitters that exist today: none are required to land with this story — the tab may render an empty state until Stories 3.8–3.14 emit. The empty state is in scope.

### Out Of Scope

- The tab shell and bottom navigation (Story 3.1).
- All actual emission call sites in Stories 3.8–3.14 (friend added you, group invites/renames, entry added/failed, suggestion arrived, unknown envelope version refused at poll time). Each emitter story publishes the events; this story only guarantees they are recorded and displayed.
- The poll-on-wake drain step and message router (Story 3.10, BLOCKED this run) — including how drained relay messages become events. This plan only fixes the contract they will publish through.
- Any wire-contract, envelope, or relay detail — owned by foodus-relay; nothing here talks to the network.

## Non-Goals

- No OS-level (system tray) notifications, no notification permission requests, no badges — in-app surface only (see Questions; this follows the story text and the no-nagging principle).
- No push transport of any kind (FCM is explicitly rejected by design; backlog item).
- No notification "actions" beyond dismiss (accepting a suggestion happens in Story 3.14's queue UI, not from a notification row).
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

- Q: `[STORY 3.5]` Are OS-level (Android system) notifications in scope at all, now or later?
  Impact: Decides whether the slice needs any platform (`androidMain`) code and a permission story; shapes the domain model (an OS bridge would want per-type channels).
  Assumption: No — in-app tab only. The story text describes a tab, the design doc calls it the app's only broadcast surface, and no-nagging is constitutional. Everything in this plan is `commonMain`.
  Status: OPEN
  Answer: —

- Q: `[STORY 3.5]` What is the retention policy for the persistent notification history — unbounded, count-capped, or age-capped?
  Impact: Unbounded growth is plausible junk-data accumulation in a household app used daily for years; a cap needs a trim step (DAO delete on insert or periodic).
  Assumption: Age-capped pruning of *dismissed* notifications after 90 days, executed on insert (cheap DAO delete). Undismissed items are never pruned. Cheap to change; flagged for owner taste.
  Status: OPEN
  Answer: —

- Q: `[STORY 3.5]` Exact event taxonomy: the story lists examples ("meal added, meal failed to add, friend added you, you joined a group, a group changed its name, ..."), but most emitters (Stories 3.8–3.14) are unplanned or blocked (3.10), so the full closed list cannot be fixed now.
  Impact: The `NotificationType` enum and payload shape must be extensible without schema migrations every time an emitter story adds a type.
  Assumption: Store the type as a string column (not an ordinal), with a versionless free-text title/body resolved at *display* time from the typed payload where possible; unknown/legacy types still render their stored text. Emitter stories add enum entries + strings additively.
  Status: OPEN
  Answer: —

- Q: `[STORY 3.5]` Does "meal added" mean only cross-device insertions (Story 3.13's auto-insert) or also purely local diary saves?
  Impact: Notifying on the user's own local saves would be noise and borders on nagging.
  Assumption: Cross-device and social/system events only; the user's own local actions never generate notifications.
  Status: OPEN
  Answer: —

- Q: `[STORY 3.5]` Should notification rows ride the existing backup/export path?
  Impact: None structurally — the entity lives in the Room DB so it is backed up with everything else; the question is only whether that is *desired*.
  Assumption: Yes by default (free), acceptable because notifications contain no secrets and the DB stays on-device.
  Status: OPEN
  Answer: —

## Execution Steps

1. Create the `notification` slice domain layer.
   - Why: Stable, framework-free contract before any persistence or UI exists.
   - Edits: `notification/domain/AppNotification.kt` (id, type, occurredAt, isDismissed, display payload), `notification/domain/AppNotificationRepository.kt` (observe undismissed / observe all / record / markDismissed / dismissAll), `notification/domain/NotificationEvents.kt` — the `IntegrationEvent` subtypes this story can already name (entry-added, entry-add-failed, unknown-message-version-refused as placeholders for Story 3.10/3.13 emitters; friend/group types land with their stories).
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
  Mitigation: Accepted for now — notification-worthy events are low-frequency (human-scale social actions), far below the 50-event buffer. Noted here so Story 3.10's drain step (which could batch many messages at once) revisits it: if a poll drains > buffer-size messages, its plan must record notifications transactionally rather than via the bus, or the bus buffer is raised. Carried as an explicit note for Story 3.10's future plan.
- Risk: Taxonomy churn — later emitter stories need payload fields this schema lacks.
  Mitigation: Type-as-string plus display-text materialized at record time; new types are additive enum + string entries; schema holds no per-type columns.
- Risk: Tab-mount seam mismatch with Story 3.1 (planned in parallel).
  Mitigation: This story exposes one self-contained `NotificationsTabContent()` composable with no parameters beyond modifier/callbacks; whatever shell shape 3.1 lands, mounting is a one-line change on its side. Coordinate at execution time.

## Phase Split

Not needed. CER within thresholds; single-pass executable.

## Evidence / References

- Pattern sources verified 28 July 2026: `common/domain/event/*`, `common/infrastructure/inmemory/SharedFlowEventBus.kt`, `common/infrastructure/koin/EventHandlerOf.kt`, `sponsorship/infrastructure/room/*`, `fooddiary/infrastructure/room/FoodDiaryDatabase.kt`, `app/infrastructure/room/FoodYouDatabase.kt` (VERSION 36), `app/di/InitKoin.kt`, `app/navigation/FoodYouAppNavHost.kt`.
- Planning inputs: milestone-3.md Story 5 + Relay Contract Conformance; design.md "The Multiplayer Exception" (poll-on-wake, refuse-loudly), Core Principles.
- Unverified claims: none — no wire-contract details were assumed (none are needed; this story is fully local).

## Complaints / Friction

None worth recording. The one structural tension — most emitters do not exist yet — is inherent to the milestone's sequencing (stub with Story 1, finalize after 9–12) and is handled by the extensible-taxonomy assumption rather than being a planning defect.

## Execution Log

Executed 29 July 2026 in a single sitting, on top of Stories 3.1, 3.2, 3.3 and 3.15.

### Deviations from the plan as written

1. **Database version rebased.** The plan said 36 → 37; Stories 3.2 and 3.3 had already taken 37 and
   38. Landed as `VERSION = 39` with `AutoMigration(from = 38, to = 39)` and a committed `39.json`.
   The additive approach itself was unchanged.
2. **One event class instead of one per kind.** The plan's step 1 called for a set of
   `IntegrationEvent` subtypes. Executed as a single `NotificationEvent(type, arguments)`. This is
   strictly better against the plan's own extensibility goal: the recording handler subscribes once,
   and an emitter story adds a `NotificationType` entry, a string template, and a `publish` call —
   it never touches the notification slice, the handler registration, or the schema.
3. **`NotificationsTabContent` became `NotificationsScreen`.** Story 3.1 had already created
   `app/ui/notifications/NotificationsScreen.kt` delegating to `PlaceholderTabScreen`. The body was
   replaced in place, so the shell mount point did not change at all.
4. **`PlaceholderTabScreen` deleted.** Removing the delegation left it with zero callers (Story 3.2
   had already given the Groups tab real content), so it and its `description_tab_coming_soon`
   string went with it, per the "clean up your own orphans" rule. Nothing else referenced either.
5. **One unplanned edit: `RoomModule.kt`.** The plan's shared-file list missed it. `NotificationDatabase`
   must be added to the `binds(...)` array beside `ProfileDatabase`, or Koin cannot resolve the DAO.
   Caught by an on-device crash on first install, not by the compiler. One additive line plus an
   import; same append-style shape as the other shared-file edits.

### Boss-directed decisions

- **No cross-tab navigation.** The shell keeps the selected tab in private `rememberSaveable`
  state and exposes no navigation API. Rows are deliberately inert: a notification reports that
  something happened, it is not a shortcut. No plumbing was added to `FoodUsAppShell`.
- **No unread badge.** The story defines dismissed/undismissed, not read/unread, and badging the tab
  edges toward nagging. Not added.
- **Bottom inset.** Default `contentWindowInsets`; the shell already consumes the system bar.
- **Relay seams unused.** The Notification Center is entirely local; neither `ObserveRelayConfigured`
  nor `RelayConnectionChecker` is referenced.

### Resolutions of the five OPEN questions (proceeded on the plan's assumptions)

1. **OS notifications** — out of scope. Everything is `commonMain`; no platform code, no permission.
2. **Retention** — dismissed rows are pruned after 90 days, executed on insert inside
   `RecordNotificationUseCase`. Undismissed rows are never pruned, so nothing unseen can expire.
3. **Taxonomy** — `NotificationType` persists a stable `key` string (never an ordinal) plus a JSON
   array of display arguments in one column. No per-kind columns exist, so no future emitter needs a
   migration. A key this build does not know resolves to `null` and renders a neutral "came from a
   newer version" row rather than vanishing.
4. **Local diary saves** — excluded. The user's own actions never notify.
5. **Backup** — included by construction; the table lives in the app database.

### Scope note: emitters

Stories 3.8–3.12 are blocked or unbuilt, and no event that exists today qualifies under decision 4
(`FoodDiaryEntryCreatedEvent` is a local save; `AppLaunchEvent` is not notification-worthy).
**Zero emitters were wired**, which is the correct outcome, not an omission — the store, the
contract, and the tab all ship complete and the first real row arrives with the first emitter story.
Three `NotificationType` entries named in the plan's Scope (`EntryAddedToYourDiary`,
`EntryFailedToAdd`, `UnknownMessageVersionRefused`) ship as the seed taxonomy with their templates.

### Note carried for Story 3.8

`SharedFlowEventBus` buffers 50 events and drops on overflow. A poll that drains more than 50
messages at once could silently lose notifications. Recorded here as the plan's Risk section
instructed; the event bus was **not** modified. Story 3.8 must either record its drained-message
notifications transactionally rather than through the bus, or raise the buffer.

### Files

New — `notification/` slice: `domain/AppNotification.kt`, `domain/NotificationType.kt`,
`domain/AppNotificationRepository.kt`, `domain/NotificationEvent.kt`,
`domain/RecordNotificationUseCase.kt`, `infrastructure/RoomAppNotificationRepository.kt`,
`infrastructure/NotificationRecordingEventHandler.kt`, `infrastructure/room/NotificationEntity.kt`,
`infrastructure/room/NotificationDao.kt`, `infrastructure/room/NotificationDatabase.kt`,
`NotificationModule.kt`. UI: `app/ui/notifications/NotificationsViewModel.kt`,
`NotificationsUiModule.kt`, `NotificationDisplayText.kt`. Tests:
`commonTest/notification/domain/{FakeAppNotificationRepository,RecordNotificationUseCaseTest,NotificationTypeTest}.kt`.
Schema: `app/schemas/.../39.json`.

Modified — `app/ui/notifications/NotificationsScreen.kt` (real body), `app/ui/UiModule.kt`,
`app/di/InitKoin.kt`, `app/infrastructure/room/FoodYouDatabase.kt`,
`app/infrastructure/room/RoomModule.kt`, `shared/resources/.../values/strings.xml`.

Deleted — `app/ui/shell/PlaceholderTabScreen.kt`.

## Completion Review

### Validation

- Targeted tests: `./gradlew.bat :app:testDebugUnitTest` — **passed**. Six new tests
  (`RecordNotificationUseCaseTest` ×3, `NotificationTypeTest` ×3) cover recording, dismiss visibility
  across both queries, the retention sweep sparing undismissed and in-window rows, key round-trip,
  key uniqueness, and unknown-key tolerance. Whole suite green; no pre-existing failures observed.
- Build: `./gradlew.bat :app:assembleDebug` — **passed**.
- Migration evidence: `39.json` committed. Programmatic diff of 38 → 39 shows exactly one added
  table (`Notification`, two indices), zero removed tables, zero view changes, and **zero changes to
  any existing table's `createSql`**.
- On-device upgrade with surviving data: a v38 install (4 meals plus a seeded canary profile row)
  was upgraded in place on emulator `foodyou`. Post-upgrade `PRAGMA user_version` = 39, the canary
  profile and all 4 meals intact, `Notification` table and both indices present, no crash.
- On-device UI: rows seeded directly into the upgraded database and the tab driven through
  UI Automation. Verified — three undismissed rows render newest-first with resolved templates; an
  unknown type key degrades to the neutral text instead of disappearing; a dismissed row is hidden
  by default; per-item dismiss removes a row from the default view; "Show dismissed" reveals all
  four and flips to "Hide dismissed"; dismissed rows show no dismiss affordance; "Dismiss all"
  clears the default view; after a force-stop and relaunch the empty state shows and the full
  history is still there.
- Remaining uncertainty: the recording path from `EventBus.publish` to a visible row was proven at
  the unit level and by direct row insertion, **not** end-to-end on device, because no emitter
  exists yet to publish a `NotificationEvent`. The first emitter story closes that gap. iOS was not
  built or run; all code is `commonMain` with no platform code, so this is a coverage gap rather
  than a suspected defect.

### Acceptance criteria

- Notifications persist across restarts, dismissed items hidden by default with a working history
  control — **met**, verified on device.
- Any code can raise a notification by publishing one `IntegrationEvent`, with no direct store
  access — **met**; `NotificationEvent` is the whole contract.
- No OS notifications, no polling, no network I/O — **met**.
- Existing `EventBus` consumers and Room data unaffected — **met**; the handler filters to its own
  event type, and the schema diff plus the on-device upgrade show the change is purely additive.

Status set to `Complete`. Cross-device emitters arrive with Stories 3.8–3.12; they extend the
taxonomy, not this story.
