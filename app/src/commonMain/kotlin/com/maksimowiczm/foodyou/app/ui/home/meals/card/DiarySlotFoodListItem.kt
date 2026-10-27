package com.maksimowiczm.foodyou.app.ui.home.meals.card

import com.maksimowiczm.foodyou.app.ui.developer.inspection.UiSourceFile
import com.maksimowiczm.foodyou.app.ui.developer.inspection.uiName

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import com.maksimowiczm.foodyou.app.ui.common.component.FoodErrorListItem
import com.maksimowiczm.foodyou.app.ui.common.component.FoodListItem
import com.maksimowiczm.foodyou.app.ui.common.utility.LocalEnergyFormatter
import com.maksimowiczm.foodyou.app.ui.common.utility.stringResource
import com.maksimowiczm.foodyou.app.ui.common.utility.stringResource
import com.maksimowiczm.foodyou.app.ui.common.utility.stringResourceWithWeight
import com.maksimowiczm.foodyou.common.compose.utility.formatClipZeros
import com.maksimowiczm.foodyou.common.domain.food.*
import com.maksimowiczm.foodyou.app.ui.food.component.reportedBasisLabel
import com.maksimowiczm.foodyou.common.domain.measurement.rawValue
import com.maksimowiczm.foodyou.app.ui.food.component.CapturedReferenceText
import foodyou.app.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun DiarySlotFoodListItem(
    entry: DiarySlotEntryModel,
    color: Color,
    contentColor: Color,
    shape: Shape,
    modifier: Modifier = Modifier,
) {
    when (entry) {
        is FoodDiarySlotEntryModel ->
            DiarySlotFoodListItem(
                entry = entry,
                color = color,
                contentColor = contentColor,
                shape = shape,
                modifier = (modifier).uiName("DiarySlotFoodListItem.DiarySlotFoodListItem.1", "com.maksimowiczm.foodyou.app.ui.home.meals.card.DiarySlotFoodListItem", uiInspectionSource, "DiarySlotFoodListItem"),
            )

        is ManualDiarySlotEntryModel ->
            DiarySlotFoodListItem(
                entry = entry,
                color = color,
                contentColor = contentColor,
                shape = shape,
                modifier = (modifier).uiName("DiarySlotFoodListItem.DiarySlotFoodListItem.2", "com.maksimowiczm.foodyou.app.ui.home.meals.card.DiarySlotFoodListItem", uiInspectionSource, "DiarySlotFoodListItem"),
            )
    }
}

