package io.plastique.auth

import android.net.Uri
import io.plastique.api.auth.AuthService
import io.plastique.core.client.ApiConfiguration
import io.plastique.core.session.Session
import io.plastique.core.session.SessionManager
import io.plastique.users.UserRepository
import io.reactivex.Completable
import io.reactivex.Single
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Authenticator @Inject constructor(
    private val apiConfig: ApiConfiguration,
    private val authService: AuthService,
    private val sessionManager: SessionManager,
    private val userRepository: UserRepository
) {
    @Volatile private var csrfToken: String? = null

    fun generateAuthUrl(): String {
        val csrfToken = CsrfTokenGenerator.generate()
        val call = authService.authorize(
                apiConfig.clientId,
                csrfToken,
                apiConfig.authUrl,
                REQUESTED_SCOPES.joinToString(" "))
        this.csrfToken = csrfToken
        return call.request().url().toString()
    }

    fun isAuthRedirectUri(uri: Uri): Boolean {
        val uriWithoutQuery = uri.buildUpon().clearQuery().build()
        return uriWithoutQuery == Uri.parse(apiConfig.authUrl)
    }

    fun onRedirect(redirectUrl: Uri): Completable {
        if (csrfToken == null) {
            throw IllegalStateException("No pending authorization")
        }

        return Single.defer {
            val authCode = redirectUrl.getQueryParameter("code")?.takeIf { it.isNotEmpty() } ?: throw AuthException("Invalid auth code")
            val csrfToken = redirectUrl.getQueryParameter("state")
            if (this.csrfToken != csrfToken) {
                throw AuthException("Invalid CSRF token")
            }

            authService.requestAccessToken(
                    clientId = apiConfig.clientId,
                    clientSecret = apiConfig.clientSecret,
                    authCode = authCode,
                    redirectUri = apiConfig.authUrl)
        }
                .flatMap { tokenResult ->
                    csrfToken = null

                    userRepository.getCurrentUser(tokenResult.accessToken)
                            .map { user ->
                                Session.User(
                                        accessToken = tokenResult.accessToken,
                                        refreshToken = tokenResult.refreshToken!!,
                                        userId = user.id,
                                        username = user.name)
                            }
                }
                .doOnSuccess { session -> sessionManager.session = session }
                .ignoreElement()
    }

    companion object {
        private val REQUESTED_SCOPES = listOf(
                "browse", "collection", "comment.post", "feed", "gallery", "message", "note", "publish", "stash", "user", "user.manage")
    }
}
