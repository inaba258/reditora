package com.redditviewer.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.redditviewer.R
import com.redditviewer.domain.model.Comment
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun CommentCard(
    comment: Comment,
    showTranslation: Boolean = true,
    isExpanded: Boolean = true,
    onToggleTranslation: () -> Unit = {},
    onToggleExpansion: () -> Unit = {},
    onReplyClick: () -> Unit = {},
    modifier: Modifier = Modifier,
    depth: Int = 0
) {
    val indentWidth = (depth * 12).dp
    val maxDepth = 8
    val actualDepth = minOf(depth, maxDepth)
    
    Column(
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            // インデント表示
            if (actualDepth > 0) {
                repeat(actualDepth) { level ->
                    Box(
                        modifier = Modifier
                            .width(2.dp)
                            .height(if (level == actualDepth - 1) 60.dp else 80.dp)
                            .padding(start = if (level == 0) 0.dp else 8.dp)
                            .background(
                                color = getIndentColor(level),
                                shape = RoundedCornerShape(1.dp)
                            )
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
            }
            
            // コメント本体
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggleExpansion() },
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (actualDepth % 2 == 0) 
                        MaterialTheme.colorScheme.surface 
                    else 
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                ) {
                    // ヘッダー（作者、スコア、時間）
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = comment.author,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        if (!comment.isScoreHidden) {
                            Text(
                                text = stringResource(R.string.comment_score, comment.score),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        
                        Text(
                            text = formatCommentTime(comment.created),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        Spacer(modifier = Modifier.weight(1f))
                        
                        // 展開/折りたたみボタン
                        if (comment.replies.isNotEmpty()) {
                            IconButton(
                                onClick = onToggleExpansion,
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = if (isExpanded) 
                                        Icons.Default.ExpandLess 
                                    else 
                                        Icons.Default.ExpandMore,
                                    contentDescription = if (isExpanded) "折りたたむ" else "展開",
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // コメント本文
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            val bodyText = if (showTranslation && !comment.bodyTranslated.isNullOrBlank()) {
                                comment.bodyTranslated
                            } else {
                                comment.body
                            }
                            
                            Text(
                                text = bodyText,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        
                        // 翻訳切り替えボタン
                        if (!comment.bodyTranslated.isNullOrBlank()) {
                            IconButton(
                                onClick = onToggleTranslation,
                                modifier = Modifier.size(28.dp)
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
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                    
                    // アクションボタン
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 返信ボタン
                        TextButton(
                            onClick = onReplyClick,
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Reply,
                                contentDescription = stringResource(R.string.comment_reply),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = stringResource(R.string.comment_reply),
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                        
                        if (comment.replies.isNotEmpty()) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "${comment.replies.size}件の返信",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        
                        if (comment.isStickied) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.Default.PushPin,
                                contentDescription = "固定コメント",
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }
        }
        
        // 返信コメント（再帰的に表示）
        if (isExpanded && comment.replies.isNotEmpty()) {
            Spacer(modifier = Modifier.height(4.dp))
            
            comment.replies.forEach { reply ->
                CommentCard(
                    comment = reply,
                    showTranslation = showTranslation,
                    isExpanded = true, // 子コメントのデフォルト状態
                    onToggleTranslation = onToggleTranslation,
                    onToggleExpansion = { },
                    onReplyClick = { },
                    depth = depth + 1,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun getIndentColor(level: Int): Color {
    val colors = listOf(
        MaterialTheme.colorScheme.primary,
        MaterialTheme.colorScheme.secondary,
        MaterialTheme.colorScheme.tertiary,
        MaterialTheme.colorScheme.error,
        MaterialTheme.colorScheme.outline,
        MaterialTheme.colorScheme.primaryContainer,
        MaterialTheme.colorScheme.secondaryContainer,
        MaterialTheme.colorScheme.tertiaryContainer
    )
    return colors[level % colors.size].copy(alpha = 0.6f)
}

private fun formatCommentTime(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    
    return when {
        diff < 60_000 -> "今"
        diff < 3600_000 -> "${diff / 60_000}分前"
        diff < 86400_000 -> "${diff / 3600_000}時間前"
        diff < 604800_000 -> "${diff / 86400_000}日前"
        else -> {
            val format = SimpleDateFormat("MM/dd HH:mm", Locale.getDefault())
            format.format(Date(timestamp))
        }
    }
} 