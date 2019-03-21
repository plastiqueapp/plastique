package io.plastique.deviations.viewer

import android.Manifest
import android.content.Context
import android.content.Intent
import android.graphics.Matrix
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.core.app.ShareCompat
import androidx.core.view.ViewCompat
import androidx.core.view.isVisible
import com.bumptech.glide.request.target.ImageViewTarget
import com.github.chrisbanes.photoview.PhotoView
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.snackbar.Snackbar
import com.sch.rxjava2.extensions.pairwiseWithPrevious
import io.plastique.comments.CommentThreadId
import io.plastique.core.MvvmActivity
import io.plastique.core.content.ContentState
import io.plastique.core.content.ContentStateController
import io.plastique.core.content.EmptyView
import io.plastique.core.dialogs.ProgressDialogController
import io.plastique.core.extensions.setActionBar
import io.plastique.core.navigation.navigationContext
import io.plastique.core.snackbar.SnackbarController
import io.plastique.core.snackbar.SnackbarState
import io.plastique.deviations.DeviationsActivityComponent
import io.plastique.deviations.DeviationsNavigator
import io.plastique.deviations.R
import io.plastique.deviations.viewer.DeviationViewerEvent.DownloadOriginalClickEvent
import io.plastique.deviations.viewer.DeviationViewerEvent.RetryClickEvent
import io.plastique.deviations.viewer.DeviationViewerEvent.SetFavoriteEvent
import io.plastique.deviations.viewer.DeviationViewerEvent.SnackbarShownEvent
import io.plastique.glide.GlideApp
import io.plastique.glide.GlideRequest
import io.plastique.inject.getComponent
import io.plastique.util.Animations
import io.plastique.util.Clipboard
import io.plastique.util.SystemUiController
import io.reactivex.android.schedulers.AndroidSchedulers
import permissions.dispatcher.NeedsPermission
import permissions.dispatcher.RuntimePermissions
import javax.inject.Inject
import kotlin.math.ln

@RuntimePermissions
class DeviationViewerActivity : MvvmActivity<DeviationViewerViewModel>() {
    @Inject lateinit var clipboard: Clipboard
    @Inject lateinit var navigator: DeviationsNavigator

    private lateinit var rootView: View
    private lateinit var appBar: AppBarLayout
    private lateinit var imageView: PhotoView
    private lateinit var infoPanelView: InfoPanelView
    private lateinit var emptyView: EmptyView
    private lateinit var contentStateController: ContentStateController
    private lateinit var progressDialogController: ProgressDialogController
    private lateinit var snackbarController: SnackbarController
    private val systemUiController = SystemUiController(this)
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

        imageView = findViewById(R.id.deviation_image)
        imageView.setOnPhotoTapListener { _, _, _ -> systemUiController.toggleVisibility() }
        ViewCompat.setOnApplyWindowInsetsListener(imageView) { _, insets -> insets }

        infoPanelView = findViewById(R.id.info_panel)
        infoPanelView.setOnAuthorClickListener { author -> navigator.openUserProfile(navigationContext, author) }
        infoPanelView.setOnFavoriteClickListener { _, isChecked ->
            if (lastState.isSignedIn) {
                viewModel.dispatch(SetFavoriteEvent(deviationId, !isChecked))
            } else {
                navigator.openLogin(navigationContext)
            }
        }
        infoPanelView.setOnCommentsClickListener { navigator.openComments(navigationContext, CommentThreadId.Deviation(deviationId)) }
        infoPanelView.setOnInfoClickListener { navigator.openDeviationInfo(navigationContext, deviationId) }

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
        if (state.contentState is ContentState.Empty) {
            emptyView.setState(state.contentState.emptyState)
        }

        if (state.content != prevState?.content) {
            when (state.content) {
                is DeviationContent.Image -> {
                    loadImage(state.content.url, state.content.thumbnailUrls)
                }

                is DeviationContent.Literature -> {
                    // TODO
                }
            }
        }

        if (state.infoViewState != null && state.infoViewState != prevState?.infoViewState) {
            infoPanelView.render(state.infoViewState, GlideApp.with(this))
            infoPanelView.isVisible = true
        }

        if (state.menuState != null && state.menuState != prevState?.menuState) {
            setHasOptionsMenu(true)
            optionsMenu?.update(state.menuState)
        }

        progressDialogController.isShown = state.showProgressDialog

        if (state.snackbarState !== SnackbarState.None && state.snackbarState != prevState?.snackbarState) {
            snackbarController.showSnackbar(state.snackbarState)
            viewModel.dispatch(SnackbarShownEvent)
        }
    }

    private fun loadImage(url: String, thumbnailUrls: List<String>) {
        val glide = GlideApp.with(this)

        // Build a chain of thumbnail requests with higher resolution thumbnails having a higher priority
        val thumbnailRequest = thumbnailUrls.asSequence()
                .fold<String, GlideRequest<Drawable>?>(null) { previous, thumbnailUrl ->
                    val current = glide.load(thumbnailUrl).onlyRetrieveFromCache(true)
                    if (previous != null) {
                        current.thumbnail(previous)
                    } else {
                        current
                    }
                }

        glide.load(url)
                .thumbnail(thumbnailRequest!!)
                .into(object : ImageViewTarget<Drawable>(imageView) {
                    override fun setResource(resource: Drawable?) {
                        if (resource != null) {
                            require(view is PhotoView)

                            // Preserve current transformation matrix in case full resolution image was loaded after a thumbnail
                            val matrix = Matrix()
                            view.getSuppMatrix(matrix)
                            view.setImageDrawable(resource)
                            view.setSuppMatrix(matrix)
                        } else {
                            view.setImageDrawable(resource)
                        }
                    }
                })
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
            title = getString(R.string.deviations_viewer_action_download, humanReadableByteCount(menuState.downloadFileSize))
            isVisible = menuState.showDownload
        }
    }

    override fun injectDependencies() {
        getComponent<DeviationsActivityComponent>().inject(this)
    }

    private fun humanReadableByteCount(bytes: Long, si: Boolean = false): String {
        val unit = if (si) 1000 else 1024
        if (bytes < unit) return "$bytes B"
        val exp = (ln(bytes.toDouble()) / ln(unit.toDouble())).toInt()
        val pre = (if (si) "kMGTPE" else "KMGTPE")[exp - 1] + if (si) "" else "i"
        return String.format("%.1f %sB", bytes / Math.pow(unit.toDouble(), exp.toDouble()), pre)
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
