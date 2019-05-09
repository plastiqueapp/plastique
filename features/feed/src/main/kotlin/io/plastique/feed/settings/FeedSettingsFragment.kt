package io.plastique.feed.settings

import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.sch.rxjava2.extensions.pairwiseWithPrevious
import io.plastique.core.content.ContentState
import io.plastique.core.content.ContentStateController
import io.plastique.core.content.EmptyView
import io.plastique.core.extensions.add
import io.plastique.core.extensions.findCallback
import io.plastique.core.extensions.isRemovingSelfOrParent
import io.plastique.core.lists.ListUpdateData
import io.plastique.core.lists.calculateDiff
import io.plastique.feed.FeedFragmentComponent
import io.plastique.feed.R
import io.plastique.feed.settings.FeedSettingsEvent.RetryClickEvent
import io.plastique.feed.settings.FeedSettingsEvent.SetEnabledEvent
import io.plastique.inject.ActivityComponent
import io.plastique.inject.getComponent
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import javax.inject.Inject

class FeedSettingsFragment : BottomSheetDialogFragment() {
    private lateinit var optionsView: RecyclerView
    private lateinit var emptyView: EmptyView
    private lateinit var optionsAdapter: OptionsAdapter
    private lateinit var contentStateController: ContentStateController
    private lateinit var state: FeedSettingsViewState
    @Inject lateinit var viewModel: FeedSettingsViewModel
    private val disposables = CompositeDisposable()

    private var listener: OnFeedSettingsChangedListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = findCallback<OnFeedSettingsChangedListener>()

        (requireActivity().getComponent<ActivityComponent>().createFragmentComponent() as FeedFragmentComponent).inject(this)
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

        disposables += viewModel.state
                .pairwiseWithPrevious()
                .map { it.add(calculateDiff(it.second?.items, it.first.items)) }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { renderState(it.first, it.third) }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        disposables.dispose()
        if (requireActivity().isFinishing || isRemovingSelfOrParent) {
            viewModel.destroy()
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)

        val state = this.state
        if (state.settings != null) {
            val include = state.items.asSequence()
                    .filter { item -> item.isChecked != state.settings.include[item.key] }
                    .associateBy({ item -> item.key }, { item -> item.isChecked })

            if (include.isNotEmpty()) {
                listener?.onFeedSettingsChanged(FeedSettings(include))
            }
        }
    }

    private fun renderState(state: FeedSettingsViewState, listUpdateData: ListUpdateData<OptionItem>) {
        this.state = state
        contentStateController.state = state.contentState

        when (state.contentState) {
            ContentState.Content ->
                listUpdateData.applyTo(optionsAdapter)

            is ContentState.Empty -> {
                emptyView.state = state.contentState.emptyState
            }
        }
    }
}

interface OnFeedSettingsChangedListener {
    fun onFeedSettingsChanged(settings: FeedSettings)
}
