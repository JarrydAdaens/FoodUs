package com.maksimowiczm.foodyou.ai.infrastructure

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.maksimowiczm.foodyou.ai.domain.AiSettings
import com.maksimowiczm.foodyou.common.infrastructure.datastore.AbstractDataStoreUserPreferencesRepository
import com.maksimowiczm.foodyou.common.infrastructure.datastore.set

/**
 * DataStore-backed persistence for [AiSettings] (Milestone 2, Story 22), mirroring the USDA-key
 * storage pattern. Blank fields are stored as absent keys (removed), so a cleared field reads back
 * as `null` (unset).
 */
internal class DataStoreAiSettingsRepository(dataStore: DataStore<Preferences>) :
    AbstractDataStoreUserPreferencesRepository<AiSettings>(dataStore) {
    override fun Preferences.toUserPreferences(): AiSettings =
        AiSettings(
            apiKey = this[AiPreferencesKeys.ApiKey],
            endpoint = this[AiPreferencesKeys.Endpoint],
            model = this[AiPreferencesKeys.Model],
            userSystemPrompt = this[AiPreferencesKeys.UserSystemPrompt],
        )

    override fun MutablePreferences.applyUserPreferences(updated: AiSettings) {
        this[AiPreferencesKeys.ApiKey] = updated.apiKey
        this[AiPreferencesKeys.Endpoint] = updated.endpoint
        this[AiPreferencesKeys.Model] = updated.model
        this[AiPreferencesKeys.UserSystemPrompt] = updated.userSystemPrompt
    }
}

private object AiPreferencesKeys {
    val ApiKey = stringPreferencesKey("ai:api_key")
    val Endpoint = stringPreferencesKey("ai:endpoint")
    val Model = stringPreferencesKey("ai:model")
    val UserSystemPrompt = stringPreferencesKey("ai:user_system_prompt")
}
