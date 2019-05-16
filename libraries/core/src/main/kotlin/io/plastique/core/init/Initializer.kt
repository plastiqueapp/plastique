package io.plastique.core.init

abstract class Initializer {
    abstract fun initialize()

    open val priority: Int = 0
}
