@file:Suppress("NOTHING_TO_INLINE")

package io.plastique.core.extensions

inline fun <T : Any> List<T>.replaceIf(crossinline predicate: (T) -> Boolean, crossinline replacer: (T) -> (T)): List<T> {
    return map { if (predicate.invoke(it)) replacer.invoke(it) else it }
}

inline fun <A, B, C> Pair<A, B>.add(value: C): Triple<A, B, C> = Triple(first, second, value)
