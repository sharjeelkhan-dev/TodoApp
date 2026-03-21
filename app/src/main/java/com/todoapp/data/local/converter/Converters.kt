package com.todoapp.data.local.converter

import androidx.room.TypeConverter
import java.util.Date

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
}
