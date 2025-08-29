package com.redditviewer.domain.repository

import com.redditviewer.domain.model.*
import kotlinx.coroutines.flow.Flow

interface RedditRepository {
    suspend fun getHomeFeed(after: String? = null): Result<List<Post>>
    suspend fun getHotPosts(subreddit: String, after: String? = null): Result<List<Post>>
    suspend fun getNewPosts(subreddit: String, after: String? = null): Result<List<Post>>
    suspend fun getTopPosts(subreddit: String, time: String = "day", after: String? = null): Result<List<Post>>
    
    suspend fun getPostComments(subreddit: String, postId: String): Result<List<Comment>>
    
    suspend fun getSubredditInfo(subreddit: String): Result<Subreddit>
    suspend fun searchSubreddits(query: String): Result<List<Subreddit>>
    
    suspend fun translateText(text: String): Result<String>
    
    // キャッシュされた翻訳結果を管理
    fun getCachedTranslation(originalText: String): String?
    suspend fun cacheTranslation(originalText: String, translatedText: String)
}

interface TranslationRepository {
    suspend fun translateText(text: String, source: String = "en", target: String = "ja"): Result<String>
    fun getCachedTranslation(text: String): String?
    suspend fun cacheTranslation(originalText: String, translatedText: String)
} 