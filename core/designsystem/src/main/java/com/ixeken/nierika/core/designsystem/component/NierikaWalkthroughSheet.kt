package com.ixeken.nierika.core.designsystem.component

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ixeken.nierika.core.designsystem.R
import com.ixeken.nierika.core.designsystem.theme.nierikaMotionScheme
import kotlinx.coroutines.launch

/**
 * Modelo de datos inmutable que define una diapositiva del recorrido de bienvenida.
 */
@Immutable
data class WalkthroughPageData(
    val badgeRes: Int,
    val titleRes: Int,
    val descRes: Int,
    val icon: ImageVector,
    val pageType: WalkthroughPageType
)

/**
 * Tipos de página para desacoplar el renderizado de la tarjeta gráfica destacada.
 */
enum class WalkthroughPageType {
    M3_EXPRESSIVE,
    TIMELINE,
    COLLECTIONS,
    ZIP_ARCHIVE
}

/**
 * Hoja modal Material 3 Expressive que despliega el recorrido interactivo de 4 páginas
 * destacando las capacidades clave de Nierika Gallery.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NierikaWalkthroughSheet(
    onDismissRequest: () -> Unit,
    onComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val coroutineScope = rememberCoroutineScope()
    val motionScheme = MaterialTheme.nierikaMotionScheme

    val pages = listOf(
        WalkthroughPageData(
            badgeRes = R.string.walkthrough_badge_m3,
            titleRes = R.string.walkthrough_title_m3,
            descRes = R.string.walkthrough_desc_m3,
            icon = Icons.Filled.Palette,
            pageType = WalkthroughPageType.M3_EXPRESSIVE
        ),
        WalkthroughPageData(
            badgeRes = R.string.walkthrough_badge_timeline,
            titleRes = R.string.walkthrough_title_timeline,
            descRes = R.string.walkthrough_desc_timeline,
            icon = Icons.Filled.PhotoLibrary,
            pageType = WalkthroughPageType.TIMELINE
        ),
        WalkthroughPageData(
            badgeRes = R.string.walkthrough_badge_collections,
            titleRes = R.string.walkthrough_title_collections,
            descRes = R.string.walkthrough_desc_collections,
            icon = Icons.Filled.Collections,
            pageType = WalkthroughPageType.COLLECTIONS
        ),
        WalkthroughPageData(
            badgeRes = R.string.walkthrough_badge_zip,
            titleRes = R.string.walkthrough_title_zip,
            descRes = R.string.walkthrough_desc_zip,
            icon = Icons.Filled.FolderZip,
            pageType = WalkthroughPageType.ZIP_ARCHIVE
        )
    )

    val pagerState = rememberPagerState(pageCount = { pages.size })
    val isLastPage = pagerState.currentPage == pages.size - 1

    BackHandler {
        coroutineScope.launch {
            sheetState.hide()
            onDismissRequest()
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        tonalElevation = 6.dp,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 16.dp, bottom = 8.dp)
                    .size(width = 40.dp, height = 4.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.outlineVariant)
            )
        },
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp)
        ) {
            // Cabecera con Logo Nierika y botón Saltar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Image(
                        painter = painterResource(R.drawable.nierika_logo),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = stringResource(R.string.app_name),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.5).sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                if (!isLastPage) {
                    TextButton(
                        onClick = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(
                                    page = pages.size - 1,
                                    animationSpec = motionScheme.fastSpatialSpec()
                                )
                            }
                        }
                    ) {
                        Text(
                            text = stringResource(R.string.walkthrough_action_skip),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.size(48.dp))
                }
            }

            // Pager horizontal con las 4 diapositivas
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(350.dp)
            ) { pageIndex ->
                WalkthroughPageContent(
                    data = pages[pageIndex],
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Indicador de puntos elásticos (Pill morphing)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(pages.size) { dotIndex ->
                    val isSelected = pagerState.currentPage == dotIndex
                    val dotWidth by animateDpAsState(
                        targetValue = if (isSelected) 28.dp else 6.dp,
                        animationSpec = motionScheme.fastSpatialSpec(),
                        label = "dotWidth"
                    )
                    val dotColor = if (isSelected) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.outlineVariant
                    }

                    Box(
                        modifier = Modifier
                            .padding(horizontal = 3.dp)
                            .size(width = dotWidth, height = 6.dp)
                            .clip(CircleShape)
                            .background(dotColor)
                            .clickable {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(
                                        page = dotIndex,
                                        animationSpec = motionScheme.fastSpatialSpec()
                                    )
                                }
                            }
                    )
                }
            }

            // Botón de acción principal
            ButtonWithAnimatedShape(
                onClick = {
                    if (isLastPage) {
                        coroutineScope.launch {
                            sheetState.hide()
                            onComplete()
                        }
                    } else {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(
                                page = pagerState.currentPage + 1,
                                animationSpec = motionScheme.fastSpatialSpec()
                            )
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                AnimatedContent(
                    targetState = isLastPage,
                    label = "btnTextContent"
                ) { last ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = if (last) {
                                stringResource(R.string.walkthrough_action_start)
                            } else {
                                stringResource(R.string.walkthrough_action_next)
                            },
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Icon(
                            imageVector = if (last) Icons.Filled.Check else Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Renderiza el contenido de cada diapositiva: Hero Card ilustrada + Textos semánticos.
 */
