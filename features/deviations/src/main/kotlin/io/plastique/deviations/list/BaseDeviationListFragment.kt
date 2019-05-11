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
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.material.behavior.HideBottomViewOnScrollBehavior
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.sch.rxjava2.extensions.pairwiseWithPrevious
import io.plastique.core.MvvmFragment
import io.plastique.core.ScrollableToTop
import io.plastique.core.content.ContentState
import io.plastique.core.content.ContentStateController
import io.plastique.core.content.EmptyView
import io.plastique.core.dialogs.ProgressDialogController
import io.plastique.core.extensions.add
import io.plastique.core.extensions.findParentOfType
import io.plastique.core.extensions.getLayoutBehavior
import io.plastique.core.lists.EndlessScrollListener
import io.plastique.core.lists.GridParams
import io.plastique.core.lists.GridParamsCalculator
import io.plastique.core.lists.IndexedItem
import io.plastique.core.lists.ItemSizeCallback
import io.plastique.core.lists.ListItem
import io.plastique.core.lists.ListUpdateData
import io.plastique.core.lists.calculateDiff
import io.plastique.core.navigation.navigationContext
import io.plastique.core.snackbar.SnackbarController
import io.plastique.core.snackbar.SnackbarState
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
import io.plastique.glide.RecyclerViewPreloader
import io.plastique.util.Size
import io.reactivex.android.schedulers.AndroidSchedulers
import javax.inject.Inject

abstract class BaseDeviationListFragment<ParamsType : FetchParams> : MvvmFragment<DeviationListViewModel>(),
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
    private var tags: List<Tag> = emptyList()
    private val tagManager: TagManager? get() = (parentFragment as? TagManagerProvider)?.tagManager
    private lateinit var gridParams: GridParams
    private lateinit var state: DeviationListViewState

    private lateinit var preloaderFactory: DeviationsPreloaderFactory
    private var preloader: RecyclerViewPreloader<*>? = null
        set(value) {
            field?.let { deviationsView.removeOnScrollListener(it) }
            field = value
            value?.let { deviationsView.addOnScrollListener(it) }
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
        snackbarController = SnackbarController(refreshLayout)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val displayMetrics = DisplayMetrics()
        requireActivity().windowManager.defaultDisplay.getMetrics(displayMetrics)

        gridParams = GridParamsCalculator.calculateGridParams(
            width = displayMetrics.widthPixels,
            minItemWidth = resources.getDimensionPixelSize(R.dimen.deviations_list_min_cell_size),
            itemSpacing = resources.getDimensionPixelOffset(R.dimen.deviations_grid_spacing))

        adapter = DeviationsAdapter(
            context = requireContext(),
            glide = GlideApp.with(this),
            layoutModeProvider = { state.layoutMode },
            itemSizeCallback = DeviationsItemSizeCallback(gridParams),
            onDeviationClick = { deviationId -> navigator.openDeviation(navigationContext, deviationId) },
            onCommentsClick = { threadId -> navigator.openComments(navigationContext, threadId) },
            onFavoriteClick = { deviationId, favorite -> viewModel.dispatch(SetFavoriteEvent(deviationId, favorite)) },
            onShareClick = { shareObjectId -> navigator.openPostStatus(navigationContext, shareObjectId) })

        onScrollListener = EndlessScrollListener(Int.MAX_VALUE) { viewModel.dispatch(LoadMoreEvent) }
        deviationsView.addOnScrollListener(onScrollListener)
        deviationsView.adapter = adapter
        preloaderFactory = DeviationsPreloaderFactory(this, deviationsView, adapter)

        @Suppress("UNCHECKED_CAST")
        params = savedInstanceState?.getParcelable(STATE_PARAMS) ?: defaultParams

        viewModel.init(params)
        viewModel.state
            .pairwiseWithPrevious()
            .map { it.add(calculateDiff(it.second?.items, it.first.items)) }
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

        if (state.layoutMode != prevState?.layoutMode) {
            deviationsView.layoutManager = createLayoutManager(requireContext(), state.layoutMode)
            onScrollListener.loadMoreThreshold = calculateLoadMoreThreshold(state.layoutMode)
            preloader = preloaderFactory.createPreloader(state.layoutMode, gridParams)

            if (adapter.itemCount > 0) {
                deviationsView.scrollToPosition(0)
            }

            adapter.items = state.items
            adapter.notifyDataSetChanged()
        } else {
            listUpdateData.applyTo(adapter)
        }

        onScrollListener.isEnabled = state.isPagingEnabled
        refreshLayout.isRefreshing = state.isRefreshing
        progressDialogController.isShown = state.showProgressDialog

        if (state.tags != prevState?.tags && isResumed) {
            tagManager?.setTags(tags, true)
        }

        if (state.snackbarState !== SnackbarState.None && state.snackbarState != prevState?.snackbarState) {
            snackbarController.showSnackbar(state.snackbarState)
            viewModel.dispatch(SnackbarShownEvent)
        }
    }

    override fun scrollToTop() {
        val coordinatorLayout = deviationsView.findParentOfType(CoordinatorLayout::class.java)
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

    private class DeviationsItemSizeCallback(private val gridParams: GridParams) : ItemSizeCallback {
        override fun getColumnCount(item: IndexedItem): Int = when (item) {
            is DeviationItem -> gridParams.columnCount
            else -> throw IllegalArgumentException("Unexpected item ${item.javaClass}")
        }

        override fun getItemSize(item: IndexedItem): Size = when (item) {
            is DeviationItem -> gridParams.getItemSize(item.index)
            else -> throw IllegalArgumentException("Unexpected item ${item.javaClass}")
        }
    }

    companion object {
        private const val STATE_PARAMS = "params"
        private const val LOAD_MORE_THRESHOLD_GRID_ROWS = 3
        private const val LOAD_MORE_THRESHOLD_LIST_ITEMS = 5
    }
}
