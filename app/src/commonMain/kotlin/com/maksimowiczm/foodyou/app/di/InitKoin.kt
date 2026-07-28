package com.maksimowiczm.foodyou.app.di

import com.maksimowiczm.foodyou.ai.aiModule
import com.maksimowiczm.foodyou.app.ui.uiModule
import com.maksimowiczm.foodyou.changelog.changelogModule
import com.maksimowiczm.foodyou.food.foodModule
import com.maksimowiczm.foodyou.food.search.foodSearchModule
import com.maksimowiczm.foodyou.fooddiary.foodDiaryModule
import com.maksimowiczm.foodyou.goals.goalsModule
import com.maksimowiczm.foodyou.importexport.australianfoodcompositiondatabase.importExportAustralianFoodCompositionDatabaseModule
import com.maksimowiczm.foodyou.importexport.importExportModule
import com.maksimowiczm.foodyou.importexport.providermetadata.providerMetadataModule
import com.maksimowiczm.foodyou.importexport.swissfoodcompositiondatabase.importExportSwissFoodCompositionDatabaseModule
import com.maksimowiczm.foodyou.notification.notificationModule
import com.maksimowiczm.foodyou.poll.pollModule
import com.maksimowiczm.foodyou.profile.profileModule
import com.maksimowiczm.foodyou.relay.relayModule
import com.maksimowiczm.foodyou.settings.settingsModule
import com.maksimowiczm.foodyou.sponsorship.sponsorshipModule
import com.maksimowiczm.foodyou.theme.themeModule
import kotlinx.coroutines.CoroutineScope
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration

fun initKoin(applicationCoroutineScope: CoroutineScope, config: KoinAppDeclaration? = null) =
    startKoin {
        config?.invoke(this)

        modules(appModule(applicationCoroutineScope))
        modules(uiModule)
        modules(
            aiModule,
            changelogModule,
            foodModule,
            foodSearchModule,
            foodDiaryModule,
            goalsModule,
            importExportModule,
            importExportSwissFoodCompositionDatabaseModule,
            importExportAustralianFoodCompositionDatabaseModule,
            providerMetadataModule,
            notificationModule,
            pollModule,
            profileModule,
            relayModule,
            settingsModule,
            sponsorshipModule,
            themeModule,
        )
    }
