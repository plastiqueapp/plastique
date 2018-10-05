package io.plastique.auth

class AuthException : Exception {
    constructor(message: String) : super(message)
    constructor(cause: Throwable) : super(cause)
}
