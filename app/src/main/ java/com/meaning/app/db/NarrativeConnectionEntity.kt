package com.meaning.app.db

import androidx.room.*
import java.util.Date

@Entity(
    tableName = "narrative_connections",
    foreignKeys = [
        ForeignKey(
            entity = QuantizedNarrativeEntity::class,
            parentColumns = ["id"],
            childColumns = ["from_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = QuantizedNarrativeEntity::class,
            parentColumns = ["id"],
            childColumns = ["to_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("from_id"), Index("to_id")]
)
data class NarrativeConnectionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    @ColumnInfo(name = "from_id")
    val fromId: Long,
    
    @ColumnInfo(name = "to_id")
    val toId: Long,
    
    @ColumnInfo(name = "connection_type")
    val connectionType: String, // pl. "synonym", "antonym", "association"
    
    @ColumnInfo(name = "strength")
    val strength: Float, // 0.0 - 1.0
    
    @ColumnInfo(name = "created_at")
    val createdAt: Date = Date(),
    
    @ColumnInfo(name = "metadata")
    val metadata: ConnectionMetadata? = null
)
