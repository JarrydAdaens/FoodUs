package com.maksimowiczm.foodyou.relay.domain

import com.maksimowiczm.foodyou.common.domain.userpreferences.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ObserveRelayConfiguredTest {

    @Test
    fun unsetRelayIsNotConfigured() = runBlocking {
        val repository = FakeRelaySettingsRepository(RelaySettings(url = null))

        assertFalse(ObserveRelayConfigured(repository)().first())
    }

    @Test
    fun blankStoredUrlIsNotConfigured() = runBlocking {
        val repository = FakeRelaySettingsRepository(RelaySettings(url = "   "))

        assertFalse(ObserveRelayConfigured(repository)().first())
    }

    @Test
    fun savingAUrlFlipsTheSignalOn() = runBlocking {
        val repository = FakeRelaySettingsRepository(RelaySettings(url = null))
        val configured = ObserveRelayConfigured(repository)

        assertFalse(configured().first())

        repository.update { RelaySettings(url = "https://relay.example.test") }

        assertTrue(configured().first())
    }
}

private class FakeRelaySettingsRepository(initial: RelaySettings) :
    UserPreferencesRepository<RelaySettings> {
    private val state = MutableStateFlow(initial)

    override fun observe(): Flow<RelaySettings> = state

    override suspend fun update(transform: RelaySettings.() -> RelaySettings) {
        state.value = state.value.transform()
    }
}
