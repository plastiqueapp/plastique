package io.plastique.auth

import android.net.Uri
import io.plastique.core.flow.Effect

sealed class LoginEffect : Effect() {
    object GenerateAuthUrlEffect : LoginEffect()
    data class AuthenticateEffect(val redirectUri: Uri) : LoginEffect()
}
