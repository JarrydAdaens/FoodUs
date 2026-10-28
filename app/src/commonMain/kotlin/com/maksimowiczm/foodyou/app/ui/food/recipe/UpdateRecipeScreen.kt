package com.maksimowiczm.foodyou.app.ui.food.recipe

import com.maksimowiczm.foodyou.app.ui.developer.inspection.UiSourceFile
import com.maksimowiczm.foodyou.app.ui.developer.inspection.uiName

import androidx.compose.material3.Text
import com.maksimowiczm.foodyou.app.ui.food.component.DefinitionReadFeedback
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationEventHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import com.maksimowiczm.foodyou.app.ui.common.component.DiscardDialog
import com.maksimowiczm.foodyou.common.compose.extension.LaunchedCollectWithLifecycle
import com.maksimowiczm.foodyou.food.domain.entity.NutritionalItemId
import foodyou.app.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun UpdateRecipeScreen(
    onBack: () -> Unit,
    onEditFood: (NutritionalItemId) -> Unit,
    onUpdate: () -> Unit,
    onUpdateUsdaApiKey: () -> Unit,
    onUpdateOpenFoodFactsCredentials: () -> Unit,
    recipeId: NutritionalItemId.Recipe,
    modifier: Modifier = Modifier,
) {
    val viewModel = koinViewModel<UpdateRecipeViewModel> { parametersOf(recipeId) }
    val latestOnUpdate by rememberUpdatedState(onUpdate)
    var saveFailed by remember { mutableStateOf(false) }
    val saving by viewModel.saving.collectAsStateWithLifecycle()
    if (saveFailed) RecipeSaveErrorDialog { saveFailed = false }
    LaunchedCollectWithLifecycle(viewModel.events) {
        when (it) {
            UpdateRecipeEvent.SaveFailed -> saveFailed = true
            UpdateRecipeEvent.Updated -> latestOnUpdate()
        }
    }

    val recipe = viewModel.recipe.collectAsStateWithLifecycle().value

    if (recipe == null) {
        val loading by viewModel.loading.collectAsStateWithLifecycle()
        DefinitionReadFeedback(loading, viewModel::retryRead, onBack, modifier = Modifier.uiName("UpdateRecipeScreen.DefinitionReadFeedback.library1", "com.maksimowiczm.foodyou.app.ui.food.component.DefinitionReadFeedback", uiInspectionSource, "UpdateRecipeScreen"))
        return
    }

    if(recipe.isReported) {
        CapturedRecipeEditor(recipe,onBack,onUpdate,(modifier).uiName("UpdateRecipeScreen.CapturedRecipeEditor.1", "com.maksimowiczm.foodyou.app.ui.food.recipe.CapturedRecipeEditor", uiInspectionSource, "UpdateRecipeScreen"))
        return
    }

    val formState =
        rememberRecipeFormState(
            initialName = recipe.name,
            initialServings = recipe.servings,
            initialNote = recipe.note,
            initialIsLiquid = recipe.isLiquid,
            initialIngredients = recipe.ingredients.map { it.toMinimalIngredient() },
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
            }, modifier = Modifier.uiName("UpdateRecipeScreen.DiscardDialog.1", "com.maksimowiczm.foodyou.app.ui.common.component.DiscardDialog", uiInspectionSource, "UpdateRecipeScreen")
        ) {
            Text(stringResource(Res.string.question_discard_changes), modifier = Modifier.uiName("UpdateRecipeScreen.Text.1", "androidx.compose.material3.Text", uiInspectionSource, "UpdateRecipeScreen"))
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
        onSave = viewModel::update,
        onEditFood = onEditFood,
        onUpdateUsdaApiKey = onUpdateUsdaApiKey,
        onUpdateOpenFoodFactsCredentials = onUpdateOpenFoodFactsCredentials,
        state = formState,
        saving = saving,
        topBarTitle = stringResource(Res.string.headline_edit_recipe),
        mainRecipeId = recipeId,
        recipe = asRecipe,
        modifier = (modifier).uiName("UpdateRecipeScreen.RecipeApp.1", "com.maksimowiczm.foodyou.app.ui.food.recipe.RecipeApp", uiInspectionSource, "UpdateRecipeScreen"),
    )
}


private val uiInspectionSource = UiSourceFile(
    "com.maksimowiczm.foodyou.app.ui.food.recipe",
    "app/src/commonMain/kotlin/com/maksimowiczm/foodyou/app/ui/food/recipe/UpdateRecipeScreen.kt",
)
