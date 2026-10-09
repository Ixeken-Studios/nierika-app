package com.ixeken.nierika.core.domain.usecase

import com.ixeken.nierika.core.domain.model.MediaItem
import com.ixeken.nierika.core.domain.repository.MediaRepository
import javax.inject.Inject

/**
 * Caso de uso atómico para clonar un elemento multimedia físico en el almacenamiento local.
 * 
 * Se desacopla de la capa de presentación para asegurar que la generación de nuevos nombres
 * y la persistencia de metadatos sigan una regla única e independiente de la vista.
 */
class DuplicateMediaUseCase @Inject constructor(
    private val mediaRepository: MediaRepository
) {
    /**
     * Ejecuta la duplicación del elemento multimedia delegando al repositorio de datos.
     * 
     * @param mediaItem Elemento origen a duplicar.
     * @return El URI del nuevo elemento clonado si fue exitoso, null en caso contrario.
     */
    suspend operator fun invoke(mediaItem: MediaItem): String? {
        return mediaRepository.duplicateMedia(mediaItem)
    }
}
