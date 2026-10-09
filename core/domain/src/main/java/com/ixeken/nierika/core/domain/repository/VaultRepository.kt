package com.ixeken.nierika.core.domain.repository

import com.ixeken.nierika.core.domain.model.MediaItem
import com.ixeken.nierika.core.domain.model.VaultMediaItem
import kotlinx.coroutines.flow.Flow

/**
 * Contrato de repositorio para la gestión de la Bóveda Oculta.
 */
interface VaultRepository {
    val vaultMedia: Flow<List<VaultMediaItem>>
    val vaultCount: Flow<Int>

    suspend fun moveToVault(items: List<MediaItem>): Result<Int>
    suspend fun restoreFromVault(items: List<VaultMediaItem>): Result<Int>
    suspend fun deletePermanently(items: List<VaultMediaItem>): Result<Int>

    val isVaultConfigured: Flow<Boolean>
    suspend fun setVaultPin(pin: String)
    suspend fun verifyVaultPin(pin: String): Boolean
}
