package com.maksimowiczm.foodyou.app.ui.food.search

import com.maksimowiczm.foodyou.app.ui.developer.inspection.UiSourceFile
import com.maksimowiczm.foodyou.app.ui.developer.inspection.uiName

import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.maksimowiczm.foodyou.app.ui.common.component.FoodErrorListItem
import com.maksimowiczm.foodyou.app.ui.common.component.FoodListItem
import com.maksimowiczm.foodyou.app.ui.common.component.FoodListItemSkeleton
import com.maksimowiczm.foodyou.app.ui.common.utility.LocalEnergyFormatter
import com.maksimowiczm.foodyou.app.ui.common.utility.stringResource
import com.maksimowiczm.foodyou.app.ui.common.utility.stringResourceWithWeight
import com.maksimowiczm.foodyou.common.compose.utility.formatClipZeros
import com.maksimowiczm.foodyou.common.domain.measurement.Measurement
import com.maksimowiczm.foodyou.food.domain.entity.Recipe
import com.maksimowiczm.foodyou.food.domain.usecase.ObserveNutritionalItemUseCase
import com.maksimowiczm.foodyou.food.search.domain.NutritionalItemSearch
import com.valentinilk.shimmer.Shimmer
import foodyou.app.generated.resources.*
import kotlin.math.roundToInt
import kotlinx.coroutines.flow.mapNotNull
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

@Composable
internal fun FoodSearchListItem(
    food: NutritionalItemSearch.Product,
    measurement: Measurement,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val measurementFacts = food.nutritionFor(measurement)
    if (measurementFacts == null) {
        return FoodErrorListItem(
            headline = food.headline,
            errorMessage = stringResource(Res.string.error_measurement_error),
            modifier = (modifier).uiName("FoodSearchListItem.FoodErrorListItem.1", "com.maksimowiczm.foodyou.app.ui.common.component.FoodErrorListItem", uiInspectionSource, "FoodSearchListItem"),
            onClick = onClick,
        )
    }

    val proteins = measurementFacts.proteins.value
    val carbohydrates = measurementFacts.carbohydrates.value
    val fats = measurementFacts.fats.value
    val energy = measurementFacts.energy.value
    val measurementString =
        measurement.stringResourceWithWeight(
            totalWeight = food.totalWeight,
            servingWeight = food.servingWeight,
            isLiquid = food.isLiquid,
        ) ?: measurement.stringResource()
    val reference=food.referencePortion?.text ?: listOfNotNull(food.referencePortion?.amount?.toString(),food.referencePortion?.unitText).joinToString(" ")
    val displayMeasurement=if(food.factBasis==com.maksimowiczm.foodyou.common.domain.food.ProductFactBasis.REFERENCE_SERVING && reference.isNotBlank())
        measurementString+" · "+stringResource(Res.string.captured_reference_short,reference) else measurementString

    FoodSearchListItem(
        headline = food.headline,
        proteins = proteins,
        carbohydrates = carbohydrates,
        fats = fats,
        energy = energy,
        measurement = { Text(displayMeasurement, modifier = Modifier.uiName("FoodSearchListItem.Text.1", "androidx.compose.material3.Text", uiInspectionSource, "FoodSearchListItem")) },
        isRecipe = false,
        onClick = onClick,
        modifier = (modifier).uiName("FoodSearchListItem.FoodSearchListItem.1", "com.maksimowiczm.foodyou.app.ui.food.search.FoodSearchListItem", uiInspectionSource, "FoodSearchListItem"),
    )
}

