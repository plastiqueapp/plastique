package io.plastique.core.cache

import org.threeten.bp.Duration
import org.threeten.bp.Instant

data class CacheEntry(
    val key: CacheKey,
    val timestamp: Instant,
    val metadata: String? = null
) {
    fun isActual(now: Instant, cacheDuration: Duration): Boolean {
        return timestamp.plus(cacheDuration).isAfter(now) && timestamp.isBefore(now)
    }
}
