package io.plastique.core.cache

import org.threeten.bp.Duration
import org.threeten.bp.Instant

enum class CacheStatus {
    Actual,
    Outdated,
    Absent
}

interface CacheEntryChecker {
    fun getCacheStatus(cacheEntry: CacheEntry): CacheStatus
}

class DurationBasedCacheEntryChecker(
    private val cacheDuration: Duration,
    private val currentTimeProvider: () -> Instant = Instant::now
) : CacheEntryChecker {
    override fun getCacheStatus(cacheEntry: CacheEntry): CacheStatus {
        return if (cacheEntry.isActual(currentTimeProvider(), cacheDuration)) {
            CacheStatus.Actual
        } else {
            CacheStatus.Outdated
        }
    }
}
