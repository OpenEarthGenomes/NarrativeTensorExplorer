package com.meaning.app.db

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.*

class Converters {
    private val gson = Gson()
    
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? = value?.let { Date(it) }
    
    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? = date?.time
    
    @TypeConverter
    fun fromFloatArray(value: FloatArray?): String? = value?.joinToString(",") { it.toString() }
    
    @TypeConverter
    fun toFloatArray(data: String?): FloatArray? = data?.split(",")?.map { it.toFloat() }?.toFloatArray()
    
    @TypeConverter
    fun fromStringList(value: List<String>?): String? = value?.let { gson.toJson(it) }
    
    @TypeConverter
    fun toStringList(data: String?): List<String>? {
        return data?.let {
            val type = object : TypeToken<List<String>>() {}.type
            gson.fromJson(it, type)
        }
    }

    @TypeConverter
    fun fromConnectionMetadata(value: ConnectionMetadata?): String? = value?.let { gson.toJson(it) }
    
    @TypeConverter
    fun toConnectionMetadata(data: String?): ConnectionMetadata? = data?.let { gson.fromJson(it, ConnectionMetadata::class.java) }
}

// Segéd osztály a metaadatokhoz (itt jó helyen van)
data class ConnectionMetadata(
    val discoveryMethod: String = "auto_generated",
    val confidence: Float = 1.0f,
    val tags: List<String> = emptyList(),
    val notes: String = ""
)
