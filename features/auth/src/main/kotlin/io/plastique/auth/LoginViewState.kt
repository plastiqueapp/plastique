package io.plastique.auth

sealed class LoginViewState {
    override fun toString(): String = "LoginViewState.${javaClass.simpleName}"

    object Initial : LoginViewState()

    data class LoadUrl(val authUrl: String) : LoginViewState() {
        override fun toString(): String {
            return "LoginViewState.LoadUrl(authUrl='$authUrl')"
        }
    }

    object InProgress : LoginViewState()
    object Success : LoginViewState()
    object Error : LoginViewState()
}
