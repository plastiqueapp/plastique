package io.plastique.collections

import android.os.Bundle
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.material.snackbar.Snackbar
import io.plastique.collections.CollectionsEvent.CreateFolderEvent
import io.plastique.collections.CollectionsEvent.LoadMoreEvent
import io.plastique.collections.CollectionsEvent.RefreshEvent
import io.plastique.collections.CollectionsEvent.RetryClickEvent
import io.plastique.collections.CollectionsEvent.SnackbarShownEvent
import io.plastique.core.ExpandableToolbarLayout
import io.plastique.core.MvvmFragment
import io.plastique.core.ScrollableToTop
import io.plastique.core.content.ContentState
import io.plastique.core.content.ContentViewController
import io.plastique.core.content.EmptyView
import io.plastique.core.dialogs.InputDialogFragment
import io.plastique.core.dialogs.OnInputDialogResultListener
import io.plastique.core.extensions.args
import io.plastique.core.extensions.withArguments
import io.plastique.core.lists.EndlessScrollListener
import io.plastique.core.lists.GridParamsCalculator
import io.plastique.core.lists.IndexedItem
import io.plastique.core.lists.ItemSizeCallback
import io.plastique.core.lists.ListItem
import io.plastique.core.lists.ListItemDiffTransformer
import io.plastique.core.navigation.navigationContext
import io.plastique.deviations.list.DeviationItem
import io.plastique.inject.getComponent
import io.plastique.main.MainPage
import io.plastique.util.Size
import io.reactivex.android.schedulers.AndroidSchedulers
import javax.inject.Inject

class CollectionsFragment : MvvmFragment<CollectionsViewModel>(), MainPage, ScrollableToTop, OnInputDialogResultListener {
    private lateinit var refreshLayout: SwipeRefreshLayout
    private lateinit var emptyView: EmptyView
    private lateinit var adapter: CollectionsAdapter
    private lateinit var collectionsView: RecyclerView
    private lateinit var contentViewController: ContentViewController
    private lateinit var onScrollListener: EndlessScrollListener
    private lateinit var state: CollectionsViewState

    @Inject lateinit var navigator: CollectionsNavigator

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_collections, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val displayMetrics = DisplayMetrics()
        requireActivity().windowManager.defaultDisplay.getMetrics(displayMetrics)

        val folderParams = GridParamsCalculator.calculateGridParams(
                width = displayMetrics.widthPixels,
                minItemWidth = resources.getDimensionPixelSize(R.dimen.collections_folder_min_width),
                itemSpacing = resources.getDimensionPixelOffset(R.dimen.collections_folder_spacing),
                heightToWidthRatio = 0.75f)

        val deviationParams = GridParamsCalculator.calculateGridParams(
                width = displayMetrics.widthPixels,
                minItemWidth = resources.getDimensionPixelSize(R.dimen.deviations_list_min_cell_size),
                itemSpacing = resources.getDimensionPixelOffset(R.dimen.deviations_grid_spacing))

        adapter = CollectionsAdapter(requireContext(), object : ItemSizeCallback {
            override fun getColumnCount(item: IndexedItem): Int = when (item) {
                is FolderItem -> folderParams.columnCount
                is DeviationItem -> deviationParams.columnCount
                else -> throw IllegalArgumentException("Unexpected item ${item.javaClass}")
            }

            override fun getItemSize(item: IndexedItem): Size = when (item) {
                is FolderItem -> folderParams.getItemSize(item.index)
                is DeviationItem -> deviationParams.getItemSize(item.index)
                else -> throw IllegalArgumentException("Unexpected item ${item.javaClass}")
            }
        })

        adapter.onFolderClickListener = { item ->
            navigator.openCollectionFolder(navigationContext, CollectionFolderId(id = item.folder.id, username = state.params.username), item.folder.name)
        }
        adapter.onDeviationClickListener = { item -> navigator.openDeviation(navigationContext, item.deviation.id) }
        onScrollListener = EndlessScrollListener(4, enabled = false) { viewModel.dispatch(LoadMoreEvent) }

