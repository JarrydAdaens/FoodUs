package com.maksimowiczm.foodyou.common.domain.measurement

import com.maksimowiczm.foodyou.common.domain.food.WeightCalculator

/** Resolves a new portion in grams for solids or milliliters for liquids; never assumes density. */
object MeasurementResolver {
    fun metricAmount(
        measurement: Measurement,
        isLiquid: Boolean,
        totalWeight: Double?,
        servingWeight: Double?,
    ): Double? {
        if (!measurement.rawValue.isFinite() || measurement.rawValue <= 0.0) return null
        when (measurement) {
            is Measurement.Gram, is Measurement.Ounce -> if (isLiquid) return null
            is Measurement.Milliliter, is Measurement.FluidOunce -> if (!isLiquid) return null
            is Measurement.Package -> if (!isKnownBasis(totalWeight)) return null
            is Measurement.Serving -> if (!isKnownBasis(servingWeight)) return null
        }
        return WeightCalculator.calculateWeight(measurement, totalWeight, servingWeight)
            ?.takeIf { it.isFinite() && it > 0.0 }
    }

    fun isKnownBasis(value: Double?): Boolean = value != null && value.isFinite() && value > 0.0

    fun supportedTypes(isLiquid: Boolean, totalWeight: Double?, servingWeight: Double?): List<MeasurementType> =
        MeasurementType.entries.filter {
            when (it) {
                MeasurementType.Gram, MeasurementType.Ounce -> !isLiquid
                MeasurementType.Milliliter, MeasurementType.FluidOunce -> isLiquid
                MeasurementType.Package -> isKnownBasis(totalWeight)
                MeasurementType.Serving -> isKnownBasis(servingWeight)
            }
        }
}
