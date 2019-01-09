package io.plastique.deviations.info

import com.sch.rxjava2.extensions.ofType
import io.plastique.core.ErrorMessageProvider
import io.plastique.core.ViewModel
import io.plastique.core.flow.MainLoop
import io.plastique.core.flow.Next
import io.plastique.core.flow.Reducer
import io.plastique.core.flow.TimberLogger
import io.plastique.core.flow.next
import io.plastique.core.text.RichTextFormatter
import io.plastique.core.text.SpannedWrapper
import io.plastique.deviations.info.DeviationInfoEffect.LoadInfoEffect
import io.plastique.deviations.info.DeviationInfoEvent.DeviationInfoChangedEvent
import io.plastique.deviations.info.DeviationInfoEvent.LoadErrorEvent
import io.plastique.deviations.info.DeviationInfoEvent.RetryClickEvent
import io.reactivex.Observable
import timber.log.Timber
import javax.inject.Inject

class DeviationInfoViewModel @Inject constructor(
    stateReducer: DeviationInfoStateReducer,
    private val deviationInfoRepository: DeviationInfoRepository
) : ViewModel() {

    lateinit var state: Observable<DeviationInfoViewState>
    private val loop = MainLoop(
            reducer = stateReducer,
            effectHandler = ::effectHandler,
            listener = TimberLogger(LOG_TAG))

    fun init(deviationId: String) {
        if (::state.isInitialized) return

        val initialState = DeviationInfoViewState.Loading(deviationId = deviationId)
        state = loop.loop(initialState, LoadInfoEffect(deviationId)).disposeOnDestroy()
    }

    fun dispatch(event: DeviationInfoEvent) {
        loop.dispatch(event)
    }

    private fun effectHandler(effects: Observable<DeviationInfoEffect>): Observable<DeviationInfoEvent> {
        return effects.ofType<LoadInfoEffect>()
                .switchMap { effect ->
                    deviationInfoRepository.getDeviationInfo(effect.deviationId)
                            .map<DeviationInfoEvent> { deviationInfo -> DeviationInfoChangedEvent(deviationInfo) }
                            .doOnError(Timber::e)
                            .onErrorReturn { error -> LoadErrorEvent(error) }
                }
    }

    companion object {
        private const val LOG_TAG = "DeviationInfoViewModel"
    }
}

class DeviationInfoStateReducer @Inject constructor(
    private val errorMessageProvider: ErrorMessageProvider,
    private val richTextFormatter: RichTextFormatter
) : Reducer<DeviationInfoEvent, DeviationInfoViewState, DeviationInfoEffect> {
    override fun invoke(state: DeviationInfoViewState, event: DeviationInfoEvent): Next<DeviationInfoViewState, DeviationInfoEffect> = when (event) {
        is DeviationInfoChangedEvent -> {
            next(DeviationInfoViewState.Content(
                    deviationId = state.deviationId,
                    title = event.deviationInfo.title,
                    author = event.deviationInfo.author,
                    publishTime = event.deviationInfo.publishTime,
                    description = SpannedWrapper(richTextFormatter.format(event.deviationInfo.description)),
                    tags = event.deviationInfo.tags))
        }

        is LoadErrorEvent -> {
            val emptyState = errorMessageProvider.getErrorState(event.error)
            next(DeviationInfoViewState.Error(deviationId = state.deviationId, emptyViewState = emptyState))
        }

        RetryClickEvent -> {
            next(DeviationInfoViewState.Loading(deviationId = state.deviationId), LoadInfoEffect(state.deviationId))
        }
    }
}
