package io.plastique.core.client

data class ApiConfiguration(
    val apiUrl: String,
    val clientId: String,
    val clientSecret: String,
    val authRedirectUrl: String,
    val apiVersion: String,
    val userAgent: String
)
