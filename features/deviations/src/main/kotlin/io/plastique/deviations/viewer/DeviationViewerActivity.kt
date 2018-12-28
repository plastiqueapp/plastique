package io.plastique.deviations.viewer

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.app.ShareCompat
import androidx.core.view.doOnNextLayout
import androidx.core.view.isVisible
import com.github.chrisbanes.photoview.PhotoView
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.snackbar.Snackbar
import com.sch.rxjava2.extensions.pairwiseWithPrevious
import io.plastique.comments.CommentThreadId
import io.plastique.core.MvvmActivity
import io.plastique.core.content.ContentViewController
import io.plastique.core.content.EmptyView
import io.plastique.core.dialogs.ProgressDialogController
import io.plastique.core.extensions.getLayoutBehavior
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
import io.plastique.deviations.viewer.DeviationViewerViewState.MenuState
import io.plastique.glide.GlideApp
import io.plastique.glide.GlideRequest
import io.plastique.inject.getComponent
import io.plastique.util.Animations
import io.plastique.util.Clipboard
import io.plastique.util.Intents
import io.reactivex.android.schedulers.AndroidSchedulers
import permissions.dispatcher.NeedsPermission
import permissions.dispatcher.RuntimePermissions
import timber.log.Timber
import javax.inject.Inject
import kotlin.math.max

@RuntimePermissions
class DeviationViewerActivity : MvvmActivity<DeviationViewerViewModel>() {
    private lateinit var rootView: View
    private lateinit var appBar: AppBarLayout
    private lateinit var photoView: PhotoView
    private lateinit var infoView: View
    private lateinit var titleView: TextView
    private lateinit var authorView: TextView
    private lateinit var descriptionView: TextView
    private lateinit var contentViewController: ContentViewController
    private lateinit var progressDialogController: ProgressDialogController
    private lateinit var snackbarController: SnackbarController
    @Inject lateinit var clipboard: Clipboard
    @Inject lateinit var navigator: DeviationsNavigator

    private lateinit var state: DeviationViewerViewState
    private var titleOnAppBar: Boolean = false
    private var appbarBackgroundColor: Int = 0

    private val deviationId: String
        get() = intent.getStringExtra(EXTRA_DEVIATION_ID)

