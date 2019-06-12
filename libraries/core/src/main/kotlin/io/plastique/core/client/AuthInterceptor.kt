package io.plastique.core.client

import androidx.annotation.Keep
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

    private val accessTokenAppender: AccessTokenAppender = if (configuration.debug) UrlAccessTokenAppender() else HeaderAccessTokenAppender()

    override fun authenticate(route: Route?, response: Response): Request? {
        // Stop if previous attempt has failed
        if (response.priorResponse() != null) {
            return null
        }
        val request = response.request()
        if (request.isAuthorized && request.tag(AccessTokenProvidedTag::class.java) == null) {
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
            .header(HttpHeaders.API_VERSION, configuration.apiVersion)
            .header(HttpHeaders.USER_AGENT, configuration.userAgent)

        val providedAccessToken = request.url().queryParameter("access_token")
        if (providedAccessToken != null) {
            builder.tag(AccessTokenProvidedTag::class.java, AccessTokenProvidedTag)
        } else if (request.isAuthorized) {
            val accessToken = accessTokenProvider.getAccessToken()
            accessTokenAppender.append(accessToken, request, builder)
        }

        return chain.proceed(builder.build())
    }

    private val Request.isAuthorized: Boolean
        get() = url().encodedPath().startsWith("/api/v1/")

    @Keep
    private object AccessTokenProvidedTag
}
