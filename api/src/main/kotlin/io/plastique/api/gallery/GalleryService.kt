package io.plastique.api.gallery

import androidx.annotation.IntRange
import io.plastique.api.common.AccessScope
import io.plastique.api.common.PagedListResult
import io.plastique.api.deviations.Deviation
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
    fun getAllContents(
        @Query("username") username: String?,
        @Query("offset") @IntRange(from = 0, to = 50000) offset: Int,
        @Query("limit") @IntRange(from = 1, to = 24) limit: Int,
        @Query("mature_content") matureContent: Boolean): Single<PagedListResult<Deviation>>

    @GET("gallery/folders?calculate_size=true")
    fun getFolders(
        @Query("username") username: String?,
        @Query("offset") @IntRange(from = 0, to = 50000) offset: Int,
        @Query("limit") @IntRange(from = 1, to = 50) limit: Int,
        @Query("ext_preload") preload: Boolean,
        @Query("mature_content") matureContent: Boolean): Single<PagedListResult<Folder>>

    @GET("gallery/{folderid}")
    fun getFolderContents(
        @Path("folderid") folderId: String?,
        @Query("username") username: String?,
        @Query("mode") order: SortOrder? = null,
        @Query("offset") @IntRange(from = 0, to = 50000) offset: Int,
        @Query("limit") @IntRange(from = 1, to = 24) limit: Int,
        @Query("mature_content") matureContent: Boolean): Single<PagedListResult<Deviation>>

    @POST("gallery/folders/create")
    @FormUrlEncoded
    @AccessScope("gallery")
    fun createFolder(@Field("folder") name: String): Single<CreateFolderResult>

    @GET("gallery/folders/remove/{folderid}")
    @AccessScope("gallery")
    fun removeFolder(@Path("folderid") folderId: String): Completable
}
