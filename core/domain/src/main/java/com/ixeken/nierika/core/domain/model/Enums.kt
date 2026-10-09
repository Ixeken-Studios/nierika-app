package com.ixeken.nierika.core.domain.model

/**
 * Modos de layout disponibles en la pestaña de Fotos.
 */
enum class LayoutMode {
    DAY,
    MONTH,
    COMFORTABLE
}

/**
 * Orden cronológico de las fotos.
 */
enum class SortOrder {
    DESCENDING,
    ASCENDING
}


/**
 * Filtros rápidos de contenido.
 */
enum class QuickFilter {
    ALL,
    FAVORITES,
    VIDEOS,
    CAMERA,
    SCREENSHOTS,
    TRASH,
    NOT_IN_ALBUM
}

/**
 * Ubicación de los botones de álbumes (categorías) en la pantalla de colecciones.
 */
enum class AlbumButtonsLocation {
    TOP,
    BOTTOM
}



/**
 * Criterios de ordenamiento para la lista de álbumes en el selector y colecciones.
 */
enum class AlbumSortOrder {
    LAST_MODIFIED,
    DATE_CREATED,
    ALPHABETICALLY
}



