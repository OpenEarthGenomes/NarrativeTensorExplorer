package com.meaning.app.kernel

import com.meaning.app.db.NarrativeDao
import com.meaning.app.db.QuantizedNarrativeEntity
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.sqrt

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
        
        // Kvantálás (Int8 konverzió a gyorsításhoz)
        val quantizedQuery = QuantizationEngine.quantizeToINT8(queryVector)
        
        // Összes vektor betöltése (később optimalizálható chunk-olt betöltésre)
        val allEntities = dao.getAllStream().first()
        
        // Párhuzamos keresés (Chunk-okra osztva a processzor magok szerint)
        val chunkSize = maxOf(1, allEntities.size / parallelDegree)
        val chunks = allEntities.chunked(chunkSize)
        
        val results = chunks.map { chunk ->
            computationScope.async {
                processSearchChunk(quantizedQuery, chunk, minSimilarity)
            }
        }.awaitAll().flatten()
        
        // Rendezés és a legjobb 'k' találat kiválasztása
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
            // Itt használjuk a NEON-optimalizált hasonlóság számítást
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
            // Középpont körüli entitások lekérése
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
            // Véletlen minta az erdőből, ha nincs középpont
            dao.getInBoundingBox(-1f, 1f, -1f, 1f, -1f, 1f, maxPoints)
        }
        
        // Dinamikus kapcsolatok generálása a megjelenített pontok között
        val connections = generateConnections(points)
        
        return NarrativeMap3D(
            points = points,
            connections = connections,
            center = center,
            metrics = MapMetrics(
                pointCount = points.size,
                connectionCount = connections.size,
                averageDensity = if (points.isNotEmpty()) points.map { it.semanticDensity }.average().toFloat() else 0f
            )
        )
    }
    
    // === KAPCSOLAT GENERÁLÁS (DINAMIKUS) ===
    private fun generateConnections(
        points: List<QuantizedNarrativeEntity>,
        maxConnections: Int = 5
    ): List<NarrativeConnection> {
        val connections = mutableListOf<NarrativeConnection>()
        
        points.forEachIndexed { i, entityA ->
            // Legközelebbi szomszédok keresése 3D euklideszi távolság alapján
            val neighbors = points
                .filterIndexed { j, _ -> j != i }
                .sortedBy { entityB ->
                    calculateEuclideanDistance(entityA, entityB)
                }
                .take(maxConnections)
            
            neighbors.forEach { entityB ->
                // Sémantikai hasonlóság számítása
                val similarity = QuantizationEngine.calculateSimilarity(
                    entityA.vectorInt8,
                    entityB.vectorInt8
                )
                
                // Csak akkor kötjük össze, ha van értelme (hasonlóság > 0.5)
                if (similarity > 0.5f) {
                    val dist3D = calculateEuclideanDistance(entityA, entityB)
                    
                    connections.add(
                        NarrativeConnection(
                            id = 0, // Memóriában lévő kapcsolat, nincs DB ID-ja
                            fromId = entityA.id,
                            toId = entityB.id,
                            strength = similarity,
                            connectionType = determineConnectionType(
                                entityA.metaphorFamily,
                                entityB.metaphorFamily
                            ),
                            distance3D = dist3D,
                            semanticSimilarity = similarity,
                            creationTime = System.currentTimeMillis(),
                            usageCount = 0
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
            // Teljesítmény okokból csak az első 50 elemre generálunk kapcsolatokat
            val activeEntities = entities.take(50)
            val connections = generateConnections(activeEntities)
            
            NarrativeMap3D(
                points = entities, // De az összes pontot visszaadjuk
                connections = connections,
                center = null,
                metrics = MapMetrics(
                    pointCount = entities.size,
                    connectionCount = connections.size,
                    averageDensity = if (entities.isNotEmpty()) entities.map { it.semanticDensity }.average().toFloat() else 0f
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
        // Dekvantálás a pontosabb távolságszámításhoz (becslés)
        val vector = QuantizationEngine.dequantizeFromINT8(
            entity.vectorInt8,
            entity.semanticDensity * 10f // Becsült skála
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
        val avgTime = if (queriesProcessed.get() > 0) 
            totalProcessingTime.get() / queriesProcessed.get().toFloat() 
        else 0f
        
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
