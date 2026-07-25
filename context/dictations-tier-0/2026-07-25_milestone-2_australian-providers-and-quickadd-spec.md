# FoodYou Fork — Milestone 2 Feature Specification

## 1. Milestone Summary

Milestone 2 adds two major capability groups to the FoodYou fork:

1. **Australian food-data providers**
   - Investigate the existing provider architecture.
   - Add support for the Australian Food Composition Database.
   - Add support for FoodSwitch, subject to confirming how its data can be accessed.
   - Store provider data locally for fast, offline-first searches.
   - Add provider-specific update checks and full-database refreshes.
   - Ensure provider updates never alter previously logged food history.

2. **Expanded Quick Add workflow**
   - Capture more useful nutritional and quantity information.
   - Allow Quick Add entries to contain an optional description.
   - Add fibre, serving count, and weight.
   - Allow new and historical Quick Add entries to be promoted into custom products or recipes.
   - Preserve the original diary entry when promotion occurs.

This milestone does **not** include supermarket scraping.

---

# 2. Product Goals

## 2.1 Australian food coverage

Make Australian food searches substantially more useful by adding first-class Australian data sources alongside the existing providers.

The application should continue to support the current providers:

- Open Food Facts
- FoodData Central / USDA
- Swiss Food Composition Database
- Manual Database Import

The new providers should appear as normal selectable providers rather than as one-off external scripts.

## 2.2 Offline-first performance

Food search should use a locally stored database rather than requiring a network request for every lookup.

The desired model is similar to offline map data:

1. Download a provider dataset.
2. Import it into the application's local database.
3. Search locally during normal use.
4. Occasionally check whether a newer source dataset exists.
5. Replace the local provider dataset when the user chooses to update it.

## 2.3 Stable historical records

Previously logged foods must remain stable.

A provider update must not retroactively change:

- Calories previously logged.
- Protein, fat, carbohydrate, or fibre previously logged.
- Serving quantities previously logged.
- Weight previously logged.
- Custom products.
- Custom recipes.
- Quick Add entries already present in diary history.

Diary records must therefore contain a nutritional snapshot rather than a live reference whose values change when a provider database is refreshed.

## 2.4 Faster capture with later refinement

Quick Add should remain fast enough for uncertain or approximate foods, while allowing the user to improve the data later.

Typical workflow:

1. Quickly record an estimated bakery item or meal.
2. Include the information currently known.
3. Continue using the diary without creating a complete reusable product or recipe.
4. Return to the historical entry later.
5. Promote it into a reusable custom product or recipe.
6. Add further information in the existing product or recipe editor.

---

# 3. Non-Goals

The following are explicitly outside Milestone 2:

- Scraping Woolworths, Coles, bakeries, or other retailers.
- Circumventing supermarket websites or loyalty-card systems.
- Building a new crowdsourced food platform.
- Replacing Open Food Facts.
- Creating a continuously synchronised remote database.
- Performing a remote API request for every food search.
- Incremental or delta provider updates unless already supported by FoodYou.
- Automatically changing historical diary entries after provider updates.
- Automatically changing a promoted product or recipe when its original Quick Add entry changes.
- Barcode scanning UI, unless it already exists and only requires the imported provider data.
- Automatic duplicate merging across providers.
- Automatic nutritional estimation using AI.

---

# 4. Delivery Phases

## Phase 1 — Provider Architecture Spike

Investigate how FoodYou currently implements food providers and manual database imports.

The spike is part of the feature delivery, not a separate optional research task.

### Required investigation areas

Determine:

- Where providers are registered.
- Whether providers implement a shared interface, abstract class, service contract, or hard-coded switch.
- How provider settings are persisted.
- How provider enable/disable checkboxes work.
- Whether disabled providers remain installed but are excluded from search.
- How the manual database import workflow is implemented.
- How downloaded files are parsed and imported.
- Whether imports write into a shared table or provider-specific tables.
- How records retain their provider identity.
- How source record IDs are stored.
- How barcodes are stored and indexed.
- How full-text or tokenised food search works.
- How provider priority affects duplicate or similar search results.
- Whether provider imports are transactional.
- Whether a provider can safely replace all of its imported rows.
- Whether existing diary entries reference provider rows directly or copy their nutritional values.
- Whether custom products and recipes use the same storage model as provider foods.
- Whether the Quick Add create screen and historical Quick Add edit screen use the same component.
- Where schema migrations are defined.
- How application updates migrate old Quick Add records.
- Whether background downloads already exist.
- How import progress and errors are currently surfaced.

