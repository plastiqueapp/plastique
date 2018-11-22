package io.plastique.core.client

import io.plastique.api.common.ApiConstants
import io.plastique.core.BuildConfig
import okhttp3.Authenticator
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import javax.inject.Inject

class AuthInterceptor @Inject constructor(
    private val configuration: ApiConfiguration,
    private val accessTokenProvider: AccessTokenProvider
) : Authenticator, Interceptor {

    private val accessTokenAppender: AccessTokenAppender = if (BuildConfig.DEBUG) UrlAccessTokenAppender() else HeaderAccessTokenAppender()

    override fun authenticate(route: Route?, response: Response): Request? {
        // Stop if previous attempt has failed
        if (response.priorResponse() != null) {
            return null
        }
        val request = response.request()
        if (request.isAuthorized) {
            val previousAccessToken = accessTokenAppender.getAccessToken(request)!!
            val accessToken = accessTokenProvider.getAccessToken(previousAccessToken)

            val builder = request.newBuilder()
            accessTokenAppender.append(accessToken, request, builder)
            return builder.build()
        }
        return null
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val builder = request.newBuilder()
                .header(HttpHeaders.API_VERSION, ApiConstants.VERSION)
                .header(HttpHeaders.USER_AGENT, configuration.userAgent)

        if (request.isAuthorized && !accessTokenAppender.hasAccessToken(request)) {
            val accessToken = accessTokenProvider.getAccessToken()
            accessTokenAppender.append(accessToken, request, builder)
        }

        return chain.proceed(builder.build())
    }

    private val Request.isAuthorized: Boolean
        get() {
            val path = url().encodedPath()
            return path.startsWith("/api/v1/oauth2") && !path.endsWith("whoami")
        }
}
