package com.example.util

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.R

object NotificationHelper {

    private const val TAG = "NotificationHelper"
    const val CHANNEL_ID = "foxy_chat_messages_channel"
    private const val CHANNEL_NAME = "Messages & Chats"
    private const val CHANNEL_DESC = "Notifications for incoming messages in Foxy Chat"

    const val EXTRA_FRIEND_ID = "extra_friend_id"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESC
                enableVibration(true)
                enableLights(true)
                lightColor = android.graphics.Color.parseColor("#7C3AED")
            }
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            notificationManager?.createNotificationChannel(channel)
        }
    }

    fun showIncomingMessageNotification(
        context: Context,
        senderId: String,
        senderName: String,
        messageText: String,
        isPhoto: Boolean
    ) {
        // Check POST_NOTIFICATIONS permission on Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                Log.d(TAG, "POST_NOTIFICATIONS not granted, skipping notification display")
                return
            }
        }

        try {
            createNotificationChannel(context)

            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra(EXTRA_FRIEND_ID, senderId)
            }

            val pendingIntent = PendingIntent.getActivity(
                context,
                senderId.hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val displayContent = if (isPhoto) "📷 Sent a photo" else messageText.ifBlank { "Sent a message" }

            val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(senderName.ifBlank { "Foxy Chat Friend" })
                .setContentText(displayContent)
                .setStyle(NotificationCompat.BigTextStyle().bigText(displayContent))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .setAutoCancel(true)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setColor(android.graphics.Color.parseColor("#7C3AED"))
                .setContentIntent(pendingIntent)
                .build()

            NotificationManagerCompat.from(context).notify(senderId.hashCode(), notification)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to show message notification", e)
        }
    }
}
