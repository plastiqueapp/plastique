package io.plastique.settings

import androidx.preference.Preference
import androidx.preference.PreferenceGroup

fun PreferenceGroup.forEach(action: (preference: Preference) -> Unit) {
    for (i in 0 until preferenceCount) {
        val preference = getPreference(i)
        action(preference)

        if (preference is PreferenceGroup) {
            preference.forEach(action)
        }
    }
}
