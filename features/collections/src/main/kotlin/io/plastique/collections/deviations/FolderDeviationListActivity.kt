package io.plastique.collections.deviations

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.add
import com.github.technoir42.android.extensions.setActionBar
import com.github.technoir42.android.extensions.setSubtitleOnClickListener
import com.github.technoir42.android.extensions.setTitleOnClickListener
import io.plastique.collections.CollectionsActivityComponent
import io.plastique.collections.R
import io.plastique.collections.databinding.ActivityCollectionFolderDeviationsBinding
import io.plastique.collections.folders.CollectionFolderId
import io.plastique.core.BaseActivity
import io.plastique.core.ScrollableToTop
import io.plastique.core.navigation.Route
import io.plastique.core.navigation.activityRoute
import io.plastique.inject.getComponent

class FolderDeviationListActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = ActivityCollectionFolderDeviationsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val folderId = intent.getParcelableExtra<CollectionFolderId>(EXTRA_FOLDER_ID)!!
        val folderName = intent.getStringExtra(EXTRA_FOLDER_NAME)!!
        initToolbar(binding.toolbar, folderId.owner, folderName)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .add<FolderDeviationListFragment>(R.id.deviations_container, args = FolderDeviationListFragment.newArgs(folderId))
                .commit()
        }
    }

    private fun initToolbar(toolbar: Toolbar, username: String?, folderName: String) {
        setActionBar(toolbar) {
            title = folderName
            subtitle = username
            setDisplayHomeAsUpEnabled(true)
        }

        val onClickListener = View.OnClickListener {
            val contentFragment = supportFragmentManager.findFragmentById(R.id.deviations_container) as ScrollableToTop
            contentFragment.scrollToTop()
        }
        toolbar.setTitleOnClickListener(onClickListener)
        toolbar.setSubtitleOnClickListener(onClickListener)
    }

    override fun injectDependencies() {
        getComponent<CollectionsActivityComponent>().inject(this)
    }

    companion object {
        private const val EXTRA_FOLDER_ID = "folder_id"
        private const val EXTRA_FOLDER_NAME = "folder_name"

        fun route(context: Context, folderId: CollectionFolderId, folderName: String): Route = activityRoute<FolderDeviationListActivity>(context) {
            putExtra(EXTRA_FOLDER_ID, folderId)
            putExtra(EXTRA_FOLDER_NAME, folderName)
        }
    }
}
