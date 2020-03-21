package io.plastique.statuses.list

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.technoir42.android.extensions.disableChangeAnimations
import com.github.technoir42.kotlin.extensions.plus
import com.github.technoir42.rxjava2.extensions.pairwiseWithPrevious
import io.plastique.core.BaseFragment
import io.plastique.core.ScrollableToTop
import io.plastique.core.content.ContentStateController
import io.plastique.core.image.ImageLoader
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
import io.plastique.inject.getComponent
import io.plastique.statuses.StatusesFragmentComponent
import io.plastique.statuses.StatusesNavigator
import io.plastique.statuses.databinding.FragmentStatusListBinding
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

    private lateinit var binding: FragmentStatusListBinding
    private lateinit var statusesAdapter: StatusListAdapter
    private lateinit var onScrollListener: EndlessScrollListener
    private lateinit var contentStateController: ContentStateController
    private lateinit var snackbarController: SnackbarController

    override fun onAttach(context: Context) {
        super.onAttach(context)
        navigator.attach(navigationContext)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentStatusListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        statusesAdapter = StatusListAdapter(
            imageLoader = ImageLoader.from(this),
            elapsedTimeFormatter = elapsedTimeFormatter,
            onDeviationClick = { deviationId -> navigator.openDeviation(deviationId) },
            onStatusClick = { statusId -> navigator.openStatus(statusId) },
            onCommentsClick = { threadId -> navigator.openComments(threadId) },
            onShareClick = { shareObjectId -> navigator.openPostStatus(shareObjectId) })

        onScrollListener = EndlessScrollListener(LOAD_MORE_THRESHOLD) { viewModel.dispatch(LoadMoreEvent) }

        binding.statuses.apply {
            adapter = statusesAdapter
            layoutManager = LinearLayoutManager(context)
            addItemDecoration(DividerItemDecoration.Builder(context).build())
            addOnScrollListener(onScrollListener)
            disableChangeAnimations()
        }

        binding.empty.onButtonClick = { viewModel.dispatch(RetryClickEvent) }
        binding.refresh.setOnRefreshListener { viewModel.dispatch(RefreshEvent) }

        contentStateController = ContentStateController(this, binding.refresh, binding.progress, binding.empty)
        snackbarController = SnackbarController(this, view)
        snackbarController.onSnackbarShown = { viewModel.dispatch(SnackbarShownEvent) }
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
        binding.empty.state = state.emptyState
        binding.refresh.isRefreshing = state.listState.isRefreshing
        onScrollListener.isEnabled = state.listState.isPagingEnabled
        state.snackbarState?.let(snackbarController::showSnackbar)

        listUpdateData.applyTo(statusesAdapter)
    }

    override fun scrollToTop() {
        binding.statuses.smartScrollToPosition(0)
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
