# Plan: Household proof

## Metadata

- Task Type: `SPIKE`
- Status: `Draft`
- Owner: Jarryd Adaens
- Last Updated: 28 July 2026

## Linked Context

- Milestone: [context/milestones/milestone-3.md](../../../milestones/milestone-3.md)
- Story: [Story 15: Household proof](../../../milestones/milestone-3.md#story-15) `[STORY 3.15]`
- Dictation source: [2026-07-27 Milestone 3 multiplayer addendum](../../../dictations-tier-0/2026-07-27_milestone-3_multiplayer-addendum.md); [2026-07-27 relay tier-0 seed](../../../dictations-tier-0/2026-07-27_addendum_foodus-relay-tier0-seed.md)
- Design authority: `context/design.md` — "The Multiplayer Exception" (relay model, E2E, key-loss policy, poll-on-wake)
- Related Plans:
  - Story 7 (relay URL setting) — both phones must be configured through it before any scenario runs.
  - Stories 1, 2, 5, 6, 8–14 — every feature under test; this plan validates their combined behavior and produces the milestone's Definition-of-Done evidence.
  - Story 10 (envelope & E2E pipeline) — plan currently blocked on contract v1; its refuse-loudly behavior is exercised by Scenario 7 here.
- External Tooling: none required. This is a structured manual validation session; results are recorded in this plan's Execution Log / Evidence sections.

## CER

- Complexity: 3
- Effort: 4
- Risk: 6
- Notes: Inline estimate. No code is written, so complexity is protocol design only. Effort is a multi-hour, two-phone, ordered session including a destructive re-key drill with backup/restore. Risk is evidence risk, not code risk: this story *is* the milestone's DoD evidence, so a skipped scenario or an undocumented failure silently weakens the milestone's closure claim; the re-key drill also intentionally destroys device state and loses in-flight messages, which is unrecoverable if sequenced wrongly (mitigated by running it last).

## Objective

Run a structured, ordered, two-phone manual validation session (owner ↔ wife, both Galaxy-class Android, both on the live relay) that proves every Milestone 3 app-side behavior in the Definition of Done — friend add via code, Full-trust group creation and invite acceptance, cross-diary round-trip with correct meal slotting, Suggest flow, block flows, and a key-loss re-key drill — and record pass/fail evidence for each scenario in this plan, so the milestone can close on documented proof rather than assertion.

## Scope

### In Scope

- A precondition checklist gating session start (relay deployed, phones configured, stories implemented).
- Eight ordered validation scenarios with expected outcomes and per-scenario evidence capture.
- Recording results (pass/fail, observations, screenshots/notes references) into this plan's `## Execution Log` and `## Evidence / References`.
- Updating milestone/story status wording afterwards to match what the evidence actually shows.

### Out Of Scope

- Any app code changes. Defects found are filed as new stories/bugs, not fixed mid-session.
- Server-side properties (ciphertext-only storage, 30-day sweep) — built and evidenced in the foodus-relay repository per the milestone's DoD; this session only observes app-visible behavior.
- Editing anything in `D:\forked-projects\FoodUs-Server`.

## Non-Goals

- Not an automated test suite; per the unit-testing rule, emergent multi-device behavior is validated manually and that boundary is documented here.
- Not a performance or load exercise; a household pair is the entire population.
- Not a security audit of the relay; the accepted server-MITM weakness (design.md) is out of scope by design.

## Current Understanding

- The app-side features under test are delivered by Stories 1, 2, 5, 6, 8–14; their plans live under `context/implementation-plans/milestone-3/`. Story 10's plan is blocked on contract v1, so envelope specifics (including unknown-version disposition) are not yet fixed — Scenario 7 stays provisional until that settles.
- Three-tier meal matching (Story 13): name match → time match against the recipient's configured meal windows (the slots checked by the existing `validate-meals` CI) → first-meal fallback; the never-dropped invariant must hold in every tier.
- Key-loss policy (design.md): keys live and die with the device; restore backup (GUID + social graph survive), generate fresh key pair, re-announce; friends pick up the new key on next poll; in-flight messages to the old key are lost by design under the 30-day sweep.
- Poll-on-wake transport: every cross-device assertion below is verified *after a deliberate app relaunch/wake* on the receiving phone — there is no push.
- Harness expectation (AGENTS.md §0.2): runtime-behavior claims need a matching harness check or a documented reason it could not run. `harness/` currently has no two-device relay module; the documented reason is that this behavior spans two physical phones plus a private live server, which the harness cannot drive. Manual structured validation with recorded evidence is the designated method for this story.
- Assumption: both phones run the same app build (side-by-side dev identity exists for debug builds per repo history; the session should use one consistent build channel).

## Questions / Unknowns

- Q: `[STORY 3.15]` Where does the session evidence live — inline in this plan's Execution Log only, or also as captured artifacts (screenshots, logcat extracts) in a folder beside it?
  Impact: Decides whether the DoD evidence is prose-only or file-backed; file-backed is stronger for milestone closure.
  Assumption: Prose results inline in this plan; screenshots/photos kept in the owner's own storage and referenced by name, since committing personal diary screenshots to a public repo conflicts with the privacy stance.
  Status: OPEN
  Answer: —

- Q: `[STORY 3.15]` Is the unknown-envelope-version refusal (Scenario 7) testable at session time — is there a debug affordance or server-side way to emit an unknown-version envelope, given contract v1 is not yet written?
  Impact: If untestable, the DoD's refuse-loudly evidence must be covered by Story 10's own validation instead, and this plan must say so rather than silently skipping.
  Assumption: Deferred to a server-assisted test (owner sends a crafted envelope via the relay) if foodus-relay provides one; otherwise marked "not testable in this session" with the gap documented.
  Status: OPEN
  Answer: —

- Q: `[STORY 3.15]` For the re-key drill, which backup path is authoritative — the app's own export/backup mechanism or a device-level backup?
  Impact: The drill must restore GUID + social graph exactly as a real device loss would; the wrong backup path invalidates the evidence.
  Assumption: The app's own database backup/restore mechanism (the one Story 2 rides), performed on the owner's phone, not the wife's.
  Status: OPEN
  Answer: —

## Execution Steps

1. **Precondition gate (session does not start until all boxes tick)**
   - Why: every scenario depends on the full stack being live; a partial run produces misleading evidence.
   - Edits: none — checklist recorded in Execution Log.
   - Checklist: foodus-relay contract v1 deployed at the owner's private HTTPS endpoint (Story 4 gate released by owner); Stories 1, 2, 5, 6, 8–14 implemented and installed on both phones (same build); both phones configured with the relay URL via Story 7's setting and passing its capability check; fresh backups taken of BOTH phones' databases before starting (the drill is destructive).

2. **Scenario 1 — Friend add via code**
   - Why: proves resolve-by-code and that becoming friends is the key exchange.
   - Steps: wife's phone displays her friend code; owner enters it in add-by-code. Expected: friend row appears with her current username; stored GUID and public key present (verify via whatever inspection surface Story 9 provides, else via a second interaction that requires the key). Repeat in the opposite direction if the design requires mutual add; record which it is.
   - Evidence: screenshots of both friends lists; note the resolved username.

3. **Scenario 2 — "Adaens" group creation and invite acceptance**
   - Why: proves the two-person Full-trust group lifecycle over the pipeline.
   - Steps: owner creates group "Adaens", trust Full, picks wife. Expected: her member row renders grey (invited) on his phone; on her next app wake a notification fires and the group card shows Accept/Reject; she Accepts; on his next wake her row is no longer grey.
   - Evidence: screenshots of the grey invited state, her Accept card, and the settled group on both phones.

4. **Scenario 3 — Taco round-trip with deliberate three-tier matching**
   - Why: the marquee feature and the never-dropped invariant, across all three matching tiers.
   - Steps (three sends, one per tier): (a) name match — recipient has a meal named identically to the sender's meal; entry must land in it; (b) time match — send with a meal name the recipient does not have, timestamped inside one of her configured meal windows; must land in that window's meal; (c) first-meal fallback — timestamp outside every window and unknown meal name; must land in her first meal. Verify each on her phone after wake, on the sender's entry date, with complete nutrition/name (recipient does not share sender's data sources).
   - Evidence: per-tier screenshots of the sent entry and the landed entry; note date and meal slot.

