package io.plastique.deviations.viewer

import android.Manifest
import android.content.Context
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.ViewGroup
import androidx.core.app.ShareCompat
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import com.github.technoir42.android.extensions.setActionBar
import com.github.technoir42.rxjava2.extensions.pairwiseWithPrevious
import io.plastique.core.BaseActivity
import io.plastique.core.content.ContentStateController
import io.plastique.core.dialogs.ProgressDialogController
import io.plastique.core.image.ImageLoader
import io.plastique.core.mvvm.viewModel
import io.plastique.core.navigation.Route
import io.plastique.core.navigation.activityRoute
import io.plastique.core.navigation.navigationContext
import io.plastique.core.snackbar.SnackbarController
import io.plastique.deviations.DeviationsActivityComponent
import io.plastique.deviations.R
import io.plastique.deviations.databinding.ActivityDeviationViewerBinding
import io.plastique.deviations.viewer.DeviationViewerEvent.CopyLinkClickEvent
import io.plastique.deviations.viewer.DeviationViewerEvent.DownloadOriginalClickEvent
import io.plastique.deviations.viewer.DeviationViewerEvent.RetryClickEvent
import io.plastique.deviations.viewer.DeviationViewerEvent.SetFavoriteEvent
import io.plastique.deviations.viewer.DeviationViewerEvent.SnackbarShownEvent
import io.plastique.inject.getComponent
import io.plastique.util.Animations
import io.plastique.util.ByteCountFormatter
import io.plastique.util.SystemUiController
import io.reactivex.android.schedulers.AndroidSchedulers
import permissions.dispatcher.NeedsPermission
import permissions.dispatcher.RuntimePermissions

@RuntimePermissions
class DeviationViewerActivity : BaseActivity() {
    private val imageLoader = ImageLoader.from(this)
    private val viewModel: DeviationViewerViewModel by viewModel()

    private lateinit var binding: ActivityDeviationViewerBinding
    private lateinit var contentStateController: ContentStateController
    private lateinit var progressDialogController: ProgressDialogController
    private lateinit var snackbarController: SnackbarController
    private val systemUiController = SystemUiController(this)
    private var contentView: DeviationContentView? = null
    private var menuState: MenuState? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(false)
        binding = ActivityDeviationViewerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setActionBar(binding.toolbar) {
            setDisplayHomeAsUpEnabled(true)
        }
        viewModel.navigator.attach(navigationContext)

        systemUiController.isVisible = true
        systemUiController.setVisibilityChangeListener { visible ->
            if (visible) {
                Animations.fadeIn(binding.appbar, Animations.DURATION_SHORT)
                Animations.fadeIn(binding.infoPanel, Animations.DURATION_SHORT)
            } else {
                Animations.fadeOut(binding.appbar, Animations.DURATION_SHORT)
                Animations.fadeOut(binding.infoPanel, Animations.DURATION_SHORT)
            }
        }

        binding.infoPanel.apply {
            onAuthorClick = { user -> viewModel.navigator.openUserProfile(user) }
            onCommentsClick = { threadId -> viewModel.navigator.openComments(threadId) }
            onFavoriteClick = { _, isFavorite -> viewModel.dispatch(SetFavoriteEvent(!isFavorite)) }
            onInfoClick = { deviationId -> viewModel.navigator.openDeviationInfo(deviationId) }
        }

        binding.content.setOnApplyWindowInsetsListener { _, insets ->
            binding.infoPanel.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                bottomMargin = insets.systemWindowInsetBottom
            }
            insets
        }

        binding.empty.onButtonClick = { viewModel.dispatch(RetryClickEvent) }

        contentStateController = ContentStateController(this, binding.content, binding.progress, binding.empty)
        progressDialogController = ProgressDialogController(this, supportFragmentManager)
        snackbarController = SnackbarController(binding.root)
        snackbarController.onSnackbarShown = { viewModel.dispatch(SnackbarShownEvent) }

        val deviationId = intent.getStringExtra(EXTRA_DEVIATION_ID)!!
        viewModel.init(deviationId)
        viewModel.state
            .pairwiseWithPrevious()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { renderState(it.first, it.second) }
            .disposeOnDestroy()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.activity_deviation_viewer, menu)
        menuState?.let { menu.update(it) }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        onRequestPermissionsResult(requestCode, grantResults)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.deviations_viewer_action_download -> {
                downloadOriginalWithPermissionCheck()
                true
            }
            R.id.deviations_viewer_action_copy_link -> {
                viewModel.dispatch(CopyLinkClickEvent)
                true
            }
            R.id.deviations_viewer_action_send_link -> {
                sendLink(menuState!!.deviationUrl)
                true
            }
            R.id.deviations_viewer_action_open_in_browser -> {
                viewModel.navigator.openUrl(menuState!!.deviationUrl)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun renderState(state: DeviationViewerViewState, prevState: DeviationViewerViewState?) {
        menuState = state.menuState

        contentStateController.state = state.contentState
        binding.empty.state = state.emptyState

        if (state.content != null && state.content != prevState?.content) {
            renderContent(state.content)
        }

        if (state.infoViewState != null && state.infoViewState != prevState?.infoViewState) {
            binding.infoPanel.render(state.infoViewState, imageLoader)
            binding.infoPanel.isVisible = true
        }

        if (state.menuState != null && state.menuState != prevState?.menuState) {
            setHasOptionsMenu(true)
            optionsMenu?.update(state.menuState)
        }

        progressDialogController.isShown = state.showProgressDialog
        state.snackbarState?.let(snackbarController::showSnackbar)
    }

    private fun renderContent(content: DeviationContent) {
        val contentView = contentView ?: run {
            createContentView(imageLoader, binding.contentStub, content).apply {
                contentView = this
                onTapListener = { systemUiController.toggleVisibility() }
            }
        }
        contentView.render(content)
    }

    @NeedsPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    fun downloadOriginal() {
        viewModel.dispatch(DownloadOriginalClickEvent)
    }

    private fun sendLink(deviationUrl: String) {
        ShareCompat.IntentBuilder.from(this)
            .setType("text/plain")
            .setText(deviationUrl)
            .startChooser()
    }

    private fun Menu.update(menuState: MenuState) {
        findItem(R.id.deviations_viewer_action_download).apply {
            title = getString(R.string.deviations_viewer_action_download, ByteCountFormatter.format(menuState.downloadFileSize))
            isVisible = menuState.showDownload
        }
    }

    override fun injectDependencies() {
        getComponent<DeviationsActivityComponent>().inject(this)
    }

    companion object {
        private const val EXTRA_DEVIATION_ID = "deviation_id"

        fun route(context: Context, deviationId: String): Route = activityRoute<DeviationViewerActivity>(context) {
            putExtra(EXTRA_DEVIATION_ID, deviationId)
        }
    }
}
