---
name: milestone-3-multiplayer-addendum
description: Tier-0 addendum defining Milestone 3 (mu) — profiles, friends, two-person groups, cross-diary logging, and the dumb encrypted relay server. Synthesize into milestone-3.md, design.md Milestones Index, and the backlog.
metadata:
  version: "1.0"
  owner: "Jarryd Adaens"
  repo: "FoodUs (fork of maksimowiczm/FoodYou)"
  date: "27 July 2026"
  source: "Owner dictation session, 27 July 2026"
---

# Milestone 3 Addendum: Multiplayer

> Tier-0 dictation synthesis. This document captures the full intent of the 27 July 2026
> dictation session defining Milestone 3. It supersedes the "TBD" placeholder in
> `context/milestones/milestone-3.md`.

## Integration instructions for the processing agent

1. Synthesize this addendum into `context/milestones/milestone-3.md` using
   `MILESTONE_TEMPLATE.md`: Intent, Why it matters, Outcome / Definition of Done, Story Index,
   Stories, Interdependency Order.
2. Update the Milestones Index row in `context/design.md`, and promote the durable
   architectural decisions in Part B (relay model, E2E encryption, no accounts, poll-on-wake,
   key-loss policy) into `design.md` — they amend the "island" principle and future agents must
   see them without reading this addendum.
3. File this document in `context/dictations-tier-0/` as the source dictation.
4. Create a stretch-goal entry (backlog or a reserved future milestone) for the deferred items
   in Part C. Do **not** schedule them in Milestone 3.
5. The proposed story list in Part D is ordered for implementation. Treat it as the starting
   Story Index; refine CER scoring and plans per the normal workflow.
6. Open decisions in Part E must be resolved (with the owner where marked) before their owning
   story is planned — carry them into the relevant story's plan as explicit decision points.

---

## Intent

Milestone 3 makes FoodUs multi-user. Theme: **Multiplayer** — features for multiple people
using an app that is currently an island (fully local, no external services). The island is
broken in exactly one controlled place: a dumb, encrypted, store-and-forward relay server that
ferries sealed packets between devices. Everything else stays local-first.

The marquee feature — the owner's wife's idea, seen in no other fitness tracker — is
**cross-diary logging**: a high-trust pair (the owner and his wife) can write food entries
directly into each other's diaries. Cook tacos together, one person logs it once, it lands in
both diaries.

Distribution via Obtainium (Milestone 1, Story 11) means the app can be handed to friends
beyond the household, so the social structures are designed to generalize — but Milestone 3
deliberately **caps groups at two people** to eliminate an entire class of privacy problems
(see Part C).

## Constitutional constraints (unchanged, restated for emphasis)

- **No cloud migration.** Each person's database stays on their device, backed up and restored
  by them. The server is a transmission vector between databases, nothing more.
- **No accounts, no login, no server-side backup, no server-side diary data in the clear.**
  You just start using the app and it is all still your data.
- **Fork philosophy applies:** additive overlays, minimal merge-conflict surface with upstream.
- **Respect the user:** no nagging, no dark patterns.

---

# Part A — Feature specification

## A1. Tabbed UI shell

The single-screen app becomes tabbed, with a bottom navigation row in the style of
Lose It / MyFitnessPal (sitting above the Android system back/home/recents bar).

Tabs, left to right:

1. **Groups** — new (A2–A6).
2. **Log** — the entire existing app (day list, diary, foods). The current UI becomes this tab
   unchanged.
3. **Notifications** — new (A9). Rightmost tab.

## A2. Groups tab layout

A flat vertical stack of cards — no submenus. Top to bottom:

1. **My Profile** card (topmost — profiles exist *for* groups, so the profile lives here).
2. **Friends** card — "Access Friends List", opens the friends list (A4).
3. **Join Group** card — "Would you like to join a group?" with a big green **Join Group**
   button. (Incoming invites also surface as new-group cards; see A6.)
4. **Create Group** card. After a group is created, the group becomes its own card and the
   Create Group control reappears below, so a second group can always be created.
5. One card per group the user belongs to.

## A3. Profile

Deliberately minimal — the minimum needed to establish connection to other people.

Fields:

- **ID** — a randomly generated GUID, hidden from casual view. Created on demand via a
  "create" button if no profile exists. This is the core identity and the primary key for
  everything social. It never changes.
