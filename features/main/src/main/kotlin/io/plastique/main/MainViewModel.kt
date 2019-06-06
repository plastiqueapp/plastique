package io.plastique.main

import com.github.technoir42.rxjava2.extensions.valveLatest
import com.gojuno.koptional.None
import com.gojuno.koptional.toOptional
import com.sch.neon.MainLoop
import com.sch.neon.StateReducer
import com.sch.neon.StateWithEffects
import com.sch.neon.next
import com.sch.neon.timber.TimberLogger
import io.plastique.core.mvvm.BaseViewModel
import io.plastique.core.session.Session
import io.plastique.core.session.SessionManager
import io.plastique.main.MainEvent.UserChangedEvent
import io.plastique.users.UserRepository
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import javax.inject.Inject

class MainViewModel @Inject constructor(
    stateReducer: MainStateReducer,
    private val sessionManager: SessionManager,
    private val userRepository: UserRepository
) : BaseViewModel() {

    lateinit var state: Observable<MainViewState>
    private val loop = MainLoop(
        reducer = stateReducer,
        externalEvents = events(),
        listener = TimberLogger(LOG_TAG))

    fun init() {
        if (::state.isInitialized) return

        state = loop.loop(MainViewState()).disposeOnDestroy()
    }

    private fun events(): Observable<MainEvent> {
        return sessionManager.sessionChanges
            .valveLatest(screenVisible)
            .distinctUntilChanged { session -> if (session is Session.User) session.userId else null }
            .switchMap { session ->
                if (session is Session.User) {
                    userRepository.getCurrentUser(session.userId)
                        .subscribeOn(Schedulers.io())
                        .map { it.toOptional() }
                        .doOnError(Timber::e)
                        .onErrorReturnItem(None)
                } else {
                    Observable.just(None)
                }
            }
            .map { user -> UserChangedEvent(user.toNullable()) }
    }

    companion object {
        private const val LOG_TAG = "MainViewModel"
    }
}

class MainStateReducer @Inject constructor() : StateReducer<MainEvent, MainViewState, MainEffect> {
    override fun reduce(state: MainViewState, event: MainEvent): StateWithEffects<MainViewState, MainEffect> = when (event) {
        is UserChangedEvent -> {
            next(state.copy(user = event.user))
        }
    }
}
