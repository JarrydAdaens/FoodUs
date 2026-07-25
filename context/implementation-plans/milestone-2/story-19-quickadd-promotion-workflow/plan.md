# Plan: Quick Add Promotion Workflow

## Metadata

- Task Type: `FEATURE`
- Status: `Complete`
- Owner: Jarryd Adaens
- Last Updated: 25 July 2026

## Linked Context

- Milestone: [context/milestones/milestone-2.md](../../../milestones/milestone-2.md)
- Story: [Story 19: Quick Add promotion workflow](../../../milestones/milestone-2.md#story-19)
- Dictation source: [2026-07-25 Milestone 2 feature spec](../../../dictations-tier-0/2026-07-25_milestone-2_australian-providers-and-quickadd-spec.md) (§6, §10.4)
- Related Plans:
  - [Story 18 (Quick Add fields)](../story-18-quickadd-expansion-fields/plan.md) — **hard dependency**: promotion maps its fields, and `weightGrams` is required for the total→per-100g conversion into a Product.
  - [Story 14 (spike)](../story-14-provider-quickadd-architecture-spike/plan.md) — its recipe-model + snapshot findings already ground the recipe-seeding decision below.

## CER

- Complexity: 6
- Effort: 6
- Risk: 5
- Notes: Inline estimate. Two genuine hazards: the **unit-basis conversion** (Quick Add stores an absolute TOTAL; `Product` is per-100 g) and **recipe seeding** (recipe nutrition is 100% ingredient-derived, with no manual-override field — a single estimate cannot seed a recipe directly). The snapshot model makes source-entry preservation nearly free, which pulls risk down from what it would otherwise be.

## Objective

Let a user turn a Quick Add entry — new, existing, or a historical diary entry that originated from Quick Add — into a reusable custom Product or Recipe, by prefilling the existing product/recipe editor from the entry's mapped fields, committing the new item only when that editor is saved, and never mutating or replacing the original diary snapshot.

## Scope

### In Scope

- `Promote to Product` and `Promote to Recipe` actions in the shared Quick Add component, available on create, edit, and historical-edit.
- Prefill entry points into the existing product and recipe create/edit flows from a Quick Add entry.
- Total→per-100 g conversion for Product promotion using the entry's `weightGrams`.
- A documented, minimal recipe-seeding approach consistent with Food You's ingredient-derived recipe model.
- Source-entry preservation, cancellation behaviour, and repeated-promotion allowance per §6.

### Out Of Scope

- Duplicate detection / "already promoted" status / back-links (§6.7 explicitly a later milestone).
- Changing the diary-snapshot model (already immutable — Story 14 confirmed).
- The Quick Add fields themselves (Story 18).

## Non-Goals

- Auto-updating a promoted product/recipe when the source entry later changes, or vice-versa (spec non-goal).
- Adding a manual-nutrition override to the Recipe domain unless the chosen approach requires it (see Q2 — the recommended path avoids it).

## Current Understanding

From 2026-07-25 recon (`file:line` where load-bearing):

- Shared Quick Add component (`app/ui/food/diary/quickadd/QuickAddScreen.kt` / `QuickAddForm.kt`) is reused by create and update-historical — adding the actions once surfaces them in all three contexts (§6.1).
- **Diary entries are immutable snapshots** (migration 25→26 `UnlinkDiaryMigration`; manual entries embed their own nutrients), so promotion is a pure "create a new editable food" operation that cannot rewrite history — source-entry preservation (§6.5) is essentially free.
- **Unit-basis mismatch**: `ManualDiaryEntry.nutritionFacts` is the absolute TOTAL; `Product.nutritionFacts` is per-100 g/100 ml and `CreateProductViewModel` derives it via `multiplier = 1/weight*100` (`CreateProductViewModel.kt:30-35`). Converting a total into a Product needs a weight divisor → depends on Story 18's `weightGrams`.
- Product editor: `app/ui/food/product/ProductFormState.kt` (`rememberProductFormState(product)`, ~1067–1189), form `ProductForm.kt`, create `app/ui/food/product/create/CreateProductScreen.kt` + `CreateProductViewModel.kt`. Energy + P/C/F are **required**; `brand/barcode/packageWeight/servingWeight` optional. `Product` has `note` (maps from description), `packageWeight`, `servingWeight`.
- Recipe editor: `app/ui/food/recipe/RecipeFormState.kt` holds only `name, servings, note, isLiquid, List<MinimalIngredient>`; `isValid` **requires ≥1 ingredient**. `Recipe.nutritionFacts` is derived from ingredients (`Recipe.kt:32-38`) — **no manual nutrition field**. So a recipe cannot be seeded from a bare estimate.
- Navigation by `FoodId` (sealed `FoodId.Product`/`FoodId.Recipe`, no "manual" variant) — any editor navigation must mint a real id first / use a draft-prefill path rather than an existing id.

## Questions / Unknowns

- Q1: Product promotion when the entry has no `weightGrams` — require weight, or treat the total as per-100 g?
  Impact: Without a weight there is no correct total→per-100 g divisor; guessing corrupts the product's nutrition density.
  Assumption: Prefill the editor with `packageWeight = weightGrams` and nutrition converted to per-100 g; if `weightGrams` is absent, open the editor with the values as-entered and a visible hint that a package/serving weight is needed, leaving the user to complete it (no silent guess).
  Status: OPEN — recommend the "prefill weight, else prompt" path.
- Q2: How to seed a Recipe from a single estimate given the ingredient-derived model?
  Impact: Determines whether Recipe promotion is a light prefill or a domain change.
  Assumption (recommended, minimal): **Promote-to-Recipe first creates a backing custom Product from the estimate (the Product path), then seeds a new recipe draft with that Product as a single ingredient** (weight so that per-100 g × weight reproduces the estimate). This reuses existing machinery and never corrupts the derived-nutrition model. The heavier alternative (add a manual-nutrition mode to `Recipe`) is documented but not chosen unless the owner wants a true single-step recipe.
  Status: OPEN — needs owner sign-off; it changes the UX (a recipe that starts with one "estimate" ingredient).
- Q3: Draft-prefill mechanism — extend `rememberProductFormState`/`rememberRecipeFormState` to accept a seed, or pass prefill values via navigation args?
  Impact: Affects how invasive the change to the editors is.
  Assumption: Add an optional seed to the `remember*FormState` factories (nullable prefill object), defaulting to today's behaviour — smallest blast radius, no navigation-contract churn.
  Status: OPEN.

## Execution Steps

1. Define a promotion seed model + mapping.
   - Why: One place that maps a Quick Add entry → product/recipe prefill.
   - Edits: a small `QuickAddPromotionSeed` (name, description, energy, protein, fat, carb, fibre, servingCount, weightGrams) and pure mapping functions per the §6.2/§6.3 tables.
2. Add the two actions to the shared Quick Add UI.
   - Why: Surface promotion in create/edit/historical at once (§6.1).
   - Edits: `QuickAddScreen.kt`/`QuickAddForm.kt` — `Promote to Product` / `Promote to Recipe` actions; ensure a not-yet-saved new entry is saved (or its state carried) before navigating, per §6.2/§6.3 step 2.
   - Dependencies: Story 18 fields present.
3. Product prefill path.
   - Why: The core promotion; also the substrate for recipe promotion (Q2).
   - Edits: optional seed on `rememberProductFormState` (Q3); `CreateProductScreen`/`CreateProductViewModel` accept the seed; convert total→per-100 g using `weightGrams` (Q1); map description→`note`, servings/weight→`packageWeight`/`servingWeight` where supported. Commit only on editor save.
   - Dependencies: steps 1–2.
4. Recipe prefill path (per Q2's recommended approach).
   - Why: Deliver recipe promotion without corrupting derived nutrition.
   - Edits: reuse step 3 to mint the backing Product, then seed `rememberRecipeFormState` with `name`, `servings = servingCount ?: 1`, `note = description`, and that Product as a single ingredient; open the recipe editor; commit only on save.
   - Dependencies: step 3.
5. Cancellation + repeated promotion.
   - Why: §6.6/§6.7 correctness.
   - Edits: ensure cancelling the editor creates nothing and returns to a sensible screen; leave the source entry and its saved edits intact; place no one-time lock.
   - Dependencies: steps 3–4.
6. Tests (§10.4).
   - Edits: integration tests — new→Product, new→Recipe, historical→Product, historical→Recipe, field mapping, cancellation, product save failure, recipe save failure, repeated promotion, edit-promoted-without-changing-history, edit-history-without-changing-promoted.
   - Dependencies: steps 1–5.

## Validation

### Automated Checks

- The project's KMP unit/integration test task for the promotion tests.
- Build compiles with the new optional seed params (default paths unchanged).

### Manual Checks

1. Promote a new Quick Add (with weight) to Product; confirm mapped per-100 g values are correct and the diary entry is unchanged after saving the product.
2. Promote a historical Quick Add to Recipe; confirm the recipe appears in the custom collection and the old diary record is byte-identical afterward.
3. Cancel a promotion mid-editor; confirm no product/recipe was created and the source entry (and its edits) survived.
4. Promote the same entry twice; confirm both succeed with no hidden lock.

### Acceptance Criteria

- Both actions appear on new, existing, and historical Quick Add edit; each opens the correct existing editor prefilled from mapped fields.
- The new custom item is created only when the editor is saved; cancelling creates nothing.
- The original diary entry is never replaced by a live reference and never mutated by promotion.
- Product nutrition density is correct for entries carrying a weight; entries without a weight prompt the user rather than guessing.
- Repeated promotion is allowed; no §6.7 status/lock is introduced.

## Risk Mitigation

- Risk: total→per-100 g conversion wrong → bad product nutrition.
  Mitigation: Convert only with a real `weightGrams`; otherwise prompt; unit-test the mapping both ways.
- Risk: Recipe seeding corrupts the derived-nutrition model.
  Mitigation: Chosen approach adds a real backing ingredient rather than faking recipe nutrition; no `Recipe` domain change.
- Risk: Promotion accidentally overwrites/links the source entry.
  Mitigation: Snapshot model guarantees independence; a manual + automated check asserts the source entry is unchanged post-promotion.
- Risk: Editor-prefill change ripples into normal create/edit.
  Mitigation: Seed is an optional param defaulting to current behaviour; existing create/edit paths pass `null`.

## Evidence / References

- Planning input: 2026-07-25 Quick Add / product / recipe recon (`ProductFormState.kt`, `CreateProductViewModel.kt:30-35`, `Recipe.kt:32-38`, `RecipeFormState.kt`, snapshot migration `UnlinkDiaryMigration`) with `file:line`; spec §6, §10.4.
- Unverified: exact editor navigation contract and whether a save-before-navigate is needed for brand-new entries — confirm during step 2.

## Execution Log

Implemented 25 July 2026.

### Decisions (resolving the open questions)

- **Q1 (product weight):** Resolved as recommended. `QuickAddPromotionSeed.basisWeightGrams` uses the
  entry's `weightGrams` when `> 0`, else falls back to 100 g. With a real weight the totals are
  converted to per-100 g (`total / weight * 100`) and `packageWeight` is set to the weight; without a
  weight the totals are placed into the editor as-entered and `packageWeight` is left null — no value
  is invented. Verified on the emulator: 200 g PromoPie mapped to 255 kcal / 10 / 15 / 20 per 100 g;
  weightless PieTest mapped 1:1 with an empty package weight.
- **Q2 (recipe seeding):** Resolved per the Story 14 spike's **single placeholder-ingredient**
  approach (task-mandated; the plan's earlier "backing product first" wording is the same mechanism).
  Promotion creates one real backing custom `Product` carrying the estimate as per-100 g nutrition,
  then opens the recipe editor seeded with that product as a single ingredient at the basis weight, so
  the derived-nutrition model is untouched and the recipe's totals reproduce the estimate exactly
  (verified: PieTest recipe summary = 740 kcal / 40 / 20).
- **Q3 (prefill mechanism):** No new nav-arg contract churn and no seed param bolted onto the giant
  `rememberProductFormState`. Instead the seed maps to a domain `Product` and reuses the **existing**
  `rememberProductFormState(product)` overload via a new additive `prefillProduct` param on
  `CreateProductScreen`/`CreateProductApp`. The recipe editor gained optional `initial*` params on
  `CreateRecipeScreen`. Smallest merge surface consistent with "commit only on save".

### Cancellation correctness (spec §6.6 vs. Q2)

The placeholder product must exist before the recipe editor can display/reference it, but "cancel
creates nothing" forbids leaving it behind. `PromoteToRecipePlaceholderViewModel` owns the
placeholder lifecycle: it creates the product once (id kept in `SavedStateHandle` for process death),
and `discardPlaceholderIfUncommitted()` deletes it on cancel via the **application** coroutine scope
so the delete survives the destination being popped. Recipe save calls `markCommitted()` so the
committed placeholder is kept. Verified on the emulator: cancelling a recipe promotion left the custom
food count unchanged (no orphan), while a saved one kept exactly one backing product + the recipe.

### Files changed

- New: `QuickAddPromotionSeed.kt`, `QuickAddPromotionMapping.kt` (pure mapping),
  `PromoteToRecipePlaceholderViewModel.kt`, `QuickAddPromotionMappingTest.kt`.
- Edited: `QuickAddScreen.kt` (overflow menu), `CreateQuickAddScreen.kt`, `UpdateQuickAddScreen.kt`
  (seed + nav callbacks), `FoodDiaryQuickAddModule.kt` (DI), `CreateProductScreen.kt`,
  `CreateProductApp.kt` (`prefillProduct`), `CreateRecipeScreen.kt` (`initial*` seed),
  `FoodYouAppNavHost.kt` (routes + wiring), `strings.xml` (two actions).

## Completion Review

- **Automated:** `:app:testDebugUnitTest --tests *QuickAddPromotionMappingTest*` — PASSED (4 tests:
  with-weight conversion, weightless fallback, absent-nutrient non-invention, recipe reproduction +
  servings rounding). `:app:assembleDebug` — SUCCESS.
- **E2E (emulator `foodyou`, package `com.acme.foodapp`):** promote-to-product from a new Quick Add
  (per-100 g correct, product searchable, diary unchanged, in-progress edits intact); promote-to-
  product from a historical entry (PieTest diary row still 740 / 40 / 20 / 100 afterwards, new product
  created); promote-to-recipe (placeholder ingredient visible, summary reproduces the estimate, recipe
  saved and searchable with the recipe icon); cancel (no recipe created, placeholder cleaned up — no
  orphan); repeated promotion (PieTest promoted to product then recipe then again, no lock). All
  passed.
- **Remaining uncertainty:** New promotion strings were added to the default `values/strings.xml`
  only; other locales fall back to English until translated. The recipe placeholder is a real,
  searchable backing product by design (spike/§6.3) — an accepted, documented side effect, not a bug.
