# FOODUS-001: AFCD in onboarding

Story status owner: [Milestone B](../../../milestones/milestone-b.md#foodus-001).
Run: [milestone-b-sequential](../../../_boss-runs/milestone-b-sequential/briefing.md).

## Scope and decisions

Offer an AFCD card in the first-run food database selector. Its navigation opens the
existing `AustralianFoodCompositionDatabaseScreen`, including its existing view model,
configuration, import, update, error, attribution, and preference persistence behavior.
Back returns to the selector with its remembered OFF, USDA, and Swiss choices intact.
The onboarding completion update already copies only OFF/USDA preferences and therefore
preserves the AFCD preference written by its management flow. No new import path or
second AFCD selection state is needed. Existing Agree and Skip callbacks remain intact.

Worker owns onboarding sources, their regression tests, and this plan. Coordinator owns
milestone closure, device evidence, completion receipts, and the authorized commit.
Preserve unrelated changes; do not pull, push, commit, or operate the phone as worker.

## Acceptance and checks

- AFCD appears beside existing first-run choices and opens the existing management UI.
- Back returns to the selector; its existing source choices and Skip remain available.
- Complete AFCD import and verify usable source after onboarding; observe failure
  behavior through the same screen used after onboarding.
- Automated Android navigation regression uses the existing DeveloperActivity as a
  temporary UI host, replacing only its content. An isolated composition-scoped Koin
  context supplies in-memory preferences/metadata, dummy OFF credentials, and fake
  AFCD/Swiss use cases. It exercises onboarding completion against those fakes only;
  no real app preferences, credentials, database, or diary are written or cleared.
- Run debug assemble and unit tests using JDK 21; compile instrumentation tests.
- Coordinator runs the targeted instrumentation and setup success/failure checks on
  the attached Samsung before Story closure and commit. Device data must be preserved.

## Execution evidence / continuation

2026-10-24: inspected AGENTS, laws, workflow, harness, Story acceptance, onboarding,
AFCD management, and preferences. Harness has no installed modules. Implementation
adds one card callback and one typed onboarding destination, reusing AFCD management.
`OnboardingAfcdTest` now covers successful setup and OFF/USDA/English selection retention
through Back/Agree, plus visible synthetic failure, retry, and AFCD preservation through
Skip. Fake successful import writes an installed metadata marker and emits the actual
AFCD progress contract; the production AFCD view model enables the source on success.
No real AFCD network/parser/database import is claimed by this test.

Tests use the platform accessibility tree, matching localized label text (including
lines inside merged text) or content descriptions, then clicking a clickable ancestor.
Missing nodes trigger forward scrolling of the current accessible scroll container;
polling has a 15-second bound. App-bar labels are searched without scrolling. Scenario
closure disposes the replacement UI before explicitly closing its private Koin application.
No global Koin override or separate activity/manifest/dependency is needed. An unlocked Samsung
with accessibility UI available is required. Selection persistence is asserted on the
in-memory preference and Swiss import boundaries after actual completion.

`git diff --check` passes for worker changes. Coordinator's baseline build was still
running when worker returned; coordinator owns subsequent debug/instrumentation builds,
targeted Samsung execution, any runtime fixes, acceptance recording, and commit. Next:
build `:app:assembleDebug :app:assembleDebugAndroidTest`, then run only
`com.maksimowiczm.foodyou.app.ui.onboarding.OnboardingAfcdTest` on the attached Samsung.
Do not close the Story until coordinator records passing device evidence.

Initial Samsung execution compiled but failed and crashed during DeveloperActivity
destruction with `KoinApplication has not been started` from the app's global context.
The initial Compose `KoinApplication` helper did not provide the required isolation.
Changed approach after inspecting the locally cached Koin 4.1.1 class signatures and
bytecode (no cached source archive): `KoinIsolatedContext` accepts an existing private
`KoinApplication` and provides its Koin/root scope through composition locals. The
test now creates that private container through `org.koin.dsl.koinApplication`, uses
`KoinIsolatedContext(context = isolated)`, and explicitly closes it after scenario
destruction. A final identity assertion checks that the application's global Koin
instance remains the original instance. No production sources changed for this repair.
Coordinator owns recompilation and Samsung rerun; this repair has not yet passed them.
Timeouts now name the exact expected localized/synthetic UI label or AFCD state and
whether forward scrolling was enabled. They never dump actual screen contents.

## Final acceptance and delivery

2026-10-24: `:app:testDebugUnitTest` passed all 120 tests (24 suites, zero failures).
`:app:assembleDebug` built the production change. Final corrected
`:app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.maksimowiczm.foodyou.app.ui.onboarding.OnboardingAfcdTest`
passed 2/2 tests on Samsung Galaxy A05s (SM-A057F), Android 15, physical 1080x2400.
The host keeps its screen on only for the test lifetime. Global Koin identity is
preserved after both tests. `git diff --check` passed. JDK 21 was used; existing
experimental compiler/SDK XML warnings remain. No paid AI, real AFCD download, or
persisted device settings/diary mutation was used by these tests. Real provider
parsing/import remains the unchanged Story 2.15 implementation; this Story verifies
onboarding reaches that exact management screen and its state/completion boundaries.

Acceptance complete. No remaining implementation step. Milestone B owns closure.
The shared catalog remains at its temporary legacy version under the policy's
release transition; this closure does not authorize a release or versionCode change.
Completion is provisional until the sealed [receipt](../../../_boss-runs/milestone-b-sequential/foodus-001-receipt.json)
reconciles with the authorized per-Story commit. The receipt binds checked code,
test, and build configuration identity to these observations.
