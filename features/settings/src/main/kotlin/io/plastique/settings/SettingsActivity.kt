package io.plastique.settings

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.fragment.app.add
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.github.technoir42.android.extensions.setActionBar
import io.plastique.core.BaseActivity
import io.plastique.core.navigation.Route
import io.plastique.core.navigation.activityRoute
import io.plastique.inject.getComponent
import io.plastique.settings.databinding.ActivitySettingsBinding

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
        val binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setActionBar(binding.toolbar) {
            setDisplayHomeAsUpEnabled(true)
        }

        supportFragmentManager.registerFragmentLifecycleCallbacks(fragmentLifecycleCallbacks, false)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .add<SettingsFragment>(R.id.settings_container)
                .commit()
        }
    }

    override fun onPreferenceStartFragment(caller: PreferenceFragmentCompat, pref: Preference): Boolean {
        val fragment = supportFragmentManager.fragmentFactory.instantiate(classLoader, pref.fragment)
        fragment.arguments = pref.extras
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
        fun route(context: Context): Route = activityRoute<SettingsActivity>(context)
    }
}
