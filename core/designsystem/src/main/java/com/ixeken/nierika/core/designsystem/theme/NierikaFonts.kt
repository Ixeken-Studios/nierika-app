@file:OptIn(ExperimentalTextApi::class)

package com.ixeken.nierika.core.designsystem.theme

import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import com.ixeken.nierika.core.designsystem.R

private fun createVariableFont(resId: Int, weight: FontWeight): Font {
    return Font(
        resId = resId,
        weight = weight,
        variationSettings = FontVariation.Settings(
            FontVariation.weight(weight.weight)
        )
    )
}

private fun createLocalFontFamily(resId: Int): FontFamily = FontFamily(
    createVariableFont(resId, FontWeight.Normal),
    createVariableFont(resId, FontWeight.Medium),
    createVariableFont(resId, FontWeight.SemiBold),
    createVariableFont(resId, FontWeight.Bold),
    createVariableFont(resId, FontWeight.ExtraBold),
    createVariableFont(resId, FontWeight.Black)
)

private val GoogleSansFlexFontFamily = createLocalFontFamily(R.font.google_sans_flex)
private val OutfitFontFamily = createLocalFontFamily(R.font.outfit)
private val AnonymousProFontFamily = createLocalFontFamily(R.font.anonymous_pro)
private val NunitoFontFamily = createLocalFontFamily(R.font.nunito)
private val JosefinSansFontFamily = createLocalFontFamily(R.font.josefin_sans)
private val LilitaOneFontFamily = createLocalFontFamily(R.font.lilita_one)
private val DMSansFontFamily = createLocalFontFamily(R.font.dm_sans)
private val KodeMonoFontFamily = createLocalFontFamily(R.font.kode_mono)

/**
 * Obtiene la fuente tipográfica de la aplicación según su nombre.
 * Soporta fuentes locales integradas (res/font) de alto rendimiento.
 */
fun getAppFontFamilyByName(fontName: String): FontFamily {
    return when (fontName) {
        "System font" -> FontFamily.Default
        "Outfit" -> OutfitFontFamily
        "Anonymous Pro" -> AnonymousProFontFamily
        "Nunito" -> NunitoFontFamily
        "Josefin Sans" -> JosefinSansFontFamily
        "Lilita One" -> LilitaOneFontFamily
        "DM Sans" -> DMSansFontFamily
        "Kode Mono" -> KodeMonoFontFamily
        "Google Sans Flex", "default", "" -> GoogleSansFlexFontFamily
        else -> GoogleSansFlexFontFamily
    }
}

/**
 * Lista de nombres de fuentes disponibles en Nierika.
 */
val availableFontNames = listOf(
    "Google Sans Flex",
    "System font",
    "Outfit",
    "Anonymous Pro",
    "Nunito",
    "Josefin Sans",
    "Lilita One",
    "DM Sans",
    "Kode Mono"
)
