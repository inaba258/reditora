package com.redditviewer.data.repository

import com.redditviewer.data.local.AuthPreferences
import com.redditviewer.data.mapper.toDomain
import com.redditviewer.data.remote.RedditApi
import com.redditviewer.data.remote.dto.CommentResponse
import com.redditviewer.data.remote.dto.PostResponse
import com.redditviewer.domain.model.*
import com.redditviewer.domain.repository.RedditRepository
import com.redditviewer.domain.repository.TranslationRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RedditRepositoryImpl @Inject constructor(
    private val redditApi: RedditApi,
    private val authPreferences: AuthPreferences,
    private val translationRepository: TranslationRepository
) : RedditRepository {

    companion object {
        private const val USER_AGENT = "android:com.redditviewer:v1.0.0 (by /u/your_username)"
    }

    private suspend fun getAuthHeader(): String {
        val token = authPreferences.accessToken.first()
        return "bearer $token"
    }

    override suspend fun getHomeFeed(after: String?): Result<List<Post>> {
        return try {
            val response = redditApi.getHomeFeed(
                authorization = getAuthHeader(),
                userAgent = USER_AGENT,
                after = after
            )
            
            if (response.isSuccessful) {
                val posts = response.body()?.data?.children
                    ?.map { it.data.toDomain() }
                    ?: emptyList()
                Result.success(posts)
            } else {
                Result.failure(Exception("Failed to fetch home feed: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getHotPosts(subreddit: String, after: String?): Result<List<Post>> {
        return try {
            val response = redditApi.getHotPosts(
                subreddit = subreddit,
                authorization = getAuthHeader(),
                userAgent = USER_AGENT,
                after = after
            )
            
            if (response.isSuccessful) {
                val posts = response.body()?.data?.children
                    ?.map { it.data.toDomain() }
                    ?: emptyList()
                Result.success(posts)
            } else {
                Result.failure(Exception("Failed to fetch hot posts: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getNewPosts(subreddit: String, after: String?): Result<List<Post>> {
        return try {
            val response = redditApi.getNewPosts(
                subreddit = subreddit,
                authorization = getAuthHeader(),
                userAgent = USER_AGENT,
                after = after
            )
            
            if (response.isSuccessful) {
                val posts = response.body()?.data?.children
                    ?.map { it.data.toDomain() }
                    ?: emptyList()
                Result.success(posts)
            } else {
                Result.failure(Exception("Failed to fetch new posts: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getTopPosts(subreddit: String, time: String, after: String?): Result<List<Post>> {
        return try {
            val response = redditApi.getTopPosts(
                subreddit = subreddit,
                authorization = getAuthHeader(),
                userAgent = USER_AGENT,
                time = time,
                after = after
            )
            
            if (response.isSuccessful) {
                val posts = response.body()?.data?.children
                    ?.map { it.data.toDomain() }
                    ?: emptyList()
                Result.success(posts)
            } else {
                Result.failure(Exception("Failed to fetch top posts: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getPostComments(subreddit: String, postId: String): Result<List<Comment>> {
        return try {
            val response = redditApi.getPostComments(
                subreddit = subreddit,
                postId = postId,
                authorization = getAuthHeader(),
                userAgent = USER_AGENT
            )
            
            if (response.isSuccessful) {
                val listings = response.body()
                // Reddit comments API returns [post_listing, comments_listing]
                val commentsListing = listings?.getOrNull(1)
                val comments = commentsListing?.data?.children
                    ?.mapNotNull { wrapper ->
                        (wrapper.data as? CommentResponse)?.toDomain()
                    } ?: emptyList()
                
                Result.success(comments)
            } else {
                Result.failure(Exception("Failed to fetch comments: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getSubredditInfo(subreddit: String): Result<Subreddit> {
        return try {
            val response = redditApi.getSubredditInfo(
                subreddit = subreddit,
                authorization = getAuthHeader(),
                userAgent = USER_AGENT
            )
            
            if (response.isSuccessful) {
                val subredditInfo = response.body()?.data?.toDomain()
                if (subredditInfo != null) {
                    Result.success(subredditInfo)
                } else {
                    Result.failure(Exception("Subreddit not found"))
                }
            } else {
                Result.failure(Exception("Failed to fetch subreddit info: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun searchSubreddits(query: String): Result<List<Subreddit>> {
        return try {
            val response = redditApi.searchSubreddits(
                query = query,
                authorization = getAuthHeader(),
                userAgent = USER_AGENT
            )
            
            if (response.isSuccessful) {
                val subreddits = response.body()?.data?.children
                    ?.map { it.data.toDomain() }
                    ?: emptyList()
                Result.success(subreddits)
            } else {
                Result.failure(Exception("Failed to search subreddits: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun translateText(text: String): Result<String> {
        return translationRepository.translateText(text)
    }

    override fun getCachedTranslation(originalText: String): String? {
        return translationRepository.getCachedTranslation(originalText)
    }

    override suspend fun cacheTranslation(originalText: String, translatedText: String) {
        translationRepository.cacheTranslation(originalText, translatedText)
    }
} 