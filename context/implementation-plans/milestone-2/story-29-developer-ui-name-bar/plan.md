---
name: story-29-developer-ui-name-bar
description: Release-available UI identification and source-oriented hierarchy copying for agent collaboration.
metadata:
  version: "1.0"
  owner: "Jarryd Adaens"
  repo: "FoodUs (fork of maksimowiczm/FoodYou)"
---
# Plan: Developer UI name bar for agent collaboration

Status: Complete — implementation, technical checks, independent minified copied-output lookup,
Story/version reconciliation and final debug APK metadata verification pass. See the final
acceptance checkpoint for exact composite evidence and runtime limits.
Authority: Owner's 2026-10-26 pairing discussion, request to add this capability as one
Story, explicit rails-planning request, and subsequent instruction to instantiate a sub-agent
to implement the completed plan. Implementation is now authorized.
Status owner: [Milestone 2 Story 29](../../../milestones/milestone-2.md#story-29).
This plan owns implementation choices, evidence, unresolved questions, and continuation.
Related: [Design observability](../../../design.md#observability),
[workflow](../../../agenticworkflow.md), [laws](../../../laws.md), and
[versioning policy](../../../versioning-policy.md).
Commit policy: the owner explicitly requested `commit-log --commit` after implementation
completed on 2026-10-26. Commit the Story 29 changes only; never pull or push.
No publication or owner-device operation is authorized.

## Outcome and scope

Let the owner use a shipped FoodUs build to identify a UI element and paste a precise,
source-oriented locator into an agent conversation. Given the copied output and repository,
an agent must find the implementing code and distinguish repeated instances.

Accepted requirements:

- A developer setting enables the top UI name bar; default off; available in release builds.
- Most bar width is a selectable name field; Copy is on the right. Include hierarchy adds
  detailed structural and source metadata to the copied text. Reserve layout space.
- Holding a visible element for about three seconds selects it, populates the name, and
  gives clear selection feedback. Ordinary taps, scrolling, and text entry stay usable.
  Owner decision during implementation: **holds inspect while enabled; disabling restores
  normal long presses**. Enabled mode suppresses ordinary long-press actions. This supersedes
  the original simultaneous-coexistence requirement and its open product decision below.
- Identify text, icons, controls, meaningful containers, repeated items, and modal surfaces.
  Perform the app-wide naming pass. Stable searchable source names, fully qualified symbols,
  ownership, and source paths matter more than compactness or polished prose.
- Structured verbose XML is suitable. Do not simplify away useful code details. Credentials,
  relay addresses, diary contents, and entered values are excluded from structural export.
- Shared skills or tooling are conditional on actual need. The output must work when pasted
  into an ordinary agent conversation without an installed interpreter skill.

Android is the shipping acceptance target; keep portable logic/UI in commonMain and any
window-specific bridge in androidMain. No iOS runtime acceptance is claimed. No network,
accessibility-service permission, account, relay, database schema change, framework upgrade,
compiler plugin, or external inspection dependency is selected. The next Story is not yet
defined; do not invent its scope or create a second Story for this feature's internal steps.

## Current understanding and inspected seams

Paths in this section are relative to `app/src/commonMain/kotlin/com/maksimowiczm/foodyou/`
unless qualified. Observations are from 2026-10-26, not runtime acceptance.

| Source | Observation and implication |
| --- | --- |
| `app/ui/FoodYouApp.kt`, `app/ui/AppViewModel.kt` | Settings Flow feeds composition providers; root includes onboarding and startup dialogs as well as the main shell. Hosting only inside the diary would miss these surfaces. |
| `app/ui/shell/FoodUsAppShell.kt` | Three tabs, independent saved state, nested screen Scaffolds, and bottom-bar padding. Adding a top host must preserve navigation state and handle insets once. |
| `app/navigation/FoodYouAppNavHost.kt` | Typed Settings routes are local to this graph. DiarySlot and Goals card long presses navigate to personalization screens. A pre-existing action can remove the pressed target before three seconds. |
| `app/ui/home/shared/FoodYouHomeCard.kt` | Uses `combinedClickable(onLongClick, onClick)`. It is a concrete first coexistence test, not a hypothetical conflict. |
| `app/ui/settings/SettingsScreen.kt`, `app/ui/common/component/SettingsListItem.kt`, `app/ui/UiModule.kt` | Existing Settings entry and Koin/ViewModel conventions provide the developer-setting seam. Use a focused Developer tools entry and screen if needed, not the backup Activity. |
| `settings/domain/entity/Settings.kt`, `settings/infrastructure/DataStoreSettingsRepository.kt` | Central preferences model and explicit mapping/read defaults/write keys. Extend using default-off preferences and atomic updates; preserve unrelated settings and adapt constructor-based test fakes. |
| `common/compose/utility/ClipboardManager.kt` | Existing injected clipboard interface accepts label and text; reuse for real copy and fake-clipboard assertions. |
| `app/ui/personalization/PersonalizeNutritionFactsScreen.kt`, `app/ui/meal/DiarySlotSettingsScreen.kt` | Explicit `clearAndSetSemantics` exists. Accessibility/test semantics alone cannot be assumed to preserve every inspector target. |
| `app/ui/food/meals/MealsScreen.kt`, captured editors, import UI | Some existing test tags include indices or database IDs. Preserve tests; do not blindly export arbitrary tags or treat them as complete source metadata. |
| `app/ui/home/goals/MacroPieChart.kt`, `app/ui/common/component/InteractiveLogo.kt` | Custom Canvas surfaces need declared selectable identities; drawing commands are not automatically separate UI nodes. |
| Android `FoodYouAbstractActivity.kt` and manifest | Shared clipboard/config providers cover several Activities. Main, backup (`DeveloperActivity`), share-product, and crash-report Activities have separate content roots. The historically named DeveloperActivity is database backup, not developer settings. |
| `gradle/libs.versions.toml`, `app/build.gradle.kts`, `app/proguard-rules.pro` | Compose Multiplatform 1.10.1, Kotlin 2.3.10. Release and miniDevRelease minify; devRelease does not. miniDevRelease inherits debug signing and the release application identity: use an isolated emulator, never assume it installs beside the household app. |
| `app/build.gradle.kts`, existing instrumented tests | commonTest has kotlin-test; Android instrumentation uses AndroidX/JUnit and existing real-screen drivers. No Compose UI test-rule dependency is currently declared. Reuse existing drivers where adequate. |

The common `app/ui` tree has 314 Kotlin files; a direct dialog/popup/bottom-sheet constructor
search matched 39 files. These counts include helpers/ViewModels and omit indirect wrappers;
they size the survey only and are not an element inventory or proof of coverage.
The initial planning survey found no installed modules. Implementation added and registered
`harness/ui-name-coverage`; its mandatory trigger now appears in AGENTS.md §0.2.

Primary framework references checked during planning:
[gesture propagation](https://developer.android.com/develop/ui/compose/touch-input/pointer-input/understand-gestures)
describes observation/interception passes; it does not establish this feature's coexistence.
[Compose semantics](https://developer.android.com/develop/ui/compose/accessibility/semantics)
describes merging and nodes without semantics. The proposed explicit metadata below is a
project design choice, not an automatic capability promised by those references.

## Proposed implementation contract

### Explicit metadata and selection ownership

The feature lives under `app/ui/developer/inspection/`. Named modifiers and component/screen
scopes register descriptors and layout bounds only while enabled. Descriptor validation and
snapshot/XML serialization are independent of layout objects and covered by deterministic unit
tests. The initial proposal to isolate all gesture resolution from Compose evolved during the
real-control spike: native pointer hit paths, clipping, window identity and lifecycle are resolved
through supported Compose APIs and verified with instrumented real-event tests. Do not claim
pure gesture-core unit coverage. The modifier supports nonclickable and disabled controls without
changing their accessibility role or making them actionable.

Descriptors carry a literal stable identifier, fully qualified composable symbol, application
owning symbol, repository-relative source file, and optional call-site owner/file for reused
components. Keep descriptor declarations close to their source or in a small feature-local
catalogue; no blanket stack capture, reflection, Layout Inspector, or runtime class-name
inference. The literal metadata must survive optimization without disabling minification.
Existing upstream namespaces remain intact. Validate paths/symbols against the actual source.

Runtime registration handles, parent handles, surface handles, bounds, and visible occurrence
are ephemeral. They are not database identities. Use explicit parent scopes so separate root
surfaces can retain a logical screen ancestry. Detach/disposal must unregister nodes and
cancel pending holds. Disabled mode must not retain a live registry or pointer timers.

Resolve the deepest visible declared target under the point on the topmost active surface,
respecting clipping and overlays. A child icon/text can be selected separately and include
its enclosing button in ancestry. Reject stale/disposed nodes; don't select covered content
behind a dialog or transparent inspector overlay. For overlapping siblings where metadata
does not establish draw order, prove an ordering solution in step 1 instead of assuming that
smallest area or latest registration is sufficient. Meaningful canvas subregions require
explicit region metadata where they represent separate controls; decorative drawing can
identify its containing named canvas.

### Bar, state, and copied format

Persist only enablement and the Include hierarchy preference using existing Settings/DataStore
patterns. Keep current selection and bounds session-local; clear live targets on disable or
surface/navigation invalidation. A last structural snapshot may remain copyable after a modal
dismisses as described below; disablement clears that too. Do not retain prior captures in
preferences or diagnostics.
The name field is selectable/read-only initially; Copy is disabled until selection exists.
Changing hierarchy mode does not perform a copy automatically. Use existing clipboard feedback.
The bar's field/Copy/checkbox need predictable handling so inspecting the inspector does not
prevent copying; settle and document that behavior in the pilot.

Short copy is the stable identifier displayed by the bar. Detailed copy uses a versioned XML
root, screen/surface identity, all declared ancestors, and the selected element's metadata.
Include source component and application owner separately, instance occurrence within its
parent, and useful structural state such as enabled and bounds when available. No labels,
field text, arbitrary semantics properties, backing object dumps, route arguments containing
data, credentials, or database IDs are automatically copied. XML escaping must handle all
attributes. The ancestry is the declared app/source hierarchy; do not imply that it is the
complete Compose runtime implementation tree. Internal framework nodes are not source owners.

Do not freeze the XML schema before the pilot. Validate representative copied output against
actual repository searches. Repeated-row occurrence is a snapshot locator; the reusable source
identifier remains stable across scrolling, recomposition, and localization.

### Roots, windows, and coverage

Reuse the shared Activity content seam where practical, with the bar inside an appropriate
theme and common inspection context. Main content, onboarding/startup overlays, backup, share,
and crash surfaces need explicit inventory and handling. Register each in-app Dialog/Popup/sheet
surface: a root pointer observer must not be presumed to receive another window's events.
Do not use an always-consuming full-screen overlay. Prove how Copy remains reachable for a
modal selection (for example after dismissing the modal, with a safe metadata snapshot), and
avoid losing the only locator before it can be copied. A copied snapshot can retain structural
descriptors after dismissal without retaining live UI or field data; mark it as a snapshot.

Inventory by screen, route, shared component, and surface. Name application-owned visible
elements, children needed to select icons/text, and meaningful layout ancestors. Framework
internals and system-owned camera/file-picker/keyboard/browser surfaces are outside app-owned
instrumentation; they must not yield a fabricated FoodUs locator. Keep existing test tags and
accessibility descriptions unchanged. No wholesale replacement of Material controls is selected.

Coverage must combine source inventory reconciliation with actual runtime selection checks.
A registry alone cannot discover an entirely uninstrumented element. Record concrete named
sources, exercised destinations/states, and any remaining gaps in this plan during execution.
Add a repeatable missing-name check for the surveyed application-owned call sites and prove
it fails on a deliberately omitted name. Simple text search can flag candidates but cannot
prove Kotlin/Compose coverage; specify its limits. If a project-specific runnable coverage
verifier is added, follow `harness/README-HARNESS.md`'s module contract and register its trigger.
Do not create an unrelated skills/tooling project to achieve initial acceptance.

## Dependencies and delivery steps

These are steps within one primary plan; no separate phase documents are selected. The owner's
later explicit instruction authorized delegated implementation of this completed plan.

| Step | Change and evidence | Prerequisites | Size / uncertainty |
| --- | --- | --- | --- |
| 1. Prove feasibility on real controls | Small implementation experiment on FoodYouHomeCard, nested icon/text, text field, disabled control, scrolling list, Dialog/Popup/sheet. Observe early long-press navigation, pointer cancellation, modal windows, coordinate conversion, overlap, and release metadata retention. Record actual coexistence and modal-copy behavior here. | Implementation authorization; synthetic test host and isolated emulator. | Highest uncertainty. Do not begin broad instrumentation until the approach meets acceptance or the owner selects a required interaction change. |
| 2. Build descriptor and selection core | Explicit metadata/scopes, lifecycle registry, hit resolver, hold state machine, XML export; tests for ancestry, overlap, stale nodes, instances, sensitive-value exclusion, and escaping. | Feasible selection/gesture approach from step 1. | Moderate; correctness depends on real surface/clip ordering. |
| 3. Wire preferences and pilot UI | Settings toggle, host bar, Copy/hierarchy UI, normal layout/insets, startup/restore/disable behavior; name main shell and representative diary/settings targets. Pilot short/full copies with repository lookup. | Core; persisted defaults and existing settings conventions. | Bounded pilot; UI and release paths both required. |
| 4. Name the app | Reconcile full route/component/surface inventory; instrument shared components then call sites, editors, home, settings, onboarding, additional Activities, modal surfaces, and custom drawings. Keep batches buildable and record coverage/gaps here. | Pilot contract stable; root/surface bridge established. | Largest effort; 314 UI files are a survey size, not an estimated number of edits. |
| 5. Accept and reconcile | Run focused plus full checks, actual-screen debug and minified runtime matrix, coverage failure self-check, and agent lookup exercise. Record evidence and residual limits; close Story only after required acceptance. | Previous steps and no unresolved product interaction decision. | Device availability and coverage dominate; no external relay/provider wait. |

Critical path is feasibility → stable metadata/selection core → pilot → coverage → release
acceptance. Settings conventions, XML fixtures, and source inventory can be prepared while
gesture details are investigated. No credible elapsed-time estimate until the pilot measures
instrumentation cost and settles separate-window behavior. Later work remains adjustable.

## Checks and recovery

Use JDK 21 and existing dependencies. Original planning commands (actual results follow below):

```powershell
.\gradlew.bat --no-daemon :app:testDebugUnitTest
.\gradlew.bat --no-daemon :app:assembleDebug :app:assembleDebugAndroidTest
.\gradlew.bat --no-daemon :app:assembleRelease :app:assembleMiniDevRelease
.\gradlew.bat --no-daemon :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.maksimowiczm.foodyou.app.ui.developer.inspection.UiNameInspectorTest
.\gradlew.bat --no-daemon :app:connectedMiniDevReleaseAndroidTest -Pfoodus.testBuildType=miniDevRelease -Pandroid.testInstrumentationRunnerArguments.package=com.maksimowiczm.foodyou.app.ui.developer.inspection
```

The planned test class was added. Targeted core checks were followed by full debug unit tests
because Settings constructors and shared UI are affected.
Use the targeted Android instrumented suite on an isolated emulator. The minified test APK
must actually be installed and exercised; neither `assembleRelease` nor debug instrumentation
proves minified runtime behavior. miniDevRelease is a signed local proxy, followed by a check
of the actual release artifact's packaged descriptor data/configuration. Record exact variant,
source identity, commands, emulator/API, and artifacts. No need for household signing keys or
publication. Use separate emulator state if application IDs/signatures conflict; never uninstall
the owner's app. A missing emulator/build tool makes its acceptance check pending, not passed.

Required test matrix:

- Core: nested/deepest hit; clipped/covered/outside bounds; disabled element; z-order;
  independent surfaces; lazy-row reuse; unregister/re-register; duplicate/cyclic metadata;
  UTF/XML characters; deterministic ancestry; no runtime values in export.
- Gesture: before/at/after three seconds; early release; move past platform touch slop;
  scrolling; extra pointer; cancellation; focus/surface loss; disable mid-hold; action on
  release after selection. Use synthetic callback counters and real app-owned long-press
  routes to detect unwanted click, duplicate action, or premature navigation.
- Bar/settings: absent defaults, persisted toggles, restart/recreation, blank/valid copy,
  hierarchy toggle, narrow width/large text, status bar/IME/insets, tabs and navigation.
- Actual app surfaces: diary cards and children, goals, search/list rows, Product/Recipe/Meal
  editors, nutrition fields, import, AI/relay settings using dummy values, onboarding,
  startup dialog, menus, sheets, backup/share/crash roots. No real network/account calls.
- Coverage: check fails for a deliberately unnamed surveyed element; reconcile all app-owned
  routes/states, not just pilot screenshots. Re-run disabled-mode workflows and existing
  AI-key privacy/secure-screen tests where host changes affect those boundaries.
- Agent lookup: provide only short/full copies and checkout access, excluding selection
  screenshots; recover the exact name declaration, owner, source file, and instance context
  for nested, shared, repeated, modal, and minified-build examples. Record lookup outcomes
  without making successful search evidence stand in for runtime hit correctness.

Recovery: retain existing test tags, package names, navigation, and preference keys. Additive
DataStore keys need no destructive migration. Keep broad naming changes mechanical and
separate from behavior edits so individual batches can be corrected without resetting work.
Disablement removes runtime hooks immediately. Preserve unrelated dirty work; no hard reset,
bulk revert, or discarded diary data. Screenshots/dumps use synthetic FoodUs content only.
Recheck the harness index before implementation; run newly applicable gates if installed.
When Story 29 actually closes, reconcile milestone status and policy-derived development
version; planning alone does not change app version or the closed-Story count.

## Questions and assumptions

1. **Long-press ownership: resolved by owner.** The original simultaneous-coexistence
   proposal cannot preserve navigating callbacks before 3 seconds. Owner explicitly selected
   "Use holds for inspection while enabled; restore normal long presses when disabled."
   Three real-control pilot tests passed, including native restoration and abandoned-hold
   click suppression. Slider measurements retain 0.2 at DOWN/capture/release with zero callbacks;
   a subsequent short tap changes it. No additional continuous-control compromise was needed.
2. **Registry/window API: resolved.** Explicit descriptors, supported layout coordinates and
   native Initial-pass pointer hit paths determine the actual deepest visible target. Per-window
   pointer ownership handles separate modal roots. Snapshot strings/bounds survive modal dismissal,
   then the ordinary Copy control exports them. Actual Dialog/Popup/sheet, overlap/clipping and
   background-target lifecycle regressions cover this choice without internal tree APIs or unsafe
   Activity callback replacement. Corrected same/disjoint-hit-path multi-pointer runtime passes,
   including original-finger-first release, all-up recovery and a subsequent clean hold.
3. **Coverage boundary: resolved and explicit.** App-owned text/icon slots, controls and meaningful
   containers have literal source identities, guarded by the scoped lexical inventory plus compiler
   and representative runtime checks. Framework-generated decoration and individual canvas-drawn
   pixels identify their named owning source component; they are not independently declared source
   elements. Scanner preview identifies its AndroidView/module owner; third-party native internals
   and system windows (IME, platform file picker) are outside the app's declared source tree. The
   inventory is lexical, not a claim every possible dynamic state was manually visited. Actual
   runtime is representative across app routes and synthetic real controls; Android/API36 is the
   tested runtime, and no iOS/device-matrix acceptance is claimed.
4. **Agent skill: unnecessary for this implementation.** A fresh-context evaluator given actual
   raw copies and repository access resolved selected short IDs and production declarations,
   owners, callers and ancestry directly. Synthetic screen-scope fixture names are explicitly
   labeled synthetic; no supplemental skill or dependency is added.

## Evidence and execution record

- 2026-10-26 implementation start: user explicitly authorized delegated implementation.
  Story is In Progress; no commit/push/pull/publication or owner-device operation authorized.
  Code confirms `FoodYouHomeCard` uses `combinedClickable`; callbacks at
  `FoodYouAppNavHost.kt:80/93` navigate away on ordinary long press. Parent obtained the
  owner's explicit choice: "Use holds for inspection while enabled; restore normal long
  presses when disabled." No arm mode or deferred ordinary long-click behavior selected.
- Implementing source descriptors, structural snapshots/XML, nullable session registry,
  Initial-pass hit-path selection, settings persistence, stable root composition slots,
  and a real `FoodYouHomeCard` instrumented pilot. Initial-pass ancestry uses Compose's
  actual hit path, avoiding guessed overlap order from bounding rectangles. Enabled
  inspection supplies a long native long-press timeout; the inspector owns the 3-second
  hold and consumes release after capture. Inspector chrome retains ordinary gestures.
- Local Gradle wrapper access failed at the user-cache ZIP lock in the sandbox. Scoped
  elevated focused build is running with JDK `C:\Java\jdk-21.0.12+8` and SDK
  `D:\dev\AndroidSDK`. Parent verified isolated emulator `emulator-5580`; runtime commands
  must explicitly select it. The attached physical device must never be operated.
- Initial implementation next step (historical): compile and run focused snapshot tests plus real-control pilot on
  `emulator-5580`; prove modal/overlap/cancellation behavior before broad naming. Source
  hierarchy across separate roots and app-wide coverage are not yet complete acceptance.
- First green gesture/window pilot: JDK21
  `:app:connectedDebugAndroidTest '-Pandroid.testInstrumentationRunnerArguments.class=com.maksimowiczm.foodyou.app.ui.developer.inspection.UiNameInspectorTest'`
  with `ANDROID_SERIAL=emulator-5580` passed **3/3** on FoodUsM2DomainMeals/API36,
  BUILD SUCCESSFUL in 1m28s. Proved real FoodYouHomeCard nested selection at 3 seconds,
  original long-click suppression/restoration, short tap and captured-release handling,
  early release/movement/ACTION_CANCEL/disable cancellation, repeated named-row occurrence=2
  through an unnamed wrapper, explicit logical Dialog screen/surface, snapshot after dismissal,
  and actual Copy action into a fake clipboard. This precedes broad naming and minification.
- Pilot failures were recorded rather than treated as passes: first connected command used
  an unquoted PowerShell `-Pandroid.test...` property and selected no task; then fixtures
  reused pre-toggle bounds and omitted UiAutomation's interactive-window service flag.
  Corrected fixtures re-read live bounds, set/restore the flag, and await modal window focus.
- Independent source review identified globally scoped cancellation from unrelated target
  focus/disposal. Corrected focus, pointer cleanup, movement, and release cancellation to
  the candidate's ownership. The then-pending strengthened runtime pilot would replace an unrelated
  background named target during modal hold. Enabled abandoned holds now consume release
  after the original native long-press threshold; genuine short taps and moved gestures
  retain normal behavior. Added regression click counters for a one-second abandoned hold.
- Added `harness/ui-name-coverage`: config-driven lexical Kotlin call survey and self-test.
  The independent evaluator exposed alias/FQN/generic omissions, comment/closed-scope false
  passes, unresolved-symbol surface classification, nested-lambda modifier false positives,
  and missing-root false passes; all have adversarial fixtures. Self-test passes after fixes.
  Parent registered its standing AGENTS trigger. Source inventory is a candidate check,
  not compiler analysis or runtime coverage. Broad pass is a one-time migration preserving
  existing IDs; future sibling additions must not renumber existing static suffixes.
- Parent extended implementation ownership to `shared/barcodescanner/src/**`, because its
  permission/scanner UI is application-owned. Keep that reusable module independent of
  the app using an optional default-identity metadata/modifier adapter supplied by FoodUs.

- Historical planning-only checkpoint, before implementation authorization (2026-10-26):
  read rails-planning and its plan/questions references, repository laws/workflow,
  Story 29, relevant Design sections, harness index, UI/settings/navigation/build sources,
  existing Android test conventions, and primary framework gesture/semantics documentation.
- Found no existing Story 29 plan. Created this primary record and linked it from the Story
  Index and detailed Story section. Story was then Not Started; that planning request alone
  did not authorize or perform source changes/runtime experiments. The later explicit
  implementation instruction above superseded that status.
- Pre-existing changes: earlier conversation's `context/design.md` and
  `context/milestones/milestone-2.md`; untracked `.codex-remote-attachments/`. Preserve them.
- Planning verification: `git diff --check` passed. Local PowerShell checks passed for every
  relative plan link, both Story-to-plan references, required plan sections, one Story 29
  plan, and the unchanged 27 closed Stories out of 29 indexed Stories. No Gradle or device
  checks were run for this documentation-only change; runtime acceptance remains pending.
- Historical planning next action (superseded by implementation checkpoint below): reload this plan and current Git state,
  inventory source surfaces, then execute step 1's bounded gesture/window/release proof
  before committing to broad instrumentation. No extra permission ritual is required once
  implementation is authorized; only a demonstrated product conflict needs a new decision.

## Implementation checkpoints (2026-10-26, chronological history)

- Full broad debug command `:app:testDebugUnitTest :app:assembleDebug :app:assembleDebugAndroidTest`
  passed in **2m39s**, with **208 unit tests / 39 suites**, zero failures/errors/skips.
  JDK 21.0.12+8; SDK `D:\dev\AndroidSDK`. Existing SDK XML/compiler opt-in/deprecation
  warnings and the pre-existing MealsScreen always-true warning remain; no new compile error.
- Strengthened pre-pass runtime pilot passed **3/3**, BUILD SUCCESSFUL **3m13s**, on isolated
  `emulator-5580`, FoodUsM2DomainMeals/API36. Includes target-owned cancellation during background
  replacement under a held Dialog and release suppression for an abandoned one-second hold.
- Source names are literal, colocated with call sites, with per-file package/path factories.
  Shared scanner receives an optional default-identity inspection bridge and app adapter;
  it has no dependency back to app. Explicit screen/modal scopes cross window boundaries.
  Native window roots have named content wrappers/modifiers, including blank modal regions.
- Selection captures the actual named Initial-pass pointer path, preserving caller/component
  modifiers on the same layout. Shared selected descriptors gain matching call-site metadata.
  Screen identity uses the scoped composable (the owner can be a navigation caller).
  Colliding initial short names were given stable package/feature prefixes. Future edits
  must preserve existing IDs rather than rerunning ordinal numbering.
- Final source inventory/self-test currently pass: **0 unnamed surveyed call sites and
  0 duplicate source IDs** across 348 Kotlin files. The exact latest count is in the module's
  ignored timestamped JSON. Parser fixtures cover aliases/FQ/generics, comments/closed scopes,
  missing roots, modifier nested lambdas, generic/receiver declarations, private functions,
  local data-type shadowing, overload imports, and string-token boundaries. Kotlin compile
  and runtime checks remain distinct gates; a lexical PASS alone cannot close this Story.
- Broad migration compile failures were corrected before any acceptance claim: receiver
  declarations, a route object, string-token insertion, and a private pure-ViewModel type
  collision. Restored SponsorViewModel's original source entirely; corrected generic helper
  owners and overloaded Material/component symbols. Independent source review rechecked fixes.
- Current expanded runtime classes: `UiNameInspectorTest` (3 pilot tests),
  `UiNameInspectorSurfacesTest` (actual Settings toggle/back-stack/draft + shared ArrowBack dump,
  TextField/disabled control, Popup/sheet, scrolling/extra pointer, measured Slider behavior),
  `UiNameInspectorPreferencesTest` (real isolated DataStore reopen), and
  `UiNameInspectorMainAppTest` (actual MainActivity onboarding/main Settings, source-only
  XML and safe synthetic-screen screenshots). No physical device, diary export, provider
  request, private relay/AI value, commit, pull, push, or publication is part of these checks.
- **Then-next action (historical):** compile/freeze the latest expanded test sources, run the inspection package
  on `emulator-5580`, resolve actual failures, then run `AiSettingsKeyPrivacyTest`,
  `SecureActivityTest`, and a relevant Product editor smoke. Run minified `miniDevRelease`
  through the opt-in `-Pfoodus.testBuildType=miniDevRelease` test seam on the same isolated
  emulator, plus actual `assembleRelease`. Capture exact variant/artifact identity and source
  XML; give representative production/minified copies to the independent agent for lookup.
  Inspect synthetic screenshots/layout and reconcile coverage inventory/runtime destinations.
  Story remains In Progress. Parent owns milestone/design/version reconciliation only after
  every required acceptance gate passes; no version or closed count changed for this checkpoint.

### Runtime continuation (2026-10-26)

- `runs/20261025T221428Z-runtime-debug`: separate privacy/security/Product workflow phase
  passed **7/7**, BUILD SUCCESSFUL **2m56s** (3 AI-key privacy, 3 existing Activity secure-flag,
  1 full Product create/edit/search/select/log/edit/cancel flow). The existing secure tests'
  asynchronous Kotlin assertions were subsequently identified as weak evidence; deterministic
  JUnit/polling assertions in both inspector modes are being added before final acceptance.
- Expanded inspection package includes common serializer tests: **17 total = 11 Android
  methods + 6 shared serializer methods**. `runs/20261025T222118Z-inspection-debug` completed
  **15 passed / 2 failed**, no skips, in **3m6s**. Actual MainActivity onboarding/Settings,
  isolated DataStore reopen, actual Settings switch/back-stack/draft/shared ArrowBack ancestry,
  scanner-denied UI, Popup/sheet, Slider and strengthened pilot/serializer assertions passed.
  Failures were fixture setup: single-pointer SOURCE_UNKNOWN versus multi-pointer touchscreen
  reset the stream; an unfocused TextField rejected accessibility selection setup. Fixtures now
  use consistent finger/touchscreen events and a real short tap before asserting collapsed caret.
- Actual standard-size onboarding and Settings PNGs from that run were inspected by implementer
  and coordinator: reserved bar, field/Copy/checkbox, status insets, content and bottom navigation
  fit; selected title/icon outlines match the held targets. These prove standard debug layout,
  not yet large-font or minified layout. No field-value/diary screenshot was captured.
- Exact structural XML is now written from actual clipboard output to fixed shell-owned evidence
  files, rather than relying only on instrumentation status (Gradle suppresses those messages).
  Verify pulled files are nonempty before claiming agent lookup. A synthetic Slider probe writes
  measured DOWN/capture/release values and callback counts; its outcome must be reported.
- Final bounded source refinements: outline expires after 1.5 seconds independently of the
  retained snapshot; actual native hit-path overlap/z-order/clipping regression; local 320dp,
  1.8-font bar bounds/hold/Copy regression; deterministic secure-flag checks with enabled and
  disabled inspector; static-title route inspection for Goals, AI/relay, import and backup.
  Existing Recipe/Meal workflow methods will provide disabled-mode editor/menu regressions.
- **Active command:** `runs/20261025T222704Z-final-debug` runs full debug unit tests,
  `assembleDebug`, and the then-frozen **18** inspection methods (overlap added), followed by
  `assembleRelease` only on success. Narrow-font/route/security test-only additions belong to
  the next test APK and require their own runtime pass. Do not treat this active run as proving
  later fixture changes. Minified actual runtime and independent raw-output lookup remain pending.
- `20261025T222704Z-final-debug` completed: full unit suite again **208/39, zero failures,
  errors or skips**; Android **17/18 passed** in **5m52s**. Typed extra-pointer cancellation,
  real TextField caret/hold, overlap/clipping and brief-outline expiry all passed. Only the
  disabled-control accessibility assertion targeted enabled child Text instead of its disabled
  Button; now it targets the actual control. The selected descriptor's enabled=false and zero
  click callbacks already passed. `assembleRelease` was correctly skipped after the failure.
  Quoted `sh -c` evidence sinks created no files on this runtime; replaced with
  `executeShellCommandRw("tee <fixed shell-owned path>")`, close stdin and assert exact echoed
  payload. These test-only evidence helpers require API31; current runtime is API36.
- **New active command:** frozen-source **24** debug checks: 19 inspection package methods
  (13 Android + 6 shared serializer), 3 deterministic secure-window methods, 1 Recipe workflow,
  1 Meals workflow. Capture actual clipboard XML for production/shared/repeated/modal examples,
  inspect the numeric Slider probe, and build unsigned release only after the consolidated pass.
  Then execute minified inspection runtime and fresh independent locator lookup. This is the
  authoritative continuation over the earlier active-command paragraph.
- `runs/20261025T223615Z-verified-debug`: **24/24 passed**, BUILD SUCCESSFUL **7m14s**.
  This includes 19 inspection methods, robust secure-window assertions (prior to adding the
  explicit rendered-bar wait), the two full Recipe/Meals workflows, 320dp/1.8-font bounds and
  hold/Copy checks, and actual static-title copies from Goals, AI/relay, import and backup.
  Actual clipboard XML files are now nonempty and retrievable via the verified stdin/tee sink.
  Slider values remained **0.2 at DOWN/capture/release, callbacks 0/0/0**; subsequent normal tap
  changed it. No Slider interaction compromise is necessary. Source/visibility/native review
  found no hidden-alpha blocker or additional app-defined native scanner controls.
- A later independent review identified a real remaining defect not covered by that APK:
  separate sibling hit paths can each see one finger, allowing sibling B to restart inspection
  while finger A remains down. Core now merges active PointerId ownership across hit paths
  within each window and blocks inspection until all fingers lift. Disposal removes only that
  target's contributions; focus resets only the corresponding active window. Added a real
  disjoint-sibling two-finger regression, including one remaining held finger and a clean fresh
  single hold afterward. Current source is frozen; **25 debug** and **20 minified inspection**
  tests are the next required runtime gates. The earlier24 PASS does not prove this correction.
- Unsigned release assembly launched automatically after24 passed and uses the now-frozen new
  pointer core. Assembly is still running; actual minified UI execution remains pending.
  Coordinator started a fresh-context locator evaluator with raw copied XML and repository
  access only, excluding screenshots/plan/report context.
- First actual `assembleRelease` failed in **3m4s**: scanner library is itself minified before
  the app, and its existing export rules preserved only the original scanner entrypoint. R8
  therefore removed/renamed the three newly public bridge API classes before app compilation
  consumed them. Added narrowly scoped library rules for `BarcodeUiSource`, `BarcodeUiInspection`
  and the `BarcodeUiInspectionProvider` Kotlin entrypoint. Both library and final app minification
  remain enabled; no blanket inspector/application keep or ignored missing-class rule was added.
- Fresh-context evaluator resolved every selected short ID and production source/owner/caller
  metadata directly from raw copies. Corrected a fixture-only `pilot.list` descriptor from default
  Column to actual LazyColumn for the next sample. `com.example.PilotScreen`/Host are explicitly
  synthetic scoped-identity fixtures, not production callables; element owners/source paths point
  to their real test declaration. Production examples locate actual functions and call sites.
- **Active final-source gate:** full debug unit suite plus **25** emulator methods, including
  disjoint-hit-path extra pointers, all-up recovery, rendered-bar/secure-flag synchronization,
  and regenerated raw copies. Build minified 20-method suite and actual unsigned Release after it passes.
- Final pointer review also caught original-finger-first release on one shared hit path: stopping
  at that finger's UP left the other pointer's final UP inside `awaitEachGesture`'s implicit wait,
  so the inspector could retain a stale pointer. The observer now forwards events until all local
  filtered pointers are up. Same-target reversed release plus clean subsequent hold is part of
  the existing regression method; disjoint-sibling coverage remains separate. Both fixes and
  tests were frozen before Kotlin compilation; SHA256 source evidence is saved with
  `runs/20261025T224909Z-pointer-final-debug`. Independent final source review is **PASS WITH
  NOTES**, explicitly pending actual final debug/minified results. No further source changes
  are planned unless a required check reveals a defect.
- `runs/20261025T224909Z-pointer-final-debug`: full **208/39** unit tests passed again;
  **24/25** Android methods passed in **9m14s**, including every corrected pointer case,
  both release orders/all-up recovery, complete inspection20 and Recipe/Meals regressions.
  Only SecureMain's newly added active-window bar assertion failed: MainActivity also hosts
  separate startup dialog/sheet windows. Fixture now probes only known public startup-label
  categories and temporarily establishes/restores safe startup preferences before assessing
  the actual Activity bar; real JUnit FLAG_SECURE/rendered-mode assertions remain unchanged.
- Final explicit-constructor audit found five remaining direct source slots: three PlainTooltip
  roots and two DatePickerDefaults.DatePickerTitle slots. Named all five, registered the exact
  constructors, and added deliberately unnamed qualified-title/tooltip self-tests. Inventory
  now **348 Kotlin files / 2793 calls / 0 missing / 0 duplicate IDs**, with both self-test and
  independent source recheck PASS. Existing modal test now also holds actual painted persistent
  PlainTooltip padding, verifies its root/source scope, dismisses and copies the retained snapshot.
- **Current active gate:** `runs/20261025T230323Z-release-acceptance` runs full debug units and
  **23** targeted Android methods (inspection20 + robust secure3); the two unchanged editor
  workflows' preceding passes are retained rather than rerun. On success it builds actual
  unsigned Release, then executes minified inspection20 with explicit testBuildType property.
  It saves variant metadata/APK hashes and nonempty raw clipboard XML for independent lookup.
  All final five naming edits and the tooltip fixture were frozen before Kotlin/test compilation.
- That23 run completed **22 passed / 1 failed** in **6m39s**. The real startup probe confirmed
  **MainActivity activeStartup=preview**; all three deterministic secure/rendered-bar tests now
  passed. Only the new tooltip fixture failed before holding: it required window focus from a
  valid nonfocusable tooltip popup. Corrected that precondition to attached/shown/nonempty bounds,
  retaining focus requirements for Popup/sheet and all actual painted-padding hold/copy assertions.
- `runs/20261025T231241Z-minified-acceptance`: targeted **4/4 passed**, BUILD SUCCESSFUL
  **1m48s** (corrected Popup/sheet/tooltip method + secure3). Composite debug acceptance is the
  preceding22 valid methods plus this corrected modal pass and secure rerun; it is not a claimed
  single23 green run. Prior full Recipe/Meals and Product/AI-privacy passes remain valid for
  unchanged disabled workflows. The final208/39 unit suite and2793-call inventory pass.
- Actual unsigned **Release assembled successfully in4m1s**, with both library and app R8 on.
  Artifact `foodus-3_2_37_3-unsigned.apk`, applicationId `io.github.jarrydadaens.foodus`, versionName
  **3.2.37.3**, versionCode **134**, SHA256
  `E6ECE21BB200C05893C5498BED7D70E063F707B1898A4E4FCAE0FE329CD6AC73`.
  Copy/metadata/hash are retained under that minified-acceptance evidence folder. Coordinator
  independently inspected its dex and found format marker, onboarding/shared IDs, full owner
  and repository source path. R8 mapping also renames UiInspectorState to `c9.r` and UiNameModifierKt
  to `c9.z`; inspector classes were not blanket-kept. Existing SDK/Compose mapping warnings remain
  nonfatal. Actual minified 20-method suite runtime is now building/running; no runtime acceptance claimed yet.
- Minified app packaging succeeded, but the independently minified AndroidTest APK initially
  failed on AndroidX Test monitor's missing ErrorProne `CanIgnoreReturnValue` and `MustBeClosed`
  annotations. `javap -v` confirmed their references are RuntimeInvisibleAnnotations on Checks
  methods and Tracer.Span.beginChildSpan, with no executable dependency. Added precisely those
  two `-dontwarn` entries through `defaultConfig.testProguardFiles` in an instrumentation-only
  rules file. Independent review confirmed the exception applies exclusively to the test APK;
  app/library R8 stays enabled and shipping rules/identity/signing remain unchanged. Cached
  minified 20-method suite is being rerun; no acceptance is inferred from packaging alone.
- `runs/20261025T232834Z-mini-test-r8`: test APK R8/package now pass; AndroidJUnitRunner
  crashed before discovery (0 tests) because app R8 merged `androidx.tracing.Trace` into an
  unrelated class while AGP deduplicated that shared dependency from the test APK. Mapping
  and usage evidence confirms the missing executable API. Added a single public-API keep for
  Trace, conditional on the opt-in `miniDevRelease` test target only. Shipping Release rules
  stay unchanged and inspector optimization stays on. Independent review agrees. This failed
  run's pulled fixed-name files were stale debug output and are explicitly marked unusable;
  the next run clears only the thirteen exact shell evidence filenames before execution and
  pulls/validates nonempty fresh files only after success. Synthetic test parity is optional
  if it would require keeping feature APIs; actual minified production runtime remains required.
- `runs/20261025T233324Z-mini-trace`: Trace's boundary was restored, but AndroidJUnitRunner
  still crashed before discovery on optimized/deduplicated `kotlin.LazyKt` in TestDirCalculator.
  Again 0 tests; no fresh locator files were collected. Stop accumulating library/API keeps.
  Removed the Trace keep entirely and selected a platform-only Java Instrumentation runner
  through opt-in `-Pfoodus.platformInspection=true`. Its actual production UI route will use
  Android framework injection/accessibility/clipboard only, avoiding AndroidX/Kotlin/app test
  APIs. Thus the minified app retains the exact shipping R8 policy (existing debug signing is
  the local proxy difference). Coordinator accepted this matrix revision: full debug inspection methods covers
  behavior; actual minified Settings/hold/copy/source/navigation/modal checks plus the unsigned
  Release artifact prove release availability. No synthetic minified 20-method suite pass will be claimed.
- `runs/20261025T234014Z-platform-mini`: restored shipping-policy mini app assembled in
  **1m1s**, SHA256 `39FDE971169892EFB63A3FB11E61ECE7A781F7BAED447DFC239C90924EF350C7`,
  byte-identical to the original mini APK before the temporary Trace experiment. ApplicationId
  `io.github.jarrydadaens.foodus`, versionName **3.2.37.3**, versionCode **134**. Platform-only
  Java driver compiled/packaged in **58s** and launched the real app, completing onboarding
  through Skip without an import/download. Its first attempt failed before toggling because
  it assumed an exact native Switch class/visibility as soon as the final-row label appeared.
  Preserved that failed attempt separately. Refined the fixture to show the complete known
  row, inspect its visible checkable semantics, and report only class/state/numeric bounds.
  Default-off/enabled/disabled assertions remain intact. Driver resets hierarchy through UI
  before disabling. Corrected Java compiles against Android36; actual runtime rerun pending.

## Final acceptance checkpoint (2026-10-26)

Implementation is complete. The coordinator closed Story 29, reconciled the closed-Story count
and policy-derived fork version; no commit, pull, push, publication or physical-device operation
was performed. Fresh-context minified locator lookup passed before that reconciliation.

| Gate | Accepted evidence |
| --- | --- |
| Source coverage | **348 Kotlin files / 2793 surveyed call sites / 0 missing / 0 duplicate IDs**. Self-test deliberately unnamed constructors/scopes and adversarial parser fixtures pass; coordinator independently repeated the gate and normal `git diff --check`. This is a lexical gate, separate from compilation/runtime. |
| Full debug compilation/units | Final broad build and **208 tests / 39 suites**, zero failures/errors/skips, pass. Full unit suite was repeated after the final five tooltip/title-slot naming corrections. |
| Inspection behavior | **20 methods** in the inspection package: 14 Android-specific methods and 6 shared serializer methods. Accepted results are composite: the final pointer run passed all 20 inspection methods; the later five-slot/tooltip run passed 22/23 (only the new tooltip fixture's pre-hold focus requirement failed); corrected real Popup/sheet/tooltip plus three secure-screen checks then passed **4/4 in 1m 48s**. No single green run of 23 methods or minified run of 20 methods is claimed. |
| Real interaction detail | Deepest nested source; same-node caller/component chain; repeated occurrence 2; z-order and clipped child; real field editing/no long-press selection; disabled control; native long-press restoration; abandoned hold release suppression; movement/scroll/cancel/focus/disposal; unrelated background replacement during modal hold; same/disjoint-hit-path extra fingers, both release orders/all-up recovery; outline expiry with retained copy; 320dp/1.8-font bar bounds and actual Copy/hierarchy; real isolated DataStore reopen; actual Settings/NavHost/draft retention pass. Slider retained 0.2 at DOWN/capture/release with zero callbacks; a subsequent short tap changed its value. |
| Privacy/security/editor regressions | Three AI-key privacy checks and one Product workflow pass. Three deterministic secure-screen/rendered-bar checks pass after confirming MainActivity's Preview startup window and establishing/restoring safe startup flags. Real Recipe and Meals workflow methods pass. No account, provider request, relay call, camera capture, diary export or credential inspection occurred. |
| Actual debug routes/roots | MainActivity onboarding/Settings, Goals, AI/relay, import and backup Activity title selections/copies pass. Shared scanner denied-permission UI passes with CAMERA denied and no imaging. All four manifest Activity roots host the bar; share/crash runtime proves rendered bar/FLAG_SECURE behavior and source naming, not their complete workflows or deep-selection routes. |
| Unsigned shipping Release | **BUILD SUCCESSFUL in 4m 1s**, version 3.2.37.3/code 134, `io.github.jarrydadaens.foodus`, SHA256 `E6ECE21BB200C05893C5498BED7D70E063F707B1898A4E4FCAE0FE329CD6AC73`. Coordinator independently verified packaged format marker, short IDs, full owner and source path; real R8 mapping renames inspector classes. |
| Actual minified runtime | **PASS**, platform Instrumentation `foodusInspectionPass=true`, static PASS and `INSTRUMENTATION_CODE: -1`. Test driver assembled in **1m 6s**. Real Settings default-off → enable → 3s Back-icon hold → short Copy → hierarchy Copy/source/caller assertions → ordinary Back/reentry → actual Graphs DropdownMenu hold → dismissal → retained modal Copy → hierarchy reset/off. No AndroidX/Kotlin/app API linkage in the Java runner and no feature/library keep accumulation. |
| Minified artifact/provenance | `runs/20261025T234014Z-platform-mini/mini.apk`: version 3.2.37.3/code 134, base applicationId, SHA256 `39FDE971169892EFB63A3FB11E61ECE7A781F7BAED447DFC239C90924EF350C7`; byte-identical to original mini before the temporary Trace experiment. Same shipping R8 policy, existing local debug signing. Java source SHA256 `7AC6A31F75B4ECEAA973DA17756EE94F6D44562B61B22D55E39AFDCD6F237BA2` and compiled driver APK retained. |
| Fresh minified copies | Exact new-prefix files were removed before execution, written from actual clipboard data and pulled only after explicit PASS. `mini-locators/settings.xml` (4910 bytes), `modal.xml` (3091 bytes), `settings.png` (250742 bytes) under that run. Earlier 0-test run copies are marked stale and excluded. Actual switch diagnostics confirmed checkable `android.view.View`; the initial exact-class fixture assumption was corrected without weakening checked-state assertions. |
| Visual QA | Implementer and coordinator viewed actual debug onboarding/Settings and fresh minified Settings: name/Copy/hierarchy/status insets/content fit; selected Back icon/name correct. Minified PNG contains Android's transient clipboard preview at bottom-left, distinguished from app layout. Narrow/large-font fitting has direct measured runtime proof. |
| Independent agent lookup | Fresh-context evaluator received only two fresh minified XML copies plus checkout access, without screenshots/plan/report context. Both short IDs resolved exact declarations and owners; Settings caller/component/navigation source and private GraphStyle menu owner/source resolved correctly. Earlier nested/shared/repeated/modal/debug copies also passed; synthetic scope fixture names were labeled explicitly. No supplemental interpreter skill was required. |

The complete minified JUnit package could not run because AndroidJUnitRunner itself references
shared dependencies optimized/deduplicated from its test APK. Those were **0-test infrastructure
failures**, not feature passes. The accepted replacement is the real production platform-driven
journey above, while the full debug matrix supplies broad gesture/control coverage. Only the two
confirmed compile-time ErrorProne annotation warnings are suppressed in instrumentation-only
rules. The temporary Trace keep was removed, shipping rules are unchanged, and no inspector API
was preserved solely for testing.

The accepted minified runner is opt-in and does not replace normal AndroidJUnitRunner checks:

```powershell
.\gradlew.bat --no-daemon :app:assembleMiniDevRelease :app:assembleMiniDevReleaseAndroidTest '-Pfoodus.testBuildType=miniDevRelease' '-Pfoodus.platformInspection=true'
# Install both generated APKs on the isolated synthetic emulator, then:
adb -s emulator-5580 shell am instrument -w -r io.github.jarrydadaens.foodus.test/com.maksimowiczm.foodyou.app.ui.developer.inspection.UiInspectionPlatformInstrumentation
```

Require `foodusInspectionPass=true` and `INSTRUMENTATION_CODE: -1`; nonzero/absent/failed results
are not acceptance. Clear only the three exact `foodus-story29-minified-` evidence filenames
before execution, then pull and verify nonempty files after success. Use a fresh isolated English
target with default-off preferences; the runner completes empty-source onboarding via Skip,
restores hierarchy false, and disables the bar through actual UI. It changes synthetic onboarding
state, so this runner must never be used on an owner's populated app or physical device.

Runtime acceptance is **Android/API36 on isolated emulator-5580**, with representative routes
and synthetic controls; it is not an all-device/iOS claim or proof that every dynamic state was
manually visited. Framework-generated decoration/canvas pixels identify the named owner; native
scanner internals and system IME/file-picker/clipboard-preview windows are outside app source
naming. XML exports declared structural metadata, never runtime field/credential/relay/diary
values. Ancestry is declared source scope plus the actual named hit path, not a literal runtime
call stack. Occurrence is runtime instance context, not a stable business identifier. The modal
copy identifies its unique private GraphStyle helper/menu source; its immediate helper call site
is recoverable from source rather than exported directly. Shared serializer coverage is included
in both JVM and Android totals, not extra unique tests. Existing SDK XML/deprecation/Compose
mapping warnings remain nonfatal.

### Closure and final version verification

Coordinator marked Story 29 **Complete** and reconciled Milestone 2 to **28 Complete / 1 Blocked**.
Ten distinct closed backlog items plus those 28 Stories give 38 closed delivery items, so the
fork development version is **3.2.38.3**. Existing externally changed versionCode **134** was
preserved. No release publication or signing-identity change was requested/performed.

Final `:app:assembleDebug` passed in **2m 49s** after that version-only update. Metadata assertions
passed: applicationId `io.github.jarrydadaens.foodus.dev`, versionName **3.2.38.3-dev**, versionCode
**134**, output `app/build/outputs/apk/debug/foodus-dev-3_2_38_3.apk` (97022313 bytes), SHA256
`ECBF18DEBD5F107E3C5D9CD3FF5D5FE6957DF8B06D737CE23CA055B166EC1747`. Log, copied APK, metadata,
hash and implementation changed-file manifest are retained under
`runs/20261025T235642Z-closed-version-debug`. Normal `git diff --check` passes.

The accepted unsigned Release and actual minified runtime artifacts remain exactly version
**3.2.37.3/code 134**, as recorded above. The version-string-only closure update required this
debug metadata assembly; their behavioral/minified suites were not needlessly repeated.

**Next action:** no implementation or acceptance work remains for Story 29. Hand off the finished
change and debug APK to the coordinator. Implementation source is frozen; future changed UI
must follow the registered naming-coverage trigger.
