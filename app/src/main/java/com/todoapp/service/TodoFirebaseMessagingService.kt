package com.todoapp.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import android.util.Log

/**
 * Firebase Cloud Messaging service for handling push notifications.
 * Receives messages when app is in foreground or background.
 */
class TodoFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "FCM Token refreshed: $token")
        // Send token to your server if needed
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Log.d(TAG, "FCM Message from: ${message.from}")

        // Create notification channel for FCM messages
        createNotificationChannel()

        // Handle notification payload
        message.notification?.let { notification ->
            Log.d(TAG, "FCM notification: ${notification.title} - ${notification.body}")
        }

        // Handle data payload
        if (message.data.isNotEmpty()) {
            Log.d(TAG, "FCM data payload: ${message.data}")
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Task Notifications",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications from TodoApp"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    companion object {
        private const val TAG = "TodoFCMService"
        private const val CHANNEL_ID = "todo_notifications"
    }
}
