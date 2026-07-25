# Plan: App Update Mechanism

## Metadata

- Task Type: `STORY`
- Status: `Draft`
- Owner: Jarryd Adaens
- Last Updated: 25 July 2026

## Linked Context

- Milestone: [context/milestones/milestone-1.md](../../../milestones/milestone-1.md)
- Story: [Story 11: App update mechanism](../../../milestones/milestone-1.md#story-11)
- Backlog source: none — story mapped directly from the 2026-07-24 initial project seed
- Related Plans:
  - [Story 15: Establish fork CI/CD pipeline](../story-15-cicd-pipeline/plan.md) — Story 15
    provides the CI build artifact and the fork's GitHub release channel; **this story (Story 11)
    owns the signing and distribution decision** and consumes Story 15's artifacts (the workflow
    scope moved there from Story 13 on 2026-07-25).
  - [Story 13: Own project infrastructure](../own-project-infrastructure/plan.md) — now scoped to
    issue templates, `metadata/`, and README badge verification.
- External Tooling: `commit-log` skill for commits; `rails-grade-cer` if formal grading is wanted

## CER

- Complexity: 3
- Effort: 4
- Risk: 6
- Notes: Inline estimate by the planning agent, not a formal `rails-grade-cer` grade. Complexity
  is low — the moving parts (keystore, signing step, versionCode, install path) are individually
  simple. Effort is moderate — mostly owner-executed procedure, a small signing script/doc, and
  two-device verification. Risk is elevated because the failure mode is exactly what the story
  exists to prevent: a signing-key or applicationId inconsistency silently forces an uninstall and
  wipes the Room database on a daily-driver phone.

## Objective

Establish a repeatable way to build an APK of the fork at will and install it as an **in-place
update** on both household phones — the owner's Galaxy S22 Ultra and his wife's phone — without a
public store release and without ever losing local diary data. The deliverables are: a decided and
documented signing scheme (dedicated fork release keystore, never committed), a decided
distribution channel, a documented versionCode bump procedure, and a verified install→update→data-
survives cycle on a real device.

## Scope

### In Scope

- The signing decision: create and adopt a dedicated fork release keystore; define its custody
  (where it lives, who holds passwords, backup) — the keystore and its passwords are **never**
  committed, logged, or copied into context files (laws.md §2).
- The distribution-channel decision: compare direct APK distribution (GitHub Releases +
  Obtainium/manual install) against private Google Play publishing; recommend and adopt one.
- A documented, repeatable local signing procedure (zipalign + apksigner, mirroring upstream's
  `release-apk.yml` post-build signing pattern) so a distributable APK can be produced without CI.
- The versionCode bump procedure in `gradle/libs.versions.toml`, including how it interacts with
  upstream merges.
- The one-time migration from today's debug-signed install to the release-signed install on the
  owner's phone, sequenced so no real data is lost.
- First install on the wife's phone (release-signed from day one).
- A data-preservation verification pass: prove an update installs over the previous build and the
  Room database survives.
- A wiki page recording the whole procedure so any future session can ship an update.

### Out Of Scope

- The CI build workflow and GitHub release channel themselves — Story 15
  ([plan](../story-15-cicd-pipeline/plan.md)) owns creating them; this story only decides what
  key signs the artifact they produce and how phones consume it.
- Public store release (F-Droid, open Google Play production track).
- App identity changes (applicationId rename, ACME branding) — Milestone 2. This story's
  data-preservation reasoning is one input to how that rename must be handled later.
- The AI-key private-build channel — Milestone 2; flagged below as a forward constraint only.
- Any production code changes. No in-app update checker or self-updater is built.

## Non-Goals

- No automation beyond what is needed to ship an update to two phones. Two devices do not justify
  an MDM, a private F-Droid repository server, or an in-app updater.
- No attempt to make `assembleRelease` sign inside Gradle. Upstream deliberately builds an
  unsigned release APK and signs afterwards with `apksigner`; copying that pattern keeps
  `app/build.gradle.kts` byte-identical to upstream (mergeability, fork philosophy).

