package com.redditviewer.domain.model

data class AuthState(
    val isAuthenticated: Boolean = false,
    val accessToken: String? = null,
    val refreshToken: String? = null,
    val username: String? = null,
    val expiresAt: Long = 0L
)

data class RedditUser(
    val id: String,
    val name: String,
    val totalKarma: Int,
    val linkKarma: Int,
    val commentKarma: Int,
    val createdUtc: Long,
    val iconImg: String?,
    val isEmployee: Boolean = false,
    val isGold: Boolean = false,
    val isPremium: Boolean = false
) 