package com.ixeken.nierika.core.domain.model

import androidx.compose.runtime.Immutable

/**
 * Modelo de dominio inmutable que representa un álbum personalizado creado por el usuario.
 */
@Immutable
data class Album(
    val id: Long = 0,
    val name: String,
    val description: String = "",
    val coverUri: String? = null,
    val itemCount: Int = 0,
    val createdAtMillis: Long = System.currentTimeMillis(),
    val tags: List<String> = emptyList()
)
