package io.plastique.gallery

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import io.plastique.core.BaseActivity
import io.plastique.inject.getComponent

class GalleryActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gallery)

        val username = intent.getStringExtra(EXTRA_USERNAME)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.subtitle = username

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                    .add(R.id.gallery_container, GalleryFragment.newInstance(username))
                    .commit()
        }
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
