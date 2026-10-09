package com.ixeken.nierika.feature.editor

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.ixeken.nierika.core.designsystem.theme.ColorSchemeStyle
import com.ixeken.nierika.core.designsystem.theme.NierikaTheme
import com.ixeken.nierika.core.designsystem.theme.getAppFontFamilyByName
import com.ixeken.nierika.core.domain.repository.UserPreferencesRepository
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Actividad autónoma e inmersiva para la suite de edición multimedia.
 * Proporciona aislamiento completo de ventana, ciclo de vida y estado frente al visor principal.
 */
@AndroidEntryPoint
class EditorActivity : ComponentActivity() {

    @Inject
    lateinit var userPreferencesRepository: UserPreferencesRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val rawUri = intent.dataString ?: intent.getStringExtra(EXTRA_MEDIA_URI)
        if (rawUri.isNullOrBlank()) {
            finish()
            return
        }

        val mimeType = intent.type ?: contentResolver.getType(Uri.parse(rawUri)).orEmpty()
        val isVideo = intent.getBooleanExtra(EXTRA_IS_VIDEO, mimeType.startsWith("video/"))

        setContent {
            val userPreferences by userPreferencesRepository.userPreferences.collectAsState(
                initial = com.ixeken.nierika.core.domain.repository.UserPreferences()
            )
            val fontFamily = remember(userPreferences.selectedFontFamily) {
                getAppFontFamilyByName(userPreferences.selectedFontFamily)
            }

            NierikaTheme(
                darkTheme = userPreferences.isDarkTheme,
                dynamicColor = userPreferences.useDynamicColor,
                themeStyle = ColorSchemeStyle.fromId(userPreferences.selectedThemeId),
                fontFamily = fontFamily,
                fontScaleMultiplier = userPreferences.fontScaleMultiplier
            ) {
                EditorScreen(
                    mediaUri = rawUri,
                    isVideo = isVideo,
                    onDismiss = {
                        setResult(Activity.RESULT_CANCELED)
                        finish()
                    },
                    onSaveSuccess = { savedUri ->
                        val resultIntent = Intent().apply {
                            data = Uri.parse(savedUri)
                            putExtra(EXTRA_SAVED_URI, savedUri)
                        }
                        setResult(Activity.RESULT_OK, resultIntent)
                        finish()
                    }
                )
            }
        }
    }

    companion object {
        const val EXTRA_MEDIA_URI = "extra_media_uri"
        const val EXTRA_IS_VIDEO = "extra_is_video"
        const val EXTRA_SAVED_URI = "extra_saved_uri"

        fun createIntent(context: Context, mediaUri: String, isVideo: Boolean): Intent {
            return Intent(context, EditorActivity::class.java).apply {
                data = Uri.parse(mediaUri)
                putExtra(EXTRA_MEDIA_URI, mediaUri)
                putExtra(EXTRA_IS_VIDEO, isVideo)
            }
        }
    }
}
