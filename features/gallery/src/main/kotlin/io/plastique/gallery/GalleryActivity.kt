package io.plastique.gallery

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import io.plastique.core.BaseActivity
import io.plastique.core.extensions.setActionBar
import io.plastique.core.extensions.setSubtitleOnClickListener
import io.plastique.core.extensions.setTitleOnClickListener
import io.plastique.inject.getComponent

class GalleryActivity : BaseActivity() {
    private lateinit var contentFragment: GalleryFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gallery)

        val username = intent.getStringExtra(EXTRA_USERNAME)
        initToolbar(username)

        if (savedInstanceState == null) {
            contentFragment = GalleryFragment.newInstance(username)
            supportFragmentManager.beginTransaction()
                    .add(R.id.gallery_container, contentFragment)
                    .commit()
        } else {
            contentFragment = supportFragmentManager.findFragmentById(R.id.gallery_container) as GalleryFragment
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
        getComponent<GalleryActivityComponent>().inject(this)
    }

    companion object {
        private const val EXTRA_USERNAME = "username"

        fun createIntent(context: Context, username: String): Intent {
            return Intent(context, GalleryActivity::class.java).apply {
                putExtra(EXTRA_USERNAME, username)
            }
        }
    }
}
