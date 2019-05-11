package io.plastique.api.watch

import androidx.annotation.IntRange
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
    fun getWatchers(
        @Query("offset") offset: Int,
        @Query("limit") @IntRange(from = 1, to = 50) limit: Int
    ): Single<PagedListResult<WatcherDto>>

    @GET("user/watchers/{username}")
    fun getWatchers(
        @Path("username") username: String,
        @Query("offset") offset: Int,
        @Query("limit") @IntRange(from = 1, to = 50) limit: Int
    ): Single<PagedListResult<WatcherDto>>

    @POST("user/friends/watch/{username}")
    @FormUrlEncoded
    fun watch(
        @Path("username") username: String,
        @FieldMap params: Map<String, Boolean>
    ): Completable

    @GET("user/friends/unwatch/{username}")
    fun unwatch(@Path("username") username: String): Completable

    @GET("user/friends/watching/{username}")
    fun isWatching(@Path("username") username: String): Single<IsWatchingResult>
}
