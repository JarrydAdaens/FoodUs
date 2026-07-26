package com.maksimowiczm.foodyou.app.navigation

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.dialog
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.maksimowiczm.foodyou.app.ui.about.AboutScreen
import com.maksimowiczm.foodyou.app.ui.database.exportcsvproducts.ExportCsvProductsScreen
import com.maksimowiczm.foodyou.app.ui.database.australianfoodcompositiondatabase.AustralianFoodCompositionDatabaseScreen
import com.maksimowiczm.foodyou.app.ui.database.externaldatabases.ExternalDatabasesScreen
import com.maksimowiczm.foodyou.app.ui.database.externaldatabases.OpenFoodFactsLoginDialog
import com.maksimowiczm.foodyou.app.ui.database.externaldatabases.UpdateUsdaApiKeyDialog
import com.maksimowiczm.foodyou.app.ui.database.importcsvproducts.ImportCsvProductsScreen
import com.maksimowiczm.foodyou.app.ui.database.master.DatabaseSettingsScreen
import com.maksimowiczm.foodyou.app.ui.database.swissfoodcompositiondatabase.SwissFoodCompositionDatabaseScreen
import com.maksimowiczm.foodyou.app.ui.food.diary.add.AddEntryScreen
import com.maksimowiczm.foodyou.app.ui.food.diary.aiscan.AiScanScreen
import com.maksimowiczm.foodyou.app.ui.food.diary.fasttext.FastTextScreen
import com.maksimowiczm.foodyou.app.ui.food.diary.placeholder.PlaceholderMetaScreen
import com.maksimowiczm.foodyou.app.ui.food.diary.quickadd.CreateQuickAddScreen
import com.maksimowiczm.foodyou.app.ui.food.diary.quickadd.PromoteToRecipePlaceholderViewModel
import com.maksimowiczm.foodyou.app.ui.food.diary.quickadd.QuickAddPromotionSeed
import com.maksimowiczm.foodyou.app.ui.food.diary.quickadd.UpdateQuickAddScreen
import com.maksimowiczm.foodyou.app.ui.food.diary.quickadd.recipeIngredientWeightGrams
import com.maksimowiczm.foodyou.app.ui.food.diary.quickadd.recipeServings
import com.maksimowiczm.foodyou.app.ui.food.diary.quickadd.toProductPrefill
import com.maksimowiczm.foodyou.app.ui.food.diary.search.DiaryFoodSearchScreen
import com.maksimowiczm.foodyou.app.ui.food.diary.update.UpdateEntryScreen
import com.maksimowiczm.foodyou.app.ui.food.product.CreateProductScreen
import com.maksimowiczm.foodyou.app.ui.food.product.UpdateProductScreen
import com.maksimowiczm.foodyou.app.ui.food.recipe.CreateRecipeScreen
import com.maksimowiczm.foodyou.app.ui.food.recipe.UpdateRecipeScreen
import com.maksimowiczm.foodyou.app.ui.goals.master.GoalsScreen
import com.maksimowiczm.foodyou.app.ui.goals.setup.DailyGoalsScreen
import com.maksimowiczm.foodyou.app.ui.home.goals.GoalsCardSettings
import com.maksimowiczm.foodyou.app.ui.home.master.HomeScreen
import com.maksimowiczm.foodyou.app.ui.home.meals.settings.MealsCardsSettingsScreen
import com.maksimowiczm.foodyou.app.ui.home.personalization.HomePersonalizationScreen
import com.maksimowiczm.foodyou.app.ui.language.LanguageScreen
import com.maksimowiczm.foodyou.app.ui.meal.MealSettingsScreen
import com.maksimowiczm.foodyou.app.ui.personalization.PersonalizationScreen
import com.maksimowiczm.foodyou.app.ui.personalization.PersonalizeNutritionFactsScreen
import com.maksimowiczm.foodyou.app.ui.settings.SettingsScreen
import com.maksimowiczm.foodyou.app.ui.sponsor.SponsorScreen
import com.maksimowiczm.foodyou.app.ui.theme.ThemeScreen
import com.maksimowiczm.foodyou.common.domain.measurement.Measurement
import com.maksimowiczm.foodyou.common.domain.measurement.MeasurementType
import com.maksimowiczm.foodyou.common.domain.measurement.from
import com.maksimowiczm.foodyou.common.domain.measurement.rawValue
import com.maksimowiczm.foodyou.common.domain.measurement.type
import com.maksimowiczm.foodyou.food.domain.entity.FoodId
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun FoodYouAppNavHost(onDatabaseBackup: () -> Unit, modifier: Modifier = Modifier) {
    val navController = rememberNavController()

    NavHost(modifier = modifier, navController = navController, startDestination = Home) {
        forwardBackwardComposable<Home> {
            HomeScreen(
                onSettings = { navController.navigateSingleTop(Settings) },
                onTitle = { navController.navigateSingleTop(About) },
                onMealCardLongClick = { navController.navigateSingleTop(MealsPersonalization) },
                onMealCardAddClick = { epochDay, mealId ->
                    navController.navigateSingleTop(FoodDiarySearch(epochDay, mealId))
                },
                onMealCardQuickAddClick = { epochDay, mealId ->
                    navController.navigateSingleTop(FoodDiaryCreateQuickAdd(epochDay, mealId))
                },
                onMealCardAiScanClick = { epochDay, mealId ->
                    navController.navigateSingleTop(FoodDiaryAiScan(epochDay, mealId))
                },
                onMealCardFastTextClick = { epochDay, mealId ->
                    navController.navigateSingleTop(FoodDiaryFastText(epochDay, mealId))
                },
                onGoalsCardLongClick = { navController.navigateSingleTop(GoalsPersonalization) },
                onGoalsCardClick = { epochDate ->
                    navController.navigateSingleTop(Goals(epochDate))
                },
                onEditDiaryEntryClick = { foodEntryId, manualEntryId, isPlaceholder ->
                    when {
                        manualEntryId != null && isPlaceholder ->
                            navController.navigateSingleTop(
                                FoodDiaryPlaceholderMeta(manualEntryId = manualEntryId)
                            )

                        manualEntryId != null ->
                            navController.navigateSingleTop(
                                UpdateQuickAdd(quickAddId = manualEntryId)
                            )

                        foodEntryId != null ->
                            navController.navigateSingleTop(
                                FoodDiaryUpdateEntry(foodEntryId = foodEntryId)
                            )

                        else -> error("Either foodEntryId or manualEntryId must be non-null")
                    }
                },
            )
        }
        forwardBackwardComposable<Settings> {
            SettingsScreen(
                onBack = { navController.popBackStackInclusive<Settings>() },
                onAbout = { navController.navigateSingleTop(About) },
                onMeals = { navController.navigateSingleTop(MealSetup) },
                onLanguage = { navController.navigateSingleTop(Language) },
                onGoals = { navController.navigateSingleTop(GoalsSetup) },
                onPersonalization = { navController.navigateSingleTop(Personalization) },
                onDatabase = { navController.navigateSingleTop(DatabaseSettings) },
            )
        }
        forwardBackwardComposable<Language> {
            LanguageScreen(onBack = { navController.popBackStackInclusive<Language>() })
        }
        forwardBackwardComposable<About> {
            AboutScreen(
                onBack = { navController.popBackStackInclusive<About>() },
                onSponsor = { navController.navigateSingleTop(Sponsor) },
            )
        }
        forwardBackwardComposable<Sponsor> {
            SponsorScreen(onBack = { navController.popBackStackInclusive<Sponsor>() })
        }
        forwardBackwardComposable<MealSetup> {
            MealSettingsScreen(onBack = { navController.popBackStackInclusive<MealSetup>() })
        }
        forwardBackwardComposable<Goals> {
            val (epochDay) = it.toRoute<Goals>()

            GoalsScreen(
                onBack = { navController.popBackStackInclusive<Goals>() },
                epochDay = epochDay,
            )
        }
        forwardBackwardComposable<GoalsSetup> {
            DailyGoalsScreen(
                onBack = { navController.popBackStackInclusive<GoalsSetup>() },
                onSave = { navController.popBackStackInclusive<GoalsSetup>() },
            )
        }
        forwardBackwardComposable<DatabaseSettings> {
            DatabaseSettingsScreen(
                onBack = { navController.popBackStackInclusive<DatabaseSettings>() },
                onExternalDatabases = { navController.navigateSingleTop(ExternalDatabases) },
                onImportCsvProducts = { navController.navigateSingleTop(ImportCsvProducts) },
                onExportCsvProducts = { navController.navigateSingleTop(ExportCsvProducts) },
                onDatabaseBackup = onDatabaseBackup,
            )
        }
        forwardBackwardComposable<ExternalDatabases> {
            ExternalDatabasesScreen(
                onBack = { navController.popBackStackInclusive<ExternalDatabases>() },
                onSwissFoodCompositionDatabase = {
                    navController.navigateSingleTop(SwissFoodCompositionDatabase)
                },
                onAustralianFoodCompositionDatabase = {
                    navController.navigateSingleTop(AustralianFoodCompositionDatabase)
                },
            )
        }
        forwardBackwardComposable<SwissFoodCompositionDatabase> {
            SwissFoodCompositionDatabaseScreen(
                onBack = { navController.popBackStackInclusive<SwissFoodCompositionDatabase>() }
            )
        }
        forwardBackwardComposable<AustralianFoodCompositionDatabase> {
            AustralianFoodCompositionDatabaseScreen(
                onBack = {
                    navController.popBackStackInclusive<AustralianFoodCompositionDatabase>()
                }
            )
        }
        forwardBackwardComposable<ImportCsvProducts> {
            ImportCsvProductsScreen(
                onBack = { navController.popBackStackInclusive<ImportCsvProducts>() },
                onFinish = { navController.popBackStackInclusive<ImportCsvProducts>() },
            )
        }
        forwardBackwardComposable<ExportCsvProducts> {
            ExportCsvProductsScreen(
                onBack = { navController.popBackStackInclusive<ExportCsvProducts>() },
                onFinish = { navController.popBackStackInclusive<ExportCsvProducts>() },
            )
        }
        dialog<UsdaApiKey> {
            UpdateUsdaApiKeyDialog(
                onDismissRequest = { navController.popBackStackInclusive<UsdaApiKey>() },
                onSave = { navController.popBackStackInclusive<UsdaApiKey>() },
            )
        }

        dialog<OpenFoodFactsLogin> {
            OpenFoodFactsLoginDialog(
                onDismissRequest = { navController.popBackStackInclusive<OpenFoodFactsLogin>() },
                onSave = { navController.popBackStackInclusive<OpenFoodFactsLogin>() },
            )
        }
        forwardBackwardComposable<FoodDiaryCreateQuickAdd> {
            val route = it.toRoute<FoodDiaryCreateQuickAdd>()

            CreateQuickAddScreen(
                onBack = { navController.popBackStackInclusive<FoodDiaryCreateQuickAdd>() },
                onSave = { navController.popBackStackInclusive<FoodDiaryCreateQuickAdd>() },
                date = LocalDate.fromEpochDays(route.epochDay),
                mealId = route.mealId,
                onPromoteToProduct = { navController.navigate(PromoteToProduct(it)) },
                onPromoteToRecipe = { navController.navigate(PromoteToRecipe(it)) },
                prefillName = route.prefillName,
                prefillEnergyKcal = route.prefillEnergyKcal,
                prefillProteins = route.prefillProteins,
                prefillCarbohydrates = route.prefillCarbohydrates,
                prefillFats = route.prefillFats,
            )
        }
        forwardBackwardComposable<FoodDiaryAiScan> {
            val (epochDay, mealId) = it.toRoute<FoodDiaryAiScan>()

            AiScanScreen(
                onBack = { navController.popBackStackInclusive<FoodDiaryAiScan>() },
                onSaveToQuickAdd = { name, calories, protein, fat ->
                    navController.navigate(
                        FoodDiaryCreateQuickAdd(
                            epochDay = epochDay,
                            mealId = mealId,
                            prefillName = name,
                            prefillEnergyKcal = calories,
                            prefillProteins = protein,
                            prefillFats = fat,
                        )
                    )
                },
                onAlignPickFood = { foodId ->
                    navController.navigate(
                        FoodDiaryCreateEntry(
                            date = epochDay,
                            mealId = mealId,
                            foodId = foodId,
                            measurement = null,
                        )
                    )
                },
                onCreateCustomFood = {
                    navController.navigateSingleTop(FoodDiaryCreateProduct(epochDay, mealId))
                },
            )
        }
        forwardBackwardComposable<FoodDiaryFastText> {
            val (epochDay, mealId) = it.toRoute<FoodDiaryFastText>()

            FastTextScreen(
                date = LocalDate.fromEpochDays(epochDay),
                mealId = mealId,
                onBack = { navController.popBackStackInclusive<FoodDiaryFastText>() },
                onSave = { navController.popBackStackInclusive<FoodDiaryFastText>() },
            )
        }
        forwardBackwardComposable<FoodDiaryPlaceholderMeta> {
            val (manualEntryId) = it.toRoute<FoodDiaryPlaceholderMeta>()

            PlaceholderMetaScreen(
                manualEntryId = manualEntryId,
                onBack = { navController.popBackStackInclusive<FoodDiaryPlaceholderMeta>() },
                onSearch = { epochDay, mealId ->
                    navController.navigate(FoodDiarySearch(date = epochDay, mealId = mealId))
                },
                onQuickAdd = { epochDay, mealId, name ->
                    navController.navigate(
                        FoodDiaryCreateQuickAdd(
                            epochDay = epochDay,
                            mealId = mealId,
                            prefillName = name,
                        )
                    )
                },
                onAiQuery = { epochDay, mealId, query ->
                    navController.navigate(
                        FoodDiarySearch(date = epochDay, mealId = mealId, initialQuery = query)
                    )
                },
                onRemoved = { navController.popBackStackInclusive<FoodDiaryPlaceholderMeta>() },
            )
        }
        forwardBackwardComposable<UpdateQuickAdd> {
            val (quickAddId) = it.toRoute<UpdateQuickAdd>()

            UpdateQuickAddScreen(
                onBack = { navController.popBackStackInclusive<UpdateQuickAdd>() },
                onSave = { navController.popBackStackInclusive<UpdateQuickAdd>() },
                id = quickAddId,
                onPromoteToProduct = { navController.navigate(PromoteToProduct(it)) },
                onPromoteToRecipe = { navController.navigate(PromoteToRecipe(it)) },
            )
        }
        forwardBackwardComposable<FoodDiarySearch> {
            val route = it.toRoute<FoodDiarySearch>()
            val date = route.date
            val mealId = route.mealId

            DiaryFoodSearchScreen(
                initialQuery = route.initialQuery,
                onBack = { navController.popBackStackInclusive<FoodDiarySearch>() },
                onCreateRecipe = {
                    navController.navigateSingleTop(FoodDiaryCreateRecipe(date, mealId))
                },
                onCreateProduct = {
                    navController.navigateSingleTop(FoodDiaryCreateProduct(date, mealId))
                },
                onMeasure = { foodId, measurement ->
                    navController.navigate(
                        FoodDiaryCreateEntry(
                            date = date,
                            mealId = mealId,
                            foodId = foodId,
                            measurement = measurement,
                        )
                    )
                },
                onUpdateUsdaApiKey = { navController.navigateSingleTop(UsdaApiKey) },
                onUpdateOpenFoodFactsCredentials = {
                    navController.navigateSingleTop(OpenFoodFactsLogin)
                },
                date = LocalDate.fromEpochDays(date),
                mealId = mealId,
                animatedVisibilityScope = this,
            )
        }
        forwardBackwardComposable<UpdateRecipe> {
            val (recipeId) = it.toRoute<UpdateRecipe>()

            UpdateRecipeScreen(
                onBack = { navController.popBackStackInclusive<UpdateRecipe>() },
                onEditFood = { id ->
                    when (id) {
                        is FoodId.Product -> navController.navigateSingleTop(UpdateProduct(id.id))
                        is FoodId.Recipe -> error("Cannot edit recipe from recipe")
                    }
                },
                onUpdate = { navController.popBackStackInclusive<UpdateRecipe>() },
                onUpdateUsdaApiKey = { navController.navigateSingleTop(UsdaApiKey) },
                onUpdateOpenFoodFactsCredentials = {
                    navController.navigateSingleTop(OpenFoodFactsLogin)
                },
                recipeId = FoodId.Recipe(recipeId),
            )
        }
        forwardBackwardComposable<UpdateProduct> {
            val (productId) = it.toRoute<UpdateProduct>()

            UpdateProductScreen(
                onBack = { navController.popBackStackInclusive<UpdateProduct>() },
                onUpdate = { navController.popBackStackInclusive<UpdateProduct>() },
                productId = FoodId.Product(productId),
            )
        }
        forwardBackwardComposable<FoodDiaryCreateRecipe> {
            val (date, mealId) = it.toRoute<FoodDiaryCreateRecipe>()

            CreateRecipeScreen(
                onBack = { navController.popBackStackInclusive<FoodDiaryCreateRecipe>() },
                onCreate = { id ->
                    navController.navigate(
                        FoodDiaryCreateEntry(
                            date = date,
                            mealId = mealId,
                            foodId = id,
                            measurement = null,
                        )
                    ) {
                        popUpTo<FoodDiaryCreateRecipe> { inclusive = true }
                    }
                },
                onEditFood = { id ->
                    when (id) {
                        is FoodId.Product -> navController.navigateSingleTop(UpdateProduct(id.id))
                        is FoodId.Recipe -> error("Cannot edit recipe from recipe")
                    }
                },
                onUpdateUsdaApiKey = { navController.navigateSingleTop(UsdaApiKey) },
                onUpdateOpenFoodFactsCredentials = {
                    navController.navigateSingleTop(OpenFoodFactsLogin)
                },
            )
        }
        forwardBackwardComposable<FoodDiaryCreateProduct> {
            val (date, mealId) = it.toRoute<FoodDiaryCreateProduct>()

            CreateProductScreen(
                onBack = { navController.popBackStackInclusive<FoodDiaryCreateProduct>() },
                onCreate = { id ->
                    navController.navigate(
                        FoodDiaryCreateEntry(
                            date = date,
                            mealId = mealId,
                            foodId = id,
                            measurement = null,
                        )
                    ) {
                        popUpTo<FoodDiaryCreateProduct> { inclusive = true }
                    }
                },
                onUpdateUsdaApiKey = { navController.navigateSingleTop(UsdaApiKey) },
                onUpdateOpenFoodFactsCredentials = {
                    navController.navigateSingleTop(OpenFoodFactsLogin)
                },
            )
        }
        forwardBackwardComposable<PromoteToProduct> {
            // Story 19: open the product editor prefilled from a Quick Add estimate. The product is
            // created only when the editor is saved; cancelling creates nothing (spec §6.2/§6.6).
            val seed = it.toRoute<PromoteToProduct>().toSeed()

            CreateProductScreen(
                onBack = { navController.popBackStackInclusive<PromoteToProduct>() },
                onCreate = { navController.popBackStackInclusive<PromoteToProduct>() },
                onUpdateUsdaApiKey = { navController.navigateSingleTop(UsdaApiKey) },
                onUpdateOpenFoodFactsCredentials = {
                    navController.navigateSingleTop(OpenFoodFactsLogin)
                },
                prefillProduct = seed.toProductPrefill(),
            )
        }
        forwardBackwardComposable<PromoteToRecipe> {
            // Story 19: seed a recipe from a Quick Add estimate via a single placeholder ingredient
            // (a real backing product created up front). The recipe is created only on save; on
            // cancel the placeholder is discarded so nothing is left behind (spec §6.3/§6.6).
            val seed = it.toRoute<PromoteToRecipe>().toSeed()
            val placeholderViewModel =
                koinViewModel<PromoteToRecipePlaceholderViewModel> { parametersOf(seed) }
            val placeholderId by
                placeholderViewModel.productId.collectAsStateWithLifecycle()

            val id = placeholderId
            if (id != null) {
                CreateRecipeScreen(
                    onBack = {
                        placeholderViewModel.discardPlaceholderIfUncommitted()
                        navController.popBackStackInclusive<PromoteToRecipe>()
                    },
                    onCreate = {
                        placeholderViewModel.markCommitted()
                        navController.popBackStackInclusive<PromoteToRecipe>()
                    },
                    onEditFood = { food ->
                        when (food) {
                            is FoodId.Product ->
                                navController.navigateSingleTop(UpdateProduct(food.id))
                            is FoodId.Recipe -> error("Cannot edit recipe from recipe")
                        }
                    },
                    onUpdateUsdaApiKey = { navController.navigateSingleTop(UsdaApiKey) },
                    onUpdateOpenFoodFactsCredentials = {
                        navController.navigateSingleTop(OpenFoodFactsLogin)
                    },
                    initialName = seed.name,
                    initialServings = seed.recipeServings,
                    initialNote = seed.description,
                    initialIngredient =
                        FoodId.Product(id) to Measurement.Gram(seed.recipeIngredientWeightGrams),
                )
            }
        }
        forwardBackwardComposable<FoodDiaryCreateEntry> {
            val route = it.toRoute<FoodDiaryCreateEntry>()

            AddEntryScreen(
                onBack = { navController.popBackStackInclusive<FoodDiaryCreateEntry>() },
                onEditFood = { id ->
                    when (id) {
                        is FoodId.Product -> navController.navigateSingleTop(UpdateProduct(id.id))
                        is FoodId.Recipe -> navController.navigateSingleTop(UpdateRecipe(id.id))
                    }
                },
                onEntryAdded = {
                    while (true) {
                        if (!navController.popBackStackInclusive<FoodDiaryCreateEntry>()) {
                            break
                        }
                    }
                },
                onFoodDeleted = { navController.popBackStackInclusive<FoodDiaryCreateEntry>() },
                onIngredient = { foodId, measurement ->
                    navController.navigate(
                        FoodDiaryCreateEntry(
                            date = route.date,
                            mealId = route.mealId,
                            foodId = foodId,
                            measurement = measurement,
                        )
                    )
                },
                foodId = route.foodId,
                mealId = route.mealId,
                date = LocalDate.fromEpochDays(route.date),
                measurement = route.measurement,
                animatedVisibilityScope = this,
            )
        }
        forwardBackwardComposable<FoodDiaryUpdateEntry> {
            val (foodEntryId) = it.toRoute<FoodDiaryUpdateEntry>()

            UpdateEntryScreen(
                entryId = foodEntryId,
                onBack = { navController.popBackStackInclusive<FoodDiaryUpdateEntry>() },
                onSave = { navController.popBackStackInclusive<FoodDiaryUpdateEntry>() },
                animatedVisibilityScope = this,
            )
        }
        forwardBackwardComposable<Personalization> {
            PersonalizationScreen(
                onBack = { navController.popBackStackInclusive<Personalization>() },
                onTheme = { navController.navigateSingleTop(ThemeSettings) },
                onHomePersonalization = { navController.navigateSingleTop(HomePersonalization) },
                onNutritionFactsPersonalization = {
                    navController.navigateSingleTop(NutritionFactsPersonalization)
                },
            )
        }
        forwardBackwardComposable<ThemeSettings> {
            ThemeScreen(onBack = { navController.popBackStackInclusive<ThemeSettings>() })
        }
        forwardBackwardComposable<HomePersonalization> {
            HomePersonalizationScreen(
                onBack = { navController.popBackStackInclusive<HomePersonalization>() },
                onMeals = { navController.navigateSingleTop(MealsPersonalization) },
                onGoals = { navController.navigateSingleTop(GoalsPersonalization) },
            )
        }
        forwardBackwardComposable<NutritionFactsPersonalization> {
            PersonalizeNutritionFactsScreen(
                onBack = { navController.popBackStackInclusive<NutritionFactsPersonalization>() }
            )
        }
        forwardBackwardComposable<MealsPersonalization> {
            MealsCardsSettingsScreen(
                onBack = { navController.popBackStackInclusive<MealsPersonalization>() },
                onMealSettings = { navController.navigateSingleTop(MealSetup) },
            )
        }
        forwardBackwardComposable<GoalsPersonalization> {
            GoalsCardSettings(
                onBack = { navController.popBackStackInclusive<GoalsPersonalization>() },
                onGoalsSettings = { navController.navigateSingleTop(GoalsSetup) },
            )
        }
    }
}

