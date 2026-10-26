package com.maksimowiczm.foodyou.common.domain.food

/** Unknown values remain unknown. Known nutrition must be finite and nonnegative on new writes. */
fun NutritionFacts.hasValidValues(): Boolean = NutritionFactsField.entries.all { field ->
    get(field).value?.let { it.isFinite() && it >= 0.0 } ?: true
}
