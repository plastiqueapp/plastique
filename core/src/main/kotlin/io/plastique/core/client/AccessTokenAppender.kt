package io.plastique.core.client

import okhttp3.Request

interface AccessTokenAppender {
    fun hasAccessToken(request: Request): Boolean

    fun append(accessToken: String, request: Request, builder: Request.Builder)
}

class DebugAccessTokenAppender : AccessTokenAppender {
    override fun hasAccessToken(request: Request): Boolean {
        return request.url().queryParameter("access_token") != null
    }

    override fun append(accessToken: String, request: Request, builder: Request.Builder) {
        if (!hasAccessToken(request)) {
            val url = request.url()
                    .newBuilder()
                    .addQueryParameter("access_token", accessToken)
                    .build()
            builder.url(url)
        }
    }
}

class ReleaseAccessTokenAppender : AccessTokenAppender {
    override fun hasAccessToken(request: Request): Boolean {
        return request.header(HttpHeaders.AUTHORIZATION) != null
    }

    override fun append(accessToken: String, request: Request, builder: Request.Builder) {
        if (!hasAccessToken(request)) {
            builder.header(HttpHeaders.AUTHORIZATION, "Bearer $accessToken")
        }
    }
}