- **Username** — free text, user's choice. Purely cosmetic metadata: collisions are allowed
  and not policed (multiple Emilys are fine), renameable at any time. Explicitly *not* the
  unique identifier (departure from classic Xbox Live, which is otherwise the North Star for
  the friends design).
- **Friend code** — a revocable connection handle (A4).
- **Created date** and **last-edited date** — for identity work and sorting. (Add the same two
  dates to groups; see A5.)
- **Key pair** — generated alongside the GUID (Part B4). Public key stored with the profile;
  private key in the Android Keystore, never in the database.

Storage: profile lives in the Room database so it is included in backup/restore. The private
key deliberately is not (Part B5).

## A4. Friends system

North Star: Xbox Live's friends model (as the foundation of modern friends systems), minus
username-as-primary-key.

**Friends list.** Reached from the Groups tab card. Shows all friends. A plus button opens
"add friend".

**Adding a friend — friend codes.** Every profile has a friend code: 12 characters in three
dash-separated blocks of four (letters and numbers, product-key style, e.g. `A7F3-9KQ2-XM41`).
To connect: get your code from your profile, send it to someone out-of-band; they tap add,
enter the code. The app queries the remote service; if found, the API returns the person's
GUID, current username, and public key, which are stored locally. That stored record is what
enables all future connection to that person through the relay.

**Friend-code revocation.** The code is a disposable layer over the permanent GUID. Refreshing
(regenerating) your code:

- does **not** break any existing friendships;
- kills every old copy of the code — anyone holding it gets nothing.

To cut off a specific person: remove them from your friends list (breaking the connection both
ways), then regenerate the code so any stored copy is dead.

**Friend row actions.** Tapping a friend expands the row to show more data plus actions:

- **Delete** — remove the friend.
- **Delete and block** — remove and add to the permanent **blocked list** (a separate stored
  list). A blocked person can never reconnect: even with a fresh friend code, their attempts
  return **"user not found"** — indistinguishable from a nonexistent user. This keeps specific
  people out even inside a malicious circle of friends.

Explicit non-goals for the expanded row: no passing on another friend's code, no bios (if bios
ever happen, the expanded row is where they'd live — but not now).

## A5. Groups

**Data model.** A group has: its own GUID; name; optional description; trust level; created
date; last-edited date; a member collection of profile IDs with per-member state.

**Two-member cap (Milestone 3 rule).** A group holds exactly the creator plus one friend. This
is a deliberate scope cut (Part C) that eliminates: the all-pairs friendship rule, third-party
friendship lookups (a privacy hole), per-member trust levels, live member-list updates, and
"anyone can add friends" mechanics. With two members, group-level trust *is* per-person trust.

**Creation flow** (from the Create Group card), a five-step form:

1. Group name — mandatory.
2. Group description — optional.
3. Trust level — mandatory (A6).
4. Add friend — picker over the full friends list with a search box (searching usernames);
   pick exactly one.
5. **Create Group** button at the bottom. A Cancel button sits alongside, guarded by an
   "are you sure" dialogue.

The creator is the group's first member. Ownership is deliberately loose — members are equal.
Example from dictation: the owner creates "Adaens" (the shared surname), trust **Full**,
containing himself and his wife.

**Invitation lifecycle.** Adding the friend puts them in **invited** state, rendered grey. On
their device: a notification fires and the group appears as a new, visually distinct card in
their Groups tab, showing the members. Two buttons at the bottom: **Accept** (green) and
**Reject** (red).

