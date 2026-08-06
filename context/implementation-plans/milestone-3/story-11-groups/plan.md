# Plan: Groups

## Metadata

- Task Type: `STORY`
- Status: `Draft`
- Owner: Jarryd Adaens
- Last Updated: 28 July 2026

## Linked Context

- Milestone: [context/milestones/milestone-3.md](../../../milestones/milestone-3.md)
- Story: [Story 11: Groups](../../../milestones/milestone-3.md#story-11) `[STORY 3.11]`
- Dictation source: [2026-07-27 Milestone 3 multiplayer addendum](../../../dictations-tier-0/2026-07-27_milestone-3_multiplayer-addendum.md) (trust levels, invitation lifecycle, two-member cap)
- Design authority: `context/design.md` — "The Multiplayer Exception" (relay model, E2E, poll-on-wake) and the Relay Contract Conformance section of the milestone
- Related Plans:
  - Story 1 (tabbed UI shell) — `../story-01-tabbed-ui-shell/plan.md`: the Groups tab this story's cards live in.
  - Story 2 (profile) — `../story-02-profile/plan.md`: the local profile GUID identifying "me" in a group.
  - Story 9 (friends list) — `../story-09-friends-list/plan.md`: groups are created from stored friends (GUID + username + public key).
  - Story 10 (message envelope & E2E pipeline) — **plan intentionally absent this run** (planning blocked on foodus-relay wire contract v1). Invite lifecycle messages ride that pipeline; this plan treats it strictly as an interface dependency.
  - Story 5 (Notification Center) — `../story-05-notification-center/plan.md`: consumer of the group lifecycle events this story emits.
- External Tooling: none required.

## CER

- Complexity: 6
- Effort: 7
- Risk: 6
- Notes: Inline estimate. Complexity sits in the invitation lifecycle state machine (invited/accepted/rejected/blocked/left across two devices that only sync at poll time) and trust-level semantics. Effort spans a new feature slice: Room schema + migration in the aggregated `FoodYouDatabase` (version bump from 36), repositories, a five-step create form, group cards with per-state rendering, and the group blacklist. Risk is elevated because the messaging seam (Story 10) is unplanned and contract-gated, and two staged owner decisions remain open — this plan must stay `Draft` until they resolve.

## Objective

Deliver the trust container for cross-diary logging: a two-member group model persisted in Room, a five-step create form fed by a searchable friend picker, trust levels fixed at creation (Full / Suggest / None), the full invitation lifecycle (invite, accept, reject, block, leave, leave-and-block) rendered as Groups-tab cards, and a group blacklist — with all cross-device lifecycle messages expressed as domain intents handed to Story 10's pipeline seam.

## Scope

### In Scope

- New `groups` feature slice (`groups/domain`, `groups/infrastructure`, `groups/GroupsModule.kt`) following the established slice + Koin pattern (`fooddiary` is the reference).
- Group domain model: group GUID, name (mandatory), optional description, trust level, created/last-edited dates, member collection with per-member state (pending/accepted/left/blocked-out), hard two-member cap (creator + exactly one friend; members equal, ownership deliberately loose).
- `TrustLevel` enum with `None` as the zero value (presence only, no defined use case yet), `Suggest`, `Full`.
- Room storage: `GroupEntity`, `GroupMemberEntity`, `GroupBlacklistEntity` + DAOs behind a `GroupsDatabase` interface, aggregated into `FoodYouDatabase` (entity registration, version 36 → 37, manual or auto migration per existing conventions).
- Five-step create form: name (mandatory), description (optional), trust level (mandatory, set once — immutable after creation), searchable picker selecting exactly one friend, Create button with are-you-sure-guarded Cancel.
- Groups-tab cards: create entry point, per-group card (invited member renders grey), incoming-invite card with Accept / Reject (Reject offers Reject / Block / Cancel), post-accept expanded card with Leave and Leave-and-Block; a departed group renders dead/dormant.
- Group blacklist: blocking a group suppresses all its notifications forever and refuses re-adds; surface the "can't be added" outcome to the adder (wording gated — see Questions).
- Invitation lifecycle as domain intents (invite, accept, reject, block-notice, leave) published to Story 10's send seam and consumed from its router seam — interface only, no envelope schemas, no wire details.
- `IntegrationEvent`s (existing `common/domain/event` EventBus) for every lifecycle outcome so Story 5's Notification Center can materialize notifications.

### Out Of Scope

- The envelope schema, encryption, send queue, poll/drain, and message routing — Story 10 (blocked on contract v1).
- Sending or receiving diary entries (Stories 12–14); this story only establishes the trust container they authorize against.
- Friend storage and add-by-code (Story 9); profile creation (Story 2); tab shell (Story 1).
- Any foodus-relay/server work — groups consume the relay only through Story 10's pipeline; no new server capability (dependency note per Relay Contract Conformance).

## Non-Goals

- No N-person groups, per-member trust levels, all-pairs friendship rule, or live member-list updates (deferred to backlog-1 Story 10).
- No trust-level editing after creation.
- No group discovery, search, or any server-side group registry — groups exist only on the two members' devices.

## Current Understanding

Verified in the working tree on 28 July 2026.

- **Slice pattern:** `fooddiary/` is the model — `domain/{entity,event,repository,usecase}`, `infrastructure/{room,repository}`, composed by `fooddiary/FoodDiaryModule.kt` (`foodDiaryDomainModule() + foodDiaryInfrastructureModule()`), registered in `app/src/commonMain/kotlin/com/maksimowiczm/foodyou/app/di/InitKoin.kt`. The new `groups` slice mirrors this exactly.
- **Room aggregation:** `app/infrastructure/room/FoodYouDatabase.kt` — single `@Database` (VERSION = 36) listing every slice entity, implementing per-slice database interfaces (e.g. `FoodDiaryDatabase` exposing DAOs); migrations are named objects under `app/infrastructure/room/migration/` added to the `migrations` list (recent precedent: `QuickAddExpansionMigration`, `MealTemplateMigration`); `exportSchema = true` with the schema dir set in `app/build.gradle.kts`. Adding group entities means: new `GroupsDatabase` interface, entities in the `@Database` list, VERSION 37, migration object.
- **Entity convention:** plain data classes with `@Entity(tableName = ...)`, e.g. `fooddiary/infrastructure/room/MealEntity.kt`; DAOs per aggregate.
- **Event seam:** `common/domain/event/{IntegrationEvent, EventBus, IntegrationEventHandler}` with slice events like `FoodDiaryEntryCreatedEvent` — group lifecycle events follow this shape and are the contract Story 5 consumes.
- **UI/navigation:** screens live under `app/ui/<area>/` wired through `app/ui/UiModule.kt`; routes are `@Serializable` objects in `app/navigation/FoodYouAppNavHost.kt` hosted via `forwardBackwardComposable<T>` (e.g. `AiSettings` at line 121). The Groups tab surface itself is Story 1's; this story contributes the composables rendered inside it plus a create-form route.
- **Strings:** English base strings added to `shared/resources/src/commonMain/composeResources/values/strings.xml` (fork-additive keys only).
- **Interfaces this plan depends on but does not own:**
  - Friend read model (GUID, username, public key) — Story 9's repository.
  - Local profile GUID — Story 2's repository.
  - `GroupMessageSender` / router registration seam — Story 10 (unplanned; represented here as domain-level ports the groups slice defines and Story 10 later implements, keeping the dependency arrow pointed at the pipeline without inventing wire details).
- **Constraints:** fork philosophy (new files in new packages; upstream-file edits limited to `FoodYouDatabase.kt`, `InitKoin.kt`, nav host, strings — all additive); laws.md §2 data boundaries (nothing leaves the device except through Story 10's sealed pipeline); two-member cap is constitutional for this milestone.

## Questions / Unknowns

- Q: `[STORY 3.11]` **Group-block notice wording (staged owner decision — resolve before shipping the notice).** The dictated text ("please respect Emily's decision to stay out of this group") reveals the blocker's username to the adder. What wording ships — the dictated text, an anonymized variant ("this person can't be added"), or no notice beyond a generic failure?
  Impact: Decides whether block status/identity leaks to the adder; shapes the blacklist response intent and its UI string. Owner already acknowledged the flag in the milestone.
  Assumption: Anonymized wording ("this member can't be added to the group") — leaks nothing while still telling the adder the add failed; the dictated sentence is treated as intent ("communicate refusal"), not literal copy.
  Status: OPEN
  Answer: —

- Q: `[STORY 3.11]` **Meal-plan editing scope (staged owner decision — confirm before execution).** The dictation grants Full trust the ability to "edit each other's meal plans" (A6), but no story in the milestone delivers that capability — every send/receive story covers diary entries only. What does meal-plan editing mean concretely, and which story owns it (extend this story, extend Stories 12–13, or a new story)?
  Impact: If it lands here, the group model may need extra authorization state and this plan's scope grows materially; if elsewhere, this plan only documents that Full trust implies it.
  Assumption: Not this story. This plan defines Full trust's authorization semantics only for diary-entry write access; meal-plan editing is treated as undelivered scope awaiting an owner ruling.
  Status: OPEN
  Answer: —

- Q: `[STORY 3.11]` How is the "told they can't be added" outcome transported to the adder — a response message from the blocker's device via the pipeline, or a local check against state the adder already holds?
  Impact: A relayed refusal needs a message intent (and poll-latency tolerance); a local check is impossible because the blacklist lives on the blocker's device only.
  Assumption: A refusal intent sent from the blocker's device over Story 10's pipeline on next poll; the adder's group card shows pending-then-refused. Exact envelope handling deferred to Story 10.
  Status: OPEN
  Answer: —

- Q: `[STORY 3.11]` What can the user do with a dead/dormant group (after a member leaves)?
  Impact: Decides retention UI — keep the card greyed forever, allow local delete, or auto-archive.
  Assumption: The card renders dormant with a local Delete affordance; deleting is local-only and sends nothing.
  Status: OPEN
  Answer: —

## Execution Steps

1. Create the `groups` slice skeleton (domain entities, `TrustLevel`, repository interfaces, `GroupsModule.kt`) and register it in `InitKoin.kt`.
   - Why: Establishes the slice per repository conventions before any storage or UI.
   - Edits: new files under `groups/`; one additive line in `InitKoin.kt`.
   - Dependencies: none.

2. Add Room storage: `GroupEntity`, `GroupMemberEntity`, `GroupBlacklistEntity`, DAOs, `GroupsDatabase` interface; aggregate into `FoodYouDatabase` with VERSION 37 and a migration object.
   - Why: Groups must ride backup/restore like all social state.
   - Edits: new files under `groups/infrastructure/room/`; additive edits to `FoodYouDatabase.kt`; new migration file.
   - Dependencies: step 1.

3. Implement repositories + use cases: create group (enforcing the two-member cap and immutable trust level), observe groups, accept/reject/leave transitions, blacklist add/check.
   - Why: Business rules live in domain, testable without UI or relay.
   - Edits: `groups/domain/usecase/*`, `groups/infrastructure/repository/*`.
   - Dependencies: step 2; Story 9's friend read model and Story 2's profile GUID (compile-time interfaces).

4. Define the pipeline-facing ports: outbound `GroupLifecycleIntent` (invite, accept, reject, refusal, leave) publisher interface and an inbound handler interface for routed group messages. No serialization, no envelope fields.
   - Why: Keeps this story buildable while Story 10 is blocked; the ports are the seam Story 10 implements once contract v1 exists.
   - Edits: `groups/domain/` port interfaces + a no-op/queuing stub binding in `GroupsModule.kt`.
   - Dependencies: step 3. Marked uncertainty: signatures may shift when Story 10 is planned.

5. Build the create form: five steps (name, description, trust level, searchable single-friend picker, Create/guarded Cancel) as a nav route from the Groups tab.
   - Why: The only way a group comes into existence.
   - Edits: `app/ui/groups/creategroup/*`, route in `FoodYouAppNavHost.kt`, strings.
   - Dependencies: steps 3–4; Story 1's tab shell.

6. Build the Groups-tab cards: group list card states (pending-grey, active, dormant), incoming-invite card (Accept / Reject → Reject/Block/Cancel sheet), expanded card (Leave / Leave-and-Block).
   - Why: The full user-visible lifecycle.
   - Edits: `app/ui/groups/*`; strings.
   - Dependencies: step 5.

7. Emit `IntegrationEvent`s for every lifecycle outcome (invited, accepted, rejected, refused, left, group renamed if description/name edits are kept) for Story 5.
   - Why: Notification Center is the app's single broadcast surface.
   - Edits: `groups/domain/event/*`; publications from use cases.
   - Dependencies: step 3.

8. Unit tests for the domain rules: two-member cap enforcement, trust-level immutability, lifecycle state transitions, blacklist suppression.
   - Why: These are the stable business rules; the pipeline seam is stubbed.
   - Edits: `app/src/commonTest/.../groups/`.
   - Dependencies: steps 3–4.

## Validation

### Automated Checks

- `./gradlew :app:testDebugUnitTest` (or the commonTest equivalent target) — domain rule tests above.
- Room schema export diff shows exactly the new tables; migration test per existing migration-test conventions if present.
- `./gradlew :app:assembleDebug` compiles with the pipeline ports stubbed.

### Manual Checks

1. Create a group end-to-end on one device: five-step form, cap enforced (picker allows exactly one friend), Cancel guarded.
2. Invited-state rendering: creator sees the member grey until acceptance (stubbed locally until Story 10 delivers transport).
3. Reject sheet shows Reject / Block / Cancel; Block adds to blacklist; subsequent invite intents are suppressed locally and no notification fires.
4. Leave and Leave-and-Block from the expanded card; group renders dormant afterward.

### Acceptance Criteria

- A group persists across app restart and rides backup/restore (Room only).
- Trust level is immutable after creation; `None` is the enum zero value.
- Two-member cap cannot be violated through any UI or repository path.
- A blacklisted group can never notify again; re-add attempts surface the refusal outcome.
- All lifecycle events reach the EventBus for Story 5.
- Cross-device behavior is expressed only through the Story 10 ports — no wire-contract details exist anywhere in this slice.

## Risk Mitigation

- Risk: Story 10 is unplanned; its eventual interface may not match the ports defined here.
  Mitigation: Ports are minimal domain intents (no serialization, no transport assumptions); revisit signatures in the same sitting Story 10 is planned. Local-first steps 1–3 and 5–8 carry no exposure.
- Risk: The two staged owner decisions change scope (notice wording; meal-plan editing possibly landing here).
  Mitigation: Plan held at `Draft`; wording is one string resource; meal-plan editing is fenced out of the model unless the owner rules otherwise.
- Risk: Room migration on the aggregated database (version 37) breaks existing installs.
  Mitigation: Additive tables only; follow the existing migration-object pattern; verify schema export and upgrade path on a device with real data before merge.
- Risk: Upstream merge surface (`FoodYouDatabase.kt`, `InitKoin.kt`, nav host, strings are upstream-shared files).
  Mitigation: Additive-only edits at established extension points, matching how milestone-2 stories touched the same files.

## Phase Split

Suggested (not yet fractured — re-grade once the owner decisions and Story 10's plan land):

- Phase 1 — local trust container: slice, storage, rules, create form, cards, events (steps 1–3, 5–8) with the pipeline ports stubbed.
- Phase 2 — cross-device lifecycle: bind the ports to Story 10's pipeline once it is planned and contract v1 exists (step 4 binding + invite/accept/refusal round-trips).

## Evidence / References

- Conventions verified: `fooddiary/FoodDiaryModule.kt`, `fooddiary/infrastructure/room/` (entity/DAO shapes), `app/infrastructure/room/FoodYouDatabase.kt` (VERSION 36, migration list, slice-interface aggregation), `common/domain/event/EventBus.kt` + `FoodDiaryEntryCreatedEvent.kt` (event seam), `app/navigation/FoodYouAppNavHost.kt` (route pattern, `AiSettings` precedent), `app/di/InitKoin.kt` (module registration).
- foodus-relay repo (read-only): wire contract v1 is that repo's Milestone 3 Story 1, Not Started — confirms the pipeline seam must stay abstract.

## Complaints / Friction

### Messaging dependency is unplanned by design

**What happened:** Story 10's plan is intentionally absent this run (planning gate on the wire contract), yet this story's invitation lifecycle is inherently cross-device.
**Why this made the task harder:** The pipeline seam had to be designed blind — domain ports with no knowledge of eventual envelope or router shape.
**What was tried:** Read the foodus-relay repo's milestone to confirm contract status; kept every cross-device concern behind two small interfaces.
**What would improve this:** Plan Story 10 immediately after contract v1 lands and reconcile the port signatures in the same sitting.
**What I think:** The Phase 1/Phase 2 split above makes Phase 1 safely executable before Story 10 exists; recommend executing in that order.
