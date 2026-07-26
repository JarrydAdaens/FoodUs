package com.maksimowiczm.foodyou.app.ui.common.utility

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import com.maksimowiczm.foodyou.common.config.AppConfig

private val defaultAppConfig =
    object : AppConfig {
        override val versionName: String = "Default version"
        override val forkVersionName: String = "Default fork version"
        override val contactEmailUri: String = "contactEmailUri"
        override val translationUri: String = "translationUri"
        override val sourceCodeUri: String = "sourceCodeUri"
        override val issueTrackerUri: String = "issueTrackerUri"
        override val upstreamAuthorUri: String = "upstreamAuthorUri"
        override val privacyPolicyUri: String = "privacyPolicyUri"
        override val openFoodFactsTermsOfUseUri: String = "openFoodFactsTermsOfUseUri"
        override val openFoodFactsPrivacyPolicyUri: String = "openFoodFactsPrivacyPolicyUri"
        override val foodDataCentralPrivacyPolicyUri: String = "foodDataCentralPrivacyPolicyUri"
        override val aiApiKey: String = ""
        override val aiEndpoint: String = "https://openrouter.ai/api/v1/chat/completions"
        override val aiModel: String = "openai/gpt-4o-mini"
    }

val LocalAppConfig = staticCompositionLocalOf<AppConfig> { defaultAppConfig }

@Composable
fun AppConfigProvider(energyFormatter: AppConfig, content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalAppConfig provides energyFormatter) { content() }
}
