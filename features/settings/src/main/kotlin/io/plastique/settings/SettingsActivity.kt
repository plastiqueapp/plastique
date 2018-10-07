package io.plastique.settings

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceFragmentCompat.OnPreferenceStartScreenCallback
import androidx.preference.PreferenceScreen
import io.plastique.core.BaseActivity
import io.plastique.core.extensions.setActionBar
import io.plastique.inject.getComponent
import io.plastique.settings.about.AboutActivity

class SettingsActivity : BaseActivity(), OnPreferenceStartScreenCallback {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        setActionBar(R.id.toolbar) {
            setDisplayHomeAsUpEnabled(true)
        }
    }

    override fun onPreferenceStartScreen(caller: PreferenceFragmentCompat, pref: PreferenceScreen): Boolean {
        if (pref.key == "screen_about") {
            startActivity(AboutActivity.createIntent(this))
            return true
        }
        return false
    }

    override fun injectDependencies() {
        getComponent<SettingsActivityComponent>().inject(this)
    }

    companion object {
        fun createIntent(context: Context): Intent {
            return Intent(context, SettingsActivity::class.java)
        }
    }
}
