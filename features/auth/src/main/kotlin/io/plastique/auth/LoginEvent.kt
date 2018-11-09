package io.plastique.auth

import android.net.Uri
import io.plastique.core.flow.Event

sealed class LoginEvent : Event() {
    data class AuthRedirectEvent(val redirectUri: Uri) : LoginEvent()
    data class AuthUrlGeneratedEvent(val authUrl: String) : LoginEvent()

    object AuthSuccessEvent : LoginEvent()
    data class AuthErrorEvent(val error: Throwable) : LoginEvent()
    object ErrorDialogDismissedEvent : LoginEvent()
}
