package com.maksimowiczm.foodyou.app.ui.groups.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maksimowiczm.foodyou.profile.domain.CreateProfileUseCase
import com.maksimowiczm.foodyou.profile.domain.Profile
import com.maksimowiczm.foodyou.profile.domain.ProfileRepository
import com.maksimowiczm.foodyou.profile.domain.RenameProfileUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

internal class ProfileViewModel(
    repository: ProfileRepository,
    private val createProfile: CreateProfileUseCase,
    private val renameProfile: RenameProfileUseCase,
) : ViewModel() {

    /** `null` means "no profile yet" — the card renders its empty state. */
    val profile: StateFlow<Profile?> =
        repository
            .observe()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = null,
            )

    fun onCreate(username: String) {
        viewModelScope.launch { createProfile(username) }
    }

    fun onRename(username: String) {
        viewModelScope.launch { renameProfile(username) }
    }
}
