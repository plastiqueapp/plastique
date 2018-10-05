package io.plastique.core.client

import okhttp3.Request

interface AccessTokenAppender {
    fun append(accessToken: String, request: Request, builder: Request.Builder)
}

class DebugAccessTokenAppender : AccessTokenAppender {
    override fun append(accessToken: String, request: Request, builder: Request.Builder) {
        if (request.url().queryParameter("access_token") == null) {
            val url = request.url()
                    .newBuilder()
                    .addQueryParameter("access_token", accessToken)
                    .build()
            builder.url(url)
        }
    }
}

class ReleaseAccessTokenAppender : AccessTokenAppender {
    override fun append(accessToken: String, request: Request, builder: Request.Builder) {
        if (request.header(HttpHeaders.AUTHORIZATION) == null) {
            builder.header(HttpHeaders.AUTHORIZATION, "Bearer $accessToken")
        }
    }
}
