package com.maksimowiczm.foodyou.importexport.providermetadata.domain

/** Outcome of comparing a locally installed dataset version against a remote one. */
enum class DatasetComparison {
    /** The strongest shared identifier matches; the local dataset is current. */
    UpToDate,

    /** The strongest shared identifier differs; a newer dataset is available. */
    UpdateAvailable,

    /** No identifier is present on both sides, so the comparison cannot decide either way. */
    Indeterminate,
}

/**
 * Compares two dataset versions using the strongest stable identifier available on both sides,
 * in precedence order: explicit version → publication date → revision id → checksum + mtime →
 * checksum. The first tier populated on both [local] and [remote] is decisive; equal values mean
 * [DatasetComparison.UpToDate] and differing values mean [DatasetComparison.UpdateAvailable]. When no
 * tier is shared the result is [DatasetComparison.Indeterminate] and the caller must not assume the
 * dataset is current.
 */
fun compareDatasetVersion(local: DatasetVersion, remote: DatasetVersion): DatasetComparison {
    val tiers: List<(DatasetVersion) -> String?> =
        listOf(
            { it.explicitVersion },
            { it.publicationDate },
            { it.revisionId },
            { it.checksumWithMtime() },
            { it.checksum },
        )

    for (identifierOf in tiers) {
        val localId = identifierOf(local)
        val remoteId = identifierOf(remote)
        if (localId != null && remoteId != null) {
            return if (localId == remoteId) {
                DatasetComparison.UpToDate
            } else {
                DatasetComparison.UpdateAvailable
            }
        }
    }

    return DatasetComparison.Indeterminate
}

/** The checksum+mtime tier is only decisive when both parts are present. */
private fun DatasetVersion.checksumWithMtime(): String? =
    if (checksum != null && modifiedTime != null) "$checksum|$modifiedTime" else null
