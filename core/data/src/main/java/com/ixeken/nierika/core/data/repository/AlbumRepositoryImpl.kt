package com.ixeken.nierika.core.data.repository

import com.ixeken.nierika.core.database.dao.AlbumDao
import com.ixeken.nierika.core.database.dao.MediaMetadataDao
import com.ixeken.nierika.core.database.entity.AlbumEntity
import com.ixeken.nierika.core.database.entity.AlbumMediaCrossRef
import com.ixeken.nierika.core.database.entity.AlbumTagEntity
import com.ixeken.nierika.core.domain.model.AddToAlbumResult
import com.ixeken.nierika.core.domain.model.Album
import com.ixeken.nierika.core.domain.model.MediaItem
import com.ixeken.nierika.core.domain.repository.AlbumRepository
import com.ixeken.nierika.core.mediastore.MediaStoreFetcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Implementación del repositorio de álbumes respaldada por Room DB y MediaStore.
 */
class AlbumRepositoryImpl @Inject constructor(
    private val albumDao: AlbumDao,
    private val fetcher: MediaStoreFetcher,
    private val metadataDao: MediaMetadataDao
) : AlbumRepository {

    override fun getAllAlbums(): Flow<List<Album>> {
        return combine(
            albumDao.getAllAlbumsWithCount(),
            metadataDao.getTrashItems()
        ) { list, trashList ->
            val trashedIds = trashList.map { it.mediaId }.toSet()
            val allMediaIds = list.flatMap { albumDao.getMediaIdsListForAlbum(it.id) }.distinct()
            val rawMap = if (allMediaIds.isNotEmpty()) {
                fetcher.fetchMediaByIds(allMediaIds).associateBy { it.id }
            } else {
                emptyMap()
            }
            list.map { item ->
                val mediaIds = albumDao.getMediaIdsListForAlbum(item.id)
                val validItems = mediaIds.mapNotNull { id ->
                    if (id !in trashedIds) rawMap[id] else null
                }
                val resolvedCover = if (!item.coverUri.isNullOrEmpty()) {
                    item.coverUri
                } else if (validItems.isNotEmpty()) {
                    validItems.minByOrNull {
                        it.dateTakenMillis.takeIf { d -> d > 0 } ?: it.dateModifiedMillis
                    }?.uriString
                } else {
                    null
                }
                val tags = albumDao.getTagsListForAlbum(item.id)
                Album(
                    id = item.id,
                    name = item.name,
                    description = item.description,
                    coverUri = resolvedCover,
                    itemCount = validItems.size,
                    createdAtMillis = item.createdAtMillis,
                    tags = tags
                )
            }
        }.flowOn(Dispatchers.IO)
    }

    override fun getAlbumById(albumId: Long): Flow<Album?> {
        return combine(
            albumDao.getAlbumById(albumId),
            metadataDao.getTrashItems()
        ) { entity, trashList ->
            entity?.let {
                val mediaIds = albumDao.getMediaIdsListForAlbum(it.id)
                val trashedIds = trashList.map { t -> t.mediaId }.toSet()
                val validIds = mediaIds.filter { id -> id !in trashedIds }
                val rawItems = if (validIds.isNotEmpty()) fetcher.fetchMediaByIds(validIds) else emptyList()
                val resolvedCover = if (!it.coverUri.isNullOrEmpty()) {
                    it.coverUri
                } else if (rawItems.isNotEmpty()) {
                    rawItems.minByOrNull { m ->
                        m.dateTakenMillis.takeIf { d -> d > 0 } ?: m.dateModifiedMillis
                    }?.uriString
                } else {
                    null
                }
                val tags = albumDao.getTagsListForAlbum(it.id)
                Album(
                    id = it.id,
                    name = it.name,
                    description = it.description,
                    coverUri = resolvedCover,
                    itemCount = validIds.size,
                    createdAtMillis = it.createdAtMillis,
                    tags = tags
                )
            }
        }.flowOn(Dispatchers.IO)
    }

    override fun getMediaForAlbum(albumId: Long): Flow<List<MediaItem>> {
        return combine(
            albumDao.getAlbumMediaCrossRefs(albumId),
            metadataDao.getTrashItems(),
            metadataDao.getFavorites()
        ) { crossRefs, trashList, favList ->
            if (crossRefs.isEmpty()) return@combine emptyList()
            val mediaIds = crossRefs.map { it.mediaId }
            val rawItems = fetcher.fetchMediaByIds(mediaIds)
            val favs = favList.associateBy { it.mediaId }
            val trashedIds = trashList.map { it.mediaId }.toSet()
            val itemsMap = rawItems.filter { it.id !in trashedIds }.associateBy { it.id }
            val addedAtMap = crossRefs.associate { it.mediaId to it.addedAtMillis }
            mediaIds.mapNotNull { id ->
                itemsMap[id]?.let { raw ->
                    val meta = favs[raw.id]
                    val addedAt = addedAtMap[raw.id] ?: 0L
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
                        isFavorite = meta?.isFavorite ?: false,
                        isTrash = false,
                        bucketName = raw.bucketName,
                        relativePath = raw.relativePath,
                        addedToAlbumMillis = addedAt,
                        durationMillis = raw.durationMillis
                    )
                }
            }
        }.flowOn(Dispatchers.IO)
    }

    override suspend fun createAlbum(
        name: String,
        description: String,
        coverUri: String?,
        tags: List<String>
    ): Long {
        val entity = AlbumEntity(
            name = name,
            description = description,
            coverUri = coverUri,
            createdAtMillis = System.currentTimeMillis()
        )
        val albumId = albumDao.insertAlbum(entity)
        if (tags.isNotEmpty()) {
            val tagEntities = tags.map { it.trim().lowercase() }
                .filter { it.isNotBlank() }
                .distinct()
                .map { AlbumTagEntity(albumId, it) }
            albumDao.insertAlbumTags(tagEntities)
        }
        return albumId
    }

    override suspend fun updateAlbumCover(albumId: Long, coverUri: String) {
        albumDao.updateCoverUri(albumId, coverUri)
    }

    override suspend fun updateAlbum(
        albumId: Long,
        name: String,
        description: String,
        coverUri: String?,
        tags: List<String>
    ) {
        albumDao.updateAlbum(albumId, name, description, coverUri)
        albumDao.clearTagsForAlbum(albumId)
        if (tags.isNotEmpty()) {
            val tagEntities = tags.map { it.trim().lowercase() }
                .filter { it.isNotBlank() }
                .distinct()
                .map { AlbumTagEntity(albumId, it) }
            albumDao.insertAlbumTags(tagEntities)
        }
    }

    override suspend fun deleteAlbum(albumId: Long) {
        albumDao.deleteAlbum(albumId)
        albumDao.clearTagsForAlbum(albumId)
    }

    override suspend fun addMediaToAlbum(albumId: Long, mediaId: Long): Boolean {
        val existingIds = albumDao.getMediaIdsListForAlbum(albumId)
        return if (existingIds.contains(mediaId)) {
            false
        } else {
            albumDao.insertCrossRef(AlbumMediaCrossRef(albumId, mediaId, System.currentTimeMillis()))
            true
        }
    }

    override suspend fun addMediaBatchToAlbum(albumId: Long, mediaIds: List<Long>, firstCoverUri: String?): AddToAlbumResult {
        val existingIds = albumDao.getMediaIdsListForAlbum(albumId).toSet()
        val newIds = mediaIds.filter { !existingIds.contains(it) }
        if (newIds.isNotEmpty()) {
            val baseTime = System.currentTimeMillis()
            val refs = newIds.mapIndexed { index, id ->
                AlbumMediaCrossRef(albumId, id, baseTime + index)
            }
            albumDao.insertCrossRefs(refs)
            if (!firstCoverUri.isNullOrEmpty()) {
                albumDao.updateCoverUri(albumId, firstCoverUri)
            }
        }
        return AddToAlbumResult(
            addedCount = newIds.size,
            alreadyPresentCount = mediaIds.size - newIds.size,
            totalRequested = mediaIds.size
        )
    }

    override suspend fun syncAlbumMedia(albumId: Long, mediaIds: List<Long>, coverUri: String?) {
        val existingCrossRefs = albumDao.getAlbumMediaCrossRefsList(albumId).associate { it.mediaId to it.addedAtMillis }
        albumDao.clearAlbumCrossRefs(albumId)
        if (mediaIds.isNotEmpty()) {
            val baseTime = System.currentTimeMillis()
            val refs = mediaIds.mapIndexed { index, id ->
                val preservedTime = existingCrossRefs[id] ?: (baseTime + index)
                AlbumMediaCrossRef(albumId, id, preservedTime)
            }
            albumDao.insertCrossRefs(refs)
        }
        val resolvedCover = coverUri ?: ""
        albumDao.updateCoverUri(albumId, resolvedCover)
    }

    override suspend fun removeMediaFromAlbum(albumId: Long, mediaId: Long) {
        albumDao.deleteCrossRef(albumId, mediaId)
        val remainingIds = albumDao.getMediaIdsListForAlbum(albumId)
        if (remainingIds.isEmpty()) {
            albumDao.updateCoverUri(albumId, "")
        }
    }

    override fun getTagsForAlbum(albumId: Long): Flow<List<String>> {
        return albumDao.getTagsForAlbum(albumId)
    }

    override fun getAllUniqueTags(): Flow<List<String>> {
        return albumDao.getAllUniqueAlbumTags()
    }

    override suspend fun addTagToAlbum(albumId: Long, tag: String) {
        val sanitized = tag.trim().lowercase()
        if (sanitized.isNotBlank()) {
            albumDao.insertAlbumTag(AlbumTagEntity(albumId, sanitized))
        }
    }

    override suspend fun removeTagFromAlbum(albumId: Long, tag: String) {
        albumDao.deleteAlbumTag(albumId, tag.trim().lowercase())
    }
}
