package com.maksimowiczm.foodyou.app.ui.food.product



import com.maksimowiczm.foodyou.app.ui.developer.inspection.UiSourceFile
import com.maksimowiczm.foodyou.app.ui.developer.inspection.uiName
import com.maksimowiczm.foodyou.app.ui.developer.inspection.UiSourceScope

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.outlined.Calculate
import androidx.compose.material.icons.outlined.Keyboard
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.SplitButtonDefaults
import androidx.compose.material3.SplitButtonLayout
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.maksimowiczm.foodyou.app.ui.common.component.FullScreenCameraBarcodeScanner
import com.maksimowiczm.foodyou.app.ui.common.form.FormField
import com.maksimowiczm.foodyou.app.ui.common.utility.LocalEnergyFormatter
import com.maksimowiczm.foodyou.app.ui.common.utility.LocalNutrientsOrder
import com.maksimowiczm.foodyou.app.ui.common.utility.stringResource
import com.maksimowiczm.foodyou.app.ui.food.component.Icon
import com.maksimowiczm.foodyou.app.ui.food.component.stringResource
import com.maksimowiczm.foodyou.common.compose.component.unorderedList
import com.maksimowiczm.foodyou.common.domain.food.ProductSource
import com.maksimowiczm.foodyou.common.domain.measurement.Measurement
import com.maksimowiczm.foodyou.settings.domain.entity.NutrientsOrder
import foodyou.app.generated.resources.*
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun ProductForm(
    state: ProductFormState,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    val layoutDirection = LocalLayoutDirection.current
    val horizontalPadding =
        PaddingValues(
            start = contentPadding.calculateStartPadding(layoutDirection),
            end = contentPadding.calculateStartPadding(layoutDirection),
        )
    val verticalPadding =
        PaddingValues(
            top = contentPadding.calculateTopPadding(),
            bottom = contentPadding.calculateBottomPadding(),
        )

    val order = LocalNutrientsOrder.current

    Column(
        modifier = (modifier.padding(verticalPadding)).uiName("ProductForm.Column.1", "androidx.compose.foundation.layout.Column", uiInspectionSource, "ProductForm"),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (state.basisUnavailable) {
            Text(
                text = stringResource(Res.string.error_product_nutrition_basis),
                color = MaterialTheme.colorScheme.error,
                modifier = (Modifier.padding(horizontalPadding)).uiName("ProductForm.Text.1", "androidx.compose.material3.Text", uiInspectionSource, "ProductForm"),
            )
        }
        if (state.saveFailed) {
            Text(
                text = stringResource(Res.string.error_product_save),
                color = MaterialTheme.colorScheme.error,
                modifier = (Modifier.padding(horizontalPadding)).uiName("ProductForm.Text.2", "androidx.compose.material3.Text", uiInspectionSource, "ProductForm"),
            )
        }
        General(state = state, horizontalPadding = horizontalPadding)

        Text(
            text = stringResource(Res.string.headline_macronutrients),
            modifier = (Modifier.padding(horizontalPadding).fillMaxWidth()).uiName("ProductForm.Text.3", "androidx.compose.material3.Text", uiInspectionSource, "ProductForm"),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
        )

        order.forEach { field ->
            when (field) {
                NutrientsOrder.Proteins ->
                    state.proteins.TextField(
                        label = stringResource(Res.string.nutriment_proteins),
                        modifier = (Modifier.padding(horizontalPadding).fillMaxWidth()).uiName("ProductForm.TextField.1", "com.maksimowiczm.foodyou.app.ui.food.product.TextField", uiInspectionSource, "ProductForm"),
                        required = true,
                    )

                NutrientsOrder.Carbohydrates ->
                    state.carbohydrates.TextField(
                        label = stringResource(Res.string.nutriment_carbohydrates),
                        modifier = (Modifier.padding(horizontalPadding).fillMaxWidth()).uiName("ProductForm.TextField.2", "com.maksimowiczm.foodyou.app.ui.food.product.TextField", uiInspectionSource, "ProductForm"),
                        required = true,
                    )

                NutrientsOrder.Fats ->
                    state.fats.TextField(
                        label = stringResource(Res.string.nutriment_fats),
                        modifier = (Modifier.padding(horizontalPadding).fillMaxWidth()).uiName("ProductForm.TextField.3", "com.maksimowiczm.foodyou.app.ui.food.product.TextField", uiInspectionSource, "ProductForm"),
                        required = true,
                    )

                NutrientsOrder.Other,
                NutrientsOrder.Vitamins,
                NutrientsOrder.Minerals -> Unit
            }
        }

        EnergyTextField(
            state = state.energy,
            autoCalculate = state.autoCalculateEnergy,
            onAutoCalculateToggle = { state.autoCalculateEnergy = it },
            modifier = (Modifier.padding(horizontalPadding).fillMaxWidth()).uiName("ProductForm.EnergyTextField.1", "com.maksimowiczm.foodyou.app.ui.food.product.EnergyTextField", uiInspectionSource, "ProductForm"),
        )

        order.forEach { field ->
            when (field) {
                NutrientsOrder.Proteins -> Unit
                NutrientsOrder.Carbohydrates -> Carbohydrates(state, horizontalPadding)
                NutrientsOrder.Fats -> Fats(state, horizontalPadding)
                NutrientsOrder.Other -> Other(state, horizontalPadding)
                NutrientsOrder.Vitamins -> Vitamins(state, horizontalPadding)
                NutrientsOrder.Minerals -> Minerals(state, horizontalPadding)
            }
        }
    }
}

