package com.meaning.app.kernel

import com.meaning.app.db.QuantizedNarrativeEntity

data class SearchResult(val entity: QuantizedNarrativeEntity, val similarity: Float, val distance3D: Float)
data class NarrativeMap3D(val points: List<QuantizedNarrativeEntity>, val connections: List<NarrativeConnection>, val center: QuantizedNarrativeEntity?, val metrics: MapMetrics)
data class NarrativeConnection(val fromId: Long, val toId: Long, val strength: Float, val connectionType: String, val distance3D: Float)
data class MapMetrics(val pointCount: Int, val connectionCount: Int, val averageDensity: Float, val processingTimeMs: Long = 0)
