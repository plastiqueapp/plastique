package io.plastique.collections.deviations

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import com.github.technoir42.android.extensions.instantiate
import com.github.technoir42.android.extensions.setActionBar
import com.github.technoir42.android.extensions.setSubtitleOnClickListener
import com.github.technoir42.android.extensions.setTitleOnClickListener
import io.plastique.collections.CollectionsActivityComponent
import io.plastique.collections.R
import io.plastique.collections.folders.CollectionFolderId
import io.plastique.core.BaseActivity
import io.plastique.inject.getComponent

class FolderDeviationListActivity : BaseActivity() {
    private lateinit var contentFragment: FolderDeviationListFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_collection_folder_deviations)

        val folderId = intent.getParcelableExtra<CollectionFolderId>(EXTRA_FOLDER_ID)!!
        val folderName = intent.getStringExtra(EXTRA_FOLDER_NAME)!!

        initToolbar(folderId.username, folderName)

        if (savedInstanceState == null) {
            contentFragment = supportFragmentManager.fragmentFactory.instantiate(this, args = FolderDeviationListFragment.newArgs(folderId))
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

        val onClickListener = View.OnClickListener { contentFragment.scrollToTop() }
        toolbar.setTitleOnClickListener(onClickListener)
        toolbar.setSubtitleOnClickListener(onClickListener)
    }

    override fun injectDependencies() {
        getComponent<CollectionsActivityComponent>().inject(this)
    }

    companion object {
        private const val EXTRA_FOLDER_ID = "folder_id"
        private const val EXTRA_FOLDER_NAME = "folder_name"

        fun createIntent(context: Context, folderId: CollectionFolderId, folderName: String): Intent {
            return Intent(context, FolderDeviationListActivity::class.java).apply {
                putExtra(EXTRA_FOLDER_ID, folderId)
                putExtra(EXTRA_FOLDER_NAME, folderName)
            }
        }
    }
}
