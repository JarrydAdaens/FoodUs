# Plan: Save to Group

## Metadata

- Task Type: `STORY`
- Status: `Draft`
- Owner: Jarryd Adaens
- Last Updated: 28 July 2026

## Linked Context

- Milestone: [context/milestones/milestone-3.md](../../../milestones/milestone-3.md)
- Story: [Story 12: Save to Group](../../../milestones/milestone-3.md#story-12) `[STORY 3.12]`
- Design authority: `context/design.md` — "The Multiplayer Exception" (relay model, E2E, poll-on-wake, Relay Contract Conformance)
- Related Plans:
  - Story 11 (Groups) — **depends on it**: supplies the group Room storage, trust levels, and membership this screen lists. Planned in parallel at `context/implementation-plans/milestone-3/story-11-groups/`.
  - Story 10 (Message envelope & E2E pipeline) — **depends on it**: supplies the send path. Its plan is BLOCKED this run (wire contract v1 is Not Started in foodus-relay); this plan consumes it strictly as an interface dependency and invents no envelope or wire details.
  - Story 7 (Configurable relay URL setting) — capability gating source: the Save to Group surface hides or greys when the relay is unset/unreachable per the capability-aware UI rule.
- Dependency note (Relay Contract Conformance rule 6): consumes the relay **only through Story 10's pipeline** (contract v1); no new server capability of its own. Dependency arrow app → server; owner releases via the Story 4 gate.
- External Tooling: none required.

## CER

- Complexity: 5
- Effort: 4
- Risk: 5
- Notes: Inline estimate. The sending UI itself is a contained Compose addition to one screen, and the fan-out logic (checked groups → one recipient each) is simple. Complexity comes from reconciling the dictated left/right button layout with the screen's actual FAB-stack idiom, and from defining a clean consumer-side port onto Story 10's not-yet-planned pipeline without inventing wire details. Risk is elevated because the story's execution is gated behind two other plans (8, 9) and a blocked contract, and because `AddEntryScreen.kt` is an upstream-shared file where edits must stay additive for merge safety.

## Objective

Add a Save to Group action to the food-entry screen: alongside the existing Save, a group-selection card (user's groups with checkboxes) and a Save to Group button that saves the entry locally and hands a fully self-contained copy of it to the messaging pipeline for every member of every checked group — one recipient per two-person group, fanning out across multiple checked groups — with per-group delivery semantics owned by the group's trust level downstream.

## Scope

### In Scope

- Group-selection card UI above the save controls on the add-entry screen: user's groups with checkboxes, collapsed/absent when the user has no groups or the relay is unset (capability gating per Story 7).
- New Save to Group action on `AddEntryScreen` positioned per the story (Save stays right; Save to Group on the left), adapted to the screen's existing FAB/scaffold idiom.
- ViewModel wiring: on Save to Group, persist the entry locally (existing `CreateFoodDiaryEntryUseCase` path), then invoke a new consumer-side send port once per checked group's other member.
- A domain-level send port (e.g. a `SendEntryToGroup`-shaped use case interface in the fork's multiplayer slice) that carries: the complete entry payload source (`DiaryFood` nutrition + name), entry timestamp, meal name, and target group/member identity. **Interface only** — its implementation, envelope schema, queuing, and encryption are Story 10's.
- New English strings (fork-owned keys) in the shared resources base `strings.xml`.

### Out Of Scope

- The packet schema, encryption, send queue, retry/offline semantics, and any relay endpoint interaction — all Story 10 (blocked on contract v1).
- Group data model, trust levels, membership storage — Story 11.
- Recipient-side delivery behavior (Full auto-insert, Suggest queue) — Stories 13 and 14.
- The relay URL setting and capability check — Story 7.

## Non-Goals

- No N-recipient group support beyond the two-member cap (one recipient per group).
- No per-send trust overrides; delivery behavior follows the group's trust level set at creation.
- No send-status tracking UI beyond what Story 5's Notification Center later surfaces.

## Current Understanding

All paths verified in the working tree on 28 July 2026.

- **Primary surface:** `app/src/commonMain/kotlin/com/maksimowiczm/foodyou/app/ui/food/diary/add/AddEntryScreen.kt` — save is currently a `LargeExtendedFloatingActionButton` in a bottom-end FAB column (lines ~206-254), with an optional Unpack FAB above it; content padding compensates for FAB height (lines ~262-269). The dictated "big button left / Save right + card above" layout must be adapted into this Scaffold/FAB idiom (see Questions).
- **ViewModel:** `app/ui/food/diary/add/AddEntryViewModel.kt` — `addEntry(measurement, mealId, date)` calls `CreateFoodDiaryEntryUseCase`, publishes `FoodDiaryEntryCreatedEvent` on the common `EventBus`, then emits `AddEntryEvent.EntryAdded`. The Save to Group path can reuse this exact persistence flow and add the fan-out send after local success.
- **Entry payload source:** `fooddiary/domain/entity/DiaryFood.kt` (+ `DiaryFoodProduct`/`DiaryFoodRecipe`) is the self-contained food snapshot the diary already stores — the natural source for the "complete nutritional information, name" the packet needs; meal name resolves from `fooddiary/domain/entity/Meal.kt` via `MealRepository`.
- **Existing event seam:** `fooddiary/domain/event/FoodDiaryEntryCreatedEvent.kt` on `common/domain/event/EventBus` — an alternative wiring (listen-and-send) exists, but an explicit call from the ViewModel keeps "Save" and "Save to Group" behaviorally distinct, which the story requires.
- **Other entry paths:** `quickadd/` (manual/placeholder entries), `fasttext/`, `update/` screens also create or edit entries. The story text says "the food-entry screen" (singular); scope is assumed to be `AddEntryScreen` only (see Questions).
- **Groups source:** Story 11 plan (parallel) owns group + membership Room storage and trust levels; this plan consumes an observe-groups query for the checkbox card.
- **Constraints:** fork philosophy — `AddEntryScreen.kt`/`AddEntryViewModel.kt` are upstream files; keep edits additive and small, with new composables and the send port in new fork-owned files. Relay Contract Conformance binds the downstream pipeline, not this UI, but capability gating (hide/grey when relay unset) applies here.

## Questions / Unknowns

- Q: `[STORY 3.12]` Do None-trust groups appear in the Save-to-Group checkbox list — hidden entirely, or shown greyed out? *(Staged open decision from the milestone; owner input wanted.)*
  Impact: Decides the group-list query filter and whether a disabled-checkbox state exists; also sets the precedent for how None trust surfaces anywhere in the UI.
  Assumption: Hidden — None trust has "no defined use case yet", and greyed-out rows invite taps that do nothing.
  Status: OPEN
  Answer: —

- Q: `[STORY 3.12]` Does Save to Group also save the entry into the sender's own diary, or only transmit?
  Impact: Changes the ViewModel flow (persist-then-send vs send-only) and the marquee "log once, lands in both diaries" behavior.
  Assumption: Yes — it is Save *and* share: the entry persists locally exactly as Save does, then fans out. This matches the milestone's "a shared meal is logged once and lands in both diaries".
  Status: OPEN
  Answer: —

- Q: `[STORY 3.12]` Which entry-creation surfaces get Save to Group? `AddEntryScreen` only, or also quick-add/manual entries (`quickadd/`), fast text, and entry edits (`update/`)?
  Impact: Multiplies UI and ViewModel touch points; edits raise re-send semantics questions that belong to a later story.
  Assumption: `AddEntryScreen` only for this story; other surfaces are follow-up work if the household wants them.
  Status: OPEN
  Answer: —

- Q: `[STORY 3.12]` The dictated layout ("big Save to Group button on the left, Save on the right, card above") conflicts with the screen's current single-FAB-column idiom. Literal two-button bottom bar, or FAB-idiom adaptation (e.g. secondary FAB / bottom action row)?
  Impact: Determines how invasive the `AddEntryScreen` scaffold change is and how far the fork drifts from upstream layout (merge surface).
  Assumption: Keep the Scaffold FAB for Save untouched; introduce a fork-owned bottom action area only when groups exist, so the upstream layout is unchanged for group-less users.
  Status: OPEN
  Answer: —

- Q: `[STORY 3.12]` Behavior when the relay is unreachable at send time (send queue? fail loudly?).
  Impact: User-visible reliability of the marquee feature.
  Assumption: Owned by Story 10's pipeline (send queue semantics); this UI only reports what the port returns and never blocks local save. Dependency, not invention.
  Status: OPEN
  Answer: —

## Execution Steps

1. Define the consumer-side send port in the fork's multiplayer domain (new file, fork-owned).
   - Why: Lets this story compile and be UI-complete while Story 10 is blocked; the port is the seam the pipeline later implements.
   - Edits: new interface (e.g. `SendDiaryEntryToGroupMember`) taking recipient identity, group id, `DiaryFood` snapshot, entry timestamp, meal name; a no-op/queued stub binding in the fork's Koin module until Story 10 lands.
   - Dependencies: naming/ownership aligned with Story 11's group model; revisit signature when Story 10 is planned.

2. Add an observe-user-groups query consumption for the checkbox card.
   - Why: The card lists the user's groups with checkboxes.
   - Edits: inject Story 11's group repository/use case into `AddEntryViewModel`; expose `StateFlow` of selectable groups (filtered per the None-trust decision) + checkbox state.
   - Dependencies: Story 11 storage exists.

3. Build the group-selection card composable (new fork-owned file under `app/ui/food/diary/add/`).
   - Why: Keeps `AddEntryScreen.kt` edits to a single insertion point.
   - Edits: card with group checkboxes, rendered only when groups exist and relay capability allows (Story 7 gating); persisted checkbox state across recomposition (`rememberSaveable`/ViewModel).

4. Add the Save to Group action to `AddEntryScreen`.
   - Why: The story's visible feature.
   - Edits: additive change wiring a new `onAddToGroups` callback through the existing screen entry point; layout per the resolved UI question (assumption: fork-owned bottom action area when groups exist); FAB Save untouched.
   - Dependencies: steps 2-3.

5. Implement the ViewModel fan-out.
   - Why: One tap → local save + one send per checked group's other member.
   - Edits: `addEntryToGroups(...)` in `AddEntryViewModel`: run existing create-entry flow; on success, for each checked group resolve the other member and call the send port; emit existing `EntryAdded` event (plus a "sent to N groups" UI event if trivially supportable).
   - Dependencies: steps 1-2; local-save-first per the recorded assumption.

6. Strings and polish.
   - Edits: fork-owned keys in `shared/resources/src/commonMain/composeResources/values/strings.xml` (button label, card title, sent confirmation).

## Validation

### Automated Checks

- `commonTest` unit test for the fan-out selection logic (checked groups → distinct recipient set; None-trust filtering per decision; empty selection → no sends).
- Build: assemble the debug variant to confirm the KMP/Compose additions compile.

### Manual Checks

1. With no groups: add-entry screen renders exactly as upstream (no card, no new button).
2. With groups: card lists them, checkbox state holds, Save alone does not transmit.
3. Save to Group with two checked groups triggers local save once and two port invocations (stub-logged until Story 10 exists).
4. Relay unset (Story 7): Save to Group surface hidden/greyed.

### Acceptance Criteria

- Save behavior for group-less users is byte-for-byte unchanged.
- Save to Group persists locally and invokes the send port once per checked group's other member.
- No envelope, endpoint, or crypto detail exists in this story's code — everything relay-facing goes through the Story 10 port.

## Risk Mitigation

- Risk: Story 10's real pipeline interface diverges from the port defined here.
  Mitigation: Keep the port minimal (recipient, group, snapshot, timestamp, meal name — exactly the story-mandated packet content); adjust in the same sitting Story 10 is planned, before any implementation.
- Risk: Upstream merge conflicts on `AddEntryScreen.kt` / `AddEntryViewModel.kt`.
  Mitigation: New composables/port live in new files; upstream files gain single-insertion-point additive edits only.
- Risk: UI decision (layout, None-trust visibility) reverses after implementation.
  Mitigation: Plan stays `Draft` until the owner answers the OPEN questions; card/button composables isolated so layout swaps are local.

## Phase Split

Not needed — single-screen feature with a stubbed downstream port.

## Evidence / References

- Verified: `AddEntryScreen.kt` FAB structure (~lines 206-269), `AddEntryViewModel.addEntry` flow, `CreateFoodDiaryEntryUseCase`, `FoodDiaryEntryCreatedEvent`/`EventBus` seam, `DiaryFood` self-contained snapshot, alternate entry surfaces under `app/ui/food/diary/`.
- foodus-relay repo (read-only): wire contract v1 Not Started — confirms the no-invented-wire-details constraint.

## Complaints / Friction

### Story UI text assumes a layout the screen does not have

**What happened:** The dictated layout (left/right buttons + card) presumes a classic two-button form; `AddEntryScreen` uses a Material 3 FAB column.
**Why this made the task harder:** The plan cannot specify final layout geometry without an owner call; recorded as an OPEN question with a merge-safe assumption.
**What was tried:** Read the screen and scaffold structure; proposed a fork-owned bottom action area shown only when groups exist.
**What would improve this:** A quick owner sketch or one-line ruling on the resolved question.
