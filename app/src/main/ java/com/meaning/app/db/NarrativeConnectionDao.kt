package com.meaning.app.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import kotlin.math.pow
import kotlin.math.sqrt

@Dao
interface NarrativeConnectionDao {
    
    // === ALAP CRUD ===
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(connection: NarrativeConnectionEntity): Long
    
    @Update
    suspend fun update(connection: NarrativeConnectionEntity)
    
    @Delete
    suspend fun delete(connection: NarrativeConnectionEntity)
    
    @Query("DELETE FROM narrative_connections WHERE id = :id")
    suspend fun deleteById(id: Long)
    
    // === LEKÉRDEZÉSEK ===
    
    @Query("SELECT * FROM narrative_connections WHERE id = :id")
    suspend fun getById(id: Long): NarrativeConnectionEntity?
    
    @Query("SELECT * FROM narrative_connections")
    suspend fun getAll(): List<NarrativeConnectionEntity>
    
    @Query("SELECT * FROM narrative_connections")
    fun getAllStream(): Flow<List<NarrativeConnectionEntity>>
    
    @Query("SELECT * FROM narrative_connections WHERE from_id = :entityId")
    suspend fun getByFromId(entityId: Long): List<NarrativeConnectionEntity>
    
    @Query("SELECT * FROM narrative_connections WHERE to_id = :entityId")
    suspend fun getByToId(entityId: Long): List<NarrativeConnectionEntity>
    
    @Query("""
        SELECT * FROM narrative_connections 
        WHERE from_id = :entityId OR to_id = :entityId
    """)
    suspend fun getConnectionsForEntity(entityId: Long): List<NarrativeConnectionEntity>
    
    @Query("SELECT * FROM narrative_connections WHERE connection_type = :type")
    suspend fun getByType(type: String): List<NarrativeConnectionEntity>
    
    @Query("SELECT * FROM narrative_connections WHERE strength >= :minStrength")
    suspend fun getByMinStrength(minStrength: Float): List<NarrativeConnectionEntity>
    
    @Query("""
        SELECT * FROM narrative_connections 
        WHERE (from_id = :entityId1 AND to_id = :entityId2) 
           OR (from_id = :entityId2 AND to_id = :entityId1)
        LIMIT 1
    """)
    suspend fun getConnectionBetween(entityId1: Long, entityId2: Long): NarrativeConnectionEntity?
    
    @Query("SELECT * FROM narrative_connections ORDER BY created_at DESC LIMIT :limit")
    suspend fun getRecent(limit: Int = 20): List<NarrativeConnectionEntity>
    
    // === STATISZTIKÁK ===
    
    @Query("SELECT COUNT(*) FROM narrative_connections")
    suspend fun getCount(): Long
    
    @Query("SELECT COUNT(DISTINCT connection_type) FROM narrative_connections")
    suspend fun getTypeCount(): Int
    
    @Query("SELECT AVG(strength) FROM narrative_connections")
    suspend fun getAverageStrength(): Float
    
    @Query("SELECT connection_type, COUNT(*) as count FROM narrative_connections GROUP BY connection_type")
    suspend fun getTypeDistribution(): List<TypeCount>
    
    @Query("""
        SELECT from_id as fromId, to_id as toId, strength 
        FROM narrative_connections 
        WHERE strength > :threshold 
        ORDER BY strength DESC 
        LIMIT :limit
    """)
    suspend fun getStrongestConnections(threshold: Float = 0.5f, limit: Int = 10): List<StrongConnection>
    
    // === BATCH MŰVELETEK ===
    
    @Insert
    suspend fun insertAll(connections: List<NarrativeConnectionEntity>): List<Long>
    
    @Query("DELETE FROM narrative_connections WHERE from_id = :entityId OR to_id = :entityId")
    suspend fun deleteConnectionsForEntity(entityId: Long)
    
    @Query("DELETE FROM narrative_connections WHERE strength < :minStrength")
    suspend fun deleteWeakConnections(minStrength: Float = 0.2f)
    
    @Query("DELETE FROM narrative_connections")
    suspend fun deleteAll()
    
    // === KOMPLEX GRÁF LEKÉRDEZÉSEK ===
    
    @Query("""
        WITH RECURSIVE ConnectionPath AS (
            SELECT from_id, to_id, strength, connection_type, 
                   1 as depth, CAST(from_id AS TEXT) || '->' || CAST(to_id AS TEXT) as pathStr
            FROM narrative_connections 
            WHERE from_id = :startId
            
            UNION ALL
            
            SELECT c.from_id, c.to_id, c.strength, c.connection_type,
                   cp.depth + 1, cp.pathStr || '->' || CAST(c.to_id AS TEXT)
            FROM narrative_connections c
            INNER JOIN ConnectionPath cp ON c.from_id = cp.to_id
            WHERE cp.depth < :maxDepth 
              AND c.to_id != :startId
              AND cp.pathStr NOT LIKE '%' || CAST(c.to_id AS TEXT) || '%'
        )
        SELECT from_id as fromId, to_id as toId, strength, connection_type as connectionType, depth, pathStr as path 
        FROM ConnectionPath WHERE to_id = :endId
    """)
    suspend fun findConnectionPath(startId: Long, endId: Long, maxDepth: Int = 3): List<ConnectionPath>
    
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

// === SEGÉD ADATOSZTÁLYOK ===

data class TypeCount(val connection_type: String, val count: Int)
data class StrongConnection(val fromId: Long, val toId: Long, val strength: Float)
data class ConnectionPath(val fromId: Long, val toId: Long, val strength: Float, val connectionType: String, val depth: Int, val path: String)

