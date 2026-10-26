package com.maksimowiczm.foodyou.app.ui.food.recipe

import com.maksimowiczm.foodyou.app.ui.developer.inspection.UiSourceFile
import com.maksimowiczm.foodyou.app.ui.developer.inspection.uiName
import com.maksimowiczm.foodyou.app.ui.developer.inspection.UiSourceScope

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.maksimowiczm.foodyou.app.navigation.forwardBackwardComposable
import com.maksimowiczm.foodyou.app.ui.common.component.ArrowBackIconButton
import com.maksimowiczm.foodyou.app.ui.common.component.IncompleteFoodsList
import com.maksimowiczm.foodyou.app.ui.food.search.FoodSearchApp
import com.maksimowiczm.foodyou.app.ui.food.shared.component.NutrientList
import com.maksimowiczm.foodyou.common.domain.food.isComplete
import com.maksimowiczm.foodyou.common.domain.measurement.Measurement
import com.maksimowiczm.foodyou.common.domain.measurement.MeasurementType
import com.maksimowiczm.foodyou.common.domain.measurement.from
import com.maksimowiczm.foodyou.common.domain.measurement.rawValue
import com.maksimowiczm.foodyou.common.domain.measurement.type
import com.maksimowiczm.foodyou.food.domain.entity.NutritionalItemId
import com.maksimowiczm.foodyou.food.domain.entity.Product
import com.maksimowiczm.foodyou.food.domain.entity.Recipe
import foodyou.app.generated.resources.*
import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
internal fun RecipeApp(
    onBack: () -> Unit,
    onSave: (RecipeFormState) -> Unit,
    onEditFood: (NutritionalItemId) -> Unit,
    onUpdateUsdaApiKey: () -> Unit,
    onUpdateOpenFoodFactsCredentials: () -> Unit,
    state: RecipeFormState,
    topBarTitle: String,
    saving: Boolean,
    mainRecipeId: NutritionalItemId.Recipe?,
    recipe: Recipe?,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {
    NavHost(navController = navController, startDestination = Form, modifier = modifier) {
        forwardBackwardComposable<Form> {
            val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text(topBarTitle, modifier = Modifier.uiName("RecipeApp.Text.1", "androidx.compose.material3.Text", uiInspectionSource, "RecipeApp")) },
                        navigationIcon = { ArrowBackIconButton(onBack, modifier = Modifier.uiName("RecipeApp.ArrowBackIconButton.1", "com.maksimowiczm.foodyou.app.ui.common.component.ArrowBackIconButton", uiInspectionSource, "RecipeApp")) },
                        actions = {
                            FilledIconButton(onClick = { onSave(state) }, enabled = state.isValid && !saving, modifier = Modifier.uiName("RecipeApp.FilledIconButton.1", "androidx.compose.material3.FilledIconButton", uiInspectionSource, "RecipeApp")) {
                                Icon(
                                    imageVector = Icons.Outlined.Save,
                                    contentDescription = stringResource(Res.string.action_save), modifier = Modifier.uiName("RecipeApp.Icon.1", "androidx.compose.material3.Icon", uiInspectionSource, "RecipeApp")
                                )
                            }
                        },
                        scrollBehavior = scrollBehavior, modifier = Modifier.uiName("RecipeApp.TopAppBar.1", "androidx.compose.material3.TopAppBar", uiInspectionSource, "RecipeApp")
                    )
                }, modifier = Modifier.uiName("RecipeApp.Scaffold.1", "androidx.compose.material3.Scaffold", uiInspectionSource, "RecipeApp")
            ) { paddingValues ->
                LazyColumn(
                    modifier =
                        (Modifier.fillMaxSize()
                            .imePadding()
                            .nestedScroll(scrollBehavior.nestedScrollConnection)).uiName("RecipeApp.LazyColumn.1", "androidx.compose.foundation.lazy.LazyColumn", uiInspectionSource, "RecipeApp"),
                    contentPadding = paddingValues,
                ) {
                    item {
                        RecipeForm(
                            state = state,
                            onAddIngredient = {
                                navController.navigate(IngredientsSearch) {
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            onEditIngredient = {
                                navController.navigate(EditIngredient(it)) {
                                    launchSingleTop = true
                                }
                            },
                            contentPadding = PaddingValues(horizontal = 16.dp), modifier = Modifier.uiName("RecipeApp.RecipeForm.1", "com.maksimowiczm.foodyou.app.ui.food.recipe.RecipeForm", uiInspectionSource, "RecipeApp")
                        )
                    }

                    if (recipe != null) {
                        item {
                            HorizontalDivider((Modifier.padding(bottom = 8.dp)).uiName("RecipeApp.HorizontalDivider.1", "androidx.compose.material3.HorizontalDivider", uiInspectionSource, "RecipeApp"))
                            Text(
                                text = stringResource(Res.string.recipe_total_nutrition),
                                modifier = (Modifier.padding(horizontal = 16.dp, vertical = 8.dp)).uiName("RecipeApp.Text.2", "androidx.compose.material3.Text", uiInspectionSource, "RecipeApp"),
                                color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.labelLarge,
                            )

                            val facts = recipe.totalNutrition
                            NutrientList(
                                facts = facts,
                                modifier = (Modifier.padding(horizontal = 8.dp)).uiName("RecipeApp.NutrientList.1", "com.maksimowiczm.foodyou.app.ui.food.shared.component.NutrientList", uiInspectionSource, "RecipeApp"),
                            )

                            Text(stringResource(Res.string.recipe_per_serving), modifier = (Modifier.padding(horizontal = 16.dp)).uiName("RecipeApp.Text.3", "androidx.compose.material3.Text", uiInspectionSource, "RecipeApp"))
                            NutrientList(recipe.nutritionFor(Measurement.Serving(1.0)) ?: com.maksimowiczm.foodyou.common.domain.food.NutritionFacts(), modifier = Modifier.uiName("RecipeApp.NutrientList.2", "com.maksimowiczm.foodyou.app.ui.food.shared.component.NutrientList", uiInspectionSource, "RecipeApp"))
                            if (!facts.isComplete) {
                                val foods =
                                    recipe
                                        .flatIngredients()
                                        .filter { it is Product }
                                        .filter { !it.nutritionFacts.isComplete }

                                IncompleteFoodsList(
                                    foods = foods.map { it.headline }.distinct(),
                                    modifier =
                                        (Modifier.padding(horizontal = 16.dp, vertical = 8.dp)).uiName("RecipeApp.IncompleteFoodsList.1", "com.maksimowiczm.foodyou.app.ui.common.component.IncompleteFoodsList", uiInspectionSource, "RecipeApp"),
                                    onFoodClick = { i -> onEditFood(foods[i].id) },
                                )
                            }
                        }
                    }
                }
            }
        }
        forwardBackwardComposable<IngredientsSearch> {
            val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text(stringResource(Res.string.headline_choose_ingredient), modifier = Modifier.uiName("RecipeApp.Text.4", "androidx.compose.material3.Text", uiInspectionSource, "RecipeApp")) },
                        navigationIcon = {
                            ArrowBackIconButton(
                                onClick = { navController.popBackStack<IngredientsSearch>(true) }, modifier = Modifier.uiName("RecipeApp.ArrowBackIconButton.2", "com.maksimowiczm.foodyou.app.ui.common.component.ArrowBackIconButton", uiInspectionSource, "RecipeApp")
                            )
                        },
                        scrollBehavior = scrollBehavior, modifier = Modifier.uiName("RecipeApp.TopAppBar.2", "androidx.compose.material3.TopAppBar", uiInspectionSource, "RecipeApp")
                    )
                },
                contentWindowInsets =
                    ScaffoldDefaults.contentWindowInsets.only(WindowInsetsSides.Top), modifier = Modifier.uiName("RecipeApp.Scaffold.2", "androidx.compose.material3.Scaffold", uiInspectionSource, "RecipeApp")
            ) { paddingValues ->
                FoodSearchApp(
                    onFoodClick = { model, measurement ->
                        navController.navigate(IngredientMeasurement(model.id, measurement)) {
                            launchSingleTop = true
                        }
                    },
                    onUpdateUsdaApiKey = onUpdateUsdaApiKey,
                    onUpdateOpenFoodFactsCredentials = onUpdateOpenFoodFactsCredentials,
                    excludedRecipe = mainRecipeId,
                    modifier =
                        (Modifier.fillMaxSize()
                            .nestedScroll(scrollBehavior.nestedScrollConnection)
                            .padding(paddingValues)
                            .consumeWindowInsets(paddingValues)).uiName("RecipeApp.FoodSearchApp.1", "com.maksimowiczm.foodyou.app.ui.food.search.FoodSearchApp", uiInspectionSource, "RecipeApp"),
                )
            }
        }
        forwardBackwardComposable<IngredientMeasurement> {
            val route = it.toRoute<IngredientMeasurement>()

            UiSourceScope(uiInspectionSource.element("RecipeApp.MeasureIngredientScreen.1", "com.maksimowiczm.foodyou.app.ui.food.recipe.MeasureIngredientScreen", "RecipeApp")) { MeasureIngredientScreen(
                onBack = { navController.popBackStack<IngredientMeasurement>(true) },
                onSave = { measurement ->
                    state.addIngredient(
                        MinimalIngredient(foodId = route.foodId, measurement = measurement)
                    )
                    navController.popBackStack<IngredientMeasurement>(true)
                    navController.popBackStack<IngredientsSearch>(true, saveState = true)
                },
                measurement = route.measurement,
                viewModel = koinViewModel { parametersOf(route.foodId) },
            ) }
        }
        forwardBackwardComposable<EditIngredient> {
            val route = it.toRoute<EditIngredient>()

            val ingredient = state.ingredients.getOrNull(route.index)

            LaunchedEffect(ingredient) {
                if (ingredient == null) {
                    navController.popBackStack<EditIngredient>(true)
                }
            }

            if (ingredient != null) {
                UiSourceScope(uiInspectionSource.element("RecipeApp.MeasureIngredientScreen.2", "com.maksimowiczm.foodyou.app.ui.food.recipe.MeasureIngredientScreen", "RecipeApp")) { MeasureIngredientScreen(
                    onBack = { navController.popBackStack<EditIngredient>(true) },
                    onSave = { measurement ->
                        state.updateIngredient(
                            route.index,
                            MinimalIngredient(foodId = ingredient.foodId, measurement = measurement),
                        )
                        navController.popBackStack<EditIngredient>(true)
                    },
                    measurement = ingredient.measurement,
                    viewModel = koinViewModel { parametersOf(ingredient.foodId) },
                ) }
            }
        }
    }
}

@Serializable private data object Form

@Serializable private data object IngredientsSearch

@Serializable private data class EditIngredient(val index: Int)

@Serializable
private data class IngredientMeasurement(
    val productId: Long?,
    val recipeId: Long?,
    val measurementType: MeasurementType,
    val quantity: Double,
) {
    constructor(
        foodId: NutritionalItemId,
        measurement: Measurement,
    ) : this(
        productId = (foodId as? NutritionalItemId.Product)?.id,
        recipeId = (foodId as? NutritionalItemId.Recipe)?.id,
        measurementType = measurement.type,
        quantity = measurement.rawValue,
    )

    val foodId: NutritionalItemId
        get() =
            when {
                productId != null -> NutritionalItemId.Product(productId)
                recipeId != null -> NutritionalItemId.Recipe(recipeId)
                else -> throw IllegalStateException("Food ID is not set")
            }

    val measurement: Measurement
        get() = Measurement.from(measurementType, quantity)
}


private val uiInspectionSource = UiSourceFile(
    "com.maksimowiczm.foodyou.app.ui.food.recipe",
    "app/src/commonMain/kotlin/com/maksimowiczm/foodyou/app/ui/food/recipe/RecipeApp.kt",
)
