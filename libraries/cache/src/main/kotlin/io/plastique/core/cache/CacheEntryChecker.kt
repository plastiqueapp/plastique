package io.plastique.core.cache

import org.threeten.bp.Clock
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
    private val clock: Clock,
    private val cacheDuration: Duration
) : CacheEntryChecker {
    override fun getCacheStatus(cacheEntry: CacheEntry): CacheStatus {
        return if (cacheEntry.isActual(clock.instant(), cacheDuration)) {
            CacheStatus.Actual
        } else {
            CacheStatus.Outdated
        }
    }
}

typealias MetadataValidator = (serializedMetadata: String) -> Boolean

class MetadataValidatingCacheEntryChecker(
    private val clock: Clock,
    private val cacheDuration: Duration,
    private val metadataValidator: MetadataValidator
) : CacheEntryChecker {
    override fun getCacheStatus(cacheEntry: CacheEntry): CacheStatus {
        return if (cacheEntry.metadata != null && metadataValidator(cacheEntry.metadata)) {
            if (cacheEntry.isActual(clock.instant(), cacheDuration)) {
                CacheStatus.Actual
            } else {
                CacheStatus.Outdated
            }
        } else {
            CacheStatus.Absent
        }
    }
}
