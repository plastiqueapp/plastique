package io.plastique.feed.settings

import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.technoir42.android.extensions.getCallback
import com.github.technoir42.kotlin.extensions.plus
import com.github.technoir42.rxjava2.extensions.pairwiseWithPrevious
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import io.plastique.core.DisposableContainer
import io.plastique.core.DisposableContainerImpl
import io.plastique.core.content.ContentState
import io.plastique.core.content.ContentStateController
import io.plastique.core.content.EmptyView
import io.plastique.core.lists.ListUpdateData
import io.plastique.core.lists.calculateDiff
import io.plastique.feed.R
import io.plastique.feed.settings.FeedSettingsEvent.RetryClickEvent
import io.plastique.feed.settings.FeedSettingsEvent.SetEnabledEvent
import io.plastique.inject.BaseActivityComponent
import io.plastique.inject.BaseFragmentComponent
import io.plastique.inject.getComponent
import io.reactivex.android.schedulers.AndroidSchedulers

class FeedSettingsFragment :
    BottomSheetDialogFragment(),
    BaseFragmentComponent.Holder,
    DisposableContainer by DisposableContainerImpl() {

    private val viewModel: FeedSettingsViewModel by lazy(LazyThreadSafetyMode.NONE) {
        ViewModelProviders.of(this, fragmentComponent.viewModelFactory()).get(FeedSettingsViewModel::class.java)
    }

    private lateinit var optionsView: RecyclerView
    private lateinit var emptyView: EmptyView
    private lateinit var optionsAdapter: OptionsAdapter
    private lateinit var contentStateController: ContentStateController

    private lateinit var state: FeedSettingsViewState
    private var listener: OnFeedSettingsChangedListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = getCallback()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.subscribeToLifecycle(lifecycle)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_feed_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        optionsAdapter = OptionsAdapter()
        optionsAdapter.onOptionCheckedChangedListener = { key, checked ->
            viewModel.dispatch(SetEnabledEvent(key, checked))
        }

        optionsView = view.findViewById(R.id.options)
        optionsView.adapter = optionsAdapter
        optionsView.layoutManager = LinearLayoutManager(context)

        emptyView = view.findViewById(android.R.id.empty)
        emptyView.setOnButtonClickListener { viewModel.dispatch(RetryClickEvent) }

        contentStateController = ContentStateController(view, R.id.options, android.R.id.progress, android.R.id.empty)
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

        when (val state = this.state) {
            is FeedSettingsViewState.Content -> {
                val include = state.items.asSequence()
                    .filter { item -> item.isChecked != state.settings.include[item.key] }
                    .associateBy({ item -> item.key }, { item -> item.isChecked })

                if (include.isNotEmpty()) {
                    listener?.onFeedSettingsChanged(FeedSettings(include))
                }
            }
        }
    }

    private fun renderState(state: FeedSettingsViewState, listUpdateData: ListUpdateData<OptionItem>) {
        this.state = state

        when (state) {
            FeedSettingsViewState.Loading -> {
                contentStateController.state = ContentState.Loading
            }

            is FeedSettingsViewState.Content -> {
                contentStateController.state = ContentState.Content
                listUpdateData.applyTo(optionsAdapter)
            }

            is FeedSettingsViewState.Empty -> {
                contentStateController.state = ContentState.Empty
                emptyView.state = state.emptyState
            }
        }
    }

    override val fragmentComponent: BaseFragmentComponent by lazy(LazyThreadSafetyMode.NONE) {
        requireActivity().getComponent<BaseActivityComponent>().createFragmentComponent()
    }

    private val FeedSettingsViewState.items: List<OptionItem>
        get() = if (this is FeedSettingsViewState.Content) items else emptyList()
}

interface OnFeedSettingsChangedListener {
    fun onFeedSettingsChanged(settings: FeedSettings)
}
