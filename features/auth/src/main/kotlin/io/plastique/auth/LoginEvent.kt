package io.plastique.auth

import android.net.Uri
import io.plastique.core.flow.Event

sealed class LoginEvent : Event() {
    data class AuthRedirectEvent(val redirectUri: Uri) : LoginEvent()
    data class AuthUrlGeneratedEvent(val authUrl: String) : LoginEvent()

    object AuthSuccessEvent : LoginEvent()
    object AuthErrorEvent : LoginEvent()
    object ErrorDialogDismissedEvent : LoginEvent()
}
