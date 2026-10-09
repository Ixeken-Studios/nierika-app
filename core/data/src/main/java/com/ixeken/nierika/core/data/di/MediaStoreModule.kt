package com.ixeken.nierika.core.data.di

import android.content.Context
import com.ixeken.nierika.core.mediastore.MediaStoreFetcher
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Módulo de Hilt para la provisión del lector de MediaStore.
 */
@Module
@InstallIn(SingletonComponent::class)
object MediaStoreModule {

    @Provides
    @Singleton
    fun provideMediaStoreFetcher(
        @ApplicationContext context: Context
    ): MediaStoreFetcher {
        return MediaStoreFetcher(context)
    }
}
