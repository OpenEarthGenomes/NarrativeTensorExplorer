package com.meaning.app.kernel

import com.meaning.app.db.QuantizedNarrativeEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.math.*

/**
 * 3D NARRATÍV TÉRKÉP TELJES IMPLEMENTÁCIÓ
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
    
    fun getPointById(id: Long): QuantizedNarrativeEntity? {
        return points.find { it.id == id }
    }
    
    fun getConnectionsForPoint(pointId: Long): List<NarrativeConnection> {
        return connections.filter { it.fromId == pointId || it.toId == pointId }
    }
    
    fun getNeighbors(pointId: Long): List<QuantizedNarrativeEntity> {
        val neighborIds = connections
            .filter { it.fromId == pointId || it.toId == pointId }
            .map { if (it.fromId == pointId) it.toId else it.fromId }
            .toSet()
        
        return points.filter { it.id in neighborIds }
    }
    
    fun calculateCentrality(pointId: Long): Float {
        val degree = connections.count { it.fromId == pointId || it.toId == pointId }
        val totalPoints = points.size
        return if (totalPoints > 1) degree.toFloat() / (totalPoints - 1) else 0f
    }
    
    // === 3D TRANSZFORMÁCIÓK ===
    
    fun translate(dx: Float, dy: Float, dz: Float): NarrativeMap3D {
        val translatedPoints = points.map { entity ->
            entity.copy(
                coordX = entity.coordX + dx,
                coordY = entity.coordY + dy,
                coordZ = entity.coordZ + dz
            )
        }
        
        return this.copy(points = translatedPoints)
    }
    
    fun scale(factor: Float, centerX: Float = 0f, centerY: Float = 0f, centerZ: Float = 0f): NarrativeMap3D {
        val scaledPoints = points.map { entity ->
            entity.copy(
                coordX = centerX + (entity.coordX - centerX) * factor,
                coordY = centerY + (entity.coordY - centerY) * factor,
                coordZ = centerZ + (entity.coordZ - centerZ) * factor
            )
        }
        
        return this.copy(points = scaledPoints)
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
    
    fun rotateZ(angle: Float): NarrativeMap3D {
        val rad = angle * PI.toFloat() / 180f
        val cos = cos(rad)
        val sin = sin(rad)
        
        val rotatedPoints = points.map { entity ->
            val x = entity.coordX * cos - entity.coordY * sin
            val y = entity.coordX * sin + entity.coordY * cos
            
            entity.copy(coordX = x, coordY = y)
        }
        
        return this.copy(points = rotatedPoints)
    }
    
    // === FILTREK ===
    
    fun filterByFamily(family: String): NarrativeMap3D {
        val filteredPoints = points.filter { it.metaphorFamily == family }
        val filteredConnections = connections.filter { connection ->
            val fromPoint = getPointById(connection.fromId)
            val toPoint = getPointById(connection.toId)
            fromPoint?.metaphorFamily == family && toPoint?.metaphorFamily == family
        }
        
        return this.copy(
            points = filteredPoints,
            connections = filteredConnections,
            metrics = metrics.copy(
                pointCount = filteredPoints.size,
                connectionCount = filteredConnections.size
            )
        )
    }
    
    fun filterByDensity(minDensity: Float): NarrativeMap3D {
        val filteredPoints = points.filter { it.semanticDensity >= minDensity }
        val filteredPointIds = filteredPoints.map { it.id }.toSet()
        
        val filteredConnections = connections.filter { connection ->
            connection.fromId in filteredPointIds && connection.toId in filteredPointIds
        }
        
        return this.copy(
            points = filteredPoints,
            connections = filteredConnections,
            metrics = metrics.copy(
                pointCount = filteredPoints.size,
                connectionCount = filteredConnections.size,
                averageDensity = filteredPoints.map { it.semanticDensity }.average().toFloat()
            )
        )
    }
    
    fun filterByBoundingBox(
        minX: Float, maxX: Float,
        minY: Float, maxY: Float,
        minZ: Float, maxZ: Float
    ): NarrativeMap3D {
        val filteredPoints = points.filter { entity ->
            entity.coordX in minX..maxX &&
            entity.coordY in minY..maxY &&
            entity.coordZ in minZ..maxZ
        }
        
        val filteredPointIds = filteredPoints.map { it.id }.toSet()
        val filteredConnections = connections.filter { connection ->
            connection.fromId in filteredPointIds && connection.toId in filteredPointIds
        }
        
        return this.copy(
            points = filteredPoints,
            connections = filteredConnections,
            metrics = metrics.copy(
                pointCount = filteredPoints.size,
                connectionCount = filteredConnections.size
            )
        )
    }
    
    // === GRÁF ANALÍZIS ===
    
    fun findShortestPath(startId: Long, endId: Long): List<Long> {
        if (startId == endId) return listOf(startId)
        
        val distances = mutableMapOf<Long, Float>()
        val previous = mutableMapOf<Long, Long>()
        val unvisited = mutableSetOf<Long>()
        
        points.forEach { entity ->
            distances[entity.id] = Float.MAX_VALUE
            unvisited.add(entity.id)
        }
        distances[startId] = 0f
        
        while (unvisited.isNotEmpty()) {
            val current = unvisited.minByOrNull { distances[it] ?: Float.MAX_VALUE } ?: break
            unvisited.remove(current)
            
            if (current == endId) break
            
            val connectionsFromCurrent = connections.filter { it.fromId == current || it.toId == current }
            
            connectionsFromCurrent.forEach { connection ->
                val neighbor = if (connection.fromId == current) connection.toId else connection.fromId
                val weight = 1f - connection.strength // Kisebb erősség = nagyobb "távolság"
                val alt = (distances[current] ?: Float.MAX_VALUE) + weight
                
                if (alt < (distances[neighbor] ?: Float.MAX_VALUE)) {
                    distances[neighbor] = alt
                    previous[neighbor] = current
                }
            }
        }
        
        // Útvonal visszaállítása
        val path = mutableListOf<Long>()
        var current: Long? = endId
        while (current != null) {
            path.add(0, current)
            current = previous[current]
        }
        
        return if (path.first() == startId) path else emptyList()
    }
    
    fun calculateClusters(minSimilarity: Float = 0.6f): List<Cluster> {
        val visited = mutableSetOf<Long>()
        val clusters = mutableListOf<Cluster>()
        
        points.forEach { entity ->
            if (entity.id !in visited) {
                val cluster = mutableSetOf<Long>()
                val queue = ArrayDeque<Long>()
                queue.add(entity.id)
                
                while (queue.isNotEmpty()) {
                    val currentId = queue.removeFirst()
                    if (currentId in visited) continue
                    
                    visited.add(currentId)
                    cluster.add(currentId)
                    
                    // Szomszédok keresése
                    val neighbors = connections
                        .filter { it.fromId == currentId || it.toId == currentId }
                        .filter { it.strength >= minSimilarity }
                        .map { if (it.fromId == currentId) it.toId else it.fromId }
                    
                    neighbors.forEach { neighborId ->
                        if (neighborId !in visited) {
                            queue.add(neighborId)
                        }
                    }
                }
                
                if (cluster.size >= 2) {
                    clusters.add(Cluster(
                        id = clusters.size,
                        pointIds = cluster.toList(),
                        centerPoint = calculateClusterCenter(cluster),
                        density = calculateClusterDensity(cluster)
                    ))
                }
            }
        }
        
        return clusters
    }
    
    private fun calculateClusterCenter(pointIds: Set<Long>): Point3D {
        val clusterPoints = points.filter { it.id in pointIds }
        val centerX = clusterPoints.map { it.coordX }.average().toFloat()
        val centerY = clusterPoints.map { it.coordY }.average().toFloat()
        val centerZ = clusterPoints.map { it.coordZ }.average().toFloat()
        
        return Point3D(centerX, centerY, centerZ)
    }
    
    private fun calculateClusterDensity(pointIds: Set<Long>): Float {
        val clusterPoints = points.filter { it.id in pointIds }
        return clusterPoints.map { it.semanticDensity }.average().toFloat()
    }
    
    // === EXPORT ===
    
    fun toJson(): String {
        return """
            {
                "id": "$id",
                "timestamp": $timestamp,
                "pointCount": ${points.size},
                "connectionCount": ${connections.size},
                "metrics": {
                    "averageDensity": ${metrics.averageDensity},
                    "processingTimeMs": ${metrics.processingTimeMs}
                },
                "points": [
                    ${points.joinToString(",\n") { point ->
                        """
                        {
                            "id": ${point.id},
                            "term": "${point.term}",
                            "family": "${point.metaphorFamily}",
                            "coordinates": {
                                "x": ${point.coordX},
                                "y": ${point.coordY},
                                "z": ${point.coordZ}
                            },
                            "density": ${point.semanticDensity}
                        }
                        """.trimIndent()
                    }}
                ],
                "connections": [
                    ${connections.joinToString(",\n") { connection ->
                        """
                        {
                            "from": ${connection.fromId},
                            "to": ${connection.toId},
                            "strength": ${connection.strength},
                            "type": "${connection.connectionType}"
                        }
                        """.trimIndent()
                    }}
                ]
            }
        """.trimIndent()
    }
    
    companion object {
        fun generateMapId(): String {
            return "map_${System.currentTimeMillis()}_${(0..9999).random()}"
        }
        
        fun fromFlow(flow: Flow<List<QuantizedNarrativeEntity>>): Flow<NarrativeMap3D> {
            return flow.map { entities ->
                val connections = generateConnections(entities)
                val metrics = MapMetrics(
                    pointCount = entities.size,
                    connectionCount = connections.size,
                    averageDensity = entities.map { it.semanticDensity }.average().toFloat()
                )
                
                NarrativeMap3D(
                    points = entities,
                    connections = connections,
                    center = entities.firstOrNull(),
                    metrics = metrics
                )
            }
        }
        
        private fun generateConnections(entities: List<QuantizedNarrativeEntity>): List<NarrativeConnection> {
            val connections = mutableListOf<NarrativeConnection>()
            
            entities.forEachIndexed { i, entityA ->
                // Csak a legközelebbi N entitással kapcsolódjunk össze
                val nearest = entities
                    .filterIndexed { j, _ -> j != i }
                    .sortedBy { entityB ->
                        calculateDistance(entityA, entityB)
                    }
                    .take(3)
                
                nearest.forEach { entityB ->
                    val distance = calculateDistance(entityA, entityB)
                    val similarity = calculateSemanticSimilarity(entityA, entityB)
                    
                    if (similarity > 0.5f) {
                        connections.add(
                            NarrativeConnection(
                                fromId = entityA.id,
                                toId = entityB.id,
                                strength = similarity,
                                connectionType = determineConnectionType(
                                    entityA.metaphorFamily,
                                    entityB.metaphorFamily
                                ),
                                distance3D = distance
                            )
                        )
                    }
                }
            }
            
            return connections
        }
        
        private fun calculateDistance(a: QuantizedNarrativeEntity, b: QuantizedNarrativeEntity): Float {
            val dx = a.coordX - b.coordX
            val dy = a.coordY - b.coordY
            val dz = a.coordZ - b.coordZ
            return sqrt(dx * dx + dy * dy + dz * dz)
        }
        
        private fun calculateSemanticSimilarity(a: QuantizedNarrativeEntity, b: QuantizedNarrativeEntity): Float {
            // Egyszerűsített hasonlóság számítás
            var score = 0f
            
            // Metafora család egyezés
            if (a.metaphorFamily == b.metaphorFamily) score += 0.4f
            
            // Sűrűség hasonlóság
            val densityDiff = abs(a.semanticDensity - b.semanticDensity)
            score += (1f - densityDiff) * 0.3f
            
            // Koordináta közelség
            val distance = calculateDistance(a, b)
            score += (1f - distance.coerceIn(0f, 1f)) * 0.3f
            
            return score.coerceIn(0f, 1f)
        }
        
        private fun determineConnectionType(familyA: String, familyB: String): String {
            return when {
                familyA == familyB -> "intra_family"
                setOf(familyA, familyB) == setOf("természet", "érzelem") -> "nature_emotion"
                setOf(familyA, familyB) == setOf("absztrakt", "test") -> "abstract_body"
                setOf(familyA, familyB).contains("idő") -> "temporal"
                else -> "cross_domain"
            }
        }
    }
}

// === ADAT STRUKTÚRÁK ===

data class MapMetrics(
    val pointCount: Int,
    val connectionCount: Int,
    val averageDensity: Float,
    val processingTimeMs: Long = 0,
    val graphDensity: Float = 0f,
    val averagePathLength: Float = 0f,
    val clusteringCoefficient: Float = 0f
)

data class CameraState(
    val position: Point3D = Point3D(0f, 0f, -3f),
    val rotation: Point3D = Point3D(0f, 0f, 0f),
    val zoom: Float = 1.0f,
    val fov: Float = 60f
)

data class Point3D(
    val x: Float,
    val y: Float,
    val z: Float
)

data class ViewSettings(
    val showLabels: Boolean = true,
    val showConnections: Boolean = true,
    val connectionThreshold: Float = 0.3f,
    val pointSize: Float = 1.0f,
    val colorBy: ColorMode = ColorMode.FAMILY,
    val animationSpeed: Float = 1.0f
)

enum class ColorMode {
    FAMILY, DENSITY, CENTRALITY, LAYER
}

data class Cluster(
    val id: Int,
    val pointIds: List<Long>,
    val centerPoint: Point3D,
    val density: Float,
    val averageStrength: Float = 0f
)

// === EXTENSION FUNCTIONS ===

fun NarrativeMap3D.getPointCoordinates(pointId: Long): Point3D? {
    val point = getPointById(pointId)
    return point?.let { Point3D(it.coordX, it.coordY, it.coordZ) }
}

fun NarrativeMap3D.getBoundingBox(): BoundingBox {
    if (points.isEmpty()) return BoundingBox(0f, 0f, 0f, 0f, 0f, 0f)
    
    val minX = points.minOf { it.coordX }
    val maxX = points.maxOf { it.coordX }
    val minY = points.minOf { it.coordY }
    val maxY = points.maxOf { it.coordY }
    val minZ = points.minOf { it.coordZ }
    val maxZ = points.maxOf { it.coordZ }
    
    return BoundingBox(minX, maxX, minY, maxY, minZ, maxZ)
}

data class BoundingBox(
    val minX: Float, val maxX: Float,
    val minY: Float, val maxY: Float,
    val minZ: Float, val maxZ: Float
) {
    val width: Float get() = maxX - minX
    val height: Float get() = maxY - minY
    val depth: Float get() = maxZ - minZ
    val center: Point3D get() = Point3D(
        (minX + maxX) / 2,
        (minY + maxY) / 2,
        (minZ + maxZ) / 2
    )
}
