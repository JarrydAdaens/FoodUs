package com.maksimowiczm.foodyou.app.ui.food.diary.add

import com.maksimowiczm.foodyou.app.ui.developer.inspection.UiSourceFile
import com.maksimowiczm.foodyou.app.ui.developer.inspection.uiName

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ViewList
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.maksimowiczm.foodyou.app.ui.common.component.IncompleteFoodsList
import com.maksimowiczm.foodyou.app.ui.common.utility.stringResource
import com.maksimowiczm.foodyou.app.ui.common.utility.stringResourceWithWeight
import com.maksimowiczm.foodyou.app.ui.food.component.EnergyProgressIndicator
import com.maksimowiczm.foodyou.app.ui.food.shared.component.NutrientList
import com.maksimowiczm.foodyou.common.domain.food.isComplete
import com.maksimowiczm.foodyou.common.domain.measurement.Measurement
import com.maksimowiczm.foodyou.common.domain.measurement.MeasurementResolver
import com.maksimowiczm.foodyou.food.domain.entity.NutritionalItemId
import foodyou.app.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun NutrientList(
    food: FoodModel,
    measurement: Measurement,
    onEditFood: (NutritionalItemId) -> Unit,
    modifier: Modifier = Modifier,
) {
    val facts = food.nutritionFor(measurement)
    if (facts == null) {
        Text(stringResource(Res.string.error_portion_save), color = MaterialTheme.colorScheme.error, modifier = (modifier).uiName("app.ui.food.diary.add.NutrientList.Text.1", "androidx.compose.material3.Text", uiInspectionSource, "NutrientList"))
        return
    }

    Column((modifier).uiName("app.ui.food.diary.add.NutrientList.Column.1", "androidx.compose.foundation.layout.Column", uiInspectionSource, "NutrientList")) {
        Row(
            modifier = (Modifier.fillMaxWidth().padding(8.dp)).uiName("app.ui.food.diary.add.NutrientList.Row.1", "androidx.compose.foundation.layout.Row", uiInspectionSource, "NutrientList"),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(modifier = (Modifier.size(48.dp)).uiName("app.ui.food.diary.add.NutrientList.Box.1", "androidx.compose.foundation.layout.Box", uiInspectionSource, "NutrientList"), contentAlignment = Alignment.Center) {
                Icon(imageVector = Icons.AutoMirrored.Outlined.ViewList, contentDescription = null, modifier = Modifier.uiName("app.ui.food.diary.add.NutrientList.Icon.1", "androidx.compose.material3.Icon", uiInspectionSource, "NutrientList"))
            }

            val proteins = facts.proteins.value
            val carbohydrates = facts.carbohydrates.value
            val fats = facts.fats.value

            if (proteins != null && carbohydrates != null && fats != null) {
                EnergyProgressIndicator(
                    proteins = proteins.toFloat(),
                    carbohydrates = carbohydrates.toFloat(),
                    fats = fats.toFloat(),
                    modifier = (Modifier.weight(1f)).uiName("app.ui.food.diary.add.NutrientList.EnergyProgressIndicator.1", "com.maksimowiczm.foodyou.app.ui.food.component.EnergyProgressIndicator", uiInspectionSource, "NutrientList"),
                )
            }
        }

        val measurementString =
            measurement.stringResourceWithWeight(
                totalWeight = food.totalWeight,
                servingWeight = food.servingWeight,
                isLiquid = food.isLiquid,
            ) ?: measurement.stringResource()

        Text(
            text = measurementString,
            modifier = (Modifier.padding(horizontal = 8.dp).padding(bottom = 8.dp)).uiName("app.ui.food.diary.add.NutrientList.Text.2", "androidx.compose.material3.Text", uiInspectionSource, "NutrientList"),
            style = MaterialTheme.typography.labelLarge,
        )

        NutrientList(facts, modifier = Modifier.uiName("app.ui.food.diary.add.NutrientList.NutrientList.1", "com.maksimowiczm.foodyou.app.ui.food.diary.add.NutrientList", uiInspectionSource, "NutrientList"))

        if (food is RecipeModel && !food.nutritionFacts.isComplete) {
            val foods =
                food.allIngredients
                    .filter { (foodId, _, facts) -> foodId is NutritionalItemId.Product && !facts.isComplete }
                    .map { (foodId, name) -> foodId to name }

            IncompleteFoodsList(
                foods = foods.map { (_, name) -> name }.distinct(),
                onFoodClick = { i -> onEditFood(foods[i].first) },
                modifier = (Modifier.padding(8.dp)).uiName("app.ui.food.diary.add.NutrientList.IncompleteFoodsList.1", "com.maksimowiczm.foodyou.app.ui.common.component.IncompleteFoodsList", uiInspectionSource, "NutrientList"),
            )
        } else {
            Spacer(Modifier.height(8.dp))
        }
    }
}


private val uiInspectionSource = UiSourceFile(
    "com.maksimowiczm.foodyou.app.ui.food.diary.add",
    "app/src/commonMain/kotlin/com/maksimowiczm/foodyou/app/ui/food/diary/add/NutrientList.kt",
)
