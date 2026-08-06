# Plan: Crypto Identity

## Metadata

- Task Type: `STORY`
- Status: `Draft`
- Owner: Jarryd Adaens
- Last Updated: 28 July 2026

## Linked Context

- Milestone: [context/milestones/milestone-3.md](../../../milestones/milestone-3.md)
- Story: [Story 6: Crypto identity](../../../milestones/milestone-3.md#story-6) `[STORY 3.6]`
- Design authority: `context/design.md` — "The Multiplayer Exception" (E2E model, private keys in the Android Keystore, keys-live-and-die-with-the-device policy) and "Security and Privacy"
- Constitutional constraint: `context/laws.md` §2 — Secrets and Data Boundaries. The private key never enters the Room database, DataStore, any file, any backup, or any log; it never leaves the Keystore vault.
- Relay Contract Conformance: [milestone-3.md §Relay Contract Conformance](../../../milestones/milestone-3.md#relay-contract-conformance-2026-07-27-relay-seed-dictation) — binds the registration seam described below.
- Related Plans:
  - Story 3.2 (Profile) — `context/implementation-plans/milestone-3/story-02-profile/plan.md` (planned in the same run): owns the profile Room entity that carries the public key; key generation hooks into its create-profile flow.
  - Story 3.7 (Relay URL setting) — supplies the user-entered relay endpoint the registration call needs.
  - Story 3.10 (Envelope & E2E pipeline) — **blocked, unplanned this run** (contract gate); future consumer of this story's decrypt capability.
  - Story 3.15 (Household proof) — exercises the re-key drill through the seam this story creates.
- Dependency note (app → server, per conformance rule 6): **blocked by foodus-relay: register/update-profile endpoints (shared slug `...-profile-registration`), contract v1, deployed.** Owner releases via the Story 4 gate. The local key work below is NOT blocked.
- External Tooling: none required.

## CER

- Complexity: 5
- Effort: 4
- Risk: 6
- Notes: Inline estimate. Effort is modest because upstream already ships the exact patterns needed: `common/crypto/` interfaces with Android Keystore actuals, Koin expect/actual definitions (`CryptoModule.kt` / `CryptoModule.android.kt`), and instrumented Keystore tests. Complexity comes from the key-type decision being contract-sensitive (the envelope encryption scheme lives in the unwritten foodus-relay wire contract) and from coordinating the profile-record schema with Story 3.2. Risk is elevated because this is the security foundation of the whole milestone: a wrong key-storage decision (key material leaking into Room/DataStore/backup) violates constitutional constraints, and a wrong key-type choice before contract v1 would force a re-key — mitigated below.

## Objective

At profile creation, generate an asymmetric key pair alongside the profile GUID: the private key lives exclusively in the Android Keystore (hardware-backed on target Galaxy S22 Ultra-class devices, never exported, never in memory beyond Keystore operations); the public key (X.509/DER) and its algorithm identifier are stored on the profile record in Room so they ride backup/restore. Expose a decrypt seam (the app hands sealed blobs to the Keystore) and a re-key seam (regenerate + update profile record) for later stories. Relay registration of the public key is a contract-gated follow-on, not deliverable now.

## Scope

### In Scope

- A fork-owned profile-messaging crypto interface in `common/crypto/` (working name `ProfileMessagingCrypto`): `publicKey: ByteArray` (X.509/DER), `algorithm: String`, `suspend fun decrypt(sealed: ByteArray): ByteArray`, `suspend fun regenerate(): ByteArray` (returns the new public key), `isSupported: Flow<Boolean>` (hardware-backed check), mirroring the existing `IdentityCrypto` / `MasterCrypto` shapes.
- Android actual under `androidMain .../common/infrastructure/crypto/` using a **new fork-owned Keystore alias** (e.g. `FOODUS_PROFILE_MESSAGING_KEY`), `PURPOSE_DECRYPT`, provisional key type per Q1 below.
- Koin expect/actual registration following the `CryptoModule.kt` / `CryptoModule.android.kt` pattern (additive: new `profileMessagingCryptoDefinition()` alongside the existing three definitions).
- Hook into Story 3.2's profile-creation use case: generate the pair when the GUID is minted; persist public key + algorithm on the profile record (column ownership coordinated with Story 3.2 — see Q4).
- Re-key seam: `regenerate()` replaces the Keystore entry and the caller updates the profile record. Re-announcement to friends/relay is Story 10/14 scope.
- Android instrumented tests mirroring `AndroidIdentityCryptoTest` / `AndroidMasterCryptoTest`: pair generated at creation, public key stable across process restarts, decrypt round-trip (encrypt with public key outside Keystore, decrypt via seam), regenerate yields a different key, private key not extractable.

### Out Of Scope

- The register/update-profile relay call and any wire-contract data classes — blocked by foodus-relay contract v1 (dependency note above). No speculative client stubs are written for it.
- Envelope schema, send/poll plumbing, message routing (Story 3.10, itself blocked this run).
- Key re-announcement messages to friends (Story 3.10 pipeline / Story 3.15 drill).
- iOS actuals (see Current Understanding — upstream ships Android-only crypto actuals today; Android-first posture holds).

## Non-Goals

- No changes to upstream's existing `IdentityCrypto` (sign-only, alias `FOODYOU_IDENTITY_KEY`) or `MasterCrypto` (alias `FOOD_YOU_MASTER_KEY`) — the fork adds a sibling, it does not repurpose upstream's auth keys (fork philosophy: additive, minimal merge surface).
- No user-facing key management UI; keys are invisible to the user by design.
- No safety-number/out-of-band key verification (backlog-1 Story 11).
- No multi-profile support — exactly one profile, one key pair per device.

## Current Understanding

All paths verified in the working tree on 28 July 2026.

- **Existing crypto seam (upstream):** `app/src/commonMain/kotlin/com/maksimowiczm/foodyou/common/crypto/` holds `IdentityCrypto.kt` (sign-only, hardware-backed, X.509 public key), `MasterCrypto.kt` (symmetric encrypt/decrypt), `SignatureVerifier.kt`. Android actuals in `app/src/androidMain/.../common/infrastructure/crypto/`: `AndroidIdentityCrypto.kt` (EC P-256, `SHA256withECDSA`, `PURPOSE_SIGN`, alias `FOODYOU_IDENTITY_KEY`, TEE/StrongBox check via `KeyInfo.securityLevel`), `AndroidMasterCrypto.kt` (AES-256-GCM, IV-prefixed ciphertext), `AndroidPublicCryptoFactory.kt` (`AndroidSignatureVerifier`).
- **DI pattern:** `common/infrastructure/crypto/CryptoModule.kt` declares `internal expect fun Module.*Definition()` per interface; `CryptoModule.android.kt` binds via `singleOf(...).bind<...>()`; `cryptoModule()` is composed in `app/src/commonMain/.../app/di/AppModule.kt:28`. The new definition follows this exactly.
- **iOS posture:** no iOS actuals for the crypto expect declarations were found under `app/src/` (only `commonMain`, `androidMain`, `androidInstrumentedTest` contain crypto files). Discovery step 1 confirms how iOS source sets satisfy (or exclude) these expects before adding a fourth definition to `cryptoModule()`.
- **Keystore usage precedent:** `initializeOrGetKey()` with a `Mutex`, lazy `publicKey`, `isSupported` distinguishing `SECURITY_LEVEL_TRUSTED_ENVIRONMENT` / `STRONGBOX` (SDK ≥ S) from `isInsideSecureHardware` — reuse this shape. Note: existing impls use `setUserAuthenticationRequired(false)`; keep that (decryption must work silently at poll time).
- **Encrypted-at-rest precedent:** `common/infrastructure/auth/SafeSessionRepository.kt` shows the house style for Keystore-backed crypto consumed through an interface.
- **Profile record:** does not exist yet — Story 3.2 (same run) plans the Room entity behind the Groups-tab My Profile card. Room schema lives in `app/src/commonMain/.../app/infrastructure/room/FoodYouDatabase.kt` (`version = VERSION`, `exportSchema = true`; migrations under `.../room/migration/`). The public key + algorithm land on that entity.
- **Tests precedent:** `app/src/androidInstrumentedTest/.../crypto/AndroidIdentityCryptoTest.kt` and `AndroidMasterCryptoTest.kt` — real-Keystore instrumented tests exist and pass on device; mirror them.
- **Constraints:** min SDK 28, target/compile 36 (`gradle/libs.versions.toml`); target hardware is Galaxy S22 Ultra class (API 31+), but the app must not crash on API 28 software-Keystore devices. Fork changes are additive; source namespace stays `com.maksimowiczm.foodyou`.
- **Behaviors to preserve:** upstream `IdentityCrypto`/`MasterCrypto` consumers (`SafeSessionRepository`, `OpenFoodFactsCredentialsRepositoryImpl`) are untouched; database backup/restore portability is unchanged (public key is portable data, private key deliberately is not).

## Questions / Unknowns

- Q: `[STORY 3.6]` Which asymmetric scheme does the envelope encryption use? The wire contract (foodus-relay, contract v1, **Not Started** as of 2026-07-28) owns interoperable crypto choices. Android Keystore realistically offers: (a) RSA-3072/4096 with OAEP, `PURPOSE_DECRYPT`, works from API 28, envelope = hybrid RSA-wrapped AES-GCM key; (b) EC key agreement (`PURPOSE_AGREE_KEY`, API 31+) with HKDF → AES-GCM, more modern but above min SDK 28 and needs a runtime capability gate.
  Impact: Determines the Keystore key type generated at profile creation and the `algorithm` string stored with the profile and later registered with the relay. Wrong guess forces a re-key.
  Assumption: Implement with **(a) RSA-OAEP hybrid-ready** as the provisional type (min-SDK-safe, one code path), explicitly marked replaceable: until Story 10 ships there is zero interop surface, so a pre-release re-key is free. The contract decision must be carried across by the owner before Story 10 planning; if the contract picks (b), `regenerate()` plus a migration of the profile's key columns absorbs the change.
  Status: OPEN
  Answer: —

- Q: `[STORY 3.6]` Does the relay's endpoint authentication (foodus-relay open decision: proof of GUID ownership, "likely device key-pair request signing") reuse this same key pair for signing, or a separate one? A single `PURPOSE_DECRYPT` key cannot also be a clean signing key; Keystore best practice is one purpose per key.
  Impact: If request signing is required, this story's interface may need a companion sign key (or upstream's existing sign-only `IdentityCrypto` pattern gets a fork-owned sibling), and the registration payload may need to carry two public keys.
  Assumption: Out of scope until the contract answers; the interface is kept narrow (decrypt + regenerate) so a signing capability can be added additively without reshaping this story's deliverable.
  Status: OPEN
  Answer: —

- Q: `[STORY 3.6]` Column ownership with Story 3.2: does the profile entity ship with `publicKey`/`keyAlgorithm` columns from day one (Story 3.2's migration), or does this story add them in a second migration?
  Impact: One Room migration vs two; ordering of the two stories' execution.
  Assumption: Story 3.2's entity includes the two columns as nullable from day one (cheapest; one migration), and this story populates them at creation time. To be reconciled between the two plans before execution.
  Status: OPEN
  Answer: —

- Q: `[STORY 3.6]` Policy when `isSupported` is false (no TEE/StrongBox — e.g. emulator or exotic API 28 device): proceed with software-backed Keystore, or refuse to create the multiplayer identity?
  Impact: Decides an error/warning path at profile creation and what Story 3.15 records as evidence.
  Assumption: Proceed with software-backed Keystore (the key still never leaves the Keystore API surface) and surface `isSupported` for Story 3.15's evidence; both household target devices are hardware-backed anyway.
  Status: OPEN
  Answer: —

## Execution Steps

1. Discovery: confirm iOS handling of the existing crypto expects
   - Why: `cryptoModule()` gains a fourth expect definition; the plan must not break the `iosArm64`/`iosSimulatorArm64` targets. No iOS actuals were found under `app/src/` — confirm where (or whether) iOS satisfies the current three before adding one.
   - Edits: none (read-only; findings noted in this plan's Execution Log).
   - Dependencies: none; do first.

2. Define `ProfileMessagingCrypto` interface in `app/src/commonMain/.../common/crypto/ProfileMessagingCrypto.kt`
   - Why: The seam every later story consumes (Story 10 decrypt, Story 15 re-key); keeps envelope crypto behind an interface so the Q1 contract decision swaps implementations, not callers.
   - Edits: new file — `isSupported: Flow<Boolean>`, `algorithm: String`, `publicKey: ByteArray`, `suspend fun decrypt(sealed: ByteArray): ByteArray`, `suspend fun regenerate(): ByteArray`; kdoc stating the constitutional storage rules and the provisional-scheme caveat.
   - Dependencies: step 1.

3. Android implementation `AndroidProfileMessagingCrypto.kt` in `androidMain .../common/infrastructure/crypto/`
   - Why: The actual Keystore work.
   - Edits: new file mirroring `AndroidIdentityCrypto`'s shape (mutex-guarded `initializeOrGetKey()`, lazy X.509 public key, `KeyInfo`-based `isSupported`): alias `FOODUS_PROFILE_MESSAGING_KEY`, provisional RSA-OAEP `PURPOSE_DECRYPT` per Q1 assumption, `setUserAuthenticationRequired(false)`; `regenerate()` deletes and recreates the alias atomically under the mutex. Key generation is lazy-on-first-use so "generated alongside the GUID" is enforced by the profile-creation use case, not by class init.
   - Dependencies: step 2.

4. Koin wiring
   - Why: Match the established DI pattern.
   - Edits: add `internal expect fun Module.profileMessagingCryptoDefinition(): KoinDefinition<out ProfileMessagingCrypto>` to `CryptoModule.kt`, the Android actual to `CryptoModule.android.kt`, the call inside `cryptoModule()`, and the iOS-side handling determined by step 1.
   - Dependencies: steps 1–3.

5. Profile-creation integration (coordinated with Story 3.2)
   - Why: The story's headline behavior — pair generated alongside the GUID; public key + algorithm persisted on the profile record so they ride backup/restore.
   - Edits: in Story 3.2's create-profile use case (exact file per that plan): after GUID mint, read `profileMessagingCrypto.publicKey`/`algorithm` and store both on the profile entity. Re-key path: a small `RekeyProfileUseCase` (fork-owned, in the profile slice) calling `regenerate()` and updating the record — the seam Story 3.15's drill and the future relay re-announcement reuse.
   - Dependencies: Story 3.2's entity + use case exist; Q3 resolved.

6. Instrumented tests `AndroidProfileMessagingCryptoTest.kt`
   - Why: Keystore behavior is untestable in unit tests; the repo's precedent is instrumented tests, and this is critical security logic (laws §5 Testability).
   - Edits: new file in `androidInstrumentedTest .../crypto/`: public key stable across instantiations; decrypt round-trip (seal with the exported public key using the matching JCA transformation, decrypt through the seam); `regenerate()` changes the public key and old ciphertext no longer decrypts; private key absent from any exported form.
   - Dependencies: step 3.

7. Verification sweep for constitutional storage rules
   - Why: laws §2 — prove the private key cannot reach Room/DataStore/files/logs.
   - Edits: none; grep audit that no new code references private-key material outside the Keystore API, and that no new Room/DataStore field stores anything but the public key + algorithm string. Record in Evidence.
   - Dependencies: steps 3–6.

## Validation

### Automated Checks

- `.\gradlew.bat :app:compileDebugKotlinAndroid` (or the toolchain-memory equivalent) — both new expect/actual pairs compile for Android; iOS targets still configure.
- Instrumented: `.\gradlew.bat :app:connectedDebugAndroidTest --tests "*AndroidProfileMessagingCryptoTest*"` on a device/emulator (per the repo's Keystore-test precedent).

### Manual Checks

1. On a debug build: create a profile; confirm via test hook/logcat (never logging key bytes) that the Keystore alias exists and the profile row holds an X.509 public key + algorithm string.
2. Back up and restore the database on a second device/emulator: profile GUID and public key survive; decrypt capability deliberately does not (fresh Keystore) — matches the keys-live-and-die-with-the-device policy.

### Acceptance Criteria

- Profile creation mints GUID + key pair in one flow; public key (X.509/DER) and algorithm stored on the profile record.
- Private key exists only under the Keystore alias: nothing in Room, DataStore, files, backups, or logs contains private-key material.
- Decrypt seam round-trips a blob sealed with the exported public key; `regenerate()` invalidates old ciphertext and yields a new public key on the record.
- Upstream crypto consumers and all existing tests are untouched and passing; iOS targets still build.

## Risk Mitigation

- Risk: Provisional key type contradicts the eventual wire contract (Q1).
  Mitigation: Interface-first design; zero interop surface exists before Story 10, so re-keying before first release costs nothing. The plan stays `Draft` and the Q1 answer is required before Story 10 planning — the owner carries it from foodus-relay contract v1.
- Risk: Private-key material leaks into portable storage (constitutional violation).
  Mitigation: Only `publicKey`/`algorithm` cross the interface as data; instrumented test asserts non-extractability; step 7 audit; laws §2 named in code kdoc.
- Risk: Breaking iOS targets by adding an expect without all actuals.
  Mitigation: Step 1 discovery before any edit; replicate whatever strategy upstream uses for the existing three crypto expects.
- Risk: Race/coordination with Story 3.2 on entity columns (Q3).
  Mitigation: Boss-level sequencing — reconcile the two plans before either executes; columns nullable so ordering is forgiving.
- Risk: `regenerate()` leaving a half-state (alias deleted, record not updated) on crash.
  Mitigation: Order operations record-last with the old key intact until the new pair exists; instrumented test covers regenerate atomicity at the Keystore level.

## Phase Split

Not needed. Single-pass story; the contract-gated registration half is excluded from scope rather than phased.

## Evidence / References

- Planning inputs verified in-tree 2026-07-28: `common/crypto/{IdentityCrypto,MasterCrypto,SignatureVerifier}.kt`, `common/infrastructure/crypto/CryptoModule.kt`, `androidMain .../crypto/{AndroidIdentityCrypto,AndroidMasterCrypto,AndroidPublicCryptoFactory,CryptoModule.android}.kt`, `androidInstrumentedTest .../crypto/*Test.kt`, `app/di/AppModule.kt:28`, `app/infrastructure/room/FoodYouDatabase.kt:81-82`, `common/infrastructure/auth/SafeSessionRepository.kt`.
- foodus-relay (read-only): wire contract v1 = that repo's Milestone 3 Story 1, Status Not Started (2026-07-28) — basis for the contract-gated markings here.
- Unverified claims: none knowingly; iOS expect/actual handling is explicitly a discovery step, not a claim.

## Complaints / Friction

### Story text vs upstream reality: an "identity key" already exists

**What happened:** Upstream already ships a hardware-backed `IdentityCrypto` ("unique key for the device", sign-only) used by auth/session code. The story reads as if the crypto identity is greenfield.
**Why this made the task harder:** Reusing the upstream key is tempting but wrong (different purpose, upstream-owned alias, sign-only); the plan must explicitly not touch it, and reviewers should know the name collision is deliberate avoidance.
**What was tried:** Read all upstream crypto interfaces, actuals, consumers, and tests before deciding to add a fork-owned sibling.
**What would improve this:** A line in the milestone story acknowledging the upstream crypto seam and blessing (or forbidding) reuse.
**What I think:** A separate fork-owned key with its own alias is the right call — additive, purpose-clean, merge-safe.

### Story 3.2's commonTest names broke the instrumented test gate

**What happened:** `:app:connectedDebugAndroidTest` failed at `dexBuilderDebugAndroidTest`, not in any
test: D8 rejects `Space characters in SimpleName ... are not allowed prior to DEX version 040`.
`commonTest` is dexed into the instrumentation APK, and Story 3.2 introduced the repo's only
backtick test names containing spaces.
**Why this made the task harder:** It blocked this story's mandatory instrumented Keystore
validation for a reason unrelated to this story's code.
**What was tried:** Confirmed the four affected files are the only offenders repo-wide
(`grep -rl 'fun \`[^\`]* [^\`]*\`' app/src/commonTest/`), then renamed every method in them to the
underscore style already used by `AndroidIdentityCryptoTest`. Behavior-preserving; no assertions
changed.
**What would improve this:** A note in the repo's testing guidance that `commonTest` in this KMP
module is dexed at min SDK 28, so backtick names with spaces are not available there.
**What I think:** Worth fixing rather than reporting-and-skipping, because the alternative was
shipping the milestone's security foundation with zero on-device evidence.

---

## Execution Log

Executed 28 July 2026. **Partial by instruction: the local key seam only.** The relay
registration half stays out of the tree pending the Story 5 contract gate — no speculative client
stubs, no wire-contract data classes. Plan Status intentionally remains `Draft`.

### Step 1 — iOS discovery (read-only, before any edit)

`app/src/` has exactly four source sets: `commonMain`, `androidMain`, `iosMain`,
`androidInstrumentedTest`/`commonTest`. `iosMain` contains **two files total**
(`AiScanCameraSection.ios.kt`, `AustralianFoodCompositionDatabaseModule.ios.kt`) and **no crypto
actuals**. The only `actual fun Module.masterCryptoDefinition` in the repo is in
`CryptoModule.android.kt`; `app/build.gradle.kts:78` does declare `iosArm64()`/`iosSimulatorArm64()`
and no custom `srcDir` redirection exists.

**Finding: the iOS targets already cannot satisfy the three existing crypto expects.** Adding a
fourth changes nothing about that posture, so this story adds the expect/actual pair Android-only
and matches upstream exactly. This resolves the plan's "Breaking iOS targets" risk as *pre-existing
and unchanged*, not as *mitigated*. It also means the plan's acceptance criterion "iOS targets still
build" was never true in the first place and is not claimed here.

### Steps 2–4 — the crypto seam

`ProfileMessagingCrypto` (commonMain) and `AndroidProfileMessagingCrypto` (androidMain), wired
through `profileMessagingCryptoDefinition()` in the existing `CryptoModule.kt` /
`CryptoModule.android.kt` pair. Upstream's `IdentityCrypto` / `MasterCrypto` and their aliases are
untouched; the new alias is `FOODUS_PROFILE_MESSAGING_KEY`.

Per the Boss ruling on Q1: provisional RSA-3072 OAEP, `PURPOSE_DECRYPT`,
`setUserAuthenticationRequired(false)`, min-SDK-28-safe, marked replaceable in the interface kdoc.
Per Q4: software-backed Keystore is accepted and `isSupported` is surfaced rather than gating
creation.

One interop detail worth carrying forward: the Android Keystore ignores the MGF1 digest in the
transformation string and always uses SHA-1, so both sides must pass an explicit
`OAEPParameterSpec("SHA-256", "MGF1", MGF1ParameterSpec.SHA1, …)`. `SHA-1` is therefore authorized
alongside `SHA-256` in the key spec, for MGF1 only. The companion exposes `TRANSFORMATION` and
`oaepParameterSpec()` so a sender reproduces it exactly.

### Steps 5 — profile integration and the Q3 ruling

Q3 was ruled in this story's favor: `ProfileMigration` (36→37) shipped without key columns, so this
story owns `ProfileKeyMigration` (37→38) adding **nullable** `publicKey` and `keyAlgorithm` to
`Profile`, `VERSION = 38`, exported schema `38.json` committed.

Honoring the 3.2 worker's constraints: generation is attached inside `CreateProfileUseCase.invoke`
between the `repository.get() != null` guard and `repository.insert(profile)`; the DAO gains a
narrow `updatePublicKey` query touching only the two key columns plus `lastEditedEpochSeconds`
(no whole-row upsert, so the GUID stays unrewritable); `profileModule` binds the new use cases with
`factoryOf` and the crypto dependency is constructor-injected.

Storage format: the public key is stored **Base64-encoded** rather than as a BLOB. `Profile` is a
`data class` and a `ByteArray` field would give it broken equality — the profile is compared by
value in several tests and in the reconcile check. Base64 is also the form the relay payload will
want. `encodeProfilePublicKey()` is the single definition of that format.

### Re-key crash-consistency — the chosen strategy

The critic finding is accepted: one fixed alias cannot hold the old and new key simultaneously, so
no atomicity is claimed. Implemented instead:

**The key vault is the source of truth; the profile record is a reconcilable copy of its public
half.** `ReconcileProfileKeyUseCase` reads the vault (minting a pair if the alias is gone), compares
against the record, and rewrites the record when they disagree. It is idempotent, and it is invoked
from `ProfileViewModel.init` — the profile card is where the identity surfaces, so recovery runs
whenever the user looks at it.

`RekeyProfileUseCase` is deliberately thin: `regenerate()` then *delegate the record write to
reconcile*, so a crash between the two halves is repaired by the same code path that repairs a
restored backup, not by a second near-duplicate path.

Boundaries considered, and what each leaves behind:

| Process dies… | State left | Repaired by |
| --- | --- | --- |
| after `regenerate()`, before the record write | vault has new key, record has old | next reconcile |
| mid record write | Room transaction; row is old or new, never torn | nothing needed |
| DB restored onto a device with a different/absent alias | record's key unusable | next reconcile (alias minted on read) |
| profile created before Story 3 | key columns NULL | next reconcile |

The invariant restored in every case: **a usable private key always matches the stored public key.**
The window is only ever "the record names a key nobody can send to", never "the record names a key
whose private half is lost with no way back" — the reverse would be unrecoverable.

### Step 7 — constitutional audit (laws §2)

- `grep -rn "privateKey\|PrivateKey" app/src --include=*.kt` outside the Android actual and its
  instrumented test: **zero hits.**
- Every `privateKey` reference inside the actual passes the Keystore handle straight into a JCA
  primitive (`KeyFactory.getKeySpec`, `Cipher.init`). `.encoded` is never called on it in production
  code; the only `.encoded` in the new actual is `certificate.publicKey.encoded`.
- Everything the profile slice persists is `publicKey`/`keyAlgorithm` as `String` — grep over
  `profile/` returns no other key-related field.
- No `Log.`/`println`/`Logger`, no `DataStore`, no `File`/`writeText`/`FileOutputStream` in any new
  file. Nothing logs key material of either half.
- The instrumented test asserts `entry.privateKey.encoded == null` — the platform itself refuses to
  export it.

### Validation

- Targeted unit tests: `:app:testDebugUnitTest` — **passed.** Profile slice: CreateProfile 5,
  Reconcile 4, Rekey 1, Rename 2 — 12 tests, 0 failures.
- Instrumented: `:app:connectedDebugAndroidTest` filtered to
  `AndroidProfileMessagingCryptoTest` on AVD `foodyou` — **passed, 4/4** (decrypt round trip;
  public key stable across instances; `regenerate` yields a different key, orphans old ciphertext,
  and the new pair immediately works; private key not exportable).
- Build: `:app:compileDebugKotlinAndroid` and `:app:assembleDebug` — **passed.**
- Migration 37→38 on device: pre-upgrade DB at `user_version` 37 holding a live profile row
  (`d51f850e-…`, `Jarryd2`). After installing the new build and launching:
  `user_version` = 38, the row survived unchanged with the two new columns NULL, Room's schema
  validation passed (no crash), and the on-device `CREATE TABLE` matches exported `38.json`
  byte for byte.
- Mismatch recovery on device: opening the Groups tab on that migrated pre-Story-3 profile
  reconciled it — same GUID, `keyAlgorithm` = `RSA/ECB/OAEPWithSHA-256AndMGF1Padding`, `publicKey`
  564 Base64 chars (= 422-byte X.509 SPKI, correct for RSA-3072).
- Full suite: **not run** — targeted validation covers the changed surface; the instrumented run was
  filtered to this story's class.
- Remaining uncertainty: the emulator is software-backed, so `isSupported` was deliberately not
  asserted and hardware-backed behavior on a Galaxy S22 Ultra is unverified. `connectedAndroidTest`
  uninstalls the app, which wiped the emulator's database, so the manual backup/restore-to-a-second-
  device check in the plan's Manual Checks was not performed. RSA-OAEP is provisional pending
  contract v1 (Q1) and Q2 (whether relay auth needs a companion signing key) is still open.

### Deviations from the plan

1. **Partial execution by instruction** — local seam only; relay registration excluded.
2. **Risk Mitigation wording superseded** — "old key intact until the new pair exists" is not
   achievable and is replaced by the reconcile strategy above.
3. **Q3 resolved the other way** — this story owns the columns and a second migration, not 3.2.
4. **`ReconcileProfileKeyUseCase` was not in the plan** — it is the concrete form the crash-
   consistency ruling took, and it is what gives `RekeyProfileUseCase` a crash-safe record write.
5. **Renamed 12 test methods across four Story 3.2/3.3 commonTest files** (see Complaints) to
   unblock `connectedDebugAndroidTest`. Out of this story's strict scope; behavior-preserving; done
   because it was the only way to satisfy the mandatory instrumented gate.
6. **`RekeyProfileUseCase` has no production caller yet** — it is the seam Story 3.14's drill
   consumes, registered in DI and covered by a unit test, but nothing in the UI invokes it today.

---

## Completion Review

**Scope delivered: PARTIAL, by instruction.** The local key seam is complete and verified on
device. The registration half — publishing this public key to the relay via the
`...-profile-registration` endpoints — is untouched and awaits the Story 5 contract gate plus Story
15's relay URL. Status stays `Draft`.

Acceptance criteria, honestly scored:

- Profile creation mints GUID + key pair in one flow, public key and algorithm on the record — **met**
  (unit test + on-device evidence).
- Private key confined to the Keystore; nothing in Room, DataStore, files, backups, or logs — **met**
  (step-7 audit + instrumented non-exportability assertion).
- Decrypt round trip; `regenerate()` invalidates old ciphertext and yields a new public key on the
  record — **met** (instrumented 4/4 + reconcile unit tests).
- Upstream crypto consumers and existing tests untouched and passing — **met**.
- "iOS targets still build" — **not met and not attempted**; step 1 established iOS could never
  satisfy the pre-existing crypto expects, so this criterion was wrong when written. Recorded rather
  than quietly dropped.

What the next stories inherit:

- **Story 3.8 (envelope pipeline)** consumes `ProfileMessagingCrypto.decrypt`. It must seal with the
  exact `TRANSFORMATION` + `oaepParameterSpec()` the companion exposes, and it should treat RSA-OAEP
  as hybrid-wrapping a per-message AES key rather than encrypting payloads directly — RSA-3072/OAEP
  SHA-256 carries only 318 plaintext bytes.
- **Story 3.14 (household proof)** drives `RekeyProfileUseCase`. Re-keying is local only today: it
  rotates the vault and repairs the record, and announcing the new key to friends does not exist
  yet.
- **Story 3.15 (relay URL)** is unaffected by this story, but registration needs both it and the
  contract before the public key can leave the device.
- **Owner decision still required before Story 3.8 planning:** Q1 (does contract v1 keep RSA-OAEP?)
  and Q2 (does relay auth need a companion signing key, given a `PURPOSE_DECRYPT` key cannot sign?).
  A change to Q1 costs one `regenerate()` and one migration — deliberately cheap, and cheapest
  before first release.
