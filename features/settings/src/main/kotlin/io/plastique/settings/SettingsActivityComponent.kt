package io.plastique.settings

import io.plastique.settings.licenses.LicensesActivity

interface SettingsActivityComponent {
    fun inject(activity: LicensesActivity)

    fun inject(activity: SettingsActivity)
}
