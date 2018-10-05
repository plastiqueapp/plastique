package io.plastique.settings

import io.plastique.settings.about.AboutFragment

interface SettingsFragmentComponent {
    fun inject(fragment: AboutFragment)
}
