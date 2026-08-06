# Plan: Profile

## Metadata

- Task Type: `STORY`
- Status: `Complete`
- Owner: Jarryd Adaens
- Last Updated: 28 July 2026

## Linked Context

- Milestone: [context/milestones/milestone-3.md](../../../milestones/milestone-3.md)
- Story: [Story 2: Profile](../../../milestones/milestone-3.md#story-2) `[STORY 3.2]`
- Dictation source: [2026-07-27 Milestone 3 multiplayer addendum](../../../dictations-tier-0/2026-07-27_milestone-3_multiplayer-addendum.md)
- Design authority: `context/design.md` — "The Multiplayer Exception" (profile metadata is the only social data the relay ever sees; the GUID is the primary key for everything social)
- Related Plans:
  - Story 3.1 (tabbed UI shell) — **depends on it**: the My Profile card renders at the top of the Groups tab that Story 3.1 creates. This plan adds the card into that tab; it does not re-plan the shell.
  - Story 3.6 (crypto identity) — **depends on this story**: the key pair is generated alongside the GUID at profile creation. This plan leaves an explicit creation-time integration seam; Story 3.6 owns all key work and the public-key column.
  - Story 3.8 (friend codes) — **depends on this story**: the friend code is a field of the profile surface. This plan reserves display space on the card only; Story 3.8 owns the column, generation, and regenerate flow.
- External Tooling: none required.

## CER

- Complexity: 3
- Effort: 4
- Risk: 3
- Notes: Inline estimate. Every needed pattern already exists in the repo: the `importexport/providermetadata` slice is the exact shape to mirror (entity + DAO + `*Database` interface + Koin module + Room repository), recent fork migrations (`MealTemplateMigration`, `QuickAddExpansionMigration`) set the manual-migration convention, and `app/ui/settings/ai/` shows the screen + ViewModel + module wiring for a fork-added UI surface. Risk sits on the Room schema bump (36 → 37) touching the upstream-shared `FoodYouDatabase.kt`/`RoomModule.kt` (both already fork-touched; edits stay additive) and on getting the GUID-immutability invariant right, since every later social story keys off it.

## Objective

Add a deliberately minimal local profile to FoodUs: a Room-persisted single record holding an immutable randomly generated GUID (the primary key for everything social), a cosmetic renameable username, and created/last-edited timestamps — created on demand from a My Profile card at the top of the Groups tab, editable afterwards, riding the existing database backup/restore because it lives in the app's single Room database.

## Scope

### In Scope

- New `profile` feature slice under `com.maksimowiczm.foodyou.profile` following the providermetadata slice shape: `domain` (Profile model, `ProfileRepository` interface, create/rename use cases), `infrastructure` (`ProfileEntity`, `ProfileDao`, `ProfileDatabase` interface, `RoomProfileRepository`), `ProfileModule.kt`.
- Room integration: `ProfileEntity` registered in `FoodYouDatabase`, version bump 36 → 37, a manual additive `ProfileMigration` mirroring the recent fork-migration convention, `ProfileDatabase` added to the `binds` array in `RoomModule.kt`, exported schema `37.json`.
- Profile creation: GUID via `kotlin.uuid.Uuid.random()` (stored as its string form), `createdEpochSeconds`/`lastEditedEpochSeconds` stamped at creation; rename updates username and `lastEditedEpochSeconds` only — the GUID is never regenerated or exposed for mutation.
- Creation-time seam for Story 3.6: profile creation flows through a single domain use case (`CreateProfileUseCase`) so key-pair generation can be attached there later without reshaping callers.
- My Profile card at the top of the Groups tab (Story 3.1's `GroupsScreen`): empty state prompting creation, filled state showing username and created/last-edited dates, with a reserved friend-code area left to Story 3.8.
- Create/edit UI (dialog or small screen — see Questions) + `ProfileViewModel`, Koin-wired per the `aiSettingsModule()` pattern.
- New English strings in `shared/resources/src/commonMain/composeResources/values/strings.xml` (additive fork-owned keys).
- Targeted tests: create-profile use case (GUID/timestamps set), rename (GUID stable, lastEdited bumped), single-profile invariant.

### Out Of Scope

- Friend codes: column, generation, regeneration (Story 3.8).
- Key pair, Keystore, public-key storage on the profile record (Story 3.6).
- Any relay/network calls — the profile is local-only in this story; registration with the relay is Story 3.6's contract-gated seam.
- Groups tab shell/navigation itself (Story 3.1).

## Non-Goals

- No multi-profile support — exactly one profile per device.
- No username uniqueness, validation against collisions, or reserved names — usernames are explicitly cosmetic.
- No avatar, bio, or any profile field beyond the dictated minimal set.
- No UI that surfaces the raw GUID (it is "hidden from casual view"; see Questions).

## Current Understanding

Paths verified in the working tree on 28 July 2026.

- **Database:** `app/src/commonMain/kotlin/com/maksimowiczm/foodyou/app/infrastructure/room/FoodYouDatabase.kt` — single Room DB, `VERSION = 36`, entity list + per-slice `*Database` interfaces as supertypes; manual migrations listed in the companion (`MealTemplateMigration`, `AustralianFoodProviderMigration`, `QuickAddExpansionMigration` are the recent fork precedents). Exported schemas live under `app/schemas/com.maksimowiczm.foodyou.app.infrastructure.room.FoodYouDatabase/` (36.json is current).
- **Koin DB binding:** `app/src/commonMain/kotlin/com/maksimowiczm/foodyou/app/infrastructure/room/RoomModule.kt` — `single<FoodYouDatabase>` bound to each `*Database` interface via `binds(...)`; `ProfileDatabase` appends there.
- **Slice to mirror:** `app/src/commonMain/kotlin/com/maksimowiczm/foodyou/importexport/providermetadata/` — entity (`ProviderMetadataEntity`), DAO, `ProviderMetadataDatabase` interface (`val providerMetadataDao`), `RoomProviderMetadataRepository`, and `ProviderMetadataModule.kt` (`factory { get<ProviderMetadataDatabase>().providerMetadataDao }` + repository bind). Module registration point for slices is `app/di/InitKoin.kt` (discovery step: confirm exact registration list line).
- **Migration convention:** manual `Migration` objects under `app/infrastructure/room/migration/` with additive `CREATE TABLE IF NOT EXISTS`, column definitions mirroring the exported schema so runtime validation passes (`MealTemplateMigration.kt` is the template).
- **Timestamp convention:** epoch-seconds `Long` columns (`createdEpochSeconds` in `MealTemplate`).
- **GUID:** no existing UUID usage in `app/src` — `kotlin.uuid.Uuid.random()` is available (Kotlin 2.3.10 per `gradle/libs.versions.toml:27`; requires `@OptIn(ExperimentalUuidApi::class)`).
- **UI pattern:** `app/src/commonMain/kotlin/com/maksimowiczm/foodyou/app/ui/settings/ai/` — screen + ViewModel + `Module.aiSettingsModule()` wiring; profile UI follows the same shape under `app/ui/` (see Questions for exact placement). Groups tab composable arrives with Story 3.1; this story slots the card into it.
- **Backup/restore:** the profile rides the existing whole-database mechanism surfaced by `app/ui/database/master/DatabaseSettingsScreen.kt` / `DatabaseSettingsListItem.kt` — anything in `FoodYouDatabase` is included by construction (discovery step: confirm the export path covers the DB file, not per-table CSV only).
- **Behaviors to preserve:** existing diary/food tables untouched; migration is purely additive; `validate-meals` CI and existing tests unaffected.
- **Constraints:** fork philosophy — new files are fork-additive inside the `com.maksimowiczm.foodyou` namespace; edits to upstream-shared files (`FoodYouDatabase.kt`, `RoomModule.kt`, `InitKoin.kt`, `strings.xml`) stay small and appended.

## Questions / Unknowns

- Q: `[STORY 3.2]` Should social features share one slice or get one slice each — i.e. does this story root `com.maksimowiczm.foodyou.profile`, or a broader `social` slice that Stories 3.8/3.9/3.11 grow into?
  Impact: Package/module layout for the whole milestone; renaming a slice later churns every social story.
  Assumption: A dedicated `profile` slice, matching the repo's vertical-slice-per-concern convention (friends and groups get their own slices in their stories).
  Status: OPEN
  Answer: —

- Q: `[STORY 3.2]` Create/edit surface: a dialog over the Groups tab (like `UpdateUsdaApiKeyDialog`) or a small dedicated screen with a nav route (like `AiSettingsScreen`)?
  Impact: Decides whether `FoodYouAppNavHost.kt` (upstream-shared) gains a route or the change stays entirely inside the Groups tab.
  Assumption: A dialog launched from the My Profile card — one field (username) plus read-only dates doesn't justify a route, and it avoids touching the nav host.
  Status: OPEN
  Answer: —

- Q: `[STORY 3.2]` "ID hidden from casual view" — hidden entirely (no UI ever shows it), or revealable via a deliberate affordance (e.g. long-press / "show ID")?
  Impact: Card UI and whether any copy-GUID affordance exists for future debugging of friend/relay issues.
  Assumption: Hidden entirely in this story; nothing in Milestone 3 requires a human to read the GUID (friend codes are the human-facing handle).
  Status: OPEN
  Answer: —

- Q: `[STORY 3.2]` Username constraints: is blank allowed, and is there a length cap?
  Impact: Validation logic in the create/edit UI and use case.
  Assumption: Non-blank after trim, no length cap beyond a sane input limit; no uniqueness anywhere (dictated: collisions allowed).
  Status: OPEN
  Answer: —

- Q: `[STORY 3.2]` Should this migration pre-create nullable `friendCode`/`publicKey` columns to spare Stories 3.8/3.6 their own schema bumps?
  Impact: One migration vs three; but pre-creating columns plans other stories' storage for them.
  Assumption: No — each story owns its additive migration, keeping every changed line traceable to its own story (laws §7).
  Status: OPEN
  Answer: —

## Execution Steps

1. Create the `profile` slice domain layer.
   - Why: Domain-first keeps the GUID invariant and creation seam in pure Kotlin, testable without Room.
   - Edits: New `profile/domain/Profile.kt` (id: String GUID, username, createdEpochSeconds, lastEditedEpochSeconds), `profile/domain/ProfileRepository.kt` (observe/create/rename), `profile/domain/CreateProfileUseCase.kt` — the single entry point that generates the GUID + timestamps and is the documented Story 3.6 hook (key-pair generation attaches here).
   - Dependencies: none.

2. Add Room infrastructure and the 36 → 37 migration.
   - Why: Persistence in the single Room DB is what makes the profile ride backup/restore.
   - Edits: New `profile/infrastructure/room/ProfileEntity.kt` (`@Entity(tableName = "Profile")`, `@PrimaryKey id: String`), `ProfileDao.kt` (Flow-returning single-row query, insert, update), `ProfileDatabase.kt` interface; new `app/infrastructure/room/migration/ProfileMigration.kt` (additive `CREATE TABLE IF NOT EXISTS Profile ...` mirroring exported schema 37); `FoodYouDatabase.kt` — add entity, supertype, `VERSION = 37`, migration list entry; `RoomModule.kt` — add `ProfileDatabase::class` to `binds`.
   - Dependencies: step 1 (entity mirrors domain model).

3. Wire the slice into Koin.
   - Why: Matches every existing slice; keeps DI additive.
   - Edits: New `profile/ProfileModule.kt` (DAO factory + `RoomProfileRepository` bind + use case factory); register in `app/di/InitKoin.kt` alongside the existing slice modules.
   - Dependencies: step 2.

4. Targeted tests for the domain invariants.
   - Why: The GUID is the primary key for everything social; its immutability and the single-profile invariant are regression-prone and cheap to lock down.
   - Edits: `commonTest` tests — create sets GUID + equal created/lastEdited stamps; rename changes username + lastEdited but never the GUID; second create is rejected/no-ops while a profile exists (single-row invariant).
   - Dependencies: steps 1–3.

5. Build the My Profile card and create/edit UI.
   - Why: The user-facing deliverable — create on demand, edit afterwards.
   - Edits: New `app/ui/groups/profile/MyProfileCard.kt` (empty state → create; filled state → username, created/last-edited dates, reserved friend-code area with a placeholder comment pointing at Story 3.8), `ProfileEditDialog.kt` (per assumption), `ProfileViewModel.kt`, `Module.profileUiModule()` mirroring `aiSettingsModule()`; slot the card at the top of Story 3.1's Groups tab composable.
   - Dependencies: Story 3.1's Groups tab exists; steps 1–3.

6. Add strings and run validation.
   - Why: Fork-additive English base strings; prove the migration and UI on-device.
   - Edits: New keys in `shared/resources/src/commonMain/composeResources/values/strings.xml`; regenerate/verify exported schema `37.json`.
   - Dependencies: step 5.

## Validation

### Automated Checks

- Targeted unit tests from step 4 (`:app:` common test task for the profile package).
- Full compile of the app module (debug variant) to catch DI/schema wiring errors.
- Room exported schema `37.json` generated and committed; migration column definitions match it.

### Manual Checks

1. Fresh install: Groups tab shows the My Profile empty-state card; creating a profile with a username shows the filled card with today's created/last-edited dates.
2. Upgrade path: install a build at schema 36 with diary data, upgrade to 37 — app opens, diary data intact, profile card in empty state.
3. Rename the username: card updates, last-edited date changes, and (via DB inspect or log-free re-open) the GUID is unchanged.
4. Backup/restore via the existing database settings surface: restored database carries the profile.

### Acceptance Criteria

- A profile can be created on demand from the Groups tab and edited afterwards; username is renameable; the GUID never changes and is not displayed.
- The profile record survives app restart and database backup/restore.
- Existing diary/food data is untouched by the migration on upgrade.

## Risk Mitigation

- Risk: Room migration bricks upgrades on the two household phones.
  Mitigation: Purely additive manual migration mirroring the exported schema (established fork convention); manual upgrade check from a schema-36 build before any device deployment.
- Risk: GUID mutability sneaks in later (e.g. an edit path that rewrites the row with a new id).
  Mitigation: Rename flows through a dedicated repository method that updates username/lastEdited only; unit test locks the invariant.
- Risk: Upstream merge friction from touching `FoodYouDatabase.kt` / `RoomModule.kt` / `InitKoin.kt`.
  Mitigation: Accepted — these files are the established fork seam (four prior fork migrations did the same); edits are single appended lines.
- Risk: Story 3.1 lands later or with a different Groups tab shape than assumed.
  Mitigation: The card is a self-contained composable with no shell dependency beyond a slot; if the tab isn't merged yet, the card can be previewed standalone and slotted when the shell lands.

## Phase Split

Not needed — single-pass story well under CER thresholds.

## Evidence / References

- Pattern sources verified 28 July 2026: `FoodYouDatabase.kt` (VERSION 36, migration list), `RoomModule.kt` (binds array), `importexport/providermetadata/*` (slice shape), `MealTemplateMigration.kt` (additive migration convention), `app/ui/settings/ai/*` (UI + ViewModel + Koin wiring), `gradle/libs.versions.toml` (Kotlin 2.3.10, Room 2.8.4), `app/schemas/.../36.json` (current exported schema).
- Unverified claims: exact registration line in `app/di/InitKoin.kt`; whether `DatabaseSettingsScreen`'s backup exports the whole DB file (assumed; discovery step in Current Understanding).

## Complaints / Friction

None worth recording — source story and repository conventions were clear.

## Execution Log

Executed 28 July 2026 in a serial rails-boss-execute run, immediately after Story 3.1 (commit
`8ac990db`).

### Decisions on the plan's OPEN questions

All five were resolved on the plan's own documented assumptions, per Boss ruling.

| Question | Decision |
| --- | --- |
| Slice layout | Dedicated `com.maksimowiczm.foodyou.profile` slice. Friends/groups get their own slices in Stories 3.6/3.7/3.9. |
| Create/edit surface | `ProfileEditDialog` launched from the card. One editable field did not justify a nav route, so `FoodYouAppNavHost.kt` was left untouched. |
| GUID visibility | Hidden entirely. No UI renders it and there is no copy/reveal affordance. |
| Username constraints | Trimmed, must be non-blank; no length cap, no uniqueness. The dialog disables **Save** while blank, and both use cases `require` non-blank as a programming-error guard. |
| Pre-creating `friendCode`/`publicKey` | No. Each story owns its own additive migration (Boss ruling confirmed the assumption). Story 3.3 adds its key columns in a 37 → 38 migration. |

### Deviations from the plan

1. **`RenameProfileUseCase` added.** The plan's Execution Step 1 named only `CreateProfileUseCase`
   while Scope said "create/rename use cases". Rename carries real logic (trim, validate, stamp
   `lastEdited`, never touch the id), so it became a peer use case rather than a ViewModel-to-DAO
   passthrough. This is what makes the GUID-immutability test exercise production code.
2. **`runBlocking`, not `runTest`.** `kotlinx-coroutines-test` is not on the `commonTest` classpath
   and `RfcCsvParserTest` establishes `runBlocking` as the repo convention. Adding a dependency for
   test sugar would have breached the Innovation Boundary law for no behavioral gain.
3. **`headline_edit_profile` reused, not added.** The key already existed at `strings.xml:585`
   (upstream goals profile) with the identical "Edit profile" text; the duplicate broke the resource
   build, and reusing it is the DRY-correct fix. Four new keys were added instead of five.
4. **`ProfileRepository.get()` added** alongside `observe()`. The single-profile invariant and the
   rename path both need a one-shot read; a `Flow.first()` in the use cases would have been less
   obvious.

### Resolved unverified claims

- `app/di/InitKoin.kt` registration line confirmed: `profileModule` appended to the `modules(...)`
  list between `pollModule` and `settingsModule`.
- **Backup covers the whole DB file.** `MainActivity.onDatabaseBackup` routes to `DeveloperActivity`,
  which does `dbFile.copyTo(backupFile, overwrite = true)` on the `open_source_database.db` file
  (`DeveloperActivity.kt:306-307`) — not a per-table CSV export. The profile rides backup/restore by
  construction, as assumed.

### Files changed

New (profile slice):

- `app/src/commonMain/kotlin/com/maksimowiczm/foodyou/profile/domain/Profile.kt`
- `app/src/commonMain/kotlin/com/maksimowiczm/foodyou/profile/domain/ProfileRepository.kt`
- `app/src/commonMain/kotlin/com/maksimowiczm/foodyou/profile/domain/CreateProfileUseCase.kt`
- `app/src/commonMain/kotlin/com/maksimowiczm/foodyou/profile/domain/RenameProfileUseCase.kt`
- `app/src/commonMain/kotlin/com/maksimowiczm/foodyou/profile/infrastructure/RoomProfileRepository.kt`
- `app/src/commonMain/kotlin/com/maksimowiczm/foodyou/profile/infrastructure/room/ProfileEntity.kt`
- `app/src/commonMain/kotlin/com/maksimowiczm/foodyou/profile/infrastructure/room/ProfileDao.kt`
- `app/src/commonMain/kotlin/com/maksimowiczm/foodyou/profile/infrastructure/room/ProfileDatabase.kt`
- `app/src/commonMain/kotlin/com/maksimowiczm/foodyou/profile/ProfileModule.kt`

New (migration, UI, tests):

- `app/src/commonMain/kotlin/com/maksimowiczm/foodyou/app/infrastructure/room/migration/ProfileMigration.kt`
- `app/src/commonMain/kotlin/com/maksimowiczm/foodyou/app/ui/groups/profile/MyProfileCard.kt`
- `app/src/commonMain/kotlin/com/maksimowiczm/foodyou/app/ui/groups/profile/ProfileEditDialog.kt`
- `app/src/commonMain/kotlin/com/maksimowiczm/foodyou/app/ui/groups/profile/ProfileViewModel.kt`
- `app/src/commonMain/kotlin/com/maksimowiczm/foodyou/app/ui/groups/profile/ProfileUiModule.kt`
- `app/src/commonTest/kotlin/com/maksimowiczm/foodyou/profile/domain/CreateProfileUseCaseTest.kt`
- `app/src/commonTest/kotlin/com/maksimowiczm/foodyou/profile/domain/RenameProfileUseCaseTest.kt`
- `app/src/commonTest/kotlin/com/maksimowiczm/foodyou/profile/domain/FakeProfileRepository.kt`
- `app/src/commonTest/kotlin/com/maksimowiczm/foodyou/profile/domain/FixedDateProvider.kt`
- `app/schemas/com.maksimowiczm.foodyou.app.infrastructure.room.FoodYouDatabase/37.json`

Modified (all edits additive, single appended lines except `GroupsScreen.kt`):

- `FoodYouDatabase.kt` — entity, supertype, `VERSION = 37`, `ProfileMigration` in the migration list
- `RoomModule.kt` — `ProfileDatabase::class` in `binds`
- `app/di/InitKoin.kt` — `profileModule`
- `app/ui/UiModule.kt` — `profileUi()`
- `app/ui/groups/GroupsScreen.kt` — replaced the `PlaceholderTabScreen` delegation with a real
  Scaffold + LazyColumn hosting `MyProfileCard` (`PlaceholderTabScreen` left intact for Notifications)
- `shared/resources/.../values/strings.xml` — 4 new keys

## Completion Review

### Acceptance criteria

| Criterion | Result |
| --- | --- |
| Profile creatable on demand from the Groups tab, editable afterwards | Met — verified on emulator |
| Username renameable | Met — "Jarryd" → "Jarryd2" via the card's Edit action |
| GUID never changes and is not displayed | Met — id stable across rename in the DB; no UI surface renders it |
| Profile survives app restart | Met — force-stop and relaunch kept the filled card |
| Profile survives database backup/restore | Met by construction — backup copies the whole DB file (see Resolved unverified claims) |
| Existing diary/food data untouched by the migration | Met — canary row and the four seeded meals survived the 36 → 37 upgrade |

### Validation

- Targeted tests: `./gradlew.bat :app:testDebugUnitTest` — **passed**. `CreateProfileUseCaseTest`
  4/4 and `RenameProfileUseCaseTest` 2/2 confirmed in the JUnit XML; whole module suite green, no
  pre-existing failures observed.
- Build/compile: `./gradlew.bat :app:assembleDebug` — **passed**.
- Room schema: exported `37.json` committed. Its `Profile` `createSql` matches `ProfileMigration`
  column-for-column (`id` TEXT NOT NULL PK, `username` TEXT NOT NULL, `createdEpochSeconds` and
  `lastEditedEpochSeconds` INTEGER NOT NULL). Room's runtime identity-hash validation passed on the
  real upgrade below, which is the strongest available proof.
- Manual checks (emulator `foodyou`, package `io.github.jarrydadaens.foodus.dev`):
  1. **Schema 36 → 37 upgrade with surviving data** — seeded a canary `Product` row into the live
     schema-36 database, installed the new APK over it, launched. `PRAGMA user_version` went 36 → 37,
     the canary row and all four `Meal` rows survived, and the new `Profile` table was present and
     empty. Canary row removed afterwards.
  2. **Empty state** — Groups tab showed the My Profile card with its create prompt and button.
  3. **Creation** — created "Jarryd"; card switched to the filled state showing "28 July 2026" for
     both dates; DB row `d51f850e-…|Jarryd|1785244404|1785244404`.
  4. **Rename + GUID immutability** — renamed to "Jarryd2"; the id and `createdEpochSeconds` were
     byte-identical while `lastEditedEpochSeconds` advanced 1785244404 → 1785244430.
  5. **Restart persistence** — force-stop and relaunch; card still filled.
- Full suite: not run beyond `:app:testDebugUnitTest` — the change is confined to `:app`, and the
  Boss's stated bar was that this task keep passing.
- Remaining uncertainty:
  - Backup/restore was **not** exercised end-to-end through the Developer Activity UI; it is argued
    from the whole-file-copy implementation rather than a round trip.
  - Verified on the emulator only. Not deployed to either household phone.
  - The card's placement relative to future Friends (3.7) and group (3.9) cards is a single
    `LazyColumn` item; those stories will append siblings.

### Handover to Story 3.3 (crypto identity)

- **Creation seam:** `CreateProfileUseCase.invoke(username): Profile?` in
  `profile/domain/CreateProfileUseCase.kt` is the only path that mints a GUID. Attach key-pair
  generation there, between the `repository.get() != null` guard and `repository.insert(profile)`,
  so the key pair is always born with the id. It returns `null` when a profile already exists.
- **Entity:** `ProfileEntity` (table `Profile`, `@PrimaryKey val id: String`) —
  add nullable key columns to it and to the domain `Profile` model.
- **Migration:** own a `Migration(37, 38)` object under
  `app/infrastructure/room/migration/`, `ALTER TABLE Profile ADD COLUMN` style, appended to
  `FoodYouDatabase.migrations` with `VERSION = 38` and a committed `38.json`. Nullable columns only —
  the profile created by this story predates the keys.
- **DAO:** `ProfileDao.updateUsername` is deliberately a narrow `@Query` that touches only
  `username` and `lastEditedEpochSeconds`. Add a separate narrow update for the public key rather
  than widening it or introducing a whole-row upsert — that is what keeps the GUID immutable.
- **DI:** `profileModule` (`profile/ProfileModule.kt`) provides the DAO, `ProfileRepository`, and
  both use cases via `factoryOf`; constructor-injecting a Keystore service into
  `CreateProfileUseCase` needs no registration change beyond binding the new dependency.
- **Private key must never enter this table.** `ProfileEntity` is inside the backed-up database
  file, so only the public key may be persisted here.
