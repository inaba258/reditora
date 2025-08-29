package com.redditviewer.data.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.redditviewer.R
import com.redditviewer.presentation.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class RedditFirebaseMessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var notificationRepository: NotificationRepository

    companion object {
        const val CHANNEL_ID = "reddit_notifications"
        const val CHANNEL_NAME = "Reddit通知"
        const val NOTIFICATION_ID = 1001
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        // バックグラウンドでの処理
        val workRequest = OneTimeWorkRequestBuilder<NotificationWorker>()
            .setInputData(
                workDataOf(
                    "title" to (remoteMessage.notification?.title ?: ""),
                    "body" to (remoteMessage.notification?.body ?: ""),
                    "data" to remoteMessage.data.toString()
                )
            )
            .build()

        WorkManager.getInstance(this).enqueue(workRequest)

        // フォアグラウンドでの即座表示
        showNotification(
            title = remoteMessage.notification?.title ?: "新しい通知",
            body = remoteMessage.notification?.body ?: "",
            data = remoteMessage.data
        )
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        
        // 新しいトークンをサーバーに送信
        val workRequest = OneTimeWorkRequestBuilder<TokenUpdateWorker>()
            .setInputData(workDataOf("fcm_token" to token))
            .build()

        WorkManager.getInstance(this).enqueue(workRequest)
    }

    private fun showNotification(
        title: String,
        body: String,
        data: Map<String, String>
    ) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            
            // データを Intent に追加
            data.forEach { (key, value) ->
                putExtra(key, value)
            }
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setColor(getColor(R.color.reddit_orange))

        // 長いテキストの場合はBigTextStyleを使用
        if (body.length > 50) {
            notificationBuilder.setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(body)
                    .setSummaryText(title)
            )
        }

        // アクションボタンを追加
        val replyIntent = Intent(this, MainActivity::class.java).apply {
            putExtra("action", "reply")
            putExtra("post_id", data["post_id"])
        }
        val replyPendingIntent = PendingIntent.getActivity(
            this,
            1,
            replyIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val markReadIntent = Intent(this, NotificationActionReceiver::class.java).apply {
            action = "MARK_READ"
            putExtra("notification_id", data["notification_id"])
        }
        val markReadPendingIntent = PendingIntent.getBroadcast(
            this,
            2,
            markReadIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        notificationBuilder
            .addAction(R.drawable.ic_reply, "返信", replyPendingIntent)
            .addAction(R.drawable.ic_check, "既読", markReadPendingIntent)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Reddit投稿とコメントの通知"
                enableLights(true)
                lightColor = getColor(R.color.reddit_orange)
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 250, 250, 250)
            }

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
} 