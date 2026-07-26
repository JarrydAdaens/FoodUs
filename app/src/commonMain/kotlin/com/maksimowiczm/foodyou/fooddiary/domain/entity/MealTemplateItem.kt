package com.maksimowiczm.foodyou.fooddiary.domain.entity

import com.maksimowiczm.foodyou.common.domain.food.NutritionFacts

/**
 * One snapshotted item inside a [MealTemplate].
 *
 * A template item captures only what is needed to recreate a diary entry when the template is
 * applied to another day: the item's [name] and its already portion-adjusted [nutritionFacts] (the
 * total for the portion that was logged, not per-100 g). Storing the snapshot rather than a
 * reference to a live product or recipe keeps templates stable — applying a template still works
 * exactly the same after the original product, recipe or Quick Add entry is edited or deleted.
 */
data class MealTemplateItem(val name: String, val nutritionFacts: NutritionFacts)

/**
 * Snapshots a diary entry into a reusable template item. The entry's [nutritionFacts][
 * DiaryEntry.nutritionFacts] is already the total for its logged portion, so re-applying the item
 * reproduces the same totals the user originally logged, regardless of the entry's original kind
 * (product, recipe or manual / Quick Add).
 */
fun DiaryEntry.toMealTemplateItem(): MealTemplateItem =
    MealTemplateItem(name = name, nutritionFacts = nutritionFacts)
