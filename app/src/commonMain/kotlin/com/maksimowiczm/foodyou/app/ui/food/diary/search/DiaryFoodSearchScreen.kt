package com.maksimowiczm.foodyou.app.ui.food.diary.search

import com.maksimowiczm.foodyou.app.ui.developer.inspection.UiSourceFile
import com.maksimowiczm.foodyou.app.ui.developer.inspection.uiName

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.animateFloatingActionButton
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationEventHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import com.maksimowiczm.foodyou.app.ui.common.component.ArrowBackIconButton
import com.maksimowiczm.foodyou.app.ui.food.search.FoodSearchApp
import com.maksimowiczm.foodyou.common.compose.component.Scrim
import com.maksimowiczm.foodyou.common.compose.extension.LaunchedCollectWithLifecycle
import com.maksimowiczm.foodyou.common.compose.extension.toDp
import com.maksimowiczm.foodyou.common.compose.utility.LocalDateFormatter
import com.maksimowiczm.foodyou.common.domain.measurement.Measurement
import com.maksimowiczm.foodyou.food.domain.entity.NutritionalItemId
import com.valentinilk.shimmer.shimmer
import foodyou.app.generated.resources.*
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun DiaryFoodSearchScreen(
    onBack: () -> Unit,
    onCreateRecipe: () -> Unit,
    onCreateProduct: () -> Unit,
    onMeasure: (NutritionalItemId, Measurement) -> Unit,
    onUpdateUsdaApiKey: () -> Unit,
    onUpdateOpenFoodFactsCredentials: () -> Unit,
    date: LocalDate,
    diarySlotId: Long,
    animatedVisibilityScope: AnimatedVisibilityScope,
    modifier: Modifier = Modifier,
    initialQuery: String? = null,
) {
    val dateFormatter = LocalDateFormatter.current

    val viewModel: DiaryFoodSearchViewModel = koinViewModel { parametersOf(diarySlotId) }
    val diarySlot = viewModel.diarySlot.collectAsStateWithLifecycle().value

    val snackBarHostState = remember { SnackbarHostState() }
    val message = stringResource(Res.string.neutral_measurement_added)

    LaunchedCollectWithLifecycle(viewModel.newEntryEvents) {
        snackBarHostState.showSnackbar(message)
    }

    var fabExpanded by rememberSaveable { mutableStateOf(false) }
    NavigationEventHandler(
        state = rememberNavigationEventState(NavigationEventInfo.None),
        isBackEnabled = fabExpanded,
        onBackCompleted = { fabExpanded = false },
    )

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    val topBar =
        @Composable {
            TopAppBar(
                title = {
                    updateTransition(diarySlot).Crossfade(contentKey = { it?.toString() }) {
                        if (diarySlot == null) {
                            Spacer(
                                modifier =
                                    Modifier.height(LocalTextStyle.current.toDp() - 4.dp)
                                        .width(100.dp)
                                        .padding(bottom = 4.dp)
                                        .shimmer()
                                        .clip(MaterialTheme.shapes.medium)
                                        .background(
                                            MaterialTheme.colorScheme.surfaceContainerHighest
                                        )
                            )
                        } else {
                            Text(diarySlot.name, modifier = Modifier.uiName("DiaryFoodSearchScreen.Text.1", "androidx.compose.material3.Text", uiInspectionSource, "DiaryFoodSearchScreen"))
                        }
                    }
                },
                subtitle = { Text(dateFormatter.formatDate(date), modifier = Modifier.uiName("DiaryFoodSearchScreen.Text.2", "androidx.compose.material3.Text", uiInspectionSource, "DiaryFoodSearchScreen")) },
                titleHorizontalAlignment = Alignment.CenterHorizontally,
                navigationIcon = { ArrowBackIconButton(onBack, modifier = Modifier.uiName("DiaryFoodSearchScreen.ArrowBackIconButton.1", "com.maksimowiczm.foodyou.app.ui.common.component.ArrowBackIconButton", uiInspectionSource, "DiaryFoodSearchScreen")) },
                scrollBehavior = scrollBehavior, modifier = Modifier.uiName("DiaryFoodSearchScreen.TopAppBar.1", "androidx.compose.material3.TopAppBar", uiInspectionSource, "DiaryFoodSearchScreen")
            )
        }
    val content: @Composable (PaddingValues) -> Unit =
        @Composable { paddingValues ->
            FoodSearchApp(
                onFoodClick = { model, measurement -> onMeasure(model.id, measurement) },
                onUpdateUsdaApiKey = onUpdateUsdaApiKey,
                onUpdateOpenFoodFactsCredentials = onUpdateOpenFoodFactsCredentials,
                initialQuery = initialQuery,
                modifier =
                    (Modifier.padding(paddingValues)
                        .consumeWindowInsets(paddingValues)
                        .nestedScroll(scrollBehavior.nestedScrollConnection)).uiName("DiaryFoodSearchScreen.FoodSearchApp.1", "com.maksimowiczm.foodyou.app.ui.food.search.FoodSearchApp", uiInspectionSource, "DiaryFoodSearchScreen"),
            )
        }

    Box((modifier).uiName("DiaryFoodSearchScreen.Box.1", "androidx.compose.foundation.layout.Box", uiInspectionSource, "DiaryFoodSearchScreen")) {
        val fabInsets =
            WindowInsets.systemBars
                .only(WindowInsetsSides.Bottom + WindowInsetsSides.Horizontal)
                .add(WindowInsets.displayCutout.only(WindowInsetsSides.Horizontal))

        FoodDiarySearchFloatingActionButton(
            fabExpanded = fabExpanded,
            onFabExpandedChange = { fabExpanded = it },
            onCreateRecipe = onCreateRecipe,
            onCreateProduct = onCreateProduct,
            modifier =
                (Modifier.zIndex(100f)
                    .align(Alignment.BottomEnd)
                    .windowInsetsPadding(fabInsets)
                    .consumeWindowInsets(fabInsets)
                    .animateFloatingActionButton(
                        visible = !animatedVisibilityScope.transition.isRunning,
                        alignment = Alignment.BottomEnd,
                    )).uiName("DiaryFoodSearchScreen.FoodDiarySearchFloatingActionButton.1", "com.maksimowiczm.foodyou.app.ui.food.diary.search.FoodDiarySearchFloatingActionButton", uiInspectionSource, "DiaryFoodSearchScreen"),
        )
        Scrim(
            visible = fabExpanded,
            onDismiss = { fabExpanded = false },
            modifier = (Modifier.fillMaxSize().zIndex(10f)).uiName("DiaryFoodSearchScreen.Scrim.1", "com.maksimowiczm.foodyou.common.compose.component.Scrim", uiInspectionSource, "DiaryFoodSearchScreen"),
        )
        Scaffold(
            topBar = topBar,
            contentWindowInsets = ScaffoldDefaults.contentWindowInsets.only(WindowInsetsSides.Top),
            floatingActionButton = {
                Box(modifier = (Modifier.windowInsetsPadding(fabInsets).height(56.dp)).uiName("DiaryFoodSearchScreen.Box.2", "androidx.compose.foundation.layout.Box", uiInspectionSource, "DiaryFoodSearchScreen"))
            },
            snackbarHost = { SnackbarHost(snackBarHostState, modifier = Modifier.uiName("DiaryFoodSearchScreen.SnackbarHost.1", "androidx.compose.material3.SnackbarHost", uiInspectionSource, "DiaryFoodSearchScreen")) },
            content = content, modifier = Modifier.uiName("DiaryFoodSearchScreen.Scaffold.1", "androidx.compose.material3.Scaffold", uiInspectionSource, "DiaryFoodSearchScreen")
        )
    }
}


private val uiInspectionSource = UiSourceFile(
    "com.maksimowiczm.foodyou.app.ui.food.diary.search",
    "app/src/commonMain/kotlin/com/maksimowiczm/foodyou/app/ui/food/diary/search/DiaryFoodSearchScreen.kt",
)
