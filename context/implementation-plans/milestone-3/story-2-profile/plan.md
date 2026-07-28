# Plan: Profile

## Metadata

- Task Type: `STORY`
- Status: `Ready`
- Owner: Jarryd Adaens
- Last Updated: 28 July 2026

## Linked Context

- Milestone: [context/milestones/milestone-3.md](../../../milestones/milestone-3.md)
- Story: [Story 2: Profile](../../../milestones/milestone-3.md#story-2) `[STORY 3.2]`
- Dictation source: [2026-07-27 Milestone 3 multiplayer addendum](../../../dictations-tier-0/2026-07-27_milestone-3_multiplayer-addendum.md)
- Design authority: `context/design.md` — "The Multiplayer Exception" (profile metadata is the only social data the relay ever sees; the GUID is the primary key for everything social)
- Related Plans:
  - Story 3.1 (tabbed UI shell) — **depends on it**: the My Profile card renders at the top of the Groups tab that Story 3.1 creates. This plan adds the card into that tab; it does not re-plan the shell.
  - Story 3.3 (crypto identity) — **depends on this story**: the key pair is generated alongside the GUID at profile creation. This plan leaves an explicit creation-time integration seam; Story 3.3 owns all key work and the public-key column.
  - Story 3.6 (friend codes) — **depends on this story**: the friend code is a field of the profile surface. This plan reserves display space on the card only; Story 3.6 owns the column, generation, and regenerate flow.
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
- Creation-time seam for Story 3.3: profile creation flows through a single domain use case (`CreateProfileUseCase`) so key-pair generation can be attached there later without reshaping callers.
- My Profile card at the top of the Groups tab (Story 3.1's `GroupsScreen`): empty state prompting creation, filled state showing username and created/last-edited dates, with a reserved friend-code area left to Story 3.6.
- Create/edit UI (dialog or small screen — see Questions) + `ProfileViewModel`, Koin-wired per the `aiSettingsModule()` pattern.
- New English strings in `shared/resources/src/commonMain/composeResources/values/strings.xml` (additive fork-owned keys).
- Targeted tests: create-profile use case (GUID/timestamps set), rename (GUID stable, lastEdited bumped), single-profile invariant.

### Out Of Scope

- Friend codes: column, generation, regeneration (Story 3.6).
- Key pair, Keystore, public-key storage on the profile record (Story 3.3).
- Any relay/network calls — the profile is local-only in this story; registration with the relay is Story 3.3's contract-gated seam.
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

- Q: `[STORY 3.2]` Should social features share one slice or get one slice each — i.e. does this story root `com.maksimowiczm.foodyou.profile`, or a broader `social` slice that Stories 3.6/3.7/3.9 grow into?
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

- Q: `[STORY 3.2]` Should this migration pre-create nullable `friendCode`/`publicKey` columns to spare Stories 3.6/3.3 their own schema bumps?
  Impact: One migration vs three; but pre-creating columns plans other stories' storage for them.
  Assumption: No — each story owns its additive migration, keeping every changed line traceable to its own story (laws §7).
  Status: OPEN
  Answer: —

## Execution Steps

1. Create the `profile` slice domain layer.
   - Why: Domain-first keeps the GUID invariant and creation seam in pure Kotlin, testable without Room.
   - Edits: New `profile/domain/Profile.kt` (id: String GUID, username, createdEpochSeconds, lastEditedEpochSeconds), `profile/domain/ProfileRepository.kt` (observe/create/rename), `profile/domain/CreateProfileUseCase.kt` — the single entry point that generates the GUID + timestamps and is the documented Story 3.3 hook (key-pair generation attaches here).
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
   - Edits: New `app/ui/groups/profile/MyProfileCard.kt` (empty state → create; filled state → username, created/last-edited dates, reserved friend-code area with a placeholder comment pointing at Story 3.6), `ProfileEditDialog.kt` (per assumption), `ProfileViewModel.kt`, `Module.profileUiModule()` mirroring `aiSettingsModule()`; slot the card at the top of Story 3.1's Groups tab composable.
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
