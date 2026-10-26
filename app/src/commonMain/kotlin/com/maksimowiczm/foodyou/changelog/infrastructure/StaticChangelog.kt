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

    // Publication dates are UTC; evidence and the founding announcement distinction live in
    // context/implementation-plans/milestone-b/foodus-009/plan.md.
    override val versions: List<Version> =
        listOf(
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
