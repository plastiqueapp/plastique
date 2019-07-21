package io.plastique.deviations.list

import android.content.Context
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.children
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.github.technoir42.android.extensions.findParentOfType
import com.github.technoir42.android.extensions.getLayoutBehavior
import com.github.technoir42.glide.preloader.ListPreloader
import com.github.technoir42.kotlin.extensions.plus
import com.github.technoir42.rxjava2.extensions.pairwiseWithPrevious
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.material.behavior.HideBottomViewOnScrollBehavior
import com.google.android.material.bottomnavigation.BottomNavigationView
import io.plastique.core.ScrollableToTop
import io.plastique.core.content.ContentState
import io.plastique.core.content.ContentStateController
import io.plastique.core.content.EmptyView
import io.plastique.core.dialogs.ProgressDialogController
import io.plastique.core.lists.EndlessScrollListener
import io.plastique.core.lists.GridParams
import io.plastique.core.lists.GridParamsCalculator
import io.plastique.core.lists.ListItem
import io.plastique.core.lists.ListUpdateData
import io.plastique.core.lists.SimpleGridItemSizeCallback
import io.plastique.core.lists.calculateDiff
import io.plastique.core.mvvm.MvvmFragment
import io.plastique.core.navigation.navigationContext
import io.plastique.core.snackbar.SnackbarController
import io.plastique.deviations.DeviationsNavigator
import io.plastique.deviations.FetchParams
import io.plastique.deviations.R
import io.plastique.deviations.list.DeviationListEvent.LoadMoreEvent
import io.plastique.deviations.list.DeviationListEvent.ParamsChangedEvent
import io.plastique.deviations.list.DeviationListEvent.RefreshEvent
import io.plastique.deviations.list.DeviationListEvent.RetryClickEvent
import io.plastique.deviations.list.DeviationListEvent.SetFavoriteEvent
import io.plastique.deviations.list.DeviationListEvent.SnackbarShownEvent
import io.plastique.deviations.tags.OnTagClickListener
import io.plastique.deviations.tags.Tag
import io.plastique.deviations.tags.TagManager
import io.plastique.deviations.tags.TagManagerProvider
import io.plastique.glide.GlideApp
import io.reactivex.android.schedulers.AndroidSchedulers
import javax.inject.Inject

