package com.maksimowiczm.foodyou.ai

import com.maksimowiczm.foodyou.ai.infrastructure.aiInfrastructureModule
import org.koin.dsl.module

/** AI feature slice (Milestone 2, Story 6): the shared endpoint client and food scanner. */
val aiModule = module { aiInfrastructureModule() }
