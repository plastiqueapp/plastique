package io.plastique.core.client

interface AccessTokenProvider {
    fun getAccessToken(invalidatedAccessToken: String? = null): String
}
