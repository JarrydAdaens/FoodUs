package com.maksimowiczm.foodyou.app.ui.food.diary.quickadd


import com.maksimowiczm.foodyou.app.ui.developer.inspection.UiSourceScope

import com.maksimowiczm.foodyou.app.ui.developer.inspection.UiSourceFile
import com.maksimowiczm.foodyou.app.ui.developer.inspection.uiName

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Calculate
import androidx.compose.material.icons.outlined.Keyboard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import com.maksimowiczm.foodyou.app.ui.common.form.FormField
import com.maksimowiczm.foodyou.app.ui.common.utility.LocalEnergyFormatter
import com.maksimowiczm.foodyou.app.ui.common.utility.LocalNutrientsOrder
import com.maksimowiczm.foodyou.common.compose.component.unorderedList
import com.maksimowiczm.foodyou.settings.domain.entity.NutrientsOrder
import foodyou.app.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun QuickAddForm(state: QuickAddFormState, modifier: Modifier = Modifier) {
    val energyFormatter = LocalEnergyFormatter.current

    Column(modifier = (modifier).uiName("QuickAddForm.Column.1", "androidx.compose.foundation.layout.Column", uiInspectionSource, "QuickAddForm")) {
        OutlinedTextField(
            state = state.name.textFieldState,
            modifier = (Modifier.fillMaxWidth()).uiName("QuickAddForm.OutlinedTextField.1", "androidx.compose.material3.OutlinedTextField", uiInspectionSource, "QuickAddForm"),
            label = { Text(stringResource(Res.string.product_name), modifier = Modifier.uiName("QuickAddForm.Text.1", "androidx.compose.material3.Text", uiInspectionSource, "QuickAddForm")) },
            supportingText = { Text(stringResource(Res.string.neutral_required), modifier = Modifier.uiName("QuickAddForm.Text.2", "androidx.compose.material3.Text", uiInspectionSource, "QuickAddForm")) },
            isError = state.name.error != null,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
        )

        OutlinedTextField(
            state = state.description.textFieldState,
            modifier = (Modifier.fillMaxWidth()).uiName("QuickAddForm.OutlinedTextField.2", "androidx.compose.material3.OutlinedTextField", uiInspectionSource, "QuickAddForm"),
            label = { Text(stringResource(Res.string.label_description), modifier = Modifier.uiName("QuickAddForm.Text.3", "androidx.compose.material3.Text", uiInspectionSource, "QuickAddForm")) },
            supportingText = { Text(stringResource(Res.string.neutral_optional), modifier = Modifier.uiName("QuickAddForm.Text.4", "androidx.compose.material3.Text", uiInspectionSource, "QuickAddForm")) },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
        )

        OutlinedTextField(
            state = state.energy.textFieldState,
            modifier = (Modifier.fillMaxWidth()).uiName("QuickAddForm.OutlinedTextField.3", "androidx.compose.material3.OutlinedTextField", uiInspectionSource, "QuickAddForm"),
            label = { Text(stringResource(Res.string.unit_energy), modifier = Modifier.uiName("QuickAddForm.Text.5", "androidx.compose.material3.Text", uiInspectionSource, "QuickAddForm")) },
            supportingText = {
                val error = state.energy.error
                if (error != null) {
                    Text(error.stringResource(), modifier = Modifier.uiName("QuickAddForm.Text.6", "androidx.compose.material3.Text", uiInspectionSource, "QuickAddForm"))
                }
            },
            suffix = { Text(energyFormatter.suffix(), modifier = Modifier.uiName("QuickAddForm.Text.7", "androidx.compose.material3.Text", uiInspectionSource, "QuickAddForm")) },
            trailingIcon = {
                UiSourceScope(uiInspectionSource.element("QuickAddForm.TooltipBox.1", "androidx.compose.material3.TooltipBox", "QuickAddForm")) { TooltipBox(
                    positionProvider =
                        TooltipDefaults.rememberTooltipPositionProvider(
                            TooltipAnchorPosition.Above
                        ),
                    tooltip = {
                        PlainTooltip(modifier = Modifier.uiName("QuickAddForm.PlainTooltip.1", "androidx.compose.material3.PlainTooltip", uiInspectionSource, "QuickAddForm")) {
                            Text(
                                text =
                                    if (state.autoCalculateEnergy) {
                                        stringResource(Res.string.headline_auto_calculate_energy)
                                    } else {
                                        stringResource(Res.string.headline_manual_energy_input)
                                    },
                                style = MaterialTheme.typography.bodySmall, modifier = Modifier.uiName("QuickAddForm.Text.8", "androidx.compose.material3.Text", uiInspectionSource, "QuickAddForm")
                            )
                        }
                    },
                    state = rememberTooltipState(isPersistent = true), modifier = Modifier.uiName("QuickAddForm.TooltipBox.1.root", "androidx.compose.material3.TooltipBox", uiInspectionSource, "QuickAddForm")
                ) {
                    IconButton(
                        onClick = { state.autoCalculateEnergy = !state.autoCalculateEnergy }, modifier = Modifier.uiName("QuickAddForm.IconButton.1", "androidx.compose.material3.IconButton", uiInspectionSource, "QuickAddForm")
                    ) {
                        if (state.autoCalculateEnergy) {
                            Icon(imageVector = Icons.Outlined.Calculate, contentDescription = stringResource(Res.string.headline_auto_calculate_energy), modifier = Modifier.uiName("QuickAddForm.Icon.1", "androidx.compose.material3.Icon", uiInspectionSource, "QuickAddForm"))
                        } else {
                            Icon(imageVector = Icons.Outlined.Keyboard, contentDescription = stringResource(Res.string.headline_manual_energy_input), modifier = Modifier.uiName("QuickAddForm.Icon.2", "androidx.compose.material3.Icon", uiInspectionSource, "QuickAddForm"))
                        }
                    }
                } }
            },
            isError = state.energy.error != null,
            keyboardOptions =
                KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next),
        )


        state.weightGrams.TextField(
            label = stringResource(Res.string.weight),
            modifier = (Modifier.fillMaxWidth()).uiName("QuickAddForm.TextField.2", "com.maksimowiczm.foodyou.app.ui.food.diary.quickadd.TextField", uiInspectionSource, "QuickAddForm"),
        )

        state.servingCount.TextField(
            label = stringResource(Res.string.label_number_of_servings),
            modifier = (Modifier.fillMaxWidth()).uiName("QuickAddForm.TextField.1", "com.maksimowiczm.foodyou.app.ui.food.diary.quickadd.TextField", uiInspectionSource, "QuickAddForm"),
            suffix = null,
        )

        LocalNutrientsOrder.current.forEach {
            when (it) {
                NutrientsOrder.Proteins ->
                    state.proteins.TextField(
                        label = stringResource(Res.string.nutriment_proteins),
                        modifier = (Modifier.fillMaxWidth()).uiName("QuickAddForm.TextField.3", "com.maksimowiczm.foodyou.app.ui.food.diary.quickadd.TextField", uiInspectionSource, "QuickAddForm"),
                    )
                NutrientsOrder.Fats ->
                    state.fats.TextField(
                        label = stringResource(Res.string.nutriment_fats),
                        modifier = (Modifier.fillMaxWidth()).uiName("QuickAddForm.TextField.4", "com.maksimowiczm.foodyou.app.ui.food.diary.quickadd.TextField", uiInspectionSource, "QuickAddForm"),
                    )
                NutrientsOrder.Carbohydrates ->
                    state.carbohydrates.TextField(
                        label = stringResource(Res.string.nutriment_carbohydrates),
                        modifier = (Modifier.fillMaxWidth()).uiName("QuickAddForm.TextField.5", "com.maksimowiczm.foodyou.app.ui.food.diary.quickadd.TextField", uiInspectionSource, "QuickAddForm"),
                    )
                else -> Unit
            }
        }

        state.fibre.TextField(
            label = stringResource(Res.string.nutriment_fiber),
            modifier = (Modifier.fillMaxWidth()).uiName("QuickAddForm.TextField.6", "com.maksimowiczm.foodyou.app.ui.food.diary.quickadd.TextField", uiInspectionSource, "QuickAddForm"),
        )

        state.sugar.TextField(
            label = stringResource(Res.string.nutriment_sugars),
            modifier = (Modifier.fillMaxWidth()).uiName("QuickAddForm.TextField.7", "com.maksimowiczm.foodyou.app.ui.food.diary.quickadd.TextField", uiInspectionSource, "QuickAddForm"),
        )


        Text(
            text = stringResource(Res.string.description_calories_are_calculated),
            style = MaterialTheme.typography.bodySmall, modifier = Modifier.uiName("QuickAddForm.Text.9", "androidx.compose.material3.Text", uiInspectionSource, "QuickAddForm")
        )
        Text(
            text =
                unorderedList(
                    stringResource(
                        Res.string.x_energy_unit_per_g,
                        stringResource(Res.string.nutriment_proteins),
                        energyFormatter.proteinsEnergyDensity,
                        energyFormatter.suffix(),
                    ),
                    stringResource(
                        Res.string.x_energy_unit_per_g,
                        stringResource(Res.string.nutriment_carbohydrates),
                        energyFormatter.carbohydratesEnergyDensity,
                        energyFormatter.suffix(),
                    ),
                    stringResource(
                        Res.string.x_energy_unit_per_g,
                        stringResource(Res.string.nutriment_fats),
                        energyFormatter.fatsEnergyDensity,
                        energyFormatter.suffix(),
                    ),
                ),
            style = MaterialTheme.typography.bodySmall, modifier = Modifier.uiName("QuickAddForm.Text.10", "androidx.compose.material3.Text", uiInspectionSource, "QuickAddForm")
        )
    }
}

