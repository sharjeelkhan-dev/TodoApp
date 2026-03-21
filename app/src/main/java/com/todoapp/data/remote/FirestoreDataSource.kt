package com.todoapp.data.remote

import com.google.firebase.firestore.FirebaseFirestore
import com.todoapp.data.local.entity.TaskEntity
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Firestore data source for cloud sync operations.
 * Handles uploading, downloading, and deleting task documents.
 */
@Singleton
class FirestoreDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    companion object {
        private const val USERS_COLLECTION = "users"
        private const val TASKS_COLLECTION = "tasks"
        private const val BACKUP_COLLECTION = "backups"
    }

    /**
     * Upload a task to Firestore under the user's collection.
     */
    suspend fun uploadTask(userId: String, task: TaskEntity) {
        firestore.collection(USERS_COLLECTION)
            .document(userId)
            .collection(TASKS_COLLECTION)
            .document(task.id)
            .set(task.toFirestoreMap())
            .await()
    }

    /**
     * Delete a task from Firestore.
     */
    suspend fun deleteTask(userId: String, taskId: String) {
        firestore.collection(USERS_COLLECTION)
            .document(userId)
            .collection(TASKS_COLLECTION)
            .document(taskId)
            .delete()
            .await()
    }

    /**
     * Fetch all tasks from Firestore for a given user.
     */
    suspend fun fetchAllTasks(userId: String): List<TaskEntity> {
        val snapshot = firestore.collection(USERS_COLLECTION)
            .document(userId)
            .collection(TASKS_COLLECTION)
            .get()
            .await()

        return snapshot.documents.mapNotNull { doc ->
            doc.toTaskEntity()
        }
    }

    /**
     * Backup all tasks to a dedicated backup collection.
     */
    suspend fun backupTasks(userId: String, tasks: List<TaskEntity>) {
        val batch = firestore.batch()
        val backupRef = firestore.collection(USERS_COLLECTION)
            .document(userId)
            .collection(BACKUP_COLLECTION)

        // Clear existing backup
        val existing = backupRef.get().await()
        existing.documents.forEach { batch.delete(it.reference) }
        batch.commit().await()

        // Write new backup in batches of 500 (Firestore limit)
        tasks.chunked(500).forEach { chunk ->
            val newBatch = firestore.batch()
            chunk.forEach { task ->
                newBatch.set(backupRef.document(task.id), task.toFirestoreMap())
            }
            newBatch.commit().await()
        }
    }

    /**
     * Restore tasks from backup collection.
     */
    suspend fun restoreTasks(userId: String): List<TaskEntity> {
        val snapshot = firestore.collection(USERS_COLLECTION)
            .document(userId)
            .collection(BACKUP_COLLECTION)
            .get()
            .await()

        return snapshot.documents.mapNotNull { doc ->
            doc.toTaskEntity()
        }
    }

    // ─── Helpers ──────────────────────────────

    private fun TaskEntity.toFirestoreMap(): Map<String, Any?> = mapOf(
        "id" to id,
        "title" to title,
        "description" to description,
        "category" to category,
        "priority" to priority,
        "isCompleted" to isCompleted,
        "dueDate" to dueDate,
        "dueTime" to dueTime,
        "createdAt" to createdAt,
        "updatedAt" to updatedAt,
        "userId" to userId
    )

    @Suppress("UNCHECKED_CAST")
    private fun com.google.firebase.firestore.DocumentSnapshot.toTaskEntity(): TaskEntity? {
        return try {
            TaskEntity(
                id = getString("id") ?: id,
                title = getString("title") ?: return null,
                description = getString("description") ?: "",
                category = getString("category") ?: "OTHER",
                priority = getString("priority") ?: "MEDIUM",
                isCompleted = getBoolean("isCompleted") ?: false,
                dueDate = getLong("dueDate"),
                dueTime = getString("dueTime"),
                createdAt = getLong("createdAt") ?: System.currentTimeMillis(),
                updatedAt = getLong("updatedAt") ?: System.currentTimeMillis(),
                userId = getString("userId") ?: "",
                syncStatus = 0  // Came from cloud, so it's synced
            )
        } catch (_: Exception) {
            null
        }
    }
}
