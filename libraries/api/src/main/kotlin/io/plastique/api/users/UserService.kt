package io.plastique.api.users

import io.plastique.api.common.AccessScope
import io.plastique.api.common.AccessScopes.BROWSE
import io.plastique.api.common.AccessScopes.USER
import io.plastique.api.common.ListResult
import io.reactivex.Single
import retrofit2.http.FieldMap
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface UserService {
    @GET("user/profile/{username}?expand=user.stats")
    @AccessScope(BROWSE)
    fun getUserProfile(@Path("username") username: String): Single<UserProfileDto>

    @GET("user/whoami")
    @AccessScope(USER)
    fun whoami(@Query("access_token") accessToken: String? = null): Single<UserDto>

    @POST("user/whois")
    @FormUrlEncoded
    @AccessScope(BROWSE)
    fun whois(@FieldMap usernames: Map<String, String>): Single<ListResult<UserDto>>
}
