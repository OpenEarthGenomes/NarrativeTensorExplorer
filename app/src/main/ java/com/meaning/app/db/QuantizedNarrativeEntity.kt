package com.meaning.app.db

import androidx.room.*
import java.util.Date

@Entity(tableName = "quantized_narrative")
data class QuantizedNarrativeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    @ColumnInfo(name = "term") val term: String,
    @ColumnInfo(name = "metaphor_family") val metaphorFamily: String,
    @ColumnInfo(name = "semantic_density") val semanticDensity: Float,
    
    // 3D elhelyezkedés a narratív térben
    @ColumnInfo(name = "coord_x") val coordX: Float,
    @ColumnInfo(name = "coord_y") val coordY: Float,
    @ColumnInfo(name = "coord_z") val coordZ: Float,
    
    // Alacsony szintű vektoradatok (NEON gyorsításhoz)
    @ColumnInfo(typeAffinity = ColumnInfo.BLOB, name = "vector_int8") 
    val vectorInt8: ByteArray,
    
    @ColumnInfo(name = "layer") val layer: Int = 0,
    @ColumnInfo(name = "usage_count") val usageCount: Int = 0,
    @ColumnInfo(name = "creation_date") val creationDate: Date = Date()
) {
    // Segédfüggvény a távolságméréshez
    fun distanceTo(other: QuantizedNarrativeEntity): Float {
        val dx = this.coordX - other.coordX
        val dy = this.coordY - other.coordY
        val dz = this.coordZ - other.coordZ
        return kotlin.math.sqrt(dx*dx + dy*dy + dz*dz)
    }
}
