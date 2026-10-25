package com.maksimowiczm.foodyou.app.ui.meal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maksimowiczm.foodyou.fooddiary.domain.repository.DiarySlotRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

internal class DiarySlotSettingsViewModel(private val diarySlotRepository: DiarySlotRepository) : ViewModel() {

    val diarySlots: StateFlow<List<DiarySlotModel>?> =
        diarySlotRepository
            .observeDiarySlots()
            .distinctUntilChanged()
            .map { diarySlots ->
                diarySlots
                    .sortedBy { it.rank }
                    .map { diarySlot ->
                        DiarySlotModel(
                            id = diarySlot.id,
                            name = diarySlot.name,
                            from = diarySlot.from,
                            to = diarySlot.to,
                            isAllDay = diarySlot.from == diarySlot.to,
                        )
                    }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(2_000),
                initialValue = null,
            )

    fun deleteDiarySlot(diarySlotModel: DiarySlotModel) {
        viewModelScope.launch { diarySlotRepository.deleteDiarySlot(diarySlotModel.id) }
    }

    fun updateDiarySlot(diarySlotModel: DiarySlotModel) {
        viewModelScope.launch {
            diarySlotRepository.updateDiarySlot(
                id = diarySlotModel.id,
                name = diarySlotModel.name,
                from = diarySlotModel.from,
                to = diarySlotModel.to,
            )
        }
    }

    fun createDiarySlot(diarySlotModel: DiarySlotModel) {
        viewModelScope.launch {
            diarySlotRepository.insertDiarySlotWithLastRank(
                name = diarySlotModel.name,
                from = diarySlotModel.from,
                to = diarySlotModel.to,
            )
        }
    }

    fun updateDiarySlotOrder(diarySlotModels: List<DiarySlotModel>) {
        viewModelScope.launch { diarySlotRepository.reorderDiarySlots(diarySlotModels.map { it.id }) }
    }
}
