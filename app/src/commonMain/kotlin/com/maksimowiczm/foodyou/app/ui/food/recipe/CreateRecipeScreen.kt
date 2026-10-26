package com.maksimowiczm.foodyou.app.ui.food.recipe

import com.maksimowiczm.foodyou.app.ui.developer.inspection.UiSourceFile
import com.maksimowiczm.foodyou.app.ui.developer.inspection.uiName

import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationEventHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import com.maksimowiczm.foodyou.app.ui.common.component.DiscardDialog
import com.maksimowiczm.foodyou.common.compose.extension.LaunchedCollectWithLifecycle
import com.maksimowiczm.foodyou.common.domain.measurement.Measurement
import com.maksimowiczm.foodyou.food.domain.entity.NutritionalItemId
import foodyou.app.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun CreateRecipeScreen(
    onBack: () -> Unit,
    onCreate: (NutritionalItemId.Recipe) -> Unit,
    onEditFood: (NutritionalItemId) -> Unit,
    onUpdateUsdaApiKey: () -> Unit,
    onUpdateOpenFoodFactsCredentials: () -> Unit,
    modifier: Modifier = Modifier,
    // Story 19 promotion seed. Defaults reproduce a blank new recipe. When promoting a Quick Add
    // entry, the caller supplies the mapped name/servings/note and a single placeholder ingredient
    // (a real backing product) so the derived-nutrition model stays intact (spec §6.3, Story 14).
    initialName: String = "",
    initialServings: Int = 1,
    initialNote: String? = null,
    initialIngredient: Pair<NutritionalItemId, Measurement>? = null,
) {
    val viewModel = koinViewModel<CreateRecipeViewModel>()
    val latestOnCreate by rememberUpdatedState(onCreate)
    var saveFailed by remember { mutableStateOf(false) }
    val saving by viewModel.saving.collectAsStateWithLifecycle()
    if (saveFailed) RecipeSaveErrorDialog { saveFailed = false }
    LaunchedCollectWithLifecycle(viewModel.events) {
        when (it) {
            CreateRecipeEvent.SaveFailed -> saveFailed = true
            is CreateRecipeEvent.Created -> latestOnCreate(it.recipeId)
        }
    }

    val initialIngredients =
        remember(initialIngredient) {
            listOfNotNull(
                initialIngredient?.let { (foodId, measurement) ->
                    MinimalIngredient(foodId = foodId, measurement = measurement)
                }
            )
        }
    val formState =
        rememberRecipeFormState(
            initialName = initialName,
            initialServings = initialServings,
            initialNote = initialNote,
            initialIsLiquid = false,
            initialIngredients = initialIngredients,
        )
    val asRecipe =
        remember(formState.draft()) { viewModel.intoRecipe(formState.draft()) }
            .collectAsStateWithLifecycle(null)
            .value

    var showDiscardDialog by rememberSaveable { mutableStateOf(false) }
    NavigationEventHandler(
        state = rememberNavigationEventState(NavigationEventInfo.None),
        isBackEnabled = formState.isModified,
        onBackCompleted = { showDiscardDialog = true },
    )
    if (showDiscardDialog) {
        DiscardDialog(
            onDismissRequest = { showDiscardDialog = false },
            onDiscard = {
                showDiscardDialog = false
                onBack()
            }, modifier = Modifier.uiName("CreateRecipeScreen.DiscardDialog.1", "com.maksimowiczm.foodyou.app.ui.common.component.DiscardDialog", uiInspectionSource, "CreateRecipeScreen")
        ) {
            Text(stringResource(Res.string.question_discard_recipe), modifier = Modifier.uiName("CreateRecipeScreen.Text.1", "androidx.compose.material3.Text", uiInspectionSource, "CreateRecipeScreen"))
        }
    }

    RecipeApp(
        onBack = {
            if (formState.isModified) {
                showDiscardDialog = true
            } else {
                onBack()
            }
        },
        onSave = viewModel::create,
        onEditFood = onEditFood,
        onUpdateUsdaApiKey = onUpdateUsdaApiKey,
        onUpdateOpenFoodFactsCredentials = onUpdateOpenFoodFactsCredentials,
        state = formState,
        saving = saving,
        topBarTitle = stringResource(Res.string.headline_create_recipe),
        mainRecipeId = null,
        recipe = asRecipe,
        modifier = (modifier).uiName("CreateRecipeScreen.RecipeApp.1", "com.maksimowiczm.foodyou.app.ui.food.recipe.RecipeApp", uiInspectionSource, "CreateRecipeScreen"),
    )
}


private val uiInspectionSource = UiSourceFile(
    "com.maksimowiczm.foodyou.app.ui.food.recipe",
    "app/src/commonMain/kotlin/com/maksimowiczm/foodyou/app/ui/food/recipe/CreateRecipeScreen.kt",
)
