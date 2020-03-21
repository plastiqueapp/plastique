package io.plastique.gallery.deviations

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.add
import com.github.technoir42.android.extensions.setActionBar
import com.github.technoir42.android.extensions.setSubtitleOnClickListener
import com.github.technoir42.android.extensions.setTitleOnClickListener
import io.plastique.core.BaseActivity
import io.plastique.core.ScrollableToTop
import io.plastique.core.navigation.Route
import io.plastique.core.navigation.activityRoute
import io.plastique.gallery.GalleryActivityComponent
import io.plastique.gallery.R
import io.plastique.gallery.databinding.ActivityGalleryFolderDeviationsBinding
import io.plastique.gallery.folders.GalleryFolderId
import io.plastique.inject.getComponent

class FolderDeviationListActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityGalleryFolderDeviationsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val folderId = intent.getParcelableExtra<GalleryFolderId>(EXTRA_FOLDER_ID)!!
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
        getComponent<GalleryActivityComponent>().inject(this)
    }

    companion object {
        private const val EXTRA_FOLDER_ID = "folder_id"
        private const val EXTRA_FOLDER_NAME = "folder_name"

        fun route(context: Context, folderId: GalleryFolderId, folderName: String): Route = activityRoute<FolderDeviationListActivity>(context) {
            putExtra(EXTRA_FOLDER_ID, folderId)
            putExtra(EXTRA_FOLDER_NAME, folderName)
        }
    }
}