### Spike output

Produce a concise technical note containing:

- Relevant classes and files.
- Provider registration flow.
- Import pipeline.
- Persistence model.
- Search integration.
- Provider enablement model.
- Historical diary data model.
- Recommended implementation pattern for the two Australian providers.
- Risks or blockers.
- Any required schema migrations.
- Any reusable code that should be extracted from Manual Database Import.

### Preferred implementation direction

Where practical, extract a reusable provider-import pipeline instead of copying a large block of provider-specific code twice.

A provider definition should ideally supply:

- Provider identifier.
- Display name.
- Source metadata URL.
- Dataset download URL or acquisition strategy.
- File format.
- Parser.
- Record mapper.
- Dataset version or publication-date detector.
- Update-check implementation.
- Optional barcode support.
- Validation rules.

The common infrastructure should handle:

- Downloading.
- Temporary-file management.
- Progress reporting.
- Validation.
- Database transactions.
- Replacing old provider rows.
- Persisting version metadata.
- Persisting last-check timestamps.
- Error handling.
- Enabling and disabling the provider.

---

## Phase 2 — Australian Food Composition Database Provider

Add an Australian Food Composition Database provider.

### Provider identity

Use a stable internal provider key, for example:

`australian_food_composition_database`

The display name should clearly identify the Australian source.

### User-visible behaviour

The provider must:

- Appear in the provider/settings list.
- Have an enable/disable checkbox.
- Support initial dataset download or import.
- Store imported foods locally.
- Participate in normal food searches when enabled.
- Be excluded from normal food searches when disabled.
- Preserve the imported database while disabled unless the user explicitly removes it.
- Display import state.
- Display update state.
- Display the last successful update-check date.
- Allow a full refresh when a newer dataset is available.

### Data mapping

Map every source field that FoodYou can represent without inventing values.

At minimum, investigate mapping for:

- Source food identifier.
- Food name.
- Food description.
- Energy in kilocalories.
- Protein in grams.
- Fat in grams.
- Carbohydrate in grams.
- Fibre in grams.
- Serving amount, where supplied.
- Serving unit, where supplied.
- Weight basis, such as values per 100 g.
- Brand, where applicable.
- Category or food group.
- Barcode, only where supplied by the source.
- Source version or publication date.
- Provider provenance.

Do not fabricate barcodes or serving information that is absent from the source.

### Unit normalisation

The import must normalise units into FoodYou's canonical internal representation.

Expected canonical units:

- Energy: kilocalories.
- Macronutrients: grams.
- Fibre: grams.
- Weight: grams.
- Liquid volume: millilitres, if supported.
- Standard nutrient basis: preserve whether data is per 100 g, per serving, or another source basis.

Any conversion must be deterministic and covered by tests.

### Import safety

The provider import should:

1. Download into a temporary location.
2. Validate the file before modifying the active provider data.
3. Parse the complete dataset.
4. Reject clearly invalid or incompatible files.
5. Import into staging storage or inside a database transaction.
6. Replace the previous provider dataset only after successful validation.
7. Roll back if parsing or persistence fails.
8. Leave the previously working dataset intact after a failed update.

### Acceptance criteria

- The provider is visible in settings.
- The provider can be enabled and disabled.
- A first-time import completes successfully with valid source data.
- Imported foods appear in search.
- Disabled provider foods do not appear in search.
- Re-enabling the provider restores them without requiring another download.
- Nutritional units are correctly mapped.
- The provider identity is retained on every imported record.
- Updating the provider does not alter historical diary records.
- A failed refresh does not destroy the last valid imported dataset.

---

## Phase 3 — FoodSwitch Provider

Add a FoodSwitch provider using the same provider infrastructure.

### Important feasibility requirement

The earlier product discussion identified FoodSwitch as an Australian packaged-food source, but Milestone 2 must not assume that a public bulk download or unrestricted API is available.

The Phase 1 spike must verify:

- Whether FoodSwitch exposes a usable API or downloadable dataset.
- Authentication requirements.
- Personal-use access requirements.
- Rate limits.
- Whether bulk acquisition is permitted by the available interface.
- Whether barcodes and nutrition panels are included.
- Whether dataset version or publication metadata is exposed.
- Whether the source can support the offline-first full-refresh model.

