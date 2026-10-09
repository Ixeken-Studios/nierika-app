package com.ixeken.nierika.core.domain.usecase

import com.ixeken.nierika.core.domain.model.MediaOperationResult
import com.ixeken.nierika.core.domain.repository.MediaRepository
import javax.inject.Inject

/**
 * Caso de uso atómico para la gestión del ciclo de vida de archivos en la papelera.
 * 
 * Centraliza las acciones de enviar a papelera, restaurar y borrado permanente,
 * aislando los contratos de Scoped Storage y persistencia local de la capa de interfaz.
 */
class TrashMediaUseCase @Inject constructor(
    private val mediaRepository: MediaRepository
) {
    /**
     * Mueve un archivo a la papelera lógica registrando su marca de tiempo.
     * 
     * @param mediaId Identificador único del archivo multimedia.
     * @param uri URI opcional del archivo para persistir en metadatos.
     * @return MediaOperationResult con el estado de la operación.
     */
    suspend fun moveToTrash(mediaId: Long, uri: String? = null): MediaOperationResult {
        return try {
            mediaRepository.moveToTrash(mediaId, uri)
            MediaOperationResult.Success
        } catch (e: Exception) {
            MediaOperationResult.Failure(e)
        }
    }

    /**
     * Restaura un archivo de la papelera devolviéndolo a la galería principal.
     * 
     * @param mediaId Identificador único del archivo a restaurar.
     * @return MediaOperationResult con el estado de la operación.
     */
    suspend fun restore(mediaId: Long): MediaOperationResult {
        return try {
            mediaRepository.restoreFromTrash(mediaId)
            MediaOperationResult.Success
        } catch (e: Exception) {
            MediaOperationResult.Failure(e)
        }
    }

    /**
     * Elimina definitivamente la entrada de metadatos del archivo.
     * 
     * @param mediaId Identificador único del archivo a purgar.
     * @return MediaOperationResult con el estado de la operación.
     */
    suspend fun deletePermanently(mediaId: Long): MediaOperationResult {
        return try {
            mediaRepository.deletePermanently(mediaId)
            MediaOperationResult.Success
        } catch (e: Exception) {
            MediaOperationResult.Failure(e)
        }
    }
}
