package io.plastique.gallery

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import io.plastique.api.gallery.GalleryService
import io.plastique.api.gallery.SortModes
import io.plastique.api.nextCursor
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
data class GalleryDeviationParams(
    @Json(name = "folder_id")
    val folderId: GalleryFolderId,

    @Json(name = "show_mature")
    override val showMatureContent: Boolean = false
) : FetchParams {
    override val showLiterature: Boolean get() = true

    override fun with(showMatureContent: Boolean, showLiterature: Boolean): FetchParams {
        return copy(showMatureContent = showMatureContent)
    }
}

class GalleryDeviationFetcher @Inject constructor(
    private val galleryService: GalleryService
) : DeviationFetcher<GalleryDeviationParams, OffsetCursor> {
    override fun getCacheKey(params: GalleryDeviationParams): String =
        "gallery-deviations-${params.folderId.id}"

    override fun createMetadataSerializer(): DeviationCacheMetadataSerializer =
        DeviationCacheMetadataSerializer(paramsType = GalleryDeviationParams::class.java, cursorType = OffsetCursor::class.java)

    override fun fetch(params: GalleryDeviationParams, cursor: OffsetCursor?): Single<FetchResult<OffsetCursor>> {
        val offset = cursor?.offset ?: 0
        return galleryService.getFolderContents(
            username = params.folderId.username,
            folderId = params.folderId.id,
            mode = SortModes.POPULAR,
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
