package com.redditviewer.presentation.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.redditviewer.R
import com.redditviewer.domain.model.RedditUser
import com.redditviewer.presentation.components.ErrorMessage
import com.redditviewer.presentation.components.LoadingIndicator
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun ProfileScreen(
    onNavigateToLogin: () -> Unit,
    onBackClick: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val notificationSettings by viewModel.notificationSettings.collectAsState()

    // 通知権限の状態
    val notificationPermissionState = rememberPermissionState(
        android.Manifest.permission.POST_NOTIFICATIONS
    )

    LaunchedEffect(Unit) {
        if (!notificationPermissionState.status.isGranted) {
            notificationPermissionState.launchPermissionRequest()
        }
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Top App Bar
        TopAppBar(
            title = {
                Text(
                    text = stringResource(R.string.nav_profile),
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
                IconButton(onClick = { viewModel.refreshProfile() }) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = stringResource(R.string.refresh)
                    )
                }
            }
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // プロフィール情報
            item {
                when {
                    uiState.isLoadingProfile -> {
                        LoadingIndicator(message = "プロフィールを読み込み中...")
                    }
                    
                    uiState.profileError != null -> {
                        ErrorMessage(
                            message = uiState.profileError,
                            onRetry = { viewModel.refreshProfile() },
                            onDismiss = { viewModel.clearProfileError() }
                        )
                    }
                    
                    currentUser != null -> {
                        UserProfileCard(
                            user = currentUser,
                            onLogout = {
                                viewModel.logout()
                                onNavigateToLogin()
                            }
                        )
                    }
                }
            }

            // 通知設定
            item {
                NotificationSettingsCard(
                    settings = notificationSettings,
                    isLoading = uiState.isLoadingSettings || uiState.isUpdatingSettings,
                    error = uiState.settingsError,
                    permissionGranted = notificationPermissionState.status.isGranted,
                    onSettingsChange = { viewModel.updateNotificationSettings(it) },
                    onRequestPermission = { viewModel.requestNotificationPermission() },
                    onClearError = { viewModel.clearSettingsError() }
                )
            }

            // データ管理
            item {
                DataManagementCard(
                    isExporting = uiState.isExportingData,
                    isDeleting = uiState.isDeletingData,
                    onExportData = { viewModel.exportUserData() },
                    onDeleteData = { viewModel.deleteUserData() }
                )
            }

            // アプリ情報
            item {
                AppInfoCard()
            }
        }
    }

    // 成功メッセージのスナックバー
    if (uiState.showSettingsUpdatedMessage) {
        LaunchedEffect(Unit) {
            kotlinx.coroutines.delay(3000)
            viewModel.dismissSettingsUpdatedMessage()
        }
    }
}

@Composable
private fun UserProfileCard(
    user: RedditUser?,
    onLogout: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // プロフィール画像
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(user?.iconImg ?: "")
                    .crossfade(true)
                    .build(),
                contentDescription = "プロフィール画像",
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop,
                fallback = androidx.compose.ui.res.painterResource(R.drawable.ic_person)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ユーザー名
            Text(
                text = user?.name ?: "Unknown User",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            // アカウント作成日
            Text(
                text = stringResource(
                    R.string.profile_created,
                    formatDate(user?.createdUtc ?: 0L)
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            // カルマ情報
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                KarmaItem(
                    label = "総カルマ",
                    value = user?.totalKarma ?: 0
                )
                KarmaItem(
                    label = "投稿カルマ",
                    value = user?.linkKarma ?: 0
                )
                KarmaItem(
                    label = "コメントカルマ",
                    value = user?.commentKarma ?: 0
                )
            }

            // バッジ
            if (user?.isEmployee == true || user?.isGold == true || user?.isPremium == true) {
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (user.isEmployee) {
                        Badge { Text("Reddit社員") }
                    }
                    if (user.isGold) {
                        Badge { Text("Gold") }
                    }
                    if (user.isPremium) {
                        Badge { Text("Premium") }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ログアウトボタン
            OutlinedButton(
                onClick = onLogout,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.ExitToApp,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.logout))
            }
        }
    }
}

@Composable
private fun KarmaItem(
    label: String,
    value: Int
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = formatNumber(value),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun NotificationSettingsCard(
    settings: NotificationSettings,
    isLoading: Boolean,
    error: String?,
    permissionGranted: Boolean,
    onSettingsChange: (NotificationSettings) -> Unit,
    onRequestPermission: () -> Unit,
    onClearError: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "通知設定",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (!permissionGranted) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "通知権限が必要です",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = onRequestPermission) {
                            Text("権限を許可")
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            if (error != null) {
                ErrorMessage(
                    message = error,
                    onDismiss = onClearError
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            if (isLoading) {
                LoadingIndicator(message = "設定を更新中...")
            } else {
                // 通知タイプ設定
                NotificationToggleItem(
                    title = "新しい投稿",
                    description = "フォローしているサブレディットの新しい投稿",
                    checked = settings.newPosts,
                    onCheckedChange = { 
                        onSettingsChange(settings.copy(newPosts = it)) 
                    }
                )

                NotificationToggleItem(
                    title = "新しいコメント",
                    description = "投稿への新しいコメント",
                    checked = settings.newComments,
                    onCheckedChange = { 
                        onSettingsChange(settings.copy(newComments = it)) 
                    }
                )

                NotificationToggleItem(
                    title = "メンション",
                    description = "あなたへのメンション",
                    checked = settings.mentions,
                    onCheckedChange = { 
                        onSettingsChange(settings.copy(mentions = it)) 
                    }
                )

                NotificationToggleItem(
                    title = "アップボート",
                    description = "投稿やコメントのアップボート",
                    checked = settings.upvotes,
                    onCheckedChange = { 
                        onSettingsChange(settings.copy(upvotes = it)) 
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // クワイエット時間設定
                NotificationToggleItem(
                    title = "クワイエット時間",
                    description = "指定した時間帯は通知を無効化",
                    checked = settings.quietHours.enabled,
                    onCheckedChange = { 
                        onSettingsChange(settings.copy(
                            quietHours = settings.quietHours.copy(enabled = it)
                        )) 
                    }
                )
            }
        }
    }
}

@Composable
private fun NotificationToggleItem(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
private fun DataManagementCard(
    isExporting: Boolean,
    isDeleting: Boolean,
    onExportData: () -> Unit,
    onDeleteData: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "データ管理",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(16.dp))

            // データエクスポート
            OutlinedButton(
                onClick = onExportData,
                enabled = !isExporting && !isDeleting,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isExporting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text("データをエクスポート")
            }

            Spacer(modifier = Modifier.height(8.dp))

            // データ削除
            OutlinedButton(
                onClick = onDeleteData,
                enabled = !isExporting && !isDeleting,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                if (isDeleting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text("すべてのデータを削除")
            }
        }
    }
}

@Composable
private fun AppInfoCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "アプリ情報",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(16.dp))

            InfoRow("バージョン", "1.0.0")
            InfoRow("ビルド", "1")
            InfoRow("開発者", "Reddit Viewer Team")
        }
    }
}

@Composable
private fun InfoRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

private fun formatDate(timestamp: Long): String {
    val format = SimpleDateFormat("yyyy年MM月dd日", Locale.getDefault())
    return format.format(Date(timestamp))
}

private fun formatNumber(number: Int): String {
    return when {
        number >= 1000000 -> "${number / 1000000}M"
        number >= 1000 -> "${number / 1000}K"
        else -> number.toString()
    }
} 