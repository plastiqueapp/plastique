package io.plastique.collections

import android.content.Context
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
import com.github.technoir42.android.extensions.disableChangeAnimations
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
import io.plastique.collections.databinding.FragmentCollectionsBinding
import io.plastique.collections.folders.Folder
import io.plastique.core.BaseFragment
import io.plastique.core.ExpandableToolbarLayout
import io.plastique.core.ScrollableToTop
import io.plastique.core.content.ContentStateController
import io.plastique.core.dialogs.ConfirmationDialogFragment
import io.plastique.core.dialogs.InputDialogFragment
import io.plastique.core.dialogs.OnConfirmListener
import io.plastique.core.dialogs.OnInputDialogResultListener
import io.plastique.core.dialogs.ProgressDialogController
import io.plastique.core.image.ImageLoader
import io.plastique.core.image.TransformType
import io.plastique.core.lists.EndlessScrollListener
import io.plastique.core.lists.GridParams
import io.plastique.core.lists.GridParamsCalculator
import io.plastique.core.lists.IndexedItem
import io.plastique.core.lists.ItemSizeCallback
import io.plastique.core.lists.ListItem
import io.plastique.core.lists.ListUpdateData
import io.plastique.core.lists.calculateDiff
import io.plastique.core.mvvm.viewModel
import io.plastique.core.navigation.navigationContext
import io.plastique.core.snackbar.SnackbarController
import io.plastique.deviations.list.DeviationItem
import io.plastique.deviations.list.ImageDeviationItem
import io.plastique.deviations.list.ImageHelper
import io.plastique.inject.getComponent
import io.plastique.main.MainPage
import io.plastique.util.Size
import io.reactivex.android.schedulers.AndroidSchedulers

class CollectionsFragment : BaseFragment(),
    MainPage,
    ScrollableToTop,
    OnConfirmListener,
    OnInputDialogResultListener {

    private val viewModel: CollectionsViewModel by viewModel()
    private val navigator: CollectionsNavigator get() = viewModel.navigator

    private lateinit var binding: FragmentCollectionsBinding
    private lateinit var collectionsAdapter: CollectionsAdapter
    private lateinit var onScrollListener: EndlessScrollListener
    private lateinit var contentStateController: ContentStateController
    private lateinit var progressDialogController: ProgressDialogController
    private lateinit var snackbarController: SnackbarController

    override fun onAttach(context: Context) {
        super.onAttach(context)
        navigator.attach(navigationContext)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentCollectionsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
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

        val imageLoader = ImageLoader.from(this)
        collectionsAdapter = CollectionsAdapter(
            imageLoader = imageLoader,
            itemSizeCallback = CollectionsItemSizeCallback(folderGridParams, deviationGridParams),
            onFolderClick = { folderId, folderName -> navigator.openCollectionFolder(folderId, folderName) },
            onFolderLongClick = { folder, itemView ->
                showFolderPopupMenu(folder, itemView)
                true
            },
            onDeviationClick = { deviationId -> navigator.openDeviation(deviationId) })

        onScrollListener = EndlessScrollListener(LOAD_MORE_THRESHOLD_ROWS * deviationGridParams.columnCount) { viewModel.dispatch(LoadMoreEvent) }

        binding.collections.apply {
            adapter = collectionsAdapter
            layoutManager = FlexboxLayoutManager(context)
            addOnScrollListener(onScrollListener)
            disableChangeAnimations()
        }

        createPreloader(imageLoader, collectionsAdapter, folderGridParams, deviationGridParams)
            .subscribeToLifecycle(lifecycle)
            .attach(binding.collections)

        binding.empty.onButtonClick = { viewModel.dispatch(RetryClickEvent) }
        binding.refresh.setOnRefreshListener { viewModel.dispatch(RefreshEvent) }

        contentStateController = ContentStateController(this, binding.refresh, binding.progress, binding.empty)
        progressDialogController = ProgressDialogController(requireContext(), childFragmentManager)
        snackbarController = SnackbarController(this, binding.refresh)
        snackbarController.onActionClick = { actionData -> viewModel.dispatch(UndoDeleteFolderEvent(actionData as String)) }
        snackbarController.onSnackbarShown = { viewModel.dispatch(SnackbarShownEvent) }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val username = arguments?.getString(ARG_USERNAME)
        viewModel.init(username)
        viewModel.state
            .pairwiseWithPrevious()
            .map { it + calculateDiff(it.second?.listState?.items, it.first.listState.items) }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { renderState(it.first, it.third) }
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

    private fun renderState(state: CollectionsViewState, listUpdateData: ListUpdateData<ListItem>) {
        setHasOptionsMenu(state.showMenu)

        contentStateController.state = state.contentState
        binding.empty.state = state.emptyState
        binding.refresh.isRefreshing = state.listState.isRefreshing
        onScrollListener.isEnabled = state.listState.isPagingEnabled
        progressDialogController.isShown = state.showProgressDialog
        state.snackbarState?.let(snackbarController::showSnackbar)

        listUpdateData.applyTo(collectionsAdapter)
    }

    private fun showFolderPopupMenu(folder: Folder, itemView: View) {
        if (!folder.isDeletable) return

        val popup = PopupMenu(requireContext(), itemView)
        popup.inflate(R.menu.collections_folder_popup)
        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.collections_action_delete_folder -> {
                    if (folder.isNotEmpty) {
                        showDeleteFolderDialog(folder)
                    } else {
                        viewModel.dispatch(DeleteFolderEvent(folder.id.id, folder.name))
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
            putString(ARG_DIALOG_FOLDER_ID, folder.id.id)
            putString(ARG_DIALOG_FOLDER_NAME, folder.name)
        })
        dialog.show(childFragmentManager, DIALOG_DELETE_FOLDER)
    }

    private fun createPreloader(
        imageLoader: ImageLoader,
        adapter: CollectionsAdapter,
        folderGridParams: GridParams,
        deviationsGridParams: GridParams
    ): ListPreloader {
        val callback = ListPreloader.Callback { position, preloader ->
            when (val item = adapter.items[position]) {
                is FolderItem -> {
                    item.folder.thumbnailUrl?.let { thumbnailUrl ->
                        val itemSize = folderGridParams.getItemSize(item.index)
                        val request = imageLoader.load(thumbnailUrl)
                            .params {
                                transforms += TransformType.CenterCrop
                            }
                            .createPreloadRequest()
                        preloader.preload(request, itemSize.width, itemSize.height)
                    }
                }

                is ImageDeviationItem -> {
                    val itemSize = deviationsGridParams.getItemSize(item.index)
                    val image = ImageHelper.chooseThumbnail(item.thumbnails, itemSize.width)
                    val request = imageLoader.load(image.url)
                        .params {
                            cacheSource = true
                        }
                        .createPreloadRequest()
                    preloader.preload(request, itemSize.width, itemSize.height)
                }
            }
        }
        return ListPreloader(imageLoader.glide, callback, MAX_PRELOAD_ROWS * deviationsGridParams.columnCount)
    }

    override fun getTitle(): Int = R.string.collections_title

    override fun createAppBarViews(parent: ExpandableToolbarLayout) {
    }

    override fun scrollToTop() {
        binding.collections.scrollToPosition(0)
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
        private const val LOAD_MORE_THRESHOLD_ROWS = 4
        private const val MAX_PRELOAD_ROWS = 4

        fun newArgs(username: String?): Bundle {
            return Bundle().apply {
                putString(ARG_USERNAME, username)
            }
        }
    }
}