- **Accept** → member becomes active.
- **Reject** → dialogue with three options: **Reject**, **Block**, **Cancel**.
- **Block** → the group goes on a group blacklist: no notification from it ever again, and
  anyone attempting to add the blocker to that group is notified the person can't be added —
  dictated wording: *"please respect Emily's decision to stay out of this group"*. (Wording is
  an open decision — it reveals the blocker's username to the adder; see Part E.)

**Membership actions after accepting.** The group card expands; at its base: **Leave** and
**Leave and Block**. Leaving just makes you not present — no more notifications about entries.
With the two-member cap, a departure leaves the group dead/dormant with one member.

## A6. Trust levels

Set once per group at creation. Three values:

- **Full** — members can write directly into each other's diaries and edit each other's meal
  plans. Incoming entries are added automatically; the recipient gets an "entry added"
  notification. This is the owner/wife configuration.
- **Suggest** — members can only suggest. Incoming entries stack up in a suggestion queue
  tied to the group; the recipient works through them, accepting or rejecting each one.
  Accepting adds the entry to the diary.
- **None** — presence only. No messages sent, no food added. No defined use case yet; retained
  on engineering instinct as the zero value of the enum. (Open decision: whether None-trust
  groups appear in the Save-to-Group picker at all — see Part E.)

Per-member trust levels were explicitly considered and deferred as "too complicated too soon"
(moot under the two-member cap anyway; parked in Part C).

## A7. Save to Group (sending food)

On the food-entry screen (the screen with date chips, meal chips, quantity, nutrition summary,
and the **Save** button):

- **Save** stays on the right as-is.
- A new big **Save to Group** button appears on the left.
- Above it, a card lists the user's groups with checkboxes. Check the groups to send to.

Tapping Save to Group transmits the entry via the relay to every member of every checked group
(with two-person groups, that is one recipient per group; multiple checked groups fan out to
multiple people). Delivery behavior per group follows its trust level (A6).

**The packet.** A full, self-contained JSON envelope: complete nutritional information, name,
and everything needed to construct the entry on the recipient's device — because the recipient
may not share the sender's data sources (the food may be unique to the sender). Alongside the
food data, the packet carries the **entry timestamp** and the **meal name**.

## A8. Meal matching on receipt (three-tier fallback)

When an entry is accepted into a recipient's diary (auto under Full, or on accept under
Suggest), the meal slot resolves as follows:

1. **Name match.** If the recipient has a meal whose name matches the sender's meal name
   (e.g. both have "Lunch"), insert there.
2. **Time match.** Otherwise, check the entry's timestamp against the recipient's own meal
   time windows (the same configured meal slots validated by the existing `validate-meals`
   CI) and insert into the slot covering that time. Example from dictation: sender's "Lunch"
   at midday, recipient has no Lunch but their Breakfast runs 06:00–13:00 → it lands in
   Breakfast.
3. **First-meal fallback.** If no meal covers the time at all (recipient misconfigured), shove
   it into the first meal in the recipient's list. Tough luck; it lands somewhere.

**Invariant: a delivered entry always lands. It is never dropped.**

## A9. Notification Center (third tab)

A rightmost tab collecting every event the system generates: meal added, meal failed to add,
friend added you, you joined a group, a group changed its name, and so on — the single place
the app communicates more to the user.

- Notifications are stored persistently.
- Dismissed/read notifications are hidden by default; a control exposes the historical list.
- Under the poll-on-wake model (Part B3) there is no push transport: "notifications" are
  materialized locally from messages drained at poll time, plus local events.

---

# Part B — Server & security architecture

## B1. The relay ("post office" model)

The server is a **store-and-forward message relay** — deliberately dumb. Mental model: a post
office. A device drops off a sealed envelope addressed to a GUID; the office parks it in that
GUID's pigeonhole; the recipient's device later asks "anything for me?" and collects it. The
office never opens the envelope and discards it after delivery.

Every user action that crosses devices — send food entry, group invite, accept, reject,
membership change, key re-announcement — is a queued message. This is closer to a mailbox/push
messaging server than live sync; the SMTP intuition from dictation is the right shape.

**What the server holds:** GUIDs, public keys, friend codes (for lookup), block relationships
(for enforcement), and per-GUID queues of sealed ciphertext envelopes. Nothing else. No
accounts, no login, no passwords, no diary data in the clear. A breach yields ciphertext,
usernames, and GUIDs.

**Minimum API surface:**

- Register / update profile (GUID, public key, current username, friend code).
- Resolve friend code → { GUID, username, public key } — with block enforcement: a blocked
  requester gets "user not found".
- Regenerate friend code.
- Push message (sealed envelope addressed to a GUID).
- Poll / drain mailbox for a GUID.
- Record block relationships (person blocks and group blocks) as needed for enforcement.

**Retention:** undelivered messages are swept after **30 days**. Month-old diet data is
worthless; this caps storage and liability at near zero.

The intelligence stays on-device; the server stack, hosting, and exact wire contract are a
dedicated research story (Part D, Story 4).

## B2. What deliberately does not exist

- No server accounts, no login system, no sessions in the account sense.
- No server-side backup of anything user-precious.
- No cloud migration of the diary. Local Room database remains the source of truth.
- No web/companion clients in scope.

## B3. Transport: poll on app wake (no push)

