package com.maksimowiczm.foodyou.fooddiary.infrastructure.room

import com.maksimowiczm.foodyou.common.domain.measurement.*

/** Invalid nonnull metadata is an unavailable read, never a fabricated legacy amount. */
internal fun capturedPortion(type: MeasurementType?, value: Double?): Measurement? {
    if (type == null && value == null) return null
    check(type != null && value != null && value.isFinite() && value > 0.0) {
        "Invalid captured portion metadata"
    }
    return Measurement.from(type, value)
}
