package io.plastique.collections.deviations

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import io.plastique.api.collections.CollectionService
import io.plastique.api.nextCursor
import io.plastique.collections.folders.CollectionFolderId
import io.plastique.core.cache.CacheKey
import io.plastique.core.cache.toCacheKey
import io.plastique.core.paging.OffsetCursor
import io.plastique.deviations.DeviationCacheMetadataSerializer
import io.plastique.deviations.DeviationFetcher
import io.plastique.deviations.FetchParams
import io.plastique.deviations.FetchResult
import io.reactivex.Single
import kotlinx.android.parcel.Parcelize
import javax.inject.Inject

@Parcelize
@JsonClass(generateAdapter = true)
data class CollectionDeviationParams(
    @Json(name = "folder_id")
    val folderId: CollectionFolderId,

    @Json(name = "show_mature")
    override val showMatureContent: Boolean = false
) : FetchParams {
    override val showLiterature: Boolean get() = true

    override fun with(showMatureContent: Boolean, showLiterature: Boolean): FetchParams {
        return copy(showMatureContent = showMatureContent)
    }
}

class CollectionDeviationFetcher @Inject constructor(
    private val collectionService: CollectionService
) : DeviationFetcher<CollectionDeviationParams, OffsetCursor> {
    override fun getCacheKey(params: CollectionDeviationParams): CacheKey =
        "collection-folder-deviations-${params.folderId.id}".toCacheKey()

    override fun createMetadataSerializer(): DeviationCacheMetadataSerializer =
        DeviationCacheMetadataSerializer(paramsType = CollectionDeviationParams::class.java, cursorType = OffsetCursor::class.java)

    override fun fetch(params: CollectionDeviationParams, cursor: OffsetCursor?): Single<FetchResult<OffsetCursor>> {
        val offset = cursor?.offset ?: 0
        return collectionService.getFolderContents(
            username = params.folderId.owner,
            folderId = params.folderId.id,
            matureContent = params.showMatureContent,
            offset = offset,
            limit = 24)
            .map { deviationList ->
                FetchResult(
                    deviations = deviationList.results,
                    nextCursor = deviationList.nextCursor,
                    replaceExisting = offset == 0)
            }
    }
}
