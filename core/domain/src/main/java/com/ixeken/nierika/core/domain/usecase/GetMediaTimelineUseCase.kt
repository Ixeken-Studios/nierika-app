package com.ixeken.nierika.core.domain.usecase

import androidx.paging.PagingData
import com.ixeken.nierika.core.domain.model.MediaItem
import com.ixeken.nierika.core.domain.model.QuickFilter
import com.ixeken.nierika.core.domain.model.SortOrder
import com.ixeken.nierika.core.domain.repository.MediaRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Caso de uso para obtener el flujo paginado de elementos multimedia según el filtro y orden seleccionado.
 */
class GetMediaTimelineUseCase @Inject constructor(
    private val mediaRepository: MediaRepository
) {
    operator fun invoke(
        quickFilter: QuickFilter = QuickFilter.ALL,
        sortOrder: SortOrder = SortOrder.DESCENDING
    ): Flow<PagingData<MediaItem>> {
        return mediaRepository.getMediaPaged(quickFilter, sortOrder)
    }
}
