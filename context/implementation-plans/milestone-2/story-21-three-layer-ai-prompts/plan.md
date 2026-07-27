# Plan: Three-Layer AI Prompt Architecture

## Metadata

- Task Type: `STORY`
- Status: `Ready`
- Owner: Jarryd Adaens
- Last Updated: 27 July 2026

## Linked Context

- Milestone: [context/milestones/milestone-2.md](../../../milestones/milestone-2.md)
- Story: [Story 21: Three-layer AI prompt architecture](../../../milestones/milestone-2.md#story-21) `[STORY 2.21]`
- Dictation source: [2026-07-26 addendum, item 1](../../../dictations-tier-0/2026-07-26_addendum_ai-settings-branding-foodus.md)
- Design authority: `context/design.md` — "AI boundary (Milestone 2)" (lines 314-322): the three-layer model is the constitutional description — baked developer prompt with **no personal data**, optional user-authored system prompt stored on-device, optional per-scan hint; nothing sent without explicit user action.
- Related Plans:
  - [Story 22 (AI settings screen)](../story-22-ai-settings-screen/plan.md) — **hard dependency, must execute first.** Story 22 defines `AiSettings` (with `userSystemPrompt`) in a `DataStoreAiSettingsRepository`, injects `UserPreferencesRepository<AiSettings>` into both OpenRouter classes for runtime config resolution, and demotes BuildConfig to blank developer fallbacks. This plan **consumes those seams** — it reads `AiSettings.userSystemPrompt` through the repository the scanners already hold after Story 22; it invents no parallel storage or config mechanism.
- External Tooling: none required.

## CER

- Complexity: 4
- Effort: 4
- Risk: 3
- Notes: Inline estimate. The change is mostly rewriting two existing prompt strings, threading one already-injected repository field into message assembly, and one screen-level UI addition (hint field + Submit). Complexity comes from the message-shape decision (system-role layer 2 alongside a multimodal user message) and from keeping the scan flow's five UI states coherent with the new required-image/optional-hint gating. Risk is behavioral, not structural: stripping the personal context measurably weakens AI results for a user with no layer-2 prompt (a designed, accepted regression — see Questions), and both prompt strings have unit tests that must be revised deliberately rather than mechanically.

## Objective

Replace the two personal embedded prompts in the `ai` slice — Story 6's "We are Australians living in Victoria, Australia…" scan prompt and Story 9's hidden "I am Australian, living in Melbourne…" query prompt — with three additive layers: (1) a baked developer prompt containing pure machinery only (task framing + JSON/plain-output shape, zero personal data); (2) the optional user system prompt persisted by Story 22, applied as a system message to both AI calls whenever set; (3) an optional per-scan free-text hint entered on the AI scanning screen and applied to that scan only. The scanning screen gains the hint field and a big Submit button (image required, hint optional).

## Scope

### In Scope

- Rewrite the scan developer prompt: strip all locale/personal content, keep only task framing and the exact JSON schema contract; relocate it from the `OpenRouterAiFoodScanner` companion to a testable domain object mirroring `AiSearchQueryPrompt`.
- Rewrite `AiSearchQueryPrompt.build` the same way: no "Australian", no "Melbourne", no "Use Australian food names" — task framing + output-shape instructions + the user's actual note/meal input only.
- Prompt layering in both `OpenRouterAiFoodScanner` and `OpenRouterAiSearchQueryGenerator`: read `AiSettings.userSystemPrompt` (Story 22's field, via the `UserPreferencesRepository<AiSettings>` those classes already hold after Story 22) and, when non-blank, send it as a `system`-role message ahead of the user message.
- `AiFoodScanner.scan` gains an optional `hint: String?` parameter; the hint rides in the same user message as the photo, applied to that call only.
- AI scanning screen: optional free-text hint field + a big **Submit** button (replacing the "Ask AI" button as the primary action); Submit enabled only when a photo exists and AI is configured.
- Update the two prompt unit tests to lock the new invariant (machinery present, personal tokens absent).
- Truth maintenance on kdoc/comments that still describe the embedded-locale design (`OpenRouterAiFoodScanner`, `AiSearchQueryPrompt`, and — if Story 22 left them — the "baked into this build" phrasing on `AiScanResult.NotConfigured` / `AiQueryResult.NotConfigured` and the two ViewModels' not-configured messages).
- New English strings for the hint field and Submit button (fork-owned, additive).

### Out Of Scope

- Everything Story 22 owns: the AI settings screen, storing the user system prompt, runtime key/endpoint/model resolution, Validate. This story only **reads** `userSystemPrompt`.
- A hint field on Story 9's placeholder meta screen — the milestone places layer 3 on the scanning screen only; the query-generation call inherits layers 1 + 2.
- Any change to the AI response parsing, result card, Tick/Align flows, or camera/gallery capture.
- Persisting the hint anywhere — it is per-call, in-memory screen state only.

## Non-Goals

- No prompt templating engine, prompt library, or per-feature user prompt overrides — three fixed layers, concatenated, nothing more.
- No migration UI or wizard for the transition; at most a non-personal example shown as field guidance (see Questions).
- No attempt to keep result quality identical for users with no layer-2 prompt — the personal steering deliberately leaves the codebase.

## Current Understanding

All paths verified in the working tree on 27 July 2026. Paths are relative to `app/src/commonMain/kotlin/com/maksimowiczm/foodyou/` unless noted.

- **Layer-1 candidate #1 (scan):** `ai/infrastructure/OpenRouterAiFoodScanner.kt:77-87` — companion `PROMPT` opens with "We are Australians living in Victoria, Australia. This photo is my food." and asks for "Australian food knowledge and typical Australian serving sizes"; the rest is the JSON schema contract (keys: name, certainty, calories, protein, fat, fibre, sugar) which is pure machinery and must survive verbatim in meaning.
- **Layer-1 candidate #2 (query):** `ai/domain/AiSearchQueryPrompt.kt:13-17` — "I am Australian, living in Melbourne. This is what I had for $meal: …" + "Use Australian food names." The machinery part ("Turn it into a short food-search query… Respond with ONLY the search query itself — no quotes, no labels…") survives.
- **Transport:** both classes build a single `ChatMessage(role = "user", content = [TextContent(prompt), (ImageContent)])` and POST via the shared named-qualifier `HttpClient` (`ai/infrastructure/AiInfrastructureModule.kt`). DTOs in `ai/infrastructure/model/ChatCompletion.kt`: `ChatMessage(role, content: List<ContentPart>)` already supports a text-only `system` message with no DTO changes.
- **Story 22 seams this plan consumes (post-Story-22 state):** `AiSettings.userSystemPrompt` in `ai/domain/AiSettings.kt`, persisted by `ai/infrastructure/DataStoreAiSettingsRepository.kt`; both OpenRouter classes hold `UserPreferencesRepository<AiSettings>` (injected in Story 22 step 3 for runtime config) and resolve settings via `observe().first()` per call — layer 2 is one more field read from the same snapshot.
- **Scan UI:** `app/ui/food/diary/aiscan/AiScanScreen.kt` — capture/preview via `AiScanCameraSection`, then a state-driven column; "Ask AI" button (`AskAiSection`, lines 140-154) shown in `Captured`/`Failed` states, gated on `viewModel.aiConfigured`. `AiScanUiState.kt` — sealed states `NoPhoto/Captured/Scanning/Result/Failed`, each carrying `jpeg`. `AiScanViewModel.kt` — `askAi()` reads `_state.value.jpeg` and calls `aiFoodScanner.scan(jpeg)`; `aiConfigured` currently reads `appConfig.aiApiKey` (line 26).
- **Story 9 consumer:** `app/ui/food/diary/placeholder/PlaceholderMetaViewModel.kt:86` calls `aiSearchQueryGenerator.generateQuery(mealName, note)` — signature unchanged by this story; layering happens inside the generator.
- **Tests:** `app/src/commonTest/kotlin/com/maksimowiczm/foodyou/ai/domain/AiSearchQueryTest.kt` — `promptEmbedsLocaleMealAndNote` asserts "Australian"/"Melbourne" ARE present (lines 18-19); this test's intent inverts under this story.
- **Strings:** `shared/resources/.../values/strings.xml` has `action_ask_ai` (line 39) but no `action_submit` and no hint-field strings — new keys needed.
- **Possible Story 22 gap to verify at execution:** `AiScanViewModel.aiConfigured` (line 26) and `PlaceholderMetaViewModel.aiConfigured` (line 37) gate the AI buttons on `appConfig.aiApiKey` directly. Story 22's execution steps rewire the scanners but do not explicitly rewire these two ViewModel gates; if they still read BuildConfig when this story starts, a user-entered key would leave the buttons disabled. This story touches `AiScanViewModel` anyway and must leave the gating truthful (see Execution Step 5 and Risks).
- **Behaviors to preserve:** blank effective key → `NotConfigured`; the key never logged; response parsing (`AiFoodEstimateParser`, `AiSearchQueryParser`) untouched — so the layer-1 rewrite must keep demanding the exact same JSON keys / bare-query output.
- **Constraints:** fork philosophy — all edits are in fork-authored files (the whole `ai` slice and the aiscan/placeholder UI are fork additions from Stories 6-9); the only upstream-shared file touched is `strings.xml` (additive keys). `com.maksimowiczm.foodyou` namespace unchanged.

## Questions / Unknowns

- Q: `[STORY 2.21]` Transition experience: an existing user (the owner's household) who has set no layer-2 prompt loses the baked "Australians in Victoria / Melbourne" steering the moment this story ships — the AI gets less locale steering until they author a system prompt. Should the app suggest or prefill an example?
  Impact: Decides whether this story adds any guidance surface and whether first-run AI quality regresses silently.
  Assumption: No automatic prefill — prefilling "I'm Australian…" would re-embed personal defaults in the codebase, defeating the story's whole point. Instead, add impersonal example guidance as the system-prompt field's supporting/placeholder text on Story 22's screen (e.g. "e.g. I'm vegetarian, allergic to nuts; I use metric units and live in Australia") — a one-line additive edit to the Story 22 screen, not scope expansion. The owner writes his own real prompt once on each device.
  Status: OPEN
  Answer: —

- Q: `[STORY 2.21]` Does the big **Submit** button replace "Ask AI" as the single primary action, or do both buttons coexist?
  Impact: Screen layout and string usage; the dictation says "a big Submit button at the bottom", the milestone repeats it, and neither mentions keeping Ask AI.
  Assumption: Submit replaces Ask AI — one primary action at the bottom of the screen (below the hint field), enabled when a photo exists and AI is configured. `action_ask_ai` stays in strings.xml (unused keys are harmless; removal is noisier than retention) unless the owner prefers cleanup.
  Status: OPEN
  Answer: —

- Q: `[STORY 2.21]` If execution reveals Story 22 left `AiScanViewModel.aiConfigured` / `PlaceholderMetaViewModel.aiConfigured` reading `appConfig.aiApiKey` (BuildConfig) rather than runtime config, does fixing that gating belong here?
  Impact: With a user-entered key and blank BuildConfig, the Ask AI/Submit buttons would be wrongly disabled — Story 21's Submit gating would be built on a false signal.
  Assumption: Yes — it is a correctness prerequisite for this story's own Submit button and this story already edits `AiScanViewModel`. Fix both gates to observe the same resolved runtime config the scanners use (a small seam, e.g. exposing an `isConfigured` flow from the `ai` slice), and note it in the execution log as inherited from Story 22. If Story 22's execution already fixed it, this question closes itself.
  Status: OPEN
  Answer: —

If the three questions above are answered before execution, nothing else is open.

## Execution Steps

Precondition: Story 22 is implemented and merged (its `AiSettings` repository and scanner injection exist).

1. Rewrite the two layer-1 developer prompts as pure machinery (domain).
   - Why: The constitutional constraint — the baked prompt ships with zero personal data; only task framing and output shape remain.
   - Edits: New `ai/domain/AiScanPrompt.kt` — object holding the scan developer prompt: photo-of-food task framing ("This photo shows food. Identify the food in the photo.") + the existing verbatim JSON schema contract (same seven keys, same "ONLY a JSON object, no Markdown" phrasing, same "If unsure, still provide your best estimate"), with no locale, nationality, region, or diet content. Rewrite `ai/domain/AiSearchQueryPrompt.kt` `build()` to: task framing ("This is what was eaten for $meal: \"$note\".") + the existing machinery ("Turn it into a short food-search query… Respond with ONLY the search query itself — no quotes, no labels, no explanation, a few words at most."), removing "I am Australian, living in Melbourne" and "Use Australian food names". Delete the `PROMPT` companion from `OpenRouterAiFoodScanner` (replaced in step 3). Update both files' kdoc (no more "Australian / Melbourne locale… embedded here").
   - Dependencies: none.

2. Update the prompt unit tests to lock the new invariant.
   - Why: The existing test asserts the personal tokens ARE present — its intent inverts; the no-personal-data property is exactly the kind of regression-prone constitutional rule worth one focused test.
   - Edits: `commonTest .../ai/domain/AiSearchQueryTest.kt` — replace `promptEmbedsLocaleMealAndNote` with a test asserting the meal context and note are embedded AND that "Australian"/"Melbourne"/"Victoria" are absent; keep the fallback and parser tests. Add `commonTest .../ai/domain/AiScanPromptTest.kt` — one test: prompt contains all seven JSON key names and the ONLY-JSON instruction, and contains no personal tokens. (2-3 focused tests total, per the unit-testing limits rule.)
   - Dependencies: step 1.

3. Layer prompts in the two OpenRouter infrastructure classes.
   - Why: Layers 2 and 3 are assembled at call time; layer 2 applies to every diet-related AI call, layer 3 to the scan only.
   - Edits: `ai/infrastructure/OpenRouterAiFoodScanner.kt` — `scan(jpeg, hint)` builds messages as: optional `ChatMessage(role = "system", content = [TextContent(userSystemPrompt)])` when the persisted prompt is non-blank (read from the same `AiSettings` snapshot Story 22's runtime-config resolution already fetches per call), then the user message `[TextContent(AiScanPrompt), TextContent(hint)?, ImageContent(photo)]` with the hint included only when non-blank. `ai/infrastructure/OpenRouterAiSearchQueryGenerator.kt` — same optional system message ahead of the existing user message (prompt from the rewritten `AiSearchQueryPrompt`). No DTO changes (`ChatMessage` already supports text-only system messages). Kdoc on both classes updated to describe the three-layer assembly. If a shared private helper for "optional system message from settings" is warranted, keep it a tiny function inside the `ai` slice — no new abstraction layer.
   - Dependencies: step 1; Story 22's injected `UserPreferencesRepository<AiSettings>`.

4. Extend the `AiFoodScanner` contract with the per-scan hint.
   - Why: Layer 3 must travel from the scanning screen to the call; the domain interface is the boundary.
   - Edits: `ai/domain/AiFoodScanner.kt` — `suspend fun scan(jpeg: ByteArray, hint: String? = null): AiScanResult` with kdoc ("optional one-off user hint for this scan only, e.g. 'I'm at McDonald's'"). `AiSearchQueryGenerator` is unchanged (no layer 3 there). Refresh the stale "No API key was baked into this build" kdoc on `AiScanResult.NotConfigured` / `AiQueryResult.NotConfigured` if Story 22 has not already.
   - Dependencies: step 3 (implementation matches signature).

5. Scanning screen: hint field + big Submit button.
   - Why: The story's user-facing surface — image required, hint optional.
   - Edits: `app/ui/food/diary/aiscan/AiScanViewModel.kt` — add a `hint: MutableStateFlow<String>` (plain screen state, never persisted), `onHintChange`, and pass `hint.value.trim().takeIf { it.isNotBlank() }` into `aiFoodScanner.scan(jpeg, hint)`; verify the `aiConfigured` gate reflects runtime config (see open question — fix here if Story 22 left it on BuildConfig, exposing the resolved-config signal from the `ai` slice rather than duplicating precedence logic). `AiScanScreen.kt` — replace `AskAiSection` with a bottom section containing an optional single-or-few-line `OutlinedTextField` for the hint (label + supporting text from new strings) and a full-width primary **Submit** `Button` (enabled = photo captured && configured; the not-configured helper text stays). The hint field is visible from the `Captured` state onward and survives discard/retry within the screen session; the `Scanning` state disables Submit as today (state-driven).
   - Dependencies: step 4.

6. Strings.
   - Why: All labels resolve via compose resources; no `action_submit` or hint strings exist yet.
   - Edits: Append fork-owned keys to `shared/resources/src/commonMain/composeResources/values/strings.xml`: `action_submit` ("Submit"), hint label (e.g. "Hint for this scan (optional)"), and hint supporting text (e.g. "One-off context, like “I’m at McDonald’s”."). Per the open question's assumption, also add the impersonal example supporting text for Story 22's system-prompt field if that lands here.
   - Dependencies: step 5 references them.

7. Truth-maintenance sweep.
   - Why: No comment, kdoc, or context doc may keep describing the embedded personal prompt as current behavior.
   - Edits: `git grep -inE "australian|victoria|melbourne" -- app/src/commonMain/kotlin/com/maksimowiczm/foodyou/ai app/src/commonMain/kotlin/com/maksimowiczm/foodyou/app/ui/food/diary/aiscan` must return zero hits (the AFCD provider's legitimate "Australian Food Composition Database" references live elsewhere and are untouched). Update the Story 6/9 status lines only if the milestone doc's maintenance pass asks for it (milestone Story 21 status flips at commit time per the usual flow).
   - Dependencies: steps 1-6.

## Validation

### Automated Checks

- `./gradlew :app:testDebugUnitTest --tests "*Ai*"` — revised `AiSearchQueryTest`, new `AiScanPromptTest`, plus the existing `AiFoodEstimateParser` tests as a regression sweep.
- `./gradlew :app:compileDebugKotlinAndroid` — catches the `scan` signature change at every call site and the Compose/Koin wiring.
- `git grep -inE "australian|victoria|melbourne"` over the `ai` slice and aiscan UI — zero hits (personal data fully evacuated from baked code).

### Manual Checks

1. With a configured key and **no** user system prompt: capture a photo, leave the hint blank, Submit → result card appears (layer 1 alone still produces valid JSON).
2. Set a user system prompt in AI settings (e.g. "Answer the name in ALL CAPS" as a visible steering probe), rescan → the result name reflects the steering, proving layer 2 reaches the scan call. Run the placeholder AI route → generated query also reflects it, proving layer 2 reaches Story 9's call.
3. Enter a hint ("this is red lentils and bread"), Submit → result reflects the hint; then run the placeholder AI route → no hint influence (layer 3 is scan-only, that call only).
4. No photo captured → Submit disabled; photo captured, hint blank → Submit enabled (image required, hint optional).
5. Clear the key → Submit disabled with the not-configured text; no logcat line contains the key or the user system prompt content during any call.

### Acceptance Criteria

- The baked developer prompts (scan + query) contain zero personal data — no locale, nationality, region, diet, or household facts — verified by test and grep.
- The persisted user system prompt, when set, is sent as a system message on **both** the scan call and Story 9's query-generation call; when unset, no system message is sent.
- The per-scan hint applies to exactly the scan call it was entered for, and only when non-blank.
- The scanning screen has an optional hint field and a big Submit button; Submit requires a photo, never requires a hint.
- Regression: JSON schema contract and query output contract unchanged (parsers untouched and passing); blank effective key still yields `NotConfigured`; the key is never logged; Story 9's `generateQuery` call-site signature unchanged.
- Fork surface: only fork-authored files plus additive `strings.xml` keys change.

## Risk Mitigation

- Risk: Stripping the personal context degrades AI accuracy for a user with no layer-2 prompt (silent quality regression for the household's existing devices).
  Mitigation: Designed and accepted — this is the story's point (personal data belongs to the user, on their device). Softened by the impersonal example guidance on the settings field (open question) and by the owner authoring his real prompt once per device at upgrade time; called out in the commit log.
- Risk: Some OpenAI-compatible endpoint mishandles a `system` message with array-form content parts alongside a multimodal user message.
  Mitigation: Array-form `content` on system messages is standard OpenAI-compatible shape and OpenRouter normalizes per-model; failure surfaces through the existing error path verbatim (same advanced-user posture as Story 22's Validate). No provider-specific branching.
- Risk: The `scan` signature change breaks a call site silently.
  Mitigation: Default parameter keeps existing call sites compiling only where a default is acceptable; the compile check plus a deliberate sweep of `AiFoodScanner` usages (only `AiScanViewModel`) closes it.
- Risk: Story 22's ViewModel `aiConfigured` gap (BuildConfig-based gating) makes Submit appear broken with a user-entered key.
  Mitigation: Explicit verification in step 5 with a contained fix that reuses the `ai` slice's resolved-config signal — no duplicated precedence logic (tracked as an open question).
- Risk: Test rewrite mechanically preserves the old test names but not the inverted intent, leaving the no-personal-data invariant unprotected.
  Mitigation: Step 2 names the negative assertions explicitly; the grep check in Validation is an independent second net.
- Risk: Hint state leaks across scans or gets persisted accidentally.
  Mitigation: Hint lives only in the ViewModel's in-memory state; nothing writes it to DataStore or the DB; manual check 3 exercises the scan-only scoping.

## Phase Split

Not needed — CER is below phasing thresholds; single-plan execution after Story 22.

## Evidence / References

- Verified sources (27 July 2026): `ai/infrastructure/OpenRouterAiFoodScanner.kt` (PROMPT at 77-87, message build at 40-51); `ai/domain/AiSearchQueryPrompt.kt:9-19`; `ai/infrastructure/OpenRouterAiSearchQueryGenerator.kt:32-51`; `ai/domain/AiFoodScanner.kt`; `ai/domain/AiSearchQueryGenerator.kt`; `ai/infrastructure/model/ChatCompletion.kt` (ChatMessage role/content shape); `ai/infrastructure/AiInfrastructureModule.kt`; `app/ui/food/diary/aiscan/{AiScanScreen,AiScanUiState,AiScanViewModel,AiScanModule}.kt`; `app/ui/food/diary/placeholder/PlaceholderMetaViewModel.kt:37,86`; `commonTest .../ai/domain/AiSearchQueryTest.kt:18-19`; `shared/resources/.../values/strings.xml:39` (no `action_submit` key exists).
- Context sources: milestone-2.md Story 21 (lines 620-642), Story 6 revision note (~line 236), Story 9 (282-306), Story 22 (646-668), interdependency note 11; 2026-07-26 addendum item 1 (three-layer table) and item 2; design.md "AI boundary" (314-322); Story 22 plan (`../story-22-ai-settings-screen/plan.md`), especially its steps 1-3 (AiSettings + repository + scanner injection) which this plan consumes.
- Known unverified claims: exact post-Story-22 shape of the scanners' settings read (planned from Story 22's plan, not executed code — reconcile at execution start); system-message handling across arbitrary OpenAI-compatible gateways (accepted; errors surface verbatim by design).

## Complaints / Friction

None material at planning time. The one wrinkle worth flagging: Story 22's plan preserves but does not explicitly rewire the two ViewModel `aiConfigured` gates, which this story depends on — recorded as an open question rather than assumed fixed.

## Execution Log

### 2026-07-27 — Single-pass execution (Opus 4.8, general-purpose worker)

Executed steps 1-7 in order against the actual post-Story-22 code (not planning assumptions).

**Files created (5):**
- `ai/domain/AiScanPrompt.kt` — layer-1 scan prompt as a domain object; task framing ("This photo shows food. Identify the food in the photo.") + the verbatim seven-key JSON contract, zero personal data.
- `ai/domain/ObserveAiConfigured.kt` — the single "is AI configured?" signal for UI gates; reuses `AiRuntimeConfig.resolve(...).isConfigured` (no duplicated precedence).
- `ai/infrastructure/UserSystemMessage.kt` — tiny shared helper building the optional layer-2 `system` message (null when blank), used by both scanners.
- `commonTest .../ai/domain/AiScanPromptTest.kt` — seven JSON keys + JSON-only instruction present; Australian/Victoria/Melbourne absent.

**Files edited (10):**
- `ai/domain/AiSearchQueryPrompt.kt` — rewritten to "This is what was eaten for $meal: …" + output-shape machinery; removed "I am Australian, living in Melbourne" and "Use Australian food names"; kdoc updated.
- `ai/domain/AiFoodScanner.kt` — `scan(jpeg, hint: String? = null)` + kdoc for layer 3.
- `ai/infrastructure/OpenRouterAiFoodScanner.kt` — deleted the embedded `PROMPT` companion; assembles `listOfNotNull(userSystemMessage(settings.userSystemPrompt), userMessage)` where the user message is `[AiScanPrompt, hint?, image]`; kdoc rewritten to describe the three layers.
- `ai/infrastructure/OpenRouterAiSearchQueryGenerator.kt` — prepends the same optional layer-2 system message; kdoc updated.
- `ai/infrastructure/AiInfrastructureModule.kt` — registered `ObserveAiConfigured` as a factory.
- `app/ui/food/diary/aiscan/AiScanViewModel.kt` — `aiConfigured` is now a reactive `StateFlow<Boolean>` from `ObserveAiConfigured`; added in-memory `hint`/`onHintChange`; `askAi()` → `submit()` passing the trimmed hint; not-configured message de-staled.
- `app/ui/food/diary/aiscan/AiScanScreen.kt` — `AskAiSection` → `SubmitSection` (hint `OutlinedTextField` + full-width **Submit** `Button`, `enabled = photoCaptured && configured`), shown in NoPhoto/Captured/Failed; collects the reactive gate + hint.
- `app/ui/food/diary/placeholder/PlaceholderMetaViewModel.kt` + `PlaceholderMetaModule.kt` — gate now folded from `ObserveAiConfigured` into the `combine` (fifth flow); de-staled not-configured message.
- `commonTest .../ai/domain/AiSearchQueryTest.kt` — inverted invariant: meal+note present, personal tokens absent; fallback phrasing updated.
- `shared/resources/.../values/strings.xml` — added `action_submit`, `label_ai_scan_hint`, `description_ai_scan_hint`; de-staled `neutral_ai_not_configured` ("in this build" → "Add a key in AI settings.").

**Open-question assumptions — all exercised exactly as written:**
1. No personal prefill. Story 22 already added impersonal guidance on the system-prompt field (`description_ai_system_prompt` = "Optional personal context (locale, diet, allergies)…"), so per instruction I checked first and added nothing to the settings screen.
2. Submit replaces Ask AI as the single primary action; `action_ask_ai` left in strings.xml (harmless).
3. Inherited gate fix DONE here (see below).

**Inherited gap from Story 22 — CLOSED:** `AiScanViewModel.aiConfigured` and `PlaceholderMetaViewModel.aiConfigured` previously read `appConfig.aiApiKey.isNotBlank()` (BuildConfig). Both now observe `ObserveAiConfigured`, which resolves the same `AiRuntimeConfig` precedence the scanners use — so a user-entered key with blank BuildConfig enables the affordances. Verified reactively on-device (see Completion Review). No precedence logic duplicated.

## Completion Review

### Estimates vs. actuals

| Metric | Estimate (CER) | Actual |
| --- | --- | --- |
| Complexity | 4 | ~3 — the Story 22 seams (`AiSettings.userSystemPrompt`, per-call settings snapshot, `AiRuntimeConfig`) were exactly as described; message-shape and gate wiring were mechanical. |
| Effort | 4 | ~4 — 5 created + 10 edited files. |
| Risk | 3 | ~2 realized — no secret/personal data landed; failure path verified on-device. |

### What was verified

- **Unit tests:** `:app:testDebugUnitTest` for `AiSearchQueryTest`, `AiScanPromptTest`, `AiFoodEstimateParserTest`, `AiRuntimeConfigTest` (by fully-qualified class name — the `--tests "*Ai*"` glob mis-expands against a repo-root file) — BUILD SUCCESSFUL.
- **Build:** `:app:assembleDebug` — BUILD SUCCESSFUL.
- **Truth-maintenance grep:** `git grep -inE "australian|victoria|melbourne"` over `commonMain` ai slice + aiscan UI — zero hits. (The only remaining references are the negative assertions in the two prompt tests, which lock the absence invariant.)
- **On-device (emulator `foodyou`, API 36):**
  - Scan screen renders the hint field ("Hint for this scan (optional)"), supporting text ("…such as a brand or portion size. Not saved."), and Submit.
  - No photo → Submit disabled; photo picked (via gallery, `adb push`ed JPEG), hint blank → Submit enabled (image required, hint optional).
  - **Gate fix proven reactively:** with a user key persisted (BuildConfig blank) the affordance was enabled; after clearing the key in AI settings and saving, the scan screen showed "AI is not configured. Add a key in AI settings." with Submit disabled even with a photo; after re-saving a garbage key, Submit re-enabled. This exercises the runtime-config gate, not BuildConfig.
  - **Failure path:** submitting with a garbage key against the default endpoint surfaced "AI request failed (401)." via the existing Failed state.
  - **Secret safety:** `logcat -d` scan for the key / system-prompt / hint text → 0 matches.

### What remains unverified (documented, not faked)

- **Layer-2 steering probe** (a user system prompt visibly changing the scan/query result) and **layer-3 hint influence** on real results require a valid paid API key, which does not exist on this machine. The request-assembly code paths are exercised (the 401 came back from a real POST that included the layer-1 prompt, and the layer-2/3 assembly is covered by the unit-tested prompt shape + `listOfNotNull` composition), but the model's *response* behavior is unverifiable here.
- Success (2xx) scan/query round-trips — same reason.