If direct FoodSwitch data access is unavailable, record the blocker clearly. Do not silently replace it with scraped supermarket data.

### Provider identity

Use a stable internal provider key, for example:

`foodswitch_australia`

### Intended user-visible behaviour

Subject to data access being feasible, the provider must:

- Appear in the provider/settings list.
- Have an enable/disable checkbox.
- Download or retrieve the Australian packaged-food dataset.
- Store products locally.
- Participate in normal searches when enabled.
- Support barcode lookup when the source provides barcodes.
- Display source version or publication date when available.
- Display the last successful update-check date.
- Support full refreshes.

### Data mapping

Investigate mapping for:

- Source product identifier.
- Barcode.
- Product name.
- Brand.
- Package size.
- Serving size.
- Energy.
- Protein.
- Fat.
- Carbohydrate.
- Fibre.
- Values per serving.
- Values per 100 g or 100 mL.
- Product category.
- Ingredient or description fields, where supported.
- Source provenance.
- Dataset version or record update date.

### Duplicate behaviour

Milestone 2 does not require automatic merging with Open Food Facts or another provider.

Equivalent records from multiple enabled providers may appear separately, but each result must retain visible or inspectable provenance so the user can understand its source.

### Acceptance criteria

- Feasibility is verified before implementation.
- Access limitations are documented.
- The provider uses the common provider infrastructure.
- Provider records retain source identity.
- Barcodes are imported only when supplied.
- Provider enablement controls search participation.
- Refreshes do not mutate historical records.
- Failed refreshes preserve the last valid provider dataset.

---

## Phase 4 — Provider Update Check and Refresh UI

Add provider-specific update controls.

### Required controls

Each downloadable provider should show:

- Provider name.
- Enabled/disabled checkbox.
- Installation/import state.
- Current local dataset version or publication date, where known.
- Last successful update-check date and time.
- A `Check for updates` button.
- Download/update status.
- Error state when a check or refresh fails.

### Check-for-updates behaviour

When the user selects `Check for updates`:

1. Contact the provider's update metadata endpoint, download page, manifest, or equivalent source.
2. Determine the latest available dataset version.
3. Compare it with the locally imported version.
4. Save the date and time of the successful check.
5. Display one of the defined outcomes.

### Required outcomes

#### Database is current

Display a clear message equivalent to:

`Your database is up to date.`

Also update the displayed last-checked date.

#### New database is available

Display a clear message equivalent to:

`A new database is available.`

Offer an explicit action to download and replace the local provider dataset.

Do not replace the database merely because the update check found a newer version unless the application already has an established automatic-update policy.

#### Check fails

Display a useful error without deleting or disabling the local provider database.

Examples:

- Network unavailable.
- Source unavailable.
- Authentication failed.
- Update metadata could not be interpreted.
- Download failed.
- Dataset validation failed.
- Import failed.

A failed check should not be recorded as a successful `last checked` event.

The application may separately store `last attempted check` if useful, but the primary date shown to the user should be the last successful check.

### Full refresh behaviour

A refresh may download and re-import the entire provider dataset.

Delta updates are not required.

The refresh must:

- Keep the old provider database usable until the new dataset is validated.
- Show progress where practical.
- Be cancellable if the existing infrastructure supports cancellation.
- Replace only the selected provider's imported rows.
- Preserve other providers.
- Preserve custom products.
- Preserve recipes.
- Preserve Quick Add entries.
- Preserve all diary history.
- Persist the newly installed dataset version.
- Persist the import date.

### Version comparison

Use the strongest stable version identifier exposed by the source, in this order:

1. Explicit dataset version.
2. Publication date.
3. Source-provided revision identifier.
4. File checksum plus source modification timestamp.
5. File checksum alone.

Avoid comparing only the local download date, because that does not identify source changes.

### Atomic replacement

The new provider dataset should become active only after:

- Download completion.
- File validation.
- Parse completion.
- Required-field validation.
- Successful persistence.
- Index creation or update.

If any step fails, retain the previous active dataset.

### Acceptance criteria

- The user can check each provider independently.
- A successful check persists the date.
- The date remains visible after restarting the app.
- Current datasets report that they are up to date.
- New datasets offer a refresh.
- A refresh replaces only that provider's data.
- Historical entries remain unchanged.
- Network and import failures leave the old dataset usable.
- The application can recover from interruption during download or import.

