package com.redditviewer.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.redditviewer.domain.model.Post
import com.redditviewer.domain.repository.AuthRepository
import com.redditviewer.domain.repository.RedditRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val redditRepository: RedditRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _posts = MutableStateFlow<List<Post>>(emptyList())
    val posts: StateFlow<List<Post>> = _posts.asStateFlow()

    init {
        loadHomeFeed()
    }

    fun loadHomeFeed() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            redditRepository.getHomeFeed()
                .onSuccess { fetchedPosts ->
                    _posts.value = fetchedPosts
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isEmpty = fetchedPosts.isEmpty()
                    )
                    
                    // 投稿タイトルを自動翻訳
                    translatePosts(fetchedPosts)
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = exception.message ?: "投稿の読み込みに失敗しました"
                    )
                }
        }
    }

    fun loadSubredditPosts(subreddit: String, sortType: SortType = SortType.HOT) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            val result = when (sortType) {
                SortType.HOT -> redditRepository.getHotPosts(subreddit)
                SortType.NEW -> redditRepository.getNewPosts(subreddit)
                SortType.TOP -> redditRepository.getTopPosts(subreddit)
            }
            
            result
                .onSuccess { fetchedPosts ->
                    _posts.value = fetchedPosts
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isEmpty = fetchedPosts.isEmpty(),
                        currentSubreddit = subreddit,
                        currentSortType = sortType
                    )
                    
                    // 投稿タイトルを自動翻訳
                    translatePosts(fetchedPosts)
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = exception.message ?: "投稿の読み込みに失敗しました"
                    )
                }
        }
    }

    private fun translatePosts(posts: List<Post>) {
        viewModelScope.launch {
            val translatedPosts = posts.map { post ->
                // キャッシュされた翻訳があるかチェック
                val cachedTitle = redditRepository.getCachedTranslation(post.title)
                val cachedSelftext = post.selftext?.let { redditRepository.getCachedTranslation(it) }
                
                if (cachedTitle != null && (post.selftext == null || cachedSelftext != null)) {
                    // キャッシュされた翻訳を使用
                    post.copy(
                        titleTranslated = cachedTitle,
                        selftextTranslated = cachedSelftext
                    )
                } else {
                    // 翻訳を実行
                    val titleResult = redditRepository.translateText(post.title)
                    val selftextResult = post.selftext?.let { redditRepository.translateText(it) }
                    
                    post.copy(
                        titleTranslated = titleResult.getOrNull(),
                        selftextTranslated = selftextResult?.getOrNull()
                    )
                }
            }
            
            _posts.value = translatedPosts
        }
    }

    fun toggleTranslation(postId: String) {
        val currentPosts = _posts.value
        val updatedPosts = currentPosts.map { post ->
            if (post.id == postId) {
                val currentState = _uiState.value.translationStates[postId] ?: true
                _uiState.value = _uiState.value.copy(
                    translationStates = _uiState.value.translationStates.toMutableMap().apply {
                        this[postId] = !currentState
                    }
                )
                post
            } else {
                post
            }
        }
        _posts.value = updatedPosts
    }

    fun refresh() {
        val currentSubreddit = _uiState.value.currentSubreddit
        if (currentSubreddit != null) {
            loadSubredditPosts(currentSubreddit, _uiState.value.currentSortType)
        } else {
            loadHomeFeed()
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

data class HomeUiState(
    val isLoading: Boolean = false,
    val isEmpty: Boolean = false,
    val error: String? = null,
    val currentSubreddit: String? = null,
    val currentSortType: SortType = SortType.HOT,
    val translationStates: Map<String, Boolean> = emptyMap() // postId -> showTranslation
)

enum class SortType {
    HOT, NEW, TOP
} 