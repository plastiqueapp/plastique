package io.plastique.gallery

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import io.plastique.api.gallery.GalleryService
import io.plastique.core.paging.OffsetCursor
import io.plastique.deviations.DeviationCacheMetadataSerializer
import io.plastique.deviations.DeviationFetcher
import io.plastique.deviations.FetchParams
import io.plastique.deviations.FetchResult
import io.reactivex.Single
import javax.inject.Inject

@JsonClass(generateAdapter = true)
data class GalleryDeviationParams(
    @Json(name = "username")
    val username: String?,

    @Json(name = "folder_id")
    val folderId: String,

    @Json(name = "show_mature")
    override val showMatureContent: Boolean = false
) : FetchParams {
    override val showLiterature: Boolean get() = true

    override fun isSameAs(params: FetchParams): Boolean {
        if (params !is GalleryDeviationParams) return false
        return username == params.username &&
                folderId == params.folderId &&
                showMatureContent == params.showMatureContent
    }

    override fun with(showMatureContent: Boolean, showLiterature: Boolean): FetchParams {
        return copy(showMatureContent = showMatureContent)
    }
}

class GalleryDeviationFetcher @Inject constructor(
    private val galleryService: GalleryService
) : DeviationFetcher<GalleryDeviationParams, OffsetCursor> {
    override fun getCacheKey(params: GalleryDeviationParams): String = "gallery-deviations-${params.folderId}"

    override fun createMetadataSerializer(): DeviationCacheMetadataSerializer =
            DeviationCacheMetadataSerializer(paramsType = GalleryDeviationParams::class.java, cursorType = OffsetCursor::class.java)

    override fun fetch(params: GalleryDeviationParams, cursor: OffsetCursor?): Single<FetchResult<OffsetCursor>> {
        val offset = cursor?.offset ?: 0
        return galleryService.getFolderContents(
                username = params.username,
                folderId = params.folderId,
                matureContent = params.showMatureContent,
                offset = offset,
                limit = 24)
                .map { deviationList ->
                    FetchResult(
                            deviations = deviationList.deviations,
                            nextCursor = if (deviationList.hasMore) OffsetCursor(deviationList.nextOffset!!) else null,
                            replaceExisting = offset == 0)
                }
    }
}
