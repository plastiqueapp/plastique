package io.plastique.statuses.list

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.github.technoir42.kotlin.extensions.plus
import com.github.technoir42.rxjava2.extensions.pairwiseWithPrevious
import io.plastique.comments.CommentThreadId
import io.plastique.core.BaseFragment
import io.plastique.core.ScrollableToTop
import io.plastique.core.content.ContentStateController
import io.plastique.core.content.EmptyView
import io.plastique.core.lists.DividerItemDecoration
import io.plastique.core.lists.EndlessScrollListener
import io.plastique.core.lists.ListItem
import io.plastique.core.lists.ListUpdateData
import io.plastique.core.lists.calculateDiff
import io.plastique.core.lists.smartScrollToPosition
import io.plastique.core.mvvm.viewModel
import io.plastique.core.navigation.navigationContext
import io.plastique.core.snackbar.SnackbarController
import io.plastique.core.time.ElapsedTimeFormatter
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

class StatusListFragment : BaseFragment(), ScrollableToTop {
    @Inject lateinit var elapsedTimeFormatter: ElapsedTimeFormatter
    @Inject lateinit var navigator: StatusesNavigator

    private val viewModel: StatusListViewModel by viewModel()

    private lateinit var statusesView: RecyclerView
    private lateinit var refreshLayout: SwipeRefreshLayout
    private lateinit var emptyView: EmptyView
    private lateinit var statusesAdapter: StatusListAdapter
    private lateinit var onScrollListener: EndlessScrollListener
    private lateinit var contentStateController: ContentStateController
    private lateinit var snackbarController: SnackbarController

    override fun onAttach(context: Context) {
        super.onAttach(context)
        navigator.attach(navigationContext)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_status_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        statusesAdapter = StatusListAdapter(
            glide = GlideApp.with(this),
            elapsedTimeFormatter = elapsedTimeFormatter,
            onDeviationClick = { deviationId -> navigator.openDeviation(deviationId) },
            onStatusClick = { statusId -> navigator.openStatus(statusId) },
            onCommentsClick = { statusId -> navigator.openComments(CommentThreadId.Status(statusId)) },
            onShareClick = { shareObjectId -> navigator.openPostStatus(shareObjectId) })

        statusesView = view.findViewById(R.id.statuses)
        statusesView.adapter = statusesAdapter
        statusesView.layoutManager = LinearLayoutManager(requireContext())
        statusesView.itemAnimator = DefaultItemAnimator().apply { supportsChangeAnimations = false }
        statusesView.addItemDecoration(DividerItemDecoration.Builder(requireContext()).build())

        onScrollListener = EndlessScrollListener(LOAD_MORE_THRESHOLD) { viewModel.dispatch(LoadMoreEvent) }
        statusesView.addOnScrollListener(onScrollListener)

        refreshLayout = view.findViewById(R.id.refresh)
        refreshLayout.setOnRefreshListener { viewModel.dispatch(RefreshEvent) }

        emptyView = view.findViewById(android.R.id.empty)
        emptyView.setOnButtonClickListener { viewModel.dispatch(RetryClickEvent) }

        contentStateController = ContentStateController(view, R.id.refresh, android.R.id.progress, android.R.id.empty)
        snackbarController = SnackbarController(this, view)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val username = requireArguments().getString(ARG_USERNAME)!!
        viewModel.init(username)
        viewModel.state
            .pairwiseWithPrevious()
            .map { it + calculateDiff(it.second?.listState?.items, it.first.listState.items) }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { renderState(it.first, it.third) }
            .disposeOnDestroy()
    }

    private fun renderState(state: StatusListViewState, listUpdateData: ListUpdateData<ListItem>) {
        contentStateController.state = state.contentState
        emptyView.state = state.emptyState

        listUpdateData.applyTo(statusesAdapter)

        onScrollListener.isEnabled = state.listState.isPagingEnabled
        refreshLayout.isRefreshing = state.listState.isRefreshing

        if (state.snackbarState != null && snackbarController.showSnackbar(state.snackbarState)) {
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
