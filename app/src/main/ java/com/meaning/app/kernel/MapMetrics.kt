
package com.meaning.app.kernel

data class MapMetrics(
    val pointCount: Int,
    val connectionCount: Int,
    val averageDensity: Float,
    val processingTimeMs: Long = 0,
    val compressionRatio: Float = 1.0f,      // Kvantálási arány
    val memoryUsageKB: Float = 0f,           // Memória használat
    val searchSpeedMs: Float = 0f,           // Keresési sebesség
    val temporalVictory: Float = 0f          // Időmegtakarítás százalékban
) {
    val pointsPerSecond: Float get() = 
        if (processingTimeMs > 0) pointCount / (processingTimeMs / 1000f) else 0f
    
    val connectionsPerPoint: Float get() = 
        if (pointCount > 0) connectionCount.toFloat() / pointCount else 0f
}
