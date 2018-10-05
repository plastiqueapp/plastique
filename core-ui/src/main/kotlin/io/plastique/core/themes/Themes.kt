package io.plastique.core.themes

import androidx.annotation.StringDef

@StringDef(Themes.DARK, Themes.LIGHT)
annotation class Theme

object Themes {
    const val DARK = "dark"
    const val LIGHT = "light"
    const val DEFAULT = LIGHT

    fun isValid(theme: String): Boolean {
        return theme == DARK || theme == LIGHT
    }
}
