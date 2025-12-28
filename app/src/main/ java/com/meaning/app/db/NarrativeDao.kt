package com.meaning.app.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface NarrativeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: QuantizedNarrativeEntity): Long
    
    @Query("SELECT * FROM quantized_narrative WHERE id = :id")
    suspend fun getById(id: Long): QuantizedNarrativeEntity?
    
    @Query("SELECT * FROM quantized_narrative")
    fun getAllStream(): Flow<List<QuantizedNarrativeEntity>>
    
    @Query("SELECT COUNT(*) FROM quantized_narrative")
    suspend fun getCount(): Long
    
    // Fontos a 3D térkép rendereléséhez (Bounding Box keresés)
    @Query("""
        SELECT * FROM quantized_narrative 
        WHERE coord_x BETWEEN :minX AND :maxX 
          AND coord_y BETWEEN :minY AND :maxY 
          AND coord_z BETWEEN :minZ AND :maxZ
        LIMIT :limit
    """)
    suspend fun getInBoundingBox(
        minX: Float, maxX: Float, 
        minY: Float, maxY: Float, 
        minZ: Float, maxZ: Float, 
        limit: Int = 100
    ): List<QuantizedNarrativeEntity>
    
    @Delete
    suspend fun delete(entity: QuantizedNarrativeEntity)
}
