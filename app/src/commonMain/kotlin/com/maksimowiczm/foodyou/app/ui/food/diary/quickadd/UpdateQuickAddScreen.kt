package com.maksimowiczm.foodyou.app.ui.food.diary.quickadd

import androidx.compose.runtime.*
import foodyou.app.generated.resources.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.maksimowiczm.foodyou.app.ui.common.utility.LocalEnergyFormatter
import com.maksimowiczm.foodyou.common.compose.extension.LaunchedCollectWithLifecycle
import com.maksimowiczm.foodyou.fooddiary.domain.entity.ManualDiaryEntryId
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun UpdateQuickAddScreen(
    onBack: () -> Unit,
    onSave: () -> Unit,
    id: Long,
    modifier: Modifier = Modifier,
    // Story 19 promotion of an existing (including historical) Quick Add entry. Prefills the editor
    // from the current form values; the source diary snapshot is never mutated (spec §6.4/§6.5).
    onPromoteToProduct: (QuickAddPromotionSeed) -> Unit = {},
    onPromoteToRecipe: (QuickAddPromotionSeed) -> Unit = {},
) {
    val viewModel: UpdateQuickAddViewModel = koinViewModel { parametersOf(ManualDiaryEntryId(id)) }
    val energyFormatter = LocalEnergyFormatter.current

    val latestOnSave by rememberUpdatedState(onSave)
    LaunchedCollectWithLifecycle(viewModel.uiEvents) {
        when (it) {
            QuickAddUiEvent.Saved -> latestOnSave()
        }
    }

    val readFailed by viewModel.readFailed.collectAsStateWithLifecycle()
    if (readFailed) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = onBack,
            text = { androidx.compose.material3.Text(org.jetbrains.compose.resources.stringResource(foodyou.app.generated.resources.Res.string.meals_unavailable)) },
            confirmButton = { androidx.compose.material3.TextButton(onClick = viewModel::retryRead) { androidx.compose.material3.Text(org.jetbrains.compose.resources.stringResource(foodyou.app.generated.resources.Res.string.meals_retry)) } },
        )
    }
    val entry = viewModel.entry.collectAsStateWithLifecycle().value

    if (entry == null) {
        // TODO loading state
        return
    }

    val formState =
        rememberQuickAddFormState(
            name = entry.name,
            energy = entry.nutritionFacts.energy.value,
            proteins = entry.nutritionFacts.proteins.value,
            carbohydrates = entry.nutritionFacts.carbohydrates.value,
            fats = entry.nutritionFacts.fats.value,
            description = entry.description,
            fibre = entry.nutritionFacts.dietaryFiber.value,
            // Quantity metadata never participates in calculations; preserve an unknown value.
            servingCount = entry.servingCount,
            weightGrams = entry.weightGrams,
            sugar = entry.nutritionFacts.sugars.value,
            preserveEnergy = entry.nutritionFacts.let {
                it.energy.value == null || it.proteins.value == null ||
                    it.carbohydrates.value == null || it.fats.value == null
            },
        )

    QuickAddScreen(
        onBack = onBack,
        onSave = {
            val name = formState.name.value

            viewModel.updateEntry(
                name = name,
                nutritionFacts = formState.toNutritionFacts(energyFormatter, entry.nutritionFacts),
                description = formState.description.value,
                servingCount = formState.servingCount.value,
                weightGrams = formState.weightGrams.value,
            )
        },
        capturedPortion = entry.capturedPortion,
        modifier = modifier,
        state = formState,
        nutritionDetails = formState.toNutritionFacts(energyFormatter, entry.nutritionFacts),
        onPromoteToProduct = {
            onPromoteToProduct(formState.toPromotionSeed(energyFormatter, entry.nutritionFacts))
        },
        onPromoteToRecipe = {
            onPromoteToRecipe(formState.toPromotionSeed(energyFormatter, entry.nutritionFacts))
        },
    )
}
