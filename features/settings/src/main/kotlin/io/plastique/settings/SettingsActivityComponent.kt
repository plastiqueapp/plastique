package io.plastique.settings

import io.plastique.settings.about.AboutActivity
import io.plastique.settings.about.licenses.LicensesActivity

interface SettingsActivityComponent {
    fun inject(activity: AboutActivity)

    fun inject(activity: LicensesActivity)

    fun inject(activity: SettingsActivity)
}
