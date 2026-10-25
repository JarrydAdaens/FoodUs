package com.maksimowiczm.foodyou.app.ui.food.meals

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.maksimowiczm.foodyou.app.ui.common.component.ArrowBackIconButton
import com.maksimowiczm.foodyou.app.ui.common.utility.LocalEnergyFormatter
import com.maksimowiczm.foodyou.app.ui.common.utility.stringResource
import com.maksimowiczm.foodyou.app.ui.goals.master.stringResource as nutrientLabel
import com.maksimowiczm.foodyou.common.domain.food.NutritionFactsField
import com.maksimowiczm.foodyou.fooddiary.domain.entity.MealItem
import foodyou.app.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import com.maksimowiczm.foodyou.app.ui.food.component.CapturedReferenceText
import com.maksimowiczm.foodyou.app.ui.food.component.CapturedMetadataView

@Composable
internal fun MealItemEditor(
    item: MealItem, onCancel: () -> Unit, onSave: (MealItem) -> Unit, modifier: Modifier = Modifier,
) {
    val energy = LocalEnergyFormatter.current
    val form = remember(item, energy) { MealItemForm(item, energy) }
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.meals_edit_item)) },
                navigationIcon = { ArrowBackIconButton(onCancel) },
                actions = {
                    TextButton(onClick = onCancel) { Text(stringResource(Res.string.action_cancel)) }
                    TextButton(onClick = { form.confirm(onSave) }) { Text(stringResource(Res.string.meals_apply_changes)) }
                },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().testTag("meal-item-editor"),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = padding.calculateTopPadding() + 8.dp, bottom = padding.calculateBottomPadding() + 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (form.error) item { Text(stringResource(Res.string.meals_invalid), color = MaterialTheme.colorScheme.error) }
            item {
                OutlinedTextField(value = form.name, onValueChange = { form.name = it },
                    label = { Text(stringResource(Res.string.meals_item_name)) }, modifier = Modifier.fillMaxWidth().testTag("meal-item-name"))
            }
            item {
                val portion = form.portion
                if(item.portionDescription!=null) {
                    Text(stringResource(Res.string.captured_original_quantity))
                    CapturedReferenceText(item.portionDescription)
                }
                CapturedMetadataView(item.metadata)
                if (portion == null) Text(stringResource(Res.string.meals_original_unknown))
                else {
                    Text(portion.stringResource())
                    DecimalInput(form.amount, { form.amount = it }, stringResource(Res.string.meals_captured_amount), "meal-item-amount")
                }
            }
            item {
                Text(stringResource(Res.string.meals_quantity_help))
                DecimalInput(form.servings, { form.servings = it }, stringResource(Res.string.label_number_of_servings), "meal-item-servings")
                DecimalInput(form.grams, { form.grams = it }, stringResource(Res.string.weight) + " (g)", "meal-item-grams")
            }
            item {
                Text(stringResource(Res.string.meals_scale_help))
                DecimalInput(form.factor, { form.factor = it }, stringResource(Res.string.meals_scale_factor), "meal-item-factor")
                TextButton(onClick = form::scale) { Text(stringResource(Res.string.meals_scale)) }
                Text(stringResource(Res.string.meals_captured_nutrition), style = MaterialTheme.typography.titleMedium)
                Text(stringResource(Res.string.meals_incomplete_help))
            }
            items(NutritionFactsField.entries, key = { it.name }) { field ->
                val suffix = if (field == NutritionFactsField.Energy) energy.suffix() else
                    stringResource(when (field.mealDisplayFactor()) {
                        1_000_000.0 -> Res.string.unit_microgram_short
                        1_000.0 -> Res.string.unit_milligram_short
                        else -> Res.string.unit_gram_short
                    })
                DecimalInput(form.nutrients[field].orEmpty(), { form.nutrients[field] = it },
                    field.nutrientLabel() + " (" + suffix + ")", "meal-nutrient-" + field.name)
            }
        }
    }
}

@Composable
private fun DecimalInput(value: String, onValueChange: (String) -> Unit, label: String, tag: String) {
    OutlinedTextField(
        value = value, onValueChange = onValueChange, label = { Text(label) },
        modifier = Modifier.fillMaxWidth().testTag(tag), singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
    )
}
