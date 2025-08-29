package com.redditviewer.presentation.postdetail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.redditviewer.R
import com.redditviewer.domain.model.Post
import com.redditviewer.presentation.components.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostDetailScreen(
    post: Post,
    onBackClick: () -> Unit,
    viewModel: PostDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val currentPost by viewModel.post.collectAsState()
    val comments by viewModel.comments.collectAsState()

    // 初回ロード
    LaunchedEffect(post.id) {
        viewModel.loadPostAndComments(post)
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Top App Bar
        TopAppBar(
            title = {
                Text(
                    text = stringResource(R.string.post_comments),
                    fontWeight = FontWeight.Bold
                )
            },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "戻る"
                    )
                }
            },
            actions = {
                // Refresh Button
                IconButton(onClick = { viewModel.refreshComments() }) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = stringResource(R.string.refresh)
                    )
                }
            }
        )

        // Content
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 投稿詳細
            item {
                PostDetailCard(
                    post = currentPost ?: post,
                    showTranslation = uiState.showPostTranslation,
                    isTranslating = uiState.isTranslatingPost,
                    onToggleTranslation = { viewModel.togglePostTranslation() }
                )
            }

            // コメント区切り
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.post_comments),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Text(
                        text = "(${comments.size}件)",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    if (uiState.isTranslatingComments) {
                        Spacer(modifier = Modifier.width(8.dp))
                        TranslationIndicator(isTranslating = true)
                    }
                }
                
                Divider(
                    modifier = Modifier.padding(top = 8.dp),
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                )
            }

            // コメント状態
            when {
                uiState.isLoadingComments -> {
                    item {
                        LoadingIndicator(
                            message = stringResource(R.string.comments_loading)
                        )
                    }
                }
                
                uiState.commentsError != null -> {
                    item {
                        ErrorMessage(
                            message = uiState.commentsError,
                            onRetry = { viewModel.refreshComments() },
                            onDismiss = { viewModel.clearCommentsError() }
                        )
                    }
                }
                
                uiState.commentsEmpty -> {
                    item {
                        EmptyState(
                            message = stringResource(R.string.comments_empty),
                            icon = Icons.Default.Comment
                        )
                    }
                }
                
                else -> {
                    // コメント一覧
                    items(
                        items = comments,
                        key = { comment -> comment.id }
                    ) { comment ->
                        CommentCard(
                            comment = comment,
                            showTranslation = uiState.commentTranslationStates[comment.id] ?: true,
                            isExpanded = uiState.expandedComments[comment.id] ?: true,
                            onToggleTranslation = { 
                                viewModel.toggleCommentTranslation(comment.id) 
                            },
                            onToggleExpansion = { 
                                viewModel.toggleCommentExpansion(comment.id) 
                            },
                            onReplyClick = { 
                                // TODO: 返信機能の実装
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PostDetailCard(
    post: Post,
    showTranslation: Boolean,
    isTranslating: Boolean,
    onToggleTranslation: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // ヘッダー
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.post_subreddit, post.subreddit),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Text(
                    text = "•",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Text(
                    text = stringResource(R.string.post_author, post.author),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.weight(1f))
                
                Text(
                    text = formatDetailTime(post.created),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // タイトル
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    val titleText = if (showTranslation && !post.titleTranslated.isNullOrBlank()) {
                        post.titleTranslated
                    } else {
                        post.title
                    }
                    
                    Text(
                        text = titleText,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    
                    if (isTranslating) {
                        Spacer(modifier = Modifier.height(8.dp))
                        TranslationIndicator(isTranslating = true)
                    }
                }
                
                // 翻訳切り替えボタン
                if (!post.titleTranslated.isNullOrBlank()) {
                    IconButton(
                        onClick = onToggleTranslation
                    ) {
                        Icon(
                            imageVector = if (showTranslation) 
                                Icons.Default.Translate 
                            else 
                                Icons.Default.Language,
                            contentDescription = if (showTranslation) 
                                stringResource(R.string.show_original) 
                            else 
                                stringResource(R.string.show_translation),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
            
            // 本文
            if (!post.selftext.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(16.dp))
                
                val bodyText = if (showTranslation && !post.selftextTranslated.isNullOrBlank()) {
                    post.selftextTranslated
                } else {
                    post.selftext
                }
                
                Text(
                    text = bodyText,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            
            // メディア
            post.url?.let { url ->
                if (post.isImage && url.startsWith("http")) {
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(url)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Post image",
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.FitWidth
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // フッター統計
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // アップボート
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowUpward,
                        contentDescription = stringResource(R.string.post_upvotes),
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = formatScore(post.score),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                Spacer(modifier = Modifier.width(24.dp))
                
                // コメント数
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Comment,
                        contentDescription = stringResource(R.string.post_comments),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = post.numComments.toString(),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Spacer(modifier = Modifier.weight(1f))
                
                // メディアタイプ
                if (post.isVideo) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Video",
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(20.dp)
                    )
                } else if (post.isImage) {
                    Icon(
                        imageVector = Icons.Default.Image,
                        contentDescription = "Image",
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

private fun formatDetailTime(timestamp: Long): String {
    val format = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault())
    return format.format(Date(timestamp))
}

private fun formatScore(score: Int): String {
    return when {
        score < 1000 -> score.toString()
        score < 10000 -> "${score / 1000}.${(score % 1000) / 100}k"
        else -> "${score / 1000}k"
    }
} 