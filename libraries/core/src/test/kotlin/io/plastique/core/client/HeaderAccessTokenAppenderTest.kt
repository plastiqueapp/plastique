package io.plastique.core.client

import okhttp3.Headers.Companion.headersOf
import okhttp3.Request
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class HeaderAccessTokenAppenderTest {
    private val appender = HeaderAccessTokenAppender()

    @Test
    fun `getAccessToken returns access token`() {
        val request = Request.Builder()
            .url("https://acme.org")
            .header(HttpHeaders.AUTHORIZATION, "Bearer abc")
            .build()
        assertEquals("abc", appender.getAccessToken(request))
    }

    @Test
    fun `getAccessToken returns null if request has no Authorization header`() {
        val request = Request.Builder()
            .url("https://acme.org")
            .build()
        assertNull(appender.getAccessToken(request))
    }

    @Test
    fun `getAccessToken throws IllegalArgumentException if Authorization header cannot be parsed`() {
        val request = Request.Builder()
            .url("https://acme.org")
            .header(HttpHeaders.AUTHORIZATION, "Basic abc")
            .build()
        assertThrows<IllegalArgumentException>("Unsupported Authorization header: 'Basic abc'") {
            appender.getAccessToken(request)
        }
    }

    @Test
    fun `append adds Authorization header with Bearer token`() {
        val request = Request.Builder()
            .url("https://acme.org")
            .build()
        val builder = request.newBuilder()
        appender.append("abc", request, builder)

        assertEquals(headersOf(HttpHeaders.AUTHORIZATION, "Bearer abc"), builder.build().headers)
    }

    @Test
    fun `append replaces existing Authorization header`() {
        val request = Request.Builder()
            .url("https://acme.org")
            .header(HttpHeaders.AUTHORIZATION, "Bearer 123")
            .build()
        val builder = request.newBuilder()
        appender.append("abc", request, builder)

        assertEquals(headersOf(HttpHeaders.AUTHORIZATION, "Bearer abc"), builder.build().headers)
    }
}