---

# 5. Quick Add Expansion

## 5.1 Existing baseline

Quick Add currently allows the user to enter a basic food-log item without creating a reusable product or recipe.

The existing form is understood to contain:

- Name.
- Kilocalories.
- Protein.
- Fat.
- Carbohydrate.

The implementation spike must confirm the exact current fields and whether the same form/component is used to edit historical Quick Add diary entries.

## 5.2 New fields

Add the following fields to Quick Add.

### Description

- Optional.
- Free-form text.
- Intended for context that does not fit in the name.
- Examples include where the food came from, flavour, ingredients, or why the estimate was chosen.
- Must persist with the diary entry.
- Must be visible when the entry is edited.
- Must be carried into a promoted product or recipe where an equivalent destination field exists.
- Absence of a description must not block saving.

### Fibre

- Optional unless the current nutrition model requires a numeric default.
- Stored in grams.
- Must accept decimal values.
- Must reject negative values.
- Should default to empty or zero according to existing FoodYou conventions.
- Must persist in historical entries.
- Must be included in promotion mapping.

Use the Australian spelling `fibre` in user-facing text unless the existing application consistently uses `fiber`.

Internal property naming may follow the existing codebase conventions.

### Number of servings

- Optional user input.
- Must support decimal quantities where the existing diary supports partial servings.
- Must reject zero or negative values.
- Recommended default: `1`.
- Must persist with the Quick Add entry.
- Must remain visible and editable later.
- Must be used when promoting the entry where the target product or recipe model supports serving quantities.

### Weight

- Optional.
- Stored canonically in grams.
- Must accept decimal values.
- Must reject zero or negative values when supplied.
- User-facing default unit should be grams.
- Must persist with the Quick Add entry.
- Must remain visible and editable later.
- Must be carried into promotion where the destination model supports total weight, serving weight, or yield.

## 5.3 Nutritional-value semantics

To preserve backward compatibility, the recommended Milestone 2 rule is:

- Kilocalories and macronutrients continue to represent the nutritional totals currently entered by Quick Add.
- Serving count and weight add quantity context.
- Existing Quick Add entries remain valid without serving count, weight, fibre, or description.

The implementation spike must confirm whether current Quick Add nutrition represents:

- The entire logged entry.
- One serving multiplied by diary quantity.
- A per-100 g basis.
- Another internal convention.

Do not silently change the meaning of existing fields.

If the current implementation stores nutrition per serving and a separate diary quantity, follow that existing model rather than introducing a conflicting one.

## 5.4 Proposed field order

Recommended form order:

1. Name.
2. Description.
3. Number of servings.
4. Weight in grams.
5. Kilocalories.
6. Protein in grams.
7. Fat in grams.
8. Carbohydrate in grams.
9. Fibre in grams.
10. Promotion actions.
11. Save/cancel actions.

The exact layout may be adjusted to match existing FoodYou UI conventions.

## 5.5 Validation

### Required validation

- Name must satisfy the existing Quick Add requirement.
- Kilocalories must follow existing validation.
- Nutrient values cannot be negative.
- Fibre cannot be negative.
- Serving count must be greater than zero when supplied.
- Weight must be greater than zero when supplied.
- Decimal input must respect the application's locale-handling rules.
- Empty optional fields must be supported.
- Invalid input must show a field-level error where the existing UI supports it.

### Consistency validation

Do not block saving merely because the entered kilocalories do not exactly equal the energy mathematically implied by protein, fat, and carbohydrate.

Quick Add exists partly for estimates, packaged-label rounding, bakery foods, and incomplete information.

A non-blocking warning may be added later, but it is not required for Milestone 2.

---

# 6. Promotion Workflow

## 6.1 Promotion actions

Add two distinct actions to the Quick Add UI:

- `Promote to Product`
- `Promote to Recipe`

These actions should be available:

- While creating a new Quick Add entry.
- While editing an existing Quick Add entry.
- While editing a historical diary entry that originated from Quick Add.

If the same UI component is reused for creation and editing, add the actions there once and conditionally apply the correct behaviour.

## 6.2 Promote to Product

When selected:

