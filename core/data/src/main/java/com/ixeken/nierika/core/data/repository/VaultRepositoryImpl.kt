package com.ixeken.nierika.core.data.repository

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.ixeken.nierika.core.database.dao.VaultDao
import com.ixeken.nierika.core.database.datastore.UserPreferencesDataStore
import com.ixeken.nierika.core.database.entity.VaultMediaEntity
import com.ixeken.nierika.core.domain.model.MediaItem
import com.ixeken.nierika.core.domain.model.VaultMediaItem
import com.ixeken.nierika.core.domain.repository.VaultRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.File
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VaultRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val vaultDao: VaultDao,
    private val userPreferencesDataStore: UserPreferencesDataStore
) : VaultRepository {

    private val vaultDir: File
        get() = File(context.filesDir, "nierika_vault").apply {
            if (!exists()) {
                mkdirs()
            }
            val noMedia = File(this, ".nomedia")
            if (!noMedia.exists()) {
                noMedia.createNewFile()
            }
        }

    override val vaultMedia: Flow<List<VaultMediaItem>> = vaultDao.getAllVaultMedia().map { list ->
        list.map { entity ->
            VaultMediaItem(
                id = entity.id,
                fileName = entity.fileName,
                internalPath = entity.internalPath,
                mimeType = entity.mimeType,
                dateAdded = entity.dateAdded,
                size = entity.size,
                width = entity.width,
                height = entity.height,
                durationMs = entity.durationMs,
                aspectRatio = entity.aspectRatio
            )
        }
    }

    override val vaultCount: Flow<Int> = vaultDao.getVaultMediaCount()

    override val isVaultConfigured: Flow<Boolean> = userPreferencesDataStore.isVaultConfigured

    override suspend fun moveToVault(items: List<MediaItem>): Result<Int> = withContext(Dispatchers.IO) {
        try {
            var movedCount = 0
            val targetDir = vaultDir

            items.forEach { item ->
                val safeName = "${System.currentTimeMillis()}_${item.displayName.replace("[^a-zA-Z0-9._-]".toRegex(), "_")}"
                val destFile = File(targetDir, safeName)
                val parsedUri = android.net.Uri.parse(item.uri)

                val inputStream = context.contentResolver.openInputStream(parsedUri)
                if (inputStream != null) {
                    inputStream.use { input ->
                        destFile.outputStream().use { output ->
                            input.copyTo(output)
                        }
                    }

                    val entity = VaultMediaEntity(
                        fileName = item.displayName,
                        internalPath = destFile.absolutePath,
                        mimeType = item.mimeType,
                        dateAdded = if (item.dateTakenMillis > 0) item.dateTakenMillis else item.dateModifiedMillis,
                        size = item.sizeBytes,
                        width = item.width,
                        height = item.height,
                        durationMs = 0L,
                        aspectRatio = item.aspectRatio
                    )

                    vaultDao.insert(entity)

                    try {
                        context.contentResolver.delete(parsedUri, null, null)
                    } catch (ignored: Exception) {
                        // MediaStore delete might require batch SAF in some OEMs
                    }

                    movedCount++
                }
            }

            Result.success(movedCount)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun restoreFromVault(items: List<VaultMediaItem>): Result<Int> = withContext(Dispatchers.IO) {
        try {
            var restoredCount = 0

            items.forEach { item ->
                val sourceFile = File(item.internalPath)
                if (sourceFile.exists()) {
                    val contentValues = ContentValues().apply {
                        put(MediaStore.MediaColumns.DISPLAY_NAME, item.fileName)
                        put(MediaStore.MediaColumns.MIME_TYPE, item.mimeType)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/Nierika")
                            put(MediaStore.MediaColumns.IS_PENDING, 1)
                        }
                    }

                    val collectionUri = if (item.isVideo) {
                        MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                    } else {
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                    }

                    val targetUri = context.contentResolver.insert(collectionUri, contentValues)
                    if (targetUri != null) {
                        context.contentResolver.openOutputStream(targetUri)?.use { out ->
                            sourceFile.inputStream().use { input ->
                                input.copyTo(out)
                            }
                        }

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            contentValues.clear()
                            contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                            context.contentResolver.update(targetUri, contentValues, null, null)
                        }

                        sourceFile.delete()
                        vaultDao.deleteById(item.id)
                        restoredCount++
                    }
                } else {
                    vaultDao.deleteById(item.id)
                }
            }

            Result.success(restoredCount)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deletePermanently(items: List<VaultMediaItem>): Result<Int> = withContext(Dispatchers.IO) {
        try {
            var deletedCount = 0
            items.forEach { item ->
                val sourceFile = File(item.internalPath)
                if (sourceFile.exists()) {
                    sourceFile.delete()
                }
                vaultDao.deleteById(item.id)
                deletedCount++
            }
            Result.success(deletedCount)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun setVaultPin(pin: String) {
        val hash = hashPin(pin)
        userPreferencesDataStore.updateVaultPinHash(hash)
    }

    override suspend fun verifyVaultPin(pin: String): Boolean {
        val currentHash = userPreferencesDataStore.vaultPinHash.first()
        if (currentHash.isBlank()) return false
        val candidateHash = hashPin(pin)
        return candidateHash == currentHash
    }

    private fun hashPin(pin: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(pin.trim().toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
