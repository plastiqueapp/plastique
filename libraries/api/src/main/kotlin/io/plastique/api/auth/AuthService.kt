package io.plastique.api.auth

import io.reactivex.Single
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface AuthService {
    @GET("/oauth2/authorize?response_type=code")
    fun authorize(
        @Query("client_id") clientId: String,
        @Query("state") csrfToken: String,
        @Query("redirect_uri") redirectUri: String,
        @Query("scope") scope: String
    ): Call<Any>

    @GET("/oauth2/token?grant_type=client_credentials")
    fun requestAccessToken(
        @Query("client_id") clientId: String,
        @Query("client_secret") clientSecret: String
    ): Single<TokenResult>

    @GET("/oauth2/token?grant_type=authorization_code")
    fun requestAccessToken(
        @Query("client_id") clientId: String,
        @Query("client_secret") clientSecret: String,
        @Query("code") authCode: String,
        @Query("redirect_uri") redirectUri: String
    ): Single<TokenResult>

    @GET("/oauth2/token?grant_type=refresh_token")
    fun refreshAccessToken(
        @Query("client_id") clientId: String,
        @Query("client_secret") clientSecret: String,
        @Query("refresh_token") refreshToken: String
    ): Single<TokenResult>
}
