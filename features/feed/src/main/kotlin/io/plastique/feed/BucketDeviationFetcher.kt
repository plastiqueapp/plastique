package io.plastique.feed

import io.plastique.api.feed.FeedService
import io.plastique.core.paging.OffsetCursor
import io.plastique.core.paging.nextCursor
import io.plastique.deviations.DeviationCacheMetadataSerializer
import io.plastique.deviations.DeviationFetcher
import io.plastique.deviations.FetchResult
import io.reactivex.Single
import javax.inject.Inject

class BucketDeviationFetcher @Inject constructor(private val feedService: FeedService) : DeviationFetcher<BucketDeviationParams, OffsetCursor> {
    override fun getCacheKey(params: BucketDeviationParams): String = "feed-bucket-${params.bucketId}"

    override fun createMetadataSerializer(): DeviationCacheMetadataSerializer =
            DeviationCacheMetadataSerializer(paramsType = BucketDeviationParams::class.java, cursorType = OffsetCursor::class.java)

    override fun fetch(params: BucketDeviationParams, cursor: OffsetCursor?): Single<FetchResult<OffsetCursor>> {
        val offset = cursor?.offset ?: 0
        return feedService.getBucket(bucketId = params.bucketId, offset = offset, limit = 20, matureContent = params.showMatureContent)
                .map { deviationList ->
                    FetchResult(deviationList.results, deviationList.nextCursor, offset == 0)
                }
    }
}
