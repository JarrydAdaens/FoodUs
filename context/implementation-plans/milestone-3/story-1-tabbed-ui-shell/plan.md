# Plan: Tabbed UI Shell

## Metadata

- Task Type: `STORY`
- Status: `Ready`
- Owner: Jarryd Adaens
- Last Updated: 28 July 2026

## Linked Context

- Milestone: [context/milestones/milestone-3.md](../../../milestones/milestone-3.md)
- Story: [Story 1: Tabbed UI shell](../../../milestones/milestone-3.md#story-1) `[STORY 3.1]`
- Dictation source: [2026-07-27 Milestone 3 multiplayer addendum](../../../dictations-tier-0/2026-07-27_milestone-3_multiplayer-addendum.md)
- Design authority: `context/design.md` — fork philosophy (additive overlays, minimal merge surface); Milestone 3 "The Multiplayer Exception" (this story is local-only and relay-independent)
- Related Plans:
  - Story 2 (Profile) — its My Profile card lives at the top of the Groups tab this story creates.
  - Story 13 (Notification Center) — finalizes the Notifications tab; this story lands it as a stub.
- External Tooling: none required.

## CER

- Complexity: 3
- Effort: 3
- Risk: 3
- Notes: Inline estimate. The change is structurally simple — a new fork-owned shell composable wrapping the existing app — and the app already uses Material 3 throughout (`jetbrains-compose-material3` in `gradle/libs.versions.toml:64`), so `NavigationBar`/`NavigationBarItem` are available with no new dependencies. Complexity and risk concentrate in two seams: preserving the Log tab's navigation back stack across tab switches (saveable state), and nesting the existing screens' `Scaffold`s under an outer bottom bar without double insets or IME overlap. Upstream merge surface is deliberately tiny: one call-site line in `FoodYouApp.kt` plus additive string keys.

## Objective

Turn the single-screen app into a three-tab app with a bottom navigation row — **Groups | Log | Notifications**, left to right, in the style of Lose It / MyFitnessPal — where the Log tab hosts the entire existing app unchanged, and Groups and Notifications land as fork-owned stub screens for Stories 2/9 and 13 to fill in.

## Scope

### In Scope

- A new fork-owned shell composable (proposed `app/src/commonMain/kotlin/com/maksimowiczm/foodyou/app/ui/shell/FoodUsAppShell.kt`) owning a Material 3 `NavigationBar` with three `NavigationBarItem`s and a `rememberSaveable` selected-tab state.
- Log tab renders the existing `FoodYouAppNavHost(onDatabaseBackup)` exactly as `FoodYouApp.kt:37` does today — no changes inside the existing NavHost or any existing screen.
- Groups tab stub (proposed `app/ui/shell/GroupsTabStub.kt` or `app/ui/groups/GroupsScreen.kt` stub) — empty scaffold with title; Story 2 adds the My Profile card, Story 9 adds group cards.
- Notifications tab stub (proposed `app/ui/shell/NotificationsTabStub.kt`) — empty scaffold with title; Story 13 finalizes.
- Per-tab UI state preservation via `SaveableStateHolder` (or equivalent) so the Log tab's back stack and scroll positions survive tab switches.
- One-line integration edit in `app/src/commonMain/kotlin/com/maksimowiczm/foodyou/app/ui/FoodYouApp.kt` (line 37): `FoodYouAppNavHost(onDatabaseBackup)` → `FoodUsAppShell(onDatabaseBackup)`. Onboarding branch stays outside the shell.
- New English tab-label strings (fork-owned additive keys) in `shared/resources/src/commonMain/composeResources/values/strings.xml`.

### Out Of Scope

- Any Groups-tab content (profile card, friends card, group cards) — Stories 2, 7, 9.
- Notification Center behavior, storage, or history toggle — Story 13.
- Any relay, networking, or Room changes — this story is purely UI composition.
- Restructuring the existing NavHost routes or moving screens between graphs.

## Non-Goals

- No per-tab deep linking or multi-back-stack navigation library adoption; tab switching is plain state, not navigation routes.
- No badge counts on the Notifications tab icon (candidate follow-up once Story 13 defines unread semantics).
- No tablet/foldable adaptive layouts (navigation rail); phone-first per the two-device household reality.

## Current Understanding

All paths verified in the working tree on 28 July 2026.

- **Root composition:** `app/src/commonMain/kotlin/com/maksimowiczm/foodyou/app/ui/FoodYouApp.kt` — after onboarding, renders `Surface { FoodYouAppNavHost(onDatabaseBackup); AppUpdateChangelogModalBottomSheet() }` (lines 35–40). This is the single integration seam; the shell slots in exactly here.
- **Existing NavHost:** `app/src/commonMain/kotlin/com/maksimowiczm/foodyou/app/navigation/FoodYouAppNavHost.kt` — `rememberNavController()` + `NavHost(startDestination = Home)` with ~30 `@Serializable private object/data class` routes (lines 560+). Routes are `private`, confirming the NavHost is a closed unit that can be re-hosted wholesale without touching route visibility.
- **Home screen:** `app/ui/home/master/HomeScreen.kt` uses its own `Scaffold` with `TopAppBar` (lines 51–76), as do the other screens. Nesting these under an outer `Scaffold(bottomBar = ...)` means the outer scaffold must pass bottom padding down (or the shell places the NavigationBar in a `Column` below the tab content) so inner scaffolds don't double-pad.
- **Material 3 availability:** `gradle/libs.versions.toml:64` — `org.jetbrains.compose.material3:material3`; `NavigationBar`/`NavigationBarItem` and `Icons.Filled.*` are available in commonMain. No new dependency needed.
- **Strings:** fork stories add English base keys to `shared/resources/src/commonMain/composeResources/values/strings.xml`, referenced via `foodyou.app.generated.resources.Res` (pattern used by `HomeScreen.kt:28,57`).
- **Existing behaviors to preserve:** onboarding flow untouched (shell only wraps the post-onboarding branch); `AppUpdateChangelogModalBottomSheet()` continues to overlay regardless of selected tab; all existing navigation callbacks inside the Log tab unchanged.
- **Constraints:** fork philosophy — all new files fork-owned under the existing `com.maksimowiczm.foodyou` namespace; the only upstream-file edits are the one-line call-site swap in `FoodYouApp.kt` and additive `strings.xml` keys.

## Questions / Unknowns

- Q: `[STORY 3.1]` Is the bottom navigation bar persistent across *all* Log-tab screens (settings, food search, product edit, …) or visible only on top-level content, MFP/LoseIt-style ambiguity?
  Impact: Persistent bar costs vertical space on deep screens and can collide with IME on text-entry screens (product forms, search); hiding it on deep screens requires the shell to observe the Log tab's back stack, which couples the shell to the upstream NavHost.
  Assumption: Persistent bar everywhere (simplest, matches Lose It's main-surface behavior and keeps the shell fully decoupled). IME handling: the bar sits below content with standard `WindowInsets` handling so the keyboard covers it rather than pushing it up.
  Status: OPEN
  Answer: —

- Q: `[STORY 3.1]` System back behavior on Groups/Notifications tabs: does back switch to the Log tab (Android bottom-nav convention) or exit the app?
  Impact: Small UX decision; switch-to-Log requires a `BackHandler` in the shell.
  Assumption: Back on a non-Log tab returns to the Log tab; back on Log behaves as today.
  Status: OPEN
  Answer: —

- Q: `[STORY 3.1]` Which tab is the start tab on cold launch — Log (the daily-driver surface) or leftmost Groups?
  Impact: Determines the `rememberSaveable` initial value only.
  Assumption: Log. The diary remains the app's center of gravity; Groups being leftmost is layout order, not priority.
  Status: OPEN
  Answer: —

## Execution Steps

1. Add tab-label strings.
   - Why: Labels resolve through shared resources like all UI text.
   - Edits: `shared/resources/src/commonMain/composeResources/values/strings.xml` — add `tab_groups`, `tab_log`, `tab_notifications` (English base only, fork-owned keys, appended to minimize merge surface).
   - Dependencies: none.

2. Create the stub tab screens.
   - Why: Groups and Notifications need landing surfaces Stories 2/9/13 will build on.
   - Edits: new `app/ui/shell/GroupsTabStub.kt` and `app/ui/shell/NotificationsTabStub.kt` — each a minimal `Scaffold` with a `TopAppBar` title and an empty content hint, mirroring `HomeScreen.kt`'s scaffold conventions.
   - Dependencies: step 1 (titles).

3. Create `FoodUsAppShell.kt`.
   - Why: The shell owns tab state and the NavigationBar; keeping it in one new file preserves the upstream merge surface.
   - Edits: new `app/ui/shell/FoodUsAppShell.kt` — `rememberSaveable { mutableIntStateOf(TAB_LOG) }`, a `SaveableStateHolder` keyed per tab wrapping the tab content, and a Material 3 `NavigationBar` with three `NavigationBarItem`s (icons: `Icons.Filled.Group`, a diary/book icon for Log, `Icons.Filled.Notifications`; confirm exact icon availability in the bundled material-icons set at implementation time). Log tab content is `FoodYouAppNavHost(onDatabaseBackup)` verbatim. Include the `BackHandler` per the back-behavior assumption.
   - Dependencies: steps 1–2.

4. Swap the call site in `FoodYouApp.kt`.
   - Why: Single-line integration keeps the upstream file edit minimal.
   - Edits: `app/ui/FoodYouApp.kt:37` — replace `FoodYouAppNavHost(onDatabaseBackup)` with `FoodUsAppShell(onDatabaseBackup)`; add the one import.
   - Dependencies: step 3.

5. Verify inset/padding behavior on device.
   - Why: Inner screens own their `Scaffold`s; the bar must not double-pad or overlap content, and text-entry screens must remain usable with the IME open.
   - Edits: none expected; adjust shell layout (Scaffold-with-bottomBar vs Column) if padding artifacts appear.
   - Dependencies: step 4.

## Validation

### Automated Checks

- `.\gradlew.bat :app:compileDebugKotlinAndroid` (or the project's standard debug assemble) — compiles cleanly.
- Existing unit test suite unaffected (no logic touched): targeted run of `:app:testDebugUnitTest` only if CI requires.

### Manual Checks

1. Cold launch lands on the assumed start tab with the bar showing Groups | Log | Notifications left-to-right.
2. Log tab is pixel-for-pixel the existing app: home cards, settings navigation, food search, diary entry add/edit all work unchanged.
3. Switch Log → Groups → Log while deep in Settings: the Settings back stack and scroll position survive.
4. Groups and Notifications tabs show their stub scaffolds.
5. Onboarding (fresh install) runs full-screen with no bottom bar; the bar appears only after onboarding finishes.
6. Open a text field in the Log tab (e.g. product create) and confirm the IME does not break layout with the bar present.
7. System back on Groups/Notifications behaves per the recorded assumption.

### Acceptance Criteria

- Three-tab bottom navigation exists; the entire existing app lives in the Log tab with no internal changes.
- Groups and Notifications tabs render as stubs ready for Stories 2/9 and 13.
- Only upstream files touched: `FoodYouApp.kt` (one line + import) and `strings.xml` (additive keys).
- Tab switches preserve per-tab UI state.

## Risk Mitigation

- Risk: Log tab back stack resets when switching tabs (composition of `rememberNavController` lost).
  Mitigation: `SaveableStateHolder` keyed per tab; manual check 3 gates acceptance.
- Risk: Double bottom padding or content hidden behind the bar because inner screens own their `Scaffold`s.
  Mitigation: Shell layout keeps tab content and `NavigationBar` in a vertical arrangement where inner scaffolds receive no extra bottom inset; verified on device (manual checks 2 and 6).
- Risk: Upstream merge conflicts.
  Mitigation: All new code in new fork-owned files; upstream edits limited to one call-site line and appended string keys.
- Risk: IME overlap on text-entry screens with a persistent bar.
  Mitigation: Standard window-insets behavior (keyboard overlays the bar); explicitly checked in manual check 6; fallback is `imePadding`-aware hiding of the bar, noted as a contained follow-up.

## Phase Split

Not needed — single-pass story, CER well under thresholds.

## Evidence / References

- Planning inputs verified 28 July 2026: `FoodYouApp.kt:35-40`, `FoodYouAppNavHost.kt:61-66,560+`, `HomeScreen.kt:51-76`, `gradle/libs.versions.toml:64`, `shared/resources/src/commonMain/composeResources/values/strings.xml` (exists).
- Unverified claims: exact material-icons names available in commonMain (`Icons.Filled.Group` etc.) — confirm at implementation; bundled icon set may require `material-icons-extended` or a drawable fallback.

## Complaints / Friction

None worth recording — story scope was clear and the codebase seams were easy to locate.
