package com.redditviewer.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.redditviewer.domain.model.RedditUser
import com.redditviewer.domain.repository.AuthRepository
import com.redditviewer.domain.repository.NotificationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val notificationRepository: NotificationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private val _currentUser = MutableStateFlow<RedditUser?>(null)
    val currentUser: StateFlow<RedditUser?> = _currentUser.asStateFlow()

    private val _notificationSettings = MutableStateFlow(NotificationSettings())
    val notificationSettings: StateFlow<NotificationSettings> = _notificationSettings.asStateFlow()

    init {
        loadUserProfile()
        loadNotificationSettings()
    }

    private fun loadUserProfile() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingProfile = true, profileError = null)
            
            authRepository.getCurrentUser()
                .onSuccess { user ->
                    _currentUser.value = user
                    _uiState.value = _uiState.value.copy(
                        isLoadingProfile = false
                    )
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoadingProfile = false,
                        profileError = exception.message ?: "プロフィールの読み込みに失敗しました"
                    )
                }
        }
    }

    private fun loadNotificationSettings() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingSettings = true)
            
            notificationRepository.getNotificationSettings()
                .onSuccess { settings ->
                    _notificationSettings.value = settings
                    _uiState.value = _uiState.value.copy(
                        isLoadingSettings = false
                    )
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoadingSettings = false,
                        settingsError = exception.message ?: "通知設定の読み込みに失敗しました"
                    )
                }
        }
    }

    fun updateNotificationSettings(settings: NotificationSettings) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isUpdatingSettings = true)
            
            notificationRepository.updateNotificationSettings(settings)
                .onSuccess {
                    _notificationSettings.value = settings
                    _uiState.value = _uiState.value.copy(
                        isUpdatingSettings = false,
                        showSettingsUpdatedMessage = true
                    )
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isUpdatingSettings = false,
                        settingsError = exception.message ?: "通知設定の更新に失敗しました"
                    )
                }
        }
    }

    fun requestNotificationPermission() {
        viewModelScope.launch {
            notificationRepository.requestNotificationPermission()
                .onSuccess { granted ->
                    _uiState.value = _uiState.value.copy(
                        notificationPermissionGranted = granted
                    )
                    
                    if (granted) {
                        registerForNotifications()
                    }
                }
        }
    }

    private fun registerForNotifications() {
        viewModelScope.launch {
            notificationRepository.registerForNotifications()
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        isRegisteredForNotifications = true
                    )
                }
        }
    }

    fun refreshProfile() {
        loadUserProfile()
    }

    fun refreshSettings() {
        loadNotificationSettings()
    }

    fun logout() {
        viewModelScope.launch {
            // 通知登録を解除
            notificationRepository.unregisterFromNotifications()
            
            // ログアウト
            authRepository.logout()
        }
    }

    fun clearProfileError() {
        _uiState.value = _uiState.value.copy(profileError = null)
    }

    fun clearSettingsError() {
        _uiState.value = _uiState.value.copy(settingsError = null)
    }

    fun dismissSettingsUpdatedMessage() {
        _uiState.value = _uiState.value.copy(showSettingsUpdatedMessage = false)
    }

    fun exportUserData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isExportingData = true)
            
            try {
                // ユーザーデータのエクスポート処理
                // TODO: 実装
                _uiState.value = _uiState.value.copy(
                    isExportingData = false,
                    showDataExportedMessage = true
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isExportingData = false,
                    profileError = "データのエクスポートに失敗しました"
                )
            }
        }
    }

    fun deleteUserData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isDeletingData = true)
            
            try {
                // ユーザーデータの削除処理
                // TODO: 実装
                _uiState.value = _uiState.value.copy(
                    isDeletingData = false,
                    showDataDeletedMessage = true
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isDeletingData = false,
                    profileError = "データの削除に失敗しました"
                )
            }
        }
    }
}

data class ProfileUiState(
    val isLoadingProfile: Boolean = false,
    val isLoadingSettings: Boolean = false,
    val isUpdatingSettings: Boolean = false,
    val isExportingData: Boolean = false,
    val isDeletingData: Boolean = false,
    val profileError: String? = null,
    val settingsError: String? = null,
    val showSettingsUpdatedMessage: Boolean = false,
    val showDataExportedMessage: Boolean = false,
    val showDataDeletedMessage: Boolean = false,
    val notificationPermissionGranted: Boolean = false,
    val isRegisteredForNotifications: Boolean = false
)

data class NotificationSettings(
    val newPosts: Boolean = true,
    val newComments: Boolean = true,
    val mentions: Boolean = true,
    val directMessages: Boolean = true,
    val upvotes: Boolean = false,
    val quietHours: QuietHours = QuietHours(),
    val frequency: NotificationFrequency = NotificationFrequency.IMMEDIATE
)

data class QuietHours(
    val enabled: Boolean = false,
    val startTime: String = "22:00",
    val endTime: String = "08:00"
)

enum class NotificationFrequency {
    IMMEDIATE, HOURLY, DAILY
} 