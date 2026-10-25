package com.maksimowiczm.foodyou.app.ui.home.meals.card

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
        Column(modifier) {
            Text(stringResource(Res.string.meals_unavailable))
            TextButton(onClick = viewModel::retryRead) { Text(stringResource(Res.string.meals_retry)) }
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
                modifier = modifier,
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
                modifier = modifier,
            )
    }
}
