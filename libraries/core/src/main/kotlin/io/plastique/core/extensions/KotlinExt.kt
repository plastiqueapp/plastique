@file:Suppress("NOTHING_TO_INLINE")

package io.plastique.core.extensions

inline fun <T : Any> List<T>.replaceIf(predicate: (T) -> Boolean, replacer: (T) -> (T)): List<T> {
    return map { if (predicate(it)) replacer(it) else it }
}

inline fun <A, B, C> Pair<A, B>.add(value: C): Triple<A, B, C> = Triple(first, second, value)

inline fun String?.nullIfEmpty(): String? = if (isNullOrEmpty()) null else this

fun String.truncate(maxLength: Int): String {
    require(maxLength > 0) { "maxLength $maxLength must be greater than zero" }
    return if (length > maxLength) substring(0, maxLength) + "..." else this
}
