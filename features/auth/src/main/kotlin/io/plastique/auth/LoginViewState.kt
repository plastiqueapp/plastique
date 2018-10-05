package io.plastique.auth

data class LoginViewState(
    val authUrl: String? = null,
    val authInProgress: Boolean = false,
    val authSuccess: Boolean = false,
    val authError: Boolean = false
)
