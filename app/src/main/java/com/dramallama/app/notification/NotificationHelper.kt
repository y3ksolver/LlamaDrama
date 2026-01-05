package com.dramallama.app.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.dramallama.app.MainActivity
import com.dramallama.app.R

object NotificationHelper {
    
    private const val CHANNEL_ID = "llamadrama_reminders"
    private const val CHANNEL_NAME = "1-on-1 Reminders"
    private const val CHANNEL_DESCRIPTION = "Notifications for overdue 1-on-1 meetings"
    private const val NOTIFICATION_ID = 1001
    
    fun createNotificationChannel(context: Context) {
        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = CHANNEL_DESCRIPTION
        }
        
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
    
    fun showOverdueNotification(context: Context, overdueCount: Int, memberNames: List<String>) {
        // Check notification permission for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
        }
        
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val title = if (overdueCount == 1) "1 overdue 1-on-1" else "$overdueCount overdue 1-on-1s"
        
        val content = when {
            memberNames.isEmpty() -> "Time to catch up with your team!"
            memberNames.size == 1 -> "Time to catch up with ${memberNames.first()}"
            memberNames.size <= 3 -> "Time to catch up with ${memberNames.joinToString(", ")}"
            else -> "Time to catch up with ${memberNames.take(2).joinToString(", ")} and ${memberNames.size - 2} others"
        }
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        
        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
    }
}

