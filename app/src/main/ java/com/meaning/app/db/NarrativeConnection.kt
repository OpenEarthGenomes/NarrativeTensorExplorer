package com.meaning.app.kernel

data class NarrativeConnection(
    val id: Long = 0,                          // Egyedi azonosító
    val fromId: Long,                          // Forrás entitás ID
    val toId: Long,                            // Cél entitás ID
    val strength: Float,                       // 0.0 - 1.0
    val connectionType: String,                // Kapcsolat típusa
    val distance3D: Float = 0f,                // 3D térbeli távolság
    val semanticSimilarity: Float = 0f,        // Sémantikai hasonlóság
    val creationTime: Long = System.currentTimeMillis(),
    val usageCount: Int = 0                    // Használati gyakoriság
) {
    val isStrong: Boolean get() = strength > 0.7f
    val isWeak: Boolean get() = strength < 0.3f
    val isBidirectional: Boolean = false       // Jelenleg mindegyik irányított
    
    fun getOppositeEnd(entityId: Long): Long {
        return if (fromId == entityId) toId else fromId
    }
    
    fun containsEntity(entityId: Long): Boolean {
        return fromId == entityId || toId == entityId
    }
}
