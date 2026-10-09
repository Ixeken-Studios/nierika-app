package com.ixeken.nierika.feature.collections

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Password
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.ixeken.nierika.core.designsystem.R
import com.ixeken.nierika.core.designsystem.component.FilledTonalButtonWithAnimatedShape
import com.ixeken.nierika.core.designsystem.component.IconButtonWithAnimatedShape
import com.ixeken.nierika.core.designsystem.component.NierikaPhotoGridItem
import com.ixeken.nierika.core.designsystem.component.NierikaSelectionTopBar
import com.ixeken.nierika.core.designsystem.theme.nierikaMotionScheme
import com.ixeken.nierika.core.designsystem.util.getGridItemShape
import com.ixeken.nierika.core.domain.model.MediaItem
import com.ixeken.nierika.core.domain.model.VaultMediaItem
import com.ixeken.nierika.feature.viewer.MediaDetailScreen
import kotlinx.coroutines.flow.Flow

/**
 * Pantalla de la Bóveda Oculta (HiddenVaultScreen) que reutiliza la estética de álbumes de Nierika,
 * con retícula de 3 columnas, selección múltiple, restauración segura y eliminación permanente.
 */
@Composable
fun HiddenVaultScreen(
    mediaPagingFlow: Flow<androidx.paging.PagingData<MediaItem>>,
    onBackClick: () -> Unit,
    onRefreshSilent: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: HiddenVaultViewModel = hiltViewModel()
) {
    val vaultItems by viewModel.vaultMedia.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val exportState by viewModel.vaultExportState.collectAsState()
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val motionScheme = MaterialTheme.nierikaMotionScheme

    val selectedVaultIds = remember { mutableStateListOf<Long>() }
    val isSelectionModeActive = selectedVaultIds.isNotEmpty()

    val deleteLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { _ ->
        // Android OS procesó la eliminación de los archivos originales de la galería pública
        onRefreshSilent()
    }

    val createZipLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/zip")
    ) { destinationUri ->
        if (destinationUri != null) {
            viewModel.exportVaultToZip(context, destinationUri)
        }
    }

    LaunchedEffect(exportState.isCompleted, exportState.error) {
        if (exportState.isCompleted) {
            Toast.makeText(
                context,
                context.getString(R.string.vault_export_success),
                Toast.LENGTH_SHORT
            ).show()
            viewModel.resetVaultExportState()
        } else if (exportState.error != null) {
            Toast.makeText(
                context,
                context.getString(R.string.vault_export_error, exportState.error),
                Toast.LENGTH_SHORT
            ).show()
            viewModel.resetVaultExportState()
        }
    }

    var isOptionsMenuVisible by remember { mutableStateOf(false) }
    var activeViewerMediaIndex by remember { mutableStateOf<Int?>(null) }
    var activeViewerMediaId by remember { mutableStateOf<Long?>(null) }
    var isViewerVisible by remember { mutableStateOf(false) }

    // Diálogo de cambio de PIN
    var newPinInput by remember { mutableStateOf("") }
    var confirmPinInput by remember { mutableStateOf("") }
    var pinErrorMessage by remember { mutableStateOf<String?>(null) }

    val handleBack: () -> Unit = {
        onRefreshSilent()
        onBackClick()
    }

    BackHandler(onBack = {
        when {
            isViewerVisible -> isViewerVisible = false
            uiState.isHidePhotosPickerVisible -> viewModel.showHidePhotosPicker(false)
            uiState.isExportConsentDialogVisible -> viewModel.showExportConsentDialog(false)
            isSelectionModeActive -> selectedVaultIds.clear()
            else -> handleBack()
        }
    })

    val topBarScrimBrush = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.background.copy(alpha = 0.95f),
            MaterialTheme.colorScheme.background.copy(alpha = 0.65f),
            Color.Transparent
        )
    )

    // Mapeo a MediaItem para el visor
    val mappedMediaItems = remember(vaultItems) {
        vaultItems.map { vaultItem ->
            MediaItem(
                id = vaultItem.id,
                uri = vaultItem.uri.toString(),
                displayName = vaultItem.fileName,
                mimeType = vaultItem.mimeType,
                sizeBytes = vaultItem.size,
                dateTakenMillis = vaultItem.dateAdded,
                dateModifiedMillis = vaultItem.dateAdded,
                width = vaultItem.width,
                height = vaultItem.height,
                durationMillis = vaultItem.durationMs
            )
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(topBarScrimBrush)
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        IconButtonWithAnimatedShape(
                            onClick = handleBack,
                            buttonSize = 44.dp
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.action_back),
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                        }

                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Lock,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = stringResource(R.string.vault_title),
                                    color = MaterialTheme.colorScheme.onBackground,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Text(
                                text = stringResource(R.string.vault_photos_hidden_count, vaultItems.size),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 13.sp
                            )
                        }
                    }

                    // Botón de opciones
                    Box {
                        IconButton(onClick = { isOptionsMenuVisible = true }) {
                            Icon(
                                imageVector = Icons.Filled.MoreVert,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                        }

                        DropdownMenu(
                            expanded = isOptionsMenuVisible,
                            onDismissRequest = { isOptionsMenuVisible = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(text = stringResource(R.string.vault_action_export_zip)) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Filled.FolderZip,
                                        contentDescription = null
                                    )
                                },
                                onClick = {
                                    isOptionsMenuVisible = false
                                    if (vaultItems.isEmpty()) {
                                        Toast.makeText(context, context.getString(R.string.vault_export_empty), Toast.LENGTH_SHORT).show()
                                    } else {
                                        viewModel.showExportConsentDialog(true)
                                    }
                                }
                            )

                            DropdownMenuItem(
                                text = { Text(text = stringResource(R.string.vault_setup_title)) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Filled.Password,
                                        contentDescription = null
                                    )
                                },
                                onClick = {
                                    isOptionsMenuVisible = false
                                    newPinInput = ""
                                    confirmPinInput = ""
                                    pinErrorMessage = null
                                    viewModel.showChangePinDialog(true)
                                }
                            )
                        }
                    }
                }
            }

            // Contenido de la Bóveda
            if (vaultItems.isEmpty()) {
                // Estado Vacío
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(96.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Lock,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(44.dp)
                            )
                        }

                        Text(
                            text = stringResource(R.string.vault_empty_title),
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )

                        Text(
                            text = stringResource(R.string.vault_empty_subtitle),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = { viewModel.showHidePhotosPicker(true) },
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Add,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = stringResource(R.string.vault_action_hide_photos))
                        }
                    }
                }
            } else {
                // Retícula de Fotos
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = 4.dp,
                        end = 4.dp,
                        top = 8.dp,
                        bottom = 120.dp
                    ),
                    horizontalArrangement = Arrangement.spacedBy(3.dp),
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    itemsIndexed(
                        items = vaultItems,
                        key = { _, item -> item.id }
                    ) { index, item ->
                        val isSelected = item.id in selectedVaultIds
                        val selectionIndex = if (isSelected) selectedVaultIds.indexOf(item.id) + 1 else 0
                        val cellShape = getGridItemShape(index, vaultItems.size, 3)

                        val mappedItem = MediaItem(
                            id = item.id,
                            uri = item.uri.toString(),
                            displayName = item.fileName,
                            mimeType = item.mimeType,
                            sizeBytes = item.size,
                            dateTakenMillis = item.dateAdded,
                            dateModifiedMillis = item.dateAdded,
                            width = item.width,
                            height = item.height,
                            durationMillis = item.durationMs
                        )

                        NierikaPhotoGridItem(
                            uri = item.uri.toString(),
                            displayName = item.fileName,
                            isVideo = item.isVideo,
                            isGif = item.isGif,
                            durationMillis = item.durationMs,
                            isSelected = isSelected,
                            selectionIndex = selectionIndex,
                            isSelectionModeActive = isSelectionModeActive,
                            cellShape = cellShape,
                            targetRatio = 1.0f,
                            onClick = {
                                if (isSelectionModeActive) {
                                    if (isSelected) selectedVaultIds.remove(item.id) else selectedVaultIds.add(item.id)
                                } else {
                                    activeViewerMediaIndex = index
                                    activeViewerMediaId = item.id
                                    isViewerVisible = true
                                }
                            },
                            onLongClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                if (isSelected) selectedVaultIds.remove(item.id) else selectedVaultIds.add(item.id)
                            }
                        )
                    }
                }
            }
        }

        // Floating Action Button para Ocultar Fotos
        AnimatedVisibility(
            visible = !isSelectionModeActive && !uiState.isHidePhotosPickerVisible,
            enter = scaleIn(animationSpec = motionScheme.fastSpatialSpec()) + fadeIn(),
            exit = scaleOut(animationSpec = motionScheme.fastSpatialSpec()) + fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .navigationBarsPadding()
                .padding(24.dp)
        ) {
            FloatingActionButton(
                onClick = { viewModel.showHidePhotosPicker(true) },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                shape = RoundedCornerShape(20.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = stringResource(R.string.vault_action_hide_photos),
                    modifier = Modifier.size(26.dp)
                )
            }
        }

        // Barra Superior de Selección Múltiple
        AnimatedVisibility(
            visible = isSelectionModeActive,
            enter = fadeIn() + slideInVertically(
                initialOffsetY = { -it },
                animationSpec = motionScheme.defaultSpatialSpec()
            ),
            exit = fadeOut() + slideOutVertically(
                targetOffsetY = { -it },
                animationSpec = motionScheme.defaultSpatialSpec()
            ),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .zIndex(30f)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(topBarScrimBrush)
                    .statusBarsPadding()
            ) {
                NierikaSelectionTopBar(
                    selectedCount = selectedVaultIds.size,
                    isAllSelected = selectedVaultIds.size == vaultItems.size && vaultItems.isNotEmpty(),
                    onClearSelection = { selectedVaultIds.clear() },
                    onToggleSelectAll = {
                        if (selectedVaultIds.size == vaultItems.size) {
                            selectedVaultIds.clear()
                        } else {
                            selectedVaultIds.clear()
                            selectedVaultIds.addAll(vaultItems.map { it.id })
                        }
                    }
                )
            }
        }

        // Barra Inferior de Acciones de Bóveda (Restaurar / Eliminar Definitivamente)
        AnimatedVisibility(
            visible = isSelectionModeActive,
            enter = slideInVertically(
                initialOffsetY = { it * 2 },
                animationSpec = motionScheme.defaultSpatialSpec()
            ) + fadeIn(),
            exit = slideOutVertically(
                targetOffsetY = { it * 2 },
                animationSpec = motionScheme.defaultSpatialSpec()
            ) + fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .zIndex(30f)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                shadowElevation = 6.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val selectedItems = vaultItems.filter { it.id in selectedVaultIds }

                    VaultSelectionActionItem(
                        icon = Icons.Filled.LockOpen,
                        label = stringResource(R.string.vault_action_restore),
                        onClick = {
                            viewModel.restoreItems(selectedItems) {
                                selectedVaultIds.clear()
                                onRefreshSilent()
                                Toast.makeText(context, context.getString(R.string.vault_restored_toast), Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )

                    VaultSelectionActionItem(
                        icon = Icons.Filled.DeleteForever,
                        label = stringResource(R.string.vault_action_delete_permanent),
                        tint = MaterialTheme.colorScheme.error,
                        onClick = {
                            viewModel.showDeleteConfirmation(true)
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Indicador de Progreso
        if (uiState.isOperationInProgress) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.45f))
                    .zIndex(50f),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh
                ) {
                    Row(
                        modifier = Modifier.padding(24.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(28.dp),
                            color = MaterialTheme.colorScheme.primary,
                            strokeWidth = 3.dp
                        )
                        Text(
                            text = stringResource(R.string.app_alias_nierika),
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        // Diálogo de Confirmación de Eliminación Permanente
        if (uiState.isDeleteConfirmationDialogVisible) {
            AlertDialog(
                onDismissRequest = { viewModel.showDeleteConfirmation(false) },
                title = { Text(text = stringResource(R.string.vault_delete_dialog_title)) },
                text = { Text(text = stringResource(R.string.vault_delete_dialog_desc)) },
                confirmButton = {
                    Button(
                        onClick = {
                            val selectedItems = vaultItems.filter { it.id in selectedVaultIds }
                            viewModel.deletePermanently(selectedItems) {
                                selectedVaultIds.clear()
                                Toast.makeText(context, context.getString(R.string.vault_permanent_deleted_toast), Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text(text = stringResource(R.string.vault_action_delete_permanent))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.showDeleteConfirmation(false) }) {
                        Text(text = stringResource(R.string.action_cancel))
                    }
                }
            )
        }

        // Diálogo de Cambio de PIN
        if (uiState.isChangePinDialogVisible) {
            AlertDialog(
                onDismissRequest = { viewModel.showChangePinDialog(false) },
                title = { Text(text = stringResource(R.string.vault_setup_title)) },
                text = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = stringResource(R.string.vault_setup_subtitle),
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        OutlinedTextField(
                            value = newPinInput,
                            onValueChange = { newPinInput = it },
                            label = { Text(text = stringResource(R.string.vault_pin_hint)) },
                            visualTransformation = PasswordVisualTransformation(),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = confirmPinInput,
                            onValueChange = { confirmPinInput = it },
                            label = { Text(text = stringResource(R.string.vault_pin_confirm_hint)) },
                            visualTransformation = PasswordVisualTransformation(),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        if (pinErrorMessage != null) {
                            Text(
                                text = pinErrorMessage ?: "",
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 12.sp
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            when {
                                newPinInput.trim().length < 4 -> {
                                    pinErrorMessage = context.getString(R.string.vault_pin_invalid)
                                }
                                newPinInput.trim() != confirmPinInput.trim() -> {
                                    pinErrorMessage = context.getString(R.string.vault_pin_mismatch)
                                }
                                else -> {
                                    viewModel.updateVaultPin(newPinInput.trim())
                                    Toast.makeText(context, context.getString(R.string.vault_restored_toast), Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    ) {
                        Text(text = stringResource(R.string.vault_action_set_pin))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.showChangePinDialog(false) }) {
                        Text(text = stringResource(R.string.action_cancel))
                    }
                }
            )
        }

        // Selector de Fotos para Ocultar
        AnimatedVisibility(
            visible = uiState.isHidePhotosPickerVisible,
            enter = slideInHorizontally(
                initialOffsetX = { it },
                animationSpec = motionScheme.defaultSpatialSpec()
            ),
            exit = slideOutHorizontally(
                targetOffsetX = { it },
                animationSpec = motionScheme.defaultSpatialSpec()
            ),
            modifier = Modifier.fillMaxSize().zIndex(40f)
        ) {
            AlbumPhotoPickerScreen(
                albumTitle = stringResource(R.string.vault_title),
                initialSelectedPhotos = emptyList(),
                mediaPagingFlow = mediaPagingFlow,
                onDismiss = { viewModel.showHidePhotosPicker(false) },
                onPhotosSelected = { pickedPhotos ->
                    viewModel.hideMediaItems(
                        context = context,
                        items = pickedPhotos,
                        onLaunchDeleteConsent = { intentSender ->
                            deleteLauncher.launch(IntentSenderRequest.Builder(intentSender).build())
                        },
                        onComplete = {
                            onRefreshSilent()
                            Toast.makeText(context, context.getString(R.string.vault_moved_to_vault_toast), Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            )
        }

        // Cerrar visor automáticamente si la lista de elementos del baúl queda vacía
        LaunchedEffect(mappedMediaItems.isEmpty()) {
            if (mappedMediaItems.isEmpty() && isViewerVisible) {
                isViewerVisible = false
                activeViewerMediaIndex = null
                activeViewerMediaId = null
            }
        }

        // Visor a Pantalla Completa
        AnimatedVisibility(
            visible = isViewerVisible && mappedMediaItems.isNotEmpty(),
            enter = slideInHorizontally(
                initialOffsetX = { it },
                animationSpec = motionScheme.defaultSpatialSpec()
            ),
            exit = slideOutHorizontally(
                targetOffsetX = { it },
                animationSpec = motionScheme.defaultSpatialSpec()
            ),
            modifier = Modifier.fillMaxSize().zIndex(100f)
        ) {
            val resolvedIndex = remember(mappedMediaItems, activeViewerMediaId, activeViewerMediaIndex) {
                if (activeViewerMediaId != null) {
                    val found = mappedMediaItems.indexOfFirst { it.id == activeViewerMediaId }
                    if (found != -1) found else (activeViewerMediaIndex ?: 0)
                } else {
                    (activeViewerMediaIndex ?: 0)
                }.coerceIn(0, mappedMediaItems.lastIndex.coerceAtLeast(0))
            }
            MediaDetailScreen(
                items = mappedMediaItems,
                initialIndex = resolvedIndex,
                isInVault = true,
                onRefreshSilent = onRefreshSilent,
                onPageChange = { page, item ->
                    if (isViewerVisible) {
                        activeViewerMediaId = item.id
                        val found = mappedMediaItems.indexOfFirst { it.id == item.id }
                        activeViewerMediaIndex = if (found != -1) found else page
                    }
                },
                onRestoreFromVaultClick = { item, onAccepted ->
                    val vaultItem = vaultItems.find { it.id == item.id }
                    if (vaultItem != null) {
                        viewModel.restoreItems(listOf(vaultItem)) {
                            onAccepted()
                            onRefreshSilent()
                            Toast.makeText(context, context.getString(R.string.vault_restored_toast), Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                onDeletePermanentlyConfirm = { item, onAccepted ->
                    val vaultItem = vaultItems.find { it.id == item.id }
                    if (vaultItem != null) {
                        viewModel.deletePermanently(listOf(vaultItem)) {
                            onAccepted()
                            onRefreshSilent()
                            Toast.makeText(context, context.getString(R.string.vault_permanent_deleted_toast), Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                onBackClick = {
                    isViewerVisible = false
                    activeViewerMediaIndex = null
                    activeViewerMediaId = null
                }
            )
        }

        // Diálogo de Consentimiento y Responsabilidad de Exportación a .ZIP
        if (uiState.isExportConsentDialogVisible) {
            AlertDialog(
                onDismissRequest = { viewModel.showExportConsentDialog(false) },
                icon = {
                    Icon(
                        imageVector = Icons.Filled.FolderZip,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                },
                title = {
                    Text(
                        text = stringResource(R.string.vault_export_consent_dialog_title),
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                text = {
                    Text(
                        text = stringResource(R.string.vault_export_consent_dialog_desc),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp,
                        lineHeight = 20.sp
                    )
                },
                confirmButton = {
                    FilledTonalButtonWithAnimatedShape(
                        onClick = {
                            viewModel.showExportConsentDialog(false)
                            createZipLauncher.launch("Nierika_Secret_Album_${System.currentTimeMillis()}.zip")
                        },
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Text(
                            text = stringResource(R.string.vault_export_consent_action_proceed),
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                dismissButton = {
                    FilledTonalButtonWithAnimatedShape(
                        onClick = { viewModel.showExportConsentDialog(false) },
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        )
                    ) {
                        Text(
                            text = stringResource(R.string.action_cancel),
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                shape = RoundedCornerShape(24.dp),
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
            )
        }

        // Diálogo de Progreso de Exportación a .ZIP
        ExportAlbumProgressDialog(
            exportState = exportState,
            onCancel = {
                viewModel.cancelVaultExport(context, android.net.Uri.EMPTY)
            }
        )
    }
}

@Composable
private fun VaultSelectionActionItem(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colorScheme.onSurface
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = tint,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            color = tint,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center
        )
    }
}
