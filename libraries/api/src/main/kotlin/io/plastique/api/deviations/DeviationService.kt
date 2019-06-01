package io.plastique.api.deviations

import androidx.annotation.IntRange
import io.plastique.api.common.AccessScope
import io.plastique.api.common.ListResult
import io.plastique.api.common.PagedListResult
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface DeviationService {
    @GET("browse/hot")
    @AccessScope("browse")
    fun getHotDeviations(
        @Query("category_path") categoryPath: String?,
        @Query("mature_content") matureContent: Boolean,
        @Query("offset") @IntRange(from = 0, to = 50000) offset: Int,
        @Query("limit") @IntRange(from = 1, to = 120) limit: Int
    ): Single<PagedListResult<DeviationDto>>

    @GET("browse/popular")
    @AccessScope("browse")
    fun getPopularDeviations(
        @Query("timerange") timeRange: String?,
        @Query("category_path") categoryPath: String?,
        @Query("mature_content") matureContent: Boolean,
        @Query("offset") @IntRange(from = 0, to = 50000) offset: Int,
        @Query("limit") @IntRange(from = 1, to = 120) limit: Int
    ): Single<PagedListResult<DeviationDto>>

    @GET("browse/undiscovered")
    @AccessScope("browse")
    fun getUndiscoveredDeviations(
        @Query("category_path") categoryPath: String?,
        @Query("mature_content") matureContent: Boolean,
        @Query("offset") @IntRange(from = 0, to = 50000) offset: Int,
        @Query("limit") @IntRange(from = 1, to = 120) limit: Int
    ): Single<PagedListResult<DeviationDto>>

    @GET("browse/dailydeviations")
    @AccessScope("browse")
    fun getDailyDeviations(
        @Query("date") date: String?,
        @Query("mature_content") matureContent: Boolean
    ): Single<ListResult<DeviationDto>>

    @GET("deviation/{deviationId}")
    @AccessScope("browse")
    fun getDeviationById(@Path("deviationId") deviationId: String): Single<DeviationDto>

    @GET("deviation/metadata")
    @AccessScope("browse")
    fun getMetadataByIds(@Query("deviationids[]") deviationIds: Collection<String>): Single<DeviationMetadataList>

    @GET("deviation/download/{deviationId}")
    @AccessScope("browse")
    fun getDownloadInfoById(@Path("deviationId") deviationId: String): Single<DownloadInfoDto>

    @GET("browse/categorytree")
    @AccessScope("browse")
    fun getCategories(@Query("catpath") path: String): Single<CategoryList>

    @GET("deviation/content")
    @AccessScope("browse")
    fun getContent(@Query("deviationid") deviationId: String): Single<DeviationContentDto>
}
