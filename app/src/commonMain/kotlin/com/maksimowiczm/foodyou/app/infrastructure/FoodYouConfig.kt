package com.maksimowiczm.foodyou.app.infrastructure

import com.maksimowiczm.foodyou.app.BuildConfig
import com.maksimowiczm.foodyou.common.config.AppConfig
import com.maksimowiczm.foodyou.common.config.NetworkConfig

internal class FoodYouConfig : AppConfig, NetworkConfig {
    override val versionName: String = BuildConfig.VERSION_NAME
    override val forkVersionName: String = BuildConfig.FORK_VERSION_NAME
    // Feedback about the fork reaches the fork's owner, not the upstream creator
    // (Milestone 2, Story 4).
    override val contactEmailUri: String =
        "mailto:jarryd.adaens@outlook.com.au?subject=FoodUs Feedback" +
            "&body=FoodUs Version: $forkVersionName (Food You $versionName)\n"
    override val translationUri: String = "https://crowdin.com/project/food-you"
    // Fork's own repository and issue tracker (Milestone 2, Story 4).
    override val sourceCodeUri: String = "https://github.com/JarrydAdaens/FoodUs"
    override val issueTrackerUri: String = "https://github.com/JarrydAdaens/FoodUs/issues"
    // Upstream creator's GitHub, credited on the About screen (Milestone 2, Story 4).
    override val upstreamAuthorUri: String = "https://github.com/maksimowiczm"
    // Fork's own published privacy policy (Milestone 2, Story 3; owner-supplied URL).
    override val privacyPolicyUri: String = "https://jarrydadaens.github.io/privacy.html"
    override val openFoodFactsTermsOfUseUri: String = "https://world.openfoodfacts.org/terms-of-use"
    override val openFoodFactsPrivacyPolicyUri: String = "https://world.openfoodfacts.org/privacy"
    override val foodDataCentralPrivacyPolicyUri: String = "https://www.usda.gov/privacy-policy"

    // AI configuration developer fallbacks (Milestone 2, Stories 6 and 22). Blank in public builds
    // and superseded by the user-entered settings; never surfaced in the UI and never logged. The
    // key is an optional local-build convenience injected from local.properties / an env variable.
    override val aiApiKey: String = BuildConfig.AI_API_KEY
    override val aiEndpoint: String = BuildConfig.AI_ENDPOINT
    override val aiModel: String = BuildConfig.AI_MODEL

    // Fork identity in outbound API requests so traffic is attributed to the fork's
    // owner, not the upstream creator.
    override val userAgent: String = "FoodUs/$forkVersionName (jarryd.adaens@outlook.com.au)"
}
