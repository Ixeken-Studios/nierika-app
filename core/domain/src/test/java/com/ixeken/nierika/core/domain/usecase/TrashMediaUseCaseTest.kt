package com.ixeken.nierika.core.domain.usecase

import androidx.paging.PagingData
import com.ixeken.nierika.core.domain.model.MediaItem
import com.ixeken.nierika.core.domain.model.MediaOperationResult
import com.ixeken.nierika.core.domain.model.QuickFilter
import com.ixeken.nierika.core.domain.model.SortOrder
import com.ixeken.nierika.core.domain.repository.MediaRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Pruebas unitarias para validar las operaciones de TrashMediaUseCase.
 */
class TrashMediaUseCaseTest {

    private class FakeMediaRepository : MediaRepository {
        var trashedId: Long? = null
        var trashedUri: String? = null
        var restoredId: Long? = null
        var deletedId: Long? = null
        var shouldThrowError = false

        override fun getMediaPaged(
            quickFilter: QuickFilter,
            sortOrder: SortOrder
        ): Flow<PagingData<MediaItem>> = emptyFlow()

        override fun getMediaForCategory(quickFilter: QuickFilter): Flow<List<MediaItem>> = emptyFlow()

        override fun getAllMedia(): Flow<List<MediaItem>> = emptyFlow()

        override fun getTrashMedia(): Flow<List<MediaItem>> = emptyFlow()

        override suspend fun toggleFavorite(mediaId: Long, isFavorite: Boolean) {}

        override suspend fun moveToTrash(mediaId: Long, uri: String?) {
            if (shouldThrowError) throw IllegalStateException("Database error")
            trashedId = mediaId
            trashedUri = uri
        }

        override suspend fun moveToTrashBatch(mediaIds: List<Long>, uris: List<String>) {}

        override suspend fun restoreFromTrash(mediaId: Long) {
            if (shouldThrowError) throw IllegalStateException("Database error")
            restoredId = mediaId
        }

        override suspend fun deletePermanently(mediaId: Long, uriString: String?) {
            if (shouldThrowError) throw IllegalStateException("Database error")
            deletedId = mediaId
        }

        override suspend fun emptyTrash() {}

        override suspend fun duplicateMedia(mediaItem: MediaItem): String? = "content://media/1"

        override fun getTagsForMedia(mediaId: Long): Flow<List<String>> = emptyFlow()

        override suspend fun addTagToMedia(mediaId: Long, tag: String) {}

        override suspend fun removeTagFromMedia(mediaId: Long, tag: String) {}

        override fun getAllUniqueTags(): Flow<List<String>> = emptyFlow()

        override fun getMediaByTag(tag: String): Flow<List<MediaItem>> = emptyFlow()
    }

    @Test
    fun moveToTrash_whenSuccessful_returnsSuccess() = runBlocking {
        val repo = FakeMediaRepository()
        val useCase = TrashMediaUseCase(repo)

        val result = useCase.moveToTrash(42L, "content://media/42")

        assertEquals(MediaOperationResult.Success, result)
        assertEquals(42L, repo.trashedId)
        assertEquals("content://media/42", repo.trashedUri)
    }

    @Test
    fun restore_whenSuccessful_returnsSuccess() = runBlocking {
        val repo = FakeMediaRepository()
        val useCase = TrashMediaUseCase(repo)

        val result = useCase.restore(42L)

        assertEquals(MediaOperationResult.Success, result)
        assertEquals(42L, repo.restoredId)
    }

    @Test
    fun deletePermanently_whenSuccessful_returnsSuccess() = runBlocking {
        val repo = FakeMediaRepository()
        val useCase = TrashMediaUseCase(repo)

        val result = useCase.deletePermanently(42L)

        assertEquals(MediaOperationResult.Success, result)
        assertEquals(42L, repo.deletedId)
    }

    @Test
    fun moveToTrash_whenRepositoryThrows_returnsFailure() = runBlocking {
        val repo = FakeMediaRepository().apply { shouldThrowError = true }
        val useCase = TrashMediaUseCase(repo)

        val result = useCase.moveToTrash(42L)

        assertTrue(result is MediaOperationResult.Failure)
    }
}