@Composable
private fun General(state: ProductFormState, horizontalPadding: PaddingValues) {
    var showBarcodeScanner by rememberSaveable { mutableStateOf(false) }
    FullScreenCameraBarcodeScanner(
        visible = showBarcodeScanner,
        onBarcodeScan = {
            state.barcode.textFieldState.setTextAndPlaceCursorAtEnd(it)
            showBarcodeScanner = false
        },
        onClose = { showBarcodeScanner = false },
    )

    Text(
        text = stringResource(Res.string.headline_general),
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = (Modifier.padding(horizontalPadding).fillMaxWidth()).uiName("General.Text.1", "androidx.compose.material3.Text", uiInspectionSource, "General"),
    )

    state.name.TextField(
        label = stringResource(Res.string.product_name),
        modifier = (Modifier.padding(horizontalPadding).fillMaxWidth()).uiName("General.TextField.1", "com.maksimowiczm.foodyou.app.ui.food.product.TextField", uiInspectionSource, "General"),
        required = true,
        suffix = null,
    )

    state.brand.TextField(
        label = stringResource(Res.string.product_brand),
        modifier = (Modifier.padding(horizontalPadding).fillMaxWidth()).uiName("General.TextField.2", "com.maksimowiczm.foodyou.app.ui.food.product.TextField", uiInspectionSource, "General"),
    )

    BarcodeTextField(
        state = state.barcode,
        onBarcodeScanner = { showBarcodeScanner = true },
        modifier = (Modifier.padding(horizontalPadding).fillMaxWidth()).uiName("General.BarcodeTextField.1", "com.maksimowiczm.foodyou.app.ui.food.product.BarcodeTextField", uiInspectionSource, "General"),
    )

    state.note.TextField(
        label = stringResource(Res.string.headline_note),
        modifier = (Modifier.padding(horizontalPadding).fillMaxWidth()).uiName("General.TextField.3", "com.maksimowiczm.foodyou.app.ui.food.product.TextField", uiInspectionSource, "General"),
        supportingText = stringResource(Res.string.description_add_note),
    )

    SourcePicker(
        url = state.sourceUrl,
        type = state.sourceType,
        onTypeChange = { state.sourceType = it },
        modifier = (Modifier.padding(horizontalPadding).fillMaxWidth()).uiName("General.SourcePicker.1", "com.maksimowiczm.foodyou.app.ui.food.product.SourcePicker", uiInspectionSource, "General"),
    )

    Row(
        modifier =
            (Modifier.fillMaxWidth()
                .clickable { state.isLiquid = !state.isLiquid }
                .padding(vertical = 8.dp)
                .padding(horizontalPadding)).uiName("General.Row.1", "androidx.compose.foundation.layout.Row", uiInspectionSource, "General"),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = (Modifier.size(48.dp)).uiName("General.Box.1", "androidx.compose.foundation.layout.Box", uiInspectionSource, "General"),
            contentAlignment = Alignment.Center,
            content = { Checkbox(checked = state.isLiquid, onCheckedChange = null, modifier = Modifier.uiName("General.Checkbox.1", "androidx.compose.material3.Checkbox", uiInspectionSource, "General")) },
        )
        Column (modifier = Modifier.uiName("General.Column.1", "androidx.compose.foundation.layout.Column", uiInspectionSource, "General")) {
            Text(
                text = stringResource(Res.string.action_treat_as_liquid),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.uiName("General.Text.2", "androidx.compose.material3.Text", uiInspectionSource, "General")
            )
            Text(
                text = stringResource(Res.string.description_treat_as_liquid),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.uiName("General.Text.3", "androidx.compose.material3.Text", uiInspectionSource, "General")
            )
        }
    }

    Text(
        text = stringResource(Res.string.product_nutrition_basis),
        modifier = (Modifier.padding(horizontalPadding)).uiName("General.Text.4", "androidx.compose.material3.Text", uiInspectionSource, "General"),
        style = MaterialTheme.typography.labelLarge,
    )
    Text(
        text = stringResource(Res.string.description_product_nutrition_basis),
        modifier = (Modifier.padding(horizontalPadding)).uiName("General.Text.5", "androidx.compose.material3.Text", uiInspectionSource, "General"),
        style = MaterialTheme.typography.bodySmall,
    )
    MeasurementPicker(
        isLiquid = state.isLiquid,
        selected = state.measurement,
        onSelect = state::selectNutritionBasis,
        modifier = (Modifier.padding(8.dp).padding(horizontalPadding).fillMaxWidth()).uiName("General.MeasurementPicker.1", "com.maksimowiczm.foodyou.app.ui.food.product.MeasurementPicker", uiInspectionSource, "General"),
    )

    state.packageWeight.TextField(
        label = stringResource(Res.string.product_package_weight),
        modifier = (Modifier.padding(horizontalPadding).fillMaxWidth()).uiName("General.TextField.4", "com.maksimowiczm.foodyou.app.ui.food.product.TextField", uiInspectionSource, "General"),
        required = state.measurement is Measurement.Package,
        suffix =
            if (state.isLiquid) {
                stringResource(Res.string.unit_milliliter_short)
            } else {
                stringResource(Res.string.unit_gram_short)
            },
    )

    state.servingWeight.TextField(
        label = stringResource(Res.string.product_reference_serving),
        modifier = (Modifier.padding(horizontalPadding).fillMaxWidth()).uiName("General.TextField.5", "com.maksimowiczm.foodyou.app.ui.food.product.TextField", uiInspectionSource, "General"),
        required = state.measurement is Measurement.Serving,
        suffix =
            if (state.isLiquid) {
                stringResource(Res.string.unit_milliliter_short)
            } else {
                stringResource(Res.string.unit_gram_short)
            },
    )
}

