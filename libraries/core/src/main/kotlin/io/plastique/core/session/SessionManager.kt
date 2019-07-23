package io.plastique.core.session

import com.gojuno.koptional.Optional
import com.gojuno.koptional.toOptional
import io.plastique.core.client.AccessTokenProvider
import io.reactivex.Observable

interface SessionManager : AccessTokenProvider {
    var session: Session

    val sessionChanges: Observable<Session>

    fun logout()
}

val SessionManager.userIdChanges: Observable<Optional<String>>
    get() = sessionChanges
        .distinctUntilChanged { session -> session.userId }
        .map { session -> session.userId.toOptional() }
