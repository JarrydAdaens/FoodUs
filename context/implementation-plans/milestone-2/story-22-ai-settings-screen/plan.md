# Plan: AI Settings Screen

## Metadata

- Task Type: `STORY`
- Status: `Ready`
- Owner: Jarryd Adaens
- Last Updated: 27 July 2026

## Linked Context

- Milestone: [context/milestones/milestone-2.md](../../../milestones/milestone-2.md)
- Story: [Story 22: AI settings screen](../../../milestones/milestone-2.md#story-22) `[STORY 2.22]`
- Dictation source: [2026-07-26 addendum, items 1-2](../../../dictations-tier-0/2026-07-26_addendum_ai-settings-branding-foodus.md)
- Design authority: `context/design.md` — "Secrets and Credentials" and "Security and Privacy" (updated 2026-07-26: user-entered key supersedes the baked key; `foodus.ai.*` BuildConfig values remain developer fallbacks that must stay blank in anything public)
- Constitutional constraint: `context/laws.md` §2 Secrets — the key is never committed, logged, or echoed into context files; on-device persistence only
- Related Plans:
  - Story 21 (three-layer prompts) — **depends on this story**: its layer-2 "user system prompt" field lives on this screen. This plan stores that field; Story 21 consumes it in prompt assembly.
- External Tooling: none required.

## CER

- Complexity: 4
- Effort: 5
- Risk: 4
- Notes: Inline estimate. The repository already contains every pattern this story needs: `AbstractDataStoreUserPreferencesRepository` for persistence, the USDA key dialog for on-device key entry, the named-qualifier `HttpClient` and OpenAI-compatible DTOs in the `ai` slice for the Validate call, and the `SettingsScreen` list-item + `FoodYouAppNavHost` route pattern for navigation. Complexity is moderate because the fallback-resolution seam (user DataStore value wins over BuildConfig) threads through three call sites plus a new validator. Risk sits on secret handling (never log/echo the key), on touching upstream-shared files (`FoodYouAppNavHost.kt`, `SettingsScreen.kt`, `strings.xml`, `AppConfig.kt`, `app/build.gradle.kts` — keep edits additive to preserve the fork's minimal merge surface), and on the live network Validate call which cannot be unit-tested meaningfully.

## Objective

Ship an AI settings screen with four persisted fields — OpenAI-compatible API key, endpoint (OpenRouter default), model (free text), and the optional user system prompt (Story 21's layer 2) — stored on-device in DataStore; rewire the `ai` slice so runtime calls read this user config first and fall back to blank-by-default BuildConfig developer values; and add a Validate button that performs one live one-token call against the entered key/endpoint/model, showing a green tick on success or the returned error string on failure.

## Scope

### In Scope

- New `AiSettings` user-preferences entity in the `ai` slice domain + a `DataStoreAiSettingsRepository` (mirroring `DataStoreFoodSearchPreferencesRepository`), registered via `userPreferencesRepositoryOf`.
- Runtime config resolution: user-entered value (non-blank) → BuildConfig developer fallback → public domain default (endpoint URL / model only; the key has no default). Applied to `OpenRouterAiFoodScanner`, `OpenRouterAiSearchQueryGenerator`, and the new validator.
- BuildConfig `foodus.ai.*` demoted to blank-by-default developer fallbacks: the OpenRouter endpoint URL and default model constants move from `app/build.gradle.kts` `secret(...)` defaults into the `ai` domain, so all three BuildConfig values default to `""`.
- Validate behavior (DECIDED, not open): one live chat-completions call to the entered endpoint with the entered model and key and a trivial one-token prompt; green tick on success, the returned error string on failure. One code path, no provider-specific handling; deliberately advanced-user.
- New `AiSettingsScreen` + `AiSettingsViewModel` in `app/ui`, a new nav route in `FoodYouAppNavHost.kt`, and an entry point on `SettingsScreen`.
- New English strings in the shared resources base `strings.xml` (additive, fork-owned keys).
- Comment/kdoc truth-maintenance on `AppConfig.aiApiKey` etc. and the `app/build.gradle.kts` secrets block (they currently describe the superseded baked-key design).

### Out Of Scope

- Prompt assembly / three-layer prompt architecture, the per-scan hint field, and the scanning-screen Submit button — all Story 21. This story only **stores** the user system prompt.
- Story 9's query prompt content, the AI scanning UI, and any provider-specific validation logic.
- Key encryption at rest beyond DataStore (matches the existing USDA key posture).

## Non-Goals

- No provider dropdowns, endpoint presets, model catalogs, or capability probing — free text, one code path, advanced-user by design.
- No migration of the existing USDA key storage; it stays where it is (`FoodSearchPreferences`).
- No removal of the BuildConfig seam — developer fallbacks remain for local builds.

## Current Understanding

All paths verified in the working tree on 27 July 2026.

- **BuildConfig seam:** `app/build.gradle.kts:34-57` — `secret()` reads `local.properties` / env; `AI_API_KEY` defaults blank, but `AI_ENDPOINT` defaults to `https://openrouter.ai/api/v1/chat/completions` and `AI_MODEL` to `openai/gpt-4o-mini`. The milestone says all `foodus.ai.*` values become *blank-by-default* fallbacks, so those two public defaults move into domain constants.
- **Config surface:** `app/src/commonMain/kotlin/com/maksimowiczm/foodyou/common/config/AppConfig.kt:42-48` declares `aiApiKey`/`aiEndpoint`/`aiModel`; `app/src/commonMain/kotlin/com/maksimowiczm/foodyou/app/infrastructure/FoodYouConfig.kt:29-31` maps them from `BuildConfig`. Kdoc/comments still describe the baked-key design and need updating.
- **`ai` slice consumers:** `ai/infrastructure/OpenRouterAiFoodScanner.kt:35-56` and `ai/infrastructure/OpenRouterAiSearchQueryGenerator.kt:33-51` read `appConfig.aiApiKey/aiEndpoint/aiModel` directly and return `NotConfigured` on a blank key. Koin wiring in `ai/infrastructure/AiInfrastructureModule.kt` (named-qualifier `HttpClient` shared by both), registered via `ai/AiModule.kt` in `app/di/InitKoin.kt:29`.
- **DTOs:** `ai/infrastructure/model/ChatCompletion.kt` — `ChatCompletionRequest(model, messages)`; no `max_tokens` field yet (the validator wants one to keep the probe one-token cheap).
- **DataStore pattern to mirror:** `common/infrastructure/datastore/AbstractDataStoreUserPreferencesRepository.kt` + `food/search/infrastructure/repository/DataStoreFoodSearchPreferencesRepository.kt` (namespaced string keys, e.g. `food:usda_api_key`) + registration one-liner `userPreferencesRepositoryOf(::DataStoreFoodSearchPreferencesRepository)` in `food/search/infrastructure/FoodSearchModule.kt:26`.
- **Existing key-entry UI to mirror:** `app/ui/database/externaldatabases/UpdateUsdaApiKeyDialog.kt` — plain `OutlinedTextField` bound to a `UserPreferencesRepository<FoodSearchPreferences>` via `koinInject(named(...))`, saved with `update { copy(...) }`; blank input saved as `null`.
- **Settings navigation pattern:** `app/ui/settings/SettingsScreen.kt` renders `*SettingsListItem` composables (one file each under `app/ui/settings/`); `app/navigation/FoodYouAppNavHost.kt:108-117` wires `SettingsScreen` callbacks to `@Serializable private object` routes declared near line 573, each hosted with `forwardBackwardComposable<T>`.
- **ViewModel pattern:** `app/ui/database/externaldatabases/ExternalDatabasesViewModel.kt` + `ExternalDatabasesModule.kt` (`viewModel { ... userPreferencesRepository() }` registered from an app-ui Koin module).
- **Strings:** UI strings resolve through `foodyou.app.generated.resources` from `shared/resources/src/commonMain/composeResources/values/strings.xml` (fork stories add English base strings only; translations are upstream-managed).
- **Behaviors to preserve:** blank effective key still yields `AiScanResult.NotConfigured` / `AiQueryResult.NotConfigured` (consumed by `app/ui/food/diary/aiscan/AiScanViewModel.kt:47` and `app/ui/food/diary/placeholder/PlaceholderMetaViewModel.kt:100`); the key is never logged (both scanners already honor this).
- **Constraints:** fork philosophy — new files are fork-additive; edits to upstream files (`FoodYouAppNavHost.kt`, `SettingsScreen.kt`, `strings.xml`, `AppConfig.kt`, `app/build.gradle.kts`) stay small and appended where possible. Source namespace `com.maksimowiczm.foodyou` untouched (new files live inside it, matching every prior fork story).

## Questions / Unknowns

- Q: `[STORY 2.22]` Where exactly does the screen's entry point live — a new item on the main `SettingsScreen` list, or inside the database/external-databases area "near the existing key entry" (the USDA key lives under External Databases)?
  Impact: Decides which upstream file gains the entry point and how discoverable the screen is.
  Assumption: A new `AiSettingsListItem` on the main `SettingsScreen` (placed after the Database item). "Near the existing key entry" is read as "a sibling settings surface following the same key-entry conventions", not literally inside the food-database screens — AI config is not a food database, and a top-level item keeps the External Databases screen single-purpose.
  Status: OPEN
  Answer: —

- Q: `[STORY 2.22]` Save semantics: explicit Save action, or persist-on-change per field?
  Impact: Affects ViewModel shape and whether Validate can test unsaved drafts.
  Assumption: Explicit Save (top-bar save action or bottom button), mirroring the USDA dialog's Save; blank fields persist as `null` (unset). Validate always tests the **current field drafts**, not the persisted values — matching the dictation's "make a live call to the configured endpoint with the entered model, key" and letting users verify before saving.
  Status: OPEN
  Answer: —

- Q: `[STORY 2.22]` Should the API key field mask its input (password-style)?
  Impact: Minor UX/security trade-off; laws §2 governs logging/committing, not the user's own field.
  Assumption: Plain text like the USDA key field (advanced-user surface, consistency wins). Trivial to flip to `SecureTextField` later if the owner prefers.
  Status: OPEN
  Answer: —

- Q: `[STORY 2.22]` Moving the endpoint/model public defaults out of BuildConfig changes behavior for any existing private build that relied on the old baked defaults while overriding only the key. Acceptable?
  Impact: The owner's own device build is the only known consumer; after this story the same effective values come from the domain defaults, so behavior is identical unless `local.properties` set a custom endpoint/model (which continues to work as the developer fallback).
  Assumption: Acceptable — the milestone text ("blank-by-default developer fallbacks") explicitly asks for this shape.
  Status: OPEN
  Answer: —

- Q: `[STORY 2.22]` Validate probe content: is `max_tokens = 1` with a one-word user message ("Hi") acceptable to all OpenAI-compatible endpoints?
  Impact: Some gateways reject `max_tokens` or bill minimums differently; the dictation demands one code path with no provider-specific handling.
  Assumption: Send `max_tokens = 1` (serialized only when set, so existing scan/query requests are unaffected). If an endpoint rejects it, its error string is surfaced verbatim — which is exactly the designed behavior for this advanced-user surface. No fallback retry logic.
  Status: OPEN
  Answer: —

## Execution Steps

1. Add the `AiSettings` preferences entity and DataStore repository (`ai` slice).
   - Why: On-device persistence for the four fields, following the established `UserPreferences` pattern; Story 21 will read `userSystemPrompt` from here.
   - Edits: New `ai/domain/AiSettings.kt` — `data class AiSettings(val apiKey: String?, val endpoint: String?, val model: String?, val userSystemPrompt: String?) : UserPreferences` (nullable = unset; one type per file per repo rules). New `ai/infrastructure/DataStoreAiSettingsRepository.kt` extending `AbstractDataStoreUserPreferencesRepository<AiSettings>` with keys `ai:api_key`, `ai:endpoint`, `ai:model`, `ai:user_system_prompt`. Register in `AiInfrastructureModule.kt` via `userPreferencesRepositoryOf(::DataStoreAiSettingsRepository)`.
   - Dependencies: none.

2. Introduce runtime config resolution in the `ai` slice.
   - Why: The DECIDED precedence — user DataStore value wins; BuildConfig is a blank-by-default developer fallback never surfaced in UI; endpoint/model public defaults live in the domain.
   - Edits: New `ai/domain/AiRuntimeConfig.kt` — `data class AiRuntimeConfig(val apiKey: String, val endpoint: String, val model: String)` with a `resolve(settings: AiSettings, appConfig: AppConfig)` companion: per field, first non-blank of user value → AppConfig → default (`DEFAULT_ENDPOINT = "https://openrouter.ai/api/v1/chat/completions"`, `DEFAULT_MODEL = "openai/gpt-4o-mini"`; key defaults to `""`). `isConfigured` = key non-blank.
   - Dependencies: step 1 (uses `AiSettings`).

3. Rewire the two existing endpoint consumers to runtime config.
   - Why: The `ai` slice must read user config, not BuildConfig, at call time.
   - Edits: `OpenRouterAiFoodScanner` and `OpenRouterAiSearchQueryGenerator` gain a `UserPreferencesRepository<AiSettings>` constructor dependency; at the top of `scan`/`generateQuery`, resolve `AiRuntimeConfig` from `aiSettingsRepository.observe().first()` + `appConfig` and use its fields (preserving the existing blank-key → `NotConfigured` behavior and never logging the key). Update `AiInfrastructureModule.kt` factories to pass the repository. Refresh both classes' kdoc (currently "baked in at build time").
   - Dependencies: steps 1-2.

4. Demote BuildConfig defaults to blank and fix stale comments.
   - Why: Milestone: "BuildConfig `foodus.ai.*` values remain as blank-by-default developer fallbacks"; design.md: they "must stay blank in anything public"; current comments describe the superseded baked-key design.
   - Edits: `app/build.gradle.kts:47-54` — drop the endpoint/model literal defaults (all three `secret()` calls default `""`); rewrite the block comment (lines 34-38) to describe developer-fallback semantics. Update kdoc on `AppConfig.aiApiKey/aiEndpoint/aiModel` and the comment in `FoodYouConfig.kt:27-28` to say: developer fallback, superseded by user-entered settings, never surfaced in UI, never logged.
   - Dependencies: step 2 (domain defaults must exist before the build-time ones vanish).

5. Add the Validate seam.
   - Why: DECIDED behavior — one live call with the entered key/endpoint/model and a trivial one-token prompt; green tick or returned error string; one code path.
   - Edits: New `ai/domain/AiConnectionValidator.kt` — interface `suspend fun validate(apiKey: String, endpoint: String, model: String): AiValidationResult` with `AiValidationResult` (`Success` | `Failure(message: String)`) as a tiny nested/sealed companion (or its own file if it grows). New `ai/infrastructure/OpenRouterAiConnectionValidator.kt` reusing the named-qualifier `HttpClient`: POST a `ChatCompletionRequest(model, [user: "Hi"], maxTokens = 1)`; success = 2xx; failure = response body/`status` text or exception message, passed through verbatim and never containing the key. Add optional `@SerialName("max_tokens") val maxTokens: Int? = null` to `ChatCompletionRequest` (`explicitNulls = false` already keeps it off existing requests). Register the validator in `AiInfrastructureModule.kt`, bound to the interface.
   - Dependencies: none structurally; parallel with 1-4.

6. Build the AI settings screen UI.
   - Why: The story's user-facing surface.
   - Edits: New folder `app/ui/settings/ai/` containing `AiSettingsScreen.kt` (scaffold with `LargeFlexibleTopAppBar` + `ArrowBackIconButton`, four `OutlinedTextField`s from `rememberTextFieldState`-style state hoisted into the ViewModel, endpoint/model placeholders showing the public defaults, Validate button beside/below the model field with idle → progress → green `Icons.Outlined.Check` tick → error-text states, Save action), `AiSettingsViewModel.kt` (loads `AiSettings` once into editable field state; `save()` persists blank→null via `repository.update { ... }`; `validate()` calls `AiConnectionValidator` with the current drafts and exposes a small sealed UI state; never logs field values), and `AiSettingsModule.kt` (`viewModel { AiSettingsViewModel(userPreferencesRepository(), get()) }`), registered alongside `externalDatabasesModule()` in the app-ui Koin module that hosts it.
   - Dependencies: steps 1 and 5.

7. Wire navigation and the settings entry point.
   - Why: The screen must be reachable; per the open question, a main-settings list item is assumed.
   - Edits: `FoodYouAppNavHost.kt` — add `@Serializable private object AiSettings` beside the existing route objects (~line 573), a `forwardBackwardComposable<AiSettings>` block hosting `AiSettingsScreen(onBack = ...)`, and an `onAiSettings = { navController.navigateSingleTop(AiSettings) }` callback into `SettingsScreen`. New `app/ui/settings/AiSettingsListItem.kt` mirroring `DatabaseSettingsListItem.kt`; add its `item { ... }` to `SettingsScreen.kt` after the Database item and the `onAiSettings` parameter.
   - Dependencies: step 6.

8. Add strings.
   - Why: All labels resolve via compose resources.
   - Edits: Append fork-owned keys to `shared/resources/src/commonMain/composeResources/values/strings.xml` (screen headline/description, field labels for key/endpoint/model/system prompt, Validate label, validating/success text). Reuse existing keys (`action_save`, `headline_api_key`) where they fit.
   - Dependencies: steps 6-7 reference them.

9. Targeted tests + truth-maintenance sweep.
   - Why: The precedence rule is the story's one piece of stable, regression-prone pure logic (unit-testing rules: 1-3 focused tests).
   - Edits: New `commonTest` `ai/domain/AiRuntimeConfigTest.kt` — user value wins over fallback; blank user value falls through to AppConfig; blank both falls to domain defaults (endpoint/model) and blank key ⇒ not configured. Confirm no remaining comment claims the key is "baked in at build time" (`FoodYouConfig`, `AppConfig`, both OpenRouter classes, `build.gradle.kts`).
   - Dependencies: step 2.

## Validation

### Automated Checks

- `./gradlew :app:testDebugUnitTest --tests "*AiRuntimeConfigTest*"` (plus the existing `ai` parser tests as a regression sweep: `--tests "*Ai*"`).
- `./gradlew :app:compileDebugKotlinAndroid` (KMP common + Android compile; catches Koin/nav wiring signature breaks at compile time where possible).
- `git grep -iE "sk-or-|sk-[A-Za-z0-9]{20}"` over the changed files — no credential material committed (laws §2).

### Manual Checks

1. Settings → AI settings: all four fields render, load persisted values after process death, and Save persists (verify via reopening the screen).
2. Validate with a real OpenRouter key/model: green tick. Validate with a garbled key: the endpoint's error string appears verbatim. Validate with an unreachable endpoint URL: the transport error message appears.
3. With a user-entered key saved and a blank BuildConfig key (a normal public build): the Ask AI scan and placeholder query-generation calls succeed — proving runtime config supersedes BuildConfig.
4. Clear the key and Save: AI features return the existing not-configured behavior.
5. Confirm no logcat line contains the entered key during save, validate, or scan.

### Acceptance Criteria

- Four fields (key, endpoint, model, user system prompt) persist on-device in DataStore and survive restart; blank means unset.
- Validate performs exactly one live call with the entered values: green tick on success, returned error string on failure; no provider-specific branches.
- `OpenRouterAiFoodScanner` and `OpenRouterAiSearchQueryGenerator` read runtime config; a user-entered value always wins over the BuildConfig fallback; the fallback is never surfaced in UI.
- All three `foodus.ai.*` BuildConfig values default to blank; a developer override in `local.properties` still works when no user value is set.
- Regression: blank effective key still yields `NotConfigured` in both AI features; upstream files receive only small additive edits; `com.maksimowiczm.foodyou` namespace unchanged.
- The stored user system prompt is not yet consumed anywhere (Story 21's job) — storing it introduces no behavior change.

## Risk Mitigation

- Risk: The key leaks via logs, error strings, or context files.
  Mitigation: No logging of field values anywhere in the new code; validator failure messages are the server/exception text only (the key travels solely in the `Authorization` header, which is never echoed); manual logcat check; plan and commit text never contain a real key.
- Risk: Fallback-precedence bug silently breaks the owner's working AI setup (e.g. blank user endpoint overriding the default).
  Mitigation: Precedence isolated in one pure function (`AiRuntimeConfig.resolve`) with the unit test as the contract; blank/whitespace treated as unset at every layer (save maps blank→null).
- Risk: Adding `max_tokens` to the shared DTO perturbs the existing scan/query requests.
  Mitigation: Nullable with default `null`; the module's `Json { explicitNulls = false }` omits it from serialized output unless the validator sets it.
- Risk: Upstream merge conflicts from touching `FoodYouAppNavHost.kt`, `SettingsScreen.kt`, `strings.xml`, `AppConfig.kt`, `build.gradle.kts`.
  Mitigation: All new logic lives in new files; upstream-file edits are small, appended near existing fork edits, and follow the exact insertion patterns prior fork stories used.
- Risk: `runBlocking` save pattern (as in the USDA dialog) jams the UI thread.
  Mitigation: Save via `viewModelScope.launch` in the new ViewModel instead of copying the dialog's `runBlocking`.
- Risk: Scope creep into Story 21 (prompt assembly, hint field, Submit button).
  Mitigation: Explicit out-of-scope entry; the system prompt is write-only in this story.

## Phase Split

Not needed — CER is below phasing thresholds; single-plan execution.

## Evidence / References

- Verified sources (27 July 2026): `app/build.gradle.kts:34-57`; `common/config/AppConfig.kt:42-48`; `app/infrastructure/FoodYouConfig.kt:27-31`; `ai/AiModule.kt`; `ai/infrastructure/AiInfrastructureModule.kt`; `ai/infrastructure/OpenRouterAiFoodScanner.kt`; `ai/infrastructure/OpenRouterAiSearchQueryGenerator.kt`; `ai/infrastructure/model/ChatCompletion.kt`; `common/infrastructure/datastore/AbstractDataStoreUserPreferencesRepository.kt`; `common/infrastructure/koin/UserPreferencesRepositoryOf.kt`; `food/search/domain/FoodSearchPreferences.kt`; `food/search/infrastructure/repository/DataStoreFoodSearchPreferencesRepository.kt`; `food/search/infrastructure/FoodSearchModule.kt:26`; `app/ui/database/externaldatabases/{UpdateUsdaApiKeyDialog,ExternalDatabasesScreen,ExternalDatabasesViewModel,ExternalDatabasesModule}.kt`; `app/ui/settings/SettingsScreen.kt`; `app/navigation/FoodYouAppNavHost.kt:108-175,573-575`; `shared/resources/src/commonMain/composeResources/values/strings.xml` (location).
- Context sources: milestone-2.md Story 22 (lines 646-668), Story 21 (621-642), interdependency note 11 (712-714); 2026-07-26 addendum items 1-2; design.md "Secrets and Credentials" (262-275) and "Security and Privacy" (306-319); laws.md §2.
- Known unverified claims: exact behavior of `max_tokens = 1` across arbitrary OpenAI-compatible gateways (accepted — errors surface verbatim by design); which app-ui Koin module aggregates `externalDatabasesModule()` (discovery step during execution: grep its call site and register `aiSettingsModule()` beside it).

## Complaints / Friction

None material at planning time. The codebase's existing patterns covered every seam this story needs.

## Execution Log

### 2026-07-27 — Single-pass execution (Opus 4.8, general-purpose worker)

All nine execution steps completed in order.

**Files created (9):**
- `ai/domain/AiSettings.kt` — nullable four-field `UserPreferences` entity.
- `ai/domain/AiRuntimeConfig.kt` — pure precedence resolver (`resolve(settings, appConfig)`) + `DEFAULT_ENDPOINT`/`DEFAULT_MODEL` + `isConfigured`.
- `ai/domain/AiConnectionValidator.kt` — interface + co-located `AiValidationResult` sealed type (mirrors the `AiScanResult`-in-`AiFoodScanner.kt` repo convention).
- `ai/infrastructure/DataStoreAiSettingsRepository.kt` — keys `ai:api_key`, `ai:endpoint`, `ai:model`, `ai:user_system_prompt`; blank→removed.
- `ai/infrastructure/OpenRouterAiConnectionValidator.kt` — one-token probe, `maxTokens = 1`, surfaces `bodyAsText()`/status/exception verbatim.
- `app/ui/settings/ai/AiSettingsViewModel.kt`, `AiValidationUiState.kt`, `AiSettingsScreen.kt`, `AiSettingsModule.kt` — screen, hoisted `TextFieldState` fields, Save via `viewModelScope.launch`, Validate against current drafts.
- `app/ui/settings/AiSettingsListItem.kt` — `Icons.Outlined.SmartToy`, placed after the Database item.
- `app/src/commonTest/.../AiRuntimeConfigTest.kt` — 3 precedence tests.

**Files edited (additive, upstream-shared kept small):** `ai/infrastructure/model/ChatCompletion.kt` (nullable `maxTokens`), `AiInfrastructureModule.kt` (repo + validator registration, repo passed to both scanners), both OpenRouter scanners (runtime-config resolution + kdoc), `AppConfig.kt` / `FoodYouConfig.kt` / `app/build.gradle.kts` (blank-by-default fallbacks + comment truth-maintenance), `SettingsScreen.kt` (+`onAiSettings` param + item), `FoodYouAppNavHost.kt` (route + wiring), `UiModule.kt` (`aiSettingsModule()`), `strings.xml` (11 fork keys), and the two domain `NotConfigured` kdocs (stale "baked into this build" → "no key configured").

**Open-question assumptions — all exercised exactly as written in the plan:**
1. Entry point: new top-level `AiSettingsListItem` on the main Settings list (after Database). Verified on-device.
2. Save semantics: explicit top-bar Save; blank→null; Validate tests current field drafts (blank endpoint/model fall to domain defaults). Verified.
3. API key field: plain text (like the USDA key). Implemented.
4. Endpoint/model public defaults moved from BuildConfig into `AiRuntimeConfig` domain constants; all three `foodus.ai.*` now default `""`. Verified via test + build.
5. Validate probe: `max_tokens = 1` + one-word "Hi"; no retry; error surfaced verbatim. Verified against live OpenRouter (401 body) and an unreachable host (DNS error).

**Inherited gap noticed and LEFT for Story 21 (per plan scope — step 3 scoped only the two scanners):** `AiScanViewModel.aiConfigured` and `PlaceholderMetaViewModel.aiConfigured` still read `appConfig.aiApiKey.isNotBlank()`. In a public build (blank BuildConfig key) these gates keep the Ask-AI / query-generation UI affordances disabled even after the user saves a key, so the runtime-config supersession is not reachable through those buttons until the gates are rewired. Making them reactive requires injecting the `AiSettings` repository into both ViewModels and observing it — that touches Story 21's AI-scanning-screen rebuild and is outside this plan's execution steps. The two scanners themselves are correctly rewired, so the domain behavior (user value wins) is proven by unit test; only the two UI pre-checks remain BuildConfig-bound. Documented here for Story 21 to close.

**Validation performed:** see Completion Review.

## Completion Review

### Estimates vs. actuals

| Metric | Estimate (CER / plan) | Actual |
| --- | --- | --- |
| Complexity | 4 | ~4 — one real snag (name collision, below); precedence seam threaded cleanly. |
| Effort | 5 | ~5 — 9 new files + 11 edited files, as scoped. |
| Risk | 4 | ~3 realized — secret handling and upstream edits landed without incident; live-call risk retired by on-device failure-path verification. |
| Files | ~15 touched | 20 (9 created, 11 edited). |

### Model / reasoning

Opus 4.8, single pass, no phase split (as planned).

### What was verified

- **Unit tests:** `:app:testDebugUnitTest` for `AiRuntimeConfigTest` + regression sweep (`AiFoodEstimateParserTest`, `AiSearchQueryTest`) — BUILD SUCCESSFUL. (Note: the `--tests "*Ai*"` glob in the plan's Validation matches the repo-root file `AGENTIC_RAILS_README.MD` via "rAILs"; ran by fully-qualified class names instead.)
- **Build:** `:app:assembleDebug` — BUILD SUCCESSFUL.
- **On-device (emulator `foodyou`, API 36):** Settings → AI settings renders title/description/four fields/Validate/Save. Entered key + model, Saved, `force-stop` + relaunch, reopened → both values restored; endpoint + system prompt correctly blank (unset). Validate failure path with garbage key against the default endpoint → OpenRouter `{"error":{"message":"Missing Authentication header","code":401}}` surfaced verbatim in red. Validate against an unreachable host → `Unable to resolve host ... No address associated with hostname` surfaced verbatim.
- **Secret safety:** `logcat -d` grep for the entered key / model / `Bearer sk` → 0 matches. Secret scan (`sk-or-v1-`, long `sk-` tokens) over all changed files → clean. No credential material in code, plan, or commit.

### What remains unverified (documented, not faked)

- **Green-tick success path** and **Manual Check 3 success halves** (real scan / query-generation call succeeding via user-entered key) require a real paid API key, which does not exist on this machine. Unverifiable here by design; the failure paths and the resolver unit test cover the surrounding logic.
- The two `aiConfigured` UI gates (see Execution Log) are BuildConfig-bound; left for Story 21.

### Narrative

Straightforward against the plan. One genuine bug caught by the compiler: inside `repository.update { AiSettings(apiKey = apiKey.trimmedOrNull(), ...) }` the lambda receiver is `AiSettings`, whose own `apiKey` is a `String?`, shadowing the ViewModel's `TextFieldState` — fixed by building the new `AiSettings` outside the `update` lambda. No other surprises; every existing pattern (DataStore repo, named-qualifier client, nav route, settings list item) transferred directly.
