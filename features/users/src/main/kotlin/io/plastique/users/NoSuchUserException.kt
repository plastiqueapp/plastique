package io.plastique.users

class NoSuchUserException(val username: String, cause: Throwable)
    : Exception("User $username not found", cause)
