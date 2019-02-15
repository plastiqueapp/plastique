package io.plastique.settings

interface SettingsFragmentComponent {
    fun inject(fragment: AboutFragment)

    fun inject(fragment: NotificationSettingsFragment)

    fun inject(fragment: SettingsFragment)
}
