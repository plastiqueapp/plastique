package io.plastique.feed.settings

import io.plastique.core.ErrorMessageProvider
import io.plastique.core.ResourceProvider
import io.plastique.core.ViewModel
import io.plastique.core.content.ContentState
import io.plastique.core.extensions.replaceIf
import io.plastique.core.flow.MainLoop
import io.plastique.core.flow.Next
import io.plastique.core.flow.Reducer
import io.plastique.core.flow.TimberLogger
import io.plastique.core.flow.next
import io.plastique.feed.R
import io.plastique.feed.settings.FeedSettingsEffect.LoadFeedSettingsEffect
import io.plastique.feed.settings.FeedSettingsEvent.FeedSettingsLoadedEvent
import io.plastique.feed.settings.FeedSettingsEvent.LoadErrorEvent
import io.plastique.feed.settings.FeedSettingsEvent.RetryClickEvent
import io.plastique.feed.settings.FeedSettingsEvent.SetEnabledEvent
import io.plastique.inject.scopes.FragmentScope
import io.reactivex.Observable
import io.reactivex.rxkotlin.ofType
import timber.log.Timber
import javax.inject.Inject

@FragmentScope
class FeedSettingsViewModel @Inject constructor(
    stateReducer: FeedSettingsStateReducer,
    private val feedSettingsManager: FeedSettingsManager,
    private val resourceProvider: ResourceProvider
) : ViewModel() {
    private val loop = MainLoop(
            reducer = stateReducer,
            effectHandler = ::effectHandler,
            listener = TimberLogger(LOG_TAG)
    )

    val state: Observable<FeedSettingsViewState> by lazy(LazyThreadSafetyMode.NONE) {
        loop.loop(FeedSettingsViewState(contentState = ContentState.Loading), LoadFeedSettingsEffect).disposeOnDestroy()
    }

    fun dispatch(event: FeedSettingsEvent) {
        loop.dispatch(event)
    }

    private fun effectHandler(effects: Observable<FeedSettingsEffect>): Observable<FeedSettingsEvent> {
        return effects.ofType<LoadFeedSettingsEffect>()
                .switchMapSingle {
                    feedSettingsManager.getSettings()
                            .map<FeedSettingsEvent> { feedSettings -> FeedSettingsLoadedEvent(feedSettings, createOptions(feedSettings)) }
                            .doOnError(Timber::e)
                            .onErrorReturn { error -> LoadErrorEvent(error) }
                }
    }

    private fun createOptions(feedSettings: FeedSettings): List<OptionItem> {
        val keysAndTitles = resourceProvider.getStringArray(R.array.feed_settings_options)
        val result = ArrayList<OptionItem>(keysAndTitles.size)
        keysAndTitles.forEach { it ->
            val (key, title) = it.split('|')
            if (feedSettings.include.containsKey(key)) {
                result += OptionItem(key, title, feedSettings.include.getValue(key))
            }
        }
        return result
    }

    companion object {
        private const val LOG_TAG = "FeedSettingsViewModel"
    }
}

class FeedSettingsStateReducer @Inject constructor(
    private val errorMessageProvider: ErrorMessageProvider
) : Reducer<FeedSettingsEvent, FeedSettingsViewState, FeedSettingsEffect> {
    override fun invoke(state: FeedSettingsViewState, event: FeedSettingsEvent): Next<FeedSettingsViewState, FeedSettingsEffect> = when (event) {
        is FeedSettingsLoadedEvent -> {
            next(state.copy(contentState = ContentState.Content, settings = event.settings, items = event.items))
        }

        is LoadErrorEvent -> {
            next(state.copy(contentState = ContentState.Empty(emptyState = errorMessageProvider.getErrorState(event.error))))
        }

        RetryClickEvent -> {
            next(state.copy(contentState = ContentState.Loading), LoadFeedSettingsEffect)
        }

        is SetEnabledEvent -> {
            next(state.copy(items = state.items.replaceIf({ item -> item.key == event.optionKey }, { item -> item.copy(isChecked = event.isEnabled) })))
        }
    }
}
