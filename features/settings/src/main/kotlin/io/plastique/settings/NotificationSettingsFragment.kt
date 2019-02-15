package io.plastique.settings

import android.os.Bundle
import io.plastique.inject.getComponent

class NotificationSettingsFragment : BasePreferenceFragment() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings_notifications, rootKey)
    }

    override fun injectDependencies() {
        getComponent<SettingsFragmentComponent>().inject(this)
    }
}
