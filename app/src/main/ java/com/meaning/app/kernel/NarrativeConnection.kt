package com.meaning.app.kernel

data class NarrativeConnection(
    val id: Long,
    val fromId: Long,
    val toId: Long,
    val strength: Float,
    val connectionType: String,
    val distance3D: Float,
    val semanticSimilarity: Float,
    val creationTime: Long,
    val usageCount: Int = 0,
    val isActive: Boolean = true
) {
    fun getOpacity(): Float = strength.coerceIn(0.2f, 1.0f)
    
    // Meghatározza, hogy a két pont vizuálisan mennyire húzza egymást
    fun getTension(): Float = (1.0f - distance3D.coerceIn(0f, 1f)) * strength
}
