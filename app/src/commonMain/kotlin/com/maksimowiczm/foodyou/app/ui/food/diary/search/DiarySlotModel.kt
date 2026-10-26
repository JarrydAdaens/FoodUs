package com.maksimowiczm.foodyou.app.ui.food.diary.search

import androidx.compose.runtime.*
import com.maksimowiczm.foodyou.fooddiary.domain.entity.DiarySlot

@Immutable
internal data class DiarySlotModel(val id: Long, val name: String) {
    constructor(diarySlot: DiarySlot) : this(id = diarySlot.id, name = diarySlot.name)
}
