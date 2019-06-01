package io.plastique.api.messages

import androidx.annotation.IntRange
import io.plastique.api.common.AccessScope
import io.plastique.api.common.PagedListResult
import io.reactivex.Completable
import io.reactivex.Single
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface MessageService {
    @GET("messages/feed?stack=false")
    @AccessScope("message")
    fun getAllMessages(@Query("cursor") cursor: String?): Single<GetAllMessagesResult>

    @GET("messages/feedback/{stackid}")
    @AccessScope("message")
    fun getFeedbackMessagesFromStack(
        @Path("stackid") stackId: String,
        @Query("offset") @IntRange(from = 0, to = 50000) offset: Int,
        @Query("limit") @IntRange(from = 1, to = 50) limit: Int
    ): Single<PagedListResult<MessageDto>>

    @GET("messages/mentions/{stackid}")
    @AccessScope("message")
    fun getMentionsFromStack(
        @Path("stackid") stackId: String,
        @Query("offset") @IntRange(from = 0, to = 50000) offset: Int,
        @Query("limit") @IntRange(from = 1, to = 50) limit: Int
    ): Single<PagedListResult<MessageDto>>

    @POST("messages/delete")
    @FormUrlEncoded
    @AccessScope("message")
    fun deleteMessage(@Field("messageid") messageId: String): Completable
}
