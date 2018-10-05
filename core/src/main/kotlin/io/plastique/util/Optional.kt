package io.plastique.util

sealed class Optional<out T : Any> {
    data class Some<T : Any>(override val value: T) : Optional<T>() {
        override fun toString(): String = "Some($value)"
    }

    object None : Optional<Nothing>() {
        override val value: Nothing
            get() = throw IllegalStateException("Optional has no value")

        override fun toString(): String = "None"
    }

    val isPresent: Boolean
        get() = this != None

    abstract val value: T

    fun orNull(): T? = when (this) {
        is Some -> value
        None -> null
    }
}

fun <T : Any> T?.toOptional(): Optional<T> = when (this) {
    null -> Optional.None
    else -> Optional.Some(this)
}
