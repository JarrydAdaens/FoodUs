package com.maksimowiczm.foodyou.common.config

interface AppConfig {

    /** Upstream Food You version name this fork derives from (e.g. "3.4.9"). */
    val versionName: String

    /** FoodUs fork version name, layered on top of [versionName] (e.g. "1.0.0"). */
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
     * AI endpoint credential baked into private builds (Milestone 2, Story 6). Blank when no key was
     * injected at build time, which disables the AI scanning call. Never log this value.
     */
    val aiApiKey: String

    /** AI chat-completions endpoint URL (OpenRouter-compatible). */
    val aiEndpoint: String

    /** Vision-capable model identifier used for AI food scanning (e.g. "openai/gpt-4o-mini"). */
    val aiModel: String
}
