package com.todoapp.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.todoapp.data.local.converter.Converters
import com.todoapp.data.local.dao.TaskDao
import com.todoapp.data.local.entity.TaskEntity

/**
 * Room database for the TodoApp.
 * Acts as the single source of truth in the offline-first architecture.
 */
@Database(
    entities = [TaskEntity::class],
    version = 7,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class TodoDatabase : RoomDatabase() {

    abstract fun taskDao(): TaskDao

    companion object {
        const val DATABASE_NAME = "todo_database"
    }
}
