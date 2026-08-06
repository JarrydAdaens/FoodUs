# Plan: Receive into diary

## Metadata

- Task Type: `STORY`
- Status: `Draft`
- Owner: Jarryd Adaens
- Last Updated: 28 July 2026

## Linked Context

- Milestone: [context/milestones/milestone-3.md](../../../milestones/milestone-3.md)
- Story: [Story 11: Receive into diary](../../../milestones/milestone-3.md#story-11) `[STORY 3.11]`
- Dictation source: [2026-07-27 Milestone 3 multiplayer addendum](../../../dictations-tier-0/2026-07-27_milestone-3_multiplayer-addendum.md)
- Design authority: `context/design.md` — "The Multiplayer Exception" (relay model, E2E, poll-on-wake)
- Related Plans:
  - Story 3.8 (envelope & E2E pipeline) — **upstream dependency, plan currently BLOCKED** on the foodus-relay wire contract v1 (Not Started in that repo). This story consumes decrypted, routed messages from Story 3.8's message router; it never touches the wire itself.
  - Story 3.12 (suggestion queue) — consumes this story's meal-matching resolver at accept time (shared open decision below).
  - Story 3.13 (Notification Center) — receives this story's "entry added" / "entry failed" events.
- External Tooling: none required.

## CER

- Complexity: 5
- Effort: 4
- Risk: 5
- Notes: Inline estimate. Complexity sits in the three-tier matching resolver's edge cases (duplicate meal names, overlapping/overnight/all-day windows, deleted meals mid-insert) under a hard never-drop invariant. Effort is moderate: one resolver, one insertion use case, event emission, and tests — no UI of its own. Risk is elevated because this story writes into another person's diary automatically (data-integrity sensitive), its input schema is owned by the blocked Story 3.8 envelope work, and a silent drop or mis-slot directly betrays the trust model the milestone exists to prove.

## Objective

When a Full-trust group message carrying a food entry is routed to this device, insert that entry into the local diary — correctly slotted by three-tier meal matching (sender's meal name → recipient's meal time windows → first meal fallback), never dropped under any input — and emit an "entry added" event for the Notification Center.

## Scope

### In Scope

- A `ReceiveDiaryEntry` use case (fooddiary or new multiplayer slice — see Questions) that accepts a routed, already-decrypted incoming entry (self-contained food payload + entry timestamp + sender meal name + originating group/sender identity) and always produces a diary insertion.
- A meal-matching resolver implementing the three tiers against `MealRepository.observeMeals()`:
  1. **Name match** — recipient meal whose name matches the sender's meal name.
  2. **Time match** — entry timestamp's time-of-day against recipient `Meal.from`/`Meal.to` windows (reusing the coverage semantics established by `shouldShowMeal` in `ObserveDiaryMealsUseCase`, including overnight `to < from` wrap and `from == to` all-day meals).
  3. **First-meal fallback** — the meal with the lowest `rank`.
- Mapping the self-contained payload to the existing diary snapshot model: construct a `DiaryFood` (`DiaryFoodProduct`, and `DiaryFoodRecipe` if the envelope preserves recipe structure) and insert via the existing snapshot persistence path (`FoodDiaryEntryRepository.insert` → `MeasurementEntity` + `DiaryProductEntity`/`DiaryRecipeEntity`). No food-catalog record is created — diary entries are already catalog-independent snapshots.
- Never-drop hardening: the insertion path must not be able to return a business failure that discards the entry (see Risk Mitigation for the `MealNotFound`/`InvalidMeasurement` handling this forces).
- Event emission via the existing `EventBus` (`common/domain/event/EventBus.kt`): an "entry added (by <sender> into <meal>)" integration event consumed by Story 3.13's persistent event store; a distinct "entry landed by fallback" flavor is optional (see Questions).
- Unit tests for the resolver tiers and the never-drop invariant (deterministic domain logic — squarely inside the unit-testing rule's "stable business logic" mandate).

### Out Of Scope

- Polling, decryption, envelope parsing, version handling, and message routing — Story 3.8 (blocked; interface dependency only).
- The suggestion-queue UI and storage — Story 3.12 (it calls this story's resolver at accept time).
- The Notification Center store/UI — Story 3.13 (this story only publishes events).
- Sending entries (Story 3.10), groups/trust storage (Story 3.9).
- Any relay/server capability — this story consumes the relay only through Story 3.8's pipeline (contract v1); no new server capability of its own.

## Non-Goals

- No conflict resolution or dedup against entries the recipient logged manually — a shared meal logged by both people appears twice by design (out of dictated scope).
- No editing/deleting of received entries beyond what any diary entry already supports.
- No meal-plan editing capability — flagged in Story 3.9's open decisions; not planned here.

## Current Understanding

All paths verified in the working tree on 28 July 2026.

- **Meal model:** `app/src/commonMain/kotlin/com/maksimowiczm/foodyou/fooddiary/domain/entity/Meal.kt` — `Meal(id, name, from: LocalTime, to: LocalTime, rank)`. Storage: `fooddiary/infrastructure/room/MealEntity.kt` (`Meal` table, hour/minute columns, `rank`). Default meals seeded per-locale by `InitializeMealsCallback` from `LocalizedMealsProvider` (`ComposeLocalizedMealsProvider`), whose source files `app/src/commonMain/composeResources/files/meals/meals*.json` are exactly the slots validated by `.github/workflows/validate-meals.yml` → `dev/test-meals-localization.bash`.
- **Window semantics precedent:** `fooddiary/domain/usecase/ObserveDiaryMealsUseCase.kt` `shouldShowMeal()` — `from == to` means all-day; `to < from` wraps midnight; otherwise inclusive `from <= t <= to`. The time-match tier reuses these semantics rather than inventing new ones.
- **Insertion path:** `fooddiary/domain/usecase/CreateFoodDiaryEntryUseCase.kt` validates measurement-vs-food and meal existence, then `FoodDiaryEntryRepository.insert(measurement, mealId, date, food, createdAt)` inside a `TransactionProvider.withTransaction`. Room implementation `RoomFoodDiaryEntryRepository` persists a **snapshot**: `MeasurementEntity` (mealId FK, epochDay, measurement type + quantity) plus `DiaryProductEntity`/`DiaryRecipeEntity`. `FoodDiaryEntry.date` is a `LocalDate` epoch-day; `createdAt`/`updatedAt` are epoch seconds.
- **Self-contained payload fit:** `DiaryFood` (`fooddiary/domain/entity/DiaryFood.kt`) needs only name, `NutritionFacts` (per 100 g/ml), serving/total weight, `isLiquid`, note — all of which the milestone says the packet carries. `DiaryFoodProduct` additionally carries a `FoodSource`; what source a received entry declares is an open question below.
- **Events:** `common/domain/event/EventBus.kt` (`publish(IntegrationEvent)`, `subscribe` ext). Precedent event: `fooddiary/domain/event/FoodDiaryEntryCreatedEvent.kt`. Story 3.13 will subscribe/persist; this story defines the received-entry event type(s).
- **Koin wiring precedent:** `fooddiary/domain/FoodDiaryDomainModule.kt` registers use cases; new resolver/use case registers the same way.
- **Interfaces this story must expose, not consume:** Story 3.8's router will call something like `suspend fun receive(incoming: IncomingDiaryEntry)`. The `IncomingDiaryEntry` domain type is owned here (payload fields per the milestone: complete nutrition, name, entry timestamp, meal name, group/sender identity) and is deliberately **not** the wire envelope — Story 3.8 maps envelope → domain type when it is planned. No wire-contract details (field names, versioning, endpoints) are assumed in this plan.
- **Assumptions and constraints:** fork philosophy (new files additive; no upstream file edits expected beyond Koin module registration); laws.md §2 input validation — the routed payload is external input and must be validated (finite/non-negative nutrition values, non-empty name) before insertion, with validation failure feeding the "sanitize then land" path, never a drop.

## Questions / Unknowns

- Q: `[STORY 3.11]` Date/meal carry-over semantics: does the received entry land on the **sender's entry date** in the recipient's diary?
  Impact: Decides the `date` passed to insertion and how the resolver interprets the entry timestamp (calendar date vs time-of-day split).
  Assumption: Yes — the entry lands on the sender's entry date; the timestamp's time-of-day drives the tier-2 time match. `createdAt`/`updatedAt` use local receive time. (Flagged during dictation; staged in the milestone for owner confirmation.)
  Status: OPEN
  Answer: —

- Q: `[STORY 3.11]` Does Suggest-queue accept (Story 3.12) apply the same three-tier matching **at accept time** using the original entry timestamp?
  Impact: Determines whether the resolver is exposed as a shared service to Story 3.12 or stays private to the Full-trust path; affects where meal resolution happens relative to queue storage.
  Assumption: Yes — shared resolver, invoked at accept time with the original timestamp (shared decision with Story 3.12; staged in the milestone).
  Status: OPEN
  Answer: —

- Q: `[STORY 3.11]` Name-match strictness: exact match, or case-insensitive/trimmed? And if multiple recipient meals share the matching name, which wins?
  Impact: Tier-1 correctness for the household's real meal lists (e.g. "breakfast" vs "Breakfast").
  Assumption: Case-insensitive comparison on trimmed names; ties broken by lowest `rank`.
  Status: OPEN
  Answer: —

- Q: `[STORY 3.11]` Time-match edge semantics: do all-day meals (`from == to`) count as covering every timestamp in tier 2, and how are overlapping windows broken?
  Impact: An all-day meal would otherwise swallow every time-matched entry; overlaps are common (snack windows inside day-long windows).
  Assumption: All-day meals are skipped in tier 2 (they express "no window", mirroring `ignoreAllDayMeals` intent) but remain eligible for tiers 1 and 3; overlapping covering windows are broken by lowest `rank`.
  Status: OPEN
  Answer: —

- Q: `[STORY 3.11]` Does the envelope preserve recipe structure (ingredients → `DiaryFoodRecipe`) or flatten everything to a single product-like payload?
  Impact: Decides whether the payload mapper targets `DiaryFoodProduct` only or both snapshot shapes; affects Story 3.10's serialization too. Contract-dependent — owned by the foodus-relay wire contract via Story 3.8.
  Assumption: Plan the mapper against the `DiaryFood` interface with `DiaryFoodProduct` as the guaranteed baseline; recipe fidelity is decided when Story 3.8 is planned against contract v1.
  Status: OPEN
  Answer: —

- Q: `[STORY 3.11]` What `FoodSource` does a received entry's `DiaryFoodProduct` declare?
  Impact: `DiaryFoodProduct.source` is non-null; the value shows up anywhere source is rendered.
  Assumption: Whatever generic/user value the enum offers today (verify `common/domain/food/FoodSource` members during implementation); introducing a new "shared" source is a nice-to-have deferred unless trivial and additive.
  Status: OPEN
  Answer: —

## Execution Steps

1. Define the incoming domain type and receiving seam.
   - Why: Story 3.8 (blocked) needs a stable, wire-agnostic target to route into; this story owns the domain side of that seam.
   - Edits: new `IncomingDiaryEntry` domain entity (self-contained food payload, entry timestamp, sender meal name, group id, sender GUID) and `ReceiveDiaryEntryUseCase` skeleton in the fooddiary slice (or the new multiplayer slice if Stories 3.9/3.10 establish one — align at implementation time with whichever exists first).
   - Dependencies: none locally; consumed later by Story 3.8's router and Story 3.12's accept path.

2. Implement the meal-matching resolver as a pure domain service.
   - Why: The three-tier rule is deterministic logic that must be unit-testable in isolation.
   - Edits: new `MealMatchingResolver` (input: sender meal name, entry timestamp, recipient `List<Meal>`; output: chosen `mealId` — total function, never empty given ≥1 meal) reusing `shouldShowMeal`-style window semantics; register in `FoodDiaryDomainModule`.
   - Dependencies: Step 1's types; Questions 3–4 assumptions encoded as named constants/predicates so a changed owner decision is a one-line fix.

3. Implement payload → `DiaryFood` mapping with validation.
   - Why: laws.md §2 — routed payloads are external input; and the snapshot model needs a concrete `DiaryFoodProduct`.
   - Edits: mapper producing `DiaryFoodProduct` (name, `NutritionFacts`, weights, `isLiquid`, note, source per Question 6); validation that sanitizes rather than rejects wherever the invariant allows (e.g. clamp negative nutrients to zero with an event flag) — hard-invalid payloads (no name and no nutrition at all) are the one exception, surfaced as a "failed to add" event, never a silent drop.
   - Dependencies: Step 1.

4. Implement `ReceiveDiaryEntryUseCase` end-to-end with the never-drop invariant.
   - Why: The story's core promise.
   - Edits: resolve meal (Step 2) → insert via `FoodDiaryEntryRepository.insert` in a transaction. Do **not** route through `CreateFoodDiaryEntryUseCase`: its `MealNotFound`/`InvalidMeasurement` failure modes are exactly what this path must not have. Measurement for received entries is normalized to grams/milliliters of the delivered weight (sidestepping serving/package validation); a meal deleted between resolve and insert retries the resolver inside the transaction.
   - Dependencies: Steps 1–3.

5. Emit Notification Center events.
   - Why: Cross-device actions must be visible (milestone: "entry added" notification; Story 3.13 is the sink).
   - Edits: new `IntegrationEvent` type(s) — received-entry-added (sender, group, meal, food name, fallback-used flag), received-entry-failed (hard-invalid payload case); `EventBus.publish` calls from Step 4.
   - Dependencies: Step 4; Story 3.13 consumes later (events are inert until then — acceptable).

6. Unit tests for resolver and use case.
   - Why: Stable business logic + a bug here corrupts another person's diary (unit-testing rules: required category).
   - Edits: `commonTest` cases — tier-1 name match (case/trim/tie), tier-2 windows (inside, overnight wrap, all-day skip, overlap tie), tier-3 fallback, never-drop under deleted-meal race and sanitized payloads.
   - Dependencies: Steps 2–4.

## Validation

### Automated Checks

- `commonTest` unit tests for `MealMatchingResolver` and `ReceiveDiaryEntryUseCase` (new).
- Full app assemble (`gradlew :app:assembleDebug` or the established build task) to confirm KMP compilation.

### Manual Checks

1. With Story 3.8 unavailable, drive `ReceiveDiaryEntryUseCase` from a debug hook or test double: verify an entry with a matching meal name lands in that meal; one with only a time match lands by window; one matching nothing lands in the first-ranked meal.
2. Verify the inserted entry renders normally in the diary UI (nutrition, weight, name) and survives app restart.
3. Verify an event is published for each insertion (log/subscriber probe until Story 3.13 exists).

### Acceptance Criteria

- Every routed entry produces exactly one diary insertion (or, for hard-invalid payloads only, one "failed to add" event) — zero silent drops across all tested inputs.
- Meal slotting follows the three tiers in order, with documented tie-breaking.
- The received entry is fully local and self-contained: visible with correct nutrition even though the recipient lacks the sender's data sources.
- No wire-contract types appear in this story's code — the seam consumes the domain `IncomingDiaryEntry` only.

## Risk Mitigation

- Risk: Story 3.8's envelope (contract v1) ends up carrying less/different data than `IncomingDiaryEntry` assumes.
  Mitigation: The domain type is minimal (only fields the milestone text guarantees); the mapper concentrates all payload interpretation in one file; contract-dependent fields are flagged in Questions. Revisit this plan when Story 3.8 unblocks — noted as a standing dependency.
- Risk: Never-drop invariant violated by reusing existing failure-returning insertion paths.
  Mitigation: Dedicated use case bypassing `CreateFoodDiaryEntryUseCase`'s rejection modes; gram/milliliter measurement normalization; in-transaction resolver retry; unit tests that assert insertion under each failure-shaped input.
- Risk: Mis-slotting annoys the household and erodes trust in Full-trust groups.
  Mitigation: Tie-break rules are explicit and tested; fallback insertions are flagged in the emitted event so the Notification Center can say "added to Breakfast (fallback)"; owner confirms Questions 1/3/4 before execution (plan stays Draft).
- Risk: Malformed/hostile payload (external input) reaches Room.
  Mitigation: Step 3 validation-and-sanitize layer; hard-invalid payloads surface as failed events (accepted residual: the sender is a trusted friend by construction — the trust model bounds exposure).

## Phase Split

Not needed — CER within single-pass thresholds.

## Evidence / References

- Repository grounding verified 28 July 2026: `Meal.kt`, `MealEntity.kt`, `InitializeMealsCallback.kt`, `ObserveDiaryMealsUseCase.kt` (`shouldShowMeal`), `CreateFoodDiaryEntryUseCase.kt`, `FoodDiaryEntryRepository.kt`, `RoomFoodDiaryEntryRepository.kt`, `MeasurementEntity.kt`, `DiaryFood.kt`, `DiaryFoodProduct.kt`, `FoodDiaryEntryCreatedEvent.kt`, `EventBus.kt`, `.github/workflows/validate-meals.yml`.
- foodus-relay repo checked read-only 28 July 2026: wire contract v1 = server Story 1, Not Started — hence the seam-only treatment of everything wire-shaped.

## Complaints / Friction

### Upstream plan blocked, downstream planned

**What happened:** This story's only input arrives via Story 3.8's router, whose plan is blocked on the unsettled wire contract.
**Why this made the task harder:** The receiving seam had to be designed against milestone prose rather than a routed message type.
**What was tried:** Owning the domain-side type (`IncomingDiaryEntry`) here and pushing all envelope mapping into Story 3.8's future plan.
**What would improve this:** Carrying contract v1 (or just its envelope payload section) across as soon as foodus-relay publishes it, then revisiting the mapper questions.
**What I think:** The seam-first split is safe — this story's logic is stable regardless of wire shape — but do not start implementation of Step 3's mapper details until Story 3.8 is at least planned.
