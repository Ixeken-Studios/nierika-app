package com.ixeken.nierika.core.domain.repository

import androidx.paging.PagingData
import com.ixeken.nierika.core.domain.model.MediaItem
import com.ixeken.nierika.core.domain.model.QuickFilter
import com.ixeken.nierika.core.domain.model.SortOrder
import kotlinx.coroutines.flow.Flow

/**
 * Contrato de repositorio para la consulta y gestión de elementos multimedia.
 */
interface MediaRepository {
    fun getMediaPaged(
        quickFilter: QuickFilter = QuickFilter.ALL,
        sortOrder: SortOrder = SortOrder.DESCENDING
    ): Flow<PagingData<MediaItem>>

    fun getMediaForCategory(quickFilter: QuickFilter): Flow<List<MediaItem>>

    fun getAllMedia(): Flow<List<MediaItem>>

    fun getTrashMedia(): Flow<List<MediaItem>>

    suspend fun toggleFavorite(mediaId: Long, isFavorite: Boolean)

    suspend fun moveToTrash(mediaId: Long, uri: String? = null)

    suspend fun moveToTrashBatch(mediaIds: List<Long>, uris: List<String> = emptyList())

    suspend fun restoreFromTrash(mediaId: Long)

    suspend fun restoreBatch(mediaIds: List<Long>) {
        mediaIds.forEach { restoreFromTrash(it) }
    }

    suspend fun deletePermanently(mediaId: Long, uriString: String? = null)

    suspend fun deletePermanentlyBatch(mediaIds: List<Long>, uris: List<String?> = emptyList()) {
        mediaIds.forEachIndexed { idx, id -> deletePermanently(id, uris.getOrNull(idx)) }
    }

    suspend fun emptyTrash()

    suspend fun duplicateMedia(mediaItem: MediaItem): String?

    fun getTagsForMedia(mediaId: Long): Flow<List<String>>

    suspend fun addTagToMedia(mediaId: Long, tag: String)

    suspend fun removeTagFromMedia(mediaId: Long, tag: String)

    fun getAllUniqueTags(): Flow<List<String>>

    fun getMediaByTag(tag: String): Flow<List<MediaItem>>
}
