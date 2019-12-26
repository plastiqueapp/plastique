package io.plastique.core.cache

inline class CacheKey(val value: String)

@Suppress("NOTHING_TO_INLINE")
inline fun String.toCacheKey(): CacheKey = CacheKey(this)
