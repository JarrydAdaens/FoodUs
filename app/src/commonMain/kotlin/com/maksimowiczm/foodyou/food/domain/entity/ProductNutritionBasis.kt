package com.maksimowiczm.foodyou.food.domain.entity

import com.maksimowiczm.foodyou.common.domain.measurement.Measurement
import com.maksimowiczm.foodyou.common.domain.measurement.MeasurementResolver

/** Converts nutrition entered for one explicit reference amount to the stored per-100 basis. */
object ProductNutritionBasis {
    /** Keeps canonical nutrition unchanged when the editor changes its displayed reference basis. */
    fun displayMultiplier(
        previous: Measurement,
        next: Measurement,
        isLiquid: Boolean,
        packageWeight: Double?,
        servingWeight: Double?,
    ): Double? {
        val previousAmount = MeasurementResolver.metricAmount(previous, isLiquid, packageWeight, servingWeight) ?: return null
        val nextAmount = MeasurementResolver.metricAmount(next, isLiquid, packageWeight, servingWeight) ?: return null
        return (nextAmount / previousAmount).takeIf { it.isFinite() && it > 0.0 }
    }

    fun multiplier(
        measurement: Measurement,
        isLiquid: Boolean,
        packageWeight: Double?,
        servingWeight: Double?,
    ): Double? = MeasurementResolver.metricAmount(measurement, isLiquid, packageWeight, servingWeight)
        ?.let { 100.0 / it }
        ?.takeIf { it.isFinite() && it > 0.0 }
}
