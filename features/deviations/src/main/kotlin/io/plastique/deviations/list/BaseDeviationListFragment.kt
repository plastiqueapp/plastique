package io.plastique.deviations.list

import android.content.Context
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.flexbox.FlexboxLayoutManager
import com.sch.rxjava2.extensions.pairwiseWithPrevious
import io.plastique.core.MvvmFragment
import io.plastique.core.ScrollableToTop
import io.plastique.core.content.ContentState
import io.plastique.core.content.ContentViewController
import io.plastique.core.content.EmptyView
import io.plastique.core.extensions.add
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
import io.plastique.deviations.list.DeviationListEvent.SnackbarShownEvent
import io.plastique.deviations.tags.OnTagClickListener
import io.plastique.deviations.tags.Tag
import io.plastique.deviations.tags.TagManager
import io.plastique.deviations.tags.TagManagerProvider
import io.plastique.util.Size
import io.reactivex.android.schedulers.AndroidSchedulers
import javax.inject.Inject

abstract class BaseDeviationListFragment<ParamsType : FetchParams> : MvvmFragment<DeviationListViewModel>(),
        OnTagClickListener,
        ScrollableToTop {

    private lateinit var deviationsView: RecyclerView
    private lateinit var refreshLayout: SwipeRefreshLayout
    private lateinit var emptyView: EmptyView

    private lateinit var contentViewController: ContentViewController
    private lateinit var snackbarController: SnackbarController
    private lateinit var adapter: DeviationsAdapter
    private lateinit var onScrollListener: EndlessScrollListener

    protected lateinit var params: ParamsType
    protected abstract val defaultParams: ParamsType
    private var activityCreated: Boolean = false
    private var visibleToUser: Boolean = false
    private var tags: List<Tag> = emptyList()
    private val tagManager: TagManager? get() = (parentFragment as? TagManagerProvider)?.tagManager
    private lateinit var gridParams: GridParams
    private lateinit var state: DeviationListViewState
    @Inject lateinit var navigator: DeviationsNavigator

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_deviations, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        deviationsView = view.findViewById(R.id.deviations)
        refreshLayout = view.findViewById(R.id.refresh)
        refreshLayout.setOnRefreshListener { viewModel.dispatch(RefreshEvent) }

        emptyView = view.findViewById(android.R.id.empty)
        emptyView.setOnButtonClickListener(View.OnClickListener { viewModel.dispatch(RetryClickEvent) })

        contentViewController = ContentViewController(view, R.id.refresh, android.R.id.progress, android.R.id.empty)
        snackbarController = SnackbarController(refreshLayout)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        activityCreated = true

        val displayMetrics = DisplayMetrics()
        requireActivity().windowManager.defaultDisplay.getMetrics(displayMetrics)

        gridParams = GridParamsCalculator.calculateGridParams(
                width = displayMetrics.widthPixels,
                minItemWidth = resources.getDimensionPixelSize(R.dimen.deviations_list_min_cell_size),
                itemSpacing = resources.getDimensionPixelOffset(R.dimen.deviations_grid_spacing))

        adapter = DeviationsAdapter(requireContext(),
                layoutModeProvider = { state.layoutMode },
                itemSizeCallback = object : ItemSizeCallback {
                    override fun getColumnCount(item: IndexedItem): Int = when (item) {
                        is DeviationItem -> gridParams.columnCount
                        else -> throw IllegalArgumentException("Unexpected item ${item.javaClass}")
                    }

                    override fun getItemSize(item: IndexedItem): Size = when (item) {
                        is DeviationItem -> gridParams.getItemSize(item.index)
                        else -> throw IllegalArgumentException("Unexpected item ${item.javaClass}")
                    }
                })

        adapter.onDeviationClickListener = { deviation -> navigator.openDeviation(navigationContext, deviation.id) }

        onScrollListener = EndlessScrollListener(Int.MAX_VALUE) { viewModel.dispatch(LoadMoreEvent) }
        deviationsView.addOnScrollListener(onScrollListener)
        deviationsView.itemAnimator = DefaultItemAnimator().apply { supportsChangeAnimations = false }
        deviationsView.adapter = adapter

        @Suppress("UNCHECKED_CAST")
        params = savedInstanceState?.getParcelable(STATE_PARAMS) ?: defaultParams

        if (visibleToUser) {
            initTags()
        }

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

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        visibleToUser = isVisibleToUser
        if (activityCreated && isVisibleToUser) {
            initTags()
        }
    }

    private fun renderState(state: DeviationListViewState, prevState: DeviationListViewState?, listUpdateData: ListUpdateData<ListItem>) {
        this.state = state
        tags = state.tags

        contentViewController.state = state.contentState
        if (state.contentState is ContentState.Empty) {
            emptyView.setState(state.contentState.emptyState)
        }

        if (state.layoutMode != prevState?.layoutMode) {
            deviationsView.layoutManager = createLayoutManager(requireContext(), state.layoutMode)
            onScrollListener.loadMoreThreshold = calculateLoadMoreThreshold(state.layoutMode)

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

        if (state.tags != prevState?.tags && visibleToUser) {
            tagManager?.setTags(tags, true)
        }

        if (state.snackbarState !== SnackbarState.None && state.snackbarState != prevState?.snackbarState) {
            snackbarController.showSnackbar(state.snackbarState)
            viewModel.dispatch(SnackbarShownEvent)
        }
    }

    override fun scrollToTop() {
        deviationsView.scrollToPosition(0)
    }

    override fun onTagClick(tag: Tag) {
    }

    protected fun setNewParams(params: ParamsType) {
        this.params = params
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
        LayoutMode.Flex -> gridParams.columnCount * 3
        LayoutMode.List -> 5
    }

    companion object {
        private const val STATE_PARAMS = "params"
    }
}
