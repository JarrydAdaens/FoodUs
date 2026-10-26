package com.maksimowiczm.foodyou.app.ui.food.diary.quickadd

import com.maksimowiczm.foodyou.app.ui.common.utility.EnergyFormatter
import com.maksimowiczm.foodyou.common.domain.food.NutrientValue.Companion.toNutrientValue
import com.maksimowiczm.foodyou.common.domain.food.NutritionFacts

/** Replaces only edited fields; other supported nutrients remain part of the accepted snapshot.
 * Manual create keeps its established blank-to-zero behavior. Scans and historical edits retain
 * unknown values so opening and saving an entry cannot silently fabricate nutrient data.
 */
internal fun QuickAddFormState.toNutritionFacts(
    formatter: EnergyFormatter,
    original: NutritionFacts = NutritionFacts(),
    preserveUnknown: Boolean = true,
): NutritionFacts = mapQuickAddNutrition(
    original = original,
    energyKcal = energy.value?.let(formatter::toKcal),
    proteins = proteins.value,
    carbohydrates = carbohydrates.value,
    fats = fats.value,
    fibre = fibre.value,
    sugars = sugar.value,
    preserveUnknown = preserveUnknown,
)

internal fun mapQuickAddNutrition(
    original: NutritionFacts = NutritionFacts(),
    energyKcal: Double?,
    proteins: Double?,
    carbohydrates: Double?,
    fats: Double?,
    fibre: Double?,
    sugars: Double?,
    preserveUnknown: Boolean,
): NutritionFacts {
    fun Double?.macro() = (this ?: if (preserveUnknown) null else 0.0).toNutrientValue()
    return original.copy(
        energy = energyKcal.macro(),
        proteins = proteins.macro(),
        carbohydrates = carbohydrates.macro(),
        fats = fats.macro(),
        dietaryFiber = fibre.toNutrientValue(),
        sugars = sugars.toNutrientValue(),
    )
}
