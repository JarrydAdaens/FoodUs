package com.maksimowiczm.foodyou.profile.domain

/**
 * The device's single local profile (Milestone 3, Story 2).
 *
 * [id] is a randomly generated GUID stamped once at creation and never changed or shown in the UI —
 * it is the primary key every social feature (friends, groups, relay traffic) references. [username]
 * is purely cosmetic: free text, collisions allowed, renameable, and explicitly *not* an identifier.
 *
 * [publicKey] is the Base64 encoding of the X.509/DER messaging public key (Milestone 3, Story 3)
 * and [keyAlgorithm] the transformation a sender must use with it. Base64 rather than raw bytes so
 * the profile stays a value type with sane equality and is ready to be published verbatim. Both are
 * null only for a profile that predates the key pair. The matching private key is never modelled
 * here — see `context/laws.md` §2.
 */
data class Profile(
    val id: String,
    val username: String,
    val createdEpochSeconds: Long,
    val lastEditedEpochSeconds: Long,
    val publicKey: String? = null,
    val keyAlgorithm: String? = null,
)
