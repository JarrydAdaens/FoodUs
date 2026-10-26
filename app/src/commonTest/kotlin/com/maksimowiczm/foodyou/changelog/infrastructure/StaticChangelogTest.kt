package com.maksimowiczm.foodyou.changelog.infrastructure

import com.maksimowiczm.foodyou.common.config.AppConfig
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.LocalDate

class StaticChangelogTest {
    @Test
    fun historyContainsExactlyTheVerifiedReleasesAndFoundingAnnouncementNewestFirst() {
        val versions = StaticChangelog(config()).versions
        assertEquals(listOf("2.23.0", "2.19.1", "1.0 announcement"), versions.map { it.version })
        assertEquals(
            listOf(LocalDate(2026, 7, 27), LocalDate(2026, 7, 26), LocalDate(2026, 7, 24)),
            versions.map { it.date },
        )
        assertTrue(versions.zipWithNext().all { (newer, older) -> newer.date > older.date })
        val latest = versions[0]
        assertTrue(latest.newFeatures.any { "AI API key, endpoint and model on-device" in it })
        assertTrue(latest.newFeatures.any { "system prompt" in it && "one-off hint" in it })
        assertTrue(latest.changes.any { "provider's website" in it })
        val previous = versions[1]
        assertTrue(previous.changes.any { "FoodUs launcher icon" in it })
        assertTrue(previous.bugFixes.any { "installed Android version" in it })
        assertTrue(previous.bugFixes.any { "privacy policy" in it })
        val announcement = versions[2].changes.joinToString(" ")
        assertTrue("ACME Food App" in announcement && "later renamed FoodUs" in announcement)
        assertTrue("not a published 1.0 release" in announcement)
        versions.forEach { version ->
            val text = (version.newFeatures + version.changes + version.bugFixes +
                version.translations + listOfNotNull(version.notes)).joinToString(" ")
            assertTrue(text.isNotBlank())
            assertFalse("TBD" in text || "TBD" in version.version)
            assertFalse(version.isPreview)
        }
    }

    @Test
    fun repositorySelectsPublishedForkVersionEvenWhenUpstreamIdentityDisagrees() = runBlocking {
        listOf("2.23.0", "2.19.1").forEach { fork ->
            val changelog = ChangelogRepositoryImpl(config(fork = fork, upstream = "3.4.9"))
                .observe().first()
            assertEquals(fork, changelog.currentVersion?.version)
        }
    }

    @Test
    fun upstreamMatchCannotImpersonateAnUnknownForkBuild() {
        assertNull(StaticChangelog(config(fork = "3.5.0", upstream = "2.23.0")).currentVersion)
    }

    @Test
    fun unknownAndFutureVersionsNeverFallBackOrTruncateTheirIdentity() {
        listOf("3.5.0", "1.3.9.0", "2.23.0.1", "2.23.0-dev", "2.23.0-preview", "")
            .forEach { fork ->
                val changelog = StaticChangelog(config(fork = fork))
                assertNull(changelog.currentVersion, "Unexpected current entry for $fork")
                assertEquals(3, changelog.versions.size)
            }
    }

    // BuildConfig's shared FORK_VERSION_NAME is unsuffixed even in a debug APK. A suffix
    // presented as part of AppConfig is therefore an unknown identity, not something to strip.
    private fun config(fork: String = "3.5.0", upstream: String = "3.4.9"): AppConfig =
        object : AppConfig {
            override val versionName = upstream
            override val forkVersionName = fork
            override val contactEmailUri = ""
            override val translationUri = ""
            override val sourceCodeUri = ""
            override val issueTrackerUri = ""
            override val upstreamAuthorUri = ""
            override val privacyPolicyUri = ""
            override val openFoodFactsTermsOfUseUri = ""
            override val openFoodFactsPrivacyPolicyUri = ""
            override val foodDataCentralPrivacyPolicyUri = ""
            override val openFoodFactsWebsiteUri = ""
            override val foodDataCentralWebsiteUri = ""
            override val swissFoodCompositionDatabaseWebsiteUri = ""
            override val australianFoodCompositionDatabaseWebsiteUri = ""
            override val aiApiKey = ""
            override val aiEndpoint = ""
            override val aiModel = ""
        }
}