/** Recipe has to be lazy loaded, so we use [ObserveFoodUseCase] to observe the recipe. */
@Composable
internal fun FoodSearchListItem(
    food: NutritionalItemSearch.Recipe,
    measurement: Measurement,
    onClick: () -> Unit,
    shimmer: Shimmer,
    modifier: Modifier = Modifier,
) {
    val observeRecipeUseCase: ObserveNutritionalItemUseCase = koinInject()

    val recipe =
        observeRecipeUseCase
            .observe(food.id)
            .mapNotNull { it as? Recipe }
            .collectAsStateWithLifecycle(null)
            .value

    if (recipe == null) {
        return FoodListItemSkeleton(shimmer, modifier = Modifier.uiName("FoodSearchListItem.FoodListItemSkeleton.1", "com.maksimowiczm.foodyou.app.ui.common.component.FoodListItemSkeleton", uiInspectionSource, "FoodSearchListItem"))
    }

    val measurementFacts = recipe.nutritionFor(measurement)
    if(measurementFacts==null)return FoodErrorListItem(headline=food.headline,modifier=(modifier).uiName("FoodSearchListItem.FoodErrorListItem.2", "com.maksimowiczm.foodyou.app.ui.common.component.FoodErrorListItem", uiInspectionSource, "FoodSearchListItem"),onClick=onClick,
        errorMessage=stringResource(if(recipe.reportedNutrition?.needsReview==true)Res.string.captured_needs_review else Res.string.error_portion_save))
    val proteins = measurementFacts.proteins.value
    val carbohydrates = measurementFacts.carbohydrates.value
    val fats = measurementFacts.fats.value
    val energy = measurementFacts.energy.value

    val measurementString =
        measurement.stringResourceWithWeight(
            totalWeight = recipe.totalWeight,
            servingWeight = recipe.servingWeight,
            isLiquid = recipe.isLiquid,
        ) ?: measurement.stringResource()

    FoodSearchListItem(
        headline = food.headline,
        proteins = proteins,
        carbohydrates = carbohydrates,
        fats = fats,
        energy = energy,
        measurement = { Text(if(recipe.isReported)measurementString+" · "+stringResource(Res.string.captured_reported)else measurementString, modifier = Modifier.uiName("FoodSearchListItem.Text.2", "androidx.compose.material3.Text", uiInspectionSource, "FoodSearchListItem")) },
        isRecipe = true,
        onClick = onClick,
        modifier = (modifier).uiName("FoodSearchListItem.FoodSearchListItem.2", "com.maksimowiczm.foodyou.app.ui.food.search.FoodSearchListItem", uiInspectionSource, "FoodSearchListItem"),
    )
}

@Composable
private fun FoodSearchListItem(
    headline: String,
    proteins: Double?,
    carbohydrates: Double?,
    fats: Double?,
    energy: Double?,
    measurement: @Composable () -> Unit,
    isRecipe: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val g = stringResource(Res.string.unit_gram_short)

    FoodListItem(
        name = { Text(text = headline, modifier = Modifier.uiName("FoodSearchListItem.Text.3", "androidx.compose.material3.Text", uiInspectionSource, "FoodSearchListItem")) },
        proteins = {
            Text(proteins?.let { it.formatClipZeros()+" "+g } ?: stringResource(Res.string.captured_unknown_value), modifier = Modifier.uiName("FoodSearchListItem.Text.4", "androidx.compose.material3.Text", uiInspectionSource, "FoodSearchListItem"))
        },
        carbohydrates = {
            Text(carbohydrates?.let { it.formatClipZeros()+" "+g } ?: stringResource(Res.string.captured_unknown_value), modifier = Modifier.uiName("FoodSearchListItem.Text.5", "androidx.compose.material3.Text", uiInspectionSource, "FoodSearchListItem"))
        },
        fats = {
            Text(fats?.let { it.formatClipZeros()+" "+g } ?: stringResource(Res.string.captured_unknown_value), modifier = Modifier.uiName("FoodSearchListItem.Text.6", "androidx.compose.material3.Text", uiInspectionSource, "FoodSearchListItem"))
        },
        calories = { Text(when {
            energy==null -> stringResource(Res.string.captured_unknown_value)
            energy>Int.MAX_VALUE -> energy.toString()+" kcal"
            else -> LocalEnergyFormatter.current.formatEnergy(energy.roundToInt())
        }, modifier = Modifier.uiName("FoodSearchListItem.Text.7", "androidx.compose.material3.Text", uiInspectionSource, "FoodSearchListItem")) },
        measurement = measurement,
        isRecipe = isRecipe,
        modifier = (modifier).uiName("FoodSearchListItem.FoodListItem.1", "com.maksimowiczm.foodyou.app.ui.common.component.FoodListItem", uiInspectionSource, "FoodSearchListItem"),
        onClick = onClick,
    )
}


private val uiInspectionSource = UiSourceFile(
    "com.maksimowiczm.foodyou.app.ui.food.search",
    "app/src/commonMain/kotlin/com/maksimowiczm/foodyou/app/ui/food/search/FoodSearchListItem.kt",
)
