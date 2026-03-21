package com.todoapp.data.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.todoapp.data.local.dao.TaskDao
import com.todoapp.data.remote.FirestoreDataSource
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/**
 * WorkManager worker for background cloud sync.
 * Runs periodically to ensure local changes are pushed to Firestore
 * and remote changes are pulled to Room.
 */
@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val taskDao: TaskDao,
    private val firestoreDataSource: FirestoreDataSource
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            // Upload pending tasks
            val pendingUploads = taskDao.getPendingUploadTasks()
            for (task in pendingUploads) {
                if (task.userId.isNotBlank()) {
                    firestoreDataSource.uploadTask(task.userId, task)
                    taskDao.markAsSynced(task.id)
                }
            }

            // Delete pending deletions from cloud
            val pendingDeletes = taskDao.getPendingDeleteTasks()
            for (task in pendingDeletes) {
                if (task.userId.isNotBlank()) {
                    firestoreDataSource.deleteTask(task.userId, task.id)
                }
                taskDao.hardDeleteTask(task.id)
            }

            Result.success()
        } catch (_: Exception) {
            Result.retry()
        }
    }

    companion object {
        const val WORK_NAME = "sync_worker"
    }
}