Decision: **polling only**, on app open/wake.

- On wake, the app shows a brief "getting messages" step, drains the mailbox, decrypts,
  routes (Full → insert + notify; Suggest → queue), and populates the Notification Center.
- No background polling service — no battery drain.
- No Firebase Cloud Messaging — keeps Google entirely out of the privacy story.
- The latency cost (you see the tacos next time you open the app) is acceptable for
  non-time-critical diet data.

## B4. End-to-end encryption

- Every profile generates an asymmetric **key pair** at creation, alongside the GUID.
- **Key distribution rides the friend-code lookup for free:** the resolve response already
  returns GUID + username; it also returns the **public key**, stored locally next to the
  friend. Becoming friends *is* the key exchange — no extra user step.
- Senders encrypt each packet with the recipient's public key before it leaves the device.
  The server only ever stores ciphertext it cannot read. It knows "a message exists for this
  GUID" and nothing more.
- **Known, accepted weakness:** the server hands out public keys, so a malicious server could
  substitute its own (man-in-the-middle). For a self-hosted household app where the server
  operator is the owner, this is theoretical. The established fix — Signal/WhatsApp-style
  safety numbers verified out-of-band — is parked as a stretch item (Part C).

## B5. Private key storage: Android Keystore

- The private key lives in the **Android Keystore** — hardware-backed on target devices
  (Galaxy S22 Ultra class): generated and held in the secure element, never entering app
  memory, never touching the Room database or DataStore, never in any file or backup. The app
  asks the Keystore to decrypt blobs; the key itself never leaves the vault.
- Consequence: the database backup remains fully portable; the private key deliberately is
  not.

## B6. Key-loss / device-death policy

Decision: **keys live and die with the device.** Chosen over (a) passphrase-wrapped exportable
keys — a bad trade that weakens hardware isolation to protect disposable data — and (b)
Signal-style secure-value-recovery infrastructure — wildly over-engineered for a household
food app.

On a lost/replaced device:

- The user sets up fresh (new GUID + new key pair), or restores their database backup to keep
  their GUID and social graph, generating a fresh key pair.
- The device announces the new public key to the server.
- Friends' devices pick up the changed key on their next poll and update their local copy,
  re-establishing sealed messaging.
- Any messages in flight to the old key are unopenable and lost — acceptable by design: they
  are throwaway diet packets under the 30-day sweep, nothing precious is ever bound to the
  key, and the audience is friends and family who can simply be told to resend. Principle
  over convenience: the security model is not weakened to protect data already judged
  disposable.

## B7. Storage map (who holds what)

| Location | Holds |
| --- | --- |
| Device — Room DB (in backup) | Diary; profile (GUID, username, friend code, dates, own public key); friends (GUIDs, usernames, public keys); blocked lists; groups + membership states; notification history; suggestion queue |
| Device — Android Keystore (never in backup) | Private key |
| Server | GUIDs, public keys, friend codes, block relationships, sealed undelivered envelopes (≤ 30 days) |
| In flight | Ciphertext envelopes only |

---

# Part C — Deferred / stretch-goal milestone

Parked deliberately. Create a stretch entry; none of this is Milestone 3 scope.

1. **N-person groups (> 2 members)** — including invite dynamics, live member-list updates
   ("group swells from 2 to 8"), and member add/remove flows.
2. **All-pairs friendship rule** — every member must already be friends with a new addition
   ("cannot add Emily, because Emily and Jacob are not friends with each other"). Known costs
   recorded at dictation time: quadratic friendship requirements (4 people → 6 pairs, 8 → 28),
   and the server exposing third-party friendship data — a deliberate privacy decision that
   must be made consciously, which is precisely why it was deferred.
3. **Per-member trust levels** within a group.
4. **Group-block message wording** — the "please respect Emily's decision" notice reveals the
   blocker's identity; resolve wording/anonymity before N-person groups.
5. **Safety-number key verification** (out-of-band public-key confirmation, Signal-style) to
   close the server-MITM hole.
6. **Friend bios** in the expanded friend row.
7. **Push transport** (FCM or self-hosted alternative) if poll-on-wake latency ever grates.

---

# Part D — Proposed stories, in implementation order

Ordering rationale: local-only foundations first (1–3), then the server contract and build
(4–5) since everything social depends on it, then the social graph (6–7), then messaging
plumbing (8), then the user-facing send/receive features (9–12), then the cross-cutting
surface (13), and finally live proof (14). Stories 1–3 are parallelizable with 4.

