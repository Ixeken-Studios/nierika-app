package com.ixeken.nierika.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ixeken.nierika.core.database.entity.AlbumEntity
import com.ixeken.nierika.core.database.entity.AlbumMediaCrossRef
import kotlinx.coroutines.flow.Flow

data class AlbumWithCount(
    val id: Long,
    val name: String,
    val description: String,
    val coverUri: String?,
    val createdAtMillis: Long,
    val itemCount: Int
)

/**
 * Data Access Object para operaciones con álbumes y relaciones multimedia.
 */
@Dao
interface AlbumDao {
    @Query("""
        SELECT a.id, a.name, a.description, a.coverUri, a.createdAtMillis, 
               COUNT(r.mediaId) as itemCount 
        FROM albums a 
        LEFT JOIN album_media_cross_ref r ON a.id = r.albumId 
        GROUP BY a.id 
        ORDER BY a.createdAtMillis DESC
    """)
    fun getAllAlbumsWithCount(): Flow<List<AlbumWithCount>>

    @Query("SELECT * FROM albums ORDER BY createdAtMillis DESC")
    fun getAllAlbums(): Flow<List<AlbumEntity>>

    @Query("SELECT * FROM albums WHERE id = :albumId")
    fun getAlbumById(albumId: Long): Flow<AlbumEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlbum(album: AlbumEntity): Long

    @Query("UPDATE albums SET coverUri = :coverUri WHERE id = :albumId")
    suspend fun updateCoverUri(albumId: Long, coverUri: String)

    @Query("DELETE FROM albums WHERE id = :albumId")
    suspend fun deleteAlbum(albumId: Long)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCrossRef(ref: AlbumMediaCrossRef)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCrossRefs(refs: List<AlbumMediaCrossRef>)

    @Query("UPDATE albums SET name = :name, description = :description, coverUri = :coverUri WHERE id = :albumId")
    suspend fun updateAlbum(albumId: Long, name: String, description: String, coverUri: String?)

    @Query("DELETE FROM album_media_cross_ref WHERE albumId = :albumId AND mediaId = :mediaId")
    suspend fun deleteCrossRef(albumId: Long, mediaId: Long)

    @Query("DELETE FROM album_media_cross_ref WHERE mediaId = :mediaId")
    suspend fun deleteCrossRefsForMedia(mediaId: Long)

    @Query("DELETE FROM album_media_cross_ref WHERE mediaId IN (:mediaIds)")
    suspend fun deleteCrossRefsForMediaBatch(mediaIds: List<Long>)

    @Query("SELECT mediaId FROM album_media_cross_ref WHERE albumId = :albumId ORDER BY addedAtMillis ASC")
    fun getMediaIdsForAlbum(albumId: Long): Flow<List<Long>>

    @Query("SELECT mediaId FROM album_media_cross_ref WHERE albumId = :albumId ORDER BY addedAtMillis ASC")
    suspend fun getMediaIdsListForAlbum(albumId: Long): List<Long>

    @Query("SELECT * FROM album_media_cross_ref WHERE albumId = :albumId ORDER BY addedAtMillis ASC")
    fun getAlbumMediaCrossRefs(albumId: Long): Flow<List<AlbumMediaCrossRef>>

    @Query("SELECT * FROM album_media_cross_ref WHERE albumId = :albumId ORDER BY addedAtMillis ASC")
    suspend fun getAlbumMediaCrossRefsList(albumId: Long): List<AlbumMediaCrossRef>

    @Query("DELETE FROM album_media_cross_ref WHERE albumId = :albumId")
    suspend fun clearAlbumCrossRefs(albumId: Long)

    @Query("SELECT COUNT(*) FROM album_media_cross_ref WHERE albumId = :albumId")
    suspend fun getMediaCountForAlbum(albumId: Long): Int

    @Query("SELECT DISTINCT mediaId FROM album_media_cross_ref")
    fun getAllAssignedMediaIds(): Flow<List<Long>>

    @Query("SELECT DISTINCT mediaId FROM album_media_cross_ref")
    suspend fun getAllAssignedMediaIdsList(): List<Long>

    @Query("SELECT tag FROM album_tags WHERE albumId = :albumId ORDER BY tag ASC")
    fun getTagsForAlbum(albumId: Long): Flow<List<String>>

    @Query("SELECT tag FROM album_tags WHERE albumId = :albumId ORDER BY tag ASC")
    suspend fun getTagsListForAlbum(albumId: Long): List<String>

    @Query("SELECT DISTINCT tag FROM album_tags ORDER BY tag ASC")
    fun getAllUniqueAlbumTags(): Flow<List<String>>

    @Query("SELECT albumId FROM album_tags WHERE tag = :tag")
    fun getAlbumIdsForTag(tag: String): Flow<List<Long>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAlbumTag(tagEntity: com.ixeken.nierika.core.database.entity.AlbumTagEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAlbumTags(tagEntities: List<com.ixeken.nierika.core.database.entity.AlbumTagEntity>)

    @Query("DELETE FROM album_tags WHERE albumId = :albumId AND tag = :tag")
    suspend fun deleteAlbumTag(albumId: Long, tag: String)

    @Query("DELETE FROM album_tags WHERE albumId = :albumId")
    suspend fun clearTagsForAlbum(albumId: Long)
}
