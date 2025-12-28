package com.meaning.app.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

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
    
    // Forrás entitás alapján
    @Query("SELECT * FROM narrative_connections WHERE fromId = :entityId")
    suspend fun getByFromId(entityId: Long): List<NarrativeConnectionEntity>
    
    // Cél entitás alapján
    @Query("SELECT * FROM narrative_connections WHERE toId = :entityId")
    suspend fun getByToId(entityId: Long): List<NarrativeConnectionEntity>
    
    // Mindkét irány
    @Query("""
        SELECT * FROM narrative_connections 
        WHERE fromId = :entityId OR toId = :entityId
    """)
    suspend fun getConnectionsForEntity(entityId: Long): List<NarrativeConnectionEntity>
    
    // Kapcsolattípus alapján
    @Query("SELECT * FROM narrative_connections WHERE connectionType = :type")
    suspend fun getByType(type: String): List<NarrativeConnectionEntity>
    
    // Erősség alapján szűrve
    @Query("SELECT * FROM narrative_connections WHERE strength >= :minStrength")
    suspend fun getByMinStrength(minStrength: Float): List<NarrativeConnectionEntity>
    
    // Két entitás között
    @Query("""
        SELECT * FROM narrative_connections 
        WHERE (fromId = :entityId1 AND toId = :entityId2) 
           OR (fromId = :entityId2 AND toId = :entityId1)
        LIMIT 1
    """)
    suspend fun getConnectionBetween(
        entityId1: Long,
        entityId2: Long
    ): NarrativeConnectionEntity?
    
    // Legújabb kapcsolatok
    @Query("SELECT * FROM narrative_connections ORDER BY creationDate DESC LIMIT :limit")
    suspend fun getRecent(limit: Int = 20): List<NarrativeConnectionEntity>
    
    // === STATISZTIKÁK ===
    
    @Query("SELECT COUNT(*) FROM narrative_connections")
    suspend fun getCount(): Long
    
    @Query("SELECT COUNT(DISTINCT connectionType) FROM narrative_connections")
    suspend fun getTypeCount(): Int
    
    @Query("SELECT AVG(strength) FROM narrative_connections")
    suspend fun getAverageStrength(): Float
    
    @Query("SELECT connectionType, COUNT(*) as count FROM narrative_connections GROUP BY connectionType")
    suspend fun getTypeDistribution(): List<TypeCount>
    
    @Query("""
        SELECT fromId, toId, strength 
        FROM narrative_connections 
        WHERE strength > :threshold 
        ORDER BY strength DESC 
        LIMIT :limit
    """)
    suspend fun getStrongestConnections(
        threshold: Float = 0.5f,
        limit: Int = 10
    ): List<StrongConnection>
    
    // === BATCH MŰVELETEK ===
    
    @Insert
    suspend fun insertAll(connections: List<NarrativeConnectionEntity>): List<Long>
    
    @Query("DELETE FROM narrative_connections WHERE fromId = :entityId OR toId = :entityId")
    suspend fun deleteConnectionsForEntity(entityId: Long)
    
    @Query("DELETE FROM narrative_connections WHERE strength < :minStrength")
    suspend fun deleteWeakConnections(minStrength: Float = 0.2f)
    
    @Query("DELETE FROM narrative_connections")
    suspend fun deleteAll()
    
    // === KOMPLEX LEKÉRDEZÉSEK ===
    
    // Átlós kapcsolatok (kör keresése)
    @Query("""
        WITH RECURSIVE ConnectionPath AS (
            SELECT fromId, toId, strength, connectionType, 
                   1 as depth, CAST(fromId AS TEXT) || '->' || CAST(toId AS TEXT) as path
            FROM narrative_connections 
            WHERE fromId = :startId
            
            UNION ALL
            
            SELECT c.fromId, c.toId, c.strength, c.connectionType,
                   cp.depth + 1, cp.path || '->' || CAST(c.toId AS TEXT)
            FROM narrative_connections c
            INNER JOIN ConnectionPath cp ON c.fromId = cp.toId
            WHERE cp.depth < :maxDepth 
              AND c.toId != :startId
              AND cp.path NOT LIKE '%' || CAST(c.toId AS TEXT) || '%'
        )
        SELECT * FROM ConnectionPath WHERE toId = :endId
    """)
    suspend fun findConnectionPath(
        startId: Long,
        endId: Long,
        maxDepth: Int = 3
    ): List<ConnectionPath>
    
    // Közösség felderítése (clustering)
    @Query("""
        WITH RECURSIVE Community AS (
            SELECT fromId as node, 1 as level
            FROM narrative_connections 
            WHERE fromId = :seedId OR toId = :seedId
            
            UNION
            
            SELECT 
                CASE WHEN c.fromId = cm.node THEN c.toId ELSE c.fromId END as node,
                cm.level + 1
            FROM narrative_connections c
            JOIN Community cm ON c.fromId = cm.node OR c.toId = cm.node
            WHERE cm.level < :maxLevel 
              AND c.strength >= :minStrength
        )
        SELECT DISTINCT node FROM Community
    """)
    suspend fun discoverCommunity(
        seedId: Long,
        maxLevel: Int = 2,
        minStrength: Float = 0.3f
    ): List<Long>
}

