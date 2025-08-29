package com.redditviewer.data.remote

import com.redditviewer.data.remote.dto.AuthTokenResponse
import com.redditviewer.data.remote.dto.RedditUserResponse
import retrofit2.Response
import retrofit2.http.*

interface RedditAuthApi {
    
    @FormUrlEncoded
    @POST("api/v1/access_token")
    suspend fun getAccessToken(
        @Header("Authorization") authorization: String,
        @Header("User-Agent") userAgent: String,
        @Field("grant_type") grantType: String,
        @Field("code") code: String,
        @Field("redirect_uri") redirectUri: String
    ): Response<AuthTokenResponse>
    
    @FormUrlEncoded
    @POST("api/v1/access_token")
    suspend fun refreshAccessToken(
        @Header("Authorization") authorization: String,
        @Header("User-Agent") userAgent: String,
        @Field("grant_type") grantType: String = "refresh_token",
        @Field("refresh_token") refreshToken: String
    ): Response<AuthTokenResponse>
    
    @GET("api/v1/me")
    suspend fun getCurrentUser(
        @Header("Authorization") authorization: String,
        @Header("User-Agent") userAgent: String
    ): Response<RedditUserResponse>
} 