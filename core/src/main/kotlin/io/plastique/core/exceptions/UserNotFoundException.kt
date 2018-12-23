package io.plastique.core.exceptions

class UserNotFoundException(val username: String, cause: Throwable)
    : Exception("User $username not found", cause)
