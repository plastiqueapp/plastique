package io.plastique.core.client

import io.plastique.api.common.ApiConstants

data class ApiConfiguration(
    val apiUrl: String = ApiConstants.URL,
    val authUrl: String,
    val clientId: String,
    val clientSecret: String,
    val userAgent: String
)
