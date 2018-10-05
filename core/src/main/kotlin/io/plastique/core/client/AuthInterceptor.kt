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

    private val accessTokenAppender: AccessTokenAppender = if (BuildConfig.DEBUG) DebugAccessTokenAppender() else ReleaseAccessTokenAppender()

    override fun authenticate(route: Route?, response: Response): Request? {
        // Stop if previous attempt has failed
        if (response.priorResponse() != null) {
            return null
        }
        if (response.request().isAuthorized) {
            val accessToken = accessTokenProvider.getAccessToken(true)
            return rewriteRequest(response.request(), accessToken)
        }
        return null
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        val accessToken = if (originalRequest.isAuthorized) accessTokenProvider.getAccessToken(false) else null
        val request = rewriteRequest(originalRequest, accessToken)

        return chain.proceed(request)
    }

    private fun rewriteRequest(request: Request, accessToken: String?): Request {
        val builder = request.newBuilder()
                .header(HttpHeaders.API_VERSION, ApiConstants.VERSION)
                .header(HttpHeaders.USER_AGENT, configuration.userAgent)

        if (accessToken != null) {
            accessTokenAppender.append(accessToken, request, builder)
        }

        return builder.build()
    }

    private val Request.isAuthorized: Boolean
        get() {
            val path = url().encodedPath()
            return path.startsWith("/api/v1/oauth2") && !path.endsWith("whoami")
        }
}
