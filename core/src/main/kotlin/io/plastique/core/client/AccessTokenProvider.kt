package io.plastique.core.client

interface AccessTokenProvider {
    fun getAccessToken(refresh: Boolean): String
}
