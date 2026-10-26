# Plan: Separate DiarySlot destinations from saved Meals

Authority: [Story 25](../../../milestones/milestone-2.md#story-25), [Design vocabulary](../../../design.md#domain-model-shared-vocabulary), and the [serial run](../../../_boss-runs/milestone-2-domain-meals-sequential/briefing.md).
Story status is owned by the milestone. Commit policy: none.

## Outcome and scope

Rename existing concepts without changing saved data or logging behavior. This worker owns application source/tests, shared resources, and this plan only. Stories 26/27/24, relay contracts, upstream packages, historical records, versioning and closure metadata are excluded.

## Decisions before code edits

| Current role | Target |
| --- | --- |
| Category `Meal`, repository, DAO/entity, preferences, UI models/settings/pickers | `DiarySlot` family; category parameters become `diarySlotId` |
| `DiaryMeal` containing a category and entries | `DiarySlotEntries`, with `diarySlot` and `entries`: a dated diary projection, not a saved definition |
| `MealTemplate`, items, ID, repository, use cases, current save/apply/delete UI | `Meal`, `MealItem`, `MealId`, and corresponding names; existing bundle actions say Meal/Meals |
| DiaryEntry | Keep the dated snapshot role, with explicit `diarySlotId` |

Category identity remains its existing Long value; reusable `MealId` remains its distinct inline value type. No ID conversion, reassignment or bundle merging.

## Preservation strategy

Keep database version and all schema JSON unchanged. Retain explicit legacy `Meal`, `MealTemplate`, `MealTemplateItem`, `mealId`, `templateId` mappings in Room and legacy migration SQL. Kotlin property renames use ColumnInfo where necessary; navigation serialization uses SerialName for existing keys/type names. Preserve DataStore keys and backup configuration. Snapshot nutrition remains independent of Product/Recipe definitions. Existing pre-refactor APK and planning backups under app/build/m2-domain-meals/baseline remain intact. No global clean.

## Delivery and checks

1. Rename category family first, then reusable Meal family, preserving persistence and serialization identities. Update current UI terminology and relevant DI/routes/state/tests.
2. Extend meaningful domain regressions for independent identities, repeated append, missing/deleted Meal/slot, historical snapshots, and placeholder/Quick Add behavior. Add synthetic Room instrumentation fixture for legacy tables and save/apply/delete, and compare generated schema to baseline.
3. Run JDK 21 `gradlew.bat --no-daemon :app:testDebugUnitTest :app:assembleDebug :app:assembleDebugAndroidTest`, capturing logs under app/build/m2-domain-meals/story25. No physical device operations. Coordinator owns isolated emulator execution and in-place upgrade/backup acceptance.
4. Freeze code, return exact changed files and evidence to coordinator for independent review/runtime acceptance; do not close the Story.

## Evidence and continuation

- Read AGENTS, laws, harness index (no installed modules), Design Domain Model, full Story 25, workflow and run briefing.
- Complexity: broad coupled rename with a small behavioral surface. Risk concentrates in implicit schema names and navigation serialization; explicit legacy mappings and persistence checks are required.
- Implemented category types/APIs/state/DI/routes as DiarySlot, saved-bundle types/APIs/current actions as Meal, and the DiarySlotEntries projection. Existing package directories and localized default-slot JSON paths remain upstream-compatible. Existing translated slot labels retain their translations; the default English labels explicitly distinguish diary slots and saved Meals.
- Retained Room tables `Meal`, `MealTemplate`, `MealTemplateItem`; explicit `ColumnInfo` retains entry `mealId` and saved-item `templateId`. Relation/index/foreign-key SQL and historical migration source remain unchanged. Navigation properties retain serialized `mealId`; renamed setup/personalization routes retain their previous fully qualified serial names. DataStore keys and HomeCard ordinal positions remain unchanged.
- Added domain checks for overlapping numeric slot/bundle IDs, append-on-repeat, deletion preserving historical snapshots, invalid destinations, date/slot filtering and unknown Quick Add nutrition/placeholder snapshot behavior. Added legacy route JSON roundtrip regression. Added Android-only baseline seed/compare fixture and renamed-API persistence/backup/operation fixture covering Product, Recipe, Quick Add, placeholder and saved Meal rows.
- `app/build/m2-domain-meals/story25/gradle-01.log`: initial sandbox attempt stopped before compilation because the Gradle wrapper ZIP lock outside the workspace was inaccessible. Scoped Gradle escalation was coordinated and allowed.
- `gradle-02.log`: `gradlew.bat --no-daemon :app:testDebugUnitTest :app:assembleDebug :app:assembleDebugAndroidTest` succeeded, exit 0, 4 minutes. Existing SDK/toolchain and instrumented-test deprecation warnings remain. Final resource-identifier/import/documentation edits overlapped this first successful build; coordinator owns a final rerun against frozen source and the closure-candidate catalogue. Do not treat this first pass as the final candidate identity.
- `schema-compatibility.json`: schema version 39, identity `b247898737d2ed1a830346124daeb1e8`, full schema JSON equality and all entity CREATE SQL equality before/after generation. No schema or migration change.
- Frozen worker source manifest: `app/build/m2-domain-meals/story25/changed-files.json` (SHA-256 for existing changed files plus original Git blob identities), with readable `changed-files.txt`. These compare the read-only Git index against LF-normalized source and include adds/deletions. The plan is an additional worker-owned file. No commit, staging, device access or Story closure performed.
- Next: coordinator final build, independent review, isolated-emulator baseline seed → in-place candidate upgrade → raw canonical comparison → copied-backup/renamed-API persistence checks → visible configured-slot navigation/save/load/delete acceptance. Required runtime evidence is still pending, not waived.

## Emulator fixture handoff

Target explicitly selected by coordinator: `emulator-5580`; worker never operates a device.
Initialize the preserved baseline APK's `io.github.jarrydadaens.foodus.dev` app through onboarding until `databases/open_source_database.db` exists (version 39). Install the debug test APK only, and invoke the individual raw fixture method; do not run the whole test suite/class automatically.

Test runner component: `io.github.jarrydadaens.foodus.dev.test/androidx.test.runner.AndroidJUnitRunner`.
Use `adb -s emulator-5580 shell am instrument -w -e class <class>#<method> <runner>` with, in this order:

1. `com.maksimowiczm.foodyou.fooddiary.LegacyDiarySlotMealFixtureTest#seedCanonicalDatabase` on baseline. Guards ranchu/goldfish hardware and unused reserved IDs 2501–2599. Seeds slots 2501/2502, saved Meal 2501, Product/Recipe snapshots, Quick Add and placeholder on epoch day 20751. Item IDs deliberately disagree with saved-item display positions. Writes only fixture rows to `files/story25-before.json`.
2. Install the final candidate APK in place, without clearing app data. Run `com.maksimowiczm.foodyou.fooddiary.LegacyDiarySlotMealFixtureTest#verifyCanonicalDatabaseAfterUpgrade`. Compares every fixture column (including nullable nutrients), schema version, assignments and foreign keys, and writes `files/story25-after.json`. The two JSON files must match exactly.
3. Run `com.maksimowiczm.foodyou.fooddiary.DiarySlotMealPersistenceTest#legacyDataBackupAndMealOperationsPreserveIndependentIdentitiesAndSnapshots`. Checkpoints/copies canonical database to fresh `story25-upgrade.db`, reads through renamed Room APIs, tests apply twice/delete/save/slot edit/reorder, and closes/reopens the backup. Requires that the dedicated copy does not already exist; it never resets or removes the canonical fixture. This test changes only the copy.

First-pass artifacts: debug APK `app/build/outputs/apk/debug/foodus-dev-3_2_32_3.apk`; test APK `app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk`. Final candidate paths/version names belong to coordinator. Raw fixture has only Android/JUnit/JSON signatures and imports, so it can run against the pre-rename APK without resolving renamed application classes.

## Review follow-up: encoding repair

Independent review found UTF-8 punctuation decoded through the Windows legacy encoding during the rename. Repaired only the affected sequences, preserving all renamed strings and concurrent edits: five ellipses, the en/em dash resources and microgram unit in default `values/strings.xml`; seven section signs in `FoodYouAppNavHost.kt` comments; and two em dashes in `DiarySlotCard.kt` comments. Explicit UTF-8 filesystem reads/writes were used for this repair. Scanned all 127 existing changed source/resource files for matching UTF-8-to-CP1252 mojibake: zero residual matches. Legitimate Portuguese accented letters were left intact. `encoding-repair.json` records exact before/after hashes and replacement counts. Frozen manifests and this plan's hash were refreshed. No worker build, approval, shell or device operation was performed; coordinator owns resource/build verification against the repaired candidate.
