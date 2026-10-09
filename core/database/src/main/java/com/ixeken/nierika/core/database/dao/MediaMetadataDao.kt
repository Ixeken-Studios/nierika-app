package com.ixeken.nierika.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ixeken.nierika.core.database.entity.MediaMetadataEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object para metadatos locales de medios (favoritos y papelera).
 */
@Dao
interface MediaMetadataDao {
    @Query("SELECT * FROM media_metadata WHERE mediaId = :mediaId")
    suspend fun getMetadata(mediaId: Long): MediaMetadataEntity?

    @Query("SELECT * FROM media_metadata WHERE mediaId IN (:mediaIds)")
    suspend fun getMetadataListByIds(mediaIds: List<Long>): List<MediaMetadataEntity>

    @Query("SELECT * FROM media_metadata")
    fun getAllMetadata(): Flow<List<MediaMetadataEntity>>

    @Query("SELECT * FROM media_metadata")
    suspend fun getAllMetadataList(): List<MediaMetadataEntity>

    @Query("SELECT * FROM media_metadata WHERE isFavorite = 1")
    fun getFavorites(): Flow<List<MediaMetadataEntity>>

    @Query("SELECT * FROM media_metadata WHERE isFavorite = 1")
    suspend fun getFavoritesList(): List<MediaMetadataEntity>

    @Query("SELECT * FROM media_metadata WHERE isTrash = 1 ORDER BY trashedAtMillis DESC")
    fun getTrashItems(): Flow<List<MediaMetadataEntity>>

    @Query("SELECT * FROM media_metadata WHERE isTrash = 1 ORDER BY trashedAtMillis DESC")
    suspend fun getTrashItemsList(): List<MediaMetadataEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertMetadata(metadata: MediaMetadataEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertMetadataList(metadataList: List<MediaMetadataEntity>)

    @Query("UPDATE media_metadata SET isFavorite = :isFavorite WHERE mediaId = :mediaId")
    suspend fun updateFavorite(mediaId: Long, isFavorite: Boolean)

    @Query("UPDATE media_metadata SET isTrash = :isTrash, trashedAtMillis = :trashedAtMillis WHERE mediaId = :mediaId")
    suspend fun updateTrashStatus(mediaId: Long, isTrash: Boolean, trashedAtMillis: Long?)

    @Query("UPDATE media_metadata SET isTrash = :isTrash, trashedAtMillis = :trashedAtMillis, uriString = :uriString WHERE mediaId = :mediaId")
    suspend fun updateTrashStatusWithUri(mediaId: Long, isTrash: Boolean, trashedAtMillis: Long?, uriString: String?)

    @Query("UPDATE media_metadata SET isTrash = 0, trashedAtMillis = NULL WHERE mediaId IN (:mediaIds)")
    suspend fun restoreBatch(mediaIds: List<Long>)

    @Query("DELETE FROM media_metadata WHERE mediaId = :mediaId")
    suspend fun deleteMetadata(mediaId: Long)

    @Query("DELETE FROM media_metadata WHERE mediaId IN (:mediaIds)")
    suspend fun deleteMetadataBatch(mediaIds: List<Long>)

    @Query("DELETE FROM media_metadata WHERE isTrash = 1")
    suspend fun emptyTrash()
}
