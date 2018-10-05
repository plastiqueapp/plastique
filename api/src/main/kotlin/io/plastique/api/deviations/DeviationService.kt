package io.plastique.api.deviations

import androidx.annotation.IntRange
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
        @Query("mature_content") matureContent: Boolean): Single<DeviationList>

    @GET("browse/popular")
    fun getPopularDeviations(
        @Query("offset") offset: Int,
        @Query("limit") @IntRange(from = 1, to = 120) limit: Int,
        @Query("timerange") timeRange: TimeRange?,
        @Query("category_path") categoryPath: String?,
        @Query("mature_content") matureContent: Boolean): Single<DeviationList>

    @GET("browse/undiscovered")
    fun getUndiscoveredDeviations(
        @Query("offset") offset: Int,
        @Query("limit") @IntRange(from = 1, to = 120) limit: Int,
        @Query("category_path") categoryPath: String?,
        @Query("mature_content") matureContent: Boolean): Single<DeviationList>

    @GET("browse/dailydeviations")
    fun getDailyDeviations(
        @Query("date") date: String?,
        @Query("mature_content") matureContent: Boolean): Single<DeviationList>

    @GET("deviation/{deviationId}")
    fun getDeviationById(@Path("deviationId") deviationId: String): Single<Deviation>

    @GET("deviation/metadata")
    fun getMetadataByIds(@Query("deviationids[]") deviationIds: Collection<String>): Single<DeviationMetadataResponse>

    @GET("deviation/download/{deviationId}")
    fun getDeviationDownload(@Path("deviationId") deviationId: String): Single<DownloadInfo>

    @GET("browse/categorytree")
    fun getCategories(@Query("catpath") path: String): Single<CategoryList>
}
