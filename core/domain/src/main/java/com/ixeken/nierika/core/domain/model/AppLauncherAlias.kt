package com.ixeken.nierika.core.domain.model

/**
 * Estilos visuales disponibles para el icono de la aplicación en el Launcher.
 */
enum class AppIconStyle(
    val id: String,
    val isCamouflage: Boolean
) {
    PHOTO_RAINBOW(id = "photo_rainbow", isCamouflage = false),
    NIERIKA(id = "nierika", isCamouflage = false),
    NEPO(id = "nepo", isCamouflage = true),
    DRAFTO(id = "drafto", isCamouflage = true);

    companion object {
        fun fromId(id: String): AppIconStyle {
            return entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: PHOTO_RAINBOW
        }
    }
}

/**
 * Opciones de etiqueta de texto para el Launcher de Android.
 */
enum class AppLabelOption(
    val id: String,
    val isCamouflage: Boolean
) {
    NIERIKA(id = "nierika", isCamouflage = false),
    GALLERY(id = "gallery", isCamouflage = false),
    PHOTOS(id = "photos", isCamouflage = false),
    LIBRARY(id = "library", isCamouflage = false),
    CAMERA(id = "camera", isCamouflage = false),
    CALCULATOR(id = "calculator", isCamouflage = true),
    NEPO(id = "nepo", isCamouflage = true),
    NOTES(id = "notes", isCamouflage = true),
    DRAFTO(id = "drafto", isCamouflage = true);

    companion object {
        fun fromId(id: String): AppLabelOption {
            return entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: NIERIKA
        }
    }
}

/**
 * Variantes de actividad alias declaradas en AndroidManifest.xml.
 * Cada elemento vincula un estilo de icono con una etiqueta de launcher.
 */
enum class AppLauncherAlias(
    val id: String,
    val aliasName: String,
    val iconStyle: AppIconStyle,
    val labelOption: AppLabelOption,
    val isCamouflage: Boolean
) {
    // Icono Galería Arcoíris (Default)
    DEFAULT(
        id = "default",
        aliasName = "com.ixeken.nierika.MainActivityDefault",
        iconStyle = AppIconStyle.PHOTO_RAINBOW,
        labelOption = AppLabelOption.NIERIKA,
        isCamouflage = false
    ),
    PHOTO_RAINBOW_GALLERY(
        id = "pr_gallery",
        aliasName = "com.ixeken.nierika.MainActivityPhotoRainbowGallery",
        iconStyle = AppIconStyle.PHOTO_RAINBOW,
        labelOption = AppLabelOption.GALLERY,
        isCamouflage = false
    ),
    PHOTO_RAINBOW_PHOTOS(
        id = "pr_photos",
        aliasName = "com.ixeken.nierika.MainActivityPhotoRainbowPhotos",
        iconStyle = AppIconStyle.PHOTO_RAINBOW,
        labelOption = AppLabelOption.PHOTOS,
        isCamouflage = false
    ),
    PHOTO_RAINBOW_LIBRARY(
        id = "pr_library",
        aliasName = "com.ixeken.nierika.MainActivityPhotoRainbowLibrary",
        iconStyle = AppIconStyle.PHOTO_RAINBOW,
        labelOption = AppLabelOption.LIBRARY,
        isCamouflage = false
    ),
    PHOTO_RAINBOW_CAMERA(
        id = "pr_camera",
        aliasName = "com.ixeken.nierika.MainActivityPhotoRainbowCamera",
        iconStyle = AppIconStyle.PHOTO_RAINBOW,
        labelOption = AppLabelOption.CAMERA,
        isCamouflage = false
    ),

    // Icono Nierika Clásico
    CLASSIC(
        id = "classic",
        aliasName = "com.ixeken.nierika.MainActivityClassic",
        iconStyle = AppIconStyle.NIERIKA,
        labelOption = AppLabelOption.NIERIKA,
        isCamouflage = false
    ),
    GALLERY(
        id = "gallery",
        aliasName = "com.ixeken.nierika.MainActivityGallery",
        iconStyle = AppIconStyle.NIERIKA,
        labelOption = AppLabelOption.GALLERY,
        isCamouflage = false
    ),
    PHOTOS(
        id = "photos",
        aliasName = "com.ixeken.nierika.MainActivityPhotos",
        iconStyle = AppIconStyle.NIERIKA,
        labelOption = AppLabelOption.PHOTOS,
        isCamouflage = false
    ),
    LIBRARY(
        id = "library",
        aliasName = "com.ixeken.nierika.MainActivityLibrary",
        iconStyle = AppIconStyle.NIERIKA,
        labelOption = AppLabelOption.LIBRARY,
        isCamouflage = false
    ),
    CAMERA(
        id = "camera",
        aliasName = "com.ixeken.nierika.MainActivityCamera",
        iconStyle = AppIconStyle.NIERIKA,
        labelOption = AppLabelOption.CAMERA,
        isCamouflage = false
    ),


    // Icono Nepo (Calculadora)
    CALCULATOR(
        id = "calculator",
        aliasName = "com.ixeken.nierika.MainActivityCalculator",
        iconStyle = AppIconStyle.NEPO,
        labelOption = AppLabelOption.CALCULATOR,
        isCamouflage = true
    ),
    NEPO(
        id = "nepo",
        aliasName = "com.ixeken.nierika.MainActivityNepo",
        iconStyle = AppIconStyle.NEPO,
        labelOption = AppLabelOption.NEPO,
        isCamouflage = true
    ),

    // Icono Drafto (Notas)
    NOTES(
        id = "notes",
        aliasName = "com.ixeken.nierika.MainActivityNotes",
        iconStyle = AppIconStyle.DRAFTO,
        labelOption = AppLabelOption.NOTES,
        isCamouflage = true
    ),
    DRAFTO(
        id = "drafto",
        aliasName = "com.ixeken.nierika.MainActivityDrafto",
        iconStyle = AppIconStyle.DRAFTO,
        labelOption = AppLabelOption.DRAFTO,
        isCamouflage = true
    );

    companion object {
        fun fromId(id: String): AppLauncherAlias {
            if (id.equals("pr_nierika", ignoreCase = true)) return DEFAULT
            return entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: DEFAULT
        }

        fun resolveAlias(icon: AppIconStyle, label: AppLabelOption): AppLauncherAlias {
            return entries.firstOrNull { it.iconStyle == icon && it.labelOption == label }
                ?: entries.firstOrNull { it.iconStyle == icon }
                ?: DEFAULT
        }
    }
}