@Serializable private object Home

@Serializable private object Settings

@Serializable private object About

@Serializable private object Language

@Serializable private object ThemeSettings

@Serializable private object Sponsor

@Serializable private object MealSetup

@Serializable private data class Goals(val epochDay: Long)

@Serializable private object GoalsSetup

@Serializable private object DatabaseSettings

@Serializable private object ExternalDatabases

@Serializable private object SwissFoodCompositionDatabase

@Serializable private object AustralianFoodCompositionDatabase

@Serializable private object UsdaApiKey

@Serializable private object OpenFoodFactsLogin

@Serializable private object ImportCsvProducts

@Serializable private object ExportCsvProducts

@Serializable
private data class FoodDiaryCreateQuickAdd(
    val epochDay: Long,
    val mealId: Long,
    // Optional AI-scan prefill (Milestone 2, Story 6). Energy is in kilocalories.
    val prefillName: String? = null,
    val prefillEnergyKcal: Double? = null,
    val prefillProteins: Double? = null,
    val prefillCarbohydrates: Double? = null,
    val prefillFats: Double? = null,
)

@Serializable private data class FoodDiaryAiScan(val epochDay: Long, val mealId: Long)

@Serializable private data class FoodDiaryFastText(val epochDay: Long, val mealId: Long)

