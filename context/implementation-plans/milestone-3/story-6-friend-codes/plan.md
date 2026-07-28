# Plan: Friend Codes

## Metadata

- Task Type: `STORY`
- Status: `Draft`
- Owner: Jarryd Adaens
- Last Updated: 28 July 2026

## Linked Context

- Milestone: [context/milestones/milestone-3.md](../../../milestones/milestone-3.md)
- Story: [Story 6: Friend codes](../../../milestones/milestone-3.md#story-6) `[STORY 3.6]`
- Design authority: `context/design.md` — "The Multiplayer Exception" (relay model, contract ownership, versioned tolerant protocol, user-entered relay URL)
- Milestone authority: `context/milestones/milestone-3.md` — "Relay Contract Conformance" section binds every relay call planned here
- Related Plans:
  - Story 3.2 (Profile) — `../story-2-profile/plan.md` (planned in the same run): owns the profile record this story's friend code lives on and the My Profile card that displays it.
  - Story 3.15 (Relay URL setting) — `../story-15-relay-url-setting/plan.md` (planned in the same run): owns the user-entered relay endpoint and the capability check that gates this story's server calls.
  - Story 3.7 (Friends list) — consumes friend codes for add-by-code resolve; not planned here.
- External Tooling: none required.

**Dependency note (milestone-mandated, app → server):** blocked by foodus-relay: friend-code endpoints (shared slug `...-friend-codes`), contract v1, deployed. The owner releases this block via the Story 5 gate. Also requires Story 3.15's relay URL configured on-device. Contract v1 is **Not Started** in the foodus-relay repo as of 28 July 2026 — every server-facing detail below is a contract-dependent seam, not a specification.

## CER

- Complexity: 4
- Effort: 4
- Risk: 5
- Notes: Inline estimate. Local scope (display + regenerate UI, profile-record field, formatting) is small and follows established patterns. Complexity concentrates in the provisioning seam: friend-code minting authority (server-assigned vs client-generated+registered) is an open decision owned by foodus-relay, so the plan must isolate that decision behind one interface rather than committing either way. Risk is elevated because the wire contract does not exist — any concrete endpoint/DTO written today would invent a competing contract (forbidden by Relay Contract Conformance rule 1), and because regenerate semantics touch the social graph (old codes must die without breaking friendships — enforced server-side, but the app must not cache stale codes).

## Objective

Give every profile a friend code — 12 characters in three dash-separated blocks of four, product-key style (e.g. `A7F3-9KQ2-XM41`), a revocable disposable layer over the permanent GUID — displayed on the profile surface with a regenerate flow, where regeneration kills every old copy of the code without breaking existing friendships. Server interaction (code registration/regeneration per the foodus-relay wire contract) is planned as a contract-gated seam that activates once contract v1 is carried across and the Story 5 gate is released.

## Scope

### In Scope

- Friend-code value type in the social slice domain: canonical storage form plus the fixed display shape (three blocks of four, dash-separated). Shape is settled; alphabet/case rules are contract-owned (see Questions).
- Friend-code field on the profile record (schema seam owned by Story 3.2's profile entity; this story populates and maintains it).
- Profile UI: friend-code display on the My Profile surface (Story 3.2's card/screen) and a regenerate flow with an are-you-sure confirmation explaining the consequence (old copies of the code die; existing friends unaffected).
- A single domain-level relay seam, e.g. `FriendCodeRelayClient` (interface in the social slice domain; infrastructure implementation deferred): `register`/`regenerate` operations whose transport details are written only after contract v1 exists. Wire DTOs are hand-written to the spec at implementation time, per Relay Contract Conformance.
- Capability gating: friend-code display/regenerate degrade gracefully when the relay is unset or unreachable (Story 3.15's capability check) — regenerate hidden or greyed per the capability-aware UI rule.
- Ktor client wiring following the repository convention: a named-qualifier `HttpClient` single in the social slice's Koin module (pattern: `USDAModule.kt:14-19`, `AiInfrastructureModule.kt:24-40`), JSON with `ignoreUnknownKeys = true` (which also satisfies the two-way tolerance conformance rule).
- New English strings in `shared/resources/src/commonMain/composeResources/values/strings.xml` (additive, fork-owned keys).

### Out Of Scope

- Resolving a *friend's* code into GUID/username/public key — Story 3.7 (add-by-code).
- The profile record, My Profile card construction, and backup inclusion — Story 3.2.
- The relay URL setting, HTTPS validation, and the version/capability probe — Story 3.15.
- Envelope schema, version stamping, mailbox transport — Story 3.8 (blocked; unplanned this run).
- Any edit to the foodus-relay repository or its contract (read-only across the repo boundary).

## Non-Goals

- No QR codes, share sheets, or deep links for code exchange — codes travel out-of-band by the humans.
- No code expiry/TTL beyond regeneration — revocation is the regenerate flow.
- No offline queue of pending registrations beyond the minimal unset-relay behavior described below.

## Current Understanding

Verified in the working tree on 28 July 2026.

- **No social slice exists yet.** `app/src/commonMain/kotlin/com/maksimowiczm/foodyou/` has feature slices `food`, `fooddiary`, `goals`, `ai`, `settings`, etc.; Milestone 3 stories will create a new slice (naming owned by Story 3.2 as the first story to build it — this plan assumes `social` and follows whatever Story 3.2 establishes).
- **Ktor convention:** per-consumer `HttpClient` registered under a named qualifier in the slice's Koin module — `USDAModule.kt:14` and `AiInfrastructureModule.kt:24` both do `single(named(...)) { HttpClient { install(HttpTimeout); install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) } } }`. The relay client follows this pattern; HTTPS-only is enforced by Story 3.15's URL validation upstream of any call.
- **Settings/preferences convention:** `AbstractDataStoreUserPreferencesRepository` + `userPreferencesRepositoryOf(...)` (see `DataStoreAiSettingsRepository.kt`) — relevant only if any friend-code UI state needs persistence; the code itself lives on the profile Room record (rides backup/restore per Story 3.2).
- **Validator precedent:** `OpenRouterAiConnectionValidator` (Milestone 2 Story 22) shows the repo's pattern for a user-triggered probe returning success-or-error-string; the regenerate flow's failure surface can mirror it.
- **Existing behaviors to preserve:** none — this is greenfield fork-additive code; no upstream files need touching except shared `strings.xml` (additive keys only).
- **Constraints:** Relay Contract Conformance rules 1–7 (conformance not ownership; version stamping; two-way tolerance; capability-aware UI; HTTPS via Ktor; dependency note above; server leads). laws.md §2: friend codes are not secrets, but the profile GUID + code mapping is social-graph data — never logged.

## Questions / Unknowns

- Q: `[STORY 3.6]` Friend-code minting authority — server-assigned, or client-generated + registered? (Contract-owned: transferred to foodus-relay's first milestone; owner input wanted there.)
  Impact: Decides whether the app ever generates code values (needing the exact alphabet locally) or only stores what the server returns. Shapes the `FriendCodeRelayClient` seam's semantics and error handling (collision retry vs none).
  Assumption: Server-assigned (the simpler client; the app treats the code as an opaque formatted string). The seam is written so a client-generated resolution changes only the infrastructure implementation, not the domain interface.
  Status: OPEN
  Answer: —

- Q: `[STORY 3.6]` Friend-code alphabet and case rules (shape fixed at 4-4-4 with dashes; exact charset pending). (Contract-owned.)
  Impact: Input-normalization for display and, in Story 3.7, for code entry (e.g. is `a7f3` the same as `A7F3`? Are ambiguous glyphs like `0/O`, `1/I` excluded?). Blocks any local generation or strict validation logic.
  Assumption: Uppercase letters + digits, case-insensitive input normalized to uppercase; validation until contract v1 checks shape only (three dash-separated blocks of four alphanumerics).
  Status: OPEN
  Answer: —

- Q: `[STORY 3.6]` When is a code first provisioned — at profile creation (requires relay configured, contradicting Story 3.2's local-only creation) or lazily, the first time the relay is configured and reachable?
  Impact: Decides whether the profile can display an empty friend-code state and whether provisioning needs a retry path on next app wake.
  Assumption: Lazy provisioning — profile creation stays local-only (Story 3.2); the code slot shows a "connect a relay to get your friend code" placeholder until the Story 5 gate is released and the first registration succeeds.
  Status: OPEN
  Answer: —

- Q: `[STORY 3.6]` Should the display include a copy-to-clipboard affordance? The story mandates display + regenerate only, but a 12-character code is painful to transcribe by eye.
  Impact: One small UI addition; out-of-band sharing friction.
  Assumption: Yes, a plain copy affordance (no share sheet), as minimal ergonomics consistent with "respect the user". Flagged rather than silently added since it exceeds the literal story text.
  Status: OPEN
  Answer: —

## Execution Steps

> Steps 1–4 are buildable now (local + seam). Step 5 is contract-gated and must not start before contract v1 is carried across and the Story 5 gate is released ("server leads, app follows").

1. Domain model: friend-code value type + shape validation in the social slice domain.
   - Why: One canonical representation for storage, display, and (later) Story 3.7 input parsing.
   - Edits: New `FriendCode` value class with `parse`/`format` (shape-only validation: `XXXX-XXXX-XXXX` alphanumerics), unit-tested; placed in the slice domain package established by Story 3.2.
   - Dependencies: Story 3.2's slice layout decision.

2. Profile record integration: persist the current code (nullable until first provisioning).
   - Why: The code is profile data and must ride Room backup/restore.
   - Edits: Friend-code column on Story 3.2's profile entity/DAO (coordinate — Story 3.2 declares the field, this story owns its lifecycle); repository accessor to observe/update it.
   - Dependencies: Story 3.2 executed first, or its schema seam agreed.

3. Relay seam: `FriendCodeRelayClient` domain interface + unconfigured/no-op behavior.
   - Why: Isolates the contract-owned decisions (minting authority, endpoints, DTOs, auth) behind one interface so contract v1 changes only infrastructure.
   - Edits: Domain interface with `register` and `regenerate` operations returning a sealed result (success-with-code / relay-unset / unreachable / refused); a `NotConfigured` implementation bound in Koin until Step 5.
   - Dependencies: none locally; semantics finalized by contract v1.

4. UI: friend-code display + regenerate flow on the My Profile surface.
   - Why: The user-visible deliverable — display the code, regenerate with consequences explained.
   - Edits: Friend-code row (formatted display, copy affordance per open question, placeholder when unprovisioned) added to Story 3.2's profile screen; regenerate action behind an are-you-sure dialog ("old copies stop working; your friends are unaffected"); action hidden/greyed when the relay capability check fails (Story 3.15). New additive strings in `strings.xml`.
   - Dependencies: Steps 1–3; Story 3.2's UI in place.

5. **[CONTRACT-GATED]** Infrastructure implementation of `FriendCodeRelayClient` per contract v1.
   - Why: Real registration/regeneration against the deployed relay.
   - Edits: Hand-written DTOs matching the wire spec (per Relay Contract Conformance — no schema sharing), Ktor implementation on a named-qualifier `HttpClient` (JSON `ignoreUnknownKeys = true`), Koin rebind, error mapping into the Step 3 sealed result; regeneration success overwrites the stored code atomically.
   - Dependencies: foodus-relay contract v1 carried across by the owner; Story 5 gate released; Story 3.15's relay URL + capability check live.

## Validation

### Automated Checks

- Unit tests for `FriendCode` shape parsing/formatting (valid shape, wrong block sizes, missing dashes, case normalization per the standing assumption).
- Unit test: regenerate flow persists the new code and never mutates friend records (once Story 3.7's friend storage exists, guard the invariant "regeneration touches only the profile's own code").
- Build: `:app` compiles for androidTarget.

### Manual Checks

1. Profile shows the placeholder state with no relay configured; regenerate is hidden/greyed.
2. (Post-gate) With a live relay: code appears after provisioning; regenerate produces a new code; the old code no longer resolves (verified from the second phone via Story 3.7's add-by-code).
3. (Post-gate) Regenerating with an existing friendship intact: the friendship survives (Story 3.14 household proof covers this end-to-end).

### Acceptance Criteria

- Every profile with a released relay gate displays a 4-4-4 formatted friend code.
- Regenerate replaces the code after explicit confirmation; old codes are dead server-side; friendships persist.
- With the relay unset/unreachable, the profile remains fully usable and no relay call is attempted.
- No wire-contract details exist in code before contract v1 (Step 5 untouched until the gate).

## Risk Mitigation

- Risk: Implementing against an imagined contract (contract v1 not started).
  Mitigation: Hard step split — Steps 1–4 contain zero wire details; Step 5 is explicitly gated on the owner carrying contract v1 across. The domain seam is the only thing the local build knows.
- Risk: Minting-authority decision lands opposite the assumption (client-generated).
  Mitigation: Seam isolates it; only the Step 5 infrastructure and possibly a local generator (using the contract's alphabet) change. Recorded as OPEN with the server repo as owner.
- Risk: Stale code cached locally after a failed regenerate round-trip.
  Mitigation: Overwrite the stored code only from a successful relay response (server's answer is authoritative in both minting models); surface failures via the sealed result, never partially applied.
- Risk: Schema coupling with Story 3.2's entity.
  Mitigation: Coordinate the nullable friend-code column in Story 3.2's schema from day one (noted in both plans), avoiding an early Room migration.

## Phase Split

Not required. CER within thresholds; the contract gate is handled by the Step 4/5 boundary rather than phases.

## Evidence / References

- Ktor named-qualifier client convention: `app/src/commonMain/kotlin/com/maksimowiczm/foodyou/food/infrastructure/usda/USDAModule.kt:14-19`; `.../ai/infrastructure/AiInfrastructureModule.kt:23-40`.
- Probe/validator precedent: `.../ai/infrastructure/OpenRouterAiConnectionValidator.kt`, `.../ai/domain/AiConnectionValidator.kt:21`.
- Preferences pattern (if needed): `.../ai/infrastructure/DataStoreAiSettingsRepository.kt`.
- Server repo (read-only): `D:\forked-projects\FoodUs-Server` — wire contract v1 is Milestone 3 Story 1 there, Not Started as of 28 July 2026; minting authority and alphabet are among its open decisions.
- Planning inputs: milestone-3.md Story 6 + Relay Contract Conformance; design.md "The Multiplayer Exception".

## Complaints / Friction

### Planning gated by an absent contract

**What happened:** The story's entire server surface depends on a wire contract that is Not Started in the owning repo.
**Why this made the task harder:** Endpoint, DTO, auth, and minting semantics cannot be planned concretely without inventing a competing contract, which Relay Contract Conformance forbids.
**What was tried:** Read-only inspection of the foodus-relay repo confirmed the contract's status and its open decisions; the plan isolates everything contract-owned behind one domain seam with an explicitly gated final step.
**What would improve this:** Owner carries contract v1 (or at least the minting-authority and alphabet decisions) across; Step 5 then becomes concretely plannable in one sitting.

## Execution Log

Not started.

## Completion Review

Not started.
