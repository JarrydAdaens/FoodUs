package com.maksimowiczm.foodyou.app.ui.home.meals.card



import com.maksimowiczm.foodyou.app.ui.developer.inspection.UiSourceFile
import com.maksimowiczm.foodyou.app.ui.developer.inspection.uiName

import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import com.maksimowiczm.foodyou.fooddiary.domain.entity.MealId
import com.valentinilk.shimmer.Shimmer

@OptIn(ExperimentalAnimationApi::class)
@Composable
internal fun HorizontalDiarySlotsCards(
    diarySlots: List<DiarySlotModel>?,
    onAdd: (diarySlotId: Long) -> Unit,
    onQuickAdd: (diarySlotId: Long) -> Unit,
    onAiScan: (diarySlotId: Long) -> Unit,
    onFastText: (diarySlotId: Long) -> Unit,
    onEditEntry: (DiarySlotEntryModel) -> Unit,
    onDeleteEntry: (DiarySlotEntryModel) -> Unit,
    onLongClick: (diarySlotId: Long) -> Unit,
    meals: List<MealModel>,
    onSaveMeal: (diarySlotId: Long, name: String, onResult: (Boolean) -> Unit) -> Unit,
    onApplyMeal: (MealId, diarySlotId: Long, onResult: (Boolean) -> Unit) -> Unit,
    onDeleteMeal: (MealId, onResult: (Boolean) -> Unit) -> Unit,
    shimmer: Shimmer,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    // Must be same as diarySlots count or more but since we don't have diarySlots count yet set it to some
    // extreme value. If it is less than actual diarySlots count pager will scroll back to the
    // last item which is annoying for the user.
    // Let's assume that user won't use more than 20 diarySlots
    val pagerState = rememberPagerState(pageCount = { diarySlots?.size ?: 20 })

    val transition = updateTransition(diarySlots)

    HorizontalPager(
        state = pagerState,
        modifier =
            (modifier.animateContentSize(
                animationSpec = MaterialTheme.motionScheme.defaultSpatialSpec()
            )).uiName("HorizontalDiarySlotsCards.HorizontalPager.1", "androidx.compose.foundation.pager.HorizontalPager", uiInspectionSource, "HorizontalDiarySlotsCards"),
        verticalAlignment = Alignment.Top,
        contentPadding =
            PaddingValues(
                start = contentPadding.calculateStartPadding(LocalLayoutDirection.current),
                end = 24.dp,
                top = contentPadding.calculateTopPadding(),
                bottom = contentPadding.calculateBottomPadding(),
            ),
    ) { page ->
        val diarySlot = diarySlots?.getOrNull(page)

        transition.Crossfade(
            contentKey = { it != null },
            modifier = (Modifier.fillMaxWidth().padding(horizontal = 2.dp)).uiName("HorizontalDiarySlotsCards.Crossfade.1", "androidx.compose.animation.Crossfade", uiInspectionSource, "HorizontalDiarySlotsCards"),
        ) {
            if (it != null && diarySlot != null) {
                DiarySlotCard(
                    diarySlot = diarySlot,
                    onAddFood = { onAdd(diarySlot.id) },
                    onQuickAdd = { onQuickAdd(diarySlot.id) },
                    onAiScan = { onAiScan(diarySlot.id) },
                    onFastText = { onFastText(diarySlot.id) },
                    onEditEntry = onEditEntry,
                    onDeleteEntry = onDeleteEntry,
                    onLongClick = { onLongClick(diarySlot.id) },
                    meals = meals,
                    onSaveMeal = { name, result -> onSaveMeal(diarySlot.id, name, result) },
                    onApplyMeal = { id, result -> onApplyMeal(id, diarySlot.id, result) },
                    onDeleteMeal = onDeleteMeal, modifier = Modifier.uiName("HorizontalDiarySlotsCards.DiarySlotCard.1", "com.maksimowiczm.foodyou.app.ui.home.meals.card.DiarySlotCard", uiInspectionSource, "HorizontalDiarySlotsCards")
                )
            } else {
                DiarySlotCardSkeleton(shimmer = shimmer, modifier = Modifier.uiName("HorizontalDiarySlotsCards.DiarySlotCardSkeleton.1", "com.maksimowiczm.foodyou.app.ui.home.meals.card.DiarySlotCardSkeleton", uiInspectionSource, "HorizontalDiarySlotsCards"))
            }
        }
    }
}


private val uiInspectionSource = UiSourceFile(
    "com.maksimowiczm.foodyou.app.ui.home.meals.card",
    "app/src/commonMain/kotlin/com/maksimowiczm/foodyou/app/ui/home/meals/card/HorizontalDiarySlotsCards.kt",
)
