package io.plastique.auth

import com.sch.rxjava2.extensions.mapError
import com.sch.rxjava2.extensions.sneakyGet
import io.plastique.api.auth.AuthService
import io.plastique.api.common.ErrorType
import io.plastique.core.client.ApiConfiguration
import io.plastique.core.exceptions.ApiException
import io.plastique.core.session.AuthenticationExpiredException
import io.plastique.core.session.OnLogoutListener
import io.plastique.core.session.Session
import io.plastique.core.session.SessionManager
import io.plastique.core.session.SessionStorage
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.subjects.BehaviorSubject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionManagerImpl @Inject constructor(
    private val apiConfig: ApiConfiguration,
    private val authService: AuthService,
    private val sessionStorage: SessionStorage,
    private val onLogoutListeners: @JvmSuppressWildcards Set<OnLogoutListener>
) : SessionManager {

    private val sessionSubject = BehaviorSubject.create<Session>().toSerialized()

    @Volatile
    override var session: Session = sessionStorage.getSession()
        set(value) {
            field = value
            sessionSubject.onNext(value)
            sessionStorage.saveSession(value)
        }

    override val sessionChanges: Observable<Session>
        get() = sessionSubject.distinctUntilChanged()

    init {
        sessionSubject.onNext(session)
    }

    override fun getAccessToken(invalidatedAccessToken: String?): String {
        var localSession = session
        if (localSession === Session.None || invalidatedAccessToken != null) {
            synchronized(this) {
                localSession = session
                if (localSession === Session.None || invalidatedAccessToken != null && localSession.accessToken == invalidatedAccessToken) {
                    session = refreshAccessToken(localSession)
                            .mapError { error ->
                                if (error is ApiException && error.errorData.type == ErrorType.InvalidRequest) {
                                    logout()
                                    AuthenticationExpiredException(error)
                                } else {
                                    error
                                }
                            }
                            .sneakyGet()
                }
            }
        }
        return session.accessToken
    }

    override fun logout() {
        session = Session.None
        onLogoutListeners.forEach { it.onLogout() }
    }

    private fun refreshAccessToken(session: Session): Single<Session> = when (session) {
        is Session.User -> authService.refreshAccessToken(apiConfig.clientId, apiConfig.clientSecret, session.refreshToken)
                .map { result -> session.copy(accessToken = result.accessToken, refreshToken = result.refreshToken!!) }

        else -> authService.requestAccessToken(apiConfig.clientId, apiConfig.clientSecret)
                .map { result -> Session.Anonymous(accessToken = result.accessToken) }
    }

    private val Session.accessToken: String
        get() = when (this) {
            is Session.Anonymous -> accessToken
            is Session.User -> accessToken
            Session.None -> throw IllegalStateException("No active session")
        }
}
