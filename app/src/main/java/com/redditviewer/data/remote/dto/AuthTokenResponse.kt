package com.redditviewer.data.remote.dto

import com.google.gson.annotations.SerializedName

data class AuthTokenResponse(
    @SerializedName("access_token")
    val accessToken: String,
    @SerializedName("token_type")
    val tokenType: String,
    @SerializedName("expires_in")
    val expiresIn: Int,
    @SerializedName("scope")
    val scope: String,
    @SerializedName("refresh_token")
    val refreshToken: String?
)

data class RedditUserResponse(
    @SerializedName("id")
    val id: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("total_karma")
    val totalKarma: Int,
    @SerializedName("link_karma")
    val linkKarma: Int,
    @SerializedName("comment_karma")
    val commentKarma: Int,
    @SerializedName("created_utc")
    val createdUtc: Long,
    @SerializedName("icon_img")
    val iconImg: String?,
    @SerializedName("is_employee")
    val isEmployee: Boolean = false,
    @SerializedName("is_gold")
    val isGold: Boolean = false,
    @SerializedName("is_premium")
    val isPremium: Boolean = false
) 