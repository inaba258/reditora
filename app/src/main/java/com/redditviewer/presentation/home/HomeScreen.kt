package com.redditviewer.presentation.home

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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.redditviewer.R
import com.redditviewer.domain.model.Post
import com.redditviewer.presentation.components.PostCard
import com.redditviewer.presentation.components.ErrorMessage
import com.redditviewer.presentation.components.LoadingIndicator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToPost: (Post) -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val posts by viewModel.posts.collectAsState()
    
    var showSearchDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Top App Bar
        TopAppBar(
            title = {
                Text(
                    text = uiState.currentSubreddit?.let { "r/$it" } 
                        ?: stringResource(R.string.app_name),
                    fontWeight = FontWeight.Bold
                )
            },
            actions = {
                // Search Subreddit Button
                IconButton(onClick = { showSearchDialog = true }) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = stringResource(R.string.subreddit_search)
                    )
                }
                
                // Home Button (if in subreddit)
                if (uiState.currentSubreddit != null) {
                    IconButton(onClick = { viewModel.loadHomeFeed() }) {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = "ホーム"
                        )
                    }
                }
                
                // Refresh Button
                IconButton(onClick = { viewModel.refresh() }) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = stringResource(R.string.refresh)
                    )
                }
                
                // Logout Button
                IconButton(onClick = { viewModel.logout(); onNavigateToLogin() }) {
                    Icon(
                        imageVector = Icons.Default.ExitToApp,
                        contentDescription = stringResource(R.string.logout)
                    )
                }
            }
        )

        // Sort Type Chips (if in subreddit)
        if (uiState.currentSubreddit != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SortType.values().forEach { sortType ->
                    FilterChip(
                        onClick = { 
                            viewModel.loadSubredditPosts(
                                uiState.currentSubreddit!!, 
                                sortType
                            ) 
                        },
                        label = { 
                            Text(
                                text = when (sortType) {
                                    SortType.HOT -> "人気"
                                    SortType.NEW -> "新着"
                                    SortType.TOP -> "トップ"
                                }
                            ) 
                        },
                        selected = uiState.currentSortType == sortType,
                        leadingIcon = {
                            Icon(
                                imageVector = when (sortType) {
                                    SortType.HOT -> Icons.Default.Whatshot
                                    SortType.NEW -> Icons.Default.NewReleases
                                    SortType.TOP -> Icons.Default.TrendingUp
                                },
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    )
                }
            }
        }

        // Content
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            when {
                uiState.isLoading -> {
                    LoadingIndicator(
                        message = stringResource(R.string.posts_loading)
                    )
                }
                
                uiState.error != null -> {
                    ErrorMessage(
                        message = uiState.error,
                        onRetry = { viewModel.refresh() },
                        onDismiss = { viewModel.clearError() }
                    )
                }
                
                uiState.isEmpty -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.posts_empty),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(
                            items = posts,
                            key = { post -> post.id }
                        ) { post ->
                            PostCard(
                                post = post,
                                showTranslation = uiState.translationStates[post.id] ?: true,
                                onToggleTranslation = { viewModel.toggleTranslation(post.id) },
                                onPostClick = { onNavigateToPost(post) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    // Search Dialog
    if (showSearchDialog) {
        SubredditSearchDialog(
            onDismiss = { showSearchDialog = false },
            onSubredditSelected = { subreddit ->
                showSearchDialog = false
                viewModel.loadSubredditPosts(subreddit)
            }
        )
    }
}

@Composable
private fun SubredditSearchDialog(
    onDismiss: () -> Unit,
    onSubredditSelected: (String) -> Unit
) {
    var searchText by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = stringResource(R.string.subreddit_search))
        },
        text = {
            Column {
                OutlinedTextField(
                    value = searchText,
                    onValueChange = { searchText = it },
                    label = { Text(stringResource(R.string.subreddit_search_hint)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "人気のサブレディット:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Popular subreddits
                val popularSubreddits = listOf(
                    "AskReddit", "funny", "gaming", "aww", "pics",
                    "science", "worldnews", "todayilearned", "movies", "music"
                )
                
                popularSubreddits.chunked(2).forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        row.forEach { subreddit ->
                            FilterChip(
                                onClick = { onSubredditSelected(subreddit) },
                                label = { Text("r/$subreddit") },
                                selected = false,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        if (row.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (searchText.isNotBlank()) {
                        onSubredditSelected(searchText.trim())
                    }
                },
                enabled = searchText.isNotBlank()
            ) {
                Text("検索")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("キャンセル")
            }
        }
    )
} 