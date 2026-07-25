package com.maksimowiczm.foodyou.importexport.providermetadata

import com.maksimowiczm.foodyou.importexport.providermetadata.domain.DatasetComparison
import com.maksimowiczm.foodyou.importexport.providermetadata.domain.DatasetVersion
import com.maksimowiczm.foodyou.importexport.providermetadata.domain.compareDatasetVersion
import kotlin.test.Test
import kotlin.test.assertEquals

class DatasetComparisonTest {

    @Test
    fun explicitVersionWinsOverWeakerButDifferingSignals() {
        // Explicit version matches, so a differing publication date and checksum are ignored.
        val local =
            DatasetVersion(
                explicitVersion = "Release 3",
                publicationDate = "Mon, 22 Dec 2025 23:20:49 GMT",
                checksum = "aaa",
                modifiedTime = "Mon, 22 Dec 2025 23:20:49 GMT",
            )
        val remote =
            DatasetVersion(
                explicitVersion = "Release 3",
                publicationDate = "Tue, 23 Dec 2025 00:00:00 GMT",
                checksum = "bbb",
                modifiedTime = "Tue, 23 Dec 2025 00:00:00 GMT",
            )

        assertEquals(DatasetComparison.UpToDate, compareDatasetVersion(local, remote))
    }

    @Test
    fun differingExplicitVersionReportsUpdateAvailable() {
        val local = DatasetVersion(explicitVersion = "Release 3")
        val remote = DatasetVersion(explicitVersion = "Release 4")

        assertEquals(DatasetComparison.UpdateAvailable, compareDatasetVersion(local, remote))
    }

    @Test
    fun fallsThroughToPublicationDateWhenVersionMissingRemotely() {
        // Remote HEAD requests expose Last-Modified but not the release number, so comparison
        // falls to the publication-date tier — the AFCD runtime case.
        val local =
            DatasetVersion(
                explicitVersion = "Release 3",
                publicationDate = "Mon, 22 Dec 2025 23:20:49 GMT",
            )
        val sameDate =
            DatasetVersion(publicationDate = "Mon, 22 Dec 2025 23:20:49 GMT", revisionId = "\"etag\"")
        val newerDate =
            DatasetVersion(publicationDate = "Wed, 15 Jul 2026 10:00:00 GMT", revisionId = "\"etag\"")

        assertEquals(DatasetComparison.UpToDate, compareDatasetVersion(local, sameDate))
        assertEquals(DatasetComparison.UpdateAvailable, compareDatasetVersion(local, newerDate))
    }

    @Test
    fun usesChecksumAndRevisionTiersWhenStrongerSignalsAbsent() {
        // No version or publication date on either side: revision id decides first.
        val local = DatasetVersion(revisionId = "\"v1\"", checksum = "aaa", modifiedTime = "t1")
        val differingRevision =
            DatasetVersion(revisionId = "\"v2\"", checksum = "aaa", modifiedTime = "t1")
        assertEquals(
            DatasetComparison.UpdateAvailable,
            compareDatasetVersion(local, differingRevision),
        )

        // Without revision ids, the checksum+mtime pair is the deciding tier.
        val checksumLocal = DatasetVersion(checksum = "aaa", modifiedTime = "t1")
        val checksumRemote = DatasetVersion(checksum = "aaa", modifiedTime = "t1")
        assertEquals(DatasetComparison.UpToDate, compareDatasetVersion(checksumLocal, checksumRemote))
    }

    @Test
    fun returnsIndeterminateWhenNoIdentifierIsShared() {
        val local = DatasetVersion(explicitVersion = "Release 3")
        val remote = DatasetVersion(checksum = "only-a-checksum")

        assertEquals(DatasetComparison.Indeterminate, compareDatasetVersion(local, remote))
    }
}
