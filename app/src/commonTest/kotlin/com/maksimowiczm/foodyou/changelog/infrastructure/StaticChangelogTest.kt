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
    fun historyContainsThePreparedReleasePublishedHistoryAndFoundingAnnouncementNewestFirst() {
        val versions = StaticChangelog(config()).versions
        assertEquals(
            listOf("3.2.41.5", "3.2.39.4", "2.23.0", "2.19.1", "1.0 announcement"),
            versions.map { it.version },
        )
        assertEquals(
            listOf(
                LocalDate(2026, 10, 28), LocalDate(2026, 10, 26), LocalDate(2026, 7, 27),
                LocalDate(2026, 7, 26), LocalDate(2026, 7, 24),
            ),
            versions.map { it.date },
        )
        assertTrue(versions.zipWithNext().all { (newer, older) -> newer.date > older.date })
        val latest = versions[0]
        assertTrue(latest.newFeatures.any { "one library" in it })
        assertTrue(latest.newFeatures.any { "text only" in it })
        assertTrue(latest.newFeatures.any { "date, time or meal slot" in it })
        val previous = versions[1]
        assertTrue(previous.newFeatures.any { "Choose which foods" in it })
        assertTrue(previous.changes.any { "Copy, move or delete" in it })
        assertTrue(previous.bugFixes.any { "nutrition details" in it })
        listOf(latest, previous).forEach {
            assertTrue((it.newFeatures + it.changes + it.bugFixes).size in 1..5)
        }
        assertTrue(versions[2].newFeatures.any { "system prompt" in it && "one-off hint" in it })
        assertTrue(versions[2].changes.any { "provider's website" in it })
        assertTrue(versions[3].changes.any { "FoodUs launcher icon" in it })
        assertTrue(versions[3].bugFixes.any { "privacy policy" in it })
        val announcement = versions[4].changes.joinToString(" ")
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
    fun repositorySelectsExactForkVersionEvenWhenUpstreamIdentityDisagrees() = runBlocking {
        listOf("3.2.41.5", "3.2.39.4", "2.23.0", "2.19.1").forEach { fork ->
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
                assertEquals(5, changelog.versions.size)
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
