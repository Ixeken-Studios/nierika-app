package com.ixeken.nierika.core.domain.model

/**
 * Resultado detallado de la operación de agregar fotos a un álbum.
 * Permite identificar cuántas fotos fueron agregadas exitosamente y cuántas ya existían previamente en el álbum.
 */
data class AddToAlbumResult(
    val addedCount: Int,
    val alreadyPresentCount: Int,
    val totalRequested: Int
)
