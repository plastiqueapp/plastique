package io.plastique.deviations.list

import io.plastique.util.Preferences

enum class LayoutMode(val id: String) {
    Grid("grid"),
    Flex("flex"),
    List("list");

    object CONVERTER : Preferences.Converter<LayoutMode> {
        override fun fromString(string: String, defaultValue: LayoutMode): LayoutMode {
            return values().firstOrNull { it.id == string } ?: defaultValue
        }

        override fun toString(value: LayoutMode): String {
            return value.id
        }
    }

    companion object {
        val DEFAULT = Grid
    }
}
