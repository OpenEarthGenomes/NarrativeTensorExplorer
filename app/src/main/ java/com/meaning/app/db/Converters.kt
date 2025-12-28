package com.meaning.app.db

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.Date

class Converters {
    private val gson = Gson()

    @TypeConverter
    fun fromTimestamp(value: Long?): Date? = value?.let { Date(it) }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? = date?.time

    @TypeConverter
    fun fromStringList(value: List<String>?): String = gson.toJson(value ?: emptyList<String>())

    @TypeConverter
    fun toStringList(value: String): List<String> {
        val listType = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(value, listType)
    }

    @TypeConverter
    fun fromMetadata(metadata: ConnectionMetadata?): String = gson.toJson(metadata ?: ConnectionMetadata())

    @TypeConverter
    fun toMetadata(value: String): ConnectionMetadata = gson.fromJson(value, ConnectionMetadata::class.java)

    @TypeConverter
    fun fromFloatArray(array: FloatArray?): String = array?.joinToString(",") ?: ""

    @TypeConverter
    fun toFloatArray(value: String): FloatArray {
        if (value.isEmpty()) return floatArrayOf()
        return value.split(",").map { it.toFloat() }.toFloatArray()
    }
}
