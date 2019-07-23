package io.plastique.profile

import com.github.technoir42.rxjava2.extensions.valveLatest
import com.sch.neon.MainLoop
import com.sch.neon.StateReducer
import com.sch.neon.StateWithEffects
import com.sch.neon.next
import com.sch.neon.timber.TimberLogger
import io.plastique.core.mvvm.BaseViewModel
import io.plastique.core.session.SessionManager
import io.plastique.core.session.userIdChanges
import io.plastique.profile.ProfileEvent.UserChangedEvent
import io.reactivex.Observable
import javax.inject.Inject

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
        return sessionManager.userIdChanges
            .skip(1)
            .valveLatest(screenVisible)
            .map { userId -> UserChangedEvent(userId.toNullable()) }
    }

    companion object {
        private const val LOG_TAG = "ProfileViewModel"
    }
}

class ProfileStateReducer @Inject constructor() : StateReducer<ProfileEvent, ProfileViewState, ProfileEffect> {
    override fun reduce(state: ProfileViewState, event: ProfileEvent): StateWithEffects<ProfileViewState, ProfileEffect> = when (event) {
        is UserChangedEvent -> {
            next(state.copy(showSignInButton = event.userId == null))
        }
    }
}
