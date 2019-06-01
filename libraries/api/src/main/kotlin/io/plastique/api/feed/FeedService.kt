package io.plastique.api.feed

import androidx.annotation.IntRange
import io.plastique.api.common.AccessScope
import io.plastique.api.common.AccessScopes.FEED
import io.plastique.api.common.PagedListResult
import io.plastique.api.deviations.DeviationDto
import io.reactivex.Completable
import io.reactivex.Single
import retrofit2.http.FieldMap
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface FeedService {
    @GET("feed/home")
    @AccessScope(FEED)
    fun getHomeFeed(
        @Query("cursor") cursor: String?,
        @Query("mature_content") matureContent: Boolean
    ): Single<FeedElementList>

    @GET("feed/profile")
    @AccessScope(FEED)
    fun getProfileFeed(
        @Query("cursor") cursor: String?,
        @Query("mature_content") matureContent: Boolean
    ): Single<FeedElementList>

    @GET("feed/notifications")
    @AccessScope(FEED)
    fun getNotificationsFeed(
        @Query("cursor") cursor: String?,
        @Query("mature_content") matureContent: Boolean
    ): Single<FeedElementList>

    @GET("feed/home/{bucketid}")
    @AccessScope(FEED)
    fun getBucket(
        @Path("bucketid") bucketId: String,
        @Query("mature_content") matureContent: Boolean,
        @Query("offset") @IntRange(from = 0, to = 50000) offset: Int,
        @Query("limit") @IntRange(from = 1, to = 120) limit: Int
    ): Single<PagedListResult<DeviationDto>>

    @GET("feed/settings")
    @AccessScope(FEED)
    fun getSettings(): Single<FeedSettingsDto>

    @POST("feed/settings/update")
    @FormUrlEncoded
    @AccessScope(FEED)
    fun updateSettings(@FieldMap include: Map<String, Boolean>): Completable
}
