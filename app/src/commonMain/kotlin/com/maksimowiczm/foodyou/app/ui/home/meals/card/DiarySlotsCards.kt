package com.maksimowiczm.foodyou.app.ui.home.meals.card

import com.maksimowiczm.foodyou.app.ui.developer.inspection.UiSourceFile
import com.maksimowiczm.foodyou.app.ui.developer.inspection.uiName

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import foodyou.app.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.maksimowiczm.foodyou.app.ui.home.shared.HomeState
import com.maksimowiczm.foodyou.fooddiary.domain.entity.DiarySlotsCardsLayout
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun DiarySlotsCards(
    homeState: HomeState,
    onAdd: (epochDay: Long, diarySlotId: Long) -> Unit,
    onQuickAdd: (epochDay: Long, diarySlotId: Long) -> Unit,
    onAiScan: (epochDay: Long, diarySlotId: Long) -> Unit,
    onFastText: (epochDay: Long, diarySlotId: Long) -> Unit,
    onEditEntry: (foodEntryId: Long?, manualEntryId: Long?, isPlaceholder: Boolean) -> Unit,
    onLongClick: (diarySlotId: Long) -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    val viewModel: DiarySlotsCardsViewModel = koinViewModel()
    val diarySlotEntries = viewModel.diarySlotEntries.collectAsStateWithLifecycle().value
    val layout by viewModel.layout.collectAsStateWithLifecycle()
    val meals by viewModel.meals.collectAsStateWithLifecycle()
    val readFailed by viewModel.readFailed.collectAsStateWithLifecycle()

    LaunchedEffect(homeState.selectedDate, viewModel) { viewModel.setDate(homeState.selectedDate) }

    if (readFailed) {
        Column((modifier).uiName("DiarySlotsCards.Column.1", "androidx.compose.foundation.layout.Column", uiInspectionSource, "DiarySlotsCards")) {
            Text(stringResource(Res.string.meals_unavailable), modifier = Modifier.uiName("DiarySlotsCards.Text.1", "androidx.compose.material3.Text", uiInspectionSource, "DiarySlotsCards"))
            TextButton(onClick = viewModel::retryRead, modifier = Modifier.uiName("DiarySlotsCards.TextButton.1", "androidx.compose.material3.TextButton", uiInspectionSource, "DiarySlotsCards")) { Text(stringResource(Res.string.meals_retry), modifier = Modifier.uiName("DiarySlotsCards.Text.2", "androidx.compose.material3.Text", uiInspectionSource, "DiarySlotsCards")) }
        }
        return
    }

    when (layout) {
        DiarySlotsCardsLayout.Horizontal ->
            HorizontalDiarySlotsCards(
                diarySlots = diarySlotEntries,
                onAdd = { diarySlotId -> onAdd(homeState.selectedDate.toEpochDays(), diarySlotId) },
                onQuickAdd = { diarySlotId -> onQuickAdd(homeState.selectedDate.toEpochDays(), diarySlotId) },
                onAiScan = { diarySlotId -> onAiScan(homeState.selectedDate.toEpochDays(), diarySlotId) },
                onFastText = { diarySlotId -> onFastText(homeState.selectedDate.toEpochDays(), diarySlotId) },
                onEditEntry = { model ->
                    val foodEntry = model as? FoodDiarySlotEntryModel
                    val manualEntry = model as? ManualDiarySlotEntryModel
                    onEditEntry(
                        foodEntry?.id?.value,
                        manualEntry?.id?.value,
                        manualEntry?.isPlaceholder == true,
                    )
                },
                onDeleteEntry = viewModel::onDeleteEntry,
                onLongClick = onLongClick,
                meals = meals,
                onSaveMeal = viewModel::saveAsMeal,
                onApplyMeal = viewModel::applyMeal,
                onDeleteMeal = viewModel::deleteMeal,
                shimmer = homeState.shimmer,
                contentPadding = contentPadding,
                modifier = (modifier).uiName("DiarySlotsCards.HorizontalDiarySlotsCards.1", "com.maksimowiczm.foodyou.app.ui.home.meals.card.HorizontalDiarySlotsCards", uiInspectionSource, "DiarySlotsCards"),
            )

        DiarySlotsCardsLayout.Vertical ->
            VerticalDiarySlotsCards(
                diarySlots = diarySlotEntries,
                onAdd = { diarySlotId -> onAdd(homeState.selectedDate.toEpochDays(), diarySlotId) },
                onQuickAdd = { diarySlotId -> onQuickAdd(homeState.selectedDate.toEpochDays(), diarySlotId) },
                onAiScan = { diarySlotId -> onAiScan(homeState.selectedDate.toEpochDays(), diarySlotId) },
                onFastText = { diarySlotId -> onFastText(homeState.selectedDate.toEpochDays(), diarySlotId) },
                onEditEntry = { model ->
                    val foodEntry = model as? FoodDiarySlotEntryModel
                    val manualEntry = model as? ManualDiarySlotEntryModel
                    onEditEntry(
                        foodEntry?.id?.value,
                        manualEntry?.id?.value,
                        manualEntry?.isPlaceholder == true,
                    )
                },
                onDeleteEntry = viewModel::onDeleteEntry,
                onLongClick = onLongClick,
                meals = meals,
                onSaveMeal = viewModel::saveAsMeal,
                onApplyMeal = viewModel::applyMeal,
                onDeleteMeal = viewModel::deleteMeal,
                shimmer = homeState.shimmer,
                contentPadding = contentPadding,
                modifier = (modifier).uiName("DiarySlotsCards.VerticalDiarySlotsCards.1", "com.maksimowiczm.foodyou.app.ui.home.meals.card.VerticalDiarySlotsCards", uiInspectionSource, "DiarySlotsCards"),
            )
    }
}


private val uiInspectionSource = UiSourceFile(
    "com.maksimowiczm.foodyou.app.ui.home.meals.card",
    "app/src/commonMain/kotlin/com/maksimowiczm/foodyou/app/ui/home/meals/card/DiarySlotsCards.kt",
)
