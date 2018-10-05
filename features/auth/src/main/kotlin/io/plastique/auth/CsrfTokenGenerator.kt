package io.plastique.auth

import java.util.UUID

object CsrfTokenGenerator {
    fun generate(): String {
        return UUID.randomUUID().toString()
    }
}
