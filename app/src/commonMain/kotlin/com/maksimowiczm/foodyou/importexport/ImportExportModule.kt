package com.maksimowiczm.foodyou.importexport

import com.maksimowiczm.foodyou.importexport.domain.importExportDomainModule
import org.koin.dsl.module
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.bind
import com.maksimowiczm.foodyou.importexport.structuredlibrary.domain.LibraryImporter
import com.maksimowiczm.foodyou.importexport.structuredlibrary.infrastructure.RoomLibraryImporter
import com.maksimowiczm.foodyou.importexport.structuredlibrary.infrastructure.StructuredLibraryParser

val importExportModule = module {
    importExportDomainModule()
    factoryOf(::StructuredLibraryParser)
    factoryOf(::RoomLibraryImporter).bind<LibraryImporter>()
}
