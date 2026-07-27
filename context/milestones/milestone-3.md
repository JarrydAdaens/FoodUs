---
name: milestone-3
description: Milestone 3 - Multiplayer. Profiles, friends, two-person groups, cross-diary logging, and the dumb encrypted relay server that breaks the island in exactly one controlled place.
metadata:
  version: "3.1"
  agentic_rails_source_version: "3.0"
  owner: "Jarryd Adaens"
  repo: "FoodUs (fork of maksimowiczm/FoodYou)"
---
# Milestone 3: Multiplayer

> Milestone. A coherent macro-feature or delivery outcome. (Tier numbers live only in `design.md` / `agenticworkflow.md`.)
>
> Related: [design.md (Milestones Index)](../design.md#milestones-index), [../backlog/](../backlog/)
>
> Source dictations: [2026-07-27 Milestone 3 multiplayer addendum](../dictations-tier-0/2026-07-27_milestone-3_multiplayer-addendum.md),
> [2026-07-27 FoodUs relay tier-0 seed](../dictations-tier-0/2026-07-27_addendum_foodus-relay-tier0-seed.md)
> (server repo split — relay decisions and app-side conformance obligations)

---

## Intent

Make FoodUs multi-user: profiles, friends, and two-person trusted groups whose marquee feature
is **cross-diary logging** — a Full-trust pair (the owner and his wife) can write food entries
directly into each other's diaries, so a shared meal is logged once and lands in both diaries.
The island is broken in exactly one controlled place: a dumb, encrypted, store-and-forward relay
server that ferries sealed packets between devices. Everything else stays local-first.

## Why it matters

- Cross-diary logging (the owner's wife's idea, seen in no other fitness tracker) directly serves
  the fork's founding goals: increase logging, decrease effort, be a better team.
- The social structures (profiles, friend codes, groups) generalize to friends beyond the
  household once Obtainium distribution (Milestone 1) makes handing the app out easy.
- The relay/E2E architecture proves multi-user can exist without sacrificing the fork's
  constitutional privacy stance: no accounts, no cloud diary, no server-readable data.

## Outcome / Definition of Done

Two household phones, each with a profile, connected as friends via friend code, in a Full-trust
two-person group. A food entry saved to the group on one phone lands correctly slotted in the
other's diary via the encrypted relay. Suggest-trust and block flows proven. A simulated device
loss recovers via re-key without weakening the security model. The server holds nothing readable
and sweeps undelivered messages at 30 days. Story 14 is the evidence for the app-side
behaviors; the server-side properties (ciphertext-only storage, the 30-day sweep) are built and
evidenced in the foodus-relay repository, whose deployment the Story 5 gate confirms.

## Status

Not Started

---

## Constitutional constraints (restated from dictation)

- **No cloud migration.** Each person's database stays on their device, backed up and restored by
  them. The server is a transmission vector between databases, nothing more.
- **No accounts, no login, no server-side backup, no server-side diary data in the clear.**
- **Fork philosophy applies:** additive overlays, minimal merge-conflict surface with upstream.
- **Respect the user:** no nagging, no dark patterns.
- **Two-member group cap.** Milestone 3 groups hold exactly two people — a deliberate scope cut
  that eliminates the all-pairs friendship rule, third-party friendship lookups (a privacy hole),
  per-member trust levels, and live member-list updates. See Deferred / Follow-up Work.

---

## Relay Contract Conformance (2026-07-27 relay seed dictation)

The relay is built in its own repository, **foodus-relay**, which owns the wire contract as a
written specification document. These obligations bind every story in this milestone that talks
to the relay; they are stated once here rather than repeated per story.

1. **Conformance, not ownership.** The app implements the wire spec that lives in the
   foodus-relay repo. Envelope data classes are hand-written to match the spec (no submodules,
   no shared schema machinery). When the spec changes, both sides change in the same sitting.
2. **Envelope version stamping.** Every packet the app sends carries the envelope version. On
   receiving an unknown version, the app refuses loudly — a Notification Center event, never a
   silent drop.
3. **Two-way tolerance.** Deserialisation ignores unknown fields and treats absent fields as
   "not provided" — never a crash. This is what lets the relay evolve additively.
4. **Capability-aware UI.** Before exposing a relay-backed feature, the app checks the relay's
   version/capability endpoint and hides or greys features the connected relay doesn't report.
   A phone that updates before the server deploys waits gracefully.
5. **HTTPS only**, via the existing Ktor client stack.
6. **Dependency notes.** Every story consuming a relay capability carries a one-way, versioned
   dependency note naming its server parent story, using the shared-slug convention (e.g. a
   `...-friends` story on both sides). The dependency arrow only ever points app → server; the
   owner releases the block once the server story is deployed. This repo's agent reads the
   relay repo's contract spec freely but never edits that repo.
7. **Server leads, app follows.** The relay deploys before the app feature that consumes it —
   enforced by the owner.

**Open contract question (owned by foodus-relay, flagged here 2026-07-27):** unknown-version
envelope disposition — when the app refuses an envelope version it doesn't know, is the message
acknowledged off the mailbox (accepting loss) or left queued (risking a repeated poll error)?
Must be settled in the wire contract before Story 8 is planned.

---

## Story Index

| # | Status | Story | Type | Complexity | Effort | Risk | Plan |
| --- | --- | --- | --- | --- | --- | --- | --- |
| 1 | Not Started | [Tabbed UI shell](#story-1) | Feature | — | — | — | *link when generated* |
| 2 | Not Started | [Profile](#story-2) | Feature | — | — | — | *link when generated* |
| 3 | Not Started | [Crypto identity](#story-3) | Feature | — | — | — | *link when generated* |
| 4 | Externalized | [Relay architecture decisions](#story-4) | External Dependency | — | — | — | *n/a — resolved in foodus-relay* |
| 5 | Externalized | [Relay server delivery gate](#story-5) | External Dependency | — | — | — | *n/a — delivered by foodus-relay* |
| 6 | Not Started | [Friend codes](#story-6) | Feature | — | — | — | *link when generated* |
| 7 | Not Started | [Friends list](#story-7) | Feature | — | — | — | *link when generated* |
| 8 | Not Started | [Message envelope & E2E pipeline](#story-8) | Feature | — | — | — | *link when generated* |
| 9 | Not Started | [Groups](#story-9) | Feature | — | — | — | *link when generated* |
| 10 | Not Started | [Save to Group](#story-10) | Feature | — | — | — | *link when generated* |
| 11 | Not Started | [Receive into diary](#story-11) | Feature | — | — | — | *link when generated* |
| 12 | Not Started | [Suggestion queue](#story-12) | Feature | — | — | — | *link when generated* |
| 13 | Not Started | [Notification Center tab](#story-13) | Feature | — | — | — | *link when generated* |
| 14 | Not Started | [Household proof](#story-14) | Research | — | — | — | *link when generated* |
| 15 | Not Started | [Configurable relay URL setting](#story-15) | Feature | — | — | — | *link when generated* |

---

## Stories

<a id="story-1"></a>

### Story 1: Tabbed UI shell

**Type:** Feature

**Summary:**
The single-screen app becomes tabbed with a bottom navigation row in the style of Lose It /
MyFitnessPal. Tabs left to right: **Groups** (new), **Log** (the entire existing app — day list,
diary, foods — unchanged), **Notifications** (new, rightmost). The new tabs may land as stubs;
Story 13 finalizes Notifications.

**Why / value:**
Every other Milestone 3 surface hangs off this shell. It is local-only and parallelizable with
the external relay gates (Stories 4-5).

**Rough scope:**
Root navigation and top-level UI composition. Additive: the existing app moves into the Log tab
without internal changes.

**CER:**

- Complexity: —
- Effort: —
- Risk: —

**Plan:** `../implementation-plans/milestone-3/story-1-tabbed-ui-shell/plan.md`

**Status:** Not Started

---

<a id="story-2"></a>

### Story 2: Profile

**Type:** Feature

**Summary:**
Deliberately minimal profile, created on demand from a My Profile card at the top of the Groups
tab. Fields: **ID** (randomly generated GUID, hidden from casual view, never changes — the
primary key for everything social), **Username** (free text, purely cosmetic, collisions allowed,
renameable — explicitly *not* the unique identifier), **friend code** (Story 6), **created** and
**last-edited** dates. Stored in the Room database so it rides backup/restore.

**Why / value:**
Profiles exist *for* groups; the GUID is the core identity everything else references.

**Rough scope:**
Room schema addition, Groups-tab My Profile card, profile create/edit UI, backup inclusion.

**CER:**

- Complexity: —
- Effort: —
- Risk: —

**Plan:** `../implementation-plans/milestone-3/story-2-profile/plan.md`

**Status:** Not Started

---

<a id="story-3"></a>

### Story 3: Crypto identity

**Type:** Feature

**Summary:**
An asymmetric key pair is generated alongside the GUID at profile creation. The public key is
stored with the profile; the private key lives in the **Android Keystore** — hardware-backed on
target devices (Galaxy S22 Ultra class), never entering app memory, never touching the Room
database, DataStore, any file, or any backup. The app asks the Keystore to decrypt blobs; the
key never leaves the vault. Consequence: the database backup stays fully portable; the private
key deliberately is not (keys live and die with the device — see the key-loss policy in
[design.md](../design.md)).

**Why / value:**
Foundation of the end-to-end encryption model. Without it nothing sealed can be sent or opened.

**Rough scope:**
Key-pair generation, Android Keystore integration, public key on the profile record.

**Dependency note:** the local key work is buildable immediately. The seed dictation also
obliges this story's identity to be **registered with the relay** — the public key rides the
register/update-profile endpoint (server parent shared slug `...-profile-registration`), and
re-key announcements (Story 14's drill) reuse the same seam. That registration call is blocked
by foodus-relay: profile registration endpoints, contract v1, deployed (Story 5 gate), and
needs Story 15's relay URL on-device.

**CER:**

- Complexity: —
- Effort: —
- Risk: —

**Plan:** `../implementation-plans/milestone-3/story-3-crypto-identity/plan.md`

**Status:** Not Started

---

<a id="story-4"></a>

### Story 4: Relay architecture decisions

**Type:** External Dependency

**Summary:**
Formerly the relay server architecture spike (research, this repo). The 2026-07-27 relay
infrastructure session resolved its stack/hosting question — **ASP.NET (C#) minimal API +
SQLite behind Caddy TLS on a self-hosted DigitalOcean droplet (Sydney)** — and moved the relay
into its own repository, **foodus-relay**, with its own agentic-rails structure. The remaining
open decisions transfer with it and resolve in that repo's first milestone, before the relay's
first implementation plan:

1. **Relay endpoint authentication** — proof of GUID ownership without accounts (likely device
   key-pair request signing plus replay protection and a re-key trust rule).
2. Friend-code minting authority: server-assigned vs client-generated + registered. *(Owner
   input wanted.)*
3. Friend-code alphabet: exact charset and case rules (shape fixed: 4-4-4 blocks, dashes,
   letters+numbers).
4. The exact wire contract document (endpoints, envelope schema, auth handshake) — the first
   deliverable of the relay repo's rails.

**Gate for this repo:**
This story is done for the app when the foodus-relay repo publishes its wire contract spec
(contract v1) covering the minimum API surface: register/update profile, resolve friend code →
{ GUID, username, public key } with block enforcement ("user not found"), regenerate friend
code, push sealed message, poll/drain mailbox, record blocks, 30-day sweep, and the
version/capability endpoint. The owner carries the spec's availability across; no app code and
no edits to the relay repo happen under this story.

**Source:** [2026-07-27 relay tier-0 seed](../dictations-tier-0/2026-07-27_addendum_foodus-relay-tier0-seed.md)

**CER:**

- Complexity: —
- Effort: —
- Risk: —

**Plan:** n/a — decision work happens in foodus-relay; this repo tracks the gate only.

**Status:** Externalized — awaiting contract v1 from foodus-relay

---

<a id="story-5"></a>

### Story 5: Relay server delivery gate

**Type:** External Dependency

**Summary:**
Formerly the relay server build (this repo). The relay is now delivered by the **foodus-relay**
repository (2026-07-27 relay seed dictation): registration, friend-code resolve, mailbox
push/poll, block enforcement (blocked requesters get "user not found", indistinguishable from
nonexistent), the 30-day sweep, and the version/capability endpoint — per that repo's wire
contract, behind Caddy TLS, deployed to the owner's private endpoint. The server never sees
plaintext; no accounts, no login, no server-side backup, no web/companion clients.

**Gate for this repo:**
This story is done for the app when the owner confirms the relay is **deployed and reachable
over HTTPS at the private endpoint, serving contract v1**. That confirmation releases the
dependency notes on Stories 6 onward ("server leads, app follows"). No app-side work happens
under this story beyond what Story 8 wires up; no edits to the relay repo.

**Source:** [2026-07-27 relay tier-0 seed](../dictations-tier-0/2026-07-27_addendum_foodus-relay-tier0-seed.md)

**CER:**

- Complexity: —
- Effort: —
- Risk: —

**Plan:** n/a — build and deployment happen in foodus-relay; this repo tracks the gate only.

**Status:** Externalized — awaiting deployed relay (contract v1) from foodus-relay

---

<a id="story-6"></a>

### Story 6: Friend codes

**Type:** Feature

**Summary:**
Every profile has a friend code: 12 characters in three dash-separated blocks of four,
product-key style (e.g. `A7F3-9KQ2-XM41`) — a revocable, disposable layer over the permanent
GUID. Display it in the profile; provide a regenerate flow. Regenerating does **not** break
existing friendships but kills every old copy of the code — anyone holding it gets nothing.

**Why / value:**
The out-of-band connection handle that makes adding friends possible without usernames being
identifiers, and the revocation lever that keeps stale codes harmless.

**Rough scope:**
Profile UI (display + regenerate), server calls for code registration/regeneration per the
foodus-relay wire contract.

**Dependency note:** blocked by foodus-relay: friend-code endpoints (shared slug
`...-friend-codes`), contract v1, deployed. Owner releases via the Story 5 gate. Also needs
Story 15's relay URL setting on-device.

**CER:**

- Complexity: —
- Effort: —
- Risk: —

**Plan:** `../implementation-plans/milestone-3/story-6-friend-codes/plan.md`

**Status:** Not Started

---

<a id="story-7"></a>

### Story 7: Friends list

**Type:** Feature

**Summary:**
Xbox Live's friends model as North Star, minus username-as-primary-key. A Friends card on the
Groups tab opens the friends list; a plus button opens add-by-code: enter someone's friend code,
the app resolves it via the relay, and the returned GUID, current username, and public key are
stored locally — becoming friends *is* the key exchange. Tapping a friend expands the row:
**Delete** (remove, breaking the connection both ways) and **Delete and block** (remove + add to
a permanent blocked list; a blocked person's future attempts return "user not found", even with a
fresh code). Non-goals for the expanded row: no passing on another friend's code, no bios.

**Why / value:**
The social graph every group is built from, and the block mechanism that keeps specific people
out even inside a malicious circle of friends.

**Rough scope:**
Friends + blocked-list Room storage, friends-list UI, add-by-code flow, expandable rows with
delete / delete-and-block, server resolve + block calls.

**Dependency note:** blocked by foodus-relay: friend-code resolve + block endpoints (shared
slug `...-friends`), contract v1, deployed. Owner releases via the Story 5 gate.

**CER:**

- Complexity: —
- Effort: —
- Risk: —

**Plan:** `../implementation-plans/milestone-3/story-7-friends-list/plan.md`

**Status:** Not Started

---

<a id="story-8"></a>

### Story 8: Message envelope & E2E pipeline

**Type:** Feature

**Summary:**
The packet schema and the encrypt/send/poll/decrypt/route plumbing. Packets are full,
self-contained JSON envelopes — complete nutritional information, name, and everything needed to
construct the entry on the recipient's device (the recipient may not share the sender's data
sources) — plus the **entry timestamp** and **meal name**. Senders encrypt with the recipient's
stored public key before anything leaves the device. Transport is **poll on app wake only**: a
brief "getting messages" step drains the mailbox, decrypts, routes (Full → insert + notify;
Suggest → queue), and populates the Notification Center. No background polling, no Firebase
Cloud Messaging — Google stays out of the privacy story; the latency cost is acceptable for
non-time-critical diet data.

**Why / value:**
The shared pipeline every cross-device action (entries, invites, accepts, blocks, key
re-announcements) rides on.

**Rough scope:**
Packet schema, crypto envelope (via Story 3's Keystore identity), send queue, poll-on-wake drain
UI step, message router. Envelope data classes are hand-written to the foodus-relay wire spec;
version stamping, refuse-loudly, and two-way tolerance per Relay Contract Conformance.

**Dependency note:** blocked by foodus-relay: mailbox push/poll endpoints and envelope schema
(shared slug `...-mailbox`), contract v1, deployed. Owner releases via the Story 5 gate. The
unknown-version envelope disposition question (see Relay Contract Conformance) must be settled
in the contract before this story is planned.

**CER:**

- Complexity: —
- Effort: —
- Risk: —

**Plan:** `../implementation-plans/milestone-3/story-8-envelope-e2e-pipeline/plan.md`

**Status:** Not Started

---

<a id="story-9"></a>

### Story 9: Groups

**Type:** Feature

**Summary:**
Group data model (own GUID, name, optional description, trust level, created/last-edited dates,
member collection with per-member state) with the **two-member cap**: exactly the creator plus
one friend; members are equal, ownership deliberately loose. Five-step create form (name
mandatory, description optional, trust level mandatory, pick exactly one friend from a searchable
picker, Create button with are-you-sure-guarded Cancel). Trust levels, set once at creation:
**Full** (write directly into each other's diaries and edit each other's meal plans; auto-add +
notification), **Suggest**
(entries stack in a per-group suggestion queue for accept/reject), **None** (presence only; the
enum's zero value, no defined use case yet). Invitation lifecycle: invited member renders grey;
on their device a notification fires and the group appears as a distinct card with **Accept** /
**Reject**; Reject offers Reject / Block / Cancel; Block puts the group on a group blacklist —
no notifications ever again, and anyone trying to re-add the blocker is told they can't be added.
After accepting, the expanded group card offers **Leave** and **Leave and Block**; under the
two-member cap a departure leaves the group dead/dormant.

**Why / value:**
The trust container that authorizes cross-diary logging. With two members, group-level trust
*is* per-person trust.

**Rough scope:**
Group + membership Room storage, Groups-tab cards (create, join, per-group), create form, invite
lifecycle messages over Story 8's pipeline, group blacklist.

**Dependency note:** consumes the relay only through Story 8's pipeline (contract v1); no new
server capability of its own.

**Open decisions:**

- Group-block notice wording (resolve before shipping the notice) — the dictated text ("please
  respect Emily's decision to stay out of this group") reveals the blocker's username to the
  adder. Owner acknowledged the flag.
- **Meal-plan editing scope.** The dictation grants Full trust the ability to "edit each other's
  meal plans" (its A6), but its own story list (Part D) defines no story delivering that
  capability — every send/receive story covers diary entries only. Confirm with the owner what
  meal-plan editing means concretely and which story owns it (extend this story, extend
  Stories 10-11, or add a story) before planning.

**CER:**

- Complexity: —
- Effort: —
- Risk: —

**Plan:** `../implementation-plans/milestone-3/story-9-groups/plan.md`

**Status:** Not Started

---

<a id="story-10"></a>

### Story 10: Save to Group

**Type:** Feature

**Summary:**
On the food-entry screen, **Save** stays on the right; a new big **Save to Group** button appears
on the left, with a card above it listing the user's groups with checkboxes. Tapping Save to
Group transmits the entry via the relay to every member of every checked group (one recipient
per two-person group; multiple checked groups fan out to multiple people). Delivery behavior per
group follows its trust level.

**Why / value:**
The sending half of the marquee feature: cook tacos together, log once.

**Rough scope:**
Food-entry screen UI addition, fan-out send over Story 8's pipeline.

**Dependency note:** consumes the relay only through Story 8's pipeline (contract v1); no new
server capability of its own.

**Open decision:**

- Do None-trust groups appear in the Save-to-Group checkbox list (hidden vs greyed out)?
  *(Flagged during dictation; owner input wanted.)*

**CER:**

- Complexity: —
- Effort: —
- Risk: —

**Plan:** `../implementation-plans/milestone-3/story-10-save-to-group/plan.md`

**Status:** Not Started

---

<a id="story-11"></a>

### Story 11: Receive into diary

**Type:** Feature

**Summary:**
Full-trust auto-insert with three-tier meal matching when an entry is accepted into a recipient's
diary: (1) **name match** — a meal with the same name as the sender's meal name; (2) **time
match** — the entry's timestamp against the recipient's own configured meal time windows (the
slots validated by the existing `validate-meals` CI); (3) **first-meal fallback** — if no meal
covers the time, it lands in the first meal in the recipient's list. **Invariant: a delivered
entry always lands. It is never dropped.**

**Why / value:**
The receiving half of cross-diary logging; the never-dropped invariant is what makes Full trust
trustworthy.

**Rough scope:**
Message router → diary insertion, meal-matching resolver, "entry added" notification emission.

**Dependency note:** consumes the relay only through Story 8's pipeline (contract v1); no new
server capability of its own.

**Open decisions (confirm assumptions):**

- Date/meal carry-over semantics: the entry lands on the sender's entry date in the recipient's
  diary (assumed yes; flagged during dictation).
- Whether Suggest-queue accept applies the same three-tier matching at accept time using the
  original timestamp (assumed yes; shared with Story 12).

**CER:**

- Complexity: —
- Effort: —
- Risk: —

**Plan:** `../implementation-plans/milestone-3/story-11-receive-into-diary/plan.md`

**Status:** Not Started

---

<a id="story-12"></a>

### Story 12: Suggestion queue

**Type:** Feature

**Summary:**
Suggest-trust delivery: incoming entries stack in a suggestion queue tied to the group; the
recipient works through them, accepting or rejecting each one. Accepting adds the entry to the
diary (via Story 11's matching — see the shared open decision there).

**Why / value:**
The lower-trust tier that still cuts logging effort without granting direct diary write access.

**Rough scope:**
Suggestion-queue storage, per-item accept/reject review UI, routing from Story 8's pipeline.

**Dependency note:** consumes the relay only through Story 8's pipeline (contract v1); no new
server capability of its own.

**CER:**

- Complexity: —
- Effort: —
- Risk: —

**Plan:** `../implementation-plans/milestone-3/story-12-suggestion-queue/plan.md`

**Status:** Not Started

---

<a id="story-13"></a>

### Story 13: Notification Center tab

**Type:** Feature

**Summary:**
The rightmost tab collecting every event the system generates (meal added, meal failed to add,
friend added you, you joined a group, a group changed its name, ...) — the single place the app
communicates more to the user. Notifications are stored persistently; dismissed/read ones are
hidden by default with a control exposing the historical list. Under poll-on-wake there is no
push transport: "notifications" are materialized locally from messages drained at poll time,
plus local events.

**Why / value:**
Without it, cross-device events are invisible; it is deliberately the app's only broadcast
surface — no nagging.

**Rough scope:**
Persistent event store, tab UI with dismissed-history toggle, wiring to all emitters. A stub tab
can land with Story 1; finalize after Stories 9-12 exist as emitters.

**CER:**

- Complexity: —
- Effort: —
- Risk: —

**Plan:** `../implementation-plans/milestone-3/story-13-notification-center/plan.md`

**Status:** Not Started

---

<a id="story-14"></a>

### Story 14: Household proof

**Type:** Research

**Summary:**
Real two-phone end-to-end proof (owner ↔ wife): friend add via code, create the "Adaens"
Full-trust group, taco round-trip landing correctly slotted in the other diary, Suggest flow,
block flows, and a key-loss re-key drill (fresh key pair announced to the server, friends'
devices pick up the changed key on next poll; in-flight messages to the old key are unopenable
and lost by design).

**Why / value:**
Doubles as the milestone's Definition-of-Done evidence.

**Rough scope:**
Structured manual validation session on both household phones against the live relay; record
results as story evidence.

**Dependency note:** blocked by foodus-relay: full contract v1 deployed at the owner's private
endpoint (Story 5 gate released), with both phones configured via Story 15's relay URL setting.

**CER:**

- Complexity: —
- Effort: —
- Risk: —

**Plan:** `../implementation-plans/milestone-3/story-14-household-proof/plan.md`

**Status:** Not Started

---

<a id="story-15"></a>

### Story 15: Configurable relay URL setting

**Type:** Feature

**Summary:**
A settings surface where the relay endpoint is entered by the user — same spirit and likely
same neighbourhood as the AI endpoint configuration from Milestone 2. Both household phones are
pointed at the owner's private endpoint by typing it in; the address ships nowhere in code or
repo, and strangers running the published app can point it at their own relay. Scope beyond a
bare text field: input validation (HTTPS scheme required, plain HTTP rejected), on-device
persistence like the AI/USDA settings, a connection check against the relay's
version/capability endpoint, and graceful behavior when the relay is unset or unreachable —
relay-backed features hide or grey per the capability-aware UI rule.

**Why / value:**
The seam that keeps the owner's endpoint private while making every relay-consuming story
configurable rather than hard-coded (2026-07-27 relay seed dictation, app obligation 1).

**Rough scope:**
Settings UI + on-device storage, URL validation, capability-endpoint check, unset/unreachable
handling consumed by later stories' capability gating.

**Dependency note:** the settings surface itself is local-only and buildable immediately; the
connection check exercises foodus-relay's version/capability endpoint (contract v1, deployed)
and degrades gracefully until the Story 5 gate is released.

**CER:**

- Complexity: —
- Effort: —
- Risk: —

**Plan:** `../implementation-plans/milestone-3/story-15-relay-url-setting/plan.md`

**Status:** Not Started

---

## Interdependency Order

Local-only foundations first, then the external relay gates (everything social depends on the
contract and the deployed relay), then the social graph, then messaging plumbing, then the
user-facing send/receive features, then the cross-cutting surface, and finally live proof.
Stories 1-3 and 15 are local-only and parallelizable with the external gates.

1. Story 1 (shell) before 2 (profile card lives in the Groups tab); 2 before 3 (keys are
   generated alongside the profile GUID).
2. Story 4 (contract v1 published) before 5 (relay deployed) — both are foodus-relay gates
   released by the owner ("server leads, app follows"); Story 4's transferred decisions,
   including relay endpoint authentication, resolve there.
3. Story 15 (relay URL setting) before any story that calls the relay (6 onward); its
   capability check completes once the Story 5 gate is released.
4. Story 5 before 6 and 7 (codes and friends need the live resolve/block API); 6 before 7.
5. Stories 5 and 7 before 8 (the pipeline needs a mailbox and stored friend keys); Story 8's
   plan additionally needs Story 4's contract to settle the unknown-version disposition.
6. Stories 7 and 8 before 9 (groups are built from friends and invite over the pipeline).
7. Stories 8 and 9 before 10 and 11; 11 before 12.
8. Story 13 stubs with 1, finalizes after 9-12 (needs all emitters).
9. Story 14 last — depends on all, including both phones configured via Story 15.

---

## Backlog Sources

- This milestone was synthesized directly from the 2026-07-27 dictation addendum, not pulled
  from the backlog. The backlog's Expected Inflows note ("Milestone 3 candidates stage here
  first") was superseded by the dictation arriving fully formed.
- Deferred multiplayer stretch items were pushed *to* the backlog instead — see below.

---

## Deferred / Follow-up Work

Parked deliberately during the 2026-07-27 dictation (Part C); staged as backlog stories, **not**
Milestone 3 scope:

- **N-person groups** (> 2 members) — including the all-pairs friendship rule and its known
  costs (quadratic pair requirements; server exposure of third-party friendship data — a privacy
  decision to be made consciously), per-member trust levels, live member-list updates, and the
  group-block notice wording/anonymity question. [backlog-1 Story 10](../backlog/backlog-1.md#story-10)
- **Safety-number key verification** (Signal-style, out-of-band) to close the accepted
  server-MITM hole in public-key distribution. [backlog-1 Story 11](../backlog/backlog-1.md#story-11)
- **Friend bios** in the expanded friend row. [backlog-1 Story 12](../backlog/backlog-1.md#story-12)
- **Push transport** (FCM or self-hosted alternative) if poll-on-wake latency ever grates.
  [backlog-1 Story 13](../backlog/backlog-1.md#story-13)

---

## Notes

- Keep this Story Index in sync with the [Milestones Index](../design.md#milestones-index) in Design.
- The open decisions embedded in Stories 9, 10, and 11 are **staged** here; per the source
  dictation's integration instructions, each must be carried into the owning story's
  implementation plan as an explicit decision point when that plan is written, and resolved
  (with the owner where marked) before the story is planned. Story 4's former open decisions
  transferred to the foodus-relay repo's first milestone (2026-07-27 relay seed dictation);
  the unknown-version envelope disposition question under Relay Contract Conformance is also
  owned there.
- The durable architecture decisions (relay model, E2E, no accounts, poll-on-wake, key-loss
  policy, storage map, and the 2026-07-27 relay repo split with its contract-ownership and
  deployment-ordering rules) are promoted into [design.md](../design.md); this document owns
  the app-side feature scope and story breakdown. Server-side scope lives in foodus-relay.
- If a story turns out to be milestone-sized, reclassify it into its own milestone rather than
  forcing it into this one.
