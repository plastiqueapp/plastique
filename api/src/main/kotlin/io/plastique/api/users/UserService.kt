package io.plastique.api.users

import io.plastique.api.common.AccessScope
import io.reactivex.Single
import retrofit2.http.FieldMap
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface UserService {
    @GET("user/profile/{username}")
    fun getUserProfile(@Path("username") username: String): Single<UserProfile>

    @GET("user/whoami")
    @AccessScope("user")
    fun whoami(@Query("access_token") accessToken: String): Single<User>

    @POST("user/whois")
    @FormUrlEncoded
    fun whois(@FieldMap usernames: Map<String, String>): Single<WhoisResult>
}
