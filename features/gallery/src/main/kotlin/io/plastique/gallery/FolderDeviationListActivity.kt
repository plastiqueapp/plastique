package io.plastique.gallery

import android.content.Context
import android.content.Intent
import android.os.Bundle
import io.plastique.core.BaseActivity
import io.plastique.inject.getComponent

class FolderDeviationListActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gallery_folder_deviations)

        val username = intent.getStringExtra(EXTRA_USERNAME)
        val folderId = intent.getStringExtra(EXTRA_FOLDER_ID)!!
        val folderName = intent.getStringExtra(EXTRA_FOLDER_NAME)!!

        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar!!.apply {
            title = folderName
            subtitle = username
            setDisplayHomeAsUpEnabled(true)
        }
    }

    override fun injectDependencies() {
        getComponent<GalleryActivityComponent>().inject(this)
    }

    companion object {
        private const val EXTRA_USERNAME = "username"
        private const val EXTRA_FOLDER_ID = "folder_id"
        private const val EXTRA_FOLDER_NAME = "folder_name"

        fun createIntent(context: Context, username: String?, folderId: String, folderName: String): Intent {
            return Intent(context, FolderDeviationListActivity::class.java).apply {
                putExtra(EXTRA_USERNAME, username)
                putExtra(EXTRA_FOLDER_ID, folderId)
                putExtra(EXTRA_FOLDER_NAME, folderName)
            }
        }
    }
}