@Composable
private fun Fats(state: ProductFormState, horizontalPadding: PaddingValues) {
    Text(
        text = stringResource(Res.string.nutriment_fats),
        modifier = (Modifier.padding(top = 8.dp).padding(horizontalPadding).fillMaxWidth()).uiName("app.ui.food.product.Fats.Text.1", "androidx.compose.material3.Text", uiInspectionSource, "Fats"),
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
    )

    state.saturatedFats.TextField(
        label = stringResource(Res.string.nutriment_saturated_fats),
        modifier = (Modifier.padding(horizontalPadding).fillMaxWidth()).uiName("app.ui.food.product.Fats.TextField.1", "com.maksimowiczm.foodyou.app.ui.food.product.TextField", uiInspectionSource, "Fats"),
    )

    state.transFats.TextField(
        label = stringResource(Res.string.nutriment_trans_fats),
        modifier = (Modifier.padding(horizontalPadding).fillMaxWidth()).uiName("app.ui.food.product.Fats.TextField.2", "com.maksimowiczm.foodyou.app.ui.food.product.TextField", uiInspectionSource, "Fats"),
    )

    state.monounsaturatedFats.TextField(
        label = stringResource(Res.string.nutriment_monounsaturated_fats),
        modifier = (Modifier.padding(horizontalPadding).fillMaxWidth()).uiName("app.ui.food.product.Fats.TextField.3", "com.maksimowiczm.foodyou.app.ui.food.product.TextField", uiInspectionSource, "Fats"),
    )

    state.polyunsaturatedFats.TextField(
        label = stringResource(Res.string.nutriment_polyunsaturated_fats),
        modifier = (Modifier.padding(horizontalPadding).fillMaxWidth()).uiName("app.ui.food.product.Fats.TextField.4", "com.maksimowiczm.foodyou.app.ui.food.product.TextField", uiInspectionSource, "Fats"),
    )

    state.omega3.TextField(
        label = stringResource(Res.string.nutriment_omega_3),
        modifier = (Modifier.padding(horizontalPadding).fillMaxWidth()).uiName("app.ui.food.product.Fats.TextField.5", "com.maksimowiczm.foodyou.app.ui.food.product.TextField", uiInspectionSource, "Fats"),
    )

    state.omega6.TextField(
        label = stringResource(Res.string.nutriment_omega_6),
        modifier = (Modifier.padding(horizontalPadding).fillMaxWidth()).uiName("app.ui.food.product.Fats.TextField.6", "com.maksimowiczm.foodyou.app.ui.food.product.TextField", uiInspectionSource, "Fats"),
    )
}

@Composable
private fun Carbohydrates(state: ProductFormState, horizontalPadding: PaddingValues) {
    Text(
        text = stringResource(Res.string.nutriment_carbohydrates),
        modifier = (Modifier.padding(top = 8.dp).padding(horizontalPadding).fillMaxWidth()).uiName("app.ui.food.product.Carbohydrates.Text.1", "androidx.compose.material3.Text", uiInspectionSource, "Carbohydrates"),
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
    )

    state.sugars.TextField(
        label = stringResource(Res.string.nutriment_sugars),
        modifier = (Modifier.padding(horizontalPadding).fillMaxWidth()).uiName("app.ui.food.product.Carbohydrates.TextField.1", "com.maksimowiczm.foodyou.app.ui.food.product.TextField", uiInspectionSource, "Carbohydrates"),
    )

    state.addedSugars.TextField(
        label = stringResource(Res.string.nutriment_added_sugars),
        modifier = (Modifier.padding(horizontalPadding).fillMaxWidth()).uiName("app.ui.food.product.Carbohydrates.TextField.2", "com.maksimowiczm.foodyou.app.ui.food.product.TextField", uiInspectionSource, "Carbohydrates"),
    )

    state.dietaryFiber.TextField(
        label = stringResource(Res.string.nutriment_fiber),
        modifier = (Modifier.padding(horizontalPadding).fillMaxWidth()).uiName("app.ui.food.product.Carbohydrates.TextField.3", "com.maksimowiczm.foodyou.app.ui.food.product.TextField", uiInspectionSource, "Carbohydrates"),
    )

    state.solubleFiber.TextField(
        label = stringResource(Res.string.nutriment_soluble_fiber),
        modifier = (Modifier.padding(horizontalPadding).fillMaxWidth()).uiName("app.ui.food.product.Carbohydrates.TextField.4", "com.maksimowiczm.foodyou.app.ui.food.product.TextField", uiInspectionSource, "Carbohydrates"),
    )

    state.insolubleFiber.TextField(
        label = stringResource(Res.string.nutriment_insoluble_fiber),
        modifier = (Modifier.padding(horizontalPadding).fillMaxWidth()).uiName("app.ui.food.product.Carbohydrates.TextField.5", "com.maksimowiczm.foodyou.app.ui.food.product.TextField", uiInspectionSource, "Carbohydrates"),
    )
}

