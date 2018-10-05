package io.plastique.core.extensions

inline fun <T : Any> List<T>.replaceIf(crossinline predicate: (T) -> Boolean, crossinline replacer: (T) -> (T)): List<T> {
    return map { if (predicate.invoke(it)) replacer.invoke(it) else it }
}
