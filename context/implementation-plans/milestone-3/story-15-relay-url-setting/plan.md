# Plan: Configurable Relay URL Setting

## Metadata

- Task Type: `STORY`
- Status: `Draft`
- Owner: Jarryd Adaens
- Last Updated: 28 July 2026

## Linked Context

- Milestone: [context/milestones/milestone-3.md](../../../milestones/milestone-3.md)
- Story: [Story 15: Configurable relay URL setting](../../../milestones/milestone-3.md#story-15) `[STORY 3.15]`
- Dictation source: [2026-07-27 relay tier-0 seed](../../../dictations-tier-0/2026-07-27_addendum_foodus-relay-tier0-seed.md) (app obligation 1: user-entered relay URL, never in code/repo)
- Design authority: `context/design.md` — "The Multiplayer Exception" (user-entered relay URL, versioned tolerant protocol, capability-aware UI, HTTPS only) and "Secrets and Credentials" (relay URL stored on-device, deliberately private)
- Constitutional constraint: `context/milestones/milestone-3.md` — Relay Contract Conformance items 4 (capability-aware UI) and 5 (HTTPS only via Ktor)
- Related Plans:
  - [Story 22 AI settings screen (Milestone 2)](../../milestone-2/story-22-ai-settings-screen/plan.md) — the direct precedent this story mirrors: DataStore-backed settings entity, settings screen with live-probe button, nav route + list-item wiring.
  - Stories 3, 6, 7, 14 (this milestone) consume the URL and configured-signal this story creates; Story 8's pipeline (plan pending, blocked on contract v1) will reuse the same base-URL seam.
- External Tooling: none required.

## CER

- Complexity: 4
- Effort: 4
- Risk: 3
- Notes: Inline estimate. The repository already ships every pattern needed — verified in the working tree on 28 July 2026: `AiSettings` + `DataStoreAiSettingsRepository` (DataStore entity with null-means-unset), `OpenRouterAiConnectionValidator` (live probe returning Success/Failure with verbatim error), `AiSettingsScreen`/`AiSettingsViewModel`/`AiValidationUiState` (TextFieldState + probe row UI), `AiSettingsListItem` + `FoodYouAppNavHost` `@Serializable private object` route, and the named-qualifier `HttpClient` wiring in `AiInfrastructureModule`. Complexity sits in designing the capability-check seam without a wire contract (contract v1 is Not Started in foodus-relay) and in the configured-signal later stories gate on. Risk is low: the surface is local-only; residual risk is upstream-shared file edits (`FoodYouAppNavHost.kt`, `SettingsScreen.kt`, `strings.xml` — keep additive) and contract drift when the capability endpoint is finally specified.

## Objective

Ship a relay settings surface where the user types the relay endpoint URL: validated as HTTPS-only (plain HTTP rejected with an inline error), persisted on-device in DataStore exactly like the AI/USDA settings, with a "Check connection" button that probes the relay's version/capability endpoint through the existing Ktor client stack, and a reactive "relay configured" signal that later relay-consuming stories use to hide or grey relay-backed features when the URL is unset or the relay unreachable. No relay address ever appears in code, repo, defaults, or placeholders.

## Scope

### In Scope

- New `relay` feature slice skeleton (`com.maksimowiczm.foodyou.relay`) mirroring the `ai` slice layout: `domain`, `infrastructure`, and a `relayModule` registered in Koin — the future home for all app-side relay client code.
- `RelaySettings` user-preferences entity (single field: `url: String?`, null = unset) + `DataStoreRelaySettingsRepository` (key `relay:url`), registered via `userPreferencesRepositoryOf`, mirroring `DataStoreAiSettingsRepository`.
- URL validation at save time: parse with Ktor's `Url`; require scheme `https`, reject `http` and unparseable input with an inline field error. Blank input saved as unset (null), like the AI fields.
- `RelayConnectionChecker` domain interface + Ktor infrastructure implementation probing the relay's version/capability endpoint, returning Success (with raw capability payload passthrough) / Unreachable / Failure(message). The endpoint path and response schema are **contract-dependent placeholders** (see Questions) isolated behind one constant + one tolerant parse function.
- `ObserveRelayConfigured` reactive signal (URL set and non-blank), mirroring `ObserveAiConfigured`, as the seam later stories' capability gating consumes.
- `RelaySettingsScreen` + `RelaySettingsViewModel` + `RelayCheckUiState` under `app/ui/settings/relay/`, a `RelaySettingsListItem` on `SettingsScreen`, and a `@Serializable private object RelaySettings` route in `FoodYouAppNavHost.kt` — all mirroring the AI settings wiring.
- Dedicated named-qualifier `HttpClient` for the relay (timeouts + JSON with `ignoreUnknownKeys = true`, `explicitNulls = false` — two-way tolerance per Relay Contract Conformance item 3).
- New English strings in the shared resources base `strings.xml` (additive, fork-owned keys).
- Graceful unset/unreachable behavior: the screen itself never blocks; the check reports state without nagging.

### Out Of Scope

- Any envelope schema, mailbox, registration, or friend-code calls (Stories 3, 6, 7, 8).
- Capability *caching* or per-feature capability flags — consuming stories decide how they gate once the contract defines capability names; this story only provides the URL, the configured-signal, and the checker.
- Notification Center events for relay state (Story 13 owns that surface).
- Any server-side work; the foodus-relay repo is read-only reference.

## Non-Goals

- No relay URL default, placeholder address, sample hostname, or baked developer fallback — unlike the AI endpoint, there is deliberately no `DEFAULT_ENDPOINT` equivalent. The field is empty until the user types their own relay.
- No HTTP-with-warning escape hatch; plain HTTP is rejected outright (Relay Contract Conformance item 5).
- No auth/key fields — relay endpoint authentication is unresolved and owned by foodus-relay.
- No connectivity polling or background re-checks; the check runs only on explicit user action.

## Current Understanding

All paths verified in the working tree on 28 July 2026.

- **Settings entity + persistence pattern:** `ai/domain/AiSettings.kt` (nullable fields = unset, `UserPreferences` marker) and `ai/infrastructure/DataStoreAiSettingsRepository.kt` (`AbstractDataStoreUserPreferencesRepository`, namespaced `stringPreferencesKey`, blank-stored-as-removed). `RelaySettings`/`DataStoreRelaySettingsRepository` copy this shape with key `relay:url`.
- **Live-probe pattern:** `ai/domain/AiConnectionValidator.kt` + `ai/infrastructure/OpenRouterAiConnectionValidator.kt` — suspend call, `CancellationException` rethrown, all other failures collapsed to `Failure(message)` with the response body surfaced verbatim. The relay checker mirrors this, minus any credential.
- **Configured-signal pattern:** `ai/infrastructure/AiInfrastructureModule.kt:47` registers `ObserveAiConfigured(aiSettingsRepository, appConfig)`; `ObserveRelayConfigured` is simpler (no BuildConfig fallback — URL set or not).
- **Ktor client wiring:** `AiInfrastructureModule.kt:24-40` — named-qualifier `HttpClient` with `HttpTimeout` + `ContentNegotiation { json(Json { ignoreUnknownKeys = true; explicitNulls = false }) }`. The relay client repeats this under its own qualifier; shorter timeouts are reasonable for a capability probe (see Execution Steps).
- **Screen/ViewModel/UI-state pattern:** `app/ui/settings/ai/AiSettingsScreen.kt` (Scaffold + `LargeFlexibleTopAppBar` Save action + `OutlinedTextField(state=TextFieldState)` + probe row with progress/tick/error), `AiSettingsViewModel.kt` (load-once via `repository.observe().first()`, `trimmedOrNull()` save, probe guard), `AiValidationUiState.kt`. The relay screen is a one-field version plus HTTPS validation error state.
- **Navigation + entry point:** `app/navigation/FoodYouAppNavHost.kt:118-122,578` (`@Serializable private object AiSettings`, `forwardBackwardComposable`, `navigateSingleTop`) and `app/ui/settings/SettingsScreen.kt:35,95-96` + `AiSettingsListItem.kt` (icon + label + supporting text). Relay settings adds one sibling item (icon candidate: `Icons.Outlined.Cloud` or similar outlined icon already in the material-icons set).
- **Koin registration:** slice module `ai/AiModule.kt` (`val aiModule = module { aiInfrastructureModule() }`) registered in `app/di/InitKoin.kt`; UI module `app/ui/UiModule.kt:23` calls `aiSettingsModule()`. The relay slice needs `relayModule` in `InitKoin.kt` and `relaySettingsModule()` in `UiModule.kt`.
- **URL parsing:** Ktor `io.ktor.http.Url` is available in `commonMain` (Ktor client is already a core dependency) — parse + `protocol == URLProtocol.HTTPS` check needs no new dependency.
- **Strings:** fork stories add English base keys to `shared/resources/src/commonMain/composeResources/values/strings.xml` only.
- **Server-side state (read-only check of `D:\forked-projects\FoodUs-Server`):** wire contract v1 is that repo's Milestone 3 Story 1, **Not Started**. The version/capability endpoint exists as a concept in both repos' milestone docs but has no specified path, method, or response schema yet.
- **Constraints:** fork philosophy (new files fork-additive; upstream-shared edits — `FoodYouAppNavHost.kt`, `SettingsScreen.kt`, `strings.xml`, `UiModule.kt`, `InitKoin.kt` — stay small and appended); the owner's relay address never enters the repo, including in tests, previews, or comments.

**Dependency note (app → server, per Relay Contract Conformance item 6):** the settings surface is local-only and buildable immediately. The connection check consumes foodus-relay's version/capability endpoint — blocked by foodus-relay: contract v1 (that repo's Milestone 3 Story 1, Not Started as of 2026-07-28), deployed per the Story 5 gate. Server parent story slug to be confirmed when contract v1 lands. Until then the checker ships behind the placeholder seam and an unreachable/404 result is expected and non-fatal.

