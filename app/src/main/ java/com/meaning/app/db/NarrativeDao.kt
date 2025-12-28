package com.meaning.app.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface NarrativeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: QuantizedNarrativeEntity): Long

    @Query("SELECT * FROM quantized_narrative WHERE id = :id")
    suspend fun getById(id: Long): QuantizedNarrativeEntity?

    @Query("SELECT COUNT(*) FROM quantized_narrative")
    suspend fun getCount(): Long

    @Query("SELECT * FROM quantized_narrative ORDER BY term ASC")
    fun getAllStream(): Flow<List<QuantizedNarrativeEntity>>

    @Query("""
        SELECT * FROM quantized_narrative 
        WHERE coordX BETWEEN :minX AND :maxX 
          AND coordY BETWEEN :minY AND :maxY 
          AND coordZ BETWEEN :minZ AND :maxZ
        LIMIT :limit
    """)
    suspend fun getInBoundingBox(
        minX: Float, maxX: Float,
        minY: Float, maxY: Float,
        minZ: Float, maxZ: Float,
        limit: Int
    ): List<QuantizedNarrativeEntity>
}
