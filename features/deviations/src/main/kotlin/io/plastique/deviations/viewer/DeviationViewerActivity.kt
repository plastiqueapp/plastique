package io.plastique.deviations.viewer

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.doOnNextLayout
import androidx.core.view.isVisible
import com.github.chrisbanes.photoview.PhotoView
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.snackbar.Snackbar
import io.plastique.core.MvvmActivity
import io.plastique.core.content.ContentViewController
import io.plastique.core.content.EmptyView
import io.plastique.core.extensions.getLayoutBehavior
import io.plastique.core.extensions.setActionBar
import io.plastique.core.navigation.navigationContext
import io.plastique.core.snackbar.SnackbarController
import io.plastique.deviations.DeviationsActivityComponent
import io.plastique.deviations.DeviationsNavigator
import io.plastique.deviations.R
import io.plastique.deviations.viewer.DeviationViewerEvent.DownloadOriginalClickEvent
import io.plastique.deviations.viewer.DeviationViewerEvent.RetryClickEvent
import io.plastique.deviations.viewer.DeviationViewerEvent.SnackbarShownEvent
import io.plastique.glide.GlideApp
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
    private lateinit var snackbarController: SnackbarController
    @Inject lateinit var clipboard: Clipboard
    @Inject lateinit var navigator: DeviationsNavigator

    private var state: DeviationViewerViewState? = null
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
        snackbarController = SnackbarController(rootView)

        authorView.setOnClickListener { navigator.openUserProfile(navigationContext, state!!.deviation!!.author.name) }

        val attrs = intArrayOf(R.attr.colorPrimary)
        val a = appBar.context.obtainStyledAttributes(attrs)
        appbarBackgroundColor = a.getColor(0, 0)
        a.recycle()

        initBottomSheet()

        val emptyView = findViewById<EmptyView>(android.R.id.empty)
        emptyView.setOnButtonClickListener(View.OnClickListener { viewModel.dispatch(RetryClickEvent) })

        val viewCommentsButton = findViewById<TextView>(R.id.button_view_comments)
        viewCommentsButton.setOnClickListener { navigator.openCommentsForDeviation(navigationContext, deviationId) }

        viewModel.init(deviationId)
        observeState()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        state?.let { state ->
            menuInflater.inflate(R.menu.deviation_viewer, menu)

            menu.findItem(R.id.deviations_viewer_action_download).run {
                isVisible = state.menuState.showDownload
            }

            menu.findItem(R.id.deviations_viewer_action_favorite).run {
                isChecked = state.menuState.isFavoriteChecked
                isVisible = state.menuState.showFavorite
                setIcon(if (state.menuState.isFavoriteChecked) R.drawable.ic_favorite_checked_24dp else R.drawable.ic_favorite_unchecked_24dp)
                setTitle(if (state.menuState.isFavoriteChecked) R.string.deviations_viewer_action_remove_from_favorites else R.string.deviations_viewer_action_add_to_favorites)
            }
        }
        return true
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        onRequestPermissionsResult(requestCode, grantResults)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.deviations_viewer_action_favorite -> {
            true
        }
        R.id.deviations_viewer_action_download -> {
            downloadOriginalWithPermissionCheck()
            true
        }
        R.id.deviations_viewer_action_open_in_browser -> {
            openInBrowser()
            true
        }
        R.id.deviations_viewer_action_copy_link -> {
            copyLinkToClipboard()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    private fun observeState() {
        viewModel.state
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { state -> this.state = state }
                .disposeOnDestroy()

        viewModel.state
                .map { state -> state.contentState }
                .distinctUntilChanged()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { contentState -> contentViewController.state = contentState }
                .disposeOnDestroy()

        viewModel.state
                .filter { state -> state.deviation != null }
                .map { state -> state.deviation!! }
                .distinctUntilChanged()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { deviation ->
                    titleView.text = deviation.title
                    authorView.text = deviation.author.name
                    infoView.visibility = View.VISIBLE

                    GlideApp.with(this)
                            .load(deviation.content!!.url)
                            .into(photoView)
                }
                .disposeOnDestroy()

        viewModel.state
                .distinctUntilChanged { state -> state.snackbarState }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { state ->
                    snackbarController.showSnackbar(state.snackbarState)
                    viewModel.dispatch(SnackbarShownEvent)
                }
                .disposeOnDestroy()

        viewModel.state
                .distinctUntilChanged { state -> state.menuState }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { invalidateOptionsMenu() }
                .disposeOnDestroy()
    }

    @NeedsPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    fun downloadOriginal() {
        viewModel.dispatch(DownloadOriginalClickEvent)
    }

    private fun openInBrowser() {
        try {
            startActivity(Intents.openUrl(state!!.deviation!!.url))
        } catch (e: ActivityNotFoundException) {
            Snackbar.make(rootView, R.string.deviations_viewer_message_no_apps_to_open_url, Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun copyLinkToClipboard() {
        clipboard.setText(state!!.deviation!!.url)
        Snackbar.make(rootView, R.string.common_message_link_copied, Snackbar.LENGTH_SHORT).show()
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
                title = state!!.deviation!!.title
                appBar.setBackgroundColor(appbarBackgroundColor)
            } else {
                title = null
                appBar.setBackgroundResource(R.drawable.gradient_vertical)
            }
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
