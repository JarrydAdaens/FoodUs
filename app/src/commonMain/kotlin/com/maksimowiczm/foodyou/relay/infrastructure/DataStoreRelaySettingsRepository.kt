package com.maksimowiczm.foodyou.relay.infrastructure

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.maksimowiczm.foodyou.common.infrastructure.datastore.AbstractDataStoreUserPreferencesRepository
import com.maksimowiczm.foodyou.common.infrastructure.datastore.set
import com.maksimowiczm.foodyou.relay.domain.RelaySettings

/**
 * DataStore-backed persistence for [RelaySettings] (Milestone 3, Story 15), mirroring the AI
 * settings storage pattern. A blank URL is stored as an absent key, so a cleared field reads back
 * as `null` (relay unset).
 */
internal class DataStoreRelaySettingsRepository(dataStore: DataStore<Preferences>) :
    AbstractDataStoreUserPreferencesRepository<RelaySettings>(dataStore) {
    override fun Preferences.toUserPreferences(): RelaySettings =
        RelaySettings(url = this[RelayPreferencesKeys.Url])

    override fun MutablePreferences.applyUserPreferences(updated: RelaySettings) {
        this[RelayPreferencesKeys.Url] = updated.url
    }
}

private object RelayPreferencesKeys {
    val Url = stringPreferencesKey("relay:url")
}
