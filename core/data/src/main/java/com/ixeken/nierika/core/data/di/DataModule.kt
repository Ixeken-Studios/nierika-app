package com.ixeken.nierika.core.data.di

import com.ixeken.nierika.core.data.repository.AlbumRepositoryImpl
import com.ixeken.nierika.core.data.repository.MediaRepositoryImpl
import com.ixeken.nierika.core.data.repository.UserPreferencesRepositoryImpl
import com.ixeken.nierika.core.data.repository.VaultRepositoryImpl
import com.ixeken.nierika.core.domain.repository.AlbumRepository
import com.ixeken.nierika.core.domain.repository.MediaRepository
import com.ixeken.nierika.core.domain.repository.UserPreferencesRepository
import com.ixeken.nierika.core.domain.repository.VaultRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Módulo de Hilt para la vinculación de contratos de repositorios.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {

    @Binds
    @Singleton
    abstract fun bindMediaRepository(
        impl: MediaRepositoryImpl
    ): MediaRepository

    @Binds
    @Singleton
    abstract fun bindAlbumRepository(
        impl: AlbumRepositoryImpl
    ): AlbumRepository

    @Binds
    @Singleton
    abstract fun bindUserPreferencesRepository(
        impl: UserPreferencesRepositoryImpl
    ): UserPreferencesRepository

    @Binds
    @Singleton
    abstract fun bindVaultRepository(
        impl: VaultRepositoryImpl
    ): VaultRepository
}
