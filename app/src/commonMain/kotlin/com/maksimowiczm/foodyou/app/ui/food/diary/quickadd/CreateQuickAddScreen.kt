package com.maksimowiczm.foodyou.app.ui.food.diary.quickadd

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.maksimowiczm.foodyou.ai.domain.AiFoodEstimate
import com.maksimowiczm.foodyou.ai.domain.toNutritionFacts
import com.maksimowiczm.foodyou.common.domain.food.NutritionFacts
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
    // Story 19 promotion. Opens the product/recipe editor prefilled from the current form values
    // without persisting or mutating any diary entry (spec §6.2/§6.3, §6.5).
    onPromoteToProduct: (QuickAddPromotionSeed) -> Unit = {},
    onPromoteToRecipe: (QuickAddPromotionSeed) -> Unit = {},
    // Optional prefill, used by the AI scanning flow (Milestone 2, Story 6) to seed the form from an
    // AI food estimate. Energy is in kilocalories. All null by default, so normal Quick Add is
    // unaffected.
    prefillName: String? = null,
    prefillEnergyKcal: Double? = null,
    prefillProteins: Double? = null,
    prefillCarbohydrates: Double? = null,
    prefillFats: Double? = null,
    scanEstimate: AiFoodEstimate? = null,
) {
    val viewModel: CreateQuickAddViewModel = koinViewModel { parametersOf(date, mealId) }
    val energyFormatter = LocalEnergyFormatter.current
    val originalNutrition = scanEstimate?.toNutritionFacts() ?: NutritionFacts()

    val latestOnSave by rememberUpdatedState(onSave)
    LaunchedCollectWithLifecycle(viewModel.uiEvents) {
        when (it) {
            QuickAddUiEvent.Saved -> latestOnSave()
        }
    }

    val formState =
        rememberQuickAddFormState(
            name = scanEstimate?.name ?: prefillName ?: "",
            proteins = scanEstimate?.protein ?: prefillProteins,
            carbohydrates = scanEstimate?.carbohydrates ?: prefillCarbohydrates,
            fats = scanEstimate?.fat ?: prefillFats,
            energy = scanEstimate?.calories ?: prefillEnergyKcal,
            description = scanEstimate?.description,
            fibre = scanEstimate?.fibre,
            sugar = scanEstimate?.sugar,
            servingCount = if (scanEstimate != null) scanEstimate.servingCount else 1.0,
            weightGrams = scanEstimate?.weightGrams,
            preserveEnergy = scanEstimate != null,
        )

    QuickAddScreen(
        onBack = onBack,
        onSave = {
            val name = formState.name.value

            viewModel.addEntry(
                name = name,
                nutritionFacts = formState.toNutritionFacts(
                    formatter = energyFormatter,
                    original = originalNutrition,
                    preserveUnknown = scanEstimate != null,
                ),
                description = formState.description.value,
                servingCount = formState.servingCount.value,
                weightGrams = formState.weightGrams.value,
            )
        },
        modifier = modifier,
        state = formState,
        nutritionDetails = scanEstimate?.let {
            formState.toNutritionFacts(energyFormatter, originalNutrition)
        },
        onPromoteToProduct = {
            onPromoteToProduct(formState.toPromotionSeed(energyFormatter, originalNutrition))
        },
        onPromoteToRecipe = {
            onPromoteToRecipe(formState.toPromotionSeed(energyFormatter, originalNutrition))
        },
    )
}
