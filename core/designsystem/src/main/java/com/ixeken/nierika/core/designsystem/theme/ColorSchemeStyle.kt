package com.ixeken.nierika.core.designsystem.theme

/**
 * Estilos de paleta de color oficial de Material Design 3 (Dynamic + 13 semillas tonales de Material Theme Builder).
 */
enum class ColorSchemeStyle(val id: String) {
    DYNAMIC("dynamic"),
    MONOCHROME("monochrome"),
    PINK("pink"),
    ROSE("rose"),
    RED("red"),
    ORANGE("orange"),
    YELLOW("yellow"),
    CHARTREUSE("chartreuse"),
    GREEN("green"),
    TEAL("teal"),
    CYAN("cyan"),
    BLUE("blue"),
    INDIGO("indigo"),
    PURPLE("purple");

    companion object {
        fun fromId(id: String): ColorSchemeStyle {
            return entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: DYNAMIC
        }
    }
}