@Composable
private fun Other(state: ProductFormState, horizontalPadding: PaddingValues) {
    Text(
        text = stringResource(Res.string.headline_other),
        modifier = (Modifier.padding(horizontalPadding).fillMaxWidth()).uiName("app.ui.food.product.Other.Text.1", "androidx.compose.material3.Text", uiInspectionSource, "Other"),
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
    )

    state.salt.TextField(
        label = stringResource(Res.string.nutriment_salt),
        modifier = (Modifier.padding(horizontalPadding).fillMaxWidth()).uiName("app.ui.food.product.Other.TextField.1", "com.maksimowiczm.foodyou.app.ui.food.product.TextField", uiInspectionSource, "Other"),
    )

    state.cholesterolMilli.TextField(
        label = stringResource(Res.string.nutriment_cholesterol),
        modifier = (Modifier.padding(horizontalPadding).fillMaxWidth()).uiName("app.ui.food.product.Other.TextField.2", "com.maksimowiczm.foodyou.app.ui.food.product.TextField", uiInspectionSource, "Other"),
        suffix = stringResource(Res.string.unit_milligram_short),
    )

    state.caffeineMilli.TextField(
        label = stringResource(Res.string.nutriment_caffeine),
        modifier = (Modifier.padding(horizontalPadding).fillMaxWidth()).uiName("app.ui.food.product.Other.TextField.3", "com.maksimowiczm.foodyou.app.ui.food.product.TextField", uiInspectionSource, "Other"),
        suffix = stringResource(Res.string.unit_milligram_short),
    )
}

@Composable
private fun Vitamins(state: ProductFormState, horizontalPadding: PaddingValues) {
    Text(
        text = stringResource(Res.string.headline_vitamins),
        modifier = (Modifier.padding(horizontalPadding).fillMaxWidth()).uiName("app.ui.food.product.Vitamins.Text.1", "androidx.compose.material3.Text", uiInspectionSource, "Vitamins"),
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
    )

    state.vitaminAMicro.TextField(
        label = stringResource(Res.string.vitamin_a),
        modifier = (Modifier.padding(horizontalPadding).fillMaxWidth()).uiName("app.ui.food.product.Vitamins.TextField.1", "com.maksimowiczm.foodyou.app.ui.food.product.TextField", uiInspectionSource, "Vitamins"),
        suffix = stringResource(Res.string.unit_microgram_short),
    )

    state.vitaminB1Milli.TextField(
        label = stringResource(Res.string.vitamin_b1),
        modifier = (Modifier.padding(horizontalPadding).fillMaxWidth()).uiName("app.ui.food.product.Vitamins.TextField.2", "com.maksimowiczm.foodyou.app.ui.food.product.TextField", uiInspectionSource, "Vitamins"),
        suffix = stringResource(Res.string.unit_milligram_short),
    )

    state.vitaminB2Milli.TextField(
        label = stringResource(Res.string.vitamin_b2),
        modifier = (Modifier.padding(horizontalPadding).fillMaxWidth()).uiName("app.ui.food.product.Vitamins.TextField.3", "com.maksimowiczm.foodyou.app.ui.food.product.TextField", uiInspectionSource, "Vitamins"),
        suffix = stringResource(Res.string.unit_milligram_short),
    )

    state.vitaminB3Milli.TextField(
        label = stringResource(Res.string.vitamin_b3),
        modifier = (Modifier.padding(horizontalPadding).fillMaxWidth()).uiName("app.ui.food.product.Vitamins.TextField.4", "com.maksimowiczm.foodyou.app.ui.food.product.TextField", uiInspectionSource, "Vitamins"),
        suffix = stringResource(Res.string.unit_milligram_short),
    )

    state.vitaminB5Milli.TextField(
        label = stringResource(Res.string.vitamin_b5),
        modifier = (Modifier.padding(horizontalPadding).fillMaxWidth()).uiName("app.ui.food.product.Vitamins.TextField.5", "com.maksimowiczm.foodyou.app.ui.food.product.TextField", uiInspectionSource, "Vitamins"),
        suffix = stringResource(Res.string.unit_milligram_short),
    )

    state.vitaminB6Milli.TextField(
        label = stringResource(Res.string.vitamin_b6),
        modifier = (Modifier.padding(horizontalPadding).fillMaxWidth()).uiName("app.ui.food.product.Vitamins.TextField.6", "com.maksimowiczm.foodyou.app.ui.food.product.TextField", uiInspectionSource, "Vitamins"),
        suffix = stringResource(Res.string.unit_milligram_short),
    )

    state.vitaminB7Micro.TextField(
        label = stringResource(Res.string.vitamin_b7),
        modifier = (Modifier.padding(horizontalPadding).fillMaxWidth()).uiName("app.ui.food.product.Vitamins.TextField.7", "com.maksimowiczm.foodyou.app.ui.food.product.TextField", uiInspectionSource, "Vitamins"),
        suffix = stringResource(Res.string.unit_microgram_short),
    )

    state.vitaminB9Micro.TextField(
        label = stringResource(Res.string.vitamin_b9),
        modifier = (Modifier.padding(horizontalPadding).fillMaxWidth()).uiName("app.ui.food.product.Vitamins.TextField.8", "com.maksimowiczm.foodyou.app.ui.food.product.TextField", uiInspectionSource, "Vitamins"),
        suffix = stringResource(Res.string.unit_microgram_short),
    )

    state.vitaminB12Micro.TextField(
        label = stringResource(Res.string.vitamin_b12),
        modifier = (Modifier.padding(horizontalPadding).fillMaxWidth()).uiName("app.ui.food.product.Vitamins.TextField.9", "com.maksimowiczm.foodyou.app.ui.food.product.TextField", uiInspectionSource, "Vitamins"),
        suffix = stringResource(Res.string.unit_microgram_short),
    )

    state.vitaminCMilli.TextField(
        label = stringResource(Res.string.vitamin_c),
        modifier = (Modifier.padding(horizontalPadding).fillMaxWidth()).uiName("app.ui.food.product.Vitamins.TextField.10", "com.maksimowiczm.foodyou.app.ui.food.product.TextField", uiInspectionSource, "Vitamins"),
        suffix = stringResource(Res.string.unit_milligram_short),
    )

    state.vitaminDMicro.TextField(
        label = stringResource(Res.string.vitamin_d),
        modifier = (Modifier.padding(horizontalPadding).fillMaxWidth()).uiName("app.ui.food.product.Vitamins.TextField.11", "com.maksimowiczm.foodyou.app.ui.food.product.TextField", uiInspectionSource, "Vitamins"),
        suffix = stringResource(Res.string.unit_microgram_short),
    )

    state.vitaminEMilli.TextField(
        label = stringResource(Res.string.vitamin_e),
        modifier = (Modifier.padding(horizontalPadding).fillMaxWidth()).uiName("app.ui.food.product.Vitamins.TextField.12", "com.maksimowiczm.foodyou.app.ui.food.product.TextField", uiInspectionSource, "Vitamins"),
        suffix = stringResource(Res.string.unit_milligram_short),
    )

    state.vitaminKMicro.TextField(
        label = stringResource(Res.string.vitamin_k),
        modifier = (Modifier.padding(horizontalPadding).fillMaxWidth()).uiName("app.ui.food.product.Vitamins.TextField.13", "com.maksimowiczm.foodyou.app.ui.food.product.TextField", uiInspectionSource, "Vitamins"),
        suffix = stringResource(Res.string.unit_microgram_short),
    )
}

