package com.meaning.app.kernel

import com.meaning.app.db.QuantizedNarrativeEntity

data class SearchResult(
    val entity: QuantizedNarrativeEntity,
    val similarity: Float,
    val distance3D: Float,
    val matchRank: Int = 0,
    val explanation: String = ""
) {
    fun getConfidence(): String {
        return when {
            similarity > 0.9f -> "Kritikus egyezés"
            similarity > 0.7f -> "Erős asszociáció"
            else -> "Gyenge kapcsolódás"
        }
    }
}
