package io.plastique.main

import io.plastique.core.ViewModel
import io.plastique.core.session.Session
import io.plastique.core.session.SessionManager
import io.plastique.inject.scopes.ActivityScope
import javax.inject.Inject

@ActivityScope
class MainViewModel @Inject constructor(
    private val sessionManager: SessionManager
) : ViewModel() {

    fun isLoggedIn(): Boolean {
        return sessionManager.session is Session.User
    }

    fun onLogoutClick() {
        sessionManager.logout()
    }
}