| # | Story | Type | Depends on |
| --- | --- | --- | --- |
| 1 | Tabbed UI shell (Groups / Log / Notifications) | Feature | — |
| 2 | Profile: GUID, username, dates, Groups-tab card, backup inclusion | Feature | 1 |
| 3 | Crypto identity: key-pair generation, Android Keystore integration, public key on profile | Feature | 2 |
| 4 | Relay server architecture spike: stack, hosting, wire contract, friend-code minting authority | Research | — |
| 5 | Relay server build: registration, friend-code resolve, mailbox push/poll, block enforcement, 30-day sweep | Feature | 3, 4 |
| 6 | Friend codes: display in profile, regenerate/revoke flow | Feature | 5 |
| 7 | Friends list: add-by-code (lookup + local store incl. public key), expandable rows, delete, delete-and-block, blocked list | Feature | 6 |
| 8 | Message envelope & E2E pipeline: packet schema (full self-contained food JSON + timestamp + meal name), encrypt/send, poll-on-wake drain ("getting messages"), decrypt/route | Feature | 5, 7 |
| 9 | Groups: data model, five-step create form, cards, two-member cap, trust levels, invite/accept/reject/block lifecycle, leave / leave-and-block | Feature | 7, 8 |
| 10 | Save to Group: entry-screen button + group-checkbox card, fan-out send | Feature | 8, 9 |
| 11 | Receive into diary: Full-trust auto-insert + three-tier meal matching (name → time window → first meal); never-dropped invariant | Feature | 8, 9 |
| 12 | Suggestion queue: Suggest-trust stacking, per-item accept/reject review UI | Feature | 11 |
| 13 | Notification Center tab: persistent event store, dismissed-history toggle, wired to all emitters | Feature | 1, 8 (stub earlier; finalize after 9–12) |
| 14 | Household proof: real two-phone end-to-end (owner ↔ wife) — friend add, "Adaens" group, Full-trust taco round-trip, suggest flow, block flows, key-loss re-key drill | Research | all |

Notes for synthesis:

- Story 4 must settle Part E's server-side open decisions before Story 5 is planned.
- Story 13 can land a stub with Story 1 (empty tab) and be completed late; sequencing above
  reflects its finalization point.
- Story 14 doubles as the milestone's Definition-of-Done evidence.

**Suggested Definition of Done for the milestone:** two household phones, each with a profile,
connected as friends via friend code, in a Full-trust two-person group; a food entry saved to
the group on one phone lands correctly slotted in the other's diary via the encrypted relay;
Suggest-trust and block flows proven; a simulated device loss recovers via re-key without
weakening the model; the server holds nothing readable and sweeps at 30 days.

---

# Part E — Open decisions (resolve before/while planning the owning story)

| # | Decision | Owning story | Notes |
| --- | --- | --- | --- |
| 1 | Friend-code minting authority: server-assigned (uniqueness guaranteed) vs client-generated + registered (collision handling needed) | 4 | Not specified in dictation; owner input wanted |
| 2 | Friend-code alphabet: exact charset (e.g. exclude 0/O, 1/I ambiguity), case rules | 4 | Dictation fixed shape (4-4-4, dashes, letters+numbers) but not charset |
| 3 | Do None-trust groups appear in the Save-to-Group checkbox list (hidden vs greyed out)? | 10 | Flagged during dictation; owner input wanted |
| 4 | Group-block notice wording — current dictated text names the blocker to the adder | 9 | Owner acknowledged the flag; decide before shipping the notice |
| 5 | Server stack / hosting / language and exact wire contract | 4 | Owner asked for advisory input; relay + poll + E2E constraints fixed, implementation open |
| 6 | Whether Suggest-queue accept applies the same three-tier meal matching at accept time using the original timestamp (assumed yes) | 11/12 | Confirm assumption |
| 7 | Date/meal carry-over semantics: entry lands on the sender's entry date in the recipient's diary (assumed yes) | 11 | Flagged during dictation; confirm |

---

## Source

Owner dictation session, 27 July 2026, conducted conversationally with iterative confirmation.
Group cap at two members and the deferral list (Part C) were decided live during the session in
response to the all-pairs privacy analysis. Key-loss policy, poll-on-wake, and E2E design were
settled during the server-architecture advisory portion of the same session.
