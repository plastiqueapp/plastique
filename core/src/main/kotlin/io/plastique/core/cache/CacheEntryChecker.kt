package io.plastique.core.cache

import io.plastique.util.TimeProvider
import org.threeten.bp.Duration

enum class CacheStatus {
    Actual,
    Outdated,
    Absent
}

interface CacheEntryChecker {
    fun getCacheStatus(cacheEntry: CacheEntry): CacheStatus
}

class DurationBasedCacheEntryChecker(
    private val timeProvider: TimeProvider,
    private val cacheDuration: Duration
) : CacheEntryChecker {
    override fun getCacheStatus(cacheEntry: CacheEntry): CacheStatus {
        return if (cacheEntry.isActual(timeProvider.currentInstant, cacheDuration)) {
            CacheStatus.Actual
        } else {
            CacheStatus.Outdated
        }
    }
}
