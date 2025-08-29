package com.redditviewer.data.remote.dto

import com.google.gson.annotations.SerializedName

data class RedditListingResponse<T>(
    @SerializedName("kind")
    val kind: String,
    @SerializedName("data")
    val data: RedditListingData<T>
)

data class RedditListingData<T>(
    @SerializedName("children")
    val children: List<RedditThingWrapper<T>>,
    @SerializedName("after")
    val after: String?,
    @SerializedName("before")
    val before: String?,
    @SerializedName("modhash")
    val modhash: String?,
    @SerializedName("dist")
    val dist: Int?
)

data class RedditThingWrapper<T>(
    @SerializedName("kind")
    val kind: String,
    @SerializedName("data")
    val data: T
)

data class PostResponse(
    @SerializedName("id")
    val id: String,
    @SerializedName("title")
    val title: String,
    @SerializedName("author")
    val author: String,
    @SerializedName("subreddit")
    val subreddit: String,
    @SerializedName("selftext")
    val selftext: String?,
    @SerializedName("url")
    val url: String?,
    @SerializedName("thumbnail")
    val thumbnail: String?,
    @SerializedName("score")
    val score: Int,
    @SerializedName("num_comments")
    val numComments: Int,
    @SerializedName("created_utc")
    val createdUtc: Long,
    @SerializedName("is_video")
    val isVideo: Boolean = false,
    @SerializedName("domain")
    val domain: String?,
    @SerializedName("permalink")
    val permalink: String,
    @SerializedName("stickied")
    val stickied: Boolean = false,
    @SerializedName("over_18")
    val over18: Boolean = false,
    @SerializedName("post_hint")
    val postHint: String?
)

data class CommentResponse(
    @SerializedName("id")
    val id: String,
    @SerializedName("author")
    val author: String?,
    @SerializedName("body")
    val body: String?,
    @SerializedName("score")
    val score: Int?,
    @SerializedName("created_utc")
    val createdUtc: Long,
    @SerializedName("depth")
    val depth: Int = 0,
    @SerializedName("parent_id")
    val parentId: String?,
    @SerializedName("replies")
    val replies: RedditListingResponse<CommentResponse>?,
    @SerializedName("stickied")
    val stickied: Boolean = false,
    @SerializedName("score_hidden")
    val scoreHidden: Boolean = false
)

data class SubredditResponse(
    @SerializedName("name")
    val name: String,
    @SerializedName("display_name")
    val displayName: String,
    @SerializedName("title")
    val title: String,
    @SerializedName("public_description")
    val publicDescription: String?,
    @SerializedName("description")
    val description: String?,
    @SerializedName("subscribers")
    val subscribers: Int,
    @SerializedName("icon_img")
    val iconImg: String?,
    @SerializedName("banner_img")
    val bannerImg: String?,
    @SerializedName("over18")
    val over18: Boolean = false
) 