@Composable
private fun Minerals(state: ProductFormState, horizontalPadding: PaddingValues) {
    Text(
        text = stringResource(Res.string.headline_minerals),
        modifier = (Modifier.padding(horizontalPadding).fillMaxWidth()).uiName("app.ui.food.product.Minerals.Text.1", "androidx.compose.material3.Text", uiInspectionSource, "Minerals"),
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
    )

    state.manganeseMilli.TextField(
        label = stringResource(Res.string.mineral_manganese),
        modifier = (Modifier.padding(horizontalPadding).fillMaxWidth()).uiName("app.ui.food.product.Minerals.TextField.1", "com.maksimowiczm.foodyou.app.ui.food.product.TextField", uiInspectionSource, "Minerals"),
        suffix = stringResource(Res.string.unit_milligram_short),
    )

    state.magnesiumMilli.TextField(
        label = stringResource(Res.string.mineral_magnesium),
        modifier = (Modifier.padding(horizontalPadding).fillMaxWidth()).uiName("app.ui.food.product.Minerals.TextField.2", "com.maksimowiczm.foodyou.app.ui.food.product.TextField", uiInspectionSource, "Minerals"),
        suffix = stringResource(Res.string.unit_milligram_short),
    )

    state.potassiumMilli.TextField(
        label = stringResource(Res.string.mineral_potassium),
        modifier = (Modifier.padding(horizontalPadding).fillMaxWidth()).uiName("app.ui.food.product.Minerals.TextField.3", "com.maksimowiczm.foodyou.app.ui.food.product.TextField", uiInspectionSource, "Minerals"),
        suffix = stringResource(Res.string.unit_milligram_short),
    )

    state.calciumMilli.TextField(
        label = stringResource(Res.string.mineral_calcium),
        modifier = (Modifier.padding(horizontalPadding).fillMaxWidth()).uiName("app.ui.food.product.Minerals.TextField.4", "com.maksimowiczm.foodyou.app.ui.food.product.TextField", uiInspectionSource, "Minerals"),
        suffix = stringResource(Res.string.unit_milligram_short),
    )

    state.copperMilli.TextField(
        label = stringResource(Res.string.mineral_copper),
        modifier = (Modifier.padding(horizontalPadding).fillMaxWidth()).uiName("app.ui.food.product.Minerals.TextField.5", "com.maksimowiczm.foodyou.app.ui.food.product.TextField", uiInspectionSource, "Minerals"),
        suffix = stringResource(Res.string.unit_milligram_short),
    )

    state.zincMilli.TextField(
        label = stringResource(Res.string.mineral_zinc),
        modifier = (Modifier.padding(horizontalPadding).fillMaxWidth()).uiName("app.ui.food.product.Minerals.TextField.6", "com.maksimowiczm.foodyou.app.ui.food.product.TextField", uiInspectionSource, "Minerals"),
        suffix = stringResource(Res.string.unit_milligram_short),
    )

    state.sodiumMilli.TextField(
        label = stringResource(Res.string.mineral_sodium),
        modifier = (Modifier.padding(horizontalPadding).fillMaxWidth()).uiName("app.ui.food.product.Minerals.TextField.7", "com.maksimowiczm.foodyou.app.ui.food.product.TextField", uiInspectionSource, "Minerals"),
        suffix = stringResource(Res.string.unit_milligram_short),
    )

    state.ironMilli.TextField(
        label = stringResource(Res.string.mineral_iron),
        modifier = (Modifier.padding(horizontalPadding).fillMaxWidth()).uiName("app.ui.food.product.Minerals.TextField.8", "com.maksimowiczm.foodyou.app.ui.food.product.TextField", uiInspectionSource, "Minerals"),
        suffix = stringResource(Res.string.unit_milligram_short),
    )

    state.phosphorusMilli.TextField(
        label = stringResource(Res.string.mineral_phosphorus),
        modifier = (Modifier.padding(horizontalPadding).fillMaxWidth()).uiName("app.ui.food.product.Minerals.TextField.9", "com.maksimowiczm.foodyou.app.ui.food.product.TextField", uiInspectionSource, "Minerals"),
        suffix = stringResource(Res.string.unit_milligram_short),
    )

    state.seleniumMicro.TextField(
        label = stringResource(Res.string.mineral_selenium),
        modifier = (Modifier.padding(horizontalPadding).fillMaxWidth()).uiName("app.ui.food.product.Minerals.TextField.10", "com.maksimowiczm.foodyou.app.ui.food.product.TextField", uiInspectionSource, "Minerals"),
        suffix = stringResource(Res.string.unit_microgram_short),
    )

    state.iodineMicro.TextField(
        label = stringResource(Res.string.mineral_iodine),
        modifier = (Modifier.padding(horizontalPadding).fillMaxWidth()).uiName("app.ui.food.product.Minerals.TextField.11", "com.maksimowiczm.foodyou.app.ui.food.product.TextField", uiInspectionSource, "Minerals"),
        suffix = stringResource(Res.string.unit_microgram_short),
    )

    state.chromiumMicro.TextField(
        label = stringResource(Res.string.mineral_chromium),
        modifier = (Modifier.padding(horizontalPadding).fillMaxWidth()).uiName("app.ui.food.product.Minerals.TextField.12", "com.maksimowiczm.foodyou.app.ui.food.product.TextField", uiInspectionSource, "Minerals"),
        suffix = stringResource(Res.string.unit_microgram_short),
        imeAction = ImeAction.Done,
    )
}

