# Plan: Crypto Identity

## Metadata

- Task Type: `STORY`
- Status: `Draft`
- Owner: Jarryd Adaens
- Last Updated: 28 July 2026

## Linked Context

- Milestone: [context/milestones/milestone-3.md](../../../milestones/milestone-3.md)
- Story: [Story 3: Crypto identity](../../../milestones/milestone-3.md#story-3) `[STORY 3.3]`
- Design authority: `context/design.md` — "The Multiplayer Exception" (E2E model, private keys in the Android Keystore, keys-live-and-die-with-the-device policy) and "Security and Privacy"
- Constitutional constraint: `context/laws.md` §2 — Secrets and Data Boundaries. The private key never enters the Room database, DataStore, any file, any backup, or any log; it never leaves the Keystore vault.
- Relay Contract Conformance: [milestone-3.md §Relay Contract Conformance](../../../milestones/milestone-3.md#relay-contract-conformance-2026-07-27-relay-seed-dictation) — binds the registration seam described below.
- Related Plans:
  - Story 3.2 (Profile) — `context/implementation-plans/milestone-3/story-2-profile/plan.md` (planned in the same run): owns the profile Room entity that carries the public key; key generation hooks into its create-profile flow.
  - Story 3.15 (Relay URL setting) — supplies the user-entered relay endpoint the registration call needs.
  - Story 3.8 (Envelope & E2E pipeline) — **blocked, unplanned this run** (contract gate); future consumer of this story's decrypt capability.
  - Story 3.14 (Household proof) — exercises the re-key drill through the seam this story creates.
- Dependency note (app → server, per conformance rule 6): **blocked by foodus-relay: register/update-profile endpoints (shared slug `...-profile-registration`), contract v1, deployed.** Owner releases via the Story 5 gate. The local key work below is NOT blocked.
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
- Re-key seam: `regenerate()` replaces the Keystore entry and the caller updates the profile record. Re-announcement to friends/relay is Story 8/14 scope.
- Android instrumented tests mirroring `AndroidIdentityCryptoTest` / `AndroidMasterCryptoTest`: pair generated at creation, public key stable across process restarts, decrypt round-trip (encrypt with public key outside Keystore, decrypt via seam), regenerate yields a different key, private key not extractable.

### Out Of Scope

- The register/update-profile relay call and any wire-contract data classes — blocked by foodus-relay contract v1 (dependency note above). No speculative client stubs are written for it.
- Envelope schema, send/poll plumbing, message routing (Story 3.8, itself blocked this run).
- Key re-announcement messages to friends (Story 3.8 pipeline / Story 3.14 drill).
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

- Q: `[STORY 3.3]` Which asymmetric scheme does the envelope encryption use? The wire contract (foodus-relay, contract v1, **Not Started** as of 2026-07-28) owns interoperable crypto choices. Android Keystore realistically offers: (a) RSA-3072/4096 with OAEP, `PURPOSE_DECRYPT`, works from API 28, envelope = hybrid RSA-wrapped AES-GCM key; (b) EC key agreement (`PURPOSE_AGREE_KEY`, API 31+) with HKDF → AES-GCM, more modern but above min SDK 28 and needs a runtime capability gate.
  Impact: Determines the Keystore key type generated at profile creation and the `algorithm` string stored with the profile and later registered with the relay. Wrong guess forces a re-key.
  Assumption: Implement with **(a) RSA-OAEP hybrid-ready** as the provisional type (min-SDK-safe, one code path), explicitly marked replaceable: until Story 8 ships there is zero interop surface, so a pre-release re-key is free. The contract decision must be carried across by the owner before Story 8 planning; if the contract picks (b), `regenerate()` plus a migration of the profile's key columns absorbs the change.
  Status: OPEN
  Answer: —

- Q: `[STORY 3.3]` Does the relay's endpoint authentication (foodus-relay open decision: proof of GUID ownership, "likely device key-pair request signing") reuse this same key pair for signing, or a separate one? A single `PURPOSE_DECRYPT` key cannot also be a clean signing key; Keystore best practice is one purpose per key.
  Impact: If request signing is required, this story's interface may need a companion sign key (or upstream's existing sign-only `IdentityCrypto` pattern gets a fork-owned sibling), and the registration payload may need to carry two public keys.
  Assumption: Out of scope until the contract answers; the interface is kept narrow (decrypt + regenerate) so a signing capability can be added additively without reshaping this story's deliverable.
  Status: OPEN
  Answer: —

- Q: `[STORY 3.3]` Column ownership with Story 3.2: does the profile entity ship with `publicKey`/`keyAlgorithm` columns from day one (Story 3.2's migration), or does this story add them in a second migration?
  Impact: One Room migration vs two; ordering of the two stories' execution.
  Assumption: Story 3.2's entity includes the two columns as nullable from day one (cheapest; one migration), and this story populates them at creation time. To be reconciled between the two plans before execution.
  Status: OPEN
  Answer: —

- Q: `[STORY 3.3]` Policy when `isSupported` is false (no TEE/StrongBox — e.g. emulator or exotic API 28 device): proceed with software-backed Keystore, or refuse to create the multiplayer identity?
  Impact: Decides an error/warning path at profile creation and what Story 3.14 records as evidence.
  Assumption: Proceed with software-backed Keystore (the key still never leaves the Keystore API surface) and surface `isSupported` for Story 3.14's evidence; both household target devices are hardware-backed anyway.
  Status: OPEN
  Answer: —

## Execution Steps

1. Discovery: confirm iOS handling of the existing crypto expects
   - Why: `cryptoModule()` gains a fourth expect definition; the plan must not break the `iosArm64`/`iosSimulatorArm64` targets. No iOS actuals were found under `app/src/` — confirm where (or whether) iOS satisfies the current three before adding one.
   - Edits: none (read-only; findings noted in this plan's Execution Log).
   - Dependencies: none; do first.

2. Define `ProfileMessagingCrypto` interface in `app/src/commonMain/.../common/crypto/ProfileMessagingCrypto.kt`
   - Why: The seam every later story consumes (Story 8 decrypt, Story 14 re-key); keeps envelope crypto behind an interface so the Q1 contract decision swaps implementations, not callers.
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
   - Edits: in Story 3.2's create-profile use case (exact file per that plan): after GUID mint, read `profileMessagingCrypto.publicKey`/`algorithm` and store both on the profile entity. Re-key path: a small `RekeyProfileUseCase` (fork-owned, in the profile slice) calling `regenerate()` and updating the record — the seam Story 3.14's drill and the future relay re-announcement reuse.
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
  Mitigation: Interface-first design; zero interop surface exists before Story 8, so re-keying before first release costs nothing. The plan stays `Draft` and the Q1 answer is required before Story 8 planning — the owner carries it from foodus-relay contract v1.
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
