package io.plastique.api.watch

import androidx.annotation.IntRange
import io.plastique.api.common.AccessScope
import io.plastique.api.common.PagedListResult
import io.reactivex.Completable
import io.reactivex.Single
import retrofit2.http.FieldMap
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface WatchService {
    @GET("user/watchers")
    @AccessScope("browse")
    fun getWatchers(
        @Query("offset") @IntRange(from = 0, to = 50000) offset: Int,
        @Query("limit") @IntRange(from = 1, to = 50) limit: Int
    ): Single<PagedListResult<WatcherDto>>

    @GET("user/watchers/{username}")
    @AccessScope("browse")
    fun getWatchers(
        @Path("username") username: String,
        @Query("offset") @IntRange(from = 0, to = 50000) offset: Int,
        @Query("limit") @IntRange(from = 1, to = 50) limit: Int
    ): Single<PagedListResult<WatcherDto>>

    @POST("user/friends/watch/{username}")
    @FormUrlEncoded
    @AccessScope("browse", "user.manage")
    fun watch(
        @Path("username") username: String,
        @FieldMap params: Map<String, Boolean>
    ): Completable

    @GET("user/friends/unwatch/{username}")
    @AccessScope("browse", "user.manage")
    fun unwatch(@Path("username") username: String): Completable

    @GET("user/friends/watching/{username}")
    @AccessScope("browse", "user")
    fun isWatching(@Path("username") username: String): Single<IsWatchingResult>
}
