package io.plastique.settings

import android.os.Bundle
import androidx.preference.ListPreference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceGroup

class SettingsFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.preferences)
        updateSummaries(preferenceScreen)
    }

    private fun updateSummaries(preferenceGroup: PreferenceGroup) {
        for (i in 0 until preferenceGroup.preferenceCount) {
            val preference = preferenceGroup.getPreference(i)
            when (preference) {
                is PreferenceGroup -> updateSummaries(preference)
                is ListPreference -> preference.summary = preference.entry
            }
        }
    }
}