@Composable
private fun FormField<Double?, QuickAddFormFieldError>.TextField(
    label: String,
    modifier: Modifier = Modifier,
    suffix: String? = stringResource(Res.string.unit_gram_short),
) {
    OutlinedTextField(
        state = textFieldState,
        modifier = (modifier).uiName("TextField.OutlinedTextField.4", "androidx.compose.material3.OutlinedTextField", uiInspectionSource, "TextField"),
        keyboardOptions =
            KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next),
        suffix = suffix?.let { { Text(it, modifier = Modifier.uiName("TextField.Text.11", "androidx.compose.material3.Text", uiInspectionSource, "TextField")) } },
        supportingText = {
            val error = error
            if (error != null) {
                Text(error.stringResource(), modifier = Modifier.uiName("TextField.Text.12", "androidx.compose.material3.Text", uiInspectionSource, "TextField"))
            }
        },
        isError = error != null,
        label = { Text(label, modifier = Modifier.uiName("TextField.Text.13", "androidx.compose.material3.Text", uiInspectionSource, "TextField")) },
    )
}


private val uiInspectionSource = UiSourceFile(
    "com.maksimowiczm.foodyou.app.ui.food.diary.quickadd",
    "app/src/commonMain/kotlin/com/maksimowiczm/foodyou/app/ui/food/diary/quickadd/QuickAddForm.kt",
)
