package io.plastique.core.session

import io.plastique.core.client.AccessTokenProvider
import io.reactivex.Observable

interface SessionManager : AccessTokenProvider {
    var session: Session

    val sessionChanges: Observable<Session>

    fun logout()
}

val SessionManager.currentUsername: String
    get() = when (val session = this.session) {
        is Session.User -> session.username
        else -> throw UserNotAuthenticatedException()
    }
