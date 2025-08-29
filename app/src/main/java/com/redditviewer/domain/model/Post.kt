package com.redditviewer.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Post(
    val id: String,
    val title: String,
    val titleTranslated: String? = null,
    val author: String,
    val subreddit: String,
    val selftext: String? = null,
    val selftextTranslated: String? = null,
    val url: String? = null,
    val thumbnail: String? = null,
    val score: Int,
    val numComments: Int,
    val created: Long,
    val isVideo: Boolean = false,
    val isImage: Boolean = false,
    val isGif: Boolean = false,
    val domain: String? = null,
    val permalink: String,
    val isStickied: Boolean = false,
    val isNsfw: Boolean = false
) : Parcelable

@Parcelize
data class Comment(
    val id: String,
    val author: String,
    val body: String,
    val bodyTranslated: String? = null,
    val score: Int,
    val created: Long,
    val depth: Int,
    val parentId: String? = null,
    val replies: List<Comment> = emptyList(),
    val isStickied: Boolean = false,
    val isScoreHidden: Boolean = false
) : Parcelable

@Parcelize
data class Subreddit(
    val name: String,
    val displayName: String,
    val title: String,
    val description: String? = null,
    val subscribers: Int,
    val iconImg: String? = null,
    val bannerImg: String? = null,
    val isNsfw: Boolean = false,
    val publicDescription: String? = null
) : Parcelable 