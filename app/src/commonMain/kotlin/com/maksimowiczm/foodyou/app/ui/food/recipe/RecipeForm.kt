package com.maksimowiczm.foodyou.app.ui.food.recipe


import com.maksimowiczm.foodyou.app.ui.developer.inspection.UiSourceFile
import com.maksimowiczm.foodyou.app.ui.developer.inspection.uiName
import com.maksimowiczm.foodyou.app.ui.developer.inspection.UiSourceScope

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.maksimowiczm.foodyou.app.ui.common.component.FoodErrorListItem
import com.maksimowiczm.foodyou.app.ui.common.component.FoodListItem
import com.maksimowiczm.foodyou.app.ui.common.component.FoodListItemSkeleton
import com.maksimowiczm.foodyou.app.ui.common.form.FormField
import com.maksimowiczm.foodyou.app.ui.common.utility.LocalEnergyFormatter
import com.maksimowiczm.foodyou.app.ui.common.utility.stringResource
import com.maksimowiczm.foodyou.app.ui.common.utility.stringResourceWithWeight
import com.maksimowiczm.foodyou.common.compose.extension.add
import com.maksimowiczm.foodyou.common.compose.utility.formatClipZeros
import com.maksimowiczm.foodyou.food.domain.entity.NutritionalItemId
import com.maksimowiczm.foodyou.food.domain.usecase.ObserveNutritionalItemUseCase
import com.valentinilk.shimmer.Shimmer
import com.valentinilk.shimmer.ShimmerBounds
import com.valentinilk.shimmer.rememberShimmer
import foodyou.app.generated.resources.*
import kotlin.math.roundToInt
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