@Composable
internal fun DiarySlotFoodListItem(
    entry: FoodDiarySlotEntryModel,
    color: Color,
    contentColor: Color,
    shape: Shape,
    modifier: Modifier = Modifier,
) {
    if(entry.isCapturedContext) {
        val facts=entry.capturedFacts
        if(facts==null || !facts.hasValidValues() || entry.name.isBlank() || !entry.referencePortion.validCapturedReference() ||
            !entry.measurement.rawValue.isFinite() || entry.measurement.rawValue<=0.0) {
            FoodErrorListItem(headline=entry.name,errorMessage=stringResource(Res.string.error_measurement_error),modifier=(modifier).uiName("DiarySlotFoodListItem.FoodErrorListItem.1", "com.maksimowiczm.foodyou.app.ui.common.component.FoodErrorListItem", uiInspectionSource, "DiarySlotFoodListItem"))
            return
        }
        CapturedDiaryRow(entry.name,facts,entry.isRecipe,color,contentColor,shape,(modifier).uiName("DiarySlotFoodListItem.CapturedDiaryRow.1", "com.maksimowiczm.foodyou.app.ui.home.meals.card.CapturedDiaryRow", uiInspectionSource, "DiarySlotFoodListItem")) {
            Text(entry.measurement.stringResourceWithWeight(entry.totalWeight,entry.servingWeight,entry.isLiquid) ?: entry.measurement.stringResource(), modifier = Modifier.uiName("DiarySlotFoodListItem.Text.1", "androidx.compose.material3.Text", uiInspectionSource, "DiarySlotFoodListItem"))
            Text(entry.reportedBasis?.let { reportedBasisLabel(it) } ?: stringResource(Res.string.captured_reference_serving), modifier = Modifier.uiName("DiarySlotFoodListItem.Text.2", "androidx.compose.material3.Text", uiInspectionSource, "DiarySlotFoodListItem"))
            CapturedReferenceText(entry.referencePortion)
        }
        return
    }
    val g = stringResource(Res.string.unit_gram_short)

    val proteinsString = entry.proteins?.let { it.formatClipZeros("%.1f") + " $g" }

    val carbohydratesString = entry.carbohydrates?.let { it.formatClipZeros("%.1f") + " $g" }

    val fatsString = entry.fats?.let { it.formatClipZeros("%.1f") + " $g" }

    val caloriesString = entry.energy?.let { LocalEnergyFormatter.current.formatEnergy(it) }

    val measurementString =
        entry.measurement.stringResourceWithWeight(
            totalWeight = entry.totalWeight,
            servingWeight = entry.servingWeight,
            isLiquid = entry.isLiquid,
        ) ?: if (entry.isRecipe) entry.measurement.stringResource() else null

    if (measurementString == null) {
        FoodErrorListItem(
            headline = entry.name,
            errorMessage = stringResource(Res.string.error_measurement_error),
            modifier = (modifier).uiName("DiarySlotFoodListItem.FoodErrorListItem.2", "com.maksimowiczm.foodyou.app.ui.common.component.FoodErrorListItem", uiInspectionSource, "DiarySlotFoodListItem"),
        )
    } else if (
        proteinsString == null ||
            carbohydratesString == null ||
            fatsString == null ||
            caloriesString == null
    ) {
        FoodErrorListItem(
            headline = entry.name,
            errorMessage = stringResource(Res.string.error_food_is_missing_required_fields),
            modifier = (modifier).uiName("DiarySlotFoodListItem.FoodErrorListItem.3", "com.maksimowiczm.foodyou.app.ui.common.component.FoodErrorListItem", uiInspectionSource, "DiarySlotFoodListItem"),
        )
    } else {
        FoodListItem(
            name = { Text(entry.name, modifier = Modifier.uiName("DiarySlotFoodListItem.Text.3", "androidx.compose.material3.Text", uiInspectionSource, "DiarySlotFoodListItem")) },
            proteins = { Text(text = proteinsString, style = MaterialTheme.typography.bodySmall, modifier = Modifier.uiName("DiarySlotFoodListItem.Text.4", "androidx.compose.material3.Text", uiInspectionSource, "DiarySlotFoodListItem")) },
            carbohydrates = {
                Text(text = carbohydratesString, style = MaterialTheme.typography.bodySmall, modifier = Modifier.uiName("DiarySlotFoodListItem.Text.5", "androidx.compose.material3.Text", uiInspectionSource, "DiarySlotFoodListItem"))
            },
            fats = { Text(text = fatsString, style = MaterialTheme.typography.bodySmall, modifier = Modifier.uiName("DiarySlotFoodListItem.Text.6", "androidx.compose.material3.Text", uiInspectionSource, "DiarySlotFoodListItem")) },
            calories = { Text(text = caloriesString, style = MaterialTheme.typography.bodySmall, modifier = Modifier.uiName("DiarySlotFoodListItem.Text.7", "androidx.compose.material3.Text", uiInspectionSource, "DiarySlotFoodListItem")) },
            measurement = {
                Text(text = measurementString, style = MaterialTheme.typography.bodySmall, modifier = Modifier.uiName("DiarySlotFoodListItem.Text.8", "androidx.compose.material3.Text", uiInspectionSource, "DiarySlotFoodListItem"))
            },
            isRecipe = entry.isRecipe, alignNutritionColumns = true,
            modifier = (modifier).uiName("DiarySlotFoodListItem.FoodListItem.1", "com.maksimowiczm.foodyou.app.ui.common.component.FoodListItem", uiInspectionSource, "DiarySlotFoodListItem"),
            containerColor = color,
            contentColor = contentColor,
            shape = shape,
            contentPadding = PaddingValues(vertical = 8.dp, horizontal = 16.dp),
        )
    }
}

