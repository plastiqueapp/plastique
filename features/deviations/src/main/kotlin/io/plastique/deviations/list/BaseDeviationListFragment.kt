package io.plastique.deviations.list

import android.content.Context
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.children
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.technoir42.android.extensions.disableChangeAnimations
import com.github.technoir42.android.extensions.findParentOfType
import com.github.technoir42.android.extensions.getLayoutBehavior
import com.github.technoir42.glide.preloader.ListPreloader
import com.github.technoir42.kotlin.extensions.plus
import com.github.technoir42.rxjava2.extensions.pairwiseWithPrevious
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.material.behavior.HideBottomViewOnScrollBehavior
import com.google.android.material.bottomnavigation.BottomNavigationView
import io.plastique.core.BaseFragment
import io.plastique.core.ScrollableToTop
import io.plastique.core.content.ContentStateController
import io.plastique.core.dialogs.ProgressDialogController
import io.plastique.core.image.ImageLoader
import io.plastique.core.lists.EndlessScrollListener
import io.plastique.core.lists.GridParams
import io.plastique.core.lists.GridParamsCalculator
import io.plastique.core.lists.ListItem
import io.plastique.core.lists.ListUpdateData
import io.plastique.core.lists.SimpleGridItemSizeCallback
import io.plastique.core.lists.calculateDiff
import io.plastique.core.mvvm.viewModel
import io.plastique.core.navigation.navigationContext
import io.plastique.core.snackbar.SnackbarController
import io.plastique.deviations.FetchParams
import io.plastique.deviations.R
import io.plastique.deviations.databinding.FragmentDeviationsBinding
import io.plastique.deviations.list.DeviationListEvent.LoadMoreEvent
import io.plastique.deviations.list.DeviationListEvent.ParamsChangedEvent
import io.plastique.deviations.list.DeviationListEvent.RefreshEvent
import io.plastique.deviations.list.DeviationListEvent.RetryClickEvent
import io.plastique.deviations.list.DeviationListEvent.SetFavoriteEvent
import io.plastique.deviations.list.DeviationListEvent.SnackbarShownEvent
import io.plastique.deviations.tags.Tag
import io.plastique.deviations.tags.TagManager
import io.plastique.deviations.tags.TagManagerProvider
import io.reactivex.android.schedulers.AndroidSchedulers

abstract class BaseDeviationListFragment<ParamsType : FetchParams> : BaseFragment(), ScrollableToTop {
    protected val viewModel: DeviationListViewModel by viewModel()

    private lateinit var binding: FragmentDeviationsBinding
    private lateinit var deviationListAdapter: DeviationListAdapter
    private lateinit var onScrollListener: EndlessScrollListener
    private lateinit var contentStateController: ContentStateController
    private lateinit var progressDialogController: ProgressDialogController
    private lateinit var snackbarController: SnackbarController

    protected lateinit var params: ParamsType
    protected abstract val defaultParams: ParamsType
    protected open val fixedLayoutMode: LayoutMode? = null
    private var tags: List<Tag> = emptyList()
    private val tagManager: TagManager? get() = (parentFragment as? TagManagerProvider)?.tagManager
    private lateinit var gridParams: GridParams
    private lateinit var layoutMode: LayoutMode

    private lateinit var preloaderFactory: DeviationsPreloaderFactory
    private var preloader: ListPreloader? = null
        set(value) {
            field?.detach()
            field = value
            value?.attach(binding.deviations)
        }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        viewModel.navigator.attach(navigationContext)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentDeviationsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val displayMetrics = DisplayMetrics()
        requireActivity().windowManager.defaultDisplay.getMetrics(displayMetrics)

        gridParams = GridParamsCalculator.calculateGridParams(
            width = displayMetrics.widthPixels,
            minItemWidth = resources.getDimensionPixelSize(R.dimen.deviations_list_min_cell_size),
            itemSpacing = resources.getDimensionPixelOffset(R.dimen.deviations_grid_spacing))

        val imageLoader = ImageLoader.from(this)
        deviationListAdapter = DeviationListAdapter(
            imageLoader = imageLoader,
            layoutModeProvider = { fixedLayoutMode ?: layoutMode },
            itemSizeCallback = SimpleGridItemSizeCallback(gridParams),
            onDeviationClick = { deviationId -> viewModel.navigator.openDeviation(deviationId) },
            onCommentsClick = { threadId -> viewModel.navigator.openComments(threadId) },
            onFavoriteClick = { deviationId, favorite -> viewModel.dispatch(SetFavoriteEvent(deviationId, favorite)) },
            onShareClick = { shareObjectId -> viewModel.navigator.openPostStatus(shareObjectId) })

        preloaderFactory = DeviationsPreloaderFactory(imageLoader, binding.deviations, deviationListAdapter)
        onScrollListener = EndlessScrollListener(Int.MAX_VALUE) { viewModel.dispatch(LoadMoreEvent) }

        binding.deviations.apply {
            adapter = deviationListAdapter
            addOnScrollListener(onScrollListener)
            disableChangeAnimations()
        }
        fixedLayoutMode?.let { initLayoutMode(it) }

        binding.empty.onButtonClick = { viewModel.dispatch(RetryClickEvent) }
        binding.refresh.setOnRefreshListener { viewModel.dispatch(RefreshEvent) }

        contentStateController = ContentStateController(this, binding.refresh, binding.progress, binding.empty)
        progressDialogController = ProgressDialogController(requireContext(), childFragmentManager)
        snackbarController = SnackbarController(this, binding.refresh)
        snackbarController.onSnackbarShown = { viewModel.dispatch(SnackbarShownEvent) }

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
        @Suppress("UNCHECKED_CAST")
        params = state.params as ParamsType
        layoutMode = state.layoutMode

        contentStateController.state = state.contentState
        binding.empty.state = state.emptyState

        if (fixedLayoutMode == null && state.layoutMode != prevState?.layoutMode) {
            initLayoutMode(state.layoutMode)

            if (deviationListAdapter.itemCount > 0) {
                binding.deviations.scrollToPosition(0)
            }

            deviationListAdapter.items = state.listState.items
            deviationListAdapter.notifyDataSetChanged()
        } else {
            listUpdateData.applyTo(deviationListAdapter)
        }

        onScrollListener.isEnabled = state.listState.isPagingEnabled
        binding.refresh.isRefreshing = state.listState.isRefreshing
        progressDialogController.isShown = state.showProgressDialog

        tags = state.tags
        if (isResumed) {
            tagManager?.setTags(state.tags, true)
        }

        state.snackbarState?.let(snackbarController::showSnackbar)
    }

    override fun scrollToTop() {
        val coordinatorLayout = binding.deviations.findParentOfType<CoordinatorLayout>()
        coordinatorLayout?.children?.forEach { child ->
            if (child is BottomNavigationView) {
                val behavior = child.getLayoutBehavior<HideBottomViewOnScrollBehavior<BottomNavigationView>>()
                behavior.slideUp(child)
                return@forEach
            }
        }

        binding.deviations.scrollToPosition(0)
    }

    open fun onTagClick(tag: Tag) {
    }

    protected fun updateParams(params: ParamsType) {
        viewModel.dispatch(ParamsChangedEvent(params))
    }

    private fun initTags() {
        tagManager?.setTags(tags, false)
        tagManager?.onTagClick = { onTagClick(it) }
    }

    private fun initLayoutMode(layoutMode: LayoutMode) {
        binding.deviations.layoutManager = createLayoutManager(binding.deviations.context, layoutMode)
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
