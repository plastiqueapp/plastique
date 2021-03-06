package io.plastique.settings.licenses

import com.sch.neon.EffectHandler
import com.sch.neon.MainLoop
import com.sch.neon.StateReducer
import com.sch.neon.StateWithEffects
import com.sch.neon.next
import com.sch.neon.timber.TimberLogger
import io.plastique.common.ErrorMessageProvider
import io.plastique.core.lists.ListItem
import io.plastique.core.mvvm.BaseViewModel
import io.plastique.settings.SettingsNavigator
import io.plastique.settings.licenses.LicensesEffect.LoadLicensesEffect
import io.plastique.settings.licenses.LicensesEvent.LoadErrorEvent
import io.plastique.settings.licenses.LicensesEvent.LoadFinishedEvent
import io.plastique.settings.licenses.LicensesEvent.RetryClickEvent
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.internal.functions.Functions
import io.reactivex.rxkotlin.ofType
import timber.log.Timber
import javax.inject.Inject

class LicensesViewModel @Inject constructor(
    stateReducer: LicensesStateReducer,
    effectHandler: LicensesEffectHandler,
    val navigator: SettingsNavigator
) : BaseViewModel() {

    private val loop = MainLoop(
        reducer = stateReducer,
        effectHandler = effectHandler,
        listener = TimberLogger(LOG_TAG))

    val state: Observable<LicensesViewState> by lazy(LazyThreadSafetyMode.NONE) {
        loop.loop(LicensesViewState.Loading, LoadLicensesEffect).disposeOnDestroy()
    }

    fun dispatch(event: LicensesEvent) {
        loop.dispatch(event)
    }

    companion object {
        private const val LOG_TAG = "LicensesViewModel"
    }
}

class LicensesEffectHandler @Inject constructor(
    private val licenseRepository: LicenseRepository
) : EffectHandler<LicensesEffect, LicensesEvent> {

    override fun handle(effects: Observable<LicensesEffect>): Observable<LicensesEvent> {
        return effects.ofType<LoadLicensesEffect>()
            .switchMapSingle {
                loadItems()
                    .map<LicensesEvent> { items -> LoadFinishedEvent(items) }
                    .doOnError(Timber::e)
                    .onErrorReturn { error -> LoadErrorEvent(error) }
            }
    }

    private fun loadItems(): Single<List<ListItem>> {
        return licenseRepository.getLicenses()
            .flattenAsObservable(Functions.identity())
            .map<ListItem> { license -> LicenseItem(license) }
            .startWith(HeaderItem)
            .toList()
    }
}

class LicensesStateReducer @Inject constructor(
    private val errorMessageProvider: ErrorMessageProvider
) : StateReducer<LicensesEvent, LicensesViewState, LicensesEffect> {

    override fun reduce(state: LicensesViewState, event: LicensesEvent): StateWithEffects<LicensesViewState, LicensesEffect> = when (event) {
        is LoadFinishedEvent -> {
            next(LicensesViewState.Content(items = event.items))
        }

        is LoadErrorEvent -> {
            next(LicensesViewState.Empty(emptyState = errorMessageProvider.getErrorState(event.error)))
        }

        RetryClickEvent -> {
            next(LicensesViewState.Loading, LoadLicensesEffect)
        }
    }
}
