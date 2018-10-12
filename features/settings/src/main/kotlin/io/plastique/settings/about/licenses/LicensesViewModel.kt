package io.plastique.settings.about.licenses

import com.sch.rxjava2.extensions.ofType
import io.plastique.core.ErrorMessageProvider
import io.plastique.core.ViewModel
import io.plastique.core.content.ContentState
import io.plastique.core.flow.MainLoop
import io.plastique.core.flow.Next
import io.plastique.core.flow.Reducer
import io.plastique.core.flow.TimberLogger
import io.plastique.core.flow.next
import io.plastique.inject.scopes.ActivityScope
import io.plastique.settings.about.licenses.LicensesEffect.LoadLicensesEffect
import io.plastique.settings.about.licenses.LicensesEvent.LoadErrorEvent
import io.plastique.settings.about.licenses.LicensesEvent.LoadFinishedEvent
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.internal.functions.Functions
import timber.log.Timber
import javax.inject.Inject

@ActivityScope
class LicensesViewModel @Inject constructor(
    stateReducer: StateReducer,
    private val licenseRepository: LicenseRepository,
    private val errorMessageProvider: ErrorMessageProvider
) : ViewModel() {
    private val loop = MainLoop(
            reducer = stateReducer,
            effectHandler = ::effectHandler,
            listener = TimberLogger(LOG_TAG))

    val state: Observable<LicensesViewState> by lazy(LazyThreadSafetyMode.NONE) {
        loop.loop(LicensesViewState(contentState = ContentState.Loading), LoadLicensesEffect).disposeOnDestroy()
    }

    private fun effectHandler(effects: Observable<LicensesEffect>): Observable<LicensesEvent> {
        return effects.ofType<LoadLicensesEffect>()
                .switchMapSingle {
                    loadItems()
                            .map<LicensesEvent> { items -> LoadFinishedEvent(items) }
                            .doOnError(Timber::e)
                            .onErrorReturn { error -> LoadErrorEvent(errorMessageProvider.getErrorState(error)) }
                }
    }

    private fun loadItems(): Single<List<LicensesItem>> {
        return licenseRepository.getLicenses()
                .flattenAsObservable(Functions.identity())
                .map<LicensesItem> { license -> LicenseItem(license) }
                .startWith(HeaderItem)
                .toList()
    }

    companion object {
        private const val LOG_TAG = "LicensesViewModel"
    }
}

class StateReducer @Inject constructor() : Reducer<LicensesEvent, LicensesViewState, LicensesEffect> {
    override fun invoke(state: LicensesViewState, event: LicensesEvent): Next<LicensesViewState, LicensesEffect> = when (event) {
        is LoadFinishedEvent -> {
            next(state.copy(contentState = ContentState.Content, items = event.items))
        }

        is LoadErrorEvent -> {
            next(state.copy(contentState = ContentState.Empty(event.emptyState, isError = true)))
        }
    }
}