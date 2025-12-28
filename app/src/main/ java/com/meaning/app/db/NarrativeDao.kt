package com.meaning.app.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface NarrativeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: QuantizedNarrativeEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<QuantizedNarrativeEntity>)

    @Update
    suspend fun update(entity: QuantizedNarrativeEntity)

    @Delete
    suspend fun delete(entity: QuantizedNarrativeEntity)

    @Query("SELECT * FROM quantized_narrative WHERE id = :id")
    suspend fun getById(id: Long): QuantizedNarrativeEntity?

    @Query("SELECT * FROM quantized_narrative WHERE term = :term LIMIT 1")
    suspend fun getByTerm(term: String): QuantizedNarrativeEntity?

    @Query("SELECT * FROM quantized_narrative ORDER BY creation_date DESC")
    fun getAllStream(): Flow<List<QuantizedNarrativeEntity>>

    @Query("SELECT COUNT(*) FROM quantized_narrative")
    suspend fun getCount(): Int

    // 3D TÉRSZŰRÉS: Csak azokat a pontokat adja vissza, amik egy adott kockában (bounding box) vannak
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

    // METAFORA CSALÁD ALAPJÁN
    @Query("SELECT * FROM quantized_narrative WHERE metaphor_family = :family")
    suspend fun getByFamily(family: String): List<QuantizedNarrativeEntity>

    // STATISZTIKA: A legsűrűbb pontok keresése
    @Query("SELECT * FROM quantized_narrative ORDER BY semantic_density DESC LIMIT :limit")
    suspend fun getDensestPoints(limit: Int): List<QuantizedNarrativeEntity>

    @Query("DELETE FROM quantized_narrative")
    suspend fun clearAll()
}