@Composable
internal fun RecipeForm(
    state: RecipeFormState,
    onAddIngredient: () -> Unit,
    onEditIngredient: (index: Int) -> Unit,
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

    var ingredientToRemoveIndex by rememberSaveable { mutableStateOf<Int?>(null) }
    LaunchedEffect(state.ingredients) { ingredientToRemoveIndex = null }
    when (val index = ingredientToRemoveIndex) {
        null -> Unit
        else ->
            UiSourceScope(uiInspectionSource.element("RecipeForm.AlertDialog.1", "androidx.compose.material3.AlertDialog", "RecipeForm")) { AlertDialog(
                onDismissRequest = { ingredientToRemoveIndex = null },
                confirmButton = {
                    TextButton(
                        onClick = {
                            state.removeIngredient(state.ingredients[index])
                            ingredientToRemoveIndex = null
                        }, modifier = Modifier.uiName("RecipeForm.TextButton.1", "androidx.compose.material3.TextButton", uiInspectionSource, "RecipeForm")
                    ) {
                        Text(stringResource(Res.string.action_delete), modifier = Modifier.uiName("RecipeForm.Text.1", "androidx.compose.material3.Text", uiInspectionSource, "RecipeForm"))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { ingredientToRemoveIndex = null }, modifier = Modifier.uiName("RecipeForm.TextButton.2", "androidx.compose.material3.TextButton", uiInspectionSource, "RecipeForm")) {
                        Text(stringResource(Res.string.action_cancel), modifier = Modifier.uiName("RecipeForm.Text.2", "androidx.compose.material3.Text", uiInspectionSource, "RecipeForm"))
                    }
                },
                title = { Text(stringResource(Res.string.action_delete_ingredient), modifier = Modifier.uiName("RecipeForm.Text.3", "androidx.compose.material3.Text", uiInspectionSource, "RecipeForm")) },
                text = { Text(stringResource(Res.string.description_delete_ingredient), modifier = Modifier.uiName("RecipeForm.Text.4", "androidx.compose.material3.Text", uiInspectionSource, "RecipeForm")) }, modifier = Modifier.uiName("RecipeForm.AlertDialog.1.root", "androidx.compose.material3.AlertDialog", uiInspectionSource, "RecipeForm")
            ) }
    }

    Column(modifier = (modifier.padding(verticalPadding)).uiName("RecipeForm.Column.1", "androidx.compose.foundation.layout.Column", uiInspectionSource, "RecipeForm")) {
        Column(
            modifier = (Modifier.padding(vertical = 8.dp)).uiName("RecipeForm.Column.2", "androidx.compose.foundation.layout.Column", uiInspectionSource, "RecipeForm"),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = stringResource(Res.string.headline_general),
                modifier = (Modifier.padding(horizontalPadding)).uiName("RecipeForm.Text.5", "androidx.compose.material3.Text", uiInspectionSource, "RecipeForm"),
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.labelLarge,
            )

            state.name.TextField(
                label = stringResource(Res.string.recipe_name),
                modifier = (Modifier.fillMaxWidth().padding(horizontalPadding)).uiName("RecipeForm.TextField.1", "com.maksimowiczm.foodyou.app.ui.food.recipe.TextField", uiInspectionSource, "RecipeForm"),
                supportingText = stringResource(Res.string.neutral_required),
            )

            state.servings.TextField(
                label = stringResource(Res.string.recipe_servings),
                modifier = (Modifier.fillMaxWidth().padding(horizontalPadding)).uiName("RecipeForm.TextField.2", "com.maksimowiczm.foodyou.app.ui.food.recipe.TextField", uiInspectionSource, "RecipeForm"),
                imeAction = ImeAction.Next,
                supportingText = stringResource(Res.string.description_recipe_servings),
            )

            state.note.TextField(
                label = stringResource(Res.string.headline_note),
                modifier = (Modifier.fillMaxWidth().padding(horizontalPadding)).uiName("RecipeForm.TextField.3", "com.maksimowiczm.foodyou.app.ui.food.recipe.TextField", uiInspectionSource, "RecipeForm"),
                imeAction = ImeAction.Done,
                supportingText = stringResource(Res.string.description_add_note),
            )

                    com.maksimowiczm.foodyou.app.ui.food.component.MetricUnitSelector(
                isLiquid = state.isLiquid, onSelect = { state.isLiquid = it }, modifier = (Modifier.padding(horizontalPadding)).uiName("RecipeForm.MetricUnitSelector.phone1", "com.maksimowiczm.foodyou.app.ui.food.component.MetricUnitSelector", uiInspectionSource, "RecipeForm"))

        }

        HorizontalDivider(modifier = (Modifier.padding(bottom = 8.dp)).uiName("RecipeForm.HorizontalDivider.1", "androidx.compose.material3.HorizontalDivider", uiInspectionSource, "RecipeForm"))

        Text(
            text = stringResource(Res.string.headline_ingredients),
            modifier = (Modifier.padding(horizontalPadding).padding(bottom = 8.dp)).uiName("RecipeForm.Text.8", "androidx.compose.material3.Text", uiInspectionSource, "RecipeForm"),
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.labelLarge,
        )

        AddIngredientButton(
            onAddIngredient = onAddIngredient,
            contentPadding = horizontalPadding,
            modifier = (Modifier.fillMaxWidth()).uiName("RecipeForm.AddIngredientButton.1", "com.maksimowiczm.foodyou.app.ui.food.recipe.AddIngredientButton", uiInspectionSource, "RecipeForm"),
        )

        val shimmer = rememberShimmer(ShimmerBounds.View)
        state.ingredients.forEachIndexed { i, ingredient ->
            IngredientListItem(
                ingredient = ingredient,
                onEdit = { onEditIngredient(i) },
                onDelete = { ingredientToRemoveIndex = i },
                contentPadding = horizontalPadding.add(vertical = 12.dp),
                shimmer = shimmer, modifier = Modifier.uiName("RecipeForm.IngredientListItem.1", "com.maksimowiczm.foodyou.app.ui.food.recipe.IngredientListItem", uiInspectionSource, "RecipeForm")
            )
        }
    }
}

