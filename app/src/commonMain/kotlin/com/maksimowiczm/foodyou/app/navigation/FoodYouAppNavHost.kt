package com.maksimowiczm.foodyou.app.navigation

import com.maksimowiczm.foodyou.app.ui.developer.inspection.UiSourceFile
import com.maksimowiczm.foodyou.app.ui.developer.inspection.uiName
import com.maksimowiczm.foodyou.app.ui.developer.inspection.UiSourceScope

import com.maksimowiczm.foodyou.ai.domain.AiFoodEstimate
import com.maksimowiczm.foodyou.ai.domain.AiFoodEstimateParser
import com.maksimowiczm.foodyou.app.ui.food.diary.quickadd.QuickAddNutritionSnapshot
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

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
import com.maksimowiczm.foodyou.app.ui.database.importlibrary.ImportLibraryScreen
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
import com.maksimowiczm.foodyou.app.ui.food.library.PersonalLibraryScreen
import com.maksimowiczm.foodyou.app.ui.food.product.CreateProductScreen
import com.maksimowiczm.foodyou.app.ui.food.product.UpdateProductScreen
import com.maksimowiczm.foodyou.app.ui.food.recipe.CreateRecipeScreen
import com.maksimowiczm.foodyou.app.ui.food.recipe.UpdateRecipeScreen
import com.maksimowiczm.foodyou.app.ui.goals.master.GoalsScreen
import com.maksimowiczm.foodyou.app.ui.goals.setup.DailyGoalsScreen
import com.maksimowiczm.foodyou.app.ui.home.goals.GoalsCardSettings
import com.maksimowiczm.foodyou.app.ui.home.master.HomeScreen
import com.maksimowiczm.foodyou.app.ui.home.meals.settings.DiarySlotsCardsSettingsScreen
import com.maksimowiczm.foodyou.app.ui.home.personalization.HomePersonalizationScreen
import com.maksimowiczm.foodyou.app.ui.language.LanguageScreen
import com.maksimowiczm.foodyou.app.ui.meal.DiarySlotSettingsScreen
import com.maksimowiczm.foodyou.app.ui.personalization.PersonalizationScreen
import com.maksimowiczm.foodyou.app.ui.personalization.PersonalizeNutritionFactsScreen
import com.maksimowiczm.foodyou.app.ui.settings.SettingsScreen
import com.maksimowiczm.foodyou.app.ui.settings.ai.AiSettingsScreen
import com.maksimowiczm.foodyou.app.ui.settings.relay.RelaySettingsScreen
import com.maksimowiczm.foodyou.app.ui.sponsor.SponsorScreen
import com.maksimowiczm.foodyou.app.ui.theme.ThemeScreen
import com.maksimowiczm.foodyou.common.domain.measurement.Measurement
import com.maksimowiczm.foodyou.common.domain.measurement.MeasurementType
import com.maksimowiczm.foodyou.common.domain.measurement.from
import com.maksimowiczm.foodyou.common.domain.measurement.rawValue
import com.maksimowiczm.foodyou.common.domain.measurement.type
import com.maksimowiczm.foodyou.food.domain.entity.NutritionalItemId
import kotlinx.datetime.LocalDate
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun FoodYouAppNavHost(onDatabaseBackup: () -> Unit, modifier: Modifier = Modifier) {
    val navController = rememberNavController()

    NavHost(modifier = modifier, navController = navController, startDestination = Home) {
        forwardBackwardComposable<Home> {
            UiSourceScope(uiInspectionSource.element("FoodYouAppNavHost.HomeScreen.1", "com.maksimowiczm.foodyou.app.ui.home.master.HomeScreen", "FoodYouAppNavHost")) { HomeScreen(
                onSettings = { navController.navigateSingleTop(Settings) },
                onTitle = { navController.navigateSingleTop(About) },
                onDiarySlotCardLongClick = { navController.navigateSingleTop(DiarySlotsPersonalization) },
                onDiarySlotCardAddClick = { epochDay, diarySlotId ->
                    navController.navigateSingleTop(FoodDiarySearch(epochDay, diarySlotId))
                },
                onDiarySlotCardQuickAddClick = { epochDay, diarySlotId ->
                    navController.navigateSingleTop(FoodDiaryCreateQuickAdd(epochDay, diarySlotId))
                },
                onDiarySlotCardAiScanClick = { epochDay, diarySlotId ->
                    navController.navigateSingleTop(FoodDiaryAiScan(epochDay, diarySlotId))
                },
                onDiarySlotCardFastTextClick = { epochDay, diarySlotId ->
                    navController.navigateSingleTop(FoodDiaryFastText(epochDay, diarySlotId))
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
            ) }
        }
        forwardBackwardComposable<PersonalLibrary> { entry ->
            val saved by entry.savedStateHandle.getStateFlow("library-saved", false).collectAsStateWithLifecycle()
            UiSourceScope(uiInspectionSource.element("FoodYouAppNavHost.PersonalLibraryScreen", "com.maksimowiczm.foodyou.app.ui.food.library.PersonalLibraryScreen", "FoodYouAppNavHost")) {
                PersonalLibraryScreen(
                    onBack = { navController.popBackStackInclusive<PersonalLibrary>() },
                    onCreateProduct = { navController.navigateSingleTop(LibraryCreateProduct) },
                    onCreateRecipe = { navController.navigateSingleTop(LibraryCreateRecipe) },
                    onEdit = { id -> when(id) {
                        is NutritionalItemId.Product -> navController.navigateSingleTop(LibraryUpdateProduct(id.id))
                        is NutritionalItemId.Recipe -> navController.navigateSingleTop(UpdateRecipe(id.id))
                    } },
                    onUpdateUsdaApiKey = { navController.navigateSingleTop(UsdaApiKey) },
                    onUpdateOpenFoodFactsCredentials = { navController.navigateSingleTop(OpenFoodFactsLogin) },
                    saved = saved, onSavedAcknowledged = { entry.savedStateHandle["library-saved"] = false },
                )
            }
        }
        forwardBackwardComposable<LibraryCreateProduct> {
            UiSourceScope(uiInspectionSource.element("FoodYouAppNavHost.LibraryCreateProduct", "com.maksimowiczm.foodyou.app.ui.food.product.CreateProductScreen", "FoodYouAppNavHost")) {
                CreateProductScreen(personalCollection = true, onBack = { navController.popBackStackInclusive<LibraryCreateProduct>() },
                    onCreate = {
                        navController.previousBackStackEntry?.savedStateHandle?.set("library-saved", true)
                        navController.popBackStackInclusive<LibraryCreateProduct>()
                    },
                    onUpdateUsdaApiKey = { navController.navigateSingleTop(UsdaApiKey) },
                    onUpdateOpenFoodFactsCredentials = { navController.navigateSingleTop(OpenFoodFactsLogin) })
            }
        }
        forwardBackwardComposable<LibraryCreateRecipe> {
            UiSourceScope(uiInspectionSource.element("FoodYouAppNavHost.LibraryCreateRecipe", "com.maksimowiczm.foodyou.app.ui.food.recipe.CreateRecipeScreen", "FoodYouAppNavHost")) {
                CreateRecipeScreen(onBack = { navController.popBackStackInclusive<LibraryCreateRecipe>() },
                    onCreate = {
                        navController.previousBackStackEntry?.savedStateHandle?.set("library-saved", true)
                        navController.popBackStackInclusive<LibraryCreateRecipe>()
                    },
                    onEditFood = { id -> when(id) {
                        is NutritionalItemId.Product -> navController.navigateSingleTop(UpdateProduct(id.id))
                        is NutritionalItemId.Recipe -> navController.navigateSingleTop(UpdateRecipe(id.id))
                    } },
                    onUpdateUsdaApiKey = { navController.navigateSingleTop(UsdaApiKey) },
                    onUpdateOpenFoodFactsCredentials = { navController.navigateSingleTop(OpenFoodFactsLogin) })
            }
        }
        forwardBackwardComposable<LibraryUpdateProduct> { entry ->
            val route = entry.toRoute<LibraryUpdateProduct>()
            UiSourceScope(uiInspectionSource.element("FoodYouAppNavHost.LibraryUpdateProduct", "com.maksimowiczm.foodyou.app.ui.food.product.UpdateProductScreen", "FoodYouAppNavHost")) {
                UpdateProductScreen(onBack = { navController.popBackStackInclusive<LibraryUpdateProduct>() },
                    onUpdate = { navController.popBackStackInclusive<LibraryUpdateProduct>() },
                    productId = NutritionalItemId.Product(route.productId), personalCollection = true)
            }
        }
        forwardBackwardComposable<Settings> {
            UiSourceScope(uiInspectionSource.element("FoodYouAppNavHost.SettingsScreen.1", "com.maksimowiczm.foodyou.app.ui.settings.SettingsScreen", "FoodYouAppNavHost")) { SettingsScreen(
                onBack = { navController.popBackStackInclusive<Settings>() },
                onAbout = { navController.navigateSingleTop(About) },
                onDiarySlots = { navController.navigateSingleTop(DiarySlotSetup) },
                onLanguage = { navController.navigateSingleTop(Language) },
                onGoals = { navController.navigateSingleTop(GoalsSetup) },
                onLibrary = { navController.navigateSingleTop(PersonalLibrary) },
                onPersonalization = { navController.navigateSingleTop(Personalization) },
                onDatabase = { navController.navigateSingleTop(DatabaseSettings) },
                onExternalDatabases = { navController.navigateSingleTop(ExternalDatabases) },
                onAiSettings = { navController.navigateSingleTop(AiSettings) },
                onRelaySettings = { navController.navigateSingleTop(RelaySettings) },
            ) }
        }
        forwardBackwardComposable<AiSettings> {
            UiSourceScope(uiInspectionSource.element("FoodYouAppNavHost.AiSettingsScreen.1", "com.maksimowiczm.foodyou.app.ui.settings.ai.AiSettingsScreen", "FoodYouAppNavHost")) { AiSettingsScreen(onBack = { navController.popBackStackInclusive<AiSettings>() }) }
        }
        forwardBackwardComposable<RelaySettings> {
            UiSourceScope(uiInspectionSource.element("FoodYouAppNavHost.RelaySettingsScreen.1", "com.maksimowiczm.foodyou.app.ui.settings.relay.RelaySettingsScreen", "FoodYouAppNavHost")) { RelaySettingsScreen(onBack = { navController.popBackStackInclusive<RelaySettings>() }) }
        }
        forwardBackwardComposable<Language> {
            UiSourceScope(uiInspectionSource.element("FoodYouAppNavHost.LanguageScreen.1", "com.maksimowiczm.foodyou.app.ui.language.LanguageScreen", "FoodYouAppNavHost")) { LanguageScreen(onBack = { navController.popBackStackInclusive<Language>() }) }
        }
        forwardBackwardComposable<About> {
            UiSourceScope(uiInspectionSource.element("FoodYouAppNavHost.AboutScreen.1", "com.maksimowiczm.foodyou.app.ui.about.AboutScreen", "FoodYouAppNavHost")) { AboutScreen(
                onBack = { navController.popBackStackInclusive<About>() },
                onSponsor = { navController.navigateSingleTop(Sponsor) },
            ) }
        }
        forwardBackwardComposable<Sponsor> {
            UiSourceScope(uiInspectionSource.element("FoodYouAppNavHost.SponsorScreen.1", "com.maksimowiczm.foodyou.app.ui.sponsor.SponsorScreen", "FoodYouAppNavHost")) { SponsorScreen(onBack = { navController.popBackStackInclusive<Sponsor>() }) }
        }
        forwardBackwardComposable<DiarySlotSetup> {
            UiSourceScope(uiInspectionSource.element("FoodYouAppNavHost.DiarySlotSettingsScreen.1", "com.maksimowiczm.foodyou.app.ui.meal.DiarySlotSettingsScreen", "FoodYouAppNavHost")) { DiarySlotSettingsScreen(onBack = { navController.popBackStackInclusive<DiarySlotSetup>() }) }
        }
        forwardBackwardComposable<Goals> {
            val (epochDay) = it.toRoute<Goals>()

            UiSourceScope(uiInspectionSource.element("FoodYouAppNavHost.GoalsScreen.1", "com.maksimowiczm.foodyou.app.ui.goals.master.GoalsScreen", "FoodYouAppNavHost")) { GoalsScreen(
                onBack = { navController.popBackStackInclusive<Goals>() },
                epochDay = epochDay,
            ) }
        }
        forwardBackwardComposable<GoalsSetup> {
            UiSourceScope(uiInspectionSource.element("FoodYouAppNavHost.DailyGoalsScreen.1", "com.maksimowiczm.foodyou.app.ui.goals.setup.DailyGoalsScreen", "FoodYouAppNavHost")) { DailyGoalsScreen(
                onBack = { navController.popBackStackInclusive<GoalsSetup>() },
                onSave = { navController.popBackStackInclusive<GoalsSetup>() },
            ) }
        }
        forwardBackwardComposable<DatabaseSettings> {
            UiSourceScope(uiInspectionSource.element("FoodYouAppNavHost.DatabaseSettingsScreen.1", "com.maksimowiczm.foodyou.app.ui.database.master.DatabaseSettingsScreen", "FoodYouAppNavHost")) { DatabaseSettingsScreen(
                onBack = { navController.popBackStackInclusive<DatabaseSettings>() },
                onImportCsvProducts = { navController.navigateSingleTop(ImportCsvProducts) },
                onImportLibrary = { navController.navigateSingleTop(ImportLibrary) },
                onExportCsvProducts = { navController.navigateSingleTop(ExportCsvProducts) },
                onDatabaseBackup = onDatabaseBackup,
            ) }
        }
        forwardBackwardComposable<ExternalDatabases> {
            UiSourceScope(uiInspectionSource.element("FoodYouAppNavHost.ExternalDatabasesScreen.1", "com.maksimowiczm.foodyou.app.ui.database.externaldatabases.ExternalDatabasesScreen", "FoodYouAppNavHost")) { ExternalDatabasesScreen(
                onBack = { navController.popBackStackInclusive<ExternalDatabases>() },
                onSwissFoodCompositionDatabase = {
                    navController.navigateSingleTop(SwissFoodCompositionDatabase)
                },
                onAustralianFoodCompositionDatabase = {
                    navController.navigateSingleTop(AustralianFoodCompositionDatabase)
                },
            ) }
        }
        forwardBackwardComposable<SwissFoodCompositionDatabase> {
            UiSourceScope(uiInspectionSource.element("FoodYouAppNavHost.SwissFoodCompositionDatabaseScreen.1", "com.maksimowiczm.foodyou.app.ui.database.swissfoodcompositiondatabase.SwissFoodCompositionDatabaseScreen", "FoodYouAppNavHost")) { SwissFoodCompositionDatabaseScreen(
                onBack = { navController.popBackStackInclusive<SwissFoodCompositionDatabase>() }
            ) }
        }
        forwardBackwardComposable<AustralianFoodCompositionDatabase> {
            UiSourceScope(uiInspectionSource.element("FoodYouAppNavHost.AustralianFoodCompositionDatabaseScreen.1", "com.maksimowiczm.foodyou.app.ui.database.australianfoodcompositiondatabase.AustralianFoodCompositionDatabaseScreen", "FoodYouAppNavHost")) { AustralianFoodCompositionDatabaseScreen(
                onBack = {
                    navController.popBackStackInclusive<AustralianFoodCompositionDatabase>()
                }
            ) }
        }
        forwardBackwardComposable<ImportLibrary> {
            UiSourceScope(uiInspectionSource.element("FoodYouAppNavHost.ImportLibraryScreen.1", "com.maksimowiczm.foodyou.app.ui.database.importlibrary.ImportLibraryScreen", "FoodYouAppNavHost")) { ImportLibraryScreen(onBack = { navController.popBackStackInclusive<ImportLibrary>() },
                onFinish = { navController.popBackStackInclusive<ImportLibrary>() }) }
        }

        forwardBackwardComposable<ImportCsvProducts> {
            UiSourceScope(uiInspectionSource.element("FoodYouAppNavHost.ImportCsvProductsScreen.1", "com.maksimowiczm.foodyou.app.ui.database.importcsvproducts.ImportCsvProductsScreen", "FoodYouAppNavHost")) { ImportCsvProductsScreen(
                onBack = { navController.popBackStackInclusive<ImportCsvProducts>() },
                onFinish = { navController.popBackStackInclusive<ImportCsvProducts>() },
            ) }
        }
        forwardBackwardComposable<ExportCsvProducts> {
            UiSourceScope(uiInspectionSource.element("FoodYouAppNavHost.ExportCsvProductsScreen.1", "com.maksimowiczm.foodyou.app.ui.database.exportcsvproducts.ExportCsvProductsScreen", "FoodYouAppNavHost")) { ExportCsvProductsScreen(
                onBack = { navController.popBackStackInclusive<ExportCsvProducts>() },
                onFinish = { navController.popBackStackInclusive<ExportCsvProducts>() },
            ) }
        }
        dialog<UsdaApiKey> {
            UpdateUsdaApiKeyDialog(
                onDismissRequest = { navController.popBackStackInclusive<UsdaApiKey>() },
                onSave = { navController.popBackStackInclusive<UsdaApiKey>() }, modifier = Modifier.uiName("FoodYouAppNavHost.UpdateUsdaApiKeyDialog.1", "com.maksimowiczm.foodyou.app.ui.database.externaldatabases.UpdateUsdaApiKeyDialog", uiInspectionSource, "FoodYouAppNavHost")
            )
        }

        dialog<OpenFoodFactsLogin> {
            OpenFoodFactsLoginDialog(
                onDismissRequest = { navController.popBackStackInclusive<OpenFoodFactsLogin>() },
                onSave = { navController.popBackStackInclusive<OpenFoodFactsLogin>() }, modifier = Modifier.uiName("FoodYouAppNavHost.OpenFoodFactsLoginDialog.1", "com.maksimowiczm.foodyou.app.ui.database.externaldatabases.OpenFoodFactsLoginDialog", uiInspectionSource, "FoodYouAppNavHost")
            )
        }
        forwardBackwardComposable<FoodDiaryCreateQuickAdd> {
            val route = it.toRoute<FoodDiaryCreateQuickAdd>()

            UiSourceScope(uiInspectionSource.element("FoodYouAppNavHost.CreateQuickAddScreen.1", "com.maksimowiczm.foodyou.app.ui.food.diary.quickadd.CreateQuickAddScreen", "FoodYouAppNavHost")) { CreateQuickAddScreen(
                onBack = { navController.popBackStackInclusive<FoodDiaryCreateQuickAdd>() },
                onSave = { ScanQuickAddHandoff.saved(navController, route) },
                date = LocalDate.fromEpochDays(route.epochDay),
                diarySlotId = route.diarySlotId,
                onPromoteToProduct = { navController.navigate(PromoteToProduct(it)) },
                onPromoteToRecipe = { navController.navigate(PromoteToRecipe(it)) },
                prefillName = route.prefillName,
                prefillEnergyKcal = route.prefillEnergyKcal,
                prefillProteins = route.prefillProteins,
                prefillCarbohydrates = route.prefillCarbohydrates,
                prefillFats = route.prefillFats,
                scanEstimate = route.scanEstimateJson?.let(AiFoodEstimateParser::parse),
            ) }
        }
        forwardBackwardComposable<FoodDiaryAiScan> {
            val (epochDay, diarySlotId) = it.toRoute<FoodDiaryAiScan>()
            val scannerState = it.savedStateHandle
            val savedItemId by scannerState.getStateFlow<String?>(ScanQuickAddHandoff.SAVED_ITEM_KEY, null)
                .collectAsStateWithLifecycle()

            UiSourceScope(uiInspectionSource.element("FoodYouAppNavHost.AiScanScreen.1", "com.maksimowiczm.foodyou.app.ui.food.diary.aiscan.AiScanScreen", "FoodYouAppNavHost")) { AiScanScreen(
                onBack = { navController.popBackStackInclusive<FoodDiaryAiScan>() },
                savedScanItemId = savedItemId,
                onSavedScanItemConsumed = { scannerState[ScanQuickAddHandoff.SAVED_ITEM_KEY] = null },
                onScanCompleted = { ScanQuickAddHandoff.complete(navController) },
                onSaveToQuickAdd = { estimate, itemId ->
                    ScanQuickAddHandoff.open(navController,
                        ScanQuickAddHandoff.route(
                            epochDay = epochDay,
                            diarySlotId = diarySlotId,
                            estimate = estimate,
                            itemId = itemId,
                        )
                    )
                },
                onAlignPickFood = { foodId ->
                    navController.navigate(
                        FoodDiaryCreateEntry(
                            date = epochDay,
                            diarySlotId = diarySlotId,
                            foodId = foodId,
                            measurement = null,
                        )
                    )
                },
                onCreateCustomFood = {
                    navController.navigateSingleTop(FoodDiaryCreateProduct(epochDay, diarySlotId))
                },
            ) }
        }
        forwardBackwardComposable<FoodDiaryFastText> {
            val (epochDay, diarySlotId) = it.toRoute<FoodDiaryFastText>()

            UiSourceScope(uiInspectionSource.element("FoodYouAppNavHost.FastTextScreen.1", "com.maksimowiczm.foodyou.app.ui.food.diary.fasttext.FastTextScreen", "FoodYouAppNavHost")) { FastTextScreen(
                date = LocalDate.fromEpochDays(epochDay),
                diarySlotId = diarySlotId,
                onBack = { navController.popBackStackInclusive<FoodDiaryFastText>() },
                onSave = { navController.popBackStackInclusive<FoodDiaryFastText>() },
            ) }
        }
        forwardBackwardComposable<FoodDiaryPlaceholderMeta> {
            val (manualEntryId) = it.toRoute<FoodDiaryPlaceholderMeta>()

            UiSourceScope(uiInspectionSource.element("FoodYouAppNavHost.PlaceholderMetaScreen.1", "com.maksimowiczm.foodyou.app.ui.food.diary.placeholder.PlaceholderMetaScreen", "FoodYouAppNavHost")) { PlaceholderMetaScreen(
                manualEntryId = manualEntryId,
                onBack = { navController.popBackStackInclusive<FoodDiaryPlaceholderMeta>() },
                onSearch = { epochDay, diarySlotId ->
                    navController.navigate(FoodDiarySearch(date = epochDay, diarySlotId = diarySlotId))
                },
                onQuickAdd = { epochDay, diarySlotId, name ->
                    navController.navigate(
                        FoodDiaryCreateQuickAdd(
                            epochDay = epochDay,
                            diarySlotId = diarySlotId,
                            prefillName = name,
                        )
                    )
                },
                onAiQuery = { epochDay, diarySlotId, query ->
                    navController.navigate(
                        FoodDiarySearch(date = epochDay, diarySlotId = diarySlotId, initialQuery = query)
                    )
                },
                onRemoved = { navController.popBackStackInclusive<FoodDiaryPlaceholderMeta>() },
            ) }
        }
        forwardBackwardComposable<UpdateQuickAdd> {
            val (quickAddId) = it.toRoute<UpdateQuickAdd>()

            UiSourceScope(uiInspectionSource.element("FoodYouAppNavHost.UpdateQuickAddScreen.1", "com.maksimowiczm.foodyou.app.ui.food.diary.quickadd.UpdateQuickAddScreen", "FoodYouAppNavHost")) { UpdateQuickAddScreen(
                onBack = { navController.popBackStackInclusive<UpdateQuickAdd>() },
                onSave = { navController.popBackStackInclusive<UpdateQuickAdd>() },
                id = quickAddId,
                onPromoteToProduct = { navController.navigate(PromoteToProduct(it)) },
                onPromoteToRecipe = { navController.navigate(PromoteToRecipe(it)) },
            ) }
        }
        forwardBackwardComposable<FoodDiarySearch> {
            val route = it.toRoute<FoodDiarySearch>()
            val date = route.date
            val diarySlotId = route.diarySlotId

            UiSourceScope(uiInspectionSource.element("FoodYouAppNavHost.DiaryFoodSearchScreen.1", "com.maksimowiczm.foodyou.app.ui.food.diary.search.DiaryFoodSearchScreen", "FoodYouAppNavHost")) { DiaryFoodSearchScreen(
                initialQuery = route.initialQuery,
                onBack = { navController.popBackStackInclusive<FoodDiarySearch>() },
                onCreateRecipe = {
                    navController.navigateSingleTop(FoodDiaryCreateRecipe(date, diarySlotId))
                },
                onCreateProduct = {
                    navController.navigateSingleTop(FoodDiaryCreateProduct(date, diarySlotId))
                },
                onMeasure = { foodId, measurement ->
                    navController.navigate(
                        FoodDiaryCreateEntry(
                            date = date,
                            diarySlotId = diarySlotId,
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
                diarySlotId = diarySlotId,
                animatedVisibilityScope = this,
            ) }
        }
        forwardBackwardComposable<UpdateRecipe> {
            val (recipeId) = it.toRoute<UpdateRecipe>()

            UiSourceScope(uiInspectionSource.element("FoodYouAppNavHost.UpdateRecipeScreen.1", "com.maksimowiczm.foodyou.app.ui.food.recipe.UpdateRecipeScreen", "FoodYouAppNavHost")) { UpdateRecipeScreen(
                onBack = { navController.popBackStackInclusive<UpdateRecipe>() },
                onEditFood = { id ->
                    when (id) {
                        is NutritionalItemId.Product -> navController.navigateSingleTop(UpdateProduct(id.id))
                        is NutritionalItemId.Recipe -> error("Cannot edit recipe from recipe")
                    }
                },
                onUpdate = { navController.popBackStackInclusive<UpdateRecipe>() },
                onUpdateUsdaApiKey = { navController.navigateSingleTop(UsdaApiKey) },
                onUpdateOpenFoodFactsCredentials = {
                    navController.navigateSingleTop(OpenFoodFactsLogin)
                },
                recipeId = NutritionalItemId.Recipe(recipeId),
            ) }
        }
        forwardBackwardComposable<UpdateProduct> {
            val (productId) = it.toRoute<UpdateProduct>()

            UiSourceScope(uiInspectionSource.element("FoodYouAppNavHost.UpdateProductScreen.1", "com.maksimowiczm.foodyou.app.ui.food.product.UpdateProductScreen", "FoodYouAppNavHost")) { UpdateProductScreen(
                onBack = { navController.popBackStackInclusive<UpdateProduct>() },
                onUpdate = { navController.popBackStackInclusive<UpdateProduct>() },
                productId = NutritionalItemId.Product(productId),
            ) }
        }
        forwardBackwardComposable<FoodDiaryCreateRecipe> {
            val (date, diarySlotId) = it.toRoute<FoodDiaryCreateRecipe>()

            UiSourceScope(uiInspectionSource.element("FoodYouAppNavHost.CreateRecipeScreen.1", "com.maksimowiczm.foodyou.app.ui.food.recipe.CreateRecipeScreen", "FoodYouAppNavHost")) { CreateRecipeScreen(
                onBack = { navController.popBackStackInclusive<FoodDiaryCreateRecipe>() },
                onCreate = { id ->
                    navController.navigate(
                        FoodDiaryCreateEntry(
                            date = date,
                            diarySlotId = diarySlotId,
                            foodId = id,
                            measurement = null,
                        )
                    ) {
                        popUpTo<FoodDiaryCreateRecipe> { inclusive = true }
                    }
                },
                onEditFood = { id ->
                    when (id) {
                        is NutritionalItemId.Product -> navController.navigateSingleTop(UpdateProduct(id.id))
                        is NutritionalItemId.Recipe -> error("Cannot edit recipe from recipe")
                    }
                },
                onUpdateUsdaApiKey = { navController.navigateSingleTop(UsdaApiKey) },
                onUpdateOpenFoodFactsCredentials = {
                    navController.navigateSingleTop(OpenFoodFactsLogin)
                },
            ) }
        }
        forwardBackwardComposable<FoodDiaryCreateProduct> {
            val (date, diarySlotId) = it.toRoute<FoodDiaryCreateProduct>()

            UiSourceScope(uiInspectionSource.element("FoodYouAppNavHost.CreateProductScreen.1", "com.maksimowiczm.foodyou.app.ui.food.product.CreateProductScreen", "FoodYouAppNavHost")) { CreateProductScreen(
                onBack = { navController.popBackStackInclusive<FoodDiaryCreateProduct>() },
                onCreate = { id ->
                    navController.navigate(
                        FoodDiaryCreateEntry(
                            date = date,
                            diarySlotId = diarySlotId,
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
            ) }
        }
        forwardBackwardComposable<PromoteToProduct> {
            // Story 19: open the product editor prefilled from a Quick Add estimate. The product is
            // created only when the editor is saved; cancelling creates nothing (spec §6.2/§6.6).
            val seed = it.toRoute<PromoteToProduct>().toSeed()

            UiSourceScope(uiInspectionSource.element("FoodYouAppNavHost.CreateProductScreen.2", "com.maksimowiczm.foodyou.app.ui.food.product.CreateProductScreen", "FoodYouAppNavHost")) { CreateProductScreen(
                onBack = { navController.popBackStackInclusive<PromoteToProduct>() },
                onCreate = { navController.popBackStackInclusive<PromoteToProduct>() },
                onUpdateUsdaApiKey = { navController.navigateSingleTop(UsdaApiKey) },
                onUpdateOpenFoodFactsCredentials = {
                    navController.navigateSingleTop(OpenFoodFactsLogin)
                },
                prefillProduct = seed.toProductPrefill(),
            ) }
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
                UiSourceScope(uiInspectionSource.element("FoodYouAppNavHost.CreateRecipeScreen.2", "com.maksimowiczm.foodyou.app.ui.food.recipe.CreateRecipeScreen", "FoodYouAppNavHost")) { CreateRecipeScreen(
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
                            is NutritionalItemId.Product ->
                                navController.navigateSingleTop(UpdateProduct(food.id))
                            is NutritionalItemId.Recipe -> error("Cannot edit recipe from recipe")
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
                        NutritionalItemId.Product(id) to Measurement.Gram(seed.recipeIngredientWeightGrams),
                ) }
            }
        }
        forwardBackwardComposable<FoodDiaryCreateEntry> {
            val route = it.toRoute<FoodDiaryCreateEntry>()

            UiSourceScope(uiInspectionSource.element("FoodYouAppNavHost.AddEntryScreen.1", "com.maksimowiczm.foodyou.app.ui.food.diary.add.AddEntryScreen", "FoodYouAppNavHost")) { AddEntryScreen(
                onBack = { navController.popBackStackInclusive<FoodDiaryCreateEntry>() },
                onEditFood = { id ->
                    when (id) {
                        is NutritionalItemId.Product -> navController.navigateSingleTop(UpdateProduct(id.id))
                        is NutritionalItemId.Recipe -> navController.navigateSingleTop(UpdateRecipe(id.id))
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
                            diarySlotId = route.diarySlotId,
                            foodId = foodId,
                            measurement = measurement,
                        )
                    )
                },
                foodId = route.foodId,
                diarySlotId = route.diarySlotId,
                date = LocalDate.fromEpochDays(route.date),
                measurement = route.measurement,
                animatedVisibilityScope = this,
            ) }
        }
        forwardBackwardComposable<FoodDiaryUpdateEntry> {
            val (foodEntryId) = it.toRoute<FoodDiaryUpdateEntry>()

            UiSourceScope(uiInspectionSource.element("FoodYouAppNavHost.UpdateEntryScreen.1", "com.maksimowiczm.foodyou.app.ui.food.diary.update.UpdateEntryScreen", "FoodYouAppNavHost")) { UpdateEntryScreen(
                entryId = foodEntryId,
                onBack = { navController.popBackStackInclusive<FoodDiaryUpdateEntry>() },
                onSave = { navController.popBackStackInclusive<FoodDiaryUpdateEntry>() },
                animatedVisibilityScope = this,
            ) }
        }
        forwardBackwardComposable<Personalization> {
            UiSourceScope(uiInspectionSource.element("FoodYouAppNavHost.PersonalizationScreen.1", "com.maksimowiczm.foodyou.app.ui.personalization.PersonalizationScreen", "FoodYouAppNavHost")) { PersonalizationScreen(
                onBack = { navController.popBackStackInclusive<Personalization>() },
                onTheme = { navController.navigateSingleTop(ThemeSettings) },
                onHomePersonalization = { navController.navigateSingleTop(HomePersonalization) },
                onNutritionFactsPersonalization = {
                    navController.navigateSingleTop(NutritionFactsPersonalization)
                },
            ) }
        }
        forwardBackwardComposable<ThemeSettings> {
            UiSourceScope(uiInspectionSource.element("FoodYouAppNavHost.ThemeScreen.1", "com.maksimowiczm.foodyou.app.ui.theme.ThemeScreen", "FoodYouAppNavHost")) { ThemeScreen(onBack = { navController.popBackStackInclusive<ThemeSettings>() }) }
        }
        forwardBackwardComposable<HomePersonalization> {
            UiSourceScope(uiInspectionSource.element("FoodYouAppNavHost.HomePersonalizationScreen.1", "com.maksimowiczm.foodyou.app.ui.home.personalization.HomePersonalizationScreen", "FoodYouAppNavHost")) { HomePersonalizationScreen(
                onBack = { navController.popBackStackInclusive<HomePersonalization>() },
                onDiarySlots = { navController.navigateSingleTop(DiarySlotsPersonalization) },
                onGoals = { navController.navigateSingleTop(GoalsPersonalization) },
            ) }
        }
        forwardBackwardComposable<NutritionFactsPersonalization> {
            UiSourceScope(uiInspectionSource.element("FoodYouAppNavHost.PersonalizeNutritionFactsScreen.1", "com.maksimowiczm.foodyou.app.ui.personalization.PersonalizeNutritionFactsScreen", "FoodYouAppNavHost")) { PersonalizeNutritionFactsScreen(
                onBack = { navController.popBackStackInclusive<NutritionFactsPersonalization>() }
            ) }
        }
        forwardBackwardComposable<DiarySlotsPersonalization> {
            UiSourceScope(uiInspectionSource.element("FoodYouAppNavHost.DiarySlotsCardsSettingsScreen.1", "com.maksimowiczm.foodyou.app.ui.home.meals.settings.DiarySlotsCardsSettingsScreen", "FoodYouAppNavHost")) { DiarySlotsCardsSettingsScreen(
                onBack = { navController.popBackStackInclusive<DiarySlotsPersonalization>() },
                onDiarySlotSettings = { navController.navigateSingleTop(DiarySlotSetup) },
            ) }
        }
        forwardBackwardComposable<GoalsPersonalization> {
            GoalsCardSettings(
                onBack = { navController.popBackStackInclusive<GoalsPersonalization>() },
                onGoalsSettings = { navController.navigateSingleTop(GoalsSetup) }, modifier = Modifier.uiName("FoodYouAppNavHost.GoalsCardSettings.1", "com.maksimowiczm.foodyou.app.ui.home.goals.GoalsCardSettings", uiInspectionSource, "FoodYouAppNavHost")
            )
        }
    }
}

@Serializable internal object Home

@Serializable private object PersonalLibrary
@Serializable private data class LibraryUpdateProduct(val productId: Long)
@Serializable private object LibraryCreateProduct
@Serializable private object LibraryCreateRecipe

@Serializable private object Settings

@Serializable private object About

@Serializable private object Language

@Serializable private object ThemeSettings

@Serializable private object Sponsor

@Serializable
@SerialName("com.maksimowiczm.foodyou.app.navigation.MealSetup")
private object DiarySlotSetup

@Serializable private data class Goals(val epochDay: Long)

@Serializable private object GoalsSetup

@Serializable private object AiSettings

@Serializable private object RelaySettings

@Serializable private object DatabaseSettings

@Serializable private object ExternalDatabases

@Serializable private object SwissFoodCompositionDatabase

@Serializable private object AustralianFoodCompositionDatabase

@Serializable private object UsdaApiKey

@Serializable private object OpenFoodFactsLogin

@Serializable private object ImportLibrary

@Serializable private object ImportCsvProducts

@Serializable private object ExportCsvProducts

@Serializable
internal data class FoodDiaryCreateQuickAdd(
    val epochDay: Long,
    @SerialName("mealId") val diarySlotId: Long,
    // Optional AI-scan prefill (Milestone 2, Story 6). Energy is in kilocalories.
    val prefillName: String? = null,
    val prefillEnergyKcal: Double? = null,
    val prefillProteins: Double? = null,
    val prefillCarbohydrates: Double? = null,
    val prefillFats: Double? = null,
    val scanEstimateJson: String? = null,
    val scanItemId: String? = null,
) {
    constructor(epochDay: Long, diarySlotId: Long, estimate: AiFoodEstimate) : this(
        epochDay = epochDay,
        diarySlotId = diarySlotId,
        scanEstimateJson = Json.encodeToString(estimate),
    )
}

@Serializable private data class FoodDiaryAiScan(val epochDay: Long, @SerialName("mealId") val diarySlotId: Long)

@Serializable private data class FoodDiaryFastText(val epochDay: Long, @SerialName("mealId") val diarySlotId: Long)

@Serializable private data class FoodDiaryPlaceholderMeta(val manualEntryId: Long)

@Serializable private data class UpdateQuickAdd(val quickAddId: Long)

@Serializable
private data class FoodDiarySearch(
    val date: Long,
    @SerialName("mealId") val diarySlotId: Long,
    // Optional pre-executed query, used by the placeholder AI route (Milestone 2, Story 9).
    val initialQuery: String? = null,
)

@Serializable private data class FoodDiaryCreateProduct(val date: Long, @SerialName("mealId") val diarySlotId: Long)

// Story 19 promotion routes. Carry a flat copy of the mapped Quick Add fields (spec §6.2/§6.3); the
// source diary entry is never referenced or mutated (§6.5).
@Serializable
internal data class PromoteToProduct(
    val name: String,
    val description: String?,
    val energyKcal: Double?,
    val proteins: Double?,
    val carbohydrates: Double?,
    val fats: Double?,
    val fibre: Double?,
    val servingCount: Double?,
    val weightGrams: Double?,
    val nutritionFactsJson: String? = null,
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
        nutritionFactsJson = seed.nutritionFacts?.let(QuickAddNutritionSnapshot::encode),
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
            nutritionFacts = nutritionFactsJson?.let(QuickAddNutritionSnapshot::decode),
        )
}

@Serializable
internal data class PromoteToRecipe(
    val name: String,
    val description: String?,
    val energyKcal: Double?,
    val proteins: Double?,
    val carbohydrates: Double?,
    val fats: Double?,
    val fibre: Double?,
    val servingCount: Double?,
    val weightGrams: Double?,
    val nutritionFactsJson: String? = null,
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
        nutritionFactsJson = seed.nutritionFacts?.let(QuickAddNutritionSnapshot::encode),
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
            nutritionFacts = nutritionFactsJson?.let(QuickAddNutritionSnapshot::decode),
        )
}

@Serializable private data class FoodDiaryCreateRecipe(val date: Long, @SerialName("mealId") val diarySlotId: Long)

@Serializable private data class UpdateRecipe(val recipeId: Long)

@Serializable private data class UpdateProduct(val productId: Long)

@Serializable
private class FoodDiaryCreateEntry(
    val date: Long,
    @SerialName("mealId") val diarySlotId: Long,
    private val productId: Long?,
    private val recipeId: Long?,
    private val measurementType: MeasurementType?,
    private val measurementValue: Double?,
) {
    constructor(
        date: Long,
        diarySlotId: Long,
        foodId: NutritionalItemId,
        measurement: Measurement?,
    ) : this(
        date = date,
        diarySlotId = diarySlotId,
        productId = if (foodId is NutritionalItemId.Product) foodId.id else null,
        recipeId = if (foodId is NutritionalItemId.Recipe) foodId.id else null,
        measurementType = measurement?.type,
        measurementValue = measurement?.rawValue,
    )

    init {
        require((productId != null) xor (recipeId != null)) {
            "Exactly one productId or recipeId must be non-null"
        }
    }

    val foodId: NutritionalItemId
        get() =
            NutritionalItemId.fromReferences(productId, recipeId)

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

@Serializable
@SerialName("com.maksimowiczm.foodyou.app.navigation.MealsPersonalization")
private object DiarySlotsPersonalization

@Serializable private object GoalsPersonalization


private val uiInspectionSource = UiSourceFile(
    "com.maksimowiczm.foodyou.app.navigation",
    "app/src/commonMain/kotlin/com/maksimowiczm/foodyou/app/navigation/FoodYouAppNavHost.kt",
)