    private val bottomSheetCallback = object : BottomSheetBehavior.BottomSheetCallback() {
        override fun onStateChanged(bottomSheet: View, newState: Int) {
            Timber.d("onStateChanged(newState: %d)", newState)
        }

        override fun onSlide(bottomSheet: View, slideOffset: Float) {
            val intersection = max(0, appBar.bottom - bottomSheet.top)
            setTitleOnAppBar(intersection > 0)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_deviation_viewer)
        setHasOptionsMenu(false)
        setActionBar(R.id.toolbar) {
            setDisplayHomeAsUpEnabled(true)
        }

        rootView = findViewById(android.R.id.content)
        appBar = findViewById(R.id.appbar)
        photoView = findViewById(R.id.photo)
        infoView = findViewById(R.id.deviation_info)
        titleView = findViewById(R.id.deviation_title)
        authorView = findViewById(R.id.deviation_author)
        descriptionView = findViewById(R.id.deviation_description)
        descriptionView.movementMethod = LinkMovementMethod.getInstance()
        photoView.setOnPhotoTapListener { _, _, _ -> toggleUiVisibility() }

        contentViewController = ContentViewController(this, R.id.photo, android.R.id.progress, android.R.id.empty)
        progressDialogController = ProgressDialogController(supportFragmentManager)
        snackbarController = SnackbarController(rootView)

        authorView.setOnClickListener { navigator.openUserProfile(navigationContext, state.deviation!!.author) }

        val attrs = intArrayOf(R.attr.colorPrimary)
        val a = appBar.context.obtainStyledAttributes(attrs)
        appbarBackgroundColor = a.getColor(0, 0)
        a.recycle()

        initBottomSheet()

        val emptyView = findViewById<EmptyView>(android.R.id.empty)
        emptyView.setOnButtonClickListener { viewModel.dispatch(RetryClickEvent) }

        val viewCommentsButton = findViewById<TextView>(R.id.button_view_comments)
        viewCommentsButton.setOnClickListener { navigator.openComments(navigationContext, CommentThreadId.Deviation(deviationId)) }

        viewModel.init(deviationId)
        viewModel.state
                .pairwiseWithPrevious()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { renderState(it.first, it.second) }
                .disposeOnDestroy()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.deviation_viewer, menu)
        menu.update(state.menuState)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        onRequestPermissionsResult(requestCode, grantResults)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.deviations_viewer_action_favorite -> {
            viewModel.dispatch(SetFavoriteEvent(state.deviationId, !item.isChecked))
            true
        }
        R.id.deviations_viewer_action_download -> {
            downloadOriginalWithPermissionCheck()
            true
        }
        R.id.deviations_viewer_action_copy_link -> {
            copyLinkToClipboard()
            true
        }
        R.id.deviations_viewer_action_send_link -> {
            sendLink()
            true
        }
        R.id.deviations_viewer_action_open_in_browser -> {
            openInBrowser()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    private fun renderState(state: DeviationViewerViewState, prevState: DeviationViewerViewState?) {
        this.state = state
        setHasOptionsMenu(state.deviation != null)

        contentViewController.state = state.contentState

        if (state.deviation != null) {
            titleView.text = state.deviation.title
            authorView.text = state.deviation.author.name
            infoView.isVisible = true
        }

        if (state.deviation?.content?.url != prevState?.deviation?.content?.url) {
            val contentUrl = state.deviation!!.content!!.url
            val previewUrl = state.deviation.preview!!.url

            val glide = GlideApp.with(this)
            val thumbnailRequest = (state.deviation.thumbnails.asSequence().map { it.url } + sequenceOf(previewUrl))
                    .fold<String, GlideRequest<Drawable>?>(null) { previous, url ->
                        val current = glide.load(url).onlyRetrieveFromCache(true)
                        if (previous != null) {
                            current.thumbnail(previous)
                        } else {
                            current
                        }
                    }

            // TODO: Preserve zoom when higher-resolution image is loaded
            glide.load(contentUrl)
                    .thumbnail(thumbnailRequest!!)
                    .into(photoView)
        }

        if (state.menuState != prevState?.menuState) {
            optionsMenu?.update(state.menuState)
        }

        if (state.showProgressDialog != (prevState?.showProgressDialog == true)) {
            if (state.showProgressDialog) {
                progressDialogController.show()
            } else {
                progressDialogController.dismiss()
            }
        }

        if (state.snackbarState !== SnackbarState.None && state.snackbarState != prevState?.snackbarState) {
            snackbarController.showSnackbar(state.snackbarState)
            viewModel.dispatch(SnackbarShownEvent)
        }
    }

    @NeedsPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    fun downloadOriginal() {
        viewModel.dispatch(DownloadOriginalClickEvent)
    }

    private fun openInBrowser() {
        try {
            startActivity(Intents.openUrl(state.deviation!!.url))
        } catch (e: ActivityNotFoundException) {
            Snackbar.make(rootView, R.string.deviations_viewer_message_no_apps_to_open_url, Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun copyLinkToClipboard() {
        clipboard.setText(state.deviation!!.url)
        Snackbar.make(rootView, R.string.common_message_link_copied, Snackbar.LENGTH_SHORT).show()
    }

    private fun sendLink() {
        ShareCompat.IntentBuilder.from(this)
                .setType("text/plain")
                .setText(state.deviation!!.url)
                .startChooser()
    }

    private fun initBottomSheet() {
        val behavior = infoView.getLayoutBehavior<BottomSheetBehavior<*>>()
        behavior.setBottomSheetCallback(bottomSheetCallback)

        val titlePanel = findViewById<LinearLayout>(R.id.title_panel)
        titlePanel.doOnNextLayout {
            behavior.peekHeight = titlePanel.height
        }
    }

    private fun toggleUiVisibility() {
        if (appBar.isVisible) {
            Animations.fadeOut(appBar, Animations.DURATION_SHORT)
            Animations.fadeOut(infoView, Animations.DURATION_SHORT)
        } else {
            Animations.fadeIn(appBar, Animations.DURATION_SHORT)
            Animations.fadeIn(infoView, Animations.DURATION_SHORT)
        }
    }

    private fun setTitleOnAppBar(titleOnAppBar: Boolean) {
        if (this.titleOnAppBar != titleOnAppBar) {
            this.titleOnAppBar = titleOnAppBar
            if (titleOnAppBar) {
                title = state.deviation!!.title
                appBar.setBackgroundColor(appbarBackgroundColor)
            } else {
                title = null
                appBar.setBackgroundResource(R.drawable.gradient_vertical)
            }
        }
    }

    private fun Menu.update(menuState: MenuState) {
        findItem(R.id.deviations_viewer_action_download).apply {
            isVisible = menuState.showDownload
        }

        findItem(R.id.deviations_viewer_action_favorite).apply {
            isChecked = menuState.isFavoriteChecked
            isVisible = menuState.showFavorite
            setIcon(if (isChecked) R.drawable.ic_favorite_checked_24dp else R.drawable.ic_favorite_unchecked_24dp)
            setTitle(if (isChecked) R.string.deviations_viewer_action_remove_from_favorites else R.string.deviations_viewer_action_add_to_favorites)
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