@Composable
private fun IngredientListItem(
    ingredient: MinimalIngredient,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    contentPadding: PaddingValues,
    shimmer: Shimmer,
    modifier: Modifier = Modifier,
) {
    val observeFoodUseCase: ObserveNutritionalItemUseCase = koinInject()

    val food = observeFoodUseCase.observe(ingredient.foodId).collectAsStateWithLifecycle(null).value

    if (food == null) {
        return FoodErrorListItem(
            headline = stringResource(Res.string.recipe_ingredient_unavailable),
            errorMessage = stringResource(Res.string.error_food_is_missing_required_fields),
            modifier = (modifier).uiName("IngredientListItem.FoodErrorListItem.1", "com.maksimowiczm.foodyou.app.ui.common.component.FoodErrorListItem", uiInspectionSource, "IngredientListItem"),
            trailingContent = {
                IconButton(onDelete, modifier = Modifier.uiName("IngredientListItem.IconButton.1", "androidx.compose.material3.IconButton", uiInspectionSource, "IngredientListItem")) {
                    Icon(Icons.Outlined.Delete, contentDescription = stringResource(Res.string.action_delete), modifier = Modifier.uiName("IngredientListItem.Icon.1", "androidx.compose.material3.Icon", uiInspectionSource, "IngredientListItem"))
                }
            },
        )
    }

    val measurementFacts = food.nutritionFor(ingredient.measurement)
    if (measurementFacts == null) {
        return FoodErrorListItem(
            headline = food.headline,
            errorMessage = stringResource(Res.string.error_measurement_error),
            modifier = (modifier).uiName("IngredientListItem.FoodErrorListItem.2", "com.maksimowiczm.foodyou.app.ui.common.component.FoodErrorListItem", uiInspectionSource, "IngredientListItem"),
            onClick = onEdit,
            trailingContent = {
                Row (modifier = Modifier.uiName("IngredientListItem.Row.1", "androidx.compose.foundation.layout.Row", uiInspectionSource, "IngredientListItem")) {
                    IconButton(onEdit, modifier = Modifier.uiName("IngredientListItem.IconButton.2", "androidx.compose.material3.IconButton", uiInspectionSource, "IngredientListItem")) {
                        Icon(imageVector = Icons.Outlined.Edit, contentDescription = stringResource(Res.string.action_edit) + ": " + food.headline, modifier = Modifier.uiName("IngredientListItem.Icon.2", "androidx.compose.material3.Icon", uiInspectionSource, "IngredientListItem"))
                    }
                    IconButton(onDelete, modifier = Modifier.uiName("IngredientListItem.IconButton.3", "androidx.compose.material3.IconButton", uiInspectionSource, "IngredientListItem")) {
                        Icon(imageVector = Icons.Outlined.Delete, contentDescription = stringResource(Res.string.action_delete) + ": " + food.headline, modifier = Modifier.uiName("IngredientListItem.Icon.3", "androidx.compose.material3.Icon", uiInspectionSource, "IngredientListItem"))
                    }
                }
            },
        )
    }

    val proteins = measurementFacts.proteins.value
    val carbohydrates = measurementFacts.carbohydrates.value
    val fats = measurementFacts.fats.value
    val energy = measurementFacts.energy.value
    val measurementString =
        ingredient.measurement.stringResourceWithWeight(
            totalWeight = food.totalWeight,
            servingWeight = food.servingWeight,
            isLiquid = food.isLiquid,
        ) ?: ingredient.measurement.stringResource()

    if (
        proteins == null ||
            carbohydrates == null ||
            fats == null ||
            energy == null
    ) {
        return FoodErrorListItem(
            headline = food.headline,
            modifier = (modifier).uiName("IngredientListItem.FoodErrorListItem.3", "com.maksimowiczm.foodyou.app.ui.common.component.FoodErrorListItem", uiInspectionSource, "IngredientListItem"),
            onClick = onEdit,
            errorMessage = stringResource(Res.string.error_food_is_missing_required_fields),
            trailingContent = {
                Row (modifier = Modifier.uiName("IngredientListItem.Row.2", "androidx.compose.foundation.layout.Row", uiInspectionSource, "IngredientListItem")) {
                    IconButton(onEdit, modifier = Modifier.uiName("IngredientListItem.IconButton.4", "androidx.compose.material3.IconButton", uiInspectionSource, "IngredientListItem")) {
                        Icon(imageVector = Icons.Outlined.Edit, contentDescription = stringResource(Res.string.action_edit) + ": " + food.headline, modifier = Modifier.uiName("IngredientListItem.Icon.4", "androidx.compose.material3.Icon", uiInspectionSource, "IngredientListItem"))
                    }
                    IconButton(onDelete, modifier = Modifier.uiName("IngredientListItem.IconButton.5", "androidx.compose.material3.IconButton", uiInspectionSource, "IngredientListItem")) {
                        Icon(imageVector = Icons.Outlined.Delete, contentDescription = stringResource(Res.string.action_delete) + ": " + food.headline, modifier = Modifier.uiName("IngredientListItem.Icon.5", "androidx.compose.material3.Icon", uiInspectionSource, "IngredientListItem"))
                    }
                }
            },
        )
    }

    val g = stringResource(Res.string.unit_gram_short)

    FoodListItem(
        name = { Text(text = food.headline, modifier = Modifier.uiName("IngredientListItem.Text.1", "androidx.compose.material3.Text", uiInspectionSource, "IngredientListItem")) },
        proteins = {
            val text = proteins.formatClipZeros()
            Text("$text $g", modifier = Modifier.uiName("IngredientListItem.Text.2", "androidx.compose.material3.Text", uiInspectionSource, "IngredientListItem"))
        },
        carbohydrates = {
            val text = carbohydrates.formatClipZeros()
            Text("$text $g", modifier = Modifier.uiName("IngredientListItem.Text.3", "androidx.compose.material3.Text", uiInspectionSource, "IngredientListItem"))
        },
        fats = {
            val text = fats.formatClipZeros()
            Text("$text $g", modifier = Modifier.uiName("IngredientListItem.Text.4", "androidx.compose.material3.Text", uiInspectionSource, "IngredientListItem"))
        },
        calories = { Text(LocalEnergyFormatter.current.formatEnergy(energy.roundToInt()), modifier = Modifier.uiName("IngredientListItem.Text.5", "androidx.compose.material3.Text", uiInspectionSource, "IngredientListItem")) },
        measurement = { Text(measurementString, modifier = Modifier.uiName("IngredientListItem.Text.6", "androidx.compose.material3.Text", uiInspectionSource, "IngredientListItem")) },
        modifier = (modifier).uiName("IngredientListItem.FoodListItem.1", "com.maksimowiczm.foodyou.app.ui.common.component.FoodListItem", uiInspectionSource, "IngredientListItem"),
        contentPadding = contentPadding,
        trailingContent = {
            Row (modifier = Modifier.uiName("IngredientListItem.Row.3", "androidx.compose.foundation.layout.Row", uiInspectionSource, "IngredientListItem")) {
                IconButton(onEdit, modifier = Modifier.uiName("IngredientListItem.IconButton.6", "androidx.compose.material3.IconButton", uiInspectionSource, "IngredientListItem")) {
                    Icon(imageVector = Icons.Outlined.Edit, contentDescription = stringResource(Res.string.action_edit) + ": " + food.headline, modifier = Modifier.uiName("IngredientListItem.Icon.6", "androidx.compose.material3.Icon", uiInspectionSource, "IngredientListItem"))
                }
                IconButton(onDelete, modifier = Modifier.uiName("IngredientListItem.IconButton.7", "androidx.compose.material3.IconButton", uiInspectionSource, "IngredientListItem")) {
                    Icon(imageVector = Icons.Outlined.Delete, contentDescription = stringResource(Res.string.action_delete) + ": " + food.headline, modifier = Modifier.uiName("IngredientListItem.Icon.7", "androidx.compose.material3.Icon", uiInspectionSource, "IngredientListItem"))
                }
            }
        },
        isRecipe = ingredient.foodId is NutritionalItemId.Recipe,
    )
}