1. Validate the Quick Add fields.
2. Preserve or save the current diary entry according to existing application behaviour.
3. Create a new custom-product draft.
4. Prepopulate every compatible product field from the Quick Add entry.
5. Open the existing product creation/editor workflow.
6. Allow the user to add missing information.
7. Add the product to the custom product collection only when the product editor is saved.

### Suggested field mapping

| Quick Add field | Custom product destination |
|---|---|
| Name | Product name |
| Description | Product description or notes |
| Kilocalories | Energy |
| Protein | Protein |
| Fat | Fat |
| Carbohydrate | Carbohydrate |
| Fibre | Fibre |
| Number of servings | Serving quantity or yield, where supported |
| Weight | Product weight or serving weight, where supported |

Do not invent values for fields not present in the Quick Add entry.

## 6.3 Promote to Recipe

When selected:

1. Validate the Quick Add fields.
2. Preserve or save the current diary entry according to existing application behaviour.
3. Create a new custom-recipe draft.
4. Prepopulate every compatible recipe field.
5. Open the existing recipe creation/editor workflow.
6. Allow the user to add ingredients, instructions, serving details, and other supported information.
7. Add the recipe to the custom recipe collection only when the recipe editor is saved.

### Suggested field mapping

| Quick Add field | Custom recipe destination |
|---|---|
| Name | Recipe name |
| Description | Recipe description or notes |
| Kilocalories | Initial total or serving nutrition according to the existing recipe model |
| Protein | Initial protein value |
| Fat | Initial fat value |
| Carbohydrate | Initial carbohydrate value |
| Fibre | Initial fibre value |
| Number of servings | Recipe yield |
| Weight | Total recipe weight or serving weight, according to the existing recipe model |

The spike must determine how FoodYou recipes calculate nutrition.

If recipe nutrition is normally derived from ingredients, the promoted values should not corrupt that model. Suitable implementation options include:

- Creating a recipe with an initial manual-nutrition mode.
- Creating a recipe draft whose nutritional estimate remains as metadata until ingredients replace it.
- Creating a single placeholder ingredient representing the original Quick Add estimate.

The selected approach must be documented and consistent with existing FoodYou behaviour.

## 6.4 Historical promotion

The user must be able to:

1. Open a previous diary entry created through Quick Add.
2. Enter its edit screen.
3. See `Promote to Product` and `Promote to Recipe`.
4. Select either action.
5. Use the historical Quick Add data as the initial data for the new reusable item.
6. Complete and save the product or recipe.
7. Find it in the relevant custom collection afterwards.

## 6.5 Source-entry preservation

Promotion must not replace the original diary entry with a live reference to the new product or recipe.

The original diary entry should remain a historical snapshot.

This prevents future edits to the promoted product or recipe from changing what was recorded previously.

## 6.6 Cancellation behaviour

If the user starts promotion and then cancels the product or recipe editor:

- Do not create the custom product or recipe.
- Do not delete the original Quick Add entry.
- Do not lose edits already saved to the Quick Add entry.
- Return to a sensible previous screen according to existing navigation conventions.

## 6.7 Repeated promotion

Milestone 2 does not require duplicate detection.

A Quick Add entry may be promoted more than once unless the existing product architecture provides a clean way to track promotion state.

Do not add a hidden one-time restriction without explicit UX.

A later milestone may add:

- `Promoted to Product` status.
- Links to created products or recipes.
- Duplicate warnings.
- Reopen-promoted-item actions.

---

# 7. Persistence and Schema Changes

## 7.1 Quick Add schema

The Quick Add persistence model needs fields equivalent to:

- `Description`, nullable text.
- `FibreGrams`, nullable decimal or existing nutrient scalar type.
- `ServingCount`, nullable decimal with a logical default of 1.
- `WeightGrams`, nullable decimal.
- Existing nutritional values.
- Existing diary-entry identity and timestamps.

Use the codebase's existing precision and numeric type for nutrient values.

Avoid binary floating-point if the existing model uses decimal storage for food quantities.

## 7.2 Backward-compatible migration

Existing Quick Add entries must remain readable after the migration.

Recommended migration defaults:

- Description: `null`.
- Fibre: `null` or `0`, matching existing nutrient conventions.
- Serving count: `1` when absence would otherwise break calculations.
- Weight: `null`.

The migration must not rewrite existing nutritional totals.

## 7.3 Provider metadata schema

Persist provider-level metadata equivalent to:

