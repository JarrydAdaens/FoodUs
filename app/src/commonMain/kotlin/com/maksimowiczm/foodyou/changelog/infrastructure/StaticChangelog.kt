package com.maksimowiczm.foodyou.changelog.infrastructure

import com.maksimowiczm.foodyou.changelog.domain.Changelog
import com.maksimowiczm.foodyou.changelog.domain.Version
import com.maksimowiczm.foodyou.common.config.AppConfig
import kotlinx.datetime.LocalDate

internal class StaticChangelog(private val appConfig: AppConfig) : Changelog {
    // Match the shared product identity verbatim, including future four-part versions.
    // Unknown local builds must not inherit another release's installed badge.
    override val currentVersion: Version?
        get() = versions.firstOrNull { it.version == appConfig.forkVersionName }

    // User highlights and release maintenance rules: context/wiki/changelog.md.
    // Published dates are UTC. The current candidate uses its planned release date;
    // including it here does not establish publication. Keep older release entries intact.
    override val versions: List<Version> =
        listOf(
            Version(
                version = "3.2.41.5",
                date = LocalDate(2026, 10, 28),
                newFeatures =
                    listOf(
                        "Find your products, recipes and meals in one library.",
                        "Use food search or AI in one screen. AI also works with text only.",
                        "Change the date, time or meal slot while you add food.",
                    ),
                changes =
                    listOf(
                        "See calories, carbs, fat and protein in a consistent layout.",
                        "Follow your progress as you save foods found by AI.",
                    ),
            ),
            Version(
                version = "3.2.39.4",
                date = LocalDate(2026, 10, 26),
                newFeatures =
                    listOf(
                        "Choose which foods to save from an AI scan.",
                        "Save recipes and meals to use again.",
                    ),
                changes =
                    listOf(
                        "Copy, move or delete all foods in a meal slot.",
                        "Import your saved food library.",
                    ),
                bugFixes = listOf("Keep nutrition details when you edit AI results."),
            ),
            Version(
                version = "2.23.0",
                date = LocalDate(2026, 7, 27),
                newFeatures =
                    listOf(
                        "Configure your AI API key, endpoint and model on-device, and validate the connection in AI settings.",
                        "Personalize AI requests with your own system prompt and add a one-off hint to a food scan.",
                    ),
                changes = listOf("Open each food database provider's website from its info link."),
            ),
            Version(
                version = "2.19.1",
                date = LocalDate(2026, 7, 26),
                changes = listOf("Added the FoodUs launcher icon from the owner's artwork."),
                bugFixes =
                    listOf(
                        "Aligned the installed Android version with the FoodUs release version for update checks.",
                        "Pointed the privacy policy link to the published policy.",
                    ),
            ),
            Version(
                version = "1.0 announcement",
                date = LocalDate(2026, 7, 24),
                changes =
                    listOf(
                        "The fork was announced as ACME Food App, starting at version 1.0, " +
                            "and was later renamed FoodUs. This is the founding announcement, " +
                            "not a published 1.0 release. Built on Food You, with a privacy-first, " +
                            "local-first food diary and no accounts or ads."
                    ),
            ),
        )
}
