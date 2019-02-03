package io.plastique.settings

interface SettingsFragmentComponent {
    fun inject(fragment: AboutFragment)

    fun inject(fragment: SettingsFragment)
}
