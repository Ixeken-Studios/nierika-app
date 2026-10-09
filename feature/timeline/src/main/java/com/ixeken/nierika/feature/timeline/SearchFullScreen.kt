package com.ixeken.nierika.feature.timeline

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.ixeken.nierika.core.designsystem.R
import com.ixeken.nierika.core.designsystem.component.IconButtonWithAnimatedShape
import com.ixeken.nierika.core.designsystem.component.NierikaPhotoGridItem
import com.ixeken.nierika.core.designsystem.theme.nierikaMotionScheme
import com.ixeken.nierika.core.designsystem.util.BiometricHelper
import com.ixeken.nierika.core.domain.model.Album
import com.ixeken.nierika.core.domain.model.MediaItem
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Pantalla completa de Búsqueda de Material 3 Expressive.
 * Despliega barra superior acoplada, carrusel de Filter Chips, sugerencias rápidas de tags,
 * álbumes coincidentes y retícula unificada de fotos y vídeos.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchFullScreen(
    onCloseClick: () -> Unit,
    onMediaClick: (MediaItem, List<MediaItem>) -> Unit,
    onAlbumClick: (Album) -> Unit,
    onNavigateToVault: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: SearchViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current

    var isVaultSetupDialogVisible by remember { mutableStateOf(false) }
    var isVaultUnlockDialogVisible by remember { mutableStateOf(false) }
    var vaultPinInput by remember { mutableStateOf("") }
    var vaultConfirmPinInput by remember { mutableStateOf("") }
    var vaultPinError by remember { mutableStateOf<String?>(null) }

    var searchQuery by remember {
        mutableStateOf(TextFieldValue(uiState.query, TextRange(uiState.query.length)))
    }

    LaunchedEffect(uiState.query) {
        if (searchQuery.text != uiState.query) {
            searchQuery = TextFieldValue(
                text = uiState.query,
                selection = TextRange(uiState.query.length)
            )
        }
    }

    val handleVaultClick: () -> Unit = {
        keyboardController?.hide()
        if (!uiState.isVaultConfigured) {
            vaultPinInput = ""
            vaultConfirmPinInput = ""
            vaultPinError = null
            isVaultSetupDialogVisible = true
        } else {
            val activity = context as? androidx.fragment.app.FragmentActivity
            if (activity != null && BiometricHelper.canAuthenticate(context)) {
                BiometricHelper.showBiometricPrompt(
                    activity = activity,
                    title = context.getString(R.string.vault_title),
                    subtitle = context.getString(R.string.vault_enter_pin_subtitle),
                    onSuccess = {
                        onNavigateToVault()
                    },
                    onError = {
                        vaultPinInput = ""
                        vaultPinError = null
                        isVaultUnlockDialogVisible = true
                    }
                )
            } else {
                vaultPinInput = ""
                vaultPinError = null
                isVaultUnlockDialogVisible = true
            }
        }
    }

    BackHandler(onBack = {
        keyboardController?.hide()
        onCloseClick()
    })

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    // Modal DatePickerDialog de Material 3 Expressive
    if (uiState.isDatePickerVisible) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = uiState.selectedDateMillis ?: System.currentTimeMillis()
        )

        DatePickerDialog(
            onDismissRequest = { viewModel.onDismissDatePicker() },
            confirmButton = {
                TextButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.SegmentTick)
                        viewModel.onDateSelected(datePickerState.selectedDateMillis)
                    }
                ) {
                    Text(
                        text = stringResource(R.string.action_apply),
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.SegmentTick)
                        viewModel.onDismissDatePicker()
                    }
                ) {
                    Text(text = stringResource(R.string.action_cancel))
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // 1. Barra de Búsqueda Superior Acoplada (M3E Full-Screen Search Bar)
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                tonalElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Botón Atrás
                    IconButtonWithAnimatedShape(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.SegmentTick)
                            keyboardController?.hide()
                            onCloseClick()
                        },
                        buttonSize = 40.dp
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.action_back),
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    // Campo de entrada de texto
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 8.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (uiState.query.isEmpty()) {
                            Text(
                                text = stringResource(R.string.search_hint),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        BasicTextField(
                            value = searchQuery,
                            onValueChange = { newValue ->
                                searchQuery = newValue
                                viewModel.onQueryChange(newValue.text)
                            },
                            singleLine = true,
                            textStyle = MaterialTheme.typography.bodyLarge.copy(
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Normal
                            ),
                            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                            keyboardOptions = KeyboardOptions(
                                imeAction = ImeAction.Search
                            ),
                            keyboardActions = KeyboardActions(
                                onSearch = {
                                    keyboardController?.hide()
                                }
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(focusRequester)
                        )
                    }

                    // Botón Limpiar
                    if (uiState.query.isNotEmpty()) {
                        IconButtonWithAnimatedShape(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.SegmentTick)
                                searchQuery = TextFieldValue("")
                                viewModel.onClearQuery()
                            },
                            buttonSize = 36.dp
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = stringResource(R.string.action_clear),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            // 2. Carrusel Horizontal de Filter Chips M3 Expressive
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SearchFilterChipItem(
                    label = stringResource(R.string.search_filter_all),
                    isSelected = uiState.selectedFilter == SearchFilter.ALL && uiState.selectedDateMillis == null,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.SegmentTick)
                        viewModel.onFilterSelect(SearchFilter.ALL)
                        viewModel.onClearDateFilter()
                    }
                )

                SearchFilterChipItem(
                    label = stringResource(R.string.search_filter_tags),
                    icon = Icons.Filled.Tag,
                    isSelected = uiState.selectedFilter == SearchFilter.TAGS,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.SegmentTick)
                        viewModel.onFilterSelect(SearchFilter.TAGS)
                    }
                )

                SearchFilterChipItem(
                    label = stringResource(R.string.search_filter_albums),
                    icon = Icons.Filled.Folder,
                    isSelected = uiState.selectedFilter == SearchFilter.ALBUMS,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.SegmentTick)
                        viewModel.onFilterSelect(SearchFilter.ALBUMS)
                    }
                )

                SearchFilterChipItem(
                    label = stringResource(R.string.search_filter_photos),
                    icon = Icons.Filled.Image,
                    isSelected = uiState.selectedFilter == SearchFilter.PHOTOS,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.SegmentTick)
                        viewModel.onFilterSelect(SearchFilter.PHOTOS)
                    }
                )

                SearchFilterChipItem(
                    label = stringResource(R.string.search_filter_videos),
                    icon = Icons.Filled.Videocam,
                    isSelected = uiState.selectedFilter == SearchFilter.VIDEOS,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.SegmentTick)
                        viewModel.onFilterSelect(SearchFilter.VIDEOS)
                    }
                )

                // Chip de Fecha con indicador de fecha activa
                val dateLabel = if (uiState.selectedDateMillis != null) {
                    val date = Date(uiState.selectedDateMillis!!)
                    SimpleDateFormat("d MMM yyyy", Locale.getDefault()).format(date)
                } else {
                    stringResource(R.string.search_filter_date)
                }

                FilterChip(
                    selected = uiState.selectedDateMillis != null,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.SegmentTick)
                        viewModel.onFilterSelect(SearchFilter.DATE)
                    },
                    label = { Text(text = dateLabel) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.DateRange,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    trailingIcon = if (uiState.selectedDateMillis != null) {
                        {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = stringResource(R.string.search_clear_date_filter),
                                modifier = Modifier
                                    .size(16.dp)
                                    .clickable {
                                        haptic.performHapticFeedback(HapticFeedbackType.SegmentTick)
                                        viewModel.onClearDateFilter()
                                    }
                            )
                        }
                    } else null,
                    shape = RoundedCornerShape(12.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }

            // 3. Contenido de Resultados / Bóveda / Sugerencias
            if (uiState.isVaultTriggerMatch) {
                // Tarjeta de Disparador Discreto de la Bóveda Oculta
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    VaultSearchTriggerCard(
                        isConfigured = uiState.isVaultConfigured,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.SegmentTick)
                            handleVaultClick()
                        }
                    )
                }
            } else if (!uiState.isSearching && uiState.availableTags.isNotEmpty()) {
                // Estado inicial: Sugerencias de Tags con 1 solo toque
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Tag,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = stringResource(R.string.search_recent_suggestions),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }

                    androidx.compose.foundation.layout.FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        uiState.availableTags.forEach { tag ->
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .border(
                                        width = 1.dp,
                                        color = MaterialTheme.colorScheme.outlineVariant,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                                    .clickable {
                                        haptic.performHapticFeedback(HapticFeedbackType.SegmentTick)
                                        viewModel.onTagSelect(tag)
                                    }
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "#$tag",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            } else if (uiState.isSearching && uiState.matchingAlbums.isEmpty() && uiState.matchingMedia.isEmpty()) {
                // Estado sin resultados
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.SearchOff,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(36.dp)
                            )
                        }

                        Text(
                            text = stringResource(R.string.search_no_results_title),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Text(
                            text = stringResource(R.string.search_no_results_subtitle),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                // Retícula Unificada de Resultados
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        top = 8.dp,
                        bottom = 80.dp
                    ),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Sección de Álbumes coincidentes
                    if (uiState.matchingAlbums.isNotEmpty()) {
                        item(span = { GridItemSpan(maxLineSpan) }, contentType = "albums_section") {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = stringResource(R.string.search_section_albums) + " (${uiState.matchingAlbums.size})",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )

                                LazyRow(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    items(
                                        items = uiState.matchingAlbums,
                                        key = { it.id },
                                        contentType = { "album_card" }
                                    ) { album ->
                                        AlbumSearchCard(
                                            album = album,
                                            onClick = {
                                                haptic.performHapticFeedback(HapticFeedbackType.SegmentTick)
                                                onAlbumClick(album)
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Encabezado de Fotos y Vídeos
                    if (uiState.matchingMedia.isNotEmpty()) {
                        item(span = { GridItemSpan(maxLineSpan) }, contentType = "media_header") {
                            Text(
                                text = stringResource(R.string.search_section_media) + " (${uiState.matchingMedia.size})",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
                            )
                        }

                        // Celdas unificadas TimelinePhotoGridItem
                        items(
                            items = uiState.matchingMedia,
                            key = { it.id },
                            contentType = { "photo_item" }
                        ) { mediaItem ->
                            NierikaPhotoGridItem(
                                uri = mediaItem.uri,
                                displayName = mediaItem.displayName,
                                isVideo = mediaItem.isVideo,
                                isGif = mediaItem.isGif,
                                durationMillis = mediaItem.durationMillis,
                                isFavorite = mediaItem.isFavorite,
                                isSelected = false,
                                isSelectionModeActive = false,
                                cellShape = RoundedCornerShape(12.dp),
                                targetRatio = 1f,
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.SegmentTick)
                                    onMediaClick(mediaItem, uiState.matchingMedia)
                                }
                            )
                        }
                    }
                }
            }
        }

        // Diálogo de Configuración de PIN de la Bóveda
        if (isVaultSetupDialogVisible) {
            AlertDialog(
                onDismissRequest = { isVaultSetupDialogVisible = false },
                title = { Text(text = stringResource(R.string.vault_setup_title)) },
                text = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = stringResource(R.string.vault_setup_subtitle),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        OutlinedTextField(
                            value = vaultPinInput,
                            onValueChange = { vaultPinInput = it },
                            label = { Text(text = stringResource(R.string.vault_pin_hint)) },
                            visualTransformation = PasswordVisualTransformation(),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = vaultConfirmPinInput,
                            onValueChange = { vaultConfirmPinInput = it },
                            label = { Text(text = stringResource(R.string.vault_pin_confirm_hint)) },
                            visualTransformation = PasswordVisualTransformation(),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        if (vaultPinError != null) {
                            Text(
                                text = vaultPinError ?: "",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            when {
                                vaultPinInput.trim().length < 4 -> {
                                    vaultPinError = context.getString(R.string.vault_pin_invalid)
                                }
                                vaultPinInput.trim() != vaultConfirmPinInput.trim() -> {
                                    vaultPinError = context.getString(R.string.vault_pin_mismatch)
                                }
                                else -> {
                                    viewModel.setVaultPin(vaultPinInput.trim())
                                    isVaultSetupDialogVisible = false
                                    onNavigateToVault()
                                }
                            }
                        }
                    ) {
                        Text(text = stringResource(R.string.vault_action_set_pin))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { isVaultSetupDialogVisible = false }) {
                        Text(text = stringResource(R.string.action_cancel))
                    }
                }
            )
        }

        // Diálogo de Desbloqueo por PIN de la Bóveda
        if (isVaultUnlockDialogVisible) {
            val coroutineScope = rememberCoroutineScope()
            AlertDialog(
                onDismissRequest = { isVaultUnlockDialogVisible = false },
                title = { Text(text = stringResource(R.string.vault_enter_pin_title)) },
                text = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = stringResource(R.string.vault_enter_pin_subtitle),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        OutlinedTextField(
                            value = vaultPinInput,
                            onValueChange = { vaultPinInput = it },
                            label = { Text(text = stringResource(R.string.vault_pin_hint)) },
                            visualTransformation = PasswordVisualTransformation(),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        if (vaultPinError != null) {
                            Text(
                                text = vaultPinError ?: "",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                val isCorrect = viewModel.verifyVaultPin(vaultPinInput.trim())
                                if (isCorrect) {
                                    isVaultUnlockDialogVisible = false
                                    onNavigateToVault()
                                } else {
                                    vaultPinError = context.getString(R.string.vault_pin_incorrect)
                                }
                            }
                        }
                    ) {
                        Text(text = stringResource(R.string.vault_action_unlock))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { isVaultUnlockDialogVisible = false }) {
                        Text(text = stringResource(R.string.action_cancel))
                    }
                }
            )
        }
    }
}

