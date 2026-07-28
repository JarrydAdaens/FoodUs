package com.maksimowiczm.foodyou.profile.domain

import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

/**
 * The one place that decides how an X.509/DER public key is written onto a [Profile]. Creation and
 * reconciliation must agree byte for byte, otherwise every comparison would report a false mismatch
 * and re-key the device forever.
 */
@OptIn(ExperimentalEncodingApi::class)
internal fun ByteArray.encodeProfilePublicKey(): String = Base64.encode(this)