@Composable
private inline fun <reified T> FormField<T, ProductFormFieldError>.TextField(
    label: String,
    modifier: Modifier = Modifier,
    required: Boolean = false,
    imeAction: ImeAction = ImeAction.Next,
    suffix: String? = stringResource(Res.string.unit_gram_short),
) {
    OutlinedTextField(
        state = textFieldState,
        modifier = (modifier).uiName("app.ui.food.product.TextField.OutlinedTextField.1", "androidx.compose.material3.OutlinedTextField", uiInspectionSource, "TextField"),
        label = { Text(label, modifier = Modifier.uiName("app.ui.food.product.TextField.Text.2", "androidx.compose.material3.Text", uiInspectionSource, "TextField")) },
        supportingText = {
            val error = this.error
            if (error != null) {
                Text(error.stringResource(), modifier = Modifier.uiName("app.ui.food.product.TextField.Text.3", "androidx.compose.material3.Text", uiInspectionSource, "TextField"))
            } else if (required) {
                Text(stringResource(Res.string.neutral_required), modifier = Modifier.uiName("TextField.Text.4", "androidx.compose.material3.Text", uiInspectionSource, "TextField"))
            }
        },
        suffix = suffix?.let { { Text(suffix, modifier = Modifier.uiName("TextField.Text.5", "androidx.compose.material3.Text", uiInspectionSource, "TextField")) } },
        isError = error != null,
        keyboardOptions =
            if (T::class == Float::class) {
                KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = imeAction)
            } else {
                KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = imeAction)
            },
    )
}

@Composable
private inline fun <reified T> FormField<T, Nothing>.TextField(
    label: String,
    modifier: Modifier = Modifier,
    imeAction: ImeAction = ImeAction.Next,
    supportingText: String? = null,
) {
    OutlinedTextField(
        state = textFieldState,
        modifier = (modifier).uiName("TextField.OutlinedTextField.2", "androidx.compose.material3.OutlinedTextField", uiInspectionSource, "TextField"),
        label = { Text(label, modifier = Modifier.uiName("TextField.Text.6", "androidx.compose.material3.Text", uiInspectionSource, "TextField")) },
        supportingText = supportingText?.let { { Text(it, modifier = Modifier.uiName("app.ui.food.product.TextField.Text.7", "androidx.compose.material3.Text", uiInspectionSource, "TextField")) } },
        keyboardOptions =
            if (T::class == Float::class) {
                KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = imeAction)
            } else {
                KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = imeAction)
            },
    )
}

@Composable
private fun BarcodeTextField(
    state: FormField<String?, Nothing>,
    onBarcodeScanner: () -> Unit,
    modifier: Modifier = Modifier,
    imeAction: ImeAction = ImeAction.Next,
) {
    OutlinedTextField(
        state = state.textFieldState,
        modifier = (modifier).uiName("BarcodeTextField.OutlinedTextField.1", "androidx.compose.material3.OutlinedTextField", uiInspectionSource, "BarcodeTextField"),
        label = { Text(stringResource(Res.string.product_barcode), modifier = Modifier.uiName("BarcodeTextField.Text.1", "androidx.compose.material3.Text", uiInspectionSource, "BarcodeTextField")) },
        keyboardOptions =
            KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = imeAction),
        trailingIcon = {
            FilledTonalIconButton(onClick = onBarcodeScanner, modifier = Modifier.uiName("BarcodeTextField.FilledTonalIconButton.1", "androidx.compose.material3.FilledTonalIconButton", uiInspectionSource, "BarcodeTextField")) {
                Icon(
                    painter = painterResource(Res.drawable.ic_barcode_scanner),
                    contentDescription = null, modifier = Modifier.uiName("BarcodeTextField.Icon.1", "androidx.compose.material3.Icon", uiInspectionSource, "BarcodeTextField")
                )
            }
        },
    )
}

