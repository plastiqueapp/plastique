package io.plastique.auth

import android.net.Uri
import com.sch.neon.EffectHandler
import com.sch.neon.MainLoop
import com.sch.neon.StateReducer
import com.sch.neon.StateWithEffects
import com.sch.neon.next
import com.sch.neon.timber.TimberLogger
import io.plastique.auth.LoginEffect.AuthenticateEffect
import io.plastique.auth.LoginEffect.GenerateAuthUrlEffect
import io.plastique.auth.LoginEvent.AuthErrorEvent
import io.plastique.auth.LoginEvent.AuthRedirectEvent
import io.plastique.auth.LoginEvent.AuthSuccessEvent
import io.plastique.auth.LoginEvent.AuthUrlGeneratedEvent
import io.plastique.auth.LoginEvent.ErrorDialogClosedEvent
import io.plastique.core.mvvm.BaseViewModel
import io.reactivex.Observable
import io.reactivex.rxkotlin.ofType
import timber.log.Timber
import javax.inject.Inject

class LoginViewModel @Inject constructor(
    stateReducer: LoginStateReducer,
    effectHandler: LoginEffectHandler,
    private val authenticator: Authenticator
) : BaseViewModel() {

    private val loop = MainLoop(
        reducer = stateReducer,
        effectHandler = effectHandler,
        listener = TimberLogger(LOG_TAG))

    val state: Observable<LoginViewState> by lazy(LazyThreadSafetyMode.NONE) {
        loop.loop(LoginViewState.Initial, GenerateAuthUrlEffect).disposeOnDestroy()
    }

    fun onRedirect(uri: Uri): Boolean {
        if (authenticator.isAuthRedirectUri(uri)) {
            dispatch(AuthRedirectEvent(uri))
            return true
        }
        return false
    }

    fun dispatch(event: LoginEvent) {
        loop.dispatch(event)
    }

    companion object {
        private const val LOG_TAG = "LoginViewModel"
    }
}

class LoginEffectHandler @Inject constructor(
    private val authenticator: Authenticator
) : EffectHandler<LoginEffect, LoginEvent> {

    override fun handle(effects: Observable<LoginEffect>): Observable<LoginEvent> {
        val generateAuthUrlEvents = effects.ofType<GenerateAuthUrlEffect>()
            .map { AuthUrlGeneratedEvent(authenticator.generateAuthUrl()) }

        val authenticateEvents = effects.ofType<AuthenticateEffect>()
            .switchMapSingle { effect ->
                authenticator.onRedirect(effect.redirectUri)
                    .doOnError(Timber::e)
                    .toSingleDefault<LoginEvent>(AuthSuccessEvent)
                    .onErrorReturn { error -> AuthErrorEvent(error) }
            }

        return Observable.merge(generateAuthUrlEvents, authenticateEvents)
    }
}

class LoginStateReducer @Inject constructor() : StateReducer<LoginEvent, LoginViewState, LoginEffect> {
    override fun reduce(state: LoginViewState, event: LoginEvent): StateWithEffects<LoginViewState, LoginEffect> = when (event) {
        is AuthRedirectEvent ->
            next(LoginViewState.InProgress, AuthenticateEffect(event.redirectUri))

        ErrorDialogClosedEvent ->
            next(state, GenerateAuthUrlEffect)

        AuthSuccessEvent ->
            next(LoginViewState.Success)

        is AuthErrorEvent ->
            next(LoginViewState.Error)

        is AuthUrlGeneratedEvent ->
            next(LoginViewState.LoadUrl(authUrl = event.authUrl))
    }
}
