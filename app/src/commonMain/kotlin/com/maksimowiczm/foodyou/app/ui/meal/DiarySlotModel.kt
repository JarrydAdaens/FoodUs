package com.maksimowiczm.foodyou.app.ui.meal

import androidx.compose.runtime.*
import kotlinx.datetime.LocalTime

@Immutable
internal data class DiarySlotModel(
    val id: Long,
    val name: String,
    val from: LocalTime,
    val to: LocalTime,
    val isAllDay: Boolean,
)