@Composable
private fun MeasurementPicker(
    isLiquid: Boolean,
    selected: Measurement,
    onSelect: (Measurement) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by rememberSaveable { mutableStateOf(false) }

    val latestOnSelect by rememberUpdatedState(onSelect)
    LaunchedEffect(isLiquid, selected) {
        // If changes and gram or milliliter is selected, switch to proper
        if (selected is Measurement.Gram && isLiquid) {
            latestOnSelect(Measurement.Milliliter(selected.value))
        } else if (selected is Measurement.Milliliter && !isLiquid) {
            latestOnSelect(Measurement.Gram(selected.value))
        }
    }

    val possibleValues =
        remember(isLiquid) {
            listOf(
                if (isLiquid) Measurement.Milliliter(100.0) else Measurement.Gram(100.0),
                Measurement.Serving(1.0),
                Measurement.Package(1.0),
            )
        }

    Row(
        modifier = (modifier).uiName("app.ui.food.product.MeasurementPicker.Row.1", "androidx.compose.foundation.layout.Row", uiInspectionSource, "MeasurementPicker"),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(Res.string.headline_values_per),
            style = MaterialTheme.typography.bodyLarge, modifier = Modifier.uiName("app.ui.food.product.MeasurementPicker.Text.1", "androidx.compose.material3.Text", uiInspectionSource, "MeasurementPicker")
        )

        SplitButtonLayout(
            leadingButton = {
                SplitButtonDefaults.LeadingButton(onClick = { expanded = !expanded }, modifier = Modifier.uiName("MeasurementPicker.LeadingButton.1", "androidx.compose.material3.SplitButtonDefaults.LeadingButton", uiInspectionSource, "MeasurementPicker")) {
                    Text(text = selected.stringResource(), modifier = Modifier.uiName("app.ui.food.product.MeasurementPicker.Text.2", "androidx.compose.material3.Text", uiInspectionSource, "MeasurementPicker"))
                }
            },
            trailingButton = {
                SplitButtonDefaults.TrailingButton(
                    checked = expanded,
                    onCheckedChange = { expanded = it }, modifier = Modifier.uiName("MeasurementPicker.TrailingButton.1", "androidx.compose.material3.SplitButtonDefaults.TrailingButton", uiInspectionSource, "MeasurementPicker")
                ) {
                    val rotation by animateFloatAsState(targetValue = if (expanded) 180f else 0f)

                    Icon(
                        Icons.Filled.KeyboardArrowDown,
                        modifier =
                            (Modifier.size(SplitButtonDefaults.TrailingIconSize).graphicsLayer {
                                this.rotationZ = rotation
                            }).uiName("app.ui.food.product.MeasurementPicker.Icon.1", "androidx.compose.material3.Icon", uiInspectionSource, "MeasurementPicker"),
                        contentDescription = null,
                    )
                }

                // Had to put it here because it must be aligned to the trailing button
                UiSourceScope(uiInspectionSource.element("MeasurementPicker.DropdownMenu.1", "androidx.compose.material3.DropdownMenu", "MeasurementPicker")) { DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }, modifier = Modifier.uiName("MeasurementPicker.DropdownMenu.1.root", "androidx.compose.material3.DropdownMenu", uiInspectionSource, "MeasurementPicker")) {
                    possibleValues.forEach {
                        DropdownMenuItem(
                            text = { Text(it.stringResource(), modifier = Modifier.uiName("MeasurementPicker.Text.3", "androidx.compose.material3.Text", uiInspectionSource, "MeasurementPicker")) },
                            onClick = {
                                expanded = false
                                onSelect(it)
                            }, modifier = Modifier.uiName("MeasurementPicker.DropdownMenuItem.1", "androidx.compose.material3.DropdownMenuItem", uiInspectionSource, "MeasurementPicker")
                        )
                    }
                } }
            }, modifier = Modifier.uiName("MeasurementPicker.SplitButtonLayout.1", "androidx.compose.material3.SplitButtonLayout", uiInspectionSource, "MeasurementPicker")
        )
    }
}

