package io.plastique.settings

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import io.plastique.core.BaseActivity
import io.plastique.core.extensions.instantiate
import io.plastique.core.extensions.setActionBar
import io.plastique.inject.getComponent

class SettingsActivity : BaseActivity(), PreferenceFragmentCompat.OnPreferenceStartFragmentCallback {
    private val fragmentLifecycleCallbacks = object : FragmentManager.FragmentLifecycleCallbacks() {
        override fun onFragmentViewCreated(fragmentManager: FragmentManager, fragment: Fragment, view: View, savedInstanceState: Bundle?) {
            if (fragment is PreferenceFragmentCompat) {
                title = fragment.preferenceScreen.title
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        setActionBar(R.id.toolbar) {
            setDisplayHomeAsUpEnabled(true)
        }

        supportFragmentManager.registerFragmentLifecycleCallbacks(fragmentLifecycleCallbacks, false)

        if (savedInstanceState == null) {
            val fragment = supportFragmentManager.fragmentFactory.instantiate<SettingsFragment>(this)
            supportFragmentManager.beginTransaction()
                    .add(R.id.settings_container, fragment)
                    .commit()
        }
    }

    override fun onPreferenceStartFragment(caller: PreferenceFragmentCompat, pref: Preference): Boolean {
        val args = pref.extras
        val fragment = supportFragmentManager.fragmentFactory.instantiate(classLoader, pref.fragment, args)
        fragment.setTargetFragment(caller, 0)

        supportFragmentManager.beginTransaction()
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .replace(R.id.settings_container, fragment)
                .addToBackStack(null)
                .commit()
        return true
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
