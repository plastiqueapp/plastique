package io.plastique.settings

import android.os.Bundle
import androidx.preference.ListPreference
import androidx.preference.PreferenceFragmentCompat

class SettingsFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.preferences)

        preferenceScreen.forEach { preference ->
            if (preference is ListPreference) {
                preference.summary = preference.entry
            }
        }
    }
}
