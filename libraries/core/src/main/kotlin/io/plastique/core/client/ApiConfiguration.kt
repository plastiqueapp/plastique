package io.plastique.core.client

data class ApiConfiguration(
    val apiUrl: String,
    val authUrl: String,
    val clientId: String,
    val clientSecret: String,
    val apiVersion: String,
    val userAgent: String
)
