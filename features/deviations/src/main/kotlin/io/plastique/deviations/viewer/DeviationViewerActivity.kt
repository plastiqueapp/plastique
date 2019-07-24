package io.plastique.deviations.viewer

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.ViewStub
import androidx.core.app.ShareCompat
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import com.github.technoir42.android.extensions.setActionBar
import com.github.technoir42.rxjava2.extensions.pairwiseWithPrevious
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.snackbar.Snackbar
import io.plastique.comments.CommentThreadId
import io.plastique.core.content.ContentStateController
import io.plastique.core.content.EmptyView
import io.plastique.core.dialogs.ProgressDialogController
import io.plastique.core.mvvm.MvvmActivity
import io.plastique.core.navigation.navigationContext
import io.plastique.core.snackbar.SnackbarController
import io.plastique.deviations.DeviationsActivityComponent
import io.plastique.deviations.DeviationsNavigator
import io.plastique.deviations.R
import io.plastique.deviations.viewer.DeviationViewerEvent.DownloadOriginalClickEvent
import io.plastique.deviations.viewer.DeviationViewerEvent.RetryClickEvent
import io.plastique.deviations.viewer.DeviationViewerEvent.SetFavoriteEvent
import io.plastique.deviations.viewer.DeviationViewerEvent.SnackbarShownEvent
import io.plastique.glide.GlideApp
import io.plastique.glide.GlideRequests
import io.plastique.inject.getComponent
import io.plastique.util.Animations
import io.plastique.util.ByteCountFormatter
import io.plastique.util.Clipboard
import io.plastique.util.InstantAppHelper
import io.plastique.util.SystemUiController
import io.reactivex.android.schedulers.AndroidSchedulers
import permissions.dispatcher.NeedsPermission
import permissions.dispatcher.RuntimePermissions
import javax.inject.Inject

@RuntimePermissions
class DeviationViewerActivity : MvvmActivity<DeviationViewerViewModel>(DeviationViewerViewModel::class.java) {
    @Inject lateinit var clipboard: Clipboard
    @Inject lateinit var navigator: DeviationsNavigator
    @Inject lateinit var instantAppHelper: InstantAppHelper

    private lateinit var rootView: View
    private lateinit var appBar: AppBarLayout
    private lateinit var infoPanelView: InfoPanelView
    private lateinit var emptyView: EmptyView
    private lateinit var contentStateController: ContentStateController
    private lateinit var progressDialogController: ProgressDialogController
    private lateinit var snackbarController: SnackbarController
    private val glide: GlideRequests by lazy(LazyThreadSafetyMode.NONE) { GlideApp.with(this) }
    private val systemUiController = SystemUiController(this)
    private var contentView: DeviationContentView? = null
    private lateinit var lastState: DeviationViewerViewState

    private val deviationId: String
        get() = intent.getStringExtra(EXTRA_DEVIATION_ID)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_deviation_viewer)
        setHasOptionsMenu(false)
        setActionBar(R.id.toolbar) {
            setDisplayHomeAsUpEnabled(true)
        }

        rootView = findViewById(R.id.root)
        appBar = findViewById(R.id.appbar)

        systemUiController.isVisible = true
        systemUiController.setVisibilityChangeListener { visible ->
            if (visible) {
                Animations.fadeIn(appBar, Animations.DURATION_SHORT)
                Animations.fadeIn(infoPanelView, Animations.DURATION_SHORT)
            } else {
                Animations.fadeOut(appBar, Animations.DURATION_SHORT)
                Animations.fadeOut(infoPanelView, Animations.DURATION_SHORT)
            }
        }

        infoPanelView = findViewById(R.id.info_panel)
        infoPanelView.setOnAuthorClickListener { author -> navigator.openUserProfile(navigationContext, author) }
        infoPanelView.setOnFavoriteClickListener { _, isChecked ->
            if (lastState.isSignedIn) {
                viewModel.dispatch(SetFavoriteEvent(!isChecked))
            } else {
                navigator.openLogin(navigationContext)
            }
        }
        infoPanelView.setOnCommentsClickListener { navigator.openComments(navigationContext, CommentThreadId.Deviation(deviationId)) }
        infoPanelView.setOnInfoClickListener { navigator.openDeviationInfo(navigationContext, deviationId) }

        val contentView = findViewById<ViewGroup>(R.id.content)
        contentView.setOnApplyWindowInsetsListener { _, insets ->
            infoPanelView.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                bottomMargin = insets.systemWindowInsetBottom
            }
            insets
        }

        contentStateController = ContentStateController(this, R.id.content, android.R.id.progress, android.R.id.empty)
        progressDialogController = ProgressDialogController(this, supportFragmentManager)
        snackbarController = SnackbarController(rootView)

        emptyView = findViewById(android.R.id.empty)
        emptyView.setOnButtonClickListener { viewModel.dispatch(RetryClickEvent) }

        viewModel.init(deviationId)
        viewModel.state
            .pairwiseWithPrevious()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { renderState(it.first, it.second) }
            .disposeOnDestroy()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.activity_deviation_viewer, menu)
        lastState.menuState?.let { menu.update(it) }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        onRequestPermissionsResult(requestCode, grantResults)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val deviationUrl = lastState.menuState!!.deviationUrl
        return when (item.itemId) {
            R.id.deviations_viewer_action_download -> {
                downloadOriginalWithPermissionCheck()
                true
            }
            R.id.deviations_viewer_action_copy_link -> {
                copyLinkToClipboard(deviationUrl)
                true
            }
            R.id.deviations_viewer_action_send_link -> {
                sendLink(deviationUrl)
                true
            }
            R.id.deviations_viewer_action_open_in_browser -> {
                navigator.openUrl(navigationContext, deviationUrl)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun renderState(state: DeviationViewerViewState, prevState: DeviationViewerViewState?) {
        lastState = state

        contentStateController.state = state.contentState
        emptyView.state = state.emptyState

        if (state.content != null && state.content != prevState?.content) {
            renderContent(state.content)
        }

        if (state.infoViewState != null && state.infoViewState != prevState?.infoViewState) {
            infoPanelView.render(state.infoViewState, glide)
            infoPanelView.isVisible = true
        }

        if (state.menuState != null && state.menuState != prevState?.menuState) {
            setHasOptionsMenu(true)
            optionsMenu?.update(state.menuState)
        }

        progressDialogController.isShown = state.showProgressDialog

        if (state.snackbarState != null && snackbarController.showSnackbar(state.snackbarState)) {
            viewModel.dispatch(SnackbarShownEvent)
        }
    }

    private fun renderContent(content: DeviationContent) {
        val contentView = contentView ?: run {
            val contentStub = findViewById<ViewStub>(R.id.deviation_content_stub)
            createContentView(glide, contentStub, content).apply {
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

    private fun copyLinkToClipboard(deviationUrl: String) {
        clipboard.setText(deviationUrl)
        Snackbar.make(rootView, R.string.common_message_link_copied, Snackbar.LENGTH_SHORT).show()
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
            isVisible = menuState.showDownload && !instantAppHelper.isInstantApp
        }
    }

    override fun injectDependencies() {
        getComponent<DeviationsActivityComponent>().inject(this)
    }

    companion object {
        private const val EXTRA_DEVIATION_ID = "deviation_id"

        fun createIntent(context: Context, deviationId: String): Intent {
            return Intent(context, DeviationViewerActivity::class.java).apply {
                putExtra(EXTRA_DEVIATION_ID, deviationId)
            }
        }
    }
}
