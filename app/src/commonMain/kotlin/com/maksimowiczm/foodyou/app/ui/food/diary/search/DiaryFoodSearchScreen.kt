package com.maksimowiczm.foodyou.app.ui.food.diary.search

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationEventHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import com.maksimowiczm.foodyou.app.ui.common.component.ArrowBackIconButton
import com.maksimowiczm.foodyou.app.ui.developer.inspection.*
import com.maksimowiczm.foodyou.app.ui.food.diary.aiscan.AiScanScreen
import com.maksimowiczm.foodyou.app.ui.food.search.FoodSearchApp
import com.maksimowiczm.foodyou.app.ui.food.search.rememberFoodSearchAppState
import com.maksimowiczm.foodyou.common.compose.utility.LocalDateFormatter
import com.maksimowiczm.foodyou.common.domain.date.DateProvider
import com.maksimowiczm.foodyou.common.domain.measurement.Measurement
import com.maksimowiczm.foodyou.food.domain.entity.NutritionalItemId
import com.maksimowiczm.foodyou.fooddiary.domain.entity.selectDiarySlot
import com.maksimowiczm.foodyou.ai.domain.AiFoodEstimate
import foodyou.app.generated.resources.*
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun DiaryFoodSearchScreen(
    onBack: () -> Unit,
    onCreateRecipe: (Long) -> Unit,
    onCreateProduct: (Long) -> Unit,
    onMeasure: (Long, NutritionalItemId, Measurement) -> Unit,
    onUpdateUsdaApiKey: () -> Unit,
    onUpdateOpenFoodFactsCredentials: () -> Unit,
    date: LocalDate,
    diarySlotId: Long,
    animatedVisibilityScope: AnimatedVisibilityScope,
    modifier: Modifier = Modifier,
    initialQuery: String? = null,
    initialAi: Boolean = false,
    initialSlotTime: String? = null,
    onMeals: () -> Unit = {},
    onSetupSlots: () -> Unit = {},
    onSaveToQuickAdd: (Long, AiFoodEstimate, String) -> Unit = { _, _, _ -> },
    onAlignPickFood: (Long, NutritionalItemId) -> Unit = { _, _ -> },
    savedScanItemId: String? = null,
    onSavedScanItemConsumed: () -> Unit = {},
    onScanCompleted: () -> Unit = {},
    onScanQueueStarted: (Long) -> Unit = {},
    onScanQueueProgress: (Int, Int) -> Unit = { _, _ -> },
    onDateTimeChanged: (LocalDate, LocalTime) -> Unit = { _, _ -> },
) {
    val viewModel: DiaryFoodSearchViewModel = koinViewModel { parametersOf(diarySlotId) }
    val slots by viewModel.diarySlots.collectAsStateWithLifecycle()
    val clock: DateProvider = koinInject()
    var selectedSlotId by rememberSaveable { mutableLongStateOf(diarySlotId) }
    var autoSelected by rememberSaveable { mutableStateOf(diarySlotId >= 0) }
    var aiTab by rememberSaveable { mutableStateOf(initialAi) }
    var menuExpanded by rememberSaveable { mutableStateOf(false) }
    var slotsExpanded by remember { mutableStateOf(false) }
    var dateTimePicker by rememberSaveable { mutableStateOf(false) }
    var selectedTimeText by rememberSaveable {
        mutableStateOf((initialSlotTime?.let { runCatching { LocalTime.parse(it) }.getOrNull() } ?: clock.now().time).let { LocalTime(it.hour, it.minute) }.toString())
    }
    if (dateTimePicker) FoodEntryDateTimePicker(date, LocalTime.parse(selectedTimeText),
        onDismiss = { dateTimePicker = false },
        onConfirm = { selectedDate, selectedTime ->
            val timeChanged = selectedTime != LocalTime.parse(selectedTimeText)
            selectedTimeText = selectedTime.toString()
            if (timeChanged) {
                selectedSlotId = selectDiarySlot(slots.orEmpty(), selectedTime)?.id ?: -1L
                autoSelected = true
                slotsExpanded = selectedSlotId < 0 && !slots.isNullOrEmpty()
            }
            onDateTimeChanged(selectedDate, selectedTime)
            dateTimePicker = false
        }, modifier = Modifier.uiName("DiaryFoodSearchScreen.FoodEntryDateTimePicker.1", "com.maksimowiczm.foodyou.app.ui.food.diary.search.FoodEntryDateTimePicker", uiInspectionSource, "DiaryFoodSearchScreen"))
    val snackbars = remember { SnackbarHostState() }
    val addedMessage = stringResource(Res.string.neutral_measurement_added)
    val dateTimeLabel = stringResource(Res.string.food_entry_date_time)
    com.maksimowiczm.foodyou.common.compose.extension.LaunchedCollectWithLifecycle(viewModel.newEntryEvents) {
        snackbars.showSnackbar(addedMessage)
    }
    val searchState = rememberFoodSearchAppState()
    val tabStates = rememberSaveableStateHolder()
    val selectedSlot = slots?.firstOrNull { it.id == selectedSlotId }
    LaunchedEffect(slots) {
        val current = slots ?: return@LaunchedEffect
        if (!autoSelected) {
            val time = initialSlotTime?.let { runCatching { LocalTime.parse(it) }.getOrNull() } ?: clock.now().time
            selectedSlotId = selectDiarySlot(current, time)?.id ?: -1L
            autoSelected = true
            slotsExpanded = selectedSlotId < 0 && current.isNotEmpty()
        }
    }
    NavigationEventHandler(state = rememberNavigationEventState(NavigationEventInfo.None), isBackEnabled = menuExpanded,
        onBackCompleted = { menuExpanded = false })
    Scaffold(snackbarHost = { SnackbarHost(snackbars, modifier = Modifier.uiName("DiaryFoodSearchScreen.SnackbarHost.phoneNew1", "androidx.compose.material3.SnackbarHost", uiInspectionSource, "DiaryFoodSearchScreen")) }, modifier = (modifier.testTag("food-entry")).uiName("DiaryFoodSearchScreen.Scaffold.phone1", "androidx.compose.material3.Scaffold", uiInspectionSource, "DiaryFoodSearchScreen"), topBar = {
        Column(modifier = Modifier.uiName("DiaryFoodSearchScreen.Column.header", "androidx.compose.foundation.layout.Column", uiInspectionSource, "DiaryFoodSearchScreen")) {
        TopAppBar(title = {
            Box (modifier = Modifier.uiName("DiaryFoodSearchScreen.Box.phone1", "androidx.compose.foundation.layout.Box", uiInspectionSource, "DiaryFoodSearchScreen")) {
                TextButton(onClick = { slotsExpanded = true }, modifier = (Modifier.testTag("food-entry-slot")).uiName("DiaryFoodSearchScreen.TextButton.phone1", "androidx.compose.material3.TextButton", uiInspectionSource, "DiaryFoodSearchScreen")) {
                    Text(selectedSlot?.name ?: stringResource(Res.string.food_entry_choose_slot), modifier = Modifier.uiName("DiaryFoodSearchScreen.Text.phone1", "androidx.compose.material3.Text", uiInspectionSource, "DiaryFoodSearchScreen"))
                    Icon(Icons.Outlined.ArrowDropDown, null, modifier = Modifier.uiName("DiaryFoodSearchScreen.Icon.phone1", "androidx.compose.material3.Icon", uiInspectionSource, "DiaryFoodSearchScreen"))
                }
                UiSourceScope(uiInspectionSource.element("DiaryFoodSearchScreen.SlotMenu", "androidx.compose.material3.DropdownMenu", "DiaryFoodSearchScreen")) {
                    DropdownMenu(expanded = slotsExpanded, onDismissRequest = { slotsExpanded = false }, modifier = Modifier.uiName("DiaryFoodSearchScreen.DropdownMenu.phone1", "androidx.compose.material3.DropdownMenu", uiInspectionSource, "DiaryFoodSearchScreen")) {
                        slots.orEmpty().forEach { slot ->
                            DropdownMenuItem(text = { Text(slot.name, modifier = Modifier.uiName("DiaryFoodSearchScreen.Text.phone2", "androidx.compose.material3.Text", uiInspectionSource, "DiaryFoodSearchScreen")) }, onClick = { selectedSlotId = slot.id; slotsExpanded = false }, modifier = Modifier.uiName("DiaryFoodSearchScreen.DropdownMenuItem.phone1", "androidx.compose.material3.DropdownMenuItem", uiInspectionSource, "DiaryFoodSearchScreen"))
                        }
                    }
                }
            }
        }, subtitle = {}, titleHorizontalAlignment = Alignment.CenterHorizontally,
            navigationIcon = { ArrowBackIconButton(onBack, modifier = Modifier.uiName("DiaryFoodSearchScreen.ArrowBackIconButton.phone1", "com.maksimowiczm.foodyou.app.ui.common.component.ArrowBackIconButton", uiInspectionSource, "DiaryFoodSearchScreen")) }, modifier = Modifier.uiName("DiaryFoodSearchScreen.TopAppBar.phone1", "androidx.compose.material3.TopAppBar", uiInspectionSource, "DiaryFoodSearchScreen"))

        TextButton(onClick = { dateTimePicker = true },
            modifier = Modifier.align(Alignment.CenterHorizontally).fillMaxWidth().padding(horizontal = 16.dp)
                .semantics { contentDescription = dateTimeLabel }.testTag("food-entry-date-time").uiName("DiaryFoodSearchScreen.TextButton.dateTime", "androidx.compose.material3.TextButton", uiInspectionSource, "DiaryFoodSearchScreen")) {
            Text("${LocalDateFormatter.current.formatDate(date)} · $selectedTimeText",
                modifier = Modifier.weight(1f, fill = false).uiName("DiaryFoodSearchScreen.Text.dateTime", "androidx.compose.material3.Text", uiInspectionSource, "DiaryFoodSearchScreen"))
            Icon(Icons.Outlined.ArrowDropDown, null,
                modifier = Modifier.uiName("DiaryFoodSearchScreen.Icon.dateTime", "androidx.compose.material3.Icon", uiInspectionSource, "DiaryFoodSearchScreen"))
        }
        }
    }, floatingActionButton = {
        if (!aiTab && selectedSlot != null) FoodDiarySearchFloatingActionButton(
            fabExpanded = menuExpanded, onFabExpandedChange = { menuExpanded = it },
            onCreateRecipe = { onCreateRecipe(selectedSlot.id) }, onCreateProduct = { onCreateProduct(selectedSlot.id) }, onMeals = onMeals, modifier = Modifier.uiName("DiaryFoodSearchScreen.FoodDiarySearchFloatingActionButton.phone1", "com.maksimowiczm.foodyou.app.ui.food.diary.search.FoodDiarySearchFloatingActionButton", uiInspectionSource, "DiaryFoodSearchScreen"))
    }) { padding ->
        Column((Modifier.fillMaxSize().padding(padding).consumeWindowInsets(padding)).uiName("DiaryFoodSearchScreen.Column.phone1", "androidx.compose.foundation.layout.Column", uiInspectionSource, "DiaryFoodSearchScreen")) {
            PrimaryTabRow(selectedTabIndex = if (aiTab) 1 else 0, modifier = Modifier.uiName("DiaryFoodSearchScreen.PrimaryTabRow.phone1", "androidx.compose.material3.PrimaryTabRow", uiInspectionSource, "DiaryFoodSearchScreen")) {
                Tab(selected = !aiTab, onClick = { aiTab = false; menuExpanded = false }, text = { Text(stringResource(Res.string.food_entry_search), modifier = Modifier.uiName("DiaryFoodSearchScreen.Text.phone4", "androidx.compose.material3.Text", uiInspectionSource, "DiaryFoodSearchScreen")) }, modifier = (Modifier.testTag("food-entry-search-tab")).uiName("DiaryFoodSearchScreen.Tab.phone1", "androidx.compose.material3.Tab", uiInspectionSource, "DiaryFoodSearchScreen"))
                Tab(selected = aiTab, onClick = { aiTab = true; menuExpanded = false }, text = { Text(stringResource(Res.string.food_entry_ai), modifier = Modifier.uiName("DiaryFoodSearchScreen.Text.phone5", "androidx.compose.material3.Text", uiInspectionSource, "DiaryFoodSearchScreen")) }, modifier = (Modifier.testTag("food-entry-ai-tab")).uiName("DiaryFoodSearchScreen.Tab.phone2", "androidx.compose.material3.Tab", uiInspectionSource, "DiaryFoodSearchScreen"))
            }
            if (slots == null) {
                CircularProgressIndicator((Modifier.padding(16.dp)).uiName("DiaryFoodSearchScreen.CircularProgressIndicator.phone1", "androidx.compose.material3.CircularProgressIndicator", uiInspectionSource, "DiaryFoodSearchScreen"))
            } else if (selectedSlot == null) {
                Column((Modifier.padding(16.dp)).uiName("DiaryFoodSearchScreen.Column.phone2", "androidx.compose.foundation.layout.Column", uiInspectionSource, "DiaryFoodSearchScreen")) {
                    Text(stringResource(if (slots!!.isEmpty()) Res.string.food_entry_no_slots else Res.string.food_entry_choose_slot), modifier = Modifier.uiName("DiaryFoodSearchScreen.Text.phone6", "androidx.compose.material3.Text", uiInspectionSource, "DiaryFoodSearchScreen"))
                    TextButton(onClick = { if (slots!!.isEmpty()) onSetupSlots() else slotsExpanded = true }, modifier = Modifier.uiName("DiaryFoodSearchScreen.TextButton.phone2", "androidx.compose.material3.TextButton", uiInspectionSource, "DiaryFoodSearchScreen")) {
                        Text(stringResource(if (slots!!.isEmpty()) Res.string.food_entry_setup_slots else Res.string.food_entry_choose_slot), modifier = Modifier.uiName("DiaryFoodSearchScreen.Text.phone7", "androidx.compose.material3.Text", uiInspectionSource, "DiaryFoodSearchScreen"))
                    }
                }
            } else tabStates.SaveableStateProvider(if (aiTab) "ai" else "search") {
                if (aiTab) UiSourceScope(uiInspectionSource.element("DiaryFoodSearchScreen.AiScanScreen", "com.maksimowiczm.foodyou.app.ui.food.diary.aiscan.AiScanScreen", "DiaryFoodSearchScreen")) { AiScanScreen(onBack = onBack, embedded = true,
                    onSaveToQuickAdd = { estimate, id -> onSaveToQuickAdd(selectedSlot.id, estimate, id) },
                    onQueueStarted = { onScanQueueStarted(selectedSlot.id) },
                    onQueueProgress = onScanQueueProgress,
                    onAlignPickFood = { onAlignPickFood(selectedSlot.id, it) },
                    onCreateCustomFood = { onCreateProduct(selectedSlot.id) },
                    savedScanItemId = savedScanItemId, onSavedScanItemConsumed = onSavedScanItemConsumed, onScanCompleted = onScanCompleted,
                    modifier = Modifier.fillMaxSize()) }
                else FoodSearchApp(onFoodClick = { model, amount -> onMeasure(selectedSlot.id, model.id, amount) },
                    onUpdateUsdaApiKey = onUpdateUsdaApiKey, onUpdateOpenFoodFactsCredentials = onUpdateOpenFoodFactsCredentials,
                    initialQuery = initialQuery, appState = searchState, modifier = (Modifier.fillMaxSize()).uiName("DiaryFoodSearchScreen.FoodSearchApp.phone1", "com.maksimowiczm.foodyou.app.ui.food.search.FoodSearchApp", uiInspectionSource, "DiaryFoodSearchScreen"))
            }
        }
    }
}

private val uiInspectionSource = UiSourceFile("com.maksimowiczm.foodyou.app.ui.food.diary.search",
    "app/src/commonMain/kotlin/com/maksimowiczm/foodyou/app/ui/food/diary/search/DiaryFoodSearchScreen.kt")
