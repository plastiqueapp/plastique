package io.plastique.core.client

import okhttp3.Request
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

class UrlAccessTokenAppenderTest {
    private val appender = UrlAccessTokenAppender()

    @Test
    @DisplayName("getAccessToken returns access token")
    fun getAccessToken() {
        val request = Request.Builder()
                .url("https://acme.org?access_token=abc")
                .build()
        assertEquals("abc", appender.getAccessToken(request))
    }

    @Test
    @DisplayName("getAccessToken returns null if request URL has no access token")
    fun getAccessToken_noToken() {
        val request = Request.Builder()
                .url("https://acme.org")
                .build()
        assertNull(appender.getAccessToken(request))
    }

    @Test
    @DisplayName("append adds access token to the URL")
    fun append() {
        val request = Request.Builder()
                .url("https://acme.org")
                .build()
        val builder = request.newBuilder()
        appender.append("abc", request, builder)

        assertEquals("https://acme.org/?access_token=abc", builder.build().url().toString())
    }

    @Test
    @DisplayName("append replaces existing access token")
    fun append_replace() {
        val request = Request.Builder()
                .url("https://acme.org?access_token=123")
                .build()
        val builder = request.newBuilder()
        appender.append("abc", request, builder)

        assertEquals("https://acme.org/?access_token=abc", builder.build().url().toString())
    }
}
