package com.maksimowiczm.foodyou.app.ui.food.library

import com.maksimowiczm.foodyou.common.domain.food.*
import com.maksimowiczm.foodyou.common.domain.measurement.Measurement
import com.maksimowiczm.foodyou.food.domain.entity.*
import com.maksimowiczm.foodyou.fooddiary.domain.entity.Meal

internal enum class LibraryBasis { Per100Grams, Per100Milliliters, ReferenceServing, RecipeServing, SavedMeal }
internal data class LibraryNutrition(
    val basis: LibraryBasis, val facts: NutritionFacts?, val reference: PortionDescription? = null,
    val reported: Boolean = false, val needsReview: Boolean = false, val note: String? = null,
)

internal fun libraryNutrition(item: NutritionalItem): LibraryNutrition = when (item) {
    is Product -> LibraryNutrition(
        if (item.factBasis == ProductFactBasis.REFERENCE_SERVING) LibraryBasis.ReferenceServing
        else if (item.isLiquid) LibraryBasis.Per100Milliliters else LibraryBasis.Per100Grams,
        item.nutritionFacts, item.referencePortion, note = item.note ?: item.nutritionMetadata?.notes,
    )
    is Recipe -> LibraryNutrition(LibraryBasis.RecipeServing, item.nutritionFor(Measurement.Serving(1.0)),
        item.reportedNutrition?.takeIf { it.basis == ReportedBasis.REFERENCE_SERVING }?.reference, item.isReported, item.reportedNutrition?.needsReview == true, note = item.note)
}

internal fun libraryNutrition(meal: Meal) = LibraryNutrition(
    LibraryBasis.SavedMeal, RecipeCalculation.totalNutrition(meal.items.map { it.nutritionFacts }),
    reported = meal.reportedNutrition != null, needsReview = meal.reportedNutrition?.needsReview == true,
)
