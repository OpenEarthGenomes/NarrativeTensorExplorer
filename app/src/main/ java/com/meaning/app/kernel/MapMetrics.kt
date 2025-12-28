package com.meaning.app.kernel

data class MapMetrics(
    val pointCount: Int,
    val connectionCount: Int,
    val averageDensity: Float,
    val processingTimeMs: Long = 0,
    val graphDensity: Float = 0f,
    val averagePathLength: Float = 0f,
    val clusteringCoefficient: Float = 0f,
    val entropy: Float = 0f
) {
    fun getComplexityScore(): Float {
        if (pointCount == 0) return 0f
        return (connectionCount.toFloat() / pointCount.toFloat()) * averageDensity
    }

    override fun toString(): String {
        return "MapMetrics(Nodes: $pointCount, Edges: $connectionCount, Density: ${String.format("%.2f", graphDensity)})"
    }
}
