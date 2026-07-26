package com.maksimowiczm.foodyou.importexport.australianfoodcompositiondatabase

/**
 * Static configuration for the Australian Food Composition Database provider.
 *
 * The dataset is published by Food Standards Australia New Zealand (FSANZ) as a direct, key-free
 * `.xlsx` download under CC BY-SA 3.0 AU. The release-dated URL is kept here rather than inline so a
 * future release bump (or the Story 17 runtime release detection) has a single place to change.
 */
object AustralianFoodCompositionDatabaseConfig {

    /** Current AFCD release, used as the installed-version identifier. */
    const val RELEASE_LABEL: String = "Release 3"

    /** Direct download URL for the AFCD nutrient profiles workbook (per-100 g nutrient values). */
    const val NUTRIENT_WORKBOOK_URL: String =
        "https://www.foodstandards.gov.au/sites/default/files/2025-12/" +
            "AFCD%20Release%203%20-%20Nutrient%20profiles.xlsx"

    /** Public landing page for the dataset, used as per-row provenance. */
    const val SOURCE_URL: String =
        "https://www.foodstandards.gov.au/science-data/food-nutrient-databases/afcd/" +
            "australian-food-composition-database-download-excel-files"

    /**
     * Attribution required by the AFCD Data User Licence (CC BY-SA 3.0 AU). Shown in the provider
     * UI alongside the imported dataset.
     */
    const val ATTRIBUTION: String =
        "Australian Food Composition Database, Food Standards Australia New Zealand, " +
            "licensed under CC BY-SA 3.0 AU."
}