5. **Scenario 4 — Suggest flow**
   - Why: proves the lower-trust tier end to end.
   - Steps: create a second group at Suggest trust (or per Story 11's model if trust is fixed per group, use a new group). Send an entry; expected: it stacks in her per-group suggestion queue, does NOT enter the diary; she Accepts one (lands via the same matching, original timestamp) and Rejects another (never lands, disappears from queue).
   - Evidence: screenshots of the queue, the accepted entry in the diary, and the diary showing no trace of the rejected one.

6. **Scenario 5 — Block flows**
   - Why: proves the permanent block promise on both the friend and group surfaces.
   - Steps: with a disposable test profile/third install if available, else deferred to the friend-level flow between the two phones and immediately unwound: (a) delete-and-block a friend; blocked side regenerates a fresh code; blocker's re-add attempt with the fresh code returns "user not found"; (b) group invite Reject → Block: no further notifications from that group, and re-add attempt tells the adder the person can't be added. Record exactly which parts were run and on which profiles; unblocking/cleanup steps recorded too (if no unblock surface exists, note that the block is permanent and plan the test profiles accordingly).
   - Evidence: screenshots of "user not found" and the can't-be-added notice.
   - Dependencies: run AFTER Scenarios 1–4 so blocking does not sabotage earlier flows; prefer sacrificial profiles.

7. **Scenario 6 — Key-loss re-key drill (destructive; run last among happy paths)**
   - Why: proves recovery without weakening the security model.
   - Steps: queue one in-flight message from wife → owner (send while his app is closed). Simulate his device loss: wipe app data/reinstall, restore his database backup (GUID + social graph intact), app generates a fresh key pair and announces it to the relay. Expected: the queued in-flight message encrypted to the old key is unopenable and lost (by design — verify it does NOT land and that the failure is not silent data corruption); on her next wake her device picks up his new key; a fresh send from her lands correctly.
   - Evidence: note the lost message (what was sent, that it never landed), screenshot of the post-re-key successful delivery.

8. **Scenario 7 — Refuse-loudly and unreachable-relay behavior (as testable)**
   - Why: DoD app-side obligations from Relay Contract Conformance.
   - Steps: (a) unknown envelope version — only if a crafted-envelope affordance exists (see Questions); expected: Notification Center event, never a silent drop; (b) unreachable relay — point one phone at an invalid HTTPS host via Story 7's setting (or disable network), wake the app; expected: graceful degradation, relay-backed features hidden/greyed, no crash; restore the real URL afterwards and confirm recovery.
   - Evidence: screenshots of the Notification Center event (if testable) and the degraded UI.

9. **Record and close**
   - Why: the session's value is durable evidence.
   - Edits: fill `## Execution Log` (per-scenario pass/fail + observations), `## Evidence / References`, and `## Completion Review` in this plan; update Story 15 and milestone Status wording to match results; file any defects found as new stories — do not fix inline.

## Validation

### Automated Checks

- None run this session by design; see Current Understanding for the harness gap rationale. `validate-meals` CI (existing) indirectly underwrites the meal-window data Scenario 3(b) relies on.

### Manual Checks

1. Every scenario above executed in order on both phones, with per-scenario evidence captured at the moment of observation (not reconstructed afterwards).
2. Poll-on-wake discipline: every "lands on the other phone" claim verified only after a deliberate app wake on the receiving phone.

### Acceptance Criteria

- All eight scenarios executed or explicitly recorded as not-testable with a reason; none silently skipped.
- The never-dropped invariant held in all three matching tiers.
- The re-key drill recovered identity (GUID, friendships, groups) with only the by-design in-flight loss.
- Results recorded in this plan constitute the milestone's app-side DoD evidence.

## Risk Mitigation

- Risk: The destructive re-key drill corrupts real household diary data.
  Mitigation: Fresh backups of both phones are a hard precondition; the drill runs on the owner's phone only, after all happy-path scenarios.
- Risk: Block scenarios permanently damage the real owner↔wife social graph.
  Mitigation: Prefer sacrificial test profiles; if impossible, document the exact unwind path before executing, and accept a re-add cycle as part of the test.
- Risk: A scenario fails and the session dissolves into debugging.
  Mitigation: Record-and-continue policy — failures are evidence, fixes are new stories; the session only stops if a failure blocks all downstream scenarios.
- Risk: Scenario 7(a) proves untestable, leaving a DoD gap.
  Mitigation: Accepted with documentation — the gap transfers to Story 10's own validation; noted in the Completion Review.

## Phase Split

Not needed — one session, sequential by nature.

## Evidence / References

To be filled at session time: per-scenario results, screenshot references, build/version installed on both phones, relay contract version reported by the capability endpoint.

## Complaints / Friction

None at planning time.
