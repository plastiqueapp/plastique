package io.plastique.profile

import com.sch.neon.MainLoop
import com.sch.neon.StateReducer
import com.sch.neon.StateWithEffects
import com.sch.neon.next
import com.sch.neon.timber.TimberLogger
import com.sch.rxjava2.extensions.valveLatest
import io.plastique.core.BaseViewModel
import io.plastique.core.session.Session
import io.plastique.core.session.SessionManager
import io.plastique.inject.scopes.FragmentScope
import io.plastique.profile.ProfileEvent.SessionChangedEvent
import io.reactivex.Observable
import javax.inject.Inject

@FragmentScope
class ProfileViewModel @Inject constructor(
    stateReducer: ProfileStateReducer,
    private val sessionManager: SessionManager
) : BaseViewModel() {

    private val loop = MainLoop(
            reducer = stateReducer,
            externalEvents = externalEvents(),
            listener = TimberLogger(LOG_TAG))

    val state: Observable<ProfileViewState> by lazy(LazyThreadSafetyMode.NONE) {
        loop.loop(ProfileViewState()).disposeOnDestroy()
    }

    private fun externalEvents(): Observable<ProfileEvent> {
        return sessionManager.sessionChanges
                .valveLatest(screenVisible)
                .map { session -> SessionChangedEvent(session) }
    }

    companion object {
        private const val LOG_TAG = "ProfileViewModel"
    }
}

class ProfileStateReducer @Inject constructor() : StateReducer<ProfileEvent, ProfileViewState, ProfileEffect> {
    override fun reduce(state: ProfileViewState, event: ProfileEvent): StateWithEffects<ProfileViewState, ProfileEffect> = when (event) {
        is SessionChangedEvent -> {
            next(state.copy(showSignInButton = event.session !is Session.User))
        }
    }
}