## Questions / Unknowns

- Q: `[STORY 3.15]` What are the version/capability endpoint's path, method, and response schema?
  Impact: The connection checker cannot be finished — only seamed. This is the story's single contract gate and the reason for `Status: Draft`.
  Assumption: The probe is an unauthenticated HTTPS `GET` at a relative path under the user-entered base URL. The path lives in one named constant and the response is parsed tolerantly (unknown fields ignored, absent fields = not provided) into an opaque pass-through; both are updated in the same sitting the contract lands. Until then, a reachable-but-404 relay is reported as Failure with the status line, which is acceptable pre-contract behavior.
  Status: OPEN
  Answer: —

- Q: `[STORY 3.15]` Where does app-side relay client code live — a new `relay` slice now, or a broader `social` slice that Stories 2/3/7/9 might introduce?
  Impact: Package/module naming that every later Milestone 3 story builds on; churn here means renames across the milestone.
  Assumption: A new top-level `relay` slice (`com.maksimowiczm.foodyou.relay`) mirroring the `ai` slice, holding transport-facing code (settings, checker, later the envelope pipeline). Social domain objects (profiles, friends, groups) can live elsewhere without conflict; the relay slice is genuinely its own concern (transport, not social graph).
  Status: OPEN
  Answer: —

- Q: `[STORY 3.15]` What exactly do later stories gate on — "URL set" only, or "URL set + last capability check succeeded + capability X reported"?
  Impact: Determines whether this story must persist check results/capability payloads, or only expose the URL and a live checker.
  Assumption: This story persists nothing beyond the URL. It exposes `ObserveRelayConfigured` (URL set) plus the on-demand checker; per-capability gating and any caching policy belong to the consuming stories once the contract names capabilities. This keeps Story 15 minimal and avoids inventing a capability schema.
  Status: OPEN
  Answer: —

