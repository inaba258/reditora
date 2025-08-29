package com.redditviewer.data.remote

import com.redditviewer.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.*

interface RedditApi {
    
    @GET("r/{subreddit}/hot.json")
    suspend fun getHotPosts(
        @Path("subreddit") subreddit: String,
        @Header("Authorization") authorization: String,
        @Header("User-Agent") userAgent: String,
        @Query("limit") limit: Int = 25,
        @Query("after") after: String? = null
    ): Response<RedditListingResponse<PostResponse>>
    
    @GET("r/{subreddit}/new.json")
    suspend fun getNewPosts(
        @Path("subreddit") subreddit: String,
        @Header("Authorization") authorization: String,
        @Header("User-Agent") userAgent: String,
        @Query("limit") limit: Int = 25,
        @Query("after") after: String? = null
    ): Response<RedditListingResponse<PostResponse>>
    
    @GET("r/{subreddit}/top.json")
    suspend fun getTopPosts(
        @Path("subreddit") subreddit: String,
        @Header("Authorization") authorization: String,
        @Header("User-Agent") userAgent: String,
        @Query("limit") limit: Int = 25,
        @Query("after") after: String? = null,
        @Query("t") time: String = "day" // hour, day, week, month, year, all
    ): Response<RedditListingResponse<PostResponse>>
    
    @GET("r/{subreddit}/comments/{postId}.json")
    suspend fun getPostComments(
        @Path("subreddit") subreddit: String,
        @Path("postId") postId: String,
        @Header("Authorization") authorization: String,
        @Header("User-Agent") userAgent: String,
        @Query("sort") sort: String = "best", // confidence, top, new, controversial, old, random, qa, live
        @Query("limit") limit: Int = 100
    ): Response<List<RedditListingResponse<Any>>>
    
    @GET("r/{subreddit}/about.json")
    suspend fun getSubredditInfo(
        @Path("subreddit") subreddit: String,
        @Header("Authorization") authorization: String,
        @Header("User-Agent") userAgent: String
    ): Response<RedditThingWrapper<SubredditResponse>>
    
    @GET("subreddits/search.json")
    suspend fun searchSubreddits(
        @Query("q") query: String,
        @Header("Authorization") authorization: String,
        @Header("User-Agent") userAgent: String,
        @Query("limit") limit: Int = 25,
        @Query("type") type: String = "sr"
    ): Response<RedditListingResponse<SubredditResponse>>
    
    // デフォルトのフロントページ（認証ユーザーのホームフィード）
    @GET("hot.json")
    suspend fun getHomeFeed(
        @Header("Authorization") authorization: String,
        @Header("User-Agent") userAgent: String,
        @Query("limit") limit: Int = 25,
        @Query("after") after: String? = null
    ): Response<RedditListingResponse<PostResponse>>
} 