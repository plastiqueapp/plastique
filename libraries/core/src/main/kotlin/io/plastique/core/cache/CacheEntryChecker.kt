package io.plastique.core.cache

import io.plastique.core.time.TimeProvider
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

typealias MetadataValidator = (serializedMetadata: String) -> Boolean

class MetadataValidatingCacheEntryChecker(
    private val timeProvider: TimeProvider,
    private val cacheDuration: Duration,
    private val metadataValidator: MetadataValidator
) : CacheEntryChecker {
    override fun getCacheStatus(cacheEntry: CacheEntry): CacheStatus {
        return if (cacheEntry.metadata != null && metadataValidator(cacheEntry.metadata)) {
            if (cacheEntry.isActual(timeProvider.currentInstant, cacheDuration)) {
                CacheStatus.Actual
            } else {
                CacheStatus.Outdated
            }
        } else {
            CacheStatus.Absent
        }
    }
}
