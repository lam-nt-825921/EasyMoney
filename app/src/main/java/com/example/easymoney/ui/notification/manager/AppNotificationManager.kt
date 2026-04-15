package com.example.easymoney.ui.notification.manager

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
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
    private val channelId = "easy_money_high_priority_notifications"
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Easy Money Notifications"
            val descriptionText = "Thông báo quan trọng về tài khoản và khoản vay"
            // IMPORTANCE_HIGH: Cho phép thông báo nổi trên màn hình (Heads-up)
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
                setShowBadge(true) // Cho phép hiển thị chấm đỏ trên App Icon
                enableVibration(true)
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
        // Note: Việc lưu vào Database nên để Service FCM hoặc Repository lo, 
        // ở đây ta tập trung vào việc hiển thị UI.

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            // Có thể thêm dữ liệu để MainActivity biết cần mở màn hình nào
            putExtra("NOTIFICATION_TYPE", type)
        }
        
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            context, System.currentTimeMillis().toInt(), intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification_small)
            .setContentTitle(title)
            .setContentText(content)
            .setStyle(NotificationCompat.BigTextStyle().bigText(content)) // Cho phép hiển thị nhiều dòng
            .setPriority(NotificationCompat.PRIORITY_HIGH) // Quan trọng để hiện Heads-up
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setBadgeIconType(NotificationCompat.BADGE_ICON_SMALL) // Hỗ trợ badge count

        try {
            notificationManager.notify(System.currentTimeMillis().toInt(), builder.build())
        } catch (e: SecurityException) {
            // Xử lý nếu thiếu quyền POST_NOTIFICATIONS trên Android 13+
        }
    }
}
