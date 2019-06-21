package io.plastique.collections

import android.os.Bundle
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.core.text.HtmlCompat
import androidx.core.text.htmlEncode
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bumptech.glide.Priority
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.github.technoir42.android.extensions.instantiate
import com.github.technoir42.glide.preloader.ListPreloader
import com.github.technoir42.kotlin.extensions.plus
import com.github.technoir42.rxjava2.extensions.pairwiseWithPrevious
import com.google.android.flexbox.FlexboxLayoutManager
import io.plastique.collections.CollectionsEvent.CreateFolderEvent
import io.plastique.collections.CollectionsEvent.DeleteFolderEvent
import io.plastique.collections.CollectionsEvent.LoadMoreEvent
import io.plastique.collections.CollectionsEvent.RefreshEvent
import io.plastique.collections.CollectionsEvent.RetryClickEvent
import io.plastique.collections.CollectionsEvent.SnackbarShownEvent
import io.plastique.collections.CollectionsEvent.UndoDeleteFolderEvent
import io.plastique.core.ExpandableToolbarLayout
import io.plastique.core.ScrollableToTop
import io.plastique.core.content.ContentState
import io.plastique.core.content.ContentStateController
import io.plastique.core.content.EmptyView
import io.plastique.core.dialogs.ConfirmationDialogFragment
import io.plastique.core.dialogs.InputDialogFragment
import io.plastique.core.dialogs.OnConfirmListener
import io.plastique.core.dialogs.OnInputDialogResultListener
import io.plastique.core.dialogs.ProgressDialogController
import io.plastique.core.lists.EndlessScrollListener
import io.plastique.core.lists.GridParams
import io.plastique.core.lists.GridParamsCalculator
import io.plastique.core.lists.IndexedItem
import io.plastique.core.lists.ItemSizeCallback
import io.plastique.core.lists.ListItem
import io.plastique.core.lists.ListUpdateData
import io.plastique.core.lists.calculateDiff
import io.plastique.core.mvvm.MvvmFragment
import io.plastique.core.navigation.navigationContext
import io.plastique.core.snackbar.SnackbarController
import io.plastique.core.snackbar.SnackbarState
import io.plastique.deviations.list.DeviationItem
import io.plastique.deviations.list.ImageDeviationItem
import io.plastique.deviations.list.ImageHelper
import io.plastique.glide.GlideApp
import io.plastique.glide.GlideRequests
import io.plastique.inject.getComponent
import io.plastique.main.MainPage
import io.plastique.util.Size
import io.reactivex.android.schedulers.AndroidSchedulers
import javax.inject.Inject

