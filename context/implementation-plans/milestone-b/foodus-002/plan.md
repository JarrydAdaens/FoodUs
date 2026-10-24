# FOODUS-002: External databases in main Settings

Story status owner: [Milestone B](../../../milestones/milestone-b.md#foodus-002).
Run: [milestone-b-sequential](../../../_boss-runs/milestone-b-sequential/briefing.md).

## Scope and decisions

Move the existing External databases row out of Database settings and into main
Settings, immediately above Data backup and export. Preserve its cloud-download
icon, translated label, and translated description. Follow the main Settings
row shape and colors. Its callback navigates to the existing typed
`ExternalDatabases` destination in `FoodYouAppNavHost`; Back still pops that
destination and now returns directly to main Settings. The existing OFF/USDA
configuration, Swiss/AFCD destinations, imports, export, and backup remain intact.
No database-management redesign or preference migration is required.

Worker owns the affected screens, callback wiring, Android regression, and this
plan. Coordinator owns milestone status, acceptance evidence, completion receipt,
Samsung operations, JDK 21 builds/tests, and the authorized per-Story commit.

## Acceptance and checks

- Main Settings has a directly accessible External databases row.
- Database settings removes that row and retains import CSV, export CSV, and backup.
- External databases and Database each return to Settings through Back.
- Existing source configuration and source-management callbacks continue to work.
- Android regression renders the production Settings, Database, and External
  databases screens in a minimal synthetic NavHost. Isolated in-memory preferences
  verify OFF toggle persistence across Back/re-entry and retained USDA/AFCD choices.
  Swiss/AFCD and import/export/backup callbacks are intercepted and counted. No real
  credentials, diary records, device preferences, imports, exports, or backups are
  changed. The existing DeveloperActivity temporarily hosts the fixture; its window
  stays awake during the scenario and private Koin is closed after destruction.
- Actual app-graph callback placement requires coordinator diff inspection: the
  synthetic host intentionally does not instantiate the production home screen or
  all its repositories. No new production test seam, dependency, or manifest entry.
- Run whitespace checks, JDK 21 debug/instrumentation builds, then targeted
  `com.maksimowiczm.foodyou.app.ui.settings.ExternalDatabasesNavigationTest` on Samsung.

## Execution evidence / continuation

2026-10-24: inspected AGENTS, laws, harness, owning Story, existing Settings rows,
Database settings, typed navigation, external source configuration, and the existing
isolated onboarding instrumentation pattern. Harness has no installed modules.
The implementation moves the row and callback without changing external source
configuration or Database's remaining action callbacks. Regression uses platform
UiAutomation and localized resource text, with bounded waits and forward scrolling;
Back/app-bar lookups never scroll. The fixture asserts the global application Koin
instance survives cleanup. No sensitive screen contents are dumped.

Worker `git diff --check` passes. Gradle and phone checks are deliberately delegated
to the coordinator to avoid concurrent builds/device operation. Acceptance remains
pending until those results and production callback inspection are recorded here.

Initial JDK21 offline debug and test APK builds passed. First Samsung run stopped
on missing `TranslationRepository` in the isolated fixture, required by the main
Settings language row. Added an inert in-memory language stub; no production
changes were needed. Rerun pending. Full unit suite is not repeated for this UI
callback relocation; the automated device regression targets the changed behavior.

## Final acceptance and delivery

2026-10-24: JDK21 `:app:assembleDebug :app:assembleDebugAndroidTest` passed offline.
After the fixture repair, direct `adb shell am instrument -w -r -e class
com.maksimowiczm.foodyou.app.ui.settings.ExternalDatabasesNavigationTest
io.github.jarrydadaens.foodus.dev.test/androidx.test.runner.AndroidJUnitRunner`
passed on Samsung SM-A057F / Android15: `OK (1 test)`, 9.24 seconds. APKs were
over-installed with `install -r -t`; no data clear/uninstall was used. Device output
is retained locally in ignored `app/build/foodus-002-device.txt`.

Verified direct main Settings entry, production External databases screen, synthetic
provider choice retention, Swiss/AFCD callbacks, coherent Back, removed Database
duplicate, and retained import/export/backup callbacks. Coordinator inspected the
actual typed app graph: the same ExternalDatabases destination callback moved from
DatabaseSettings to Settings. Its provider configuration/destination implementation
is unchanged. File pickers and actual import/export/backup were deliberately not
invoked by the test; only their retained UI callbacks changed context. Whitespace
checks passed. Existing compiler and SDK XML warnings remain. No optional independent
review was selected for this bounded relocation.

Acceptance complete; no implementation step remains. Milestone owns closure and
the sealed [receipt](../../../_boss-runs/milestone-b-sequential/foodus-002-receipt.json)
owns delivery evidence pending reconciliation with the authorized commit. Legacy
catalog version is retained under the release-transition exception; no release.
