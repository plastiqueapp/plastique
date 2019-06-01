package io.plastique.api.gallery

import androidx.annotation.IntRange
import io.plastique.api.common.AccessScope
import io.plastique.api.common.PagedListResult
import io.plastique.api.deviations.DeviationDto
import io.reactivex.Completable
import io.reactivex.Single
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface GalleryService {
    @GET("gallery/all")
    @AccessScope("browse")
    fun getAllContents(
        @Query("username") username: String?,
        @Query("mature_content") matureContent: Boolean,
        @Query("offset") @IntRange(from = 0, to = 50000) offset: Int,
        @Query("limit") @IntRange(from = 1, to = 24) limit: Int
    ): Single<PagedListResult<DeviationDto>>

    @GET("gallery/folders?calculate_size=true")
    @AccessScope("browse")
    fun getFolders(
        @Query("username") username: String?,
        @Query("ext_preload") preload: Boolean,
        @Query("mature_content") matureContent: Boolean,
        @Query("offset") @IntRange(from = 0, to = 50000) offset: Int,
        @Query("limit") @IntRange(from = 1, to = 50) limit: Int
    ): Single<PagedListResult<FolderDto>>

    @GET("gallery/{folderid}")
    @AccessScope("browse")
    @Suppress("LongParameterList")
    fun getFolderContents(
        @Path("folderid") folderId: String?,
        @Query("username") username: String?,
        @Query("mode") mode: String? = null,
        @Query("mature_content") matureContent: Boolean,
        @Query("offset") @IntRange(from = 0, to = 50000) offset: Int,
        @Query("limit") @IntRange(from = 1, to = 24) limit: Int
    ): Single<PagedListResult<DeviationDto>>

    @POST("gallery/folders/create")
    @FormUrlEncoded
    @AccessScope("browse", "gallery")
    fun createFolder(@Field("folder") name: String): Single<FolderDto>

    @GET("gallery/folders/remove/{folderid}")
    @AccessScope("browse", "gallery")
    fun removeFolder(@Path("folderid") folderId: String): Completable
}