@Composable
private fun EnergyTextField(
    state: FormField<Float?, ProductFormFieldError>,
    autoCalculate: Boolean,
    onAutoCalculateToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val energyFormatter = LocalEnergyFormatter.current

    Column(modifier = (modifier).uiName("EnergyTextField.Column.1", "androidx.compose.foundation.layout.Column", uiInspectionSource, "EnergyTextField"), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            state = state.textFieldState,
            modifier = (Modifier.fillMaxWidth()).uiName("EnergyTextField.OutlinedTextField.1", "androidx.compose.material3.OutlinedTextField", uiInspectionSource, "EnergyTextField"),
            label = { Text(stringResource(Res.string.unit_energy), modifier = Modifier.uiName("EnergyTextField.Text.1", "androidx.compose.material3.Text", uiInspectionSource, "EnergyTextField")) },
            supportingText = {
                val error = state.error
                if (error != null) {
                    Text(error.stringResource(), modifier = Modifier.uiName("EnergyTextField.Text.2", "androidx.compose.material3.Text", uiInspectionSource, "EnergyTextField"))
                } else {
                    Text(stringResource(Res.string.neutral_required), modifier = Modifier.uiName("EnergyTextField.Text.3", "androidx.compose.material3.Text", uiInspectionSource, "EnergyTextField"))
                }
            },
            trailingIcon = {
                UiSourceScope(uiInspectionSource.element("EnergyTextField.TooltipBox.1", "androidx.compose.material3.TooltipBox", "EnergyTextField")) { TooltipBox(
                    positionProvider =
                        TooltipDefaults.rememberTooltipPositionProvider(
                            TooltipAnchorPosition.Above
                        ),
                    tooltip = {
                        PlainTooltip(modifier = Modifier.uiName("EnergyTextField.PlainTooltip.1", "androidx.compose.material3.PlainTooltip", uiInspectionSource, "EnergyTextField")) {
                            Text(
                                text =
                                    if (autoCalculate) {
                                        stringResource(Res.string.headline_auto_calculate_energy)
                                    } else {
                                        stringResource(Res.string.headline_manual_energy_input)
                                    },
                                style = MaterialTheme.typography.bodySmall, modifier = Modifier.uiName("EnergyTextField.Text.4", "androidx.compose.material3.Text", uiInspectionSource, "EnergyTextField")
                            )
                        }
                    },
                    state = rememberTooltipState(isPersistent = true), modifier = Modifier.uiName("EnergyTextField.TooltipBox.1.root", "androidx.compose.material3.TooltipBox", uiInspectionSource, "EnergyTextField")
                ) {
                    IconButton(onClick = { onAutoCalculateToggle(!autoCalculate) }, modifier = Modifier.uiName("EnergyTextField.IconButton.1", "androidx.compose.material3.IconButton", uiInspectionSource, "EnergyTextField")) {
                        if (autoCalculate) {
                            Icon(
                                imageVector = Icons.Outlined.Calculate,
                                contentDescription = stringResource(Res.string.headline_auto_calculate_energy), modifier = Modifier.uiName("EnergyTextField.Icon.1", "androidx.compose.material3.Icon", uiInspectionSource, "EnergyTextField")
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Outlined.Keyboard,
                                contentDescription = stringResource(Res.string.headline_manual_energy_input), modifier = Modifier.uiName("EnergyTextField.Icon.2", "androidx.compose.material3.Icon", uiInspectionSource, "EnergyTextField")
                            )
                        }
                    }
                } }
            },
            isError = state.error != null,
            keyboardOptions =
                KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done),
            suffix = { Text(energyFormatter.suffix(), modifier = Modifier.uiName("EnergyTextField.Text.5", "androidx.compose.material3.Text", uiInspectionSource, "EnergyTextField")) },
        )
        Text(
            text = stringResource(Res.string.description_calories_are_calculated),
            style = MaterialTheme.typography.bodySmall, modifier = Modifier.uiName("EnergyTextField.Text.6", "androidx.compose.material3.Text", uiInspectionSource, "EnergyTextField")
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
            style = MaterialTheme.typography.bodySmall, modifier = Modifier.uiName("EnergyTextField.Text.7", "androidx.compose.material3.Text", uiInspectionSource, "EnergyTextField")
        )
    }
}

@Composable
private fun SourcePicker(
    url: FormField<String?, Nothing>,
    type: ProductSource.Type,
    onTypeChange: (ProductSource.Type) -> Unit,
    modifier: Modifier = Modifier,
    imeAction: ImeAction = ImeAction.Next,
) {
    var expanded by rememberSaveable { mutableStateOf(false) }

    val dropdownMenu =
        @Composable {
            UiSourceScope(uiInspectionSource.element("SourcePicker.DropdownMenu.1", "androidx.compose.material3.DropdownMenu", "SourcePicker")) { DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }, modifier = Modifier.uiName("SourcePicker.DropdownMenu.1.root", "androidx.compose.material3.DropdownMenu", uiInspectionSource, "SourcePicker")) {
                ProductSource.Type.entries.forEach {
                    DropdownMenuItem(
                        leadingIcon = { it.Icon( modifier = Modifier.uiName("SourcePicker.Icon.1", "androidx.compose.material3.Icon", uiInspectionSource, "SourcePicker")) },
                        text = { Text(it.stringResource(), modifier = Modifier.uiName("SourcePicker.Text.1", "androidx.compose.material3.Text", uiInspectionSource, "SourcePicker")) },
                        onClick = {
                            expanded = false
                            onTypeChange(it)
                        }, modifier = Modifier.uiName("SourcePicker.DropdownMenuItem.1", "androidx.compose.material3.DropdownMenuItem", uiInspectionSource, "SourcePicker")
                    )
                }
            } }
        }

    OutlinedTextField(
        state = url.textFieldState,
        modifier = (modifier).uiName("SourcePicker.OutlinedTextField.1", "androidx.compose.material3.OutlinedTextField", uiInspectionSource, "SourcePicker"),
        label = { Text(stringResource(Res.string.headline_source), modifier = Modifier.uiName("SourcePicker.Text.2", "androidx.compose.material3.Text", uiInspectionSource, "SourcePicker")) },
        supportingText = { Text(stringResource(Res.string.description_food_source), modifier = Modifier.uiName("SourcePicker.Text.3", "androidx.compose.material3.Text", uiInspectionSource, "SourcePicker")) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = imeAction),
        trailingIcon = {
            dropdownMenu()
            FilledTonalIconButton(onClick = { expanded = true }, modifier = Modifier.uiName("SourcePicker.FilledTonalIconButton.1", "androidx.compose.material3.FilledTonalIconButton", uiInspectionSource, "SourcePicker")) { type.Icon( modifier = Modifier.uiName("SourcePicker.Icon.2", "androidx.compose.material3.Icon", uiInspectionSource, "SourcePicker")) }
        },
    )
}


private val uiInspectionSource = UiSourceFile(
    "com.maksimowiczm.foodyou.app.ui.food.product",
    "app/src/commonMain/kotlin/com/maksimowiczm/foodyou/app/ui/food/product/ProductForm.kt",
)