@Serializable private data class FoodDiaryPlaceholderMeta(val manualEntryId: Long)

@Serializable private data class UpdateQuickAdd(val quickAddId: Long)

@Serializable
private data class FoodDiarySearch(
    val date: Long,
    val mealId: Long,
    // Optional pre-executed query, used by the placeholder AI route (Milestone 2, Story 9).
    val initialQuery: String? = null,
)

@Serializable private data class FoodDiaryCreateProduct(val date: Long, val mealId: Long)

// Story 19 promotion routes. Carry a flat copy of the mapped Quick Add fields (spec §6.2/§6.3); the
// source diary entry is never referenced or mutated (§6.5).
@Serializable
private data class PromoteToProduct(
    val name: String,
    val description: String?,
    val energyKcal: Double?,
    val proteins: Double?,
    val carbohydrates: Double?,
    val fats: Double?,
    val fibre: Double?,
    val servingCount: Double?,
    val weightGrams: Double?,
) {
    constructor(
        seed: QuickAddPromotionSeed
    ) : this(
        name = seed.name,
        description = seed.description,
        energyKcal = seed.energyKcal,
        proteins = seed.proteins,
        carbohydrates = seed.carbohydrates,
        fats = seed.fats,
        fibre = seed.fibre,
        servingCount = seed.servingCount,
        weightGrams = seed.weightGrams,
    )

    fun toSeed() =
        QuickAddPromotionSeed(
            name = name,
            description = description,
            energyKcal = energyKcal,
            proteins = proteins,
            carbohydrates = carbohydrates,
            fats = fats,
            fibre = fibre,
            servingCount = servingCount,
            weightGrams = weightGrams,
        )
}