// === SEGÉD ADATOSZTÁLYOK ===

data class TypeCount(
    val connectionType: String,
    val count: Int
)

data class StrongConnection(
    val fromId: Long,
    val toId: Long,
    val strength: Float
)

data class ConnectionPath(
    val fromId: Long,
    val toId: Long,
    val strength: Float,
    val connectionType: String,
    val depth: Int,
    val path: String
)

// === REPOSITORY ===

class NarrativeConnectionRepository(
    private val dao: NarrativeConnectionDao,
    private val narrativeDao: NarrativeDao
) {
    
    suspend fun createConnection(
        fromId: Long,
        toId: Long,
        type: String,
        strength: Float = calculateAutoStrength(fromId, toId)
    ): Long {
        val connection = NarrativeConnectionEntity(
            fromId = fromId,
            toId = toId,
            connectionType = type,
            strength = strength
        )
        return dao.insert(connection)
    }
    
    suspend fun getConnectionNetwork(entityId: Long): ConnectionNetwork {
        val directConnections = dao.getConnectionsForEntity(entityId)
        val connectedEntities = mutableSetOf<Long>()
        val connectionsByType = mutableMapOf<String, Int>()
        
        directConnections.forEach { connection ->
            connectedEntities.add(if (connection.fromId == entityId) connection.toId else connection.fromId)
            connectionsByType[connection.connectionType] = 
                connectionsByType.getOrDefault(connection.connectionType, 0) + 1
        }
        
        // Második szintű kapcsolatok
        val secondLevel = mutableSetOf<Long>()
        connectedEntities.forEach { connectedId ->
            val secondary = dao.getConnectionsForEntity(connectedId)
            secondary.forEach { conn ->
                val otherId = if (conn.fromId == connectedId) conn.toId else conn.fromId
                if (otherId != entityId) {
                    secondLevel.add(otherId)
                }
            }
        }
        
        return ConnectionNetwork(
            centerEntityId = entityId,
            directConnections = directConnections,
            connectedEntityIds = connectedEntities.toList(),
            secondLevelEntityIds = secondLevel.toList(),
            connectionTypeDistribution = connectionsByType,
            averageStrength = directConnections.map { it.strength }.average().toFloat()
        )
    }
    
    suspend fun findBridgeConnections(): List<NarrativeConnectionEntity> {
        // Kapcsolatok, amelyek különböző közösségeket kötnek össze
        return dao.getAll().filter { connection ->
            val fromCommunity = dao.discoverCommunity(connection.fromId, maxLevel = 1)
            val toCommunity = dao.discoverCommunity(connection.toId, maxLevel = 1)
            
            // Csak azokat, ahol a közösségek kevés átfedéssel rendelkeznek
            val intersection = fromCommunity.intersect(toCommunity.toSet()).size
            intersection < 2 && connection.strength > 0.4f
        }
    }
    
    suspend fun calculateGraphDensity(): Float {
        val totalConnections = dao.getCount()
        val totalEntities = narrativeDao.getCount()
        
        if (totalEntities < 2) return 0f
        
        // Lehetséges maximális kapcsolatok száma n*(n-1)/2
        val maxPossible = totalEntities * (totalEntities - 1) / 2
        return totalConnections.toFloat() / maxPossible.toFloat()
    }
    
    private suspend fun calculateAutoStrength(fromId: Long, toId: Long): Float {
        val entity1 = narrativeDao.getById(fromId)
        val entity2 = narrativeDao.getById(toId)
        
        if (entity1 == null || entity2 == null) return 0.5f
        
        // Erősség számítása több faktor alapján
        var strength = 0f
        
        // 1. Metafora család egyezés
        if (entity1.metaphorFamily == entity2.metaphorFamily) {
            strength += 0.3f
        }
        
        // 2. Térbeli közelség
        val distance = kotlin.math.sqrt(
            (entity1.coordX - entity2.coordX).pow(2) +
            (entity1.coordY - entity2.coordY).pow(2) +
            (entity1.coordZ - entity2.coordZ).pow(2)
        )
        strength += (1.0f - distance.coerceIn(0f, 1f)) * 0.4f
        
        // 3. Sémantikus sűrűség hasonlóság
        val densityDiff = kotlin.math.abs(entity1.semanticDensity - entity2.semanticDensity)
        strength += (1.0f - densityDiff) * 0.3f
        
        return strength.coerceIn(0.1f, 1.0f)
    }
}

data class ConnectionNetwork(
    val centerEntityId: Long,
    val directConnections: List<NarrativeConnectionEntity>,
    val connectedEntityIds: List<Long>,
    val secondLevelEntityIds: List<Long>,
    val connectionTypeDistribution: Map<String, Int>,
    val averageStrength: Float
)
