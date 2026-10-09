package com.ixeken.nierika.feature.collections

import android.app.RecoverableSecurityException
import android.content.Context
import android.content.IntentSender
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ixeken.nierika.core.domain.model.MediaItem
import com.ixeken.nierika.core.domain.model.VaultMediaItem
import com.ixeken.nierika.core.domain.repository.MediaRepository
import com.ixeken.nierika.core.domain.repository.UserPreferencesRepository
import com.ixeken.nierika.core.domain.repository.VaultRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

import androidx.compose.runtime.Immutable
import com.ixeken.nierika.core.designsystem.R
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.first
import java.io.BufferedOutputStream
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

@Immutable
data class HiddenVaultUiState(
    val isSelectionModeActive: Boolean = false,
    val isHidePhotosPickerVisible: Boolean = false,
    val isDeleteConfirmationDialogVisible: Boolean = false,
    val isChangePinDialogVisible: Boolean = false,
    val isExportConsentDialogVisible: Boolean = false,
    val isOperationInProgress: Boolean = false
)

@HiltViewModel
class HiddenVaultViewModel @Inject constructor(
    private val vaultRepository: VaultRepository,
    private val mediaRepository: MediaRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HiddenVaultUiState())
    val uiState: StateFlow<HiddenVaultUiState> = _uiState.asStateFlow()

    private val _vaultExportState = MutableStateFlow(AlbumExportState())
    val vaultExportState: StateFlow<AlbumExportState> = _vaultExportState.asStateFlow()

    private var exportJob: Job? = null

    val vaultMedia: StateFlow<List<VaultMediaItem>> = vaultRepository.vaultMedia.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = emptyList()
    )

    val allGalleryMedia: StateFlow<List<MediaItem>> = mediaRepository.getAllMedia().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun showHidePhotosPicker(show: Boolean) {
        _uiState.update { it.copy(isHidePhotosPickerVisible = show) }
    }

    fun showDeleteConfirmation(show: Boolean) {
        _uiState.update { it.copy(isDeleteConfirmationDialogVisible = show) }
    }

    fun showChangePinDialog(show: Boolean) {
        _uiState.update { it.copy(isChangePinDialogVisible = show) }
    }

    fun showExportConsentDialog(show: Boolean) {
        _uiState.update { it.copy(isExportConsentDialogVisible = show) }
    }

    fun createDeleteIntentSender(context: Context, items: List<MediaItem>): IntentSender? {
        val contentUris = items.mapNotNull {
            try {
                val parsed = Uri.parse(it.uri)
                if (parsed.scheme == "content") parsed else null
            } catch (_: Exception) {
                null
            }
        }
        if (contentUris.isEmpty()) return null

        return try {
            when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> {
                    val pendingIntent = MediaStore.createDeleteRequest(context.contentResolver, contentUris)
                    pendingIntent.intentSender
                }
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
                    try {
                        context.contentResolver.delete(contentUris.first(), null, null)
                        contentUris.drop(1).forEach { uri ->
                            try {
                                context.contentResolver.delete(uri, null, null)
                            } catch (_: Exception) {}
                        }
                        null
                    } catch (e: RecoverableSecurityException) {
                        e.userAction.actionIntent.intentSender
                    } catch (_: Exception) {
                        null
                    }
                }
                else -> {
                    contentUris.forEach { uri ->
                        try {
                            context.contentResolver.delete(uri, null, null)
                        } catch (_: Exception) {}
                    }
                    null
                }
            }
        } catch (_: Exception) {
            null
        }
    }

    fun hideMediaItems(
        context: Context,
        items: List<MediaItem>,
        onLaunchDeleteConsent: (IntentSender) -> Unit,
        onComplete: () -> Unit = {}
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isOperationInProgress = true) }
            // 1. Mover físicamente a almacenamiento privado y registrar en DB
            vaultRepository.moveToVault(items)

            // 2. Eliminar de metadatos locales para que desaparezcan de inmediato del timeline sin ensuciar la papelera
            items.forEach { item ->
                mediaRepository.deletePermanently(item.id, item.uri)
            }

            // 3. Crear solicitud de borrado del sistema para eliminar de MediaStore público
            val intentSender = createDeleteIntentSender(context, items)
            _uiState.update { it.copy(isOperationInProgress = false, isHidePhotosPickerVisible = false) }

            if (intentSender != null) {
                onLaunchDeleteConsent(intentSender)
            }
            onComplete()
        }
    }

    fun restoreItems(items: List<VaultMediaItem>, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            _uiState.update { it.copy(isOperationInProgress = true) }
            vaultRepository.restoreFromVault(items)
            _uiState.update { it.copy(isOperationInProgress = false) }
            onComplete()
        }
    }

    fun deletePermanently(items: List<VaultMediaItem>, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            _uiState.update { it.copy(isOperationInProgress = true) }
            vaultRepository.deletePermanently(items)
            _uiState.update { it.copy(isOperationInProgress = false, isDeleteConfirmationDialogVisible = false) }
            onComplete()
        }
    }

    fun updateVaultPin(newPin: String) {
        viewModelScope.launch {
            vaultRepository.setVaultPin(newPin)
            _uiState.update { it.copy(isChangePinDialogVisible = false) }
        }
    }

    fun exportVaultToZip(context: Context, destinationUri: Uri) {
        exportJob?.cancel()
        exportJob = viewModelScope.launch(Dispatchers.IO) {
            _vaultExportState.value = AlbumExportState(
                isExporting = true,
                albumName = context.getString(R.string.vault_title),
                currentItem = 0,
                totalItems = 0,
                progress = 0f,
                isCompleted = false,
                error = null
            )

            try {
                val items = vaultRepository.vaultMedia.first()
                if (items.isEmpty()) {
                    _vaultExportState.value = _vaultExportState.value.copy(
                        isExporting = false,
                        error = context.getString(R.string.vault_export_empty)
                    )
                    return@launch
                }

                val total = items.size
                _vaultExportState.value = _vaultExportState.value.copy(totalItems = total)

                val outputStream = context.contentResolver.openOutputStream(destinationUri)
                    ?: throw IllegalStateException("Cannot open destination output stream")

                val usedFileNames = mutableSetOf<String>()
                val folderName = "Secret_Album"

                ZipOutputStream(BufferedOutputStream(outputStream)).use { zipOut ->
                    val buffer = ByteArray(64 * 1024)

                    items.forEachIndexed { index, item ->
                        ensureActive()

                        var baseName = item.fileName.ifEmpty { "secret_media_${item.id}" }
                        if (!baseName.contains(".")) {
                            val ext = if (item.isVideo) ".mp4" else ".jpg"
                            baseName += ext
                        }

                        var uniqueName = baseName
                        var counter = 1
                        while (usedFileNames.contains(uniqueName)) {
                            val dotIndex = baseName.lastIndexOf('.')
                            uniqueName = if (dotIndex != -1) {
                                "${baseName.substring(0, dotIndex)} ($counter)${baseName.substring(dotIndex)}"
                            } else {
                                "$baseName ($counter)"
                            }
                            counter++
                        }
                        usedFileNames.add(uniqueName)

                        val entryPath = "$folderName/$uniqueName"
                        val zipEntry = ZipEntry(entryPath)
                        zipOut.putNextEntry(zipEntry)

                        try {
                            val sourceFile = File(item.internalPath)
                            if (sourceFile.exists()) {
                                sourceFile.inputStream().use { input ->
                                    var bytesRead: Int
                                    while (input.read(buffer).also { bytesRead = it } != -1) {
                                        ensureActive()
                                        zipOut.write(buffer, 0, bytesRead)
                                    }
                                }
                            }
                        } catch (_: Exception) {
                            // Continuar con el siguiente archivo si ocurre un error de lectura individual
                        } finally {
                            zipOut.closeEntry()
                        }

                        val current = index + 1
                        val prog = current.toFloat() / total.toFloat()
                        _vaultExportState.value = _vaultExportState.value.copy(
                            currentItem = current,
                            progress = prog
                        )
                    }
                    zipOut.flush()
                }

                _vaultExportState.value = _vaultExportState.value.copy(
                    isExporting = false,
                    isCompleted = true,
                    progress = 1.0f
                )
            } catch (e: CancellationException) {
                _vaultExportState.value = AlbumExportState()
                try {
                    context.contentResolver.delete(destinationUri, null, null)
                } catch (_: Exception) {}
            } catch (e: Exception) {
                _vaultExportState.value = _vaultExportState.value.copy(
                    isExporting = false,
                    error = e.localizedMessage ?: "Export error"
                )
            }
        }
    }

    fun cancelVaultExport(context: Context, destinationUri: Uri) {
        exportJob?.cancel()
        _vaultExportState.value = AlbumExportState()
        try {
            context.contentResolver.delete(destinationUri, null, null)
        } catch (_: Exception) {}
    }

    fun resetVaultExportState() {
        _vaultExportState.value = AlbumExportState()
    }
}
