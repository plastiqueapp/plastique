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
import com.github.technoir42.android.extensions.disableChangeAnimations
import com.github.technoir42.kotlin.extensions.plus
import com.github.technoir42.rxjava2.extensions.pairwiseWithPrevious
import com.google.android.flexbox.FlexboxLayoutManager
import io.plastique.core.BaseFragment
import io.plastique.core.ExpandableToolbarLayout
import io.plastique.core.ScrollableToTop
import io.plastique.core.content.ContentStateController
import io.plastique.core.content.ProgressViewController
import io.plastique.core.dialogs.ProgressDialogController
import io.plastique.core.image.ImageLoader
import io.plastique.core.lists.EndlessScrollListener
import io.plastique.core.lists.GridParamsCalculator
import io.plastique.core.lists.ListItem
import io.plastique.core.lists.ListUpdateData
import io.plastique.core.lists.SimpleGridItemSizeCallback
import io.plastique.core.lists.calculateDiff
import io.plastique.core.mvvm.viewModel
import io.plastique.core.navigation.navigationContext
import io.plastique.core.snackbar.SnackbarController
import io.plastique.core.time.ElapsedTimeFormatter
import io.plastique.feed.FeedEvent.LoadMoreEvent
import io.plastique.feed.FeedEvent.RefreshEvent
import io.plastique.feed.FeedEvent.RetryClickEvent
import io.plastique.feed.FeedEvent.SetFavoriteEvent
import io.plastique.feed.FeedEvent.SetFeedSettingsEvent
import io.plastique.feed.FeedEvent.SnackbarShownEvent
import io.plastique.feed.databinding.FragmentFeedBinding
import io.plastique.feed.settings.FeedSettings
import io.plastique.feed.settings.OnFeedSettingsChangedListener
import io.plastique.inject.getComponent
import io.plastique.main.MainPage
import io.reactivex.android.schedulers.AndroidSchedulers
import javax.inject.Inject

class FeedFragment : BaseFragment(),
    MainPage,
    ScrollableToTop,
    OnFeedSettingsChangedListener {

    @Inject lateinit var elapsedTimeFormatter: ElapsedTimeFormatter

    private val viewModel: FeedViewModel by viewModel()

    private lateinit var binding: FragmentFeedBinding
    private lateinit var feedAdapter: FeedAdapter
    private lateinit var onScrollListener: EndlessScrollListener
    private lateinit var contentStateController: ContentStateController
    private lateinit var horizontalProgressViewController: ProgressViewController
    private lateinit var progressDialogController: ProgressDialogController
    private lateinit var snackbarController: SnackbarController

    init {
        setHasOptionsMenu(true)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        viewModel.navigator.attach(navigationContext)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentFeedBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val displayMetrics = DisplayMetrics()
        requireActivity().windowManager.defaultDisplay.getMetrics(displayMetrics)

        val deviationParams = GridParamsCalculator.calculateGridParams(
            width = displayMetrics.widthPixels,
            minItemWidth = resources.getDimensionPixelSize(R.dimen.deviations_list_min_cell_size),
            itemSpacing = resources.getDimensionPixelOffset(R.dimen.deviations_grid_spacing))

        feedAdapter = FeedAdapter(
            imageLoader = ImageLoader.from(this),
            elapsedTimeFormatter = elapsedTimeFormatter,
            gridItemSizeCallback = SimpleGridItemSizeCallback(deviationParams),
            onCollectionFolderClick = { folderId, folderName -> viewModel.navigator.openCollectionFolder(folderId, folderName) },
            onCommentsClick = { threadId -> viewModel.navigator.openComments(threadId) },
            onDeviationClick = { deviationId -> viewModel.navigator.openDeviation(deviationId) },
            onFavoriteClick = { deviationId, favorite -> viewModel.dispatch(SetFavoriteEvent(deviationId, !favorite)) },
            onShareClick = { shareObjectId -> viewModel.navigator.openPostStatus(shareObjectId) },
            onStatusClick = { statusId -> viewModel.navigator.openStatus(statusId) },
            onUserClick = { user -> viewModel.navigator.openUserProfile(user) })

        onScrollListener = EndlessScrollListener(LOAD_MORE_THRESHOLD) { viewModel.dispatch(LoadMoreEvent) }

        binding.feed.apply {
            adapter = feedAdapter
            layoutManager = FlexboxLayoutManager(context)
            addItemDecoration(FeedItemDecoration(context))
            addOnScrollListener(onScrollListener)
            disableChangeAnimations()
        }

        binding.empty.onButtonClick = { viewModel.dispatch(RetryClickEvent) }
        binding.refresh.setOnRefreshListener { viewModel.dispatch(RefreshEvent) }

        contentStateController = ContentStateController(this, binding.refresh, binding.progress, binding.empty)
        horizontalProgressViewController = ProgressViewController(binding.progressHorizontal)
        progressDialogController = ProgressDialogController(requireContext(), childFragmentManager)
        snackbarController = SnackbarController(this, binding.refresh)
        snackbarController.onSnackbarShown = { viewModel.dispatch(SnackbarShownEvent) }

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
            viewModel.navigator.showFeedSettingsDialog(DIALOG_FEED_SETTINGS)
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
        setHasOptionsMenu(state.isSignedIn)

        contentStateController.state = state.contentState
        binding.empty.state = state.emptyState
        binding.refresh.isRefreshing = state.listState.isRefreshing
        onScrollListener.isEnabled = state.listState.isPagingEnabled
        horizontalProgressViewController.isVisible = state.isApplyingSettings
        progressDialogController.isShown = state.showProgressDialog
        state.snackbarState?.let(snackbarController::showSnackbar)

        listUpdateData.applyTo(feedAdapter)
    }

    override fun scrollToTop() {
        binding.feed.scrollToPosition(0)
    }

    override fun injectDependencies() {
        getComponent<FeedFragmentComponent>().inject(this)
    }

    companion object {
        private const val DIALOG_FEED_SETTINGS = "dialog.feed_settings"
        private const val LOAD_MORE_THRESHOLD = 4
    }
}
