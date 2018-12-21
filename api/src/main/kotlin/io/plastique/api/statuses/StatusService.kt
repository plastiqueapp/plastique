package io.plastique.api.statuses

import androidx.annotation.IntRange
import io.plastique.api.common.AccessScope
import io.plastique.api.common.PagedListResult
import io.plastique.api.users.PostStatusResult
import io.plastique.api.users.StatusDto
import io.reactivex.Single
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface StatusService {
    @GET("user/statuses/")
    fun getStatuses(
        @Query("username") username: String,
        @Query("offset") offset: Int,
        @Query("limit") @IntRange(from = 1, to = 50) limit: Int,
        @Query("mature_content") matureContent: Boolean
    ): Single<PagedListResult<StatusDto>>

    @GET("user/statuses/{statusid}")
    fun getStatusById(
        @Path("statusid") statusId: String,
        @Query("mature_content") matureContent: Boolean
    ): Single<StatusDto>

    @POST("user/statuses/post")
    @FormUrlEncoded
    @AccessScope("user.manage")
    fun postStatus(
        @Field("body") body: String?,
        @Field("id") shareObjectId: String?,
        @Field("parentid") parentId: String?,
        @Field("stashid") stashId: String?
    ): Single<PostStatusResult>
}
