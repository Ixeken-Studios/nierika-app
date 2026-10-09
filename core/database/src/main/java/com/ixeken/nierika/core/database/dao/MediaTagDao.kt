package com.ixeken.nierika.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ixeken.nierika.core.database.entity.MediaTagEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object para gestión y consulta de etiquetas (#tags) asociadas a fotos y vídeos.
 */
@Dao
interface MediaTagDao {
    @Query("SELECT tag FROM media_tags WHERE mediaId = :mediaId ORDER BY tag ASC")
    fun getTagsForMedia(mediaId: Long): Flow<List<String>>

    @Query("SELECT tag FROM media_tags WHERE mediaId = :mediaId ORDER BY tag ASC")
    suspend fun getTagsListForMedia(mediaId: Long): List<String>

    @Query("SELECT DISTINCT tag FROM media_tags ORDER BY tag ASC")
    fun getAllTags(): Flow<List<String>>

    @Query("SELECT * FROM media_tags")
    fun getAllMediaTags(): Flow<List<MediaTagEntity>>

    @Query("SELECT * FROM media_tags")
    suspend fun getAllMediaTagsList(): List<MediaTagEntity>

    @Query("SELECT mediaId FROM media_tags WHERE tag = :tag")
    fun getMediaIdsForTag(tag: String): Flow<List<Long>>

    @Query("SELECT mediaId FROM media_tags WHERE tag = :tag")
    suspend fun getMediaIdsListForTag(tag: String): List<Long>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTag(tagEntity: MediaTagEntity)

    @Query("DELETE FROM media_tags WHERE mediaId = :mediaId AND tag = :tag")
    suspend fun deleteTag(mediaId: Long, tag: String)

    @Query("DELETE FROM media_tags WHERE mediaId = :mediaId")
    suspend fun clearTagsForMedia(mediaId: Long)
}
