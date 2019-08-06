package io.plastique.deviations.viewer

import android.Manifest
import android.content.Context
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
import io.plastique.comments.CommentThreadId
import io.plastique.core.BaseActivity
import io.plastique.core.content.ContentStateController
import io.plastique.core.content.EmptyView
import io.plastique.core.dialogs.ProgressDialogController
import io.plastique.core.mvvm.viewModel
import io.plastique.core.navigation.Route
import io.plastique.core.navigation.activityRoute
import io.plastique.core.navigation.navigationContext
import io.plastique.core.snackbar.SnackbarController
import io.plastique.deviations.DeviationsActivityComponent
import io.plastique.deviations.DeviationsNavigator
import io.plastique.deviations.R
import io.plastique.deviations.viewer.DeviationViewerEvent.CopyLinkClickEvent
import io.plastique.deviations.viewer.DeviationViewerEvent.DownloadOriginalClickEvent
import io.plastique.deviations.viewer.DeviationViewerEvent.RetryClickEvent
import io.plastique.deviations.viewer.DeviationViewerEvent.SetFavoriteEvent
import io.plastique.deviations.viewer.DeviationViewerEvent.SnackbarShownEvent
import io.plastique.glide.GlideApp
import io.plastique.glide.GlideRequests
import io.plastique.inject.getComponent
import io.plastique.util.Animations
import io.plastique.util.ByteCountFormatter
import io.plastique.util.InstantAppHelper
import io.plastique.util.SystemUiController
import io.reactivex.android.schedulers.AndroidSchedulers
import permissions.dispatcher.NeedsPermission
import permissions.dispatcher.RuntimePermissions
import javax.inject.Inject

@RuntimePermissions
class DeviationViewerActivity : BaseActivity() {
    @Inject lateinit var instantAppHelper: InstantAppHelper

    private val glide: GlideRequests by lazy(LazyThreadSafetyMode.NONE) { GlideApp.with(this) }
    private val viewModel: DeviationViewerViewModel by viewModel()
    private val navigator: DeviationsNavigator get() = viewModel.navigator

    private lateinit var rootView: View
    private lateinit var appBar: AppBarLayout
    private lateinit var infoPanelView: InfoPanelView
    private lateinit var emptyView: EmptyView
    private lateinit var contentStateController: ContentStateController
    private lateinit var progressDialogController: ProgressDialogController
    private lateinit var snackbarController: SnackbarController
    private val systemUiController = SystemUiController(this)
    private var contentView: DeviationContentView? = null
    private var menuState: MenuState? = null

    private val deviationId: String
        get() = intent.getStringExtra(EXTRA_DEVIATION_ID)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_deviation_viewer)
        setHasOptionsMenu(false)
        setActionBar(R.id.toolbar) {
            setDisplayHomeAsUpEnabled(true)
        }
        navigator.attach(navigationContext)

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
        infoPanelView.setOnAuthorClickListener { author -> navigator.openUserProfile(author) }
        infoPanelView.setOnFavoriteClickListener { _, isChecked -> viewModel.dispatch(SetFavoriteEvent(!isChecked)) }
        infoPanelView.setOnCommentsClickListener { navigator.openComments(CommentThreadId.Deviation(deviationId)) }
        infoPanelView.setOnInfoClickListener { navigator.openDeviationInfo(deviationId) }

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
                navigator.openUrl(menuState!!.deviationUrl)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun renderState(state: DeviationViewerViewState, prevState: DeviationViewerViewState?) {
        menuState = state.menuState

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

        fun route(context: Context, deviationId: String): Route = activityRoute<DeviationViewerActivity>(context) {
            putExtra(EXTRA_DEVIATION_ID, deviationId)
        }
    }
}
