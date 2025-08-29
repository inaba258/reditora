package com.redditviewer.data.repository

import android.util.Base64
import com.redditviewer.BuildConfig
import com.redditviewer.data.local.AuthPreferences
import com.redditviewer.data.remote.RedditAuthApi
import com.redditviewer.domain.model.AuthState
import com.redditviewer.domain.model.RedditUser
import com.redditviewer.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.net.URLEncoder
import java.security.SecureRandom
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val authApi: RedditAuthApi,
    private val authPreferences: AuthPreferences
) : AuthRepository {

    companion object {
        private const val REDDIT_AUTH_URL = "https://www.reddit.com/api/v1/authorize.compact"
        private const val USER_AGENT = "android:com.redditviewer:v1.0.0 (by /u/your_username)"
        private const val SCOPE = "identity read history submit"
        private const val RESPONSE_TYPE = "code"
        private const val DURATION = "permanent"
    }

    private var currentState: String? = null

    override val authState: Flow<AuthState> = combine(
        authPreferences.accessToken,
        authPreferences.refreshToken,
        authPreferences.username,
        authPreferences.expiresAt
    ) { accessToken, refreshToken, username, expiresAt ->
        AuthState(
            isAuthenticated = !accessToken.isNullOrEmpty() && !isTokenExpired(expiresAt),
            accessToken = accessToken,
            refreshToken = refreshToken,
            username = username,
            expiresAt = expiresAt
        )
    }

    override suspend fun startOAuthFlow(): String {
        currentState = generateRandomState()
        
        val params = mapOf(
            "client_id" to BuildConfig.REDDIT_CLIENT_ID,
            "response_type" to RESPONSE_TYPE,
            "state" to currentState!!,
            "redirect_uri" to BuildConfig.REDDIT_REDIRECT_URI,
            "duration" to DURATION,
            "scope" to SCOPE
        )
        
        val queryString = params.map { "${it.key}=${URLEncoder.encode(it.value, "UTF-8")}" }
            .joinToString("&")
        
        return "$REDDIT_AUTH_URL?$queryString"
    }

    override suspend fun handleOAuthCallback(code: String): Result<Unit> {
        return try {
            val basicAuth = createBasicAuthHeader()
            
            val response = authApi.getAccessToken(
                authorization = basicAuth,
                userAgent = USER_AGENT,
                grantType = "authorization_code",
                code = code,
                redirectUri = BuildConfig.REDDIT_REDIRECT_URI
            )
            
            if (response.isSuccessful) {
                val tokenResponse = response.body()!!
                val expiresAt = System.currentTimeMillis() + (tokenResponse.expiresIn * 1000L)
                
                // ユーザー情報を取得
                val userResponse = authApi.getCurrentUser(
                    authorization = "bearer ${tokenResponse.accessToken}",
                    userAgent = USER_AGENT
                )
                
                if (userResponse.isSuccessful) {
                    val user = userResponse.body()!!
                    
                    authPreferences.saveAuthData(
                        accessToken = tokenResponse.accessToken,
                        refreshToken = tokenResponse.refreshToken,
                        username = user.name,
                        expiresAt = expiresAt
                    )
                    
                    Result.success(Unit)
                } else {
                    Result.failure(Exception("Failed to get user info"))
                }
            } else {
                Result.failure(Exception("Authentication failed: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getCurrentUser(): Result<RedditUser> {
        return try {
            val token = authPreferences.accessToken
            // TODO: 実装を完了する
            Result.failure(Exception("Not implemented"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun refreshToken(): Result<Unit> {
        return try {
            // TODO: リフレッシュトークンの実装
            Result.failure(Exception("Not implemented"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun logout() {
        authPreferences.clearAuthData()
    }

    override fun isTokenExpired(): Boolean {
        // TODO: 現在時刻と比較する実装
        return false
    }
    
    private fun isTokenExpired(expiresAt: Long): Boolean {
        return System.currentTimeMillis() >= expiresAt
    }

    private fun generateRandomState(): String {
        val random = SecureRandom()
        val bytes = ByteArray(32)
        random.nextBytes(bytes)
        return Base64.encodeToString(bytes, Base64.URL_SAFE or Base64.NO_WRAP)
    }

    private fun createBasicAuthHeader(): String {
        val credentials = "${BuildConfig.REDDIT_CLIENT_ID}:"
        val encoded = Base64.encodeToString(credentials.toByteArray(), Base64.NO_WRAP)
        return "Basic $encoded"
    }
} 