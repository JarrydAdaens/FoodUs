package com.maksimowiczm.foodyou.app.ui.home.meals.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maksimowiczm.foodyou.common.domain.userpreferences.UserPreferencesRepository
import com.maksimowiczm.foodyou.fooddiary.domain.entity.DiarySlotsPreferences
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

internal class DiarySlotsCardsSettingsViewModel(
    private val diarySlotsPreferencesRepository: UserPreferencesRepository<DiarySlotsPreferences>
) : ViewModel() {

    private val _preferences = diarySlotsPreferencesRepository.observe()
    val preferences =
        _preferences.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(2_000),
            initialValue = runBlocking { _preferences.first() },
        )

    fun updatePreferences(preferences: DiarySlotsPreferences) {
        viewModelScope.launch { diarySlotsPreferencesRepository.update { preferences } }
    }
}
