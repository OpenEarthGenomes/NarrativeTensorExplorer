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
    indices = [
        Index(value = ["from_id", "to_id"], unique = true),
        Index(value = ["connection_type"]),
        Index(value = ["strength"]),
        Index(value = ["created_at"])
    ]
)
data class NarrativeConnectionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    @ColumnInfo(name = "from_id")
    val fromId: Long,
    
    @ColumnInfo(name = "to_id")
    val toId: Long,
    
    @ColumnInfo(name = "connection_type")
    val connectionType: String,
    
    @ColumnInfo(name = "strength")
    val strength: Float, // 0.0 - 1.0
    
    @ColumnInfo(name = "semantic_similarity")
    val semanticSimilarity: Float = 0f,
    
    @ColumnInfo(name = "spatial_distance")
    val spatialDistance: Float = 0f,
    
    @ColumnInfo(name = "metadata_json")
    val metadataJson: String = "{}",
    
    @ColumnInfo(name = "created_at")
    val createdAt: Date = Date(),
    
    @ColumnInfo(name = "updated_at")
    val updatedAt: Date = Date(),
    
    @ColumnInfo(name = "usage_count")
    val usageCount: Int = 0,
    
    @ColumnInfo(name = "is_active")
    val isActive: Boolean = true
) {
    fun toKernelConnection(): com.meaning.app.kernel.NarrativeConnection {
        return com.meaning.app.kernel.NarrativeConnection(
            id = this.id,
            fromId = this.fromId,
            toId = this.toId,
            strength = this.strength,
            connectionType = this.connectionType,
            distance3D = this.spatialDistance,
            semanticSimilarity = this.semanticSimilarity,
            creationTime = this.createdAt.time
        )
    }
    
    companion object {
        fun fromKernelConnection(
            kernelConn: com.meaning.app.kernel.NarrativeConnection
        ): NarrativeConnectionEntity {
            return NarrativeConnectionEntity(
                id = kernelConn.id,
                fromId = kernelConn.fromId,
                toId = kernelConn.toId,
                connectionType = kernelConn.connectionType,
                strength = kernelConn.strength,
                semanticSimilarity = kernelConn.semanticSimilarity,
                spatialDistance = kernelConn.distance3D,
                createdAt = Date(kernelConn.creationTime)
            )
        }
    }
}
