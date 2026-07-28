package com.maksimowiczm.foodyou.profile.infrastructure

import com.maksimowiczm.foodyou.profile.domain.Profile
import com.maksimowiczm.foodyou.profile.domain.ProfileRepository
import com.maksimowiczm.foodyou.profile.infrastructure.room.ProfileDao
import com.maksimowiczm.foodyou.profile.infrastructure.room.ProfileEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal class RoomProfileRepository(private val dao: ProfileDao) : ProfileRepository {
    override fun observe(): Flow<Profile?> = dao.observe().map { it?.toDomain() }

    override suspend fun get(): Profile? = dao.get()?.toDomain()

    override suspend fun insert(profile: Profile) {
        dao.insert(profile.toEntity())
    }

    override suspend fun updateUsername(
        id: String,
        username: String,
        lastEditedEpochSeconds: Long,
    ) {
        dao.updateUsername(
            id = id,
            username = username,
            lastEditedEpochSeconds = lastEditedEpochSeconds,
        )
    }
}

private fun ProfileEntity.toDomain(): Profile =
    Profile(
        id = id,
        username = username,
        createdEpochSeconds = createdEpochSeconds,
        lastEditedEpochSeconds = lastEditedEpochSeconds,
    )

private fun Profile.toEntity(): ProfileEntity =
    ProfileEntity(
        id = id,
        username = username,
        createdEpochSeconds = createdEpochSeconds,
        lastEditedEpochSeconds = lastEditedEpochSeconds,
    )
