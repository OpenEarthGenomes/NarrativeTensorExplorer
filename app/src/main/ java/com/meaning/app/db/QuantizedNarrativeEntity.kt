package com.meaning.app.db

import androidx.room.*
import java.util.Date

@Entity(tableName = "quantized_narrative")
data class QuantizedNarrativeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    @ColumnInfo(name = "term")
    val term: String,
    
    @ColumnInfo(name = "metaphor_family")
    val metaphorFamily: String,
    
    @ColumnInfo(name = "semantic_density")
    val semanticDensity: Float,
    
    // 3D koordináták
    @ColumnInfo(name = "coord_x")
    val coordX: Float,
    
    @ColumnInfo(name = "coord_y")
    val coordY: Float,
    
    @ColumnInfo(name = "coord_z")
    val coordZ: Float,
    
    @ColumnInfo(name = "layer")
    val layer: Int = 0,
    
    // Vektor adatok
    @ColumnInfo(typeAffinity = ColumnInfo.BLOB, name = "vector_int8")
    val vectorInt8: ByteArray,
    
    @ColumnInfo(typeAffinity = ColumnInfo.BLOB, name = "vector_fp32")
    val vectorFP32: ByteArray? = null,
    
    // Metaadatok
    @ColumnInfo(name = "creation_date")
    val creationDate: Date = Date(),
    
    @ColumnInfo(name = "usage_count")
    val usageCount: Int = 0
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is QuantizedNarrativeEntity) return false
        return id == other.id
    }

    override fun hashCode(): Int = id.hashCode()
}
