package io.plastique.core.themes

import io.plastique.util.Preferences

inline class ThemeId(val value: String)

object ThemeIdConverter : Preferences.Converter<ThemeId> {
    override fun fromString(string: String, defaultValue: ThemeId): ThemeId = ThemeId(string)

    override fun toString(value: ThemeId): String = value.value
}
