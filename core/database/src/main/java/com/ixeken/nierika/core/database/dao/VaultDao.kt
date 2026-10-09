package com.ixeken.nierika.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ixeken.nierika.core.database.entity.VaultMediaEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object para la Bóveda Oculta.
 */
@Dao
interface VaultDao {
    @Query("SELECT * FROM vault_media ORDER BY date_added DESC")
    fun getAllVaultMedia(): Flow<List<VaultMediaEntity>>

    @Query("SELECT * FROM vault_media WHERE id = :id LIMIT 1")
    suspend fun getVaultMediaById(id: Long): VaultMediaEntity?

    @Query("SELECT COUNT(*) FROM vault_media")
    fun getVaultMediaCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: VaultMediaEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<VaultMediaEntity>)

    @Query("DELETE FROM vault_media WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM vault_media WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: List<Long>)

    @Delete
    suspend fun delete(entity: VaultMediaEntity)
}
