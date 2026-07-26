package com.maksimowiczm.foodyou.app.ui.food.diary.quickadd

import com.maksimowiczm.foodyou.app.ui.common.utility.EnergyFormatter
import com.maksimowiczm.foodyou.common.domain.food.FoodSource
import com.maksimowiczm.foodyou.common.domain.food.NutrientValue.Companion.toNutrientValue
import com.maksimowiczm.foodyou.common.domain.food.NutritionFacts
import com.maksimowiczm.foodyou.food.domain.entity.FoodId
import com.maksimowiczm.foodyou.food.domain.entity.Product
import kotlin.math.roundToInt

/**
 * Pure mapping from a Quick Add entry to the product/recipe drafts used by promotion (Story 19,
 * spec §6.2/§6.3). Kept free of Compose and persistence so it can be unit tested directly.
 */

/** Captures the current form values as a promotion seed (§6.5: a copy, never the live entry). */
internal fun QuickAddFormState.toPromotionSeed(energyFormatter: EnergyFormatter) =
    QuickAddPromotionSeed(
        name = name.value,
        description = description.value,
        energyKcal = energy.value?.let(energyFormatter::toKcal),
        proteins = proteins.value,
        carbohydrates = carbohydrates.value,
        fats = fats.value,
        fibre = fibre.value,
        servingCount = servingCount.value,
        weightGrams = weightGrams.value,
    )

/**
 * Weight basis (grams) used to convert the entry's absolute totals into the per-100 g values a
 * [Product] stores. Quick Add nutrition is an absolute total for the entry (spec §5.3) while a
 * Product is per 100 g, so the conversion needs a divisor. When the entry carries no usable weight
 * we fall back to 100 g, which places the totals into the editor unchanged for the user to correct
 * rather than guessing a density (spec §6.2: never invent a value).
 */
internal val QuickAddPromotionSeed.basisWeightGrams: Double
    get() = weightGrams?.takeIf { it > 0.0 } ?: 100.0

private fun Double?.per100(basis: Double): Double? = this?.let { it / basis * 100.0 }

/**
 * Builds the per-100 g [Product] draft that prefills the product editor and backs a promoted recipe.
 * Only fields the entry actually carries are populated; absent nutrients stay null so the editor
 * prompts for them instead of inventing zeros. Package weight is set only when the entry has a real
 * weight. The [FoodId.Product] id is a placeholder — the editor mints a real id when it is saved.
 */
internal fun QuickAddPromotionSeed.toProductPrefill(): Product {
    val basis = basisWeightGrams
    return Product(
        id = FoodId.Product(0L),
        name = name,
        brand = null,
        barcode = null,
        note = description,
        isLiquid = false,
        packageWeight = weightGrams?.takeIf { it > 0.0 },
        servingWeight = null,
        source = FoodSource(FoodSource.Type.User),
        nutritionFacts =
            NutritionFacts(
                energy = energyKcal.per100(basis).toNutrientValue(),
                proteins = proteins.per100(basis).toNutrientValue(),
                carbohydrates = carbohydrates.per100(basis).toNutrientValue(),
                fats = fats.per100(basis).toNutrientValue(),
                dietaryFiber = fibre.per100(basis).toNutrientValue(),
            ),
    )
}

/** Recipe yield: the entry's serving count rounded to a whole number, at least one (spec §6.3). */
internal val QuickAddPromotionSeed.recipeServings: Int
    get() = servingCount?.takeIf { it > 0.0 }?.roundToInt()?.coerceAtLeast(1) ?: 1

/**
 * Grams of the single placeholder ingredient seeded into a promoted recipe. Equal to the per-100 g
 * basis, so the placeholder's per-100 g nutrition times this weight reproduces the original Quick Add
 * estimate exactly, keeping Food You's ingredient-derived recipe model uncorrupted (spec §6.3, Story
 * 14 §10.6).
 */
internal val QuickAddPromotionSeed.recipeIngredientWeightGrams: Double
    get() = basisWeightGrams
