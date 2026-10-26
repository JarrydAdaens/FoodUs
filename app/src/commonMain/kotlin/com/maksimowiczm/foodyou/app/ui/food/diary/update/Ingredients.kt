package com.maksimowiczm.foodyou.app.ui.food.diary.update

import com.maksimowiczm.foodyou.app.ui.developer.inspection.UiSourceFile
import com.maksimowiczm.foodyou.app.ui.developer.inspection.uiName

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.maksimowiczm.foodyou.app.ui.common.component.FoodErrorListItem
import com.maksimowiczm.foodyou.app.ui.common.component.FoodListItem
import com.maksimowiczm.foodyou.app.ui.common.utility.LocalEnergyFormatter
import com.maksimowiczm.foodyou.app.ui.common.utility.stringResource
import com.maksimowiczm.foodyou.app.ui.common.utility.stringResourceWithWeight
import com.maksimowiczm.foodyou.common.compose.extension.add
import com.maksimowiczm.foodyou.common.compose.extension.horizontal
import com.maksimowiczm.foodyou.common.compose.extension.vertical
import com.maksimowiczm.foodyou.common.compose.utility.formatClipZeros
import com.maksimowiczm.foodyou.fooddiary.domain.entity.RecipeSnapshot
import com.maksimowiczm.foodyou.fooddiary.domain.entity.RecipeIngredientSnapshot
import foodyou.app.generated.resources.*
import kotlin.math.roundToInt
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun Ingredients(
    ingredients: List<RecipeIngredientSnapshot>,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    val g = stringResource(Res.string.unit_gram_short)

    val verticalPadding = contentPadding.vertical()
    val horizontal = contentPadding.horizontal()

    Column((modifier.padding(verticalPadding)).uiName("app.ui.food.diary.update.Ingredients.Column.1", "androidx.compose.foundation.layout.Column", uiInspectionSource, "Ingredients")) {
        Text(
            text = stringResource(Res.string.headline_ingredients),
            modifier = (Modifier.padding(horizontal)).uiName("app.ui.food.diary.update.Ingredients.Text.1", "androidx.compose.material3.Text", uiInspectionSource, "Ingredients"),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
        )
        ingredients.forEach { ingredient ->
            val facts = ingredient.nutritionFacts ?: com.maksimowiczm.foodyou.common.domain.food.NutritionFacts()
            val proteins = facts.proteins.value
            val carbs = facts.carbohydrates.value
            val fats = facts.fats.value
            val energy = facts.energy.value
            val measurementString =
                ingredient.measurement.stringResourceWithWeight(
                    totalWeight = ingredient.food.totalWeight,
                    servingWeight = ingredient.food.servingWeight,
                    isLiquid = ingredient.food.isLiquid,
                ) ?: ingredient.measurement.stringResource()

            if (
                proteins == null ||
                    carbs == null ||
                    fats == null ||
                    energy == null
            ) {
                FoodErrorListItem(
                    headline = ingredient.food.name,
                    errorMessage = stringResource(Res.string.error_food_is_missing_required_fields),
                    contentPadding = horizontal.add(vertical = 8.dp), modifier = Modifier.uiName("app.ui.food.diary.update.Ingredients.FoodErrorListItem.1", "com.maksimowiczm.foodyou.app.ui.common.component.FoodErrorListItem", uiInspectionSource, "Ingredients")
                )
            } else {
                FoodListItem(
                    name = { Text(ingredient.food.name, modifier = Modifier.uiName("app.ui.food.diary.update.Ingredients.Text.2", "androidx.compose.material3.Text", uiInspectionSource, "Ingredients")) },
                    proteins = {
                        val text = proteins.formatClipZeros() + " $g"
                        Text(text, modifier = Modifier.uiName("app.ui.food.diary.update.Ingredients.Text.3", "androidx.compose.material3.Text", uiInspectionSource, "Ingredients"))
                    },
                    carbohydrates = {
                        val text = carbs.formatClipZeros() + " $g"
                        Text(text, modifier = Modifier.uiName("app.ui.food.diary.update.Ingredients.Text.4", "androidx.compose.material3.Text", uiInspectionSource, "Ingredients"))
                    },
                    fats = {
                        val text = fats.formatClipZeros() + " $g"
                        Text(text, modifier = Modifier.uiName("app.ui.food.diary.update.Ingredients.Text.5", "androidx.compose.material3.Text", uiInspectionSource, "Ingredients"))
                    },
                    calories = {
                        Text(LocalEnergyFormatter.current.formatEnergy(energy.roundToInt()), modifier = Modifier.uiName("app.ui.food.diary.update.Ingredients.Text.6", "androidx.compose.material3.Text", uiInspectionSource, "Ingredients"))
                    },
                    measurement = { Text(measurementString, modifier = Modifier.uiName("app.ui.food.diary.update.Ingredients.Text.7", "androidx.compose.material3.Text", uiInspectionSource, "Ingredients")) },
                    contentPadding = horizontal.add(vertical = 8.dp),
                    isRecipe = ingredient.food is RecipeSnapshot, modifier = Modifier.uiName("app.ui.food.diary.update.Ingredients.FoodListItem.1", "com.maksimowiczm.foodyou.app.ui.common.component.FoodListItem", uiInspectionSource, "Ingredients")
                )
            }
        }
    }
}


private val uiInspectionSource = UiSourceFile(
    "com.maksimowiczm.foodyou.app.ui.food.diary.update",
    "app/src/commonMain/kotlin/com/maksimowiczm/foodyou/app/ui/food/diary/update/Ingredients.kt",
)
