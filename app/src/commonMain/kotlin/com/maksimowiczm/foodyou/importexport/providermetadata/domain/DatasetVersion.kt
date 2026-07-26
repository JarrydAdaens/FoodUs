package com.maksimowiczm.foodyou.importexport.providermetadata.domain

/**
 * The identifiers that can distinguish one release of a provider dataset from another, from the
 * strongest stable signal to the weakest. A given provider populates only the fields it can supply;
 * [compareDatasetVersion] walks them in precedence order and decides on the first identifier present
 * on both the local and the remote side.
 *
 * @param explicitVersion An explicit release identifier (e.g. "Release 3"). Strongest signal.
 * @param publicationDate The dataset's publication or last-modified date, as reported by the source.
 * @param revisionId An opaque revision token such as an HTTP `ETag`.
 * @param checksum A content hash of the dataset bytes.
 * @param modifiedTime The file modification time, paired with [checksum] for the checksum+mtime tier.
 */
data class DatasetVersion(
    val explicitVersion: String? = null,
    val publicationDate: String? = null,
    val revisionId: String? = null,
    val checksum: String? = null,
    val modifiedTime: String? = null,
)
