package com.ixeken.nierika.feature.editor.components

import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.canhub.cropper.CropImageView
import com.ixeken.nierika.feature.editor.model.CropAspectRatio

/**
 * Envoltorio Compose del motor de recorte de alta precisión adoptado de Fossify Gallery (CanHub / android-image-cropper).
 * 
 * Ofrece soporte para auto-zoom suave al redimensionar el marco de recorte, encuadre elástico dentro de los límites
 * de la fotografía, rotación de 90 grados, volteo horizontal/vertical y proporciones de aspecto fijas o libres.
 */
@Composable
fun NierikaCropView(
    mediaUri: String,
    croppedBitmap: Bitmap? = null,
    aspectRatio: CropAspectRatio,
    customAspectRatioPair: Pair<Float, Float>?,
    rotationDegrees: Float,
    isFlippedHorizontally: Boolean,
    isFlippedVertically: Boolean,
    onCropViewReady: (CropImageView) -> Unit,
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val context = LocalContext.current

    val cropImageView = remember(mediaUri, croppedBitmap) {
        CropImageView(context).apply {
            guidelines = CropImageView.Guidelines.ON_TOUCH
            setMultiTouchEnabled(true)
            isShowProgressBar = false
            setBackgroundColor(android.graphics.Color.BLACK)
            try {
                this.javaClass.methods.firstOrNull { it.name == "setBorderCornerColor" }?.invoke(this, primaryColor.toArgb())
            } catch (_: Throwable) {
                // Ignore if method signature varies
            }
            if (croppedBitmap != null) {
                setImageBitmap(croppedBitmap)
            } else {
                setImageUriAsync(Uri.parse(mediaUri))
            }
        }
    }

    LaunchedEffect(mediaUri, croppedBitmap) {
        if (croppedBitmap != null) {
            cropImageView.setImageBitmap(croppedBitmap)
        } else {
            cropImageView.setImageUriAsync(Uri.parse(mediaUri))
        }
    }

    DisposableEffect(cropImageView) {
        onCropViewReady(cropImageView)
        onDispose { }
    }

    // Sincronizar relación de aspecto
    LaunchedEffect(aspectRatio, customAspectRatioPair) {
        when {
            aspectRatio == CropAspectRatio.FREE -> {
                cropImageView.setFixedAspectRatio(false)
            }
            customAspectRatioPair != null -> {
                cropImageView.setAspectRatio(
                    (customAspectRatioPair.first * 100).toInt(),
                    (customAspectRatioPair.second * 100).toInt()
                )
            }
            aspectRatio.ratio != null -> {
                val ratio = aspectRatio.ratio
                val (x, y) = when (aspectRatio) {
                    CropAspectRatio.RATIO_1_1 -> Pair(1, 1)
                    CropAspectRatio.RATIO_4_5 -> Pair(4, 5)
                    CropAspectRatio.RATIO_9_16 -> Pair(9, 16)
                    CropAspectRatio.RATIO_16_9 -> Pair(16, 9)
                    CropAspectRatio.RATIO_3_4 -> Pair(3, 4)
                    else -> Pair((ratio * 100).toInt(), 100)
                }
                cropImageView.setAspectRatio(x, y)
            }
        }
    }

    // Sincronizar rotación del usuario (sólo aplicar deltas cuando el usuario rota intencionalmente)
    var lastKnownUserRotation by remember(mediaUri, croppedBitmap) { mutableIntStateOf(rotationDegrees.toInt()) }
    LaunchedEffect(rotationDegrees) {
        val target = rotationDegrees.toInt()
        val delta = target - lastKnownUserRotation
        if (delta != 0) {
            cropImageView.rotateImage(delta)
            lastKnownUserRotation = target
        }
    }

    // Sincronizar volteo horizontal
    LaunchedEffect(isFlippedHorizontally) {
        if (cropImageView.isFlippedHorizontally != isFlippedHorizontally) {
            cropImageView.flipImageHorizontally()
        }
    }

    // Sincronizar volteo vertical
    LaunchedEffect(isFlippedVertically) {
        if (cropImageView.isFlippedVertically != isFlippedVertically) {
            cropImageView.flipImageVertically()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        AndroidView(
            factory = { cropImageView },
            modifier = Modifier.fillMaxSize()
        )
    }
}
