package com.ixeken.nierika.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidad de Room para representar un álbum creado por el usuario.
 */
@Entity(tableName = "albums")
data class AlbumEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val description: String,
    val coverUri: String?,
    val createdAtMillis: Long
)

/**
 * Tabla relacional muchos a muchos entre álbumes y archivos multimedia.
 */
@Entity(
    tableName = "album_media_cross_ref",
    primaryKeys = ["albumId", "mediaId"]
)
data class AlbumMediaCrossRef(
    val albumId: Long,
    val mediaId: Long,
    val addedAtMillis: Long = System.currentTimeMillis()
)

/**
 * Entidad para metadatos locales (favoritos y estado en papelera).
 */
@Entity(tableName = "media_metadata")
data class MediaMetadataEntity(
    @PrimaryKey
    val mediaId: Long,
    val isFavorite: Boolean = false,
    val isTrash: Boolean = false,
    val trashedAtMillis: Long? = null,
    val uriString: String? = null
)

/**
 * Entidad para etiquetas (#tags) asociadas a elementos multimedia.
 */
@Entity(
    tableName = "media_tags",
    primaryKeys = ["mediaId", "tag"]
)
data class MediaTagEntity(
    val mediaId: Long,
    val tag: String
)

/**
 * Entidad para etiquetas (#tags) asociadas a álbumes personalizados.
 */
@Entity(
    tableName = "album_tags",
    primaryKeys = ["albumId", "tag"]
)
data class AlbumTagEntity(
    val albumId: Long,
    val tag: String
)
