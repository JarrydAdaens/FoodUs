package com.maksimowiczm.foodyou.app.ui.common.component

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.maksimowiczm.foodyou.app.ui.common.utility.LocalNutrientsOrder
import com.maksimowiczm.foodyou.settings.domain.entity.NutrientsOrder
import com.maksimowiczm.foodyou.app.ui.developer.inspection.*

/** Shared cell geometry for diary entries and their totals. Cells never depend on number length. */
@Composable
internal fun NutritionColumns(calories: @Composable () -> Unit, proteins: @Composable () -> Unit,
    carbohydrates: @Composable () -> Unit, fats: @Composable () -> Unit, modifier: Modifier = Modifier,
) {
    Row((modifier.fillMaxWidth()).uiName("NutritionColumns.Row.phone1", "androidx.compose.foundation.layout.Row", uiInspectionSource, "NutritionColumns")) {
        Box((Modifier.weight(1f)).uiName("NutritionColumns.Box.phone1", "androidx.compose.foundation.layout.Box", uiInspectionSource, "NutritionColumns"), contentAlignment = Alignment.CenterEnd) { calories() }
        LocalNutrientsOrder.current.forEach { field ->
            if (field == NutrientsOrder.Proteins || field == NutrientsOrder.Carbohydrates || field == NutrientsOrder.Fats) {
                Box((Modifier.weight(1f)).uiName("NutritionColumns.Box.phone2", "androidx.compose.foundation.layout.Box", uiInspectionSource, "NutritionColumns"), contentAlignment = Alignment.CenterEnd) {
                    when (field) {
                        NutrientsOrder.Proteins -> proteins()
                        NutrientsOrder.Carbohydrates -> carbohydrates()
                        NutrientsOrder.Fats -> fats()
                    }
                }
            }
        }
    }
}
private val uiInspectionSource = UiSourceFile("com.maksimowiczm.foodyou.app.ui.common.component",
    "app/src/commonMain/kotlin/com/maksimowiczm/foodyou/app/ui/common/component/NutritionColumns.kt")
