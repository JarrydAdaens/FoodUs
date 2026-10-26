package com.maksimowiczm.foodyou.importexport.structuredlibrary.infrastructure.room

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.Query

/** No cascade: deleting a mapped target must remain detectable as an explicit reimport conflict. */
@Entity(tableName = "LibraryImportIdentity", primaryKeys = ["namespace", "kind", "externalId"])
data class LibraryIdentityEntity(
    val namespace: String,
    val kind: String,
    val externalId: String,
    val sourceDigest: String,
    val localId: Long,
)

@Dao
interface LibraryIdentityDao {
    @Query("SELECT * FROM LibraryImportIdentity WHERE namespace = :namespace")
    suspend fun getNamespace(namespace: String): List<LibraryIdentityEntity>

    @Insert suspend fun insert(identity: LibraryIdentityEntity)
}