- Provider ID.
- Enabled state.
- Installed state.
- Locally installed dataset version.
- Locally installed publication date.
- Source checksum, where used.
- Import date.
- Last successful update-check date.
- Optional last attempted check date.
- Last error summary.
- Current import state.

## 7.4 Food provenance

Every imported provider record should retain:

- Provider ID.
- Source record ID.
- Source version or record revision where available.
- Import batch/version ID.

This enables provider-specific replacement without affecting other providers or custom foods.

## 7.5 Historical diary snapshots

Confirm that diary entries store copied values for:

- Display name.
- Description where applicable.
- Kilocalories.
- Protein.
- Fat.
- Carbohydrate.
- Fibre.
- Serving count.
- Weight.
- Unit information.
- Source provenance where useful.

If historical entries currently depend on mutable provider rows, fixing that is a prerequisite for safe provider refreshes.

---

# 8. Search and Provider Enablement

## 8.1 Enabled providers

Only enabled providers should participate in normal search.

Disabling a provider should not:

- Delete its local data.
- Alter diary history.
- Alter custom foods.
- Require another download when re-enabled.

## 8.2 Result provenance

Search results should retain provider identity.

Where the current UI supports it, show the provider name or source badge.

This matters because equivalent foods may appear from:

- Open Food Facts.
- USDA.
- Swiss data.
- Australian Food Composition Database.
- FoodSwitch.
- Custom products.
- Custom recipes.

## 8.3 Barcode search

Where the provider supplies barcodes:

- Import and index them.
- Preserve leading zeroes.
- Treat barcodes as strings, not numeric values.
- Avoid locale formatting.
- Allow the existing barcode workflow to find them.

Do not fabricate or infer barcodes.

---

# 9. Error Handling

## 9.1 Provider download errors

Handle:

- No network.
- HTTP failure.
- Authentication failure.
- Access denied.
- Rate limiting.
- Redirect or endpoint changes.
- Partial downloads.
- Insufficient disk space.
- Cancellation.

## 9.2 Provider data errors

Handle:

- Unexpected file format.
- Missing required columns.
- Unsupported encoding.
- Invalid numeric values.
- Invalid units.
- Empty dataset.
- Duplicate source IDs.
- Corrupt archive.
- Schema changes.
- Impossible record counts.

## 9.3 Import errors

The UI should report:

- Which provider failed.
- Whether the existing local database remains available.
- A concise failure reason.
- Whether retry is possible.

Technical details should be logged for diagnostics without exposing an unreadable stack trace as the primary user message.

## 9.4 Quick Add errors

The Quick Add form should retain user-entered values after validation failure.

Promotion errors must not destroy the source diary entry.

---

# 10. Testing Requirements

## 10.1 Provider unit tests

Cover:

- Version comparison.
- Publication-date comparison.
- Checksum comparison.
- Nutrient-unit conversion.
- Decimal parsing.
- Barcode preservation.
- Source-ID mapping.
- Missing optional fields.
- Invalid required fields.
- Duplicate source records.
- Provider enablement filtering.

## 10.2 Provider integration tests

Cover:

- Initial import.
- Re-import of the same version.
- Import of a newer version.
- Import rollback after failure.
- Restart after successful import.
- Restart during or after interrupted import.
- Enabling and disabling.
- Search before and after refresh.
- Coexistence with existing providers.
- Provider-specific replacement.
- Preservation of custom products and recipes.
- Preservation of diary history.

## 10.3 Quick Add unit tests

Cover:

- Saving with only existing fields.
- Saving with all new fields.
- Empty optional description.
- Decimal fibre.
- Decimal serving count.
- Decimal weight.
- Rejection of negative values.
- Rejection of zero serving count.
- Rejection of zero weight when supplied.
- Migration of old entries.
- Editing migrated entries.

## 10.4 Promotion integration tests

Cover:

- New Quick Add to Product.
- New Quick Add to Recipe.
- Historical Quick Add to Product.
- Historical Quick Add to Recipe.
- Field mapping.
- Cancellation.
- Product save failure.
- Recipe save failure.
- Repeated promotion.
- Editing the promoted item without changing the historical entry.
- Editing the historical entry without changing the promoted item.

## 10.5 Regression tests

Confirm that Milestone 2 does not break:

- Existing Quick Add.
- Existing manual database imports.
- Open Food Facts.
- USDA provider authentication.
- Swiss provider imports.
- Existing product creation.
- Existing recipe creation.
- Diary totals.
- Barcode lookup.
- Provider enablement.
- Existing historical entries.

