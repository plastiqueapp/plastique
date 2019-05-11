package io.plastique.api.comments

import androidx.annotation.IntRange
import io.plastique.api.common.AccessScope
import io.reactivex.Single
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface CommentService {
    @GET("comments/deviation/{deviationid}")
    fun getCommentsOnDeviation(
        @Path("deviationid") deviationId: String,
        @Query("commentid") parentCommentId: String?,
        @Query("maxdepth") @IntRange(from = 0, to = 5) maxDepth: Int,
        @Query("offset") offset: Int,
        @Query("limit") @IntRange(from = 1, to = 50) limit: Int
    ): Single<CommentList>

    @GET("comments/profile/{username}")
    fun getCommentsOnProfile(
        @Path("username") username: String,
        @Query("commentid") parentCommentId: String?,
        @Query("maxdepth") @IntRange(from = 0, to = 5) maxDepth: Int,
        @Query("offset") offset: Int,
        @Query("limit") @IntRange(from = 1, to = 50) limit: Int
    ): Single<CommentList>

    @GET("comments/status/{statusid}")
    fun getCommentsOnStatus(
        @Path("statusid") statusId: String,
        @Query("commentid") parentCommentId: String?,
        @Query("maxdepth") @IntRange(from = 0, to = 5) maxDepth: Int,
        @Query("offset") offset: Int,
        @Query("limit") @IntRange(from = 1, to = 50) limit: Int
    ): Single<CommentList>

    @POST("comments/post/deviation/{deviationid}")
    @FormUrlEncoded
    @AccessScope("comment.post")
    fun postCommentOnDeviation(
        @Path("deviationid") deviationId: String,
        @Query("commentid") parentCommentId: String?,
        @Field("body") text: String
    ): Single<CommentDto>

    @POST("comments/post/profile/{username}")
    @FormUrlEncoded
    @AccessScope("comment.post")
    fun postCommentOnProfile(
        @Path("username") username: String,
        @Query("commentid") parentCommentId: String?,
        @Field("body") text: String
    ): Single<CommentDto>

    @POST("comments/post/status/{statusid}")
    @FormUrlEncoded
    @AccessScope("comment.post")
    fun postCommentOnStatus(
        @Path("statusid") statusId: String,
        @Query("commentid") parentCommentId: String?,
        @Field("body") text: String
    ): Single<CommentDto>
}
