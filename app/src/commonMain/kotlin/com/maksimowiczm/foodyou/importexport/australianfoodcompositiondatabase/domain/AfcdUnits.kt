package com.maksimowiczm.foodyou.importexport.australianfoodcompositiondatabase.domain

/**
 * Deterministic unit normalization from the Australian Food Composition Database's published units
 * into Food You's canonical domain representation.
 *
 * Food You stores macronutrients in grams and energy in kilocalories, and its Room layer expects
 * minerals expressed in grams (it multiplies by 1000 when persisting `*Milli` columns). AFCD
 * publishes energy in kilojoules and minerals in milligrams, so both must be converted on import.
 *
 * All functions are pure and null-preserving: a missing source value yields a missing result, never
 * a fabricated zero.
 */
object AfcdUnits {

    /** Thermochemical conversion factor: 1 kcal = 4.184 kJ. */
    const val KILOJOULES_PER_KILOCALORIE: Double = 4.184

    /** Converts kilojoules to kilocalories. Returns null when [kilojoules] is null. */
    fun kilojoulesToKilocalories(kilojoules: Double?): Double? =
        kilojoules?.let { it / KILOJOULES_PER_KILOCALORIE }

    /** Converts milligrams to grams. Returns null when [milligrams] is null. */
    fun milligramsToGrams(milligrams: Double?): Double? = milligrams?.let { it / 1_000.0 }

    /**
     * Parses an AFCD numeric cell. AFCD uses blanks and occasionally text markers for
     * missing/undetermined values; anything that is not a plain number is treated as absent so no
     * value is invented.
     */
    fun parseNumber(raw: String?): Double? {
        val trimmed = raw?.trim() ?: return null
        if (trimmed.isEmpty()) return null
        return trimmed.replace(",", "").toDoubleOrNull()
    }
}
