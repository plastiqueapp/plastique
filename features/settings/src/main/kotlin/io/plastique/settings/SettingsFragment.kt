package io.plastique.settings

import android.content.Context
import android.os.Bundle
import android.text.SpannableString
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.AttributeSet
import android.view.View
import android.widget.TextView
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceViewHolder
import androidx.preference.SwitchPreferenceCompat
import io.plastique.core.navigation.navigationContext
import io.plastique.core.session.Session
import io.plastique.core.session.SessionManager
import io.plastique.inject.getComponent
import io.reactivex.android.schedulers.AndroidSchedulers
import javax.inject.Inject

class SettingsFragment : BasePreferenceFragment() {
    @Inject lateinit var navigator: SettingsNavigator
    @Inject lateinit var sessionManager: SessionManager

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings_main, rootKey)

        val matureContentPreference = findPreference<Preference>("content.show_mature")!!
        matureContentPreference.summaryProvider = Preference.SummaryProvider<Preference> { preference ->
            if (preference.isEnabled) "" else getMatureContentSummary()
        }

        sessionManager.sessionChanges
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { session -> matureContentPreference.isEnabled = session is Session.User }
            .disposeOnDestroy()

        preferenceScreen.forEach { preference ->
            if (preference is ListPreference) {
                preference.summary = preference.entry
            }
        }
    }

    private fun getMatureContentSummary(): CharSequence {
        val signInText = getString(R.string.common_button_sign_in)
        return SpannableString.valueOf(getString(R.string.settings_content_show_mature_summary, signInText)).apply {
            val linkSpan = ClickableLinkSpan { navigator.openLogin(navigationContext) }
            val signInStart = indexOf(signInText)
            setSpan(linkSpan, signInStart, signInStart + signInText.length, 0)
        }
    }

    override fun injectDependencies() {
        getComponent<SettingsFragmentComponent>().inject(this)
    }
}

class MatureContentPreference(context: Context, attrs: AttributeSet?) : SwitchPreferenceCompat(context, attrs) {
    override fun syncSummaryView(holder: PreferenceViewHolder) {
        super.syncSummaryView(holder)
        val summaryView = holder.findViewById(android.R.id.summary) as TextView
        summaryView.isEnabled = true
        summaryView.movementMethod = LinkMovementMethod.getInstance()
    }
}

private class ClickableLinkSpan(private val listener: (View) -> Unit) : ClickableSpan() {
    override fun onClick(widget: View) {
        listener(widget)
    }
}