@Composable
private fun WalkthroughPageContent(
    data: WalkthroughPageData,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Hero Graphic Card
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHighest,
            tonalElevation = 2.dp,
            modifier = Modifier
                .fillMaxWidth()
                .height(196.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                when (data.pageType) {
                    WalkthroughPageType.M3_EXPRESSIVE -> HeroM3Expressive()
                    WalkthroughPageType.TIMELINE -> HeroTimeline()
                    WalkthroughPageType.COLLECTIONS -> HeroCollections()
                    WalkthroughPageType.ZIP_ARCHIVE -> HeroZipArchive()
                }
            }
        }

        // Bloque de texto
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, start = 4.dp, end = 4.dp)
        ) {
            Text(
                text = stringResource(data.badgeRes),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            Text(
                text = stringResource(data.titleRes),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 6.dp)
            )
            Text(
                text = stringResource(data.descRes),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Ilustración Hero: Barra flotante Nierika con fotos reales de fondo.
 */
@Composable
private fun HeroM3Expressive() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(20.dp)),
        contentAlignment = Alignment.BottomCenter
    ) {
        // Fondo: Fotos reales con velo degradado
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Image(
                painter = painterResource(R.drawable.walkthrough_sample_landscape),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
                    .clip(RoundedCornerShape(14.dp))
            )
            Image(
                painter = painterResource(R.drawable.walkthrough_sample_portrait),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
                    .clip(RoundedCornerShape(14.dp))
            )
            Image(
                painter = painterResource(R.drawable.walkthrough_sample_album),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
                    .clip(RoundedCornerShape(14.dp))
            )
        }

        // Velo oscuro para contraste de la barra
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.5f),
                            Color.Black.copy(alpha = 0.85f)
                        )
                    )
                )
        )

        // Réplica 1:1 de NierikaFloatingNavBar
        Row(
            modifier = Modifier.padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Cápsula central
            Surface(
                shape = CircleShape,
                color = Color(0xFF2D3135),
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Pestaña activa (Library / Fotos)
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFFA8C7FA)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.PhotoLibrary,
                                contentDescription = null,
                                tint = Color(0xFF041E49),
                                modifier = Modifier.size(15.dp)
                            )
                            Text(
                                text = stringResource(R.string.nav_tab_library),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF041E49)
                            )
                        }
                    }

                    // Pestaña inactiva (Collections / Colecciones): Solo texto, sin icono
                    Text(
                        text = stringResource(R.string.nav_tab_collections),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFC4C7C5),
                        modifier = Modifier.padding(end = 12.dp)
                    )
                }
            }

            // Botón satélite circular de búsqueda
            Surface(
                shape = CircleShape,
                color = Color(0xFF2D3135),
                shadowElevation = 8.dp,
                modifier = Modifier.size(42.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(17.dp)
                    )
                }
            }
        }
    }
}

/**
 * Ilustración Hero: Retícula Comfy de biblioteca real en dos filas sin adornos superfluos.
 */
@Composable
private fun HeroTimeline() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Fila 1: 2 fotos (panorámica + estándar)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Image(
                painter = painterResource(R.drawable.walkthrough_sample_landscape),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .weight(1.6f)
                    .fillMaxSize()
                    .clip(RoundedCornerShape(16.dp))
            )
            Image(
                painter = painterResource(R.drawable.walkthrough_sample_album),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .weight(1.1f)
                    .fillMaxSize()
                    .clip(RoundedCornerShape(16.dp))
            )
        }

        // Fila 2: 3 fotos (vertical + vertical + cuadrada)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Image(
                painter = painterResource(R.drawable.walkthrough_sample_portrait),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
                    .clip(RoundedCornerShape(16.dp))
            )
            Image(
                painter = painterResource(R.drawable.walkthrough_sample_landscape),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
                    .clip(RoundedCornerShape(16.dp))
            )
            Image(
                painter = painterResource(R.drawable.walkthrough_sample_album),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .weight(1.3f)
                    .fillMaxSize()
                    .clip(RoundedCornerShape(16.dp))
            )
        }
    }
}

/**
 * Ilustración Hero: Tarjetas de colección tipo squircle idénticas a la pantalla de Colecciones.
 */
@Composable
private fun HeroCollections() {
    Row(
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Tarjeta 1
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 4.dp)
        ) {
            Image(
                painter = painterResource(R.drawable.walkthrough_sample_album),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(96.dp)
                    .clip(RoundedCornerShape(22.dp))
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.walkthrough_hero_sample_album_1),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = stringResource(R.string.walkthrough_hero_sample_count_1),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline
            )
        }

        // Tarjeta 2
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 4.dp)
        ) {
            Image(
                painter = painterResource(R.drawable.walkthrough_sample_portrait),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(96.dp)
                    .clip(RoundedCornerShape(22.dp))
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.walkthrough_hero_sample_album_2),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = stringResource(R.string.walkthrough_hero_sample_count_2),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}

/**
 * Ilustración Hero: Réplica exacta de ExportAlbumProgressDialog con barra ondulada M3 Expressive.
 */
@Composable
private fun HeroZipArchive() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = RoundedCornerShape(26.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 18.dp, end = 18.dp, top = 10.dp, bottom = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Icono destacado en cápsula circular tonal
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.FolderZip,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Título del diálogo con nombre del álbum
                Text(
                    text = stringResource(R.string.walkthrough_hero_zip_exporting_title),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(2.dp))

                // Conteo de archivos procesados
                Text(
                    text = stringResource(R.string.walkthrough_hero_zip_progress_count),
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Barra de progreso ondulada M3 Expressive oficial
                NierikaLinearWavyProgressIndicator(
                    progress = 0.72f,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Porcentaje numérico alineado a la derecha
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(
                        text = "72%",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}
