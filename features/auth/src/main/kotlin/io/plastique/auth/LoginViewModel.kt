package io.plastique.auth

import android.net.Uri
import android.webkit.CookieManager
import com.sch.rxjava2.extensions.ofType
import io.plastique.auth.LoginEffect.AuthenticateEffect
import io.plastique.auth.LoginEffect.GenerateAuthUrlEffect
import io.plastique.auth.LoginEvent.AuthErrorEvent
import io.plastique.auth.LoginEvent.AuthRedirectEvent
import io.plastique.auth.LoginEvent.AuthSuccessEvent
import io.plastique.auth.LoginEvent.AuthUrlGeneratedEvent
import io.plastique.auth.LoginEvent.ErrorDialogDismissedEvent
import io.plastique.core.ViewModel
import io.plastique.core.flow.MainLoop
import io.plastique.core.flow.Next
import io.plastique.core.flow.Reducer
import io.plastique.core.flow.TimberLogger
import io.plastique.core.flow.next
import io.plastique.inject.scopes.ActivityScope
import io.reactivex.Observable
import timber.log.Timber
import javax.inject.Inject

@ActivityScope
class LoginViewModel @Inject constructor(
    stateReducer: LoginStateReducer,
    private val authenticator: Authenticator,
    private val cookieManager: CookieManager
) : ViewModel() {
    private val loop = MainLoop(
            reducer = stateReducer,
            effectHandler = ::effectHandler,
            listener = TimberLogger(LOG_TAG))

    val state: Observable<LoginViewState> by lazy(LazyThreadSafetyMode.NONE) {
        loop.loop(LoginViewState(), GenerateAuthUrlEffect).disposeOnDestroy()
    }

    fun onRedirect(redirectUrl: String): Boolean {
        val uri = Uri.parse(redirectUrl)
        if (!authenticator.isAuthRedirectUri(uri)) {
            return false
        }
        dispatch(AuthRedirectEvent(uri))
        return true
    }

    fun dispatch(event: LoginEvent) {
        loop.dispatch(event)
    }

    private fun effectHandler(effects: Observable<LoginEffect>): Observable<LoginEvent> {
        return Observable.merge(handleGenerateAuthUrl(effects), handleAuthenticate(effects))
    }

    private fun handleGenerateAuthUrl(effects: Observable<LoginEffect>): Observable<LoginEvent> {
        return effects.ofType<GenerateAuthUrlEffect>()
                .map { AuthUrlGeneratedEvent(authenticator.generateAuthUrl()) }
    }

    private fun handleAuthenticate(effects: Observable<LoginEffect>): Observable<LoginEvent> {
        return effects.ofType<AuthenticateEffect>()
                .switchMapSingle { effect ->
                    authenticator.onRedirect(effect.redirectUri)
                            .doOnError { error ->
                                Timber.e(error)
                                cookieManager.removeAllCookies(null)
                            }
                            .toSingleDefault<LoginEvent>(AuthSuccessEvent)
                            .onErrorReturn { AuthErrorEvent }
                }
    }

    companion object {
        private const val LOG_TAG = "LoginViewModel"
    }
}

class LoginStateReducer @Inject constructor() : Reducer<LoginEvent, LoginViewState, LoginEffect> {
    override fun invoke(state: LoginViewState, event: LoginEvent): Next<LoginViewState, LoginEffect> = when (event) {
        is AuthRedirectEvent -> {
            next(state.copy(authInProgress = true), AuthenticateEffect(event.redirectUri))
        }

        ErrorDialogDismissedEvent -> {
            next(state.copy(authError = false), GenerateAuthUrlEffect)
        }

        AuthSuccessEvent -> {
            next(state.copy(authInProgress = false, authSuccess = true))
        }

        AuthErrorEvent -> {
            next(state.copy(authInProgress = false, authError = true))
        }

        is AuthUrlGeneratedEvent -> {
            next(state.copy(authUrl = event.authUrl))
        }
    }
}
