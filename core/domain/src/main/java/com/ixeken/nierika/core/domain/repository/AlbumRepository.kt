package com.ixeken.nierika.core.domain.repository

import com.ixeken.nierika.core.domain.model.AddToAlbumResult
import com.ixeken.nierika.core.domain.model.Album
import com.ixeken.nierika.core.domain.model.MediaItem
import kotlinx.coroutines.flow.Flow

/**
 * Contrato de repositorio para la gestión de álbumes personalizados.
 */
interface AlbumRepository {
    fun getAllAlbums(): Flow<List<Album>>

    fun getAlbumById(albumId: Long): Flow<Album?>

    fun getMediaForAlbum(albumId: Long): Flow<List<MediaItem>>

    suspend fun createAlbum(
        name: String,
        description: String = "",
        coverUri: String? = null,
        tags: List<String> = emptyList()
    ): Long

    suspend fun updateAlbumCover(albumId: Long, coverUri: String)

    suspend fun updateAlbum(
        albumId: Long,
        name: String,
        description: String,
        coverUri: String?,
        tags: List<String> = emptyList()
    )

    suspend fun deleteAlbum(albumId: Long)

    suspend fun addMediaToAlbum(albumId: Long, mediaId: Long): Boolean

    suspend fun addMediaBatchToAlbum(albumId: Long, mediaIds: List<Long>, firstCoverUri: String? = null): AddToAlbumResult

    suspend fun syncAlbumMedia(albumId: Long, mediaIds: List<Long>, coverUri: String? = null)

    suspend fun removeMediaFromAlbum(albumId: Long, mediaId: Long)

    fun getTagsForAlbum(albumId: Long): Flow<List<String>>

    fun getAllUniqueTags(): Flow<List<String>>

    suspend fun addTagToAlbum(albumId: Long, tag: String)

    suspend fun removeTagFromAlbum(albumId: Long, tag: String)
}
