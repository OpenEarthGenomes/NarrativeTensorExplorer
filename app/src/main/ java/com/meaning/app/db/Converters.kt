package com.meaning.app.db

import androidx.room.TypeConverter
import java.util.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {
    private val gson = Gson()
    
    // === DATE ===
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }
    
    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }
    
    // === FLOAT ARRAY ===
    @TypeConverter
    fun fromFloatArray(value: FloatArray?): String? {
        return value?.joinToString(",") { it.toString() }
    }
    
    @TypeConverter
    fun toFloatArray(data: String?): FloatArray? {
        return data?.split(",")?.map { it.toFloat() }?.toFloatArray()
    }
    
    // === STRING LIST ===
    @TypeConverter
    fun fromStringList(value: List<String>?): String? {
        return value?.let { gson.toJson(it) }
    }
    
    @TypeConverter
    fun toStringList(data: String?): List<String>? {
        return data?.let {
            val type = object : TypeToken<List<String>>() {}.type
            gson.fromJson(it, type)
        }
    }
    
    // === MAP<String, Any> ===
    @TypeConverter
    fun fromStringMap(value: Map<String, Any>?): String? {
        return value?.let { gson.toJson(it) }
    }
    
    @TypeConverter
    fun toStringMap(data: String?): Map<String, Any>? {
        return data?.let {
            val type = object : TypeToken<Map<String, Any>>() {}.type
            gson.fromJson(it, type)
        }
    }
    
    // === CONNECTION METADATA ===
    @TypeConverter
    fun fromConnectionMetadata(value: ConnectionMetadata?): String? {
        return value?.let { gson.toJson(it) }
    }
    
    @TypeConverter
    fun toConnectionMetadata(data: String?): ConnectionMetadata? {
        return data?.let { gson.fromJson(it, ConnectionMetadata::class.java) }
    }
    
    // === NARRATIVE COORDINATES ===
    @TypeConverter
    fun fromNarrativeCoords(coords: NarrativeCoordinates?): String? {
        return coords?.let { "${it.x},${it.y},${it.z},${it.layer}" }
    }
    
    @TypeConverter
    fun toNarrativeCoords(data: String?): NarrativeCoordinates? {
        return data?.split(",")?.let {
            if (it.size == 4) {
                NarrativeCoordinates(
                    x = it[0].toFloat(),
                    y = it[1].toFloat(),
                    z = it[2].toFloat(),
                    layer = it[3].toInt()
                )
            } else null
        }
    }
}

// === SEGÉD OSZTÁLYOK ===

data class ConnectionMetadata(
    val discoveryMethod: String = "auto_generated",
    val confidence: Float = 1.0f,
    val tags: List<String> = emptyList(),
    val notes: String = "",
    val lastValidated: Date = Date()
)

data class NarrativeCoordinates(
    val x: Float,
    val y: Float,
    val z: Float,
    val layer: Int = 0
)
