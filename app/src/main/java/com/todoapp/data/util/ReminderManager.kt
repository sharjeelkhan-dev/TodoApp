package com.todoapp.data.util

import android.content.Context
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.todoapp.data.worker.ReminderWorker
import com.todoapp.domain.model.Task
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Calendar
import java.util.TimeZone
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReminderManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val workManager = WorkManager.getInstance(context)

    fun scheduleReminder(task: Task) {
        if (!task.isReminderEnabled || task.isCompleted || task.dueDate == null) {
            cancelReminder(task.id)
            return
        }

        val reminderTime = calculateReminderTime(task)
        val currentTime = System.currentTimeMillis()
        val delay = reminderTime - currentTime

        // Only schedule if the reminder time is in the future
        if (delay > 0) {
            val data = Data.Builder()
                .putString(ReminderWorker.KEY_TASK_ID, task.id)
                .putString(ReminderWorker.KEY_TASK_TITLE, task.title)
                .build()

            val reminderRequest = OneTimeWorkRequestBuilder<ReminderWorker>()
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .setInputData(data)
                .addTag(ReminderWorker.WORK_NAME_PREFIX + task.id)
                .build()

            workManager.enqueueUniqueWork(
                ReminderWorker.WORK_NAME_PREFIX + task.id,
                ExistingWorkPolicy.REPLACE,
                reminderRequest
            )
        } else {
            // If the calculated time is in the past, we cancel any existing reminder
            cancelReminder(task.id)
        }
    }

    fun cancelReminder(taskId: String) {
        workManager.cancelUniqueWork(ReminderWorker.WORK_NAME_PREFIX + taskId)
    }

    private fun calculateReminderTime(task: Task): Long {
        // Task.dueDate is often stored as UTC midnight from DatePicker
        // We need to extract the year/month/day in UTC to avoid timezone shifts
        val dateCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        dateCalendar.time = task.dueDate!!
        
        val localCalendar = Calendar.getInstance()
        localCalendar.set(Calendar.YEAR, dateCalendar.get(Calendar.YEAR))
        localCalendar.set(Calendar.MONTH, dateCalendar.get(Calendar.MONTH))
        localCalendar.set(Calendar.DAY_OF_MONTH, dateCalendar.get(Calendar.DAY_OF_MONTH))
        
        task.dueTime?.let { timeStr ->
            val parts = timeStr.split(":")
            if (parts.size == 2) {
                localCalendar.set(Calendar.HOUR_OF_DAY, parts[0].toInt())
                localCalendar.set(Calendar.MINUTE, parts[1].toInt())
                localCalendar.set(Calendar.SECOND, 0)
                localCalendar.set(Calendar.MILLISECOND, 0)
            }
        } ?: run {
            // Default reminder time if only date is set
            localCalendar.set(Calendar.HOUR_OF_DAY, 9)
            localCalendar.set(Calendar.MINUTE, 0)
            localCalendar.set(Calendar.SECOND, 0)
            localCalendar.set(Calendar.MILLISECOND, 0)
        }
        
        return localCalendar.timeInMillis
    }
}
