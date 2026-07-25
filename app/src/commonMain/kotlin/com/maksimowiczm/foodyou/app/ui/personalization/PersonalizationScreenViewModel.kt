package com.maksimowiczm.foodyou.app.ui.personalization

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maksimowiczm.foodyou.common.domain.userpreferences.UserPreferencesRepository
import com.maksimowiczm.foodyou.settings.domain.entity.EnergyFormat
import com.maksimowiczm.foodyou.settings.domain.entity.GraphStyle
import com.maksimowiczm.foodyou.settings.domain.entity.Settings
import com.maksimowiczm.foodyou.settings.domain.entity.WeekLayout
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

internal class PersonalizationScreenViewModel(
    private val settingsRepository: UserPreferencesRepository<Settings>
) : ViewModel() {

    private val _secureScreen = settingsRepository.observe().map { it.secureScreen }
    val secureScreen =
        _secureScreen.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(2_000),
            initialValue = runBlocking { _secureScreen.first() },
        )

    fun toggleSecureScreen(newState: Boolean) {
        viewModelScope.launch { settingsRepository.update { copy(secureScreen = newState) } }
    }

    private val _energyUnit = settingsRepository.observe().map { it.energyFormat }
    val energyUnit =
        _energyUnit.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(2_000),
            initialValue = runBlocking { _energyUnit.first() },
        )

    fun setEnergyFormat(format: EnergyFormat) {
        viewModelScope.launch { settingsRepository.update { copy(energyFormat = format) } }
    }

    private val _graphStyle = settingsRepository.observe().map { it.graphStyle }
    val graphStyle =
        _graphStyle.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(2_000),
            initialValue = runBlocking { _graphStyle.first() },
        )

    fun setGraphStyle(style: GraphStyle) {
        viewModelScope.launch { settingsRepository.update { copy(graphStyle = style) } }
    }

    private val _weekLayout = settingsRepository.observe().map { it.weekLayout }
    val weekLayout =
        _weekLayout.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(2_000),
            initialValue = runBlocking { _weekLayout.first() },
        )

    fun setWeekLayout(layout: WeekLayout) {
        viewModelScope.launch { settingsRepository.update { copy(weekLayout = layout) } }
    }
}
