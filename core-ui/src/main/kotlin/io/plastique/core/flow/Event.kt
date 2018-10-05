package io.plastique.core.flow

abstract class Event {
    override fun toString(): String = javaClass.simpleName
}
