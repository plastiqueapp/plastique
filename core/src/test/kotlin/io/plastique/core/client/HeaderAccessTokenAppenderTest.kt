package io.plastique.core.client

import okhttp3.Headers
import okhttp3.Request
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class HeaderAccessTokenAppenderTest {
    private val appender = HeaderAccessTokenAppender()

    @Test
    @DisplayName("hasAccessToken returns true if request has Authorization header")
    fun hasAccessToken_true() {
        val request = Request.Builder()
                .url("https://acme.org")
                .header(HttpHeaders.AUTHORIZATION, "Bearer abc")
                .build()
        assertTrue(appender.hasAccessToken(request))
    }

    @Test
    @DisplayName("hasAccessToken returns false if request has no Authorization header")
    fun hasAccessToken_false() {
        val request = Request.Builder()
                .url("https://acme.org")
                .build()
        assertFalse(appender.hasAccessToken(request))
    }

    @Test
    @DisplayName("getAccessToken returns access token")
    fun getAccessToken() {
        val request = Request.Builder()
                .url("https://acme.org")
                .header(HttpHeaders.AUTHORIZATION, "Bearer abc")
                .build()
        assertEquals("abc", appender.getAccessToken(request))
    }

    @Test
    @DisplayName("getAccessToken returns null if request has no Authorization header")
    fun getAccessToken_noHeader() {
        val request = Request.Builder()
                .url("https://acme.org")
                .build()
        assertNull(appender.getAccessToken(request))
    }

    @Test
    @DisplayName("getAccessToken throws IllegalArgumentException if Authorization header cannot be parsed")
    fun getAccessToken_unknownFormat() {
        val request = Request.Builder()
                .url("https://acme.org")
                .header(HttpHeaders.AUTHORIZATION, "Basic abc")
                .build()
        assertThrows<IllegalArgumentException>("Unsupported Authorization header: 'Basic abc'") {
            appender.getAccessToken(request)
        }
    }

    @Test
    @DisplayName("append adds Authorization header with Bearer token")
    fun append() {
        val request = Request.Builder()
                .url("https://acme.org")
                .build()
        val builder = request.newBuilder()
        appender.append("abc", request, builder)

        assertEquals(Headers.of(HttpHeaders.AUTHORIZATION, "Bearer abc"), builder.build().headers())
    }

    @Test
    @DisplayName("append replaces existing Authorization header")
    fun append_replaces() {
        val request = Request.Builder()
                .url("https://acme.org")
                .header(HttpHeaders.AUTHORIZATION, "Bearer 123")
                .build()
        val builder = request.newBuilder()
        appender.append("abc", request, builder)

        assertEquals(Headers.of(HttpHeaders.AUTHORIZATION, "Bearer abc"), builder.build().headers())
    }
}
