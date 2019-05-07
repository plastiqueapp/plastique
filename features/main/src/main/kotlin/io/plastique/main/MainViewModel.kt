package io.plastique.main

import com.sch.neon.MainLoop
import com.sch.neon.StateReducer
import com.sch.neon.StateWithEffects
import com.sch.neon.next
import com.sch.neon.timber.TimberLogger
import com.sch.rxjava2.extensions.valveLatest
import io.plastique.core.BaseViewModel
import io.plastique.core.session.Session
import io.plastique.core.session.SessionManager
import io.plastique.inject.scopes.ActivityScope
import io.plastique.main.MainEvent.UserChangedEvent
import io.plastique.users.UserRepository
import io.plastique.util.Optional
import io.plastique.util.toOptional
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import javax.inject.Inject

@ActivityScope
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
                                .onErrorReturnItem(Optional.None)
                    } else {
                        Observable.just(Optional.None)
                    }
                }
                .map { user -> UserChangedEvent(user.orNull()) }
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
