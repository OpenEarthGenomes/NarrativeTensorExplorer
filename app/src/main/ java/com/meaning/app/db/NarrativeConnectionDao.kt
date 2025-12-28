package com.meaning.app.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface NarrativeConnectionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(connection: NarrativeConnectionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(connections: List<NarrativeConnectionEntity>)

    @Delete
    suspend fun delete(connection: NarrativeConnectionEntity)

    @Query("SELECT * FROM narrative_connections")
    fun getAllStream(): Flow<List<NarrativeConnectionEntity>>

    @Query("SELECT * FROM narrative_connections WHERE from_id = :pointId OR to_id = :pointId")
    suspend fun getConnectionsForPoint(pointId: Long): List<NarrativeConnectionEntity>

    // ERŐSSÉG ALAPJÁN SZŰRT KAPCSOLATOK
    @Query("SELECT * FROM narrative_connections WHERE strength >= :minStrength ORDER BY strength DESC")
    suspend fun getStrongConnections(minStrength: Float): List<NarrativeConnectionEntity>

    // REKURZÍV GRÁF-BEJÁRÁS (Közösségkeresés)
    // Ez megkeresi egy pont összes közvetett kapcsolatát egy bizonyos mélységig
    @Query("""
        WITH RECURSIVE Community AS (
            -- Bázis eset: a kezdőpont közvetlen kapcsolatai
            SELECT from_id as node, to_id as peer, 1 as depth
            FROM narrative_connections 
            WHERE from_id = :seedId OR to_id = :seedId
            
            UNION
            
            -- Rekurzív lépés: a szomszédok szomszédai
            SELECT 
                c.from_id, c.to_id, cm.depth + 1
            FROM narrative_connections c
            JOIN Community cm ON (c.from_id = cm.peer OR c.to_id = cm.peer)
            WHERE cm.depth < :maxDepth 
              AND c.strength >= :minStrength
        )
        SELECT DISTINCT 
            CASE WHEN node = :seedId THEN peer ELSE node END 
        FROM Community 
        WHERE node != :seedId OR peer != :seedId
    """)
    suspend fun discoverNetwork(seedId: Long, maxDepth: Int = 3, minStrength: Float = 0.4f): List<Long>

    @Query("SELECT COUNT(*) FROM narrative_connections")
    suspend fun getCount(): Int

    @Query("DELETE FROM narrative_connections")
    suspend fun clearAll()
}
