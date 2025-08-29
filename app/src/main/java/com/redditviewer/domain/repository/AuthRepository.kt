package com.redditviewer.domain.repository

import com.redditviewer.domain.model.AuthState
import com.redditviewer.domain.model.RedditUser
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val authState: Flow<AuthState>
    
    suspend fun startOAuthFlow(): String
    suspend fun handleOAuthCallback(code: String): Result<Unit>
    suspend fun getCurrentUser(): Result<RedditUser>
    suspend fun refreshToken(): Result<Unit>
    suspend fun logout()
    
    fun isTokenExpired(): Boolean
} 