@Composable
internal fun DiarySlotFoodListItem(
    entry: ManualDiarySlotEntryModel,
    color: Color,
    contentColor: Color,
    shape: Shape,
    modifier: Modifier = Modifier,
) {
    val captured = entry.capturedFacts
    if (captured != null) {
        val valid = !entry.isPlaceholder && entry.name.isNotBlank() && captured.hasValidValues() &&
            entry.portionDescription.validCapturedReference() && entry.capturedPortion?.rawValue.let { it==null || it.isFinite() && it>0.0 }
        if (!valid) {
            FoodErrorListItem(headline=entry.name,errorMessage=stringResource(Res.string.error_food_is_missing_required_fields),modifier=(modifier).uiName("DiarySlotFoodListItem.FoodErrorListItem.4", "com.maksimowiczm.foodyou.app.ui.common.component.FoodErrorListItem", uiInspectionSource, "DiarySlotFoodListItem"))
            return
        }
        CapturedDiaryRow(entry.name,captured,false,color,contentColor,shape,(modifier).uiName("DiarySlotFoodListItem.CapturedDiaryRow.2", "com.maksimowiczm.foodyou.app.ui.home.meals.card.CapturedDiaryRow", uiInspectionSource, "DiarySlotFoodListItem")) {
            entry.capturedPortion?.let { Text(it.stringResource(), modifier = Modifier.uiName("DiarySlotFoodListItem.Text.9", "androidx.compose.material3.Text", uiInspectionSource, "DiarySlotFoodListItem")) }
            CapturedReferenceText(entry.portionDescription)
        }
        return
    }
    val g = stringResource(Res.string.unit_gram_short)

    val proteinsString = entry.proteins?.let { it.formatClipZeros("%.1f") + " $g" }

    val carbohydratesString = entry.carbohydrates?.let { it.formatClipZeros("%.1f") + " $g" }

    val fatsString = entry.fats?.let { it.formatClipZeros("%.1f") + " $g" }

    val caloriesString = entry.energy?.let { LocalEnergyFormatter.current.formatEnergy(it) }

    if (
        proteinsString == null ||
            carbohydratesString == null ||
            fatsString == null ||
            caloriesString == null
    ) {
        FoodErrorListItem(
            headline = entry.name,
            errorMessage = stringResource(Res.string.error_food_is_missing_required_fields),
            modifier = (modifier).uiName("DiarySlotFoodListItem.FoodErrorListItem.5", "com.maksimowiczm.foodyou.app.ui.common.component.FoodErrorListItem", uiInspectionSource, "DiarySlotFoodListItem"),
        )
    } else {
        FoodListItem(
            name = {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically, modifier = Modifier.uiName("DiarySlotFoodListItem.Row.1", "androidx.compose.foundation.layout.Row", uiInspectionSource, "DiarySlotFoodListItem")
                ) {
                    Text(entry.name, modifier = Modifier.uiName("DiarySlotFoodListItem.Text.10", "androidx.compose.material3.Text", uiInspectionSource, "DiarySlotFoodListItem"))
                    Icon(
                        imageVector = Icons.Outlined.Bolt,
                        contentDescription = null,
                        modifier = (Modifier.size(18.dp)).uiName("DiarySlotFoodListItem.Icon.1", "androidx.compose.material3.Icon", uiInspectionSource, "DiarySlotFoodListItem"),
                    )
                }
            },
            proteins = { Text(text = proteinsString, style = MaterialTheme.typography.bodySmall, modifier = Modifier.uiName("DiarySlotFoodListItem.Text.11", "androidx.compose.material3.Text", uiInspectionSource, "DiarySlotFoodListItem")) },
            carbohydrates = {
                Text(text = carbohydratesString, style = MaterialTheme.typography.bodySmall, modifier = Modifier.uiName("DiarySlotFoodListItem.Text.12", "androidx.compose.material3.Text", uiInspectionSource, "DiarySlotFoodListItem"))
            },
            fats = { Text(text = fatsString, style = MaterialTheme.typography.bodySmall, modifier = Modifier.uiName("DiarySlotFoodListItem.Text.13", "androidx.compose.material3.Text", uiInspectionSource, "DiarySlotFoodListItem")) },
            calories = { Text(text = caloriesString, style = MaterialTheme.typography.bodySmall, modifier = Modifier.uiName("DiarySlotFoodListItem.Text.14", "androidx.compose.material3.Text", uiInspectionSource, "DiarySlotFoodListItem")) },
            measurement = { entry.capturedPortion?.let { Text(it.stringResource(), style = MaterialTheme.typography.bodySmall, modifier = Modifier.uiName("DiarySlotFoodListItem.Text.15", "androidx.compose.material3.Text", uiInspectionSource, "DiarySlotFoodListItem")) } },
            isRecipe = false, alignNutritionColumns = true,
            modifier = (modifier).uiName("DiarySlotFoodListItem.FoodListItem.2", "com.maksimowiczm.foodyou.app.ui.common.component.FoodListItem", uiInspectionSource, "DiarySlotFoodListItem"),
            containerColor = color,
            contentColor = contentColor,
            shape = shape,
            contentPadding = PaddingValues(vertical = 8.dp, horizontal = 16.dp),
        )
    }
}

