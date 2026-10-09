package com.ixeken.nierika.core.data.di

import android.content.Context
import androidx.room.Room
import com.ixeken.nierika.core.database.NierikaDatabase
import com.ixeken.nierika.core.database.dao.AlbumDao
import com.ixeken.nierika.core.database.dao.MediaMetadataDao
import com.ixeken.nierika.core.database.datastore.UserPreferencesDataStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Módulo de Hilt para la provisión de instancias de Room DB y DataStore.
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideNierikaDatabase(
        @ApplicationContext context: Context
    ): NierikaDatabase {
        return Room.databaseBuilder(
            context,
            NierikaDatabase::class.java,
            "nierika_database"
        ).addMigrations(NierikaDatabase.MIGRATION_5_6)
        .fallbackToDestructiveMigration(dropAllTables = true)
        .build()
    }

    @Provides
    fun provideAlbumDao(database: NierikaDatabase): AlbumDao {
        return database.albumDao()
    }

    @Provides
    fun provideMediaMetadataDao(database: NierikaDatabase): MediaMetadataDao {
        return database.mediaMetadataDao()
    }

    @Provides
    fun provideMediaTagDao(database: NierikaDatabase): com.ixeken.nierika.core.database.dao.MediaTagDao {
        return database.mediaTagDao()
    }

    @Provides
    fun provideVaultDao(database: NierikaDatabase): com.ixeken.nierika.core.database.dao.VaultDao {
        return database.vaultDao()
    }

    @Provides
    @Singleton
    fun provideUserPreferencesDataStore(
        @ApplicationContext context: Context
    ): UserPreferencesDataStore {
        return UserPreferencesDataStore(context)
    }
}