abstract class BaseDeviationListFragment<ParamsType : FetchParams> : MvvmFragment<DeviationListViewModel>(DeviationListViewModel::class.java),
    OnTagClickListener,
    ScrollableToTop {

    @Inject lateinit var navigator: DeviationsNavigator

    private lateinit var deviationsView: RecyclerView
    private lateinit var refreshLayout: SwipeRefreshLayout
    private lateinit var emptyView: EmptyView

    private lateinit var contentStateController: ContentStateController
    private lateinit var progressDialogController: ProgressDialogController
    private lateinit var snackbarController: SnackbarController
    private lateinit var adapter: DeviationsAdapter
    private lateinit var onScrollListener: EndlessScrollListener

    protected lateinit var params: ParamsType
    protected abstract val defaultParams: ParamsType
    protected open val fixedLayoutMode: LayoutMode? = null
    private var tags: List<Tag> = emptyList()
    private val tagManager: TagManager? get() = (parentFragment as? TagManagerProvider)?.tagManager
    private lateinit var gridParams: GridParams
    private lateinit var state: DeviationListViewState

    private lateinit var preloaderFactory: DeviationsPreloaderFactory
    private var preloader: ListPreloader? = null
        set(value) {
            field?.detach()
            field = value
            value?.attach(deviationsView)
        }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_deviations, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        deviationsView = view.findViewById(R.id.deviations)
        deviationsView.itemAnimator = DefaultItemAnimator().apply { supportsChangeAnimations = false }

        refreshLayout = view.findViewById(R.id.refresh)
        refreshLayout.setOnRefreshListener { viewModel.dispatch(RefreshEvent) }

        emptyView = view.findViewById(android.R.id.empty)
        emptyView.setOnButtonClickListener { viewModel.dispatch(RetryClickEvent) }

        contentStateController = ContentStateController(view, R.id.refresh, android.R.id.progress, android.R.id.empty)
        progressDialogController = ProgressDialogController(requireContext(), childFragmentManager)
        snackbarController = SnackbarController(this, refreshLayout)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val displayMetrics = DisplayMetrics()
        requireActivity().windowManager.defaultDisplay.getMetrics(displayMetrics)

        gridParams = GridParamsCalculator.calculateGridParams(
            width = displayMetrics.widthPixels,
            minItemWidth = resources.getDimensionPixelSize(R.dimen.deviations_list_min_cell_size),
            itemSpacing = resources.getDimensionPixelOffset(R.dimen.deviations_grid_spacing))

        val glide = GlideApp.with(this)
        adapter = DeviationsAdapter(
            glide = glide,
            layoutModeProvider = { fixedLayoutMode ?: state.layoutMode },
            itemSizeCallback = SimpleGridItemSizeCallback(gridParams),
            onDeviationClick = { deviationId -> navigator.openDeviation(navigationContext, deviationId) },
            onCommentsClick = { threadId -> navigator.openComments(navigationContext, threadId) },
            onFavoriteClick = { deviationId, favorite -> viewModel.dispatch(SetFavoriteEvent(deviationId, favorite)) },
            onShareClick = { shareObjectId -> navigator.openPostStatus(navigationContext, shareObjectId) })

        preloaderFactory = DeviationsPreloaderFactory(glide, deviationsView, adapter)
        onScrollListener = EndlessScrollListener(Int.MAX_VALUE) { viewModel.dispatch(LoadMoreEvent) }
        deviationsView.addOnScrollListener(onScrollListener)
        deviationsView.adapter = adapter
        fixedLayoutMode?.let { initLayoutMode(it) }

        @Suppress("UNCHECKED_CAST")
        params = savedInstanceState?.getParcelable(STATE_PARAMS) ?: defaultParams

        viewModel.init(params)
        viewModel.state
            .pairwiseWithPrevious()
            .map { it + calculateDiff(it.second?.listState?.items, it.first.listState.items) }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { renderState(it.first, it.second, it.third) }
            .disposeOnDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable(STATE_PARAMS, params)
    }

    override fun onResume() {
        super.onResume()
        initTags()
    }

    private fun renderState(state: DeviationListViewState, prevState: DeviationListViewState?, listUpdateData: ListUpdateData<ListItem>) {
        this.state = state
        @Suppress("UNCHECKED_CAST")
        params = state.params as ParamsType
        tags = state.tags

        contentStateController.state = state.contentState
        if (state.contentState is ContentState.Empty) {
            emptyView.state = state.contentState.emptyState
        }

        if (fixedLayoutMode == null && state.layoutMode != prevState?.layoutMode) {
            initLayoutMode(state.layoutMode)

            if (adapter.itemCount > 0) {
                deviationsView.scrollToPosition(0)
            }

            adapter.items = state.listState.items
            adapter.notifyDataSetChanged()
        } else {
            listUpdateData.applyTo(adapter)
        }

        onScrollListener.isEnabled = state.listState.isPagingEnabled
        refreshLayout.isRefreshing = state.listState.isRefreshing
        progressDialogController.isShown = state.showProgressDialog

        if (state.tags != prevState?.tags && isResumed) {
            tagManager?.setTags(tags, true)
        }

        if (state.snackbarState != null && state.snackbarState != prevState?.snackbarState) {
            snackbarController.showSnackbar(state.snackbarState)
            viewModel.dispatch(SnackbarShownEvent)
        }
    }

    override fun scrollToTop() {
        val coordinatorLayout = deviationsView.findParentOfType<CoordinatorLayout>()
        coordinatorLayout?.children?.forEach { child ->
            if (child is BottomNavigationView) {
                val behavior = child.getLayoutBehavior<HideBottomViewOnScrollBehavior<BottomNavigationView>>()
                behavior.slideUp(child)
                return@forEach
            }
        }

        deviationsView.scrollToPosition(0)
    }

    override fun onTagClick(tag: Tag) {
    }

    protected fun updateParams(params: ParamsType) {
        viewModel.dispatch(ParamsChangedEvent(params))
    }

    private fun initTags() {
        tagManager?.setTags(tags, false)
        tagManager?.onTagClickListener = this
    }

    private fun initLayoutMode(layoutMode: LayoutMode) {
        deviationsView.layoutManager = createLayoutManager(requireContext(), layoutMode)
        onScrollListener.loadMoreThreshold = calculateLoadMoreThreshold(layoutMode)
        preloader = preloaderFactory.createPreloader(layoutMode, gridParams).subscribeToLifecycle(lifecycle)
    }

    private fun createLayoutManager(context: Context, layoutMode: LayoutMode): RecyclerView.LayoutManager = when (layoutMode) {
        LayoutMode.Grid,
        LayoutMode.Flex -> FlexboxLayoutManager(context)
        LayoutMode.List -> LinearLayoutManager(context)
    }

    private fun calculateLoadMoreThreshold(layoutMode: LayoutMode): Int = when (layoutMode) {
        LayoutMode.Grid,
        LayoutMode.Flex -> gridParams.columnCount * LOAD_MORE_THRESHOLD_GRID_ROWS
        LayoutMode.List -> LOAD_MORE_THRESHOLD_LIST_ITEMS
    }

    companion object {
        private const val STATE_PARAMS = "params"
        private const val LOAD_MORE_THRESHOLD_GRID_ROWS = 3
        private const val LOAD_MORE_THRESHOLD_LIST_ITEMS = 5
    }
}