---

# 11. UX Acceptance Scenarios

## Scenario A — Log an estimated bakery pie

1. Open Quick Add.
2. Enter `Beef and onion bakery pie`.
3. Enter estimated kilocalories and macros.
4. Enter an optional description such as the bakery source and approximate size.
5. Enter a weight estimate.
6. Save.
7. Confirm the diary displays the entry and totals correctly.

## Scenario B — Record a multi-serving food

1. Open Quick Add.
2. Enter the food name.
3. Enter total or per-serving nutrition according to the confirmed existing model.
4. Enter the number of servings.
5. Enter total weight.
6. Save.
7. Reopen the entry and confirm all values persist.

## Scenario C — Promote a new Quick Add to Product

1. Enter Quick Add details.
2. Select `Promote to Product`.
3. Confirm the custom-product editor opens with mapped values.
4. Add any missing product metadata.
5. Save.
6. Confirm the product appears in the custom product collection.
7. Confirm the diary entry remains unchanged.

## Scenario D — Promote an old Quick Add to Recipe

1. Open diary history.
2. Select an older Quick Add entry.
3. Open edit.
4. Select `Promote to Recipe`.
5. Confirm mapped values appear in the recipe editor.
6. Add ingredients or other recipe information.
7. Save.
8. Confirm the recipe appears in the custom recipe collection.
9. Confirm the old diary record remains unchanged.

## Scenario E — Check an Australian provider

1. Open provider settings.
2. Locate the Australian provider.
3. Read the last successful check date.
4. Select `Check for updates`.
5. Confirm the application reports that the database is current.
6. Restart the application.
7. Confirm the updated last-check date persists.

## Scenario F — Refresh an Australian provider

1. Check for updates.
2. Receive a new-database-available result.
3. Start the refresh.
4. Complete the download and import.
5. Confirm the local provider version changes.
6. Confirm new source data appears in search.
7. Confirm historical diary entries retain their original values.

## Scenario G — Failed provider refresh

1. Begin a refresh.
2. Simulate a corrupt or invalid downloaded dataset.
3. Confirm the import fails.
4. Confirm the previous provider database remains active.
5. Confirm diary history and custom foods remain unchanged.
6. Confirm the error is visible and retry is possible.

---

# 12. Definition of Done

Milestone 2 is complete when:

- The provider architecture spike is documented.
- Australian Food Composition Database integration works through the normal provider UI.
- FoodSwitch access feasibility is verified and either implemented or recorded as a concrete external blocker.
- New providers can be enabled and disabled.
- Provider data is stored locally.
- Food search works without a network request after import.
- Provider update checks persist the last successful check date.
- New provider datasets can be fully refreshed.
- Failed refreshes preserve the previous working data.
- Historical food logs remain immutable.
- Quick Add supports description, fibre, serving count, and weight.
- Quick Add supports promotion to custom product.
- Quick Add supports promotion to custom recipe.
- Historical Quick Add entries can also be promoted.
- Promotion does not mutate or replace historical diary snapshots.
- Existing Quick Add entries survive schema migration.
- Automated tests cover provider imports, refreshes, migrations, new fields, and promotion.
- No supermarket scraping is introduced.

---

# 13. Implementation Risks and Decisions to Resolve

The implementation agent should explicitly resolve and document these points rather than guessing:

1. Whether FoodSwitch exposes a usable data-access mechanism.
2. Whether FoodYou already has a general provider interface.
3. Whether Manual Database Import is reusable or needs refactoring.
4. Whether provider records share one table or use separate storage.
5. Whether diary entries are already immutable snapshots.
6. Whether Quick Add create and edit use the same UI component.
7. Whether Quick Add nutrition is stored as total, per serving, or per 100 g.
8. Whether recipes support manually supplied nutrition.
9. How Quick Add estimates should map into ingredient-derived recipes.
10. Whether fibre already exists elsewhere in the domain model under another name.
11. How serving count and weight are represented in existing products and recipes.
12. Which source metadata can reliably identify a new database version.
13. Whether imports can be made atomic with the current persistence layer.
14. Whether database indexes need rebuilding after a full provider refresh.
15. How provider provenance is shown in search results.

All decisions should preserve existing behaviour unless a deliberate migration is documented.
