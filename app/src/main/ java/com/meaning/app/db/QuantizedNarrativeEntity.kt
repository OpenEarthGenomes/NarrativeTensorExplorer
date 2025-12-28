package com.meaning.app.db

import androidx.room.*
import java.util.Date

@Entity(tableName = "quantized_narrative")
data class QuantizedNarrativeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    // Sémantikai adatok
    val term: String,
    val metaphorFamily: String,
    val semanticDensity: Float, // 0.0-1.0
    
    // 3D térbeli koordináták
    val coordX: Float,
    val coordY: Float,
    val coordZ: Float,
    val layer: Int = 0,
    
    // Kvantált vektorok
    @ColumnInfo(typeAffinity = ColumnInfo.BLOB)
    val vectorInt8: ByteArray,
    
    @ColumnInfo(typeAffinity = ColumnInfo.BLOB)
    val vectorFP32: ByteArray? = null,
    
    // Metaadatok
    val creationDate: Date = Date(),
    val usageCount: Int = 0,
    val attentionWeight: Float = 1.0f,
    
    // Kapcsolatok (JSON serializált)
    val connections: String = "[]"
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as QuantizedNarrativeEntity
        return id == other.id
    }
    
    override fun hashCode(): Int = id.hashCode()
}

// Kapcsolat entitás
@Entity(
    tableName = "narrative_connections",
    foreignKeys = [
        ForeignKey(
            entity = QuantizedNarrativeEntity::class,
            parentColumns = ["id"],
            childColumns = ["fromId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = QuantizedNarrativeEntity::class,
            parentColumns = ["id"],
            childColumns = ["toId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["fromId", "toId"], unique = true),
        Index(value = ["connectionType"])
    ]
)
data class NarrativeConnectionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    val fromId: Long,
    val toId: Long,
    
    val connectionType: String, // "analogia", "kontraszt", "kiterjesztes"
    val strength: Float, // 0.0-1.0
    val creationDate: Date = Date()
)