class CollectionsFragment : MvvmFragment<CollectionsViewModel>(CollectionsViewModel::class.java),
    MainPage,
    ScrollableToTop,
    OnConfirmListener,
    OnInputDialogResultListener {

    @Inject lateinit var navigator: CollectionsNavigator

    private lateinit var refreshLayout: SwipeRefreshLayout
    private lateinit var emptyView: EmptyView
    private lateinit var adapter: CollectionsAdapter
    private lateinit var collectionsView: RecyclerView
    private lateinit var contentStateController: ContentStateController
    private lateinit var progressDialogController: ProgressDialogController
    private lateinit var snackbarController: SnackbarController
    private lateinit var onScrollListener: EndlessScrollListener

    private lateinit var state: CollectionsViewState

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_collections, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val displayMetrics = DisplayMetrics()
        requireActivity().windowManager.defaultDisplay.getMetrics(displayMetrics)

        val folderGridParams = GridParamsCalculator.calculateGridParams(
            width = displayMetrics.widthPixels,
            minItemWidth = resources.getDimensionPixelSize(R.dimen.collections_folder_min_width),
            itemSpacing = resources.getDimensionPixelOffset(R.dimen.collections_folder_spacing),
            heightToWidthRatio = 0.75f)

        val deviationGridParams = GridParamsCalculator.calculateGridParams(
            width = displayMetrics.widthPixels,
            minItemWidth = resources.getDimensionPixelSize(R.dimen.deviations_list_min_cell_size),
            itemSpacing = resources.getDimensionPixelOffset(R.dimen.deviations_grid_spacing))

        val glide = GlideApp.with(this)
        adapter = CollectionsAdapter(
            glide = glide,
            itemSizeCallback = CollectionsItemSizeCallback(folderGridParams, deviationGridParams),
            onFolderClick = { item -> navigator.openCollectionFolder(navigationContext, state.params.username, item.folder.id, item.folder.name) },
            onFolderLongClick = { item, itemView ->
                if (state.showMenu && item.folder.isDeletable) {
                    showFolderPopupMenu(item.folder, itemView)
                    true
                } else {
                    false
                }
            },
            onDeviationClick = { deviationId -> navigator.openDeviation(navigationContext, deviationId) })

        collectionsView = view.findViewById(R.id.collections)
        collectionsView.adapter = adapter
        collectionsView.layoutManager = FlexboxLayoutManager(context)
        collectionsView.itemAnimator = DefaultItemAnimator().apply { supportsChangeAnimations = false }

        onScrollListener = EndlessScrollListener(LOAD_MORE_THRESHOLD) { viewModel.dispatch(LoadMoreEvent) }
        collectionsView.addOnScrollListener(onScrollListener)

        createPreloader(glide, adapter, folderGridParams, deviationGridParams)
            .subscribeToLifecycle(lifecycle)
            .attach(collectionsView)

        refreshLayout = view.findViewById(R.id.refresh)
        refreshLayout.setOnRefreshListener { viewModel.dispatch(RefreshEvent) }

        emptyView = view.findViewById(android.R.id.empty)
        emptyView.setOnButtonClickListener {
            if (state.signInNeeded) {
                navigator.openLogin(navigationContext)
            } else {
                viewModel.dispatch(RetryClickEvent)
            }
        }

        contentStateController = ContentStateController(view, R.id.refresh, android.R.id.progress, android.R.id.empty)
        progressDialogController = ProgressDialogController(requireContext(), childFragmentManager)
        snackbarController = SnackbarController(this, refreshLayout)
        snackbarController.onActionClickListener = { actionData -> viewModel.dispatch(UndoDeleteFolderEvent(actionData as String)) }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val username = requireArguments().getString(ARG_USERNAME)
        viewModel.init(username)
        viewModel.state
            .pairwiseWithPrevious()
            .map { it + calculateDiff(it.second?.items, it.first.items) }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { renderState(it.first, it.second, it.third) }
            .disposeOnDestroy()
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

    override fun onConfirm(dialog: ConfirmationDialogFragment) {
        when (dialog.tag) {
            DIALOG_DELETE_FOLDER -> {
                val folderId = dialog.requireArguments().getString(ARG_DIALOG_FOLDER_ID)!!
                val folderName = dialog.requireArguments().getString(ARG_DIALOG_FOLDER_NAME)!!
                viewModel.dispatch(DeleteFolderEvent(folderId, folderName))
            }
        }
    }

    override fun onInputDialogResult(dialog: InputDialogFragment, text: String) {
        when (dialog.tag) {
            DIALOG_CREATE_FOLDER -> viewModel.dispatch(CreateFolderEvent(text.trim()))
        }
    }

    private fun renderState(state: CollectionsViewState, prevState: CollectionsViewState?, listUpdateData: ListUpdateData<ListItem>) {
        this.state = state
        setHasOptionsMenu(state.showMenu)

        contentStateController.state = state.contentState
        if (state.contentState is ContentState.Empty) {
            emptyView.state = state.contentState.emptyState
        }

        listUpdateData.applyTo(adapter)

        onScrollListener.isEnabled = state.isPagingEnabled
        refreshLayout.isRefreshing = state.isRefreshing
        progressDialogController.isShown = state.showProgressDialog

        if (state.snackbarState !== SnackbarState.None && state.snackbarState != prevState?.snackbarState) {
            snackbarController.showSnackbar(state.snackbarState)
            viewModel.dispatch(SnackbarShownEvent)
        }
    }

    private fun showFolderPopupMenu(folder: Folder, itemView: View) {
        val popup = PopupMenu(requireContext(), itemView)
        popup.inflate(R.menu.collections_folder_popup)
        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.collections_action_delete_folder -> {
                    if (folder.isNotEmpty) {
                        showDeleteFolderDialog(folder)
                    } else {
                        viewModel.dispatch(DeleteFolderEvent(folder.id, folder.name))
                    }
                    true
                }
                else -> false
            }
        }
        popup.show()
    }

    private fun showCreateFolderDialog() {
        val dialog = childFragmentManager.fragmentFactory.instantiate<InputDialogFragment>(requireContext(), args = InputDialogFragment.newArgs(
            title = R.string.collections_action_create_folder,
            hint = R.string.collections_folder_name_hint,
            positiveButton = R.string.collections_button_create,
            maxLength = FOLDER_NAME_MAX_LENGTH))
        dialog.show(childFragmentManager, DIALOG_CREATE_FOLDER)
    }

    private fun showDeleteFolderDialog(folder: Folder) {
        val dialog = childFragmentManager.fragmentFactory.instantiate<ConfirmationDialogFragment>(requireContext(), args = ConfirmationDialogFragment.newArgs(
            titleId = R.string.collections_dialog_delete_folder_title,
            message = HtmlCompat.fromHtml(getString(R.string.collections_dialog_delete_folder_message, folder.name.htmlEncode(),
                resources.getQuantityString(R.plurals.common_deviations, folder.size, folder.size)), 0),
            positiveButtonTextId = R.string.common_button_delete,
            negativeButtonTextInt = R.string.common_button_cancel
        ).apply {
            putString(ARG_DIALOG_FOLDER_ID, folder.id)
            putString(ARG_DIALOG_FOLDER_NAME, folder.name)
        })
        dialog.show(childFragmentManager, DIALOG_DELETE_FOLDER)
    }

    private fun createPreloader(
        glide: GlideRequests,
        adapter: CollectionsAdapter,
        folderGridParams: GridParams,
        deviationsGridParams: GridParams
    ): ListPreloader {
        val callback = ListPreloader.Callback { position, preloader ->
            when (val item = adapter.items[position]) {
                is FolderItem -> {
                    item.folder.thumbnailUrl?.let { thumbnailUrl ->
                        val itemSize = folderGridParams.getItemSize(item.index)
                        val request = glide.load(thumbnailUrl)
                            .centerCrop()
                            .priority(Priority.LOW)
                        preloader.preload(request, itemSize.width, itemSize.height)
                    }
                }

                is ImageDeviationItem -> {
                    val itemSize = deviationsGridParams.getItemSize(item.index)
                    val image = ImageHelper.chooseThumbnail(item.thumbnails, itemSize.width)
                    val request = glide.load(image.url)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .priority(Priority.LOW)
                    preloader.preload(request, itemSize.width, itemSize.height)
                }
            }
        }
        return ListPreloader(glide, callback, MAX_PRELOAD_ROWS * deviationsGridParams.columnCount)
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

    private class CollectionsItemSizeCallback(private val folderParams: GridParams, private val deviationParams: GridParams) : ItemSizeCallback {
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
    }

    companion object {
        private const val ARG_USERNAME = "username"
        private const val ARG_DIALOG_FOLDER_ID = "folder_id"
        private const val ARG_DIALOG_FOLDER_NAME = "folder_name"
        private const val DIALOG_CREATE_FOLDER = "dialog.create_folder"
        private const val DIALOG_DELETE_FOLDER = "dialog.delete_folder"
        private const val FOLDER_NAME_MAX_LENGTH = 50
        private const val LOAD_MORE_THRESHOLD = 4
        private const val MAX_PRELOAD_ROWS = 4

        fun newArgs(username: String? = null): Bundle {
            return Bundle().apply {
                putString(ARG_USERNAME, username)
            }
        }
    }
}
