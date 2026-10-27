package com.maksimowiczm.foodyou.app.ui.food.library

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.maksimowiczm.foodyou.app.ui.common.component.ArrowBackIconButton
import com.maksimowiczm.foodyou.app.ui.common.utility.LocalEnergyFormatter
import com.maksimowiczm.foodyou.app.ui.food.meals.*
import com.maksimowiczm.foodyou.app.ui.developer.inspection.*
import com.maksimowiczm.foodyou.common.domain.food.*
import com.maksimowiczm.foodyou.common.extension.now
import com.maksimowiczm.foodyou.food.domain.entity.*
import com.maksimowiczm.foodyou.fooddiary.domain.entity.Meal
import foodyou.app.generated.resources.*
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.StringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun PersonalLibraryScreen(
    onBack: () -> Unit, onCreateProduct: () -> Unit, onCreateRecipe: () -> Unit,
    onEdit: (NutritionalItemId) -> Unit, onUpdateUsdaApiKey: () -> Unit,
    onUpdateOpenFoodFactsCredentials: () -> Unit, saved: Boolean = false,
    onSavedAcknowledged: () -> Unit = {}, modifier: Modifier = Modifier,
) {
    val library: LibraryViewModel = koinViewModel()
    val meals: MealsViewModel = koinViewModel()
    var tab by rememberSaveable { mutableStateOf(LibraryTab.Recipes) }
    var query by rememberSaveable { mutableStateOf("") }
    val recipeScroll = rememberLazyListState()
    val mealScroll = rememberLazyListState()
    val productScroll = rememberLazyListState()
    val snackbars = remember { SnackbarHostState() }
    val savedMessage = stringResource(Res.string.library_saved)
    val mealSaves by meals.savedCount.collectAsStateWithLifecycle()
    var handledMealSaves by rememberSaveable { mutableIntStateOf(0) }
    LaunchedEffect(saved) {
        if (saved) {
            snackbars.showSnackbar(savedMessage)
            onSavedAcknowledged()
        }
    }
    LaunchedEffect(mealSaves) {
        if (mealSaves == 0) handledMealSaves = 0
        if (mealSaves > handledMealSaves) {
            handledMealSaves = mealSaves
            snackbars.showSnackbar(savedMessage)
        }
    }
    // This parent remains composed during Meal editing; navigation saves it during other editors.
    MealsApp(meals, onBack, LocalDate.now(), onUpdateUsdaApiKey, onUpdateOpenFoodFactsCredentials,
        modifier = (modifier).uiName("PersonalLibraryScreen.MealsApp.library1", "com.maksimowiczm.foodyou.app.ui.food.meals.MealsApp", uiInspectionSource, "PersonalLibraryScreen"),
        library = { editMeal, loadMeal, deleteMeal ->
            val savedMeals by meals.meals.collectAsStateWithLifecycle()
            val readFailed by meals.readFailed.collectAsStateWithLifecycle()
            LibraryContent(library, tab, { tab = it }, query, { query = it },
                when(tab) { LibraryTab.Recipes -> recipeScroll; LibraryTab.Meals -> mealScroll; LibraryTab.Products -> productScroll },
                savedMeals, readFailed, { library.retry(); meals.retryRead() }, onBack,
                { when(tab) { LibraryTab.Recipes -> onCreateRecipe(); LibraryTab.Meals -> meals.create(); LibraryTab.Products -> onCreateProduct() } },
                onEdit, editMeal, loadMeal, deleteMeal, snackbars, modifier = Modifier.uiName("PersonalLibraryScreen.LibraryContent.library1", "com.maksimowiczm.foodyou.app.ui.food.library.LibraryContent", uiInspectionSource, "PersonalLibraryScreen"))
        })
}

internal fun LibraryTab.title(): StringResource = when(this) {
    LibraryTab.Recipes -> Res.string.library_recipes
    LibraryTab.Meals -> Res.string.library_meals
    LibraryTab.Products -> Res.string.library_products
}
internal fun LibraryTab.createLabel(): StringResource = when(this) {
    LibraryTab.Recipes -> Res.string.library_create_recipe
    LibraryTab.Meals -> Res.string.library_create_meal
    LibraryTab.Products -> Res.string.library_create_product
}

