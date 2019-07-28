package io.plastique.feed

import android.content.Context
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.github.technoir42.android.extensions.instantiate
import com.github.technoir42.kotlin.extensions.plus
import com.github.technoir42.rxjava2.extensions.pairwiseWithPrevious
import com.google.android.flexbox.FlexboxLayoutManager
import io.plastique.core.ExpandableToolbarLayout
import io.plastique.core.ScrollableToTop
import io.plastique.core.content.ContentStateController
import io.plastique.core.content.EmptyView
import io.plastique.core.content.ProgressViewController
import io.plastique.core.dialogs.ProgressDialogController
import io.plastique.core.lists.EndlessScrollListener
import io.plastique.core.lists.GridParamsCalculator
import io.plastique.core.lists.ListItem
import io.plastique.core.lists.ListUpdateData
import io.plastique.core.lists.SimpleGridItemSizeCallback
import io.plastique.core.lists.calculateDiff
import io.plastique.core.mvvm.MvvmFragment
import io.plastique.core.navigation.navigationContext
import io.plastique.core.snackbar.SnackbarController
import io.plastique.core.time.ElapsedTimeFormatter
import io.plastique.feed.FeedEvent.LoadMoreEvent
import io.plastique.feed.FeedEvent.RefreshEvent
import io.plastique.feed.FeedEvent.RetryClickEvent
import io.plastique.feed.FeedEvent.SetFavoriteEvent
import io.plastique.feed.FeedEvent.SetFeedSettingsEvent
import io.plastique.feed.FeedEvent.SnackbarShownEvent
import io.plastique.feed.settings.FeedSettings
import io.plastique.feed.settings.FeedSettingsFragment
import io.plastique.feed.settings.OnFeedSettingsChangedListener
import io.plastique.glide.GlideApp
import io.plastique.inject.getComponent
import io.plastique.main.MainPage
import io.reactivex.android.schedulers.AndroidSchedulers
import javax.inject.Inject

class FeedFragment : MvvmFragment<FeedViewModel>(FeedViewModel::class.java),
    MainPage,
    ScrollableToTop,
    OnFeedSettingsChangedListener {

    @Inject lateinit var elapsedTimeFormatter: ElapsedTimeFormatter
    @Inject lateinit var navigator: FeedNavigator

    private lateinit var feedView: RecyclerView
    private lateinit var emptyView: EmptyView
    private lateinit var refreshLayout: SwipeRefreshLayout
    private lateinit var adapter: FeedAdapter
    private lateinit var onScrollListener: EndlessScrollListener
    private lateinit var contentStateController: ContentStateController
    private lateinit var horizontalProgressViewController: ProgressViewController
    private lateinit var progressDialogController: ProgressDialogController
    private lateinit var snackbarController: SnackbarController

    private lateinit var state: FeedViewState

    init {
        setHasOptionsMenu(true)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        navigator.attach(navigationContext)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_feed, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val displayMetrics = DisplayMetrics()
        requireActivity().windowManager.defaultDisplay.getMetrics(displayMetrics)

        val deviationParams = GridParamsCalculator.calculateGridParams(
            width = displayMetrics.widthPixels,
            minItemWidth = resources.getDimensionPixelSize(R.dimen.deviations_list_min_cell_size),
            itemSpacing = resources.getDimensionPixelOffset(R.dimen.deviations_grid_spacing))

        adapter = FeedAdapter(
            glide = GlideApp.with(this),
            elapsedTimeFormatter = elapsedTimeFormatter,
            gridItemSizeCallback = SimpleGridItemSizeCallback(deviationParams),
            onCollectionFolderClick = { username, folderId, folderName -> navigator.openCollectionFolder(username, folderId, folderName) },
            onCommentsClick = { threadId -> navigator.openComments(threadId) },
            onDeviationClick = { deviationId -> navigator.openDeviation(deviationId) },
            onFavoriteClick = { deviationId, favorite -> viewModel.dispatch(SetFavoriteEvent(deviationId, favorite)) },
            onShareClick = { shareObjectId -> navigator.openPostStatus(shareObjectId) },
            onStatusClick = { statusId -> navigator.openStatus(statusId) },
            onUserClick = { user -> navigator.openUserProfile(user) })

        feedView = view.findViewById(R.id.feed)
        feedView.adapter = adapter
        feedView.layoutManager = FlexboxLayoutManager(requireContext())
        feedView.itemAnimator = DefaultItemAnimator().apply { supportsChangeAnimations = false }
        feedView.addItemDecoration(FeedItemDecoration(requireContext()))

        onScrollListener = EndlessScrollListener(LOAD_MORE_THRESHOLD) { viewModel.dispatch(LoadMoreEvent) }
        feedView.addOnScrollListener(onScrollListener)

        refreshLayout = view.findViewById(R.id.refresh)
        refreshLayout.setOnRefreshListener { viewModel.dispatch(RefreshEvent) }

        emptyView = view.findViewById(android.R.id.empty)
        emptyView.setOnButtonClickListener {
            if (!state.isSignedIn) {
                navigator.openLogin()
            } else {
                viewModel.dispatch(RetryClickEvent)
            }
        }

        contentStateController = ContentStateController(view, R.id.refresh, android.R.id.progress, android.R.id.empty)
        horizontalProgressViewController = ProgressViewController(view, R.id.progress_horizontal)
        progressDialogController = ProgressDialogController(requireContext(), childFragmentManager)
        snackbarController = SnackbarController(this, refreshLayout)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel.state
            .pairwiseWithPrevious()
            .map { it + calculateDiff(it.second?.listState?.items, it.first.listState.items) }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { renderState(it.first, it.third) }
            .disposeOnDestroy()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.fragment_feed, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.feed_action_settings -> {
            val fragment = childFragmentManager.fragmentFactory.instantiate<FeedSettingsFragment>(requireContext())
            fragment.show(childFragmentManager, DIALOG_FEED_SETTINGS)
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    override fun onFeedSettingsChanged(settings: FeedSettings) {
        viewModel.dispatch(SetFeedSettingsEvent(settings))
    }

    override fun getTitle(): Int = R.string.feed_title

    override fun createAppBarViews(parent: ExpandableToolbarLayout) {
    }

    private fun renderState(state: FeedViewState, listUpdateData: ListUpdateData<ListItem>) {
        this.state = state

        setHasOptionsMenu(state.isSignedIn)

        contentStateController.state = state.contentState
        emptyView.state = state.emptyState

        listUpdateData.applyTo(adapter)

        onScrollListener.isEnabled = state.listState.isPagingEnabled
        refreshLayout.isRefreshing = state.listState.isRefreshing
        horizontalProgressViewController.isVisible = state.isApplyingSettings
        progressDialogController.isShown = state.showProgressDialog

        if (state.snackbarState != null && snackbarController.showSnackbar(state.snackbarState)) {
            viewModel.dispatch(SnackbarShownEvent)
        }
    }

    override fun scrollToTop() {
        feedView.scrollToPosition(0)
    }

    override fun injectDependencies() {
        getComponent<FeedFragmentComponent>().inject(this)
    }

    companion object {
        private const val DIALOG_FEED_SETTINGS = "dialog.feed_settings"
        private const val LOAD_MORE_THRESHOLD = 4
    }
}