## Current Understanding

### Why updates preserve data (or don't) — the concrete rules

Android performs an in-place update, preserving `/data/data/<applicationId>/` (the Room database
`FoodYou.db`, DataStore preferences, everything), **only** when all three hold:

1. **Same `applicationId`** — `com.maksimowiczm.foodyou`
   (`app/build.gradle.kts` `defaultConfig`). The `preview` build type appends `.preview` and is
   therefore a *different app* with its own data — never use it as the household build type.
2. **Same signing certificate** — the new APK must be signed by the same key as the installed one.
   A mismatch makes the package manager refuse the install; the only way forward is uninstall,
   which deletes the app's data.
3. **`versionCode` not lower than installed** — Android refuses downgrades. Each shipped build
   needs `android-versionCode` in `gradle/libs.versions.toml` (currently `123`) to be ≥ the
   installed one; in practice, bump it every distributed build.

Room schema migrations are upstream's concern and already handled in the app; on an in-place
update the database is migrated, not recreated.

### Why the current state is fragile

- The owner's phone currently runs a **debug-signed** build (Story 1, deployed via Android
  Studio/adb — see design.md Configuration). The debug keystore lives at
  `~/.android/debug.keystore` on one machine and is machine-generated: rebuilds from any other
  machine, a reinstalled OS, or a wiped `.android` folder produce a different certificate →
  forced uninstall → data loss. A daily driver cannot rest on it.
- The `release` build type has **no signing config** in `app/build.gradle.kts`;
  `assembleRelease` outputs `app-release-unsigned.apk`. Upstream's `release-apk.yml` signs it
  afterwards with `zipalign` + `apksigner` using keystore secrets — the exact pattern this story
  adopts for the fork.
- The wife's phone has **no install yet** (Story 1 covered the owner's phone only) — it can start
  clean on the release key with no migration.

### Key files and facts

- `app/build.gradle.kts` — applicationId, build types (`release` minified/unsigned, `devRelease`
  debug-signed, `miniDevRelease`, `preview` with `.preview` suffix). **Expected to remain
  unchanged** by this story.
- `gradle/libs.versions.toml` — `version-name = "3.4.9"`, `android-versionCode = "123"`. The
  versionCode line is the one upstream file this story's procedure repeatedly edits.
- `.github/workflows/release-apk.yml` — upstream's sign-after-build reference implementation
  (secrets `KEYSTORE` base64, `KEY_ALIAS`, `KEYSTORE_PASSWORD`); Story 15 reworks it for the fork
  using the keystore this story creates.
- `context/wiki/` — destination for the update-procedure page.
- Existing behaviors to preserve: local debug build/deploy flow for development; upstream files
  stay mergeable.
- Constraints: agents never push or pull; every remote or on-device step is owner-executed. The
  keystore never enters the repository, context files, or logs.

### Distribution options compared

