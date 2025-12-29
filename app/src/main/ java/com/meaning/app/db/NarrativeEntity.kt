package com.meaning.app.db
import androidx.room.Entity
import androidx.room.PrimaryKey
@Entity(tableName = "narratives")
data class NarrativeEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)