- Q: `[STORY 3.15]` Entry-point placement: a top-level `RelaySettingsListItem` on `SettingsScreen` next to the AI item?
  Impact: Which upstream file gains an edit and how discoverable the setting is; the milestone says "same neighbourhood as the AI endpoint configuration".
  Assumption: Yes — a sibling list item directly after the AI settings item, following the identical component pattern.
  Status: OPEN
  Answer: —

## Execution Steps

1. Create the `relay` slice skeleton and settings persistence.
   - Why: Every later relay story needs a home; the URL is the slice's first citizen.
   - Edits: New `relay/domain/RelaySettings.kt` (`url: String?`, kdoc stating null = unset and that the value is deliberately private — never logged, never a default), `relay/infrastructure/DataStoreRelaySettingsRepository.kt` (key `relay:url`), `relay/RelayModule.kt` (`val relayModule = module { relayInfrastructureModule() }`), `relay/infrastructure/RelayInfrastructureModule.kt` registering the repository via `userPreferencesRepositoryOf`; append `relayModule` to `app/di/InitKoin.kt`.
   - Dependencies: none.

2. Add URL validation in the domain.
   - Why: HTTPS-only is constitutional (Conformance item 5); validation must be testable without UI.
   - Edits: New `relay/domain/RelayUrlValidator.kt` — parses with Ktor `Url`, returns a small sealed result: `Valid(normalizedUrl)` / `NotHttps` / `Malformed`. Blank input is not an error (means unset).
   - Dependencies: step 1 (slice exists).

