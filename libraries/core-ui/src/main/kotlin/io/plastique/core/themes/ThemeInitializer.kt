package io.plastique.core.themes

import io.plastique.core.init.Initializer
import javax.inject.Inject

class ThemeInitializer @Inject constructor(private val themeManager: ThemeManager) : Initializer() {
    override fun initialize() {
        themeManager.init()
    }
}