@Composable
private inline fun <reified T> FormField<T, RecipeFormFieldError>.TextField(
    label: String,
    modifier: Modifier = Modifier,
    imeAction: ImeAction = ImeAction.Next,
    supportingText: String? = null,
) {
    OutlinedTextField(
        state = textFieldState,
        modifier = (modifier).uiName("app.ui.food.recipe.TextField.OutlinedTextField.1", "androidx.compose.material3.OutlinedTextField", uiInspectionSource, "TextField"),
        label = { Text(label, modifier = Modifier.uiName("app.ui.food.recipe.TextField.Text.7", "androidx.compose.material3.Text", uiInspectionSource, "TextField")) },
        supportingText = {
            val error = this.error
            if (error != null) {
                Text(error.stringResource(), modifier = Modifier.uiName("TextField.Text.8", "androidx.compose.material3.Text", uiInspectionSource, "TextField"))
            } else if (supportingText != null) {
                Text(supportingText, modifier = Modifier.uiName("TextField.Text.9", "androidx.compose.material3.Text", uiInspectionSource, "TextField"))
            }
        },
        isError = error != null,
        keyboardOptions =
            if (T::class == Int::class) {
                KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = imeAction)
            } else {
                KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = imeAction)
            },
    )
}

@Composable
private fun AddIngredientButton(
    onAddIngredient: () -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            (modifier
                .clickable { onAddIngredient() }
                .padding(contentPadding)
                .padding(vertical = 12.dp)).uiName("AddIngredientButton.Row.1", "androidx.compose.foundation.layout.Row", uiInspectionSource, "AddIngredientButton"),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(
            modifier = (Modifier).uiName("AddIngredientButton.Surface.1", "androidx.compose.material3.Surface", uiInspectionSource, "AddIngredientButton"),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                modifier = (Modifier.size(40.dp).padding(8.dp)).uiName("AddIngredientButton.Icon.1", "androidx.compose.material3.Icon", uiInspectionSource, "AddIngredientButton"),
            )
        }

        Spacer(Modifier.width(12.dp))

        Text(
            text = stringResource(Res.string.action_add_ingredient),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary, modifier = Modifier.uiName("AddIngredientButton.Text.1", "androidx.compose.material3.Text", uiInspectionSource, "AddIngredientButton")
        )
    }
}


private val uiInspectionSource = UiSourceFile(
    "com.maksimowiczm.foodyou.app.ui.food.recipe",
    "app/src/commonMain/kotlin/com/maksimowiczm/foodyou/app/ui/food/recipe/RecipeForm.kt",
)
