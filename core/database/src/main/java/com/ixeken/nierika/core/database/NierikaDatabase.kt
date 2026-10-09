package com.ixeken.nierika.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.ixeken.nierika.core.database.dao.AlbumDao
import com.ixeken.nierika.core.database.dao.MediaMetadataDao
import com.ixeken.nierika.core.database.dao.MediaTagDao
import com.ixeken.nierika.core.database.dao.VaultDao
import com.ixeken.nierika.core.database.entity.AlbumEntity
import com.ixeken.nierika.core.database.entity.AlbumMediaCrossRef
import com.ixeken.nierika.core.database.entity.AlbumTagEntity
import com.ixeken.nierika.core.database.entity.MediaMetadataEntity
import com.ixeken.nierika.core.database.entity.MediaTagEntity
import com.ixeken.nierika.core.database.entity.VaultMediaEntity

/**
 * Base de datos principal de Room para Nierika.
 */
@Database(
    entities = [
        AlbumEntity::class,
        AlbumMediaCrossRef::class,
        AlbumTagEntity::class,
        MediaMetadataEntity::class,
        MediaTagEntity::class,
        VaultMediaEntity::class
    ],
    version = 6,
    exportSchema = false
)
abstract class NierikaDatabase : RoomDatabase() {
    abstract fun albumDao(): AlbumDao
    abstract fun mediaMetadataDao(): MediaMetadataDao
    abstract fun mediaTagDao(): MediaTagDao
    abstract fun vaultDao(): VaultDao

    companion object {
        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE album_media_cross_ref ADD COLUMN addedAtMillis INTEGER NOT NULL DEFAULT 0")
            }
        }
    }
}
