package com.todoapp.data.local.converter

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.todoapp.domain.model.SubTask
import java.util.Date

private val gson = Gson()
private val subTaskType = object : TypeToken<List<SubTask>>() {}.type

/**
 * Room type converters for storing complex types in SQLite.
 */
class Converters {

    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }

    @TypeConverter
    fun fromSubTaskList(value: List<SubTask>?): String? {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toSubTaskList(value: String?): List<SubTask>? {
        return try {
            if (value == null) null else gson.fromJson(value, subTaskType)
        } catch (_: Exception) {
            null
        }
    }
}
