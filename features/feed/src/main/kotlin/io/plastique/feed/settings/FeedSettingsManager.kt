package io.plastique.feed.settings

import io.plastique.api.feed.FeedService
import io.plastique.core.cache.CacheEntryRepository
import io.plastique.feed.FeedRepository
import io.reactivex.Completable
import io.reactivex.Single
import javax.inject.Inject

class FeedSettingsManager @Inject constructor(
    private val feedService: FeedService,
    private val cacheEntryRepository: CacheEntryRepository
) {
    fun getSettings(): Single<FeedSettings> {
        return feedService.getSettings()
                .map { FeedSettings(include = it.include) }
    }

    fun updateSettings(settings: FeedSettings): Completable {
        val params = settings.include.mapKeys { entry -> "include[${entry.key}]" }
        return feedService.updateSettings(params)
                .doOnComplete { cacheEntryRepository.deleteEntryByKey(FeedRepository.CACHE_KEY) }
    }
}
