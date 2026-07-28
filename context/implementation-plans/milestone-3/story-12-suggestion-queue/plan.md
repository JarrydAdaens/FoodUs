# Plan: Suggestion Queue

## Metadata

- Task Type: `STORY`
- Status: `Draft`
- Owner: Jarryd Adaens
- Last Updated: 28 July 2026

## Linked Context

- Milestone: [context/milestones/milestone-3.md](../../../milestones/milestone-3.md)
- Story: [Story 12: Suggestion queue](../../../milestones/milestone-3.md#story-12) `[STORY 3.12]`
- Dictation source: [2026-07-27 Milestone 3 multiplayer addendum](../../../dictations-tier-0/2026-07-27_milestone-3_multiplayer-addendum.md)
- Design authority: `context/design.md` — "The Multiplayer Exception" (relay model, poll-on-wake, E2E)
- Related Plans:
  - Story 9 (Groups) — `../story-9-groups/plan.md` (planned in parallel this run): owns group + membership storage and the Groups-tab cards this queue hangs off. The suggestion queue is per-group; its foreign key targets Story 9's group entity.
  - Story 11 (Receive into diary) — `../story-11-receive-into-diary/plan.md` (planned in parallel this run): owns the three-tier meal-matching resolver. Accepting a suggestion inserts via that resolver; the accept-time matching semantics are a shared open decision recorded in both plans.
  - Story 8 (Message envelope & E2E pipeline) — **BLOCKED this run** (foodus-relay wire contract v1 is Not Started; the milestone gates Story 8's planning on the unknown-version envelope disposition). This plan consumes Story 8's router strictly as an interface dependency and invents no envelope or wire details.
  - Story 13 (Notification Center) — `../story-13-notification-center/plan.md`: consumes the arrival/outcome events this story emits.
- External Tooling: none required.

**Dependency note (Relay Contract Conformance):** consumes the relay only through Story 8's
pipeline (contract v1); no new server capability of its own.

## CER

- Complexity: 4
- Effort: 5
- Risk: 4
- Notes: Inline estimate. Storage, use cases, and UI all follow well-established repository patterns (`ManualDiaryEntryEntity`-style self-contained nutrition storage, feature-slice Koin modules, Compose list screens), which caps complexity. Effort spans a Room entity + DAO + repository, three use cases, a review UI, and event emission. Risk sits on the two upstream seams that do not exist yet — Story 8's router (blocked on the wire contract) and Story 11's resolver — and on the never-silently-lost queue semantics; both are mitigated by planning them as narrow interfaces owned by this slice's domain layer.

## Objective

Deliver the Suggest-trust receive path: incoming shared entries stack durably in a per-group
suggestion queue; the recipient reviews them one by one, accepting (insert into the diary via
Story 11's three-tier matching resolver) or rejecting (discard locally); every arrival and every
decision emits a Notification Center event. The queue survives app restarts and never silently
loses an entry.

## Scope

### In Scope

- Domain: a `SuggestedEntry` entity (self-contained food payload + entry timestamp + meal name +
  sender/group identity), a repository interface, and use cases: observe queue per group, accept
  one, reject one.
- An inbox seam (domain interface) that Story 8's message router calls to deposit Suggest-trust
  entries — defined here, consumed by Story 8 when it is planned/built.
- Infrastructure: Room entity + DAO + repository implementation; entity registered in
  `FoodYouDatabase` with a schema-version bump and migration.
- UI: suggestion count surfaced on the owning group's card (Story 9's Groups tab), opening a
  per-group review screen with per-item Accept / Reject; ViewModel + Koin wiring + new English
  strings.
- Event emission via the existing `IntegrationEvent` bus on arrival, accept, and reject, for
  Story 13's Notification Center.
- Focused unit tests for accept/reject use cases (never-dropped invariant, resolver delegation).

### Out Of Scope

- The three-tier meal-matching resolver itself — Story 11 owns it; this story only calls it.
- Envelope schema, decryption, polling, and routing — Story 8 owns the pipeline end-to-end.
- Group storage, trust levels, and the Groups-tab card framework — Story 9.
- Notification Center storage and UI — Story 13; this story only emits events.
- Any relay/server calls of its own; any reject-notification message back to the sender.

## Non-Goals

- No batch accept-all / reject-all actions (per-item review is the story's stated behavior).
- No editing of a suggested entry before accepting it (accept inserts as sent; edits happen in
  the diary afterwards).
- No retention/archive UI for decided suggestions — the Notification Center event is the
  durable trace.

## Current Understanding

Verified in the working tree on 28 July 2026.

- **Storage shape precedent:** `app/src/commonMain/kotlin/com/maksimowiczm/foodyou/fooddiary/infrastructure/room/ManualDiaryEntryEntity.kt` — a fully self-contained entry (name + `@Embedded` `Nutrients`/`Vitamins`/`Minerals` from `common/infrastructure/room`, plus `servingCount`/`weightGrams` context). This is exactly the shape a Suggest-trust payload needs, since Story 8's packets are self-contained by design (recipient may not share the sender's data sources). The suggested-entry entity mirrors it, adding group/sender identity, the sender's entry timestamp, and the sender's meal name.
- **Database registration:** `app/src/commonMain/kotlin/com/maksimowiczm/foodyou/app/infrastructure/room/FoodYouDatabase.kt` — entities list, `VERSION = 36`, manual `migrations` list (fork precedent: `PlaceholderDiaryEntryMigration`, `MealTemplateMigration`, `QuickAddExpansionMigration` under `app/infrastructure/room/migration/`). New entity registers here with a version bump.
- **Insertion seam:** `fooddiary/domain/usecase/CreateFoodDiaryEntryUseCase.kt` shows the transaction + repository insertion pattern; Story 11's resolver will own the actual accept-time insertion (this story passes it the stored payload, timestamp, and meal name).
- **Event bus:** `common/domain/event/IntegrationEvent.kt`, `IntegrationEventHandler.kt`, and `common/infrastructure/inmemory/SharedFlowEventBus.kt`; slice precedent `fooddiary/domain/event/FoodDiaryEntryCreatedEvent.kt`. Arrival/accept/reject events follow this pattern.
- **Slice composition:** `fooddiary/FoodDiaryModule.kt` composes `foodDiaryDomainModule()` + `foodDiaryInfrastructureModule()`; the social slice Story 9 establishes will follow the same pattern, and this story's pieces register in that slice's modules.
- **Home of this feature:** Story 9's plan (parallel) establishes the social/groups feature slice and package. Narrowest credible subsystem: that slice. Discovery step at execution time: adopt Story 9's actual package and module names rather than minting a second social slice.
- **Behaviors to preserve:** existing diary insertion paths and `fooddiary` slice behavior are untouched; the queue only writes to the diary through Story 11's resolver at accept time.
- **Assumptions and constraints:** fork philosophy (new files additive, upstream edits minimal — the only shared-file edits are `FoodYouDatabase.kt` registration lines and string resources); laws.md §2 — the queue stores plaintext food data locally only after Story 8 has decrypted it on-device, which matches the local-first boundary.

## Questions / Unknowns

- Q: `[STORY 3.12]` (shared with Story 3.11) Does Suggest-queue accept apply the same three-tier matching at accept time using the **original sender timestamp** (not the accept-time clock)?
  Impact: Determines what this story passes to Story 11's resolver and whether a stale suggestion (accepted days later) lands on the sender's original date/meal slot or the recipient's current one.
  Assumption: Yes — original timestamp and sender meal name, exactly as a Full-trust delivery would have used them (flagged during dictation; owner confirmation staged in the milestone).
  Status: OPEN
  Answer: —

- Q: `[STORY 3.12]` Where does the review UI surface — expansion of the owning group's card on the Groups tab, or a dedicated screen navigated from it?
  Impact: Decides whether Story 9's group card gains an inline list region or just a "N suggestions" affordance plus a nav route.
  Assumption: A suggestion-count affordance on the group card opening a dedicated review screen — the story's "tied to the group" wording anchors entry at the group, and a dedicated screen keeps per-item Accept/Reject ergonomics clean for stacks of entries.
  Status: OPEN
  Answer: —

- Q: `[STORY 3.12]` Reject semantics: purely local discard, or is the sender ever informed?
  Impact: A sender-notification would require a new message type over Story 8's pipeline (new cross-device scope, contract-relevant).
  Assumption: Purely local discard — no story or dictation defines a rejection message, and adding one would expand the wire contract; the recipient-side Notification Center event is the only record.
  Status: OPEN
  Answer: —

- Q: `[STORY 3.12]` Are decided (accepted/rejected) suggestions retained in the queue store?
  Impact: Entity lifecycle and whether the DAO needs status filtering vs deletion.
  Assumption: Deleted on decision. The accept path's diary entry and the emitted Notification Center events (persistent per Story 13) are the durable traces; retaining decided rows would duplicate that history.
  Status: OPEN
  Answer: —

## Execution Steps

1. Alignment discovery
   - Why: Three seams are owned by parallel plans and must be adopted, not duplicated.
   - Edits: none. Confirm Story 9's slice package/module names, Story 11's resolver interface signature, and (once Story 8 is planned) the router's dispatch contract for Suggest-trust messages.
   - Dependencies: Story 9 and Story 11 plans; Story 8 plan when unblocked.

2. Domain layer
   - Why: The queue's rules (never silently lost, per-item decisions) live above persistence.
   - Edits: `SuggestedEntry` domain entity; `SuggestedEntryRepository` interface; `SuggestedEntryInbox` interface (the deposit seam Story 8's router will call); use cases `ObserveGroupSuggestionsUseCase`, `AcceptSuggestionUseCase` (delegates insertion to Story 11's resolver inside a transaction, then deletes the row and emits the outcome event), `RejectSuggestionUseCase` (deletes + emits); arrival/accept/reject `IntegrationEvent` types.
   - Dependencies: step 1.

3. Infrastructure layer
   - Why: Durable storage so the queue survives restarts.
   - Edits: `SuggestedEntryEntity` (mirrors `ManualDiaryEntryEntity`'s embedded nutrition shape + `groupId` FK to Story 9's group entity, sender GUID, `entryEpochSeconds`, `senderMealName`, `receivedEpochSeconds`); DAO; Room repository implementing both `SuggestedEntryRepository` and `SuggestedEntryInbox`; registration in `FoodYouDatabase.kt` entities list with version bump and migration (coordinate the bump order with the other Milestone-3 storage stories at execution time).
   - Dependencies: step 2; Story 9's group entity must exist first at execution time.

4. UI layer
   - Why: The recipient needs a review surface.
   - Edits: suggestion-count affordance on the group card (small addition inside Story 9's card composable); `SuggestionQueueScreen` + ViewModel (per-item Accept/Reject, empty state); nav route registration; Koin viewModel wiring; new English strings in `shared/resources` base `strings.xml`.
   - Dependencies: steps 2–3; Story 9's Groups tab exists.

5. Event wiring
   - Why: Story 13's Notification Center is the app's only broadcast surface; arrivals and outcomes must be visible there.
   - Edits: emit arrival event from the inbox deposit path and outcome events from the accept/reject use cases via the `EventBus`.
   - Dependencies: steps 2–4; Story 13 consumes, but emission is independent.

6. Tests
   - Why: The never-dropped invariant and resolver delegation are the story's stable business rules.
   - Edits: `commonTest` unit tests — accept removes the row only after successful resolver insertion (failure leaves the row queued), reject removes exactly the targeted row, inbox deposit persists and emits.
   - Dependencies: steps 2–3.

## Validation

### Automated Checks

- `commonTest` unit tests above (targeted, per the unit-testing limits rule — no mock-heavy pipeline simulation).
- Debug build compiles (`assembleDebug` or the toolchain's established build task).

### Manual Checks

1. Seed a suggestion via the inbox seam (test hook or Story 8 once live); verify it appears on the group card count and in the review screen after an app restart.
2. Accept a suggestion → entry appears in the diary via Story 11's matching; suggestion gone from the queue; Notification Center event present.
3. Reject a suggestion → gone from the queue, diary untouched, event present.

### Acceptance Criteria

- Suggest-trust entries persist in a per-group queue across app restarts until explicitly decided.
- Accept inserts into the diary through Story 11's resolver using the sender's original timestamp and meal name (per the shared assumption, pending owner confirmation).
- A resolver failure never deletes the queued suggestion — nothing is silently lost.
- Reject discards locally only.
- Existing diary behavior is unchanged when the queue is empty or unused.

## Risk Mitigation

- Risk: Story 8's router contract is unplanned (blocked on wire contract v1); its dispatch shape may not match the inbox seam.
  Mitigation: The seam is a one-method domain interface taking an already-decrypted, self-contained payload — the narrowest surface Story 8 could need; revisit this plan's step 2 when Story 8 is planned.
- Risk: Parallel Milestone-3 stories (9, 12, 13, and possibly 2) all bump the Room schema version.
  Mitigation: Sequence migrations at execution time in interdependency order (9 before 12); each story rebases its version bump on the then-current `VERSION`.
- Risk: Accept-time matching semantics are an open owner decision shared with Story 11.
  Mitigation: Plan held at `Draft`; both plans carry the same assumption so a single owner answer resolves both consistently.
- Risk: Divergent duplicate storage shape vs Story 11's insertion input.
  Mitigation: Both derive from the `ManualDiaryEntryEntity` nutrition shape; alignment check in step 1.

## Phase Split

Not needed — the story fits a single execution pass once Stories 8, 9, and 11 exist.

## Evidence / References

- Planning inputs verified 28 July 2026: `FoodYouDatabase.kt` (entities list, VERSION 36, migration precedent), `ManualDiaryEntryEntity.kt` (self-contained nutrition shape), `CreateFoodDiaryEntryUseCase.kt` (transactional insertion pattern), `IntegrationEvent.kt` / `SharedFlowEventBus.kt` (event bus), `FoodDiaryModule.kt` (slice composition).
- foodus-relay repo read-only check: wire contract v1 = that repo's Milestone 3 Story 1, Not Started (grounds the Story 8 interface-only stance).
