# Plan: Provider Website Info Links

## Metadata

- Task Type: `STORY`
- Status: `Ready`
- Owner: Jarryd Adaens (planned by agent)
- Last Updated: 27 July 2026

## Linked Context

- Milestone: [context/milestones/milestone-2.md](../../../milestones/milestone-2.md) — Story 23 (`[STORY 2.23]`)
- Story: [Story 23: Provider website info links](../../../milestones/milestone-2.md#story-23)
- Backlog source: none — synthesized directly into Milestone 2 from the 2026-07-26 addendum
- Dictation source: [2026-07-26 addendum, item 4](../../../dictations-tier-0/2026-07-26_addendum_ai-settings-branding-foodus.md)
- Related Plans:
  - [Story 15 — AFCD provider](../story-15-australian-food-composition-database-provider/plan.md) (built the AFCD screen this story decorates)
  - [Story 17 — provider update-check & refresh UI](../story-17-provider-update-check-refresh-ui/plan.md) (built the update controls on that same screen)
- External Tooling: `commit-log` skill at commit time

## CER

- Complexity: 2
- Effort: 2
- Risk: 1
- Notes: Self-graded. Pure additive UI: one reusable chip component, one string, four call sites, four static URLs. No schema, navigation, or provider-behavior changes. The only judgment calls are URL choice and where the "website URL" lives (static config, not a DB column — see Questions).

## Objective

Every remote/composition food-data provider surface in the app — AFCD, Swiss Food Composition Database, Open Food Facts, and USDA FoodData Central — gains an outbound "Website" affordance that opens the provider's authoritative website in the browser, so users can check what a dataset actually is before importing or trusting it. Provider website URLs are recorded once, in static provider configuration.

## Scope

### In Scope

- A shared `WebsiteChip` composable (mirroring the existing `TermsOfUseChip` / `PrivacyPolicyChip` pattern).
- Website link on the AFCD provider screen (`AustralianFoodCompositionDatabaseScreen`).
- Website link on the Swiss FCD import screen (`SwissFoodCompositionDatabaseScreen`).
- Website link on the Open Food Facts card and the USDA card in `ExternalDatabasesScreen` (their only settings/import surface — neither has a dedicated screen).
- Static per-provider website URLs added to `AppConfig` / `FoodYouConfig` (the same home as the existing OFF terms/privacy and USDA privacy URIs).
- One new user-facing string for the chip label.

### Out Of Scope

- Any provider behavior change (search, import, update-check, enablement).
- Any Room schema change or migration (`ProviderMetadata` table stays as shipped by Story 15 — see Q2).
- Navigation changes; the links open via `LocalUriHandler`, not in-app screens.
- The Swiss CSV manual-import flow screens (`importcsvproducts`) — they are a generic CSV tool, not a provider surface.

## Non-Goals

- No in-app browser / WebView.
- No per-product source links (those already exist via `FoodSource.url` provenance).
- No re-verification of AFCD download URLs or release detection (Story 17 territory, already shipped).

## Current Understanding

All paths relative to repo root `D:\forked-projects\FoodYou`. All verified against the tree on 2026-07-27.

- **Provider surfaces (the four touchpoints):**
  - AFCD: `app/src/commonMain/kotlin/com/maksimowiczm/foodyou/app/ui/database/australianfoodcompositiondatabase/AustralianFoodCompositionDatabaseScreen.kt` — Story 15/17 screen; `LazyColumn` of description, enable switch, state section, update outcome, action buttons, attribution footer. Wired in `app/src/commonMain/kotlin/com/maksimowiczm/foodyou/app/navigation/FoodYouAppNavHost.kt:174`.
  - Swiss FCD: `app/src/commonMain/kotlin/com/maksimowiczm/foodyou/app/ui/database/swissfoodcompositiondatabase/SwissFoodCompositionDatabaseScreen.kt` — `LanguagePick` state renders a description item then the language picker; the link belongs after the description item.
  - Open Food Facts + USDA: no dedicated screens; their settings/import surface is the two cards in `app/src/commonMain/kotlin/com/maksimowiczm/foodyou/app/ui/database/externaldatabases/ExternalDatabasesScreen.kt`, whose bodies (`OpenFoodFactsPrivacyCard`, `UsdaPrivacyCard`) live in `app/src/commonMain/kotlin/com/maksimowiczm/foodyou/app/ui/common/component/PrivacyCard.kt`. Each already renders a `FlowRow` of `AssistChip`s (`TermsOfUseChip` at line 131, `PrivacyPolicyChip` at lines 132/217) that open URIs via `LocalUriHandler.current.openUri(...)` with the URI defaulted from `LocalAppConfig.current`.
- **Outbound-link pattern to copy:** `AboutScreen.kt` (lines 56-57, 121, 131-134) and `PrivacyCard.kt` both use `LocalUriHandler.current.openUri(uri)` with URIs supplied by `LocalAppConfig` — no platform-specific code needed.
- **Chip components to mirror:** `app/src/commonMain/kotlin/com/maksimowiczm/foodyou/app/ui/common/component/TermsOfUseChip.kt` and `PrivacyPolicyChip.kt` — `AssistChip` + `Icons.AutoMirrored.Outlined.OpenInNew` + `stringResource` label.
- **URL configuration home:** `app/src/commonMain/kotlin/com/maksimowiczm/foodyou/common/config/AppConfig.kt` (interface) implemented by `app/src/commonMain/kotlin/com/maksimowiczm/foodyou/app/infrastructure/FoodYouConfig.kt` — already fork-modified (fork version, contact email, AI config) and already carries provider document URIs (`openFoodFactsTermsOfUseUri`, `openFoodFactsPrivacyPolicyUri`, `foodDataCentralPrivacyPolicyUri`). Exposed to composables through `LocalAppConfig` (`app/ui/common/utility/`).
- **Provider metadata model:** the Story 15 `ProviderMetadata` Room table + domain model (`app/src/commonMain/kotlin/com/maksimowiczm/foodyou/importexport/providermetadata/domain/ProviderMetadata.kt`) holds *runtime dataset state* (installed version, checksum, check timestamps, last error). Rows exist only for downloadable providers (currently AFCD) and only after an import/check. It is the wrong carrier for a static website URL that must cover all four providers — see Q2.
- **AFCD static config:** `app/src/commonMain/kotlin/com/maksimowiczm/foodyou/importexport/australianfoodcompositiondatabase/AustralianFoodCompositionDatabaseConfig.kt` already holds `SOURCE_URL` — the FSANZ AFCD download landing page, verified live during the Story 14 spike (`context/wiki/provider-quickadd-architecture.md` §9.1).
- **Strings:** user-facing strings live in `shared/resources/src/commonMain/composeResources/values/strings.xml` (fork already added AFCD strings at lines 333-336); generated accessor namespace `foodyou.app.generated.resources`.
- **Authoritative website URLs** (see Evidence for in-repo verification):

  | Provider | URL | Verification |
  | --- | --- | --- |
  | AFCD (FSANZ) | `https://www.foodstandards.gov.au/science-data/food-nutrient-databases/afcd/australian-food-composition-database-download-excel-files` | Already in repo as `AustralianFoodCompositionDatabaseConfig.SOURCE_URL`; fetched live with HTTP 200 during the Story 14 spike. Reused, not duplicated (Q1 covers whether a nicer landing page is preferred). |
  | Swiss FCD (FSVO) | `https://naehrwertdaten.ch` | Cited as the official source in the bundled dataset's own copyright file: `app/src/commonMain/composeResources/files/swiss-food-composition-database/COPYRIGHT` ("Source: Federal authorities of the Swiss Confederation, https://naehrwertdaten.ch"). |
  | Open Food Facts | `https://world.openfoodfacts.org` | Matches the app's own API base (`OpenFoodFactsRemoteDataSource.kt:174`) and per-product provenance URLs. |
  | USDA FoodData Central | `https://fdc.nal.usda.gov` | Matches the app's own per-product provenance URLs (`USDAMapper.kt:42,91`). |

- **Existing behaviors to preserve:** provider enable/disable, import, and update-check flows are untouched; the Swiss importing/finished states and the AFCD busy state must not gain the chip in a state where tapping mid-import would be odd (chip is harmless while busy, but keep it out of the importing/finished Swiss states by placing it in the `LanguagePick` content only; on AFCD the chip stays visible — opening a website during import is safe and useful).
- **Assumptions and constraints:**
  - Fork philosophy: additive overlay; source namespace `com.maksimowiczm.foodyou` retained for merge-friendliness. New files are pure additions; edits to upstream files (`PrivacyCard.kt`, `SwissFoodCompositionDatabaseScreen.kt`, `AppConfig.kt`, `FoodYouConfig.kt`, `strings.xml`) are minimal, localized insertions.
  - Per the unit-testing rules, this is UI glue — no unit tests are warranted; validation is build + manual on-device checks.

## Questions / Unknowns

- Q: `[STORY 2.23]` Should the AFCD link target the FSANZ *download files* page (already in repo as `SOURCE_URL`, live-verified during the Story 14 spike) or a more general AFCD landing page? The spike also referenced `https://www.foodstandards.gov.au/science-data/monitoringnutrients/afcd/...` paths (licence page), so a cleaner top-level AFCD page likely exists, but no candidate has been verified from this machine during planning.
  Impact: Which FSANZ URL ships as the user-facing "more info" destination.
  Assumption: Reuse `AustralianFoodCompositionDatabaseConfig.SOURCE_URL` — it is authoritative FSANZ, already verified reachable, already the per-row provenance URL, and reusing it adds zero new unverified URLs. Swap is a one-line change if the owner prefers a different page.
  Status: OPEN

- Q: `[STORY 2.23]` The milestone's rough scope says "provider metadata gains a website URL." Does the owner intend a `websiteUrl` column on the Story 15 `ProviderMetadata` Room table, or is static provider configuration sufficient?
  Impact: A DB column means a Room migration and still fails to cover OFF/USDA/Swiss, which have no `ProviderMetadata` rows (the table only tracks downloadable-dataset state, and rows appear only after a check/import). Static config covers all four providers uniformly with no migration.
  Assumption: Static config satisfies the intent — website URLs are compile-time provider facts, not runtime dataset state. They are added to `AppConfig`/`FoodYouConfig` beside the existing OFF/USDA document URIs. No schema change.
  Status: OPEN

- Q: `[STORY 2.23]` The dictation says "button"; the closest existing idiom is the `AssistChip` with an open-in-new icon (Terms of Use / Privacy Policy chips). Is a chip acceptable on all four surfaces, or does the owner want a full-width button on the dedicated AFCD/Swiss screens?
  Impact: Pure visual choice; either is a one-composable swap.
  Assumption: One shared `WebsiteChip` everywhere — consistent with the app's existing outbound-link affordance and DRY. The milestone's "button" is satisfied: an AssistChip is a Material button affordance.
  Status: OPEN

## Execution Steps

1. Add website URIs to app configuration.
   - Why: Single authoritative home for outbound provider URLs, matching where OFF/USDA document URIs already live; composables read them via `LocalAppConfig` exactly like the existing chips.
   - Edits:
     - `common/config/AppConfig.kt`: add four documented vals — `openFoodFactsWebsiteUri`, `foodDataCentralWebsiteUri`, `swissFoodCompositionDatabaseWebsiteUri`, `australianFoodCompositionDatabaseWebsiteUri`.
     - `app/infrastructure/FoodYouConfig.kt`: implement them with the four URLs from the table above; the AFCD value references `AustralianFoodCompositionDatabaseConfig.SOURCE_URL` (same module — no duplicated literal), with a `(Milestone 2, Story 23)` comment matching the file's existing style.
   - Dependencies: none.

2. Add the chip label string.
   - Why: All user-facing text is resource-backed.
   - Edits: `shared/resources/src/commonMain/composeResources/values/strings.xml`: add one string, e.g. `<string name="action_visit_website">Website</string>`, beside the fork's existing provider strings (~line 336).
   - Dependencies: none.

3. Create the shared `WebsiteChip` composable.
   - Why: Four call sites, one affordance — mirrors `TermsOfUseChip.kt` exactly (Rule of Three already exceeded by the existing chip pair + four new uses).
   - Edits: new file `app/ui/common/component/WebsiteChip.kt` — `AssistChip`, `Icons.AutoMirrored.Outlined.OpenInNew` leading icon, `Res.string.action_visit_website` label, `onClick: () -> Unit` parameter.
   - Dependencies: step 2 (string).

4. Wire the chip into the Open Food Facts and USDA cards.
   - Why: These cards are OFF's and USDA's only settings/import surface.
   - Edits: `app/ui/common/component/PrivacyCard.kt`:
     - `OpenFoodFactsPrivacyCard`: add `websiteUri: String = LocalAppConfig.current.openFoodFactsWebsiteUri` parameter (matching the existing `termsOfUseUri`/`privacyPolicyUri` defaults) and prepend `WebsiteChip(onClick = { uriHandler.openUri(websiteUri) })` in the `FlowRow` (line ~130).
     - `UsdaPrivacyCard`: same pattern with `foodDataCentralWebsiteUri` in its `FlowRow` (line ~217).
   - Dependencies: steps 1, 3.

5. Wire the chip into the Swiss FCD screen.
   - Why: The Swiss import screen is that provider's dedicated surface.
   - Edits: `app/ui/database/swissfoodcompositiondatabase/SwissFoodCompositionDatabaseScreen.kt`: in the `LanguagePick` branch's `LazyColumn`, add an item between the description and the language picker: read `LocalAppConfig.current` + `LocalUriHandler.current` in the private screen composable and render `WebsiteChip`. Not shown in `Importing`/`Finished` states (they are transient progress/success views).
   - Dependencies: steps 1, 3.

6. Wire the chip into the AFCD screen.
   - Why: The AFCD provider screen (Story 15/17) is the "Australian database screen" from the addendum.
   - Edits: `app/ui/database/australianfoodcompositiondatabase/AustralianFoodCompositionDatabaseScreen.kt`: add an item directly under the description item rendering `WebsiteChip(onClick = { uriHandler.openUri(appConfig.australianFoodCompositionDatabaseWebsiteUri) })`. Chip stays enabled while busy (opening a website is side-effect-free).
   - Dependencies: steps 1, 3.

7. Build and manually verify (see Validation).
   - Why: UI glue — the harness for visible-behavior claims is an on-device check.
   - Edits: none.
   - Dependencies: steps 1-6.

## Validation

### Automated Checks

- `./gradlew.bat :app:assembleDebug` — compiles common + Android source sets and the generated resource accessors (catches string/accessor typos). Run foreground within the 600 s cap per the toolchain memory note.
- No new unit tests: outbound-link chips are UI glue with no logic to lock down (per the workspace unit-testing rules, tests here are discouraged).

### Manual Checks

On the `foodyou` AVD (Pixel 6, API 36), after `adb install -r`:

1. Settings → Food database → Open Food Facts card shows a "Website" chip; tapping opens `world.openfoodfacts.org` in the browser.
2. Same card list → USDA card chip opens `fdc.nal.usda.gov`.
3. Swiss Food Composition Database screen (language-pick state) shows the chip; tapping opens `naehrwertdaten.ch`. Chip absent during import progress and on the finished state.
4. Australian Food Composition Database screen shows the chip; tapping opens the FSANZ AFCD page. Repeat with an import running to confirm nothing breaks.
5. Regression: OFF/USDA enable checkboxes, Terms/Privacy chips, Swiss import, and AFCD import/check-for-updates all still behave as before.

### Acceptance Criteria

- Every provider surface (AFCD, Swiss FCD, OFF, USDA) has a working outbound website link.
- All four URLs are authoritative for their provider and recorded once in configuration.
- No provider behavior, schema, or navigation changes; diff outside the six named files is empty.
- App builds; no new strings hard-coded in composables.

## Risk Mitigation

- Risk: A chosen URL is wrong or dies later (FSANZ pages are release-dated).
  Mitigation: All four URLs are either already shipped in this repo (AFCD provenance, OFF/USDA in-code URLs) or cited by the dataset's own bundled COPYRIGHT (Swiss). Each lives in exactly one config location, so a fix is a one-line change. Q1 flags the AFCD page choice explicitly.
- Risk: Upstream merge conflicts from editing shared files (`PrivacyCard.kt`, `AppConfig.kt`, `strings.xml`).
  Mitigation: Insertions only, grouped beside existing fork additions, following each file's existing patterns; the new component and config values are net-new files/lines. Accepted as the minimal unavoidable surface — the buttons must appear on existing screens.
- Risk: Misreading "provider metadata gains a website URL" as requiring a DB column.
  Mitigation: Recorded as open question Q2 with the static-config assumption and rationale; if the owner wants the column, it is a follow-up additive migration that does not invalidate this work.

## Phase Split

Not needed — single small feature, CER well under any phasing threshold.

## Evidence / References

- Story text: `context/milestones/milestone-2.md` lines 672-689 (Story 23) and 712-714 (independence note); Stories 15 (lines 438-451) and 17 (lines 496-510) for the surfaces this decorates.
- Dictation: `context/dictations-tier-0/2026-07-26_addendum_ai-settings-branding-foodus.md` item 4. Its "Australian database screen" open observation is resolved by the milestone: that screen is this fork's own Story 15 AFCD screen.
- Spike: `context/wiki/provider-quickadd-architecture.md` §9.1 (FSANZ URLs live-verified 2026-07-25), §6 (enablement/UI map).
- Code verified 2026-07-27: `AustralianFoodCompositionDatabaseScreen.kt`, `SwissFoodCompositionDatabaseScreen.kt`, `ExternalDatabasesScreen.kt`, `PrivacyCard.kt` (chip `FlowRow`s at lines 130-132, 217), `TermsOfUseChip.kt`, `AboutScreen.kt` (`LocalUriHandler` pattern, lines 56-57/121/131-134), `AppConfig.kt`, `FoodYouConfig.kt`, `AustralianFoodCompositionDatabaseConfig.kt` (`SOURCE_URL`), `ProviderMetadata.kt` (no URL field; runtime-state only), `shared/resources/src/commonMain/composeResources/values/strings.xml` lines 329-336, Swiss dataset `COPYRIGHT` (naehrwertdaten.ch), `OpenFoodFactsRemoteDataSource.kt:174`, `USDAMapper.kt:42,91`, `FoodYouAppNavHost.kt:174`.
- Unverified claim: none of the four URLs were re-fetched over the network during planning; verification relies on the in-repo evidence above (AFCD was HTTP-verified during the Story 14 spike).

## Complaints / Friction

None worth recording — context tier and code were consistent; the only ambiguity ("provider metadata gains a website URL") is captured as Q2.

## Execution Log

- 2026-07-27 — Executed Steps 1-7 in order. All three open questions were resolved on their documented assumptions (owner unavailable):
  - Q1 (AFCD URL): reused `AustralianFoodCompositionDatabaseConfig.SOURCE_URL` — no new unverified URL introduced; `FoodYouConfig.australianFoodCompositionDatabaseWebsiteUri` references the constant directly.
  - Q2 (config vs DB column): static config in `AppConfig`/`FoodYouConfig`. No Room/schema change.
  - Q3 (chip vs button): one shared `WebsiteChip` (`AssistChip` + `OpenInNew`) on all four surfaces.
- Step 1: added four `val`s to `common/config/AppConfig.kt` and implemented them in `app/infrastructure/FoodYouConfig.kt` (added an import for `AustralianFoodCompositionDatabaseConfig`).
- Step 2: added `<string name="action_visit_website">Website</string>` to `strings.xml` beside the AFCD action strings.
- Step 3: created `app/ui/common/component/WebsiteChip.kt` mirroring `TermsOfUseChip.kt`.
- Step 4: prepended `WebsiteChip` to the OFF and USDA `FlowRow`s in `PrivacyCard.kt`, each with a `websiteUri` parameter defaulted from `LocalAppConfig`.
- Step 5: added a `WebsiteChip` item between the description and language picker in the Swiss screen's `LanguagePick` branch (added `LocalUriHandler` + `LocalAppConfig` reads and two imports).
- Step 6: added a `WebsiteChip` item under the description on the AFCD screen (added `LocalUriHandler` + `LocalAppConfig` reads and two imports).
- Unplanned extra file: `app/ui/common/utility/AppConfig.kt` holds an anonymous `AppConfig` used as the `LocalAppConfig` default; the first build failed because it did not implement the four new members. Added placeholder values there. This is a required consequence of the interface change (the plan's "six named files" count missed this default implementation), not scope creep.
- Step 7: `:app:assembleDebug` green after that fix; deployed to the `foodyou` AVD and verified all four links on-device.

## Completion Review

- Model: Claude Opus 4.8 (1M). Reasoning: default.
- Estimates vs actuals:
  - Files: estimated 6 (1 new + 5 edits); actual 7 (2 new: `WebsiteChip.kt` + the string is in an existing file, so 1 new component; 6 edited: `AppConfig.kt`, `FoodYouConfig.kt`, `strings.xml`, `PrivacyCard.kt`, `SwissFoodCompositionDatabaseScreen.kt`, `AustralianFoodCompositionDatabaseScreen.kt`, plus the unplanned `ui/common/utility/AppConfig.kt` default impl). The one miss was the second `AppConfig` implementation.
  - CER: Complexity 2 / Effort 2 / Risk 1 all held. No surprises beyond the extra default-impl file, which the compiler caught immediately.
- What was verified (on `foodyou` AVD, Pixel 6 API 36, via `adb logcat` VIEW-intent capture):
  - OFF card "Website" chip → `act=android.intent.action.VIEW dat=https://world.openfoodfacts.org` (Chrome opened).
  - USDA card "Website" chip → `dat=https://fdc.nal.usda.gov`.
  - Swiss FCD screen (LanguagePick) "Website" chip → `dat=https://naehrwertdaten.ch`; chip renders between description and language picker.
  - AFCD screen "Website" chip → `dat=https://www.foodstandards.gov.au/...` (the FSANZ SOURCE_URL).
  - Regression glance: OFF Terms of use / Privacy policy / Sign in chips, USDA Privacy policy / API key chips, both enable checkboxes, and the Swiss language picker + AFCD Import button all still render and behave as before.
- Remaining uncertainty: Swiss `Importing`/`Finished` states were not exercised (no import run) but the chip is placed only inside the `LanguagePick` branch, so it is structurally absent there. AFCD chip during an active import was not exercised (import downloads a large workbook); the chip is state-independent and opening a website mid-import is side-effect-free.
