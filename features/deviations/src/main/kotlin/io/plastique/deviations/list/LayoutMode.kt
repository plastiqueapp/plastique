package io.plastique.deviations.list

import io.plastique.util.Preferences

enum class LayoutMode(val id: String) {
    Grid("grid"),
    Flex("flex"),
    List("list");

    companion object {
        val DEFAULT = Grid
    }
}

object LayoutModeConverter : Preferences.Converter<LayoutMode> {
    override fun fromString(string: String, defaultValue: LayoutMode): LayoutMode {
        return LayoutMode.values().firstOrNull { it.id == string } ?: defaultValue
    }

    override fun toString(value: LayoutMode): String {
        return value.id
    }
}
