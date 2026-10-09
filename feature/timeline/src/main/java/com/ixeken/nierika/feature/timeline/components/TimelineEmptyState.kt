package com.ixeken.nierika.feature.timeline.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ixeken.nierika.core.domain.model.QuickFilter
import com.ixeken.nierika.core.designsystem.R

/**
 * Componente M3 Expressive para desplegar estados vacíos con icono y texto explicativo
 * cuando no hay elementos en Favoritos, Papelera o la Galería.
 */
@Composable
fun TimelineEmptyState(
    quickFilter: QuickFilter,
    modifier: Modifier = Modifier
) {
    val (icon, titleRes, subtitleRes) = when (quickFilter) {
        QuickFilter.FAVORITES -> Triple(
            Icons.Filled.Favorite,
            R.string.empty_favorites_title,
            R.string.empty_favorites_subtitle
        )
        QuickFilter.VIDEOS -> Triple(
            Icons.Filled.VideoLibrary,
            R.string.empty_videos_title,
            R.string.empty_videos_subtitle
        )
        QuickFilter.TRASH -> Triple(
            Icons.Filled.Delete,
            R.string.empty_trash_title,
            R.string.empty_trash_subtitle
        )
        else -> Triple(
            Icons.Filled.PhotoLibrary,
            R.string.empty_timeline_title,
            R.string.empty_timeline_subtitle
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp, vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceContainerHigh),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(44.dp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = stringResource(titleRes),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stringResource(subtitleRes),
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.widthIn(max = 280.dp)
        )
    }
}