| Option | How it works | Pros | Cons |
| --- | --- | --- | --- |
| **A. GitHub Releases + Obtainium** (recommended) | Story 15's release workflow publishes the signed APK to the fork's GitHub Releases; both phones run [Obtainium](https://github.com/ImranR98/Obtainium) pointed at `JarrydAdaens/FoodYou`, which notifies and installs updates | Near-store UX for two phones; no cloud account, no fees, no review; builds on Story 15's channel; wife's phone updates without the owner's laptop | Requires installing one extra app per phone; releases on a public repo are publicly downloadable (fine for unmodified Milestone 1 builds; **not** acceptable once Milestone 2 bakes the AI key in — see risks) |
| **B. Manual sideload** (fallback / always available) | Signed APK reaches the phone by browser download from the GitHub Release, `adb install -r`, or file share; user taps to install | Zero new apps; works offline; the baseline every other option degrades to | Manual per phone per update; wife's phone updates depend on someone doing it |
| **C. Private Google Play (internal testing track)** | $25 developer account; upload AABs to an internal track limited to the two household accounts | Real store update UX; Play handles delivery | Heaviest: account cost and identity verification, AAB + Play App Signing (key custody moves to Google), review/processing latency per build, Play policy surface for a GPL fork, and new-personal-account testing requirements add friction. Overkill for two phones |

**Recommendation:** Option A, with Option B as the documented fallback. The milestone allows Play
"if cleanest" — it is not cleanest here; it is the most infrastructure for the least gain at
two devices.

### The one-time signature migration

Switching the owner's phone from the debug cert to the release cert requires exactly one
uninstall/reinstall. The cheap, safe window is **now — before Story 10's data import and
Story 12's daily use begin**, while the installed app contains nothing worth keeping. This is why
the milestone's Interdependency Order puts Story 11 before customized builds ship: sequencing the
key switch first makes the only unavoidable data-destroying step cost-free. The wife's phone never
migrates — its first install is release-signed.

## Questions / Unknowns

- Q: [STORY 11] Does the owner accept Option A (GitHub Releases + Obtainium on both phones), or
  prefer manual sideload only (B), or does he specifically want the private Play route (C)?
  Impact: Determines whether Obtainium setup is part of the install steps and whether Story 15's
  release channel is the actual delivery path or just an archive.
  Assumption: Option A, with B as fallback.
  Status: OPEN
- Q: [STORY 11] Where should the release keystore and its passwords live (custody), and where is
  its backup? Candidates: the owner's password manager (passwords + base64 keystore attachment)
  plus an offline copy.
  Impact: Losing this keystore permanently breaks in-place updates for both phones — the failure
  is unrecoverable by design. Custody must be decided before the key signs anything.
  Assumption: Password manager holds passwords and a copy of the keystore file; a second copy
  lives outside the dev machine. Never in the repo or GitHub outside Actions secrets.
  Status: OPEN
- Q: [STORY 11] Which build type ships to the household: `release` (R8-minified) or `devRelease`
  re-signed with the release key?
  Impact: Minified is what upstream ships and is smaller/faster; but any R8 issue in the fork
  would hit the daily drivers. Signing `release` is the straightforward path since the fork is
  currently unmodified.
  Assumption: `release`, signed post-build — identical to upstream's shipping configuration.
  Status: OPEN
- Q: [STORY 11] versionCode scheme going forward: simple `max(upstream, installed) + 1` bump per
  shipped build, or reserve headroom (e.g. jump to `1230`) so upstream merges never collide?
  Impact: Affects every future release and the merge-conflict story on upstream pulls; also feeds
  Milestone 2's fork-versioning story (fork versionName 1.0 layered on upstream).
  Assumption: Simple bump: before each shipped build, set `android-versionCode` to at least
  `installed + 1`; on upstream merges take `max(upstream, ours) + 1` if shipping.
  Status: OPEN
- Q: [STORY 11] Is the wife's phone's Android version ≥ minSdk 28, and is she comfortable with
  the one-time "install unknown apps" permission grant for Obtainium/browser?
  Impact: A device below minSdk 28 or a hard objection to sideloading would force Option C.
  Assumption: Yes on both — modern household phone.
  Status: OPEN
- Q: [STORY 11] Forward constraint only (decision belongs to Milestone 2): once the AI key is
  baked into private builds, public GitHub Releases can no longer carry the shipped APK. Which
  private channel replaces it (private repo + token in Obtainium, direct sideload, other)?
  Impact: None for this story's Milestone 1 builds (unmodified, no secrets); recorded so the
  Milestone 2 AI story inherits it explicitly rather than discovering it after a leak.
  Assumption: Milestone 1 ships public-release artifacts; the channel is revisited before any
  key-bearing build exists.
  Status: OPEN

## Execution Steps

1. Resolve the open questions with the owner (one short exchange).
   - Why: Channel, custody, build type, and versionCode answers shape every step below.
   - Edits: none.
   - Dependencies: blocks steps 2–8.

2. Owner creates the fork's release keystore (owner-executed, never committed).
   - Why: A durable, machine-independent signing identity is the foundation of data-safe updates.
   - Edits: none in the repo. The agent documents the command in the wiki page (step 3); the owner
     runs it and stores the outputs per the custody decision. Reference command:
     `keytool -genkeypair -v -keystore foodyou-fork.jks -alias foodyou -keyalg RSA -keysize 4096 -validity 10000`.
     Verify `.gitignore` covers `*.jks` / `*.keystore` and add the patterns if not (small,
     fork-only addition).
   - Dependencies: step 1 (custody answer).

3. Write the update-procedure wiki page and local signing script.
   - Why: The mechanism must be repeatable from documentation, not memory.
   - Edits: new `context/wiki/foodyou-app-update-procedure.md` covering: versionCode bump →
     `gradlew assembleRelease` → `zipalign` → `apksigner sign` (same commands as
     `.github/workflows/release-apk.yml`, keystore path/passwords supplied locally, never
     recorded) → `apksigner verify --print-certs` → distribute. Optionally a small
     `jarryd/scripts/sign-apk.ps1` wrapping the zipalign/apksigner calls, reading keystore path
     and passwords from environment/prompt only. Link the page from `context/wiki/home.md`.
   - Dependencies: steps 1–2.

4. Coordinate the CI signing hand-off with Story 15.
   - Why: Story 15's release workflow should sign with **this** keystore so CI-built and
     locally-built APKs are interchangeable on the phones.
   - Edits: none here beyond a note in both plans; the owner adds `KEYSTORE` (base64),
     `KEY_ALIAS`, `KEYSTORE_PASSWORD` as fork repository secrets; Story 15's workflow consumes
     them exactly as upstream's `release-apk.yml` does (plus `KEY_PASSWORD` if the key password
     differs — Story 15's open question, answered by how this story generates the keystore).
   - Dependencies: step 2; Story 15's workflow work proceeds independently.

5. Bump versionCode and produce the first release-signed APK locally.
   - Why: First real artifact; proves the local pipeline end-to-end without waiting on CI.
   - Edits: `gradle/libs.versions.toml` `android-versionCode` `123` → `124` (single-line edit;
     the one recurring upstream-file touch this story accepts — see Risk Mitigation).
   - Dependencies: steps 1–3.

6. One-time migration on the owner's phone, first install on the wife's phone (owner-executed).
   - Why: Moves both devices onto the durable signing identity while the debug install still
     holds no real data — the only moment this is free.
   - Edits: none. Owner: uninstall the debug-signed app from the S22 Ultra, install the signed
     APK on both phones (per the chosen channel: via a GitHub Release + Obtainium subscription,
     or direct sideload). If Option A: install and configure Obtainium on both phones now.
   - Dependencies: step 5; must complete **before** Story 10's CSV import and Story 12's daily
     use start.

7. Verify the update path preserves data (owner-executed, on the owner's phone).
   - Why: The story's hard constraint must be demonstrated, not assumed.
   - Edits: `android-versionCode` `124` → `125` for the test build.
   - Procedure: log a marker diary entry and a custom food in the installed app → build and sign
     versionCode 125 → install it **over** the existing app (no uninstall; via Obtainium update or
     `adb install -r`) → confirm the app opens with the marker entry and food intact and the
     About/version reflects the new build.
   - Dependencies: step 6.

8. Close out: record decisions and update context.
   - Why: Durable decisions must not strand in chat (design.md Context Maintenance).
   - Edits: fill this plan's Execution Log and Completion Review; mark Story 11's status in
     `context/milestones/milestone-1.md`; finalize the wiki page with the answered questions
     (channel, custody location *by name only* — e.g. "password manager", never contents).
   - Dependencies: steps 6–7.

## Validation

### Automated Checks

- `gradlew assembleRelease` succeeds locally and outputs `app-release-unsigned.apk`.
- `apksigner verify --print-certs signed.apk` shows the fork keystore's certificate (and, once
  Story 15's CI signs, the CI artifact shows the **same** certificate digest).
- `git status` confirms no keystore, `.jks`, or password material is tracked after the work.

### Manual Checks

1. Fresh install of the signed APK succeeds on both phones (owner-executed).
2. The step 7 update cycle: versionCode-bumped build installs over the previous one without an
   uninstall prompt; marker diary entry and custom food survive.
3. Negative check (knowledge, not performed on a phone with data): attempting to install a
   debug-signed build over the release-signed install is refused by the package manager —
   confirming the signature rule behaves as documented.
4. If Option A: Obtainium on both phones detects and installs a new GitHub Release.

### Acceptance Criteria

- A documented, repeatable procedure exists (wiki) by which a signed, installable APK is produced
  at will and delivered to both phones without a public store release.
- An in-place update was demonstrated on real hardware with local data verifiably preserved.
- Both phones run a build signed by the fork's release keystore; no device remains on debug
  signing.
- The keystore and its passwords exist only in the decided custody locations and GitHub Actions
  secrets — nowhere in the repository, context files, or logs.
- `app/build.gradle.kts` is unchanged; the only recurring repo edit per release is the
  `android-versionCode` line in `gradle/libs.versions.toml`.

## Risk Mitigation

- Risk: Keystore loss → permanent inability to update in place; both phones would eventually need
  uninstall/reinstall and lose data.
  Mitigation: Custody question answered before first signing; at least two copies outside the dev
  machine; `apksigner verify` cert digest recorded in the wiki so any candidate keystore can be
  confirmed as the right one.
- Risk: Keystore or passwords leak into the repo, context files, or CI logs (laws.md §2).
  Mitigation: Owner-executed key handling; `.gitignore` patterns; secrets only in GitHub Actions
  secrets; the wiki records commands with placeholder paths, never credentials; `git status`
  check in validation.
- Risk: The signature switch happens **after** real data exists, forcing a lossy uninstall.
  Mitigation: Step 6 is explicitly sequenced before Story 10 import and Story 12 daily use; the
  milestone's Interdependency Order already encodes this — do not reorder.
- Risk: A build ships with a stale versionCode and phones silently refuse the update.
  Mitigation: versionCode bump is the first line of the documented procedure; Obtainium surfaces
  the installed vs. available version mismatch.
- Risk: Recurring `libs.versions.toml` edits conflict on upstream pulls (fork mergeability).
  Mitigation: Single-line, well-understood conflict; resolution rule documented in the wiki
  (`max(upstream, ours) + 1` when shipping). Accepted as the minimal unavoidable upstream touch.
- Risk: Public GitHub Releases later carry a build with the baked-in AI key (Milestone 2).
  Mitigation: Forward constraint recorded in Questions; the Milestone 2 AI story must choose a
  private channel before any key-bearing build is produced. No key exists in Milestone 1 builds.
- Risk: R8-minified `release` build misbehaves on the daily drivers where debug builds did not.
  Mitigation: It is upstream's shipped configuration (3.4.9 releases are minified); step 7's
  hands-on verification exercises the shipped build before real data lands.

## Phase Split

Not needed — CER is under threshold; single-pass story with owner-executed device steps.

## Evidence / References

- Planning inputs: `context/milestones/milestone-1.md` (Story 11, Interdependency Order),
  `context/design.md` (two-device reality, fork philosophy, Configuration/signing note),
  `app/build.gradle.kts` (applicationId, build types, unsigned `release`),
  `gradle/libs.versions.toml` (`android-versionCode = "123"`, `version-name = "3.4.9"`,
  minSdk 28), `.github/workflows/release-apk.yml` (upstream's zipalign/apksigner sign-after-build
  pattern and secret names), `context/implementation-plans/milestone-1/story-15-cicd-pipeline/plan.md`
  (Story 15 boundary: CI artifact + release channel there; signing/distribution decision here).
- Unverified claims: the wife's phone's Android version and sideload consent (question open);
  Obtainium behavior on both devices is unverified until the owner installs it; Play internal-
  testing friction for new personal accounts was assessed from general knowledge, not re-verified
  against current Play Console policy — acceptable since Option C is not recommended.
