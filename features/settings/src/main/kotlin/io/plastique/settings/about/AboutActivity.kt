package io.plastique.settings.about

import android.content.Context
import android.content.Intent
import android.os.Bundle
import io.plastique.core.BaseActivity
import io.plastique.core.extensions.setActionBar
import io.plastique.inject.getComponent
import io.plastique.settings.R
import io.plastique.settings.SettingsActivityComponent

class AboutActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)
        setActionBar(R.id.toolbar) {
            setDisplayHomeAsUpEnabled(true)
        }
    }

    override fun injectDependencies() {
        getComponent<SettingsActivityComponent>().inject(this)
    }

    companion object {
        fun createIntent(context: Context): Intent {
            return Intent(context, AboutActivity::class.java)
        }
    }
}
