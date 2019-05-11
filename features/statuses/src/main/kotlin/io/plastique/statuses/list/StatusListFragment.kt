package io.plastique.statuses.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.sch.rxjava2.extensions.pairwiseWithPrevious
import io.plastique.comments.CommentThreadId
import io.plastique.core.MvvmFragment
import io.plastique.core.ScrollableToTop
import io.plastique.core.content.ContentState
import io.plastique.core.content.ContentStateController
import io.plastique.core.content.EmptyView
import io.plastique.core.extensions.add
import io.plastique.core.extensions.args
import io.plastique.core.extensions.smartScrollToPosition
import io.plastique.core.lists.DividerItemDecoration
import io.plastique.core.lists.EndlessScrollListener
import io.plastique.core.lists.ListItem
import io.plastique.core.lists.ListUpdateData
import io.plastique.core.lists.calculateDiff
import io.plastique.core.navigation.navigationContext
import io.plastique.core.snackbar.SnackbarController
import io.plastique.core.snackbar.SnackbarState
import io.plastique.glide.GlideApp
import io.plastique.inject.getComponent
import io.plastique.statuses.R
import io.plastique.statuses.StatusesFragmentComponent
import io.plastique.statuses.StatusesNavigator
import io.plastique.statuses.list.StatusListEvent.LoadMoreEvent
import io.plastique.statuses.list.StatusListEvent.RefreshEvent
import io.plastique.statuses.list.StatusListEvent.RetryClickEvent
import io.plastique.statuses.list.StatusListEvent.SnackbarShownEvent
import io.reactivex.android.schedulers.AndroidSchedulers
import javax.inject.Inject

class StatusListFragment : MvvmFragment<StatusListViewModel>(), ScrollableToTop {
    @Inject lateinit var navigator: StatusesNavigator

    private lateinit var statusesView: RecyclerView
    private lateinit var refreshLayout: SwipeRefreshLayout
    private lateinit var emptyView: EmptyView
    private lateinit var statusesAdapter: StatusListAdapter
    private lateinit var onScrollListener: EndlessScrollListener
    private lateinit var contentStateController: ContentStateController
    private lateinit var snackbarController: SnackbarController

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_status_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        statusesAdapter = StatusListAdapter(
                glide = GlideApp.with(this),
                onDeviationClick = { deviationId -> navigator.openDeviation(navigationContext, deviationId) },
                onStatusClick = { statusId -> navigator.openStatus(navigationContext, statusId) },
                onCommentsClick = { statusId -> navigator.openComments(navigationContext, CommentThreadId.Status(statusId)) },
                onShareClick = { shareObjectId -> navigator.openPostStatus(navigationContext, shareObjectId) })

        statusesView = view.findViewById(R.id.statuses)
        statusesView.adapter = statusesAdapter
        statusesView.layoutManager = LinearLayoutManager(requireContext())
        statusesView.itemAnimator = DefaultItemAnimator().apply { supportsChangeAnimations = false }
        statusesView.addItemDecoration(DividerItemDecoration.Builder(requireContext()).build())

        onScrollListener = EndlessScrollListener(LOAD_MORE_THRESHOLD, isEnabled = false) { viewModel.dispatch(LoadMoreEvent) }
        statusesView.addOnScrollListener(onScrollListener)

        refreshLayout = view.findViewById(R.id.refresh)
        refreshLayout.setOnRefreshListener { viewModel.dispatch(RefreshEvent) }

        emptyView = view.findViewById(android.R.id.empty)
        emptyView.setOnButtonClickListener { viewModel.dispatch(RetryClickEvent) }

        contentStateController = ContentStateController(view, R.id.refresh, android.R.id.progress, android.R.id.empty)
        snackbarController = SnackbarController(view)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val username = args.getString(ARG_USERNAME)!!
        viewModel.init(username)
        viewModel.state
                .pairwiseWithPrevious()
                .map { it.add(calculateDiff(it.second?.items, it.first.items)) }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { renderState(it.first, it.second, it.third) }
                .disposeOnDestroy()
    }

    private fun renderState(state: StatusListViewState, prevState: StatusListViewState?, listUpdateData: ListUpdateData<ListItem>) {
        contentStateController.state = state.contentState
        if (state.contentState is ContentState.Empty) {
            emptyView.state = state.contentState.emptyState
        }

        listUpdateData.applyTo(statusesAdapter)

        onScrollListener.isEnabled = state.isPagingEnabled
        refreshLayout.isRefreshing = state.isRefreshing

        if (state.snackbarState !== SnackbarState.None && state.snackbarState != prevState?.snackbarState) {
            snackbarController.showSnackbar(state.snackbarState)
            viewModel.dispatch(SnackbarShownEvent)
        }
    }

    override fun scrollToTop() {
        statusesView.smartScrollToPosition(0)
    }

    override fun injectDependencies() {
        getComponent<StatusesFragmentComponent>().inject(this)
    }

    companion object {
        private const val ARG_USERNAME = "username"
        private const val LOAD_MORE_THRESHOLD = 4

        fun newArgs(username: String): Bundle {
            return Bundle().apply {
                putString(ARG_USERNAME, username)
            }
        }
    }
}
