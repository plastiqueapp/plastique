package io.plastique.deviations.info

import com.sch.neon.EffectHandler
import com.sch.neon.MainLoop
import com.sch.neon.StateReducer
import com.sch.neon.StateWithEffects
import com.sch.neon.next
import com.sch.neon.timber.TimberLogger
import io.plastique.common.ErrorMessageProvider
import io.plastique.core.BaseViewModel
import io.plastique.core.text.RichTextFormatter
import io.plastique.core.text.SpannedWrapper
import io.plastique.deviations.info.DeviationInfoEffect.LoadInfoEffect
import io.plastique.deviations.info.DeviationInfoEvent.DeviationInfoChangedEvent
import io.plastique.deviations.info.DeviationInfoEvent.LoadErrorEvent
import io.plastique.deviations.info.DeviationInfoEvent.RetryClickEvent
import io.reactivex.Observable
import io.reactivex.rxkotlin.ofType
import timber.log.Timber
import javax.inject.Inject

class DeviationInfoViewModel @Inject constructor(
    stateReducer: DeviationInfoStateReducer,
    effectHandler: DeviationInfoEffectHandler
) : BaseViewModel() {

    lateinit var state: Observable<DeviationInfoViewState>
    private val loop = MainLoop(
        reducer = stateReducer,
        effectHandler = effectHandler,
        listener = TimberLogger(LOG_TAG))

    fun init(deviationId: String) {
        if (::state.isInitialized) return

        val initialState = DeviationInfoViewState.Loading(deviationId = deviationId)
        state = loop.loop(initialState, LoadInfoEffect(deviationId)).disposeOnDestroy()
    }

    fun dispatch(event: DeviationInfoEvent) {
        loop.dispatch(event)
    }

    companion object {
        private const val LOG_TAG = "DeviationInfoViewModel"
    }
}

class DeviationInfoEffectHandler @Inject constructor(
    private val deviationInfoRepository: DeviationInfoRepository
) : EffectHandler<DeviationInfoEffect, DeviationInfoEvent> {

    override fun handle(effects: Observable<DeviationInfoEffect>): Observable<DeviationInfoEvent> {
        return effects.ofType<LoadInfoEffect>()
            .switchMap { effect ->
                deviationInfoRepository.getDeviationInfo(effect.deviationId)
                    .map<DeviationInfoEvent> { deviationInfo -> DeviationInfoChangedEvent(deviationInfo) }
                    .doOnError(Timber::e)
                    .onErrorReturn { error -> LoadErrorEvent(error) }
            }
    }
}

class DeviationInfoStateReducer @Inject constructor(
    private val errorMessageProvider: ErrorMessageProvider,
    private val richTextFormatter: RichTextFormatter
) : StateReducer<DeviationInfoEvent, DeviationInfoViewState, DeviationInfoEffect> {

    override fun reduce(state: DeviationInfoViewState, event: DeviationInfoEvent): StateWithEffects<DeviationInfoViewState, DeviationInfoEffect> =
        when (event) {
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
