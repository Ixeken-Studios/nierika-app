package com.ixeken.nierika.core.domain.usecase

import androidx.paging.PagingData
import com.ixeken.nierika.core.domain.model.MediaItem
import com.ixeken.nierika.core.domain.model.QuickFilter
import com.ixeken.nierika.core.domain.model.SortOrder
import com.ixeken.nierika.core.domain.repository.MediaRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.runBlocking
import org.junit.Test

/**
 * Pruebas unitarias para validar el comportamiento aislado de DuplicateMediaUseCase.
 */
class DuplicateMediaUseCaseTest {

    private class FakeMediaRepository(
        private val shouldSucceed: Boolean
    ) : MediaRepository {
        var duplicateCalledWith: MediaItem? = null

        override fun getMediaPaged(
            quickFilter: QuickFilter,
            sortOrder: SortOrder
        ): Flow<PagingData<MediaItem>> = emptyFlow()

        override fun getMediaForCategory(quickFilter: QuickFilter): Flow<List<MediaItem>> = emptyFlow()

        override fun getAllMedia(): Flow<List<MediaItem>> = emptyFlow()

        override fun getTrashMedia(): Flow<List<MediaItem>> = emptyFlow()

        override suspend fun toggleFavorite(mediaId: Long, isFavorite: Boolean) {}

        override suspend fun moveToTrash(mediaId: Long, uri: String?) {}

        override suspend fun moveToTrashBatch(mediaIds: List<Long>, uris: List<String>) {}

        override suspend fun restoreFromTrash(mediaId: Long) {}

        override suspend fun deletePermanently(mediaId: Long, uriString: String?) {}

        override suspend fun emptyTrash() {}

        override suspend fun duplicateMedia(mediaItem: MediaItem): String? {
            duplicateCalledWith = mediaItem
            return if (shouldSucceed) "content://media/external/images/media/101" else null
        }

        override fun getTagsForMedia(mediaId: Long): Flow<List<String>> = emptyFlow()

        override suspend fun addTagToMedia(mediaId: Long, tag: String) {}

        override suspend fun removeTagFromMedia(mediaId: Long, tag: String) {}

        override fun getAllUniqueTags(): Flow<List<String>> = emptyFlow()

        override fun getMediaByTag(tag: String): Flow<List<MediaItem>> = emptyFlow()
    }

    private val sampleItem = MediaItem(
        id = 100L,
        uri = "content://media/external/images/media/100",
        displayName = "sample.jpg",
        mimeType = "image/jpeg",
        dateTakenMillis = 1000L,
        dateModifiedMillis = 1000L,
        sizeBytes = 2048L,
        width = 1920,
        height = 1080
    )

    @Test
    fun invoke_whenRepositorySucceeds_returnsUri() = runBlocking {
        val fakeRepo = FakeMediaRepository(shouldSucceed = true)
        val useCase = DuplicateMediaUseCase(fakeRepo)

        val result = useCase(sampleItem)

        org.junit.Assert.assertNotNull(result)
        org.junit.Assert.assertEquals("content://media/external/images/media/101", result)
        org.junit.Assert.assertEquals(sampleItem, fakeRepo.duplicateCalledWith)
    }

    @Test
    fun invoke_whenRepositoryFails_returnsNull() = runBlocking {
        val fakeRepo = FakeMediaRepository(shouldSucceed = false)
        val useCase = DuplicateMediaUseCase(fakeRepo)

        val result = useCase(sampleItem)

        org.junit.Assert.assertNull(result)
        org.junit.Assert.assertEquals(sampleItem, fakeRepo.duplicateCalledWith)
    }
}