3. Add the connection checker behind the contract seam.
   - Why: The story's capability probe, isolated so contract v1 lands as a one-file change.
   - Edits: New `relay/domain/RelayConnectionChecker.kt` (interface + `RelayCheckResult` sealed: `Success` / `Failure(message)`), `relay/infrastructure/KtorRelayConnectionChecker.kt` (GET base-URL + `CAPABILITY_PATH` constant marked contract-dependent; 2xx = Success, anything else = Failure with body/status verbatim, transport exception = Failure(message), `CancellationException` rethrown — mirroring `OpenRouterAiConnectionValidator`), and a relay-qualified `HttpClient` in `RelayInfrastructureModule` (timeouts ~10 s connect / 15 s request — a capability probe should fail fast; JSON tolerant config identical to the AI client).
   - Dependencies: step 1.

4. Add the configured-signal for later stories.
   - Why: Capability-aware UI rule — consuming stories need one seam to observe.
   - Edits: New `relay/domain/ObserveRelayConfigured.kt` (flow of `Boolean` from the settings repository: URL non-null/non-blank), registered as `factory` in `RelayInfrastructureModule`.
   - Dependencies: step 1.

5. Build the settings screen and wire navigation.
   - Why: The user-facing surface; both household phones type the owner's endpoint here.
   - Edits: New `app/ui/settings/relay/RelaySettingsScreen.kt` (one `OutlinedTextField` for the URL, Save action in the top bar, inline error text for `NotHttps`/`Malformed`, "Check connection" row with progress/tick/error mirroring `ValidateRow`), `RelaySettingsViewModel.kt` (load-once, validate-on-save via step 2, probe via step 3 guarded against re-entry; the URL is never logged), `RelayCheckUiState.kt`, `RelaySettingsModule.kt` (+ call from `app/ui/UiModule.kt`); new `app/ui/settings/RelaySettingsListItem.kt`; append route object + `forwardBackwardComposable` + `onRelaySettings` callback in `app/navigation/FoodYouAppNavHost.kt` and `SettingsScreen.kt` (additive edits, after the AI entries).
   - Dependencies: steps 1–4.

