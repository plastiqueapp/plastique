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

class FolderDeviationListActivity : BaseActivity() {
    private lateinit var contentFragment: FolderDeviationListFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_collection_folder_deviations)

        val username = intent.getStringExtra(EXTRA_USERNAME)
        val folderId = intent.getStringExtra(EXTRA_FOLDER_ID)!!
        val folderName = intent.getStringExtra(EXTRA_FOLDER_NAME)!!

        initToolbar(username, folderName)

        if (savedInstanceState == null) {
            contentFragment = FolderDeviationListFragment.newInstance(username, folderId)
            supportFragmentManager.beginTransaction()
                    .add(R.id.deviations_container, contentFragment)
                    .commit()
        } else {
            contentFragment = supportFragmentManager.findFragmentById(R.id.deviations_container) as FolderDeviationListFragment
        }
    }

    private fun initToolbar(username: String?, folderName: String) {
        val toolbar = setActionBar(R.id.toolbar) {
            title = folderName
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