        collectionsView = view.findViewById(R.id.collections)
        collectionsView.layoutManager = FlexboxLayoutManager(context)
        collectionsView.adapter = adapter
        collectionsView.addOnScrollListener(onScrollListener)

        refreshLayout = view.findViewById(R.id.refresh)
        refreshLayout.setOnRefreshListener { viewModel.dispatch(RefreshEvent) }

        emptyView = view.findViewById(android.R.id.empty)
        emptyView.setOnButtonClickListener(View.OnClickListener {
            if (state.signInNeeded) {
                navigator.openLogin(navigationContext)
            } else {
                viewModel.dispatch(RetryClickEvent)
            }
        })
        contentViewController = ContentViewController(view, R.id.refresh, android.R.id.progress, android.R.id.empty)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val username = args.getString(ARG_USERNAME)
        viewModel.init(username)
        observeState()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.fragment_collections, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.collections_action_create_folder -> {
            showCreateFolderDialog()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    override fun onInputDialogResult(dialog: InputDialogFragment, text: String) {
        if (dialog.tag == DIALOG_CREATE_FOLDER) {
            viewModel.dispatch(CreateFolderEvent(text.trim()))
        }
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
                .subscribe { contentState ->
                    contentViewController.switchState(contentState)
                    if (contentState is ContentState.Empty) {
                        emptyView.setState(contentState.emptyState)
                    }
                }
                .disposeOnDestroy()

        @Suppress("RemoveExplicitTypeArguments")
        viewModel.state
                .map { state -> state.items }
                .compose(ListItemDiffTransformer<ListItem>())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { updateData -> updateData.applyTo(adapter) }
                .disposeOnDestroy()

        viewModel.state
                .distinctUntilChanged { state -> state.pagingEnabled }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { state -> onScrollListener.enabled = state.pagingEnabled }
                .disposeOnDestroy()

        viewModel.state
                .distinctUntilChanged { state -> state.refreshing }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { state -> refreshLayout.isRefreshing = state.refreshing }
                .disposeOnDestroy()

        viewModel.state
                .distinctUntilChanged { state -> state.snackbarMessage }
                .filter { state -> state.snackbarMessage != null }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { state ->
                    Snackbar.make(refreshLayout, state.snackbarMessage!!, Snackbar.LENGTH_LONG).show()
                    viewModel.dispatch(SnackbarShownEvent)
                }
                .disposeOnDestroy()

        viewModel.state
                .distinctUntilChanged { state -> state.showMenu }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { state -> setHasOptionsMenu(state.showMenu) }
                .disposeOnDestroy()
    }

    private fun showCreateFolderDialog() {
        val dialog = InputDialogFragment.newInstance(
                title = R.string.collections_action_create_folder,
                hint = R.string.collections_folder_name_hint,
                positiveButton = R.string.collections_button_create,
                maxLength = FOLDER_NAME_MAX_LENGTH)
        dialog.show(childFragmentManager, DIALOG_CREATE_FOLDER)
    }

    override fun getTitle(): Int = R.string.collections_title

    override fun createAppBarViews(parent: ExpandableToolbarLayout) {
    }

    override fun scrollToTop() {
        collectionsView.scrollToPosition(0)
    }

    override fun injectDependencies() {
        getComponent<CollectionsFragmentComponent>().inject(this)
    }

    companion object {
        private const val ARG_USERNAME = "username"
        private const val DIALOG_CREATE_FOLDER = "dialog.create_folder"
        private const val FOLDER_NAME_MAX_LENGTH = 50

        fun newInstance(username: String? = null): CollectionsFragment {
            return CollectionsFragment().withArguments {
                putString(ARG_USERNAME, username)
            }
        }
    }
}
