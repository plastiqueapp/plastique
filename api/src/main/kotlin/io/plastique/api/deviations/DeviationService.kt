package io.plastique.api.deviations

import androidx.annotation.IntRange
import io.plastique.api.common.ListResult
import io.plastique.api.common.PagedListResult
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface DeviationService {
    @GET("browse/hot")
    fun getHotDeviations(
        @Query("offset") offset: Int,
        @Query("limit") @IntRange(from = 1, to = 120) limit: Int,
        @Query("category_path") categoryPath: String?,
        @Query("mature_content") matureContent: Boolean): Single<PagedListResult<DeviationDto>>

    @GET("browse/popular")
    fun getPopularDeviations(
        @Query("offset") offset: Int,
        @Query("limit") @IntRange(from = 1, to = 120) limit: Int,
        @Query("timerange") timeRange: TimeRange?,
        @Query("category_path") categoryPath: String?,
        @Query("mature_content") matureContent: Boolean): Single<PagedListResult<DeviationDto>>

    @GET("browse/undiscovered")
    fun getUndiscoveredDeviations(
        @Query("offset") offset: Int,
        @Query("limit") @IntRange(from = 1, to = 120) limit: Int,
        @Query("category_path") categoryPath: String?,
        @Query("mature_content") matureContent: Boolean): Single<PagedListResult<DeviationDto>>

    @GET("browse/dailydeviations")
    fun getDailyDeviations(
        @Query("date") date: String?,
        @Query("mature_content") matureContent: Boolean): Single<ListResult<DeviationDto>>

    @GET("deviation/{deviationId}")
    fun getDeviationById(@Path("deviationId") deviationId: String): Single<DeviationDto>

    @GET("deviation/metadata")
    fun getMetadataByIds(@Query("deviationids[]") deviationIds: Collection<String>): Single<DeviationMetadataList>

    @GET("deviation/download/{deviationId}")
    fun getDownloadInfoById(@Path("deviationId") deviationId: String): Single<DownloadInfoDto>

    @GET("browse/categorytree")
    fun getCategories(@Query("catpath") path: String): Single<CategoryList>

    @GET("deviation/content")
    fun getContent(@Query("deviationid") deviationId: String): Single<DeviationContentDto>
}
