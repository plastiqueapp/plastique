package io.plastique.users

class UserNotFoundException(val username: String, cause: Throwable) : Exception("User '$username' not found", cause)
