package io.plastique.profile

import io.plastique.core.ViewModel
import io.plastique.core.flow.MainLoop
import io.plastique.core.flow.Next
import io.plastique.core.flow.Reducer
import io.plastique.core.flow.TimberLogger
import io.plastique.core.flow.next
import io.plastique.core.session.Session
import io.plastique.core.session.SessionManager
import io.plastique.inject.scopes.FragmentScope
import io.plastique.profile.ProfileEvent.SessionChangedEvent
import io.reactivex.Observable
import javax.inject.Inject

@FragmentScope
class ProfileViewModel @Inject constructor(
    stateReducer: StateReducer,
    private val sessionManager: SessionManager
) : ViewModel() {
    private val loop = MainLoop(
            reducer = stateReducer,
            externalEvents = externalEvents(),
            listener = TimberLogger(LOG_TAG))

    val state: Observable<ProfileViewState> by lazy(LazyThreadSafetyMode.NONE) {
        loop.loop(ProfileViewState()).disposeOnDestroy()
    }

    private fun externalEvents(): Observable<ProfileEvent> {
        return sessionManager.sessionChanges
                .bindToLifecycle()
                .map { session -> SessionChangedEvent(session) }
    }

    companion object {
        private const val LOG_TAG = "ProfileViewModel"
    }
}

class StateReducer @Inject constructor() : Reducer<ProfileEvent, ProfileViewState, ProfileEffect> {
    override fun invoke(state: ProfileViewState, event: ProfileEvent): Next<ProfileViewState, ProfileEffect> = when (event) {
        is SessionChangedEvent -> {
            next(state.copy(showLoginButton = event.session !is Session.User))
        }
    }
}
