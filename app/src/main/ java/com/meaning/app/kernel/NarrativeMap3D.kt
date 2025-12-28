package com.meaning.app.kernel

import com.meaning.app.db.QuantizedNarrativeEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.math.*

/**
 * 3D NARRATÍV TÉRKÉP - OKOS IMPLEMENTÁCIÓ
 * Tartalmazza a transzformációkat, gráfelméleti keresőt és a kamera állapotát.
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
    
    // === ALAP MŰVELETEK ===
    fun getPointById(id: Long): QuantizedNarrativeEntity? = points.find { it.id == id }
    
    fun getConnectionsForPoint(pointId: Long): List<NarrativeConnection> = 
        connections.filter { it.fromId == pointId || it.toId == pointId }

    // === 3D TRANSZFORMÁCIÓK (A UI-hoz elengedhetetlen) ===
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

    // === GRÁF ANALÍZIS ===
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
                    val alt = distances[current]!! + (1f - conn.strength)
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
        fun generateMapId() = "map_${System.currentTimeMillis()}"

        // Statikus kapcsolat generálás a pontok között
        fun generateConnections(entities: List<QuantizedNarrativeEntity>): List<NarrativeConnection> {
            val connections = mutableListOf<NarrativeConnection>()
            entities.forEachIndexed { i, entityA ->
                entities.filterIndexed { j, _ -> i != j }
                    .sortedBy { entityB -> calculateDist(entityA, entityB) }
                    .take(3)
                    .forEach { entityB ->
                        val d = calculateDist(entityA, entityB)
                        val sim = 0.7f // Egyszerűsített sémantikai hasonlóság
                        connections.add(
                            NarrativeConnection(
                                id = 0,
                                fromId = entityA.id,
                                toId = entityB.id,
                                strength = sim,
                                connectionType = "auto",
                                distance3D = d,
                                semanticSimilarity = sim,
                                creationTime = System.currentTimeMillis(),
                                usageCount = 0
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

// === KIEGÉSZÍTŐ ADATSTRUKTÚRÁK (Ha nem lennének máshol definiálva) ===

data class CameraState(
    val position: Point3D = Point3D(0f, 0f, -5f),
    val rotation: Point3D = Point3D(0f, 0f, 0f),
    val zoom: Float = 1.0f
)

data class Point3D(val x: Float, val y: Float, val z: Float)

data class ViewSettings(
    val showLabels: Boolean = true,
    val pointSize: Float = 2.0f,
    val colorMode: String = "FAMILY"
)

data class BoundingBox(
    val minX: Float, val maxX: Float,
    val minY: Float, val maxY: Float,
    val minZ: Float, val maxZ: Float
)
