package io.plastique.collections

import android.content.Context
import android.content.Intent
import android.os.Bundle
import io.plastique.core.BaseActivity
import io.plastique.core.extensions.setActionBar
import io.plastique.inject.getComponent

class CollectionsActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_collections)

        val username = intent.getStringExtra(EXTRA_USERNAME)!!
        setActionBar(R.id.toolbar) {
            subtitle = username
            setDisplayHomeAsUpEnabled(true)
        }

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                    .add(R.id.collections_container, CollectionsFragment.newInstance(username))
                    .commit()
        }
    }

    override fun injectDependencies() {
        getComponent<CollectionsActivityComponent>().inject(this)
    }

    companion object {
        private const val EXTRA_USERNAME = "username"

        fun createIntent(context: Context, username: String): Intent {
            return Intent(context, CollectionsActivity::class.java).apply {
                putExtra(EXTRA_USERNAME, username)
            }
        }
    }
}
