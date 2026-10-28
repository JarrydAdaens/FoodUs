# Plan: Phone-session UI corrections

Status: Complete — implementation and testing APK delivery verified 2026-10-28
Authority: Owner's PHONE-SESH-COMPLAINTS session on 2026-10-27, followed by explicit authorization to implement the plan; final acceptance recorded 2026-10-28.
Status owner: This standalone plan owns this batch's execution and acceptance. Existing milestone Story statuses remain with their milestone documents.

## Outcome and scope

Deliver all 13 collected requests as a coherent update to the personal library, diary food entry and Log UI. This plan preserves the complaint numbers so each request can be checked independently. The owner authorized implementation on 2026-10-27 and reported the phone available again. Use synthetic test data for mutation/runtime acceptance and preserve owner data. APK delivery is authorized by the standing local instructions; publication and a VCS commit remain unauthorized.

This is new follow-up work spanning completed Stories, rather than a replacement for their delivery records. Keep the historical plans for [library Story 2.38](../milestone-2/story-38-recipes-meals-products-library/plan.md), [Meals Story 2.24](../milestone-2/story-24-meals-library-editor/plan.md), [bulk actions Story 2.39](../milestone-2/story-39-diary-slot-bulk-actions/plan.md), and [shell Story 3.1](../milestone-3/story-01-tabbed-ui-shell/plan.md). The owner's new requests supersede their earlier presentation choices where explicitly changed here, including Story 38's exclusion of sorting controls. This plan does not reopen, close or invent milestone Stories, change the version, or create a second Story ledger. Reconcile scheduling with the existing trackers when this batch is adopted for execution.

