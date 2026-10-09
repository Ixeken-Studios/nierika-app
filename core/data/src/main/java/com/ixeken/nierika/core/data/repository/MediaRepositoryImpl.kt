package com.ixeken.nierika.core.data.repository

import android.content.ContentUris
import android.provider.MediaStore
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingSource
import androidx.paging.PagingState
import androidx.paging.filter
import androidx.paging.map
import com.ixeken.nierika.core.database.dao.AlbumDao
import com.ixeken.nierika.core.database.dao.MediaMetadataDao
import com.ixeken.nierika.core.database.dao.MediaTagDao
import com.ixeken.nierika.core.database.entity.MediaMetadataEntity
import com.ixeken.nierika.core.database.entity.MediaTagEntity
import com.ixeken.nierika.core.domain.model.MediaItem
import com.ixeken.nierika.core.domain.model.QuickFilter
import com.ixeken.nierika.core.domain.model.SortOrder
import com.ixeken.nierika.core.domain.repository.MediaRepository
import com.ixeken.nierika.core.mediastore.MediaStoreFetcher
import com.ixeken.nierika.core.mediastore.RawMediaItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * PagingSource para la carga paginada de imágenes desde MediaStore.
 */
class MediaPagingSource(
    private val fetcher: MediaStoreFetcher,
    private val sortOrder: SortOrder
) : PagingSource<Int, RawMediaItem>() {

    override fun getRefreshKey(state: PagingState<Int, RawMediaItem>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(anchorPage.data.size)
                ?: anchorPage?.nextKey?.minus(anchorPage.data.size)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, RawMediaItem> {
        val currentOffset = params.key ?: 0
        val loadSize = params.loadSize

        return try {
            val items = fetcher.fetchMedia(
                offset = currentOffset,
                limit = loadSize,
                sortAscending = sortOrder == SortOrder.ASCENDING
            )
            LoadResult.Page(
                data = items,
                prevKey = if (currentOffset == 0) null else maxOf(0, currentOffset - loadSize),
                nextKey = if (items.isEmpty() || items.size < loadSize) null else currentOffset + items.size
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }
}

/**
 * Implementación del repositorio de medios con aislamiento entre MediaStore, Room y Dominio.
 */
class MediaRepositoryImpl @Inject constructor(
    private val fetcher: MediaStoreFetcher,
    private val metadataDao: MediaMetadataDao,
    private val tagDao: MediaTagDao,
    private val albumDao: AlbumDao
) : MediaRepository {

    override fun getMediaPaged(
        quickFilter: QuickFilter,
        sortOrder: SortOrder
    ): Flow<PagingData<MediaItem>> {
        return Pager(
            config = PagingConfig(
                pageSize = 60,
                initialLoadSize = 60,
                prefetchDistance = 20,
                enablePlaceholders = false
            ),
            pagingSourceFactory = { MediaPagingSource(fetcher, sortOrder) }
        ).flow.map { pagingData ->
            pagingData.map { raw ->
                val metadata = metadataDao.getMetadata(raw.id)
                MediaItem(
                    id = raw.id,
                    uri = raw.uriString,
                    displayName = raw.displayName,
                    mimeType = raw.mimeType,
                    dateTakenMillis = raw.dateTakenMillis,
                    dateModifiedMillis = raw.dateModifiedMillis,
                    sizeBytes = raw.sizeBytes,
                    width = raw.width,
                    height = raw.height,
                    isFavorite = metadata?.isFavorite ?: false,
                    isTrash = metadata?.isTrash ?: false,
                    bucketName = raw.bucketName,
                    relativePath = raw.relativePath,
                    durationMillis = raw.durationMillis,
                    trashedAtMillis = metadata?.trashedAtMillis ?: 0L
                )
            }.filter { item ->
                when (quickFilter) {
                    QuickFilter.ALL -> !item.isTrash
                    QuickFilter.FAVORITES -> !item.isTrash && item.isFavorite
                    QuickFilter.VIDEOS -> !item.isTrash && item.isVideo
                    QuickFilter.CAMERA -> !item.isTrash && item.isCameraPhoto
                    QuickFilter.SCREENSHOTS -> !item.isTrash && item.isScreenshot
                    QuickFilter.TRASH -> item.isTrash
                    QuickFilter.NOT_IN_ALBUM -> !item.isTrash
                }
            }
        }
    }

    override fun getMediaForCategory(quickFilter: QuickFilter): Flow<List<MediaItem>> = flow {
        val rawList = fetcher.fetchAllMedia()
        combine(
            tagDao.getAllMediaTags(),
            albumDao.getAllAssignedMediaIds(),
            metadataDao.getAllMetadata()
        ) { allTags, assignedIds, allMetadata ->
            val metadataMap = allMetadata.associateBy { it.mediaId }
            val assignedSet = assignedIds.toSet()
            val tagsByMediaId = allTags.groupBy({ it.mediaId }, { it.tag })
            val mapped = rawList.map { raw ->
                val metadata = metadataMap[raw.id]
                val tags = tagsByMediaId[raw.id] ?: emptyList()
                MediaItem(
                    id = raw.id,
                    uri = raw.uriString,
                    displayName = raw.displayName,
                    mimeType = raw.mimeType,
                    dateTakenMillis = raw.dateTakenMillis,
                    dateModifiedMillis = raw.dateModifiedMillis,
                    sizeBytes = raw.sizeBytes,
                    width = raw.width,
                    height = raw.height,
                    isFavorite = metadata?.isFavorite ?: false,
                    isTrash = metadata?.isTrash ?: false,
                    bucketName = raw.bucketName,
                    relativePath = raw.relativePath,
                    tags = tags,
                    durationMillis = raw.durationMillis,
                    trashedAtMillis = metadata?.trashedAtMillis ?: 0L
                )
            }.filter { item ->
                when (quickFilter) {
                    QuickFilter.ALL -> !item.isTrash
                    QuickFilter.FAVORITES -> !item.isTrash && item.isFavorite
                    QuickFilter.VIDEOS -> !item.isTrash && item.isVideo
                    QuickFilter.CAMERA -> !item.isTrash && item.isCameraPhoto
                    QuickFilter.SCREENSHOTS -> !item.isTrash && item.isScreenshot
                    QuickFilter.TRASH -> item.isTrash
                    QuickFilter.NOT_IN_ALBUM -> !item.isTrash && item.id !in assignedSet
                }
            }.let { filtered ->
                if (quickFilter == QuickFilter.TRASH) {
                    filtered.sortedByDescending { it.effectiveTrashedAtMillis }
                } else {
                    filtered
                }
            }
            mapped
        }.collect { mapped ->
            emit(mapped)
        }
    }.flowOn(Dispatchers.IO)

    override fun getAllMedia(): Flow<List<MediaItem>> {
        return getMediaForCategory(QuickFilter.ALL)
    }

    override fun getTrashMedia(): Flow<List<MediaItem>> = flow {
        combine(
            metadataDao.getTrashItems(),
            tagDao.getAllMediaTags()
        ) { trashEntities, allTags ->
            val tagsByMediaId = allTags.groupBy({ it.mediaId }, { it.tag })
            val rawTrash = fetcher.fetchTrashMedia()
            if (rawTrash.isNotEmpty()) {
                val metaMap = trashEntities.associateBy { it.mediaId }
                rawTrash.map { raw ->
                    val metadata = metaMap[raw.id]
                    val tags = tagsByMediaId[raw.id] ?: emptyList()
                    MediaItem(
                        id = raw.id,
                        uri = raw.uriString,
                        displayName = raw.displayName,
                        mimeType = raw.mimeType,
                        dateTakenMillis = raw.dateTakenMillis,
                        dateModifiedMillis = raw.dateModifiedMillis,
                        sizeBytes = raw.sizeBytes,
                        width = raw.width,
                        height = raw.height,
                        isFavorite = metadata?.isFavorite ?: false,
                        isTrash = true,
                        bucketName = raw.bucketName,
                        relativePath = raw.relativePath,
                        tags = tags,
                        dateExpiresMillis = raw.dateExpiresMillis,
                        durationMillis = raw.durationMillis,
                        trashedAtMillis = metadata?.trashedAtMillis ?: 0L
                    )
                }.sortedByDescending { it.effectiveTrashedAtMillis }
            } else {
                trashEntities.map { meta ->
                    val resolvedUri = if (meta.uriString.isNullOrBlank()) {
                        ContentUris.withAppendedId(
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                            meta.mediaId
                        ).toString()
                    } else {
                        meta.uriString!!
                    }
                    val tags = tagsByMediaId[meta.mediaId] ?: emptyList()
                    MediaItem(
                        id = meta.mediaId,
                        uri = resolvedUri,
                        displayName = "Trash Item",
                        mimeType = "image/*",
                        dateTakenMillis = meta.trashedAtMillis ?: 0L,
                        dateModifiedMillis = 0L,
                        sizeBytes = 0L,
                        width = 0,
                        height = 0,
                        isFavorite = meta.isFavorite,
                        isTrash = true,
                        tags = tags,
                        trashedAtMillis = meta.trashedAtMillis ?: 0L
                    )
                }.sortedByDescending { it.effectiveTrashedAtMillis }
            }
        }.collect { emit(it) }
    }.flowOn(Dispatchers.IO)

    override suspend fun toggleFavorite(mediaId: Long, isFavorite: Boolean) {
        val existing = metadataDao.getMetadata(mediaId)
        if (existing != null) {
            metadataDao.updateFavorite(mediaId, isFavorite)
        } else {
            metadataDao.upsertMetadata(
                MediaMetadataEntity(mediaId = mediaId, isFavorite = isFavorite)
            )
        }
    }

    override suspend fun moveToTrash(mediaId: Long, uri: String?) {
        val resolvedUri = uri ?: ContentUris.withAppendedId(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            mediaId
        ).toString()

        val existing = metadataDao.getMetadata(mediaId)
        if (existing != null) {
            metadataDao.updateTrashStatusWithUri(
                mediaId = mediaId,
                isTrash = true,
                trashedAtMillis = System.currentTimeMillis(),
                uriString = if (!uri.isNullOrBlank()) uri else existing.uriString ?: resolvedUri
            )
        } else {
            metadataDao.upsertMetadata(
                MediaMetadataEntity(
                    mediaId = mediaId,
                    isTrash = true,
                    trashedAtMillis = System.currentTimeMillis(),
                    uriString = resolvedUri
                )
            )
        }
    }

    override suspend fun moveToTrashBatch(mediaIds: List<Long>, uris: List<String>) = withContext(Dispatchers.IO) {
        if (mediaIds.isEmpty()) return@withContext
        val existingMap = metadataDao.getMetadataListByIds(mediaIds).associateBy { it.mediaId }
        val now = System.currentTimeMillis()
        val entities = mediaIds.mapIndexed { index, id ->
            val uri = uris.getOrNull(index)
            val existing = existingMap[id]
            val resolvedUri = if (!uri.isNullOrBlank()) {
                uri
            } else {
                existing?.uriString ?: ContentUris.withAppendedId(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    id
                ).toString()
            }
            MediaMetadataEntity(
                mediaId = id,
                isFavorite = existing?.isFavorite ?: false,
                isTrash = true,
                trashedAtMillis = now,
                uriString = resolvedUri
            )
        }
        metadataDao.upsertMetadataList(entities)
    }

    override suspend fun restoreFromTrash(mediaId: Long) = withContext(Dispatchers.IO) {
        metadataDao.updateTrashStatus(mediaId, isTrash = false, trashedAtMillis = null)
    }

    override suspend fun restoreBatch(mediaIds: List<Long>) = withContext(Dispatchers.IO) {
        if (mediaIds.isEmpty()) return@withContext
        metadataDao.restoreBatch(mediaIds)
    }

    override suspend fun deletePermanently(mediaId: Long, uriString: String?) = withContext(Dispatchers.IO) {
        metadataDao.deleteMetadata(mediaId)
        albumDao.deleteCrossRefsForMedia(mediaId)
        if (!uriString.isNullOrEmpty()) {
            try {
                fetcher.deleteMediaPermanently(uriString)
            } catch (_: Exception) {}
        }
    }

    override suspend fun deletePermanentlyBatch(mediaIds: List<Long>, uris: List<String?>) = withContext(Dispatchers.IO) {
        if (mediaIds.isEmpty()) return@withContext
        metadataDao.deleteMetadataBatch(mediaIds)
        albumDao.deleteCrossRefsForMediaBatch(mediaIds)
        uris.filterNotNull().forEach { uri ->
            if (uri.isNotEmpty()) {
                try {
                    fetcher.deleteMediaPermanently(uri)
                } catch (_: Exception) {}
            }
        }
    }

    override suspend fun emptyTrash() = withContext(Dispatchers.IO) {
        val trashItems = metadataDao.getTrashItemsList()
        val mediaIds = trashItems.map { it.mediaId }
        trashItems.forEach { item ->
            val uri = item.uriString
            if (!uri.isNullOrEmpty()) {
                try {
                    fetcher.deleteMediaPermanently(uri)
                } catch (_: Exception) {}
            }
        }
        metadataDao.emptyTrash()
        if (mediaIds.isNotEmpty()) {
            albumDao.deleteCrossRefsForMediaBatch(mediaIds)
        }
    }

    override suspend fun duplicateMedia(mediaItem: MediaItem): String? {
        return fetcher.duplicateMedia(
            uriString = mediaItem.uri,
            displayName = mediaItem.displayName,
            mimeType = mediaItem.mimeType
        )
    }

    override fun getTagsForMedia(mediaId: Long): Flow<List<String>> {
        return tagDao.getTagsForMedia(mediaId)
    }

    override suspend fun addTagToMedia(mediaId: Long, tag: String) {
        val sanitized = tag.trim().lowercase()
        if (sanitized.isNotBlank()) {
            tagDao.insertTag(MediaTagEntity(mediaId = mediaId, tag = sanitized))
        }
    }

    override suspend fun removeTagFromMedia(mediaId: Long, tag: String) {
        tagDao.deleteTag(mediaId = mediaId, tag = tag.trim().lowercase())
    }

    override fun getAllUniqueTags(): Flow<List<String>> {
        return tagDao.getAllTags()
    }

    override fun getMediaByTag(tag: String): Flow<List<MediaItem>> = flow {
        val sanitized = tag.trim().lowercase()
        val mediaIds = tagDao.getMediaIdsListForTag(sanitized)
        if (mediaIds.isEmpty()) {
            emit(emptyList())
            return@flow
        }
        val rawList = fetcher.fetchMediaByIds(mediaIds)
        val mediaMap = rawList.associateBy { it.id }
        val items = mediaIds.mapNotNull { mediaMap[it] }.map { raw ->
            val metadata = metadataDao.getMetadata(raw.id)
            val tags = tagDao.getTagsListForMedia(raw.id)
            MediaItem(
                id = raw.id,
                uri = raw.uriString,
                displayName = raw.displayName,
                mimeType = raw.mimeType,
                dateTakenMillis = raw.dateTakenMillis,
                dateModifiedMillis = raw.dateModifiedMillis,
                sizeBytes = raw.sizeBytes,
                width = raw.width,
                height = raw.height,
                isFavorite = metadata?.isFavorite ?: false,
                isTrash = metadata?.isTrash ?: false,
                bucketName = raw.bucketName,
                relativePath = raw.relativePath,
                tags = tags,
                durationMillis = raw.durationMillis
            )
        }
        emit(items)
    }.flowOn(Dispatchers.IO)
}
