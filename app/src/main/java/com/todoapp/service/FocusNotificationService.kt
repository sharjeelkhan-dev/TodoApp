package com.todoapp.service

import android.Manifest
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.todoapp.R
import com.todoapp.TodoApplication
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FocusNotificationService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    fun showFocusCompleteNotification(isBreak: Boolean) {
        // Check POST_NOTIFICATIONS permission for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // Permission missing, notification cannot be shown
                return
            }
        }

        val title = if (isBreak) {
            context.getString(R.string.back_to_work)
        } else {
            context.getString(R.string.focus_completed)
        }

        val message = if (isBreak) {
            "Break time is over!"
        } else {
            context.getString(R.string.take_a_break)
        }

        val notification = NotificationCompat.Builder(context, TodoApplication.CHANNEL_ID)
            .setSmallIcon(R.drawable.notepad_icon)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(FOCUS_NOTIFICATION_ID, notification)
    }

    companion object {
        private const val FOCUS_NOTIFICATION_ID = 1001
    }
}