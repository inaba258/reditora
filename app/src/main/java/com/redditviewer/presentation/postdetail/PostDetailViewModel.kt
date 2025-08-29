package com.redditviewer.presentation.postdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.redditviewer.domain.model.Comment
import com.redditviewer.domain.model.Post
import com.redditviewer.domain.repository.RedditRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PostDetailViewModel @Inject constructor(
    private val redditRepository: RedditRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PostDetailUiState())
    val uiState: StateFlow<PostDetailUiState> = _uiState.asStateFlow()

    private val _post = MutableStateFlow<Post?>(null)
    val post: StateFlow<Post?> = _post.asStateFlow()

    private val _comments = MutableStateFlow<List<Comment>>(emptyList())
    val comments: StateFlow<List<Comment>> = _comments.asStateFlow()

    fun loadPostAndComments(post: Post) {
        _post.value = post
        loadComments(post.subreddit, post.id)
        
        // 投稿の翻訳が未完了の場合は実行
        if (post.titleTranslated == null || (post.selftext != null && post.selftextTranslated == null)) {
            translatePost(post)
        }
    }

    private fun loadComments(subreddit: String, postId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingComments = true, commentsError = null)
            
            redditRepository.getPostComments(subreddit, postId)
                .onSuccess { fetchedComments ->
                    _comments.value = fetchedComments
                    _uiState.value = _uiState.value.copy(
                        isLoadingComments = false,
                        commentsEmpty = fetchedComments.isEmpty()
                    )
                    
                    // コメントを自動翻訳
                    translateComments(fetchedComments)
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoadingComments = false,
                        commentsError = exception.message ?: "コメントの読み込みに失敗しました"
                    )
                }
        }
    }

    private fun translatePost(post: Post) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isTranslatingPost = true)
            
            val titleResult = if (post.titleTranslated == null) {
                redditRepository.translateText(post.title)
            } else {
                Result.success(post.titleTranslated)
            }
            
            val selftextResult = if (post.selftext != null && post.selftextTranslated == null) {
                redditRepository.translateText(post.selftext)
            } else {
                post.selftextTranslated?.let { Result.success(it) }
            }
            
            val translatedPost = post.copy(
                titleTranslated = titleResult.getOrNull() ?: post.titleTranslated,
                selftextTranslated = selftextResult?.getOrNull() ?: post.selftextTranslated
            )
            
            _post.value = translatedPost
            _uiState.value = _uiState.value.copy(isTranslatingPost = false)
        }
    }

    private fun translateComments(comments: List<Comment>) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isTranslatingComments = true)
            
            val translatedComments = translateCommentsRecursively(comments)
            _comments.value = translatedComments
            _uiState.value = _uiState.value.copy(isTranslatingComments = false)
        }
    }

    private suspend fun translateCommentsRecursively(comments: List<Comment>): List<Comment> {
        return comments.map { comment ->
            // キャッシュされた翻訳があるかチェック
            val cachedTranslation = redditRepository.getCachedTranslation(comment.body)
            
            val translatedBody = if (cachedTranslation != null) {
                cachedTranslation
            } else {
                redditRepository.translateText(comment.body).getOrNull()
            }
            
            // 返信も再帰的に翻訳
            val translatedReplies = if (comment.replies.isNotEmpty()) {
                translateCommentsRecursively(comment.replies)
            } else {
                comment.replies
            }
            
            comment.copy(
                bodyTranslated = translatedBody,
                replies = translatedReplies
            )
        }
    }

    fun togglePostTranslation() {
        val currentState = _uiState.value.showPostTranslation
        _uiState.value = _uiState.value.copy(showPostTranslation = !currentState)
    }

    fun toggleCommentTranslation(commentId: String) {
        val currentStates = _uiState.value.commentTranslationStates.toMutableMap()
        val currentState = currentStates[commentId] ?: true
        currentStates[commentId] = !currentState
        
        _uiState.value = _uiState.value.copy(
            commentTranslationStates = currentStates
        )
    }

    fun toggleCommentExpansion(commentId: String) {
        val currentStates = _uiState.value.expandedComments.toMutableMap()
        val currentState = currentStates[commentId] ?: true
        currentStates[commentId] = !currentState
        
        _uiState.value = _uiState.value.copy(
            expandedComments = currentStates
        )
    }

    fun refreshComments() {
        _post.value?.let { currentPost ->
            loadComments(currentPost.subreddit, currentPost.id)
        }
    }

    fun clearCommentsError() {
        _uiState.value = _uiState.value.copy(commentsError = null)
    }
}

data class PostDetailUiState(
    val isLoadingComments: Boolean = false,
    val isTranslatingPost: Boolean = false,
    val isTranslatingComments: Boolean = false,
    val commentsEmpty: Boolean = false,
    val commentsError: String? = null,
    val showPostTranslation: Boolean = true,
    val commentTranslationStates: Map<String, Boolean> = emptyMap(), // commentId -> showTranslation
    val expandedComments: Map<String, Boolean> = emptyMap() // commentId -> isExpanded
) 