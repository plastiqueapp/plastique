package io.plastique.core.session

sealed class Session {
    data class Anonymous(val accessToken: String) : Session()

    data class User(
        val accessToken: String,
        val refreshToken: String,
        val userId: String
    ) : Session()

    object None : Session() {
        override fun toString(): String = "None"
    }
}
