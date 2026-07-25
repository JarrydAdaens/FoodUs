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
        "mailto:jarryd.adaens@outlook.com.au?subject=ACME Food App Feedback" +
            "&body=ACME Food App Version: $forkVersionName (Food You $versionName)\n"
    override val translationUri: String = "https://crowdin.com/project/food-you"
    // Fork's own repository and issue tracker (Milestone 2, Story 4).
    override val sourceCodeUri: String = "https://github.com/JarrydAdaens/FoodYou"
    override val issueTrackerUri: String = "https://github.com/JarrydAdaens/FoodYou/issues"
    // Upstream creator's GitHub, credited on the About screen (Milestone 2, Story 4).
    override val upstreamAuthorUri: String = "https://github.com/maksimowiczm"
    // Fork's own privacy policy. Placeholder until the owner supplies the URL of the
    // privacy policy already published for his apps (Milestone 2, Story 3).
    override val privacyPolicyUri: String =
        "https://github.com/JarrydAdaens/FoodYou/blob/main/PRIVACY.md"
    override val openFoodFactsTermsOfUseUri: String = "https://world.openfoodfacts.org/terms-of-use"
    override val openFoodFactsPrivacyPolicyUri: String = "https://world.openfoodfacts.org/privacy"
    override val foodDataCentralPrivacyPolicyUri: String = "https://www.usda.gov/privacy-policy"

    override val userAgent: String = "Food You/$versionName (maksimowicz.dev@gmail.com)"
}
