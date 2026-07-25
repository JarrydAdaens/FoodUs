package com.maksimowiczm.foodyou.app.ui.food.diary.quickadd

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.maksimowiczm.foodyou.app.ui.common.utility.LocalEnergyFormatter
import com.maksimowiczm.foodyou.common.compose.extension.LaunchedCollectWithLifecycle
import kotlinx.datetime.LocalDate
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun CreateQuickAddScreen(
    onBack: () -> Unit,
    onSave: () -> Unit,
    mealId: Long,
    date: LocalDate,
    modifier: Modifier = Modifier,
    // Optional prefill, used by the AI scanning flow (Milestone 2, Story 6) to seed the form from an
    // AI food estimate. Energy is in kilocalories. All null by default, so normal Quick Add is
    // unaffected.
    prefillName: String? = null,
    prefillEnergyKcal: Double? = null,
    prefillProteins: Double? = null,
    prefillCarbohydrates: Double? = null,
    prefillFats: Double? = null,
) {
    val viewModel: CreateQuickAddViewModel = koinViewModel { parametersOf(date, mealId) }
    val energyFormatter = LocalEnergyFormatter.current

    val latestOnSave by rememberUpdatedState(onSave)
    LaunchedCollectWithLifecycle(viewModel.uiEvents) {
        when (it) {
            QuickAddUiEvent.Saved -> latestOnSave()
        }
    }

    val formState =
        rememberQuickAddFormState(
            name = prefillName ?: "",
            proteins = prefillProteins,
            carbohydrates = prefillCarbohydrates,
            fats = prefillFats,
            energy = prefillEnergyKcal,
        )

    QuickAddScreen(
        onBack = onBack,
        onSave = {
            val name = formState.name.value
            val energy = formState.energy.value?.let(energyFormatter::toKcal) ?: 0.0
            val proteins = formState.proteins.value ?: 0.0
            val carbohydrates = formState.carbohydrates.value ?: 0.0
            val fats = formState.fats.value ?: 0.0

            viewModel.addEntry(
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
    )
}
