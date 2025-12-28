package com.meaning.app.db

import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt

class NarrativeConnectionRepository(
    private val dao: NarrativeConnectionDao,
    private val narrativeDao: NarrativeDao
) {
    
    suspend fun createConnection(
        fromId: Long,
        toId: Long,
        type: String,
        strength: Float? = null
    ): Long {
        val finalStrength = strength ?: calculateAutoStrength(fromId, toId)
        val connection = NarrativeConnectionEntity(
            fromId = fromId,
            toId = toId,
            connectionType = type,
            strength = finalStrength
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
        
        val secondLevel = mutableSetOf<Long>()
        connectedEntities.forEach { connectedId ->
            val secondary = dao.getConnectionsForEntity(connectedId)
            secondary.forEach { conn ->
                val otherId = if (conn.fromId == connectedId) conn.toId else conn.fromId
                if (otherId != entityId) secondLevel.add(otherId)
            }
        }
        
        return ConnectionNetwork(
            centerEntityId = entityId,
            directConnections = directConnections,
            connectedEntityIds = connectedEntities.toList(),
            secondLevelEntityIds = secondLevel.toList(),
            connectionTypeDistribution = connectionsByType,
            averageStrength = if (directConnections.isNotEmpty()) directConnections.map { it.strength }.average().toFloat() else 0f
        )
    }
    
    suspend fun findBridgeConnections(): List<NarrativeConnectionEntity> {
        val allConnections = dao.getAll()
        return allConnections.filter { connection ->
            val fromCommunity = dao.discoverCommunity(connection.fromId, maxLevel = 1)
            val toCommunity = dao.discoverCommunity(connection.toId, maxLevel = 1)
            val intersection = fromCommunity.intersect(toCommunity.toSet()).size
            intersection < 2 && connection.strength > 0.4f
        }
    }
    
    suspend fun calculateGraphDensity(): Float {
        val totalConnections = dao.getCount()
        val totalEntities = narrativeDao.getCount()
        if (totalEntities < 2) return 0f
        val maxPossible = totalEntities * (totalEntities - 1) / 2
        return totalConnections.toFloat() / maxPossible.toFloat()
    }
    
    private suspend fun calculateAutoStrength(fromId: Long, toId: Long): Float {
        val entity1 = narrativeDao.getById(fromId)
        val entity2 = narrativeDao.getById(toId)
        if (entity1 == null || entity2 == null) return 0.5f
        
        var strength = 0f
        if (entity1.metaphorFamily == entity2.metaphorFamily) strength += 0.3f
        
        val distance = sqrt(
            (entity1.coordX - entity2.coordX).pow(2) +
            (entity1.coordY - entity2.coordY).pow(2) +
            (entity1.coordZ - entity2.coordZ).pow(2)
        )
        strength += (1.0f - distance.coerceIn(0f, 1f)) * 0.4f
        
        val densityDiff = abs(entity1.semanticDensity - entity2.semanticDensity)
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
