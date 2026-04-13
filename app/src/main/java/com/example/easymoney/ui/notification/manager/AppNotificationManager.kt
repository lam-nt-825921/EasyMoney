package com.example.easymoney.ui.notification.manager

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.easymoney.MainActivity
import com.example.easymoney.R
import com.example.easymoney.domain.repository.NotificationRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppNotificationManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val notificationRepository: NotificationRepository
) {
    private val channelId = "easy_money_notifications"
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Easy Money Notifications"
            val descriptionText = "Thông báo về biến động số dư và khuyến mại"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
                setShowBadge(true)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showNotification(
        userId: String,
        title: String,
        content: String,
        type: String = "general",
        amount: Long? = null,
        balanceAfter: Long? = null,
        transactionCode: String? = null
    ) {
        // 1. Save to Database
        CoroutineScope(Dispatchers.IO).launch {
            val notification = com.example.easymoney.data.local.entity.NotificationEntity(
                userId = userId,
                title = title,
                content = content,
                type = type,
                amount = amount,
                balanceAfter = balanceAfter,
                transactionCode = transactionCode,
                timestamp = System.currentTimeMillis()
            )
            notificationRepository.addNotificationRaw(notification)
        }

        // 2. Show System Notification
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification_small)
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(context)) {
            // notificationId is a unique int for each notification that you must define
            notify(System.currentTimeMillis().toInt(), builder.build())
        }
    }
}
