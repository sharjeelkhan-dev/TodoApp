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

    suspend fun uploadTask(userId: String, task: TaskEntity) {
        firestore.collection(USERS_COLLECTION)
            .document(userId)
            .collection(TASKS_COLLECTION)
            .document(task.id)
            .set(task.toFirestoreMap())
            .await()
    }

    suspend fun deleteTask(userId: String, taskId: String) {
        firestore.collection(USERS_COLLECTION)
            .document(userId)
            .collection(TASKS_COLLECTION)
            .document(taskId)
            .delete()
            .await()
    }

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

    suspend fun backupTasks(userId: String, tasks: List<TaskEntity>) {
        val backupRef = firestore.collection(USERS_COLLECTION)
            .document(userId)
            .collection(BACKUP_COLLECTION)

        val existing = backupRef.get().await()
        if (!existing.isEmpty) {
            existing.documents.chunked(500).forEach { chunk ->
                val deleteBatch = firestore.batch()
                chunk.forEach { doc -> deleteBatch.delete(doc.reference) }
                deleteBatch.commit().await()
            }
        }

        if (tasks.isNotEmpty()) {
            tasks.chunked(500).forEach { chunk ->
                val writeBatch = firestore.batch()
                chunk.forEach { task ->
                    writeBatch.set(backupRef.document(task.id), task.toFirestoreMap())
                }
                writeBatch.commit().await()
            }
        }
    }

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

    private fun TaskEntity.toFirestoreMap(): Map<String, Any?> = mapOf(
        "id" to id,
        "title" to title,
        "description" to description,
        "category" to category,
        "priority" to priority,
        "isCompleted" to isCompleted,
        "isReminderEnabled" to isReminderEnabled,
        "dueDate" to dueDate,
        "dueTime" to dueTime,
        "createdAt" to createdAt,
        "updatedAt" to updatedAt,
        "userId" to userId,
        "subTasks" to subTasks
    )

    private fun com.google.firebase.firestore.DocumentSnapshot.toTaskEntity(): TaskEntity? {
        return try {
            val id = getString("id") ?: id
            val title = getString("title") ?: return null
            
            // Robustly check for boolean completion status
            val isCompleted = when (val value = get("isCompleted")) {
                is Boolean -> value
                is Long -> value == 1L
                is String -> value.toBoolean()
                else -> false
            }

            TaskEntity(
                id = id,
                title = title,
                description = getString("description") ?: "",
                category = getString("category") ?: "OTHER",
                priority = getString("priority") ?: "MEDIUM",
                isCompleted = isCompleted,
                isReminderEnabled = getBoolean("isReminderEnabled") ?: false,
                dueDate = getLong("dueDate"),
                dueTime = getString("dueTime"),
                createdAt = getLong("createdAt") ?: System.currentTimeMillis(),
                updatedAt = getLong("updatedAt") ?: System.currentTimeMillis(),
                userId = getString("userId") ?: "",
                syncStatus = 0,
                subTasks = getString("subTasks")
            )
        } catch (e: Exception) {
            null
        }
    }
}
