package com.meaning.app.kernel

import com.meaning.app.db.NarrativeDao
import com.meaning.app.db.QuantizedNarrativeEntity
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.pow
import kotlin.math.sqrt
import kotlin.system.measureTimeMillis

class NarrativeKernel(
    private val dao: NarrativeDao,
    private val useNeon: Boolean = true,
    private val parallelDegree: Int = Runtime.getRuntime().availableProcessors()
) {
    
    private val computationScope = CoroutineScope(
        Dispatchers.Default + SupervisorJob()
    )
    
    // Metrikák
    private val queriesProcessed = AtomicInteger(0)
    private val totalProcessingTime = AtomicInteger(0)
    
    // === KERESÉS ===
    suspend fun findNearest(
        queryVector: FloatArray,
        k: Int = 10,
        minSimilarity: Float = 0.6f
    ): List<SearchResult> = withContext(Dispatchers.IO) {
        
        val startTime = System.nanoTime()
        
        // Kvantálás
        val quantizedQuery = QuantizationEngine.quantizeToINT8(queryVector)
        
        // Összes vektor betöltése
        val allEntities = dao.getAllStream().first()
        
        // Párhuzamos keresés
        val chunkSize = maxOf(1, allEntities.size / parallelDegree)
        val chunks = allEntities.chunked(chunkSize)
        
        val results = chunks.map { chunk ->
            computationScope.async {
                processSearchChunk(quantizedQuery, chunk, minSimilarity)
            }
        }.awaitAll().flatten()
        
        // Rendezés és limit
        val sortedResults = results
            .sortedByDescending { it.similarity }
            .take(k)
        
        // Metrikák frissítése
        val processingTime = System.nanoTime() - startTime
        queriesProcessed.incrementAndGet()
        totalProcessingTime.addAndGet(processingTime.toInt())
        
        logMetrics(allEntities.size, sortedResults.size, processingTime)
        
        return@withContext sortedResults
    }
    
    private fun processSearchChunk(
        query: ByteArray,
        chunk: List<QuantizedNarrativeEntity>,
        minSimilarity: Float
    ): List<SearchResult> {
        val results = mutableListOf<SearchResult>()
        
        chunk.forEach { entity ->
            val similarity = QuantizationEngine.calculateSimilarity(query, entity.vectorInt8)
            
            if (similarity >= minSimilarity) {
                results.add(
                    SearchResult(
                        entity = entity,
                        similarity = similarity,
                        distance3D = calculate3DDistance(entity, query)
                    )
                )
            }
        }
        
        return results
    }
    
    // === 3D TÉRKÉP GENERÁLÁS ===
    suspend fun generate3DMap(
        centerEntityId: Long? = null,
        radius: Float = 1.0f,
        maxPoints: Int = 100
    ): NarrativeMap3D {
        
        val center = centerEntityId?.let { dao.getById(it) }
        
        val points = if (center != null) {
            // Középpont körüli tér
            dao.getInBoundingBox(
                minX = center.coordX - radius,
                maxX = center.coordX + radius,
                minY = center.coordY - radius,
                maxY = center.coordY + radius,
                minZ = center.coordZ - radius,
                maxZ = center.coordZ + radius,
                limit = maxPoints
            )
        } else {
            // Véletlen tér
            dao.getInBoundingBox(-1f, 1f, -1f, 1f, -1f, 1f, maxPoints)
        }
        
        // Kapcsolatok generálása
        val connections = generateConnections(points)
        
        return NarrativeMap3D(
            points = points,
            connections = connections,
            center = center,
            metrics = MapMetrics(
                pointCount = points.size,
                connectionCount = connections.size,
                averageDensity = points.map { it.semanticDensity }.average().toFloat()
            )
        )
    }
    
    // === KAPCSOLAT GENERÁLÁS ===
    private fun generateConnections(
        points: List<QuantizedNarrativeEntity>,
        maxConnections: Int = 5
    ): List<NarrativeConnection> {
        val connections = mutableListOf<NarrativeConnection>()
        
        points.forEachIndexed { i, entityA ->
            // Legközelebbi szomszédok keresése 3D térben
            val neighbors = points
                .filterIndexed { j, _ -> j != i }
                .sortedBy { entityB ->
                    calculateEuclideanDistance(entityA, entityB)
                }
                .take(maxConnections)
            
            neighbors.forEach { entityB ->
                val similarity = QuantizationEngine.calculateSimilarity(
                    entityA.vectorInt8,
                    entityB.vectorInt8
                )
                
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
                            distance3D = calculateEuclideanDistance(entityA, entityB)
                        )
                    )
                }
            }
        }
        
        return connections
    }
    
    // === REAL-TIME STREAM ===
    fun observeNarrativeSpace(): Flow<NarrativeMap3D> {
        return dao.getAllStream().map { entities ->
            val connections = generateConnections(entities.take(50))
            NarrativeMap3D(
                points = entities,
                connections = connections,
                center = null,
                metrics = MapMetrics(
                    pointCount = entities.size,
                    connectionCount = connections.size,
                    averageDensity = entities.map { it.semanticDensity }.average().toFloat()
                )
            )
        }
    }
    
    // === SEGÉDFÜGGVÉNYEK ===
    private fun calculateEuclideanDistance(
        a: QuantizedNarrativeEntity,
        b: QuantizedNarrativeEntity
    ): Float {
        val dx = a.coordX - b.coordX
        val dy = a.coordY - b.coordY
        val dz = a.coordZ - b.coordZ
        return sqrt(dx * dx + dy * dy + dz * dz)
    }
    
    private fun calculate3DDistance(entity: QuantizedNarrativeEntity, query: ByteArray): Float {
        // Egyszerűsített 3D távolság becslés
        val vector = QuantizationEngine.dequantizeFromINT8(
            entity.vectorInt8,
            entity.semanticDensity * 10f // Becsült max
        )
        
        val queryFloat = QuantizationEngine.dequantizeFromINT8(
            query,
            vector.maxOfOrNull { kotlin.math.abs(it) } ?: 1.0f
        )
        
        var sum = 0.0f
        for (i in vector.indices) {
            val diff = vector[i] - queryFloat[i]
            sum += diff * diff
        }
        
        return sqrt(sum)
    }
    
    private fun determineConnectionType(familyA: String, familyB: String): String {
        return when {
            familyA == familyB -> "intra_family"
            setOf(familyA, familyB) == setOf("természet", "érzelem") -> "nature_emotion"
            setOf(familyA, familyB).contains("absztrakt") -> "abstract_link"
            else -> "general"
        }
    }
    
    private fun logMetrics(
        totalVectors: Int,
        resultsFound: Int,
        processingTime: Long
    ) {
        val avgTime = totalProcessingTime.get() / queriesProcessed.get().toFloat()
        
        println("""
            🔥 NARRATÍV KERNEL 🔥
            Összes vektor: $totalVectors
            Találatok: $resultsFound
            Feldolgozási idő: ${processingTime / 1_000_000}ms
            Átlagos idő: ${avgTime / 1_000_000}ms
            Párhuzamos fokozat: $parallelDegree
            NEON használat: $useNeon
        """.trimIndent())
    }
}

// === ADAT STRUKTÚRÁK ===
data class SearchResult(
    val entity: QuantizedNarrativeEntity,
    val similarity: Float,
    val distance3D: Float
)

data class NarrativeMap3D(
    val points: List<QuantizedNarrativeEntity>,
    val connections: List<NarrativeConnection>,
    val center: QuantizedNarrativeEntity?,
    val metrics: MapMetrics
)

data class NarrativeConnection(
    val fromId: Long,
    val toId: Long,
    val strength: Float,
    val connectionType: String,
    val distance3D: Float
)

data class MapMetrics(
    val pointCount: Int,
    val connectionCount: Int,
    val averageDensity: Float
)
