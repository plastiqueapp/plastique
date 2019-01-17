package io.plastique.core.session

class AuthenticationExpiredException(cause: Throwable) : Exception("Session expired", cause)
