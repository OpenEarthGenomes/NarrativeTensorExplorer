package com.meaning.app.ui

import com.meaning.app.db.QuantizedNarrativeEntity
import androidx.compose.ui.geometry.Offset

data class ProjectedPoint(
    val entity: QuantizedNarrativeEntity,
    val position: Offset,                // 2D képernyő koordináták
    val depth: Float,                    // Z koordináta (mélység)
    val density: Float,                  // Sémantikai sűrűség
    val screenRadius: Float = 0f,        // Képernyőn mért sugár
    val isVisible: Boolean = true,       // Látható-e a képernyőn
    val isSelected: Boolean = false,     // Kijelölve van-e
    val original3DPosition: Triple<Float, Float, Float> = Triple(
        entity.coordX, entity.coordY, entity.coordZ
    )
) {
    val distanceFromCenter: Float get() = position.getDistance()
    
    fun getColorBasedOn(family: String): Int {
        return when (family.lowercase()) {
            "természet" -> 0xFF4CAF50
            "érzelem" -> 0xFFF44336
            "absztrakt" -> 0xFF2196F3
            "idő" -> 0xFF9C27B0
            "test" -> 0xFFFF9800
            else -> 0xFF9E9E9E
        }
    }
}
