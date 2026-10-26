package com.maksimowiczm.foodyou.app.ui.food.recipe


import com.maksimowiczm.foodyou.app.ui.developer.inspection.UiSourceFile
import com.maksimowiczm.foodyou.app.ui.developer.inspection.uiName

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ViewList
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeExtendedFloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.maksimowiczm.foodyou.app.ui.common.component.ArrowBackIconButton
import com.maksimowiczm.foodyou.app.ui.common.component.IncompleteFoodsList
import com.maksimowiczm.foodyou.app.ui.common.utility.stringResource
import com.maksimowiczm.foodyou.app.ui.common.utility.stringResourceWithWeight
import com.maksimowiczm.foodyou.app.ui.food.component.EnergyProgressIndicator
import com.maksimowiczm.foodyou.app.ui.food.component.MeasurementPicker
import com.maksimowiczm.foodyou.app.ui.food.component.rememberMeasurementPickerState
import com.maksimowiczm.foodyou.app.ui.food.shared.component.NutrientList
import com.maksimowiczm.foodyou.common.compose.extension.add
import com.maksimowiczm.foodyou.common.domain.food.isComplete
import com.maksimowiczm.foodyou.common.domain.measurement.Measurement
import com.maksimowiczm.foodyou.food.domain.entity.Product
import com.maksimowiczm.foodyou.food.domain.entity.Recipe
import foodyou.app.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun MeasureIngredientScreen(
    onBack: () -> Unit,
    measurement: Measurement,
    viewModel: MeasureIngredientViewModel,
    onSave: (Measurement) -> Unit,
    modifier: Modifier = Modifier,
) {
    val possibleMeasurements = viewModel.possibleMeasurements.collectAsStateWithLifecycle().value
    val suggestions = viewModel.suggestions.collectAsStateWithLifecycle().value
    val food = viewModel.food.collectAsStateWithLifecycle().value

    if (possibleMeasurements == null || suggestions == null || food == null) {
        // TODO loading state
        return
    }

    val selectedMeasurement = measurement

    val measurementPickerState =
        rememberMeasurementPickerState(
            suggestions = suggestions,
            possibleTypes = possibleMeasurements,
            selectedMeasurement = selectedMeasurement,
        )

    val portionFacts = food.nutritionFor(measurementPickerState.measurement)
    val amountValid = measurementPickerState.inputField.error == null && portionFacts != null

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = (modifier).uiName("MeasureIngredientScreen.Scaffold.1", "androidx.compose.material3.Scaffold", uiInspectionSource, "MeasureIngredientScreen"),
        topBar = {
            MediumTopAppBar(
                title = { Text(food.headline, modifier = Modifier.uiName("MeasureIngredientScreen.Text.1", "androidx.compose.material3.Text", uiInspectionSource, "MeasureIngredientScreen")) },
                navigationIcon = { ArrowBackIconButton(onBack, modifier = Modifier.uiName("MeasureIngredientScreen.ArrowBackIconButton.1", "com.maksimowiczm.foodyou.app.ui.common.component.ArrowBackIconButton", uiInspectionSource, "MeasureIngredientScreen")) },
                scrollBehavior = scrollBehavior, modifier = Modifier.uiName("MeasureIngredientScreen.MediumTopAppBar.1", "androidx.compose.material3.MediumTopAppBar", uiInspectionSource, "MeasureIngredientScreen")
            )
        },
        floatingActionButton = {
            if (amountValid) LargeExtendedFloatingActionButton(
                onClick = { onSave(measurementPickerState.measurement) },
                icon = { Icon(imageVector = Icons.Filled.Edit, contentDescription = stringResource(Res.string.action_save), modifier = Modifier.uiName("MeasureIngredientScreen.Icon.1", "androidx.compose.material3.Icon", uiInspectionSource, "MeasureIngredientScreen")) },
                text = { Text(stringResource(Res.string.action_save), modifier = Modifier.uiName("MeasureIngredientScreen.Text.2", "androidx.compose.material3.Text", uiInspectionSource, "MeasureIngredientScreen")) }, modifier = Modifier.uiName("MeasureIngredientScreen.LargeExtendedFloatingActionButton.1", "androidx.compose.material3.LargeExtendedFloatingActionButton", uiInspectionSource, "MeasureIngredientScreen")
            )
        },
    ) { paddingValues ->
        LazyColumn(
            modifier =
                (Modifier.fillMaxSize()
                    .imePadding()
                    .padding(horizontal = 8.dp)
                    .nestedScroll(scrollBehavior.nestedScrollConnection)).uiName("MeasureIngredientScreen.LazyColumn.1", "androidx.compose.foundation.lazy.LazyColumn", uiInspectionSource, "MeasureIngredientScreen"),
            contentPadding = paddingValues.add(vertical = 8.dp).add(bottom = 80.dp + 24.dp),
        ) {
            item { HorizontalDivider( modifier = Modifier.uiName("MeasureIngredientScreen.HorizontalDivider.1", "androidx.compose.material3.HorizontalDivider", uiInspectionSource, "MeasureIngredientScreen")) }

            item {
                MeasurementPicker(
                    state = measurementPickerState,
                    modifier = (Modifier.padding(vertical = 8.dp)).uiName("MeasureIngredientScreen.MeasurementPicker.1", "com.maksimowiczm.foodyou.app.ui.food.component.MeasurementPicker", uiInspectionSource, "MeasureIngredientScreen"),
                )
            }

            item { HorizontalDivider( modifier = Modifier.uiName("MeasureIngredientScreen.HorizontalDivider.2", "androidx.compose.material3.HorizontalDivider", uiInspectionSource, "MeasureIngredientScreen")) }

            item {
                val measurement = measurementPickerState.measurement
                val facts = portionFacts
                if (facts == null) {
                    Text(stringResource(Res.string.error_portion_save), color = MaterialTheme.colorScheme.error, modifier = Modifier.uiName("MeasureIngredientScreen.Text.3", "androidx.compose.material3.Text", uiInspectionSource, "MeasureIngredientScreen"))
                    return@item
                }

                Row(
                    modifier = (Modifier.fillMaxWidth().padding(8.dp)).uiName("MeasureIngredientScreen.Row.1", "androidx.compose.foundation.layout.Row", uiInspectionSource, "MeasureIngredientScreen"),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(modifier = (Modifier.size(48.dp)).uiName("MeasureIngredientScreen.Box.1", "androidx.compose.foundation.layout.Box", uiInspectionSource, "MeasureIngredientScreen"), contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ViewList,
                            contentDescription = null, modifier = Modifier.uiName("MeasureIngredientScreen.Icon.2", "androidx.compose.material3.Icon", uiInspectionSource, "MeasureIngredientScreen")
                        )
                    }

                    val proteins = facts.proteins.value
                    val carbohydrates = facts.carbohydrates.value
                    val fats = facts.fats.value

                    if (proteins != null && carbohydrates != null && fats != null) {
                        EnergyProgressIndicator(
                            proteins = proteins.toFloat(),
                            carbohydrates = carbohydrates.toFloat(),
                            fats = fats.toFloat(),
                            modifier = (Modifier.weight(1f)).uiName("MeasureIngredientScreen.EnergyProgressIndicator.1", "com.maksimowiczm.foodyou.app.ui.food.component.EnergyProgressIndicator", uiInspectionSource, "MeasureIngredientScreen"),
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
                    modifier = (Modifier.padding(horizontal = 8.dp).padding(bottom = 8.dp)).uiName("MeasureIngredientScreen.Text.4", "androidx.compose.material3.Text", uiInspectionSource, "MeasureIngredientScreen"),
                    style = MaterialTheme.typography.labelLarge,
                )

                NutrientList(facts, modifier = Modifier.uiName("MeasureIngredientScreen.NutrientList.1", "com.maksimowiczm.foodyou.app.ui.food.shared.component.NutrientList", uiInspectionSource, "MeasureIngredientScreen"))
            }

            if (food is Recipe) {
                item {
                    val incompleteIngredients =
                        food
                            .flatIngredients()
                            .filter { it is Product }
                            .filterNot { it.nutritionFacts.isComplete }

                    IncompleteFoodsList(
                        foods = incompleteIngredients.map { it.headline }.distinct(),
                        modifier = (Modifier.padding(8.dp)).uiName("MeasureIngredientScreen.IncompleteFoodsList.1", "com.maksimowiczm.foodyou.app.ui.common.component.IncompleteFoodsList", uiInspectionSource, "MeasureIngredientScreen"),
                    )
                }
            }
        }
    }
}


private val uiInspectionSource = UiSourceFile(
    "com.maksimowiczm.foodyou.app.ui.food.recipe",
    "app/src/commonMain/kotlin/com/maksimowiczm/foodyou/app/ui/food/recipe/MeasureIngredientScreen.kt",
)