@Composable
private fun LibraryContent(
    viewModel: LibraryViewModel, tab: LibraryTab, onTab: (LibraryTab) -> Unit,
    query: String, onQuery: (String) -> Unit, scroll: LazyListState,
    meals: List<Meal>?, mealReadFailed: Boolean, onRetry: () -> Unit,
    onBack: () -> Unit, onCreate: () -> Unit, onEdit: (NutritionalItemId) -> Unit,
    onEditMeal: (Meal) -> Unit, onLoadMeal: (Meal) -> Unit, onDeleteMeal: (Meal) -> Unit,
    snackbars: SnackbarHostState, modifier: Modifier = Modifier,
) {
    Scaffold(modifier = (modifier.imePadding().testTag("personal-library")).uiName("LibraryContent.Scaffold.library1", "androidx.compose.material3.Scaffold", uiInspectionSource, "LibraryContent"),
        topBar = { TopAppBar(title = { Text(stringResource(Res.string.library_title), modifier = Modifier.uiName("LibraryContent.Text.library1", "androidx.compose.material3.Text", uiInspectionSource, "LibraryContent")) },
            navigationIcon = { ArrowBackIconButton(onBack, modifier = Modifier.uiName("LibraryContent.ArrowBackIconButton.library1", "com.maksimowiczm.foodyou.app.ui.common.component.ArrowBackIconButton", uiInspectionSource, "LibraryContent")) }, modifier = Modifier.uiName("LibraryContent.TopAppBar.library1", "androidx.compose.material3.TopAppBar", uiInspectionSource, "LibraryContent")) },
        snackbarHost = { SnackbarHost(snackbars, modifier = Modifier.uiName("LibraryContent.SnackbarHost.library1", "androidx.compose.material3.SnackbarHost", uiInspectionSource, "LibraryContent")) },
        bottomBar = {
            Surface (modifier = Modifier.uiName("LibraryContent.Surface.library1", "androidx.compose.material3.Surface", uiInspectionSource, "LibraryContent")) {
                Button(onClick = onCreate, modifier = (Modifier.fillMaxWidth().navigationBarsPadding().padding(16.dp).testTag("library-create")).uiName("LibraryContent.Button.library1", "androidx.compose.material3.Button", uiInspectionSource, "LibraryContent")) {
                    Text(stringResource(tab.createLabel()), modifier = Modifier.uiName("LibraryContent.Text.library2", "androidx.compose.material3.Text", uiInspectionSource, "LibraryContent"))
                }
            }
        },
    ) { padding ->
        Column(modifier = (Modifier.fillMaxSize().padding(padding)).uiName("LibraryContent.Column.library1", "androidx.compose.foundation.layout.Column", uiInspectionSource, "LibraryContent")) {
            OutlinedTextField(value = query, onValueChange = onQuery,
                label = { Text(stringResource(Res.string.library_search), modifier = Modifier.uiName("LibraryContent.Text.library3", "androidx.compose.material3.Text", uiInspectionSource, "LibraryContent")) }, singleLine = true,
                leadingIcon = { Icon(Icons.Outlined.Search, null, modifier = Modifier.uiName("LibraryContent.Icon.library1", "androidx.compose.material3.Icon", uiInspectionSource, "LibraryContent")) },
                trailingIcon = {
                    if (query.isNotEmpty()) IconButton(onClick = { onQuery("") }, modifier = Modifier.uiName("LibraryContent.IconButton.library1", "androidx.compose.material3.IconButton", uiInspectionSource, "LibraryContent")) {
                        Icon(Icons.Outlined.Close, stringResource(Res.string.library_clear_search), modifier = Modifier.uiName("LibraryContent.Icon.library2", "androidx.compose.material3.Icon", uiInspectionSource, "LibraryContent"))
                    }
                }, modifier = (Modifier.fillMaxWidth().padding(horizontal = 16.dp).testTag("library-query")).uiName("LibraryContent.OutlinedTextField.library1", "androidx.compose.material3.OutlinedTextField", uiInspectionSource, "LibraryContent"))
            PrimaryScrollableTabRow(selectedTabIndex = tab.ordinal, edgePadding = 0.dp, modifier = Modifier.uiName("LibraryContent.PrimaryScrollableTabRow.library1", "androidx.compose.material3.PrimaryScrollableTabRow", uiInspectionSource, "LibraryContent")) {
                LibraryTab.entries.forEach { choice ->
                    Tab(selected = tab == choice, onClick = { onTab(choice) },
                        text = { Text(stringResource(choice.title()), maxLines = 1, softWrap = false, modifier = Modifier.uiName("LibraryContent.Text.library4", "androidx.compose.material3.Text", uiInspectionSource, "LibraryContent")) }, modifier = (Modifier.testTag("library-tab-" + choice.name)).uiName("LibraryContent.Tab.library1", "androidx.compose.material3.Tab", uiInspectionSource, "LibraryContent"))
                }
            }
            key(tab) {
                if (tab == LibraryTab.Meals) {
                    val filtered = meals?.filter { it.name.contains(query.trim(), true) }
                        ?.sortedWith(compareBy<Meal> { it.name.lowercase() }.thenBy { it.id.value })
                    LazyColumn(state = scroll, contentPadding = PaddingValues(16.dp), modifier = (Modifier.fillMaxSize().testTag("library-list")).uiName("LibraryContent.LazyColumn.library1", "androidx.compose.foundation.lazy.LazyColumn", uiInspectionSource, "LibraryContent")) {
                        when {
                            mealReadFailed -> item { LibraryStatus(Res.string.meals_unavailable, onRetry, modifier = Modifier.uiName("LibraryContent.LibraryStatus.library1", "com.maksimowiczm.foodyou.app.ui.food.library.LibraryStatus", uiInspectionSource, "LibraryContent")) }
                            filtered == null -> item { LibraryStatus(Res.string.library_loading, modifier = Modifier.uiName("LibraryContent.LibraryStatus.library2", "com.maksimowiczm.foodyou.app.ui.food.library.LibraryStatus", uiInspectionSource, "LibraryContent")) }
                            filtered.isEmpty() -> item { LibraryEmpty(query, modifier = Modifier.uiName("LibraryContent.LibraryEmpty.library1", "com.maksimowiczm.foodyou.app.ui.food.library.LibraryEmpty", uiInspectionSource, "LibraryContent")) }
                            else -> items(filtered, key = { "meal-" + it.id.value }) { meal ->
                                Column (modifier = Modifier.uiName("LibraryContent.Column.library2", "androidx.compose.foundation.layout.Column", uiInspectionSource, "LibraryContent")) {
                                    LibraryRow(meal.name, libraryNutrition(meal), { onEditMeal(meal) }, (Modifier.testTag("library-meal-" + meal.id.value)).uiName("LibraryContent.LibraryRow.library1", "com.maksimowiczm.foodyou.app.ui.food.library.LibraryRow", uiInspectionSource, "LibraryContent"))
                                    FlowRow (modifier = Modifier.uiName("LibraryContent.FlowRow.library1", "androidx.compose.foundation.layout.FlowRow", uiInspectionSource, "LibraryContent")) {
                                        TextButton(onClick = { onLoadMeal(meal) }, modifier = Modifier.uiName("LibraryContent.TextButton.library1", "androidx.compose.material3.TextButton", uiInspectionSource, "LibraryContent")) { Text(stringResource(Res.string.action_load_meal), modifier = Modifier.uiName("LibraryContent.Text.library5", "androidx.compose.material3.Text", uiInspectionSource, "LibraryContent")) }
                                        TextButton(onClick = { onDeleteMeal(meal) }, modifier = Modifier.uiName("LibraryContent.TextButton.library2", "androidx.compose.material3.TextButton", uiInspectionSource, "LibraryContent")) { Text(stringResource(Res.string.meals_delete), modifier = Modifier.uiName("LibraryContent.Text.library6", "androidx.compose.material3.Text", uiInspectionSource, "LibraryContent")) }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    val names by (if(tab == LibraryTab.Recipes) viewModel.recipes else viewModel.products).collectAsStateWithLifecycle()
                    LazyColumn(state = scroll, contentPadding = PaddingValues(16.dp), modifier = (Modifier.fillMaxSize().testTag("library-list")).uiName("LibraryContent.LazyColumn.library2", "androidx.compose.foundation.lazy.LazyColumn", uiInspectionSource, "LibraryContent")) {
                        when (val state = names) {
                            LibraryRead.Loading -> item { LibraryStatus(Res.string.library_loading, modifier = Modifier.uiName("LibraryContent.LibraryStatus.library3", "com.maksimowiczm.foodyou.app.ui.food.library.LibraryStatus", uiInspectionSource, "LibraryContent")) }
                            LibraryRead.Unavailable -> item { LibraryStatus(Res.string.meals_unavailable, onRetry, modifier = Modifier.uiName("LibraryContent.LibraryStatus.library4", "com.maksimowiczm.foodyou.app.ui.food.library.LibraryStatus", uiInspectionSource, "LibraryContent")) }
                            is LibraryRead.Ready -> {
                                val filtered = filterLibraryNames(state.value, query)
                                if (filtered.isEmpty()) item { LibraryEmpty(query, modifier = Modifier.uiName("LibraryContent.LibraryEmpty.library2", "com.maksimowiczm.foodyou.app.ui.food.library.LibraryEmpty", uiInspectionSource, "LibraryContent")) }
                                items(filtered, key = { it.id.toString() }) { name ->
                                    DefinitionLibraryRow(name, viewModel, onEdit, onRetry, modifier = Modifier.uiName("LibraryContent.DefinitionLibraryRow.library1", "com.maksimowiczm.foodyou.app.ui.food.library.DefinitionLibraryRow", uiInspectionSource, "LibraryContent"))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DefinitionLibraryRow(name: NutritionalItemName, viewModel: LibraryViewModel,
    onEdit: (NutritionalItemId) -> Unit, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    var retry by remember { mutableIntStateOf(0) }
    val read = remember(name.id, retry) { viewModel.row(name.id) }
    val item by read.collectAsStateWithLifecycle(viewModel.cachedRow(name.id))
    val facts = (item as? LibraryRead.Ready)?.value
    Column(modifier = (modifier.testTag("library-definition-" + name.id.toString())).uiName("DefinitionLibraryRow.Column.library1", "androidx.compose.foundation.layout.Column", uiInspectionSource, "DefinitionLibraryRow")) {
        LibraryRow(name.name, facts, { onEdit(name.id) }, modifier = Modifier.uiName("DefinitionLibraryRow.LibraryRow.library1", "com.maksimowiczm.foodyou.app.ui.food.library.LibraryRow", uiInspectionSource, "DefinitionLibraryRow"))
        if (item == LibraryRead.Unavailable) LibraryStatus(Res.string.library_unavailable, { retry++; onRetry() }, modifier = Modifier.uiName("DefinitionLibraryRow.LibraryStatus.library1", "com.maksimowiczm.foodyou.app.ui.food.library.LibraryStatus", uiInspectionSource, "DefinitionLibraryRow"))
        else if (item == LibraryRead.Loading) Text(stringResource(Res.string.library_loading), modifier = Modifier.uiName("DefinitionLibraryRow.Text.library1", "androidx.compose.material3.Text", uiInspectionSource, "DefinitionLibraryRow"))
    }
}

@Composable
private fun LibraryRow(name: String, nutrition: LibraryNutrition?, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(onClick = onClick, modifier = (modifier.fillMaxWidth()).uiName("LibraryRow.Surface.library1", "androidx.compose.material3.Surface", uiInspectionSource, "LibraryRow"), shape = MaterialTheme.shapes.medium) {
        Row(modifier = (Modifier.padding(vertical = 12.dp, horizontal = 8.dp)).uiName("LibraryRow.Row.library1", "androidx.compose.foundation.layout.Row", uiInspectionSource, "LibraryRow"), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Icon(Icons.Outlined.Restaurant, null, modifier = (Modifier.size(28.dp)).uiName("LibraryRow.Icon.library1", "androidx.compose.material3.Icon", uiInspectionSource, "LibraryRow"))
            Column(modifier = (Modifier.weight(1f)).uiName("LibraryRow.Column.library1", "androidx.compose.foundation.layout.Column", uiInspectionSource, "LibraryRow")) {
                Text(name, style = MaterialTheme.typography.titleMedium, modifier = Modifier.uiName("LibraryRow.Text.library1", "androidx.compose.material3.Text", uiInspectionSource, "LibraryRow"))
                if (nutrition != null) {
                    Text(stringResource(when(nutrition.basis) {
                        LibraryBasis.Per100Grams -> Res.string.library_per_100g
                        LibraryBasis.Per100Milliliters -> Res.string.library_per_100ml
                        LibraryBasis.ReferenceServing -> Res.string.library_per_reference
                        LibraryBasis.RecipeServing -> Res.string.library_per_serving
                        LibraryBasis.SavedMeal -> Res.string.library_whole_meal
                    }), style = MaterialTheme.typography.labelMedium, modifier = Modifier.uiName("LibraryRow.Text.library2", "androidx.compose.material3.Text", uiInspectionSource, "LibraryRow"))
                    nutrition.reference?.let { reference ->
                        val description = listOfNotNull(reference.amount?.toString(), reference.unitText, reference.text).joinToString(" ")
                        Text(description.ifBlank { stringResource(Res.string.library_reference_unknown) }, style = MaterialTheme.typography.bodySmall, modifier = Modifier.uiName("LibraryRow.Text.library3", "androidx.compose.material3.Text", uiInspectionSource, "LibraryRow"))
                    }
                    val facts = nutrition.facts
                    if (facts == null) Text(stringResource(if(nutrition.needsReview) Res.string.library_needs_review else Res.string.library_serving_unavailable), modifier = Modifier.uiName("LibraryRow.Text.library4", "androidx.compose.material3.Text", uiInspectionSource, "LibraryRow"))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.uiName("LibraryRow.FlowRow.library1", "androidx.compose.foundation.layout.FlowRow", uiInspectionSource, "LibraryRow")) {
                        Text(stringResource(Res.string.library_carbs, libraryValue(facts?.carbohydrates)), modifier = Modifier.uiName("LibraryRow.Text.library5", "androidx.compose.material3.Text", uiInspectionSource, "LibraryRow"))
                        Text(stringResource(Res.string.library_fat, libraryValue(facts?.fats)), modifier = Modifier.uiName("LibraryRow.Text.library6", "androidx.compose.material3.Text", uiInspectionSource, "LibraryRow"))
                        Text(stringResource(Res.string.library_protein, libraryValue(facts?.proteins)), modifier = Modifier.uiName("LibraryRow.Text.library7", "androidx.compose.material3.Text", uiInspectionSource, "LibraryRow"))
                    }
                    if (nutrition.reported) Text(stringResource(Res.string.library_reported_panel), style = MaterialTheme.typography.bodySmall, modifier = Modifier.uiName("LibraryRow.Text.library8", "androidx.compose.material3.Text", uiInspectionSource, "LibraryRow"))
                }
            }
            Text(libraryValue(nutrition?.facts?.energy, energy = true), style = MaterialTheme.typography.titleSmall,
                modifier = (Modifier.widthIn(max = 96.dp)).uiName("LibraryRow.Text.library9", "androidx.compose.material3.Text", uiInspectionSource, "LibraryRow"))
        }
    }
}

@Composable
private fun libraryValue(value: NutrientValue?, energy: Boolean = false): String {
    val formatter = LocalEnergyFormatter.current
    val unknown = stringResource(Res.string.captured_unknown_value)
    val unavailable = if(energy) unknown + " " + formatter.suffix() else unknown
    val amount = value?.value?.takeIf { it.isFinite() && it >= 0.0 } ?: return unavailable
    val converted = if(energy) formatter.fromKcal(amount) else amount
    if (!converted.isFinite()) return unavailable
    val shown = if(energy) formatter.formatEnergy(amount, withSuffix = true)
        else amount.toString() + " " + stringResource(Res.string.unit_gram_short)
    return if(value.isIncomplete) stringResource(Res.string.captured_incomplete_value, shown) else shown
}

@Composable
private fun LibraryEmpty(query: String, modifier: Modifier = Modifier) {
    Column(modifier = (modifier.padding(vertical = 24.dp)).uiName("LibraryEmpty.Column.library1", "androidx.compose.foundation.layout.Column", uiInspectionSource, "LibraryEmpty")) {
        Text(stringResource(if(query.isBlank()) Res.string.library_empty else Res.string.library_no_results), style = MaterialTheme.typography.titleMedium, modifier = Modifier.uiName("LibraryEmpty.Text.library1", "androidx.compose.material3.Text", uiInspectionSource, "LibraryEmpty"))
        if(query.isBlank()) Text(stringResource(Res.string.library_empty_help), modifier = Modifier.uiName("LibraryEmpty.Text.library2", "androidx.compose.material3.Text", uiInspectionSource, "LibraryEmpty"))
    }
}

@Composable
private fun LibraryStatus(message: StringResource, onRetry: (() -> Unit)? = null, modifier: Modifier = Modifier) {
    Column(modifier = (modifier.padding(16.dp)).uiName("LibraryStatus.Column.library1", "androidx.compose.foundation.layout.Column", uiInspectionSource, "LibraryStatus")) {
        Text(stringResource(message), modifier = Modifier.uiName("LibraryStatus.Text.library1", "androidx.compose.material3.Text", uiInspectionSource, "LibraryStatus"))
        if(onRetry != null) TextButton(onClick = onRetry, modifier = Modifier.uiName("LibraryStatus.TextButton.library1", "androidx.compose.material3.TextButton", uiInspectionSource, "LibraryStatus")) { Text(stringResource(Res.string.meals_retry), modifier = Modifier.uiName("LibraryStatus.Text.library2", "androidx.compose.material3.Text", uiInspectionSource, "LibraryStatus")) }
    }
}

private val uiInspectionSource = UiSourceFile("com.maksimowiczm.foodyou.app.ui.food.library",
    "app/src/commonMain/kotlin/com/maksimowiczm/foodyou/app/ui/food/library/PersonalLibraryScreen.kt")
