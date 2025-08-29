package com.redditviewer.data.mapper

import com.redditviewer.data.remote.dto.*
import com.redditviewer.domain.model.*

fun PostResponse.toDomain(): Post {
    return Post(
        id = id,
        title = title,
        author = author,
        subreddit = subreddit,
        selftext = selftext?.takeIf { it.isNotBlank() },
        url = url,
        thumbnail = thumbnail?.takeIf { it.startsWith("http") },
        score = score,
        numComments = numComments,
        created = createdUtc * 1000L, // Convert to milliseconds
        isVideo = isVideo,
        isImage = postHint == "image" || domain?.contains("i.redd.it") == true,
        isGif = postHint == "image" && (url?.contains(".gif") == true || domain?.contains("gfycat") == true),
        domain = domain,
        permalink = permalink,
        isStickied = stickied,
        isNsfw = over18
    )
}

fun CommentResponse.toDomain(): Comment? {
    // Skip deleted comments or comments without body
    if (author == null || body.isNullOrBlank() || body == "[deleted]" || body == "[removed]") {
        return null
    }
    
    return Comment(
        id = id,
        author = author,
        body = body,
        score = score ?: 0,
        created = createdUtc * 1000L,
        depth = depth,
        parentId = parentId,
        replies = replies?.data?.children?.mapNotNull { it.data.toDomain() } ?: emptyList(),
        isStickied = stickied,
        isScoreHidden = scoreHidden
    )
}

fun SubredditResponse.toDomain(): Subreddit {
    return Subreddit(
        name = name,
        displayName = displayName,
        title = title,
        description = description,
        subscribers = subscribers,
        iconImg = iconImg?.takeIf { it.isNotBlank() },
        bannerImg = bannerImg?.takeIf { it.isNotBlank() },
        isNsfw = over18,
        publicDescription = publicDescription
    )
}

fun RedditUserResponse.toDomain(): RedditUser {
    return RedditUser(
        id = id,
        name = name,
        totalKarma = totalKarma,
        linkKarma = linkKarma,
        commentKarma = commentKarma,
        createdUtc = createdUtc * 1000L,
        iconImg = iconImg?.takeIf { it.isNotBlank() },
        isEmployee = isEmployee,
        isGold = isGold,
        isPremium = isPremium
    )
} 