private fun PortionDescription?.validCapturedReference():Boolean = this==null ||
    listOf(amount,metricAmount).all { it==null || it.isFinite() && it>0.0 } && (metricAmount==null)==(metricUnit==null)

/** Captured snapshots can be valid with unknown nutrients; no missing value becomes zero. */
@Composable
private fun CapturedDiaryRow(name:String,facts:NutritionFacts,isRecipe:Boolean,color:Color,contentColor:Color,shape:Shape,
    modifier:Modifier,context:@Composable ()->Unit) {
    val unknown=stringResource(Res.string.captured_unknown_value)
    val gram=stringResource(Res.string.unit_gram_short)
    fun macro(value:NutrientValue)=value.value?.takeIf { value.isComplete }?.let { it.formatClipZeros("%.1f")+" "+gram } ?: unknown
    val calories=facts.energy.value?.takeIf { facts.energy.isComplete }?.let {
        if(it>Int.MAX_VALUE)it.toString()+" kcal" else LocalEnergyFormatter.current.formatEnergy(it)
    } ?: unknown
    FoodListItem(name={Text(name, modifier = Modifier.uiName("CapturedDiaryRow.Text.1", "androidx.compose.material3.Text", uiInspectionSource, "CapturedDiaryRow"))},proteins={Text(macro(facts.proteins), modifier = Modifier.uiName("CapturedDiaryRow.Text.2", "androidx.compose.material3.Text", uiInspectionSource, "CapturedDiaryRow"))},carbohydrates={Text(macro(facts.carbohydrates), modifier = Modifier.uiName("CapturedDiaryRow.Text.3", "androidx.compose.material3.Text", uiInspectionSource, "CapturedDiaryRow"))},
        fats={Text(macro(facts.fats), modifier = Modifier.uiName("CapturedDiaryRow.Text.4", "androidx.compose.material3.Text", uiInspectionSource, "CapturedDiaryRow"))},calories={Text(calories, modifier = Modifier.uiName("CapturedDiaryRow.Text.5", "androidx.compose.material3.Text", uiInspectionSource, "CapturedDiaryRow"))},measurement={Column (modifier = Modifier.uiName("CapturedDiaryRow.Column.1", "androidx.compose.foundation.layout.Column", uiInspectionSource, "CapturedDiaryRow")) { context() }},isRecipe=isRecipe,alignNutritionColumns=true,
        modifier=(modifier).uiName("CapturedDiaryRow.FoodListItem.1", "com.maksimowiczm.foodyou.app.ui.common.component.FoodListItem", uiInspectionSource, "CapturedDiaryRow"),containerColor=color,contentColor=contentColor,shape=shape,
        contentPadding=PaddingValues(vertical=8.dp,horizontal=16.dp))
}


private val uiInspectionSource = UiSourceFile(
    "com.maksimowiczm.foodyou.app.ui.home.meals.card",
    "app/src/commonMain/kotlin/com/maksimowiczm/foodyou/app/ui/home/meals/card/DiarySlotFoodListItem.kt",
)
