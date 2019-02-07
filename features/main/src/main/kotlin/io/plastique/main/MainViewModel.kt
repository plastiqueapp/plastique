package io.plastique.main

import io.plastique.core.ViewModel
import io.plastique.core.flow.MainLoop
import io.plastique.core.flow.Next
import io.plastique.core.flow.Reducer
import io.plastique.core.flow.TimberLogger
import io.plastique.core.flow.next
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
) : ViewModel() {

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
                .bindToLifecycle()
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

class MainStateReducer @Inject constructor() : Reducer<MainEvent, MainViewState, MainEffect> {
    override fun invoke(state: MainViewState, event: MainEvent): Next<MainViewState, MainEffect> = when (event) {
        is UserChangedEvent -> {
            next(state.copy(user = event.user))
        }
    }
}
