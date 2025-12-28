package com.meaning.app.kernel

import com.meaning.app.db.QuantizedNarrativeEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.math.*

/**
 * 3D NARRATÍV TÉRKÉP - A RENDSZER AGYA
 * Kezeli a térbeli transzformációkat, a navigációt és a gráfelemzést.
 */
data class NarrativeMap3D(
    val id: String = generateMapId(),
    val points: List<QuantizedNarrativeEntity>,
    val connections: List<NarrativeConnection>,
    val center: QuantizedNarrativeEntity?,
    val metrics: MapMetrics,
    val cameraState: CameraState = CameraState(),
    val viewSettings: ViewSettings = ViewSettings(),
    val timestamp: Long = System.currentTimeMillis()
) {
    
    // === KERESÉS ÉS LEKÉRDEZÉS ===
    fun getPointById(id: Long): QuantizedNarrativeEntity? = points.find { it.id == id }
    
    fun getConnectionsForPoint(pointId: Long): List<NarrativeConnection> = 
        connections.filter { it.fromId == pointId || it.toId == pointId }
    
    fun getNeighbors(pointId: Long): List<QuantizedNarrativeEntity> {
        val neighborIds = connections
            .filter { it.fromId == pointId || it.toId == pointId }
            .map { if (it.fromId == pointId) it.toId else it.fromId }
            .toSet()
        return points.filter { it.id in neighborIds }
    }

    // === 3D MATEK (FORGATÁS, SKÁLÁZÁS) ===
    fun rotateY(angle: Float): NarrativeMap3D {
        val rad = angle * PI.toFloat() / 180f
        val cos = cos(rad)
        val sin = sin(rad)
        val rotatedPoints = points.map { entity ->
            val x = entity.coordX * cos + entity.coordZ * sin
            val z = -entity.coordX * sin + entity.coordZ * cos
            entity.copy(coordX = x, coordZ = z)
        }
        return this.copy(points = rotatedPoints)
    }

    fun rotateX(angle: Float): NarrativeMap3D {
        val rad = angle * PI.toFloat() / 180f
        val cos = cos(rad)
        val sin = sin(rad)
        val rotatedPoints = points.map { entity ->
            val y = entity.coordY * cos - entity.coordZ * sin
            val z = entity.coordY * sin + entity.coordZ * cos
            entity.copy(coordY = y, coordZ = z)
        }
        return this.copy(points = rotatedPoints)
    }

    // === GRÁF ANALÍZIS (Dijkstra útvonalkereső) ===
    fun findShortestPath(startId: Long, endId: Long): List<Long> {
        if (startId == endId) return listOf(startId)
        val distances = mutableMapOf<Long, Float>()
        val previous = mutableMapOf<Long, Long>()
        val unvisited = points.map { it.id }.toMutableSet()
        
        points.forEach { distances[it.id] = Float.MAX_VALUE }
        distances[startId] = 0f
        
        while (unvisited.isNotEmpty()) {
            val current = unvisited.minByOrNull { distances[it] ?: Float.MAX_VALUE } ?: break
            if (current == endId || distances[current] == Float.MAX_VALUE) break
            unvisited.remove(current)
            
            connections.filter { it.fromId == current || it.toId == current }.forEach { conn ->
                val neighbor = if (conn.fromId == current) conn.toId else conn.fromId
                if (neighbor in unvisited) {
                    // Súlyozás: 1.0 - erősség (minél erősebb a kapcsolat, annál "rövidebb" az út)
                    val alt = distances[current]!! + (1.0f - conn.strength)
                    if (alt < (distances[neighbor] ?: Float.MAX_VALUE)) {
                        distances[neighbor] = alt
                        previous[neighbor] = current
                    }
                }
            }
        }
        val path = mutableListOf<Long>()
        var curr: Long? = endId
        while (curr != null) {
            path.add(0, curr)
            curr = previous[curr]
        }
        return if (path.firstOrNull() == startId) path else emptyList()
    }

    companion object {
        fun generateMapId(): String = "map_${System.currentTimeMillis()}"

        // Statikus gyár a Flow-hoz
        fun fromEntities(entities: List<QuantizedNarrativeEntity>): NarrativeMap3D {
            val connections = generateConnections(entities)
            return NarrativeMap3D(
                points = entities,
                connections = connections,
                center = entities.firstOrNull(),
                metrics = MapMetrics(
                    pointCount = entities.size,
                    connectionCount = connections.size,
                    averageDensity = if (entities.isNotEmpty()) entities.map { it.semanticDensity }.average().toFloat() else 0f
                )
            )
        }

        private fun generateConnections(entities: List<QuantizedNarrativeEntity>): List<NarrativeConnection> {
            val connections = mutableListOf<NarrativeConnection>()
            entities.forEachIndexed { i, entityA ->
                entities.filterIndexed { j, _ -> i != j }
                    .sortedBy { entityB -> calculateDist(entityA, entityB) }
                    .take(3) // Minden ponthoz a 3 legközelebbit kötjük be
                    .forEach { entityB ->
                        val d = calculateDist(entityA, entityB)
                        connections.add(
                            NarrativeConnection(
                                id = 0,
                                fromId = entityA.id,
                                toId = entityB.id,
                                strength = 0.7f, 
                                connectionType = "auto_link",
                                distance3D = d,
                                semanticSimilarity = 0.7f,
                                creationTime = System.currentTimeMillis()
                            )
                        )
                    }
            }
            return connections
        }

        private fun calculateDist(a: QuantizedNarrativeEntity, b: QuantizedNarrativeEntity): Float {
            return sqrt((a.coordX - b.coordX).pow(2) + (a.coordY - b.coordY).pow(2) + (a.coordZ - b.coordZ).pow(2))
        }
    }
}

// === HIÁNYZÓ UI ÁLLAPOTOK (Csak ami ide kell) ===

data class CameraState(
    val position: Point3D = Point3D(0f, 0f, -5f),
    val rotation: Point3D = Point3D(0f, 0f, 0f),
    val zoom: Float = 1.0f
)

data class ViewSettings(
    val showLabels: Boolean = true,
    val pointSize: Float = 2.0f,
    val colorMode: String = "FAMILY",
    val showConnections: Boolean = true
)

data class Point3D(val x: Float, val y: Float, val z: Float)