Follow [laws](../../laws.md), [workflow](../../agenticworkflow.md), [domain and portion contracts](../../design.md#domain-model-shared-vocabulary), and [AI privacy boundaries](../../design.md#security-and-privacy). Keep upstream namespaces, typed identities, nutrition semantics, diary snapshots, existing library management and saved Meal semantics intact. No relay changes or external-service prerequisite. No commit, pull, push, publication or phone installation is authorized by planning.

## Request and acceptance register

| # | Requested result | Acceptance |
| --- | --- | --- |
| 1 | Personal Library tabs fill the available width equally. | Recipes / Meals / Products each occupy one third of the tab row. Names and indicators stay usable at narrow widths and larger text sizes. |
| 2 | Consistent Product, Recipe and Meal rows: title, note, calories on the right, and carbs/fat/protein; category-specific icons centred vertically. | The same row structure serves all three categories. Nonblank notes appear below the title; calories have a stable trailing position; macros use consistent formatting/order. Product, Recipe and Meal have different icons, all vertically centred against the complete row. Empty notes do not produce a blank line. Unknown, incomplete and reported nutrition stays truthful. |
| 3 | Explicit Grams / Millilitres radio selection replaces the liquid checkbox. | Product and Recipe create/edit forms expose exactly one selected option, labelled Grams (g) or Millilitres (ml). Tapping either label selects it. Existing drafts and saved values round-trip. No implied density conversion or changed diary history. |
| 4 | Remembered library sort selector beneath the top app bar. | Six choices: A–Z, Z–A, oldest created first, newest created first, calories low–high, calories high–low. Selection survives tab changes, editor returns, leaving Settings and process restart. Sorting applies to the entire filtered category, including offscreen rows. |
| 5 | Change DiarySlot inside food search using a dropdown title. | The configured slots appear in the top-bar dropdown. Selecting one changes all subsequent food-entry destinations without leaving the screen; the displayed day stays unchanged. Search, AI, creation and review callbacks all use the selected slot. |
| 6 | Add an easy-to-reach + action at the far right of bottom navigation. | Keep the three current destinations and add a fourth action. + opens diary food search, picks a slot using current local system time and configured slot windows, and does not become a selected destination tab. It works from Log, Notifications and Groups without losing their state. |
| 7 | Food-search action menu links to the existing Meal editor. | Add a labelled Meals/editor action beside Product and Recipe actions. It opens the existing reusable Meal management/editing flow, with Meals selected, and Back returns to the same food-entry state. Opening/editing never implicitly logs food. |
| 8 | Move Load Meal out of the diary-slot ellipsis menu into the card action row. | Each slot card has five actions: existing food search, Quick Add, AI and fast text, plus Load Meal. The new action uses the existing picker/apply path for that card's displayed date and slot. Remove only the old Load Meal menu item; retain Save Meal and bulk actions. Empty slots can still load a Meal. |
| 9 | Hide the entire bottom navigation while Settings is open. | No navigation bar or reserved blank bar area in Settings and its child flow, including library/editors and Log personalisation. The planned + action also disappears. Returning to Log restores the bar and correct insets. |
| 10 | Call the page Log in its personalisation UI. | Use “Customize your log page” for the subtitle and consistent “Log settings” wording for its heading/entry point. Retain source identifiers and historical paths; this is a bounded visible-copy change. |
| 11 | Align food-row nutrition and slot totals in stable columns. | Calories and carbs/fat/protein in Log food rows and meal-slot totals share horizontal anchors, like a table. Different number lengths, notes, optional fields, captured entries and recipe markers do not shift columns. Respect the existing Log nutrient-order preference by using the same ordered column definition everywhere in that card. |
| 12 | Allow text-only AI food lookup with no picture. | With AI configured, a nonblank prompt submits without an image; a photo can still submit with or without text. With neither input present, submission is disabled. Whitespace-only text does not count. Results use the same multi-food review and save flow. |
| 13 | Put food search and AI scanning into two tabs under the shared header. | First tab contains the current search field and everything below it; second contains AI image/prompt entry and review. One Back/date/meal-slot header serves both. Switching tabs preserves query, scroll, prompt, photo and review state during the session, and never submits automatically. Existing card AI action opens the AI tab directly. |

## Source findings and affected seams

Baseline inspected: `0ab77fb4`; working tree clean before planning. Paths in this section are relative to `app/src/commonMain/kotlin/com/maksimowiczm/foodyou/`, unless stated otherwise.

| Seam | Observed behavior and planned use |
| --- | --- |
| `app/ui/food/library/PersonalLibraryScreen.kt` | Uses `PrimaryScrollableTabRow`; all categories use `Restaurant` with no vertical row alignment. Rows include basis/reference/reporting text, but no note. Meals sort inline by lowercase name; Products/Recipes call `filterLibraryNames`. Replace layout through one category-aware row presentation; keep truthful basis information compact and retained detail routes accessible. |
| `app/ui/food/library/LibraryViewModel.kt`, `LibraryNutrition.kt` | Names are lightweight; nutrition is observed lazily by composed rows and cached with a 128-row bound. A visible-row cache cannot implement global calorie sorting. Extend the projection/repository boundary and invalidate nutrition sort keys when dependent definitions change. |
| `food/domain/entity/NutritionalItemName.kt`, `food/infrastructure/room/DefinitionName.kt`, Product/Recipe DAOs and repositories | Listing currently exposes only typed identity/name. Product and Recipe entities have notes but no direct creation-time column. Add needed list metadata without changing nutrition arithmetic or creating a second collection. |
| `food/domain/entity/NutritionalItemHistory.kt`, `food/infrastructure/room/FoodEventDao.kt`, `FoodEventTypeConverter.kt` | Existing Created/Imported/Downloaded history has timestamps; ordinary creation/import use it, including structured import. Prefer an aggregate first-creation/import event projection for date sorting over a new schema column. Use actual persisted event discriminators through the existing converter/constants. Missing legacy history is possible and must remain unknown. |
| `fooddiary/domain/entity/Meal.kt`, `fooddiary/infrastructure/room/MealEntity.kt` | Meals already have creation time and optional `metadata.notes`. Display that note; do not invent a second note column or alter constituent totals. Product/Recipe use their own note, with existing captured metadata as an explicit fallback where appropriate. |
| `app/ui/food/product/ProductForm.kt`, `app/ui/food/recipe/RecipeForm.kt` | Both use a “treat as liquid” checkbox. The supplied selection for #3 points at an old library icon, so target the matching controls found in source. Captured editors already have explicit reference-unit handling; retain their confirmation/unknown-reference rules. |
| `app/ui/food/diary/search/DiaryFoodSearchScreen.kt`, `DiaryFoodSearchViewModel.kt` | Header shows one slot observed from an immutable constructor parameter; no selection state exists. `FoodSearchApp` owns existing search content. Add a saved selected slot and tab in a single food-entry session, separate from reusable ingredient/library search. |
| `app/navigation/FoodYouAppNavHost.kt` | Search and AI routes separately close over their original date/slot IDs. Changing the header alone would still log into the original slot. Update callback arguments/state ownership coherently for measure, create, align and Quick Add. Add an initial Meals-tab route argument for the management action. |
| `app/navigation/ScanQuickAddHandoff.kt`, `app/ui/food/diary/aiscan/AiScanReviewSession.kt` | Current scanner acknowledges successful Quick Add through the previous entry's saved-state handle, then returns to Home when the selected queue completes. Preserve exactly-once acknowledgements and queue completion when AI becomes tab content. |
| `app/ui/food/diary/aiscan/AiScanScreen.kt`, `AiScanViewModel.kt`, `AiScanUiState.kt` | Button requires `photoCaptured && configured`; `submit()` returns immediately without JPEG; Scanning/Result/Failed require a JPEG. Text-only support needs coordinated UI, state and domain-boundary changes. |
| `ai/domain/AiFoodScanner.kt`, `AiScanPrompt.kt`, `ai/infrastructure/OpenRouterAiFoodScanner.kt` | API requires JPEG; transport always creates image content; prompt asserts there is a photo. Introduce an explicit valid text/image request representation, modality-aware wording and optional image construction, preserving shared parsing/configuration. |
| `app/ui/shell/FoodUsAppShell.kt` | Bar is unconditional and the Log NavController is private inside the host. Add explicit shell/navigation integration for + and Settings visibility. Preserve each tab's saved state; do not model + as a fourth content tab or derive visibility from localized text. |
| `common/domain/date/DateProvider.kt`, `fooddiary/domain/usecase/ObserveDiarySlotEntriesUseCase.kt` | Clock is injectable. Existing slot-window logic supports ordinary, overnight and all-day slots, with inclusive endpoints. Reuse/extract its semantics for quick-entry selection; ranking a diary display alone is not slot selection. |
| `app/ui/home/meals/card/DiarySlotCard.kt`, `DiarySlotFoodListItem.kt`, `app/ui/common/component/FoodListItem.kt` | Totals and entries use independent content-sized rows; the common row also serves other searches. Introduce a shared Log column layout/geometry passed through the affected rows rather than inadvertently changing every generic search layout. Existing Load Meal uses `onApplyClick`; move its invocation, preserving eligibility and dialogs. |
| `app/ui/home/personalization/HomePersonalizationScreen.kt`, `shared/resources/src/commonMain/composeResources/values/strings.xml` | `headline_home_settings` currently says “Home settings”; `description_home_settings` says “Customize your home page”. Correct visible Log settings copy and adjacent entry-point references. |

## Implementation decisions

### Library presentation and sorting

- Use a full-width fixed tab row with equally allocated tabs. Share a row model containing typed category/identity, title, optional note and truthful nutrition presentation. Proposed existing-asset choices: a product/package icon for Products, skillet for Recipes, fork-and-knife for Meals. Icons are decorative and centred vertically; categories remain accessible through labels/context.
- Primary row order is title, note, then compact carbs/fat/protein. Calories occupy the trailing area. Format through current energy/unit formatters; honour kcal/kJ preferences. Keep basis/reference information as a concise caption associated with the figures, without crowding out the requested note. Preserve needs-review/incomplete markers and the ability to inspect reported panels. Unknown values never become zero; do not calculate calories from macros.
- Row basis stays per 100 g/ml for metric Products, declared reference serving for reference Products, one serving for Recipes, and the saved constituent aggregate for Meals. Sort calories on the same basis displayed. Do not silently standardise incompatible bases or use a Meal's independent reported panel in place of calculated totals.
- Put a labelled sort dropdown directly below the top app bar and above search/tabs. Use one persisted library-wide enum, default A–Z, through the existing DataStore preferences pattern. Invalid/missing stored values fall back safely; no separate settings screen or per-category preference is needed.
- Case-insensitive name sorting uses stable typed-ID tie breaks. Creation sorting uses the earliest known creation/import arrival timestamp, never a later edit or an ID pretending to be a date; Meals use their stored creation time. Imported “date created” means first local arrival, not the unknown date in another app. Put unknown dates last in both directions, with deterministic name/ID ties. Legacy import/migration event meaning must be checked before including those events.
- Calories sorting compares finite, complete known energy values; unknown/incomplete/unavailable energy goes last in either direction, with deterministic ties. Keep the incomplete marker visible. Filter before sorting the complete category. Retain existing personal/custom Product filtering.
- Name/date ordering should remain lightweight. Calorie ordering needs keys for every match, including offscreen Recipes, calculated by existing resolvers. Use a bounded, cancellable hydration/projection path; release full Recipe graphs after deriving display facts. Never retain all transitive graphs or sort only whichever rows are composed. Observe changes to ingredients/yields/reported panels so dependent Recipe order refreshes; report read failures rather than treating them as zero calories. On sort change, intentionally reset that list to the top; editor returns preserve a valid anchor.
- Grams/Millilitres radio controls bind to the same existing `isLiquid` form state using a selectable group and radio accessibility roles. This is an explicit declaration of basis, with the current form reconciliation rules. It must not convert grams to millilitres, fabricate density, or relabel reference-serving quantities incorrectly.

### Shared food-entry flow and shell integration

- Retain one food-entry host with date, selected slot, selected tab and its child state. Initial route parameters seed the session; they must not overwrite later selection on recomposition. Keep the shared header outside Search/AI content. Extract AI content from its standalone scaffold rather than nesting duplicate top bars.
- Retain the old AI route as a compatibility entry/redirect to the AI tab where needed. Diary-card Search opens Search; diary-card AI opens AI. Ingredient and library discovery retain their existing dedicated context.
- Every child launch receives the current selected slot/date explicitly. Once a form or multi-food save queue starts, capture its destination so a pending save cannot be silently redirected. On return, acknowledge only a successful save by its stable review item ID. Queue cancellation/back, partial saves, retry and stale acknowledgements preserve existing behavior. Switching tabs does not reset a running request or start a second one.
- Persist lightweight destination/tab state through the existing saved-state mechanism. Photos and prompts stay scoped to the session; do not add disk persistence for sensitive scan content. Preserve surviving in-memory tab state; after process death, use the existing privacy/recovery behavior for images/results rather than promising restoration of raw JPEG in route arguments.
- Observe configured slots and validate selection before any write. A deleted/stale selection produces recovery and no write. No configured slots offers slot setup; do not supply dummy IDs. The dropdown applies to both tabs and preserves the visible date.
- For shell +, use the current local date/time at tap through `DateProvider`; ordinary card entry continues using that card's selected diary date. Timed matching slots take priority over all-day fallbacks, with rank then stable ID resolving overlap. Preserve inclusive existing window semantics, including midnight wrap. If nothing matches, ask for a slot in the entry UI; if there are no slots, offer setup. Select automatically once on entry, not continuously as the clock advances, and retain a manual choice.
- Give the shell a defined navigation action/visibility contract. Hoisting the Log controller/state to the shell boundary is a likely small solution; an acknowledged saved-state command is an alternative if required by existing lifecycle behavior. + must switch to Log and navigate once, including when Log content was inactive. Preserve three-tab identities, secondary-tab Back behavior and back stacks.
- Hide the bar for the Settings branch and its descendants based on typed navigation/back-stack context, including during editor/subpage navigation. It reappears on leaving that branch. Avoid per-screen disposable “hide/show” flags that race with transitions or leave stale padding.
- #7 means reusable Meals, consistent with Settings' current combined library. Route to that library with Meals initially selected and reuse `MealsApp`/existing editing. It is distinct from editing Breakfast/Lunch slot definitions. Entry-point Back returns to the preserved search/AI session; management carries no implicit diary write.

### AI text-only input

- Represent a submitted request with nonblank description, image, or both; reject an empty combination at the ViewModel/domain boundary as well as in the UI. Use an explicit optional-image request or equivalent typed variants, rather than an empty JPEG sentinel.
- Allow Scanning/Result/Failed to retain the request context without requiring a photo. Button enablement reacts to trimmed text, image presence, current configuration and in-flight state. Guard repeat taps. Keep retry and photo-retake behavior and keyboard visibility working in both modes.
- Assemble text-only content without any image block or fake data URL. Prompt wording describes the submitted input while preserving the established multi-food JSON shape, absolute nutrition totals, optional nutrients and unknown-value rules. Keep user system prompt and per-request text layers distinct and privacy-safe.
- Use the same scanner configuration, collection parser and review pipeline. No real endpoint call is required for deterministic verification: inject a fake scanner/Ktor mock transport. Errors must remain safe and avoid exposing raw provider bodies, keys, images or prompt content.
- Planned OCR Stories 30–37 are independent and are not prerequisites. Future image classification/OCR must be gated by actual image presence; text-only entry must never require camera permission or an OCR provider.

### Log action row and nutrient columns

- Move the existing Load Meal callback into the fifth card action without changing saved-Meal apply arithmetic, append order, confirmation or duplicate-submit guards. Keep practical touch targets and all five actions within the supported phone width. Empty/loading/no-Meals states must retain honest feedback.
- Use the same column widths, internal padding, order and numeric alignment for Log row nutrition and the slot totals. Cover ordinary Product/Recipe snapshots, captured/imported entries, Quick Add and partially known data; retain placeholder/error feedback. Totals use the same content inset as the rows, not the current mismatched card edge.
- Keep title, note/measurement and optional icons outside the numeric column allocation. Prefer consistent right-aligned numeric cells; units and labels may wrap within their cells at large font sizes without changing horizontal anchors. Preserve nutrient colours and the existing Log order/visibility preference. This request does not introduce a whole new table screen or globally rewrite other search rows.

## Dependencies and delivery steps

These are ordered work packages in this single plan; no phase files or sidecars are needed. No delegated execution is started by this planning request.

| Step | Change and independently verifiable result | Prerequisites | Effort / uncertainty |
| --- | --- | --- | --- |
| A | Add complete-library sort projections, first-arrival dates and a persisted sort enum. Synthetic data proves ordering, unknown/tie rules, personal filtering and dependent Recipe refresh. | Recheck current repositories/history event coverage. | Medium–large; global Recipe calorie projection is the largest library uncertainty. |
| B | Deliver equal-width tabs, shared category rows, notes/icons, sort selector and radio unit controls (#1–4). | A for global sorting; presentation/radio work can proceed independently. | Medium; long notes, truthful basis captions and large text need layout checks. |
| C | Add shared food-entry session/slot selection, current-time selection helper and callbacks using selected targets (#5). Prove Search/creation destinations before adding shell entry. | None; inspect route compatibility and saved-state scope. | Medium; stale closure and process-restoration errors are consequential. |
| D | Add text-only scanner request/state/transport support (#12), with deterministic request-payload and ViewModel tests. | None; compatible with C in preparation. | Medium; touches all input layers, not just button enablement. |
| E | Extract scanner content into the second tab, adapt existing AI entry routes and save acknowledgements, add Meals editor menu access (#7, #13). | C and D. | Medium–large; review queues and state retention need focused navigation tests. |
| F | Add far-right + navigation action and hide bar throughout Settings (#6, #9). | C/E establish final entry route; route visibility can be prepared earlier. | Medium; inactive Log tab and nested scaffolds need runtime proof. |
| G | Move Load Meal, implement aligned nutrition columns and correct Log personalisation copy (#8, #10–11). | None; regress entry links against E/F at integration. | Medium; captured/optional nutrition and text scaling broaden layout cases. |
| H | Run integration checks, independent review where applicable, then synthetic emulator runtime acceptance. Record item-by-item results here. | A–G. | Medium; runtime evidence depends on an available disposable emulator. Owner phone is available again; use the disposable emulator for mutation acceptance. |

Critical dependency chain: destination ownership (C) + text-only scanner (D) → tab/queue integration (E) → global + (F) → integrated acceptance (H). Library (A/B) and Log layout (G) are logically independent work, not authorization for parallel agents. Avoid a fixed completion-time promise before testing projection cost and navigation restoration.

## Verification and recovery

Planning validation: confirm all 13 requests are represented, local source links exist, historical plan/status ownership remains intact, and the diff contains only this document. No build or runtime pass is implied by source inspection.

Implementation checks, with JDK 21:

```powershell
.\gradlew.bat --no-daemon :app:testDebugUnitTest
.\harness\ui-name-coverage\run.ps1
.\gradlew.bat --no-daemon :app:assembleDebug
```

Use focused test filters while developing, then the full app unit suite once integrated. [UI-name coverage](../../../harness/ui-name-coverage/README.md) is required for changed application UI call sites; name added tabs, dropdowns, radios and popups with appropriate source scopes. Lexical coverage is not runtime selection proof.

Meaningful automated regression coverage:

- Six sort orders for every category; equal names/calories/timestamps, missing history, zero versus unknown/incomplete energy, imported/reference/reported definitions and an offscreen item that belongs first. Verify edits and transitive ingredient changes reorder Recipes. Real-Room query tests exercise event joins without duplicate rows, and DataStore/restart tests prove remembered choice and invalid-value fallback.
- Unit radio selection in create/edit, Save/Cancel, metric versus reference-serving definitions and existing Product form reconciliation. No nutrition or historical snapshot changes caused solely by replacing the control.
- Fake-clock slot choice at boundaries, overlapping/all-day/overnight windows, no match/no slots, rank ties, local-date rollover and manual selection retention. Parameterized navigation tests prove every child target uses the newly chosen slot, including after restoration and slot deletion.
- Text/photo/both/neither/whitespace cases; configured/unconfigured and in-flight guards; exact mock request payload with no image block for text-only; parser failures, empty/partial collections, retry and cancellation. Extend `AiScanPromptTest`, `AiScanReviewSessionTest` and `ScanQuickAddHandoffTest` where appropriate.
- Tab switching retains input/results, card AI opens the correct tab, one queue item saves once, failed/cancelled forms do not acknowledge, final completion returns correctly. Add the Meals action and verify edit-only behavior and Back restoration.
- + from each shell tab navigates once to today's selected slot; Settings/subpages hide the bar and reserve no old bar padding; returning restores it. Regression-check tab Back behavior and saved tab state.
- Load Meal via the fifth action appends saved constituents to the selected day/slot, including an empty slot, while Save Meal and bulk actions still work. Reuse existing Meal/bulk workflow tests instead of duplicating their arithmetic assertions.

Runtime acceptance must use synthetic records on a disposable emulator/test installation, never the owner's diary. Run focused instrumentation using the current module's connected-test task and explicit test-class filters, selecting only that emulator serial so another connected device cannot be targeted accidentally. No device commands were run during planning; implementation runtime commands targeted only the disposable emulator. If no emulator is available at execution, record those checks as pending and do not claim the visual work verified.

Inspect light/dark themes, narrow width, long names/notes, large font/display scale, keyboard/insets, touch targets, radio semantics, dropdown focus and Back order. Measure/assert matching column anchors across synthetic row types and totals; visually inspect all 13 changes. Repeat developer UI selections on changed tabs, menus and popup surfaces when runtime access is available. Retain only sanitized synthetic evidence.

No schema migration is expected: existing history supports dates, Meal metadata supports notes, and sorting is a DataStore preference. If execution proves a missing durable field unavoidable, stop that dependent slice for a documented plan revision, inspect current schema/version (43 at planning), preserve old schema files, and add real-Room upgrade/reopen and backup checks before adopting it. Never backfill fake historical dates or reconstruct owner records.

Before any later owner testing build, read [version policy](../../versioning-policy.md) and obey the session's local APK delivery instructions without copying personal destination paths into repository files. This planning turn builds/copies nothing. Release publication remains a separate authorization.

Recovery: changes should land as separable code slices matching A–G, with tests before integration. Reverting presentation/navigation must leave saved definitions, diary snapshots and sort preferences readable. Keep existing data formats/routes compatible where persisted; unknown preference values fall back. On interruption, record changed files, checks, unresolved targets and the next step here. Do not discard unrelated edits that appear after this baseline.

## Assumptions and remaining uncertainty

| Item | Evidence / assumption | Resolution |
| --- | --- | --- |
| #3 selection mismatch | Attached XML repeats the library icon; source has matching liquid checkboxes in ProductForm and RecipeForm. | Plan targets both matching forms. No phone access needed for planning; verify visible controls later. |
| Sort persistence scope | Owner requests a remembered selector, without per-tab behavior. | One library-wide sort choice, default A–Z; no extra preference UI. |
| Meaning of creation date | Product/Recipe history records creation/import arrival; Meal stores creation time. | Use first local creation/import arrival. Missing legacy dates stay unknown and sort last. Validate event coverage during A. |
| #7 Meal editor | Settings now hosts reusable Meals inside the combined library; no Meal is selected in search. | Open that library on Meals, then reuse its existing editor. Do not confuse it with DiarySlot setup. |
| Settings descendants | Owner says to hide the whole bar when Settings is open. | Apply throughout that Settings navigation branch, restoring it on exit. |
| + date and unmatched windows | Owner specifies current system clock for choosing slot, without defining date/gap behavior. | Global + uses today; retain explicit day for card-based entry. Use a manual slot chooser for a gap rather than a hidden arbitrary destination. |
| Calorie projection cost | Existing on-demand rows deliberately avoid retaining complete Recipe graphs. | Measure on a representative synthetic library during A; use bounded projection work and record any limitation. All six requested sorts remain acceptance, not optional scope. |

These are explicit implementation defaults, not new blockers or requests to interrupt the complaint session. Revise them if the owner supplies a different preference.

## Evidence and continuation

- 2026-10-27: read repository instructions, laws, relevant Design/workflow/version policy, harness contract, related plan ownership and affected source. Confirmed clean baseline `0ab77fb4`.
- Confirmed actual source causes for left-aligned tabs, repeated/top-aligned icons, checkbox unit selection, name-only sorting, static slot title/callback targets, unconditional shell navigation and photo-only scan gating.
- Confirmed existing creation/import timestamps and Meal metadata notes; prefer existing persistence rather than adding a migration.
- All 13 complaints are specified above. No app code was changed; no tests or builds were run; no device was accessed; no Story status or version changed during planning. Plan remains uncommitted.
- Next step after an implementation request: recheck Git state and this plan, reconcile any adopted tracking owner, then begin A and C/D at their testable boundaries. Runtime acceptance waits for a disposable emulator or later authorized device availability.


### Implementation checkpoint — 2026-10-28

- Implemented request groups #1–13 in source; runtime acceptance remains pending. Added complete-library sorting, persisted preference, existing first-arrival history projections, category rows/notes/icons, radio unit controls, shared Search/AI entry, slot selection, far-right shell action, Settings visibility, Meal editor access, fifth Load Meal action, Log copy and aligned nutrition cells.
- Main code compiled during initial checks. First compile exposed a primitive-array nullable check and it was corrected. The first full unit run found a test assertion comparing a ranked copy to its original instead of its ID; corrected. Second checks built main source and ran the unit task, but instrumentation compilation found an unescaped format-placeholder dollar sign in the new fixture; corrected.
- Independent evaluator reviewed source and returned PASS WITH NOTES, runtime evidence pending. Repaired its concrete findings: user-started AI review queues now capture the newly selected slot after cancellation; continuation queues retain their original destination. Library ordering retains compact initial lists instead of swapping to a one-row loading list on editor return. Text-only results can be edited/resubmitted, and image replacement cancels/invalidates an in-flight request. Final noted creation-menu icon duplication has also been corrected.
- UI naming gate passed with zero missing/duplicate names before the final small review edits; rerun at final acceptance.
- Added `PhoneSessionRulesTest`, handoff restart regression and emulator-only `PhoneSessionRegressionTest`. The latter covers Room first-arrival dates/transitive Recipe calorie refresh, production shell/library/radios/slot switching, text-only ViewModel submission/duplicate guard/editing, numeric anchors, and an explicitly flagged process-restart verifier.
- Kept fork version 3.2.40.4 and release count unchanged. Advanced Android versionCode from 136 to 137 for the planned distributed testing build under the version policy; no Story closure/publication is implied.
- Tooling uses JDK `C:/Program Files/Android/openjdk/jdk-21.0.8`. Default sandbox cannot write the user Gradle cache or start ADB, so authorized builds/device discovery use reviewed elevated execution. Both owner phone and disposable emulator are connected; all runtime mutations target only `emulator-5584`.
- Current build: `:app:testDebugUnitTest :app:assembleDebugAndroidTest :app:assembleDebug`, log `app/build/phone-sesh-checks3.log`. Once successful, deliver its metadata-selected app APK under the standing local instructions, then install app/test APK only on the emulator and run new regression methods plus the existing long-editor-return appearance check. Do not report runtime acceptance complete until actual results are reviewed.
- No commit, pull, push, publication or owner-phone installation performed. APK delivery is not yet complete at this checkpoint.


### Build and first native acceptance — 2026-10-28

- `phone-sesh-checks6.log`: full app/test APK packaging and unit task PASS (`BUILD SUCCESSFUL`, 1m43s). Parsed unit XML: 228 tests, zero failures/errors/skips. APK metadata: `foodus-dev-3_2_40_4.apk`, `3.2.40.4-dev`, versionCode 137. SHA-256 `b5d007173e5552a26d93fb396647fb3266bbeac6c626cad2ad967064275531d1`.
- App APK delivered to BOTH owner-authorized local destinations; each destination's SHA-256 matched the current source. No instrumentation APK or owner data delivered. Personal paths remain outside repository files.
- `phone-sesh-runtime1.log`: five selected emulator methods ran. PASS: text-only scanner submit/duplicate guard/result revision; real-Room first-arrival date and transitive Recipe calorie ordering; actual numeric anchor alignment; direct fifth Load Meal action appending exactly one constituent into one slot. Shell workflow passed quick entry from all three tabs, slot switching, AI prompt tab retention, Meals editor access and Settings bar hiding before a fixture-only failure: the geometry helper incorrectly required a selected tab to expose a click action. Corrected it to accept selected-tab semantics; no app source change.
- Final scroll cache now keys by category/query/sort, keeping the last ready list across edited metadata refreshes. Existing long-editor appearance regression extended to calorie sorting and a saved note edit, in both light/default and dark/large-font/narrow layouts. Existing UI inspection fixture explicitly selects its own alphabetical order to isolate its expectations from the new remembered preference.
- Next: rebuild only test APK, rerun repaired shell workflow; perform flagged process-restart verifier immediately afterward, then existing appearance, UI-inspection and bulk-action regression checks. Review synthetic captures under emulator external files and copy them only to ignored build output. Source app APK is unchanged by fixture-only repairs.

- Final shortcut audit found that single-top navigation could reuse an existing changed-slot Search session when + was tapped from another shell tab. Global + now pushes a fresh entry session, preserving the prior one underneath while reapplying the tap's date/time selection. Added that exact retained-session regression. Advanced testing versionCode to 138 because build 137 had already been delivered; the next successful owner APK replaces the same versioned filename and requires fresh destination hashes. Earlier build-137 evidence remains historical. Appearance/bulk acceptance on 137 may continue because this correction changes only shortcut session creation, not those layouts.

- `phone-sesh-test-build8.log`: test-only build PASS. `phone-sesh-restart.log`: process-stop/restart verifier PASS (1 test). `phone-sesh-runtime3.log`: all five methods PASS, including repaired full shell workflow, light/default and dark/1.5-font/narrow library appearance with calorie sorting + saved-note/long-editor scroll restoration, runtime source-selection export, and horizontal/vertical bulk copy/move/delete/empty-copy-from workflows. 235.087s, `OK (5 tests)`.
- Reviewed synthetic `library-layout.png`, `food-entry-tabs.png`, `explicit-units.png` and `numeric-columns.png`. They show equal-width tabs, centred category icon/trailing calories, shared meal-slot/date header and two entry tabs, explicit mutually exclusive units, and aligned numeric anchors. Standalone geometry-fixture status-bar overlap is fixture layout only; production appearance is separately tested. No app source changed during fixture-only repairs.
- Final naming gate PASS: `harness/ui-name-coverage/runs/20261027T133000267810Z/report.json`, zero missing/duplicate identifiers. `git diff --check` PASS. Final versionCode-138 build is in `phone-sesh-checks9.log`; rerun core/retained-session shortcut checks on that exact app before completing final delivery and status.

- Final wording audit found the personalisation entry still used the old “Home” label even though its destination title/subtitle had been corrected. It now reuses the existing “Log settings” resource. This is the last copy-only change after code-138 runtime core acceptance; advance testing versionCode to 139 for the final artifact. All behavioral source remains identical to accepted code 138. Final delivery must replace the same filename and verify fresh hashes again.

- Enlarged-font visual review (not the existing geometry assertions) exposed poor wrapping: the Product tab split its last letter and weighted macro cells split words. Corrected library tabs to the equal-width generic Tab content with reduced horizontal padding and a single-line label; library macro fields now flow as complete fields rather than being forced into three narrow cells. Log's shared numeric grid is unchanged. Added an enlarged-tab line-height assertion and scheduled a fresh appearance review. Advanced testing versionCode to 140; final artifact/hash/delivery must refer to this build. Earlier 138/139 artifacts are superseded.
- `phone-sesh-runtime4.log`: seven core/keyboard methods PASS on behavior build 138, including the retained-session shortcut regression and both short/multiline IME checks; `phone-sesh-restart-final.log` PASS. Final visual correction still needs revalidation before closure.

- Continuation checkpoint: current build is `phone-sesh-build11.log` (`testDebugUnitTest`, main APK and test APK), final intended versionCode 140. Once successful, install only on emulator-5584. Run shell regression, then force-stop + flagged remembered-sort restart verifier; then run updated appearance and UI inspection methods. Pull the newly generated appearance captures and visually confirm whole Product tab text and intact macro words. Deliver the metadata-selected app APK to both authorized destinations with fresh SHA-256 equality, and finish this primary plan only after those results. Earlier 12 distinct behavioral emulator methods have passed; no code/commit/publication/owner-phone install is pending beyond the specified final acceptance/delivery.


## Final acceptance — 2026-10-28 (authoritative completion record)

All 13 requests are implemented and accepted. This completion record supersedes pending/next-step wording in the historical planning and execution checkpoints above. No implementation or delivery work remains for this batch.

| Acceptance group | Evidence and outcome |
| --- | --- |
| #1–4 Library, categories, explicit units and remembered sorting | Six-order/unknown/tie rules in unit suite; real-Room first-arrival and transitive Recipe calorie refresh; production shell workflow; final appearance test verifies equal tab labels stay single-line under enlarged text and preserves scroll after a saved note edit with calorie sorting. Final dark/narrow and default captures reviewed: whole Product label and whole macro fields, with centred icons and right-side calories. |
| #5–7 and #13 Shared food-entry session, slot selector, global + and Meals access | Full shell regression PASS on code 140, including + from all three shell tabs and the retained changed-slot Search → Notifications → + fresh clock-selected session → Back to original prompt/slot case. Meals management opens on Meals and returns correctly. |
| #8 and #11 Direct Load Meal and numeric columns | Native production Load Meal action appends one constituent to one slot exactly once; actual screen-coordinate assertions confirm entry and total numeric anchors. Existing horizontal and vertical bulk action workflows both regress cleanly. |
| #9–10 Settings and Log wording | Production native workflow verifies the entire shell bar disappears in Settings/library/editors and returns on exit. Heading, subtitle and personalisation entry use Log wording. |
| #12 AI text-only lookup and scanner recovery | Unit request assembly proves no image content for text-only input. Native configured fake-scanner flow proves photo-free submit, duplicate guard, result review and text revision/resubmit. Short/multiline keyboard regressions pass. No live AI request was made. |
| Persistence and inspection | Explicit process-stop/restart sort verifier PASS on code 140. Actual source selection exports PASS after the new tab/row layout; lexical naming gate also passes. |

Final build and artifact:

- JDK 21; `phone-sesh-build11.log`: `BUILD SUCCESSFUL` (1m47s) for `:app:testDebugUnitTest :app:assembleDebug :app:assembleDebugAndroidTest`.
- Parsed XML: **228 unit tests, 0 failures, 0 errors, 0 skipped**. Across the recorded native runs: **12 distinct Android methods, all latest outcomes PASS**. Machine-readable, sanitized receipt: ignored `app/build/phone-session-acceptance.json`.
- Final exact artifact runtime logs: `phone-sesh-final-shell140.log` (`OK (1 test)`, 56.504s), `phone-sesh-final-restart140.log` (`OK (1 test)`), `phone-sesh-final-appearance140.log` (`OK (2 tests)`, 67.195s). Other unchanged behavioral checks are in `phone-sesh-runtime4.log` (seven core/keyboard methods PASS) and `phone-sesh-runtime3.log` (both bulk layouts PASS).
- AGP metadata and independent SDK `aapt dump badging` agree: package `io.github.jarrydadaens.foodus.dev`, label FoodUs Dev, versionName `3.2.40.4-dev`, **versionCode 140**, file `foodus-dev-3_2_40_4.apk`.
- APK SHA-256: `1497998ab5ec58aa8ade3ac2192d482d953d88620d5569e39f1087f9a28f415a`.
- Final metadata-selected app APK copied to **both** owner-authorized local delivery folders; both destination SHA-256 hashes equal the source. The same filename was replaced; older versioned builds were retained. No test APK or owner data was delivered. Personal destination paths remain outside repository files/history.
- Final source review and visual recheck completed with no unresolved source blocker. Earlier enlarged-word wrapping failure was corrected and recaptured; it is not a pending limitation.

Boundaries: all runtime mutations used synthetic emulator data; the owner's phone was not installed to or modified. Historical dates missing from legacy records remain unknown under the documented sort fallback. Nutrition calculations and saved diary snapshots remain under existing domain contracts; no schema migration, relay change, new dependency or publication was introduced. Fork version/release count and Story statuses remain unchanged; only Android installation order advanced for the successive testing artifacts. Implementation was accepted before committing; the owner subsequently authorized a local commit through `commit-log --commit`. No pull or push was run.

## Fresh owner testing build — 2026-10-28

- Owner requested a fresh installable APK after local implementation commit `b0bc1063`.
- Advanced Android installation sequence from 140 to **141** under the version policy; fork version remains `3.2.40.4` and no Story or release status changed.
- JDK 21, `:app:assembleDebug`: **BUILD SUCCESSFUL** in 23s. Log: ignored `app/build/phone-sesh-fresh141-build.log`. This packaging-only change does not alter the previously accepted application behavior; no additional runtime test or owner-phone installation was performed.
- AGP output metadata and SDK `aapt` agree: `io.github.jarrydadaens.foodus.dev`, `3.2.40.4-dev`, versionCode **141**, `foodus-dev-3_2_40_4.apk` (97,679,005 bytes).
- SHA-256: `49494993885f9a51189961d846c98234cc233e611676f4aae13600d60bef8e29`. Fresh app APK copied to both authorized local delivery folders, with matching destination hashes. Existing older versioned filenames retained; personal destination paths remain outside repository history.
- Sanitized receipt: ignored `app/build/fresh141-delivery.json`. Build-number and delivery-record edits remain uncommitted; no pull, push or publication performed.
