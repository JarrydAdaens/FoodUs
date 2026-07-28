package com.maksimowiczm.foodyou.app.ui.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maksimowiczm.foodyou.notification.domain.AppNotification
import com.maksimowiczm.foodyou.notification.domain.AppNotificationRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

internal class NotificationsViewModel(private val repository: AppNotificationRepository) :
    ViewModel() {

    private val _showHistory = MutableStateFlow(false)

    /** `false` is the tab's default: dismissed notifications stay out of the way until asked for. */
    val showHistory: StateFlow<Boolean> = _showHistory.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val notifications: StateFlow<List<AppNotification>> =
        _showHistory
            .flatMapLatest { repository.observe(includeDismissed = it) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList(),
            )

    fun onToggleHistory() {
        _showHistory.value = !_showHistory.value
    }

    fun onDismiss(id: Long) {
        viewModelScope.launch { repository.dismiss(id) }
    }

    fun onDismissAll() {
        viewModelScope.launch { repository.dismissAll() }
    }
}
