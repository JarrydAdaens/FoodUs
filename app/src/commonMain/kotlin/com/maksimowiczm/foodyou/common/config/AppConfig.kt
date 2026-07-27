package com.maksimowiczm.foodyou.common.config

interface AppConfig {

    /** Upstream Food You version name this fork derives from (e.g. "3.4.9"). */
    val versionName: String

    /**
     * FoodUs fork version name in the <milestone>.<story>.<build> scheme (e.g. "2.19.1").
     * This is the app's real shipped version; [versionName] is the upstream Food You
     * version it derives from, kept as metadata.
     */
    val forkVersionName: String

    /** Mailto URI for contacting the developer (including subject and body). */
    val contactEmailUri: String

    /** URL to the translation platform where users can contribute translations. */
    val translationUri: String

    val sourceCodeUri: String
    val issueTrackerUri: String

    /** URL to the upstream Food You creator's GitHub, credited on the About screen. */
    val upstreamAuthorUri: String

    val privacyPolicyUri: String

    /** URI to the Open Food Facts Terms of Use document. */
    val openFoodFactsTermsOfUseUri: String

    /** URI to the Open Food Facts Privacy Policy document. */
    val openFoodFactsPrivacyPolicyUri: String

    /** URI to the FoodData Central Privacy Policy document. */
    val foodDataCentralPrivacyPolicyUri: String

    /**
     * AI endpoint credential developer fallback (Milestone 2, Stories 6 and 22). Blank by default
     * and superseded by the user-entered key on the AI settings screen; it is never surfaced in the
     * UI. Never log this value.
     */
    val aiApiKey: String

    /**
     * AI chat-completions endpoint developer fallback (OpenRouter-compatible). Blank by default;
     * superseded by the user-entered endpoint, falling back to the AI slice's domain default.
     */
    val aiEndpoint: String

    /**
     * AI model developer fallback (e.g. "openai/gpt-4o-mini"). Blank by default; superseded by the
     * user-entered model, falling back to the AI slice's domain default.
     */
    val aiModel: String
}
