# Plan: Friends List

## Metadata

- Task Type: `STORY`
- Status: `Draft`
- Owner: Jarryd Adaens
- Last Updated: 28 July 2026

## Linked Context

- Milestone: [context/milestones/milestone-3.md](../../../milestones/milestone-3.md)
- Story: [Story 7: Friends list](../../../milestones/milestone-3.md#story-7) `[STORY 3.7]`
- Design authority: `context/design.md` — "The Multiplayer Exception" (E2E model: becoming friends *is* the key exchange; accepted server-MITM weakness; poll-on-wake, no push)
- Relay Contract Conformance: `context/milestones/milestone-3.md` — binds every relay call in this plan (version stamping, two-way tolerance, capability-aware UI, HTTPS via Ktor, app→server dependency notes)
- Related Plans:
  - Story 3.1 (tabbed UI shell) — the Groups tab this story's Friends card lives on. `../story-1-tabbed-ui-shell/plan.md`
  - Story 3.2 (profile) — establishes the social storage slice and the local GUID identity. `../story-2-profile/plan.md`
  - Story 3.6 (friend codes) — the code being entered in add-by-code; display/regenerate is that story's scope. `../story-6-friend-codes/plan.md`
  - Story 3.15 (relay URL setting) — supplies the endpoint and the capability gating this story's relay calls sit behind. `../story-15-relay-url-setting/plan.md`
  - Story 3.8 (envelope & E2E pipeline) — **BLOCKED, unplanned this run** (wire contract v1 not started). Referenced only as the future transport for any cross-device friend-removal signalling.
- External Tooling: none required.

**Dependency note (app → server, per Relay Contract Conformance):** blocked by foodus-relay:
friend-code resolve + block endpoints (shared slug `...-friends`), contract v1, deployed. Owner
releases via the Story 5 gate. This plan stays `Draft` until that release; the relay-facing
seams below are contract-dependent and deliberately unspecified.

## CER

- Complexity: 6
- Effort: 6
- Risk: 5
- Notes: Inline estimate. Complexity comes from a new social-graph storage area, two contract-dependent network seams (resolve, block), and UX flows with destructive branches (delete vs delete-and-block). Effort spans Room entities + migration, domain layer, two network seams, and three UI surfaces (Friends card, list screen with expandable rows, add-by-code flow). Risk is dominated by the absent wire contract (any concrete request/response shape written now would be invented — mitigated by seam interfaces + Draft status) and by the unresolved both-ways delete semantics, which may pull in Story 3.8's pipeline.

## Objective

Ship the client-side friends system: durable local storage of friends (GUID, username, public
key) and a permanent blocked list; a Friends card on the Groups tab opening a friends list; an
add-by-code flow that resolves a friend code via the relay and stores the returned identity
(the key exchange); and expandable friend rows offering **Delete** and **Delete and block** —
all with the relay calls isolated behind contract-dependent interfaces so the plan can execute
fully once foodus-relay contract v1 is released.

## Scope

### In Scope

- Room storage for friends and the blocked list, registered in the aggregate `FoodYouDatabase`
  (schema version bump + AutoMigration), so both ride existing backup/restore.
- Domain layer: `Friend` entity (GUID, username snapshot, public key blob, added date),
  repository interface(s), and use cases: observe friends, add-by-code, delete, delete-and-block.
- Contract-dependent network seams (domain interfaces only, no invented wire detail):
  - *resolve friend code* → `{ GUID, current username, public key }`, with the server's block
    enforcement surfacing as "user not found".
  - *record block* → registers the block server-side so future attempts by the blocked person
    return "user not found" even with a fresh code.
- Friends card on the Groups tab (Story 3.1's shell); friends-list screen + ViewModel;
  add-by-code entry UI; expandable rows with Delete / Delete-and-block, each behind an
  are-you-sure confirmation.
- Capability-aware gating: add-by-code (and any relay call) hidden or greyed when the relay is
  unset/unreachable or the connected relay does not report the friends capability (consumes
  Story 3.15's check).
- New English base strings in `shared/resources` (fork-owned keys, additive).
- Koin module wiring following the established slice pattern.

### Out Of Scope

- Friend-code display and regeneration (Story 3.6).
- Profile creation/identity (Story 3.2) and key-pair generation (Story 3.3).
- The message pipeline and any envelope handling (Story 3.8, blocked).
- Group membership effects of deleting a friend (Story 3.9 owns group lifecycle; see Questions).
- Server-side block enforcement logic (foodus-relay scope).

## Non-Goals

- No passing on another friend's code from the expanded row (explicit story non-goal).
- No friend bios (backlog-1 Story 12).
- No safety-number key verification (backlog-1 Story 11); the server-MITM hole is accepted.
- No push transport; everything is poll-on-wake.

## Current Understanding

Verified in the working tree on 28 July 2026.

- **Feature-slice pattern:** slices live under `app/src/commonMain/kotlin/com/maksimowiczm/foodyou/<slice>/`
  with `domain/` + `infrastructure/` and a root Koin module (e.g. `sponsorship/SponsorshipModule.kt`
  composing `sponsorshipDomainModule()` + `sponsorshipInfrastructureModule()`), registered in
  `app/di/InitKoin.kt:28-43`. The friends work should join the social slice established by
  Story 3.2 rather than invent a second pattern (see Questions).
- **Room aggregation:** `app/infrastructure/room/FoodYouDatabase.kt` — single `@Database`
  aggregating per-slice database interfaces (e.g. `SponsorshipDatabase` exposing
  `sponsorshipDao`); entities registered centrally, `VERSION = 36` today, additive changes via
  `AutoMigration` entries. New `FriendEntity` + `BlockedUserEntity` (working names) follow the
  `SponsorshipEntity` shape (`infrastructure/room/` inside the owning slice, plain data class,
  epoch-seconds timestamps).
- **Network client pattern:** Ktor `HttpClient` injected via Koin with `NetworkConfig`
  (e.g. `sponsorship/infrastructure/github/GithubSponsorsApiClient.kt`); the `ai` slice uses a
  named-qualifier `HttpClient`. The relay client (owned by Story 3.15/3.8 territory) will follow
  this stack — this story only defines the two seam interfaces and consumes whatever relay
  client base Story 3.15 establishes.
- **Navigation:** `app/navigation/FoodYouAppNavHost.kt` uses `@Serializable` route objects +
  `forwardBackwardComposable<T>` (verified lines 66-520). Whether the friends list is a NavHost
  route or a surface inside the Groups tab's own navigation depends on Story 3.1's shell
  decision — discovery step: read Story 3.1's plan before implementation.
- **Existing behaviors to preserve:** the entire Log-tab app is untouched; upstream files gain
  only additive edits (nav routes, DI registration list, base `strings.xml` keys) per fork
  philosophy.
- **Constraints:** laws.md §2 — the friend's public key is not a secret but is
  integrity-critical: store it exactly as delivered (opaque encoded blob), never log resolved
  identities beyond debug necessity; all relay traffic HTTPS-only via Ktor.

## Questions / Unknowns

- Q: `[STORY 3.7]` What mechanism makes Delete "break the connection both ways"? With a dumb
  relay, no push, and Story 3.8's pipeline blocked/unplanned, remote-side removal needs either a
  contract-level construct or a friend-removed message over the pipeline — neither exists yet.
  Impact: Determines whether Delete is purely local (the ex-friend's sends simply stop being
  accepted/routable on our side) or requires a send path, which would add a Story 3.8 dependency
  to this story's Delete flow.
  Assumption: Delete removes the friend locally and drops their key; no outbound signal is sent
  in this story. The both-ways contract semantics are deferred to contract v1 / Story 3.8.
  Status: OPEN
  Answer: —

- Q: `[STORY 3.7]` Where does the social storage slice live and what is it named? Stories 3.2,
  3.6, 3.7, 3.9 all add social data; parallel planning means the slice name/layout is settled by
  Story 3.2's plan.
  Impact: File paths, module names, and DI wiring in this plan's execution steps.
  Assumption: A single fork-owned social slice (working name `social/`) established by
  Story 3.2, with this story adding `social/friends/` domain + infrastructure inside it.
  Status: OPEN
  Answer: —

- Q: `[STORY 3.7]` What happens when the *record block* relay call fails (offline, relay
  unreachable) after the user chooses Delete-and-block?
  Impact: Blocking is a safety feature; a silently un-registered server-side block would let the
  blocked person resolve a fresh code later.
  Assumption: The local block applies immediately; the server call is retried on next app wake
  until acknowledged, with a Notification Center event if it remains unregistered. Retry
  machinery may belong to Story 3.8's send queue — flagged as a seam.
  Status: OPEN
  Answer: —

- Q: `[STORY 3.7]` Does deleting (or blocking) a friend do anything to an existing two-person
  group containing them, beyond Story 3.9's own leave/dead-group rules?
  Impact: Ordering between this story's delete flow and Story 3.9's group lifecycle; potential
  confirm-dialog wording ("this will also...").
  Assumption: Group effects are wholly owned by Story 3.9; this story's confirm dialog warns
  generically and performs no group mutation.
  Status: OPEN
  Answer: —

- Q: `[STORY 3.7]` Is the resolved public key an opaque contract-defined encoding, and must the
  app validate it at add time (e.g. parseable key check) or store it blind?
  Impact: Add-by-code validation surface and error UX.
  Assumption: Stored as the opaque encoded value the contract delivers; a parse check happens
  only when Story 3.8 first encrypts to it. No format is invented now.
  Status: OPEN
  Answer: —

## Execution Steps

> Steps 4-6 cannot start until the owner releases the Story 5 gate (contract v1 deployed) and
> the seam interfaces in step 3 are filled against the real spec. Steps 1-3 and 7-8 are
> buildable immediately against fakes.

1. Confirm slice placement and shell integration.
   - Why: Two parallel plans (3.1 shell, 3.2 profile slice) settle this plan's file roots.
   - Edits: none — read `../story-1-tabbed-ui-shell/plan.md` and `../story-2-profile/plan.md`,
     then pin the concrete paths in this plan (update Current Understanding).
   - Dependencies: Stories 3.1 and 3.2 plans accepted.

2. Room storage for friends and blocked users.
   - Why: Durable social graph that rides backup/restore.
   - Edits: `FriendEntity` (GUID string PK, username, publicKey blob/string, addedEpochSeconds),
     `BlockedUserEntity` (GUID string PK, blockedEpochSeconds) + DAOs in the social slice's
     `infrastructure/room/`; a `FriendsDatabase` interface; register entities + interface in
     `FoodYouDatabase` with `VERSION = 37` and `AutoMigration(36, 37)`.
   - Dependencies: step 1 (paths); coordinate version bump if Story 3.2 also bumps schema —
     whichever story lands first takes 37, the next takes 38.

3. Domain layer + contract-dependent seams.
   - Why: Isolate everything the missing wire contract owns behind interfaces so local work
     proceeds.
   - Edits: `Friend` domain entity; `FriendRepository` (observe/add/delete) and blocked-list
     repository; use cases `AddFriendByCodeUseCase`, `DeleteFriendUseCase`,
     `DeleteAndBlockFriendUseCase`; seam interfaces `FriendCodeResolver`
     (`resolve(code) → { guid, username, publicKey } | UserNotFound | RelayUnavailable`) and
     `BlockRegistrar` (`recordBlock(guid)`), placed in domain with **no** URL/JSON specifics.
     Fake implementations for tests/dev.
   - Dependencies: step 2.

4. Relay-backed seam implementations (contract-gated).
   - Why: The real resolve/block calls per contract v1.
   - Edits: infrastructure implementations of `FriendCodeResolver`/`BlockRegistrar` hand-written
     to the published spec, over the Ktor relay client base from Story 3.15, HTTPS-only;
     envelope-version and tolerance rules per Relay Contract Conformance.
   - Dependencies: Story 5 gate released; Story 3.15 landed.

5. Add-by-code flow UI.
   - Why: The user-facing key exchange.
   - Edits: plus-button entry on the friends list → code entry (reuse the 4-4-4 formatting
     conventions from Story 3.6's display), calls `AddFriendByCodeUseCase`; distinct outcomes
     for success, "user not found" (covers blocked-or-nonexistent indistinguishably — copy must
     not hint which), and relay-unreachable.
   - Dependencies: steps 3-4; capability gating from Story 3.15.

6. Friends card + list screen with expandable rows.
   - Why: The browse/manage surface.
   - Edits: Friends card on the Groups tab; `FriendsListScreen` + ViewModel (Koin `viewModel`
     pattern); tappable rows expanding to Delete / Delete-and-block with confirmation dialogs;
     row shows username + added date, never the raw GUID by default.
   - Dependencies: steps 2-3 (list works against local data + fakes before relay lands).

7. Wiring, strings, gating polish.
   - Why: Ship-shape integration.
   - Edits: Koin module registration in `InitKoin.kt` (or inside the social slice's root
     module), new base `strings.xml` keys, hide/grey states when relay unset or capability
     missing.
   - Dependencies: steps 5-6.

8. Tests.
   - Why: The delete/block state transitions and add-by-code outcomes are stable business logic.
   - Edits: `commonTest` unit tests — add-by-code success/user-not-found/unreachable against the
     fake resolver; delete removes friend + key; delete-and-block writes the blocked list and
     invokes `BlockRegistrar`; blocked GUID cannot be re-added locally.
   - Dependencies: step 3.

## Validation

### Automated Checks

- `commonTest` unit tests from step 8 (targeted; run the social slice test package).
- Full assemble of a debug variant to verify Room schema migration compiles and exports.

### Manual Checks

1. Fresh install → Groups tab shows Friends card; list empty-state renders.
2. With relay unset: add-by-code hidden/greyed (capability rule).
3. (Post-gate) Enter a real friend's code on the live relay → friend appears with username;
   re-open app → friend persisted; backup/restore → friend survives.
4. (Post-gate) Delete-and-block → friend gone, blocked list updated, and the blocked person's
   fresh-code attempt returns "user not found" on their device.

### Acceptance Criteria

- A resolved friend's GUID, current username, and public key are stored locally and survive
  backup/restore.
- Delete removes the friend and their key locally; Delete-and-block additionally records a
  permanent local block and registers it with the relay.
- "User not found" is presented identically for nonexistent and blocked/blocking cases.
- No relay request is ever attempted over plain HTTP or when the relay is unset.
- No wire-contract detail (paths, field names, auth) exists in code before contract v1 is
  published.

## Risk Mitigation

- Risk: Wire contract v1 does not exist; any concrete network code written now is invented.
  Mitigation: Steps 3/4 split — domain seams + fakes now, contract-conformant implementations
  only after the Story 5 gate; plan held at `Draft`.
- Risk: Both-ways delete semantics undefined (first open question).
  Mitigation: Delete is local-only in this story; the question is escalated to the owner and the
  contract; no protocol invented.
- Risk: Parallel schema bumps (Stories 3.2/3.7/3.9 all touch `FoodYouDatabase`).
  Mitigation: Version-number coordination rule in step 2; migrations stay additive
  AutoMigrations.
- Risk: Upstream merge surface.
  Mitigation: All new files are fork-owned; upstream files (`InitKoin.kt`, nav host,
  `strings.xml`) receive additive-only edits, matching every prior fork story.
- Risk: Block registration failure window (third open question).
  Mitigation: Local block is authoritative immediately; server registration retried on wake;
  surfaced via Notification Center if persistent — pending owner confirmation.

## Phase Split

Not required at this CER. If the both-ways delete answer pulls Story 3.8 machinery into scope,
re-grade and consider splitting local-graph (steps 1-3, 6-8) from relay-backed (steps 4-5)
phases.

## Evidence / References

- Pattern sources verified 28 July 2026: `sponsorship/` slice (module composition, Room + Ktor
  patterns), `app/infrastructure/room/FoodYouDatabase.kt` (VERSION 36, AutoMigration list),
  `app/di/InitKoin.kt`, `app/navigation/FoodYouAppNavHost.kt` (route pattern),
  `milestone-2/story-22-ai-settings-screen/plan.md` (settings/DataStore/named-client precedent).
- foodus-relay repo read (read-only): wire contract v1 = its Milestone 3 Story 1, Not Started —
  confirms the contract gate.

## Complaints / Friction

### Planning gated on an unwritten contract

**What happened:** The story's two network calls are fully specified behaviorally in the
milestone, but the owning wire contract (foodus-relay) has not been written.
**Why this made the task harder:** Every request/response detail had to be pushed behind seam
interfaces and OPEN questions instead of concrete steps.
**What was tried:** Read the server repo's milestone/design docs read-only to confirm contract
status rather than guessing.
**What would improve this:** Owner carries contract v1 (or even just the friends-endpoint
section) across as soon as foodus-relay publishes it; steps 4-5 then become concrete in one
sitting.
**What I think:** The seam split keeps ~70% of this story buildable now with low rework risk.
