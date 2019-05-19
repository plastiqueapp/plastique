package io.plastique.core.session

import io.plastique.core.client.AccessTokenProvider
import io.reactivex.Observable

interface SessionManager : AccessTokenProvider {
    var session: Session

    val sessionChanges: Observable<Session>

    fun logout()
}
