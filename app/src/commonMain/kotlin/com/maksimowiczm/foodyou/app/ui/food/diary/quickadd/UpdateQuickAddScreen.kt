package com.maksimowiczm.foodyou.app.ui.food.diary.quickadd

import androidx.compose.runtime.*
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
            // Entries created before Story 18 have no serving count; treat that as one serving.
            servingCount = entry.servingCount ?: 1.0,
            weightGrams = entry.weightGrams,
        )

    QuickAddScreen(
        onBack = onBack,
        onSave = {
            val name = formState.name.value
            val energy = formState.energy.value?.let(energyFormatter::toKcal) ?: 0.0
            val proteins = formState.proteins.value ?: 0.0
            val carbohydrates = formState.carbohydrates.value ?: 0.0
            val fats = formState.fats.value ?: 0.0

            viewModel.updateEntry(
                name = name,
                energy = energy,
                proteins = proteins,
                carbohydrates = carbohydrates,
                fats = fats,
                description = formState.description.value,
                fibre = formState.fibre.value,
                servingCount = formState.servingCount.value,
                weightGrams = formState.weightGrams.value,
            )
        },
        modifier = modifier,
        state = formState,
        onPromoteToProduct = { onPromoteToProduct(formState.toPromotionSeed(energyFormatter)) },
        onPromoteToRecipe = { onPromoteToRecipe(formState.toPromotionSeed(energyFormatter)) },
    )
}
