package com.maksimowiczm.foodyou.app.ui.food.component

import com.maksimowiczm.foodyou.app.ui.developer.inspection.UiSourceFile
import com.maksimowiczm.foodyou.app.ui.developer.inspection.uiName

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.maksimowiczm.foodyou.app.ui.common.utility.LocalEnergyFormatter
import com.maksimowiczm.foodyou.app.ui.food.meals.mealDisplayFactor
import com.maksimowiczm.foodyou.app.ui.goals.master.stringResource as nutrientLabel
import com.maksimowiczm.foodyou.common.domain.food.*
import com.maksimowiczm.foodyou.fooddiary.domain.entity.Meal
import foodyou.app.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun CapturedNutritionSummary(facts:NutritionFacts) {
    val energy=LocalEnergyFormatter.current
    Column(verticalArrangement=Arrangement.spacedBy(4.dp), modifier = Modifier.uiName("CapturedNutritionSummary.Column.1", "androidx.compose.foundation.layout.Column", uiInspectionSource, "CapturedNutritionSummary")) {
        NutritionFactsField.entries.forEach { field ->
            val value=facts[field].value
            val scaled=value?.let { if(field==NutritionFactsField.Energy)energy.fromKcal(it)else it*field.mealDisplayFactor() }
            val fallback=scaled!=null && !scaled.isFinite()
            val unit=when {
                fallback -> if(field==NutritionFactsField.Energy)"kcal" else stringResource(Res.string.unit_gram_short)
                field==NutritionFactsField.Energy -> energy.suffix()
                else -> stringResource(when(field.mealDisplayFactor()){1_000_000.0->Res.string.unit_microgram_short;1_000.0->Res.string.unit_milligram_short;else->Res.string.unit_gram_short})
            }
            val quantity=if(value==null)stringResource(Res.string.captured_unknown_value)else {
                val shown=(if(fallback)value else scaled).toString()+" "+unit
                if(facts[field].isIncomplete)stringResource(Res.string.captured_incomplete_value,shown)else shown
            }
            Text(field.nutrientLabel()+": "+quantity, modifier = Modifier.uiName("CapturedNutritionSummary.Text.1", "androidx.compose.material3.Text", uiInspectionSource, "CapturedNutritionSummary"))
        }
    }
}

@Composable
internal fun CapturedMealPanel(meal:Meal) {
    val panel=meal.reportedNutrition ?: return
    var expanded by remember(meal.id){mutableStateOf(false)}
    val total=RecipeCalculation.totalNutrition(meal.items.map { it.nutritionFacts })
    Column(verticalArrangement=Arrangement.spacedBy(4.dp), modifier = Modifier.uiName("CapturedMealPanel.Column.1", "androidx.compose.foundation.layout.Column", uiInspectionSource, "CapturedMealPanel")) {
        Text(stringResource(Res.string.captured_meal_panel),style=MaterialTheme.typography.titleMedium, modifier = Modifier.uiName("CapturedMealPanel.Text.1", "androidx.compose.material3.Text", uiInspectionSource, "CapturedMealPanel"))
        Text(stringResource(Res.string.captured_meal_items_energy,capturedEnergyValue(total.energy)), modifier = Modifier.uiName("CapturedMealPanel.Text.2", "androidx.compose.material3.Text", uiInspectionSource, "CapturedMealPanel"))
        Text(reportedBasisLabel(panel.basis), modifier = Modifier.uiName("CapturedMealPanel.Text.3", "androidx.compose.material3.Text", uiInspectionSource, "CapturedMealPanel"))
        Text(stringResource(Res.string.captured_meal_reported_energy,capturedEnergyValue(panel.facts.energy)), modifier = Modifier.uiName("CapturedMealPanel.Text.4", "androidx.compose.material3.Text", uiInspectionSource, "CapturedMealPanel"))
        Text(stringResource(Res.string.captured_meal_panel_help), modifier = Modifier.uiName("CapturedMealPanel.Text.5", "androidx.compose.material3.Text", uiInspectionSource, "CapturedMealPanel"))
        if(panel.needsReview)Text(stringResource(Res.string.captured_meal_review),color=MaterialTheme.colorScheme.error, modifier = Modifier.uiName("CapturedMealPanel.Text.6", "androidx.compose.material3.Text", uiInspectionSource, "CapturedMealPanel"))
        CapturedReferenceText(panel.reference)
        TextButton(onClick={expanded=!expanded}, modifier = Modifier.uiName("CapturedMealPanel.TextButton.1", "androidx.compose.material3.TextButton", uiInspectionSource, "CapturedMealPanel")){Text(stringResource(if(expanded)Res.string.captured_hide_details else Res.string.captured_show_details), modifier = Modifier.uiName("CapturedMealPanel.Text.7", "androidx.compose.material3.Text", uiInspectionSource, "CapturedMealPanel"))}
        if(expanded)CapturedNutritionSummary(panel.facts)
        CapturedMetadataView(panel.metadata)
        CapturedMetadataView(meal.metadata,true)
    }
}

@Composable private fun capturedEnergyValue(value:NutrientValue):String {
    val amount=value.value ?: return stringResource(Res.string.captured_unknown_value)
    return if(value.isIncomplete)stringResource(Res.string.captured_incomplete_value,amount.toString())else amount.toString()
}


private val uiInspectionSource = UiSourceFile(
    "com.maksimowiczm.foodyou.app.ui.food.component",
    "app/src/commonMain/kotlin/com/maksimowiczm/foodyou/app/ui/food/component/CapturedNutritionSummary.kt",
)