@Serializable
private data class PromoteToRecipe(
    val name: String,
    val description: String?,
    val energyKcal: Double?,
    val proteins: Double?,
    val carbohydrates: Double?,
    val fats: Double?,
    val fibre: Double?,
    val servingCount: Double?,
    val weightGrams: Double?,
) {
    constructor(
        seed: QuickAddPromotionSeed
    ) : this(
        name = seed.name,
        description = seed.description,
        energyKcal = seed.energyKcal,
        proteins = seed.proteins,
        carbohydrates = seed.carbohydrates,
        fats = seed.fats,
        fibre = seed.fibre,
        servingCount = seed.servingCount,
        weightGrams = seed.weightGrams,
    )

    fun toSeed() =
        QuickAddPromotionSeed(
            name = name,
            description = description,
            energyKcal = energyKcal,
            proteins = proteins,
            carbohydrates = carbohydrates,
            fats = fats,
            fibre = fibre,
            servingCount = servingCount,
            weightGrams = weightGrams,
        )
}

@Serializable private data class FoodDiaryCreateRecipe(val date: Long, val mealId: Long)

@Serializable private data class UpdateRecipe(val recipeId: Long)

@Serializable private data class UpdateProduct(val productId: Long)

@Serializable
private class FoodDiaryCreateEntry(
    val date: Long,
    val mealId: Long,
    private val productId: Long?,
    private val recipeId: Long?,
    private val measurementType: MeasurementType?,
    private val measurementValue: Double?,
) {
    constructor(
        date: Long,
        mealId: Long,
        foodId: FoodId,
        measurement: Measurement?,
    ) : this(
        date = date,
        mealId = mealId,
        productId = if (foodId is FoodId.Product) foodId.id else null,
        recipeId = if (foodId is FoodId.Recipe) foodId.id else null,
        measurementType = measurement?.type,
        measurementValue = measurement?.rawValue,
    )

    init {
        require(productId != null || recipeId != null) {
            "Either productId or recipeId must be non-null"
        }
    }

    val foodId: FoodId
        get() =
            when {
                productId != null -> FoodId.Product(productId)
                recipeId != null -> FoodId.Recipe(recipeId)
                else -> error("Either productId or recipeId must be non-null")
            }

    val measurement: Measurement?
        get() =
            if (measurementValue != null && measurementType != null) {
                Measurement.from(type = measurementType, rawValue = measurementValue)
            } else {
                null
            }
}

@Serializable private data class FoodDiaryUpdateEntry(val foodEntryId: Long)

@Serializable private object Personalization

@Serializable private object HomePersonalization

@Serializable private object NutritionFactsPersonalization

@Serializable private object MealsPersonalization

@Serializable private object GoalsPersonalization
