package io.plastique.feed.settings

import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.technoir42.android.extensions.disableChangeAnimations
import com.github.technoir42.android.extensions.getCallback
import com.github.technoir42.kotlin.extensions.plus
import com.github.technoir42.rxjava2.extensions.pairwiseWithPrevious
import io.plastique.core.BaseBottomSheetDialogFragment
import io.plastique.core.DisposableContainer
import io.plastique.core.DisposableContainerImpl
import io.plastique.core.content.ContentState
import io.plastique.core.content.ContentStateController
import io.plastique.core.content.EmptyView
import io.plastique.core.lists.ListUpdateData
import io.plastique.core.lists.calculateDiff
import io.plastique.core.mvvm.viewModel
import io.plastique.core.navigation.Route
import io.plastique.core.navigation.dialogRoute
import io.plastique.feed.R
import io.plastique.feed.settings.FeedSettingsEvent.RetryClickEvent
import io.plastique.feed.settings.FeedSettingsEvent.SetEnabledEvent
import io.plastique.inject.BaseActivityComponent
import io.plastique.inject.BaseFragmentComponent
import io.plastique.inject.getComponent
import io.reactivex.android.schedulers.AndroidSchedulers

class FeedSettingsFragment : BaseBottomSheetDialogFragment(R.layout.fragment_feed_settings),
    BaseFragmentComponent.Holder,
    DisposableContainer by DisposableContainerImpl() {

    private val viewModel: FeedSettingsViewModel by viewModel()

    private lateinit var optionsView: RecyclerView
    private lateinit var emptyView: EmptyView
    private lateinit var optionsAdapter: OptionsAdapter
    private lateinit var contentStateController: ContentStateController

    private var changedSettings: FeedSettings? = null
    private var listener: OnFeedSettingsChangedListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = getCallback()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        optionsAdapter = OptionsAdapter(onOptionCheckedChanged = { key, checked ->
            viewModel.dispatch(SetEnabledEvent(key, checked))
        })

        optionsView = view.findViewById(R.id.options)
        optionsView.adapter = optionsAdapter
        optionsView.layoutManager = LinearLayoutManager(context)
        optionsView.disableChangeAnimations()

        emptyView = view.findViewById(android.R.id.empty)
        emptyView.onButtonClick = { viewModel.dispatch(RetryClickEvent) }

        contentStateController = ContentStateController(this, R.id.options, android.R.id.progress, android.R.id.empty)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel.state
            .pairwiseWithPrevious()
            .map { it + calculateDiff(it.second?.items, it.first.items) }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { renderState(it.first, it.third) }
            .disposeOnDestroy()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        disposeAll()
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        changedSettings
            ?.takeIf { it.include.isNotEmpty() }
            ?.let { listener?.onFeedSettingsChanged(it) }
    }

    private fun renderState(state: FeedSettingsViewState, listUpdateData: ListUpdateData<OptionItem>) {
        when (state) {
            FeedSettingsViewState.Loading -> {
                contentStateController.state = ContentState.Loading
            }

            is FeedSettingsViewState.Content -> {
                changedSettings = state.changedSettings
                contentStateController.state = ContentState.Content
                listUpdateData.applyTo(optionsAdapter)
            }

            is FeedSettingsViewState.Empty -> {
                contentStateController.state = ContentState.Empty
                emptyView.state = state.emptyState
            }
        }
    }

    private val FeedSettingsViewState.items: List<OptionItem>
        get() = if (this is FeedSettingsViewState.Content) items else emptyList()

    override val fragmentComponent: BaseFragmentComponent by lazy(LazyThreadSafetyMode.NONE) {
        requireActivity().getComponent<BaseActivityComponent>().createFragmentComponent()
    }

    override fun getDefaultViewModelProviderFactory(): ViewModelProvider.Factory = fragmentComponent.viewModelFactory()

    companion object {
        fun route(tag: String): Route = dialogRoute<FeedSettingsFragment>(tag)
    }
}

interface OnFeedSettingsChangedListener {
    fun onFeedSettingsChanged(settings: FeedSettings)
}