6. Add strings.
   - Why: All UI text resolves through shared resources; fork adds English base keys only.
   - Edits: `shared/resources/src/commonMain/composeResources/values/strings.xml` — headline/description for the list item and screen, field label, HTTPS/malformed error texts, check-connection action/progress/success strings. No string may contain a real relay address.
   - Dependencies: step 5 (final key list).

7. Unit tests for the deterministic logic.
   - Why: Validation is stable business logic (per unit-testing rule: rules/validation are required test targets); everything else is UI/glue.
   - Edits: `commonTest` for `RelayUrlValidator` (https accepted, http rejected, garbage rejected, blank = unset) — 3-4 focused cases.
   - Dependencies: step 2.

## Validation

### Automated Checks

- `RelayUrlValidator` unit tests in `commonTest` (targeted run).
- Full compile of `commonMain` (standard debug build) — proves nav/Koin wiring resolves.

### Manual Checks

1. Open Settings → Relay: field empty, no placeholder address anywhere.
2. Enter `http://example.test` → Save rejected with the HTTPS-required error; enter garbage → malformed error; clear the field → saves as unset.
3. Enter a syntactically valid `https://` URL → saves; kill and relaunch the app → value persists.
4. Check connection with no reachable relay → graceful Failure message, no crash, no retry loop.
5. Grep the diff for any real relay hostname before commit — must be none.

### Acceptance Criteria

- Relay URL is user-entered, HTTPS-validated, persisted on-device, and survives restart.
- Plain HTTP and malformed input never persist.
- `ObserveRelayConfigured` emits false when unset, true once a URL is saved (verified via debugger or a temporary consuming toggle).
- Connection check reports success/failure without blocking or nagging when the relay is unset/unreachable.
- No relay address, sample hostname, or default endpoint exists anywhere in the repository.

## Risk Mitigation

- Risk: Contract v1 later defines the capability endpoint differently than the seam assumes (path shape, auth required, response schema).
  Mitigation: The entire contract-facing surface is two code points — `CAPABILITY_PATH` and the tolerant parse — both marked contract-dependent; the plan's first OPEN question tracks it, and both sides change in the same sitting per Conformance item 1.
- Risk: Upstream merge friction from edits to `FoodYouAppNavHost.kt` / `SettingsScreen.kt` / `strings.xml` / `UiModule.kt` / `InitKoin.kt`.
  Mitigation: All edits are single appended lines/blocks in the same locations Story 22 already touched; new code lives in fork-additive files.
- Risk: The owner's private relay address leaks into the repo via tests, previews, or screenshots.
  Mitigation: Explicit acceptance criterion + manual diff grep; tests use `example.test`-style hosts only.
- Risk: A future `social` slice makes the `relay` package home look wrong.
  Mitigation: Accepted — recorded as an OPEN question; the slice boundary (transport vs social graph) is defensible either way and renaming a young slice is cheap.

## Phase Split

Not needed — single-pass story well under CER thresholds.

## Evidence / References

- Pattern sources verified 2026-07-28: `ai/domain/AiSettings.kt`, `ai/infrastructure/DataStoreAiSettingsRepository.kt`, `ai/infrastructure/AiInfrastructureModule.kt`, `ai/infrastructure/OpenRouterAiConnectionValidator.kt`, `app/ui/settings/ai/*` (screen, viewmodel, ui-state, module), `app/ui/settings/AiSettingsListItem.kt`, `app/ui/settings/SettingsScreen.kt:35,95`, `app/navigation/FoodYouAppNavHost.kt:118-122,578`, `app/ui/UiModule.kt:23`, `ai/AiModule.kt`.
- Server-side gate verified read-only in `D:\forked-projects\FoodUs-Server\context\milestones\milestone-3.md`: wire contract v1 = Story 1, Not Started.
- Planning input: milestone-3.md Story 15 + Relay Contract Conformance; design.md "The Multiplayer Exception".

## Complaints / Friction

None worth recording — the Story 22 precedent covers this story almost end to end; the only genuine gap is the missing wire contract, which is tracked as the plan's gating question rather than friction.
