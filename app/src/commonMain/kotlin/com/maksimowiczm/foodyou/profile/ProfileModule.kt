package com.maksimowiczm.foodyou.profile

import com.maksimowiczm.foodyou.profile.domain.CreateProfileUseCase
import com.maksimowiczm.foodyou.profile.domain.ProfileRepository
import com.maksimowiczm.foodyou.profile.domain.ReconcileProfileKeyUseCase
import com.maksimowiczm.foodyou.profile.domain.RekeyProfileUseCase
import com.maksimowiczm.foodyou.profile.domain.RenameProfileUseCase
import com.maksimowiczm.foodyou.profile.infrastructure.RoomProfileRepository
import com.maksimowiczm.foodyou.profile.infrastructure.room.ProfileDatabase
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.bind
import org.koin.dsl.module

val profileModule = module {
    factory { get<ProfileDatabase>().profileDao }
    factoryOf(::RoomProfileRepository).bind<ProfileRepository>()
    factoryOf(::CreateProfileUseCase)
    factoryOf(::RenameProfileUseCase)
    factoryOf(::ReconcileProfileKeyUseCase)
    factoryOf(::RekeyProfileUseCase)
}
