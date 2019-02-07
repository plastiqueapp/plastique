package io.plastique.core.client

import okhttp3.Request

interface AccessTokenAppender {
    fun getAccessToken(request: Request): String?

    fun append(accessToken: String, request: Request, builder: Request.Builder)
}

class HeaderAccessTokenAppender : AccessTokenAppender {
    override fun getAccessToken(request: Request): String? {
        return request.header(HttpHeaders.AUTHORIZATION)?.let { value ->
            val typeAndCredentials = value.split(' ')
            if (typeAndCredentials.size != 2 || typeAndCredentials[0] != "Bearer") {
                throw IllegalArgumentException("Unsupported Authorization header: '$value'")
            }
            typeAndCredentials[1]
        }
    }

    override fun append(accessToken: String, request: Request, builder: Request.Builder) {
        builder.header(HttpHeaders.AUTHORIZATION, "Bearer $accessToken")
    }
}

class UrlAccessTokenAppender : AccessTokenAppender {
    override fun getAccessToken(request: Request): String? {
        return request.url().queryParameter("access_token")
    }

    override fun append(accessToken: String, request: Request, builder: Request.Builder) {
        val url = request.url()
                .newBuilder()
                .setQueryParameter("access_token", accessToken)
                .build()
        builder.url(url)
    }
}
