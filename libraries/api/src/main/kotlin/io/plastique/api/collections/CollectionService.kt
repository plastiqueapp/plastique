package io.plastique.api.collections

import androidx.annotation.IntRange
import io.plastique.api.common.AccessScope
import io.plastique.api.common.AccessScopes.BROWSE
import io.plastique.api.common.AccessScopes.COLLECTION
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

interface CollectionService {
    @GET("collections/folders?calculate_size=true")
    @AccessScope(BROWSE)
    fun getFolders(
        @Query("username") username: String?,
        @Query("ext_preload") preload: Boolean,
        @Query("mature_content") matureContent: Boolean,
        @Query("offset") @IntRange(from = 0, to = 50000) offset: Int,
        @Query("limit") @IntRange(from = 1, to = 50) limit: Int
    ): Single<PagedListResult<FolderDto>>

    @GET("collections/{folderid}")
    @AccessScope(BROWSE)
    fun getFolderContents(
        @Path("folderid") folderId: String,
        @Query("username") username: String?,
        @Query("mature_content") matureContent: Boolean,
        @Query("offset") @IntRange(from = 0, to = 50000) offset: Int,
        @Query("limit") @IntRange(from = 1, to = 24) limit: Int
    ): Single<PagedListResult<DeviationDto>>

    @POST("collections/fave")
    @FormUrlEncoded
    @AccessScope(BROWSE, COLLECTION)
    fun addToFolder(
        @Field("deviationid") deviationId: String,
        @Field("folderid[0]") folderId: String?
    ): Single<AddRemoveFavoriteResult>

    @POST("collections/unfave")
    @FormUrlEncoded
    @AccessScope(BROWSE, COLLECTION)
    fun removeFromFolder(
        @Field("deviationid") deviationId: String,
        @Field("folderid[0]") folderId: String?
    ): Single<AddRemoveFavoriteResult>

    @POST("collections/folders/create")
    @FormUrlEncoded
    @AccessScope(BROWSE, COLLECTION)
    fun createFolder(@Field("folder") folderName: String): Single<FolderDto>

    @GET("collections/folders/remove/{folderid}")
    @AccessScope(BROWSE, COLLECTION)
    fun removeFolder(@Path("folderid") folderId: String): Completable
}