/**
 * Chip individual con estilo Material 3 Expressive para filtros rápidos.
 */
@Composable
private fun SearchFilterChipItem(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    icon: ImageVector? = null,
    modifier: Modifier = Modifier
) {
    FilterChip(
        selected = isSelected,
        onClick = onClick,
        label = { Text(text = label) },
        leadingIcon = if (icon != null) {
            {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            }
        } else null,
        shape = RoundedCornerShape(12.dp),
        colors = FilterChipDefaults.filterChipColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
            selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimaryContainer
        ),
        modifier = modifier
    )
}

/**
 * Tarjeta horizontal representativa de álbum encontrado en la búsqueda.
 */
@Composable
private fun AlbumSearchCard(
    album: Album,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        modifier = modifier
            .width(160.dp)
            .clip(RoundedCornerShape(16.dp))
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .background(MaterialTheme.colorScheme.surfaceContainerHighest),
                contentAlignment = Alignment.Center
            ) {
                if (!album.coverUri.isNullOrEmpty()) {
                    AsyncImage(
                        model = album.coverUri,
                        contentDescription = album.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        imageVector = Icons.Filled.Folder,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }

            Column(
                modifier = Modifier.padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = album.name,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = stringResource(R.string.album_items_count, album.itemCount),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun VaultSearchTriggerCard(
    isConfigured: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Lock,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(24.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.vault_search_trigger_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = if (isConfigured) {
                        stringResource(R.string.vault_search_trigger_subtitle)
                    } else {
                        stringResource(R.string.vault_search_setup_subtitle)
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
