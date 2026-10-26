package com.maksimowiczm.foodyou.app.ui.database

import com.maksimowiczm.foodyou.app.ui.database.australianfoodcompositiondatabase.australianFoodCompositionDatabaseModule
import com.maksimowiczm.foodyou.app.ui.database.exportcsvproducts.exportCsvProductsModule
import com.maksimowiczm.foodyou.app.ui.database.externaldatabases.externalDatabasesModule
import com.maksimowiczm.foodyou.app.ui.database.importcsvproducts.importCsvProductsModule
import com.maksimowiczm.foodyou.app.ui.database.swissfoodcompositiondatabase.swissFoodCompositionDatabaseModule
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModelOf
import com.maksimowiczm.foodyou.app.ui.database.importlibrary.ImportLibraryViewModel

fun Module.database() {
    australianFoodCompositionDatabaseModule()
    exportCsvProductsModule()
    externalDatabasesModule()
    importCsvProductsModule()
    viewModelOf(::ImportLibraryViewModel)
    swissFoodCompositionDatabaseModule()
}
