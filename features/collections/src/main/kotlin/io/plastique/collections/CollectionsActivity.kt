package io.plastique.collections

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import io.plastique.core.BaseActivity
import io.plastique.core.extensions.setActionBar
import io.plastique.core.extensions.setSubtitleOnClickListener
import io.plastique.core.extensions.setTitleOnClickListener
import io.plastique.inject.getComponent

class CollectionsActivity : BaseActivity() {
    private lateinit var contentFragment: CollectionsFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_collections)

        val username = intent.getStringExtra(EXTRA_USERNAME)
        initToolbar(username)

        if (savedInstanceState == null) {
            contentFragment = CollectionsFragment.newInstance(username)
            supportFragmentManager.beginTransaction()
                    .add(R.id.collections_container, contentFragment)
                    .commit()
        } else {
            contentFragment = supportFragmentManager.findFragmentById(R.id.collections_container) as CollectionsFragment
        }
    }

    private fun initToolbar(username: String?) {
        val toolbar = setActionBar(R.id.toolbar) {
            subtitle = username
            setDisplayHomeAsUpEnabled(true)
        }

        val listener = View.OnClickListener { contentFragment.scrollToTop() }
        toolbar.setTitleOnClickListener(listener)
        toolbar.setSubtitleOnClickListener(listener)
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
