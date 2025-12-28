package com.meaning.app.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface NarrativeConnectionDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(connection: NarrativeConnectionEntity): Long
    
    @Query("SELECT * FROM narrative_connections")
    fun getAllStream(): Flow<List<NarrativeConnectionEntity>>
    
    @Query("SELECT * FROM narrative_connections WHERE from_id = :entityId OR to_id = :entityId")
    suspend fun getConnectionsForEntity(entityId: Long): List<NarrativeConnectionEntity>
    
    @Query("SELECT COUNT(*) FROM narrative_connections")
    suspend fun getCount(): Long
    
    @Delete
    suspend fun delete(connection: NarrativeConnectionEntity)
    
    // Speciális: Közösségkeresés (Community Detection) a gráfban
    @Query("""
        WITH RECURSIVE Community AS (
            SELECT from_id as node, 1 as level
            FROM narrative_connections 
            WHERE from_id = :seedId OR to_id = :seedId
            
            UNION
            
            SELECT 
                CASE WHEN c.from_id = cm.node THEN c.to_id ELSE c.from_id END as node,
                cm.level + 1
            FROM narrative_connections c
            JOIN Community cm ON c.from_id = cm.node OR c.to_id = cm.node
            WHERE cm.level < :maxLevel 
              AND c.strength >= :minStrength
        )
        SELECT DISTINCT node FROM Community
    """)
    suspend fun discoverCommunity(seedId: Long, maxLevel: Int = 2, minStrength: Float = 0.3f): List<Long>
}
