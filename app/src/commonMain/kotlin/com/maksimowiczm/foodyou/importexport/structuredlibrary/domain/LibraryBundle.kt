package com.maksimowiczm.foodyou.importexport.structuredlibrary.domain

import com.maksimowiczm.foodyou.common.domain.food.*
import com.maksimowiczm.foodyou.common.domain.measurement.Measurement
import kotlinx.serialization.Serializable

/** FoodUs-owned local wire DTOs. Database payload serializers are deliberately separate. */
@Serializable
data class LibraryBundle(
    val format: String,
    val formatVersion: Int,
    val namespace: String,
    val products: List<LibraryProduct> = emptyList(),
    val recipes: List<LibraryRecipe> = emptyList(),
    val meals: List<LibraryMeal> = emptyList(),
)

@Serializable data class LibraryFacts(val units: String, val values: Map<String, Double?> = emptyMap())
@Serializable data class LibraryMetric(val amount: Double, val unit: MetricUnit)
@Serializable data class LibraryReference(val kind: DefinitionKind, val externalId: String)
@Serializable enum class LibraryRecipeMode { DERIVED, REPORTED }
@Serializable enum class LibraryPortionUnit { GRAM, MILLILITER, OUNCE, FLUID_OUNCE, SERVING, PACKAGE }
@Serializable data class LibraryPortion(val amount: Double, val unit: LibraryPortionUnit) {
    fun measurement(): Measurement = when (unit) {
        LibraryPortionUnit.GRAM -> Measurement.Gram(amount)
        LibraryPortionUnit.MILLILITER -> Measurement.Milliliter(amount)
        LibraryPortionUnit.OUNCE -> Measurement.Ounce(amount)
        LibraryPortionUnit.FLUID_OUNCE -> Measurement.FluidOunce(amount)
        LibraryPortionUnit.SERVING -> Measurement.Serving(amount)
        LibraryPortionUnit.PACKAGE -> Measurement.Package(amount)
    }
}

@Serializable data class LibraryMetadata(
    val notes: String? = null,
    val provenance: String? = null,
    val percentDailyValues: Map<String, Double> = emptyMap(),
    val observations: List<NutritionObservation> = emptyList(),
    val textTruncated: Boolean? = null,
) {
    fun captured(reference: DefinitionProvenance? = null) = NutritionMetadata(notes, provenance, percentDailyValues, observations, textTruncated, reference)
}

@Serializable data class LibraryPanel(
    val nutrition: LibraryFacts,
    val basis: ReportedBasis,
    val reference: PortionDescription? = null,
    val metadata: LibraryMetadata? = null,
)

@Serializable data class LibraryProduct(
    val externalId: String,
    val name: String,
    val nutrition: LibraryFacts,
    val basis: ProductFactBasis,
    val reference: PortionDescription,
    val packageMetric: LibraryMetric? = null,
    val servingMetric: LibraryMetric? = null,
    val brand: String? = null,
    val barcode: String? = null,
    val note: String? = null,
    val metadata: LibraryMetadata? = null,
)

@Serializable data class LibraryIngredient(val reference: LibraryReference, val portion: LibraryPortion)
@Serializable data class LibraryComponent(
    val name: String,
    val portion: PortionDescription? = null,
    val nutrition: LibraryFacts? = null,
    val reference: LibraryReference? = null,
    val metadata: LibraryMetadata? = null,
)
@Serializable data class LibraryRecipe(
    val externalId: String,
    val name: String,
    val mode: LibraryRecipeMode,
    val yield: Int? = null,
    val reportedPanel: LibraryPanel? = null,
    val components: List<LibraryComponent> = emptyList(),
    val ingredients: List<LibraryIngredient> = emptyList(),
    val note: String? = null,
    val metadata: LibraryMetadata? = null,
)
@Serializable data class LibraryMealItem(
    val name: String,
    val nutrition: LibraryFacts,
    val portion: PortionDescription? = null,
    val capturedPortion: LibraryPortion? = null,
    val servingCount: Double? = null,
    val weightGrams: Double? = null,
    val reference: LibraryReference? = null,
    val metadata: LibraryMetadata? = null,
)
@Serializable data class LibraryMeal(
    val externalId: String,
    val name: String,
    val items: List<LibraryMealItem>,
    val reportedPanel: LibraryPanel? = null,
    val metadata: LibraryMetadata? = null,
)

data class LibraryKey(val kind: DefinitionKind, val externalId: String)
class ValidatedLibrary internal constructor(
    val bundle: LibraryBundle,
    val digests: Map<LibraryKey, String>,
)

enum class LibraryErrorCode {
    FILE_TOO_LARGE, MALFORMED, VERSION, LIMIT, IDENTITY, REFERENCE, CYCLE, NUMBER, UNIT, BASIS,
    CONFLICT, PERSISTENCE, FILE_UNAVAILABLE,
}
/** Never include input names, values, URIs or decoder exception messages in the diagnostic. */
class LibraryImportException(val code: LibraryErrorCode, val kind: DefinitionKind? = null, val index: Int? = null) :
    IllegalArgumentException(code.name)
