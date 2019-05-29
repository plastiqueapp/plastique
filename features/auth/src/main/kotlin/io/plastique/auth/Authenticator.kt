package io.plastique.auth

import android.net.Uri
import android.webkit.CookieManager
import io.plastique.api.auth.AuthService
import io.plastique.api.users.UserService
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
    private val userRepository: UserRepository,
    private val userService: UserService,
    private val cookieManager: CookieManager
) {
    @Volatile private var csrfToken: String? = null

    fun generateAuthUrl(): String {
        val csrfToken = CsrfTokenGenerator.generate()
        val call = authService.authorize(
            apiConfig.clientId,
            csrfToken,
            apiConfig.authRedirectUrl,
            REQUESTED_SCOPES.joinToString(" "))
        this.csrfToken = csrfToken
        return call.request().url().toString()
    }

    fun isAuthRedirectUri(uri: Uri): Boolean {
        val uriWithoutQuery = uri.buildUpon().clearQuery().build()
        return uriWithoutQuery == Uri.parse(apiConfig.authRedirectUrl)
    }

    fun onRedirect(redirectUrl: Uri): Completable {
        return Single.defer {
            val authCode = redirectUrl.getQueryParameter("code")?.takeIf { it.isNotEmpty() } ?: throw AuthException("Invalid auth code")
            val csrfToken = redirectUrl.getQueryParameter("state") ?: throw AuthException("Missing CSRF token")
            validateCsrfToken(csrfToken)

            authService.requestAccessToken(
                clientId = apiConfig.clientId,
                clientSecret = apiConfig.clientSecret,
                authCode = authCode,
                redirectUri = apiConfig.authRedirectUrl)
        }
            .doOnError { cookieManager.removeAllCookies(null) }
            .flatMap { tokenResult ->
                csrfToken = null
                userService.whoami(tokenResult.accessToken)
                    .map { user ->
                        userRepository.persistWithTimestamp(user)
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

    private fun validateCsrfToken(csrfToken: String) {
        val localCsrfToken = this.csrfToken ?: throw AuthException("No pending authorization")
        if (localCsrfToken != csrfToken) {
            throw AuthException("Invalid CSRF token")
        }
    }

    companion object {
        private val REQUESTED_SCOPES = listOf(
            "browse",
            "browse.mlt",
            "collection",
            "comment.post",
            "feed",
            "gallery",
            "message",
            "note",
            "publish",
            "stash",
            "user",
            "user.manage")
    }
}
