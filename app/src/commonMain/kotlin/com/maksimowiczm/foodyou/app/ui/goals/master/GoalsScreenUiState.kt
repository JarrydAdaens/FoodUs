package com.maksimowiczm.foodyou.app.ui.goals.master

import androidx.compose.runtime.*
import com.maksimowiczm.foodyou.goals.domain.entity.DailyGoal

@Immutable internal data class GoalsScreenUiState(val diarySlots: List<DiarySlotModel>, val goal: DailyGoal)
