package com.maksimowiczm.foodyou.profile.infrastructure.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ProfileDao {
    @Query("SELECT * FROM Profile LIMIT 1") fun observe(): Flow<ProfileEntity?>

    @Query("SELECT * FROM Profile LIMIT 1") suspend fun get(): ProfileEntity?

    @Insert suspend fun insert(entity: ProfileEntity)

    /** Renames a profile. Deliberately never touches `id` or `createdEpochSeconds`. */
    @Query(
        """
        UPDATE Profile
        SET username = :username, lastEditedEpochSeconds = :lastEditedEpochSeconds
        WHERE id = :id
        """
    )
    suspend fun updateUsername(id: String, username: String, lastEditedEpochSeconds: Long)
}
